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
 * Copyright (c) 2008-2012, Stephen Colebourne & Michael Nascimento Santos
 * <p>
 * All rights hg qreserved.
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
 * Copyright (c) 2008-2012, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights hg qreserved.
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
package java.time.format;

import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleResources;
import sun.util.locale.provider.TimeZoneNameUtility;

import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.ParsePosition;
import java.time.*;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeTextProvider.LocaleStore;
import java.time.temporal.*;
import java.time.zone.ZoneRulesProvider;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.time.temporal.ChronoField.*;

public final class DateTimeFormatterBuilder{
    //-------------------------------------------------------------------------
    static final Comparator<String> LENGTH_SORT=new Comparator<String>(){
        @Override
        public int compare(String str1,String str2){
            return str1.length()==str2.length()?str1.compareTo(str2):str1.length()-str2.length();
        }
    };
    private static final TemporalQuery<ZoneId> QUERY_REGION_ONLY=(temporal)->{
        ZoneId zone=temporal.query(TemporalQueries.zoneId());
        return (zone!=null&&zone instanceof ZoneOffset==false?zone:null);
    };
    private static final Map<Character,TemporalField> FIELD_MAP=new HashMap<>();

    static{
        // SDF = SimpleDateFormat
        FIELD_MAP.put('G',ChronoField.ERA);                       // SDF, LDML (different to both for 1/2 chars)
        FIELD_MAP.put('y',ChronoField.YEAR_OF_ERA);               // SDF, LDML
        FIELD_MAP.put('u',ChronoField.YEAR);                      // LDML (different in SDF)
        FIELD_MAP.put('Q',IsoFields.QUARTER_OF_YEAR);             // LDML (removed quarter from 310)
        FIELD_MAP.put('q',IsoFields.QUARTER_OF_YEAR);             // LDML (stand-alone)
        FIELD_MAP.put('M',ChronoField.MONTH_OF_YEAR);             // SDF, LDML
        FIELD_MAP.put('L',ChronoField.MONTH_OF_YEAR);             // SDF, LDML (stand-alone)
        FIELD_MAP.put('D',ChronoField.DAY_OF_YEAR);               // SDF, LDML
        FIELD_MAP.put('d',ChronoField.DAY_OF_MONTH);              // SDF, LDML
        FIELD_MAP.put('F',ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH);  // SDF, LDML
        FIELD_MAP.put('E',ChronoField.DAY_OF_WEEK);               // SDF, LDML (different to both for 1/2 chars)
        FIELD_MAP.put('c',ChronoField.DAY_OF_WEEK);               // LDML (stand-alone)
        FIELD_MAP.put('e',ChronoField.DAY_OF_WEEK);               // LDML (needs localized week number)
        FIELD_MAP.put('a',ChronoField.AMPM_OF_DAY);               // SDF, LDML
        FIELD_MAP.put('H',ChronoField.HOUR_OF_DAY);               // SDF, LDML
        FIELD_MAP.put('k',ChronoField.CLOCK_HOUR_OF_DAY);         // SDF, LDML
        FIELD_MAP.put('K',ChronoField.HOUR_OF_AMPM);              // SDF, LDML
        FIELD_MAP.put('h',ChronoField.CLOCK_HOUR_OF_AMPM);        // SDF, LDML
        FIELD_MAP.put('m',ChronoField.MINUTE_OF_HOUR);            // SDF, LDML
        FIELD_MAP.put('s',ChronoField.SECOND_OF_MINUTE);          // SDF, LDML
        FIELD_MAP.put('S',ChronoField.NANO_OF_SECOND);            // LDML (SDF uses milli-of-second number)
        FIELD_MAP.put('A',ChronoField.MILLI_OF_DAY);              // LDML
        FIELD_MAP.put('n',ChronoField.NANO_OF_SECOND);            // 310 (proposed for LDML)
        FIELD_MAP.put('N',ChronoField.NANO_OF_DAY);               // 310 (proposed for LDML)
        // 310 - z - time-zone names, matches LDML and SimpleDateFormat 1 to 4
        // 310 - Z - matches SimpleDateFormat and LDML
        // 310 - V - time-zone id, matches LDML
        // 310 - p - prefix for padding
        // 310 - X - matches LDML, almost matches SDF for 1, exact match 2&3, extended 4&5
        // 310 - x - matches LDML
        // 310 - w, W, and Y are localized forms matching LDML
        // LDML - U - cycle year name, not supported by 310 yet
        // LDML - l - deprecated
        // LDML - j - not relevant
        // LDML - g - modified-julian-day
        // LDML - v,V - extended time-zone names
    }

    private final DateTimeFormatterBuilder parent;
    private final List<DateTimePrinterParser> printerParsers=new ArrayList<>();
    private final boolean optional;
    private DateTimeFormatterBuilder active=this;
    private int padNextWidth;
    private char padNextChar;
    private int valueParserIndex=-1;

    public DateTimeFormatterBuilder(){
        super();
        parent=null;
        optional=false;
    }

    private DateTimeFormatterBuilder(DateTimeFormatterBuilder parent,boolean optional){
        super();
        this.parent=parent;
        this.optional=optional;
    }

    public static String getLocalizedDateTimePattern(FormatStyle dateStyle,FormatStyle timeStyle,
                                                     Chronology chrono,Locale locale){
        Objects.requireNonNull(locale,"locale");
        Objects.requireNonNull(chrono,"chrono");
        if(dateStyle==null&&timeStyle==null){
            throw new IllegalArgumentException("Either dateStyle or timeStyle must be non-null");
        }
        LocaleResources lr=LocaleProviderAdapter.getResourceBundleBased().getLocaleResources(locale);
        String pattern=lr.getJavaTimeDateTimePattern(
                convertStyle(timeStyle),convertStyle(dateStyle),chrono.getCalendarType());
        return pattern;
    }

    private static int convertStyle(FormatStyle style){
        if(style==null){
            return -1;
        }
        return style.ordinal();  // indices happen to align
    }

    //-----------------------------------------------------------------------
    public DateTimeFormatterBuilder parseCaseSensitive(){
        appendInternal(SettingsParser.SENSITIVE);
        return this;
    }

    //-----------------------------------------------------------------------
    private int appendInternal(DateTimePrinterParser pp){
        Objects.requireNonNull(pp,"pp");
        if(active.padNextWidth>0){
            if(pp!=null){
                pp=new PadPrinterParserDecorator(pp,active.padNextWidth,active.padNextChar);
            }
            active.padNextWidth=0;
            active.padNextChar=0;
        }
        active.printerParsers.add(pp);
        active.valueParserIndex=-1;
        return active.printerParsers.size()-1;
    }

    public DateTimeFormatterBuilder parseCaseInsensitive(){
        appendInternal(SettingsParser.INSENSITIVE);
        return this;
    }

    //-----------------------------------------------------------------------
    public DateTimeFormatterBuilder parseStrict(){
        appendInternal(SettingsParser.STRICT);
        return this;
    }

    public DateTimeFormatterBuilder parseLenient(){
        appendInternal(SettingsParser.LENIENT);
        return this;
    }

    //-----------------------------------------------------------------------
    public DateTimeFormatterBuilder parseDefaulting(TemporalField field,long value){
        Objects.requireNonNull(field,"field");
        appendInternal(new DefaultValueParser(field,value));
        return this;
    }

    //-----------------------------------------------------------------------
    public DateTimeFormatterBuilder appendValue(TemporalField field){
        Objects.requireNonNull(field,"field");
        appendValue(new NumberPrinterParser(field,1,19,SignStyle.NORMAL));
        return this;
    }

    public DateTimeFormatterBuilder appendValue(TemporalField field,int width){
        Objects.requireNonNull(field,"field");
        if(width<1||width>19){
            throw new IllegalArgumentException("The width must be from 1 to 19 inclusive but was "+width);
        }
        NumberPrinterParser pp=new NumberPrinterParser(field,width,width,SignStyle.NOT_NEGATIVE);
        appendValue(pp);
        return this;
    }

    public DateTimeFormatterBuilder appendValue(
            TemporalField field,int minWidth,int maxWidth,SignStyle signStyle){
        if(minWidth==maxWidth&&signStyle==SignStyle.NOT_NEGATIVE){
            return appendValue(field,maxWidth);
        }
        Objects.requireNonNull(field,"field");
        Objects.requireNonNull(signStyle,"signStyle");
        if(minWidth<1||minWidth>19){
            throw new IllegalArgumentException("The minimum width must be from 1 to 19 inclusive but was "+minWidth);
        }
        if(maxWidth<1||maxWidth>19){
            throw new IllegalArgumentException("The maximum width must be from 1 to 19 inclusive but was "+maxWidth);
        }
        if(maxWidth<minWidth){
            throw new IllegalArgumentException("The maximum width must exceed or equal the minimum width but "+
                    maxWidth+" < "+minWidth);
        }
        NumberPrinterParser pp=new NumberPrinterParser(field,minWidth,maxWidth,signStyle);
        appendValue(pp);
        return this;
    }

    //-----------------------------------------------------------------------
    public DateTimeFormatterBuilder appendValueReduced(TemporalField field,
                                                       int width,int maxWidth,int baseValue){
        Objects.requireNonNull(field,"field");
        ReducedPrinterParser pp=new ReducedPrinterParser(field,width,maxWidth,baseValue,null);
        appendValue(pp);
        return this;
    }

    private DateTimeFormatterBuilder appendValue(NumberPrinterParser pp){
        if(active.valueParserIndex>=0){
            final int activeValueParser=active.valueParserIndex;
            // adjacent parsing mode, update setting in previous parsers
            NumberPrinterParser basePP=(NumberPrinterParser)active.printerParsers.get(activeValueParser);
            if(pp.minWidth==pp.maxWidth&&pp.signStyle==SignStyle.NOT_NEGATIVE){
                // Append the width to the subsequentWidth of the active parser
                basePP=basePP.withSubsequentWidth(pp.maxWidth);
                // Append the new parser as a fixed width
                appendInternal(pp.withFixedWidth());
                // Retain the previous active parser
                active.valueParserIndex=activeValueParser;
            }else{
                // Modify the active parser to be fixed width
                basePP=basePP.withFixedWidth();
                // The new parser becomes the mew active parser
                active.valueParserIndex=appendInternal(pp);
            }
            // Replace the modified parser with the updated one
            active.printerParsers.set(activeValueParser,basePP);
        }else{
            // The new Parser becomes the active parser
            active.valueParserIndex=appendInternal(pp);
        }
        return this;
    }

    public DateTimeFormatterBuilder appendValueReduced(
            TemporalField field,int width,int maxWidth,ChronoLocalDate baseDate){
        Objects.requireNonNull(field,"field");
        Objects.requireNonNull(baseDate,"baseDate");
        ReducedPrinterParser pp=new ReducedPrinterParser(field,width,maxWidth,0,baseDate);
        appendValue(pp);
        return this;
    }

    //-----------------------------------------------------------------------
    public DateTimeFormatterBuilder appendFraction(
            TemporalField field,int minWidth,int maxWidth,boolean decimalPoint){
        appendInternal(new FractionPrinterParser(field,minWidth,maxWidth,decimalPoint));
        return this;
    }

    //-----------------------------------------------------------------------
    public DateTimeFormatterBuilder appendText(TemporalField field){
        return appendText(field,TextStyle.FULL);
    }

