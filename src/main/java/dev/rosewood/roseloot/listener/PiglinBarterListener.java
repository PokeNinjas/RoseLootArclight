package dev.rosewood.roseloot.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.roseloot.loot.LootContents;
import dev.rosewood.roseloot.loot.LootResult;
import dev.rosewood.roseloot.loot.OverwriteExisting;
import dev.rosewood.roseloot.loot.context.LootContext;
import dev.rosewood.roseloot.loot.context.LootContextParams;
import dev.rosewood.roseloot.loot.table.LootTableTypes;
import dev.rosewood.roseloot.manager.ConfigurationManager.Setting;
import dev.rosewood.roseloot.manager.LootTableManager;
import java.util.List;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Piglin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.inventory.ItemStack;

public class PiglinBarterListener implements Listener {

    private final LootTableManager lootTableManager;

    public PiglinBarterListener(RosePlugin rosePlugin) {
        this.lootTableManager = rosePlugin.getManager(LootTableManager.class);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPiglinBarter(PiglinBarterEvent event) {
        Piglin piglin = event.getEntity();
        if (Setting.DISABLED_WORLDS.getStringList().stream().anyMatch(x -> x.equalsIgnoreCase(piglin.getWorld().getName())))
            return;

        LootContext lootContext = LootContext.builder()
                .put(LootContextParams.ORIGIN, piglin.getLocation())
                .put(LootContextParams.LOOTED_ENTITY, piglin)
                .put(LootContextParams.INPUT_ITEM, event.getInput())
                .put(LootContextParams.HAS_EXISTING_ITEMS, !event.getOutcome().isEmpty())
                .build();
        LootResult lootResult = this.lootTableManager.getLoot(LootTableTypes.PIGLIN_BARTER, lootContext);
        LootContents lootContents = lootResult.getLootContents();

        // Overwrite existing loot if applicable
        List<ItemStack> outputItems = event.getOutcome();
        if (lootResult.doesOverwriteExisting(OverwriteExisting.ITEMS))
            outputItems.clear();

        // Set items and drop experience
        outputItems.addAll(lootResult.getLootContents().getItems());

        int experience = lootContents.getExperience();
        if (experience > 0)
            piglin.getWorld().spawn(piglin.getLocation(), ExperienceOrb.class, x -> x.setExperience(experience));

        lootContents.triggerExtras(piglin.getLocation());
    }

}
