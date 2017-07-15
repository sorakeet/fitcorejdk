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
/**
 *
 *
 *
 *
 *
 * Copyright (c) 2012, Stephen Colebourne & Michael Nascimento Santos
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

import java.time.*;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.format.TextStyle;
import java.time.temporal.*;
import java.util.*;

public interface Chronology extends Comparable<Chronology>{
    static Chronology from(TemporalAccessor temporal){
        Objects.requireNonNull(temporal,"temporal");
        Chronology obj=temporal.query(TemporalQueries.chronology());
        return (obj!=null?obj:IsoChronology.INSTANCE);
    }

    //-----------------------------------------------------------------------
    static Chronology ofLocale(Locale locale){
        return AbstractChronology.ofLocale(locale);
    }

    //-----------------------------------------------------------------------
    static Chronology of(String id){
        return AbstractChronology.of(id);
    }

    static Set<Chronology> getAvailableChronologies(){
        return AbstractChronology.getAvailableChronologies();
    }

    //-----------------------------------------------------------------------
    String getId();

    String getCalendarType();

    //-----------------------------------------------------------------------
    default ChronoLocalDate date(Era era,int yearOfEra,int month,int dayOfMonth){
        return date(prolepticYear(era,yearOfEra),month,dayOfMonth);
    }

    ChronoLocalDate date(int prolepticYear,int month,int dayOfMonth);

    int prolepticYear(Era era,int yearOfEra);

    default ChronoLocalDate dateYearDay(Era era,int yearOfEra,int dayOfYear){
        return dateYearDay(prolepticYear(era,yearOfEra),dayOfYear);
    }

    ChronoLocalDate dateYearDay(int prolepticYear,int dayOfYear);

    ChronoLocalDate dateEpochDay(long epochDay);

    //-----------------------------------------------------------------------
    default ChronoLocalDate dateNow(){
        return dateNow(Clock.systemDefaultZone());
    }

    default ChronoLocalDate dateNow(Clock clock){
        Objects.requireNonNull(clock,"clock");
        return date(LocalDate.now(clock));
    }

    //-----------------------------------------------------------------------
    ChronoLocalDate date(TemporalAccessor temporal);

    default ChronoLocalDate dateNow(ZoneId zone){
        return dateNow(Clock.system(zone));
    }

    default ChronoZonedDateTime<? extends ChronoLocalDate> zonedDateTime(TemporalAccessor temporal){
        try{
            ZoneId zone=ZoneId.from(temporal);
            try{
                Instant instant=Instant.from(temporal);
                return zonedDateTime(instant,zone);
            }catch(DateTimeException ex1){
                ChronoLocalDateTimeImpl<?> cldt=ChronoLocalDateTimeImpl.ensureValid(this,localDateTime(temporal));
                return ChronoZonedDateTimeImpl.ofBest(cldt,zone,null);
            }
        }catch(DateTimeException ex){
            throw new DateTimeException("Unable to obtain ChronoZonedDateTime from TemporalAccessor: "+temporal.getClass(),ex);
        }
    }

    default ChronoLocalDateTime<? extends ChronoLocalDate> localDateTime(TemporalAccessor temporal){
        try{
            return date(temporal).atTime(LocalTime.from(temporal));
        }catch(DateTimeException ex){
            throw new DateTimeException("Unable to obtain ChronoLocalDateTime from TemporalAccessor: "+temporal.getClass(),ex);
        }
    }

    default ChronoZonedDateTime<? extends ChronoLocalDate> zonedDateTime(Instant instant,ZoneId zone){
        return ChronoZonedDateTimeImpl.ofInstant(this,instant,zone);
    }

    //-----------------------------------------------------------------------
    boolean isLeapYear(long prolepticYear);

    Era eraOf(int eraValue);

    List<Era> eras();

    //-----------------------------------------------------------------------
    ValueRange range(ChronoField field);

    //-----------------------------------------------------------------------
    default String getDisplayName(TextStyle style,Locale locale){
        TemporalAccessor temporal=new TemporalAccessor(){
            @Override
            public boolean isSupported(TemporalField field){
                return false;
            }

            @Override
            public long getLong(TemporalField field){
                throw new UnsupportedTemporalTypeException("Unsupported field: "+field);
            }

            @SuppressWarnings("unchecked")
            @Override
            public <R> R query(TemporalQuery<R> query){
                if(query==TemporalQueries.chronology()){
                    return (R)Chronology.this;
                }
                return TemporalAccessor.super.query(query);
            }
        };
        return new DateTimeFormatterBuilder().appendChronologyText(style).toFormatter(locale).format(temporal);
    }

    //-----------------------------------------------------------------------
    ChronoLocalDate resolveDate(Map<TemporalField,Long> fieldValues,ResolverStyle resolverStyle);

    //-----------------------------------------------------------------------
    default ChronoPeriod period(int years,int months,int days){
        return new ChronoPeriodImpl(this,years,months,days);
    }

    //-----------------------------------------------------------------------
    @Override
    int compareTo(Chronology other);

    @Override
    int hashCode();

    @Override
    boolean equals(Object obj);

    //-----------------------------------------------------------------------
    @Override
    String toString();
}