    public DateTimeFormatterBuilder appendText(TemporalField field,TextStyle textStyle){
        Objects.requireNonNull(field,"field");
        Objects.requireNonNull(textStyle,"textStyle");
        appendInternal(new TextPrinterParser(field,textStyle,DateTimeTextProvider.getInstance()));
        return this;
    }

    public DateTimeFormatterBuilder appendText(TemporalField field,Map<Long,String> textLookup){
        Objects.requireNonNull(field,"field");
        Objects.requireNonNull(textLookup,"textLookup");
        Map<Long,String> copy=new LinkedHashMap<>(textLookup);
        Map<TextStyle,Map<Long,String>> map=Collections.singletonMap(TextStyle.FULL,copy);
        final LocaleStore store=new LocaleStore(map);
        DateTimeTextProvider provider=new DateTimeTextProvider(){
            @Override
            public String getText(TemporalField field,long value,TextStyle style,Locale locale){
                return store.getText(value,style);
            }

            @Override
            public Iterator<Entry<String,Long>> getTextIterator(TemporalField field,TextStyle style,Locale locale){
                return store.getTextIterator(style);
            }
        };
        appendInternal(new TextPrinterParser(field,TextStyle.FULL,provider));
        return this;
    }

    //-----------------------------------------------------------------------
    public DateTimeFormatterBuilder appendInstant(){
        appendInternal(new InstantPrinterParser(-2));
        return this;
    }

    public DateTimeFormatterBuilder appendInstant(int fractionalDigits){
        if(fractionalDigits<-1||fractionalDigits>9){
            throw new IllegalArgumentException("The fractional digits must be from -1 to 9 inclusive but was "+fractionalDigits);
        }
        appendInternal(new InstantPrinterParser(fractionalDigits));
        return this;
    }

    //-----------------------------------------------------------------------
    public DateTimeFormatterBuilder appendOffsetId(){
        appendInternal(OffsetIdPrinterParser.INSTANCE_ID_Z);
        return this;
    }

    public DateTimeFormatterBuilder appendOffset(String pattern,String noOffsetText){
        appendInternal(new OffsetIdPrinterParser(pattern,noOffsetText));
        return this;
    }

    public DateTimeFormatterBuilder appendLocalizedOffset(TextStyle style){
        Objects.requireNonNull(style,"style");
        if(style!=TextStyle.FULL&&style!=TextStyle.SHORT){
            throw new IllegalArgumentException("Style must be either full or short");
        }
        appendInternal(new LocalizedOffsetIdPrinterParser(style));
        return this;
    }

    //-----------------------------------------------------------------------
    public DateTimeFormatterBuilder appendZoneId(){
        appendInternal(new ZoneIdPrinterParser(TemporalQueries.zoneId(),"ZoneId()"));
        return this;
    }

    public DateTimeFormatterBuilder appendZoneRegionId(){
        appendInternal(new ZoneIdPrinterParser(QUERY_REGION_ONLY,"ZoneRegionId()"));
        return this;
    }

    public DateTimeFormatterBuilder appendZoneOrOffsetId(){
        appendInternal(new ZoneIdPrinterParser(TemporalQueries.zone(),"ZoneOrOffsetId()"));
        return this;
    }

    public DateTimeFormatterBuilder appendZoneText(TextStyle textStyle){
        appendInternal(new ZoneTextPrinterParser(textStyle,null));
        return this;
    }

    public DateTimeFormatterBuilder appendZoneText(TextStyle textStyle,
                                                   Set<ZoneId> preferredZones){
        Objects.requireNonNull(preferredZones,"preferredZones");
        appendInternal(new ZoneTextPrinterParser(textStyle,preferredZones));
        return this;
    }

    //-----------------------------------------------------------------------
    public DateTimeFormatterBuilder appendChronologyId(){
        appendInternal(new ChronoPrinterParser(null));
        return this;
    }

    public DateTimeFormatterBuilder appendChronologyText(TextStyle textStyle){
        Objects.requireNonNull(textStyle,"textStyle");
        appendInternal(new ChronoPrinterParser(textStyle));
        return this;
    }

    //-----------------------------------------------------------------------
    public DateTimeFormatterBuilder appendLocalized(FormatStyle dateStyle,FormatStyle timeStyle){
        if(dateStyle==null&&timeStyle==null){
            throw new IllegalArgumentException("Either the date or time style must be non-null");
        }
        appendInternal(new LocalizedPrinterParser(dateStyle,timeStyle));
        return this;
    }

    //-----------------------------------------------------------------------
    public DateTimeFormatterBuilder appendLiteral(char literal){
        appendInternal(new CharLiteralPrinterParser(literal));
        return this;
    }

    public DateTimeFormatterBuilder appendLiteral(String literal){
        Objects.requireNonNull(literal,"literal");
        if(literal.length()>0){
            if(literal.length()==1){
                appendInternal(new CharLiteralPrinterParser(literal.charAt(0)));
            }else{
                appendInternal(new StringLiteralPrinterParser(literal));
            }
        }
        return this;
    }

    //-----------------------------------------------------------------------
    public DateTimeFormatterBuilder append(DateTimeFormatter formatter){
        Objects.requireNonNull(formatter,"formatter");
        appendInternal(formatter.toPrinterParser(false));
        return this;
    }

    public DateTimeFormatterBuilder appendOptional(DateTimeFormatter formatter){
        Objects.requireNonNull(formatter,"formatter");
        appendInternal(formatter.toPrinterParser(true));
        return this;
    }

    //-----------------------------------------------------------------------
    public DateTimeFormatterBuilder appendPattern(String pattern){
        Objects.requireNonNull(pattern,"pattern");
        parsePattern(pattern);
        return this;
    }

    private void parsePattern(String pattern){
        for(int pos=0;pos<pattern.length();pos++){
            char cur=pattern.charAt(pos);
            if((cur>='A'&&cur<='Z')||(cur>='a'&&cur<='z')){
                int start=pos++;
                for(;pos<pattern.length()&&pattern.charAt(pos)==cur;pos++) ;  // short loop
                int count=pos-start;
                // padding
                if(cur=='p'){
                    int pad=0;
                    if(pos<pattern.length()){
                        cur=pattern.charAt(pos);
                        if((cur>='A'&&cur<='Z')||(cur>='a'&&cur<='z')){
                            pad=count;
                            start=pos++;
                            for(;pos<pattern.length()&&pattern.charAt(pos)==cur;pos++) ;  // short loop
                            count=pos-start;
                        }
                    }
                    if(pad==0){
                        throw new IllegalArgumentException(
                                "Pad letter 'p' must be followed by valid pad pattern: "+pattern);
                    }
                    padNext(pad); // pad and continue parsing
                }
                // main rules
                TemporalField field=FIELD_MAP.get(cur);
                if(field!=null){
                    parseField(cur,count,field);
                }else if(cur=='z'){
                    if(count>4){
                        throw new IllegalArgumentException("Too many pattern letters: "+cur);
                    }else if(count==4){
                        appendZoneText(TextStyle.FULL);
                    }else{
                        appendZoneText(TextStyle.SHORT);
                    }
                }else if(cur=='V'){
                    if(count!=2){
                        throw new IllegalArgumentException("Pattern letter count must be 2: "+cur);
                    }
                    appendZoneId();
                }else if(cur=='Z'){
                    if(count<4){
                        appendOffset("+HHMM","+0000");
                    }else if(count==4){
                        appendLocalizedOffset(TextStyle.FULL);
                    }else if(count==5){
                        appendOffset("+HH:MM:ss","Z");
                    }else{
                        throw new IllegalArgumentException("Too many pattern letters: "+cur);
                    }
                }else if(cur=='O'){
                    if(count==1){
                        appendLocalizedOffset(TextStyle.SHORT);
                    }else if(count==4){
                        appendLocalizedOffset(TextStyle.FULL);
                    }else{
                        throw new IllegalArgumentException("Pattern letter count must be 1 or 4: "+cur);
                    }
                }else if(cur=='X'){
                    if(count>5){
                        throw new IllegalArgumentException("Too many pattern letters: "+cur);
                    }
                    appendOffset(OffsetIdPrinterParser.PATTERNS[count+(count==1?0:1)],"Z");
                }else if(cur=='x'){
                    if(count>5){
                        throw new IllegalArgumentException("Too many pattern letters: "+cur);
                    }
                    String zero=(count==1?"+00":(count%2==0?"+0000":"+00:00"));
                    appendOffset(OffsetIdPrinterParser.PATTERNS[count+(count==1?0:1)],zero);
                }else if(cur=='W'){
                    // Fields defined by Locale
                    if(count>1){
                        throw new IllegalArgumentException("Too many pattern letters: "+cur);
                    }
                    appendInternal(new WeekBasedFieldPrinterParser(cur,count));
                }else if(cur=='w'){
                    // Fields defined by Locale
                    if(count>2){
                        throw new IllegalArgumentException("Too many pattern letters: "+cur);
                    }
                    appendInternal(new WeekBasedFieldPrinterParser(cur,count));
                }else if(cur=='Y'){
                    // Fields defined by Locale
                    appendInternal(new WeekBasedFieldPrinterParser(cur,count));
                }else{
                    throw new IllegalArgumentException("Unknown pattern letter: "+cur);
                }
                pos--;
            }else if(cur=='\''){
                // parse literals
                int start=pos++;
                for(;pos<pattern.length();pos++){
                    if(pattern.charAt(pos)=='\''){
                        if(pos+1<pattern.length()&&pattern.charAt(pos+1)=='\''){
                            pos++;
                        }else{
                            break;  // end of literal
                        }
                    }
                }
                if(pos>=pattern.length()){
                    throw new IllegalArgumentException("Pattern ends with an incomplete string literal: "+pattern);
                }
                String str=pattern.substring(start+1,pos);
                if(str.length()==0){
                    appendLiteral('\'');
                }else{
                    appendLiteral(str.replace("''","'"));
                }
            }else if(cur=='['){
                optionalStart();
            }else if(cur==']'){
                if(active.parent==null){
                    throw new IllegalArgumentException("Pattern invalid as it contains ] without previous [");
                }
                optionalEnd();
            }else if(cur=='{'||cur=='}'||cur=='#'){
                throw new IllegalArgumentException("Pattern includes reserved character: '"+cur+"'");
            }else{
                appendLiteral(cur);
            }
        }
    }

