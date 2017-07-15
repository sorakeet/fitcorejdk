/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.awt.*;
import java.awt.event.KeyEvent;

public class KeyStroke extends AWTKeyStroke{
    private static final long serialVersionUID=-9060180771037902530L;

    private KeyStroke(){
    }

    private KeyStroke(char keyChar,int keyCode,int modifiers,
                      boolean onKeyRelease){
        super(keyChar,keyCode,modifiers,onKeyRelease);
    }

    public static KeyStroke getKeyStroke(char keyChar){
        synchronized(AWTKeyStroke.class){
            registerSubclass(KeyStroke.class);
            return (KeyStroke)getAWTKeyStroke(keyChar);
        }
    }

    @Deprecated
    public static KeyStroke getKeyStroke(char keyChar,boolean onKeyRelease){
        return new KeyStroke(keyChar,KeyEvent.VK_UNDEFINED,0,onKeyRelease);
    }

    public static KeyStroke getKeyStroke(Character keyChar,int modifiers){
        synchronized(AWTKeyStroke.class){
            registerSubclass(KeyStroke.class);
            return (KeyStroke)getAWTKeyStroke(keyChar,modifiers);
        }
    }

    public static KeyStroke getKeyStroke(int keyCode,int modifiers,
                                         boolean onKeyRelease){
        synchronized(AWTKeyStroke.class){
            registerSubclass(KeyStroke.class);
            return (KeyStroke)getAWTKeyStroke(keyCode,modifiers,
                    onKeyRelease);
        }
    }

    public static KeyStroke getKeyStroke(int keyCode,int modifiers){
        synchronized(AWTKeyStroke.class){
            registerSubclass(KeyStroke.class);
            return (KeyStroke)getAWTKeyStroke(keyCode,modifiers);
        }
    }

    public static KeyStroke getKeyStrokeForEvent(KeyEvent anEvent){
        synchronized(AWTKeyStroke.class){
            registerSubclass(KeyStroke.class);
            return (KeyStroke)getAWTKeyStrokeForEvent(anEvent);
        }
    }

    public static KeyStroke getKeyStroke(String s){
        if(s==null||s.length()==0){
            return null;
        }
        synchronized(AWTKeyStroke.class){
            registerSubclass(KeyStroke.class);
            try{
                return (KeyStroke)getAWTKeyStroke(s);
            }catch(IllegalArgumentException e){
                return null;
            }
        }
    }
}
