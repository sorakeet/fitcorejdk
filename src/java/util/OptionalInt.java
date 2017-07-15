/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class OptionalInt{
    private static final OptionalInt EMPTY=new OptionalInt();
    private final boolean isPresent;
    private final int value;

    private OptionalInt(){
        this.isPresent=false;
        this.value=0;
    }

    private OptionalInt(int value){
        this.isPresent=true;
        this.value=value;
    }

    public static OptionalInt empty(){
        return EMPTY;
    }

    public static OptionalInt of(int value){
        return new OptionalInt(value);
    }

    public int getAsInt(){
        if(!isPresent){
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public boolean isPresent(){
        return isPresent;
    }

    public void ifPresent(IntConsumer consumer){
        if(isPresent)
            consumer.accept(value);
    }

    public int orElse(int other){
        return isPresent?value:other;
    }

    public int orElseGet(IntSupplier other){
        return isPresent?value:other.getAsInt();
    }

    public <X extends Throwable> int orElseThrow(Supplier<X> exceptionSupplier) throws X{
        if(isPresent){
            return value;
        }else{
            throw exceptionSupplier.get();
        }
    }

    @Override
    public int hashCode(){
        return isPresent?Integer.hashCode(value):0;
    }

    @Override
    public boolean equals(Object obj){
        if(this==obj){
            return true;
        }
        if(!(obj instanceof OptionalInt)){
            return false;
        }
        OptionalInt other=(OptionalInt)obj;
        return (isPresent&&other.isPresent)
                ?value==other.value
                :isPresent==other.isPresent;
    }

    @Override
    public String toString(){
        return isPresent
                ?String.format("OptionalInt[%s]",value)
                :"OptionalInt.empty";
    }
}
