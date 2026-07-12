package rms.carpet_rms_addition.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import rms.carpet_rms_addition.RawCustomPayload;
import rms.carpet_rms_addition.WorldMapIdentityHelper;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;

//#if MC >= 12002
//$$ @Mixin(net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket.class)
//#else
@Pseudo
@Mixin(targets = "net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket")
//#endif
public abstract class MapModCustomPayloadC2SPacketMixin {
    //#if MC >= 12002 && MC < 12100
    //$$ @Redirect(method = "<clinit>", remap = false, at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;"))
    //$$ private static com.google.common.collect.ImmutableMap<Identifier, PacketByteBuf.PacketReader<?>> addVoxelMapReader(final com.google.common.collect.ImmutableMap.Builder<Identifier, PacketByteBuf.PacketReader<?>> builder) {
    //$$     final Identifier channel = WorldMapIdentityHelper.voxelMapChannel();
    //$$     builder.put(channel, buf -> {
    //$$         final byte[] bytes = new byte[buf.readableBytes()];
    //$$         buf.readBytes(bytes);
    //$$         return new RawCustomPayload(channel, bytes);
    //$$     });
    //$$     return builder.build();
    //$$ }
    //#endif

    //#if MC >= 12100
    //$$ @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/CustomPayload;createCodec(Lnet/minecraft/network/packet/CustomPayload$CodecFactory;Ljava/util/List;)Lnet/minecraft/network/codec/PacketCodec;"), index = 1)
    //$$ private static List<net.minecraft.network.packet.CustomPayload.Type<? super net.minecraft.network.PacketByteBuf, ?>> addMapModPayloads(final List<net.minecraft.network.packet.CustomPayload.Type<? super net.minecraft.network.PacketByteBuf, ?>> types) {
    //$$     final List<net.minecraft.network.packet.CustomPayload.Type<? super net.minecraft.network.PacketByteBuf, ?>> mutable = new ArrayList<>(types);
    //$$     mutable.add(RawCustomPayload.type(WorldMapIdentityHelper.voxelMapChannel()));
    //$$     return mutable;
    //$$ }
    //#endif
}
