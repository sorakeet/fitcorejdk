/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.sql;

import java.time.Instant;
import java.time.LocalTime;

public class Time extends java.util.Date{
    static final long serialVersionUID=8397324403548013681L;

    @Deprecated
    public Time(int hour,int minute,int second){
        super(70,0,1,hour,minute,second);
    }

    public Time(long time){
        super(time);
    }

    public static Time valueOf(String s){
        int hour;
        int minute;
        int second;
        int firstColon;
        int secondColon;
        if(s==null) throw new IllegalArgumentException();
        firstColon=s.indexOf(':');
        secondColon=s.indexOf(':',firstColon+1);
        if((firstColon>0)&(secondColon>0)&
                (secondColon<s.length()-1)){
            hour=Integer.parseInt(s.substring(0,firstColon));
            minute=
                    Integer.parseInt(s.substring(firstColon+1,secondColon));
            second=Integer.parseInt(s.substring(secondColon+1));
        }else{
            throw new IllegalArgumentException();
        }
        return new Time(hour,minute,second);
    }

    @SuppressWarnings("deprecation")
    public static Time valueOf(LocalTime time){
        return new Time(time.getHour(),time.getMinute(),time.getSecond());
    }
    // Override all the date operations inherited from java.util.Date;

    @Deprecated
    public int getYear(){
        throw new IllegalArgumentException();
    }

    @Deprecated
    public void setYear(int i){
        throw new IllegalArgumentException();
    }

    @Deprecated
    public int getMonth(){
        throw new IllegalArgumentException();
    }

    @Deprecated
    public void setMonth(int i){
        throw new IllegalArgumentException();
    }

    @Deprecated
    public int getDate(){
        throw new IllegalArgumentException();
    }

    @Deprecated
    public void setDate(int i){
        throw new IllegalArgumentException();
    }

    @Deprecated
    public int getDay(){
        throw new IllegalArgumentException();
    }

    public void setTime(long time){
        super.setTime(time);
    }

    @SuppressWarnings("deprecation")
    public String toString(){
        int hour=super.getHours();
        int minute=super.getMinutes();
        int second=super.getSeconds();
        String hourString;
        String minuteString;
        String secondString;
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
        return (hourString+":"+minuteString+":"+secondString);
    }

    @Override
    public Instant toInstant(){
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("deprecation")
    public LocalTime toLocalTime(){
        return LocalTime.of(getHours(),getMinutes(),getSeconds());
    }
}