    @SuppressWarnings("fallthrough")
    private void parseField(char cur,int count,TemporalField field){
        boolean standalone=false;
        switch(cur){
            case 'u':
            case 'y':
                if(count==2){
                    appendValueReduced(field,2,2,ReducedPrinterParser.BASE_DATE);
                }else if(count<4){
                    appendValue(field,count,19,SignStyle.NORMAL);
                }else{
                    appendValue(field,count,19,SignStyle.EXCEEDS_PAD);
                }
                break;
            case 'c':
                if(count==2){
                    throw new IllegalArgumentException("Invalid pattern \"cc\"");
                }
                /**fallthrough*/
            case 'L':
            case 'q':
                standalone=true;
                /**fallthrough*/
            case 'M':
            case 'Q':
            case 'E':
            case 'e':
                switch(count){
                    case 1:
                    case 2:
                        if(cur=='c'||cur=='e'){
                            appendInternal(new WeekBasedFieldPrinterParser(cur,count));
                        }else if(cur=='E'){
                            appendText(field,TextStyle.SHORT);
                        }else{
                            if(count==1){
                                appendValue(field);
                            }else{
                                appendValue(field,2);
                            }
                        }
                        break;
                    case 3:
                        appendText(field,standalone?TextStyle.SHORT_STANDALONE:TextStyle.SHORT);
                        break;
                    case 4:
                        appendText(field,standalone?TextStyle.FULL_STANDALONE:TextStyle.FULL);
                        break;
                    case 5:
                        appendText(field,standalone?TextStyle.NARROW_STANDALONE:TextStyle.NARROW);
                        break;
                    default:
                        throw new IllegalArgumentException("Too many pattern letters: "+cur);
                }
                break;
            case 'a':
                if(count==1){
                    appendText(field,TextStyle.SHORT);
                }else{
                    throw new IllegalArgumentException("Too many pattern letters: "+cur);
                }
                break;
            case 'G':
                switch(count){
                    case 1:
                    case 2:
                    case 3:
                        appendText(field,TextStyle.SHORT);
                        break;
                    case 4:
                        appendText(field,TextStyle.FULL);
                        break;
                    case 5:
                        appendText(field,TextStyle.NARROW);
                        break;
                    default:
                        throw new IllegalArgumentException("Too many pattern letters: "+cur);
                }
                break;
            case 'S':
                appendFraction(NANO_OF_SECOND,count,count,false);
                break;
            case 'F':
                if(count==1){
                    appendValue(field);
                }else{
                    throw new IllegalArgumentException("Too many pattern letters: "+cur);
                }
                break;
            case 'd':
            case 'h':
            case 'H':
            case 'k':
            case 'K':
            case 'm':
            case 's':
                if(count==1){
                    appendValue(field);
                }else if(count==2){
                    appendValue(field,count);
                }else{
                    throw new IllegalArgumentException("Too many pattern letters: "+cur);
                }
                break;
            case 'D':
                if(count==1){
                    appendValue(field);
                }else if(count<=3){
                    appendValue(field,count);
                }else{
                    throw new IllegalArgumentException("Too many pattern letters: "+cur);
                }
                break;
            default:
                if(count==1){
                    appendValue(field);
                }else{
                    appendValue(field,count);
                }
                break;
        }
    }

    //-----------------------------------------------------------------------
    public DateTimeFormatterBuilder padNext(int padWidth){
        return padNext(padWidth,' ');
    }

    public DateTimeFormatterBuilder padNext(int padWidth,char padChar){
        if(padWidth<1){
            throw new IllegalArgumentException("The pad width must be at least one but was "+padWidth);
        }
        active.padNextWidth=padWidth;
        active.padNextChar=padChar;
        active.valueParserIndex=-1;
        return this;
    }

    //-----------------------------------------------------------------------
    public DateTimeFormatterBuilder optionalStart(){
        active.valueParserIndex=-1;
        active=new DateTimeFormatterBuilder(active,true);
        return this;
    }

    //-----------------------------------------------------------------------
    public DateTimeFormatter toFormatter(){
        return toFormatter(Locale.getDefault(Locale.Category.FORMAT));
    }

    public DateTimeFormatter toFormatter(Locale locale){
        return toFormatter(locale,ResolverStyle.SMART,null);
    }

    private DateTimeFormatter toFormatter(Locale locale,ResolverStyle resolverStyle,Chronology chrono){
        Objects.requireNonNull(locale,"locale");
        while(active.parent!=null){
            optionalEnd();
        }
        CompositePrinterParser pp=new CompositePrinterParser(printerParsers,false);
        return new DateTimeFormatter(pp,locale,DecimalStyle.STANDARD,
                resolverStyle,null,chrono,null);
    }

    public DateTimeFormatterBuilder optionalEnd(){
        if(active.parent==null){
            throw new IllegalStateException("Cannot call optionalEnd() as there was no previous call to optionalStart()");
        }
        if(active.printerParsers.size()>0){
            CompositePrinterParser cpp=new CompositePrinterParser(active.printerParsers,active.optional);
            active=active.parent;
            appendInternal(cpp);
        }else{
            active=active.parent;
        }
        return this;
    }

    DateTimeFormatter toFormatter(ResolverStyle resolverStyle,Chronology chrono){
        return toFormatter(Locale.getDefault(Locale.Category.FORMAT),resolverStyle,chrono);
    }

    //-----------------------------------------------------------------------
    static enum SettingsParser implements DateTimePrinterParser{
        SENSITIVE,
        INSENSITIVE,
        STRICT,
        LENIENT;

        @Override
        public String toString(){
            // using ordinals to avoid javac synthetic inner class
            switch(ordinal()){
                case 0:
                    return "ParseCaseSensitive(true)";
                case 1:
                    return "ParseCaseSensitive(false)";
                case 2:
                    return "ParseStrict(true)";
                case 3:
                    return "ParseStrict(false)";
            }
            throw new IllegalStateException("Unreachable");
        }

        @Override
        public boolean format(DateTimePrintContext context,StringBuilder buf){
            return true;  // nothing to do here
        }

        @Override
        public int parse(DateTimeParseContext context,CharSequence text,int position){
            // using ordinals to avoid javac synthetic inner class
            switch(ordinal()){
                case 0:
                    context.setCaseSensitive(true);
                    break;
                case 1:
                    context.setCaseSensitive(false);
                    break;
                case 2:
                    context.setStrict(true);
                    break;
                case 3:
                    context.setStrict(false);
                    break;
            }
            return position;
        }
    }

    //-----------------------------------------------------------------------
    interface DateTimePrinterParser{
        boolean format(DateTimePrintContext context,StringBuilder buf);

        int parse(DateTimeParseContext context,CharSequence text,int position);
    }

    //-----------------------------------------------------------------------
    static final class CompositePrinterParser implements DateTimePrinterParser{
        private final DateTimePrinterParser[] printerParsers;
        private final boolean optional;

        CompositePrinterParser(List<DateTimePrinterParser> printerParsers,boolean optional){
            this(printerParsers.toArray(new DateTimePrinterParser[printerParsers.size()]),optional);
        }

        CompositePrinterParser(DateTimePrinterParser[] printerParsers,boolean optional){
            this.printerParsers=printerParsers;
            this.optional=optional;
        }

        public CompositePrinterParser withOptional(boolean optional){
            if(optional==this.optional){
                return this;
            }
            return new CompositePrinterParser(printerParsers,optional);
        }

        @Override
        public String toString(){
            StringBuilder buf=new StringBuilder();
            if(printerParsers!=null){
                buf.append(optional?"[":"(");
                for(DateTimePrinterParser pp : printerParsers){
                    buf.append(pp);
                }
                buf.append(optional?"]":")");
            }
            return buf.toString();
        }

        @Override
        public boolean format(DateTimePrintContext context,StringBuilder buf){
            int length=buf.length();
            if(optional){
                context.startOptional();
            }
            try{
                for(DateTimePrinterParser pp : printerParsers){
                    if(pp.format(context,buf)==false){
                        buf.setLength(length);  // reset buffer
                        return true;
                    }
                }
            }finally{
                if(optional){
                    context.endOptional();
                }
            }
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context,CharSequence text,int position){
            if(optional){
                context.startOptional();
                int pos=position;
                for(DateTimePrinterParser pp : printerParsers){
                    pos=pp.parse(context,text,pos);
                    if(pos<0){
                        context.endOptional(false);
                        return position;  // return original position
                    }
                }
                context.endOptional(true);
                return pos;
            }else{
                for(DateTimePrinterParser pp : printerParsers){
                    position=pp.parse(context,text,position);
                    if(position<0){
                        break;
                    }
                }
                return position;
            }
        }
    }

    //-----------------------------------------------------------------------
    static final class PadPrinterParserDecorator implements DateTimePrinterParser{
        private final DateTimePrinterParser printerParser;
        private final int padWidth;
        private final char padChar;

        PadPrinterParserDecorator(DateTimePrinterParser printerParser,int padWidth,char padChar){
            // input checked by DateTimeFormatterBuilder
            this.printerParser=printerParser;
            this.padWidth=padWidth;
            this.padChar=padChar;
        }

        @Override
        public boolean format(DateTimePrintContext context,StringBuilder buf){
            int preLen=buf.length();
            if(printerParser.format(context,buf)==false){
                return false;
            }
            int len=buf.length()-preLen;
            if(len>padWidth){
                throw new DateTimeException(
                        "Cannot print as output of "+len+" characters exceeds pad width of "+padWidth);
            }
            for(int i=0;i<padWidth-len;i++){
                buf.insert(preLen,padChar);
            }
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context,CharSequence text,int position){
            // cache context before changed by decorated parser
            final boolean strict=context.isStrict();
            // parse
            if(position>text.length()){
                throw new IndexOutOfBoundsException();
            }
            if(position==text.length()){
                return ~position;  // no more characters in the string
            }
            int endPos=position+padWidth;
            if(endPos>text.length()){
                if(strict){
                    return ~position;  // not enough characters in the string to meet the parse width
                }
                endPos=text.length();
            }
            int pos=position;
            while(pos<endPos&&context.charEquals(text.charAt(pos),padChar)){
                pos++;
            }
            text=text.subSequence(0,endPos);
            int resultPos=printerParser.parse(context,text,pos);
            if(resultPos!=endPos&&strict){
                return ~(position+pos);  // parse of decorated field didn't parse to the end
            }
            return resultPos;
        }

        @Override
        public String toString(){
            return "Pad("+printerParser+","+padWidth+(padChar==' '?")":",'"+padChar+"')");
        }
    }

    //-----------------------------------------------------------------------
    static class DefaultValueParser implements DateTimePrinterParser{
        private final TemporalField field;
        private final long value;

        DefaultValueParser(TemporalField field,long value){
            this.field=field;
            this.value=value;
        }

        public boolean format(DateTimePrintContext context,StringBuilder buf){
            return true;
        }

        public int parse(DateTimeParseContext context,CharSequence text,int position){
            if(context.getParsed(field)==null){
                context.setParsedField(field,value,position,position);
            }
            return position;
        }
    }

    //-----------------------------------------------------------------------
    static final class CharLiteralPrinterParser implements DateTimePrinterParser{
        private final char literal;

        CharLiteralPrinterParser(char literal){
            this.literal=literal;
        }

        @Override
        public boolean format(DateTimePrintContext context,StringBuilder buf){
            buf.append(literal);
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context,CharSequence text,int position){
            int length=text.length();
            if(position==length){
                return ~position;
            }
            char ch=text.charAt(position);
            if(ch!=literal){
                if(context.isCaseSensitive()||
                        (Character.toUpperCase(ch)!=Character.toUpperCase(literal)&&
                                Character.toLowerCase(ch)!=Character.toLowerCase(literal))){
                    return ~position;
                }
            }
            return position+1;
        }

        @Override
        public String toString(){
            if(literal=='\''){
                return "''";
            }
            return "'"+literal+"'";
        }
    }

    //-----------------------------------------------------------------------
    static final class StringLiteralPrinterParser implements DateTimePrinterParser{
        private final String literal;

        StringLiteralPrinterParser(String literal){
            this.literal=literal;  // validated by caller
        }

        @Override
        public boolean format(DateTimePrintContext context,StringBuilder buf){
            buf.append(literal);
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context,CharSequence text,int position){
            int length=text.length();
            if(position>length||position<0){
                throw new IndexOutOfBoundsException();
            }
            if(context.subSequenceEquals(text,position,literal,0,literal.length())==false){
                return ~position;
            }
            return position+literal.length();
        }

