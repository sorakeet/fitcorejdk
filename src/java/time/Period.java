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
 * Copyright (c) 2008-2012, Stephen Colebourne & Michael Nascimento Santos
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
import java.time.chrono.ChronoPeriod;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeParseException;
import java.time.temporal.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.temporal.ChronoUnit.*;

public final class Period
        implements ChronoPeriod, Serializable{
    public static final Period ZERO=new Period(0,0,0);
    private static final long serialVersionUID=-3587258372562876L;
    private static final Pattern PATTERN=
            Pattern.compile("([-+]?)P(?:([-+]?[0-9]+)Y)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)W)?(?:([-+]?[0-9]+)D)?",Pattern.CASE_INSENSITIVE);
    private static final List<TemporalUnit> SUPPORTED_UNITS=
            Collections.unmodifiableList(Arrays.<TemporalUnit>asList(YEARS,MONTHS,DAYS));
    private final int years;
    private final int months;
    private final int days;

    private Period(int years,int months,int days){
        this.years=years;
        this.months=months;
        this.days=days;
    }

    //-----------------------------------------------------------------------
    public static Period ofYears(int years){
        return create(years,0,0);
    }

    //-----------------------------------------------------------------------
    private static Period create(int years,int months,int days){
        if((years|months|days)==0){
            return ZERO;
        }
        return new Period(years,months,days);
    }

    public static Period ofMonths(int months){
        return create(0,months,0);
    }

    public static Period ofWeeks(int weeks){
        return create(0,0,Math.multiplyExact(weeks,7));
    }

    public static Period ofDays(int days){
        return create(0,0,days);
    }

    //-----------------------------------------------------------------------
    public static Period parse(CharSequence text){
        Objects.requireNonNull(text,"text");
        Matcher matcher=PATTERN.matcher(text);
        if(matcher.matches()){
            int negate=("-".equals(matcher.group(1))?-1:1);
            String yearMatch=matcher.group(2);
            String monthMatch=matcher.group(3);
            String weekMatch=matcher.group(4);
            String dayMatch=matcher.group(5);
            if(yearMatch!=null||monthMatch!=null||dayMatch!=null||weekMatch!=null){
                try{
                    int years=parseNumber(text,yearMatch,negate);
                    int months=parseNumber(text,monthMatch,negate);
                    int weeks=parseNumber(text,weekMatch,negate);
                    int days=parseNumber(text,dayMatch,negate);
                    days=Math.addExact(days,Math.multiplyExact(weeks,7));
                    return create(years,months,days);
                }catch(NumberFormatException ex){
                    throw new DateTimeParseException("Text cannot be parsed to a Period",text,0,ex);
                }
            }
        }
        throw new DateTimeParseException("Text cannot be parsed to a Period",text,0);
    }

    private static int parseNumber(CharSequence text,String str,int negate){
        if(str==null){
            return 0;
        }
        int val=Integer.parseInt(str);
        try{
            return Math.multiplyExact(val,negate);
        }catch(ArithmeticException ex){
            throw new DateTimeParseException("Text cannot be parsed to a Period",text,0,ex);
        }
    }

    //-----------------------------------------------------------------------
    public static Period between(LocalDate startDateInclusive,LocalDate endDateExclusive){
        return startDateInclusive.until(endDateExclusive);
    }

    static Period readExternal(DataInput in) throws IOException{
        int years=in.readInt();
        int months=in.readInt();
        int days=in.readInt();
        return Period.of(years,months,days);
    }

    //-----------------------------------------------------------------------
    public static Period of(int years,int months,int days){
        return create(years,months,days);
    }

    //-----------------------------------------------------------------------
    @Override
    public long get(TemporalUnit unit){
        if(unit==ChronoUnit.YEARS){
            return getYears();
        }else if(unit==ChronoUnit.MONTHS){
            return getMonths();
        }else if(unit==ChronoUnit.DAYS){
            return getDays();
        }else{
            throw new UnsupportedTemporalTypeException("Unsupported unit: "+unit);
        }
    }

    @Override
    public List<TemporalUnit> getUnits(){
        return SUPPORTED_UNITS;
    }

    @Override
    public IsoChronology getChronology(){
        return IsoChronology.INSTANCE;
    }

    //-----------------------------------------------------------------------
    public boolean isZero(){
        return (this==ZERO);
    }

    public boolean isNegative(){
        return years<0||months<0||days<0;
    }

    //-----------------------------------------------------------------------
    public Period plus(TemporalAmount amountToAdd){
        Period isoAmount=Period.from(amountToAdd);
        return create(
                Math.addExact(years,isoAmount.years),
                Math.addExact(months,isoAmount.months),
                Math.addExact(days,isoAmount.days));
    }

    //-----------------------------------------------------------------------
    public static Period from(TemporalAmount amount){
        if(amount instanceof Period){
            return (Period)amount;
        }
        if(amount instanceof ChronoPeriod){
            if(IsoChronology.INSTANCE.equals(((ChronoPeriod)amount).getChronology())==false){
                throw new DateTimeException("Period requires ISO chronology: "+amount);
            }
        }
        Objects.requireNonNull(amount,"amount");
        int years=0;
        int months=0;
        int days=0;
        for(TemporalUnit unit : amount.getUnits()){
            long unitAmount=amount.get(unit);
            if(unit==ChronoUnit.YEARS){
                years=Math.toIntExact(unitAmount);
            }else if(unit==ChronoUnit.MONTHS){
                months=Math.toIntExact(unitAmount);
            }else if(unit==ChronoUnit.DAYS){
                days=Math.toIntExact(unitAmount);
            }else{
                throw new DateTimeException("Unit must be Years, Months or Days, but was "+unit);
            }
        }
        return create(years,months,days);
    }

    //-----------------------------------------------------------------------
    public Period minus(TemporalAmount amountToSubtract){
        Period isoAmount=Period.from(amountToSubtract);
        return create(
                Math.subtractExact(years,isoAmount.years),
                Math.subtractExact(months,isoAmount.months),
                Math.subtractExact(days,isoAmount.days));
    }

    //-----------------------------------------------------------------------
    public Period multipliedBy(int scalar){
        if(this==ZERO||scalar==1){
            return this;
        }
        return create(
                Math.multiplyExact(years,scalar),
                Math.multiplyExact(months,scalar),
                Math.multiplyExact(days,scalar));
    }

    public Period negated(){
        return multipliedBy(-1);
    }

    //-----------------------------------------------------------------------
    public Period normalized(){
        long totalMonths=toTotalMonths();
        long splitYears=totalMonths/12;
        int splitMonths=(int)(totalMonths%12);  // no overflow
        if(splitYears==years&&splitMonths==months){
            return this;
        }
        return create(Math.toIntExact(splitYears),splitMonths,days);
    }

    public long toTotalMonths(){
        return years*12L+months;  // no overflow
    }

    //-------------------------------------------------------------------------
    @Override
    public Temporal addTo(Temporal temporal){
        validateChrono(temporal);
        if(months==0){
            if(years!=0){
                temporal=temporal.plus(years,YEARS);
            }
        }else{
            long totalMonths=toTotalMonths();
            if(totalMonths!=0){
                temporal=temporal.plus(totalMonths,MONTHS);
            }
        }
        if(days!=0){
            temporal=temporal.plus(days,DAYS);
        }
        return temporal;
    }

    @Override
    public Temporal subtractFrom(Temporal temporal){
        validateChrono(temporal);
        if(months==0){
            if(years!=0){
                temporal=temporal.minus(years,YEARS);
            }
        }else{
            long totalMonths=toTotalMonths();
            if(totalMonths!=0){
                temporal=temporal.minus(totalMonths,MONTHS);
            }
        }
        if(days!=0){
            temporal=temporal.minus(days,DAYS);
        }
        return temporal;
    }

    private void validateChrono(TemporalAccessor temporal){
        Objects.requireNonNull(temporal,"temporal");
        Chronology temporalChrono=temporal.query(TemporalQueries.chronology());
        if(temporalChrono!=null&&IsoChronology.INSTANCE.equals(temporalChrono)==false){
            throw new DateTimeException("Chronology mismatch, expected: ISO, actual: "+temporalChrono.getId());
        }
    }

    //-----------------------------------------------------------------------
    public int getYears(){
        return years;
    }

    public int getMonths(){
        return months;
    }

    public int getDays(){
        return days;
    }

    //-----------------------------------------------------------------------
    public Period withYears(int years){
        if(years==this.years){
            return this;
        }
        return create(years,months,days);
    }

    public Period withMonths(int months){
        if(months==this.months){
            return this;
        }
        return create(years,months,days);
    }

    public Period withDays(int days){
        if(days==this.days){
            return this;
        }
        return create(years,months,days);
    }

    public Period minusYears(long yearsToSubtract){
        return (yearsToSubtract==Long.MIN_VALUE?plusYears(Long.MAX_VALUE).plusYears(1):plusYears(-yearsToSubtract));
    }

    public Period plusYears(long yearsToAdd){
        if(yearsToAdd==0){
            return this;
        }
        return create(Math.toIntExact(Math.addExact(years,yearsToAdd)),months,days);
    }

    public Period minusMonths(long monthsToSubtract){
        return (monthsToSubtract==Long.MIN_VALUE?plusMonths(Long.MAX_VALUE).plusMonths(1):plusMonths(-monthsToSubtract));
    }

    public Period plusMonths(long monthsToAdd){
        if(monthsToAdd==0){
            return this;
        }
        return create(years,Math.toIntExact(Math.addExact(months,monthsToAdd)),days);
    }

    public Period minusDays(long daysToSubtract){
        return (daysToSubtract==Long.MIN_VALUE?plusDays(Long.MAX_VALUE).plusDays(1):plusDays(-daysToSubtract));
    }

    public Period plusDays(long daysToAdd){
        if(daysToAdd==0){
            return this;
        }
        return create(years,months,Math.toIntExact(Math.addExact(days,daysToAdd)));
    }

    @Override
    public int hashCode(){
        return years+Integer.rotateLeft(months,8)+Integer.rotateLeft(days,16);
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj){
        if(this==obj){
            return true;
        }
        if(obj instanceof Period){
            Period other=(Period)obj;
            return years==other.years&&
                    months==other.months&&
                    days==other.days;
        }
        return false;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString(){
        if(this==ZERO){
            return "P0D";
        }else{
            StringBuilder buf=new StringBuilder();
            buf.append('P');
            if(years!=0){
                buf.append(years).append('Y');
            }
            if(months!=0){
                buf.append(months).append('M');
            }
            if(days!=0){
                buf.append(days).append('D');
            }
            return buf.toString();
        }
    }

    //-----------------------------------------------------------------------
    private Object writeReplace(){
        return new Ser(Ser.PERIOD_TYPE,this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException{
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(DataOutput out) throws IOException{
        out.writeInt(years);
        out.writeInt(months);
        out.writeInt(days);
    }
}
