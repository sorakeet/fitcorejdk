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
package java.time.temporal;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.Chronology;

import static java.time.temporal.ChronoField.*;

public final class TemporalQueries{
    // note that it is vital that each method supplies a constant, not a
    // calculated value, as they will be checked for using ==
    // it is also vital that each constant is different (due to the == checking)
    // as such, alterations to this code must be done with care
    //-----------------------------------------------------------------------
    static final TemporalQuery<ZoneId> ZONE_ID=(temporal)->
            temporal.query(TemporalQueries.ZONE_ID);
    //-----------------------------------------------------------------------
    // special constants should be used to extract information from a TemporalAccessor
    // that cannot be derived in other ways
    // Javadoc added here, so as to pretend they are more normal than they really are
    static final TemporalQuery<Chronology> CHRONO=(temporal)->
            temporal.query(TemporalQueries.CHRONO);
    static final TemporalQuery<TemporalUnit> PRECISION=(temporal)->
            temporal.query(TemporalQueries.PRECISION);
    //-----------------------------------------------------------------------
    static final TemporalQuery<ZoneOffset> OFFSET=(temporal)->{
        if(temporal.isSupported(OFFSET_SECONDS)){
            return ZoneOffset.ofTotalSeconds(temporal.get(OFFSET_SECONDS));
        }
        return null;
    };
    static final TemporalQuery<ZoneId> ZONE=(temporal)->{
        ZoneId zone=temporal.query(ZONE_ID);
        return (zone!=null?zone:temporal.query(OFFSET));
    };
    static final TemporalQuery<LocalDate> LOCAL_DATE=(temporal)->{
        if(temporal.isSupported(EPOCH_DAY)){
            return LocalDate.ofEpochDay(temporal.getLong(EPOCH_DAY));
        }
        return null;
    };
    static final TemporalQuery<LocalTime> LOCAL_TIME=(temporal)->{
        if(temporal.isSupported(NANO_OF_DAY)){
            return LocalTime.ofNanoOfDay(temporal.getLong(NANO_OF_DAY));
        }
        return null;
    };

    private TemporalQueries(){
    }

    public static TemporalQuery<ZoneId> zoneId(){
        return TemporalQueries.ZONE_ID;
    }

    public static TemporalQuery<Chronology> chronology(){
        return TemporalQueries.CHRONO;
    }

    public static TemporalQuery<TemporalUnit> precision(){
        return TemporalQueries.PRECISION;
    }

    //-----------------------------------------------------------------------
    // non-special constants are standard queries that derive information from other information
    public static TemporalQuery<ZoneId> zone(){
        return TemporalQueries.ZONE;
    }

    public static TemporalQuery<ZoneOffset> offset(){
        return TemporalQueries.OFFSET;
    }

    public static TemporalQuery<LocalDate> localDate(){
        return TemporalQueries.LOCAL_DATE;
    }

    public static TemporalQuery<LocalTime> localTime(){
        return TemporalQueries.LOCAL_TIME;
    }
}