        @Override
        public String toString(){
            String converted=literal.replace("'","''");
            return "'"+converted+"'";
        }
    }

    //-----------------------------------------------------------------------
    static class NumberPrinterParser implements DateTimePrinterParser{
        static final long[] EXCEED_POINTS=new long[]{
                0L,
                10L,
                100L,
                1000L,
                10000L,
                100000L,
                1000000L,
                10000000L,
                100000000L,
                1000000000L,
                10000000000L,
        };
        final TemporalField field;
        final int minWidth;
        final int maxWidth;
        final int subsequentWidth;
        private final SignStyle signStyle;

        NumberPrinterParser(TemporalField field,int minWidth,int maxWidth,SignStyle signStyle){
            // validated by caller
            this.field=field;
            this.minWidth=minWidth;
            this.maxWidth=maxWidth;
            this.signStyle=signStyle;
            this.subsequentWidth=0;
        }

        protected NumberPrinterParser(TemporalField field,int minWidth,int maxWidth,SignStyle signStyle,int subsequentWidth){
            // validated by caller
            this.field=field;
            this.minWidth=minWidth;
            this.maxWidth=maxWidth;
            this.signStyle=signStyle;
            this.subsequentWidth=subsequentWidth;
        }

        NumberPrinterParser withFixedWidth(){
            if(subsequentWidth==-1){
                return this;
            }
            return new NumberPrinterParser(field,minWidth,maxWidth,signStyle,-1);
        }

        NumberPrinterParser withSubsequentWidth(int subsequentWidth){
            return new NumberPrinterParser(field,minWidth,maxWidth,signStyle,this.subsequentWidth+subsequentWidth);
        }

        @Override
        public boolean format(DateTimePrintContext context,StringBuilder buf){
            Long valueLong=context.getValue(field);
            if(valueLong==null){
                return false;
            }
            long value=getValue(context,valueLong);
            DecimalStyle decimalStyle=context.getDecimalStyle();
            String str=(value==Long.MIN_VALUE?"9223372036854775808":Long.toString(Math.abs(value)));
            if(str.length()>maxWidth){
                throw new DateTimeException("Field "+field+
                        " cannot be printed as the value "+value+
                        " exceeds the maximum print width of "+maxWidth);
            }
            str=decimalStyle.convertNumberToI18N(str);
            if(value>=0){
                switch(signStyle){
                    case EXCEEDS_PAD:
                        if(minWidth<19&&value>=EXCEED_POINTS[minWidth]){
                            buf.append(decimalStyle.getPositiveSign());
                        }
                        break;
                    case ALWAYS:
                        buf.append(decimalStyle.getPositiveSign());
                        break;
                }
            }else{
                switch(signStyle){
                    case NORMAL:
                    case EXCEEDS_PAD:
                    case ALWAYS:
                        buf.append(decimalStyle.getNegativeSign());
                        break;
                    case NOT_NEGATIVE:
                        throw new DateTimeException("Field "+field+
                                " cannot be printed as the value "+value+
                                " cannot be negative according to the SignStyle");
                }
            }
            for(int i=0;i<minWidth-str.length();i++){
                buf.append(decimalStyle.getZeroDigit());
            }
            buf.append(str);
            return true;
        }

        long getValue(DateTimePrintContext context,long value){
            return value;
        }

        boolean isFixedWidth(DateTimeParseContext context){
            return subsequentWidth==-1||
                    (subsequentWidth>0&&minWidth==maxWidth&&signStyle==SignStyle.NOT_NEGATIVE);
        }

        @Override
        public int parse(DateTimeParseContext context,CharSequence text,int position){
            int length=text.length();
            if(position==length){
                return ~position;
            }
            char sign=text.charAt(position);  // IOOBE if invalid position
            boolean negative=false;
            boolean positive=false;
            if(sign==context.getDecimalStyle().getPositiveSign()){
                if(signStyle.parse(true,context.isStrict(),minWidth==maxWidth)==false){
                    return ~position;
                }
                positive=true;
                position++;
            }else if(sign==context.getDecimalStyle().getNegativeSign()){
                if(signStyle.parse(false,context.isStrict(),minWidth==maxWidth)==false){
                    return ~position;
                }
                negative=true;
                position++;
            }else{
                if(signStyle==SignStyle.ALWAYS&&context.isStrict()){
                    return ~position;
                }
            }
            int effMinWidth=(context.isStrict()||isFixedWidth(context)?minWidth:1);
            int minEndPos=position+effMinWidth;
            if(minEndPos>length){
                return ~position;
            }
            int effMaxWidth=(context.isStrict()||isFixedWidth(context)?maxWidth:9)+Math.max(subsequentWidth,0);
            long total=0;
            BigInteger totalBig=null;
            int pos=position;
            for(int pass=0;pass<2;pass++){
                int maxEndPos=Math.min(pos+effMaxWidth,length);
                while(pos<maxEndPos){
                    char ch=text.charAt(pos++);
                    int digit=context.getDecimalStyle().convertToDigit(ch);
                    if(digit<0){
                        pos--;
                        if(pos<minEndPos){
                            return ~position;  // need at least min width digits
                        }
                        break;
                    }
                    if((pos-position)>18){
                        if(totalBig==null){
                            totalBig=BigInteger.valueOf(total);
                        }
                        totalBig=totalBig.multiply(BigInteger.TEN).add(BigInteger.valueOf(digit));
                    }else{
                        total=total*10+digit;
                    }
                }
                if(subsequentWidth>0&&pass==0){
                    // re-parse now we know the correct width
                    int parseLen=pos-position;
                    effMaxWidth=Math.max(effMinWidth,parseLen-subsequentWidth);
                    pos=position;
                    total=0;
                    totalBig=null;
                }else{
                    break;
                }
            }
            if(negative){
                if(totalBig!=null){
                    if(totalBig.equals(BigInteger.ZERO)&&context.isStrict()){
                        return ~(position-1);  // minus zero not allowed
                    }
                    totalBig=totalBig.negate();
                }else{
                    if(total==0&&context.isStrict()){
                        return ~(position-1);  // minus zero not allowed
                    }
                    total=-total;
                }
            }else if(signStyle==SignStyle.EXCEEDS_PAD&&context.isStrict()){
                int parseLen=pos-position;
                if(positive){
                    if(parseLen<=minWidth){
                        return ~(position-1);  // '+' only parsed if minWidth exceeded
                    }
                }else{
                    if(parseLen>minWidth){
                        return ~position;  // '+' must be parsed if minWidth exceeded
                    }
                }
            }
            if(totalBig!=null){
                if(totalBig.bitLength()>63){
                    // overflow, parse 1 less digit
                    totalBig=totalBig.divide(BigInteger.TEN);
                    pos--;
                }
                return setValue(context,totalBig.longValue(),position,pos);
            }
            return setValue(context,total,position,pos);
        }

        int setValue(DateTimeParseContext context,long value,int errorPos,int successPos){
            return context.setParsedField(field,value,errorPos,successPos);
        }

        @Override
        public String toString(){
            if(minWidth==1&&maxWidth==19&&signStyle==SignStyle.NORMAL){
                return "Value("+field+")";
            }
            if(minWidth==maxWidth&&signStyle==SignStyle.NOT_NEGATIVE){
                return "Value("+field+","+minWidth+")";
            }
            return "Value("+field+","+minWidth+","+maxWidth+","+signStyle+")";
        }
    }

    //-----------------------------------------------------------------------
    static final class ReducedPrinterParser extends NumberPrinterParser{
        static final LocalDate BASE_DATE=LocalDate.of(2000,1,1);
        private final int baseValue;
        private final ChronoLocalDate baseDate;

        ReducedPrinterParser(TemporalField field,int minWidth,int maxWidth,
                             int baseValue,ChronoLocalDate baseDate){
            this(field,minWidth,maxWidth,baseValue,baseDate,0);
            if(minWidth<1||minWidth>10){
                throw new IllegalArgumentException("The minWidth must be from 1 to 10 inclusive but was "+minWidth);
            }
            if(maxWidth<1||maxWidth>10){
                throw new IllegalArgumentException("The maxWidth must be from 1 to 10 inclusive but was "+minWidth);
            }
            if(maxWidth<minWidth){
                throw new IllegalArgumentException("Maximum width must exceed or equal the minimum width but "+
                        maxWidth+" < "+minWidth);
            }
            if(baseDate==null){
                if(field.range().isValidValue(baseValue)==false){
                    throw new IllegalArgumentException("The base value must be within the range of the field");
                }
                if((((long)baseValue)+EXCEED_POINTS[maxWidth])>Integer.MAX_VALUE){
                    throw new DateTimeException("Unable to add printer-parser as the range exceeds the capacity of an int");
                }
            }
        }

        private ReducedPrinterParser(TemporalField field,int minWidth,int maxWidth,
                                     int baseValue,ChronoLocalDate baseDate,int subsequentWidth){
            super(field,minWidth,maxWidth,SignStyle.NOT_NEGATIVE,subsequentWidth);
            this.baseValue=baseValue;
            this.baseDate=baseDate;
        }

        @Override
        ReducedPrinterParser withFixedWidth(){
            if(subsequentWidth==-1){
                return this;
            }
            return new ReducedPrinterParser(field,minWidth,maxWidth,baseValue,baseDate,-1);
        }

        @Override
        ReducedPrinterParser withSubsequentWidth(int subsequentWidth){
            return new ReducedPrinterParser(field,minWidth,maxWidth,baseValue,baseDate,
                    this.subsequentWidth+subsequentWidth);
        }

        @Override
        long getValue(DateTimePrintContext context,long value){
            long absValue=Math.abs(value);
            int baseValue=this.baseValue;
            if(baseDate!=null){
                Chronology chrono=Chronology.from(context.getTemporal());
                baseValue=chrono.date(baseDate).get(field);
            }
            if(value>=baseValue&&value<baseValue+EXCEED_POINTS[minWidth]){
                // Use the reduced value if it fits in minWidth
                return absValue%EXCEED_POINTS[minWidth];
            }
            // Otherwise truncate to fit in maxWidth
            return absValue%EXCEED_POINTS[maxWidth];
        }

        @Override
        boolean isFixedWidth(DateTimeParseContext context){
            if(context.isStrict()==false){
                return false;
            }
            return super.isFixedWidth(context);
        }

        @Override
        int setValue(DateTimeParseContext context,long value,int errorPos,int successPos){
            int baseValue=this.baseValue;
            if(baseDate!=null){
                Chronology chrono=context.getEffectiveChronology();
                baseValue=chrono.date(baseDate).get(field);
                // In case the Chronology is changed later, add a callback when/if it changes
                final long initialValue=value;
                context.addChronoChangedListener(
                        (_unused)->{
                            /** Repeat the set of the field using the current Chronology
                             * The success/error position is ignored because the value is
                             * intentionally being overwritten.
                             */
                            setValue(context,initialValue,errorPos,successPos);
                        });
            }
            int parseLen=successPos-errorPos;
            if(parseLen==minWidth&&value>=0){
                long range=EXCEED_POINTS[minWidth];
                long lastPart=baseValue%range;
                long basePart=baseValue-lastPart;
                if(baseValue>0){
                    value=basePart+value;
                }else{
                    value=basePart-value;
                }
                if(value<baseValue){
                    value+=range;
                }
            }
            return context.setParsedField(field,value,errorPos,successPos);
        }

