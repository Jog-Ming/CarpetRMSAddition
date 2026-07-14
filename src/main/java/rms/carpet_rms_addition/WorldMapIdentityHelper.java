package rms.carpet_rms_addition;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.zip.CRC32;

public final class WorldMapIdentityHelper {
    private static final String AUTO = "auto";
    private static final String DISABLED = "false";
    private static final String AUTO_ID_FILE = "carpet-rms-addition-map-world-id.txt";
    private static final byte VOXELMAP_MAGIC = 42;

    private WorldMapIdentityHelper() {
    }

    public static Identifier voxelMapChannel() {
        return identifier("worldinfo", "world_id");
    }

    public static Identifier xaeroWorldMapChannel() {
        return identifier("xaeroworldmap", "main");
    }

    public static Identifier xaeroMiniMapChannel() {
        return identifier("xaerominimap", "main");
    }

    public static String resolveWorldId(final MinecraftServer server) {
        final String ruleValue = CarpetRMSAdditionSettings.separateMapModWorlds;
        if (DISABLED.equals(ruleValue)) return null;
        final String worldId = AUTO.equals(ruleValue) ? getOrCreateAutoWorldId(server) : ruleValue;
        return isValidWorldId(worldId) ? worldId : null;
    }

    public static byte[] formatVoxelMapResponse(final String worldId) {
        final byte[] worldIdBytes = worldId.getBytes(StandardCharsets.UTF_8);
        final byte[] response = new byte[3 + worldIdBytes.length];
        response[0] = 0;
        response[1] = VOXELMAP_MAGIC;
        response[2] = (byte) worldIdBytes.length;
        System.arraycopy(worldIdBytes, 0, response, 3, worldIdBytes.length);
        return response;
    }

    @Deprecated
    public static byte[] formatVoxelMapResponse(final byte[] requestBytes, final String worldId) {
        return formatVoxelMapResponse(worldId);
    }

    public static byte[] formatXaeroPayload(final String worldId) {
        final CRC32 crc32 = new CRC32();
        final byte[] worldIdBytes = worldId.getBytes(StandardCharsets.UTF_8);
        crc32.update(worldIdBytes, 0, worldIdBytes.length);
        final int crc = (int) crc32.getValue();
        return new byte[] {
            0,
            (byte) (crc >>> 24),
            (byte) (crc >>> 16),
            (byte) (crc >>> 8),
            (byte) crc
        };
    }

    private static String getOrCreateAutoWorldId(final MinecraftServer server) {
        final Path path = server.getSavePath(WorldSavePath.ROOT).resolve(AUTO_ID_FILE);
        final String existing = readExistingId(path);
        if (existing != null) return existing;
        final String generated = UUID.randomUUID().toString();
        try {
            Files.write(path, (generated + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
            return generated;
        } catch (final IOException e) {
            return null;
        }
    }

    private static String readExistingId(final Path path) {
        if (!Files.isRegularFile(path)) return null;
        try {
            final String id = Files.readString(path, StandardCharsets.UTF_8).trim();
            return id.isEmpty() ? null : id;
        } catch (final IOException e) {
            return null;
        }
    }

    private static boolean isValidWorldId(final String worldId) {
        return worldId != null && !worldId.isEmpty() && worldId.getBytes(StandardCharsets.UTF_8).length <= 255;
    }

    private static Identifier identifier(final String namespace, final String path) {
        //#if MC >= 12100
        //$$ return Identifier.of(namespace, path);
        //#else
        return new Identifier(namespace, path);
        //#endif
    }
}
