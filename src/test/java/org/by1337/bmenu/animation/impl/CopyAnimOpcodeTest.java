package dev.by1337.bmenu.animation.impl;

import org.junit.jupiter.api.Test;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class CopyAnimOpcodeTest {

    @Test
    void parseTest() throws ParseException {
        assertArrayEquals(new int[]{10, 11, 12}, new CopyAnimOpcode(("10,11,12 10-15")).getSrc());
        assertArrayEquals(new int[]{10, 11, 12, 17}, new CopyAnimOpcode(("10 10-12,17")).getDest());
        assertArrayEquals(new int[]{10, 11, 12, 17, 18, 19}, new CopyAnimOpcode(("10 10-12,17-19")).getDest());
        assertArrayEquals(new int[]{10, 11, 12, 17}, new CopyAnimOpcode(("10-12,17 10-12,17")).getSrc());
        assertArrayEquals(new int[]{10, 11, 12, 17}, new CopyAnimOpcode(("10-12,17 10-12,17")).getDest());
        assertArrayEquals(new int[]{10}, new CopyAnimOpcode(("10++")).getSrc());
        assertArrayEquals(new int[]{11}, new CopyAnimOpcode(("10++")).getDest());
        assertArrayEquals(new int[]{10}, new CopyAnimOpcode(("10--")).getSrc());
        assertArrayEquals(new int[]{9}, new CopyAnimOpcode(("10--")).getDest());
    }
}