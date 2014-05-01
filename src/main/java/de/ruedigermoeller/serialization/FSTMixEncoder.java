package de.ruedigermoeller.serialization;

import de.ruedigermoeller.serialization.minbin.MBOut;
import de.ruedigermoeller.serialization.minbin.MBPrinter;
import de.ruedigermoeller.serialization.minbin.MinBin;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Copyright (c) 2012, Ruediger Moeller. All rights reserved.
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 * <p/>
 * Date: 30.03.2014
 * Time: 18:47
 * To change this template use File | Settings | File Templates.
 */
public class FSTMixEncoder implements FSTEncoder {

    MBOut out = new MBOut();
    OutputStream outputStream;
    FSTConfiguration conf;
    private int offset = 0;

    public FSTMixEncoder(FSTConfiguration conf) {
        this.conf = conf;
    }

    @Override
    public void writeRawBytes(byte[] bufferedName, int off, int length) throws IOException {
        out.writeArray(bufferedName, off, length);
    }

    /**
     * does not write class tag and length
     *
     * @param array
     * @param start
     * @param length @throws java.io.IOException
     */
    @Override
    public void writePrimitiveArray(Object array, int start, int length) throws IOException {
        out.writeArray(array, start, length);
    }

    @Override
    public void writeStringUTF(String str) throws IOException {
        out.writeTag(str);
    }

    @Override
    public void writeFShort(short c) throws IOException {
        out.writeInt(MinBin.INT_16,c);
    }

    @Override
    public void writeFChar(char c) throws IOException {
        out.writeInt(MinBin.CHAR, c);
    }

    @Override
    public void writeFByte(int v) throws IOException {
        out.writeInt(MinBin.INT_8, v);
    }

    @Override
    public void writeFInt(int anInt) throws IOException {
        out.writeInt(MinBin.INT_32,anInt);
    }

    @Override
    public void writeFLong(long anInt) throws IOException {
        out.writeInt(MinBin.INT_64,anInt);
    }

    @Override
    public void writeFFloat(float value) throws IOException {
        out.writeTag(value);
    }

    @Override
    public void writeFDouble(double value) throws IOException {
        out.writeTag(value);
    }

    @Override
    public int getWritten() {
        return out.getWritten()+offset;
    }

    @Override
    public void skip(int i) {
        throw new RuntimeException("not supported");
    }

    /**
     * close and flush to underlying stream if present. The stream is also closed
     *
     * @throws java.io.IOException
     */
    @Override
    public void close() throws IOException {
        if ( outputStream != null ) {
            
            outputStream.close();
        }
    }

    @Override
    public void reset() {
        offset = 0;
        out.reset();
    }

    @Override
    public void reset(byte[] bytez) {
        offset = 0;
        out.reset(bytez);
    }

    /**
     * resets stream (positions are lost)
     *
     * @throws java.io.IOException
     */
    @Override
    public void flush() throws IOException {
        if ( outputStream != null ) {
            outputStream.write(out.getBytez(),0,out.getWritten());
            offset = out.getWritten();
            out.resetPosition();
        }
    }

    /**
     * used to write uncompressed int (guaranteed length = 4) at a (eventually recent) position
     *
     * @param position
     * @param v
     */
    @Override
    public void writeInt32At(int position, int v) {
        throw new RuntimeException("not supported");
    }

    /**
     * if output stream is null, just encode into a byte array
     *
     * @param outstream
     */
    @Override
    public void setOutstream(OutputStream outstream) {
        this.outputStream = outstream;
    }

    @Override
    public void ensureFree(int bytes) throws IOException {

    }

    @Override
    public byte[] getBuffer() {
        return out.getBytez();
    }

    @Override
    public void registerClass(Class possible) {

    }

    @Override
    public void writeClass(Class cl) {
       // already written in write tag
    }

    @Override
    public void writeClass(FSTClazzInfo clInf) {
        // already written in write tag
    }

    @Override
    public void writeAttributeName(FSTClazzInfo.FSTFieldInfo subInfo) {
        out.writeTag(subInfo.getField().getName());
    }
    
