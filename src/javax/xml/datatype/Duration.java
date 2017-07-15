/**
 * Copyright (c) 2003, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.datatype;

import javax.xml.namespace.QName;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public abstract class Duration{
    private static final boolean DEBUG=true;

    public Duration(){
    }

    public QName getXMLSchemaType(){
        boolean yearSet=isSet(DatatypeConstants.YEARS);
        boolean monthSet=isSet(DatatypeConstants.MONTHS);
        boolean daySet=isSet(DatatypeConstants.DAYS);
        boolean hourSet=isSet(DatatypeConstants.HOURS);
        boolean minuteSet=isSet(DatatypeConstants.MINUTES);
        boolean secondSet=isSet(DatatypeConstants.SECONDS);
        // DURATION
        if(yearSet
                &&monthSet
                &&daySet
                &&hourSet
                &&minuteSet
                &&secondSet){
            return DatatypeConstants.DURATION;
        }
        // DURATION_DAYTIME
        if(!yearSet
                &&!monthSet
                &&daySet
                &&hourSet
                &&minuteSet
                &&secondSet){
            return DatatypeConstants.DURATION_DAYTIME;
        }
        // DURATION_YEARMONTH
        if(yearSet
                &&monthSet
                &&!daySet
                &&!hourSet
                &&!minuteSet
                &&!secondSet){
            return DatatypeConstants.DURATION_YEARMONTH;
        }
        // nothing matches
        throw new IllegalStateException(
                "javax.xml.datatype.Duration#getXMLSchemaType():"
                        +" this Duration does not match one of the XML Schema date/time datatypes:"
                        +" year set = "+yearSet
                        +" month set = "+monthSet
                        +" day set = "+daySet
                        +" hour set = "+hourSet
                        +" minute set = "+minuteSet
                        +" second set = "+secondSet
        );
    }

    public abstract boolean isSet(final DatatypeConstants.Field field);

    public int getYears(){
        return getField(DatatypeConstants.YEARS).intValue();
    }

    public abstract Number getField(final DatatypeConstants.Field field);

    public int getMonths(){
        return getField(DatatypeConstants.MONTHS).intValue();
    }

    public int getDays(){
        return getField(DatatypeConstants.DAYS).intValue();
    }

    public int getHours(){
        return getField(DatatypeConstants.HOURS).intValue();
    }

    public int getMinutes(){
        return getField(DatatypeConstants.MINUTES).intValue();
    }

    public int getSeconds(){
        return getField(DatatypeConstants.SECONDS).intValue();
    }

    public long getTimeInMillis(final Calendar startInstant){
        Calendar cal=(Calendar)startInstant.clone();
        addTo(cal);
        return getCalendarTimeInMillis(cal)
                -getCalendarTimeInMillis(startInstant);
    }

    public abstract void addTo(Calendar calendar);

    private static long getCalendarTimeInMillis(final Calendar cal){
        return cal.getTime().getTime();
    }

    public long getTimeInMillis(final Date startInstant){
        Calendar cal=new GregorianCalendar();
        cal.setTime(startInstant);
        this.addTo(cal);
        return getCalendarTimeInMillis(cal)-startInstant.getTime();
    }

    public void addTo(Date date){
        // check data parameter
        if(date==null){
            throw new NullPointerException(
                    "Cannot call "
                            +this.getClass().getName()
                            +"#addTo(Date date) with date == null."
            );
        }
        Calendar cal=new GregorianCalendar();
        cal.setTime(date);
        this.addTo(cal);
        date.setTime(getCalendarTimeInMillis(cal));
    }

    public Duration subtract(final Duration rhs){
        return add(rhs.negate());
    }

    public abstract Duration add(final Duration rhs);

    public Duration multiply(int factor){
        return multiply(new BigDecimal(String.valueOf(factor)));
    }

    public abstract Duration multiply(final BigDecimal factor);

    public abstract Duration negate();

    public abstract Duration normalizeWith(final Calendar startTimeInstant);

    public boolean isLongerThan(final Duration duration){
        return compare(duration)==DatatypeConstants.GREATER;
    }

    public abstract int compare(final Duration duration);

    public boolean isShorterThan(final Duration duration){
        return compare(duration)==DatatypeConstants.LESSER;
    }

    public abstract int hashCode();

    public boolean equals(final Object duration){
        if(duration==null||!(duration instanceof Duration)){
            return false;
        }
        return compare((Duration)duration)==DatatypeConstants.EQUAL;
    }

    public String toString(){
        StringBuffer buf=new StringBuffer();
        if(getSign()<0){
            buf.append('-');
        }
        buf.append('P');
        BigInteger years=(BigInteger)getField(DatatypeConstants.YEARS);
        if(years!=null){
            buf.append(years+"Y");
        }
        BigInteger months=(BigInteger)getField(DatatypeConstants.MONTHS);
        if(months!=null){
            buf.append(months+"M");
        }
        BigInteger days=(BigInteger)getField(DatatypeConstants.DAYS);
        if(days!=null){
            buf.append(days+"D");
        }
        BigInteger hours=(BigInteger)getField(DatatypeConstants.HOURS);
        BigInteger minutes=(BigInteger)getField(DatatypeConstants.MINUTES);
        BigDecimal seconds=(BigDecimal)getField(DatatypeConstants.SECONDS);
        if(hours!=null||minutes!=null||seconds!=null){
            buf.append('T');
            if(hours!=null){
                buf.append(hours+"H");
            }
            if(minutes!=null){
                buf.append(minutes+"M");
            }
            if(seconds!=null){
                buf.append(toString(seconds)+"S");
            }
        }
        return buf.toString();
    }

    public abstract int getSign();

    private String toString(BigDecimal bd){
        String intString=bd.unscaledValue().toString();
        int scale=bd.scale();
        if(scale==0){
            return intString;
        }
        /** Insert decimal point */
        StringBuffer buf;
        int insertionPoint=intString.length()-scale;
        if(insertionPoint==0){ /** Point goes right before intVal */
            return "0."+intString;
        }else if(insertionPoint>0){ /** Point goes inside intVal */
            buf=new StringBuffer(intString);
            buf.insert(insertionPoint,'.');
        }else{ /** We must insert zeros between point and intVal */
            buf=new StringBuffer(3-insertionPoint+intString.length());
            buf.append("0.");
            for(int i=0;i<-insertionPoint;i++){
                buf.append('0');
            }
            buf.append(intString);
        }
        return buf.toString();
    }
}
