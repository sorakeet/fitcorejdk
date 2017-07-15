/**
 * Copyright (c) 2003, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.datatype;

import javax.xml.namespace.QName;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public abstract class XMLGregorianCalendar
        implements Cloneable{
    public XMLGregorianCalendar(){
    }

    public abstract void clear();

    public abstract void reset();

    public abstract void setYear(BigInteger year);

    public void setTime(int hour,int minute,int second){
        setTime(
                hour,
                minute,
                second,
                null // fractional
        );
    }

    public void setTime(
            int hour,
            int minute,
            int second,
            BigDecimal fractional){
        setHour(hour);
        setMinute(minute);
        setSecond(second);
        setFractionalSecond(fractional);
    }

    public void setTime(int hour,int minute,int second,int millisecond){
        setHour(hour);
        setMinute(minute);
        setSecond(second);
        setMillisecond(millisecond);
    }

    public abstract BigInteger getEon();

    public abstract int getYear();

    public abstract void setYear(int year);

    public abstract BigInteger getEonAndYear();

    public abstract int getMonth();

    public abstract void setMonth(int month);

    public abstract int getDay();

    public abstract void setDay(int day);

    public abstract int getHour();

    public abstract void setHour(int hour);

    public abstract int getMinute();

    public abstract void setMinute(int minute);

    public abstract int getSecond();

    public abstract void setSecond(int second);

    public int getMillisecond(){
        BigDecimal fractionalSeconds=getFractionalSecond();
        // is field undefined?
        if(fractionalSeconds==null){
            return DatatypeConstants.FIELD_UNDEFINED;
        }
        return getFractionalSecond().movePointRight(3).intValue();
    }

    public abstract void setMillisecond(int millisecond);

    public abstract BigDecimal getFractionalSecond();

    public abstract void setFractionalSecond(BigDecimal fractional);

    public int hashCode(){
        // Following two dates compare to EQUALS since in different timezones.
        // 2000-01-15T12:00:00-05:00 == 2000-01-15T13:00:00-04:00
        //
        // Must ensure both instances generate same hashcode by normalizing
        // this to UTC timezone.
        int timezone=getTimezone();
        if(timezone==DatatypeConstants.FIELD_UNDEFINED){
            timezone=0;
        }
        XMLGregorianCalendar gc=this;
        if(timezone!=0){
            gc=this.normalize();
        }
        return gc.getYear()
                +gc.getMonth()
                +gc.getDay()
                +gc.getHour()
                +gc.getMinute()
                +gc.getSecond();
    }

    public abstract int getTimezone();

    public abstract void setTimezone(int offset);

    public abstract XMLGregorianCalendar normalize();

    public boolean equals(Object obj){
        if(obj==null||!(obj instanceof XMLGregorianCalendar)){
            return false;
        }
        return compare((XMLGregorianCalendar)obj)==DatatypeConstants.EQUAL;
    }

    // comparisons
    public abstract int compare(XMLGregorianCalendar xmlGregorianCalendar);

    public abstract Object clone();

    public String toString(){
        return toXMLFormat();
    }

    public abstract String toXMLFormat();

    public abstract QName getXMLSchemaType();

    public abstract boolean isValid();

    public abstract void add(Duration duration);

    public abstract GregorianCalendar toGregorianCalendar();

    public abstract GregorianCalendar toGregorianCalendar(
            TimeZone timezone,
            java.util.Locale aLocale,
            XMLGregorianCalendar defaults);

    public abstract TimeZone getTimeZone(int defaultZoneoffset);
}
