package java.time.format;

import java.io.IOException;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.time.DateTimeException;
import java.time.Period;
import java.time.ZoneId;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatterBuilder.CompositePrinterParser;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQuery;
import java.util.*;

import static java.time.temporal.ChronoField.*;

public final class DateTimeFormatter{
    public static final DateTimeFormatter ISO_LOCAL_DATE;
    public static final DateTimeFormatter ISO_OFFSET_DATE;
    public static final DateTimeFormatter ISO_DATE;
    public static final DateTimeFormatter ISO_LOCAL_TIME;
    public static final DateTimeFormatter ISO_OFFSET_TIME;
    public static final DateTimeFormatter ISO_TIME;
    public static final DateTimeFormatter ISO_LOCAL_DATE_TIME;
    public static final DateTimeFormatter ISO_OFFSET_DATE_TIME;
    public static final DateTimeFormatter ISO_ZONED_DATE_TIME;
    public static final DateTimeFormatter ISO_DATE_TIME;
    public static final DateTimeFormatter ISO_ORDINAL_DATE;
    public static final DateTimeFormatter ISO_WEEK_DATE;
    public static final DateTimeFormatter ISO_INSTANT;
    public static final DateTimeFormatter BASIC_ISO_DATE;
    public static final DateTimeFormatter RFC_1123_DATE_TIME;
    private static final TemporalQuery<Period> PARSED_EXCESS_DAYS=t->{
        if(t instanceof Parsed){
            return ((Parsed)t).excessDays;
        }else{
            return Period.ZERO;
        }
    };
    private static final TemporalQuery<Boolean> PARSED_LEAP_SECOND=t->{
        if(t instanceof Parsed){
            return ((Parsed)t).leapSecond;
        }else{
            return Boolean.FALSE;
        }
    };

    static{
        ISO_LOCAL_DATE=new DateTimeFormatterBuilder()
                .appendValue(YEAR,4,10,SignStyle.EXCEEDS_PAD)
                .appendLiteral('-')
                .appendValue(MONTH_OF_YEAR,2)
                .appendLiteral('-')
                .appendValue(DAY_OF_MONTH,2)
                .toFormatter(ResolverStyle.STRICT,IsoChronology.INSTANCE);
    }

    static{
        ISO_OFFSET_DATE=new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .append(ISO_LOCAL_DATE)
                .appendOffsetId()
                .toFormatter(ResolverStyle.STRICT,IsoChronology.INSTANCE);
    }

    static{
        ISO_DATE=new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .append(ISO_LOCAL_DATE)
                .optionalStart()
                .appendOffsetId()
                .toFormatter(ResolverStyle.STRICT,IsoChronology.INSTANCE);
    }

