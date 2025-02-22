package dev.rosewood.roseloot.loot;

import dev.rosewood.roseloot.loot.condition.LootCondition;
import dev.rosewood.roseloot.loot.context.LootContext;
import dev.rosewood.roseloot.loot.item.ItemLootItem;
import dev.rosewood.roseloot.loot.item.LootItem;
import dev.rosewood.roseloot.provider.NumberProvider;
import dev.rosewood.roseloot.util.RandomCollection;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.inventory.ItemStack;

public class LootEntry implements LootContentsPopulator {

    private final List<LootCondition> conditions;
    private final NumberProvider weight;
    private final NumberProvider quality;
    private final List<LootItem> lootItems;

    private final ChildrenStrategy childrenStrategy;
    private final List<LootEntry> children;

    public LootEntry(List<LootCondition> conditions, NumberProvider weight, NumberProvider quality, List<LootItem> lootItems, ChildrenStrategy childrenStrategy, List<LootEntry> children) {
        this.conditions = conditions;
        this.weight = weight;
        this.quality = quality;
        this.lootItems = lootItems;
        this.childrenStrategy = childrenStrategy;
        this.children = children;
    }

    @Override
    public void populate(LootContext context, LootContents contents) {
        contents.add(this.lootItems);

        if (this.children != null && this.childrenStrategy != null) {
            switch (this.childrenStrategy) {
                case NORMAL -> {
                    List<LootEntry> unweightedEntries = new ArrayList<>();
                    RandomCollection<LootEntry> randomEntries = new RandomCollection<>();
                    for (LootEntry child : this.children) {
                        if (child.isWeighted()) {
                            // If weighted, add to the random entries if it passes conditions
                            if (!child.check(context))
                                continue;

                            randomEntries.add(child.getWeight(context), child);
                        } else {
                            // Otherwise, generate it right away
                            unweightedEntries.add(child);
                        }
                    }

                    if (!randomEntries.isEmpty())
                        randomEntries.next().populate(context, contents);

                    for (LootEntry entry : unweightedEntries)
                        if (entry.check(context))
                            entry.populate(context, contents);
                }
                case SEQUENTIAL -> {
                    for (LootEntry child : this.children) {
                        if (!child.check(context))
                            break;

                        child.populate(context, contents);
                    }
                }
                case FIRST_PASSING -> {
                    for (LootEntry child : this.children) {
                        if (child.check(context)) {
                            child.populate(context, contents);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<ItemStack> getAllItems(LootContext context) {
        List<ItemStack> items = new ArrayList<>();
        if (this.children != null)
            items.addAll(this.children.stream().flatMap(x -> x.getAllItems(context).stream()).toList());
        for (LootItem lootItem : this.lootItems)
            if (lootItem instanceof ItemLootItem itemLootItem)
                items.addAll(itemLootItem.getAllItems(context));
        return items;
    }

    @Override
    public boolean check(LootContext context) {
        return this.conditions.stream().allMatch(x -> x.check(context));
    }

    /**
     * Gets the weight of this entry taking the quality into account
     *
     * @param context The LootContext
     * @return the weight of this entry
     */
    public double getWeight(LootContext context) {
        return this.weight.getDouble(context) + this.quality.getDouble(context) * context.getLuckLevel();
    }

    /**
     * @return true if this entry is weighted
     */
    public boolean isWeighted() {
        return this.weight != null;
    }

    /**
     * The strategy to use when evaluating a LootEntry's children
     */
    public enum ChildrenStrategy {
        NORMAL,        // Process as if this is a LootPool with a single roll and no bonuses
        SEQUENTIAL,    // Keep processing children until a child does not pass conditions
        FIRST_PASSING; // Keep attempting to process children until one passes conditions, then stop

        public static ChildrenStrategy fromString(String name) {
            for (ChildrenStrategy value : values())
                if (value.name().toLowerCase().equals(name))
                    return value;
            return NORMAL;
        }
    }

}
