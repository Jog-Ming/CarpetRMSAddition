package rms.carpet_rms_addition;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;

public final class CarpetRMSAddition implements CarpetExtension, ModInitializer {
    private static final String ID = "carpet-rms-addition";
    private static String name;
    private static String version;
    
    public static String getName() {
        return name;
    }
    
    public static String getVersion() {
        return version;
    }
    
    @Override
    public void onInitialize() {
        final ModMetadata metadata = FabricLoader.getInstance()
            .getModContainer(ID)
            .orElseThrow(IllegalStateException::new)
            .getMetadata();
        name = metadata.getName();
        version = metadata.getVersion().getFriendlyString();
        //#if MC >= 12100
        //$$ WorldMapIdentityNetworking.registerVoxelMapReceiver();
        //#endif
        CarpetServer.manageExtension(new CarpetRMSAddition());
    }
    
    @Override
    public void onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(CarpetRMSAdditionSettings.class);
    }
}
