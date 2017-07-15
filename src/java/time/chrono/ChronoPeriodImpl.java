/**
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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
 * Copyright (c) 2013, Stephen Colebourne & Michael Nascimento Santos
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
/** Copyright (c) 2013, Stephen Colebourne & Michael Nascimento Santos
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
import java.time.DateTimeException;
import java.time.temporal.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoUnit.*;

final class ChronoPeriodImpl
        implements ChronoPeriod, Serializable{
    // this class is only used by JDK chronology implementations and makes assumptions based on that fact
    private static final long serialVersionUID=57387258289L;
    private static final List<TemporalUnit> SUPPORTED_UNITS=
            Collections.unmodifiableList(Arrays.<TemporalUnit>asList(YEARS,MONTHS,DAYS));
    final int years;
    final int months;
    final int days;
    private final Chronology chrono;

    ChronoPeriodImpl(Chronology chrono,int years,int months,int days){
        Objects.requireNonNull(chrono,"chrono");
        this.chrono=chrono;
        this.years=years;
        this.months=months;
        this.days=days;
    }

    static ChronoPeriodImpl readExternal(DataInput in) throws IOException{
        Chronology chrono=Chronology.of(in.readUTF());
        int years=in.readInt();
        int months=in.readInt();
        int days=in.readInt();
        return new ChronoPeriodImpl(chrono,years,months,days);
    }

    //-----------------------------------------------------------------------
    @Override
    public long get(TemporalUnit unit){
        if(unit==ChronoUnit.YEARS){
            return years;
        }else if(unit==ChronoUnit.MONTHS){
            return months;
        }else if(unit==ChronoUnit.DAYS){
            return days;
        }else{
            throw new UnsupportedTemporalTypeException("Unsupported unit: "+unit);
        }
    }

    @Override
    public List<TemporalUnit> getUnits(){
        return ChronoPeriodImpl.SUPPORTED_UNITS;
    }

    @Override
    public Chronology getChronology(){
        return chrono;
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isZero(){
        return years==0&&months==0&&days==0;
    }

    @Override
    public boolean isNegative(){
        return years<0||months<0||days<0;
    }

    //-----------------------------------------------------------------------
    @Override
    public ChronoPeriod plus(TemporalAmount amountToAdd){
        ChronoPeriodImpl amount=validateAmount(amountToAdd);
        return new ChronoPeriodImpl(
                chrono,
                Math.addExact(years,amount.years),
                Math.addExact(months,amount.months),
                Math.addExact(days,amount.days));
    }

    @Override
    public ChronoPeriod minus(TemporalAmount amountToSubtract){
        ChronoPeriodImpl amount=validateAmount(amountToSubtract);
        return new ChronoPeriodImpl(
                chrono,
                Math.subtractExact(years,amount.years),
                Math.subtractExact(months,amount.months),
                Math.subtractExact(days,amount.days));
    }

    //-----------------------------------------------------------------------
    @Override
    public ChronoPeriod multipliedBy(int scalar){
        if(this.isZero()||scalar==1){
            return this;
        }
        return new ChronoPeriodImpl(
                chrono,
                Math.multiplyExact(years,scalar),
                Math.multiplyExact(months,scalar),
                Math.multiplyExact(days,scalar));
    }

    //-----------------------------------------------------------------------
    @Override
    public ChronoPeriod normalized(){
        long monthRange=monthRange();
        if(monthRange>0){
            long totalMonths=years*monthRange+months;
            long splitYears=totalMonths/monthRange;
            int splitMonths=(int)(totalMonths%monthRange);  // no overflow
            if(splitYears==years&&splitMonths==months){
                return this;
            }
            return new ChronoPeriodImpl(chrono,Math.toIntExact(splitYears),splitMonths,days);
        }
        return this;
    }

    private long monthRange(){
        ValueRange startRange=chrono.range(MONTH_OF_YEAR);
        if(startRange.isFixed()&&startRange.isIntValue()){
            return startRange.getMaximum()-startRange.getMinimum()+1;
        }
        return -1;
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
            long monthRange=monthRange();
            if(monthRange>0){
                temporal=temporal.plus(years*monthRange+months,MONTHS);
            }else{
                if(years!=0){
                    temporal=temporal.plus(years,YEARS);
                }
                temporal=temporal.plus(months,MONTHS);
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
            long monthRange=monthRange();
            if(monthRange>0){
                temporal=temporal.minus(years*monthRange+months,MONTHS);
            }else{
                if(years!=0){
                    temporal=temporal.minus(years,YEARS);
                }
                temporal=temporal.minus(months,MONTHS);
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
        if(temporalChrono!=null&&chrono.equals(temporalChrono)==false){
            throw new DateTimeException("Chronology mismatch, expected: "+chrono.getId()+", actual: "+temporalChrono.getId());
        }
    }

    private ChronoPeriodImpl validateAmount(TemporalAmount amount){
        Objects.requireNonNull(amount,"amount");
        if(amount instanceof ChronoPeriodImpl==false){
            throw new DateTimeException("Unable to obtain ChronoPeriod from TemporalAmount: "+amount.getClass());
        }
        ChronoPeriodImpl period=(ChronoPeriodImpl)amount;
        if(chrono.equals(period.getChronology())==false){
            throw new ClassCastException("Chronology mismatch, expected: "+chrono.getId()+", actual: "+period.getChronology().getId());
        }
        return period;
    }

    @Override
    public int hashCode(){
        return (years+Integer.rotateLeft(months,8)+Integer.rotateLeft(days,16))^chrono.hashCode();
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj){
        if(this==obj){
            return true;
        }
        if(obj instanceof ChronoPeriodImpl){
            ChronoPeriodImpl other=(ChronoPeriodImpl)obj;
            return years==other.years&&months==other.months&&
                    days==other.days&&chrono.equals(other.chrono);
        }
        return false;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString(){
        if(isZero()){
            return getChronology().toString()+" P0D";
        }else{
            StringBuilder buf=new StringBuilder();
            buf.append(getChronology().toString()).append(' ').append('P');
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
    protected Object writeReplace(){
        return new Ser(Ser.CHRONO_PERIOD_TYPE,this);
    }

    private void readObject(ObjectInputStream s) throws ObjectStreamException{
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(DataOutput out) throws IOException{
        out.writeUTF(chrono.getId());
        out.writeInt(years);
        out.writeInt(months);
        out.writeInt(days);
    }
}
