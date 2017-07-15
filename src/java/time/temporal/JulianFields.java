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
package java.time.temporal;

import java.time.DateTimeException;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.format.ResolverStyle;
import java.util.Map;

import static java.time.temporal.ChronoField.EPOCH_DAY;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.FOREVER;

public final class JulianFields{
    public static final TemporalField JULIAN_DAY=Field.JULIAN_DAY;
    public static final TemporalField MODIFIED_JULIAN_DAY=Field.MODIFIED_JULIAN_DAY;
    public static final TemporalField RATA_DIE=Field.RATA_DIE;
    private static final long JULIAN_DAY_OFFSET=2440588L;

    private JulianFields(){
        throw new AssertionError("Not instantiable");
    }

    private static enum Field implements TemporalField{
        JULIAN_DAY("JulianDay",DAYS,FOREVER,JULIAN_DAY_OFFSET),
        MODIFIED_JULIAN_DAY("ModifiedJulianDay",DAYS,FOREVER,40587L),
        RATA_DIE("RataDie",DAYS,FOREVER,719163L);
        private static final long serialVersionUID=-7501623920830201812L;
        private final transient String name;
        private final transient TemporalUnit baseUnit;
        private final transient TemporalUnit rangeUnit;
        private final transient ValueRange range;
        private final transient long offset;

        private Field(String name,TemporalUnit baseUnit,TemporalUnit rangeUnit,long offset){
            this.name=name;
            this.baseUnit=baseUnit;
            this.rangeUnit=rangeUnit;
            this.range=ValueRange.of(-365243219162L+offset,365241780471L+offset);
            this.offset=offset;
        }

        //-----------------------------------------------------------------------
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

        @Override
        public boolean isDateBased(){
            return true;
        }

        @Override
        public boolean isTimeBased(){
            return false;
        }

        //-----------------------------------------------------------------------
        @Override
        public boolean isSupportedBy(TemporalAccessor temporal){
            return temporal.isSupported(EPOCH_DAY);
        }

        @Override
        public ValueRange rangeRefinedBy(TemporalAccessor temporal){
            if(isSupportedBy(temporal)==false){
                throw new DateTimeException("Unsupported field: "+this);
            }
            return range();
        }

        @Override
        public long getFrom(TemporalAccessor temporal){
            return temporal.getLong(EPOCH_DAY)+offset;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <R extends Temporal> R adjustInto(R temporal,long newValue){
            if(range().isValidValue(newValue)==false){
                throw new DateTimeException("Invalid value: "+name+" "+newValue);
            }
            return (R)temporal.with(EPOCH_DAY,Math.subtractExact(newValue,offset));
        }

        //-----------------------------------------------------------------------
        @Override
        public ChronoLocalDate resolve(
                Map<TemporalField,Long> fieldValues,TemporalAccessor partialTemporal,ResolverStyle resolverStyle){
            long value=fieldValues.remove(this);
            Chronology chrono=Chronology.from(partialTemporal);
            if(resolverStyle==ResolverStyle.LENIENT){
                return chrono.dateEpochDay(Math.subtractExact(value,offset));
            }
            range().checkValidValue(value,this);
            return chrono.dateEpochDay(value-offset);
        }

        //-----------------------------------------------------------------------
        @Override
        public String toString(){
            return name;
        }
    }
}
