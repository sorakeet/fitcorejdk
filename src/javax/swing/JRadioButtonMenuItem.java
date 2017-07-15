/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class JRadioButtonMenuItem extends JMenuItem implements Accessible{
    private static final String uiClassID="RadioButtonMenuItemUI";

    public JRadioButtonMenuItem(Icon icon){
        this(null,icon,false);
    }

    public JRadioButtonMenuItem(String text,Icon icon,boolean selected){
        super(text,icon);
        setModel(new JToggleButton.ToggleButtonModel());
        setSelected(selected);
        setFocusable(false);
    }

    public JRadioButtonMenuItem(Action a){
        this();
        setAction(a);
    }

    public JRadioButtonMenuItem(){
        this(null,null,false);
    }

    public JRadioButtonMenuItem(String text,Icon icon){
        this(text,icon,false);
    }

    public JRadioButtonMenuItem(String text,boolean selected){
        this(text);
        setSelected(selected);
    }

    public JRadioButtonMenuItem(String text){
        this(text,null,false);
    }

    public JRadioButtonMenuItem(Icon icon,boolean selected){
        this(null,icon,selected);
    }

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

    public String getUIClassID(){
        return uiClassID;
    }

    protected String paramString(){
        return super.paramString();
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJRadioButtonMenuItem();
        }
        return accessibleContext;
    }
/////////////////
// Accessibility support
////////////////

    boolean shouldUpdateSelectedStateFromAction(){
        return true;
    }

    protected class AccessibleJRadioButtonMenuItem extends AccessibleJMenuItem{
        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.RADIO_BUTTON;
        }
    } // inner class AccessibleJRadioButtonMenuItem
}
