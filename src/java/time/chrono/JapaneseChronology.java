/**
 * Copyright (c) 2012, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Copyright (c) 2012, Stephen Colebourne & Michael Nascimento Santos
 * <p>
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * <p>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * * Neither the name of JSR-310 nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/** Copyright (c) 2012, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package java.time.chrono;

import sun.util.calendar.CalendarSystem;
import sun.util.calendar.LocalGregorianCalendar;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.*;
import java.time.format.ResolverStyle;
import java.time.temporal.*;
import java.util.*;

import static java.time.temporal.ChronoField.*;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;

public final class JapaneseChronology extends AbstractChronology implements Serializable{
    public static final JapaneseChronology INSTANCE=new JapaneseChronology();
    static final LocalGregorianCalendar JCAL=
            (LocalGregorianCalendar)CalendarSystem.forName("japanese");
    // Locale for creating a JapaneseImpericalCalendar.
    static final Locale LOCALE=Locale.forLanguageTag("ja-JP-u-ca-japanese");
    private static final long serialVersionUID=459996390165777884L;

    //-----------------------------------------------------------------------
    private JapaneseChronology(){
    }

    //-----------------------------------------------------------------------
    @Override
    public String getId(){
        return "Japanese";
    }

    @Override
    public String getCalendarType(){
        return "japanese";
    }

    //-----------------------------------------------------------------------
    @Override
    public JapaneseDate date(Era era,int yearOfEra,int month,int dayOfMonth){
        if(era instanceof JapaneseEra==false){
            throw new ClassCastException("Era must be JapaneseEra");
        }
        return JapaneseDate.of((JapaneseEra)era,yearOfEra,month,dayOfMonth);
    }

    @Override
    public JapaneseDate date(int prolepticYear,int month,int dayOfMonth){
        return new JapaneseDate(LocalDate.of(prolepticYear,month,dayOfMonth));
    }

    @Override
    public JapaneseDate dateYearDay(Era era,int yearOfEra,int dayOfYear){
        return JapaneseDate.ofYearDay((JapaneseEra)era,yearOfEra,dayOfYear);
    }

    @Override
    public JapaneseDate dateYearDay(int prolepticYear,int dayOfYear){
        return new JapaneseDate(LocalDate.ofYearDay(prolepticYear,dayOfYear));
    }

    @Override  // override with covariant return type
    public JapaneseDate dateEpochDay(long epochDay){
        return new JapaneseDate(LocalDate.ofEpochDay(epochDay));
    }

    @Override
    public JapaneseDate dateNow(){
        return dateNow(Clock.systemDefaultZone());
    }

    @Override
    public JapaneseDate dateNow(ZoneId zone){
        return dateNow(Clock.system(zone));
    }

    @Override
    public JapaneseDate dateNow(Clock clock){
        return date(LocalDate.now(clock));
    }

    @Override
    public JapaneseDate date(TemporalAccessor temporal){
        if(temporal instanceof JapaneseDate){
            return (JapaneseDate)temporal;
        }
        return new JapaneseDate(LocalDate.from(temporal));
    }

    @Override
    @SuppressWarnings("unchecked")
    public ChronoLocalDateTime<JapaneseDate> localDateTime(TemporalAccessor temporal){
        return (ChronoLocalDateTime<JapaneseDate>)super.localDateTime(temporal);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ChronoZonedDateTime<JapaneseDate> zonedDateTime(TemporalAccessor temporal){
        return (ChronoZonedDateTime<JapaneseDate>)super.zonedDateTime(temporal);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ChronoZonedDateTime<JapaneseDate> zonedDateTime(Instant instant,ZoneId zone){
        return (ChronoZonedDateTime<JapaneseDate>)super.zonedDateTime(instant,zone);
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isLeapYear(long prolepticYear){
        return IsoChronology.INSTANCE.isLeapYear(prolepticYear);
    }

    @Override
    public int prolepticYear(Era era,int yearOfEra){
        if(era instanceof JapaneseEra==false){
            throw new ClassCastException("Era must be JapaneseEra");
        }
        JapaneseEra jera=(JapaneseEra)era;
        int gregorianYear=jera.getPrivateEra().getSinceDate().getYear()+yearOfEra-1;
        if(yearOfEra==1){
            return gregorianYear;
        }
        if(gregorianYear>=Year.MIN_VALUE&&gregorianYear<=Year.MAX_VALUE){
            LocalGregorianCalendar.Date jdate=JCAL.newCalendarDate(null);
            jdate.setEra(jera.getPrivateEra()).setDate(yearOfEra,1,1);
            if(JapaneseChronology.JCAL.validate(jdate)){
                return gregorianYear;
            }
        }
        throw new DateTimeException("Invalid yearOfEra value");
    }

    @Override
    public JapaneseEra eraOf(int eraValue){
        return JapaneseEra.of(eraValue);
    }

    @Override
    public List<Era> eras(){
        return Arrays.<Era>asList(JapaneseEra.values());
    }

    //-----------------------------------------------------------------------
    @Override
    public ValueRange range(ChronoField field){
        switch(field){
            case ALIGNED_DAY_OF_WEEK_IN_MONTH:
            case ALIGNED_DAY_OF_WEEK_IN_YEAR:
            case ALIGNED_WEEK_OF_MONTH:
            case ALIGNED_WEEK_OF_YEAR:
                throw new UnsupportedTemporalTypeException("Unsupported field: "+field);
            case YEAR_OF_ERA:{
                Calendar jcal=Calendar.getInstance(LOCALE);
                int startYear=getCurrentEra().getPrivateEra().getSinceDate().getYear();
                return ValueRange.of(1,jcal.getGreatestMinimum(Calendar.YEAR),
                        jcal.getLeastMaximum(Calendar.YEAR)+1, // +1 due to the different definitions
                        Year.MAX_VALUE-startYear);
            }
            case DAY_OF_YEAR:{
                Calendar jcal=Calendar.getInstance(LOCALE);
                int fieldIndex=Calendar.DAY_OF_YEAR;
                return ValueRange.of(jcal.getMinimum(fieldIndex),jcal.getGreatestMinimum(fieldIndex),
                        jcal.getLeastMaximum(fieldIndex),jcal.getMaximum(fieldIndex));
            }
            case YEAR:
                return ValueRange.of(JapaneseDate.MEIJI_6_ISODATE.getYear(),Year.MAX_VALUE);
            case ERA:
                return ValueRange.of(JapaneseEra.MEIJI.getValue(),getCurrentEra().getValue());
            default:
                return field.range();
        }
    }

    JapaneseEra getCurrentEra(){
        // Assume that the last JapaneseEra is the current one.
        JapaneseEra[] eras=JapaneseEra.values();
        return eras[eras.length-1];
    }

    //-----------------------------------------------------------------------
    @Override  // override for return type
    public JapaneseDate resolveDate(Map<TemporalField,Long> fieldValues,ResolverStyle resolverStyle){
        return (JapaneseDate)super.resolveDate(fieldValues,resolverStyle);
    }

    @Override
        // override for special Japanese behavior
    ChronoLocalDate resolveYearOfEra(Map<TemporalField,Long> fieldValues,ResolverStyle resolverStyle){
        // validate era and year-of-era
        Long eraLong=fieldValues.get(ERA);
        JapaneseEra era=null;
        if(eraLong!=null){
            era=eraOf(range(ERA).checkValidIntValue(eraLong,ERA));  // always validated
        }
        Long yoeLong=fieldValues.get(YEAR_OF_ERA);
        int yoe=0;
        if(yoeLong!=null){
            yoe=range(YEAR_OF_ERA).checkValidIntValue(yoeLong,YEAR_OF_ERA);  // always validated
        }
        // if only year-of-era and no year then invent era unless strict
        if(era==null&&yoeLong!=null&&fieldValues.containsKey(YEAR)==false&&resolverStyle!=ResolverStyle.STRICT){
            era=JapaneseEra.values()[JapaneseEra.values().length-1];
        }
        // if both present, then try to create date
        if(yoeLong!=null&&era!=null){
            if(fieldValues.containsKey(MONTH_OF_YEAR)){
                if(fieldValues.containsKey(DAY_OF_MONTH)){
                    return resolveYMD(era,yoe,fieldValues,resolverStyle);
                }
            }
            if(fieldValues.containsKey(DAY_OF_YEAR)){
                return resolveYD(era,yoe,fieldValues,resolverStyle);
            }
        }
        return null;
    }

    //-----------------------------------------------------------------------
    @Override
    Object writeReplace(){
        return super.writeReplace();
    }

    private int prolepticYearLenient(JapaneseEra era,int yearOfEra){
        return era.getPrivateEra().getSinceDate().getYear()+yearOfEra-1;
    }

    private ChronoLocalDate resolveYMD(JapaneseEra era,int yoe,Map<TemporalField,Long> fieldValues,ResolverStyle resolverStyle){
        fieldValues.remove(ERA);
        fieldValues.remove(YEAR_OF_ERA);
        if(resolverStyle==ResolverStyle.LENIENT){
            int y=prolepticYearLenient(era,yoe);
            long months=Math.subtractExact(fieldValues.remove(MONTH_OF_YEAR),1);
            long days=Math.subtractExact(fieldValues.remove(DAY_OF_MONTH),1);
            return date(y,1,1).plus(months,MONTHS).plus(days,DAYS);
        }
        int moy=range(MONTH_OF_YEAR).checkValidIntValue(fieldValues.remove(MONTH_OF_YEAR),MONTH_OF_YEAR);
        int dom=range(DAY_OF_MONTH).checkValidIntValue(fieldValues.remove(DAY_OF_MONTH),DAY_OF_MONTH);
        if(resolverStyle==ResolverStyle.SMART){  // previous valid
            if(yoe<1){
                throw new DateTimeException("Invalid YearOfEra: "+yoe);
            }
            int y=prolepticYearLenient(era,yoe);
            JapaneseDate result;
            try{
                result=date(y,moy,dom);
            }catch(DateTimeException ex){
                result=date(y,moy,1).with(TemporalAdjusters.lastDayOfMonth());
            }
            // handle the era being changed
            // only allow if the new date is in the same Jan-Dec as the era change
            // determine by ensuring either original yoe or result yoe is 1
            if(result.getEra()!=era&&result.get(YEAR_OF_ERA)>1&&yoe>1){
                throw new DateTimeException("Invalid YearOfEra for Era: "+era+" "+yoe);
            }
            return result;
        }
        return date(era,yoe,moy,dom);
    }

    private ChronoLocalDate resolveYD(JapaneseEra era,int yoe,Map<TemporalField,Long> fieldValues,ResolverStyle resolverStyle){
        fieldValues.remove(ERA);
        fieldValues.remove(YEAR_OF_ERA);
        if(resolverStyle==ResolverStyle.LENIENT){
            int y=prolepticYearLenient(era,yoe);
            long days=Math.subtractExact(fieldValues.remove(DAY_OF_YEAR),1);
            return dateYearDay(y,1).plus(days,DAYS);
        }
        int doy=range(DAY_OF_YEAR).checkValidIntValue(fieldValues.remove(DAY_OF_YEAR),DAY_OF_YEAR);
        return dateYearDay(era,yoe,doy);  // smart is same as strict
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException{
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }
}
