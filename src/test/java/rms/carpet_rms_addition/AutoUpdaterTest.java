package rms.carpet_rms_addition;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

final class AutoUpdaterTest {
    private static final String VERSIONS_JSON = """
        [
          {"version_number":"v2.3.2-mc1.21.1","version_type":"release","loaders":["fabric"],"game_versions":["1.21.1"],"files":[{"url":"https://cdn/2.3.2.jar","filename":"a-2.3.2.jar","primary":true,"hashes":{"sha1":"aaa"}}]},
          {"version_number":"v2.3.1-mc1.21.1","version_type":"release","loaders":["fabric"],"game_versions":["1.21.1"],"files":[{"url":"https://cdn/2.3.1.jar","filename":"a-2.3.1.jar","primary":true,"hashes":{"sha1":"bbb"}}]},
          {"version_number":"v2.3.2-mc1.21.4","version_type":"release","loaders":["fabric"],"game_versions":["1.21.4"],"files":[{"url":"https://cdn/1.21.4.jar","filename":"a-1.21.4.jar","primary":true,"hashes":{"sha1":"ddd"}}]},
          {"version_number":"v2.3.2","version_type":"release","loaders":["quilt"],"game_versions":["1.21.1"],"files":[{"url":"https://cdn/quilt.jar","filename":"a-quilt.jar","primary":true,"hashes":{"sha1":"eee"}}]}
        ]
        """;
    private static final String BETA_FIRST_JSON = """
        [
          {"version_number":"v2.4.0-beta","version_type":"beta","loaders":["fabric"],"game_versions":["1.21.1"],"files":[{"url":"u","filename":"f","primary":true,"hashes":{"sha1":"x"}}]},
          {"version_number":"v2.3.2-mc1.21.1","version_type":"release","loaders":["fabric"],"game_versions":["1.21.1"],"files":[{"url":"u","filename":"f","primary":true,"hashes":{"sha1":"x"}}]}
        ]
        """;

    @Test
    void extractsSemverFromModrinthVersionNumber() {
        assertEquals("2.3.1", AutoUpdater.extractSemver("v2.3.1-mc1.21.1"));
    }

    @Test
    void extractsPlainSemver() {
        assertEquals("2.3.1", AutoUpdater.extractSemver("2.3.1"));
        assertEquals("3", AutoUpdater.extractSemver("v3"));
    }

    @Test
    void fallsBackToRawWhenNoVersionFound() {
        assertEquals("nightly", AutoUpdater.extractSemver("nightly"));
    }

    @Test
    void detectsNewerVersion() {
        assertEquals(true, AutoUpdater.isNewer("2.3.2", "2.3.1"));
        assertEquals(true, AutoUpdater.isNewer("2.4.0", "2.3.1"));
        assertEquals(true, AutoUpdater.isNewer("3.0.0", "2.3.1"));
    }

    @Test
    void detectsSameOrOlderVersion() {
        assertEquals(false, AutoUpdater.isNewer("2.3.1", "2.3.1"));
        assertEquals(false, AutoUpdater.isNewer("2.3.0", "2.3.1"));
    }

    @Test
    void comparesNumericallyNotLexicographically() {
        // 2.3.10 is newer than 2.3.1; a naive string compare would say "2.3.1" > "2.3.10"
        assertEquals(true, AutoUpdater.isNewer("2.3.10", "2.3.1"));
        assertEquals(false, AutoUpdater.isNewer("2.3.1", "2.3.10"));
    }

    @Test
    void findsLatestFabricReleaseForCurrentMinecraftVersion() {
        final JsonObject latest = AutoUpdater.findLatestRelease(VERSIONS_JSON, "1.21.1");

        assertNotNull(latest);
        assertEquals("v2.3.2-mc1.21.1", latest.get("version_number").getAsString());
    }

    @Test
    void skipsBetaVersionsEvenWhenListedFirst() {
        final JsonObject latest = AutoUpdater.findLatestRelease(BETA_FIRST_JSON, "1.21.1");

        assertNotNull(latest);
        assertEquals("v2.3.2-mc1.21.1", latest.get("version_number").getAsString());
    }

    @Test
    void returnsNullWhenNoReleaseMatchesCurrentMinecraftVersion() {
        assertNull(AutoUpdater.findLatestRelease(VERSIONS_JSON, "1.20.1"));
    }

    @Test
    void picksPrimaryFile() {
        final JsonArray files = new JsonArray();
        files.add(file("https://cdn/secondary.jar", false));
        files.add(file("https://cdn/primary.jar", true));

        final JsonObject file = AutoUpdater.pickFile(files);

        assertNotNull(file);
        assertEquals("https://cdn/primary.jar", file.get("url").getAsString());
    }

    @Test
    void fallsBackToFirstFileWhenNoPrimary() {
        final JsonArray files = new JsonArray();
        files.add(file("https://cdn/only.jar", false));

        final JsonObject file = AutoUpdater.pickFile(files);

        assertNotNull(file);
        assertEquals("https://cdn/only.jar", file.get("url").getAsString());
    }

    @Test
    void returnsNullForNonJsonBody() {
        assertNull(AutoUpdater.findLatestRelease("<html>error page</html>", "1.21.1"));
    }

    @Test
    void skipsMalformedEntriesAndContinuesSearch() {
        final String malformed = """
            [
              {"version_number":"v2.3.2-mc1.21.1","version_type":"release","game_versions":["1.21.1"],"files":[{"url":"u","filename":"f","primary":true,"hashes":{"sha512":"x"}}]},
              {"version_number":"v2.3.3-mc1.21.1","version_type":"release","loaders":["fabric"],"game_versions":["1.21.1"],"files":[{"url":"u","filename":"f","primary":true,"hashes":{"sha512":"x"}}]}
            ]
            """;
        // The first entry is missing "loaders"; it must be skipped, and the complete second entry selected.
        final JsonObject latest = AutoUpdater.findLatestRelease(malformed, "1.21.1");

        assertNotNull(latest);
        assertEquals("v2.3.3-mc1.21.1", latest.get("version_number").getAsString());
    }

    @Test
    void returnsNullWhenFilesArrayIsAbsent() {
        assertNull(AutoUpdater.pickFile(null));
    }

    private static JsonObject file(final String url, final boolean primary) {
        final JsonObject file = new JsonObject();
        file.addProperty("url", url);
        file.addProperty("filename", url.substring(url.lastIndexOf('/') + 1));
        file.addProperty("primary", primary);
        final JsonObject hashes = new JsonObject();
        hashes.addProperty("sha1", "x");
        file.add("hashes", hashes);
        return file;
    }
}
