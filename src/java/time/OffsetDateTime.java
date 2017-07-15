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
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.time.zone.ZoneRules;
import java.util.Comparator;
import java.util.Objects;

import static java.time.temporal.ChronoField.*;
import static java.time.temporal.ChronoUnit.FOREVER;
import static java.time.temporal.ChronoUnit.NANOS;

public final class OffsetDateTime
        implements Temporal, TemporalAdjuster, Comparable<OffsetDateTime>, Serializable{
    public static final OffsetDateTime MIN=LocalDateTime.MIN.atOffset(ZoneOffset.MAX);
    public static final OffsetDateTime MAX=LocalDateTime.MAX.atOffset(ZoneOffset.MIN);
    private static final long serialVersionUID=2287754244819255394L;
    private final LocalDateTime dateTime;
    private final ZoneOffset offset;

    //-----------------------------------------------------------------------
    private OffsetDateTime(LocalDateTime dateTime,ZoneOffset offset){
        this.dateTime=Objects.requireNonNull(dateTime,"dateTime");
        this.offset=Objects.requireNonNull(offset,"offset");
    }

    public static Comparator<OffsetDateTime> timeLineOrder(){
        return OffsetDateTime::compareInstant;
    }

    //-----------------------------------------------------------------------
    public static OffsetDateTime now(){
        return now(Clock.systemDefaultZone());
    }

    public static OffsetDateTime now(Clock clock){
        Objects.requireNonNull(clock,"clock");
        final Instant now=clock.instant();  // called once
        return ofInstant(now,clock.getZone().getRules().getOffset(now));
    }

    //-----------------------------------------------------------------------
    public static OffsetDateTime ofInstant(Instant instant,ZoneId zone){
        Objects.requireNonNull(instant,"instant");
        Objects.requireNonNull(zone,"zone");
        ZoneRules rules=zone.getRules();
        ZoneOffset offset=rules.getOffset(instant);
        LocalDateTime ldt=LocalDateTime.ofEpochSecond(instant.getEpochSecond(),instant.getNano(),offset);
        return new OffsetDateTime(ldt,offset);
    }

    public static OffsetDateTime now(ZoneId zone){
        return now(Clock.system(zone));
    }

    public static OffsetDateTime of(
            int year,int month,int dayOfMonth,
            int hour,int minute,int second,int nanoOfSecond,ZoneOffset offset){
        LocalDateTime dt=LocalDateTime.of(year,month,dayOfMonth,hour,minute,second,nanoOfSecond);
        return new OffsetDateTime(dt,offset);
    }

    //-----------------------------------------------------------------------
    public static OffsetDateTime parse(CharSequence text){
        return parse(text,DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public static OffsetDateTime parse(CharSequence text,DateTimeFormatter formatter){
        Objects.requireNonNull(formatter,"formatter");
        return formatter.parse(text,OffsetDateTime::from);
    }

    static OffsetDateTime readExternal(ObjectInput in) throws IOException, ClassNotFoundException{
        LocalDateTime dateTime=LocalDateTime.readExternal(in);
        ZoneOffset offset=ZoneOffset.readExternal(in);
        return OffsetDateTime.of(dateTime,offset);
    }

    public static OffsetDateTime of(LocalDateTime dateTime,ZoneOffset offset){
        return new OffsetDateTime(dateTime,offset);
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isSupported(TemporalField field){
        return field instanceof ChronoField||(field!=null&&field.isSupportedBy(this));
    }

    //-----------------------------------------------------------------------
    @Override
    public ValueRange range(TemporalField field){
        if(field instanceof ChronoField){
            if(field==INSTANT_SECONDS||field==OFFSET_SECONDS){
                return field.range();
            }
            return dateTime.range(field);
        }
        return field.rangeRefinedBy(this);
    }

    @Override
    public int get(TemporalField field){
        if(field instanceof ChronoField){
            switch((ChronoField)field){
                case INSTANT_SECONDS:
                    throw new UnsupportedTemporalTypeException("Invalid field 'InstantSeconds' for get() method, use getLong() instead");
                case OFFSET_SECONDS:
                    return getOffset().getTotalSeconds();
            }
            return dateTime.get(field);
        }
        return Temporal.super.get(field);
    }

    @Override
    public long getLong(TemporalField field){
        if(field instanceof ChronoField){
            switch((ChronoField)field){
                case INSTANT_SECONDS:
                    return toEpochSecond();
                case OFFSET_SECONDS:
                    return getOffset().getTotalSeconds();
            }
            return dateTime.getLong(field);
        }
        return field.getFrom(this);
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public <R> R query(TemporalQuery<R> query){
        if(query==TemporalQueries.offset()||query==TemporalQueries.zone()){
            return (R)getOffset();
        }else if(query==TemporalQueries.zoneId()){
            return null;
        }else if(query==TemporalQueries.localDate()){
            return (R)toLocalDate();
        }else if(query==TemporalQueries.localTime()){
            return (R)toLocalTime();
        }else if(query==TemporalQueries.chronology()){
            return (R)IsoChronology.INSTANCE;
        }else if(query==TemporalQueries.precision()){
            return (R)NANOS;
        }
        // inline TemporalAccessor.super.query(query) as an optimization
        // non-JDK classes are not permitted to make this optimization
        return query.queryFrom(this);
    }

    //-----------------------------------------------------------------------
    public LocalDate toLocalDate(){
        return dateTime.toLocalDate();
    }

    //-----------------------------------------------------------------------
    public LocalTime toLocalTime(){
        return dateTime.toLocalTime();
    }

    public long toEpochSecond(){
        return dateTime.toEpochSecond(offset);
    }

    //-----------------------------------------------------------------------
    public ZoneOffset getOffset(){
        return offset;
    }

    @Override  // override for Javadoc
    public boolean isSupported(TemporalUnit unit){
        if(unit instanceof ChronoUnit){
            return unit!=FOREVER;
        }
        return unit!=null&&unit.isSupportedBy(this);
    }

    //-----------------------------------------------------------------------
    @Override
    public OffsetDateTime with(TemporalAdjuster adjuster){
        // optimizations
        if(adjuster instanceof LocalDate||adjuster instanceof LocalTime||adjuster instanceof LocalDateTime){
            return with(dateTime.with(adjuster),offset);
        }else if(adjuster instanceof Instant){
            return ofInstant((Instant)adjuster,offset);
        }else if(adjuster instanceof ZoneOffset){
            return with(dateTime,(ZoneOffset)adjuster);
        }else if(adjuster instanceof OffsetDateTime){
            return (OffsetDateTime)adjuster;
        }
        return (OffsetDateTime)adjuster.adjustInto(this);
    }

    @Override
    public OffsetDateTime with(TemporalField field,long newValue){
        if(field instanceof ChronoField){
            ChronoField f=(ChronoField)field;
            switch(f){
                case INSTANT_SECONDS:
                    return ofInstant(Instant.ofEpochSecond(newValue,getNano()),offset);
                case OFFSET_SECONDS:{
                    return with(dateTime,ZoneOffset.ofTotalSeconds(f.checkValidIntValue(newValue)));
                }
            }
            return with(dateTime.with(field,newValue),offset);
        }
        return field.adjustInto(this,newValue);
    }

    public int getNano(){
        return dateTime.getNano();
    }

    //-----------------------------------------------------------------------
    @Override
    public OffsetDateTime plus(TemporalAmount amountToAdd){
        return (OffsetDateTime)amountToAdd.addTo(this);
    }

    @Override
    public OffsetDateTime plus(long amountToAdd,TemporalUnit unit){
        if(unit instanceof ChronoUnit){
            return with(dateTime.plus(amountToAdd,unit),offset);
        }
        return unit.addTo(this,amountToAdd);
    }

    //-----------------------------------------------------------------------
    @Override
    public OffsetDateTime minus(TemporalAmount amountToSubtract){
        return (OffsetDateTime)amountToSubtract.subtractFrom(this);
    }

    @Override
    public OffsetDateTime minus(long amountToSubtract,TemporalUnit unit){
        return (amountToSubtract==Long.MIN_VALUE?plus(Long.MAX_VALUE,unit).plus(1,unit):plus(-amountToSubtract,unit));
    }

    @Override
    public long until(Temporal endExclusive,TemporalUnit unit){
        OffsetDateTime end=OffsetDateTime.from(endExclusive);
        if(unit instanceof ChronoUnit){
            end=end.withOffsetSameInstant(offset);
            return dateTime.until(end.dateTime,unit);
        }
        return unit.between(this,end);
    }

    //-----------------------------------------------------------------------
    public static OffsetDateTime from(TemporalAccessor temporal){
        if(temporal instanceof OffsetDateTime){
            return (OffsetDateTime)temporal;
        }
        try{
            ZoneOffset offset=ZoneOffset.from(temporal);
            LocalDate date=temporal.query(TemporalQueries.localDate());
            LocalTime time=temporal.query(TemporalQueries.localTime());
            if(date!=null&&time!=null){
                return OffsetDateTime.of(date,time,offset);
            }else{
                Instant instant=Instant.from(temporal);
                return OffsetDateTime.ofInstant(instant,offset);
            }
        }catch(DateTimeException ex){
            throw new DateTimeException("Unable to obtain OffsetDateTime from TemporalAccessor: "+
                    temporal+" of type "+temporal.getClass().getName(),ex);
        }
    }

    //-----------------------------------------------------------------------
    public static OffsetDateTime of(LocalDate date,LocalTime time,ZoneOffset offset){
        LocalDateTime dt=LocalDateTime.of(date,time);
        return new OffsetDateTime(dt,offset);
    }

    private OffsetDateTime with(LocalDateTime dateTime,ZoneOffset offset){
        if(this.dateTime==dateTime&&this.offset.equals(offset)){
            return this;
        }
        return new OffsetDateTime(dateTime,offset);
    }

    public OffsetDateTime withOffsetSameLocal(ZoneOffset offset){
        return with(dateTime,offset);
    }

    public OffsetDateTime withOffsetSameInstant(ZoneOffset offset){
        if(offset.equals(this.offset)){
            return this;
        }
        int difference=offset.getTotalSeconds()-this.offset.getTotalSeconds();
        LocalDateTime adjusted=dateTime.plusSeconds(difference);
        return new OffsetDateTime(adjusted,offset);
    }

    public int getYear(){
        return dateTime.getYear();
    }

    public int getMonthValue(){
        return dateTime.getMonthValue();
    }

    public Month getMonth(){
        return dateTime.getMonth();
    }

    public int getDayOfMonth(){
        return dateTime.getDayOfMonth();
    }

    public int getDayOfYear(){
        return dateTime.getDayOfYear();
    }

    public DayOfWeek getDayOfWeek(){
        return dateTime.getDayOfWeek();
    }

    public int getHour(){
        return dateTime.getHour();
    }

    public int getMinute(){
        return dateTime.getMinute();
    }

    public int getSecond(){
        return dateTime.getSecond();
    }

    //-----------------------------------------------------------------------
    public OffsetDateTime withYear(int year){
        return with(dateTime.withYear(year),offset);
    }

    public OffsetDateTime withMonth(int month){
        return with(dateTime.withMonth(month),offset);
    }

    public OffsetDateTime withDayOfMonth(int dayOfMonth){
        return with(dateTime.withDayOfMonth(dayOfMonth),offset);
    }

    public OffsetDateTime withDayOfYear(int dayOfYear){
        return with(dateTime.withDayOfYear(dayOfYear),offset);
    }

    //-----------------------------------------------------------------------
    public OffsetDateTime withHour(int hour){
        return with(dateTime.withHour(hour),offset);
    }

    public OffsetDateTime withMinute(int minute){
        return with(dateTime.withMinute(minute),offset);
    }

    public OffsetDateTime withSecond(int second){
        return with(dateTime.withSecond(second),offset);
    }

    public OffsetDateTime withNano(int nanoOfSecond){
        return with(dateTime.withNano(nanoOfSecond),offset);
    }

    //-----------------------------------------------------------------------
    public OffsetDateTime truncatedTo(TemporalUnit unit){
        return with(dateTime.truncatedTo(unit),offset);
    }

    //-----------------------------------------------------------------------
    public OffsetDateTime minusYears(long years){
        return (years==Long.MIN_VALUE?plusYears(Long.MAX_VALUE).plusYears(1):plusYears(-years));
    }

    //-----------------------------------------------------------------------
    public OffsetDateTime plusYears(long years){
        return with(dateTime.plusYears(years),offset);
    }

    public OffsetDateTime minusMonths(long months){
        return (months==Long.MIN_VALUE?plusMonths(Long.MAX_VALUE).plusMonths(1):plusMonths(-months));
    }

    public OffsetDateTime plusMonths(long months){
        return with(dateTime.plusMonths(months),offset);
    }

    public OffsetDateTime minusWeeks(long weeks){
        return (weeks==Long.MIN_VALUE?plusWeeks(Long.MAX_VALUE).plusWeeks(1):plusWeeks(-weeks));
    }

    public OffsetDateTime plusWeeks(long weeks){
        return with(dateTime.plusWeeks(weeks),offset);
    }

    public OffsetDateTime minusDays(long days){
        return (days==Long.MIN_VALUE?plusDays(Long.MAX_VALUE).plusDays(1):plusDays(-days));
    }

    public OffsetDateTime plusDays(long days){
        return with(dateTime.plusDays(days),offset);
    }

    public OffsetDateTime minusHours(long hours){
        return (hours==Long.MIN_VALUE?plusHours(Long.MAX_VALUE).plusHours(1):plusHours(-hours));
    }

    public OffsetDateTime plusHours(long hours){
        return with(dateTime.plusHours(hours),offset);
    }

    public OffsetDateTime minusMinutes(long minutes){
        return (minutes==Long.MIN_VALUE?plusMinutes(Long.MAX_VALUE).plusMinutes(1):plusMinutes(-minutes));
    }

    public OffsetDateTime plusMinutes(long minutes){
        return with(dateTime.plusMinutes(minutes),offset);
    }

    public OffsetDateTime minusSeconds(long seconds){
        return (seconds==Long.MIN_VALUE?plusSeconds(Long.MAX_VALUE).plusSeconds(1):plusSeconds(-seconds));
    }

    public OffsetDateTime plusSeconds(long seconds){
        return with(dateTime.plusSeconds(seconds),offset);
    }

    public OffsetDateTime minusNanos(long nanos){
        return (nanos==Long.MIN_VALUE?plusNanos(Long.MAX_VALUE).plusNanos(1):plusNanos(-nanos));
    }

    public OffsetDateTime plusNanos(long nanos){
        return with(dateTime.plusNanos(nanos),offset);
    }

    @Override
    public Temporal adjustInto(Temporal temporal){
        // OffsetDateTime is treated as three separate fields, not an instant
        // this produces the most consistent set of results overall
        // the offset is set after the date and time, as it is typically a small
        // tweak to the result, with ZonedDateTime frequently ignoring the offset
        return temporal
                .with(EPOCH_DAY,toLocalDate().toEpochDay())
                .with(NANO_OF_DAY,toLocalTime().toNanoOfDay())
                .with(OFFSET_SECONDS,getOffset().getTotalSeconds());
    }

    public String format(DateTimeFormatter formatter){
        Objects.requireNonNull(formatter,"formatter");
        return formatter.format(this);
    }

    //-----------------------------------------------------------------------
    public ZonedDateTime atZoneSameInstant(ZoneId zone){
        return ZonedDateTime.ofInstant(dateTime,offset,zone);
    }

    public ZonedDateTime atZoneSimilarLocal(ZoneId zone){
        return ZonedDateTime.ofLocal(dateTime,zone,offset);
    }

    //-----------------------------------------------------------------------
    public OffsetTime toOffsetTime(){
        return OffsetTime.of(dateTime.toLocalTime(),offset);
    }

    public ZonedDateTime toZonedDateTime(){
        return ZonedDateTime.of(dateTime,offset);
    }

    public Instant toInstant(){
        return dateTime.toInstant(offset);
    }

    //-----------------------------------------------------------------------
    @Override
    public int compareTo(OffsetDateTime other){
        int cmp=compareInstant(this,other);
        if(cmp==0){
            cmp=toLocalDateTime().compareTo(other.toLocalDateTime());
        }
        return cmp;
    }

    private static int compareInstant(OffsetDateTime datetime1,OffsetDateTime datetime2){
        if(datetime1.getOffset().equals(datetime2.getOffset())){
            return datetime1.toLocalDateTime().compareTo(datetime2.toLocalDateTime());
        }
        int cmp=Long.compare(datetime1.toEpochSecond(),datetime2.toEpochSecond());
        if(cmp==0){
            cmp=datetime1.toLocalTime().getNano()-datetime2.toLocalTime().getNano();
        }
        return cmp;
    }

    //-----------------------------------------------------------------------
    public LocalDateTime toLocalDateTime(){
        return dateTime;
    }

    //-----------------------------------------------------------------------
    public boolean isAfter(OffsetDateTime other){
        long thisEpochSec=toEpochSecond();
        long otherEpochSec=other.toEpochSecond();
        return thisEpochSec>otherEpochSec||
                (thisEpochSec==otherEpochSec&&toLocalTime().getNano()>other.toLocalTime().getNano());
    }

    public boolean isBefore(OffsetDateTime other){
        long thisEpochSec=toEpochSecond();
        long otherEpochSec=other.toEpochSecond();
        return thisEpochSec<otherEpochSec||
                (thisEpochSec==otherEpochSec&&toLocalTime().getNano()<other.toLocalTime().getNano());
    }

    public boolean isEqual(OffsetDateTime other){
        return toEpochSecond()==other.toEpochSecond()&&
                toLocalTime().getNano()==other.toLocalTime().getNano();
    }

    @Override
    public int hashCode(){
        return dateTime.hashCode()^offset.hashCode();
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj){
        if(this==obj){
            return true;
        }
        if(obj instanceof OffsetDateTime){
            OffsetDateTime other=(OffsetDateTime)obj;
            return dateTime.equals(other.dateTime)&&offset.equals(other.offset);
        }
        return false;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString(){
        return dateTime.toString()+offset.toString();
    }

    //-----------------------------------------------------------------------
    private Object writeReplace(){
        return new Ser(Ser.OFFSET_DATE_TIME_TYPE,this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException{
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(ObjectOutput out) throws IOException{
        dateTime.writeExternal(out);
        offset.writeExternal(out);
    }
}
