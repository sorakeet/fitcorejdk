/**
 * Copyright (c) 1996, 2009, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import java.awt.event.KeyEvent;

public class MenuShortcut implements java.io.Serializable{
    private static final long serialVersionUID=143448358473180225L;
    int key;
    boolean usesShift;

    public MenuShortcut(int key){
        this(key,false);
    }

    public MenuShortcut(int key,boolean useShiftModifier){
        this.key=key;
        this.usesShift=useShiftModifier;
    }

    public int getKey(){
        return key;
    }

    public int hashCode(){
        return (usesShift)?(~key):key;
    }

    public boolean equals(Object obj){
        if(obj instanceof MenuShortcut){
            return equals((MenuShortcut)obj);
        }
        return false;
    }

    public boolean equals(MenuShortcut s){
        return (s!=null&&(s.getKey()==key)&&
                (s.usesShiftModifier()==usesShift));
    }

    public String toString(){
        int modifiers=0;
        if(!GraphicsEnvironment.isHeadless()){
            modifiers=Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        }
        if(usesShiftModifier()){
            modifiers|=Event.SHIFT_MASK;
        }
        return KeyEvent.getKeyModifiersText(modifiers)+"+"+
                KeyEvent.getKeyText(key);
    }

    public boolean usesShiftModifier(){
        return usesShift;
    }

    protected String paramString(){
        String str="key="+key;
        if(usesShiftModifier()){
            str+=",usesShiftModifier";
        }
        return str;
    }
}