    @Override
    public boolean writeTag(byte tag, Object infoOrObject, long somValue, Object toWrite) throws IOException {
        switch (tag) {
            case FSTObjectOutput.HANDLE:
                out.writeTagHeader(MinBin.HANDLE);
                out.writeInt(MinBin.INT_32,somValue);
                return true;
            case FSTObjectOutput.NULL:
                out.writeTag(null);
                return true;
            case FSTObjectOutput.TYPED:
            case FSTObjectOutput.OBJECT:
                FSTClazzInfo clzInfo = (FSTClazzInfo) infoOrObject;
                if (clzInfo.getClazz() == String.class )
                    break;
                if (clzInfo.getClazz() == Double.class )
                    break;
                if (clzInfo.getClazz() == Float.class )
                    break;
                if (clzInfo.getClazz() == Byte.class )
                    break;
                if (clzInfo.getClazz() == Short.class )
                    break;
                if (clzInfo.getClazz() == Integer.class )
                    break;
                if (clzInfo.getClazz() == Long.class )
                    break;
                if (clzInfo.getClazz() == Character.class )
                    break;
                if (clzInfo.getClazz() == Boolean.class )
                    break;
//                if ( clzInfo.getClazz() == Byte.class || clzInfo.getClazz() == Short.class || clzInfo.getClazz() == Character.class ) {
//                    out.writeObject(toWrite);
//                    return true;
//                }
                if ( clzInfo.getSer()!=null ) {
                    out.writeTagHeader(MinBin.SEQUENCE);
                    out.writeTag(classToString(clzInfo.getClazz()));
                    out.writeIntPacked(-1); // END Marker required
                } else
                {
                    out.writeTagHeader(MinBin.OBJECT);
                    out.writeTag(classToString(clzInfo.getClazz()));
                    out.writeIntPacked(clzInfo.getFieldInfo().length);
                }
                break;
            case FSTObjectOutput.ONE_OF:
                throw new RuntimeException("not implemented");
            case FSTObjectOutput.STRING:
                break; // ignore, header created by calling writeUTF
            case FSTObjectOutput.BIG_BOOLEAN_FALSE:
                out.writeTag(Boolean.FALSE);
                break; // ignore, header created by writing long. FIXME: won't work
            case FSTObjectOutput.BIG_BOOLEAN_TRUE:
                out.writeTag(Boolean.TRUE);
                break; // ignore, header created by writing long. FIXME: won't work
            case FSTObjectOutput.BIG_LONG:
                break; // ignore, header created by writing long. FIXME: won't work
            case FSTObjectOutput.BIG_INT:
                break; // ignore, header created by writing int. FIXME: won't work
            case FSTObjectOutput.ARRAY:
                Class<?> clz = infoOrObject.getClass();
                Class<?> componentType = clz.getComponentType();
                if ( clz.isArray() && componentType.isPrimitive() )
                {
                    if ( componentType == double.class ) {
                        out.writeTagHeader(MinBin.SEQUENCE);
                        out.writeTag(classToString(clz));
                        int length = Array.getLength(infoOrObject);
                        out.writeIntPacked(length);
                        for ( int i = 0; i < length; i++ ) {
                            out.writeTag(Array.getDouble(infoOrObject,i));
                        }
                    } else if ( componentType == float.class ) {
                        out.writeTagHeader(MinBin.SEQUENCE);
                        out.writeTag(classToString(clz));
                        int length = Array.getLength(infoOrObject);
                        out.writeIntPacked(length);
                        for ( int i = 0; i < length; i++ ) {
                            out.writeTag(Array.getFloat(infoOrObject, i));
                        }
                    } else {
                        out.writeArray(infoOrObject, 0, Array.getLength(infoOrObject));
                    }
                    return true;
                } else {
                    out.writeTagHeader(MinBin.SEQUENCE);
                    out.writeTag(classToString(clz));
                }
                break;
            case FSTObjectOutput.ENUM:
                throw new RuntimeException("not supported");
            default:
                throw new RuntimeException("unexpected tag "+tag);
        }
        return false;
    }

    protected String classToString(Class clz) {
        return conf.getCPNameForClass(clz);
    }

    public void externalEnd(FSTClazzInfo clz) {
        if ( clz == null || (clz.getSer() instanceof FSTCrossPlatformSerialzer && ((FSTCrossPlatformSerialzer) clz.getSer()).writeTupleEnd()) )
            out.writeTag(MinBin.END_MARKER);
    }

    @Override
    public boolean isWritingAttributes() {
        return true;
    }

    static class BigNums implements Serializable {

        Boolean _aBoolean = false;
        Boolean ugly[][] = {{true,false},null,{true,false}};

