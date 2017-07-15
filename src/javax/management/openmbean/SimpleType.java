/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.openmbean;
// java import
//

import javax.management.ObjectName;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
// jmx import
//

public final class SimpleType<T> extends OpenType<T>{
    // SimpleType instances.
    // IF YOU ADD A SimpleType, YOU MUST UPDATE OpenType and typeArray
    public static final SimpleType<Void> VOID=
            new SimpleType<Void>(Void.class);
    public static final SimpleType<Boolean> BOOLEAN=
            new SimpleType<Boolean>(Boolean.class);
    public static final SimpleType<Character> CHARACTER=
            new SimpleType<Character>(Character.class);
    public static final SimpleType<Byte> BYTE=
            new SimpleType<Byte>(Byte.class);
    public static final SimpleType<Short> SHORT=
            new SimpleType<Short>(Short.class);
    public static final SimpleType<Integer> INTEGER=
            new SimpleType<Integer>(Integer.class);
    public static final SimpleType<Long> LONG=
            new SimpleType<Long>(Long.class);
    public static final SimpleType<Float> FLOAT=
            new SimpleType<Float>(Float.class);
    public static final SimpleType<Double> DOUBLE=
            new SimpleType<Double>(Double.class);
    public static final SimpleType<String> STRING=
            new SimpleType<String>(String.class);
    public static final SimpleType<BigDecimal> BIGDECIMAL=
            new SimpleType<BigDecimal>(BigDecimal.class);
    public static final SimpleType<BigInteger> BIGINTEGER=
            new SimpleType<BigInteger>(BigInteger.class);
    public static final SimpleType<Date> DATE=
            new SimpleType<Date>(Date.class);
    public static final SimpleType<ObjectName> OBJECTNAME=
            new SimpleType<ObjectName>(ObjectName.class);
    static final long serialVersionUID=2215577471957694503L;
    private static final SimpleType<?>[] typeArray={
            VOID,BOOLEAN,CHARACTER,BYTE,SHORT,INTEGER,LONG,FLOAT,
            DOUBLE,STRING,BIGDECIMAL,BIGINTEGER,DATE,OBJECTNAME,
    };
    private static final Map<SimpleType<?>,SimpleType<?>> canonicalTypes=
            new HashMap<SimpleType<?>,SimpleType<?>>();

    static{
        for(int i=0;i<typeArray.length;i++){
            final SimpleType<?> type=typeArray[i];
            canonicalTypes.put(type,type);
        }
    }

    private transient Integer myHashCode=null;        // As this instance is immutable, these two values
    private transient String myToString=null;        // need only be calculated once.

    private SimpleType(Class<T> valueClass){
        super(valueClass.getName(),valueClass.getName(),valueClass.getName(),
                false);
    }

    public boolean isValue(Object obj){
        // if obj is null, return false
        //
        if(obj==null){
            return false;
        }
        // Test if obj's class name is the same as for this instance
        //
        return this.getClassName().equals(obj.getClass().getName());
    }

    public boolean equals(Object obj){
        /** If it weren't for readReplace(), we could replace this method
         with just:
         return (this == obj);
         */
        if(!(obj instanceof SimpleType<?>))
            return false;
        SimpleType<?> other=(SimpleType<?>)obj;
        // Test if other's className field is the same as for this instance
        //
        return this.getClassName().equals(other.getClassName());
    }

    public int hashCode(){
        // Calculate the hash code value if it has not yet been done (ie 1st call to hashCode())
        //
        if(myHashCode==null){
            myHashCode=Integer.valueOf(this.getClassName().hashCode());
        }
        // return always the same hash code for this instance (immutable)
        //
        return myHashCode.intValue();
    }

    public String toString(){
        // Calculate the string representation if it has not yet been done (ie 1st call to toString())
        //
        if(myToString==null){
            myToString=this.getClass().getName()+"(name="+getTypeName()+")";
        }
        // return always the same string representation for this instance (immutable)
        //
        return myToString;
    }

    public Object readResolve() throws ObjectStreamException{
        final SimpleType<?> canonical=canonicalTypes.get(this);
        if(canonical==null){
            // Should not happen
            throw new InvalidObjectException("Invalid SimpleType: "+this);
        }
        return canonical;
    }
}