        @Override
        public String toString(){
            return "ReducedValue("+field+","+minWidth+","+maxWidth+","+(baseDate!=null?baseDate:baseValue)+")";
        }
    }

    //-----------------------------------------------------------------------
    static final class FractionPrinterParser implements DateTimePrinterParser{
        private final TemporalField field;
        private final int minWidth;
        private final int maxWidth;
        private final boolean decimalPoint;

        FractionPrinterParser(TemporalField field,int minWidth,int maxWidth,boolean decimalPoint){
            Objects.requireNonNull(field,"field");
            if(field.range().isFixed()==false){
                throw new IllegalArgumentException("Field must have a fixed set of values: "+field);
            }
            if(minWidth<0||minWidth>9){
                throw new IllegalArgumentException("Minimum width must be from 0 to 9 inclusive but was "+minWidth);
            }
            if(maxWidth<1||maxWidth>9){
                throw new IllegalArgumentException("Maximum width must be from 1 to 9 inclusive but was "+maxWidth);
            }
            if(maxWidth<minWidth){
                throw new IllegalArgumentException("Maximum width must exceed or equal the minimum width but "+
                        maxWidth+" < "+minWidth);
            }
            this.field=field;
            this.minWidth=minWidth;
            this.maxWidth=maxWidth;
            this.decimalPoint=decimalPoint;
        }

        @Override
        public boolean format(DateTimePrintContext context,StringBuilder buf){
            Long value=context.getValue(field);
            if(value==null){
                return false;
            }
            DecimalStyle decimalStyle=context.getDecimalStyle();
            BigDecimal fraction=convertToFraction(value);
            if(fraction.scale()==0){  // scale is zero if value is zero
                if(minWidth>0){
                    if(decimalPoint){
                        buf.append(decimalStyle.getDecimalSeparator());
                    }
                    for(int i=0;i<minWidth;i++){
                        buf.append(decimalStyle.getZeroDigit());
                    }
                }
            }else{
                int outputScale=Math.min(Math.max(fraction.scale(),minWidth),maxWidth);
                fraction=fraction.setScale(outputScale,RoundingMode.FLOOR);
                String str=fraction.toPlainString().substring(2);
                str=decimalStyle.convertNumberToI18N(str);
                if(decimalPoint){
                    buf.append(decimalStyle.getDecimalSeparator());
                }
                buf.append(str);
            }
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context,CharSequence text,int position){
            int effectiveMin=(context.isStrict()?minWidth:0);
            int effectiveMax=(context.isStrict()?maxWidth:9);
            int length=text.length();
            if(position==length){
                // valid if whole field is optional, invalid if minimum width
                return (effectiveMin>0?~position:position);
            }
            if(decimalPoint){
                if(text.charAt(position)!=context.getDecimalStyle().getDecimalSeparator()){
                    // valid if whole field is optional, invalid if minimum width
                    return (effectiveMin>0?~position:position);
                }
                position++;
            }
            int minEndPos=position+effectiveMin;
            if(minEndPos>length){
                return ~position;  // need at least min width digits
            }
            int maxEndPos=Math.min(position+effectiveMax,length);
            int total=0;  // can use int because we are only parsing up to 9 digits
            int pos=position;
            while(pos<maxEndPos){
                char ch=text.charAt(pos++);
                int digit=context.getDecimalStyle().convertToDigit(ch);
                if(digit<0){
                    if(pos<minEndPos){
                        return ~position;  // need at least min width digits
                    }
                    pos--;
                    break;
                }
                total=total*10+digit;
            }
            BigDecimal fraction=new BigDecimal(total).movePointLeft(pos-position);
            long value=convertFromFraction(fraction);
            return context.setParsedField(field,value,position,pos);
        }

        private BigDecimal convertToFraction(long value){
            ValueRange range=field.range();
            range.checkValidValue(value,field);
            BigDecimal minBD=BigDecimal.valueOf(range.getMinimum());
            BigDecimal rangeBD=BigDecimal.valueOf(range.getMaximum()).subtract(minBD).add(BigDecimal.ONE);
            BigDecimal valueBD=BigDecimal.valueOf(value).subtract(minBD);
            BigDecimal fraction=valueBD.divide(rangeBD,9,RoundingMode.FLOOR);
            // stripTrailingZeros bug
            return fraction.compareTo(BigDecimal.ZERO)==0?BigDecimal.ZERO:fraction.stripTrailingZeros();
        }

        private long convertFromFraction(BigDecimal fraction){
            ValueRange range=field.range();
            BigDecimal minBD=BigDecimal.valueOf(range.getMinimum());
            BigDecimal rangeBD=BigDecimal.valueOf(range.getMaximum()).subtract(minBD).add(BigDecimal.ONE);
            BigDecimal valueBD=fraction.multiply(rangeBD).setScale(0,RoundingMode.FLOOR).add(minBD);
            return valueBD.longValueExact();
        }

        @Override
        public String toString(){
            String decimal=(decimalPoint?",DecimalPoint":"");
            return "Fraction("+field+","+minWidth+","+maxWidth+decimal+")";
        }
    }

    //-----------------------------------------------------------------------
    static final class TextPrinterParser implements DateTimePrinterParser{
        private final TemporalField field;
        private final TextStyle textStyle;
        private final DateTimeTextProvider provider;
        private volatile NumberPrinterParser numberPrinterParser;

        TextPrinterParser(TemporalField field,TextStyle textStyle,DateTimeTextProvider provider){
            // validated by caller
            this.field=field;
            this.textStyle=textStyle;
            this.provider=provider;
        }

        @Override
        public boolean format(DateTimePrintContext context,StringBuilder buf){
            Long value=context.getValue(field);
            if(value==null){
                return false;
            }
            String text;
            Chronology chrono=context.getTemporal().query(TemporalQueries.chronology());
            if(chrono==null||chrono==IsoChronology.INSTANCE){
                text=provider.getText(field,value,textStyle,context.getLocale());
            }else{
                text=provider.getText(chrono,field,value,textStyle,context.getLocale());
            }
            if(text==null){
                return numberPrinterParser().format(context,buf);
            }
            buf.append(text);
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context,CharSequence parseText,int position){
            int length=parseText.length();
            if(position<0||position>length){
                throw new IndexOutOfBoundsException();
            }
            TextStyle style=(context.isStrict()?textStyle:null);
            Chronology chrono=context.getEffectiveChronology();
            Iterator<Entry<String,Long>> it;
            if(chrono==null||chrono==IsoChronology.INSTANCE){
                it=provider.getTextIterator(field,style,context.getLocale());
            }else{
                it=provider.getTextIterator(chrono,field,style,context.getLocale());
            }
            if(it!=null){
                while(it.hasNext()){
                    Entry<String,Long> entry=it.next();
                    String itText=entry.getKey();
                    if(context.subSequenceEquals(itText,0,parseText,position,itText.length())){
                        return context.setParsedField(field,entry.getValue(),position,position+itText.length());
                    }
                }
                if(context.isStrict()){
                    return ~position;
                }
            }
            return numberPrinterParser().parse(context,parseText,position);
        }

        private NumberPrinterParser numberPrinterParser(){
            if(numberPrinterParser==null){
                numberPrinterParser=new NumberPrinterParser(field,1,19,SignStyle.NORMAL);
            }
            return numberPrinterParser;
        }

        @Override
        public String toString(){
            if(textStyle==TextStyle.FULL){
                return "Text("+field+")";
            }
            return "Text("+field+","+textStyle+")";
        }
    }

    //-----------------------------------------------------------------------
    static final class InstantPrinterParser implements DateTimePrinterParser{
        // days in a 400 year cycle = 146097
        // days in a 10,000 year cycle = 146097 * 25
        // seconds per day = 86400
        private static final long SECONDS_PER_10000_YEARS=146097L*25L*86400L;
        private static final long SECONDS_0000_TO_1970=((146097L*5L)-(30L*365L+7L))*86400L;
        private final int fractionalDigits;

        InstantPrinterParser(int fractionalDigits){
            this.fractionalDigits=fractionalDigits;
        }

        @Override
        public boolean format(DateTimePrintContext context,StringBuilder buf){
            // use INSTANT_SECONDS, thus this code is not bound by Instant.MAX
            Long inSecs=context.getValue(INSTANT_SECONDS);
            Long inNanos=null;
            if(context.getTemporal().isSupported(NANO_OF_SECOND)){
                inNanos=context.getTemporal().getLong(NANO_OF_SECOND);
            }
            if(inSecs==null){
                return false;
            }
            long inSec=inSecs;
            int inNano=NANO_OF_SECOND.checkValidIntValue(inNanos!=null?inNanos:0);
            // format mostly using LocalDateTime.toString
            if(inSec>=-SECONDS_0000_TO_1970){
                // current era
                long zeroSecs=inSec-SECONDS_PER_10000_YEARS+SECONDS_0000_TO_1970;
                long hi=Math.floorDiv(zeroSecs,SECONDS_PER_10000_YEARS)+1;
                long lo=Math.floorMod(zeroSecs,SECONDS_PER_10000_YEARS);
                LocalDateTime ldt=LocalDateTime.ofEpochSecond(lo-SECONDS_0000_TO_1970,0,ZoneOffset.UTC);
                if(hi>0){
                    buf.append('+').append(hi);
                }
                buf.append(ldt);
                if(ldt.getSecond()==0){
                    buf.append(":00");
                }
            }else{
                // before current era
                long zeroSecs=inSec+SECONDS_0000_TO_1970;
                long hi=zeroSecs/SECONDS_PER_10000_YEARS;
                long lo=zeroSecs%SECONDS_PER_10000_YEARS;
                LocalDateTime ldt=LocalDateTime.ofEpochSecond(lo-SECONDS_0000_TO_1970,0,ZoneOffset.UTC);
                int pos=buf.length();
                buf.append(ldt);
                if(ldt.getSecond()==0){
                    buf.append(":00");
                }
                if(hi<0){
                    if(ldt.getYear()==-10_000){
                        buf.replace(pos,pos+2,Long.toString(hi-1));
                    }else if(lo==0){
                        buf.insert(pos,hi);
                    }else{
                        buf.insert(pos+1,Math.abs(hi));
                    }
                }
            }
            // add fraction
            if((fractionalDigits<0&&inNano>0)||fractionalDigits>0){
                buf.append('.');
                int div=100_000_000;
                for(int i=0;((fractionalDigits==-1&&inNano>0)||
                        (fractionalDigits==-2&&(inNano>0||(i%3)!=0))||
                        i<fractionalDigits);i++){
                    int digit=inNano/div;
                    buf.append((char)(digit+'0'));
                    inNano=inNano-(digit*div);
                    div=div/10;
                }
            }
            buf.append('Z');
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context,CharSequence text,int position){
            // new context to avoid overwriting fields like year/month/day
            int minDigits=(fractionalDigits<0?0:fractionalDigits);
            int maxDigits=(fractionalDigits<0?9:fractionalDigits);
            CompositePrinterParser parser=new DateTimeFormatterBuilder()
                    .append(DateTimeFormatter.ISO_LOCAL_DATE).appendLiteral('T')
                    .appendValue(HOUR_OF_DAY,2).appendLiteral(':')
                    .appendValue(MINUTE_OF_HOUR,2).appendLiteral(':')
                    .appendValue(SECOND_OF_MINUTE,2)
                    .appendFraction(NANO_OF_SECOND,minDigits,maxDigits,true)
                    .appendLiteral('Z')
                    .toFormatter().toPrinterParser(false);
            DateTimeParseContext newContext=context.copy();
            int pos=parser.parse(newContext,text,position);
            if(pos<0){
                return pos;
            }
            // parser restricts most fields to 2 digits, so definitely int
            // correctly parsed nano is also guaranteed to be valid
            long yearParsed=newContext.getParsed(YEAR);
            int month=newContext.getParsed(MONTH_OF_YEAR).intValue();
            int day=newContext.getParsed(DAY_OF_MONTH).intValue();
            int hour=newContext.getParsed(HOUR_OF_DAY).intValue();
            int min=newContext.getParsed(MINUTE_OF_HOUR).intValue();
            Long secVal=newContext.getParsed(SECOND_OF_MINUTE);
            Long nanoVal=newContext.getParsed(NANO_OF_SECOND);
            int sec=(secVal!=null?secVal.intValue():0);
            int nano=(nanoVal!=null?nanoVal.intValue():0);
            int days=0;
            if(hour==24&&min==0&&sec==0&&nano==0){
                hour=0;
                days=1;
            }else if(hour==23&&min==59&&sec==60){
                context.setParsedLeapSecond();
                sec=59;
            }
            int year=(int)yearParsed%10_000;
            long instantSecs;
            try{
                LocalDateTime ldt=LocalDateTime.of(year,month,day,hour,min,sec,0).plusDays(days);
                instantSecs=ldt.toEpochSecond(ZoneOffset.UTC);
                instantSecs+=Math.multiplyExact(yearParsed/10_000L,SECONDS_PER_10000_YEARS);
            }catch(RuntimeException ex){
                return ~position;
            }
            int successPos=pos;
            successPos=context.setParsedField(INSTANT_SECONDS,instantSecs,position,successPos);
            return context.setParsedField(NANO_OF_SECOND,nano,position,successPos);
        }

