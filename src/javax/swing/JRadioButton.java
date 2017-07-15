/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.plaf.ButtonUI;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class JRadioButton extends JToggleButton implements Accessible{
    private static final String uiClassID="RadioButtonUI";

    public JRadioButton(Icon icon){
        this(null,icon,false);
    }

    public JRadioButton(String text,Icon icon,boolean selected){
        super(text,icon,selected);
        setBorderPainted(false);
        setHorizontalAlignment(LEADING);
    }

    public JRadioButton(Action a){
        this();
        setAction(a);
    }

    public JRadioButton(){
        this(null,null,false);
    }

    public JRadioButton(Icon icon,boolean selected){
        this(null,icon,selected);
    }

    public JRadioButton(String text){
        this(text,null,false);
    }

    public JRadioButton(String text,boolean selected){
        this(text,null,selected);
    }

    public JRadioButton(String text,Icon icon){
        this(text,icon,false);
    }

    public void updateUI(){
        setUI((ButtonUI)UIManager.getUI(this));
    }

    public String getUIClassID(){
        return uiClassID;
    }

    protected String paramString(){
        return super.paramString();
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJRadioButton();
        }
        return accessibleContext;
    }

    void setIconFromAction(Action a){
    }
/////////////////
// Accessibility support
////////////////

    private void writeObject(ObjectOutputStream s) throws IOException{
        s.defaultWriteObject();
        if(getUIClassID().equals(uiClassID)){
            byte count=JComponent.getWriteObjCounter(this);
            JComponent.setWriteObjCounter(this,--count);
            if(count==0&&ui!=null){
                ui.installUI(this);
            }
        }
    }

    protected class AccessibleJRadioButton extends AccessibleJToggleButton{
        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.RADIO_BUTTON;
        }
    } // inner class AccessibleJRadioButton
}
