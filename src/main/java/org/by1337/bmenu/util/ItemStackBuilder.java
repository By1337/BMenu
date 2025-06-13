package org.by1337.bmenu.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Arrow;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.by1337.blib.BLib;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.chat.util.Message;
import org.by1337.blib.inventory.FastItemMutator;
import org.by1337.blib.inventory.LegacyFastItemMutator;
import org.by1337.blib.nbt.NBT;
import org.by1337.blib.nbt.NbtType;
import org.by1337.blib.nbt.impl.CompoundTag;
import org.by1337.blib.nbt.impl.StringNBT;
import org.by1337.blib.util.Version;
import org.by1337.bmenu.MenuItemBuilder;
import org.by1337.bmenu.hook.ItemStackCreator;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ItemStackBuilder {
    private static final boolean IS_LEGACY = Version.is1_20_4orOlder();
    private static final LegacyFastItemMutator LEGACY_MUTATOR = BLib.getApi().getUnsafe().getLegacyFastItemMutator();
    private static final FastItemMutator MUTATOR = BLib.getApi().getUnsafe().getFastItemMutator();

    private final MenuItemBuilder builder;

    private Object cached;
    private boolean fullCached;
    private boolean displayCached;
    private final CachedComponents cachedComponents;

    public ItemStackBuilder(MenuItemBuilder builder) {
        this.builder = builder;
        cachedComponents = new CachedComponents();
        if (canBeCached(builder.material())) {
            ItemStack itemStack = ItemStackCreator.getItem(builder.material());
            buildBase(itemStack);
            cached = toNms(itemStack);
            buildNMS(cached);
            if (isDisplayCashed()) {
                setDisplay(cached, s -> s, BLib.getApi().getMessage(), builder.lore(), builder.name(), s -> s);
                displayCached = true;
                if (itemStack.hasItemMeta() && canBeCached(builder.amount()) && canBeCached(builder.damage())) {
                    setAmountAndDamage(cached, s -> s, s -> s);
                    fullCached = true;
                }
            }
        }
    }

    private static Object toNms(ItemStack itemStack) {
        if (IS_LEGACY) {
            return LEGACY_MUTATOR.asNMSCopyItemStack(itemStack);
        }
        return MUTATOR.asNMSCopyItemStack(itemStack);
    }

    private static ItemStack asBukkitMirror(Object itemStack) {
        if (IS_LEGACY) {
            return LEGACY_MUTATOR.asBukkitMirror(itemStack);
        }
        return MUTATOR.asBukkitMirror(itemStack);
    }

    private static Object cloneNms(Object itemStack) {
        if (IS_LEGACY) {
            return LEGACY_MUTATOR.cloneNMSItemStack(itemStack);
        }
        return MUTATOR.cloneNMSItemStack(itemStack);
    }

    private static void setCount(Object itemStack, int count) {
        if (IS_LEGACY) {
            LEGACY_MUTATOR.setCount(count, itemStack);
        }else {
            MUTATOR.setCount(count, itemStack);
        }
    }

    private static void setDamage(Object itemStack, int count) {
        if (IS_LEGACY) {
            LEGACY_MUTATOR.setDamage(count, itemStack);
        }else {
            MUTATOR.setInt(FastItemMutator.DAMAGE, count, itemStack);
        }
    }

    private boolean isDisplayCashed() {
        if (builder.name() != null && !canBeCached(builder.name())) {
            return false;
        }
        return builder.lore().stream().allMatch(ItemStackBuilder::canBeCached);
    }

    public ItemStack build(@Nullable ItemStack i, Message message, Placeholderable placeholderable, @Nullable MenuPlaceholders placeholders) {
        if (i == null && fullCached) return asBukkitMirror(cached);
        Object itemStack;
        if (i != null) {
            ItemStack item = i.clone();
            buildBase(item);
            itemStack = toNms(item);
        } else if (cached != null) {
            itemStack = cloneNms(cached);
        } else {
            ItemStack item = ItemStackCreator.getItem(placeholderable.replace(builder.material()));
            buildBase(item);
            itemStack = toNms(item);
        }
        if (!displayCached)
            setDisplay(itemStack, placeholderable, message, builder.lore(), builder.name(), placeholders == null ? s -> s : placeholders);
        setAmountAndDamage(itemStack, placeholderable, placeholders == null ? s -> s : placeholders);
        buildNMS(itemStack);

        return asBukkitMirror(itemStack);
    }

    private void setAmountAndDamage(Object item, Placeholderable placeholderable, Placeholderable prePlaceholder) {
        if (builder.amount() != null) {
            setCount(item, Integer.parseInt(placeholderable.replace(prePlaceholder.replace(builder.amount()))));
        }
        if (builder.damage() != null) {
            setDamage(item, Integer.parseInt(placeholderable.replace(prePlaceholder.replace(builder.damage()))));
        }
    }

    private static CompoundTag getLegacyDisplay(Object item) {
        NBT nbt = LEGACY_MUTATOR.getNBT(item, LegacyFastItemMutator.DISPLAY);
        return nbt instanceof CompoundTag ? (CompoundTag) nbt : new CompoundTag();
    }

    private void setDisplay(Object item, Placeholderable placeholderable, Message message, List<String> lore, @Nullable String name, Placeholderable prePlaceholder) {
        if (IS_LEGACY) {
            CompoundTag display = getLegacyDisplay(item);
            if (name != null) {
                String name0 = prePlaceholder.replace(name);
                CachedComponent cached = cachedComponents.getCached(name0);
                if (cached != null) {
                    display.putString(LegacyFastItemMutator.NAME, cached.getCachedJson());
                } else {
                    String json = GsonComponentSerializer.gson().serialize(message.componentBuilder(placeholderable.replace(name0)).decoration(TextDecoration.ITALIC, false));
                    display.putString(LegacyFastItemMutator.NAME, json);
                }
            }
            List<String> lore0;
            if (display.has(LegacyFastItemMutator.LORE, NbtType.LIST)) {
                lore0 = display.getAsList(LegacyFastItemMutator.LORE, StringNBT.class, StringNBT::getValue);
            } else {
                lore0 = new ArrayList<>();
            }
            for (String s : lore) {
                String line = prePlaceholder.replace(s);
                loreBuilder(line, line1 -> {
                    CachedComponent cached = cachedComponents.getCached(line1);
                    if (cached != null) {
                        lore0.add(cached.getCachedJson());
                    } else {
                        loreBuilder(placeholderable.replace(line1),
                                s1 -> lore0.add(
                                        GsonComponentSerializer.gson().serialize(
                                                message.componentBuilder(s1).decoration(TextDecoration.ITALIC, false)
                                        )
                                )
                        );
                    }
                });
            }
            display.putList(LegacyFastItemMutator.LORE, lore0, StringNBT::new);
            LEGACY_MUTATOR.setNBT(item, LegacyFastItemMutator.DISPLAY, display);
        } else {
            if (name != null) {
                String name0 = prePlaceholder.replace(name);
                CachedComponent cached = cachedComponents.getCached(name0);
                if (cached != null) {
                    MUTATOR.setComponent(FastItemMutator.CUSTOM_NAME, cached.getCached(), item);
                } else {
                    var c = message.componentBuilder(placeholderable.replace(name0)).decoration(TextDecoration.ITALIC, false);
                    MUTATOR.setComponent(FastItemMutator.CUSTOM_NAME, c, item);
                }
            }
            List<Component> lore0 = new ArrayList<>();
            if (MUTATOR.has(FastItemMutator.LORE, item)) {
                var l = MUTATOR.getItemLore(FastItemMutator.LORE, item);
                lore0.addAll(l);
            }
            for (String s : lore) {
                String line = prePlaceholder.replace(s);
                loreBuilder(line, line1 -> {
                    CachedComponent cached = cachedComponents.getCached(line1);
                    if (cached != null) {
                        lore0.add(cached.getCached());
                    } else {
                        loreBuilder(placeholderable.replace(line1),
                                s1 -> lore0.add(
                                        message.componentBuilder(s1).decoration(TextDecoration.ITALIC, false)
                                )
                        );
                    }
                });
            }
            MUTATOR.setItemLore(FastItemMutator.LORE, lore0, item);
        }
    }

    private void loreBuilder(String lore, Consumer<String> line) {
        lore = lore.replace("\\n", "\n");
        if (lore.contains("\n")) {
            for (String string : lore.split("\n")) {
                line.accept(string);
            }
        } else {
            line.accept(lore);
        }
    }

    private void buildBase(ItemStack result) {
        ItemMeta im = result.getItemMeta();
        if (im == null) return;
        for (ItemFlag itemFlag : builder.itemFlags())
            im.addItemFlags(itemFlag);

        for (PotionEffect potionEffect : builder.potionEffects()) {
            if (im instanceof PotionMeta potionMeta) {
                potionMeta.addCustomEffect(potionEffect, true);
            } else if (im instanceof Arrow arrow) {
                arrow.addCustomEffect(potionEffect, true);
            } else if (im instanceof SuspiciousStewMeta m) {
                m.addCustomEffect(potionEffect, true);
            }
        }

        if (builder.color() != null) {
            if (im instanceof TropicalFishBucketMeta buket) {
                ObjectUtil.applyIfNotNull(DyeColor.getByColor(builder.color()), buket::setBodyColor);
            } else if (im instanceof PotionMeta pm) {
                pm.setColor(builder.color());
            } else if (im instanceof MapMeta map) {
                map.setColor(builder.color());
            } else if (im instanceof LeatherArmorMeta m) {
                m.setColor(builder.color());
            } else if (im instanceof FireworkEffectMeta effectMeta) {
                effectMeta.setEffect(FireworkEffect.builder().withColor(builder.color()).build());
            }
        }
        for (var pair : builder.enchantments()) {
            im.addEnchant(pair.getLeft(), pair.getRight(), true);
        }
        if (builder.modelData() != 0) {
            im.setCustomModelData(builder.modelData());
        }
        if (builder.unbreakable()) {
            im.setUnbreakable(true);
        }
        if (builder.itemFlags().contains(ItemFlag.HIDE_ATTRIBUTES) && Version.is1_20_5orNewer()) {
            // https://github.com/PaperMC/Paper/issues/10655
            im.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier("123", 1, AttributeModifier.Operation.ADD_NUMBER));
        }

        result.setItemMeta(im);
    }
    private void buildNMS(Object item){
        if (!IS_LEGACY && builder.hideTooltip()) {
            MUTATOR.setUnit(FastItemMutator.HIDE_TOOLTIP, item);
        }
    }

    private static boolean canBeCached(String s) {
        return s == null || (!s.contains("{") && !s.contains("%"));
    }

    public static class CachedComponents {
        private final Map<String, CachedComponent> textToComponent = new HashMap<>();

        public @Nullable CachedComponent getCached(String text) {
            if (!canBeCached(text) || text.contains("\n")) return null;
            return textToComponent.computeIfAbsent(text, k -> new CachedComponent(text));
        }
    }
}
