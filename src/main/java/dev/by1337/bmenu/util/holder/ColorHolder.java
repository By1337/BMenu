package dev.by1337.bmenu.util.holder;

import dev.by1337.core.ServerVersion;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

public class ColorHolder {
    public static final YamlCodec<ColorHolder> CODEC = YamlCodec.STRING.flatMap(
            s -> {
                try {
                    return DataResult.success(ColorHolder.fromHex(s));
                } catch (Exception e) {
                    return DataResult.error("Expected '#AARRGGBB', but got '" + s + "'");
                }
            },
            ColorHolder::toHex
    );
    public static final ColorHolder BLACK;
    public static final ColorHolder RED;
    public static final ColorHolder DARK_BLUE;
    public static final ColorHolder LIGHT_PURPLE;
    public static final ColorHolder DARK_GREEN;
    public static final ColorHolder YELLOW;
    public static final ColorHolder DARK_AQUA;
    public static final ColorHolder WHITE;
    public static final ColorHolder DARK_RED;
    public static final ColorHolder DARK_PURPLE;
    public static final ColorHolder GOLD;
    public static final ColorHolder GRAY;
    public static final ColorHolder DARK_GRAY;
    public static final ColorHolder BLUE;
    public static final ColorHolder GREEN;
    public static final ColorHolder AQUA;

    private final byte alpha;
    private final byte red;
    private final byte green;
    private final byte blue;
    private final Color bukkit;

    private ColorHolder(int alpha, int red, int green, int blue) {
        this.alpha = (byte) alpha;
        this.red = (byte) red;
        this.green = (byte) green;
        this.blue = (byte) blue;
        bukkit = toBukkit0();
    }

    public ColorHolder(byte alpha, byte red, byte green, byte blue) {
        this.alpha = alpha;
        this.red = red;
        this.green = green;
        this.blue = blue;
        bukkit = toBukkit0();
    }

    public Color toBukkit() {
        return bukkit;
    }

    private Color toBukkit0() {
        if (ServerVersion.is1_19_4orNewer() && alpha() != 255) {
            return Color.fromARGB(alpha(), red(), green(), blue());
        }
        return Color.fromRGB(red(), green(), blue());
    }

    public static ColorHolder fromBukkit(Color color) {
        if (ServerVersion.is1_19_4orNewer()) {
            return new ColorHolder(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
        }
        return new ColorHolder(255, color.getRed(), color.getGreen(), color.getBlue());
    }

    public static ColorHolder fromHex(String hex) {
        return switch (hex) {
            case "black" -> BLACK;
            case "red" -> RED;
            case "dark_blue" -> DARK_BLUE;
            case "light_purple" -> LIGHT_PURPLE;
            case "dark_green" -> DARK_GREEN;
            case "yellow" -> YELLOW;
            case "dark_aqua" -> DARK_AQUA;
            case "white" -> WHITE;
            case "dark_red" -> DARK_RED;
            case "dark_purple" -> DARK_PURPLE;
            case "gold" -> GOLD;
            case "gray" -> GRAY;
            case "dark_gray" -> DARK_GRAY;
            case "blue" -> BLUE;
            case "green" -> GREEN;
            case "aqua" -> AQUA;
            default -> fromHex0(hex);
        };
    }

    private static ColorHolder fromHex0(String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        int idx = 0;
        int alpha;
        if (hex.length() == 8) {
            alpha = Integer.parseInt(hex.substring(idx, idx += 2), 16);
        } else {
            alpha = 255;
        }
        int red = Integer.parseInt(hex.substring(idx, idx += 2), 16);
        int green = Integer.parseInt(hex.substring(idx, idx += 2), 16);
        int blue = Integer.parseInt(hex.substring(idx, idx += 2), 16);
        return new ColorHolder(alpha, red, green, blue);
    }

    public String toHex() {
        return alpha() == 255 ? toHexRGB() : toHexARGB();
    }

    public String toHexRGB() {
        return String.format("#%02X%02X%02X", red(), green(), blue());
    }

    public String toHexARGB() {
        return String.format("#%02X%02X%02X%02X", alpha(), red(), green(), blue());
    }


    public static @NotNull ColorHolder fromARGB(int alpha, int red, int green, int blue) throws IllegalArgumentException {
        return new ColorHolder(alpha, red, green, blue);
    }

    public static @NotNull ColorHolder fromRGB(int red, int green, int blue) throws IllegalArgumentException {
        return new ColorHolder(255, red, green, blue);
    }

    public int asRGB() {
        return (red() << 16) | (green() << 8) | blue();
    }

    public int asARGB() {
        return (alpha() << 24) | (red() << 16) | (green() << 8) | blue();
    }

    public int alpha() {
        return 255 & alpha;
    }

    public int red() {
        return 255 & red;
    }

    public int green() {
        return 255 & green;
    }

    public int blue() {
        return 255 & blue;
    }

    static {
        BLACK = fromHex("000000");
        RED = fromHex("FF5555");
        DARK_BLUE = fromHex("0000AA");
        LIGHT_PURPLE = fromHex("FF55FF");
        DARK_GREEN = fromHex("00AA00");
        YELLOW = fromHex("FFFF55");
        DARK_AQUA = fromHex("00AAAA");
        WHITE = fromHex("FFFFFF");
        DARK_RED = fromHex("AA0000");
        DARK_PURPLE = fromHex("AA00AA");
        GOLD = fromHex("FFAA00");
        GRAY = fromHex("AAAAAA");
        DARK_GRAY = fromHex("555555");
        BLUE = fromHex("5555FF");
        GREEN = fromHex("55FF55");
        AQUA = fromHex("55FFFF");
    }
}
