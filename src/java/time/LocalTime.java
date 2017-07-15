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
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.util.Objects;

import static java.time.temporal.ChronoField.*;
import static java.time.temporal.ChronoUnit.NANOS;

public final class LocalTime
        implements Temporal, TemporalAdjuster, Comparable<LocalTime>, Serializable{
    public static final LocalTime MIN;
    public static final LocalTime MAX;
    public static final LocalTime MIDNIGHT;
    public static final LocalTime NOON;
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
    private static final LocalTime[] HOURS=new LocalTime[24];
    private static final long serialVersionUID=6414437269572265201L;

    static{
        for(int i=0;i<HOURS.length;i++){
            HOURS[i]=new LocalTime(i,0,0,0);
        }
        MIDNIGHT=HOURS[0];
        NOON=HOURS[12];
        MIN=HOURS[0];
        MAX=new LocalTime(23,59,59,999_999_999);
    }

    private final byte hour;
    private final byte minute;
    private final byte second;
    private final int nano;

    private LocalTime(int hour,int minute,int second,int nanoOfSecond){
        this.hour=(byte)hour;
        this.minute=(byte)minute;
        this.second=(byte)second;
        this.nano=nanoOfSecond;
    }

    //-----------------------------------------------------------------------
    public static LocalTime now(){
        return now(Clock.systemDefaultZone());
    }

    public static LocalTime now(Clock clock){
        Objects.requireNonNull(clock,"clock");
        // inline OffsetTime factory to avoid creating object and InstantProvider checks
        final Instant now=clock.instant();  // called once
        ZoneOffset offset=clock.getZone().getRules().getOffset(now);
        long localSecond=now.getEpochSecond()+offset.getTotalSeconds();  // overflow caught later
        int secsOfDay=(int)Math.floorMod(localSecond,SECONDS_PER_DAY);
        return ofNanoOfDay(secsOfDay*NANOS_PER_SECOND+now.getNano());
    }

    public static LocalTime ofNanoOfDay(long nanoOfDay){
        NANO_OF_DAY.checkValidValue(nanoOfDay);
        int hours=(int)(nanoOfDay/NANOS_PER_HOUR);
        nanoOfDay-=hours*NANOS_PER_HOUR;
        int minutes=(int)(nanoOfDay/NANOS_PER_MINUTE);
        nanoOfDay-=minutes*NANOS_PER_MINUTE;
        int seconds=(int)(nanoOfDay/NANOS_PER_SECOND);
        nanoOfDay-=seconds*NANOS_PER_SECOND;
        return create(hours,minutes,seconds,(int)nanoOfDay);
    }

    //-----------------------------------------------------------------------
    private static LocalTime create(int hour,int minute,int second,int nanoOfSecond){
        if((minute|second|nanoOfSecond)==0){
            return HOURS[hour];
        }
        return new LocalTime(hour,minute,second,nanoOfSecond);
    }

    public static LocalTime now(ZoneId zone){
        return now(Clock.system(zone));
    }

    //-----------------------------------------------------------------------
    public static LocalTime of(int hour,int minute){
        HOUR_OF_DAY.checkValidValue(hour);
        if(minute==0){
            return HOURS[hour];  // for performance
        }
        MINUTE_OF_HOUR.checkValidValue(minute);
        return new LocalTime(hour,minute,0,0);
    }

    public static LocalTime of(int hour,int minute,int second){
        HOUR_OF_DAY.checkValidValue(hour);
        if((minute|second)==0){
            return HOURS[hour];  // for performance
        }
        MINUTE_OF_HOUR.checkValidValue(minute);
        SECOND_OF_MINUTE.checkValidValue(second);
        return new LocalTime(hour,minute,second,0);
    }

    //-----------------------------------------------------------------------
    public static LocalTime ofSecondOfDay(long secondOfDay){
        SECOND_OF_DAY.checkValidValue(secondOfDay);
        int hours=(int)(secondOfDay/SECONDS_PER_HOUR);
        secondOfDay-=hours*SECONDS_PER_HOUR;
        int minutes=(int)(secondOfDay/SECONDS_PER_MINUTE);
        secondOfDay-=minutes*SECONDS_PER_MINUTE;
        return create(hours,minutes,(int)secondOfDay,0);
    }

    //-----------------------------------------------------------------------
    public static LocalTime parse(CharSequence text){
        return parse(text,DateTimeFormatter.ISO_LOCAL_TIME);
    }

    public static LocalTime parse(CharSequence text,DateTimeFormatter formatter){
        Objects.requireNonNull(formatter,"formatter");
        return formatter.parse(text,LocalTime::from);
    }

    static LocalTime readExternal(DataInput in) throws IOException{
        int hour=in.readByte();
        int minute=0;
        int second=0;
        int nano=0;
        if(hour<0){
            hour=~hour;
        }else{
            minute=in.readByte();
            if(minute<0){
                minute=~minute;
            }else{
                second=in.readByte();
                if(second<0){
                    second=~second;
                }else{
                    nano=in.readInt();
                }
            }
        }
        return LocalTime.of(hour,minute,second,nano);
    }

    public static LocalTime of(int hour,int minute,int second,int nanoOfSecond){
        HOUR_OF_DAY.checkValidValue(hour);
        MINUTE_OF_HOUR.checkValidValue(minute);
        SECOND_OF_MINUTE.checkValidValue(second);
        NANO_OF_SECOND.checkValidValue(nanoOfSecond);
        return create(hour,minute,second,nanoOfSecond);
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isSupported(TemporalField field){
        if(field instanceof ChronoField){
            return field.isTimeBased();
        }
        return field!=null&&field.isSupportedBy(this);
    }

    //-----------------------------------------------------------------------
    @Override  // override for Javadoc
    public ValueRange range(TemporalField field){
        return Temporal.super.range(field);
    }

    @Override  // override for Javadoc and performance
    public int get(TemporalField field){
        if(field instanceof ChronoField){
            return get0(field);
        }
        return Temporal.super.get(field);
    }

    @Override
    public long getLong(TemporalField field){
        if(field instanceof ChronoField){
            if(field==NANO_OF_DAY){
                return toNanoOfDay();
            }
            if(field==MICRO_OF_DAY){
                return toNanoOfDay()/1000;
            }
            return get0(field);
        }
        return field.getFrom(this);
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public <R> R query(TemporalQuery<R> query){
        if(query==TemporalQueries.chronology()||query==TemporalQueries.zoneId()||
                query==TemporalQueries.zone()||query==TemporalQueries.offset()){
            return null;
        }else if(query==TemporalQueries.localTime()){
            return (R)this;
        }else if(query==TemporalQueries.localDate()){
            return null;
        }else if(query==TemporalQueries.precision()){
            return (R)NANOS;
        }
        // inline TemporalAccessor.super.query(query) as an optimization
        // non-JDK classes are not permitted to make this optimization
        return query.queryFrom(this);
    }

    private int get0(TemporalField field){
        switch((ChronoField)field){
            case NANO_OF_SECOND:
                return nano;
            case NANO_OF_DAY:
                throw new UnsupportedTemporalTypeException("Invalid field 'NanoOfDay' for get() method, use getLong() instead");
            case MICRO_OF_SECOND:
                return nano/1000;
            case MICRO_OF_DAY:
                throw new UnsupportedTemporalTypeException("Invalid field 'MicroOfDay' for get() method, use getLong() instead");
            case MILLI_OF_SECOND:
                return nano/1000_000;
            case MILLI_OF_DAY:
                return (int)(toNanoOfDay()/1000_000);
            case SECOND_OF_MINUTE:
                return second;
            case SECOND_OF_DAY:
                return toSecondOfDay();
            case MINUTE_OF_HOUR:
                return minute;
            case MINUTE_OF_DAY:
                return hour*60+minute;
            case HOUR_OF_AMPM:
                return hour%12;
            case CLOCK_HOUR_OF_AMPM:
                int ham=hour%12;
                return (ham%12==0?12:ham);
            case HOUR_OF_DAY:
                return hour;
            case CLOCK_HOUR_OF_DAY:
                return (hour==0?24:hour);
            case AMPM_OF_DAY:
                return hour/12;
        }
        throw new UnsupportedTemporalTypeException("Unsupported field: "+field);
    }

    //-----------------------------------------------------------------------
    public int toSecondOfDay(){
        int total=hour*SECONDS_PER_HOUR;
        total+=minute*SECONDS_PER_MINUTE;
        total+=second;
        return total;
    }

    public long toNanoOfDay(){
        long total=hour*NANOS_PER_HOUR;
        total+=minute*NANOS_PER_MINUTE;
        total+=second*NANOS_PER_SECOND;
        total+=nano;
        return total;
    }

    @Override  // override for Javadoc
    public boolean isSupported(TemporalUnit unit){
        if(unit instanceof ChronoUnit){
            return unit.isTimeBased();
        }
        return unit!=null&&unit.isSupportedBy(this);
    }

    //-----------------------------------------------------------------------
    @Override
    public LocalTime with(TemporalAdjuster adjuster){
        // optimizations
        if(adjuster instanceof LocalTime){
            return (LocalTime)adjuster;
        }
        return (LocalTime)adjuster.adjustInto(this);
    }

    @Override
    public LocalTime with(TemporalField field,long newValue){
        if(field instanceof ChronoField){
            ChronoField f=(ChronoField)field;
            f.checkValidValue(newValue);
            switch(f){
                case NANO_OF_SECOND:
                    return withNano((int)newValue);
                case NANO_OF_DAY:
                    return LocalTime.ofNanoOfDay(newValue);
                case MICRO_OF_SECOND:
                    return withNano((int)newValue*1000);
                case MICRO_OF_DAY:
                    return LocalTime.ofNanoOfDay(newValue*1000);
                case MILLI_OF_SECOND:
                    return withNano((int)newValue*1000_000);
                case MILLI_OF_DAY:
                    return LocalTime.ofNanoOfDay(newValue*1000_000);
                case SECOND_OF_MINUTE:
                    return withSecond((int)newValue);
                case SECOND_OF_DAY:
                    return plusSeconds(newValue-toSecondOfDay());
                case MINUTE_OF_HOUR:
                    return withMinute((int)newValue);
                case MINUTE_OF_DAY:
                    return plusMinutes(newValue-(hour*60+minute));
                case HOUR_OF_AMPM:
                    return plusHours(newValue-(hour%12));
                case CLOCK_HOUR_OF_AMPM:
                    return plusHours((newValue==12?0:newValue)-(hour%12));
                case HOUR_OF_DAY:
                    return withHour((int)newValue);
                case CLOCK_HOUR_OF_DAY:
                    return withHour((int)(newValue==24?0:newValue));
                case AMPM_OF_DAY:
                    return plusHours((newValue-(hour/12))*12);
            }
            throw new UnsupportedTemporalTypeException("Unsupported field: "+field);
        }
        return field.adjustInto(this,newValue);
    }

    //-----------------------------------------------------------------------
    public LocalTime withHour(int hour){
        if(this.hour==hour){
            return this;
        }
        HOUR_OF_DAY.checkValidValue(hour);
        return create(hour,minute,second,nano);
    }

    public LocalTime withMinute(int minute){
        if(this.minute==minute){
            return this;
        }
        MINUTE_OF_HOUR.checkValidValue(minute);
        return create(hour,minute,second,nano);
    }

    public LocalTime withSecond(int second){
        if(this.second==second){
            return this;
        }
        SECOND_OF_MINUTE.checkValidValue(second);
        return create(hour,minute,second,nano);
    }

    public LocalTime withNano(int nanoOfSecond){
        if(this.nano==nanoOfSecond){
            return this;
        }
        NANO_OF_SECOND.checkValidValue(nanoOfSecond);
        return create(hour,minute,second,nanoOfSecond);
    }

    //-----------------------------------------------------------------------
    @Override
    public LocalTime plus(TemporalAmount amountToAdd){
        return (LocalTime)amountToAdd.addTo(this);
    }

    @Override
    public LocalTime plus(long amountToAdd,TemporalUnit unit){
        if(unit instanceof ChronoUnit){
            switch((ChronoUnit)unit){
                case NANOS:
                    return plusNanos(amountToAdd);
                case MICROS:
                    return plusNanos((amountToAdd%MICROS_PER_DAY)*1000);
                case MILLIS:
                    return plusNanos((amountToAdd%MILLIS_PER_DAY)*1000_000);
                case SECONDS:
                    return plusSeconds(amountToAdd);
                case MINUTES:
                    return plusMinutes(amountToAdd);
                case HOURS:
                    return plusHours(amountToAdd);
                case HALF_DAYS:
                    return plusHours((amountToAdd%2)*12);
            }
            throw new UnsupportedTemporalTypeException("Unsupported unit: "+unit);
        }
        return unit.addTo(this,amountToAdd);
    }

    public LocalTime plusNanos(long nanosToAdd){
        if(nanosToAdd==0){
            return this;
        }
        long nofd=toNanoOfDay();
        long newNofd=((nanosToAdd%NANOS_PER_DAY)+nofd+NANOS_PER_DAY)%NANOS_PER_DAY;
        if(nofd==newNofd){
            return this;
        }
        int newHour=(int)(newNofd/NANOS_PER_HOUR);
        int newMinute=(int)((newNofd/NANOS_PER_MINUTE)%MINUTES_PER_HOUR);
        int newSecond=(int)((newNofd/NANOS_PER_SECOND)%SECONDS_PER_MINUTE);
        int newNano=(int)(newNofd%NANOS_PER_SECOND);
        return create(newHour,newMinute,newSecond,newNano);
    }

    //-----------------------------------------------------------------------
    @Override
    public LocalTime minus(TemporalAmount amountToSubtract){
        return (LocalTime)amountToSubtract.subtractFrom(this);
    }

    @Override
    public LocalTime minus(long amountToSubtract,TemporalUnit unit){
        return (amountToSubtract==Long.MIN_VALUE?plus(Long.MAX_VALUE,unit).plus(1,unit):plus(-amountToSubtract,unit));
    }

    @Override
    public long until(Temporal endExclusive,TemporalUnit unit){
        LocalTime end=LocalTime.from(endExclusive);
        if(unit instanceof ChronoUnit){
            long nanosUntil=end.toNanoOfDay()-toNanoOfDay();  // no overflow
            switch((ChronoUnit)unit){
                case NANOS:
                    return nanosUntil;
                case MICROS:
                    return nanosUntil/1000;
                case MILLIS:
                    return nanosUntil/1000_000;
                case SECONDS:
                    return nanosUntil/NANOS_PER_SECOND;
                case MINUTES:
                    return nanosUntil/NANOS_PER_MINUTE;
                case HOURS:
                    return nanosUntil/NANOS_PER_HOUR;
                case HALF_DAYS:
                    return nanosUntil/(12*NANOS_PER_HOUR);
            }
            throw new UnsupportedTemporalTypeException("Unsupported unit: "+unit);
        }
        return unit.between(this,end);
    }

    //-----------------------------------------------------------------------
    public static LocalTime from(TemporalAccessor temporal){
        Objects.requireNonNull(temporal,"temporal");
        LocalTime time=temporal.query(TemporalQueries.localTime());
        if(time==null){
            throw new DateTimeException("Unable to obtain LocalTime from TemporalAccessor: "+
                    temporal+" of type "+temporal.getClass().getName());
        }
        return time;
    }

    //-----------------------------------------------------------------------
    public LocalTime plusHours(long hoursToAdd){
        if(hoursToAdd==0){
            return this;
        }
        int newHour=((int)(hoursToAdd%HOURS_PER_DAY)+hour+HOURS_PER_DAY)%HOURS_PER_DAY;
        return create(newHour,minute,second,nano);
    }

    public LocalTime plusMinutes(long minutesToAdd){
        if(minutesToAdd==0){
            return this;
        }
        int mofd=hour*MINUTES_PER_HOUR+minute;
        int newMofd=((int)(minutesToAdd%MINUTES_PER_DAY)+mofd+MINUTES_PER_DAY)%MINUTES_PER_DAY;
        if(mofd==newMofd){
            return this;
        }
        int newHour=newMofd/MINUTES_PER_HOUR;
        int newMinute=newMofd%MINUTES_PER_HOUR;
        return create(newHour,newMinute,second,nano);
    }

    public LocalTime plusSeconds(long secondstoAdd){
        if(secondstoAdd==0){
            return this;
        }
        int sofd=hour*SECONDS_PER_HOUR+
                minute*SECONDS_PER_MINUTE+second;
        int newSofd=((int)(secondstoAdd%SECONDS_PER_DAY)+sofd+SECONDS_PER_DAY)%SECONDS_PER_DAY;
        if(sofd==newSofd){
            return this;
        }
        int newHour=newSofd/SECONDS_PER_HOUR;
        int newMinute=(newSofd/SECONDS_PER_MINUTE)%MINUTES_PER_HOUR;
        int newSecond=newSofd%SECONDS_PER_MINUTE;
        return create(newHour,newMinute,newSecond,nano);
    }

    //-----------------------------------------------------------------------
    public int getHour(){
        return hour;
    }

    public int getMinute(){
        return minute;
    }

    public int getSecond(){
        return second;
    }

    public int getNano(){
        return nano;
    }

    //-----------------------------------------------------------------------
    public LocalTime truncatedTo(TemporalUnit unit){
        if(unit==ChronoUnit.NANOS){
            return this;
        }
        Duration unitDur=unit.getDuration();
        if(unitDur.getSeconds()>SECONDS_PER_DAY){
            throw new UnsupportedTemporalTypeException("Unit is too large to be used for truncation");
        }
        long dur=unitDur.toNanos();
        if((NANOS_PER_DAY%dur)!=0){
            throw new UnsupportedTemporalTypeException("Unit must divide into a standard day without remainder");
        }
        long nod=toNanoOfDay();
        return ofNanoOfDay((nod/dur)*dur);
    }

    //-----------------------------------------------------------------------
    public LocalTime minusHours(long hoursToSubtract){
        return plusHours(-(hoursToSubtract%HOURS_PER_DAY));
    }

    public LocalTime minusMinutes(long minutesToSubtract){
        return plusMinutes(-(minutesToSubtract%MINUTES_PER_DAY));
    }

    public LocalTime minusSeconds(long secondsToSubtract){
        return plusSeconds(-(secondsToSubtract%SECONDS_PER_DAY));
    }

    public LocalTime minusNanos(long nanosToSubtract){
        return plusNanos(-(nanosToSubtract%NANOS_PER_DAY));
    }

    @Override
    public Temporal adjustInto(Temporal temporal){
        return temporal.with(NANO_OF_DAY,toNanoOfDay());
    }

    public String format(DateTimeFormatter formatter){
        Objects.requireNonNull(formatter,"formatter");
        return formatter.format(this);
    }

    //-----------------------------------------------------------------------
    public LocalDateTime atDate(LocalDate date){
        return LocalDateTime.of(date,this);
    }

    public OffsetTime atOffset(ZoneOffset offset){
        return OffsetTime.of(this,offset);
    }

    public boolean isAfter(LocalTime other){
        return compareTo(other)>0;
    }

    //-----------------------------------------------------------------------
    @Override
    public int compareTo(LocalTime other){
        int cmp=Integer.compare(hour,other.hour);
        if(cmp==0){
            cmp=Integer.compare(minute,other.minute);
            if(cmp==0){
                cmp=Integer.compare(second,other.second);
                if(cmp==0){
                    cmp=Integer.compare(nano,other.nano);
                }
            }
        }
        return cmp;
    }

    public boolean isBefore(LocalTime other){
        return compareTo(other)<0;
    }

    @Override
    public int hashCode(){
        long nod=toNanoOfDay();
        return (int)(nod^(nod>>>32));
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj){
        if(this==obj){
            return true;
        }
        if(obj instanceof LocalTime){
            LocalTime other=(LocalTime)obj;
            return hour==other.hour&&minute==other.minute&&
                    second==other.second&&nano==other.nano;
        }
        return false;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString(){
        StringBuilder buf=new StringBuilder(18);
        int hourValue=hour;
        int minuteValue=minute;
        int secondValue=second;
        int nanoValue=nano;
        buf.append(hourValue<10?"0":"").append(hourValue)
                .append(minuteValue<10?":0":":").append(minuteValue);
        if(secondValue>0||nanoValue>0){
            buf.append(secondValue<10?":0":":").append(secondValue);
            if(nanoValue>0){
                buf.append('.');
                if(nanoValue%1000_000==0){
                    buf.append(Integer.toString((nanoValue/1000_000)+1000).substring(1));
                }else if(nanoValue%1000==0){
                    buf.append(Integer.toString((nanoValue/1000)+1000_000).substring(1));
                }else{
                    buf.append(Integer.toString((nanoValue)+1000_000_000).substring(1));
                }
            }
        }
        return buf.toString();
    }

    //-----------------------------------------------------------------------
    private Object writeReplace(){
        return new Ser(Ser.LOCAL_TIME_TYPE,this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException{
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(DataOutput out) throws IOException{
        if(nano==0){
            if(second==0){
                if(minute==0){
                    out.writeByte(~hour);
                }else{
                    out.writeByte(hour);
                    out.writeByte(~minute);
                }
            }else{
                out.writeByte(hour);
                out.writeByte(minute);
                out.writeByte(~second);
            }
        }else{
            out.writeByte(hour);
            out.writeByte(minute);
            out.writeByte(second);
            out.writeInt(nano);
        }
    }
}
