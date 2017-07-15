/**
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.basic;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Element;
import javax.swing.text.PasswordView;
import javax.swing.text.View;

public class BasicPasswordFieldUI extends BasicTextFieldUI{
    public static ComponentUI createUI(JComponent c){
        return new BasicPasswordFieldUI();
    }

    protected void installDefaults(){
        super.installDefaults();
        String prefix=getPropertyPrefix();
        Character echoChar=(Character)UIManager.getDefaults().get(prefix+".echoChar");
        if(echoChar!=null){
            LookAndFeel.installProperty(getComponent(),"echoChar",echoChar);
        }
    }

    protected String getPropertyPrefix(){
        return "PasswordField";
    }

    public View create(Element elem){
        return new PasswordView(elem);
    }

    ActionMap createActionMap(){
        ActionMap map=super.createActionMap();
        if(map.get(DefaultEditorKit.selectWordAction)!=null){
            Action a=map.get(DefaultEditorKit.selectLineAction);
            if(a!=null){
                map.remove(DefaultEditorKit.selectWordAction);
                map.put(DefaultEditorKit.selectWordAction,a);
            }
        }
        return map;
    }
}