        Byte _aByte0 = -13;
        Object _aByte1 = Byte.MIN_VALUE;
        Byte _aByte2 = Byte.MAX_VALUE;

        Short _aShort0 = -1334;
        Object _aShort1 = Short.MIN_VALUE;
        Short _aShort2 = Short.MAX_VALUE;

        Character _aChar0 = 35345;
        Object _aChar1 = Character.MIN_VALUE;
        Character _aChar2 = Character.MAX_VALUE;

        Integer _aInt0 = 35345;
        Object _aInt1 = Integer.MIN_VALUE;
        Integer _aInt2 = Integer.MAX_VALUE;

        Long _aLong0 = -34564567l;
        Object _aLong1 = Long.MIN_VALUE;
        Long _aLong2 = Long.MAX_VALUE;

        Float _aFloat0 = 123.66f;
        Object _aFloat1 = Float.MIN_VALUE;
        Float _aFloat2 = Float.MAX_VALUE;

        Double _aDouble0 = 123.66d;
        Object _aDouble1 = Double.MIN_VALUE;
        Double _aDouble2 = Double.MAX_VALUE;
    }
    
    static class MixTester implements Serializable {
        boolean x;
        double dda[] = {1112323.342,11234,-11234,114234.3,11234443453.1};
        double d = 2334534.223434;
        String s = "Hallo";
        Object strOb = "StrObj";
        Integer bigInt = 234;
        Object obs[] = { 34,55d };
        int arr[] = {1,2,3,4,5,6};
        ArrayList l = new ArrayList();
        HashMap mp = new HashMap();
        short sh = 34;
        int in = 34234;
        boolean y = true;
        Dimension _da[] = {new Dimension(1,2),new Dimension(3,4)};
        int iiii[][][] = new int[][][] { { {1,2,3}, {4,5,6} }, { {7,8,9}, {10,11,12} } };
        Object iii = new int[][] {{1,2,3},{4,5,6}};
        Dimension dim[][][] = new Dimension[][][] {
            {
                { new Dimension(11,10) },
                { new Dimension(9,10), new Dimension(1666661,11) }
            }
        };
        Boolean bigBa[][] = { {true,false,true},{true,false,true,true}};
        boolean ba[] = {true,false,true};
        Boolean b1a[] = {true,false,true};
        Integer bia[][] = {{1,2,3}};

        public MixTester() {
//            l.add("asdasd");
//            l.add(3425);
//            l.add(new Rectangle(1,2,3,4));
//            mp.put("name", 9999);
//            mp.put(349587, "number");
//            mp.put(3497, new Dimension[] {new Dimension(0,0), new Dimension(1,1)} );
        }
    }

    public static void main(String arg[]) throws IOException, ClassNotFoundException {

        FSTConfiguration conf = FSTConfiguration.createCrossPlatformConfiguration();
        conf.registerCrossPlatformClassMapping( new String[][] {
                { "mixtest", MixTester.class.getName() },
                { "rect", Rectangle.class.getName() },
                { "dim", Dimension.class.getName() },
                { "dim[3]", Dimension[][][].class.getName() },
                { "dim[2]", Dimension[][].class.getName() },
                { "dim[1]", Dimension[].class.getName() },
                { "int[2]", int[][].class.getName() },
                { "int[3]", int[][][].class.getName() },
        } );
        FSTObjectOutput out = new FSTObjectOutput(conf);

        HashMap obj = new HashMap();
        ArrayList li = new ArrayList(); li.add("zero"); li.add("second");
        obj.put("x", li);
//        obj.put("in", new int[]{1,2,3,4});
//        obj.put("y", li);
        obj.put(4,"99999");

        out.writeObject(obj);
//        out.writeObject(new BigNums());
//        out.writeObject(new int[][] {{99,98,97}, {77,76,75}});
        MBPrinter.printMessage(out.getBuffer(), System.out);

        FSTObjectInput fin = new FSTObjectInput(conf);
        fin.resetForReuseUseArray(out.getBuffer(),out.getWritten());
        Object deser = fin.readObject();
        System.out.println("");
        System.out.println("SIZE "+out.getWritten());

    }

    public boolean isPrimitiveArray(Object array, Class<?> componentType) {
        return componentType.isPrimitive() && array instanceof double[] == false && array instanceof float[] == false;
    }

    public boolean isTagMultiDimSubArrays() {
        return true;
    }
}