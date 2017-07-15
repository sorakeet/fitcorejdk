/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.sampled;

public abstract class CompoundControl extends Control{
    // TYPE DEFINES
    // INSTANCE VARIABLES
    private Control[] controls;
    // CONSTRUCTORS

    protected CompoundControl(Type type,Control[] memberControls){
        super(type);
        this.controls=memberControls;
    }
    // METHODS

    public Control[] getMemberControls(){
        Control[] localArray=new Control[controls.length];
        for(int i=0;i<controls.length;i++){
            localArray[i]=controls[i];
        }
        return localArray;
    }
    // ABSTRACT METHOD IMPLEMENTATIONS: CONTROL

    public String toString(){
        StringBuffer buf=new StringBuffer();
        for(int i=0;i<controls.length;i++){
            if(i!=0){
                buf.append(", ");
                if((i+1)==controls.length){
                    buf.append("and ");
                }
            }
            buf.append(controls[i].getType());
        }
        return new String(getType()+" Control containing "+buf+" Controls.");
    }
    // INNER CLASSES

    public static class Type extends Control.Type{
        // TYPE DEFINES
        // CONSTRUCTOR

        protected Type(String name){
            super(name);
        }
    } // class Type
} // class CompoundControl
