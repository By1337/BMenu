package org.by1337.bmenu.io.nbt;

import java.util.Set;

public interface NBTWalker {
    byte TAG_BYTE = 1;
    byte TAG_SHORT = 2;
    byte TAG_INT = 3;
    byte TAG_LONG = 4;
    byte TAG_FLOAT = 5;
    byte TAG_DOUBLE = 6;
    byte TAG_BYTE_ARRAY = 7;
    byte TAG_STRING = 8;
    byte TAG_LIST = 9;
    byte TAG_COMPOUND = 10;
    byte TAG_INT_ARRAY = 11;
    byte TAG_LONG_ARRAY = 12;

    Set<String> keySet();

    void pushKey(String key);

    void pushObject();

    void popObject();

    void removeTag(String key);

    void pushByte(byte v);

    default void pushByte(int v) {
        pushByte((byte) v);
    }

    void pushShort(short v);

    void pushInt(int v);

    void pushLong(long v);

    void pushFloat(float v);

    void pushDouble(double v);

    void pushByteArray(byte[] v);

    void pushIntArray(int[] v);

    void pushLongArray(long[] v);

    void pushString(String v);

    default void pushBool(boolean v) {
        pushByte(v ? 1 : 0);
    }

    byte getByte(String key);

    short getShort(String key);

    int getInt(String key);

    long getLong(String key);

    float getFloat(String key);

    double getDouble(String key);

    byte[] getByteArray(String key);

    int[] getIntArray(String key);

    long[] getLongArray(String key);
    String getString(String key);

    int getType(String key);

    default boolean getBool(String key) {
        return getByte(key) == 1;
    }

    boolean hasKey(String key);

    boolean hasKey(String key, int type);

    void clear();

    int size();

    void pushList();

    void popList();

    int getType(int index);

    byte getByte(int index);

    short getShort(int index);

    int getInt(int index);

    long getLong(int index);

    float getFloat(int index);

    double getDouble(int index);

    byte[] getByteArray(int index);

    int[] getIntArray(int index);

    long[] getLongArray(int index);
    String getString(int index);
}
