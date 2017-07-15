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
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Copyright (c) 2007-2012, Stephen Colebourne & Michael Nascimento Santos
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
/**
 *
 *
 *
 *
 *
 * Copyright (c) 2007-2012, Stephen Colebourne & Michael Nascimento Santos
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
package java.time;

import java.io.*;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Era;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.util.Objects;

import static java.time.LocalTime.SECONDS_PER_DAY;
import static java.time.temporal.ChronoField.*;

public final class LocalDate
        implements Temporal, TemporalAdjuster, ChronoLocalDate, Serializable{
    public static final LocalDate MIN=LocalDate.of(Year.MIN_VALUE,1,1);
    public static final LocalDate MAX=LocalDate.of(Year.MAX_VALUE,12,31);
    private static final long serialVersionUID=2942565459149668126L;
    private static final int DAYS_PER_CYCLE=146097;
    static final long DAYS_0000_TO_1970=(DAYS_PER_CYCLE*5L)-(30L*365L+7L);
    private final int year;
    private final short month;
    private final short day;

    private LocalDate(int year,int month,int dayOfMonth){
        this.year=year;
        this.month=(short)month;
        this.day=(short)dayOfMonth;
    }

    //-----------------------------------------------------------------------
    public static LocalDate now(){
        return now(Clock.systemDefaultZone());
    }

    public static LocalDate now(Clock clock){
        Objects.requireNonNull(clock,"clock");
        // inline to avoid creating object and Instant checks
        final Instant now=clock.instant();  // called once
        ZoneOffset offset=clock.getZone().getRules().getOffset(now);
        long epochSec=now.getEpochSecond()+offset.getTotalSeconds();  // overflow caught later
        long epochDay=Math.floorDiv(epochSec,SECONDS_PER_DAY);
        return LocalDate.ofEpochDay(epochDay);
    }

    //-----------------------------------------------------------------------
    public static LocalDate ofEpochDay(long epochDay){
        long zeroDay=epochDay+DAYS_0000_TO_1970;
        // find the march-based year
        zeroDay-=60;  // adjust to 0000-03-01 so leap day is at end of four year cycle
        long adjust=0;
        if(zeroDay<0){
            // adjust negative years to positive for calculation
            long adjustCycles=(zeroDay+1)/DAYS_PER_CYCLE-1;
            adjust=adjustCycles*400;
            zeroDay+=-adjustCycles*DAYS_PER_CYCLE;
        }
        long yearEst=(400*zeroDay+591)/DAYS_PER_CYCLE;
        long doyEst=zeroDay-(365*yearEst+yearEst/4-yearEst/100+yearEst/400);
        if(doyEst<0){
            // fix estimate
            yearEst--;
            doyEst=zeroDay-(365*yearEst+yearEst/4-yearEst/100+yearEst/400);
        }
        yearEst+=adjust;  // reset any negative year
        int marchDoy0=(int)doyEst;
        // convert march-based values back to january-based
        int marchMonth0=(marchDoy0*5+2)/153;
        int month=(marchMonth0+2)%12+1;
        int dom=marchDoy0-(marchMonth0*306+5)/10+1;
        yearEst+=marchMonth0/10;
        // check year now we are certain it is correct
        int year=YEAR.checkValidIntValue(yearEst);
        return new LocalDate(year,month,dom);
    }

    public static LocalDate now(ZoneId zone){
        return now(Clock.system(zone));
    }

    //-----------------------------------------------------------------------
    public static LocalDate of(int year,Month month,int dayOfMonth){
        YEAR.checkValidValue(year);
        Objects.requireNonNull(month,"month");
        DAY_OF_MONTH.checkValidValue(dayOfMonth);
        return create(year,month.getValue(),dayOfMonth);
    }

    //-----------------------------------------------------------------------
    private static LocalDate create(int year,int month,int dayOfMonth){
        if(dayOfMonth>28){
            int dom=31;
            switch(month){
                case 2:
                    dom=(IsoChronology.INSTANCE.isLeapYear(year)?29:28);
                    break;
                case 4:
                case 6:
                case 9:
                case 11:
                    dom=30;
                    break;
            }
            if(dayOfMonth>dom){
                if(dayOfMonth==29){
                    throw new DateTimeException("Invalid date 'February 29' as '"+year+"' is not a leap year");
                }else{
                    throw new DateTimeException("Invalid date '"+Month.of(month).name()+" "+dayOfMonth+"'");
                }
            }
        }
        return new LocalDate(year,month,dayOfMonth);
    }

    //-----------------------------------------------------------------------
    public static LocalDate ofYearDay(int year,int dayOfYear){
        YEAR.checkValidValue(year);
        DAY_OF_YEAR.checkValidValue(dayOfYear);
        boolean leap=IsoChronology.INSTANCE.isLeapYear(year);
        if(dayOfYear==366&&leap==false){
            throw new DateTimeException("Invalid date 'DayOfYear 366' as '"+year+"' is not a leap year");
        }
        Month moy=Month.of((dayOfYear-1)/31+1);
        int monthEnd=moy.firstDayOfYear(leap)+moy.length(leap)-1;
        if(dayOfYear>monthEnd){
            moy=moy.plus(1);
        }
        int dom=dayOfYear-moy.firstDayOfYear(leap)+1;
        return new LocalDate(year,moy.getValue(),dom);
    }

    //-----------------------------------------------------------------------
    public static LocalDate parse(CharSequence text){
        return parse(text,DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static LocalDate parse(CharSequence text,DateTimeFormatter formatter){
        Objects.requireNonNull(formatter,"formatter");
        return formatter.parse(text,LocalDate::from);
    }

    static LocalDate readExternal(DataInput in) throws IOException{
        int year=in.readInt();
        int month=in.readByte();
        int dayOfMonth=in.readByte();
        return LocalDate.of(year,month,dayOfMonth);
    }

    public static LocalDate of(int year,int month,int dayOfMonth){
        YEAR.checkValidValue(year);
        MONTH_OF_YEAR.checkValidValue(month);
        DAY_OF_MONTH.checkValidValue(dayOfMonth);
        return create(year,month,dayOfMonth);
    }

    //-----------------------------------------------------------------------
    @Override  // override for Javadoc
    public boolean isSupported(TemporalField field){
        return ChronoLocalDate.super.isSupported(field);
    }

    //-----------------------------------------------------------------------
    @Override
    public ValueRange range(TemporalField field){
        if(field instanceof ChronoField){
            ChronoField f=(ChronoField)field;
            if(f.isDateBased()){
                switch(f){
                    case DAY_OF_MONTH:
                        return ValueRange.of(1,lengthOfMonth());
                    case DAY_OF_YEAR:
                        return ValueRange.of(1,lengthOfYear());
                    case ALIGNED_WEEK_OF_MONTH:
                        return ValueRange.of(1,getMonth()==Month.FEBRUARY&&isLeapYear()==false?4:5);
                    case YEAR_OF_ERA:
                        return (getYear()<=0?ValueRange.of(1,Year.MAX_VALUE+1):ValueRange.of(1,Year.MAX_VALUE));
                }
                return field.range();
            }
            throw new UnsupportedTemporalTypeException("Unsupported field: "+field);
        }
        return field.rangeRefinedBy(this);
    }

    @Override  // override for Javadoc and performance
    public int get(TemporalField field){
        if(field instanceof ChronoField){
            return get0(field);
        }
        return ChronoLocalDate.super.get(field);
    }

    @Override
    public long getLong(TemporalField field){
        if(field instanceof ChronoField){
            if(field==EPOCH_DAY){
                return toEpochDay();
            }
            if(field==PROLEPTIC_MONTH){
                return getProlepticMonth();
            }
            return get0(field);
        }
        return field.getFrom(this);
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public <R> R query(TemporalQuery<R> query){
        if(query==TemporalQueries.localDate()){
            return (R)this;
        }
        return ChronoLocalDate.super.query(query);
    }

    private int get0(TemporalField field){
        switch((ChronoField)field){
            case DAY_OF_WEEK:
                return getDayOfWeek().getValue();
            case ALIGNED_DAY_OF_WEEK_IN_MONTH:
                return ((day-1)%7)+1;
            case ALIGNED_DAY_OF_WEEK_IN_YEAR:
                return ((getDayOfYear()-1)%7)+1;
            case DAY_OF_MONTH:
                return day;
            case DAY_OF_YEAR:
                return getDayOfYear();
            case EPOCH_DAY:
                throw new UnsupportedTemporalTypeException("Invalid field 'EpochDay' for get() method, use getLong() instead");
            case ALIGNED_WEEK_OF_MONTH:
                return ((day-1)/7)+1;
            case ALIGNED_WEEK_OF_YEAR:
                return ((getDayOfYear()-1)/7)+1;
            case MONTH_OF_YEAR:
                return month;
            case PROLEPTIC_MONTH:
                throw new UnsupportedTemporalTypeException("Invalid field 'ProlepticMonth' for get() method, use getLong() instead");
            case YEAR_OF_ERA:
                return (year>=1?year:1-year);
            case YEAR:
                return year;
            case ERA:
                return (year>=1?1:0);
        }
        throw new UnsupportedTemporalTypeException("Unsupported field: "+field);
    }

    public int getDayOfYear(){
        return getMonth().firstDayOfYear(isLeapYear())+day-1;
    }

    public DayOfWeek getDayOfWeek(){
        int dow0=(int)Math.floorMod(toEpochDay()+3,7);
        return DayOfWeek.of(dow0+1);
    }

    public int getYear(){
        return year;
    }

    public Month getMonth(){
        return Month.of(month);
    }

    @Override  // override for Javadoc
    public boolean isSupported(TemporalUnit unit){
        return ChronoLocalDate.super.isSupported(unit);
    }

    //-----------------------------------------------------------------------
    @Override
    public LocalDate with(TemporalAdjuster adjuster){
        // optimizations
        if(adjuster instanceof LocalDate){
            return (LocalDate)adjuster;
        }
        return (LocalDate)adjuster.adjustInto(this);
    }

    @Override
    public LocalDate with(TemporalField field,long newValue){
        if(field instanceof ChronoField){
            ChronoField f=(ChronoField)field;
            f.checkValidValue(newValue);
            switch(f){
                case DAY_OF_WEEK:
                    return plusDays(newValue-getDayOfWeek().getValue());
                case ALIGNED_DAY_OF_WEEK_IN_MONTH:
                    return plusDays(newValue-getLong(ALIGNED_DAY_OF_WEEK_IN_MONTH));
                case ALIGNED_DAY_OF_WEEK_IN_YEAR:
                    return plusDays(newValue-getLong(ALIGNED_DAY_OF_WEEK_IN_YEAR));
                case DAY_OF_MONTH:
                    return withDayOfMonth((int)newValue);
                case DAY_OF_YEAR:
                    return withDayOfYear((int)newValue);
                case EPOCH_DAY:
                    return LocalDate.ofEpochDay(newValue);
                case ALIGNED_WEEK_OF_MONTH:
                    return plusWeeks(newValue-getLong(ALIGNED_WEEK_OF_MONTH));
                case ALIGNED_WEEK_OF_YEAR:
                    return plusWeeks(newValue-getLong(ALIGNED_WEEK_OF_YEAR));
                case MONTH_OF_YEAR:
                    return withMonth((int)newValue);
                case PROLEPTIC_MONTH:
                    return plusMonths(newValue-getProlepticMonth());
                case YEAR_OF_ERA:
                    return withYear((int)(year>=1?newValue:1-newValue));
                case YEAR:
                    return withYear((int)newValue);
                case ERA:
                    return (getLong(ERA)==newValue?this:withYear(1-year));
            }
            throw new UnsupportedTemporalTypeException("Unsupported field: "+field);
        }
        return field.adjustInto(this,newValue);
    }

    //-----------------------------------------------------------------------
    @Override
    public LocalDate plus(TemporalAmount amountToAdd){
        if(amountToAdd instanceof Period){
            Period periodToAdd=(Period)amountToAdd;
            return plusMonths(periodToAdd.toTotalMonths()).plusDays(periodToAdd.getDays());
        }
        Objects.requireNonNull(amountToAdd,"amountToAdd");
        return (LocalDate)amountToAdd.addTo(this);
    }

    @Override
    public LocalDate plus(long amountToAdd,TemporalUnit unit){
        if(unit instanceof ChronoUnit){
            ChronoUnit f=(ChronoUnit)unit;
            switch(f){
                case DAYS:
                    return plusDays(amountToAdd);
                case WEEKS:
                    return plusWeeks(amountToAdd);
                case MONTHS:
                    return plusMonths(amountToAdd);
                case YEARS:
                    return plusYears(amountToAdd);
                case DECADES:
                    return plusYears(Math.multiplyExact(amountToAdd,10));
                case CENTURIES:
                    return plusYears(Math.multiplyExact(amountToAdd,100));
                case MILLENNIA:
                    return plusYears(Math.multiplyExact(amountToAdd,1000));
                case ERAS:
                    return with(ERA,Math.addExact(getLong(ERA),amountToAdd));
            }
            throw new UnsupportedTemporalTypeException("Unsupported unit: "+unit);
        }
        return unit.addTo(this,amountToAdd);
    }

    //-----------------------------------------------------------------------
    @Override
    public LocalDate minus(TemporalAmount amountToSubtract){
        if(amountToSubtract instanceof Period){
            Period periodToSubtract=(Period)amountToSubtract;
            return minusMonths(periodToSubtract.toTotalMonths()).minusDays(periodToSubtract.getDays());
        }
        Objects.requireNonNull(amountToSubtract,"amountToSubtract");
        return (LocalDate)amountToSubtract.subtractFrom(this);
    }

    @Override
    public LocalDate minus(long amountToSubtract,TemporalUnit unit){
        return (amountToSubtract==Long.MIN_VALUE?plus(Long.MAX_VALUE,unit).plus(1,unit):plus(-amountToSubtract,unit));
    }

    @Override
    public long until(Temporal endExclusive,TemporalUnit unit){
        LocalDate end=LocalDate.from(endExclusive);
        if(unit instanceof ChronoUnit){
            switch((ChronoUnit)unit){
                case DAYS:
                    return daysUntil(end);
                case WEEKS:
                    return daysUntil(end)/7;
                case MONTHS:
                    return monthsUntil(end);
                case YEARS:
                    return monthsUntil(end)/12;
                case DECADES:
                    return monthsUntil(end)/120;
                case CENTURIES:
                    return monthsUntil(end)/1200;
                case MILLENNIA:
                    return monthsUntil(end)/12000;
                case ERAS:
                    return end.getLong(ERA)-getLong(ERA);
            }
            throw new UnsupportedTemporalTypeException("Unsupported unit: "+unit);
        }
        return unit.between(this,end);
    }

    public LocalDate minusMonths(long monthsToSubtract){
        return (monthsToSubtract==Long.MIN_VALUE?plusMonths(Long.MAX_VALUE).plusMonths(1):plusMonths(-monthsToSubtract));
    }

    public LocalDate plusMonths(long monthsToAdd){
        if(monthsToAdd==0){
            return this;
        }
        long monthCount=year*12L+(month-1);
        long calcMonths=monthCount+monthsToAdd;  // safe overflow
        int newYear=YEAR.checkValidIntValue(Math.floorDiv(calcMonths,12));
        int newMonth=(int)Math.floorMod(calcMonths,12)+1;
        return resolvePreviousValid(newYear,newMonth,day);
    }

    private static LocalDate resolvePreviousValid(int year,int month,int day){
        switch(month){
            case 2:
                day=Math.min(day,IsoChronology.INSTANCE.isLeapYear(year)?29:28);
                break;
            case 4:
            case 6:
            case 9:
            case 11:
                day=Math.min(day,30);
                break;
        }
        return new LocalDate(year,month,day);
    }

    //-----------------------------------------------------------------------
    @Override
    public IsoChronology getChronology(){
        return IsoChronology.INSTANCE;
    }

    @Override // override for Javadoc
    public Era getEra(){
        return ChronoLocalDate.super.getEra();
    }

    //-----------------------------------------------------------------------
    @Override // override for Javadoc and performance
    public boolean isLeapYear(){
        return IsoChronology.INSTANCE.isLeapYear(year);
    }

    @Override
    public int lengthOfMonth(){
        switch(month){
            case 2:
                return (isLeapYear()?29:28);
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
            default:
                return 31;
        }
    }

    @Override // override for Javadoc and performance
    public int lengthOfYear(){
        return (isLeapYear()?366:365);
    }

    @Override
    public Period until(ChronoLocalDate endDateExclusive){
        LocalDate end=LocalDate.from(endDateExclusive);
        long totalMonths=end.getProlepticMonth()-this.getProlepticMonth();  // safe
        int days=end.day-this.day;
        if(totalMonths>0&&days<0){
            totalMonths--;
            LocalDate calcDate=this.plusMonths(totalMonths);
            days=(int)(end.toEpochDay()-calcDate.toEpochDay());  // safe
        }else if(totalMonths<0&&days>0){
            totalMonths++;
            days-=end.lengthOfMonth();
        }
        long years=totalMonths/12;  // safe
        int months=(int)(totalMonths%12);  // safe
        return Period.of(Math.toIntExact(years),months,days);
    }

    //-----------------------------------------------------------------------
    public static LocalDate from(TemporalAccessor temporal){
        Objects.requireNonNull(temporal,"temporal");
        LocalDate date=temporal.query(TemporalQueries.localDate());
        if(date==null){
            throw new DateTimeException("Unable to obtain LocalDate from TemporalAccessor: "+
                    temporal+" of type "+temporal.getClass().getName());
        }
        return date;
    }

    private long getProlepticMonth(){
        return (year*12L+month-1);
    }

    @Override  // override for Javadoc and performance
    public String format(DateTimeFormatter formatter){
        Objects.requireNonNull(formatter,"formatter");
        return formatter.format(this);
    }

    //-----------------------------------------------------------------------
    @Override
    public LocalDateTime atTime(LocalTime time){
        return LocalDateTime.of(this,time);
    }

    //-----------------------------------------------------------------------
    @Override
    public long toEpochDay(){
        long y=year;
        long m=month;
        long total=0;
        total+=365*y;
        if(y>=0){
            total+=(y+3)/4-(y+99)/100+(y+399)/400;
        }else{
            total-=y/-4-y/-100+y/-400;
        }
        total+=((367*m-362)/12);
        total+=day-1;
        if(m>2){
            total--;
            if(isLeapYear()==false){
                total--;
            }
        }
        return total-DAYS_0000_TO_1970;
    }

    //-----------------------------------------------------------------------
    @Override  // override for Javadoc and performance
    public int compareTo(ChronoLocalDate other){
        if(other instanceof LocalDate){
            return compareTo0((LocalDate)other);
        }
        return ChronoLocalDate.super.compareTo(other);
    }

    int compareTo0(LocalDate otherDate){
        int cmp=(year-otherDate.year);
        if(cmp==0){
            cmp=(month-otherDate.month);
            if(cmp==0){
                cmp=(day-otherDate.day);
            }
        }
        return cmp;
    }

    @Override  // override for Javadoc and performance
    public boolean isAfter(ChronoLocalDate other){
        if(other instanceof LocalDate){
            return compareTo0((LocalDate)other)>0;
        }
        return ChronoLocalDate.super.isAfter(other);
    }

    @Override  // override for Javadoc and performance
    public boolean isBefore(ChronoLocalDate other){
        if(other instanceof LocalDate){
            return compareTo0((LocalDate)other)<0;
        }
        return ChronoLocalDate.super.isBefore(other);
    }

    @Override  // override for Javadoc and performance
    public boolean isEqual(ChronoLocalDate other){
        if(other instanceof LocalDate){
            return compareTo0((LocalDate)other)==0;
        }
        return ChronoLocalDate.super.isEqual(other);
    }

    public int getMonthValue(){
        return month;
    }

    public int getDayOfMonth(){
        return day;
    }

    //-----------------------------------------------------------------------
    public LocalDate withYear(int year){
        if(this.year==year){
            return this;
        }
        YEAR.checkValidValue(year);
        return resolvePreviousValid(year,month,day);
    }

    public LocalDate withMonth(int month){
        if(this.month==month){
            return this;
        }
        MONTH_OF_YEAR.checkValidValue(month);
        return resolvePreviousValid(year,month,day);
    }

    public LocalDate withDayOfMonth(int dayOfMonth){
        if(this.day==dayOfMonth){
            return this;
        }
        return of(year,month,dayOfMonth);
    }

    public LocalDate withDayOfYear(int dayOfYear){
        if(this.getDayOfYear()==dayOfYear){
            return this;
        }
        return ofYearDay(year,dayOfYear);
    }

    //-----------------------------------------------------------------------
    public LocalDate minusYears(long yearsToSubtract){
        return (yearsToSubtract==Long.MIN_VALUE?plusYears(Long.MAX_VALUE).plusYears(1):plusYears(-yearsToSubtract));
    }

    //-----------------------------------------------------------------------
    public LocalDate plusYears(long yearsToAdd){
        if(yearsToAdd==0){
            return this;
        }
        int newYear=YEAR.checkValidIntValue(year+yearsToAdd);  // safe overflow
        return resolvePreviousValid(newYear,month,day);
    }

    public LocalDate minusWeeks(long weeksToSubtract){
        return (weeksToSubtract==Long.MIN_VALUE?plusWeeks(Long.MAX_VALUE).plusWeeks(1):plusWeeks(-weeksToSubtract));
    }

    public LocalDate plusWeeks(long weeksToAdd){
        return plusDays(Math.multiplyExact(weeksToAdd,7));
    }

    public LocalDate plusDays(long daysToAdd){
        if(daysToAdd==0){
            return this;
        }
        long mjDay=Math.addExact(toEpochDay(),daysToAdd);
        return LocalDate.ofEpochDay(mjDay);
    }

    public LocalDate minusDays(long daysToSubtract){
        return (daysToSubtract==Long.MIN_VALUE?plusDays(Long.MAX_VALUE).plusDays(1):plusDays(-daysToSubtract));
    }

    @Override  // override for Javadoc
    public Temporal adjustInto(Temporal temporal){
        return ChronoLocalDate.super.adjustInto(temporal);
    }

    long daysUntil(LocalDate end){
        return end.toEpochDay()-toEpochDay();  // no overflow
    }

    private long monthsUntil(LocalDate end){
        long packed1=getProlepticMonth()*32L+getDayOfMonth();  // no overflow
        long packed2=end.getProlepticMonth()*32L+end.getDayOfMonth();  // no overflow
        return (packed2-packed1)/32;
    }

    public LocalDateTime atTime(int hour,int minute){
        return atTime(LocalTime.of(hour,minute));
    }

    public LocalDateTime atTime(int hour,int minute,int second){
        return atTime(LocalTime.of(hour,minute,second));
    }

    public LocalDateTime atTime(int hour,int minute,int second,int nanoOfSecond){
        return atTime(LocalTime.of(hour,minute,second,nanoOfSecond));
    }

    public OffsetDateTime atTime(OffsetTime time){
        return OffsetDateTime.of(LocalDateTime.of(this,time.toLocalTime()),time.getOffset());
    }

    public LocalDateTime atStartOfDay(){
        return LocalDateTime.of(this,LocalTime.MIDNIGHT);
    }

    public ZonedDateTime atStartOfDay(ZoneId zone){
        Objects.requireNonNull(zone,"zone");
        // need to handle case where there is a gap from 11:30 to 00:30
        // standard ZDT factory would result in 01:00 rather than 00:30
        LocalDateTime ldt=atTime(LocalTime.MIDNIGHT);
        if(zone instanceof ZoneOffset==false){
            ZoneRules rules=zone.getRules();
            ZoneOffsetTransition trans=rules.getTransition(ldt);
            if(trans!=null&&trans.isGap()){
                ldt=trans.getDateTimeAfter();
            }
        }
        return ZonedDateTime.of(ldt,zone);
    }

    @Override
    public int hashCode(){
        int yearValue=year;
        int monthValue=month;
        int dayValue=day;
        return (yearValue&0xFFFFF800)^((yearValue<<11)+(monthValue<<6)+(dayValue));
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj){
        if(this==obj){
            return true;
        }
        if(obj instanceof LocalDate){
            return compareTo0((LocalDate)obj)==0;
        }
        return false;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString(){
        int yearValue=year;
        int monthValue=month;
        int dayValue=day;
        int absYear=Math.abs(yearValue);
        StringBuilder buf=new StringBuilder(10);
        if(absYear<1000){
            if(yearValue<0){
                buf.append(yearValue-10000).deleteCharAt(1);
            }else{
                buf.append(yearValue+10000).deleteCharAt(0);
            }
        }else{
            if(yearValue>9999){
                buf.append('+');
            }
            buf.append(yearValue);
        }
        return buf.append(monthValue<10?"-0":"-")
                .append(monthValue)
                .append(dayValue<10?"-0":"-")
                .append(dayValue)
                .toString();
    }

    //-----------------------------------------------------------------------
    private Object writeReplace(){
        return new Ser(Ser.LOCAL_DATE_TYPE,this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException{
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(DataOutput out) throws IOException{
        out.writeInt(year);
        out.writeByte(month);
        out.writeByte(day);
    }
}