        @Override
        public String toString(){
            return "Instant()";
        }
    }

    //-----------------------------------------------------------------------
    static final class OffsetIdPrinterParser implements DateTimePrinterParser{
        static final String[] PATTERNS=new String[]{
                "+HH","+HHmm","+HH:mm","+HHMM","+HH:MM","+HHMMss","+HH:MM:ss","+HHMMSS","+HH:MM:SS",
        };  // order used in pattern builder
        static final OffsetIdPrinterParser INSTANCE_ID_Z=new OffsetIdPrinterParser("+HH:MM:ss","Z");
        static final OffsetIdPrinterParser INSTANCE_ID_ZERO=new OffsetIdPrinterParser("+HH:MM:ss","0");
        private final String noOffsetText;
        private final int type;

        OffsetIdPrinterParser(String pattern,String noOffsetText){
            Objects.requireNonNull(pattern,"pattern");
            Objects.requireNonNull(noOffsetText,"noOffsetText");
            this.type=checkPattern(pattern);
            this.noOffsetText=noOffsetText;
        }

        private int checkPattern(String pattern){
            for(int i=0;i<PATTERNS.length;i++){
                if(PATTERNS[i].equals(pattern)){
                    return i;
                }
            }
            throw new IllegalArgumentException("Invalid zone offset pattern: "+pattern);
        }

        @Override
        public boolean format(DateTimePrintContext context,StringBuilder buf){
            Long offsetSecs=context.getValue(OFFSET_SECONDS);
            if(offsetSecs==null){
                return false;
            }
            int totalSecs=Math.toIntExact(offsetSecs);
            if(totalSecs==0){
                buf.append(noOffsetText);
            }else{
                int absHours=Math.abs((totalSecs/3600)%100);  // anything larger than 99 silently dropped
                int absMinutes=Math.abs((totalSecs/60)%60);
                int absSeconds=Math.abs(totalSecs%60);
                int bufPos=buf.length();
                int output=absHours;
                buf.append(totalSecs<0?"-":"+")
                        .append((char)(absHours/10+'0')).append((char)(absHours%10+'0'));
                if(type>=3||(type>=1&&absMinutes>0)){
                    buf.append((type%2)==0?":":"")
                            .append((char)(absMinutes/10+'0')).append((char)(absMinutes%10+'0'));
                    output+=absMinutes;
                    if(type>=7||(type>=5&&absSeconds>0)){
                        buf.append((type%2)==0?":":"")
                                .append((char)(absSeconds/10+'0')).append((char)(absSeconds%10+'0'));
                        output+=absSeconds;
                    }
                }
                if(output==0){
                    buf.setLength(bufPos);
                    buf.append(noOffsetText);
                }
            }
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context,CharSequence text,int position){
            int length=text.length();
            int noOffsetLen=noOffsetText.length();
            if(noOffsetLen==0){
                if(position==length){
                    return context.setParsedField(OFFSET_SECONDS,0,position,position);
                }
            }else{
                if(position==length){
                    return ~position;
                }
                if(context.subSequenceEquals(text,position,noOffsetText,0,noOffsetLen)){
                    return context.setParsedField(OFFSET_SECONDS,0,position,position+noOffsetLen);
                }
            }
            // parse normal plus/minus offset
            char sign=text.charAt(position);  // IOOBE if invalid position
            if(sign=='+'||sign=='-'){
                // starts
                int negative=(sign=='-'?-1:1);
                int[] array=new int[4];
                array[0]=position+1;
                if((parseNumber(array,1,text,true)||
                        parseNumber(array,2,text,type>=3)||
                        parseNumber(array,3,text,false))==false){
                    // success
                    long offsetSecs=negative*(array[1]*3600L+array[2]*60L+array[3]);
                    return context.setParsedField(OFFSET_SECONDS,offsetSecs,position,array[0]);
                }
            }
            // handle special case of empty no offset text
            if(noOffsetLen==0){
                return context.setParsedField(OFFSET_SECONDS,0,position,position+noOffsetLen);
            }
            return ~position;
        }

        private boolean parseNumber(int[] array,int arrayIndex,CharSequence parseText,boolean required){
            if((type+3)/2<arrayIndex){
                return false;  // ignore seconds/minutes
            }
            int pos=array[0];
            if((type%2)==0&&arrayIndex>1){
                if(pos+1>parseText.length()||parseText.charAt(pos)!=':'){
                    return required;
                }
                pos++;
            }
            if(pos+2>parseText.length()){
                return required;
            }
            char ch1=parseText.charAt(pos++);
            char ch2=parseText.charAt(pos++);
            if(ch1<'0'||ch1>'9'||ch2<'0'||ch2>'9'){
                return required;
            }
            int value=(ch1-48)*10+(ch2-48);
            if(value<0||value>59){
                return required;
            }
            array[arrayIndex]=value;
            array[0]=pos;
            return false;
        }

        @Override
        public String toString(){
            String converted=noOffsetText.replace("'","''");
            return "Offset("+PATTERNS[type]+",'"+converted+"')";
        }
    }

    //-----------------------------------------------------------------------
    static final class LocalizedOffsetIdPrinterParser implements DateTimePrinterParser{
        private final TextStyle style;

        LocalizedOffsetIdPrinterParser(TextStyle style){
            this.style=style;
        }

        private static StringBuilder appendHMS(StringBuilder buf,int t){
            return buf.append((char)(t/10+'0'))
                    .append((char)(t%10+'0'));
        }

        @Override
        public boolean format(DateTimePrintContext context,StringBuilder buf){
            Long offsetSecs=context.getValue(OFFSET_SECONDS);
            if(offsetSecs==null){
                return false;
            }
            String gmtText="GMT";  // TODO: get localized version of 'GMT'
            if(gmtText!=null){
                buf.append(gmtText);
            }
            int totalSecs=Math.toIntExact(offsetSecs);
            if(totalSecs!=0){
                int absHours=Math.abs((totalSecs/3600)%100);  // anything larger than 99 silently dropped
                int absMinutes=Math.abs((totalSecs/60)%60);
                int absSeconds=Math.abs(totalSecs%60);
                buf.append(totalSecs<0?"-":"+");
                if(style==TextStyle.FULL){
                    appendHMS(buf,absHours);
                    buf.append(':');
                    appendHMS(buf,absMinutes);
                    if(absSeconds!=0){
                        buf.append(':');
                        appendHMS(buf,absSeconds);
                    }
                }else{
                    if(absHours>=10){
                        buf.append((char)(absHours/10+'0'));
                    }
                    buf.append((char)(absHours%10+'0'));
                    if(absMinutes!=0||absSeconds!=0){
                        buf.append(':');
                        appendHMS(buf,absMinutes);
                        if(absSeconds!=0){
                            buf.append(':');
                            appendHMS(buf,absSeconds);
                        }
                    }
                }
            }
            return true;
        }

        int getDigit(CharSequence text,int position){
            char c=text.charAt(position);
            if(c<'0'||c>'9'){
                return -1;
            }
            return c-'0';
        }

