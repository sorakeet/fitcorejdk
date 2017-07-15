/**
 * Copyright (c) 1994, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang;

public final class Boolean implements java.io.Serializable,
        Comparable<Boolean>{
    public static final Boolean TRUE=new Boolean(true);
    public static final Boolean FALSE=new Boolean(false);
    @SuppressWarnings("unchecked")
    public static final Class<Boolean> TYPE=(Class<Boolean>)Class.getPrimitiveClass("boolean");
    private static final long serialVersionUID=-3665804199014368530L;
    private final boolean value;

    public Boolean(String s){
        this(parseBoolean(s));
    }

    public Boolean(boolean value){
        this.value=value;
    }

    public static boolean parseBoolean(String s){
        return ((s!=null)&&s.equalsIgnoreCase("true"));
    }

    public static Boolean valueOf(boolean b){
        return (b?TRUE:FALSE);
    }

    public static Boolean valueOf(String s){
        return parseBoolean(s)?TRUE:FALSE;
    }

    public static String toString(boolean b){
        return b?"true":"false";
    }

    public static boolean getBoolean(String name){
        boolean result=false;
        try{
            result=parseBoolean(System.getProperty(name));
        }catch(IllegalArgumentException|NullPointerException e){
        }
        return result;
    }

    public static boolean logicalAnd(boolean a,boolean b){
        return a&&b;
    }

    public static boolean logicalOr(boolean a,boolean b){
        return a||b;
    }

    public static boolean logicalXor(boolean a,boolean b){
        return a^b;
    }

    @Override
    public int hashCode(){
        return Boolean.hashCode(value);
    }

    public static int hashCode(boolean value){
        return value?1231:1237;
    }

    public boolean equals(Object obj){
        if(obj instanceof Boolean){
            return value==((Boolean)obj).booleanValue();
        }
        return false;
    }

    public boolean booleanValue(){
        return value;
    }

    public String toString(){
        return value?"true":"false";
    }

    public int compareTo(Boolean b){
        return compare(this.value,b.value);
    }

    public static int compare(boolean x,boolean y){
        return (x==y)?0:(x?1:-1);
    }
}
