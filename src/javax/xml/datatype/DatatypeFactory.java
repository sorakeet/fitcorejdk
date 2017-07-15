/**
 * Copyright (c) 2004, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.datatype;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class DatatypeFactory{
    public static final String DATATYPEFACTORY_PROPERTY=
            // We use a String constant here, rather than calling
            // DatatypeFactory.class.getName() - in order to make javadoc
            // generate a See Also: Constant Field Value link.
            "javax.xml.datatype.DatatypeFactory";
    public static final String DATATYPEFACTORY_IMPLEMENTATION_CLASS=
            // We use new String() here to prevent javadoc from generating
            // a See Also: Constant Field Value link.
            new String("com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl");
    private static final Pattern XDTSCHEMA_YMD=
            Pattern.compile("[^DT]*");
    private static final Pattern XDTSCHEMA_DTD=
            Pattern.compile("[^YM]*[DT].*");

    protected DatatypeFactory(){
    }

    public static DatatypeFactory newInstance()
            throws DatatypeConfigurationException{
        return FactoryFinder.find(
                /** The default property name according to the JAXP spec */
                DatatypeFactory.class,
                /** The fallback implementation class name */
                DATATYPEFACTORY_IMPLEMENTATION_CLASS);
    }

    public static DatatypeFactory newInstance(String factoryClassName,ClassLoader classLoader)
            throws DatatypeConfigurationException{
        return FactoryFinder.newInstance(DatatypeFactory.class,
                factoryClassName,classLoader,false);
    }

    public Duration newDuration(
            final boolean isPositive,
            final int years,
            final int months,
            final int days,
            final int hours,
            final int minutes,
            final int seconds){
        // years may not be set
        BigInteger realYears=(years!=DatatypeConstants.FIELD_UNDEFINED)?BigInteger.valueOf((long)years):null;
        // months may not be set
        BigInteger realMonths=(months!=DatatypeConstants.FIELD_UNDEFINED)?BigInteger.valueOf((long)months):null;
        // days may not be set
        BigInteger realDays=(days!=DatatypeConstants.FIELD_UNDEFINED)?BigInteger.valueOf((long)days):null;
        // hours may not be set
        BigInteger realHours=(hours!=DatatypeConstants.FIELD_UNDEFINED)?BigInteger.valueOf((long)hours):null;
        // minutes may not be set
        BigInteger realMinutes=(minutes!=DatatypeConstants.FIELD_UNDEFINED)?BigInteger.valueOf((long)minutes):null;
        // seconds may not be set
        BigDecimal realSeconds=(seconds!=DatatypeConstants.FIELD_UNDEFINED)?BigDecimal.valueOf((long)seconds):null;
        return newDuration(
                isPositive,
                realYears,
                realMonths,
                realDays,
                realHours,
                realMinutes,
                realSeconds
        );
    }

    public abstract Duration newDuration(
            final boolean isPositive,
            final BigInteger years,
            final BigInteger months,
            final BigInteger days,
            final BigInteger hours,
            final BigInteger minutes,
            final BigDecimal seconds);

    public Duration newDurationDayTime(final String lexicalRepresentation){
        // lexicalRepresentation must be non-null
        if(lexicalRepresentation==null){
            throw new NullPointerException(
                    "Trying to create an xdt:dayTimeDuration with an invalid"
                            +" lexical representation of \"null\"");
        }
        // test lexicalRepresentation against spec regex
        Matcher matcher=XDTSCHEMA_DTD.matcher(lexicalRepresentation);
        if(!matcher.matches()){
            throw new IllegalArgumentException(
                    "Trying to create an xdt:dayTimeDuration with an invalid"
                            +" lexical representation of \""+lexicalRepresentation
                            +"\", data model requires years and months only.");
        }
        return newDuration(lexicalRepresentation);
    }

    public abstract Duration newDuration(final String lexicalRepresentation);

    public Duration newDurationDayTime(final long durationInMilliseconds){
        return newDuration(durationInMilliseconds);
    }

    public abstract Duration newDuration(final long durationInMilliSeconds);

    public Duration newDurationDayTime(
            final boolean isPositive,
            final int day,
            final int hour,
            final int minute,
            final int second){
        return newDurationDayTime(
                isPositive,
                BigInteger.valueOf((long)day),
                BigInteger.valueOf((long)hour),
                BigInteger.valueOf((long)minute),
                BigInteger.valueOf((long)second)
        );
    }

    public Duration newDurationDayTime(
            final boolean isPositive,
            final BigInteger day,
            final BigInteger hour,
            final BigInteger minute,
            final BigInteger second){
        return newDuration(
                isPositive,
                null,  // years
                null, // months
                day,
                hour,
                minute,
                (second!=null)?new BigDecimal(second):null
        );
    }

    public Duration newDurationYearMonth(
            final String lexicalRepresentation){
        // lexicalRepresentation must be non-null
        if(lexicalRepresentation==null){
            throw new NullPointerException(
                    "Trying to create an xdt:yearMonthDuration with an invalid"
                            +" lexical representation of \"null\"");
        }
        // test lexicalRepresentation against spec regex
        Matcher matcher=XDTSCHEMA_YMD.matcher(lexicalRepresentation);
        if(!matcher.matches()){
            throw new IllegalArgumentException(
                    "Trying to create an xdt:yearMonthDuration with an invalid"
                            +" lexical representation of \""+lexicalRepresentation
                            +"\", data model requires days and times only.");
        }
        return newDuration(lexicalRepresentation);
    }

    public Duration newDurationYearMonth(
            final long durationInMilliseconds){
        // create a Duration that only has sign, year & month
        // Duration is immutable, so need to create a new Duration
        // implementations may override this method in a more efficient way
        Duration fullDuration=newDuration(durationInMilliseconds);
        boolean isPositive=(fullDuration.getSign()==-1)?false:true;
        BigInteger years=
                (BigInteger)fullDuration.getField(DatatypeConstants.YEARS);
        if(years==null){
            years=BigInteger.ZERO;
        }
        BigInteger months=
                (BigInteger)fullDuration.getField(DatatypeConstants.MONTHS);
        if(months==null){
            months=BigInteger.ZERO;
        }
        return newDurationYearMonth(isPositive,years,months);
    }

    public Duration newDurationYearMonth(
            final boolean isPositive,
            final BigInteger year,
            final BigInteger month){
        return newDuration(
                isPositive,
                year,
                month,
                null, // days
                null, // hours
                null, // minutes
                null  // seconds
        );
    }

    public Duration newDurationYearMonth(
            final boolean isPositive,
            final int year,
            final int month){
        return newDurationYearMonth(
                isPositive,
                BigInteger.valueOf((long)year),
                BigInteger.valueOf((long)month));
    }

    public abstract XMLGregorianCalendar newXMLGregorianCalendar();

    public abstract XMLGregorianCalendar newXMLGregorianCalendar(final String lexicalRepresentation);

    public abstract XMLGregorianCalendar newXMLGregorianCalendar(final GregorianCalendar cal);

    public XMLGregorianCalendar newXMLGregorianCalendarDate(
            final int year,
            final int month,
            final int day,
            final int timezone){
        return newXMLGregorianCalendar(
                year,
                month,
                day,
                DatatypeConstants.FIELD_UNDEFINED, // hour
                DatatypeConstants.FIELD_UNDEFINED, // minute
                DatatypeConstants.FIELD_UNDEFINED, // second
                DatatypeConstants.FIELD_UNDEFINED, // millisecond
                timezone);
    }

    public XMLGregorianCalendar newXMLGregorianCalendar(
            final int year,
            final int month,
            final int day,
            final int hour,
            final int minute,
            final int second,
            final int millisecond,
            final int timezone){
        // year may be undefined
        BigInteger realYear=(year!=DatatypeConstants.FIELD_UNDEFINED)?BigInteger.valueOf((long)year):null;
        // millisecond may be undefined
        // millisecond must be >= 0 millisecond <= 1000
        BigDecimal realMillisecond=null; // undefined value
        if(millisecond!=DatatypeConstants.FIELD_UNDEFINED){
            if(millisecond<0||millisecond>1000){
                throw new IllegalArgumentException(
                        "javax.xml.datatype.DatatypeFactory#newXMLGregorianCalendar("
                                +"int year, int month, int day, int hour, int minute, int second, int millisecond, int timezone)"
                                +"with invalid millisecond: "+millisecond
                );
            }
            realMillisecond=BigDecimal.valueOf((long)millisecond).movePointLeft(3);
        }
        return newXMLGregorianCalendar(
                realYear,
                month,
                day,
                hour,
                minute,
                second,
                realMillisecond,
                timezone
        );
    }

    public abstract XMLGregorianCalendar newXMLGregorianCalendar(
            final BigInteger year,
            final int month,
            final int day,
            final int hour,
            final int minute,
            final int second,
            final BigDecimal fractionalSecond,
            final int timezone);

    public XMLGregorianCalendar newXMLGregorianCalendarTime(
            final int hours,
            final int minutes,
            final int seconds,
            final int timezone){
        return newXMLGregorianCalendar(
                DatatypeConstants.FIELD_UNDEFINED, // Year
                DatatypeConstants.FIELD_UNDEFINED, // Month
                DatatypeConstants.FIELD_UNDEFINED, // Day
                hours,
                minutes,
                seconds,
                DatatypeConstants.FIELD_UNDEFINED, //Millisecond
                timezone);
    }

    public XMLGregorianCalendar newXMLGregorianCalendarTime(
            final int hours,
            final int minutes,
            final int seconds,
            final int milliseconds,
            final int timezone){
        // millisecond may be undefined
        // millisecond must be >= 0 millisecond <= 1000
        BigDecimal realMilliseconds=null; // undefined value
        if(milliseconds!=DatatypeConstants.FIELD_UNDEFINED){
            if(milliseconds<0||milliseconds>1000){
                throw new IllegalArgumentException(
                        "javax.xml.datatype.DatatypeFactory#newXMLGregorianCalendarTime("
                                +"int hours, int minutes, int seconds, int milliseconds, int timezone)"
                                +"with invalid milliseconds: "+milliseconds
                );
            }
            realMilliseconds=BigDecimal.valueOf((long)milliseconds).movePointLeft(3);
        }
        return newXMLGregorianCalendarTime(
                hours,
                minutes,
                seconds,
                realMilliseconds,
                timezone
        );
    }

    public XMLGregorianCalendar newXMLGregorianCalendarTime(
            final int hours,
            final int minutes,
            final int seconds,
            final BigDecimal fractionalSecond,
            final int timezone){
        return newXMLGregorianCalendar(
                null, // year
                DatatypeConstants.FIELD_UNDEFINED, // month
                DatatypeConstants.FIELD_UNDEFINED, // day
                hours,
                minutes,
                seconds,
                fractionalSecond,
                timezone);
    }
}
