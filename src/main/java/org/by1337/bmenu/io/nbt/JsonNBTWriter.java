package org.by1337.bmenu.io.nbt;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;

public class JsonNBTWriter implements NBTWalker{
    private final Appendable writer;
    private boolean separator;

    public JsonNBTWriter(Appendable writer) {
        this.writer = writer;
    }

    @Override
    public void pushObject() {
        try {
            if (separator) writer.append(',');
            writer.append('{');
            separator = false;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void popObject() {
        try {
            writer.append('}');
            separator = true;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void pushKey(String key) {
        try {
            if (separator) writer.append(',');
            writer.append("\"");
            appendEscaped(key);
            writer.append("\"").append(':');
            separator = false;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void removeTag(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void pushByte(byte v) {
        try {
            if (separator) writer.append(',');
            writer.append(Byte.toString(v)).append("b");
            separator = true;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    @Override
    public void pushShort(short v) {
        try {
            if (separator) writer.append(',');
            writer.append(Short.toString(v)).append("s");
            separator = true;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void pushInt(int v) {
        try {
            if (separator) writer.append(',');
            writer.append(Integer.toString(v));
            separator = true;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void pushLong(long v) {
        try {
            if (separator) writer.append(',');
            writer.append(Long.toString(v)).append("l");
            separator = true;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void pushFloat(float v) {
        try {
            if (separator) writer.append(',');
            writer.append(Float.toString(v)).append("f");
            separator = true;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void pushDouble(double v) {
        try {
            if (separator) writer.append(',');
            writer.append(Double.toString(v)).append("d");
            separator = true;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void pushByteArray(byte[] v) {
        try {
            if (separator) writer.append(',');
            writer.append("[B;");
            for (int i = 0; i < v.length; i++) {
                if (i != 0){
                    writer.append(',');
                }
                writer.append(Byte.toString(v[i])).append("B");
            }
            writer.append(']');
            separator = true;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void pushIntArray(int[] v) {
        try {
            if (separator) writer.append(',');
            writer.append("[I;");
            for (int i = 0; i < v.length; i++) {
                if (i != 0){
                    writer.append(',');
                }
                writer.append(Integer.toString(v[i]));
            }
            writer.append(']');
            separator = true;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void pushLongArray(long[] v) {
        try {
            if (separator) writer.append(',');
            writer.append("[L;");
            for (int i = 0; i < v.length; i++) {
                if (i != 0){
                    writer.append(',');
                }
                writer.append(Long.toString(v[i])).append("L");
            }
            writer.append(']');
            separator = true;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void pushString(String v) {
        try {
            writer.append("\"");
            appendEscaped(v);
            writer.append("\"");
            separator = true;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public byte getByte(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public short getShort(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getInt(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLong(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getFloat(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getDouble(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getByteArray(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] getIntArray(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long[] getLongArray(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getString(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getType(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasKey(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasKey(String key, int type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void pushList() {
        try {
            if (separator) writer.append(',');
            writer.append('[');
            separator = false;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public final void popList() {
        try {
            writer.append(']');
            separator = true;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public int getType(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte getByte(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public short getShort(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getInt(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLong(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getFloat(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getDouble(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getByteArray(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] getIntArray(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long[] getLongArray(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getString(int index) {
        throw new UnsupportedOperationException();
    }


    public final char appendChar(char c) {
        try {
            appendEscaped(c);
            return c;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void appendEscaped(String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            appendEscaped(s.charAt(i));
        }
    }

    private void appendEscaped(char c) throws IOException {
        switch (c) {
            case '"':
                writer.append("\\\"");
                break;
            case '\\':
                writer.append("\\\\");
                break;
            case '\n':
                writer.append("\\n");
                break;
            case '\r':
                writer.append("\\r");
                break;
            case '\t':
                writer.append("\\t");
                break;
            default:
                writer.append(c);
        }

    }
}
