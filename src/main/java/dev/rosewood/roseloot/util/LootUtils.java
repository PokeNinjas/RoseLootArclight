package dev.rosewood.roseloot.util;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.roseloot.RoseLoot;
import java.util.List;
import java.util.Random;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class LootUtils {

    private LootUtils() {

    }

    private static final Random RANDOM = new Random();
    private static final String SPAWN_REASON_METADATA_NAME = "spawn_reason";

    /**
     * Checks if a chance between 0 and 100 passes
     *
     * @param chance The chance
     * @return true if the chance passed, otherwise false
     */
    public static boolean checkChance(double chance) {
        return RANDOM.nextDouble() <= chance;
    }

    /**
     * Gets a random value between the given range, inclusively
     *
     * @param min The minimum value
     * @param max The maximum value
     * @return A value between the min and max, inclusively
     */
    public static int randomInRange(int min, int max) {
        if (min == max)
            return min;

        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }
        return RANDOM.nextInt(max - min + 1) + min;
    }

    /**
     * Sets the spawn reason for the given LivingEntity.
     * Does not overwrite an existing spawn reason.
     *
     * @param entity The entity to set the spawn reason of
     * @param spawnReason The spawn reason to set
     */
    public static void setEntitySpawnReason(LivingEntity entity, SpawnReason spawnReason) {
        RosePlugin rosePlugin = RoseLoot.getInstance();
        if (NMSUtil.getVersionNumber() > 13) {
            PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(rosePlugin, SPAWN_REASON_METADATA_NAME);
            if (!dataContainer.has(key, PersistentDataType.STRING))
                dataContainer.set(key, PersistentDataType.STRING, spawnReason.name());
        } else {
            if (!entity.hasMetadata(SPAWN_REASON_METADATA_NAME))
                entity.setMetadata(SPAWN_REASON_METADATA_NAME, new FixedMetadataValue(rosePlugin, spawnReason.name()));
        }
    }

    /**
     * Gets the spawn reason of the given LivingEntity
     *
     * @param entity The entity to get the spawn reason of
     * @return The SpawnReason, or SpawnReason.CUSTOM if none is saved
     */
    public static SpawnReason getEntitySpawnReason(LivingEntity entity) {
        RosePlugin rosePlugin = RoseLoot.getInstance();
        if (NMSUtil.getVersionNumber() > 13) {
            String reason = entity.getPersistentDataContainer().get(new NamespacedKey(rosePlugin, SPAWN_REASON_METADATA_NAME), PersistentDataType.STRING);
            SpawnReason spawnReason;
            if (reason != null) {
                try {
                    spawnReason = SpawnReason.valueOf(reason);
                } catch (Exception ex) {
                    spawnReason = SpawnReason.CUSTOM;
                }
            } else {
                spawnReason = SpawnReason.CUSTOM;
            }
            return spawnReason;
        } else {
            List<MetadataValue> metaValues = entity.getMetadata(SPAWN_REASON_METADATA_NAME);
            SpawnReason spawnReason = null;
            for (MetadataValue meta : metaValues) {
                try {
                    spawnReason = SpawnReason.valueOf(meta.asString());
                    break;
                } catch (Exception ignored) { }
            }
            return spawnReason != null ? spawnReason : SpawnReason.CUSTOM;
        }
    }

}
