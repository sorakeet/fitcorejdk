/**
 * Copyright (c) 2013, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.zip;

import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

class ZipUtils{
    // used to adjust values between Windows and java epoch
    private static final long WINDOWS_EPOCH_IN_MICROSECONDS=-11644473600000000L;

    public static final FileTime winTimeToFileTime(long wtime){
        return FileTime.from(wtime/10+WINDOWS_EPOCH_IN_MICROSECONDS,
                TimeUnit.MICROSECONDS);
    }

    public static final long fileTimeToWinTime(FileTime ftime){
        return (ftime.to(TimeUnit.MICROSECONDS)-WINDOWS_EPOCH_IN_MICROSECONDS)*10;
    }

    public static final FileTime unixTimeToFileTime(long utime){
        return FileTime.from(utime,TimeUnit.SECONDS);
    }

    public static final long fileTimeToUnixTime(FileTime ftime){
        return ftime.to(TimeUnit.SECONDS);
    }

    public static long extendedDosToJavaTime(long xdostime){
        long time=dosToJavaTime(xdostime);
        return time+(xdostime>>32);
    }

    private static long dosToJavaTime(long dtime){
        @SuppressWarnings("deprecation") // Use of date constructor.
                Date d=new Date((int)(((dtime>>25)&0x7f)+80),
                (int)(((dtime>>21)&0x0f)-1),
                (int)((dtime>>16)&0x1f),
                (int)((dtime>>11)&0x1f),
                (int)((dtime>>5)&0x3f),
                (int)((dtime<<1)&0x3e));
        return d.getTime();
    }

    public static long javaToExtendedDosTime(long time){
        if(time<0){
            return ZipEntry.DOSTIME_BEFORE_1980;
        }
        long dostime=javaToDosTime(time);
        return (dostime!=ZipEntry.DOSTIME_BEFORE_1980)
                ?dostime+((time%2000)<<32)
                :ZipEntry.DOSTIME_BEFORE_1980;
    }

    @SuppressWarnings("deprecation") // Use of date methods
    private static long javaToDosTime(long time){
        Date d=new Date(time);
        int year=d.getYear()+1900;
        if(year<1980){
            return ZipEntry.DOSTIME_BEFORE_1980;
        }
        return (year-1980)<<25|(d.getMonth()+1)<<21|
                d.getDate()<<16|d.getHours()<<11|d.getMinutes()<<5|
                d.getSeconds()>>1;
    }

    public static final long get64(byte b[],int off){
        return get32(b,off)|(get32(b,off+4)<<32);
    }

    public static final long get32(byte b[],int off){
        return (get16(b,off)|((long)get16(b,off+2)<<16))&0xffffffffL;
    }

    public static final int get16(byte b[],int off){
        return Byte.toUnsignedInt(b[off])|(Byte.toUnsignedInt(b[off+1])<<8);
    }
}
