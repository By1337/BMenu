package dev.by1337.bmenu.animation.opcode;

import dev.by1337.bmenu.animation.FrameOpcode;
import dev.by1337.bmenu.animation.util.AnimationUtil;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.slot.SlotContent;
import dev.by1337.plc.PlaceholderSyntax;
import dev.by1337.plc.Placeholders;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ThemeMaterialOpcodeTest {
    private static class Resolved extends RuntimeException {
        Resolved(String material) { super(material); }
    }

    @Test
    void acceptsLeadingAndRepeatedWhitespaceWithoutParsingMaterialAsSlot() {
        String input = " \t%bthemes_gui_material_1%   40, 41-42  ";
        var parsed = AnimationUtil.readMaterialSlots(input);
        assertEquals("%bthemes_gui_material_1%", parsed.material());
        assertArrayEquals(new int[]{40, 41, 42}, parsed.slots());
        assertNotNull(new SetAnimOpcode(input));
        assertNotNull(new SetIfEmptyAnimOpcode(input));
        assertThrows(IllegalArgumentException.class, () -> new SetAnimOpcode("stone"));
    }

    @Test
    void replacesPapiMaterialAtExecutionOnceAndDoesNotFreezeTheFirstTheme() {
        AtomicReference<String> selected = new AtomicReference<>("orange_stained_glass_pane");
        AtomicInteger calls = new AtomicInteger();
        var papi = new Placeholders<Object>(PlaceholderSyntax.PAPI)
                .withParams("bthemes", key -> key.equals("gui_material_1") ? selected.get() : null);
        Menu menu = (Menu) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Menu.class}, (proxy, method, args) -> {
            if (method.getName().equals("setPlaceholders")) {
                calls.incrementAndGet();
                return papi.setPlaceholders((String) args[0], null);
            }
            if (method.getName().equals("resolveSlotBuilder")) throw new Resolved((String) args[0]);
            return null;
        });
        for (FrameOpcode opcode : new FrameOpcode[]{new SetAnimOpcode(" %bthemes_gui_material_1% 40"),
                new SetIfEmptyAnimOpcode(" %bthemes_gui_material_1% 40"), new FillAnimOpcode(" %bthemes_gui_material_1% ")}) {
            selected.set("orange_stained_glass_pane");
            assertEquals(selected.get(), assertThrows(Resolved.class, () -> opcode.apply(new SlotContent[54], menu, null)).getMessage());
            selected.set("blue_stained_glass_pane");
            assertEquals(selected.get(), assertThrows(Resolved.class, () -> opcode.apply(new SlotContent[54], menu, null)).getMessage());
        }
        assertEquals(6, calls.get());
    }
}
