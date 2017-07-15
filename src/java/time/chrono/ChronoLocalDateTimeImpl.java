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
package java.time.chrono;

import java.io.*;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.*;
import java.util.Objects;

import static java.time.temporal.ChronoField.EPOCH_DAY;

final class ChronoLocalDateTimeImpl<D extends ChronoLocalDate>
        implements ChronoLocalDateTime<D>, Temporal, TemporalAdjuster, Serializable{
    static final int HOURS_PER_DAY=24;
    static final int MINUTES_PER_HOUR=60;
    static final int MINUTES_PER_DAY=MINUTES_PER_HOUR*HOURS_PER_DAY;
    static final int SECONDS_PER_MINUTE=60;
    static final int SECONDS_PER_HOUR=SECONDS_PER_MINUTE*MINUTES_PER_HOUR;
    static final int SECONDS_PER_DAY=SECONDS_PER_HOUR*HOURS_PER_DAY;
    static final long MILLIS_PER_DAY=SECONDS_PER_DAY*1000L;
    static final long MICROS_PER_DAY=SECONDS_PER_DAY*1000_000L;
    static final long NANOS_PER_SECOND=1000_000_000L;
    static final long NANOS_PER_MINUTE=NANOS_PER_SECOND*SECONDS_PER_MINUTE;
    static final long NANOS_PER_HOUR=NANOS_PER_MINUTE*MINUTES_PER_HOUR;
    static final long NANOS_PER_DAY=NANOS_PER_HOUR*HOURS_PER_DAY;
    private static final long serialVersionUID=4556003607393004514L;
    private final transient D date;
    private final transient LocalTime time;

    private ChronoLocalDateTimeImpl(D date,LocalTime time){
        Objects.requireNonNull(date,"date");
        Objects.requireNonNull(time,"time");
        this.date=date;
        this.time=time;
    }

    //-----------------------------------------------------------------------
    static <R extends ChronoLocalDate> ChronoLocalDateTimeImpl<R> of(R date,LocalTime time){
        return new ChronoLocalDateTimeImpl<>(date,time);
    }

    static ChronoLocalDateTime<?> readExternal(ObjectInput in) throws IOException, ClassNotFoundException{
        ChronoLocalDate date=(ChronoLocalDate)in.readObject();
        LocalTime time=(LocalTime)in.readObject();
        return date.atTime(time);
    }

    private ChronoLocalDateTimeImpl<D> plusDays(long days){
        return with(date.plus(days,ChronoUnit.DAYS),time);
    }

    private ChronoLocalDateTimeImpl<D> plusHours(long hours){
        return plusWithOverflow(date,hours,0,0,0);
    }

    private ChronoLocalDateTimeImpl<D> plusMinutes(long minutes){
        return plusWithOverflow(date,0,minutes,0,0);
    }

    ChronoLocalDateTimeImpl<D> plusSeconds(long seconds){
        return plusWithOverflow(date,0,0,seconds,0);
    }

    private ChronoLocalDateTimeImpl<D> plusNanos(long nanos){
        return plusWithOverflow(date,0,0,0,nanos);
    }

    //-----------------------------------------------------------------------
    private ChronoLocalDateTimeImpl<D> plusWithOverflow(D newDate,long hours,long minutes,long seconds,long nanos){
        // 9223372036854775808 long, 2147483648 int
        if((hours|minutes|seconds|nanos)==0){
            return with(newDate,time);
        }
        long totDays=nanos/NANOS_PER_DAY+             //   max/24*60*60*1B
                seconds/SECONDS_PER_DAY+                //   max/24*60*60
                minutes/MINUTES_PER_DAY+                //   max/24*60
                hours/HOURS_PER_DAY;                     //   max/24
        long totNanos=nanos%NANOS_PER_DAY+                    //   max  86400000000000
                (seconds%SECONDS_PER_DAY)*NANOS_PER_SECOND+   //   max  86400000000000
                (minutes%MINUTES_PER_DAY)*NANOS_PER_MINUTE+   //   max  86400000000000
                (hours%HOURS_PER_DAY)*NANOS_PER_HOUR;          //   max  86400000000000
        long curNoD=time.toNanoOfDay();                          //   max  86400000000000
        totNanos=totNanos+curNoD;                              // total 432000000000000
        totDays+=Math.floorDiv(totNanos,NANOS_PER_DAY);
        long newNoD=Math.floorMod(totNanos,NANOS_PER_DAY);
        LocalTime newTime=(newNoD==curNoD?time:LocalTime.ofNanoOfDay(newNoD));
        return with(newDate.plus(totDays,ChronoUnit.DAYS),newTime);
    }

    //-----------------------------------------------------------------------
    @Override
    public long until(Temporal endExclusive,TemporalUnit unit){
        Objects.requireNonNull(endExclusive,"endExclusive");
        @SuppressWarnings("unchecked")
        ChronoLocalDateTime<D> end=(ChronoLocalDateTime<D>)getChronology().localDateTime(endExclusive);
        if(unit instanceof ChronoUnit){
            if(unit.isTimeBased()){
                long amount=end.getLong(EPOCH_DAY)-date.getLong(EPOCH_DAY);
                switch((ChronoUnit)unit){
                    case NANOS:
                        amount=Math.multiplyExact(amount,NANOS_PER_DAY);
                        break;
                    case MICROS:
                        amount=Math.multiplyExact(amount,MICROS_PER_DAY);
                        break;
                    case MILLIS:
                        amount=Math.multiplyExact(amount,MILLIS_PER_DAY);
                        break;
                    case SECONDS:
                        amount=Math.multiplyExact(amount,SECONDS_PER_DAY);
                        break;
                    case MINUTES:
                        amount=Math.multiplyExact(amount,MINUTES_PER_DAY);
                        break;
                    case HOURS:
                        amount=Math.multiplyExact(amount,HOURS_PER_DAY);
                        break;
                    case HALF_DAYS:
                        amount=Math.multiplyExact(amount,2);
                        break;
                }
                return Math.addExact(amount,time.until(end.toLocalTime(),unit));
            }
            ChronoLocalDate endDate=end.toLocalDate();
            if(end.toLocalTime().isBefore(time)){
                endDate=endDate.minus(1,ChronoUnit.DAYS);
            }
            return date.until(endDate,unit);
        }
        Objects.requireNonNull(unit,"unit");
        return unit.between(this,end);
    }    @Override
    public ValueRange range(TemporalField field){
        if(field instanceof ChronoField){
            ChronoField f=(ChronoField)field;
            return (f.isTimeBased()?time.range(field):date.range(field));
        }
        return field.rangeRefinedBy(this);
    }

    //-----------------------------------------------------------------------
    private Object writeReplace(){
        return new Ser(Ser.CHRONO_LOCAL_DATE_TIME_TYPE,this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException{
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(ObjectOutput out) throws IOException{
        out.writeObject(date);
        out.writeObject(time);
    }    @Override
    public int get(TemporalField field){
        if(field instanceof ChronoField){
            ChronoField f=(ChronoField)field;
            return (f.isTimeBased()?time.get(field):date.get(field));
        }
        return range(field).checkValidIntValue(getLong(field),field);
    }

    @Override
    public int hashCode(){
        return toLocalDate().hashCode()^toLocalTime().hashCode();
    }

    //-----------------------------------------------------------------------
    @Override
    public D toLocalDate(){
        return date;
    }

    @Override
    public LocalTime toLocalTime(){
        return time;
    }    @Override
    public long getLong(TemporalField field){
        if(field instanceof ChronoField){
            ChronoField f=(ChronoField)field;
            return (f.isTimeBased()?time.getLong(field):date.getLong(field));
        }
        return field.getFrom(this);
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isSupported(TemporalField field){
        if(field instanceof ChronoField){
            ChronoField f=(ChronoField)field;
            return f.isDateBased()||f.isTimeBased();
        }
        return field!=null&&field.isSupportedBy(this);
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public ChronoLocalDateTimeImpl<D> with(TemporalAdjuster adjuster){
        if(adjuster instanceof ChronoLocalDate){
            // The Chronology is checked in with(date,time)
            return with((ChronoLocalDate)adjuster,time);
        }else if(adjuster instanceof LocalTime){
            return with(date,(LocalTime)adjuster);
        }else if(adjuster instanceof ChronoLocalDateTimeImpl){
            return ChronoLocalDateTimeImpl.ensureValid(date.getChronology(),(ChronoLocalDateTimeImpl<?>)adjuster);
        }
        return ChronoLocalDateTimeImpl.ensureValid(date.getChronology(),(ChronoLocalDateTimeImpl<?>)adjuster.adjustInto(this));
    }

    static <R extends ChronoLocalDate> ChronoLocalDateTimeImpl<R> ensureValid(Chronology chrono,Temporal temporal){
        @SuppressWarnings("unchecked")
        ChronoLocalDateTimeImpl<R> other=(ChronoLocalDateTimeImpl<R>)temporal;
        if(chrono.equals(other.getChronology())==false){
            throw new ClassCastException("Chronology mismatch, required: "+chrono.getId()
                    +", actual: "+other.getChronology().getId());
        }
        return other;
    }

    private ChronoLocalDateTimeImpl<D> with(Temporal newDate,LocalTime newTime){
        if(date==newDate&&time==newTime){
            return this;
        }
        // Validate that the new Temporal is a ChronoLocalDate (and not something else)
        D cd=ChronoLocalDateImpl.ensureValid(date.getChronology(),newDate);
        return new ChronoLocalDateTimeImpl<>(cd,newTime);
    }

    @Override
    public ChronoLocalDateTimeImpl<D> with(TemporalField field,long newValue){
        if(field instanceof ChronoField){
            ChronoField f=(ChronoField)field;
            if(f.isTimeBased()){
                return with(date,time.with(field,newValue));
            }else{
                return with(date.with(field,newValue),time);
            }
        }
        return ChronoLocalDateTimeImpl.ensureValid(date.getChronology(),field.adjustInto(this,newValue));
    }

    //-----------------------------------------------------------------------
    @Override
    public ChronoLocalDateTimeImpl<D> plus(long amountToAdd,TemporalUnit unit){
        if(unit instanceof ChronoUnit){
            ChronoUnit f=(ChronoUnit)unit;
            switch(f){
                case NANOS:
                    return plusNanos(amountToAdd);
                case MICROS:
                    return plusDays(amountToAdd/MICROS_PER_DAY).plusNanos((amountToAdd%MICROS_PER_DAY)*1000);
                case MILLIS:
                    return plusDays(amountToAdd/MILLIS_PER_DAY).plusNanos((amountToAdd%MILLIS_PER_DAY)*1000000);
                case SECONDS:
                    return plusSeconds(amountToAdd);
                case MINUTES:
                    return plusMinutes(amountToAdd);
                case HOURS:
                    return plusHours(amountToAdd);
                case HALF_DAYS:
                    return plusDays(amountToAdd/256).plusHours((amountToAdd%256)*12);  // no overflow (256 is multiple of 2)
            }
            return with(date.plus(amountToAdd,unit),time);
        }
        return ChronoLocalDateTimeImpl.ensureValid(date.getChronology(),unit.addTo(this,amountToAdd));
    }

    //-----------------------------------------------------------------------
    @Override
    public ChronoZonedDateTime<D> atZone(ZoneId zone){
        return ChronoZonedDateTimeImpl.ofBest(this,zone,null);
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj){
        if(this==obj){
            return true;
        }
        if(obj instanceof ChronoLocalDateTime){
            return compareTo((ChronoLocalDateTime<?>)obj)==0;
        }
        return false;
    }

    @Override
    public String toString(){
        return toLocalDate().toString()+'T'+toLocalTime().toString();
    }






}
