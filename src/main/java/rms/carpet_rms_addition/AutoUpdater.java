package rms.carpet_rms_addition;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks Modrinth for a newer release of this mod once a dedicated server has finished loading. On POSIX systems
 * (Linux, macOS) the running jar can be replaced while the server keeps running: the JVM holds the old jar's file
 * descriptor, so renaming or replacing it succeeds and the new jar only takes effect on the next restart. Windows
 * locks files that are open, so on Windows (and whenever the jar cannot be resolved or replaced) we only log a
 * notice so an administrator can update manually.
 */
public final class AutoUpdater {
    private static final Logger LOGGER = LoggerFactory.getLogger("Carpet RMS Addition");
    private static final String PROJECT_ID = "ij9knTzG";
    private static final String VERSIONS_URL = "https://api.modrinth.com/v2/project/" + PROJECT_ID + "/version";
    private static final String PROJECT_URL = "https://modrinth.com/project/" + PROJECT_ID;
    private static final long MAX_DOWNLOAD_BYTES = 64L * 1024 * 1024;
    private static final Pattern SEMVER_PATTERN = Pattern.compile("v?(\\d+(?:\\.\\d+)*)");
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    private AutoUpdater() {
    }

    public static void checkIfNeeded() {
        // Only dedicated servers: integrated servers run on clients whose mods folder is managed by a launcher.
        // The check itself always runs here; the autoUpdate rule only gates the automatic jar replacement.
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.SERVER) return;
        if (!STARTED.compareAndSet(false, true)) return;
        final Thread thread = new Thread(AutoUpdater::run, "CarpetRMSAddition-AutoUpdater");
        thread.setDaemon(true);
        thread.start();
    }

    private static void run() {
        try {
            final String currentVersion = CarpetRMSAddition.getVersion();
            final String minecraftVersion = FabricLoader.getInstance()
                .getModContainer("minecraft")
                .orElseThrow(IllegalStateException::new)
                .getMetadata()
                .getVersion()
                .getFriendlyString();
            final JsonObject latest = findLatestRelease(httpGet(VERSIONS_URL), minecraftVersion);
            if (latest == null) {
                LOGGER.debug("[AutoUpdater] No compatible release found on Modrinth for MC {}", minecraftVersion);
                return;
            }
            final String latestRaw = text(latest, "version_number");
            if (latestRaw == null || !isNewer(extractSemver(latestRaw), currentVersion)) {
                LOGGER.debug("[AutoUpdater] Up to date ({}).", currentVersion);
                return;
            }
            final JsonObject file = pickFile(array(latest, "files"));
            if (file == null) {
                LOGGER.warn("[AutoUpdater] A new version {} is available (current {}), but no downloadable file was listed.", latestRaw, currentVersion);
                return;
            }
            final String downloadUrl = text(file, "url");
            final String filename = text(file, "filename");
            final JsonObject hashes = file.has("hashes") && file.get("hashes").isJsonObject() ? file.getAsJsonObject("hashes") : null;
            final String expectedSha512 = hashes != null ? text(hashes, "sha512") : null;
            if (downloadUrl == null || filename == null || expectedSha512 == null) {
                LOGGER.warn("[AutoUpdater] A new version {} is available (current {}), but its file metadata was malformed. Download: {}", latestRaw, currentVersion, PROJECT_URL);
                return;
            }
            final boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("win");
            final Path currentJar = locateCurrentJar();
            final boolean canReplace = CarpetRMSAdditionSettings.autoUpdate && !windows && currentJar != null;
            if (!canReplace) {
                final String reason;
                if (!CarpetRMSAdditionSettings.autoUpdate) {
                    reason = " Auto-replace is disabled (set the autoUpdate rule to true to enable it).";
                } else if (windows) {
                    reason = " Auto-replace is not supported on Windows while the server is running.";
                } else {
                    reason = " The active jar could not be located, so it cannot be auto-replaced.";
                }
                LOGGER.warn("[AutoUpdater] A new version {} is available (current {}).{} Download it manually and restart: {}", latestRaw, currentVersion, reason, downloadUrl);
                return;
            }
            replaceJar(currentJar, downloadUrl, filename, expectedSha512, latestRaw, currentVersion);
        } catch (final Exception exception) {
            LOGGER.warn("[AutoUpdater] Failed to check for updates", exception);
        }
    }

    static JsonObject findLatestRelease(final String body, final String minecraftVersion) {
        final JsonArray versions;
        try {
            final JsonElement parsed = new Gson().fromJson(body, JsonElement.class);
            if (parsed == null || !parsed.isJsonArray()) return null;
            versions = parsed.getAsJsonArray();
        } catch (final Exception exception) {
            return null;
        }
        for (final JsonElement element : versions) {
            if (element == null || !element.isJsonObject()) continue;
            final JsonObject version = element.getAsJsonObject();
            if (!"release".equals(text(version, "version_type"))) continue;
            if (!containsString(array(version, "loaders"), "fabric")) continue;
            if (!containsString(array(version, "game_versions"), minecraftVersion)) continue;
            return version;
        }
        return null;
    }

    static JsonObject pickFile(final JsonArray files) {
        JsonObject fallback = null;
        if (files == null) return null;
        for (final JsonElement element : files) {
            if (element == null || !element.isJsonObject()) continue;
            final JsonObject file = element.getAsJsonObject();
            if (file.has("primary") && file.get("primary").getAsBoolean()) return file;
            if (fallback == null) fallback = file;
        }
        return fallback;
    }

    private static boolean containsString(final JsonArray array, final String target) {
        if (array == null) return false;
        for (final JsonElement element : array) {
            if (element != null && element.isJsonPrimitive() && target.equals(element.getAsString())) return true;
        }
        return false;
    }

    private static String text(final JsonObject object, final String key) {
        if (object == null || !object.has(key) || !object.get(key).isJsonPrimitive()) return null;
        return object.get(key).getAsString();
    }

    private static JsonArray array(final JsonObject object, final String key) {
        if (object == null || !object.has(key) || !object.get(key).isJsonArray()) return null;
        return object.getAsJsonArray(key);
    }

    static String extractSemver(final String raw) {
        final Matcher matcher = SEMVER_PATTERN.matcher(raw);
        return matcher.find() ? matcher.group(1) : raw;
    }

    static boolean isNewer(final String latestSemver, final String currentVersion) {
        return compareSemver(latestSemver, extractSemver(currentVersion)) > 0;
    }

    private static int compareSemver(final String left, final String right) {
        final String[] leftParts = left.split("\\.");
        final String[] rightParts = right.split("\\.");
        final int length = Math.max(leftParts.length, rightParts.length);
        for (int index = 0; index < length; index++) {
            final int leftValue = index < leftParts.length ? parseInteger(leftParts[index]) : 0;
            final int rightValue = index < rightParts.length ? parseInteger(rightParts[index]) : 0;
            if (leftValue != rightValue) return Integer.compare(leftValue, rightValue);
        }
        return 0;
    }

    private static int parseInteger(final String value) {
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException exception) {
            return 0;
        }
    }

    private static Path locateCurrentJar() {
        final List<Path> roots = FabricLoader.getInstance()
            .getModContainer(CarpetRMSAddition.getId())
            .orElseThrow(IllegalStateException::new)
            .getRootPaths();
        for (final Path path : roots) {
            if (!"file".equals(path.getFileSystem().provider().getScheme())) continue;
            final Path absolute = path.toAbsolutePath();
            final Path fileName = absolute.getFileName();
            if (fileName == null) continue;
            if (fileName.toString().endsWith(".jar") && Files.exists(absolute) && !Files.isDirectory(absolute)) {
                return absolute;
            }
        }
        return null;
    }

    private static void replaceJar(final Path currentJar, final String downloadUrl, final String filename, final String expectedSha512, final String latestRaw, final String currentVersion) {
        if (filename.isEmpty() || filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
            LOGGER.warn("[AutoUpdater] A new version {} is available (current {}), but the reported filename is unsafe ({}). Download it manually and restart: {}", latestRaw, currentVersion, filename, downloadUrl);
            return;
        }
        final Path modsDir = currentJar.getParent();
        final Path target = modsDir.resolve(filename).toAbsolutePath();
        final Path downloadTmp = modsDir.resolve(filename + ".part").toAbsolutePath();
        final boolean sameName = target.equals(currentJar.toAbsolutePath());
        // Fixed backup name so successive updates overwrite instead of accumulating one .bak per version.
        final Path backup = modsDir.resolve(CarpetRMSAddition.getId() + ".jar.bak");
        try {
            downloadFile(downloadUrl, downloadTmp);
            final String actualSha512 = sha512(downloadTmp);
            if (!expectedSha512.equalsIgnoreCase(actualSha512)) {
                throw new IOException("SHA-512 mismatch: expected " + expectedSha512 + ", got " + actualSha512);
            }
            // Place the new jar first. On POSIX it either coexists with the in-use jar (different name) or atomically
            // replaces it (same name); in both cases the JVM keeps reading the old jar through its open descriptor.
            Files.move(downloadTmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            // Only now retire the old jar, and only if the new one landed under a different name. This order means a
            // crash mid-update leaves at worst two jars on disk (old + new) rather than zero.
            if (!sameName) {
                Files.move(currentJar, backup, StandardCopyOption.REPLACE_EXISTING);
            }
            LOGGER.info("[AutoUpdater] Updated from {} to {}.{} Restart the server to apply the update.",
                currentVersion, latestRaw, sameName ? "" : " The previous jar was backed up as " + backup.getFileName() + ".");
        } catch (final Exception exception) {
            deleteQuietly(downloadTmp);
            // Remove a half-placed new jar (different name) so Fabric does not load two copies of the mod next restart.
            if (!sameName) deleteQuietly(target);
            LOGGER.warn("[AutoUpdater] A new version {} is available (current {}), but the jar could not be replaced automatically ({}). The running jar is unchanged. Download it manually and restart: {}", latestRaw, currentVersion, exception.toString(), downloadUrl);
        }
    }

    private static String httpGet(final String url) throws IOException, InterruptedException {
        final HttpRequest request = baseRequest(url).header("Accept", "application/json").GET().build();
        final HttpResponse<String> response = httpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new IOException("HTTP " + response.statusCode() + " from " + url);
        return response.body();
    }

    private static void downloadFile(final String url, final Path destination) throws IOException, InterruptedException {
        final HttpRequest request = baseRequest(url).GET().build();
        final HttpResponse<InputStream> response = httpClient().send(request, HttpResponse.BodyHandlers.ofInputStream());
        try (final InputStream input = response.body()) {
            if (response.statusCode() != 200) {
                throw new IOException("HTTP " + response.statusCode() + " downloading " + url);
            }
            try (final OutputStream output = Files.newOutputStream(destination, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                final byte[] buffer = new byte[8192];
                long total = 0;
                int read;
                while ((read = input.read(buffer)) != -1) {
                    total += read;
                    if (total > MAX_DOWNLOAD_BYTES) {
                        throw new IOException("Download from " + url + " exceeded " + MAX_DOWNLOAD_BYTES + " bytes; aborting");
                    }
                    output.write(buffer, 0, read);
                }
            }
        }
    }

    private static HttpRequest.Builder baseRequest(final String url) {
        return HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(30))
            .header("User-Agent", "CarpetRMSAddition/" + CarpetRMSAddition.getVersion() + " (+" + PROJECT_URL + ")");
    }

    private static HttpClient httpClient() {
        return HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    }

    private static String sha512(final Path path) throws IOException, NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-512");
        try (final InputStream input = Files.newInputStream(path)) {
            final byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        final byte[] bytes = digest.digest();
        final StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (final byte value : bytes) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }

    private static void deleteQuietly(final Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (final IOException ignored) {
        }
    }
}
