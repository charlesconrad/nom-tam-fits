package nom.tam.fits;

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

import java.io.EOFException;
import java.io.IOException;

import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;

/**
 * This class provides a simple holder for data which is not handled by other
 * classes.
 */
public class UndefinedData extends Data {

    private static final int BITS_PER_BYTE = 8;

    private byte[] data;

    public UndefinedData(Header h) throws FitsException {

        /**
         * Just get a byte buffer to hold the data.
         */
        // Bug fix by Vincenzo Forzi.
        int naxis = h.getIntValue("NAXIS");

        int size = naxis > 0 ? 1 : 0;
        for (int i = 0; i < naxis; i += 1) {
            size *= h.getIntValue("NAXIS" + (i + 1));
        }
        size += h.getIntValue("PCOUNT");
        if (h.getIntValue("GCOUNT") > 1) {
            size *= h.getIntValue("GCOUNT");
        }
        size *= Math.abs(h.getIntValue("BITPIX") / BITS_PER_BYTE);

        this.data = new byte[size];
    }

    /**
     * Create an UndefinedData object using the specified object.
     * 
     * @param x
     *            object to create the hdu from
     */
    public UndefinedData(Object x) {
        this.data = new byte[(int) ArrayFuncs.computeLSize(x)];
        ArrayFuncs.copyInto(x, this.data);
    }

    /**
     * Fill header with keywords that describe data.
     * 
     * @param head
     *            The FITS header
     */
    @Override
    protected void fillHeader(Header head) {

        try {
            head.setXtension("UNKNOWN");
            head.setBitpix(BasicHDU.BITPIX_BYTE);
            head.setNaxes(1);
            head.addValue("NAXIS1", this.data.length, "ntf::undefineddata:naxis1:1");
            head.addValue("PCOUNT", 0, "ntf::undefineddata:pcount:1");
            head.addValue("GCOUNT", 1, "ntf::undefineddata:gcount:1");
            head.addValue("EXTEND", true, "ntf::undefineddata:extend:1"); // Just
                                                                          // in
                                                                          // case!
        } catch (HeaderCardException e) {
            System.err.println("Unable to create unknown header:" + e);
        }

    }

    @Override
    public Object getData() {
        return this.data;
    }

    /** Get the size in bytes of the data */
    @Override
    protected long getTrueSize() {
        return this.data.length;
    }

    @Override
    public void read(ArrayDataInput i) throws FitsException {
        setFileOffset(i);
        try {
            i.readFully(this.data);
        } catch (IOException e) {
            throw new FitsException("Unable to read unknown data:" + e);
        }

        int pad = FitsUtil.padding(getTrueSize());
        try {
            i.skipBytes(pad);
        } catch (EOFException e) {
            throw new PaddingException("EOF skipping padding in undefined data", this);
        } catch (IOException e) {
            throw new FitsException("Error skipping padding in undefined data");
        }
    }

    @Override
    public void write(ArrayDataOutput o) throws FitsException {
        try {
            o.write(this.data);
        } catch (IOException e) {
            throw new FitsException("IO Error on unknown data write" + e);
        }
        FitsUtil.pad(o, getTrueSize());
    }
}
