package nom.tam.util.test;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2015 nom-tam-fits
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

import java.util.Arrays;

import nom.tam.util.ArrayFuncs;
import nom.tam.util.array.MultyArrayCopier;
import nom.tam.util.array.MultyArrayIterator;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class ArrayManipulationTests {

    private double[][][] testArray = {
        {
            {
                1,
                2
            },
            {
                3,
                4,
                5
            },
            {}
        },
        {
            null,
            {
                6,
                7,
                8,
                9
            },
            {
                10,
                11,
                12,
                13,
                14,
                15
            },
            {
                16
            }
        }
    };

    @Test
    public void testArrayIterator() {
        MultyArrayIterator primitiveArrayIterator = new MultyArrayIterator(testArray);

        Assert.assertEquals("[1.0, 2.0]", Arrays.toString(((double[]) primitiveArrayIterator.next())));
        Assert.assertEquals("[3.0, 4.0, 5.0]", Arrays.toString(((double[]) primitiveArrayIterator.next())));
        Assert.assertEquals("[6.0, 7.0, 8.0, 9.0]", Arrays.toString(((double[]) primitiveArrayIterator.next())));
        Assert.assertEquals("[10.0, 11.0, 12.0, 13.0, 14.0, 15.0]", Arrays.toString(((double[]) primitiveArrayIterator.next())));
        Assert.assertEquals("[16.0]", Arrays.toString(((double[]) primitiveArrayIterator.next())));
        Assert.assertNull(primitiveArrayIterator.next());
        Assert.assertEquals(0, primitiveArrayIterator.size());
        primitiveArrayIterator.reset();
        Assert.assertEquals(16, primitiveArrayIterator.size());
        primitiveArrayIterator.reset();
        Assert.assertEquals("[1.0, 2.0]", Arrays.toString(((double[]) primitiveArrayIterator.next())));

    }

    @Test
    public void testSize() {
        Assert.assertEquals(16, new MultyArrayIterator(testArray).size());
        double[][] testTargetArray = new double[2][8];
        MultyArrayCopier.copyInto(testArray, testTargetArray);
        Assert.assertEquals(16, new MultyArrayIterator(testTargetArray).size());
    }

    @Test
    public void testArrayCopy() {
        double[][] testTargetArray = new double[2][8];
        MultyArrayCopier.copyInto(testArray, testTargetArray);
        Assert.assertEquals("[1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0]", Arrays.toString(testTargetArray[0]));
        Assert.assertEquals("[9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0]", Arrays.toString(testTargetArray[1]));
    }

    @Test
    public void testEqualArrayCopy() {
        int[][] testArray = new int[8][8];
        for (int index = 0; index < testArray.length; index++) {
            for (int index2 = 0; index2 < testArray[index].length; index2++) {
                testArray[index][index2] = index + index2;
            }
        }
        int[][] testArrayCopy = new int[8][8];
        MultyArrayCopier.copyInto(testArray, testArrayCopy);
        for (int index = 0; index < testArrayCopy.length; index++) {
            for (int index2 = 0; index2 < testArrayCopy[index].length; index2++) {
                Assert.assertEquals(index + index2, testArrayCopy[index][index2]);
            }
        }
    }

    @Test
    public void testArrayCopyConvert() {
        int[][] testTargetArray = new int[2][8];
        MultyArrayCopier.copyInto(testArray, testTargetArray);
        Assert.assertEquals("[1, 2, 3, 4, 5, 6, 7, 8]", Arrays.toString(testTargetArray[0]));
        Assert.assertEquals("[9, 10, 11, 12, 13, 14, 15, 16]", Arrays.toString(testTargetArray[1]));
    }

    @Test
    @Ignore
    public void testEqualArrayCopyPerf() {

        int[][] testArray = new int[2000][2000];
        for (int index = 0; index < testArray.length; index++) {
            for (int index2 = 0; index2 < testArray[index].length; index2++) {
                testArray[index][index2] = index + index2;
            }
        }
        long[][] testArrayCopy = new long[2000][2000];
        // let them both initialize a little
        MultyArrayCopier.copyInto(testArray, testArrayCopy);
        ArrayFuncs.copyInto(testArray, testArrayCopy);
        long timeCopy = 0;
        long timeIter = 0;
        long start;
        for (int loop = 0; loop < 50; loop++) {

            start = System.currentTimeMillis();
            for (int index = 0; index < 100; index++) {
                MultyArrayCopier.copyInto(testArray, testArrayCopy);
            }
            timeIter += System.currentTimeMillis() - start;

            start = System.currentTimeMillis();
            for (int index = 0; index < 100; index++) {
                ArrayFuncs.copyInto(testArray, testArrayCopy);
            }
            timeCopy += System.currentTimeMillis() - start;
        }
        System.out.println("timeCopy:" + timeCopy);
        System.out.println("timeIter:" + timeIter);
        for (int index = 0; index < testArrayCopy.length; index++) {
            for (int index2 = 0; index2 < testArrayCopy[index].length; index2++) {
                Assert.assertEquals(index + index2, testArrayCopy[index][index2]);
            }
        }

    }

}
