/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.sampled;

public abstract class EnumControl extends Control{
    // TYPE DEFINES
    // INSTANCE VARIABLES
    private Object[] values;
    private Object value;
    // CONSTRUCTORS

    protected EnumControl(Type type,Object[] values,Object value){
        super(type);
        this.values=values;
        this.value=value;
    }
    // METHODS

    public Object[] getValues(){
        Object[] localArray=new Object[values.length];
        for(int i=0;i<values.length;i++){
            localArray[i]=values[i];
        }
        return localArray;
    }

    public String toString(){
        return new String(getType()+" with current value: "+getValue());
    }

    public Object getValue(){
        return value;
    }

    public void setValue(Object value){
        if(!isValueSupported(value)){
            throw new IllegalArgumentException("Requested value "+value+" is not supported.");
        }
        this.value=value;
    }
    // ABSTRACT METHOD IMPLEMENTATIONS: CONTROL

    private boolean isValueSupported(Object value){
        for(int i=0;i<values.length;i++){
            //$$fb 2001-07-20: Fix for bug 4400392: setValue() in ReverbControl always throws Exception
            //if (values.equals(values[i])) {
            if(value.equals(values[i])){
                return true;
            }
        }
        return false;
    }
    // INNER CLASSES

    public static class Type extends Control.Type{
        // TYPE DEFINES
        public static final Type REVERB=new Type("Reverb");
        // CONSTRUCTOR

        protected Type(String name){
            super(name);
        }
    } // class Type
} // class EnumControl
