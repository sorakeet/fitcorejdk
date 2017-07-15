/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.sql;

import java.time.Instant;
import java.time.LocalDate;

public class Date extends java.util.Date{
    static final long serialVersionUID=1511598038487230103L;

    @Deprecated
    public Date(int year,int month,int day){
        super(year,month,day);
    }

    public Date(long date){
        // If the millisecond date value contains time info, mask it out.
        super(date);
    }

    public static Date valueOf(String s){
        final int YEAR_LENGTH=4;
        final int MONTH_LENGTH=2;
        final int DAY_LENGTH=2;
        final int MAX_MONTH=12;
        final int MAX_DAY=31;
        int firstDash;
        int secondDash;
        Date d=null;
        if(s==null){
            throw new IllegalArgumentException();
        }
        firstDash=s.indexOf('-');
        secondDash=s.indexOf('-',firstDash+1);
        if((firstDash>0)&&(secondDash>0)&&(secondDash<s.length()-1)){
            String yyyy=s.substring(0,firstDash);
            String mm=s.substring(firstDash+1,secondDash);
            String dd=s.substring(secondDash+1);
            if(yyyy.length()==YEAR_LENGTH&&
                    (mm.length()>=1&&mm.length()<=MONTH_LENGTH)&&
                    (dd.length()>=1&&dd.length()<=DAY_LENGTH)){
                int year=Integer.parseInt(yyyy);
                int month=Integer.parseInt(mm);
                int day=Integer.parseInt(dd);
                if((month>=1&&month<=MAX_MONTH)&&(day>=1&&day<=MAX_DAY)){
                    d=new Date(year-1900,month-1,day);
                }
            }
        }
        if(d==null){
            throw new IllegalArgumentException();
        }
        return d;
    }

    @SuppressWarnings("deprecation")
    public static Date valueOf(LocalDate date){
        return new Date(date.getYear()-1900,date.getMonthValue()-1,
                date.getDayOfMonth());
    }
    // Override all the time operations inherited from java.util.Date;

    @Deprecated
    public int getHours(){
        throw new IllegalArgumentException();
    }

    @Deprecated
    public void setHours(int i){
        throw new IllegalArgumentException();
    }

    @Deprecated
    public int getMinutes(){
        throw new IllegalArgumentException();
    }

    @Deprecated
    public void setMinutes(int i){
        throw new IllegalArgumentException();
    }

    @Deprecated
    public int getSeconds(){
        throw new IllegalArgumentException();
    }

    @Deprecated
    public void setSeconds(int i){
        throw new IllegalArgumentException();
    }

    public void setTime(long date){
        // If the millisecond date value contains time info, mask it out.
        super.setTime(date);
    }

    @SuppressWarnings("deprecation")
    public String toString(){
        int year=super.getYear()+1900;
        int month=super.getMonth()+1;
        int day=super.getDate();
        char buf[]="2000-00-00".toCharArray();
        buf[0]=Character.forDigit(year/1000,10);
        buf[1]=Character.forDigit((year/100)%10,10);
        buf[2]=Character.forDigit((year/10)%10,10);
        buf[3]=Character.forDigit(year%10,10);
        buf[5]=Character.forDigit(month/10,10);
        buf[6]=Character.forDigit(month%10,10);
        buf[8]=Character.forDigit(day/10,10);
        buf[9]=Character.forDigit(day%10,10);
        return new String(buf);
    }

    @Override
    public Instant toInstant(){
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("deprecation")
    public LocalDate toLocalDate(){
        return LocalDate.of(getYear()+1900,getMonth()+1,getDate());
    }
}
