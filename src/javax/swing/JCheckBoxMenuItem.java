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

public class JCheckBoxMenuItem extends JMenuItem implements SwingConstants,
        Accessible{
    private static final String uiClassID="CheckBoxMenuItemUI";

    public JCheckBoxMenuItem(Icon icon){
        this(null,icon,false);
    }

    public JCheckBoxMenuItem(String text,Icon icon,boolean b){
        super(text,icon);
        setModel(new JToggleButton.ToggleButtonModel());
        setSelected(b);
        setFocusable(false);
    }

    public JCheckBoxMenuItem(String text){
        this(text,null,false);
    }

    public JCheckBoxMenuItem(Action a){
        this();
        setAction(a);
    }

    public JCheckBoxMenuItem(){
        this(null,null,false);
    }

    public JCheckBoxMenuItem(String text,Icon icon){
        this(text,icon,false);
    }

    public JCheckBoxMenuItem(String text,boolean b){
        this(text,null,b);
    }

    public boolean getState(){
        return isSelected();
    }

    public synchronized void setState(boolean b){
        setSelected(b);
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
            accessibleContext=new AccessibleJCheckBoxMenuItem();
        }
        return accessibleContext;
    }

    boolean shouldUpdateSelectedStateFromAction(){
        return true;
    }
/////////////////
// Accessibility support
////////////////

    public Object[] getSelectedObjects(){
        if(isSelected()==false)
            return null;
        Object[] selectedObjects=new Object[1];
        selectedObjects[0]=getText();
        return selectedObjects;
    }

    protected class AccessibleJCheckBoxMenuItem extends AccessibleJMenuItem{
        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.CHECK_BOX;
        }
    } // inner class AccessibleJCheckBoxMenuItem
}
