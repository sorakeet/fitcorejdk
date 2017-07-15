/**
 * Copyright (c) 2004, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.xerces.internal.jaxp.datatype;

import com.sun.org.apache.xerces.internal.util.DatatypeMessageFormatter;
import com.sun.org.apache.xerces.internal.utils.SecuritySupport;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class XMLGregorianCalendarImpl
        extends XMLGregorianCalendar
        implements Serializable, Cloneable{
    public static final XMLGregorianCalendar LEAP_YEAR_DEFAULT=
            createDateTime(
                    400,  //year
                    DatatypeConstants.JANUARY,  //month
                    1,  // day
                    0,  // hour
                    0,  // minute
                    0,  // second
                    DatatypeConstants.FIELD_UNDEFINED,  // milliseconds
                    DatatypeConstants.FIELD_UNDEFINED // timezone
            );
    private static final BigInteger BILLION=new BigInteger("1000000000");
    private static final Date PURE_GREGORIAN_CHANGE=
            new Date(Long.MIN_VALUE);
    private static final int YEAR=0;
    private static final int MONTH=1;
    private static final int DAY=2;
    private static final int HOUR=3;
    private static final int MINUTE=4;
    private static final int SECOND=5;
    private static final int MILLISECOND=6;
    private static final int TIMEZONE=7;
    private static final String FIELD_NAME[]={
            "Year",
            "Month",
            "Day",
            "Hour",
            "Minute",
            "Second",
            "Millisecond",
            "Timezone"
    };
    private static final long serialVersionUID=1L;
    private static final BigInteger FOUR=BigInteger.valueOf(4);
    private static final BigInteger HUNDRED=BigInteger.valueOf(100);
    private static final BigInteger FOUR_HUNDRED=BigInteger.valueOf(400);
    private static final BigInteger SIXTY=BigInteger.valueOf(60);
    private static final BigInteger TWENTY_FOUR=BigInteger.valueOf(24);
    private static final BigInteger TWELVE=BigInteger.valueOf(12);
    private static final BigDecimal DECIMAL_ZERO=new BigDecimal("0");
    private static final BigDecimal DECIMAL_ONE=new BigDecimal("1");
    private static final BigDecimal DECIMAL_SIXTY=new BigDecimal("60");
    // Constructors
    private static int daysInMonth[]={0,  // XML Schema months start at 1.
            31,28,31,30,31,30,
            31,31,30,31,30,31};
    private BigInteger eon=null;
    private int year=DatatypeConstants.FIELD_UNDEFINED;
    private int month=DatatypeConstants.FIELD_UNDEFINED;
    private int day=DatatypeConstants.FIELD_UNDEFINED;
    // Factories
    private int timezone=DatatypeConstants.FIELD_UNDEFINED;
    private int hour=DatatypeConstants.FIELD_UNDEFINED;
    private int minute=DatatypeConstants.FIELD_UNDEFINED;
    private int second=DatatypeConstants.FIELD_UNDEFINED;
    private BigDecimal fractionalSecond=null;

    protected XMLGregorianCalendarImpl(String lexicalRepresentation)
            throws IllegalArgumentException{
        // compute format string for this lexical representation.
        String format=null;
        String lexRep=lexicalRepresentation;
        final int NOT_FOUND=-1;
        int lexRepLength=lexRep.length();
        // current parser needs a format string,
        // use following heuristics to figure out what xml schema date/time
        // datatype this lexical string could represent.
        // Fix 4971612: invalid SCCS macro substitution in data string,
        //   no %{alpha}% to avoid SCCS maco substitution
        if(lexRep.indexOf('T')!=NOT_FOUND){
            // found Date Time separater, must be xsd:DateTime
            format="%Y-%M-%DT%h:%m:%s"+"%z";
        }else if(lexRepLength>=3&&lexRep.charAt(2)==':'){
            // found ":", must be xsd:Time
            format="%h:%m:%s"+"%z";
        }else if(lexRep.startsWith("--")){
            // check for gDay || gMonth || gMonthDay
            if(lexRepLength>=3&&lexRep.charAt(2)=='-'){
                // gDay, ---DD(z?)
                format="---%D"+"%z";
            }else if(lexRepLength==4     // --MM
                    ||lexRepLength==5     // --MMZ
                    ||lexRepLength==10){ // --MMSHH:MM
                // gMonth, --MM(z?),
                // per XML Schema Errata, used to be --MM--(z?)
                format="--%M"+"%z";
            }else{
                // gMonthDay, --MM-DD(z?), (or invalid lexicalRepresentation)
                // length should be:
                //  7: --MM-DD
                //  8: --MM-DDZ
                // 13: --MM-DDSHH:MM
                format="--%M-%D"+"%z";
            }
        }else{
            // check for Date || GYear | GYearMonth
            int countSeparator=0;
            // start at index 1 to skip potential negative sign for year.
            int timezoneOffset=lexRep.indexOf(':');
            if(timezoneOffset!=NOT_FOUND){
                // found timezone, strip it off for distinguishing
                // between Date, GYear and GYearMonth so possible
                // negative sign in timezone is not mistaken as
                // a separator.
                lexRepLength-=6;
            }
            for(int i=1;i<lexRepLength;i++){
                if(lexRep.charAt(i)=='-'){
                    countSeparator++;
                }
            }
            if(countSeparator==0){
                // GYear
                format="%Y"+"%z";
            }else if(countSeparator==1){
                // GYearMonth
                format="%Y-%M"+"%z";
            }else{
                // Date or invalid lexicalRepresentation
                // Fix 4971612: invalid SCCS macro substitution in data string
                format="%Y-%M-%D"+"%z";
            }
        }
        Parser p=new Parser(format,lexRep);
        p.parse();
        // check for validity
        if(!isValid()){
            throw new IllegalArgumentException(
                    DatatypeMessageFormatter.formatMessage(null,"InvalidXGCRepresentation",new Object[]{lexicalRepresentation})
                    //"\"" + lexicalRepresentation + "\" is not a valid representation of an XML Gregorian Calendar value."
            );
        }
    }

    public XMLGregorianCalendarImpl(){
        // field initializers already do the correct initialization.
    }
    // Accessors

    protected XMLGregorianCalendarImpl(
            BigInteger year,
            int month,
            int day,
            int hour,
            int minute,
            int second,
            BigDecimal fractionalSecond,
            int timezone){
        setYear(year);
        setMonth(month);
        setDay(day);
        setTime(hour,minute,second,fractionalSecond);
        setTimezone(timezone);
        // check for validity
        if(!isValid()){
            throw new IllegalArgumentException(
                    DatatypeMessageFormatter.formatMessage(null,
                            "InvalidXGCValue-fractional",
                            new Object[]{year,new Integer(month),new Integer(day),
                                    new Integer(hour),new Integer(minute),new Integer(second),
                                    fractionalSecond,new Integer(timezone)})
            );
            /**
             String yearString = "null";
             if (year != null) {
             yearString = year.toString();
             }
             String fractionalSecondString = "null";
             if (fractionalSecond != null) {
             fractionalSecondString = fractionalSecond.toString();
             }

             throw new IllegalArgumentException(
             "year = " + yearString
             + ", month = " + month
             + ", day = " + day
             + ", hour = " + hour
             + ", minute = " + minute
             + ", second = " + second
             + ", fractionalSecond = " + fractionalSecondString
             + ", timezone = " + timezone
             + ", is not a valid representation of an XML Gregorian Calendar value."
             );
             */
        }
    }    public BigInteger getEon(){
        return eon;
    }

    private XMLGregorianCalendarImpl(
            int year,
            int month,
            int day,
            int hour,
            int minute,
            int second,
            int millisecond,
            int timezone){
        setYear(year);
        setMonth(month);
        setDay(day);
        setTime(hour,minute,second);
        setTimezone(timezone);
        setMillisecond(millisecond);
        if(!isValid()){
            throw new IllegalArgumentException(
                    DatatypeMessageFormatter.formatMessage(null,
                            "InvalidXGCValue-milli",
                            new Object[]{new Integer(year),new Integer(month),new Integer(day),
                                    new Integer(hour),new Integer(minute),new Integer(second),
                                    new Integer(millisecond),new Integer(timezone)})
            );
            /**
             throw new IllegalArgumentException(
             "year = " + year
             + ", month = " + month
             + ", day = " + day
             + ", hour = " + hour
             + ", minute = " + minute
             + ", second = " + second
             + ", millisecond = " + millisecond
             + ", timezone = " + timezone
             + ", is not a valid representation of an XML Gregorian Calendar value."
             );
             */
        }
    }    public int getYear(){
        return year;
    }

    public XMLGregorianCalendarImpl(GregorianCalendar cal){
        int year=cal.get(Calendar.YEAR);
        if(cal.get(Calendar.ERA)==GregorianCalendar.BC){
            year=-year;
        }
        this.setYear(year);
        // Calendar.MONTH is zero based, XSD Date datatype's month field starts
        // with JANUARY as 1.
        this.setMonth(cal.get(Calendar.MONTH)+1);
        this.setDay(cal.get(Calendar.DAY_OF_MONTH));
        this.setTime(
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND),
                cal.get(Calendar.MILLISECOND));
        // Calendar ZONE_OFFSET and DST_OFFSET fields are in milliseconds.
        int offsetInMinutes=(cal.get(Calendar.ZONE_OFFSET)+cal.get(Calendar.DST_OFFSET))/(60*1000);
        this.setTimezone(offsetInMinutes);
    }    public BigInteger getEonAndYear(){
        // both are defined
        if(year!=DatatypeConstants.FIELD_UNDEFINED
                &&eon!=null){
            return eon.add(BigInteger.valueOf((long)year));
        }
        // only year is defined
        if(year!=DatatypeConstants.FIELD_UNDEFINED
                &&eon==null){
            return BigInteger.valueOf((long)year);
        }
        // neither are defined
        // or only eon is defined which is not valid without a year
        return null;
    }

    public static XMLGregorianCalendar createDateTime(
            BigInteger year,
            int month,
            int day,
            int hours,
            int minutes,
            int seconds,
            BigDecimal fractionalSecond,
            int timezone){
        return new XMLGregorianCalendarImpl(
                year,
                month,
                day,
                hours,
                minutes,
                seconds,
                fractionalSecond,
                timezone);
    }    public int getMonth(){
        return month;
    }

    public static XMLGregorianCalendar createDateTime(
            int year,
            int month,
            int day,
            int hour,
            int minute,
            int second){
        return new XMLGregorianCalendarImpl(
                year,
                month,
                day,
                hour,
                minute,
                second,
                DatatypeConstants.FIELD_UNDEFINED,  //millisecond
                DatatypeConstants.FIELD_UNDEFINED //timezone
        );
    }    public int getDay(){
        return day;
    }

    public static XMLGregorianCalendar createDateTime(
            int year,
            int month,
            int day,
            int hours,
            int minutes,
            int seconds,
            int milliseconds,
            int timezone){
        return new XMLGregorianCalendarImpl(
                year,
                month,
                day,
                hours,
                minutes,
                seconds,
                milliseconds,
                timezone);
    }    public int getTimezone(){
        return timezone;
    }

    public static XMLGregorianCalendar createDate(
            int year,
            int month,
            int day,
            int timezone){
        return new XMLGregorianCalendarImpl(
                year,
                month,
                day,
                DatatypeConstants.FIELD_UNDEFINED, // hour
                DatatypeConstants.FIELD_UNDEFINED, // minute
                DatatypeConstants.FIELD_UNDEFINED, // second
                DatatypeConstants.FIELD_UNDEFINED, // millisecond
                timezone);
    }    public int getHour(){
        return hour;
    }

    public static XMLGregorianCalendar createTime(
            int hours,
            int minutes,
            int seconds,
            int timezone){
        return new XMLGregorianCalendarImpl(
                DatatypeConstants.FIELD_UNDEFINED, // Year
                DatatypeConstants.FIELD_UNDEFINED, // Month
                DatatypeConstants.FIELD_UNDEFINED, // Day
                hours,
                minutes,
                seconds,
                DatatypeConstants.FIELD_UNDEFINED, //Millisecond
                timezone);
    }    public int getMinute(){
        return minute;
    }

    public static XMLGregorianCalendar createTime(
            int hours,
            int minutes,
            int seconds,
            BigDecimal fractionalSecond,
            int timezone){
        return new XMLGregorianCalendarImpl(
                null,            // Year
                DatatypeConstants.FIELD_UNDEFINED, // month
                DatatypeConstants.FIELD_UNDEFINED, // day
                hours,
                minutes,
                seconds,
                fractionalSecond,
                timezone);
    }    public int getSecond(){
        return second;
    }

    public static XMLGregorianCalendar createTime(
            int hours,
            int minutes,
            int seconds,
            int milliseconds,
            int timezone){
        return new XMLGregorianCalendarImpl(
                DatatypeConstants.FIELD_UNDEFINED, // year
                DatatypeConstants.FIELD_UNDEFINED, // month
                DatatypeConstants.FIELD_UNDEFINED, // day
                hours,
                minutes,
                seconds,
                milliseconds,
                timezone);
    }

    public static XMLGregorianCalendar parse(String lexicalRepresentation){
        return new XMLGregorianCalendarImpl(lexicalRepresentation);
    }    public int getMillisecond(){
        if(fractionalSecond==null){
            return DatatypeConstants.FIELD_UNDEFINED;
        }else{
            // TODO: Non-optimal solution for now.
            // Efficient implementation would only store as BigDecimal
            // when needed and millisecond otherwise.
            return fractionalSecond.movePointRight(3).intValue();
        }
    }

    private static boolean isDigit(char ch){
        return '0'<=ch&&ch<='9';
    }    public BigDecimal getFractionalSecond(){
        return fractionalSecond;
    }
    // setters

    static BigInteger sanitize(Number value,int signum){
        if(signum==0||value==null){
            return BigInteger.ZERO;
        }
        return (signum<0)?((BigInteger)value).negate():(BigInteger)value;
    }

    private BigDecimal getSeconds(){
        if(second==DatatypeConstants.FIELD_UNDEFINED){
            return DECIMAL_ZERO;
        }
        BigDecimal result=BigDecimal.valueOf((long)second);
        if(fractionalSecond!=null){
            return result.add(fractionalSecond);
        }else{
            return result;
        }
    }    public void setYear(int year){
        if(year==DatatypeConstants.FIELD_UNDEFINED){
            this.year=DatatypeConstants.FIELD_UNDEFINED;
            this.eon=null;
        }else if(Math.abs(year)<BILLION.intValue()){
            this.year=year;
            this.eon=null;
        }else{
            BigInteger theYear=BigInteger.valueOf((long)year);
            BigInteger remainder=theYear.remainder(BILLION);
            this.year=remainder.intValue();
            setEon(theYear.subtract(remainder));
        }
    }

    private void invalidFieldValue(int field,int value){
        throw new IllegalArgumentException(
                DatatypeMessageFormatter.formatMessage(null,"InvalidFieldValue",
                        new Object[]{new Integer(value),FIELD_NAME[field]})
        );
    }    private void setEon(BigInteger eon){
        if(eon!=null&&eon.compareTo(BigInteger.ZERO)==0){
            // Treat ZERO as field being undefined.
            this.eon=null;
        }else{
            this.eon=eon;
        }
    }

    private void testHour(){
        // http://www.w3.org/2001/05/xmlschema-errata#e2-45
        if(getHour()==24){
            if(getMinute()!=0
                    ||getSecond()!=0){
                invalidFieldValue(HOUR,getHour());
            }
            // while 0-24 is acceptable in the lexical space, 24 is not valid in value space
            // W3C XML Schema Part 2, Section 3.2.7.1
            setHour(0,false);
            add(new DurationImpl(true,0,0,1,0,0,0));
        }
    }    public void setMonth(int month){
        if(month<DatatypeConstants.JANUARY||DatatypeConstants.DECEMBER<month)
            if(month!=DatatypeConstants.FIELD_UNDEFINED)
                invalidFieldValue(MONTH,month);
        this.month=month;
    }

    private void setHour(int hour,boolean validate){
        if(hour<0||hour>24){
            if(hour!=DatatypeConstants.FIELD_UNDEFINED){
                invalidFieldValue(HOUR,hour);
            }
        }
        this.hour=hour;
        if(validate){
            testHour();
        }
    }    public void setDay(int day){
        if(day<1||31<day)
            if(day!=DatatypeConstants.FIELD_UNDEFINED)
                invalidFieldValue(DAY,day);
        this.day=day;
    }

    public void clear(){
        eon=null;
        year=DatatypeConstants.FIELD_UNDEFINED;
        month=DatatypeConstants.FIELD_UNDEFINED;
        day=DatatypeConstants.FIELD_UNDEFINED;
        timezone=DatatypeConstants.FIELD_UNDEFINED;  // in minutes
        hour=DatatypeConstants.FIELD_UNDEFINED;
        minute=DatatypeConstants.FIELD_UNDEFINED;
        second=DatatypeConstants.FIELD_UNDEFINED;
        fractionalSecond=null;
    }    public void setTimezone(int offset){
        if(offset<-14*60||14*60<offset)
            if(offset!=DatatypeConstants.FIELD_UNDEFINED)
                invalidFieldValue(TIMEZONE,offset);
        this.timezone=offset;
    }

    public void reset(){
        //PENDING : Implementation of reset method
    }    public void setTime(int hour,int minute,int second){
        setTime(hour,minute,second,null);
    }

    public void setYear(BigInteger year){
        if(year==null){
            this.eon=null;
            this.year=DatatypeConstants.FIELD_UNDEFINED;
        }else{
            BigInteger temp=year.remainder(BILLION);
            this.year=temp.intValue();
            setEon(year.subtract(temp));
        }
    }

    private final class Parser{
        private final String format;
        private final String value;
        private final int flen;
        private final int vlen;
        private int fidx;
        private int vidx;

        private Parser(String format,String value){
            this.format=format;
            this.value=value;
            this.flen=format.length();
            this.vlen=value.length();
        }

        public void parse() throws IllegalArgumentException{
            while(fidx<flen){
                char fch=format.charAt(fidx++);
                if(fch!='%'){ // not a meta character
                    skip(fch);
                    continue;
                }
                // seen meta character. we don't do error check against the format
                switch(format.charAt(fidx++)){
                    case 'Y': // year
                        parseAndSetYear(4);
                        break;
                    case 'M': // month
                        setMonth(parseInt(2,2));
                        break;
                    case 'D': // days
                        setDay(parseInt(2,2));
                        break;
                    case 'h': // hours
                        setHour(parseInt(2,2),false);
                        break;
                    case 'm': // minutes
                        setMinute(parseInt(2,2));
                        break;
                    case 's': // parse seconds.
                        setSecond(parseInt(2,2));
                        if(peek()=='.'){
                            setFractionalSecond(parseBigDecimal());
                        }
                        break;
                    case 'z': // time zone. missing, 'Z', or [+-]nn:nn
                        char vch=peek();
                        if(vch=='Z'){
                            vidx++;
                            setTimezone(0);
                        }else if(vch=='+'||vch=='-'){
                            vidx++;
                            int h=parseInt(2,2);
                            skip(':');
                            int m=parseInt(2,2);
                            setTimezone((h*60+m)*(vch=='+'?1:-1));
                        }
                        break;
                    default:
                        // illegal meta character. impossible.
                        throw new InternalError();
                }
            }
            if(vidx!=vlen){
                // some tokens are left in the input
                throw new IllegalArgumentException(value); //,vidx);
            }
            testHour();
        }

        private void skip(char ch) throws IllegalArgumentException{
            if(read()!=ch){
                throw new IllegalArgumentException(value); //,vidx-1);
            }
        }

        private char read() throws IllegalArgumentException{
            if(vidx==vlen){
                throw new IllegalArgumentException(value); //,vidx);
            }
            return value.charAt(vidx++);
        }

        private int parseInt(int minDigits,int maxDigits)
                throws IllegalArgumentException{
            int n=0;
            char ch;
            int vstart=vidx;
            while(isDigit(ch=peek())&&(vidx-vstart)<=maxDigits){
                vidx++;
                n=n*10+ch-'0';
            }
            if((vidx-vstart)<minDigits){
                // we are expecting more digits
                throw new IllegalArgumentException(value); //,vidx);
            }
            return n;
        }

        private void parseAndSetYear(int minDigits)
                throws IllegalArgumentException{
            int vstart=vidx;
            int n=0;
            boolean neg=false;
            // skip leading negative, if it exists
            if(peek()=='-'){
                vidx++;
                neg=true;
            }
            while(true){
                char ch=peek();
                if(!isDigit(ch))
                    break;
                vidx++;
                n=n*10+ch-'0';
            }
            if((vidx-vstart)<minDigits){
                // we are expecting more digits
                throw new IllegalArgumentException(value); //,vidx);
            }
            if(vidx-vstart<7){
                // definitely int only. I don't know the exact # of digits that can be in int,
                // but as long as we can catch (0-9999) range, that should be enough.
                if(neg) n=-n;
                year=n;
                eon=null;
            }else{
                setYear(new BigInteger(value.substring(vstart,vidx)));
            }
        }

        private char peek() throws IllegalArgumentException{
            if(vidx==vlen){
                return (char)-1;
            }
            return value.charAt(vidx);
        }

        private BigDecimal parseBigDecimal()
                throws IllegalArgumentException{
            int vstart=vidx;
            if(peek()=='.'){
                vidx++;
            }else{
                throw new IllegalArgumentException(value);
            }
            while(isDigit(peek())){
                vidx++;
            }
            return new BigDecimal(value.substring(vstart,vidx));
        }
    }

    public void setHour(int hour){
        setHour(hour,true);
    }



    public void setMinute(int minute){
        if(minute<0||59<minute)
            if(minute!=DatatypeConstants.FIELD_UNDEFINED)
                invalidFieldValue(MINUTE,minute);
        this.minute=minute;
    }

    public void setSecond(int second){
        if(second<0||60<second)   // leap second allows for 60
            if(second!=DatatypeConstants.FIELD_UNDEFINED)
                invalidFieldValue(SECOND,second);
        this.second=second;
    }

    public void setTime(
            int hour,
            int minute,
            int second,
            BigDecimal fractional){
        setHour(hour,false);
        setMinute(minute);
        if(second!=60){
            setSecond(second);
        }else if((hour==23&&minute==59)||(hour==0&&minute==0)){
            setSecond(second);
        }else{
            invalidFieldValue(SECOND,second);
        }
        setFractionalSecond(fractional);
        // must test hour after setting seconds
        testHour();
    }

    public void setTime(int hour,int minute,int second,int millisecond){
        setHour(hour,false);
        setMinute(minute);
        if(second!=60){
            setSecond(second);
        }else if((hour==23&&minute==59)||(hour==0&&minute==0)){
            setSecond(second);
        }else{
            invalidFieldValue(SECOND,second);
        }
        setMillisecond(millisecond);
        // must test hour after setting seconds
        testHour();
    }

    // comparisons
    public int compare(XMLGregorianCalendar rhs){
        XMLGregorianCalendar lhs=this;
        int result=DatatypeConstants.INDETERMINATE;
        XMLGregorianCalendarImpl P=(XMLGregorianCalendarImpl)lhs;
        XMLGregorianCalendarImpl Q=(XMLGregorianCalendarImpl)rhs;
        if(P.getTimezone()==Q.getTimezone()){
            // Optimization:
            // both instances are in same timezone or
            // both are FIELD_UNDEFINED.
            // Avoid costly normalization of timezone to 'Z' time.
            return internalCompare(P,Q);
        }else if(P.getTimezone()!=DatatypeConstants.FIELD_UNDEFINED&&
                Q.getTimezone()!=DatatypeConstants.FIELD_UNDEFINED){
            // Both instances have different timezones.
            // Normalize to UTC time and compare.
            P=(XMLGregorianCalendarImpl)P.normalize();
            Q=(XMLGregorianCalendarImpl)Q.normalize();
            return internalCompare(P,Q);
        }else if(P.getTimezone()!=DatatypeConstants.FIELD_UNDEFINED){
            if(P.getTimezone()!=0){
                P=(XMLGregorianCalendarImpl)P.normalize();
            }
            // C. step 1
            XMLGregorianCalendar MinQ=Q.normalizeToTimezone(DatatypeConstants.MIN_TIMEZONE_OFFSET);
            result=internalCompare(P,MinQ);
            if(result==DatatypeConstants.LESSER){
                return result;
            }
            // C. step 2
            XMLGregorianCalendar MaxQ=Q.normalizeToTimezone(DatatypeConstants.MAX_TIMEZONE_OFFSET);
            result=internalCompare(P,MaxQ);
            if(result==DatatypeConstants.GREATER){
                return result;
            }else{
                // C. step 3
                return DatatypeConstants.INDETERMINATE;
            }
        }else{ // Q.getTimezone() != DatatypeConstants.FIELD_UNDEFINED
            // P has no timezone and Q does.
            if(Q.getTimezone()!=0){
                Q=(XMLGregorianCalendarImpl)Q.normalizeToTimezone(Q.getTimezone());
            }
            // D. step 1
            XMLGregorianCalendar MaxP=P.normalizeToTimezone(DatatypeConstants.MAX_TIMEZONE_OFFSET);
            result=internalCompare(MaxP,Q);
            if(result==DatatypeConstants.LESSER){
                return result;
            }
            // D. step 2
            XMLGregorianCalendar MinP=P.normalizeToTimezone(DatatypeConstants.MIN_TIMEZONE_OFFSET);
            result=internalCompare(MinP,Q);
            if(result==DatatypeConstants.GREATER){
                return result;
            }else{
                // D. step 3
                return DatatypeConstants.INDETERMINATE;
            }
        }
    }

    public XMLGregorianCalendar normalize(){
        XMLGregorianCalendar normalized=normalizeToTimezone(timezone);
        // if timezone was undefined, leave it undefined
        if(getTimezone()==DatatypeConstants.FIELD_UNDEFINED){
            normalized.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
        }
        // if milliseconds was undefined, leave it undefined
        if(getMillisecond()==DatatypeConstants.FIELD_UNDEFINED){
            normalized.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
        }
        return normalized;
    }

    private XMLGregorianCalendar normalizeToTimezone(int timezone){
        int minutes=timezone;
        XMLGregorianCalendar result=(XMLGregorianCalendar)this.clone();
        // normalizing to UTC time negates the timezone offset before
        // addition.
        minutes=-minutes;
        Duration d=new DurationImpl(minutes>=0, // isPositive
                0, //years
                0, //months
                0, //days
                0, //hours
                minutes<0?-minutes:minutes, // absolute
                0  //seconds
        );
        result.add(d);
        // set to zulu UTC time.
        result.setTimezone(0);
        return result;
    }

    private static int internalCompare(XMLGregorianCalendar P,
                                       XMLGregorianCalendar Q){
        int result;
        // compare Year.
        if(P.getEon()==Q.getEon()){
            // Eon field is only equal when null.
            // optimized case for comparing year not requiring eon field.
            result=compareField(P.getYear(),Q.getYear());
            if(result!=DatatypeConstants.EQUAL){
                return result;
            }
        }else{
            result=compareField(P.getEonAndYear(),Q.getEonAndYear());
            if(result!=DatatypeConstants.EQUAL){
                return result;
            }
        }
        result=compareField(P.getMonth(),Q.getMonth());
        if(result!=DatatypeConstants.EQUAL){
            return result;
        }
        result=compareField(P.getDay(),Q.getDay());
        if(result!=DatatypeConstants.EQUAL){
            return result;
        }
        result=compareField(P.getHour(),Q.getHour());
        if(result!=DatatypeConstants.EQUAL){
            return result;
        }
        result=compareField(P.getMinute(),Q.getMinute());
        if(result!=DatatypeConstants.EQUAL){
            return result;
        }
        result=compareField(P.getSecond(),Q.getSecond());
        if(result!=DatatypeConstants.EQUAL){
            return result;
        }
        result=compareField(P.getFractionalSecond(),Q.getFractionalSecond());
        return result;
    }

    private static int compareField(int Pfield,int Qfield){
        if(Pfield==Qfield){
            //fields are either equal in value or both undefined.
            // Step B. 1.1 AND optimized result of performing 1.1-1.4.
            return DatatypeConstants.EQUAL;
        }else{
            if(Pfield==DatatypeConstants.FIELD_UNDEFINED||Qfield==DatatypeConstants.FIELD_UNDEFINED){
                // Step B. 1.2
                return DatatypeConstants.INDETERMINATE;
            }else{
                // Step B. 1.3-4.
                return (Pfield<Qfield?DatatypeConstants.LESSER:DatatypeConstants.GREATER);
            }
        }
    }

    private static int compareField(BigInteger Pfield,BigInteger Qfield){
        if(Pfield==null){
            return (Qfield==null?DatatypeConstants.EQUAL:DatatypeConstants.INDETERMINATE);
        }
        if(Qfield==null){
            return DatatypeConstants.INDETERMINATE;
        }
        return Pfield.compareTo(Qfield);
    }

    private static int compareField(BigDecimal Pfield,BigDecimal Qfield){
        // optimization. especially when both arguments are null.
        if(Pfield==Qfield){
            return DatatypeConstants.EQUAL;
        }
        if(Pfield==null){
            Pfield=DECIMAL_ZERO;
        }
        if(Qfield==null){
            Qfield=DECIMAL_ZERO;
        }
        return Pfield.compareTo(Qfield);
    }

    public boolean equals(Object obj){
        if(obj==null||!(obj instanceof XMLGregorianCalendar)){
            return false;
        }
        return compare((XMLGregorianCalendar)obj)==DatatypeConstants.EQUAL;
    }

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
            gc=this.normalizeToTimezone(getTimezone());
        }
        return gc.getYear()+gc.getMonth()+gc.getDay()+
                gc.getHour()+gc.getMinute()+gc.getSecond();
    }



    public String toXMLFormat(){
        QName typekind=getXMLSchemaType();
        String formatString=null;
        // Fix 4971612: invalid SCCS macro substitution in data string
        //   no %{alpha}% to avoid SCCS macro substitution
        if(typekind==DatatypeConstants.DATETIME){
            formatString="%Y-%M-%DT%h:%m:%s"+"%z";
        }else if(typekind==DatatypeConstants.DATE){
            formatString="%Y-%M-%D"+"%z";
        }else if(typekind==DatatypeConstants.TIME){
            formatString="%h:%m:%s"+"%z";
        }else if(typekind==DatatypeConstants.GMONTH){
            formatString="--%M"+"%z";
        }else if(typekind==DatatypeConstants.GDAY){
            formatString="---%D"+"%z";
        }else if(typekind==DatatypeConstants.GYEAR){
            formatString="%Y"+"%z";
        }else if(typekind==DatatypeConstants.GYEARMONTH){
            formatString="%Y-%M"+"%z";
        }else if(typekind==DatatypeConstants.GMONTHDAY){
            formatString="--%M-%D"+"%z";
        }
        return format(formatString);
    }

    public QName getXMLSchemaType(){
        int mask=
                (year!=DatatypeConstants.FIELD_UNDEFINED?0x20:0)|
                        (month!=DatatypeConstants.FIELD_UNDEFINED?0x10:0)|
                        (day!=DatatypeConstants.FIELD_UNDEFINED?0x08:0)|
                        (hour!=DatatypeConstants.FIELD_UNDEFINED?0x04:0)|
                        (minute!=DatatypeConstants.FIELD_UNDEFINED?0x02:0)|
                        (second!=DatatypeConstants.FIELD_UNDEFINED?0x01:0);
        switch(mask){
            case 0x3F:
                return DatatypeConstants.DATETIME;
            case 0x38:
                return DatatypeConstants.DATE;
            case 0x07:
                return DatatypeConstants.TIME;
            case 0x30:
                return DatatypeConstants.GYEARMONTH;
            case 0x18:
                return DatatypeConstants.GMONTHDAY;
            case 0x20:
                return DatatypeConstants.GYEAR;
            case 0x10:
                return DatatypeConstants.GMONTH;
            case 0x08:
                return DatatypeConstants.GDAY;
            default:
                throw new IllegalStateException(
                        this.getClass().getName()
                                +"#getXMLSchemaType() :"
                                +DatatypeMessageFormatter.formatMessage(null,"InvalidXGCFields",null)
                );
        }
    }

    public boolean isValid(){
        // since setters do not allow for invalid values,
        // (except for exceptional case of year field of zero),
        // no need to check for anything except for constraints
        // between fields.
        //check if days in month is valid. Can be dependent on leap year.
        if(getMonth()==DatatypeConstants.FEBRUARY){
            // years could not be set
            int maxDays=29;
            if(eon==null){
                if(year!=DatatypeConstants.FIELD_UNDEFINED)
                    maxDays=maximumDayInMonthFor(year,getMonth());
            }else{
                BigInteger years=getEonAndYear();
                if(years!=null){
                    maxDays=maximumDayInMonthFor(getEonAndYear(),DatatypeConstants.FEBRUARY);
                }
            }
            if(getDay()>maxDays){
                return false;
            }
        }
        // http://www.w3.org/2001/05/xmlschema-errata#e2-45
        if(getHour()==24){
            if(getMinute()!=0){
                return false;
            }else if(getSecond()!=0){
                return false;
            }
        }
        // XML Schema 1.0 specification defines year value of zero as
        // invalid. Allow this class to set year field to zero
        // since XML Schema 1.0 errata states that lexical zero will
        // be allowed in next version and treated as 1 B.C.E.
        if(eon==null){
            // optimize check.
            if(year==0){
                return false;
            }
        }else{
            BigInteger yearField=getEonAndYear();
            if(yearField!=null){
                int result=compareField(yearField,BigInteger.ZERO);
                if(result==DatatypeConstants.EQUAL){
                    return false;
                }
            }
        }
        return true;
    }

    public void add(Duration duration){
        /**
         * Extracted from
         * http://www.w3.org/TR/xmlschema-2/#adding-durations-to-dateTimes
         * to ensure implemented properly. See spec for definitions of methods
         * used in algorithm.
         *
         * Given a dateTime S and a duration D, specifies how to compute a
         * dateTime E where E is the end of the time period with start S and
         * duration D i.e. E = S + D.
         *
         * The following is the precise specification.
         * These steps must be followed in the same order.
         * If a field in D is not specified, it is treated as if it were zero.
         * If a field in S is not specified, it is treated in the calculation
         * as if it were the minimum allowed value in that field, however,
         * after the calculation is concluded, the corresponding field in
         * E is removed (set to unspecified).
         *
         * Months (may be modified additionally below)
         *  temp := S[month] + D[month]
         *  E[month] := modulo(temp, 1, 13)
         *  carry := fQuotient(temp, 1, 13)
         */
        boolean fieldUndefined[]={
                false,
                false,
                false,
                false,
                false,
                false
        };
        int signum=duration.getSign();
        int startMonth=getMonth();
        if(startMonth==DatatypeConstants.FIELD_UNDEFINED){
            startMonth=DatatypeConstants.JANUARY;
            fieldUndefined[MONTH]=true;
        }
        BigInteger dMonths=sanitize(duration.getField(DatatypeConstants.MONTHS),signum);
        BigInteger temp=BigInteger.valueOf((long)startMonth).add(dMonths);
        setMonth(temp.subtract(BigInteger.ONE).mod(TWELVE).intValue()+1);
        BigInteger carry=
                new BigDecimal(temp.subtract(BigInteger.ONE)).divide(new BigDecimal(TWELVE),BigDecimal.ROUND_FLOOR).toBigInteger();
        /** Years (may be modified additionally below)
         *  E[year] := S[year] + D[year] + carry
         */
        BigInteger startYear=getEonAndYear();
        if(startYear==null){
            fieldUndefined[YEAR]=true;
            startYear=BigInteger.ZERO;
        }
        BigInteger dYears=sanitize(duration.getField(DatatypeConstants.YEARS),signum);
        BigInteger endYear=startYear.add(dYears).add(carry);
        setYear(endYear);
        /** Zone
         *  E[zone] := S[zone]
         *
         * no-op since adding to this, not to a new end point.
         */
        /** Seconds
         *  temp := S[second] + D[second]
         *  E[second] := modulo(temp, 60)
         *  carry := fQuotient(temp, 60)
         */
        BigDecimal startSeconds;
        if(getSecond()==DatatypeConstants.FIELD_UNDEFINED){
            fieldUndefined[SECOND]=true;
            startSeconds=DECIMAL_ZERO;
        }else{
            // seconds + fractionalSeconds
            startSeconds=getSeconds();
        }
        // Duration seconds is SECONDS + FRACTIONALSECONDS.
        BigDecimal dSeconds=DurationImpl.sanitize((BigDecimal)duration.getField(DatatypeConstants.SECONDS),signum);
        BigDecimal tempBD=startSeconds.add(dSeconds);
        BigDecimal fQuotient=
                new BigDecimal(new BigDecimal(tempBD.toBigInteger()).divide(DECIMAL_SIXTY,BigDecimal.ROUND_FLOOR).toBigInteger());
        BigDecimal endSeconds=tempBD.subtract(fQuotient.multiply(DECIMAL_SIXTY));
        carry=fQuotient.toBigInteger();
        setSecond(endSeconds.intValue());
        BigDecimal tempFracSeconds=endSeconds.subtract(new BigDecimal(BigInteger.valueOf((long)getSecond())));
        if(tempFracSeconds.compareTo(DECIMAL_ZERO)<0){
            setFractionalSecond(DECIMAL_ONE.add(tempFracSeconds));
            if(getSecond()==0){
                setSecond(59);
                carry=carry.subtract(BigInteger.ONE);
            }else{
                setSecond(getSecond()-1);
            }
        }else{
            setFractionalSecond(tempFracSeconds);
        }
        /** Minutes
         *  temp := S[minute] + D[minute] + carry
         *  E[minute] := modulo(temp, 60)
         *  carry := fQuotient(temp, 60)
         */
        int startMinutes=getMinute();
        if(startMinutes==DatatypeConstants.FIELD_UNDEFINED){
            fieldUndefined[MINUTE]=true;
            startMinutes=0;
        }
        BigInteger dMinutes=sanitize(duration.getField(DatatypeConstants.MINUTES),signum);
        temp=BigInteger.valueOf(startMinutes).add(dMinutes).add(carry);
        setMinute(temp.mod(SIXTY).intValue());
        carry=new BigDecimal(temp).divide(DECIMAL_SIXTY,BigDecimal.ROUND_FLOOR).toBigInteger();
        /** Hours
         *  temp := S[hour] + D[hour] + carry
         *  E[hour] := modulo(temp, 24)
         *  carry := fQuotient(temp, 24)
         */
        int startHours=getHour();
        if(startHours==DatatypeConstants.FIELD_UNDEFINED){
            fieldUndefined[HOUR]=true;
            startHours=0;
        }
        BigInteger dHours=sanitize(duration.getField(DatatypeConstants.HOURS),signum);
        temp=BigInteger.valueOf(startHours).add(dHours).add(carry);
        setHour(temp.mod(TWENTY_FOUR).intValue(),false);
        carry=new BigDecimal(temp).divide(new BigDecimal(TWENTY_FOUR),BigDecimal.ROUND_FLOOR).toBigInteger();
        /** Days
         *  if S[day] > maximumDayInMonthFor(E[year], E[month])
         *       + tempDays := maximumDayInMonthFor(E[year], E[month])
         *  else if S[day] < 1
         *       + tempDays := 1
         *  else
         *       + tempDays := S[day]
         *  E[day] := tempDays + D[day] + carry
         *  START LOOP
         *       + IF E[day] < 1
         *             # E[day] := E[day] +
         *                 maximumDayInMonthFor(E[year], E[month] - 1)
         *             # carry := -1
         *       + ELSE IF E[day] > maximumDayInMonthFor(E[year], E[month])
         *             # E[day] :=
         *                    E[day] - maximumDayInMonthFor(E[year], E[month])
         *             # carry := 1
         *       + ELSE EXIT LOOP
         *       + temp := E[month] + carry
         *       + E[month] := modulo(temp, 1, 13)
         *       + E[year] := E[year] + fQuotient(temp, 1, 13)
         *       + GOTO START LOOP
         */
        BigInteger tempDays;
        int startDay=getDay();
        if(startDay==DatatypeConstants.FIELD_UNDEFINED){
            fieldUndefined[DAY]=true;
            startDay=1;
        }
        BigInteger dDays=sanitize(duration.getField(DatatypeConstants.DAYS),signum);
        int maxDayInMonth=maximumDayInMonthFor(getEonAndYear(),getMonth());
        if(startDay>maxDayInMonth){
            tempDays=BigInteger.valueOf(maxDayInMonth);
        }else if(startDay<1){
            tempDays=BigInteger.ONE;
        }else{
            tempDays=BigInteger.valueOf(startDay);
        }
        BigInteger endDays=tempDays.add(dDays).add(carry);
        int monthCarry;
        int intTemp;
        while(true){
            if(endDays.compareTo(BigInteger.ONE)<0){
                // calculate days in previous month, watch for month roll over
                BigInteger mdimf=null;
                if(month>=2){
                    mdimf=BigInteger.valueOf(maximumDayInMonthFor(getEonAndYear(),getMonth()-1));
                }else{
                    // roll over to December of previous year
                    mdimf=BigInteger.valueOf(maximumDayInMonthFor(getEonAndYear().subtract(BigInteger.valueOf((long)1)),12));
                }
                endDays=endDays.add(mdimf);
                monthCarry=-1;
            }else if(endDays.compareTo(BigInteger.valueOf(maximumDayInMonthFor(getEonAndYear(),getMonth())))>0){
                endDays=endDays.add(BigInteger.valueOf(-maximumDayInMonthFor(getEonAndYear(),getMonth())));
                monthCarry=1;
            }else{
                break;
            }
            intTemp=getMonth()+monthCarry;
            int endMonth=(intTemp-1)%(13-1);
            int quotient;
            if(endMonth<0){
                endMonth=(13-1)+endMonth+1;
                quotient=new BigDecimal(intTemp-1).divide(new BigDecimal(TWELVE),BigDecimal.ROUND_UP).intValue();
            }else{
                quotient=(intTemp-1)/(13-1);
                endMonth+=1;
            }
            setMonth(endMonth);
            if(quotient!=0){
                setYear(getEonAndYear().add(BigInteger.valueOf(quotient)));
            }
        }
        setDay(endDays.intValue());
        // set fields that where undefined before this addition, back to undefined.
        for(int i=YEAR;i<=SECOND;i++){
            if(fieldUndefined[i]){
                switch(i){
                    case YEAR:
                        setYear(DatatypeConstants.FIELD_UNDEFINED);
                        break;
                    case MONTH:
                        setMonth(DatatypeConstants.FIELD_UNDEFINED);
                        break;
                    case DAY:
                        setDay(DatatypeConstants.FIELD_UNDEFINED);
                        break;
                    case HOUR:
                        setHour(DatatypeConstants.FIELD_UNDEFINED,false);
                        break;
                    case MINUTE:
                        setMinute(DatatypeConstants.FIELD_UNDEFINED);
                        break;
                    case SECOND:
                        setSecond(DatatypeConstants.FIELD_UNDEFINED);
                        setFractionalSecond(null);
                        break;
                }
            }
        }
    }












    private static int maximumDayInMonthFor(BigInteger year,int month){
        if(month!=DatatypeConstants.FEBRUARY){
            return daysInMonth[month];
        }else{
            if(year.mod(FOUR_HUNDRED).equals(BigInteger.ZERO)||
                    (!year.mod(HUNDRED).equals(BigInteger.ZERO)&&
                            year.mod(FOUR).equals(BigInteger.ZERO))){
                // is a leap year.
                return 29;
            }else{
                return daysInMonth[month];
            }
        }
    }

    private static int maximumDayInMonthFor(int year,int month){
        if(month!=DatatypeConstants.FEBRUARY){
            return daysInMonth[month];
        }else{
            if(((year%400)==0)||
                    (((year%100)!=0)&&((year%4)==0))){
                // is a leap year.
                return 29;
            }else{
                return daysInMonth[DatatypeConstants.FEBRUARY];
            }
        }
    }

    public GregorianCalendar toGregorianCalendar(){
        GregorianCalendar result=null;
        final int DEFAULT_TIMEZONE_OFFSET=DatatypeConstants.FIELD_UNDEFINED;
        TimeZone tz=getTimeZone(DEFAULT_TIMEZONE_OFFSET);
        /** Use the following instead for JDK7 only:
         * Locale locale = Locale.getDefault(Locale.Category.FORMAT);
         */
        Locale locale=getDefaultLocale();
        result=new GregorianCalendar(tz,locale);
        result.clear();
        result.setGregorianChange(PURE_GREGORIAN_CHANGE);
        // if year( and eon) are undefined, leave default Calendar values
        BigInteger year=getEonAndYear();
        if(year!=null){
            result.set(Calendar.ERA,year.signum()==-1?GregorianCalendar.BC:GregorianCalendar.AD);
            result.set(Calendar.YEAR,year.abs().intValue());
        }
        // only set month if it is set
        if(month!=DatatypeConstants.FIELD_UNDEFINED){
            // Calendar.MONTH is zero based while XMLGregorianCalendar month field is not.
            result.set(Calendar.MONTH,month-1);
        }
        // only set day if it is set
        if(day!=DatatypeConstants.FIELD_UNDEFINED){
            result.set(Calendar.DAY_OF_MONTH,day);
        }
        // only set hour if it is set
        if(hour!=DatatypeConstants.FIELD_UNDEFINED){
            result.set(Calendar.HOUR_OF_DAY,hour);
        }
        // only set minute if it is set
        if(minute!=DatatypeConstants.FIELD_UNDEFINED){
            result.set(Calendar.MINUTE,minute);
        }
        // only set second if it is set
        if(second!=DatatypeConstants.FIELD_UNDEFINED){
            result.set(Calendar.SECOND,second);
        }
        // only set millisend if it is set
        if(fractionalSecond!=null){
            result.set(Calendar.MILLISECOND,getMillisecond());
        }
        return result;
    }

    private Locale getDefaultLocale(){
        String lang=SecuritySupport.getSystemProperty("user.language.format");
        String country=SecuritySupport.getSystemProperty("user.country.format");
        String variant=SecuritySupport.getSystemProperty("user.variant.format");
        Locale locale=null;
        if(lang!=null){
            if(country!=null){
                if(variant!=null){
                    locale=new Locale(lang,country,variant);
                }else{
                    locale=new Locale(lang,country);
                }
            }else{
                locale=new Locale(lang);
            }
        }
        if(locale==null){
            locale=Locale.getDefault();
        }
        return locale;
    }

    public GregorianCalendar toGregorianCalendar(TimeZone timezone,
                                                 Locale aLocale,
                                                 XMLGregorianCalendar defaults){
        GregorianCalendar result=null;
        TimeZone tz=timezone;
        if(tz==null){
            int defaultZoneoffset=DatatypeConstants.FIELD_UNDEFINED;
            if(defaults!=null){
                defaultZoneoffset=defaults.getTimezone();
            }
            tz=getTimeZone(defaultZoneoffset);
        }
        if(aLocale==null){
            aLocale=Locale.getDefault();
        }
        result=new GregorianCalendar(tz,aLocale);
        result.clear();
        result.setGregorianChange(PURE_GREGORIAN_CHANGE);
        // if year( and eon) are undefined, leave default Calendar values
        BigInteger year=getEonAndYear();
        if(year!=null){
            result.set(Calendar.ERA,year.signum()==-1?GregorianCalendar.BC:GregorianCalendar.AD);
            result.set(Calendar.YEAR,year.abs().intValue());
        }else{
            // use default if set
            BigInteger defaultYear=(defaults!=null)?defaults.getEonAndYear():null;
            if(defaultYear!=null){
                result.set(Calendar.ERA,defaultYear.signum()==-1?GregorianCalendar.BC:GregorianCalendar.AD);
                result.set(Calendar.YEAR,defaultYear.abs().intValue());
            }
        }
        // only set month if it is set
        if(month!=DatatypeConstants.FIELD_UNDEFINED){
            // Calendar.MONTH is zero based while XMLGregorianCalendar month field is not.
            result.set(Calendar.MONTH,month-1);
        }else{
            // use default if set
            int defaultMonth=(defaults!=null)?defaults.getMonth():DatatypeConstants.FIELD_UNDEFINED;
            if(defaultMonth!=DatatypeConstants.FIELD_UNDEFINED){
                // Calendar.MONTH is zero based while XMLGregorianCalendar month field is not.
                result.set(Calendar.MONTH,defaultMonth-1);
            }
        }
        // only set day if it is set
        if(day!=DatatypeConstants.FIELD_UNDEFINED){
            result.set(Calendar.DAY_OF_MONTH,day);
        }else{
            // use default if set
            int defaultDay=(defaults!=null)?defaults.getDay():DatatypeConstants.FIELD_UNDEFINED;
            if(defaultDay!=DatatypeConstants.FIELD_UNDEFINED){
                result.set(Calendar.DAY_OF_MONTH,defaultDay);
            }
        }
        // only set hour if it is set
        if(hour!=DatatypeConstants.FIELD_UNDEFINED){
            result.set(Calendar.HOUR_OF_DAY,hour);
        }else{
            // use default if set
            int defaultHour=(defaults!=null)?defaults.getHour():DatatypeConstants.FIELD_UNDEFINED;
            if(defaultHour!=DatatypeConstants.FIELD_UNDEFINED){
                result.set(Calendar.HOUR_OF_DAY,defaultHour);
            }
        }
        // only set minute if it is set
        if(minute!=DatatypeConstants.FIELD_UNDEFINED){
            result.set(Calendar.MINUTE,minute);
        }else{
            // use default if set
            int defaultMinute=(defaults!=null)?defaults.getMinute():DatatypeConstants.FIELD_UNDEFINED;
            if(defaultMinute!=DatatypeConstants.FIELD_UNDEFINED){
                result.set(Calendar.MINUTE,defaultMinute);
            }
        }
        // only set second if it is set
        if(second!=DatatypeConstants.FIELD_UNDEFINED){
            result.set(Calendar.SECOND,second);
        }else{
            // use default if set
            int defaultSecond=(defaults!=null)?defaults.getSecond():DatatypeConstants.FIELD_UNDEFINED;
            if(defaultSecond!=DatatypeConstants.FIELD_UNDEFINED){
                result.set(Calendar.SECOND,defaultSecond);
            }
        }
        // only set millisend if it is set
        if(fractionalSecond!=null){
            result.set(Calendar.MILLISECOND,getMillisecond());
        }else{
            // use default if set
            BigDecimal defaultFractionalSecond=(defaults!=null)?defaults.getFractionalSecond():null;
            if(defaultFractionalSecond!=null){
                result.set(Calendar.MILLISECOND,defaults.getMillisecond());
            }
        }
        return result;
    }

    public TimeZone getTimeZone(int defaultZoneoffset){
        TimeZone result=null;
        int zoneoffset=getTimezone();
        if(zoneoffset==DatatypeConstants.FIELD_UNDEFINED){
            zoneoffset=defaultZoneoffset;
        }
        if(zoneoffset==DatatypeConstants.FIELD_UNDEFINED){
            result=TimeZone.getDefault();
        }else{
            // zoneoffset is in minutes. Convert to custom timezone id format.
            char sign=zoneoffset<0?'-':'+';
            if(sign=='-'){
                zoneoffset=-zoneoffset;
            }
            int hour=zoneoffset/60;
            int minutes=zoneoffset-(hour*60);
            // Javadoc for java.util.TimeZone documents max length
            // for customTimezoneId is 8 when optional ':' is not used.
            // Format is
            // "GMT" ('-'|''+') (digit digit?) (digit digit)?
            //                   hour          minutes
            StringBuffer customTimezoneId=new StringBuffer(8);
            customTimezoneId.append("GMT");
            customTimezoneId.append(sign);
            customTimezoneId.append(hour);
            if(minutes!=0){
                customTimezoneId.append(minutes);
            }
            result=TimeZone.getTimeZone(customTimezoneId.toString());
        }
        return result;
    }

    public Object clone(){
        // Both this.eon and this.fractionalSecond are instances
        // of immutable classes, so they do not need to be cloned.
        return new XMLGregorianCalendarImpl(getEonAndYear(),
                this.month,this.day,
                this.hour,this.minute,this.second,
                this.fractionalSecond,
                this.timezone);
    }



    public void setMillisecond(int millisecond){
        if(millisecond==DatatypeConstants.FIELD_UNDEFINED){
            fractionalSecond=null;
        }else{
            if(millisecond<0||999<millisecond)
                if(millisecond!=DatatypeConstants.FIELD_UNDEFINED)
                    invalidFieldValue(MILLISECOND,millisecond);
            fractionalSecond=new BigDecimal((long)millisecond).movePointLeft(3);
        }
    }

    public void setFractionalSecond(BigDecimal fractional){
        if(fractional!=null){
            if((fractional.compareTo(DECIMAL_ZERO)<0)||
                    (fractional.compareTo(DECIMAL_ONE)>0)){
                throw new IllegalArgumentException(DatatypeMessageFormatter.formatMessage(null,
                        "InvalidFractional",new Object[]{fractional}));
            }
        }
        this.fractionalSecond=fractional;
    }





    private String format(String format){
        char[] buf=new char[32];
        int bufPtr=0;
        int fidx=0, flen=format.length();
        while(fidx<flen){
            char fch=format.charAt(fidx++);
            if(fch!='%'){// not a meta char
                buf[bufPtr++]=fch;
                continue;
            }
            switch(format.charAt(fidx++)){
                case 'Y':
                    if(eon==null){
                        // optimized path
                        int y=getYear();
                        if(y<0){
                            buf[bufPtr++]='-';
                            y=-y;
                        }
                        bufPtr=print4Number(buf,bufPtr,y);
                    }else{
                        String s=getEonAndYear().toString();
                        // reallocate the buffer now so that it has enough space
                        char[] n=new char[buf.length+s.length()];
                        System.arraycopy(buf,0,n,0,bufPtr);
                        buf=n;
                        for(int i=s.length();i<4;i++)
                            buf[bufPtr++]='0';
                        s.getChars(0,s.length(),buf,bufPtr);
                        bufPtr+=s.length();
                    }
                    break;
                case 'M':
                    bufPtr=print2Number(buf,bufPtr,getMonth());
                    break;
                case 'D':
                    bufPtr=print2Number(buf,bufPtr,getDay());
                    break;
                case 'h':
                    bufPtr=print2Number(buf,bufPtr,getHour());
                    break;
                case 'm':
                    bufPtr=print2Number(buf,bufPtr,getMinute());
                    break;
                case 's':
                    bufPtr=print2Number(buf,bufPtr,getSecond());
                    if(getFractionalSecond()!=null){
                        // Note: toPlainString() isn't available before Java 1.5
                        String frac=getFractionalSecond().toString();
                        int pos=frac.indexOf("E-");
                        if(pos>=0){
                            String zeros=frac.substring(pos+2);
                            frac=frac.substring(0,pos);
                            pos=frac.indexOf(".");
                            if(pos>=0){
                                frac=frac.substring(0,pos)+frac.substring(pos+1);
                            }
                            int count=Integer.parseInt(zeros);
                            if(count<40){
                                frac="00000000000000000000000000000000000000000".substring(0,count-1)+frac;
                            }else{
                                // do it the hard way
                                while(count>1){
                                    frac="0"+frac;
                                    count--;
                                }
                            }
                            frac="0."+frac;
                        }
                        // reallocate the buffer now so that it has enough space
                        char[] n=new char[buf.length+frac.length()];
                        System.arraycopy(buf,0,n,0,bufPtr);
                        buf=n;
                        //skip leading zero.
                        frac.getChars(1,frac.length(),buf,bufPtr);
                        bufPtr+=frac.length()-1;
                    }
                    break;
                case 'z':
                    int offset=getTimezone();
                    if(offset==0){
                        buf[bufPtr++]='Z';
                    }else if(offset!=DatatypeConstants.FIELD_UNDEFINED){
                        if(offset<0){
                            buf[bufPtr++]='-';
                            offset*=-1;
                        }else{
                            buf[bufPtr++]='+';
                        }
                        bufPtr=print2Number(buf,bufPtr,offset/60);
                        buf[bufPtr++]=':';
                        bufPtr=print2Number(buf,bufPtr,offset%60);
                    }
                    break;
                default:
                    throw new InternalError();  // impossible
            }
        }
        return new String(buf,0,bufPtr);
    }

    private int print2Number(char[] out,int bufptr,int number){
        out[bufptr++]=(char)('0'+(number/10));
        out[bufptr++]=(char)('0'+(number%10));
        return bufptr;
    }

    private int print4Number(char[] out,int bufptr,int number){
        out[bufptr+3]=(char)('0'+(number%10));
        number/=10;
        out[bufptr+2]=(char)('0'+(number%10));
        number/=10;
        out[bufptr+1]=(char)('0'+(number%10));
        number/=10;
        out[bufptr]=(char)('0'+(number%10));
        return bufptr+4;
    }




}
