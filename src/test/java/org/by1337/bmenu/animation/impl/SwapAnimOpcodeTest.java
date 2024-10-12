package org.by1337.bmenu.animation.impl;

import org.by1337.blib.configuration.YamlValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SwapAnimOpcodeTest {
    @Test
    void parseTest() {
        assertArrayEquals(new int[]{10, 11, 12}, new SwapAnimOpcode(new YamlValue("10,11,12 10-15")).getFrom());
        assertArrayEquals(new int[]{10, 11, 12, 17}, new SwapAnimOpcode(new YamlValue("10 10-12,17")).getTo());
        assertArrayEquals(new int[]{10, 11, 12, 17, 18, 19}, new SwapAnimOpcode(new YamlValue("10 10-12,17-19")).getTo());
        assertArrayEquals(new int[]{10, 11, 12, 17}, new SwapAnimOpcode(new YamlValue("10-12,17 10-12,17")).getFrom());
        assertArrayEquals(new int[]{10, 11, 12, 17}, new SwapAnimOpcode(new YamlValue("10-12,17 10-12,17")).getTo());
    }
}