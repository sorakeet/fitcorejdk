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
 * Copyright (c) 2009-2012, Stephen Colebourne & Michael Nascimento Santos
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
 * Copyright (c) 2009-2012, Stephen Colebourne & Michael Nascimento Santos
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
package java.time.zone;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ZoneOffsetTransition
        implements Comparable<ZoneOffsetTransition>, Serializable{
    private static final long serialVersionUID=-6946044323557704546L;
    private final LocalDateTime transition;
    private final ZoneOffset offsetBefore;
    private final ZoneOffset offsetAfter;

    ZoneOffsetTransition(LocalDateTime transition,ZoneOffset offsetBefore,ZoneOffset offsetAfter){
        this.transition=transition;
        this.offsetBefore=offsetBefore;
        this.offsetAfter=offsetAfter;
    }

    ZoneOffsetTransition(long epochSecond,ZoneOffset offsetBefore,ZoneOffset offsetAfter){
        this.transition=LocalDateTime.ofEpochSecond(epochSecond,0,offsetBefore);
        this.offsetBefore=offsetBefore;
        this.offsetAfter=offsetAfter;
    }

    //-----------------------------------------------------------------------
    public static ZoneOffsetTransition of(LocalDateTime transition,ZoneOffset offsetBefore,ZoneOffset offsetAfter){
        Objects.requireNonNull(transition,"transition");
        Objects.requireNonNull(offsetBefore,"offsetBefore");
        Objects.requireNonNull(offsetAfter,"offsetAfter");
        if(offsetBefore.equals(offsetAfter)){
            throw new IllegalArgumentException("Offsets must not be equal");
        }
        if(transition.getNano()!=0){
            throw new IllegalArgumentException("Nano-of-second must be zero");
        }
        return new ZoneOffsetTransition(transition,offsetBefore,offsetAfter);
    }

    static ZoneOffsetTransition readExternal(DataInput in) throws IOException{
        long epochSecond=Ser.readEpochSec(in);
        ZoneOffset before=Ser.readOffset(in);
        ZoneOffset after=Ser.readOffset(in);
        if(before.equals(after)){
            throw new IllegalArgumentException("Offsets must not be equal");
        }
        return new ZoneOffsetTransition(epochSecond,before,after);
    }

    //-----------------------------------------------------------------------
    private void readObject(ObjectInputStream s) throws InvalidObjectException{
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    private Object writeReplace(){
        return new Ser(Ser.ZOT,this);
    }

    void writeExternal(DataOutput out) throws IOException{
        Ser.writeEpochSec(toEpochSecond(),out);
        Ser.writeOffset(offsetBefore,out);
        Ser.writeOffset(offsetAfter,out);
    }

    public long toEpochSecond(){
        return transition.toEpochSecond(offsetBefore);
    }

    //-------------------------------------------------------------------------
    public LocalDateTime getDateTimeBefore(){
        return transition;
    }

    public LocalDateTime getDateTimeAfter(){
        return transition.plusSeconds(getDurationSeconds());
    }

    private int getDurationSeconds(){
        return getOffsetAfter().getTotalSeconds()-getOffsetBefore().getTotalSeconds();
    }

    public ZoneOffset getOffsetBefore(){
        return offsetBefore;
    }

    public ZoneOffset getOffsetAfter(){
        return offsetAfter;
    }

    public Duration getDuration(){
        return Duration.ofSeconds(getDurationSeconds());
    }

    public boolean isOverlap(){
        return getOffsetAfter().getTotalSeconds()<getOffsetBefore().getTotalSeconds();
    }

    public boolean isValidOffset(ZoneOffset offset){
        return isGap()?false:(getOffsetBefore().equals(offset)||getOffsetAfter().equals(offset));
    }

    public boolean isGap(){
        return getOffsetAfter().getTotalSeconds()>getOffsetBefore().getTotalSeconds();
    }

    List<ZoneOffset> getValidOffsets(){
        if(isGap()){
            return Collections.emptyList();
        }
        return Arrays.asList(getOffsetBefore(),getOffsetAfter());
    }

    //-----------------------------------------------------------------------
    @Override
    public int compareTo(ZoneOffsetTransition transition){
        return this.getInstant().compareTo(transition.getInstant());
    }

    //-----------------------------------------------------------------------
    public Instant getInstant(){
        return transition.toInstant(offsetBefore);
    }

    @Override
    public int hashCode(){
        return transition.hashCode()^offsetBefore.hashCode()^Integer.rotateLeft(offsetAfter.hashCode(),16);
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object other){
        if(other==this){
            return true;
        }
        if(other instanceof ZoneOffsetTransition){
            ZoneOffsetTransition d=(ZoneOffsetTransition)other;
            return transition.equals(d.transition)&&
                    offsetBefore.equals(d.offsetBefore)&&offsetAfter.equals(d.offsetAfter);
        }
        return false;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString(){
        StringBuilder buf=new StringBuilder();
        buf.append("Transition[")
                .append(isGap()?"Gap":"Overlap")
                .append(" at ")
                .append(transition)
                .append(offsetBefore)
                .append(" to ")
                .append(offsetAfter)
                .append(']');
        return buf.toString();
    }
}
