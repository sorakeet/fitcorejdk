package java.time;

import java.io.*;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.*;
import java.util.Objects;

import static java.time.temporal.ChronoField.*;
import static java.time.temporal.ChronoUnit.*;

public final class Year
        implements Temporal, TemporalAdjuster, Comparable<Year>, Serializable{
    public static final int MIN_VALUE=-999_999_999;
    public static final int MAX_VALUE=999_999_999;
    private static final long serialVersionUID=-23038383694477807L;
    private static final DateTimeFormatter PARSER=new DateTimeFormatterBuilder()
            .appendValue(YEAR,4,10,SignStyle.EXCEEDS_PAD)
            .toFormatter();
    private final int year;

    private Year(int year){
        this.year=year;
    }

    /**
     * �����Ƕ���API���������õ�ǰ���
     * */
    public static Year now(){
        return now(Clock.systemDefaultZone());
    }
    /**
     * �����Ƕ���API�������� Clock ����
     * */
    public static Year now(Clock clock){
        final LocalDate now=LocalDate.now(clock);  // called once
        return Year.of(now.getYear());
    }

    public static Year of(int isoYear){
        YEAR.checkValidValue(isoYear);
        return new Year(isoYear);
    }

    public static Year now(ZoneId zone){
        return now(Clock.system(zone));
    }

    public static Year parse(CharSequence text){
        return parse(text,PARSER);
    }

    public static Year parse(CharSequence text,DateTimeFormatter formatter){
        Objects.requireNonNull(formatter,"formatter");
        return formatter.parse(text,Year::from);
    }

    static Year readExternal(DataInput in) throws IOException{
        return Year.of(in.readInt());
    }

    //-----------------------------------------------------------------------
    public int getValue(){
        return year;
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isSupported(TemporalField field){
        if(field instanceof ChronoField){
            return field==YEAR||field==YEAR_OF_ERA||field==ERA;
        }
        return field!=null&&field.isSupportedBy(this);
    }

    //-----------------------------------------------------------------------
    @Override
    public ValueRange range(TemporalField field){
        if(field==YEAR_OF_ERA){
            return (year<=0?ValueRange.of(1,MAX_VALUE+1):ValueRange.of(1,MAX_VALUE));
        }
        return Temporal.super.range(field);
    }

    @Override  // override for Javadoc
    public int get(TemporalField field){
        return range(field).checkValidIntValue(getLong(field),field);
    }

    @Override
    public long getLong(TemporalField field){
        if(field instanceof ChronoField){
            switch((ChronoField)field){
                case YEAR_OF_ERA:
                    return (year<1?1-year:year);
                case YEAR:
                    return year;
                case ERA:
                    return (year<1?0:1);
            }
            throw new UnsupportedTemporalTypeException("Unsupported field: "+field);
        }
        return field.getFrom(this);
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public <R> R query(TemporalQuery<R> query){
        if(query==TemporalQueries.chronology()){
            return (R)IsoChronology.INSTANCE;
        }else if(query==TemporalQueries.precision()){
            return (R)YEARS;
        }
        return Temporal.super.query(query);
    }

    @Override
    public boolean isSupported(TemporalUnit unit){
        if(unit instanceof ChronoUnit){
            return unit==YEARS||unit==DECADES||unit==CENTURIES||unit==MILLENNIA||unit==ERAS;
        }
        return unit!=null&&unit.isSupportedBy(this);
    }

    //-----------------------------------------------------------------------
    @Override
    public Year with(TemporalAdjuster adjuster){
        return (Year)adjuster.adjustInto(this);
    }

    @Override
    public Year with(TemporalField field,long newValue){
        if(field instanceof ChronoField){
            ChronoField f=(ChronoField)field;
            f.checkValidValue(newValue);
            switch(f){
                case YEAR_OF_ERA:
                    return Year.of((int)(year<1?1-newValue:newValue));
                case YEAR:
                    return Year.of((int)newValue);
                case ERA:
                    return (getLong(ERA)==newValue?this:Year.of(1-year));
            }
            throw new UnsupportedTemporalTypeException("Unsupported field: "+field);
        }
        return field.adjustInto(this,newValue);
    }

    //-----------------------------------------------------------------------
    @Override
    public Year plus(TemporalAmount amountToAdd){
        return (Year)amountToAdd.addTo(this);
    }

    @Override
    public Year plus(long amountToAdd,TemporalUnit unit){
        if(unit instanceof ChronoUnit){
            switch((ChronoUnit)unit){
                case YEARS:
                    return plusYears(amountToAdd);
                case DECADES:
                    return plusYears(Math.multiplyExact(amountToAdd,10));
                case CENTURIES:
                    return plusYears(Math.multiplyExact(amountToAdd,100));
                case MILLENNIA:
                    return plusYears(Math.multiplyExact(amountToAdd,1000));
                case ERAS:
                    return with(ERA,Math.addExact(getLong(ERA),amountToAdd));
            }
            throw new UnsupportedTemporalTypeException("Unsupported unit: "+unit);
        }
        return unit.addTo(this,amountToAdd);
    }

    public Year plusYears(long yearsToAdd){
        if(yearsToAdd==0){
            return this;
        }
        return of(YEAR.checkValidIntValue(year+yearsToAdd));  // overflow safe
    }

    //-----------------------------------------------------------------------
    @Override
    public Year minus(TemporalAmount amountToSubtract){
        return (Year)amountToSubtract.subtractFrom(this);
    }

    @Override
    public Year minus(long amountToSubtract,TemporalUnit unit){
        return (amountToSubtract==Long.MIN_VALUE?plus(Long.MAX_VALUE,unit).plus(1,unit):plus(-amountToSubtract,unit));
    }

    @Override
    public long until(Temporal endExclusive,TemporalUnit unit){
        Year end=Year.from(endExclusive);
        if(unit instanceof ChronoUnit){
            long yearsUntil=((long)end.year)-year;  // no overflow
            switch((ChronoUnit)unit){
                case YEARS:
                    return yearsUntil;
                case DECADES:
                    return yearsUntil/10;
                case CENTURIES:
                    return yearsUntil/100;
                case MILLENNIA:
                    return yearsUntil/1000;
                case ERAS:
                    return end.getLong(ERA)-getLong(ERA);
            }
            throw new UnsupportedTemporalTypeException("Unsupported unit: "+unit);
        }
        return unit.between(this,end);
    }

    //-----------------------------------------------------------------------
    public static Year from(TemporalAccessor temporal){
        if(temporal instanceof Year){
            return (Year)temporal;
        }
        Objects.requireNonNull(temporal,"temporal");
        try{
            if(IsoChronology.INSTANCE.equals(Chronology.from(temporal))==false){
                temporal=LocalDate.from(temporal);
            }
            return of(temporal.get(YEAR));
        }catch(DateTimeException ex){
            throw new DateTimeException("Unable to obtain Year from TemporalAccessor: "+
                    temporal+" of type "+temporal.getClass().getName(),ex);
        }
    }

    public boolean isValidMonthDay(MonthDay monthDay){
        return monthDay!=null&&monthDay.isValidYear(year);
    }

    public int length(){
        return isLeap()?366:365;
    }

    //-----------------------------------------------------------------------
    public boolean isLeap(){
        return Year.isLeap(year);
    }

    //-------------------------------------------------------------------------
    public static boolean isLeap(long year){
        return ((year&3)==0)&&((year%100)!=0||(year%400)==0);
    }

    public Year minusYears(long yearsToSubtract){
        return (yearsToSubtract==Long.MIN_VALUE?plusYears(Long.MAX_VALUE).plusYears(1):plusYears(-yearsToSubtract));
    }

    @Override
    public Temporal adjustInto(Temporal temporal){
        if(Chronology.from(temporal).equals(IsoChronology.INSTANCE)==false){
            throw new DateTimeException("Adjustment only supported on ISO date-time");
        }
        return temporal.with(YEAR,year);
    }

    public String format(DateTimeFormatter formatter){
        Objects.requireNonNull(formatter,"formatter");
        return formatter.format(this);
    }

    //-----------------------------------------------------------------------
    public LocalDate atDay(int dayOfYear){
        return LocalDate.ofYearDay(year,dayOfYear);
    }

    public YearMonth atMonth(Month month){
        return YearMonth.of(year,month);
    }

    public YearMonth atMonth(int month){
        return YearMonth.of(year,month);
    }

    public LocalDate atMonthDay(MonthDay monthDay){
        return monthDay.atYear(year);
    }

    //-----------------------------------------------------------------------
    @Override
    public int compareTo(Year other){
        return year-other.year;
    }

    public boolean isAfter(Year other){
        return year>other.year;
    }

    public boolean isBefore(Year other){
        return year<other.year;
    }

    @Override
    public int hashCode(){
        return year;
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj){
        if(this==obj){
            return true;
        }
        if(obj instanceof Year){
            return year==((Year)obj).year;
        }
        return false;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString(){
        return Integer.toString(year);
    }

    //-----------------------------------------------------------------------
    private Object writeReplace(){
        return new Ser(Ser.YEAR_TYPE,this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException{
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(DataOutput out) throws IOException{
        out.writeInt(year);
    }
}
