package nom.tam.util;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * This is free and unencumbered software released into the public domain.
 * 
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * 
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * #L%
 */

// What do we use in here?
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class is intended for high performance I/O in scientific applications.
 * It combines the functionality of the BufferedOutputStream and the
 * DataOutputStream as well as more efficient handling of arrays. This minimizes
 * the number of method calls that are required to write data. Informal tests of
 * this method show that it can be as much as 10 times faster than using a
 * DataOutputStream layered on a BufferedOutputStream for writing large arrays.
 * The performance gain on scalars or small arrays will be less but there should
 * probably never be substantial degradation of performance.
 * <p>
 * Note that there is substantial duplication of code to minimize method
 * invocations. However simple output methods were used where empirical tests
 * seemed to indicate that the simpler method did not cost any time. It seems
 * likely that most of these variations will be washed out across different
 * compilers and users who wish to tune the method for their particular system
 * may wish to compare the the implementation of write(int[], int, int) with
 * write(float[], int, int).
 * <p>
 * Testing and timing for this class is peformed in the
 * nom.tam.util.test.BufferedFileTester class.
 */
public class BufferedDataOutputStream extends BufferedOutputStream implements ArrayDataOutput {

    /**
     * Use the BufferedOutputStream constructor
     * 
     * @param o
     *            An open output stream.
     */
    public BufferedDataOutputStream(OutputStream o) {
        super(o, 32768);
    }

    /**
     * Use the BufferedOutputStream constructor
     * 
     * @param o
     *            An open output stream.
     * @param bufLength
     *            The buffer size.
     */
    public BufferedDataOutputStream(OutputStream o, int bufLength) {
        super(o, bufLength);
    }

    /**
     * See if there is enough space to add something to the buffer.
     * 
     * @param need
     *            the number of bytes that should be available in the buffer.
     * @throws IOException
     *             if the underlying write operation fails
     */
    protected void checkBuf(int need) throws IOException {

        if (this.count + need > this.buf.length) {
            this.out.write(this.buf, 0, this.count);
            this.count = 0;
        }
    }

