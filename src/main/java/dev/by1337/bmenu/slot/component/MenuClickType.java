package dev.by1337.bmenu.slot.component;

import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.EnumMap;

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

    public static YamlCodec<MenuClickType> CODEC = YamlCodec.fromEnum(MenuClickType.class);
    private static final MenuClickType[] NUMBER_KEY = new MenuClickType[9];
    private static final EnumMap<ClickType, MenuClickType> BUKKIT_TO_BMENU = new EnumMap<>(ClickType.class);

    private final ClickType clickType;
    private final String configKeyClick;

    MenuClickType(ClickType clickType, String configKeyClick) {
        this.clickType = clickType;
        this.configKeyClick = configKeyClick;
    }

    public static MenuClickType getClickType(InventoryClickEvent e) {
        if (e.getClick() == ClickType.NUMBER_KEY) {
            int button = e.getHotbarButton();
            if (button >= 0 && button < 9) {
                return NUMBER_KEY[button];
            }
            return ANY_CLICK;
        }
        return BUKKIT_TO_BMENU.getOrDefault(e.getClick(), ANY_CLICK);
    }

    public ClickType getClickType() {
        return clickType;
    }

    public String getConfigKeyClick() {
        return configKeyClick;
    }

    static {
        NUMBER_KEY[0] = NUMBER_KEY_0;
        NUMBER_KEY[1] = NUMBER_KEY_1;
        NUMBER_KEY[2] = NUMBER_KEY_2;
        NUMBER_KEY[3] = NUMBER_KEY_3;
        NUMBER_KEY[4] = NUMBER_KEY_4;
        NUMBER_KEY[5] = NUMBER_KEY_5;
        NUMBER_KEY[6] = NUMBER_KEY_6;
        NUMBER_KEY[7] = NUMBER_KEY_7;
        NUMBER_KEY[8] = NUMBER_KEY_8;
        for (MenuClickType value : values()) {
            if (value.clickType != null){
                BUKKIT_TO_BMENU.put(value.clickType, value);
            }
        }
    }

}
