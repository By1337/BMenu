package org.by1337.bmenu.click;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

public enum MenuClickType {
    LEFT(ClickType.LEFT, "on_left_click"),
    RIGHT(ClickType.RIGHT, "on_right_click"),
    SHIFT_LIFT(ClickType.SHIFT_LEFT, "on_shift_left_click"),
    SHIFT_RIGHT(ClickType.SHIFT_RIGHT, "on_shift_right_click"),
    MIDDLE(ClickType.MIDDLE, "on_middle_click"),
    DROP(ClickType.DROP, "on_drop_click"),
    ANY_CLICK(null, "on_click"),
    NUMBER_KEY_0(null, "on_number_key_0_click"),
    NUMBER_KEY_1(null, "on_number_key_1_click"),
    NUMBER_KEY_2(null, "on_number_key_2_click"),
    NUMBER_KEY_3(null, "on_number_key_3_click"),
    NUMBER_KEY_4(null, "on_number_key_4_click"),
    NUMBER_KEY_5(null, "on_number_key_5_click"),
    NUMBER_KEY_6(null, "on_number_key_6_click"),
    NUMBER_KEY_7(null, "on_number_key_7_click"),
    NUMBER_KEY_8(null, "on_number_key_8_click"),
    SWAP_OFFHAND(ClickType.SWAP_OFFHAND, "on_swap_offhand_click"),
    CONTROL_DROP(ClickType.CONTROL_DROP, "on_control_drop_click");


    private final ClickType clickType;
    private final String configKeyClick;

    MenuClickType(ClickType clickType, String configKeyClick) {
        this.clickType = clickType;
        this.configKeyClick = configKeyClick;
    }


    public static MenuClickType getClickType(InventoryClickEvent e) {
        if (e.getClick() == org.bukkit.event.inventory.ClickType.NUMBER_KEY) {
            return MenuClickType.valueOf("NUMBER_KEY_" + e.getHotbarButton());
        }
        for (MenuClickType clickType1 : MenuClickType.values()) {
            if (clickType1.clickType == e.getClick()) {
                return clickType1;
            }
        }
        return ANY_CLICK;
    }

    public ClickType getClickType() {
        return clickType;
    }

    public String getConfigKeyClick() {
        return configKeyClick;
    }


}