    @Override
    public void write(boolean[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(boolean[] b, int start, int len) throws IOException {

        for (int i = start; i < start + len; i += 1) {

            if (this.count + 1 > this.buf.length) {
                checkBuf(1);
            }
            if (b[i]) {
                this.buf[this.count++] = 1;
            } else {
                this.buf[this.count++] = 0;
            }
        }
    }

    @Override
    public void write(char[] c) throws IOException {
        write(c, 0, c.length);
    }

    @Override
    public void write(char[] c, int start, int len) throws IOException {

        for (int i = start; i < start + len; i += 1) {
            if (this.count + 2 > this.buf.length) {
                checkBuf(2);
            }
            this.buf[this.count++] = (byte) (c[i] >> 8);
            this.buf[this.count++] = (byte) c[i];
        }
    }

    @Override
    public void write(double[] d) throws IOException {
        write(d, 0, d.length);
    }

    @Override
    public void write(double[] d, int start, int len) throws IOException {

        for (int i = start; i < start + len; i += 1) {
            if (this.count + 8 > this.buf.length) {
                checkBuf(8);
            }
            long t = Double.doubleToLongBits(d[i]);

            int ix = (int) (t >>> 32);

            this.buf[this.count++] = (byte) (ix >>> 24);
            this.buf[this.count++] = (byte) (ix >>> 16);
            this.buf[this.count++] = (byte) (ix >>> 8);
            this.buf[this.count++] = (byte) ix;

            ix = (int) t;

            this.buf[this.count++] = (byte) (ix >>> 24);
            this.buf[this.count++] = (byte) (ix >>> 16);
            this.buf[this.count++] = (byte) (ix >>> 8);
            this.buf[this.count++] = (byte) ix;
        }

    }

    @Override
    public void write(float[] f) throws IOException {
        write(f, 0, f.length);
    }

    @Override
    public void write(float[] f, int start, int len) throws IOException {

        for (int i = start; i < start + len; i += 1) {

            if (this.count + 4 > this.buf.length) {
                checkBuf(4);
            }
            int t = Float.floatToIntBits(f[i]);
            this.buf[this.count++] = (byte) (t >>> 24);
            this.buf[this.count++] = (byte) (t >>> 16);
            this.buf[this.count++] = (byte) (t >>> 8);
            this.buf[this.count++] = (byte) t;
        }
    }

    @Override
    public void write(int[] i) throws IOException {
        write(i, 0, i.length);
    }

    @Override
    public void write(int[] i, int start, int len) throws IOException {

        for (int ii = start; ii < start + len; ii += 1) {
            if (this.count + 4 > this.buf.length) {
                checkBuf(4);
            }

            this.buf[this.count++] = (byte) (i[ii] >>> 24);
            this.buf[this.count++] = (byte) (i[ii] >>> 16);
            this.buf[this.count++] = (byte) (i[ii] >>> 8);
            this.buf[this.count++] = (byte) i[ii];

        }

    }

    @Override
    public void write(long[] l) throws IOException {
        write(l, 0, l.length);
    }

    @Override
    public void write(long[] l, int start, int len) throws IOException {

        for (int i = start; i < start + len; i += 1) {
            if (this.count + 8 > this.buf.length) {
                checkBuf(8);
            }
            int t = (int) (l[i] >>> 32);

            this.buf[this.count++] = (byte) (t >>> 24);
            this.buf[this.count++] = (byte) (t >>> 16);
            this.buf[this.count++] = (byte) (t >>> 8);
            this.buf[this.count++] = (byte) t;

            t = (int) l[i];

            this.buf[this.count++] = (byte) (t >>> 24);
            this.buf[this.count++] = (byte) (t >>> 16);
            this.buf[this.count++] = (byte) (t >>> 8);
            this.buf[this.count++] = (byte) t;
        }
    }

    @Override
    public void write(short[] s) throws IOException {
        write(s, 0, s.length);
    }

    @Override
    public void write(short[] s, int start, int len) throws IOException {

        for (int i = start; i < start + len; i += 1) {
            if (this.count + 2 > this.buf.length) {
                checkBuf(2);
            }
            this.buf[this.count++] = (byte) (s[i] >> 8);
            this.buf[this.count++] = (byte) s[i];
        }
    }

    @Override
    public void write(String[] s) throws IOException {
        write(s, 0, s.length);
    }

    @Override
    public void write(String[] s, int start, int len) throws IOException {

        // Do not worry about buffering this specially since the
        // strings may be of differing lengths.

        for (String element : s) {
            writeBytes(element);
        }
    }

    @Override
    public void writeArray(Object o) throws IOException {
        if (!o.getClass().isArray()) {
            throw new IOException("Invalid object passed to BufferedDataOutputStream.write" + o.getClass().getName());
        }

        // Is this a multidimensional array? If so process recursively.
        if (o.getClass().getComponentType().isArray()) {
            for (int i = 0; i < ((Object[]) o).length; i += 1) {
                writeArray(((Object[]) o)[i]);
            }
        } else {
            if (o instanceof boolean[]) {
                write((boolean[]) o, 0, ((boolean[]) o).length);
            } else if (o instanceof byte[]) {
                write((byte[]) o, 0, ((byte[]) o).length);
            } else if (o instanceof char[]) {
                write((char[]) o, 0, ((char[]) o).length);
            } else if (o instanceof short[]) {
                write((short[]) o, 0, ((short[]) o).length);
            } else if (o instanceof int[]) {
                write((int[]) o, 0, ((int[]) o).length);
            } else if (o instanceof long[]) {
                write((long[]) o, 0, ((long[]) o).length);
            } else if (o instanceof float[]) {
                write((float[]) o, 0, ((float[]) o).length);
            } else if (o instanceof double[]) {
                write((double[]) o, 0, ((double[]) o).length);
            } else if (o instanceof String[]) {
                write((String[]) o, 0, ((String[]) o).length);
            } else {
                for (int i = 0; i < ((Object[]) o).length; i += 1) {
                    writeArray(((Object[]) o)[i]);
                }
            }
        }
    }

    @Override
    public void writeBoolean(boolean b) throws IOException {

        checkBuf(1);
        if (b) {
            this.buf[this.count++] = 1;
        } else {
            this.buf[this.count++] = 0;
        }
    }

    @Override
    public void writeByte(int b) throws IOException {
        checkBuf(1);
        this.buf[this.count++] = (byte) b;
    }

    @Override
    public void writeBytes(String s) throws IOException {
        write(AsciiFuncs.getBytes(s), 0, s.length());
    }

    @Override
    public void writeChar(int c) throws IOException {

        checkBuf(2);
        this.buf[this.count++] = (byte) (c >>> 8);
        this.buf[this.count++] = (byte) c;
    }

    @Override
    public void writeChars(String s) throws IOException {

        for (int i = 0; i < s.length(); i += 1) {
            writeChar(s.charAt(i));
        }
    }

    @Override
    public void writeDouble(double d) throws IOException {

        checkBuf(8);
        long l = Double.doubleToLongBits(d);

        this.buf[this.count++] = (byte) (l >>> 56);
        this.buf[this.count++] = (byte) (l >>> 48);
        this.buf[this.count++] = (byte) (l >>> 40);
        this.buf[this.count++] = (byte) (l >>> 32);
        this.buf[this.count++] = (byte) (l >>> 24);
        this.buf[this.count++] = (byte) (l >>> 16);
        this.buf[this.count++] = (byte) (l >>> 8);
        this.buf[this.count++] = (byte) l;

    }

    @Override
    public void writeFloat(float f) throws IOException {

        checkBuf(4);

        int i = Float.floatToIntBits(f);

        this.buf[this.count++] = (byte) (i >>> 24);
        this.buf[this.count++] = (byte) (i >>> 16);
        this.buf[this.count++] = (byte) (i >>> 8);
        this.buf[this.count++] = (byte) i;

    }

    @Override
    public void writeInt(int i) throws IOException {

        checkBuf(4);
        this.buf[this.count++] = (byte) (i >>> 24);
        this.buf[this.count++] = (byte) (i >>> 16);
        this.buf[this.count++] = (byte) (i >>> 8);
        this.buf[this.count++] = (byte) i;
    }

    @Override
    public void writeLong(long l) throws IOException {

        checkBuf(8);

        this.buf[this.count++] = (byte) (l >>> 56);
        this.buf[this.count++] = (byte) (l >>> 48);
        this.buf[this.count++] = (byte) (l >>> 40);
        this.buf[this.count++] = (byte) (l >>> 32);
        this.buf[this.count++] = (byte) (l >>> 24);
        this.buf[this.count++] = (byte) (l >>> 16);
        this.buf[this.count++] = (byte) (l >>> 8);
        this.buf[this.count++] = (byte) l;
    }

    /**
     * Deprecated use {@link #writeArray(Object)}.
     * 
     * @param o
     *            The object to be written.
     * @throws IOException
     *             if one of the underlying write operations failed
     * @see #writeArray(Object)
     */
    @Deprecated
    public void writePrimitiveArray(Object o) throws IOException {
        writeArray(o);
    }

    /**
     * Write a short value.
     */
    @Override
    public void writeShort(int s) throws IOException {

        checkBuf(2);
        this.buf[this.count++] = (byte) (s >>> 8);
        this.buf[this.count++] = (byte) s;

    }

    @Override
    public void writeUTF(String s) throws IOException {
        // Punt on this one and use standard routines.
        DataOutputStream d = new DataOutputStream(this);
        d.writeUTF(s);
        d.flush();
        // do not close the dataoutput stream because it propagates to ourself.
    }
}
