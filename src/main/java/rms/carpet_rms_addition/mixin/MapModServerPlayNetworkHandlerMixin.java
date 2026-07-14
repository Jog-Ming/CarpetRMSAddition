package rms.carpet_rms_addition.mixin;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rms.carpet_rms_addition.WorldMapIdentityNetworking;

//#if MC >= 12002
//$$ @Mixin(net.minecraft.server.network.ServerCommonNetworkHandler.class)
//#else
@Mixin(ServerPlayNetworkHandler.class)
//#endif
public abstract class MapModServerPlayNetworkHandlerMixin {
    //#if MC < 12100
    //#if MC < 12002
    @Shadow
    public ServerPlayerEntity player;
    //#endif

    @Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
    private void handleMapModWorldInfoQuery(
        //#if MC >= 12002
        //$$ final net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket packet,
        //#else
        final net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket packet,
        //#endif
        final CallbackInfo ci
    ) {
        //#if MC >= 12002
        //$$ if (!((Object)this instanceof ServerPlayNetworkHandler)) return;
        //$$ final ServerPlayerEntity player = ((ServerPlayNetworkHandler)(Object)this).player;
        //$$ if (WorldMapIdentityNetworking.handleVoxelMapQuery(player, packet)) ci.cancel();
        //#else
        if (WorldMapIdentityNetworking.handleVoxelMapQuery(this.player, packet)) ci.cancel();
        //#endif
    }
    //#endif
}
