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
package java.time.chrono;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.util.Comparator;
import java.util.Objects;

import static java.time.temporal.ChronoField.EPOCH_DAY;
import static java.time.temporal.ChronoField.NANO_OF_DAY;
import static java.time.temporal.ChronoUnit.FOREVER;
import static java.time.temporal.ChronoUnit.NANOS;

public interface ChronoLocalDateTime<D extends ChronoLocalDate>
        extends Temporal, TemporalAdjuster, Comparable<ChronoLocalDateTime<?>>{
    static Comparator<ChronoLocalDateTime<?>> timeLineOrder(){
        return AbstractChronology.DATE_TIME_ORDER;
    }

    //-----------------------------------------------------------------------
    static ChronoLocalDateTime<?> from(TemporalAccessor temporal){
        if(temporal instanceof ChronoLocalDateTime){
            return (ChronoLocalDateTime<?>)temporal;
        }
        Objects.requireNonNull(temporal,"temporal");
        Chronology chrono=temporal.query(TemporalQueries.chronology());
        if(chrono==null){
            throw new DateTimeException("Unable to obtain ChronoLocalDateTime from TemporalAccessor: "+temporal.getClass());
        }
        return chrono.localDateTime(temporal);
    }

    @Override
    boolean isSupported(TemporalField field);

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    default <R> R query(TemporalQuery<R> query){
        if(query==TemporalQueries.zoneId()||query==TemporalQueries.zone()||query==TemporalQueries.offset()){
            return null;
        }else if(query==TemporalQueries.localTime()){
            return (R)toLocalTime();
        }else if(query==TemporalQueries.chronology()){
            return (R)getChronology();
        }else if(query==TemporalQueries.precision()){
            return (R)NANOS;
        }
        // inline TemporalAccessor.super.query(query) as an optimization
        // non-JDK classes are not permitted to make this optimization
        return query.queryFrom(this);
    }

    LocalTime toLocalTime();

    //-----------------------------------------------------------------------
    default Chronology getChronology(){
        return toLocalDate().getChronology();
    }

    D toLocalDate();

    @Override
    default boolean isSupported(TemporalUnit unit){
        if(unit instanceof ChronoUnit){
            return unit!=FOREVER;
        }
        return unit!=null&&unit.isSupportedBy(this);
    }

    //-----------------------------------------------------------------------
    // override for covariant return type
    @Override
    default ChronoLocalDateTime<D> with(TemporalAdjuster adjuster){
        return ChronoLocalDateTimeImpl.ensureValid(getChronology(),Temporal.super.with(adjuster));
    }

    @Override
    ChronoLocalDateTime<D> with(TemporalField field,long newValue);

    @Override
    default ChronoLocalDateTime<D> plus(TemporalAmount amount){
        return ChronoLocalDateTimeImpl.ensureValid(getChronology(),Temporal.super.plus(amount));
    }

    @Override
    ChronoLocalDateTime<D> plus(long amountToAdd,TemporalUnit unit);

    @Override
    default ChronoLocalDateTime<D> minus(TemporalAmount amount){
        return ChronoLocalDateTimeImpl.ensureValid(getChronology(),Temporal.super.minus(amount));
    }

    @Override
    default ChronoLocalDateTime<D> minus(long amountToSubtract,TemporalUnit unit){
        return ChronoLocalDateTimeImpl.ensureValid(getChronology(),Temporal.super.minus(amountToSubtract,unit));
    }

    @Override
    default Temporal adjustInto(Temporal temporal){
        return temporal
                .with(EPOCH_DAY,toLocalDate().toEpochDay())
                .with(NANO_OF_DAY,toLocalTime().toNanoOfDay());
    }

    default String format(DateTimeFormatter formatter){
        Objects.requireNonNull(formatter,"formatter");
        return formatter.format(this);
    }

    //-----------------------------------------------------------------------
    ChronoZonedDateTime<D> atZone(ZoneId zone);

    //-----------------------------------------------------------------------
    default Instant toInstant(ZoneOffset offset){
        return Instant.ofEpochSecond(toEpochSecond(offset),toLocalTime().getNano());
    }

    default long toEpochSecond(ZoneOffset offset){
        Objects.requireNonNull(offset,"offset");
        long epochDay=toLocalDate().toEpochDay();
        long secs=epochDay*86400+toLocalTime().toSecondOfDay();
        secs-=offset.getTotalSeconds();
        return secs;
    }

    //-----------------------------------------------------------------------
    @Override
    default int compareTo(ChronoLocalDateTime<?> other){
        int cmp=toLocalDate().compareTo(other.toLocalDate());
        if(cmp==0){
            cmp=toLocalTime().compareTo(other.toLocalTime());
            if(cmp==0){
                cmp=getChronology().compareTo(other.getChronology());
            }
        }
        return cmp;
    }

    default boolean isAfter(ChronoLocalDateTime<?> other){
        long thisEpDay=this.toLocalDate().toEpochDay();
        long otherEpDay=other.toLocalDate().toEpochDay();
        return thisEpDay>otherEpDay||
                (thisEpDay==otherEpDay&&this.toLocalTime().toNanoOfDay()>other.toLocalTime().toNanoOfDay());
    }

    default boolean isBefore(ChronoLocalDateTime<?> other){
        long thisEpDay=this.toLocalDate().toEpochDay();
        long otherEpDay=other.toLocalDate().toEpochDay();
        return thisEpDay<otherEpDay||
                (thisEpDay==otherEpDay&&this.toLocalTime().toNanoOfDay()<other.toLocalTime().toNanoOfDay());
    }

    default boolean isEqual(ChronoLocalDateTime<?> other){
        // Do the time check first, it is cheaper than computing EPOCH day.
        return this.toLocalTime().toNanoOfDay()==other.toLocalTime().toNanoOfDay()&&
                this.toLocalDate().toEpochDay()==other.toLocalDate().toEpochDay();
    }

    @Override
    int hashCode();

    @Override
    boolean equals(Object obj);

    //-----------------------------------------------------------------------
    @Override
    String toString();
}
