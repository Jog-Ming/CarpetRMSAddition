package rms.carpet_rms_addition;

import net.minecraft.util.Identifier;

import java.util.Arrays;

//#if MC >= 12002
//$$ public final class RawCustomPayload implements net.minecraft.network.packet.CustomPayload {
//#else
public final class RawCustomPayload {
//#endif
    private final Identifier channel;
    private final byte[] data;

    public RawCustomPayload(final Identifier channel, final byte[] data) {
        this.channel = channel;
        this.data = Arrays.copyOf(data, data.length);
    }

    public Identifier channel() {
        return this.channel;
    }

    public byte[] data() {
        return Arrays.copyOf(this.data, this.data.length);
    }

    //#if MC >= 12002 && MC < 12100
    //$$ @Override
    //$$ public Identifier id() {
    //$$     return this.channel;
    //$$ }
    //$$
    //$$ @Override
    //$$ public void write(final net.minecraft.network.PacketByteBuf buf) {
    //$$     buf.writeBytes(this.data);
    //$$ }
    //#endif

    //#if MC >= 12100
    //$$ @Override
    //$$ public net.minecraft.network.packet.CustomPayload.Id<RawCustomPayload> getId() {
    //$$     return id(this.channel);
    //$$ }
    //$$
    //$$ public static net.minecraft.network.packet.CustomPayload.Id<RawCustomPayload> id(final Identifier channel) {
    //$$     return new net.minecraft.network.packet.CustomPayload.Id<>(channel);
    //$$ }
    //$$
    //$$ public static net.minecraft.network.packet.CustomPayload.Type<net.minecraft.network.PacketByteBuf, RawCustomPayload> type(final Identifier channel) {
    //$$     return new net.minecraft.network.packet.CustomPayload.Type<>(id(channel), codec(channel));
    //$$ }
    //$$
    //$$ public static net.minecraft.network.codec.PacketCodec<net.minecraft.network.PacketByteBuf, RawCustomPayload> codec(final Identifier channel) {
    //$$     return net.minecraft.network.packet.CustomPayload.codecOf(
    //$$         (payload, buffer) -> buffer.writeBytes(payload.data),
    //$$         buffer -> new RawCustomPayload(channel, readBytes(buffer))
    //$$     );
    //$$ }
    //$$
    //$$ private static byte[] readBytes(final net.minecraft.network.PacketByteBuf buffer) {
    //$$     final byte[] bytes = new byte[buffer.readableBytes()];
    //$$     buffer.readBytes(bytes);
    //$$     return bytes;
    //$$ }
    //#endif
}