    static{
        ISO_LOCAL_TIME=new DateTimeFormatterBuilder()
                .appendValue(HOUR_OF_DAY,2)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR,2)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(SECOND_OF_MINUTE,2)
                .optionalStart()
                .appendFraction(NANO_OF_SECOND,0,9,true)
                .toFormatter(ResolverStyle.STRICT,null);
    }

    static{
        ISO_OFFSET_TIME=new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .append(ISO_LOCAL_TIME)
                .appendOffsetId()
                .toFormatter(ResolverStyle.STRICT,null);
    }

    static{
        ISO_TIME=new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .append(ISO_LOCAL_TIME)
                .optionalStart()
                .appendOffsetId()
                .toFormatter(ResolverStyle.STRICT,null);
    }

    static{
        ISO_LOCAL_DATE_TIME=new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .append(ISO_LOCAL_DATE)
                .appendLiteral('T')
                .append(ISO_LOCAL_TIME)
                .toFormatter(ResolverStyle.STRICT,IsoChronology.INSTANCE);
    }

    static{
        ISO_OFFSET_DATE_TIME=new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .append(ISO_LOCAL_DATE_TIME)
                .appendOffsetId()
                .toFormatter(ResolverStyle.STRICT,IsoChronology.INSTANCE);
    }

    static{
        ISO_ZONED_DATE_TIME=new DateTimeFormatterBuilder()
                .append(ISO_OFFSET_DATE_TIME)
                .optionalStart()
                .appendLiteral('[')
                .parseCaseSensitive()
                .appendZoneRegionId()
                .appendLiteral(']')
                .toFormatter(ResolverStyle.STRICT,IsoChronology.INSTANCE);
    }

    static{
        ISO_DATE_TIME=new DateTimeFormatterBuilder()
                .append(ISO_LOCAL_DATE_TIME)
                .optionalStart()
                .appendOffsetId()
                .optionalStart()
                .appendLiteral('[')
                .parseCaseSensitive()
                .appendZoneRegionId()
                .appendLiteral(']')
                .toFormatter(ResolverStyle.STRICT,IsoChronology.INSTANCE);
    }

    static{
        ISO_ORDINAL_DATE=new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendValue(YEAR,4,10,SignStyle.EXCEEDS_PAD)
                .appendLiteral('-')
                .appendValue(DAY_OF_YEAR,3)
                .optionalStart()
                .appendOffsetId()
                .toFormatter(ResolverStyle.STRICT,IsoChronology.INSTANCE);
    }

    static{
        ISO_WEEK_DATE=new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendValue(IsoFields.WEEK_BASED_YEAR,4,10,SignStyle.EXCEEDS_PAD)
                .appendLiteral("-W")
                .appendValue(IsoFields.WEEK_OF_WEEK_BASED_YEAR,2)
                .appendLiteral('-')
                .appendValue(DAY_OF_WEEK,1)
                .optionalStart()
                .appendOffsetId()
                .toFormatter(ResolverStyle.STRICT,IsoChronology.INSTANCE);
    }

    static{
        ISO_INSTANT=new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendInstant()
                .toFormatter(ResolverStyle.STRICT,null);
    }

    static{
        BASIC_ISO_DATE=new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendValue(YEAR,4)
                .appendValue(MONTH_OF_YEAR,2)
                .appendValue(DAY_OF_MONTH,2)
                .optionalStart()
                .appendOffset("+HHMMss","Z")
                .toFormatter(ResolverStyle.STRICT,IsoChronology.INSTANCE);
    }

    static{
        // manually code maps to ensure correct data always used
        // (locale data can be changed by application code)
        Map<Long,String> dow=new HashMap<>();
        dow.put(1L,"Mon");
        dow.put(2L,"Tue");
        dow.put(3L,"Wed");
        dow.put(4L,"Thu");
        dow.put(5L,"Fri");
        dow.put(6L,"Sat");
        dow.put(7L,"Sun");
        Map<Long,String> moy=new HashMap<>();
        moy.put(1L,"Jan");
        moy.put(2L,"Feb");
        moy.put(3L,"Mar");
        moy.put(4L,"Apr");
        moy.put(5L,"May");
        moy.put(6L,"Jun");
        moy.put(7L,"Jul");
        moy.put(8L,"Aug");
        moy.put(9L,"Sep");
        moy.put(10L,"Oct");
        moy.put(11L,"Nov");
        moy.put(12L,"Dec");
        RFC_1123_DATE_TIME=new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .parseLenient()
                .optionalStart()
                .appendText(DAY_OF_WEEK,dow)
                .appendLiteral(", ")
                .optionalEnd()
                .appendValue(DAY_OF_MONTH,1,2,SignStyle.NOT_NEGATIVE)
                .appendLiteral(' ')
                .appendText(MONTH_OF_YEAR,moy)
                .appendLiteral(' ')
                .appendValue(YEAR,4)  // 2 digit year not handled
                .appendLiteral(' ')
                .appendValue(HOUR_OF_DAY,2)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR,2)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(SECOND_OF_MINUTE,2)
                .optionalEnd()
                .appendLiteral(' ')
                .appendOffset("+HHMM","GMT")  // should handle UT/Z/EST/EDT/CST/CDT/MST/MDT/PST/MDT
                .toFormatter(ResolverStyle.SMART,IsoChronology.INSTANCE);
    }

    private final CompositePrinterParser printerParser;
    private final Locale locale;
    private final DecimalStyle decimalStyle;
    private final ResolverStyle resolverStyle;
    private final Set<TemporalField> resolverFields;
    private final Chronology chrono;
    private final ZoneId zone;

    //-----------------------------------------------------------------------
    DateTimeFormatter(CompositePrinterParser printerParser,
                      Locale locale,DecimalStyle decimalStyle,
                      ResolverStyle resolverStyle,Set<TemporalField> resolverFields,
                      Chronology chrono,ZoneId zone){
        this.printerParser=Objects.requireNonNull(printerParser,"printerParser");
        this.resolverFields=resolverFields;
        this.locale=Objects.requireNonNull(locale,"locale");
        this.decimalStyle=Objects.requireNonNull(decimalStyle,"decimalStyle");
        this.resolverStyle=Objects.requireNonNull(resolverStyle,"resolverStyle");
        this.chrono=chrono;
        this.zone=zone;
    }

    public static DateTimeFormatter ofPattern(String pattern){
        return new DateTimeFormatterBuilder().appendPattern(pattern).toFormatter();
    }

    public static DateTimeFormatter ofPattern(String pattern,Locale locale){
        return new DateTimeFormatterBuilder().appendPattern(pattern).toFormatter(locale);
    }

    //-----------------------------------------------------------------------
    public static DateTimeFormatter ofLocalizedDate(FormatStyle dateStyle){
        Objects.requireNonNull(dateStyle,"dateStyle");
        return new DateTimeFormatterBuilder().appendLocalized(dateStyle,null)
                .toFormatter(ResolverStyle.SMART,IsoChronology.INSTANCE);
    }

    public static DateTimeFormatter ofLocalizedTime(FormatStyle timeStyle){
        Objects.requireNonNull(timeStyle,"timeStyle");
        return new DateTimeFormatterBuilder().appendLocalized(null,timeStyle)
                .toFormatter(ResolverStyle.SMART,IsoChronology.INSTANCE);
    }

    public static DateTimeFormatter ofLocalizedDateTime(FormatStyle dateTimeStyle){
        Objects.requireNonNull(dateTimeStyle,"dateTimeStyle");
        return new DateTimeFormatterBuilder().appendLocalized(dateTimeStyle,dateTimeStyle)
                .toFormatter(ResolverStyle.SMART,IsoChronology.INSTANCE);
    }

    public static DateTimeFormatter ofLocalizedDateTime(FormatStyle dateStyle,FormatStyle timeStyle){
        Objects.requireNonNull(dateStyle,"dateStyle");
        Objects.requireNonNull(timeStyle,"timeStyle");
        return new DateTimeFormatterBuilder().appendLocalized(dateStyle,timeStyle)
                .toFormatter(ResolverStyle.SMART,IsoChronology.INSTANCE);
    }

    //-----------------------------------------------------------------------
    public static final TemporalQuery<Period> parsedExcessDays(){
        return PARSED_EXCESS_DAYS;
    }

    public static final TemporalQuery<Boolean> parsedLeapSecond(){
        return PARSED_LEAP_SECOND;
    }

    //-----------------------------------------------------------------------
    public Locale getLocale(){
        return locale;
    }

    public DateTimeFormatter withLocale(Locale locale){
        if(this.locale.equals(locale)){
            return this;
        }
        return new DateTimeFormatter(printerParser,locale,decimalStyle,resolverStyle,resolverFields,chrono,zone);
    }

    //-----------------------------------------------------------------------
    public DecimalStyle getDecimalStyle(){
        return decimalStyle;
    }

    public DateTimeFormatter withDecimalStyle(DecimalStyle decimalStyle){
        if(this.decimalStyle.equals(decimalStyle)){
            return this;
        }
        return new DateTimeFormatter(printerParser,locale,decimalStyle,resolverStyle,resolverFields,chrono,zone);
    }

    //-----------------------------------------------------------------------
    public Chronology getChronology(){
        return chrono;
    }

    public DateTimeFormatter withChronology(Chronology chrono){
        if(Objects.equals(this.chrono,chrono)){
            return this;
        }
        return new DateTimeFormatter(printerParser,locale,decimalStyle,resolverStyle,resolverFields,chrono,zone);
    }

    //-----------------------------------------------------------------------
    public ZoneId getZone(){
        return zone;
    }

    public DateTimeFormatter withZone(ZoneId zone){
        if(Objects.equals(this.zone,zone)){
            return this;
        }
        return new DateTimeFormatter(printerParser,locale,decimalStyle,resolverStyle,resolverFields,chrono,zone);
    }

    //-----------------------------------------------------------------------
    public ResolverStyle getResolverStyle(){
        return resolverStyle;
    }

    public DateTimeFormatter withResolverStyle(ResolverStyle resolverStyle){
        Objects.requireNonNull(resolverStyle,"resolverStyle");
        if(Objects.equals(this.resolverStyle,resolverStyle)){
            return this;
        }
        return new DateTimeFormatter(printerParser,locale,decimalStyle,resolverStyle,resolverFields,chrono,zone);
    }

    //-----------------------------------------------------------------------
    public Set<TemporalField> getResolverFields(){
        return resolverFields;
    }

    public DateTimeFormatter withResolverFields(TemporalField... resolverFields){
        Set<TemporalField> fields=null;
        if(resolverFields!=null){
            fields=Collections.unmodifiableSet(new HashSet<>(Arrays.asList(resolverFields)));
        }
        if(Objects.equals(this.resolverFields,fields)){
            return this;
        }
        return new DateTimeFormatter(printerParser,locale,decimalStyle,resolverStyle,fields,chrono,zone);
    }

    public DateTimeFormatter withResolverFields(Set<TemporalField> resolverFields){
        if(Objects.equals(this.resolverFields,resolverFields)){
            return this;
        }
        if(resolverFields!=null){
            resolverFields=Collections.unmodifiableSet(new HashSet<>(resolverFields));
        }
        return new DateTimeFormatter(printerParser,locale,decimalStyle,resolverStyle,resolverFields,chrono,zone);
    }

    //-----------------------------------------------------------------------
    public String format(TemporalAccessor temporal){
        StringBuilder buf=new StringBuilder(32);
        formatTo(temporal,buf);
        return buf.toString();
    }

    //-----------------------------------------------------------------------
    public void formatTo(TemporalAccessor temporal,Appendable appendable){
        Objects.requireNonNull(temporal,"temporal");
        Objects.requireNonNull(appendable,"appendable");
        try{
            DateTimePrintContext context=new DateTimePrintContext(temporal,this);
            if(appendable instanceof StringBuilder){
                printerParser.format(context,(StringBuilder)appendable);
            }else{
                // buffer output to avoid writing to appendable in case of error
                StringBuilder buf=new StringBuilder(32);
                printerParser.format(context,buf);
                appendable.append(buf);
            }
        }catch(IOException ex){
            throw new DateTimeException(ex.getMessage(),ex);
        }
    }

    //-----------------------------------------------------------------------
    public TemporalAccessor parse(CharSequence text){
        Objects.requireNonNull(text,"text");
        try{
            return parseResolved0(text,null);
        }catch(DateTimeParseException ex){
            throw ex;
        }catch(RuntimeException ex){
            throw createError(text,ex);
        }
    }

    private DateTimeParseException createError(CharSequence text,RuntimeException ex){
        String abbr;
        if(text.length()>64){
            abbr=text.subSequence(0,64).toString()+"...";
        }else{
            abbr=text.toString();
        }
        return new DateTimeParseException("Text '"+abbr+"' could not be parsed: "+ex.getMessage(),text,0,ex);
    }

    //-----------------------------------------------------------------------
    private TemporalAccessor parseResolved0(final CharSequence text,final ParsePosition position){
        ParsePosition pos=(position!=null?position:new ParsePosition(0));
        DateTimeParseContext context=parseUnresolved0(text,pos);
        if(context==null||pos.getErrorIndex()>=0||(position==null&&pos.getIndex()<text.length())){
            String abbr;
            if(text.length()>64){
                abbr=text.subSequence(0,64).toString()+"...";
            }else{
                abbr=text.toString();
            }
            if(pos.getErrorIndex()>=0){
                throw new DateTimeParseException("Text '"+abbr+"' could not be parsed at index "+
                        pos.getErrorIndex(),text,pos.getErrorIndex());
            }else{
                throw new DateTimeParseException("Text '"+abbr+"' could not be parsed, unparsed text found at index "+
                        pos.getIndex(),text,pos.getIndex());
            }
        }
        return context.toResolved(resolverStyle,resolverFields);
    }

    private DateTimeParseContext parseUnresolved0(CharSequence text,ParsePosition position){
        Objects.requireNonNull(text,"text");
        Objects.requireNonNull(position,"position");
        DateTimeParseContext context=new DateTimeParseContext(this);
        int pos=position.getIndex();
        pos=printerParser.parse(context,text,pos);
        if(pos<0){
            position.setErrorIndex(~pos);  // index not updated from input
            return null;
        }
        position.setIndex(pos);  // errorIndex not updated from input
        return context;
    }

    public TemporalAccessor parse(CharSequence text,ParsePosition position){
        Objects.requireNonNull(text,"text");
        Objects.requireNonNull(position,"position");
        try{
            return parseResolved0(text,position);
        }catch(DateTimeParseException|IndexOutOfBoundsException ex){
            throw ex;
        }catch(RuntimeException ex){
            throw createError(text,ex);
        }
    }

    //-----------------------------------------------------------------------
    public <T> T parse(CharSequence text,TemporalQuery<T> query){
        Objects.requireNonNull(text,"text");
        Objects.requireNonNull(query,"query");
        try{
            return parseResolved0(text,null).query(query);
        }catch(DateTimeParseException ex){
            throw ex;
        }catch(RuntimeException ex){
            throw createError(text,ex);
        }
    }

    public TemporalAccessor parseBest(CharSequence text,TemporalQuery<?>... queries){
        Objects.requireNonNull(text,"text");
        Objects.requireNonNull(queries,"queries");
        if(queries.length<2){
            throw new IllegalArgumentException("At least two queries must be specified");
        }
        try{
            TemporalAccessor resolved=parseResolved0(text,null);
            for(TemporalQuery<?> query : queries){
                try{
                    return (TemporalAccessor)resolved.query(query);
                }catch(RuntimeException ex){
                    // continue
                }
            }
            throw new DateTimeException("Unable to convert parsed text using any of the specified queries");
        }catch(DateTimeParseException ex){
            throw ex;
        }catch(RuntimeException ex){
            throw createError(text,ex);
        }
    }

    public TemporalAccessor parseUnresolved(CharSequence text,ParsePosition position){
        DateTimeParseContext context=parseUnresolved0(text,position);
        if(context==null){
            return null;
        }
        return context.toUnresolved();
    }

    //-----------------------------------------------------------------------
    CompositePrinterParser toPrinterParser(boolean optional){
        return printerParser.withOptional(optional);
    }

    public Format toFormat(){
        return new ClassicFormat(this,null);
    }

    public Format toFormat(TemporalQuery<?> parseQuery){
        Objects.requireNonNull(parseQuery,"parseQuery");
        return new ClassicFormat(this,parseQuery);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString(){
        String pattern=printerParser.toString();
        pattern=pattern.startsWith("[")?pattern:pattern.substring(1,pattern.length()-1);
        return pattern;
        // TODO: Fix tests to not depend on toString()
//        return "DateTimeFormatter[" + locale +
//                (chrono != null ? "," + chrono : "") +
//                (zone != null ? "," + zone : "") +
//                pattern + "]";
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("serial")  // not actually serializable
    static class ClassicFormat extends Format{
        private final DateTimeFormatter formatter;
        private final TemporalQuery<?> parseType;

        public ClassicFormat(DateTimeFormatter formatter,TemporalQuery<?> parseType){
            this.formatter=formatter;
            this.parseType=parseType;
        }

        @Override
        public StringBuffer format(Object obj,StringBuffer toAppendTo,FieldPosition pos){
            Objects.requireNonNull(obj,"obj");
            Objects.requireNonNull(toAppendTo,"toAppendTo");
            Objects.requireNonNull(pos,"pos");
            if(obj instanceof TemporalAccessor==false){
                throw new IllegalArgumentException("Format target must implement TemporalAccessor");
            }
            pos.setBeginIndex(0);
            pos.setEndIndex(0);
            try{
                formatter.formatTo((TemporalAccessor)obj,toAppendTo);
            }catch(RuntimeException ex){
                throw new IllegalArgumentException(ex.getMessage(),ex);
            }
            return toAppendTo;
        }

        @Override
        public Object parseObject(String text,ParsePosition pos){
            Objects.requireNonNull(text,"text");
            DateTimeParseContext context;
            try{
                context=formatter.parseUnresolved0(text,pos);
            }catch(IndexOutOfBoundsException ex){
                if(pos.getErrorIndex()<0){
                    pos.setErrorIndex(0);
                }
                return null;
            }
            if(context==null){
                if(pos.getErrorIndex()<0){
                    pos.setErrorIndex(0);
                }
                return null;
            }
            try{
                TemporalAccessor resolved=context.toResolved(formatter.resolverStyle,formatter.resolverFields);
                if(parseType==null){
                    return resolved;
                }
                return resolved.query(parseType);
            }catch(RuntimeException ex){
                pos.setErrorIndex(0);
                return null;
            }
        }

        @Override
        public Object parseObject(String text) throws ParseException{
            Objects.requireNonNull(text,"text");
            try{
                if(parseType==null){
                    return formatter.parseResolved0(text,null);
                }
                return formatter.parse(text,parseType);
            }catch(DateTimeParseException ex){
                throw new ParseException(ex.getMessage(),ex.getErrorIndex());
            }catch(RuntimeException ex){
                throw (ParseException)new ParseException(ex.getMessage(),0).initCause(ex);
            }
        }
    }
}
