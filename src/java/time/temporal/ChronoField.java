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
package java.time.temporal;

import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleResources;

import java.time.Year;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

import static java.time.temporal.ChronoUnit.*;

public enum ChronoField implements TemporalField{
    NANO_OF_SECOND("NanoOfSecond",NANOS,SECONDS,ValueRange.of(0,999_999_999)),
    NANO_OF_DAY("NanoOfDay",NANOS,DAYS,ValueRange.of(0,86400L*1000_000_000L-1)),
    MICRO_OF_SECOND("MicroOfSecond",MICROS,SECONDS,ValueRange.of(0,999_999)),
    MICRO_OF_DAY("MicroOfDay",MICROS,DAYS,ValueRange.of(0,86400L*1000_000L-1)),
    MILLI_OF_SECOND("MilliOfSecond",MILLIS,SECONDS,ValueRange.of(0,999)),
    MILLI_OF_DAY("MilliOfDay",MILLIS,DAYS,ValueRange.of(0,86400L*1000L-1)),
    SECOND_OF_MINUTE("SecondOfMinute",SECONDS,MINUTES,ValueRange.of(0,59),"second"),
    SECOND_OF_DAY("SecondOfDay",SECONDS,DAYS,ValueRange.of(0,86400L-1)),
    MINUTE_OF_HOUR("MinuteOfHour",MINUTES,HOURS,ValueRange.of(0,59),"minute"),
    MINUTE_OF_DAY("MinuteOfDay",MINUTES,DAYS,ValueRange.of(0,(24*60)-1)),
    HOUR_OF_AMPM("HourOfAmPm",HOURS,HALF_DAYS,ValueRange.of(0,11)),
    CLOCK_HOUR_OF_AMPM("ClockHourOfAmPm",HOURS,HALF_DAYS,ValueRange.of(1,12)),
    HOUR_OF_DAY("HourOfDay",HOURS,DAYS,ValueRange.of(0,23),"hour"),
    CLOCK_HOUR_OF_DAY("ClockHourOfDay",HOURS,DAYS,ValueRange.of(1,24)),
    AMPM_OF_DAY("AmPmOfDay",HALF_DAYS,DAYS,ValueRange.of(0,1),"dayperiod"),
    DAY_OF_WEEK("DayOfWeek",DAYS,WEEKS,ValueRange.of(1,7),"weekday"),
    ALIGNED_DAY_OF_WEEK_IN_MONTH("AlignedDayOfWeekInMonth",DAYS,WEEKS,ValueRange.of(1,7)),
    ALIGNED_DAY_OF_WEEK_IN_YEAR("AlignedDayOfWeekInYear",DAYS,WEEKS,ValueRange.of(1,7)),
    DAY_OF_MONTH("DayOfMonth",DAYS,MONTHS,ValueRange.of(1,28,31),"day"),
    DAY_OF_YEAR("DayOfYear",DAYS,YEARS,ValueRange.of(1,365,366)),
    EPOCH_DAY("EpochDay",DAYS,FOREVER,ValueRange.of((long)(Year.MIN_VALUE*365.25),(long)(Year.MAX_VALUE*365.25))),
    ALIGNED_WEEK_OF_MONTH("AlignedWeekOfMonth",WEEKS,MONTHS,ValueRange.of(1,4,5)),
    ALIGNED_WEEK_OF_YEAR("AlignedWeekOfYear",WEEKS,YEARS,ValueRange.of(1,53)),
    MONTH_OF_YEAR("MonthOfYear",MONTHS,YEARS,ValueRange.of(1,12),"month"),
    PROLEPTIC_MONTH("ProlepticMonth",MONTHS,FOREVER,ValueRange.of(Year.MIN_VALUE*12L,Year.MAX_VALUE*12L+11)),
    YEAR_OF_ERA("YearOfEra",YEARS,FOREVER,ValueRange.of(1,Year.MAX_VALUE,Year.MAX_VALUE+1)),
    YEAR("Year",YEARS,FOREVER,ValueRange.of(Year.MIN_VALUE,Year.MAX_VALUE),"year"),
    ERA("Era",ERAS,FOREVER,ValueRange.of(0,1),"era"),
    INSTANT_SECONDS("InstantSeconds",SECONDS,FOREVER,ValueRange.of(Long.MIN_VALUE,Long.MAX_VALUE)),
    OFFSET_SECONDS("OffsetSeconds",SECONDS,FOREVER,ValueRange.of(-18*3600,18*3600));
    private final String name;
    private final TemporalUnit baseUnit;
    private final TemporalUnit rangeUnit;
    private final ValueRange range;
    private final String displayNameKey;

    private ChronoField(String name,TemporalUnit baseUnit,TemporalUnit rangeUnit,ValueRange range){
        this.name=name;
        this.baseUnit=baseUnit;
        this.rangeUnit=rangeUnit;
        this.range=range;
        this.displayNameKey=null;
    }

    private ChronoField(String name,TemporalUnit baseUnit,TemporalUnit rangeUnit,
                        ValueRange range,String displayNameKey){
        this.name=name;
        this.baseUnit=baseUnit;
        this.rangeUnit=rangeUnit;
        this.range=range;
        this.displayNameKey=displayNameKey;
    }

    @Override
    public String getDisplayName(Locale locale){
        Objects.requireNonNull(locale,"locale");
        if(displayNameKey==null){
            return name;
        }
        LocaleResources lr=LocaleProviderAdapter.getResourceBundleBased()
                .getLocaleResources(locale);
        ResourceBundle rb=lr.getJavaTimeFormatData();
        String key="field."+displayNameKey;
        return rb.containsKey(key)?rb.getString(key):name;
    }

    @Override
    public TemporalUnit getBaseUnit(){
        return baseUnit;
    }

    @Override
    public TemporalUnit getRangeUnit(){
        return rangeUnit;
    }

    @Override
    public ValueRange range(){
        return range;
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isDateBased(){
        return ordinal()>=DAY_OF_WEEK.ordinal()&&ordinal()<=ERA.ordinal();
    }

    @Override
    public boolean isTimeBased(){
        return ordinal()<DAY_OF_WEEK.ordinal();
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isSupportedBy(TemporalAccessor temporal){
        return temporal.isSupported(this);
    }

    @Override
    public ValueRange rangeRefinedBy(TemporalAccessor temporal){
        return temporal.range(this);
    }

    @Override
    public long getFrom(TemporalAccessor temporal){
        return temporal.getLong(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends Temporal> R adjustInto(R temporal,long newValue){
        return (R)temporal.with(this,newValue);
    }

    //-----------------------------------------------------------------------
    public long checkValidValue(long value){
        return range().checkValidValue(value,this);
    }

    public int checkValidIntValue(long value){
        return range().checkValidIntValue(value,this);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString(){
        return name;
    }
}
