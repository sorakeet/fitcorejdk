/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.sampled;

public abstract class FloatControl extends Control{
    private final String units;
    private final String minLabel;
    private final String maxLabel;
    private final String midLabel;
    // INSTANCE VARIABLES
    // FINAL VARIABLES
    private float minimum;
    private float maximum;
    private float precision;
    private int updatePeriod;
    // STATE VARIABLES
    private float value;
    // CONSTRUCTORS

    protected FloatControl(Type type,float minimum,float maximum,
                           float precision,int updatePeriod,float initialValue,String units){
        this(type,minimum,maximum,precision,updatePeriod,
                initialValue,units,"","","");
    }

    protected FloatControl(Type type,float minimum,float maximum,
                           float precision,int updatePeriod,float initialValue,
                           String units,String minLabel,String midLabel,String maxLabel){
        super(type);
        if(minimum>maximum){
            throw new IllegalArgumentException("Minimum value "+minimum
                    +" exceeds maximum value "+maximum+".");
        }
        if(initialValue<minimum){
            throw new IllegalArgumentException("Initial value "+initialValue
                    +" smaller than allowable minimum value "+minimum+".");
        }
        if(initialValue>maximum){
            throw new IllegalArgumentException("Initial value "+initialValue
                    +" exceeds allowable maximum value "+maximum+".");
        }
        this.minimum=minimum;
        this.maximum=maximum;
        this.precision=precision;
        this.updatePeriod=updatePeriod;
        this.value=initialValue;
        this.units=units;
        this.minLabel=((minLabel==null)?"":minLabel);
        this.midLabel=((midLabel==null)?"":midLabel);
        this.maxLabel=((maxLabel==null)?"":maxLabel);
    }
    // METHODS

    public float getMaximum(){
        return maximum;
    }

    public float getMinimum(){
        return minimum;
    }

    public String getUnits(){
        return units;
    }

    public String getMinLabel(){
        return minLabel;
    }

    public String getMidLabel(){
        return midLabel;
    }

    public String getMaxLabel(){
        return maxLabel;
    }

    public float getPrecision(){
        return precision;
    }

    public int getUpdatePeriod(){
        return updatePeriod;
    }

    public void shift(float from,float to,int microseconds){
        // test "from" value, "to" value will be tested by setValue()
        if(from<minimum){
            throw new IllegalArgumentException("Requested value "+from
                    +" smaller than allowable minimum value "+minimum+".");
        }
        if(from>maximum){
            throw new IllegalArgumentException("Requested value "+from
                    +" exceeds allowable maximum value "+maximum+".");
        }
        setValue(to);
    }

    public String toString(){
        return new String(getType()+" with current value: "+getValue()+" "+units+
                " (range: "+minimum+" - "+maximum+")");
    }

    public float getValue(){
        return value;
    }
    // ABSTRACT METHOD IMPLEMENTATIONS: CONTROL

    public void setValue(float newValue){
        if(newValue>maximum){
            throw new IllegalArgumentException("Requested value "+newValue+" exceeds allowable maximum value "+maximum+".");
        }
        if(newValue<minimum){
            throw new IllegalArgumentException("Requested value "+newValue+" smaller than allowable minimum value "+minimum+".");
        }
        value=newValue;
    }
    // INNER CLASSES

    public static class Type extends Control.Type{
        // TYPE DEFINES
        // GAIN TYPES
        public static final Type MASTER_GAIN=new Type("Master Gain");
        public static final Type AUX_SEND=new Type("AUX Send");
        public static final Type AUX_RETURN=new Type("AUX Return");
        public static final Type REVERB_SEND=new Type("Reverb Send");
        public static final Type REVERB_RETURN=new Type("Reverb Return");
        // VOLUME
        public static final Type VOLUME=new Type("Volume");
        // PAN
        public static final Type PAN=new Type("Pan");
        // BALANCE
        public static final Type BALANCE=new Type("Balance");
        // SAMPLE RATE
        public static final Type SAMPLE_RATE=new Type("Sample Rate");
        // CONSTRUCTOR

        protected Type(String name){
            super(name);
        }
    } // class Type
} // class FloatControl
