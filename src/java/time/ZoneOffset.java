package java.time;

import java.io.*;
import java.time.temporal.*;
import java.time.zone.ZoneRules;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.time.LocalTime.*;
import static java.time.temporal.ChronoField.OFFSET_SECONDS;

public final class ZoneOffset
        extends ZoneId
        implements TemporalAccessor, TemporalAdjuster, Comparable<ZoneOffset>, Serializable{
    private static final ConcurrentMap<Integer,ZoneOffset> SECONDS_CACHE=new ConcurrentHashMap<>(16,0.75f,4);
    private static final ConcurrentMap<String,ZoneOffset> ID_CACHE=new ConcurrentHashMap<>(16,0.75f,4);
    private static final int MAX_SECONDS=18*SECONDS_PER_HOUR;
    public static final ZoneOffset UTC=ZoneOffset.ofTotalSeconds(0);
    public static final ZoneOffset MIN=ZoneOffset.ofTotalSeconds(-MAX_SECONDS);
    public static final ZoneOffset MAX=ZoneOffset.ofTotalSeconds(MAX_SECONDS);
    private static final long serialVersionUID=2357656521762053153L;
    private final int totalSeconds;
    private final transient String id;

    //-----------------------------------------------------------------------
    private ZoneOffset(int totalSeconds){
        super();
        this.totalSeconds=totalSeconds;
        id=buildId(totalSeconds);
    }

    private static String buildId(int totalSeconds){
        if(totalSeconds==0){
            return "Z";
        }else{
            int absTotalSeconds=Math.abs(totalSeconds);
            StringBuilder buf=new StringBuilder();
            int absHours=absTotalSeconds/SECONDS_PER_HOUR;
            int absMinutes=(absTotalSeconds/SECONDS_PER_MINUTE)%MINUTES_PER_HOUR;
            buf.append(totalSeconds<0?"-":"+")
                    .append(absHours<10?"0":"").append(absHours)
                    .append(absMinutes<10?":0":":").append(absMinutes);
            int absSeconds=absTotalSeconds%SECONDS_PER_MINUTE;
            if(absSeconds!=0){
                buf.append(absSeconds<10?":0":":").append(absSeconds);
            }
            return buf.toString();
        }
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("fallthrough")
    public static ZoneOffset of(String offsetId){
        Objects.requireNonNull(offsetId,"offsetId");
        // "Z" is always in the cache
        ZoneOffset offset=ID_CACHE.get(offsetId);
        if(offset!=null){
            return offset;
        }
        // parse - +h, +hh, +hhmm, +hh:mm, +hhmmss, +hh:mm:ss
        final int hours, minutes, seconds;
        switch(offsetId.length()){
            case 2:
                offsetId=offsetId.charAt(0)+"0"+offsetId.charAt(1);  // fallthru
            case 3:
                hours=parseNumber(offsetId,1,false);
                minutes=0;
                seconds=0;
                break;
            case 5:
                hours=parseNumber(offsetId,1,false);
                minutes=parseNumber(offsetId,3,false);
                seconds=0;
                break;
            case 6:
                hours=parseNumber(offsetId,1,false);
                minutes=parseNumber(offsetId,4,true);
                seconds=0;
                break;
            case 7:
                hours=parseNumber(offsetId,1,false);
                minutes=parseNumber(offsetId,3,false);
                seconds=parseNumber(offsetId,5,false);
                break;
            case 9:
                hours=parseNumber(offsetId,1,false);
                minutes=parseNumber(offsetId,4,true);
                seconds=parseNumber(offsetId,7,true);
                break;
            default:
                throw new DateTimeException("Invalid ID for ZoneOffset, invalid format: "+offsetId);
        }
        char first=offsetId.charAt(0);
        if(first!='+'&&first!='-'){
            throw new DateTimeException("Invalid ID for ZoneOffset, plus/minus not found when expected: "+offsetId);
        }
        if(first=='-'){
            return ofHoursMinutesSeconds(-hours,-minutes,-seconds);
        }else{
            return ofHoursMinutesSeconds(hours,minutes,seconds);
        }
    }

    private static int parseNumber(CharSequence offsetId,int pos,boolean precededByColon){
        if(precededByColon&&offsetId.charAt(pos-1)!=':'){
            throw new DateTimeException("Invalid ID for ZoneOffset, colon not found when expected: "+offsetId);
        }
        char ch1=offsetId.charAt(pos);
        char ch2=offsetId.charAt(pos+1);
        if(ch1<'0'||ch1>'9'||ch2<'0'||ch2>'9'){
            throw new DateTimeException("Invalid ID for ZoneOffset, non numeric characters found: "+offsetId);
        }
        return (ch1-48)*10+(ch2-48);
    }

    public static ZoneOffset ofHoursMinutesSeconds(int hours,int minutes,int seconds){
        validate(hours,minutes,seconds);
        int totalSeconds=totalSeconds(hours,minutes,seconds);
        return ofTotalSeconds(totalSeconds);
    }

    //-----------------------------------------------------------------------
    private static void validate(int hours,int minutes,int seconds){
        if(hours<-18||hours>18){
            throw new DateTimeException("Zone offset hours not in valid range: value "+hours+
                    " is not in the range -18 to 18");
        }
        if(hours>0){
            if(minutes<0||seconds<0){
                throw new DateTimeException("Zone offset minutes and seconds must be positive because hours is positive");
            }
        }else if(hours<0){
            if(minutes>0||seconds>0){
                throw new DateTimeException("Zone offset minutes and seconds must be negative because hours is negative");
            }
        }else if((minutes>0&&seconds<0)||(minutes<0&&seconds>0)){
            throw new DateTimeException("Zone offset minutes and seconds must have the same sign");
        }
        if(Math.abs(minutes)>59){
            throw new DateTimeException("Zone offset minutes not in valid range: abs(value) "+
                    Math.abs(minutes)+" is not in the range 0 to 59");
        }
        if(Math.abs(seconds)>59){
            throw new DateTimeException("Zone offset seconds not in valid range: abs(value) "+
                    Math.abs(seconds)+" is not in the range 0 to 59");
        }
        if(Math.abs(hours)==18&&(Math.abs(minutes)>0||Math.abs(seconds)>0)){
            throw new DateTimeException("Zone offset not in valid range: -18:00 to +18:00");
        }
    }

    private static int totalSeconds(int hours,int minutes,int seconds){
        return hours*SECONDS_PER_HOUR+minutes*SECONDS_PER_MINUTE+seconds;
    }

    //-----------------------------------------------------------------------
    public static ZoneOffset ofTotalSeconds(int totalSeconds){
        if(Math.abs(totalSeconds)>MAX_SECONDS){
            throw new DateTimeException("Zone offset not in valid range: -18:00 to +18:00");
        }
        if(totalSeconds%(15*SECONDS_PER_MINUTE)==0){
            Integer totalSecs=totalSeconds;
            ZoneOffset result=SECONDS_CACHE.get(totalSecs);
            if(result==null){
                result=new ZoneOffset(totalSeconds);
                SECONDS_CACHE.putIfAbsent(totalSecs,result);
                result=SECONDS_CACHE.get(totalSecs);
                ID_CACHE.putIfAbsent(result.getId(),result);
            }
            return result;
        }else{
            return new ZoneOffset(totalSeconds);
        }
    }

    //-----------------------------------------------------------------------
    public static ZoneOffset ofHours(int hours){
        return ofHoursMinutesSeconds(hours,0,0);
    }

    public static ZoneOffset ofHoursMinutes(int hours,int minutes){
        return ofHoursMinutesSeconds(hours,minutes,0);
    }

    //-----------------------------------------------------------------------
    public static ZoneOffset from(TemporalAccessor temporal){
        Objects.requireNonNull(temporal,"temporal");
        ZoneOffset offset=temporal.query(TemporalQueries.offset());
        if(offset==null){
            throw new DateTimeException("Unable to obtain ZoneOffset from TemporalAccessor: "+
                    temporal+" of type "+temporal.getClass().getName());
        }
        return offset;
    }

    static ZoneOffset readExternal(DataInput in) throws IOException{
        int offsetByte=in.readByte();
        return (offsetByte==127?ZoneOffset.ofTotalSeconds(in.readInt()):ZoneOffset.ofTotalSeconds(offsetByte*900));
    }

    //-----------------------------------------------------------------------
    public int getTotalSeconds(){
        return totalSeconds;
    }

    @Override
    public String getId(){
        return id;
    }

    @Override
    public ZoneRules getRules(){
        return ZoneRules.of(this);
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj){
        if(this==obj){
            return true;
        }
        if(obj instanceof ZoneOffset){
            return totalSeconds==((ZoneOffset)obj).totalSeconds;
        }
        return false;
    }

    @Override
    public int hashCode(){
        return totalSeconds;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString(){
        return id;
    }

    @Override
    void write(DataOutput out) throws IOException{
        out.writeByte(Ser.ZONE_OFFSET_TYPE);
        writeExternal(out);
    }

    void writeExternal(DataOutput out) throws IOException{
        final int offsetSecs=totalSeconds;
        int offsetByte=offsetSecs%900==0?offsetSecs/900:127;  // compress to -72 to +72
        out.writeByte(offsetByte);
        if(offsetByte==127){
            out.writeInt(offsetSecs);
        }
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isSupported(TemporalField field){
        if(field instanceof ChronoField){
            return field==OFFSET_SECONDS;
        }
        return field!=null&&field.isSupportedBy(this);
    }

    @Override  // override for Javadoc
    public ValueRange range(TemporalField field){
        return TemporalAccessor.super.range(field);
    }

    @Override  // override for Javadoc and performance
    public int get(TemporalField field){
        if(field==OFFSET_SECONDS){
            return totalSeconds;
        }else if(field instanceof ChronoField){
            throw new UnsupportedTemporalTypeException("Unsupported field: "+field);
        }
        return range(field).checkValidIntValue(getLong(field),field);
    }

    @Override
    public long getLong(TemporalField field){
        if(field==OFFSET_SECONDS){
            return totalSeconds;
        }else if(field instanceof ChronoField){
            throw new UnsupportedTemporalTypeException("Unsupported field: "+field);
        }
        return field.getFrom(this);
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public <R> R query(TemporalQuery<R> query){
        if(query==TemporalQueries.offset()||query==TemporalQueries.zone()){
            return (R)this;
        }
        return TemporalAccessor.super.query(query);
    }

    @Override
    public Temporal adjustInto(Temporal temporal){
        return temporal.with(OFFSET_SECONDS,totalSeconds);
    }

    //-----------------------------------------------------------------------
    @Override
    public int compareTo(ZoneOffset other){
        return other.totalSeconds-totalSeconds;
    }

    // -----------------------------------------------------------------------
    private Object writeReplace(){
        return new Ser(Ser.ZONE_OFFSET_TYPE,this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException{
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }
}
