package org.by1337.bmenu.click;

import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

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
    private static final Map<ClickType, MenuClickType> BUKKIT_CLICK_TO_MENU_CLICK;

    private final ClickType clickType;
    private final String configKeyClick;

    MenuClickType(ClickType clickType, String configKeyClick) {
        this.clickType = clickType;
        this.configKeyClick = configKeyClick;
    }
    @Nullable
    public static MenuClickType getClickType(ClickType clickType) {
        return BUKKIT_CLICK_TO_MENU_CLICK.get(clickType);
    }

    public ClickType getClickType() {
        return clickType;
    }

    public String getConfigKeyClick() {
        return configKeyClick;
    }

    static {
        BUKKIT_CLICK_TO_MENU_CLICK = new EnumMap<>(ClickType.class);
        for (MenuClickType value : values()) {
            BUKKIT_CLICK_TO_MENU_CLICK.put(value.clickType, value);
        }
    }
}
