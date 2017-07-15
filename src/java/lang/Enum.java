/**
 * Copyright (c) 2003, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang;

import java.io.*;

public abstract class Enum<E extends Enum<E>>
        implements Comparable<E>, Serializable{
    private final String name;
    private final int ordinal;

    protected Enum(String name,int ordinal){
        this.name=name;
        this.ordinal=ordinal;
    }

    public static <T extends Enum<T>> T valueOf(Class<T> enumType,
                                                String name){
        T result=enumType.enumConstantDirectory().get(name);
        if(result!=null)
            return result;
        if(name==null)
            throw new NullPointerException("Name is null");
        throw new IllegalArgumentException(
                "No enum constant "+enumType.getCanonicalName()+"."+name);
    }

    public final String name(){
        return name;
    }

    public final int ordinal(){
        return ordinal;
    }

    public final int hashCode(){
        return super.hashCode();
    }

    public final boolean equals(Object other){
        return this==other;
    }

    protected final Object clone() throws CloneNotSupportedException{
        throw new CloneNotSupportedException();
    }

    public String toString(){
        return name;
    }

    protected final void finalize(){
    }

    public final int compareTo(E o){
        Enum<?> other=(Enum<?>)o;
        Enum<E> self=this;
        if(self.getClass()!=other.getClass()&& // optimization
                self.getDeclaringClass()!=other.getDeclaringClass())
            throw new ClassCastException();
        return self.ordinal-other.ordinal;
    }

    @SuppressWarnings("unchecked")
    public final Class<E> getDeclaringClass(){
        Class<?> clazz=getClass();
        Class<?> zuper=clazz.getSuperclass();
        return (zuper==Enum.class)?(Class<E>)clazz:(Class<E>)zuper;
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException{
        throw new InvalidObjectException("can't deserialize enum");
    }

    private void readObjectNoData() throws ObjectStreamException{
        throw new InvalidObjectException("can't deserialize enum");
    }
}
