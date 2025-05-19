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
import org.by1337.blib.inventory.ItemStackUtil;
import org.by1337.blib.util.Version;
import org.by1337.bmenu.MenuItemBuilder;
import org.by1337.bmenu.hook.ItemStackCreator;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class ItemStackBuilder {
    private static final ItemStackUtil UNSAFE_ITEM = BLib.getApi().getUnsafe().getItemStackUtil();

    private final MenuItemBuilder builder;

    private Object cached;
    private boolean fullCached;
    private boolean displayCached;

    public ItemStackBuilder(MenuItemBuilder builder) {
        this.builder = builder;
        if (canBeCached(builder.material())) {
            ItemStack itemStack = ItemStackCreator.getItem(builder.material());
            buildBase(itemStack);
            cached = BLib.getApi().getUnsafe().getItemStackUtil().asNMSCopyItemStack(itemStack);
            if (isDisplayCashed()) {
                setDisplay(cached, s -> s, BLib.getApi().getMessage());
                displayCached = true;
                if (canBeCached(builder.amount()) && canBeCached(builder.damage())) {
                    setAmountAndDamage(cached, s -> s);
                    fullCached = true;
                }
            }
        }
    }

    public ItemStack build(@Nullable ItemStack i, Placeholderable placeholderable, Message message) {
        if (i == null && fullCached) return UNSAFE_ITEM.asBukkitMirror(cached);
        Object itemStack;
        if (i != null) {
            var item = i.clone();
            buildBase(item);
            itemStack = UNSAFE_ITEM.asNMSCopyItemStack(item);
        } else if (cached != null) {
            itemStack = UNSAFE_ITEM.copyNMSItemStack(cached);
        } else {
            var item = ItemStackCreator.getItem(placeholderable.replace(builder.material()));
            buildBase(item);
            itemStack = UNSAFE_ITEM.asNMSCopyItemStack(item);
        }
        if (!displayCached) setDisplay(itemStack, placeholderable, message);
        setAmountAndDamage(itemStack, placeholderable);
        return UNSAFE_ITEM.asBukkitMirror(itemStack);
    }

    private void setAmountAndDamage(Object item, Placeholderable placeholderable) {
        if (builder.amount() != null) {
            UNSAFE_ITEM.setCount(Integer.parseInt(placeholderable.replace(builder.amount())), item);
        }
        if (builder.damage() != null) {
            UNSAFE_ITEM.setDamage(Integer.parseInt(placeholderable.replace(builder.damage())), item);
        }
    }

    private boolean isDisplayCashed() {
        if (builder.getCashedName() != null && !builder.getCashedName().isCached()) {
            return false;
        }
        return builder.getCashedLore().stream().allMatch(CachedComponent::isCached);
    }

    private void setDisplay(Object item, Placeholderable placeholderable, Message message) {
        CachedComponent name = builder.getCashedName();
        if (name != null) {
            if (name.getCachedJson() != null) {
                UNSAFE_ITEM.setDisplayName(name.getCachedJson(), item);
            } else if (name.getCached() != null) {
                UNSAFE_ITEM.setDisplayName(name.getCached(), item);
            } else {
                UNSAFE_ITEM.setDisplayName(
                        message.componentBuilder(placeholderable.replace(name.getSource())).decoration(TextDecoration.ITALIC, false),
                        item
                );
            }
        }
        if (UNSAFE_ITEM.isJsonSupport()) {
            List<String> lore = UNSAFE_ITEM.getJsonLore(item);
            for (CachedComponent cachedComponent : builder.getCashedLore()) {
                if (cachedComponent.getCachedJson() != null) {
                    lore.add(cachedComponent.getCachedJson());
                } else {
                    loreBuilder(placeholderable.replace(cachedComponent.getSource()),
                            s -> lore.add(
                                    GsonComponentSerializer.gson().serialize(
                                            message.componentBuilder(s).decoration(TextDecoration.ITALIC, false)
                                    )
                            )
                    );
                }
            }
            UNSAFE_ITEM.setLoreJson(lore, item);
        } else {
            List<Component> lore = UNSAFE_ITEM.getLore(item);
            for (CachedComponent cachedComponent : builder.getCashedLore()) {
                if (cachedComponent.isCached()) {
                    lore.add(cachedComponent.getCached());
                } else {
                    loreBuilder(placeholderable.replace(cachedComponent.getSource()),
                            s -> lore.add(message.componentBuilder(s).decoration(TextDecoration.ITALIC, false))
                    );
                }
            }
            UNSAFE_ITEM.setLore(lore, item);
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

    private static boolean canBeCached(String s) {
        return s == null || (!s.contains("{") && !s.contains("%"));
    }
}
