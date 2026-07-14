package rms.carpet_rms_addition;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

final class WorldMapIdentityHelperTest {
    @Test
    void formatsVoxelMapResponseWithModernFrame() {
        final String worldId = "world-id";

        final byte[] response = WorldMapIdentityHelper.formatVoxelMapResponse(worldId);

        assertArrayEquals(
            new byte[] { 0, 42, 8, 'w', 'o', 'r', 'l', 'd', '-', 'i', 'd' },
            response
        );
    }

    @Test
    void formatsLengthFortyTwoWithoutLegacyAmbiguity() {
        final String worldId = "123456789012345678901234567890123456789012";
        final byte[] worldIdBytes = worldId.getBytes(StandardCharsets.UTF_8);
        final byte[] expected = new byte[3 + worldIdBytes.length];
        expected[0] = 0;
        expected[1] = 42;
        expected[2] = 42;
        System.arraycopy(worldIdBytes, 0, expected, 3, worldIdBytes.length);

        final byte[] response = WorldMapIdentityHelper.formatVoxelMapResponse(worldId);

        assertArrayEquals(expected, response);
    }
}
