/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util;

import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public final class OptionalLong{
    private static final OptionalLong EMPTY=new OptionalLong();
    private final boolean isPresent;
    private final long value;

    private OptionalLong(){
        this.isPresent=false;
        this.value=0;
    }

    private OptionalLong(long value){
        this.isPresent=true;
        this.value=value;
    }

    public static OptionalLong empty(){
        return EMPTY;
    }

    public static OptionalLong of(long value){
        return new OptionalLong(value);
    }

    public long getAsLong(){
        if(!isPresent){
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public boolean isPresent(){
        return isPresent;
    }

    public void ifPresent(LongConsumer consumer){
        if(isPresent)
            consumer.accept(value);
    }

    public long orElse(long other){
        return isPresent?value:other;
    }

    public long orElseGet(LongSupplier other){
        return isPresent?value:other.getAsLong();
    }

    public <X extends Throwable> long orElseThrow(Supplier<X> exceptionSupplier) throws X{
        if(isPresent){
            return value;
        }else{
            throw exceptionSupplier.get();
        }
    }

    @Override
    public int hashCode(){
        return isPresent?Long.hashCode(value):0;
    }

    @Override
    public boolean equals(Object obj){
        if(this==obj){
            return true;
        }
        if(!(obj instanceof OptionalLong)){
            return false;
        }
        OptionalLong other=(OptionalLong)obj;
        return (isPresent&&other.isPresent)
                ?value==other.value
                :isPresent==other.isPresent;
    }

    @Override
    public String toString(){
        return isPresent
                ?String.format("OptionalLong[%s]",value)
                :"OptionalLong.empty";
    }
}