        @Override
        public int parse(DateTimeParseContext context,CharSequence text,int position){
            int pos=position;
            int end=pos+text.length();
            String gmtText="GMT";  // TODO: get localized version of 'GMT'
            if(gmtText!=null){
                if(!context.subSequenceEquals(text,pos,gmtText,0,gmtText.length())){
                    return ~position;
                }
                pos+=gmtText.length();
            }
            // parse normal plus/minus offset
            int negative=0;
            if(pos==end){
                return context.setParsedField(OFFSET_SECONDS,0,position,pos);
            }
            char sign=text.charAt(pos);  // IOOBE if invalid position
            if(sign=='+'){
                negative=1;
            }else if(sign=='-'){
                negative=-1;
            }else{
                return context.setParsedField(OFFSET_SECONDS,0,position,pos);
            }
            pos++;
            int h=0;
            int m=0;
            int s=0;
            if(style==TextStyle.FULL){
                int h1=getDigit(text,pos++);
                int h2=getDigit(text,pos++);
                if(h1<0||h2<0||text.charAt(pos++)!=':'){
                    return ~position;
                }
                h=h1*10+h2;
                int m1=getDigit(text,pos++);
                int m2=getDigit(text,pos++);
                if(m1<0||m2<0){
                    return ~position;
                }
                m=m1*10+m2;
                if(pos+2<end&&text.charAt(pos)==':'){
                    int s1=getDigit(text,pos+1);
                    int s2=getDigit(text,pos+2);
                    if(s1>=0&&s2>=0){
                        s=s1*10+s2;
                        pos+=3;
                    }
                }
            }else{
                h=getDigit(text,pos++);
                if(h<0){
                    return ~position;
                }
                if(pos<end){
                    int h2=getDigit(text,pos);
                    if(h2>=0){
                        h=h*10+h2;
                        pos++;
                    }
                    if(pos+2<end&&text.charAt(pos)==':'){
                        if(pos+2<end&&text.charAt(pos)==':'){
                            int m1=getDigit(text,pos+1);
                            int m2=getDigit(text,pos+2);
                            if(m1>=0&&m2>=0){
                                m=m1*10+m2;
                                pos+=3;
                                if(pos+2<end&&text.charAt(pos)==':'){
                                    int s1=getDigit(text,pos+1);
                                    int s2=getDigit(text,pos+2);
                                    if(s1>=0&&s2>=0){
                                        s=s1*10+s2;
                                        pos+=3;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            long offsetSecs=negative*(h*3600L+m*60L+s);
            return context.setParsedField(OFFSET_SECONDS,offsetSecs,position,pos);
        }

        @Override
        public String toString(){
            return "LocalizedOffset("+style+")";
        }
    }

    //-----------------------------------------------------------------------
    static final class ZoneTextPrinterParser extends ZoneIdPrinterParser{
        private static final int STD=0;
        private static final int DST=1;
        private static final int GENERIC=2;
        private static final Map<String,SoftReference<Map<Locale,String[]>>> cache=
                new ConcurrentHashMap<>();
        private final TextStyle textStyle;
        // cache per instance for now
        private final Map<Locale,Entry<Integer,SoftReference<PrefixTree>>>
                cachedTree=new HashMap<>();
        private final Map<Locale,Entry<Integer,SoftReference<PrefixTree>>>
                cachedTreeCI=new HashMap<>();
        private Set<String> preferredZones;

        ZoneTextPrinterParser(TextStyle textStyle,Set<ZoneId> preferredZones){
            super(TemporalQueries.zone(),"ZoneText("+textStyle+")");
            this.textStyle=Objects.requireNonNull(textStyle,"textStyle");
            if(preferredZones!=null&&preferredZones.size()!=0){
                this.preferredZones=new HashSet<>();
                for(ZoneId id : preferredZones){
                    this.preferredZones.add(id.getId());
                }
            }
        }

        @Override
        public boolean format(DateTimePrintContext context,StringBuilder buf){
            ZoneId zone=context.getValue(TemporalQueries.zoneId());
            if(zone==null){
                return false;
            }
            String zname=zone.getId();
            if(!(zone instanceof ZoneOffset)){
                TemporalAccessor dt=context.getTemporal();
                String name=getDisplayName(zname,
                        dt.isSupported(ChronoField.INSTANT_SECONDS)
                                ?(zone.getRules().isDaylightSavings(Instant.from(dt))?DST:STD)
                                :GENERIC,
                        context.getLocale());
                if(name!=null){
                    zname=name;
                }
            }
            buf.append(zname);
            return true;
        }

        private String getDisplayName(String id,int type,Locale locale){
            if(textStyle==TextStyle.NARROW){
                return null;
            }
            String[] names;
            SoftReference<Map<Locale,String[]>> ref=cache.get(id);
            Map<Locale,String[]> perLocale=null;
            if(ref==null||(perLocale=ref.get())==null||
                    (names=perLocale.get(locale))==null){
                names=TimeZoneNameUtility.retrieveDisplayNames(id,locale);
                if(names==null){
                    return null;
                }
                names=Arrays.copyOfRange(names,0,7);
                names[5]=
                        TimeZoneNameUtility.retrieveGenericDisplayName(id,TimeZone.LONG,locale);
                if(names[5]==null){
                    names[5]=names[0]; // use the id
                }
                names[6]=
                        TimeZoneNameUtility.retrieveGenericDisplayName(id,TimeZone.SHORT,locale);
                if(names[6]==null){
                    names[6]=names[0];
                }
                if(perLocale==null){
                    perLocale=new ConcurrentHashMap<>();
                }
                perLocale.put(locale,names);
                cache.put(id,new SoftReference<>(perLocale));
            }
            switch(type){
                case STD:
                    return names[textStyle.zoneNameStyleIndex()+1];
                case DST:
                    return names[textStyle.zoneNameStyleIndex()+3];
            }
            return names[textStyle.zoneNameStyleIndex()+5];
        }

        @Override
        protected PrefixTree getTree(DateTimeParseContext context){
            if(textStyle==TextStyle.NARROW){
                return super.getTree(context);
            }
            Locale locale=context.getLocale();
            boolean isCaseSensitive=context.isCaseSensitive();
            Set<String> regionIds=ZoneRulesProvider.getAvailableZoneIds();
            int regionIdsSize=regionIds.size();
            Map<Locale,Entry<Integer,SoftReference<PrefixTree>>> cached=
                    isCaseSensitive?cachedTree:cachedTreeCI;
            Entry<Integer,SoftReference<PrefixTree>> entry=null;
            PrefixTree tree=null;
            String[][] zoneStrings=null;
            if((entry=cached.get(locale))==null||
                    (entry.getKey()!=regionIdsSize||
                            (tree=entry.getValue().get())==null)){
                tree=PrefixTree.newTree(context);
                zoneStrings=TimeZoneNameUtility.getZoneStrings(locale);
                for(String[] names : zoneStrings){
                    String zid=names[0];
                    if(!regionIds.contains(zid)){
                        continue;
                    }
                    tree.add(zid,zid);    // don't convert zid -> metazone
                    zid=ZoneName.toZid(zid,locale);
                    int i=textStyle==TextStyle.FULL?1:2;
                    for(;i<names.length;i+=2){
                        tree.add(names[i],zid);
                    }
                }
                // if we have a set of preferred zones, need a copy and
                // add the preferred zones again to overwrite
                if(preferredZones!=null){
                    for(String[] names : zoneStrings){
                        String zid=names[0];
                        if(!preferredZones.contains(zid)||!regionIds.contains(zid)){
                            continue;
                        }
                        int i=textStyle==TextStyle.FULL?1:2;
                        for(;i<names.length;i+=2){
                            tree.add(names[i],zid);
                        }
                    }
                }
                cached.put(locale,new SimpleImmutableEntry<>(regionIdsSize,new SoftReference<>(tree)));
            }
            return tree;
        }
    }

    //-----------------------------------------------------------------------
    static class ZoneIdPrinterParser implements DateTimePrinterParser{
        private static volatile Entry<Integer,PrefixTree> cachedPrefixTree;
        private static volatile Entry<Integer,PrefixTree> cachedPrefixTreeCI;
        private final TemporalQuery<ZoneId> query;
        private final String description;

        ZoneIdPrinterParser(TemporalQuery<ZoneId> query,String description){
            this.query=query;
            this.description=description;
        }

        @Override
        public boolean format(DateTimePrintContext context,StringBuilder buf){
            ZoneId zone=context.getValue(query);
            if(zone==null){
                return false;
            }
            buf.append(zone.getId());
            return true;
        }

        protected PrefixTree getTree(DateTimeParseContext context){
            // prepare parse tree
            Set<String> regionIds=ZoneRulesProvider.getAvailableZoneIds();
            final int regionIdsSize=regionIds.size();
            Entry<Integer,PrefixTree> cached=context.isCaseSensitive()
                    ?cachedPrefixTree:cachedPrefixTreeCI;
            if(cached==null||cached.getKey()!=regionIdsSize){
                synchronized(this){
                    cached=context.isCaseSensitive()?cachedPrefixTree:cachedPrefixTreeCI;
                    if(cached==null||cached.getKey()!=regionIdsSize){
                        cached=new SimpleImmutableEntry<>(regionIdsSize,PrefixTree.newTree(regionIds,context));
                        if(context.isCaseSensitive()){
                            cachedPrefixTree=cached;
                        }else{
                            cachedPrefixTreeCI=cached;
                        }
                    }
                }
            }
            return cached.getValue();
        }

        @Override
        public int parse(DateTimeParseContext context,CharSequence text,int position){
            int length=text.length();
            if(position>length){
                throw new IndexOutOfBoundsException();
            }
            if(position==length){
                return ~position;
            }
            // handle fixed time-zone IDs
            char nextChar=text.charAt(position);
            if(nextChar=='+'||nextChar=='-'){
                return parseOffsetBased(context,text,position,position,OffsetIdPrinterParser.INSTANCE_ID_Z);
            }else if(length>=position+2){
                char nextNextChar=text.charAt(position+1);
                if(context.charEquals(nextChar,'U')&&context.charEquals(nextNextChar,'T')){
                    if(length>=position+3&&context.charEquals(text.charAt(position+2),'C')){
                        return parseOffsetBased(context,text,position,position+3,OffsetIdPrinterParser.INSTANCE_ID_ZERO);
                    }
                    return parseOffsetBased(context,text,position,position+2,OffsetIdPrinterParser.INSTANCE_ID_ZERO);
                }else if(context.charEquals(nextChar,'G')&&length>=position+3&&
                        context.charEquals(nextNextChar,'M')&&context.charEquals(text.charAt(position+2),'T')){
                    return parseOffsetBased(context,text,position,position+3,OffsetIdPrinterParser.INSTANCE_ID_ZERO);
                }
            }
            // parse
            PrefixTree tree=getTree(context);
            ParsePosition ppos=new ParsePosition(position);
            String parsedZoneId=tree.match(text,ppos);
            if(parsedZoneId==null){
                if(context.charEquals(nextChar,'Z')){
                    context.setParsed(ZoneOffset.UTC);
                    return position+1;
                }
                return ~position;
            }
            context.setParsed(ZoneId.of(parsedZoneId));
            return ppos.getIndex();
        }

        private int parseOffsetBased(DateTimeParseContext context,CharSequence text,int prefixPos,int position,OffsetIdPrinterParser parser){
            String prefix=text.toString().substring(prefixPos,position).toUpperCase();
            if(position>=text.length()){
                context.setParsed(ZoneId.of(prefix));
                return position;
            }
            // '0' or 'Z' after prefix is not part of a valid ZoneId; use bare prefix
            if(text.charAt(position)=='0'||
                    context.charEquals(text.charAt(position),'Z')){
                context.setParsed(ZoneId.of(prefix));
                return position;
            }
            DateTimeParseContext newContext=context.copy();
            int endPos=parser.parse(newContext,text,position);
            try{
                if(endPos<0){
                    if(parser==OffsetIdPrinterParser.INSTANCE_ID_Z){
                        return ~prefixPos;
                    }
                    context.setParsed(ZoneId.of(prefix));
                    return position;
                }
                int offset=(int)newContext.getParsed(OFFSET_SECONDS).longValue();
                ZoneOffset zoneOffset=ZoneOffset.ofTotalSeconds(offset);
                context.setParsed(ZoneId.ofOffset(prefix,zoneOffset));
                return endPos;
            }catch(DateTimeException dte){
                return ~prefixPos;
            }
        }

        @Override
        public String toString(){
            return description;
        }
    }

    //-----------------------------------------------------------------------
    static class PrefixTree{
        protected String key;
        protected String value;
        protected char c0;    // performance optimization to avoid the
        // boundary check cost of key.charat(0)
        protected PrefixTree child;
        protected PrefixTree sibling;

        private PrefixTree(String k,String v,PrefixTree child){
            this.key=k;
            this.value=v;
            this.child=child;
            if(k.length()==0){
                c0=0xffff;
            }else{
                c0=key.charAt(0);
            }
        }

        public static PrefixTree newTree(Set<String> keys,DateTimeParseContext context){
            PrefixTree tree=newTree(context);
            for(String k : keys){
                tree.add0(k,k);
            }
            return tree;
        }

        public static PrefixTree newTree(DateTimeParseContext context){
            //if (!context.isStrict()) {
            //    return new LENIENT("", null, null);
            //}
            if(context.isCaseSensitive()){
                return new PrefixTree("",null,null);
            }
            return new CI("",null,null);
        }

        public PrefixTree copyTree(){
            PrefixTree copy=new PrefixTree(key,value,null);
            if(child!=null){
                copy.child=child.copyTree();
            }
            if(sibling!=null){
                copy.sibling=sibling.copyTree();
            }
            return copy;
        }

        public boolean add(String k,String v){
            return add0(k,v);
        }

        private boolean add0(String k,String v){
            k=toKey(k);
            int prefixLen=prefixLength(k);
            if(prefixLen==key.length()){
                if(prefixLen<k.length()){  // down the tree
                    String subKey=k.substring(prefixLen);
                    PrefixTree c=child;
                    while(c!=null){
                        if(isEqual(c.c0,subKey.charAt(0))){
                            return c.add0(subKey,v);
                        }
                        c=c.sibling;
                    }
                    // add the node as the child of the current node
                    c=newNode(subKey,v,null);
                    c.sibling=child;
                    child=c;
                    return true;
                }
                // have an existing <key, value> already, overwrite it
                // if (value != null) {
                //    return false;
                //}
                value=v;
                return true;
            }
            // split the existing node
            PrefixTree n1=newNode(key.substring(prefixLen),value,child);
            key=k.substring(0,prefixLen);
            child=n1;
            if(prefixLen<k.length()){
                PrefixTree n2=newNode(k.substring(prefixLen),v,null);
                child.sibling=n2;
                value=null;
            }else{
                value=v;
            }
            return true;
        }

        protected String toKey(String k){
            return k;
        }

        protected PrefixTree newNode(String k,String v,PrefixTree child){
            return new PrefixTree(k,v,child);
        }

        private int prefixLength(String k){
            int off=0;
            while(off<k.length()&&off<key.length()){
                if(!isEqual(k.charAt(off),key.charAt(off))){
                    return off;
                }
                off++;
            }
            return off;
        }

        protected boolean isEqual(char c1,char c2){
            return c1==c2;
        }

        public String match(CharSequence text,int off,int end){
            if(!prefixOf(text,off,end)){
                return null;
            }
            if(child!=null&&(off+=key.length())!=end){
                PrefixTree c=child;
                do{
                    if(isEqual(c.c0,text.charAt(off))){
                        String found=c.match(text,off,end);
                        if(found!=null){
                            return found;
                        }
                        return value;
                    }
                    c=c.sibling;
                }while(c!=null);
            }
            return value;
        }

        protected boolean prefixOf(CharSequence text,int off,int end){
            if(text instanceof String){
                return ((String)text).startsWith(key,off);
            }
            int len=key.length();
            if(len>end-off){
                return false;
            }
            int off0=0;
            while(len-->0){
                if(!isEqual(key.charAt(off0++),text.charAt(off++))){
                    return false;
                }
            }
            return true;
        }

        public String match(CharSequence text,ParsePosition pos){
            int off=pos.getIndex();
            int end=text.length();
            if(!prefixOf(text,off,end)){
                return null;
            }
            off+=key.length();
            if(child!=null&&off!=end){
                PrefixTree c=child;
                do{
                    if(isEqual(c.c0,text.charAt(off))){
                        pos.setIndex(off);
                        String found=c.match(text,pos);
                        if(found!=null){
                            return found;
                        }
                        break;
                    }
                    c=c.sibling;
                }while(c!=null);
            }
            pos.setIndex(off);
            return value;
        }

        private static class CI extends PrefixTree{
            private CI(String k,String v,PrefixTree child){
                super(k,v,child);
            }

            @Override
            protected CI newNode(String k,String v,PrefixTree child){
                return new CI(k,v,child);
            }

            @Override
            protected boolean isEqual(char c1,char c2){
                return DateTimeParseContext.charEqualsIgnoreCase(c1,c2);
            }

            @Override
            protected boolean prefixOf(CharSequence text,int off,int end){
                int len=key.length();
                if(len>end-off){
                    return false;
                }
                int off0=0;
                while(len-->0){
                    if(!isEqual(key.charAt(off0++),text.charAt(off++))){
                        return false;
                    }
                }
                return true;
            }
        }

        private static class LENIENT extends CI{
            private LENIENT(String k,String v,PrefixTree child){
                super(k,v,child);
            }

            @Override
            protected CI newNode(String k,String v,PrefixTree child){
                return new LENIENT(k,v,child);
            }

            @Override
            public String match(CharSequence text,ParsePosition pos){
                int off=pos.getIndex();
                int end=text.length();
                int len=key.length();
                int koff=0;
                while(koff<len&&off<end){
                    if(isLenientChar(text.charAt(off))){
                        off++;
                        continue;
                    }
                    if(!isEqual(key.charAt(koff++),text.charAt(off++))){
                        return null;
                    }
                }
                if(koff!=len){
                    return null;
                }
                if(child!=null&&off!=end){
                    int off0=off;
                    while(off0<end&&isLenientChar(text.charAt(off0))){
                        off0++;
                    }
                    if(off0<end){
                        PrefixTree c=child;
                        do{
                            if(isEqual(c.c0,text.charAt(off0))){
                                pos.setIndex(off0);
                                String found=c.match(text,pos);
                                if(found!=null){
                                    return found;
                                }
                                break;
                            }
                            c=c.sibling;
                        }while(c!=null);
                    }
                }
                pos.setIndex(off);
                return value;
            }

            protected String toKey(String k){
                for(int i=0;i<k.length();i++){
                    if(isLenientChar(k.charAt(i))){
                        StringBuilder sb=new StringBuilder(k.length());
                        sb.append(k,0,i);
                        i++;
                        while(i<k.length()){
                            if(!isLenientChar(k.charAt(i))){
                                sb.append(k.charAt(i));
                            }
                            i++;
                        }
                        return sb.toString();
                    }
                }
                return k;
            }

            private boolean isLenientChar(char c){
                return c==' '||c=='_'||c=='/';
            }
        }
    }

    //-----------------------------------------------------------------------
    static final class ChronoPrinterParser implements DateTimePrinterParser{
        private final TextStyle textStyle;

        ChronoPrinterParser(TextStyle textStyle){
            // validated by caller
            this.textStyle=textStyle;
        }

        @Override
        public boolean format(DateTimePrintContext context,StringBuilder buf){
            Chronology chrono=context.getValue(TemporalQueries.chronology());
            if(chrono==null){
                return false;
            }
            if(textStyle==null){
                buf.append(chrono.getId());
            }else{
                buf.append(getChronologyName(chrono,context.getLocale()));
            }
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context,CharSequence text,int position){
            // simple looping parser to find the chronology
            if(position<0||position>text.length()){
                throw new IndexOutOfBoundsException();
            }
            Set<Chronology> chronos=Chronology.getAvailableChronologies();
            Chronology bestMatch=null;
            int matchLen=-1;
            for(Chronology chrono : chronos){
                String name;
                if(textStyle==null){
                    name=chrono.getId();
                }else{
                    name=getChronologyName(chrono,context.getLocale());
                }
                int nameLen=name.length();
                if(nameLen>matchLen&&context.subSequenceEquals(text,position,name,0,nameLen)){
                    bestMatch=chrono;
                    matchLen=nameLen;
                }
            }
            if(bestMatch==null){
                return ~position;
            }
            context.setParsed(bestMatch);
            return position+matchLen;
        }

        private String getChronologyName(Chronology chrono,Locale locale){
            String key="calendarname."+chrono.getCalendarType();
            String name=DateTimeTextProvider.getLocalizedResource(key,locale);
            return name!=null?name:chrono.getId();
        }
    }

    //-----------------------------------------------------------------------
    static final class LocalizedPrinterParser implements DateTimePrinterParser{
        private static final ConcurrentMap<String,DateTimeFormatter> FORMATTER_CACHE=new ConcurrentHashMap<>(16,0.75f,2);
        private final FormatStyle dateStyle;
        private final FormatStyle timeStyle;

        LocalizedPrinterParser(FormatStyle dateStyle,FormatStyle timeStyle){
            // validated by caller
            this.dateStyle=dateStyle;
            this.timeStyle=timeStyle;
        }

        @Override
        public boolean format(DateTimePrintContext context,StringBuilder buf){
            Chronology chrono=Chronology.from(context.getTemporal());
            return formatter(context.getLocale(),chrono).toPrinterParser(false).format(context,buf);
        }

        @Override
        public int parse(DateTimeParseContext context,CharSequence text,int position){
            Chronology chrono=context.getEffectiveChronology();
            return formatter(context.getLocale(),chrono).toPrinterParser(false).parse(context,text,position);
        }

        private DateTimeFormatter formatter(Locale locale,Chronology chrono){
            String key=chrono.getId()+'|'+locale.toString()+'|'+dateStyle+timeStyle;
            DateTimeFormatter formatter=FORMATTER_CACHE.get(key);
            if(formatter==null){
                String pattern=getLocalizedDateTimePattern(dateStyle,timeStyle,chrono,locale);
                formatter=new DateTimeFormatterBuilder().appendPattern(pattern).toFormatter(locale);
                DateTimeFormatter old=FORMATTER_CACHE.putIfAbsent(key,formatter);
                if(old!=null){
                    formatter=old;
                }
            }
            return formatter;
        }

        @Override
        public String toString(){
            return "Localized("+(dateStyle!=null?dateStyle:"")+","+
                    (timeStyle!=null?timeStyle:"")+")";
        }
    }

    //-----------------------------------------------------------------------
    static final class WeekBasedFieldPrinterParser implements DateTimePrinterParser{
        private char chr;
        private int count;

        WeekBasedFieldPrinterParser(char chr,int count){
            this.chr=chr;
            this.count=count;
        }

        @Override
        public boolean format(DateTimePrintContext context,StringBuilder buf){
            return printerParser(context.getLocale()).format(context,buf);
        }

        @Override
        public int parse(DateTimeParseContext context,CharSequence text,int position){
            return printerParser(context.getLocale()).parse(context,text,position);
        }

        private DateTimePrinterParser printerParser(Locale locale){
            WeekFields weekDef=WeekFields.of(locale);
            TemporalField field=null;
            switch(chr){
                case 'Y':
                    field=weekDef.weekBasedYear();
                    if(count==2){
                        return new ReducedPrinterParser(field,2,2,0,ReducedPrinterParser.BASE_DATE,0);
                    }else{
                        return new NumberPrinterParser(field,count,19,
                                (count<4)?SignStyle.NORMAL:SignStyle.EXCEEDS_PAD,-1);
                    }
                case 'e':
                case 'c':
                    field=weekDef.dayOfWeek();
                    break;
                case 'w':
                    field=weekDef.weekOfWeekBasedYear();
                    break;
                case 'W':
                    field=weekDef.weekOfMonth();
                    break;
                default:
                    throw new IllegalStateException("unreachable");
            }
            return new NumberPrinterParser(field,(count==2?2:1),2,SignStyle.NOT_NEGATIVE);
        }

        @Override
        public String toString(){
            StringBuilder sb=new StringBuilder(30);
            sb.append("Localized(");
            if(chr=='Y'){
                if(count==1){
                    sb.append("WeekBasedYear");
                }else if(count==2){
                    sb.append("ReducedValue(WeekBasedYear,2,2,2000-01-01)");
                }else{
                    sb.append("WeekBasedYear,").append(count).append(",")
                            .append(19).append(",")
                            .append((count<4)?SignStyle.NORMAL:SignStyle.EXCEEDS_PAD);
                }
            }else{
                switch(chr){
                    case 'c':
                    case 'e':
                        sb.append("DayOfWeek");
                        break;
                    case 'w':
                        sb.append("WeekOfWeekBasedYear");
                        break;
                    case 'W':
                        sb.append("WeekOfMonth");
                        break;
                    default:
                        break;
                }
                sb.append(",");
                sb.append(count);
            }
            sb.append(")");
            return sb.toString();
        }
    }
}
