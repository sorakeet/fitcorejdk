/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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

import sun.util.calendar.CalendarDate;
import sun.util.calendar.LocalGregorianCalendar;

import java.io.*;
import java.time.*;
import java.time.temporal.*;
import java.util.Calendar;
import java.util.Objects;

import static java.time.temporal.ChronoField.*;

public final class JapaneseDate
        extends ChronoLocalDateImpl<JapaneseDate>
        implements ChronoLocalDate, Serializable{
    static final LocalDate MEIJI_6_ISODATE=LocalDate.of(1873,1,1);
    private static final long serialVersionUID=-305327627230580483L;
    private final transient LocalDate isoDate;
    private transient JapaneseEra era;
    private transient int yearOfEra;

    //-----------------------------------------------------------------------
    JapaneseDate(LocalDate isoDate){
        if(isoDate.isBefore(MEIJI_6_ISODATE)){
            throw new DateTimeException("JapaneseDate before Meiji 6 is not supported");
        }
        LocalGregorianCalendar.Date jdate=toPrivateJapaneseDate(isoDate);
        this.era=JapaneseEra.toJapaneseEra(jdate.getEra());
        this.yearOfEra=jdate.getYear();
        this.isoDate=isoDate;
    }

    private static LocalGregorianCalendar.Date toPrivateJapaneseDate(LocalDate isoDate){
        LocalGregorianCalendar.Date jdate=JapaneseChronology.JCAL.newCalendarDate(null);
        sun.util.calendar.Era sunEra=JapaneseEra.privateEraFrom(isoDate);
        int year=isoDate.getYear();
        if(sunEra!=null){
            year-=sunEra.getSinceDate().getYear()-1;
        }
        jdate.setEra(sunEra).setYear(year).setMonth(isoDate.getMonthValue()).setDayOfMonth(isoDate.getDayOfMonth());
        JapaneseChronology.JCAL.normalize(jdate);
        return jdate;
    }

    JapaneseDate(JapaneseEra era,int year,LocalDate isoDate){
        if(isoDate.isBefore(MEIJI_6_ISODATE)){
            throw new DateTimeException("JapaneseDate before Meiji 6 is not supported");
        }
        this.era=era;
        this.yearOfEra=year;
        this.isoDate=isoDate;
    }

    //-----------------------------------------------------------------------
    public static JapaneseDate now(){
        return now(Clock.systemDefaultZone());
    }

    public static JapaneseDate now(Clock clock){
        return new JapaneseDate(LocalDate.now(clock));
    }

    public static JapaneseDate now(ZoneId zone){
        return now(Clock.system(zone));
    }

    public static JapaneseDate of(JapaneseEra era,int yearOfEra,int month,int dayOfMonth){
        Objects.requireNonNull(era,"era");
        LocalGregorianCalendar.Date jdate=JapaneseChronology.JCAL.newCalendarDate(null);
        jdate.setEra(era.getPrivateEra()).setDate(yearOfEra,month,dayOfMonth);
        if(!JapaneseChronology.JCAL.validate(jdate)){
            throw new DateTimeException("year, month, and day not valid for Era");
        }
        LocalDate date=LocalDate.of(jdate.getNormalizedYear(),month,dayOfMonth);
        return new JapaneseDate(era,yearOfEra,date);
    }

    public static JapaneseDate of(int prolepticYear,int month,int dayOfMonth){
        return new JapaneseDate(LocalDate.of(prolepticYear,month,dayOfMonth));
    }

    static JapaneseDate ofYearDay(JapaneseEra era,int yearOfEra,int dayOfYear){
        Objects.requireNonNull(era,"era");
        CalendarDate firstDay=era.getPrivateEra().getSinceDate();
        LocalGregorianCalendar.Date jdate=JapaneseChronology.JCAL.newCalendarDate(null);
        jdate.setEra(era.getPrivateEra());
        if(yearOfEra==1){
            jdate.setDate(yearOfEra,firstDay.getMonth(),firstDay.getDayOfMonth()+dayOfYear-1);
        }else{
            jdate.setDate(yearOfEra,1,dayOfYear);
        }
        JapaneseChronology.JCAL.normalize(jdate);
        if(era.getPrivateEra()!=jdate.getEra()||yearOfEra!=jdate.getYear()){
            throw new DateTimeException("Invalid parameters");
        }
        LocalDate localdate=LocalDate.of(jdate.getNormalizedYear(),
                jdate.getMonth(),jdate.getDayOfMonth());
        return new JapaneseDate(era,yearOfEra,localdate);
    }

    public static JapaneseDate from(TemporalAccessor temporal){
        return JapaneseChronology.INSTANCE.date(temporal);
    }

    static JapaneseDate readExternal(DataInput in) throws IOException{
        int year=in.readInt();
        int month=in.readByte();
        int dayOfMonth=in.readByte();
        return JapaneseChronology.INSTANCE.date(year,month,dayOfMonth);
    }

    @Override
    public ValueRange range(TemporalField field){
        if(field instanceof ChronoField){
            if(isSupported(field)){
                ChronoField f=(ChronoField)field;
                switch(f){
                    case DAY_OF_MONTH:
                        return ValueRange.of(1,lengthOfMonth());
                    case DAY_OF_YEAR:
                        return ValueRange.of(1,lengthOfYear());
                    case YEAR_OF_ERA:{
                        Calendar jcal=Calendar.getInstance(JapaneseChronology.LOCALE);
                        jcal.set(Calendar.ERA,era.getValue()+JapaneseEra.ERA_OFFSET);
                        jcal.set(yearOfEra,isoDate.getMonthValue()-1,isoDate.getDayOfMonth());
                        return ValueRange.of(1,jcal.getActualMaximum(Calendar.YEAR));
                    }
                }
                return getChronology().range(f);
            }
            throw new UnsupportedTemporalTypeException("Unsupported field: "+field);
        }
        return field.rangeRefinedBy(this);
    }

    //-----------------------------------------------------------------------
    @Override
    public JapaneseChronology getChronology(){
        return JapaneseChronology.INSTANCE;
    }

    @Override
    public JapaneseEra getEra(){
        return era;
    }

    @Override
    public int lengthOfMonth(){
        return isoDate.lengthOfMonth();
    }

    @Override
    public int lengthOfYear(){
        Calendar jcal=Calendar.getInstance(JapaneseChronology.LOCALE);
        jcal.set(Calendar.ERA,era.getValue()+JapaneseEra.ERA_OFFSET);
        jcal.set(yearOfEra,isoDate.getMonthValue()-1,isoDate.getDayOfMonth());
        return jcal.getActualMaximum(Calendar.DAY_OF_YEAR);
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isSupported(TemporalField field){
        if(field==ALIGNED_DAY_OF_WEEK_IN_MONTH||field==ALIGNED_DAY_OF_WEEK_IN_YEAR||
                field==ALIGNED_WEEK_OF_MONTH||field==ALIGNED_WEEK_OF_YEAR){
            return false;
        }
        return ChronoLocalDate.super.isSupported(field);
    }

    @Override
    public ChronoPeriod until(ChronoLocalDate endDate){
        Period period=isoDate.until(endDate);
        return getChronology().period(period.getYears(),period.getMonths(),period.getDays());
    }

    @Override        // for javadoc and covariant return type
    @SuppressWarnings("unchecked")
    public final ChronoLocalDateTime<JapaneseDate> atTime(LocalTime localTime){
        return (ChronoLocalDateTime<JapaneseDate>)super.atTime(localTime);
    }

    @Override  // override for performance
    public long toEpochDay(){
        return isoDate.toEpochDay();
    }

    @Override
    public long getLong(TemporalField field){
        if(field instanceof ChronoField){
            // same as ISO:
            // DAY_OF_WEEK, DAY_OF_MONTH, EPOCH_DAY, MONTH_OF_YEAR, PROLEPTIC_MONTH, YEAR
            //
            // calendar specific fields
            // DAY_OF_YEAR, YEAR_OF_ERA, ERA
            switch((ChronoField)field){
                case ALIGNED_DAY_OF_WEEK_IN_MONTH:
                case ALIGNED_DAY_OF_WEEK_IN_YEAR:
                case ALIGNED_WEEK_OF_MONTH:
                case ALIGNED_WEEK_OF_YEAR:
                    throw new UnsupportedTemporalTypeException("Unsupported field: "+field);
                case YEAR_OF_ERA:
                    return yearOfEra;
                case ERA:
                    return era.getValue();
                case DAY_OF_YEAR:
                    Calendar jcal=Calendar.getInstance(JapaneseChronology.LOCALE);
                    jcal.set(Calendar.ERA,era.getValue()+JapaneseEra.ERA_OFFSET);
                    jcal.set(yearOfEra,isoDate.getMonthValue()-1,isoDate.getDayOfMonth());
                    return jcal.get(Calendar.DAY_OF_YEAR);
            }
            return isoDate.getLong(field);
        }
        return field.getFrom(this);
    }

    @Override
    public JapaneseDate with(TemporalAdjuster adjuster){
        return super.with(adjuster);
    }

    //-----------------------------------------------------------------------
    @Override
    public JapaneseDate with(TemporalField field,long newValue){
        if(field instanceof ChronoField){
            ChronoField f=(ChronoField)field;
            if(getLong(f)==newValue){  // getLong() validates for supported fields
                return this;
            }
            switch(f){
                case YEAR_OF_ERA:
                case YEAR:
                case ERA:{
                    int nvalue=getChronology().range(f).checkValidIntValue(newValue,f);
                    switch(f){
                        case YEAR_OF_ERA:
                            return this.withYear(nvalue);
                        case YEAR:
                            return with(isoDate.withYear(nvalue));
                        case ERA:{
                            return this.withYear(JapaneseEra.of(nvalue),yearOfEra);
                        }
                    }
                }
            }
            // YEAR, PROLEPTIC_MONTH and others are same as ISO
            return with(isoDate.with(field,newValue));
        }
        return super.with(field,newValue);
    }

    @Override
    public JapaneseDate plus(TemporalAmount amount){
        return super.plus(amount);
    }

    @Override
    public JapaneseDate plus(long amountToAdd,TemporalUnit unit){
        return super.plus(amountToAdd,unit);
    }

    @Override
    public JapaneseDate minus(TemporalAmount amount){
        return super.minus(amount);
    }

    @Override
    public JapaneseDate minus(long amountToAdd,TemporalUnit unit){
        return super.minus(amountToAdd,unit);
    }

    //-----------------------------------------------------------------------
    @Override
    JapaneseDate plusYears(long years){
        return with(isoDate.plusYears(years));
    }

    @Override
    JapaneseDate plusMonths(long months){
        return with(isoDate.plusMonths(months));
    }

    @Override
    JapaneseDate plusWeeks(long weeksToAdd){
        return with(isoDate.plusWeeks(weeksToAdd));
    }

    @Override
    JapaneseDate plusDays(long days){
        return with(isoDate.plusDays(days));
    }

    @Override
    JapaneseDate minusYears(long yearsToSubtract){
        return super.minusYears(yearsToSubtract);
    }

    @Override
    JapaneseDate minusMonths(long monthsToSubtract){
        return super.minusMonths(monthsToSubtract);
    }

    @Override
    JapaneseDate minusWeeks(long weeksToSubtract){
        return super.minusWeeks(weeksToSubtract);
    }

    @Override
    JapaneseDate minusDays(long daysToSubtract){
        return super.minusDays(daysToSubtract);
    }

    //-------------------------------------------------------------------------
    @Override  // override for performance
    public boolean equals(Object obj){
        if(this==obj){
            return true;
        }
        if(obj instanceof JapaneseDate){
            JapaneseDate otherDate=(JapaneseDate)obj;
            return this.isoDate.equals(otherDate.isoDate);
        }
        return false;
    }

    @Override  // override for performance
    public int hashCode(){
        return getChronology().getId().hashCode()^isoDate.hashCode();
    }

    private JapaneseDate with(LocalDate newDate){
        return (newDate.equals(isoDate)?this:new JapaneseDate(newDate));
    }

    //-----------------------------------------------------------------------
    private JapaneseDate withYear(JapaneseEra era,int yearOfEra){
        int year=JapaneseChronology.INSTANCE.prolepticYear(era,yearOfEra);
        return with(isoDate.withYear(year));
    }

    private JapaneseDate withYear(int year){
        return withYear(getEra(),year);
    }

    //-----------------------------------------------------------------------
    private void readObject(ObjectInputStream s) throws InvalidObjectException{
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    private Object writeReplace(){
        return new Ser(Ser.JAPANESE_DATE_TYPE,this);
    }

    void writeExternal(DataOutput out) throws IOException{
        // JapaneseChronology is implicit in the JAPANESE_DATE_TYPE
        out.writeInt(get(YEAR));
        out.writeByte(get(MONTH_OF_YEAR));
        out.writeByte(get(DAY_OF_MONTH));
    }
}
