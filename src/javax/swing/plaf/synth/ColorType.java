/**
 * Copyright (c) 2002, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.synth;

public class ColorType{
    public static final ColorType FOREGROUND=new ColorType("Foreground");
    public static final ColorType BACKGROUND=new ColorType("Background");
    public static final ColorType TEXT_FOREGROUND=new ColorType(
            "TextForeground");
    public static final ColorType TEXT_BACKGROUND=new ColorType(
            "TextBackground");
    public static final ColorType FOCUS=new ColorType("Focus");
    public static final int MAX_COUNT;
    private static int nextID;

    static{
        MAX_COUNT=Math.max(FOREGROUND.getID(),Math.max(
                BACKGROUND.getID(),FOCUS.getID()))+1;
    }

    private String description;
    private int index;

    protected ColorType(String description){
        if(description==null){
            throw new NullPointerException(
                    "ColorType must have a valid description");
        }
        this.description=description;
        synchronized(ColorType.class){
            this.index=nextID++;
        }
    }

    public final int getID(){
        return index;
    }

    public String toString(){
        return description;
    }
}
