/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.sql;

import java.time.Instant;
import java.time.LocalDateTime;

public class Timestamp extends java.util.Date{
    static final long serialVersionUID=2745179027874758501L;
    private static final int MILLIS_PER_SECOND=1000;
    private int nanos;

    @Deprecated
    public Timestamp(int year,int month,int date,
                     int hour,int minute,int second,int nano){
        super(year,month,date,hour,minute,second);
        if(nano>999999999||nano<0){
            throw new IllegalArgumentException("nanos > 999999999 or < 0");
        }
        nanos=nano;
    }

    public Timestamp(long time){
        super((time/1000)*1000);
        nanos=(int)((time%1000)*1000000);
        if(nanos<0){
            nanos=1000000000+nanos;
            super.setTime(((time/1000)-1)*1000);
        }
    }

    public static Timestamp valueOf(String s){
        final int YEAR_LENGTH=4;
        final int MONTH_LENGTH=2;
        final int DAY_LENGTH=2;
        final int MAX_MONTH=12;
        final int MAX_DAY=31;
        String date_s;
        String time_s;
        String nanos_s;
        int year=0;
        int month=0;
        int day=0;
        int hour;
        int minute;
        int second;
        int a_nanos=0;
        int firstDash;
        int secondDash;
        int dividingSpace;
        int firstColon=0;
        int secondColon=0;
        int period=0;
        String formatError="Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]";
        String zeros="000000000";
        String delimiterDate="-";
        String delimiterTime=":";
        if(s==null) throw new IllegalArgumentException("null string");
        // Split the string into date and time components
        s=s.trim();
        dividingSpace=s.indexOf(' ');
        if(dividingSpace>0){
            date_s=s.substring(0,dividingSpace);
            time_s=s.substring(dividingSpace+1);
        }else{
            throw new IllegalArgumentException(formatError);
        }
        // Parse the date
        firstDash=date_s.indexOf('-');
        secondDash=date_s.indexOf('-',firstDash+1);
        // Parse the time
        if(time_s==null)
            throw new IllegalArgumentException(formatError);
        firstColon=time_s.indexOf(':');
        secondColon=time_s.indexOf(':',firstColon+1);
        period=time_s.indexOf('.',secondColon+1);
        // Convert the date
        boolean parsedDate=false;
        if((firstDash>0)&&(secondDash>0)&&(secondDash<date_s.length()-1)){
            String yyyy=date_s.substring(0,firstDash);
            String mm=date_s.substring(firstDash+1,secondDash);
            String dd=date_s.substring(secondDash+1);
            if(yyyy.length()==YEAR_LENGTH&&
                    (mm.length()>=1&&mm.length()<=MONTH_LENGTH)&&
                    (dd.length()>=1&&dd.length()<=DAY_LENGTH)){
                year=Integer.parseInt(yyyy);
                month=Integer.parseInt(mm);
                day=Integer.parseInt(dd);
                if((month>=1&&month<=MAX_MONTH)&&(day>=1&&day<=MAX_DAY)){
                    parsedDate=true;
                }
            }
        }
        if(!parsedDate){
            throw new IllegalArgumentException(formatError);
        }
        // Convert the time; default missing nanos
        if((firstColon>0)&(secondColon>0)&
                (secondColon<time_s.length()-1)){
            hour=Integer.parseInt(time_s.substring(0,firstColon));
            minute=
                    Integer.parseInt(time_s.substring(firstColon+1,secondColon));
            if((period>0)&(period<time_s.length()-1)){
                second=
                        Integer.parseInt(time_s.substring(secondColon+1,period));
                nanos_s=time_s.substring(period+1);
                if(nanos_s.length()>9)
                    throw new IllegalArgumentException(formatError);
                if(!Character.isDigit(nanos_s.charAt(0)))
                    throw new IllegalArgumentException(formatError);
                nanos_s=nanos_s+zeros.substring(0,9-nanos_s.length());
                a_nanos=Integer.parseInt(nanos_s);
            }else if(period>0){
                throw new IllegalArgumentException(formatError);
            }else{
                second=Integer.parseInt(time_s.substring(secondColon+1));
            }
        }else{
            throw new IllegalArgumentException(formatError);
        }
        return new Timestamp(year-1900,month-1,day,hour,minute,second,a_nanos);
    }

    @SuppressWarnings("deprecation")
    public static Timestamp valueOf(LocalDateTime dateTime){
        return new Timestamp(dateTime.getYear()-1900,
                dateTime.getMonthValue()-1,
                dateTime.getDayOfMonth(),
                dateTime.getHour(),
                dateTime.getMinute(),
                dateTime.getSecond(),
                dateTime.getNano());
    }

    public static Timestamp from(Instant instant){
        try{
            Timestamp stamp=new Timestamp(instant.getEpochSecond()*MILLIS_PER_SECOND);
            stamp.nanos=instant.getNano();
            return stamp;
        }catch(ArithmeticException ex){
            throw new IllegalArgumentException(ex);
        }
    }

