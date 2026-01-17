/*
package dev.by1337.bmenu.item.render.experemental;

import net.minecraft.nbt.*;
import dev.by1337.bmenu.io.nbt.NBTWalker;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class NmsNBTWalker implements NBTWalker {
    private NBTWalker current;


    public static NBTWalker ofCompound(CompoundTag tag) {
        NmsNBTWalker impl = new NmsNBTWalker();
        impl.current = impl.new NBTWalkerCompound(tag, null);
        return impl;
    }

    public class NBTWalkerCompound implements NBTWalker {
        private final CompoundTag tag;
        private final @Nullable NBTWalker perv;
        private @Nullable String key;

        public NBTWalkerCompound(CompoundTag tag, @Nullable NBTWalker perv) {
            this.tag = tag;
            this.perv = perv;
        }

        @Override
        public Set<String> keySet() {
            return tag.getKeys();
        }

        @Override
        public void pushKey(String key) {
            this.key = key;
        }

        @Override
        public void pushObject() {
            CompoundTag extra;
            var v = tag.get(key);
            if (v instanceof CompoundTag t1) {
                extra = t1;
            } else {
                extra = new CompoundTag();
            }
            tag.set(key, extra); //todo
            current = new NBTWalkerCompound(extra, this);
        }

        @Override
        public void popObject() {
            if (perv == null) {
                throw new IllegalStateException("already in root tag!");
            }
            current = perv;
        }

        @Override
        public void removeTag(String key) {
            tag.remove(key);
        }

        @Override
        public void pushByte(byte v) {
            tag.setByte(key, v);
        }

        @Override
        public void pushShort(short v) {
            tag.setShort(key, v);
        }

        @Override
        public void pushInt(int v) {
            tag.setInt(key, v);
        }

        @Override
        public void pushLong(long v) {
            tag.setLong(key, v);
        }

        @Override
        public void pushFloat(float v) {
            tag.setFloat(key, v);
        }

        @Override
        public void pushDouble(double v) {
            tag.setDouble(key, v);
        }

        @Override
        public void pushByteArray(byte[] v) {
            tag.setByteArray(key, v);
        }

        @Override
        public void pushIntArray(int[] v) {
            tag.setIntArray(key, v);
        }

        @Override
        public void pushLongArray(long[] v) {
            tag.putLongArray(key, v);
        }

        @Override
        public void pushString(String v) {
            tag.setString(key, v);
        }

        @Override
        public byte getByte(String key) {
            Tag v = tag.get(key);
            if (v instanceof NumericTag numericTag) {
                return numericTag.asByte();
            }
            throw new IllegalArgumentException("Tag " + (v == null ? "null" : v.getTypeId()) + " not a number!");
        }

        @Override
        public short getShort(String key) {
            Tag v = tag.get(key);
            if (v instanceof NumericTag numericTag) {
                return numericTag.asShort();
            }
            throw new IllegalArgumentException("Tag " + (v == null ? "null" : v.getTypeId()) + " not a number!");

        }

        @Override
        public int getInt(String key) {
            Tag v = tag.get(key);
            if (v instanceof NumericTag numericTag) {
                return numericTag.asInt();
            }
            throw new IllegalArgumentException("Tag " + (v == null ? "null" : v.getTypeId()) + " not a number!");

        }

        @Override
        public long getLong(String key) {
            Tag v = tag.get(key);
            if (v instanceof NumericTag numericTag) {
                return numericTag.asLong();
            }
            throw new IllegalArgumentException("Tag " + (v == null ? "null" : v.getTypeId()) + " not a number!");

        }

        @Override
        public float getFloat(String key) {
            Tag v = tag.get(key);
            if (v instanceof NumericTag numericTag) {
                return numericTag.asFloat();
            }
            throw new IllegalArgumentException("Tag " + (v == null ? "null" : v.getTypeId()) + " not a number!");

        }

        @Override
        public double getDouble(String key) {
            Tag v = tag.get(key);
            if (v instanceof NumericTag numericTag) {
                return numericTag.asDouble();
            }
            throw new IllegalArgumentException("Tag " + (v == null ? "null" : v.getTypeId()) + " not a number!");

        }

        @Override
        public byte[] getByteArray(String key) {
            Tag v = tag.get(key);
            if (v instanceof ByteArrayTag arr) {
                return arr.getBytes();
            }
            throw new IllegalArgumentException("Tag " + (v == null ? "null" : v.getTypeId()) + " not a byte array!");

        }

        @Override
        public int[] getIntArray(String key) {
            Tag v = tag.get(key);
            if (v instanceof IntArrayTag arr) {
                return arr.getInts();
            }
            throw new IllegalArgumentException("Tag " + (v == null ? "null" : v.getTypeId()) + " not a int array!");

        }

        @Override
        public long[] getLongArray(String key) {
            Tag v = tag.get(key);
            if (v instanceof LongArrayTag arr) {
                return arr.getLongs();
            }
            throw new IllegalArgumentException("Tag " + (v == null ? "null" : v.getTypeId()) + " not a long array!");

        }

        @Override
        public String getString(String key) {
            Tag v = tag.get(key);
            if (v instanceof StringTag arr) {
                return arr.asString();
            }
            throw new IllegalArgumentException("Tag " + (v == null ? "null" : v.getTypeId()) + " not a string!");

        }

        @Override
        public int getType(String key) {
            Tag v = tag.get(key);
            return v == null ? 0 : v.getTypeId();
        }

        @Override
        public boolean hasKey(String key) {
            return tag.hasKey(key);
        }

        @Override
        public boolean hasKey(String key, int type) {
            return tag.hasKeyOfType(key, type);
        }

        @Override
        public void clear() {
            tag.map.clear();
        }

        @Override
        public int size() {
            return tag.size();
        }

        @Override
        public void pushList() {
            var v = tag.get(key);
            ListTag listTag;
            if (v instanceof ListTag t1) {
                listTag = t1;
            } else {
                listTag = new ListTag();
            }
            tag.set(key, listTag);
            current = new NBTWalkerList(listTag, this);
        }

        @Override
        public void popList() {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public int getType(int index) {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public byte getByte(int index) {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public short getShort(int index) {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public int getInt(int index) {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public long getLong(int index) {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public float getFloat(int index) {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public double getDouble(int index) {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public byte[] getByteArray(int index) {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public int[] getIntArray(int index) {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public long[] getLongArray(int index) {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public String getString(int index) {
            throw new UnsupportedOperationException("Only in list!");
        }
    }

    public class NBTWalkerList implements NBTWalker {
        private final ListTag list;
        private final @Nullable NBTWalker perv;

        public NBTWalkerList(ListTag list, @Nullable NBTWalker perv) {
            this.list = list;
            this.perv = perv;
        }


        @Override
        public Set<String> keySet() {
            throw new UnsupportedOperationException("Only in compound!");
        }

        @Override
        public void pushKey(String key) {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public void pushObject() {
            CompoundTag tag = new CompoundTag();
            list.add(tag);
            current = new NBTWalkerCompound(tag, this);
        }

        @Override
        public void popObject() {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public void removeTag(String key) {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public void pushByte(byte v) {
            list.add(ByteTag.valueOf(v));
        }

        @Override
        public void pushShort(short v) {
            list.add(ShortTag.valueOf(v));
        }

        @Override
        public void pushInt(int v) {
            list.add(IntTag.valueOf(v));
        }

        @Override
        public void pushLong(long v) {
            list.add(LongTag.valueOf(v));
        }

        @Override
        public void pushFloat(float v) {
            list.add(FloatTag.valueOf(v));
        }

        @Override
        public void pushDouble(double v) {
            list.add(DoubleTag.valueOf(v));
        }

        @Override
        public void pushByteArray(byte[] v) {
            list.add(new ByteArrayTag(v));
        }

        @Override
        public void pushIntArray(int[] v) {
            list.add(new IntArrayTag(v));
        }

        @Override
        public void pushLongArray(long[] v) {
            list.add(new LongArrayTag(v));
        }

        @Override
        public void pushString(String v) {
            list.add(StringTag.valueOf(v));
        }

        @Override
        public byte getByte(String key) {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public short getShort(String key) {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public int getInt(String key) {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public long getLong(String key) {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public float getFloat(String key) {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public double getDouble(String key) {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public byte[] getByteArray(String key) {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public int[] getIntArray(String key) {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public long[] getLongArray(String key) {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public String getString(String key) {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public int getType(String key) {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public boolean hasKey(String key) {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public boolean hasKey(String key, int type) {
            throw new UnsupportedOperationException("Only in list!");
        }

        @Override
        public void clear() {
            list.clear();
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public void pushList() {
            ListTag listTag = new ListTag();
            list.add(listTag);
            current = new NBTWalkerList(listTag, this);
        }

        @Override
        public void popList() {
            if (perv == null) {
                throw new IllegalStateException("already in root tag!");
            }
            current = perv;
        }

        @Override
        public int getType(int index) {
            var v = list.get(index);
            return v == null ? 0 : v.getTypeId();
        }

        @Override
        public byte getByte(int index) {
            Tag v = list.get(index);
            if (v instanceof NumericTag numericTag) {
                return numericTag.asByte();
            }
            throw new IllegalArgumentException("Tag " + v.getTypeId() + " not a byte!");

        }

        @Override
        public short getShort(int index) {
            Tag v = list.get(index);
            if (v instanceof NumericTag numericTag) {
                return numericTag.asShort();
            }
            throw new IllegalArgumentException("Tag " + v.getTypeId() + " not a short!");

        }

        @Override
        public int getInt(int index) {
            Tag v = list.get(index);
            if (v instanceof NumericTag numericTag) {
                return numericTag.asInt();
            }
            throw new IllegalArgumentException("Tag " + v.getTypeId() + " not a int!");

        }

        @Override
        public long getLong(int index) {
            Tag v = list.get(index);
            if (v instanceof NumericTag numericTag) {
                return numericTag.asLong();
            }
            throw new IllegalArgumentException("Tag " + v.getTypeId() + " not a long!");

        }

        @Override
        public float getFloat(int index) {
            Tag v = list.get(index);
            if (v instanceof NumericTag numericTag) {
                return numericTag.asFloat();
            }
            throw new IllegalArgumentException("Tag " + v.getTypeId() + " not a float!");

        }

        @Override
        public double getDouble(int index) {
            Tag v = list.get(index);
            if (v instanceof NumericTag numericTag) {
                return numericTag.asDouble();
            }
            throw new IllegalArgumentException("Tag " + v.getTypeId() + " not a double!");

        }

        @Override
        public byte[] getByteArray(int index) {
            Tag v = list.get(index);
            if (v instanceof ByteArrayTag array) {
                return array.getBytes();
            }
            throw new IllegalArgumentException("Tag " + v.getTypeId() + " not a byte array!");

        }

        @Override
        public int[] getIntArray(int index) {
            Tag v = list.get(index);
            if (v instanceof IntArrayTag array) {
                return array.getInts();
            }
            throw new IllegalArgumentException("Tag " + v.getTypeId() + " not a int array!");

        }

        @Override
        public long[] getLongArray(int index) {
            Tag v = list.get(index);
            if (v instanceof LongArrayTag array) {
                return array.getLongs();
            }
            throw new IllegalArgumentException("Tag " + v.getTypeId() + " not a long array!");
        }

        @Override
        public String getString(int index) {
            Tag v = list.get(index);
            if (v instanceof StringTag stringTag) {
                return stringTag.asString();
            }
            throw new IllegalArgumentException("Tag " + v.getTypeId() + " not a string!");
        }

    }


    @Override
    public Set<String> keySet() {
        return current.keySet();
    }

    @Override
    public void pushKey(String key) {
        current.pushKey(key);
    }

    @Override
    public void pushObject() {
        current.pushObject();
    }

    @Override
    public void popObject() {
        current.popObject();
    }

    @Override
    public void removeTag(String key) {
        current.removeTag(key);
    }

    @Override
    public void pushByte(byte v) {
        current.pushByte(v);
    }

    @Override
    public void pushByte(int v) {
        current.pushByte(v);
    }

    @Override
    public void pushShort(short v) {
        current.pushShort(v);
    }

    @Override
    public void pushInt(int v) {
        current.pushInt(v);
    }

    @Override
    public void pushLong(long v) {
        current.pushLong(v);
    }

    @Override
    public void pushFloat(float v) {
        current.pushFloat(v);
    }

    @Override
    public void pushDouble(double v) {
        current.pushDouble(v);
    }

    @Override
    public void pushByteArray(byte[] v) {
        current.pushByteArray(v);
    }

    @Override
    public void pushIntArray(int[] v) {
        current.pushIntArray(v);
    }

    @Override
    public void pushLongArray(long[] v) {
        current.pushLongArray(v);
    }

    @Override
    public void pushString(String v) {
        current.pushString(v);
    }

    @Override
    public void pushBool(boolean v) {
        current.pushBool(v);
    }

    @Override
    public byte getByte(String key) {
        return current.getByte(key);
    }

    @Override
    public short getShort(String key) {
        return current.getShort(key);
    }

    @Override
    public int getInt(String key) {
        return current.getInt(key);
    }

    @Override
    public long getLong(String key) {
        return current.getLong(key);
    }

    @Override
    public float getFloat(String key) {
        return current.getFloat(key);
    }

    @Override
    public double getDouble(String key) {
        return current.getDouble(key);
    }

    @Override
    public byte[] getByteArray(String key) {
        return current.getByteArray(key);
    }

    @Override
    public int[] getIntArray(String key) {
        return current.getIntArray(key);
    }

    @Override
    public long[] getLongArray(String key) {
        return current.getLongArray(key);
    }

    @Override
    public String getString(String key) {
        return current.getString(key);
    }

    @Override
    public int getType(String key) {
        return current.getType(key);
    }

    @Override
    public boolean getBool(String key) {
        return current.getBool(key);
    }

    @Override
    public boolean hasKey(String key) {
        return current.hasKey(key);
    }

    @Override
    public boolean hasKey(String key, int type) {
        return current.hasKey(key, type);
    }

    @Override
    public void clear() {
        current.clear();
    }

    @Override
    public int size() {
        return current.size();
    }

    @Override
    public void pushList() {
        current.pushList();
    }

    @Override
    public void popList() {
        current.popList();
    }

    @Override
    public int getType(int index) {
        return current.getType(index);
    }

    @Override
    public byte getByte(int index) {
        return current.getByte(index);
    }

    @Override
    public short getShort(int index) {
        return current.getShort(index);
    }

    @Override
    public int getInt(int index) {
        return current.getInt(index);
    }

    @Override
    public long getLong(int index) {
        return current.getLong(index);
    }

    @Override
    public float getFloat(int index) {
        return current.getFloat(index);
    }

    @Override
    public double getDouble(int index) {
        return current.getDouble(index);
    }

    @Override
    public byte[] getByteArray(int index) {
        return current.getByteArray(index);
    }

    @Override
    public int[] getIntArray(int index) {
        return current.getIntArray(index);
    }

    @Override
    public long[] getLongArray(int index) {
        return current.getLongArray(index);
    }

    @Override
    public String getString(int index) {
        return current.getString(index);
    }
}
*/
