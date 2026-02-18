package dev.by1337.bmenu.animation.opcode;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class SwapAnimOpcodeTest {
    @Test
    void parseTest() {
        assertArrayEquals(new int[]{10, 11, 12}, new SwapAnimOpcode(("10,11,12 10-15")).getFrom());
        assertArrayEquals(new int[]{10, 11, 12, 17}, new SwapAnimOpcode(("10 10-12,17")).getTo());
        assertArrayEquals(new int[]{10, 11, 12, 17, 18, 19}, new SwapAnimOpcode(("10 10-12,17-19")).getTo());
        assertArrayEquals(new int[]{10, 11, 12, 17}, new SwapAnimOpcode(("10-12,17 10-12,17")).getFrom());
        assertArrayEquals(new int[]{10, 11, 12, 17}, new SwapAnimOpcode(("10-12,17 10-12,17")).getTo());
    }
}