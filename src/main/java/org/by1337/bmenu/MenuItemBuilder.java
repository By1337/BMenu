package org.by1337.bmenu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.chat.placeholder.MultiPlaceholder;
import org.by1337.blib.chat.util.Message;
import org.by1337.blib.util.Pair;
import org.by1337.bmenu.click.ClickHandler;
import org.by1337.bmenu.click.MenuClickType;
import org.by1337.bmenu.hook.BaseHeadHook;
import org.by1337.bmenu.requirement.Requirement;
import org.by1337.bmenu.requirement.Requirements;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

public class MenuItemBuilder implements Comparable<MenuItemBuilder> {

    private static final Logger LOGGER = LoggerFactory.getLogger("BMenu#ItemBuilder");

    private int[] slots = new int[]{0};
    private List<String> lore = new ArrayList<>();
    private String name;
    private Map<MenuClickType, ClickHandler> clicks = new HashMap<>();
    private String amount = "1";
    private String material = "STONE";
    private ViewRequirement viewRequirement = ViewRequirement.EMPTY;
    private int modelData = 0;
    private List<ItemFlag> itemFlags = new ArrayList<>();
    private List<PotionEffect> potionEffects = new ArrayList<>();
    private Color color = null;
    private int priority = 0;
    private List<Pair<Enchantment, Integer>> enchantments = new ArrayList<>();
    private boolean unbreakable;

    @Nullable
    public MenuItem build(Menu menu) {
        return build(menu, null);
    }

    @Nullable
    public MenuItem build(Menu menu, @Nullable final ItemStack itemStack, Placeholderable... placeholderables) {
        MultiPlaceholder placeholder = new MultiPlaceholder(placeholderables);
        placeholder.add(menu);
        if (!viewRequirement.requirement.test(menu, placeholder, menu.viewer)) {
            menu.runCommands(viewRequirement.denyCommands);
            return null;
        }
        ItemStack result;
        if (itemStack == null) {
            String tmpMaterial = placeholder.replace(material);
            if (tmpMaterial.startsWith("basehead-")) {
                result = BaseHeadHook.getItem(tmpMaterial);
            } else {
                result = new ItemStack(Material.valueOf(menu.replace(tmpMaterial).toUpperCase(Locale.ENGLISH)));
            }
        } else {
            result = itemStack.clone();
        }
        ItemMeta im = result.getItemMeta();
        if (im == null) {
            LOGGER.error("ItemMeta is null! {}", result.getType());
            result = new ItemStack(Material.JIGSAW);
            im = result.getItemMeta();
        }
        Message message = menu.loader.getMessage();
        List<Component> lore = new ArrayList<>(Objects.requireNonNullElseGet(im.lore(), ArrayList::new));
        for (String s : this.lore) {
            String s1 = placeholder.replace(s);
            if (s1.contains("\n")) {
                for (String string : s1.split("\n")) {
                    lore.add(message.componentBuilder(string).decoration(TextDecoration.ITALIC, false));
                }
            } else {
                lore.add(message.componentBuilder(s1).decoration(TextDecoration.ITALIC, false));
            }
        }
        im.lore(lore);
        if (name != null)
            im.displayName(message.componentBuilder(placeholder.replace(name)).decoration(TextDecoration.ITALIC, false));

        for (ItemFlag itemFlag : itemFlags)
            im.addItemFlags(itemFlag);

        for (PotionEffect potionEffect : potionEffects) {
            ((PotionMeta) im).addCustomEffect(potionEffect, true);
        }
        if (color != null) {
            if (im instanceof PotionMeta potionMeta) {
                potionMeta.setColor(color);
            } else if (im instanceof LeatherArmorMeta armorMeta) {
                armorMeta.setColor(color);
            }
        }
        for (var pair : enchantments) {
            im.addEnchant(pair.getLeft(), pair.getRight(), true);
        }
        if (modelData != 0) {
            im.setCustomModelData(modelData);
        }
        if (unbreakable) {
            im.setUnbreakable(true);
        }
        result.setItemMeta(im);
        result.setAmount(Integer.parseInt(placeholder.replace(amount)));
        return new MenuItem(
                slots,
                result,
                clicks
        );
    }


    @Override
    public int compareTo(@NotNull MenuItemBuilder o) {
        return Integer.compare(priority, o.priority);
    }

    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    public void setClicks(Map<MenuClickType, ClickHandler> clicks) {
        this.clicks = clicks;
    }

    public void setEnchantments(List<Pair<Enchantment, Integer>> enchantments) {
        this.enchantments = enchantments;
    }

    public void addEnchantment(Enchantment enchantment, int level) {
        enchantments.add(new Pair<>(enchantment, level));
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setItemFlags(List<ItemFlag> itemFlags) {
        this.itemFlags = itemFlags;
    }

    public void setPotionEffects(List<PotionEffect> potionEffects) {
        this.potionEffects = potionEffects;
    }

    public void addPotionEffect(final PotionEffect effect) {
        potionEffects.add(effect);
    }

    public void addItemFlag(ItemFlag flag) {
        itemFlags.add(flag);
    }

    public void setModelData(int modelData) {
        this.modelData = modelData;
    }

    public void setViewRequirement(Requirement requirement, List<String> denyCommands) {
        this.viewRequirement = new ViewRequirement(requirement, denyCommands);
    }

    public void setViewRequirement(ViewRequirement viewRequirement) {
        this.viewRequirement = viewRequirement;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public void addClickListener(MenuClickType type, ClickHandler handler) {
        clicks.put(type, handler);
    }

    public String getName() {
        return name;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public void addLore(String lore) {
        this.lore.add(lore);
    }

    public void setSlots(int[] slots) {
        this.slots = slots;
    }

    public void addSlot(final int slot) {
        int[] newSlots = new int[slots.length + 1];
        System.arraycopy(slots, 0, newSlots, 0, slots.length);
        newSlots[slots.length] = slot;
        slots = newSlots;
    }

    public static class ViewRequirement {
        private static final ViewRequirement EMPTY = new ViewRequirement(Requirements.EMPTY, Collections.emptyList());
        private final Requirement requirement;
        private final List<String> denyCommands;

        public ViewRequirement(Requirement requirement, List<String> denyCommands) {
            this.requirement = requirement;
            this.denyCommands = denyCommands;
        }

        public Requirement getRequirement() {
            return requirement;
        }

        public List<String> getDenyCommands() {
            return denyCommands;
        }
    }
}
