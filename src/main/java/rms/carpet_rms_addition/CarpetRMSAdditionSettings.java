package rms.carpet_rms_addition;

import carpet.settings.ParsedRule;
import carpet.settings.Rule;
import carpet.settings.Validator;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.entity.EntityType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.Spawner;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

//#if MC >= 12001
//$$ @SuppressWarnings("removal")
//#endif
public final class CarpetRMSAdditionSettings {
    private static final String RMS = "RMS";
    private static final ReferenceArraySet<ThreadedAnvilChunkStorage> chunkStorages = new ReferenceArraySet<>();
    private static final ReferenceArraySet<Spawner> spawners = new ReferenceArraySet<>();
    @SuppressWarnings("unused")
    @Rule(desc = "Override the block light level when spawning monsters", category = { RMS }, options = { "false", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" }, validate = OverrideBlockLightLevelValidator.class)
    public static String overrideMonsterBlockLightLevel = "false";
    @SuppressWarnings("unused")
    @Rule(desc = "Override the sky light level when spawning monsters", category = { RMS }, options = { "false", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" }, validate = OverrideSkyLightLevelValidator.class)
    public static String overrideMonsterSkyLightLevel = "false";
    @Rule(desc = "Make /data get return more nbt", category = { RMS })
    public static boolean enhancedDataGet = false;
    @Rule(desc = "Automatically apply a newer release found on Modrinth by replacing the jar on Linux/macOS (next restart). Update checks always run regardless of this setting; when false a notice is logged instead", category = { RMS })
    public static boolean autoUpdate = false;
    @SuppressWarnings("unused")
    @Rule(desc = "A list of entities, in the form of [minecraft:boat,minecraft:creeper], for each the server will not send update packets", category = { RMS }, validate = InterceptUpdatePacketEntitiesValidator.class)
    public static String interceptUpdatePacketEntities = "[]";
    @SuppressWarnings("unused")
    @Rule(desc = "A list of entities, in the form of [minecraft:boat,minecraft:creeper], for each the server will not send all packets and immediately remove from client. This takes precedence over interceptUpdatePacketEntities", category = { RMS }, validate = InterceptAllPacketEntitiesValidator.class)
    public static String interceptAllPacketEntities = "[]";
    @SuppressWarnings("unused")
    @Rule(desc = "A list of entities, in the form of [minecraft:cat,minecraft:creeper], that will not naturally spawn", category = { RMS }, validate = NaturalSpawnBlacklistValidator.class)
    public static String naturalSpawnBlacklist = "[]";
    @SuppressWarnings("unused")
    @Rule(desc = "A list of entities, in the form of [minecraft:boat,minecraft:creeper], that will not go through portals", category = { RMS }, validate = UsePortalBlacklistValidator.class)
    public static String usePortalBlacklist = "[]";
    @Rule(desc = "Stops all particle packets from being sent", category = { RMS })
    public static boolean interceptParticlePackets = false;
    @SuppressWarnings("unused")
    @Rule(desc = "Send a stable world identity to VoxelMap and Xaero's Map to keep maps from different worlds on the same server address, seed, mirror, or survival server separated", category = { RMS }, options = { "false", "auto" }, strict = false, validate = MapModWorldIdentityValidator.class)
    public static String separateMapModWorlds = "false";
    //#if MC < 11802
    @Rule(desc = "Port the behavior of falling blocks from 1.18.2+", category = { RMS })
    public static boolean fallingBlockBackport = false;
    //#endif
    //#if MC < 12101
    @Rule(desc = "Use offline UUID for fake players instead of querying Mojang API. Prevents server freeze when API is slow or unreachable", category = { RMS })
    public static boolean fakePlayerOfflineUUID = false;
    //#endif
    @Rule(desc = "Forces dispensers, droppers, and note blocks to check their block state when placed by a player", extra = { "When these blocks are placed by a player, they normally do not check if they are triggered by a redstone signal, and stay in BUD state.", "On §ntrigger§r, they will recieve a neighbor update when placed by a player.", "On §nsilent§r, they will not trigger but correct their block state silently.", "(In these two modes, note blocks will differ by playing a sound or not.)" }, category = { RMS }, options = { "false", "trigger", "silent" })
    public static String selfCheckOnPlacement = "false";
    //#if MC < 11903
    @Rule(desc = "Ports the behavior of fireball explosions from 1.19.3+, where they give credit to itself (and its owner) when creating an explosion", category = { RMS })
    public static boolean fireballExplosionCreditBackport = false;
    //#endif
    //#if MC < 12100
    @Rule(desc = "Ports the behavior of end portals from 1.21 pre1, where the end platform drops items when breaking blocks", category = { RMS })
    public static boolean endPlatformBreakingBackport = false;
    //#endif
    
    
    private static int blockLightLevel = -1;
    private static int skyLightLevel = -1;
    private static boolean keepBlockLightLevel = true;
    private static boolean keepSkyLightLevel = true;
    private static ReferenceArraySet<EntityType<?>> interceptUpdatePacketsEntityTypes = new ReferenceArraySet<>();
    private static ReferenceArraySet<EntityType<?>> interceptAllPacketsEntityTypes = new ReferenceArraySet<>();
    private static ReferenceArraySet<EntityType<?>> naturalSpawnBlacklistEntityTypes = new ReferenceArraySet<>();
    private static ReferenceArraySet<EntityType<?>> usePortalBlacklistEntityTypes = new ReferenceArraySet<>();
    
    public static int getBlockLightLevel() {
        return blockLightLevel;
    }
    
    public static int getSkyLightLevel() {
        return skyLightLevel;
    }
    
    public static boolean isKeepingBlockLightLevel() {
        return keepBlockLightLevel;
    }
    
    public static boolean isKeepingSkyLightLevel() {
        return keepSkyLightLevel;
    }
    
    public static void registerChunkStorage(final ThreadedAnvilChunkStorage chunkStorage) {
        chunkStorages.add(chunkStorage);
    }
    
    public static void registerSpawners(final List<Spawner> spawners) {
        CarpetRMSAdditionSettings.spawners.addAll(spawners);
    }
    
    public static ReferenceArraySet<EntityType<?>> getInterceptUpdatePacketsEntityTypes() {
        return interceptUpdatePacketsEntityTypes;
    }
    
    public static ReferenceArraySet<EntityType<?>> getInterceptAllPacketsEntityTypes() {
        return interceptAllPacketsEntityTypes;
    }
    
    public static ReferenceArraySet<EntityType<?>> getNaturalSpawnBlacklistEntityTypes() {
        return naturalSpawnBlacklistEntityTypes;
    }
    
    public static ReferenceArraySet<EntityType<?>> getUsePortalBlacklistEntityTypes() {
        return usePortalBlacklistEntityTypes;
    }
    
    private static int parseLightLevel(final String lightLevel) {
        return switch (lightLevel) {
            case "0" -> 0;
            case "1" -> 1;
            case "2" -> 2;
            case "3" -> 3;
            case "4" -> 4;
            case "5" -> 5;
            case "6" -> 6;
            case "7" -> 7;
            case "8" -> 8;
            case "9" -> 9;
            case "10" -> 10;
            case "11" -> 11;
            case "12" -> 12;
            case "13" -> 13;
            case "14" -> 14;
            case "15" -> 15;
            default -> -1;
        };
    }
    
    private static class OverrideBlockLightLevelValidator extends Validator<String> {
        @Override
        public String validate(final ServerCommandSource source, final ParsedRule<String> currentRule, final String newValue, final String string) {
            blockLightLevel = parseLightLevel(newValue);
            keepBlockLightLevel = blockLightLevel == -1;
            return newValue;
        }
    }
    
    private static class OverrideSkyLightLevelValidator extends Validator<String> {
        @Override
        public String validate(final ServerCommandSource source, final ParsedRule<String> currentRule, final String newValue, final String string) {
            skyLightLevel = parseLightLevel(newValue);
            keepSkyLightLevel = skyLightLevel == -1;
            return newValue;
        }
    }

    private static class MapModWorldIdentityValidator extends Validator<String> {
        @Override
        public String validate(final ServerCommandSource source, final ParsedRule<String> currentRule, final String newValue, final String string) {
            if (newValue.equals("false") || newValue.equals("auto")) return newValue;
            if (newValue.isEmpty()) return null;
            return newValue.getBytes(StandardCharsets.UTF_8).length > 255 ? null : newValue;
        }
    }
    
    private abstract static class EntityListValidator extends Validator<String> {
        @Override
        public String validate(final ServerCommandSource source, final ParsedRule<String> currentRule, final String newValue, final String string) {
            final String values = newValue.replaceAll("\\s", "");
            if (values.equals("[]")) {
                this.update(new ReferenceArraySet<>());
                return "[]";
            }
            final int i = values.length() - 1;
            if (values.charAt(0) != '[' || values.charAt(i) != ']') return null;
            final ReferenceArraySet<EntityType<?>> entityTypes = new ReferenceArraySet<>();
            final StringBuilder stringBuilder = new StringBuilder("[");
            boolean first = true;
            for (final String value : values.substring(1, i).split(",")) {
                final Identifier identifier = Identifier.tryParse(value);
                if (identifier == null) return null;
                //#if MC < 12001
                final Optional<EntityType<?>> optionalEntityType = net.minecraft.util.registry.Registry.ENTITY_TYPE.getOrEmpty(identifier);
                //#elseif MC < 12104
                //$$ final Optional<EntityType<?>> optionalEntityType = net.minecraft.registry.Registries.ENTITY_TYPE.getOrEmpty(identifier);
                //#else
                //$$ final Optional<EntityType<?>> optionalEntityType = net.minecraft.registry.Registries.ENTITY_TYPE.getOptionalValue(identifier);
                //#endif
                if (optionalEntityType.isEmpty()) return null;
                final EntityType<?> entityType = optionalEntityType.get();
                if (!entityTypes.add(entityType)) continue;
                if (first) {
                    stringBuilder.append(identifier);
                    first = false;
                } else stringBuilder.append(',').append(identifier);
            }
            this.update(entityTypes);
            return stringBuilder.append(']').toString();
        }
        
        protected abstract void update(final ReferenceArraySet<EntityType<?>> entityTypes);
    }
    
    private static class InterceptUpdatePacketEntitiesValidator extends EntityListValidator {
        @Override
        protected void update(final ReferenceArraySet<EntityType<?>> entityTypes) {
            interceptUpdatePacketsEntityTypes = entityTypes;
            for (final ThreadedAnvilChunkStorage chunkStorage : chunkStorages)
                for (final ThreadedAnvilChunkStorage.EntityTracker entityTracker : chunkStorage.entityTrackers.values())
                    ((UpdateEntityPacketInterceptor) entityTracker.entry).updateInterceptUpdatePacketsEntityTypes(entityTypes);
        }
    }
    
    private static class InterceptAllPacketEntitiesValidator extends EntityListValidator {
        @Override
        protected void update(final ReferenceArraySet<EntityType<?>> entityTypes) {
            interceptAllPacketsEntityTypes = entityTypes;
            for (final ThreadedAnvilChunkStorage chunkStorage : chunkStorages)
                for (final ThreadedAnvilChunkStorage.EntityTracker entityTracker : chunkStorage.entityTrackers.values()) {
                    ((AllEntityPacketInterceptor) entityTracker).updateInterceptAllPacketsEntityTypes(entityTypes);
                    ((AllEntityPacketInterceptor) entityTracker.entry).updateInterceptAllPacketsEntityTypes(entityTypes);
                }
        }
    }
    
    private static class NaturalSpawnBlacklistValidator extends EntityListValidator {
        @Override
        protected void update(final ReferenceArraySet<EntityType<?>> entityTypes) {
            naturalSpawnBlacklistEntityTypes = entityTypes;
            for (final Spawner spawner : spawners)
                ((NaturalSpawnBlacklistEnforcer) spawner).updateNaturalSpawnBlacklist(entityTypes);
        }
    }
    
    private static class UsePortalBlacklistValidator extends EntityListValidator {
        @Override
        protected void update(final ReferenceArraySet<EntityType<?>> entityTypes) {
            usePortalBlacklistEntityTypes = entityTypes;
            for (final ThreadedAnvilChunkStorage chunkStorage : chunkStorages)
                for (final ThreadedAnvilChunkStorage.EntityTracker entityTracker : chunkStorage.entityTrackers.values())
                    ((UsePortalBlacklistEnforcer) entityTracker).updateUsePortalBlacklist(entityTypes);
        }
    }
}
