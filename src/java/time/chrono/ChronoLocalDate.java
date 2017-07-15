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

import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.util.Comparator;
import java.util.Objects;

import static java.time.temporal.ChronoField.*;
import static java.time.temporal.ChronoUnit.DAYS;

public interface ChronoLocalDate
        extends Temporal, TemporalAdjuster, Comparable<ChronoLocalDate>{
    static Comparator<ChronoLocalDate> timeLineOrder(){
        return AbstractChronology.DATE_ORDER;
    }

    //-----------------------------------------------------------------------
    static ChronoLocalDate from(TemporalAccessor temporal){
        if(temporal instanceof ChronoLocalDate){
            return (ChronoLocalDate)temporal;
        }
        Objects.requireNonNull(temporal,"temporal");
        Chronology chrono=temporal.query(TemporalQueries.chronology());
        if(chrono==null){
            throw new DateTimeException("Unable to obtain ChronoLocalDate from TemporalAccessor: "+temporal.getClass());
        }
        return chrono.date(temporal);
    }

    default Era getEra(){
        return getChronology().eraOf(get(ERA));
    }

    //-----------------------------------------------------------------------
    Chronology getChronology();

    int lengthOfMonth();

    default int lengthOfYear(){
        return (isLeapYear()?366:365);
    }

    default boolean isLeapYear(){
        return getChronology().isLeapYear(getLong(YEAR));
    }

    @Override
    default boolean isSupported(TemporalField field){
        if(field instanceof ChronoField){
            return field.isDateBased();
        }
        return field!=null&&field.isSupportedBy(this);
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    default <R> R query(TemporalQuery<R> query){
        if(query==TemporalQueries.zoneId()||query==TemporalQueries.zone()||query==TemporalQueries.offset()){
            return null;
        }else if(query==TemporalQueries.localTime()){
            return null;
        }else if(query==TemporalQueries.chronology()){
            return (R)getChronology();
        }else if(query==TemporalQueries.precision()){
            return (R)DAYS;
        }
        // inline TemporalAccessor.super.query(query) as an optimization
        // non-JDK classes are not permitted to make this optimization
        return query.queryFrom(this);
    }

    @Override
    default boolean isSupported(TemporalUnit unit){
        if(unit instanceof ChronoUnit){
            return unit.isDateBased();
        }
        return unit!=null&&unit.isSupportedBy(this);
    }

    //-----------------------------------------------------------------------
    // override for covariant return type
    @Override
    default ChronoLocalDate with(TemporalAdjuster adjuster){
        return ChronoLocalDateImpl.ensureValid(getChronology(),Temporal.super.with(adjuster));
    }

    @Override
    default ChronoLocalDate with(TemporalField field,long newValue){
        if(field instanceof ChronoField){
            throw new UnsupportedTemporalTypeException("Unsupported field: "+field);
        }
        return ChronoLocalDateImpl.ensureValid(getChronology(),field.adjustInto(this,newValue));
    }

    @Override
    default ChronoLocalDate plus(TemporalAmount amount){
        return ChronoLocalDateImpl.ensureValid(getChronology(),Temporal.super.plus(amount));
    }

    @Override
    default ChronoLocalDate plus(long amountToAdd,TemporalUnit unit){
        if(unit instanceof ChronoUnit){
            throw new UnsupportedTemporalTypeException("Unsupported unit: "+unit);
        }
        return ChronoLocalDateImpl.ensureValid(getChronology(),unit.addTo(this,amountToAdd));
    }

    @Override
    default ChronoLocalDate minus(TemporalAmount amount){
        return ChronoLocalDateImpl.ensureValid(getChronology(),Temporal.super.minus(amount));
    }

    @Override
    default ChronoLocalDate minus(long amountToSubtract,TemporalUnit unit){
        return ChronoLocalDateImpl.ensureValid(getChronology(),Temporal.super.minus(amountToSubtract,unit));
    }

    @Override
        // override for Javadoc
    long until(Temporal endExclusive,TemporalUnit unit);

    @Override
    default Temporal adjustInto(Temporal temporal){
        return temporal.with(EPOCH_DAY,toEpochDay());
    }

    //-----------------------------------------------------------------------
    default long toEpochDay(){
        return getLong(EPOCH_DAY);
    }

    ChronoPeriod until(ChronoLocalDate endDateExclusive);

    default String format(DateTimeFormatter formatter){
        Objects.requireNonNull(formatter,"formatter");
        return formatter.format(this);
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    default ChronoLocalDateTime<?> atTime(LocalTime localTime){
        return ChronoLocalDateTimeImpl.of(this,localTime);
    }

    //-----------------------------------------------------------------------
    @Override
    default int compareTo(ChronoLocalDate other){
        int cmp=Long.compare(toEpochDay(),other.toEpochDay());
        if(cmp==0){
            cmp=getChronology().compareTo(other.getChronology());
        }
        return cmp;
    }

    default boolean isAfter(ChronoLocalDate other){
        return this.toEpochDay()>other.toEpochDay();
    }

    default boolean isBefore(ChronoLocalDate other){
        return this.toEpochDay()<other.toEpochDay();
    }

    default boolean isEqual(ChronoLocalDate other){
        return this.toEpochDay()==other.toEpochDay();
    }

    @Override
    int hashCode();

    //-----------------------------------------------------------------------
    @Override
    boolean equals(Object obj);

    //-----------------------------------------------------------------------
    @Override
    String toString();
}
