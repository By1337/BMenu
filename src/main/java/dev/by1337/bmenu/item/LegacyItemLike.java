/*
package dev.by1337.bmenu.item;

import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import dev.by1337.bmenu.io.nbt.JsonNBTWriter;
import dev.by1337.bmenu.io.nbt.NBTWalker;
import dev.by1337.bmenu.text.JsonComponent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LegacyItemLike implements ItemLike {
    private int count = 1;
    private @Nullable ComponentLike name;
    private @Nullable ItemLore lore;
    private int rgb = -1;
    private Material material = Material.DIRT;

    public String writeToString(){
        StringBuilder sb = new StringBuilder();
        JsonNBTWriter writer = new JsonNBTWriter(sb);
        writer.pushObject();
        writeToObject(writer);
        writer.popObject();
        return sb.toString();
    }

    private void writeToObject(NBTWalker walker) {
        walker.pushKey("id");
        walker.pushString(material.getKey().toString());
        walker.pushKey("Count");
        walker.pushByte(count);
        if (rgb != -1 && isPotion()) {
            walker.pushKey("CustomPotionColor");
            walker.pushInt(rgb);
        }
        if (name != null || lore != null || rgb != -1) {
            walker.pushKey("display");
            walker.pushObject();
            if (name != null) {
                walker.pushKey("Name");
                walker.pushString(componentToJson(name));
            }
            if (lore != null) {
                walker.pushKey("Lore");
                walker.pushList();
                for (ComponentLike l : lore.lore()) {
                    walker.pushString(componentToJson(l));
                }
                walker.popList();
            }
            if (rgb != -1 && !isPotion()) {
                walker.pushKey("color");
                walker.pushInt(rgb);
            }
            walker.popObject();//close display
        }
    }

    private String componentToJson(ComponentLike c) {
        if (c instanceof JsonComponent jsonComponent) {
            return jsonComponent.json();
        } else {
            return GsonComponentSerializer.gson().serialize(c.asComponent());
        }
    }

    private boolean isPotion(){
        return material == Material.POTION || material == Material.SPLASH_POTION || material == Material.LINGERING_POTION;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    @Override
    public Material getMaterial() {
        return material ;
    }

    @Override
    public ItemStack asItemStack(ItemStack like) {
        return null;
    }


    @Override
    public ItemLike copy() {
        return null;
    }

    @Override
    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public void setName(@Nullable ComponentLike name) {
        this.name = name;
    }

    @Override
    public @Nullable ComponentLike getName() {
        return name;
    }

    @Override
    public void setLore(@Nullable ItemLore lore) {
        this.lore = lore;
    }

    @Override
    public void setColor(int rgb) {
        this.rgb = rgb;
    }

    @Override
    public int getColor() {
        return rgb;
    }
}
*/
