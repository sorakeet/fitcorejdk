/**
 * Copyright (c) 2000, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio;

import sun.misc.JavaLangRefAccess;
import sun.misc.SharedSecrets;
import sun.misc.Unsafe;
import sun.misc.VM;

import java.security.AccessController;
import java.util.concurrent.atomic.AtomicLong;

class Bits{                            // package-private
    // -- Bulk get/put acceleration --
    // These numbers represent the point at which we have empirically
    // determined that the average cost of a JNI call exceeds the expense
    // of an element by element copy.  These numbers may change over time.
    static final int JNI_COPY_TO_ARRAY_THRESHOLD=6;
    // -- Swapping --
    static final int JNI_COPY_FROM_ARRAY_THRESHOLD=6;
    // This number limits the number of bytes to copy per call to Unsafe's
    // copyMemory method. A limit is imposed to allow for safepoint polling
    // during a large copy
    static final long UNSAFE_COPY_THRESHOLD=1024L*1024L;
    // -- Unsafe access --
    private static final Unsafe unsafe=Unsafe.getUnsafe();
    // -- Processor and memory-system properties --
    private static final ByteOrder byteOrder;
    // -- get/put char --
    private static final AtomicLong reservedMemory=new AtomicLong();
    private static final AtomicLong totalCapacity=new AtomicLong();
    private static final AtomicLong count=new AtomicLong();
    // max. number of sleeps during try-reserving with exponentially
    // increasing delay before throwing OutOfMemoryError:
    // 1, 2, 4, 8, 16, 32, 64, 128, 256 (total 511 ms ~ 0.5 s)
    // which means that OOME will be thrown after 0.5 s of trying
    private static final int MAX_SLEEPS=9;
    private static int pageSize=-1;
    private static boolean unaligned;
    private static boolean unalignedKnown=false;
    // -- Direct memory management --
    // A user-settable upper limit on the maximum amount of allocatable
    // direct buffer memory.  This value may be changed during VM
    // initialization if it is launched with "-XX:MaxDirectMemorySize=<size>".
    private static volatile long maxMemory=VM.maxDirectMemory();
    private static volatile boolean memoryLimitSet=false;

    static{
        long a=unsafe.allocateMemory(8);
        try{
            unsafe.putLong(a,0x0102030405060708L);
            byte b=unsafe.getByte(a);
            switch(b){
                case 0x01:
                    byteOrder=ByteOrder.BIG_ENDIAN;
                    break;
                case 0x08:
                    byteOrder=ByteOrder.LITTLE_ENDIAN;
                    break;
                default:
                    assert false;
                    byteOrder=null;
            }
        }finally{
            unsafe.freeMemory(a);
        }
    }

    static{
        // setup access to this package in SharedSecrets
        SharedSecrets.setJavaNioAccess(
                new sun.misc.JavaNioAccess(){
                    @Override
                    public BufferPool getDirectBufferPool(){
                        return new BufferPool(){
                            @Override
                            public String getName(){
                                return "direct";
                            }

                            @Override
                            public long getCount(){
                                return Bits.count.get();
                            }

                            @Override
                            public long getTotalCapacity(){
                                return Bits.totalCapacity.get();
                            }

                            @Override
                            public long getMemoryUsed(){
                                return Bits.reservedMemory.get();
                            }
                        };
                    }

                    @Override
                    public ByteBuffer newDirectByteBuffer(long addr,int cap,Object ob){
                        return new DirectByteBuffer(addr,cap,ob);
                    }

                    @Override
                    public void truncate(Buffer buf){
                        buf.truncate();
                    }
                });
    }

    private Bits(){
    }

    static short swap(short x){
        return Short.reverseBytes(x);
    }

    static char swap(char x){
        return Character.reverseBytes(x);
    }

    static int swap(int x){
        return Integer.reverseBytes(x);
    }
    // -- get/put short --

    static long swap(long x){
        return Long.reverseBytes(x);
    }

    static char getChar(ByteBuffer bb,int bi,boolean bigEndian){
        return bigEndian?getCharB(bb,bi):getCharL(bb,bi);
    }

    static char getCharL(ByteBuffer bb,int bi){
        return makeChar(bb._get(bi+1),
                bb._get(bi));
    }

    static char getCharB(ByteBuffer bb,int bi){
        return makeChar(bb._get(bi),
                bb._get(bi+1));
    }

    static private char makeChar(byte b1,byte b0){
        return (char)((b1<<8)|(b0&0xff));
    }

    static char getChar(long a,boolean bigEndian){
        return bigEndian?getCharB(a):getCharL(a);
    }

    static char getCharL(long a){
        return makeChar(_get(a+1),
                _get(a));
    }

    static char getCharB(long a){
        return makeChar(_get(a),
                _get(a+1));
    }

    private static byte _get(long a){
        return unsafe.getByte(a);
    }

    static void putChar(ByteBuffer bb,int bi,char x,boolean bigEndian){
        if(bigEndian)
            putCharB(bb,bi,x);
        else
            putCharL(bb,bi,x);
    }

    static void putCharL(ByteBuffer bb,int bi,char x){
        bb._put(bi,char0(x));
        bb._put(bi+1,char1(x));
    }

    static void putCharB(ByteBuffer bb,int bi,char x){
        bb._put(bi,char1(x));
        bb._put(bi+1,char0(x));
    }

    private static byte char1(char x){
        return (byte)(x>>8);
    }

    private static byte char0(char x){
        return (byte)(x);
    }

    static void putChar(long a,char x,boolean bigEndian){
        if(bigEndian)
            putCharB(a,x);
        else
            putCharL(a,x);
    }
    // -- get/put int --

    static void putCharL(long a,char x){
        _put(a,char0(x));
        _put(a+1,char1(x));
    }

    static void putCharB(long a,char x){
        _put(a,char1(x));
        _put(a+1,char0(x));
    }

    private static void _put(long a,byte b){
        unsafe.putByte(a,b);
    }

    static short getShort(ByteBuffer bb,int bi,boolean bigEndian){
        return bigEndian?getShortB(bb,bi):getShortL(bb,bi);
    }

    static short getShortL(ByteBuffer bb,int bi){
        return makeShort(bb._get(bi+1),
                bb._get(bi));
    }

    static short getShortB(ByteBuffer bb,int bi){
        return makeShort(bb._get(bi),
                bb._get(bi+1));
    }

    static private short makeShort(byte b1,byte b0){
        return (short)((b1<<8)|(b0&0xff));
    }

    static short getShort(long a,boolean bigEndian){
        return bigEndian?getShortB(a):getShortL(a);
    }

    static short getShortL(long a){
        return makeShort(_get(a+1),
                _get(a));
    }

    static short getShortB(long a){
        return makeShort(_get(a),
                _get(a+1));
    }

    static void putShort(ByteBuffer bb,int bi,short x,boolean bigEndian){
        if(bigEndian)
            putShortB(bb,bi,x);
        else
            putShortL(bb,bi,x);
    }

    static void putShortL(ByteBuffer bb,int bi,short x){
        bb._put(bi,short0(x));
        bb._put(bi+1,short1(x));
    }

    static void putShortB(ByteBuffer bb,int bi,short x){
        bb._put(bi,short1(x));
        bb._put(bi+1,short0(x));
    }

    private static byte short1(short x){
        return (byte)(x>>8);
    }

    private static byte short0(short x){
        return (byte)(x);
    }

    static void putShort(long a,short x,boolean bigEndian){
        if(bigEndian)
            putShortB(a,x);
        else
            putShortL(a,x);
    }

    static void putShortL(long a,short x){
        _put(a,short0(x));
        _put(a+1,short1(x));
    }
    // -- get/put long --

    static void putShortB(long a,short x){
        _put(a,short1(x));
        _put(a+1,short0(x));
    }

    static int getInt(ByteBuffer bb,int bi,boolean bigEndian){
        return bigEndian?getIntB(bb,bi):getIntL(bb,bi);
    }

    static int getIntL(ByteBuffer bb,int bi){
        return makeInt(bb._get(bi+3),
                bb._get(bi+2),
                bb._get(bi+1),
                bb._get(bi));
    }

    static int getIntB(ByteBuffer bb,int bi){
        return makeInt(bb._get(bi),
                bb._get(bi+1),
                bb._get(bi+2),
                bb._get(bi+3));
    }

    static private int makeInt(byte b3,byte b2,byte b1,byte b0){
        return (((b3)<<24)|
                ((b2&0xff)<<16)|
                ((b1&0xff)<<8)|
                ((b0&0xff)));
    }

    static int getInt(long a,boolean bigEndian){
        return bigEndian?getIntB(a):getIntL(a);
    }

    static int getIntL(long a){
        return makeInt(_get(a+3),
                _get(a+2),
                _get(a+1),
                _get(a));
    }

    static int getIntB(long a){
        return makeInt(_get(a),
                _get(a+1),
                _get(a+2),
                _get(a+3));
    }

    static void putInt(ByteBuffer bb,int bi,int x,boolean bigEndian){
        if(bigEndian)
            putIntB(bb,bi,x);
        else
            putIntL(bb,bi,x);
    }

    static void putIntL(ByteBuffer bb,int bi,int x){
        bb._put(bi+3,int3(x));
        bb._put(bi+2,int2(x));
        bb._put(bi+1,int1(x));
        bb._put(bi,int0(x));
    }

    static void putIntB(ByteBuffer bb,int bi,int x){
        bb._put(bi,int3(x));
        bb._put(bi+1,int2(x));
        bb._put(bi+2,int1(x));
        bb._put(bi+3,int0(x));
    }

    private static byte int3(int x){
        return (byte)(x>>24);
    }

    private static byte int2(int x){
        return (byte)(x>>16);
    }

    private static byte int1(int x){
        return (byte)(x>>8);
    }

    private static byte int0(int x){
        return (byte)(x);
    }

    static void putInt(long a,int x,boolean bigEndian){
        if(bigEndian)
            putIntB(a,x);
        else
            putIntL(a,x);
    }

    static void putIntL(long a,int x){
        _put(a+3,int3(x));
        _put(a+2,int2(x));
        _put(a+1,int1(x));
        _put(a,int0(x));
    }

    static void putIntB(long a,int x){
        _put(a,int3(x));
        _put(a+1,int2(x));
        _put(a+2,int1(x));
        _put(a+3,int0(x));
    }

    static long getLong(ByteBuffer bb,int bi,boolean bigEndian){
        return bigEndian?getLongB(bb,bi):getLongL(bb,bi);
    }

    static long getLongL(ByteBuffer bb,int bi){
        return makeLong(bb._get(bi+7),
                bb._get(bi+6),
                bb._get(bi+5),
                bb._get(bi+4),
                bb._get(bi+3),
                bb._get(bi+2),
                bb._get(bi+1),
                bb._get(bi));
    }

    static long getLongB(ByteBuffer bb,int bi){
        return makeLong(bb._get(bi),
                bb._get(bi+1),
                bb._get(bi+2),
                bb._get(bi+3),
                bb._get(bi+4),
                bb._get(bi+5),
                bb._get(bi+6),
                bb._get(bi+7));
    }
    // -- get/put float --

    static private long makeLong(byte b7,byte b6,byte b5,byte b4,
                                 byte b3,byte b2,byte b1,byte b0){
        return ((((long)b7)<<56)|
                (((long)b6&0xff)<<48)|
                (((long)b5&0xff)<<40)|
                (((long)b4&0xff)<<32)|
                (((long)b3&0xff)<<24)|
                (((long)b2&0xff)<<16)|
                (((long)b1&0xff)<<8)|
                (((long)b0&0xff)));
    }

    static long getLong(long a,boolean bigEndian){
        return bigEndian?getLongB(a):getLongL(a);
    }

    static long getLongL(long a){
        return makeLong(_get(a+7),
                _get(a+6),
                _get(a+5),
                _get(a+4),
                _get(a+3),
                _get(a+2),
                _get(a+1),
                _get(a));
    }

    static long getLongB(long a){
        return makeLong(_get(a),
                _get(a+1),
                _get(a+2),
                _get(a+3),
                _get(a+4),
                _get(a+5),
                _get(a+6),
                _get(a+7));
    }

    static void putLong(ByteBuffer bb,int bi,long x,boolean bigEndian){
        if(bigEndian)
            putLongB(bb,bi,x);
        else
            putLongL(bb,bi,x);
    }

    static void putLongL(ByteBuffer bb,int bi,long x){
        bb._put(bi+7,long7(x));
        bb._put(bi+6,long6(x));
        bb._put(bi+5,long5(x));
        bb._put(bi+4,long4(x));
        bb._put(bi+3,long3(x));
        bb._put(bi+2,long2(x));
        bb._put(bi+1,long1(x));
        bb._put(bi,long0(x));
    }

    static void putLongB(ByteBuffer bb,int bi,long x){
        bb._put(bi,long7(x));
        bb._put(bi+1,long6(x));
        bb._put(bi+2,long5(x));
        bb._put(bi+3,long4(x));
        bb._put(bi+4,long3(x));
        bb._put(bi+5,long2(x));
        bb._put(bi+6,long1(x));
        bb._put(bi+7,long0(x));
    }

    private static byte long7(long x){
        return (byte)(x>>56);
    }

    private static byte long6(long x){
        return (byte)(x>>48);
    }

    private static byte long5(long x){
        return (byte)(x>>40);
    }

    private static byte long4(long x){
        return (byte)(x>>32);
    }

    private static byte long3(long x){
        return (byte)(x>>24);
    }
    // -- get/put double --

    private static byte long2(long x){
        return (byte)(x>>16);
    }

    private static byte long1(long x){
        return (byte)(x>>8);
    }

    private static byte long0(long x){
        return (byte)(x);
    }

    static void putLong(long a,long x,boolean bigEndian){
        if(bigEndian)
            putLongB(a,x);
        else
            putLongL(a,x);
    }

    static void putLongL(long a,long x){
        _put(a+7,long7(x));
        _put(a+6,long6(x));
        _put(a+5,long5(x));
        _put(a+4,long4(x));
        _put(a+3,long3(x));
        _put(a+2,long2(x));
        _put(a+1,long1(x));
        _put(a,long0(x));
    }

    static void putLongB(long a,long x){
        _put(a,long7(x));
        _put(a+1,long6(x));
        _put(a+2,long5(x));
        _put(a+3,long4(x));
        _put(a+4,long3(x));
        _put(a+5,long2(x));
        _put(a+6,long1(x));
        _put(a+7,long0(x));
    }

    static float getFloat(ByteBuffer bb,int bi,boolean bigEndian){
        return bigEndian?getFloatB(bb,bi):getFloatL(bb,bi);
    }

    static float getFloatL(ByteBuffer bb,int bi){
        return Float.intBitsToFloat(getIntL(bb,bi));
    }

    static float getFloatB(ByteBuffer bb,int bi){
        return Float.intBitsToFloat(getIntB(bb,bi));
    }

    static float getFloat(long a,boolean bigEndian){
        return bigEndian?getFloatB(a):getFloatL(a);
    }

    static float getFloatL(long a){
        return Float.intBitsToFloat(getIntL(a));
    }

    static float getFloatB(long a){
        return Float.intBitsToFloat(getIntB(a));
    }

    static void putFloat(ByteBuffer bb,int bi,float x,boolean bigEndian){
        if(bigEndian)
            putFloatB(bb,bi,x);
        else
            putFloatL(bb,bi,x);
    }

    static void putFloatL(ByteBuffer bb,int bi,float x){
        putIntL(bb,bi,Float.floatToRawIntBits(x));
    }

    static void putFloatB(ByteBuffer bb,int bi,float x){
        putIntB(bb,bi,Float.floatToRawIntBits(x));
    }

    static void putFloat(long a,float x,boolean bigEndian){
        if(bigEndian)
            putFloatB(a,x);
        else
            putFloatL(a,x);
    }

    static void putFloatL(long a,float x){
        putIntL(a,Float.floatToRawIntBits(x));
    }

    static void putFloatB(long a,float x){
        putIntB(a,Float.floatToRawIntBits(x));
    }

    static double getDouble(ByteBuffer bb,int bi,boolean bigEndian){
        return bigEndian?getDoubleB(bb,bi):getDoubleL(bb,bi);
    }

    static double getDoubleL(ByteBuffer bb,int bi){
        return Double.longBitsToDouble(getLongL(bb,bi));
    }

    static double getDoubleB(ByteBuffer bb,int bi){
        return Double.longBitsToDouble(getLongB(bb,bi));
    }

    static double getDouble(long a,boolean bigEndian){
        return bigEndian?getDoubleB(a):getDoubleL(a);
    }

    static double getDoubleL(long a){
        return Double.longBitsToDouble(getLongL(a));
    }

    static double getDoubleB(long a){
        return Double.longBitsToDouble(getLongB(a));
    }

    static void putDouble(ByteBuffer bb,int bi,double x,boolean bigEndian){
        if(bigEndian)
            putDoubleB(bb,bi,x);
        else
            putDoubleL(bb,bi,x);
    }

    static void putDoubleL(ByteBuffer bb,int bi,double x){
        putLongL(bb,bi,Double.doubleToRawLongBits(x));
    }

    static void putDoubleB(ByteBuffer bb,int bi,double x){
        putLongB(bb,bi,Double.doubleToRawLongBits(x));
    }

    static void putDouble(long a,double x,boolean bigEndian){
        if(bigEndian)
            putDoubleB(a,x);
        else
            putDoubleL(a,x);
    }

    static void putDoubleL(long a,double x){
        putLongL(a,Double.doubleToRawLongBits(x));
    }

    static void putDoubleB(long a,double x){
        putLongB(a,Double.doubleToRawLongBits(x));
    }

    static ByteOrder byteOrder(){
        if(byteOrder==null)
            throw new Error("Unknown byte order");
        return byteOrder;
    }

    static int pageCount(long size){
        return (int)(size+(long)pageSize()-1L)/pageSize();
    }

    static int pageSize(){
        if(pageSize==-1)
            pageSize=unsafe().pageSize();
        return pageSize;
    }

    static Unsafe unsafe(){
        return unsafe;
    }
    // -- Monitoring of direct buffer usage --

    static boolean unaligned(){
        if(unalignedKnown)
            return unaligned;
        String arch=AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction("os.arch"));
        unaligned=arch.equals("i386")||arch.equals("x86")
                ||arch.equals("amd64")||arch.equals("x86_64");
        unalignedKnown=true;
        return unaligned;
    }

    // These methods should be called whenever direct memory is allocated or
    // freed.  They allow the user to control the amount of direct memory
    // which a process may access.  All sizes are specified in bytes.
    static void reserveMemory(long size,int cap){
        if(!memoryLimitSet&&VM.isBooted()){
            maxMemory=VM.maxDirectMemory();
            memoryLimitSet=true;
        }
        // optimist!
        if(tryReserveMemory(size,cap)){
            return;
        }
        final JavaLangRefAccess jlra=SharedSecrets.getJavaLangRefAccess();
        // retry while helping enqueue pending Reference objects
        // which includes executing pending Cleaner(s) which includes
        // Cleaner(s) that free direct buffer memory
        while(jlra.tryHandlePendingReference()){
            if(tryReserveMemory(size,cap)){
                return;
            }
        }
        // trigger VM's Reference processing
        System.gc();
        // a retry loop with exponential back-off delays
        // (this gives VM some time to do it's job)
        boolean interrupted=false;
        try{
            long sleepTime=1;
            int sleeps=0;
            while(true){
                if(tryReserveMemory(size,cap)){
                    return;
                }
                if(sleeps>=MAX_SLEEPS){
                    break;
                }
                if(!jlra.tryHandlePendingReference()){
                    try{
                        Thread.sleep(sleepTime);
                        sleepTime<<=1;
                        sleeps++;
                    }catch(InterruptedException e){
                        interrupted=true;
                    }
                }
            }
            // no luck
            throw new OutOfMemoryError("Direct buffer memory");
        }finally{
            if(interrupted){
                // don't swallow interrupts
                Thread.currentThread().interrupt();
            }
        }
    }

    private static boolean tryReserveMemory(long size,int cap){
        // -XX:MaxDirectMemorySize limits the total capacity rather than the
        // actual memory usage, which will differ when buffers are page
        // aligned.
        long totalCap;
        while(cap<=maxMemory-(totalCap=totalCapacity.get())){
            if(totalCapacity.compareAndSet(totalCap,totalCap+cap)){
                reservedMemory.addAndGet(size);
                count.incrementAndGet();
                return true;
            }
        }
        return false;
    }

    static void unreserveMemory(long size,int cap){
        long cnt=count.decrementAndGet();
        long reservedMem=reservedMemory.addAndGet(-size);
        long totalCap=totalCapacity.addAndGet(-cap);
        assert cnt>=0&&reservedMem>=0&&totalCap>=0;
    }
    // These methods do no bounds checking.  Verification that the copy will not
    // result in memory corruption should be done prior to invocation.
    // All positions and lengths are specified in bytes.

    static void copyFromArray(Object src,long srcBaseOffset,long srcPos,
                              long dstAddr,long length){
        long offset=srcBaseOffset+srcPos;
        while(length>0){
            long size=(length>UNSAFE_COPY_THRESHOLD)?UNSAFE_COPY_THRESHOLD:length;
            unsafe.copyMemory(src,offset,null,dstAddr,size);
            length-=size;
            offset+=size;
            dstAddr+=size;
        }
    }

    static void copyToArray(long srcAddr,Object dst,long dstBaseOffset,long dstPos,
                            long length){
        long offset=dstBaseOffset+dstPos;
        while(length>0){
            long size=(length>UNSAFE_COPY_THRESHOLD)?UNSAFE_COPY_THRESHOLD:length;
            unsafe.copyMemory(null,srcAddr,dst,offset,size);
            length-=size;
            srcAddr+=size;
            offset+=size;
        }
    }

    static void copyFromCharArray(Object src,long srcPos,long dstAddr,
                                  long length){
        copyFromShortArray(src,srcPos,dstAddr,length);
    }

    static native void copyFromShortArray(Object src,long srcPos,long dstAddr,
                                          long length);

    static void copyToCharArray(long srcAddr,Object dst,long dstPos,
                                long length){
        copyToShortArray(srcAddr,dst,dstPos,length);
    }

    static native void copyToShortArray(long srcAddr,Object dst,long dstPos,
                                        long length);

    static native void copyFromIntArray(Object src,long srcPos,long dstAddr,
                                        long length);

    static native void copyToIntArray(long srcAddr,Object dst,long dstPos,
                                      long length);

    static native void copyFromLongArray(Object src,long srcPos,long dstAddr,
                                         long length);

    static native void copyToLongArray(long srcAddr,Object dst,long dstPos,
                                       long length);
}
