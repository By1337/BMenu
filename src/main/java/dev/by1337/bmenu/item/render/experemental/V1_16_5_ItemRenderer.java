/*
package dev.by1337.bmenu.item.render.experemental;

import dev.by1337.bmenu.item.ItemModel;
import dev.by1337.bmenu.item.render.ItemRenderer;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.text.JsonComponent;
import dev.by1337.bmenu.text.RawTextComponent;
import dev.by1337.plc.PlaceholderApplier;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.core.Registry;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class V1_16_5_ItemRenderer implements ItemRenderer<Inventory> {
    private static final int PACKET_ID = ConnectionProtocol.PLAY.getPacketId(PacketFlow.CLIENTBOUND, new ClientboundContainerSetSlotPacket());
    private static final ItemStack AIR = CraftItemStack.asCraftMirror(net.minecraft.world.item.ItemStack.EMPTY);
    private static final net.minecraft.world.item.ItemStack HOE = new net.minecraft.world.item.ItemStack(Items.WOODEN_HOE);
    private static final net.minecraft.world.item.ItemStack STONE = new net.minecraft.world.item.ItemStack(Items.STONE);
    private static final Logger log = LoggerFactory.getLogger("BMenu");

    @Override
    public void render(Inventory ctx, int slot, ItemModel item, Menu menu, PlaceholderApplier placeholders) {
        long nanos = System.nanoTime();
        if (item == null) {
            ctx.setItem(slot, AIR);
            return;
        }
        CraftInventory craftCtx = (CraftInventory) ctx;

        String material = "minecraft:dirt";
        Material bukkitMaterial;
        try {
            bukkitMaterial = Material.valueOf(item.material().get(placeholders).toUpperCase());
            material = bukkitMaterial.getKey().toString();
        } catch (Exception e) {
            log.error("Failed to create item {}", material);
            bukkitMaterial = Material.STONE;
        }
        int amount = item.amount().getOrDefault(placeholders, 1);
        if (bukkitMaterial.getMaxStackSize() == 1) {
            craftCtx.getInventory().setItem(slot, HOE);
            // ctx.setItem(slot, HOE);
        } else {
            // ctx.setItem(slot, STONE.asQuantity(amount));
            if (amount != 1) {
                var v = STONE.cloneItemStack();
                v.setCount(amount);
                craftCtx.getInventory().setItem(slot, v);
            } else {
                craftCtx.getInventory().setItem(slot, STONE);
            }
        }
        ServerPlayer sp = ((CraftPlayer) menu.getViewer()).getHandle();
        ByteBuf buf = sp.networkManager.channel.alloc().buffer();
        writeVarInt(buf, PACKET_ID);
        buf.writeByte(sp.activeContainer.windowId);
        buf.writeShort(slot);
        //item
        buf.writeBoolean(true);
        Item itemType = Registry.ITEM.get(new ResourceLocation(material));
        writeVarInt(buf, Item.getId(itemType));
        buf.writeByte(amount);
        NBTStream stream = new NBTStream(buf);
        buf.writeByte(10); //open tag
        buf.writeShort(0); //empty tag name

        stream.pushKey("Damage");
        stream.pushInt(item.damage().getOrDefault(placeholders, 0));

        if (item.hasItemFlags()) {
            // item.forEachItemFlags(itemFlag -> {
            //
            // });
            // int hideFlags = 0;
            // for (ItemFlag flag : flags) {
            //     hideFlags |= (byte) (1 << flag.ordinal());
            // }
            // stream.pushKey("HideFlags");
            // stream.pushInt(hideFlags);
        }
        var color = item.color();
        if (color != null) {
            if (isPotion(itemType)) {
                stream.pushKey("CustomPotionColor");
                stream.pushInt(color.asRGB());
            }

        }
        var name = item.name();
        if (item.hasLore() || name != null) {
            stream.pushKey("display");
            stream.pushObject();
            if (name != null) {
                stream.pushKey("Name");
                stream.pushString(componentToJson(name, placeholders));
            }
            if (item.hasLore()) {
                stream.pushKey("Lore");
                stream.pushList();
                item.forEachLore(lore -> {
                    stream.pushString(componentToJson(lore, placeholders));
                });
                stream.popList();
            }
            stream.popObject(); //close display
        }
        buf.writeByte(0); // close root TAG_COMPOUND
        //  log.info("sent in {}us", (System.nanoTime() - nanos) / 1000D);
        sp.networkManager.channel.write(buf);
    }

    @Override
    public void flush(Inventory ctx, Menu menu) {
        ((CraftPlayer) menu.getViewer()).getHandle().networkManager.channel.flush();
    }

    public static void writeVarInt(ByteBuf buf, int value) {
        if ((value & (0xFFFFFFFF << 7)) == 0) {
            buf.writeByte(value);
        } else if ((value & (0xFFFFFFFF << 14)) == 0) {
            int w = (value & 0x7F | 0x80) << 8 | (value >>> 7);
            buf.writeShort(w);
        } else {
            writeVarIntFull(buf, value);
        }
    }

    public static void writeVarIntFull(ByteBuf buf, int value) {
        if ((value & (0xFFFFFFFF << 7)) == 0) {
            buf.writeByte(value);
        } else if ((value & (0xFFFFFFFF << 14)) == 0) {
            int w = (value & 0x7F | 0x80) << 8 | (value >>> 7);
            buf.writeShort(w);
        } else if ((value & (0xFFFFFFFF << 21)) == 0) {
            int w = (value & 0x7F | 0x80) << 16 | ((value >>> 7) & 0x7F | 0x80) << 8 | (value >>> 14);
            buf.writeMedium(w);
        } else if ((value & (0xFFFFFFFF << 28)) == 0) {
            int w = (value & 0x7F | 0x80) << 24 | (((value >>> 7) & 0x7F | 0x80) << 16)
                    | ((value >>> 14) & 0x7F | 0x80) << 8 | (value >>> 21);
            buf.writeInt(w);
        } else {
            int w = (value & 0x7F | 0x80) << 24 | ((value >>> 7) & 0x7F | 0x80) << 16
                    | ((value >>> 14) & 0x7F | 0x80) << 8 | ((value >>> 21) & 0x7F | 0x80);
            buf.writeInt(w);
            buf.writeByte(value >>> 28);
        }
    }

    private boolean isPotion(Item material) {
        return material == Items.POTION || material == Items.SPLASH_POTION || material == Items.LINGERING_POTION;
    }

    private String componentToJson(ComponentLike c, PlaceholderApplier placeholders) {
        if (c instanceof JsonComponent jsonComponent) {
            return jsonComponent.json();
        } else if (c instanceof RawTextComponent raw) {
            return GsonComponentSerializer.gson().serialize(raw.asComponent(placeholders));
        } else {
            return GsonComponentSerializer.gson().serialize(c.asComponent());
        }
    }
}
*/
