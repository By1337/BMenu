/*
package dev.by1337.bmenu.slot.render.experemental;


import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import dev.by1337.bmenu.io.nbt.NBTWalker;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class NBTStream {
    private final ByteBuf buf;
    private NBTWalker current;

    public NBTStream(ByteBuf buffer) {
        this.buf = buffer;
        current = new CompoundStream(null);
    }

    private class CompoundStream implements NBTWalker {
        private final @Nullable NBTWalker perv;
        private @Nullable String key;

        private CompoundStream(@Nullable NBTWalker perv) {
            this.perv = perv;
        }

        @Override
        public Set<String> keySet() {
            return Set.of();
        }

        @Override
        public void pushKey(String key) {
            this.key = key;
        }

        @Override
        public void pushObject() {
            buf.writeByte(TAG_COMPOUND);
            DataIOLike.writeUTF(buf, key);
            current = new CompoundStream(this);
        }

        @Override
        public void popObject() {
            current = perv;
            buf.writeByte(0);
        }

        @Override
        public void removeTag(String key) {

        }

        @Override
        public void pushByte(byte v) {
            buf.writeByte(TAG_BYTE);
            DataIOLike.writeUTF(buf, key);
            buf.writeByte(v);
        }

        @Override
        public void pushShort(short v) {
            buf.writeByte(TAG_SHORT);
            DataIOLike.writeUTF(buf, key);
            buf.writeShort(v);
        }

        @Override
        public void pushInt(int v) {
            buf.writeByte(TAG_INT);
            DataIOLike.writeUTF(buf, key);
            buf.writeInt(v);
        }

        @Override
        public void pushLong(long v) {
            buf.writeByte(TAG_LONG);
            DataIOLike.writeUTF(buf, key);
            buf.writeLong(v);
        }

        @Override
        public void pushFloat(float v) {
            buf.writeByte(TAG_FLOAT);
            DataIOLike.writeUTF(buf, key);
            buf.writeFloat(v);
        }

        @Override
        public void pushDouble(double v) {
            buf.writeByte(TAG_DOUBLE);
            DataIOLike.writeUTF(buf, key);
            buf.writeDouble(v);
        }

        @Override
        public void pushByteArray(byte[] v) {
            buf.writeByte(TAG_BYTE_ARRAY);
            DataIOLike.writeUTF(buf, key);
            buf.writeInt(v.length);
            buf.writeBytes(v);
        }

        @Override
        public void pushIntArray(int[] v) {
            buf.writeByte(TAG_INT_ARRAY);
            DataIOLike.writeUTF(buf, key);
            buf.writeInt(v.length);
            for (int i : v) {
                buf.writeInt(i);
            }
        }

        @Override
        public void pushLongArray(long[] v) {
            buf.writeByte(TAG_LONG_ARRAY);
            DataIOLike.writeUTF(buf, key);
            buf.writeInt(v.length);
            for (var i : v) {
                buf.writeLong(i);
            }
        }

        @Override
        public void pushString(String v) {
            buf.writeByte(TAG_STRING);
            DataIOLike.writeUTF(buf, key);
            DataIOLike.writeUTF(buf, v);
        }

        @Override
        public byte getByte(String key) {
            return 0;
        }

        @Override
        public short getShort(String key) {
            return 0;
        }

        @Override
        public int getInt(String key) {
            return 0;
        }

        @Override
        public long getLong(String key) {
            return 0;
        }

        @Override
        public float getFloat(String key) {
            return 0;
        }

        @Override
        public double getDouble(String key) {
            return 0;
        }

        @Override
        public byte[] getByteArray(String key) {
            return new byte[0];
        }

        @Override
        public int[] getIntArray(String key) {
            return new int[0];
        }

        @Override
        public long[] getLongArray(String key) {
            return new long[0];
        }

        @Override
        public String getString(String key) {
            return "";
        }

        @Override
        public int getType(String key) {
            return 0;
        }

        @Override
        public boolean hasKey(String key) {
            return false;
        }

        @Override
        public boolean hasKey(String key, int type) {
            return false;
        }

        @Override
        public void clear() {

        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public void pushList() {
            buf.writeByte(TAG_LIST);
            DataIOLike.writeUTF(buf, key);
            current = new ListStream(this, TAG_STRING);

        }

        @Override
        public void popList() {
           throw new UnsupportedOperationException("Only in list!");
        }


        @Override
        public int getType(int index) {
            return 0;
        }

        @Override
        public byte getByte(int index) {
            return 0;
        }

        @Override
        public short getShort(int index) {
            return 0;
        }

        @Override
        public int getInt(int index) {
            return 0;
        }

        @Override
        public long getLong(int index) {
            return 0;
        }

        @Override
        public float getFloat(int index) {
            return 0;
        }

        @Override
        public double getDouble(int index) {
            return 0;
        }

        @Override
        public byte[] getByteArray(int index) {
            return new byte[0];
        }

        @Override
        public int[] getIntArray(int index) {
            return new int[0];
        }

        @Override
        public long[] getLongArray(int index) {
            return new long[0];
        }

        @Override
        public String getString(int index) {
            return "";
        }
    }

    private class ListStream implements NBTWalker{
        private final @Nullable NBTWalker perv;
        private @Nullable String key;
        private int size;
        private int sizePtr;
        private final int elementType;

        private ListStream(@Nullable NBTWalker perv, int elementType) {
            this.perv = perv;
            this.elementType = elementType;
            buf.writeByte(elementType);
            sizePtr = buf.writerIndex();
            buf.writeInt(0);
        }

        @Override
        public Set<String> keySet() {
            return Set.of();
        }

        @Override
        public void pushKey(String key) {

        }

        @Override
        public void pushObject() {
            //todo
           */