    public boolean before(Timestamp ts){
        return compareTo(ts)<0;
    }

    public int compareTo(Timestamp ts){
        long thisTime=this.getTime();
        long anotherTime=ts.getTime();
        int i=(thisTime<anotherTime?-1:(thisTime==anotherTime?0:1));
        if(i==0){
            if(nanos>ts.nanos){
                return 1;
            }else if(nanos<ts.nanos){
                return -1;
            }
        }
        return i;
    }

    public long getTime(){
        long time=super.getTime();
        return (time+(nanos/1000000));
    }

    public void setTime(long time){
        super.setTime((time/1000)*1000);
        nanos=(int)((time%1000)*1000000);
        if(nanos<0){
            nanos=1000000000+nanos;
            super.setTime(((time/1000)-1)*1000);
        }
    }

    public boolean equals(Object ts){
        if(ts instanceof Timestamp){
            return this.equals((Timestamp)ts);
        }else{
            return false;
        }
    }

    public boolean equals(Timestamp ts){
        if(super.equals(ts)){
            if(nanos==ts.nanos){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    public int compareTo(java.util.Date o){
        if(o instanceof Timestamp){
            // When Timestamp instance compare it with a Timestamp
            // Hence it is basically calling this.compareTo((Timestamp))o);
            // Note typecasting is safe because o is instance of Timestamp
            return compareTo((Timestamp)o);
        }else{
            // When Date doing a o.compareTo(this)
            // will give wrong results.
            Timestamp ts=new Timestamp(o.getTime());
            return this.compareTo(ts);
        }
    }

    @Override
    public int hashCode(){
        return super.hashCode();
    }

    @SuppressWarnings("deprecation")
    public String toString(){
        int year=super.getYear()+1900;
        int month=super.getMonth()+1;
        int day=super.getDate();
        int hour=super.getHours();
        int minute=super.getMinutes();
        int second=super.getSeconds();
        String yearString;
        String monthString;
        String dayString;
        String hourString;
        String minuteString;
        String secondString;
        String nanosString;
        String zeros="000000000";
        String yearZeros="0000";
        StringBuffer timestampBuf;
        if(year<1000){
            // Add leading zeros
            yearString=""+year;
            yearString=yearZeros.substring(0,(4-yearString.length()))+
                    yearString;
        }else{
            yearString=""+year;
        }
        if(month<10){
            monthString="0"+month;
        }else{
            monthString=Integer.toString(month);
        }
        if(day<10){
            dayString="0"+day;
        }else{
            dayString=Integer.toString(day);
        }
        if(hour<10){
            hourString="0"+hour;
        }else{
            hourString=Integer.toString(hour);
        }
        if(minute<10){
            minuteString="0"+minute;
        }else{
            minuteString=Integer.toString(minute);
        }
        if(second<10){
            secondString="0"+second;
        }else{
            secondString=Integer.toString(second);
        }
        if(nanos==0){
            nanosString="0";
        }else{
            nanosString=Integer.toString(nanos);
            // Add leading zeros
            nanosString=zeros.substring(0,(9-nanosString.length()))+
                    nanosString;
            // Truncate trailing zeros
            char[] nanosChar=new char[nanosString.length()];
            nanosString.getChars(0,nanosString.length(),nanosChar,0);
            int truncIndex=8;
            while(nanosChar[truncIndex]=='0'){
                truncIndex--;
            }
            nanosString=new String(nanosChar,0,truncIndex+1);
        }
        // do a string buffer here instead.
        timestampBuf=new StringBuffer(20+nanosString.length());
        timestampBuf.append(yearString);
        timestampBuf.append("-");
        timestampBuf.append(monthString);
        timestampBuf.append("-");
        timestampBuf.append(dayString);
        timestampBuf.append(" ");
        timestampBuf.append(hourString);
        timestampBuf.append(":");
        timestampBuf.append(minuteString);
        timestampBuf.append(":");
        timestampBuf.append(secondString);
        timestampBuf.append(".");
        timestampBuf.append(nanosString);
        return (timestampBuf.toString());
    }

    @Override
    public Instant toInstant(){
        return Instant.ofEpochSecond(super.getTime()/MILLIS_PER_SECOND,nanos);
    }

    public boolean after(Timestamp ts){
        return compareTo(ts)>0;
    }

    @SuppressWarnings("deprecation")
    public LocalDateTime toLocalDateTime(){
        return LocalDateTime.of(getYear()+1900,
                getMonth()+1,
                getDate(),
                getHours(),
                getMinutes(),
                getSeconds(),
                getNanos());
    }

    public int getNanos(){
        return nanos;
    }

    public void setNanos(int n){
        if(n>999999999||n<0){
            throw new IllegalArgumentException("nanos > 999999999 or < 0");
        }
        nanos=n;
    }
}
