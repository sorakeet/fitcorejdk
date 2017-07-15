/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.sampled;

public abstract class BooleanControl extends Control{
    // INSTANCE VARIABLES
    private final String trueStateLabel;
    private final String falseStateLabel;
    private boolean value;
    // CONSTRUCTORS

    protected BooleanControl(Type type,boolean initialValue){
        this(type,initialValue,"true","false");
    }

    protected BooleanControl(Type type,boolean initialValue,String trueStateLabel,String falseStateLabel){
        super(type);
        this.value=initialValue;
        this.trueStateLabel=trueStateLabel;
        this.falseStateLabel=falseStateLabel;
    }
    // METHODS

    public String toString(){
        return new String(super.toString()+" with current value: "+getStateLabel(getValue()));
    }

    public boolean getValue(){
        return value;
    }

    public void setValue(boolean value){
        this.value=value;
    }
    // ABSTRACT METHOD IMPLEMENTATIONS: CONTROL

    public String getStateLabel(boolean state){
        return ((state==true)?trueStateLabel:falseStateLabel);
    }
    // INNER CLASSES

    public static class Type extends Control.Type{
        // TYPE DEFINES
        public static final Type MUTE=new Type("Mute");
        public static final Type APPLY_REVERB=new Type("Apply Reverb");
        // CONSTRUCTOR

        protected Type(String name){
            super(name);
        }
    } // class Type
}
