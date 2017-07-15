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

public final class YearMonth
        implements Temporal, TemporalAdjuster, Comparable<YearMonth>, Serializable{
    private static final long serialVersionUID=4183400860270640070L;
    private static final DateTimeFormatter PARSER=new DateTimeFormatterBuilder()
            .appendValue(YEAR,4,10,SignStyle.EXCEEDS_PAD)
            .appendLiteral('-')
            .appendValue(MONTH_OF_YEAR,2)
            .toFormatter();
    private final int year;
    private final int month;

    //-----------------------------------------------------------------------
    private YearMonth(int year,int month){
        this.year=year;
        this.month=month;
    }

    //-----------------------------------------------------------------------
    public static YearMonth now(){
        return now(Clock.systemDefaultZone());
    }

    public static YearMonth now(Clock clock){
        final LocalDate now=LocalDate.now(clock);  // called once
        return YearMonth.of(now.getYear(),now.getMonth());
    }

    //-----------------------------------------------------------------------
    public static YearMonth of(int year,Month month){
        Objects.requireNonNull(month,"month");
        return of(year,month.getValue());
    }

    public static YearMonth of(int year,int month){
        YEAR.checkValidValue(year);
        MONTH_OF_YEAR.checkValidValue(month);
        return new YearMonth(year,month);
    }

    public static YearMonth now(ZoneId zone){
        return now(Clock.system(zone));
    }

    //-----------------------------------------------------------------------
    public static YearMonth parse(CharSequence text){
        return parse(text,PARSER);
    }

    public static YearMonth parse(CharSequence text,DateTimeFormatter formatter){
        Objects.requireNonNull(formatter,"formatter");
        return formatter.parse(text,YearMonth::from);
    }

    static YearMonth readExternal(DataInput in) throws IOException{
        int year=in.readInt();
        byte month=in.readByte();
        return YearMonth.of(year,month);
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isSupported(TemporalField field){
        if(field instanceof ChronoField){
            return field==YEAR||field==MONTH_OF_YEAR||
                    field==PROLEPTIC_MONTH||field==YEAR_OF_ERA||field==ERA;
        }
        return field!=null&&field.isSupportedBy(this);
    }

    //-----------------------------------------------------------------------
    @Override
    public ValueRange range(TemporalField field){
        if(field==YEAR_OF_ERA){
            return (getYear()<=0?ValueRange.of(1,Year.MAX_VALUE+1):ValueRange.of(1,Year.MAX_VALUE));
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
                case MONTH_OF_YEAR:
                    return month;
                case PROLEPTIC_MONTH:
                    return getProlepticMonth();
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

    private long getProlepticMonth(){
        return (year*12L+month-1);
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public <R> R query(TemporalQuery<R> query){
        if(query==TemporalQueries.chronology()){
            return (R)IsoChronology.INSTANCE;
        }else if(query==TemporalQueries.precision()){
            return (R)MONTHS;
        }
        return Temporal.super.query(query);
    }

    //-----------------------------------------------------------------------
    public int getYear(){
        return year;
    }

    @Override
    public boolean isSupported(TemporalUnit unit){
        if(unit instanceof ChronoUnit){
            return unit==MONTHS||unit==YEARS||unit==DECADES||unit==CENTURIES||unit==MILLENNIA||unit==ERAS;
        }
        return unit!=null&&unit.isSupportedBy(this);
    }

    //-----------------------------------------------------------------------
    @Override
    public YearMonth with(TemporalAdjuster adjuster){
        return (YearMonth)adjuster.adjustInto(this);
    }

    @Override
    public YearMonth with(TemporalField field,long newValue){
        if(field instanceof ChronoField){
            ChronoField f=(ChronoField)field;
            f.checkValidValue(newValue);
            switch(f){
                case MONTH_OF_YEAR:
                    return withMonth((int)newValue);
                case PROLEPTIC_MONTH:
                    return plusMonths(newValue-getProlepticMonth());
                case YEAR_OF_ERA:
                    return withYear((int)(year<1?1-newValue:newValue));
                case YEAR:
                    return withYear((int)newValue);
                case ERA:
                    return (getLong(ERA)==newValue?this:withYear(1-year));
            }
            throw new UnsupportedTemporalTypeException("Unsupported field: "+field);
        }
        return field.adjustInto(this,newValue);
    }

    //-----------------------------------------------------------------------
    @Override
    public YearMonth plus(TemporalAmount amountToAdd){
        return (YearMonth)amountToAdd.addTo(this);
    }

    @Override
    public YearMonth plus(long amountToAdd,TemporalUnit unit){
        if(unit instanceof ChronoUnit){
            switch((ChronoUnit)unit){
                case MONTHS:
                    return plusMonths(amountToAdd);
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

    //-----------------------------------------------------------------------
    @Override
    public YearMonth minus(TemporalAmount amountToSubtract){
        return (YearMonth)amountToSubtract.subtractFrom(this);
    }

    @Override
    public YearMonth minus(long amountToSubtract,TemporalUnit unit){
        return (amountToSubtract==Long.MIN_VALUE?plus(Long.MAX_VALUE,unit).plus(1,unit):plus(-amountToSubtract,unit));
    }

    @Override
    public long until(Temporal endExclusive,TemporalUnit unit){
        YearMonth end=YearMonth.from(endExclusive);
        if(unit instanceof ChronoUnit){
            long monthsUntil=end.getProlepticMonth()-getProlepticMonth();  // no overflow
            switch((ChronoUnit)unit){
                case MONTHS:
                    return monthsUntil;
                case YEARS:
                    return monthsUntil/12;
                case DECADES:
                    return monthsUntil/120;
                case CENTURIES:
                    return monthsUntil/1200;
                case MILLENNIA:
                    return monthsUntil/12000;
                case ERAS:
                    return end.getLong(ERA)-getLong(ERA);
            }
            throw new UnsupportedTemporalTypeException("Unsupported unit: "+unit);
        }
        return unit.between(this,end);
    }

    //-----------------------------------------------------------------------
    public static YearMonth from(TemporalAccessor temporal){
        if(temporal instanceof YearMonth){
            return (YearMonth)temporal;
        }
        Objects.requireNonNull(temporal,"temporal");
        try{
            if(IsoChronology.INSTANCE.equals(Chronology.from(temporal))==false){
                temporal=LocalDate.from(temporal);
            }
            return of(temporal.get(YEAR),temporal.get(MONTH_OF_YEAR));
        }catch(DateTimeException ex){
            throw new DateTimeException("Unable to obtain YearMonth from TemporalAccessor: "+
                    temporal+" of type "+temporal.getClass().getName(),ex);
        }
    }

    public int getMonthValue(){
        return month;
    }

    public boolean isValidDay(int dayOfMonth){
        return dayOfMonth>=1&&dayOfMonth<=lengthOfMonth();
    }

    public int lengthOfMonth(){
        return getMonth().length(isLeapYear());
    }

    public Month getMonth(){
        return Month.of(month);
    }

    //-----------------------------------------------------------------------
    public boolean isLeapYear(){
        return IsoChronology.INSTANCE.isLeapYear(year);
    }

    public int lengthOfYear(){
        return (isLeapYear()?366:365);
    }

    //-----------------------------------------------------------------------
    public YearMonth withYear(int year){
        YEAR.checkValidValue(year);
        return with(year,month);
    }

    public YearMonth withMonth(int month){
        MONTH_OF_YEAR.checkValidValue(month);
        return with(year,month);
    }

    public YearMonth minusYears(long yearsToSubtract){
        return (yearsToSubtract==Long.MIN_VALUE?plusYears(Long.MAX_VALUE).plusYears(1):plusYears(-yearsToSubtract));
    }

    public YearMonth plusYears(long yearsToAdd){
        if(yearsToAdd==0){
            return this;
        }
        int newYear=YEAR.checkValidIntValue(year+yearsToAdd);  // safe overflow
        return with(newYear,month);
    }

    private YearMonth with(int newYear,int newMonth){
        if(year==newYear&&month==newMonth){
            return this;
        }
        return new YearMonth(newYear,newMonth);
    }

    public YearMonth minusMonths(long monthsToSubtract){
        return (monthsToSubtract==Long.MIN_VALUE?plusMonths(Long.MAX_VALUE).plusMonths(1):plusMonths(-monthsToSubtract));
    }

    public YearMonth plusMonths(long monthsToAdd){
        if(monthsToAdd==0){
            return this;
        }
        long monthCount=year*12L+(month-1);
        long calcMonths=monthCount+monthsToAdd;  // safe overflow
        int newYear=YEAR.checkValidIntValue(Math.floorDiv(calcMonths,12));
        int newMonth=(int)Math.floorMod(calcMonths,12)+1;
        return with(newYear,newMonth);
    }

    @Override
    public Temporal adjustInto(Temporal temporal){
        if(Chronology.from(temporal).equals(IsoChronology.INSTANCE)==false){
            throw new DateTimeException("Adjustment only supported on ISO date-time");
        }
        return temporal.with(PROLEPTIC_MONTH,getProlepticMonth());
    }

    public String format(DateTimeFormatter formatter){
        Objects.requireNonNull(formatter,"formatter");
        return formatter.format(this);
    }

    //-----------------------------------------------------------------------
    public LocalDate atDay(int dayOfMonth){
        return LocalDate.of(year,month,dayOfMonth);
    }

    public LocalDate atEndOfMonth(){
        return LocalDate.of(year,month,lengthOfMonth());
    }

    public boolean isAfter(YearMonth other){
        return compareTo(other)>0;
    }

    //-----------------------------------------------------------------------
    @Override
    public int compareTo(YearMonth other){
        int cmp=(year-other.year);
        if(cmp==0){
            cmp=(month-other.month);
        }
        return cmp;
    }

    public boolean isBefore(YearMonth other){
        return compareTo(other)<0;
    }

    @Override
    public int hashCode(){
        return year^(month<<27);
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj){
        if(this==obj){
            return true;
        }
        if(obj instanceof YearMonth){
            YearMonth other=(YearMonth)obj;
            return year==other.year&&month==other.month;
        }
        return false;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString(){
        int absYear=Math.abs(year);
        StringBuilder buf=new StringBuilder(9);
        if(absYear<1000){
            if(year<0){
                buf.append(year-10000).deleteCharAt(1);
            }else{
                buf.append(year+10000).deleteCharAt(0);
            }
        }else{
            buf.append(year);
        }
        return buf.append(month<10?"-0":"-")
                .append(month)
                .toString();
    }

    //-----------------------------------------------------------------------
    private Object writeReplace(){
        return new Ser(Ser.YEAR_MONTH_TYPE,this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException{
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(DataOutput out) throws IOException{
        out.writeInt(year);
        out.writeByte(month);
    }
}
