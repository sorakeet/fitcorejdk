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
 * Copyright (c) 2012, Stephen Colebourne & Michael Nascimento Santos
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
 * Copyright (c) 2012, Stephen Colebourne & Michael Nascimento Santos
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

import sun.util.calendar.CalendarDate;

import java.io.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.temporal.TemporalField;
import java.time.temporal.ValueRange;
import java.util.Arrays;
import java.util.Objects;

import static java.time.chrono.JapaneseDate.MEIJI_6_ISODATE;
import static java.time.temporal.ChronoField.ERA;

public final class JapaneseEra
        implements Era, Serializable{
    public static final JapaneseEra MEIJI=new JapaneseEra(-1,LocalDate.of(1868,1,1));
    public static final JapaneseEra TAISHO=new JapaneseEra(0,LocalDate.of(1912,7,30));
    public static final JapaneseEra SHOWA=new JapaneseEra(1,LocalDate.of(1926,12,25));
    public static final JapaneseEra HEISEI=new JapaneseEra(2,LocalDate.of(1989,1,8));
    // The offset value to 0-based index from the era value.
    // i.e., getValue() + ERA_OFFSET == 0-based index
    static final int ERA_OFFSET=2;
    static final sun.util.calendar.Era[] ERA_CONFIG;
    // the number of defined JapaneseEra constants.
    // There could be an extra era defined in its configuration.
    private static final int N_ERA_CONSTANTS=HEISEI.getValue()+ERA_OFFSET;
    private static final long serialVersionUID=1466499369062886794L;
    // array for the singleton JapaneseEra instances
    private static final JapaneseEra[] KNOWN_ERAS;

    static{
        ERA_CONFIG=JapaneseChronology.JCAL.getEras();
        KNOWN_ERAS=new JapaneseEra[ERA_CONFIG.length];
        KNOWN_ERAS[0]=MEIJI;
        KNOWN_ERAS[1]=TAISHO;
        KNOWN_ERAS[2]=SHOWA;
        KNOWN_ERAS[3]=HEISEI;
        for(int i=N_ERA_CONSTANTS;i<ERA_CONFIG.length;i++){
            CalendarDate date=ERA_CONFIG[i].getSinceDate();
            LocalDate isoDate=LocalDate.of(date.getYear(),date.getMonth(),date.getDayOfMonth());
            KNOWN_ERAS[i]=new JapaneseEra(i-ERA_OFFSET+1,isoDate);
        }
    }

    ;
    private final transient int eraValue;
    // the first day of the era
    private final transient LocalDate since;

    private JapaneseEra(int eraValue,LocalDate since){
        this.eraValue=eraValue;
        this.since=since;
    }

    public static JapaneseEra valueOf(String japaneseEra){
        Objects.requireNonNull(japaneseEra,"japaneseEra");
        for(JapaneseEra era : KNOWN_ERAS){
            if(era.getName().equals(japaneseEra)){
                return era;
            }
        }
        throw new IllegalArgumentException("japaneseEra is invalid");
    }

    public static JapaneseEra[] values(){
        return Arrays.copyOf(KNOWN_ERAS,KNOWN_ERAS.length);
    }

    //-----------------------------------------------------------------------
    static JapaneseEra from(LocalDate date){
        if(date.isBefore(MEIJI_6_ISODATE)){
            throw new DateTimeException("JapaneseDate before Meiji 6 are not supported");
        }
        for(int i=KNOWN_ERAS.length-1;i>0;i--){
            JapaneseEra era=KNOWN_ERAS[i];
            if(date.compareTo(era.since)>=0){
                return era;
            }
        }
        return null;
    }

    static JapaneseEra toJapaneseEra(sun.util.calendar.Era privateEra){
        for(int i=ERA_CONFIG.length-1;i>=0;i--){
            if(ERA_CONFIG[i].equals(privateEra)){
                return KNOWN_ERAS[i];
            }
        }
        return null;
    }

    static sun.util.calendar.Era privateEraFrom(LocalDate isoDate){
        for(int i=KNOWN_ERAS.length-1;i>0;i--){
            JapaneseEra era=KNOWN_ERAS[i];
            if(isoDate.compareTo(era.since)>=0){
                return ERA_CONFIG[i];
            }
        }
        return null;
    }

    static JapaneseEra readExternal(DataInput in) throws IOException{
        byte eraValue=in.readByte();
        return JapaneseEra.of(eraValue);
    }

    //-----------------------------------------------------------------------
    public static JapaneseEra of(int japaneseEra){
        if(japaneseEra<MEIJI.eraValue||japaneseEra+ERA_OFFSET>KNOWN_ERAS.length){
            throw new DateTimeException("Invalid era: "+japaneseEra);
        }
        return KNOWN_ERAS[ordinal(japaneseEra)];
    }

    private static int ordinal(int eraValue){
        return eraValue+ERA_OFFSET-1;
    }

    //-----------------------------------------------------------------------
    sun.util.calendar.Era getPrivateEra(){
        return ERA_CONFIG[ordinal(eraValue)];
    }

    //-----------------------------------------------------------------------
    String getAbbreviation(){
        int index=ordinal(getValue());
        if(index==0){
            return "";
        }
        return ERA_CONFIG[index].getAbbreviation();
    }

    //-----------------------------------------------------------------------
    @Override
    public int getValue(){
        return eraValue;
    }

    //-----------------------------------------------------------------------
    @Override  // override as super would return range from 0 to 1
    public ValueRange range(TemporalField field){
        if(field==ERA){
            return JapaneseChronology.INSTANCE.range(ERA);
        }
        return Era.super.range(field);
    }

    @Override
    public String toString(){
        return getName();
    }

    String getName(){
        return ERA_CONFIG[ordinal(getValue())].getName();
    }

    //-----------------------------------------------------------------------
    private void readObject(ObjectInputStream s) throws InvalidObjectException{
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    //-----------------------------------------------------------------------
    private Object writeReplace(){
        return new Ser(Ser.JAPANESE_ERA_TYPE,this);
    }

    void writeExternal(DataOutput out) throws IOException{
        out.writeByte(this.getValue());
    }
}