/* if (elementType == TAG_COMPOUND){
                //wrap
                buf.writeByte(TAG_COMPOUND);
                buf.writeShort(0); //empty name
            }*//*

            size++;
            current = new CompoundStream(this);
        }

        @Override
        public void popObject() {

        }

        @Override
        public void removeTag(String key) {

        }

        @Override
        public void pushByte(byte v) {
            size++;
            buf.writeByte(v);
        }

        @Override
        public void pushShort(short v) {
            size++;
            buf.writeShort(v);
        }

        @Override
        public void pushInt(int v) {
            size++;
            buf.writeInt(v);
        }

        @Override
        public void pushLong(long v) {
            size++;
            buf.writeLong(v);
        }

        @Override
        public void pushFloat(float v) {
            size++;
            buf.writeFloat(v);
        }

        @Override
        public void pushDouble(double v) {
            size++;
            buf.writeDouble(v);
        }

        @Override
        public void pushByteArray(byte[] v) {
            size++;
            buf.writeInt(v.length);
            buf.writeBytes(v);
        }

        @Override
        public void pushIntArray(int[] v) {
            size++;
            buf.writeInt(v.length);
            for (int i : v) {
                buf.writeInt(i);
            }
        }

        @Override
        public void pushLongArray(long[] v) {
            size++;
            buf.writeInt(v.length);
            for (var i : v) {
                buf.writeLong(i);
            }
        }

        @Override
        public void pushString(String v) {
            size++;
            DataIOLike.writeUTF(buf, v);
        }

        @Override
        public byte getByte(String key) {
            return 0;
        }

        @Override
        public short getShort(String key) {
            return 0;
        }

        @Override
        public int getInt(String key) {
            return 0;
        }

        @Override
        public long getLong(String key) {
            return 0;
        }

        @Override
        public float getFloat(String key) {
            return 0;
        }

        @Override
        public double getDouble(String key) {
            return 0;
        }

        @Override
        public byte[] getByteArray(String key) {
            return new byte[0];
        }

        @Override
        public int[] getIntArray(String key) {
            return new int[0];
        }

        @Override
        public long[] getLongArray(String key) {
            return new long[0];
        }

        @Override
        public String getString(String key) {
            return "";
        }

        @Override
        public int getType(String key) {
            return 0;
        }

        @Override
        public boolean hasKey(String key) {
            return false;
        }

        @Override
        public boolean hasKey(String key, int type) {
            return false;
        }

        @Override
        public void clear() {

        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public void pushList() {
            size++;
            buf.writeByte(TAG_LIST);
            buf.writeByte(TAG_STRING);
            current = new ListStream(this, TAG_STRING);
        }

        @Override
        public void popList() {
            buf.setInt(sizePtr, size);
            current = perv;
        }

        @Override
        public int getType(int index) {
            return 0;
        }

        @Override
        public byte getByte(int index) {
            return 0;
        }

        @Override
        public short getShort(int index) {
            return 0;
        }

        @Override
        public int getInt(int index) {
            return 0;
        }

        @Override
        public long getLong(int index) {
            return 0;
        }

        @Override
        public float getFloat(int index) {
            return 0;
        }

        @Override
        public double getDouble(int index) {
            return 0;
        }

        @Override
        public byte[] getByteArray(int index) {
            return new byte[0];
        }

        @Override
        public int[] getIntArray(int index) {
            return new int[0];
        }

        @Override
        public long[] getLongArray(int index) {
            return new long[0];
        }

        @Override
        public String getString(int index) {
            return "";
        }
    }

    public void pushKey(String key) {
        current.pushKey(key);
    }

    public void pushObject() {
        current.pushObject();
    }

    public void pushByte(byte v) {
        current.pushByte(v);
    }

    public void pushByte(int v) {
        current.pushByte(v);
    }

    public void pushShort(short v) {
        current.pushShort(v);
    }

    public void pushInt(int v) {
        current.pushInt(v);
    }

    public void pushLong(long v) {
        current.pushLong(v);
    }

    public void pushFloat(float v) {
        current.pushFloat(v);
    }

    public void pushDouble(double v) {
        current.pushDouble(v);
    }

    public void pushByteArray(byte[] v) {
        current.pushByteArray(v);
    }

    public void pushIntArray(int[] v) {
        current.pushIntArray(v);
    }

    public void pushLongArray(long[] v) {
        current.pushLongArray(v);
    }

    public void pushString(String v) {
        current.pushString(v);
    }

    public void pushList() {
        current.pushList();
    }

    public void popList() {
        current.popList();
    }

    public void popObject() {
        current.popObject();
    }

    public static class DataIOLike {

        public static void writeUTF(ByteBuf buf, String s) throws EncoderException {
            int utfLen = 0;
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c >= 0x0001 && c <= 0x007F) {
                    utfLen += 1;
                } else if (c > 0x07FF) {
                    utfLen += 3;
                } else {
                    utfLen += 2;
                }
            }

            if (utfLen > 65535) {
                throw new EncoderException("Encoded string too long: " + utfLen + " bytes");
            }

            buf.writeShort(utfLen); // 2 байта длина

            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c >= 0x0001 && c <= 0x007F) {
                    buf.writeByte(c);
                } else if (c > 0x07FF) {
                    buf.writeByte(0xE0 | ((c >> 12) & 0x0F));
                    buf.writeByte(0x80 | ((c >> 6) & 0x3F));
                    buf.writeByte(0x80 | (c & 0x3F));
                } else {
                    buf.writeByte(0xC0 | ((c >> 6) & 0x1F));
                    buf.writeByte(0x80 | (c & 0x3F));
                }
            }
        }

        public static String readUTF(ByteBuf buf) throws EncoderException {
            int utfLen = buf.readUnsignedShort();
            byte[] byteArr = new byte[utfLen];
            buf.readBytes(byteArr);

            char[] charArr = new char[utfLen];
            int c, char2, char3;
            int count = 0;
            int charCount = 0;

            while (count < utfLen) {
                c = byteArr[count] & 0xFF;
                if (c > 127) break;
                count++;
                charArr[charCount++] = (char) c;
            }

            while (count < utfLen) {
                c = byteArr[count] & 0xFF;
                switch (c >> 4) {
                    case 0x0:
                    case 0x1:
                    case 0x2:
                    case 0x3:
                    case 0x4:
                    case 0x5:
                    case 0x6:
                    case 0x7:
                        count++;
                        charArr[charCount++] = (char) c;
                        break;
                    case 0xC:
                    case 0xD:
                        count += 2;
                        if (count > utfLen)
                            throw new EncoderException("Malformed input: partial character at end");
                        char2 = byteArr[count - 1];
                        charArr[charCount++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
                        break;
                    case 0xE:
                        count += 3;
                        if (count > utfLen)
                            throw new EncoderException("Malformed input: partial character at end");
                        char2 = byteArr[count - 2];
                        char3 = byteArr[count - 1];
                        charArr[charCount++] = (char) (((c & 0x0F) << 12) |
                                ((char2 & 0x3F) << 6) |
                                (char3 & 0x3F));
                        break;
                    default:
                        throw new EncoderException("Malformed input around byte " + count);
                }
            }

            return new String(charArr, 0, charCount);
        }
    }
}
*/
