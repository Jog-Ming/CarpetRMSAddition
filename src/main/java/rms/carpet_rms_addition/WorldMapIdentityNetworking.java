package rms.carpet_rms_addition;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class WorldMapIdentityNetworking {
    private WorldMapIdentityNetworking() {
    }

    //#if MC >= 12100
    //$$ public static void registerVoxelMapReceiver() {
    //$$     final Identifier channel = WorldMapIdentityHelper.voxelMapChannel();
    //$$     final net.minecraft.network.packet.CustomPayload.Id<RawCustomPayload> packetId = RawCustomPayload.id(channel);
    //$$     net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(packetId, RawCustomPayload.codec(channel));
    //$$     net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(
    //$$         packetId,
    //$$         (payload, context) -> sendVoxelMapResponse(context.player())
    //$$     );
    //$$ }
    //#endif

    public static boolean handleVoxelMapQuery(
        final ServerPlayerEntity player,
        //#if MC >= 12002
        //$$ final net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket packet
        //#else
        final net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket packet
        //#endif
    ) {
        final Identifier channel;
        //#if MC >= 12002
        //$$ if (!(packet.payload() instanceof RawCustomPayload payload)) return false;
        //$$ channel = payload.channel();
        //#else
        channel = packet.getChannel();
        //#endif
        if (!WorldMapIdentityHelper.voxelMapChannel().equals(channel)) return false;
        sendVoxelMapResponse(player);
        return true;
    }

    public static void sendXaeroWorldInfo(final ServerPlayerEntity player) {
        final String worldId = WorldMapIdentityHelper.resolveWorldId(player.getServerWorld().getServer());
        if (worldId == null) return;
        final byte[] payload = WorldMapIdentityHelper.formatXaeroPayload(worldId);
        send(player, WorldMapIdentityHelper.xaeroMiniMapChannel(), payload);
        send(player, WorldMapIdentityHelper.xaeroWorldMapChannel(), payload);
    }

    private static void sendVoxelMapResponse(final ServerPlayerEntity player) {
        final String worldId = WorldMapIdentityHelper.resolveWorldId(player.getServerWorld().getServer());
        if (worldId == null) return;
        send(player, WorldMapIdentityHelper.voxelMapChannel(), WorldMapIdentityHelper.formatVoxelMapResponse(worldId));
    }

    private static void send(final ServerPlayerEntity player, final Identifier channel, final byte[] bytes) {
        //#if MC >= 12002
        //$$ player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket(new RawCustomPayload(channel, bytes)));
        //#else
        player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket(channel, new PacketByteBuf(Unpooled.wrappedBuffer(bytes))));
        //#endif
    }

}
