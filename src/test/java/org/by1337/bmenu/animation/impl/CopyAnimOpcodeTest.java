package org.by1337.bmenu.animation.impl;

import org.by1337.blib.configuration.YamlValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CopyAnimOpcodeTest {

    @Test
    void parseTest() {
        assertArrayEquals(new int[]{10, 11, 12}, new CopyAnimOpcode(new YamlValue("10,11,12 10-15")).getSrc());
        assertArrayEquals(new int[]{10, 11, 12, 17}, new CopyAnimOpcode(new YamlValue("10 10-12,17")).getDest());
        assertArrayEquals(new int[]{10, 11, 12, 17, 18, 19}, new CopyAnimOpcode(new YamlValue("10 10-12,17-19")).getDest());
        assertArrayEquals(new int[]{10, 11, 12, 17}, new CopyAnimOpcode(new YamlValue("10-12,17 10-12,17")).getSrc());
        assertArrayEquals(new int[]{10, 11, 12, 17}, new CopyAnimOpcode(new YamlValue("10-12,17 10-12,17")).getDest());
    }
}