/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.plaf.ButtonUI;
import java.beans.ConstructorProperties;
import java.io.IOException;
import java.io.ObjectOutputStream;

@SuppressWarnings("serial")
public class JButton extends AbstractButton implements Accessible{
    private static final String uiClassID="ButtonUI";

    public JButton(Icon icon){
        this(null,icon);
    }

    public JButton(String text,Icon icon){
        // Create the model
        setModel(new DefaultButtonModel());
        // initialize
        init(text,icon);
    }

    @ConstructorProperties({"text"})
    public JButton(String text){
        this(text,null);
    }

    public JButton(Action a){
        this();
        setAction(a);
    }

    public JButton(){
        this(null,null);
    }

    public boolean isDefaultButton(){
        JRootPane root=SwingUtilities.getRootPane(this);
        if(root!=null){
            return root.getDefaultButton()==this;
        }
        return false;
    }

    public boolean isDefaultCapable(){
        return defaultCapable;
    }

    public void setDefaultCapable(boolean defaultCapable){
        boolean oldDefaultCapable=this.defaultCapable;
        this.defaultCapable=defaultCapable;
        firePropertyChange("defaultCapable",oldDefaultCapable,defaultCapable);
    }

    public void removeNotify(){
        JRootPane root=SwingUtilities.getRootPane(this);
        if(root!=null&&root.getDefaultButton()==this){
            root.setDefaultButton(null);
        }
        super.removeNotify();
    }

    public void updateUI(){
        setUI((ButtonUI)UIManager.getUI(this));
    }

    protected String paramString(){
        String defaultCapableString=(defaultCapable?"true":"false");
        return super.paramString()+
                ",defaultCapable="+defaultCapableString;
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
/////////////////
// Accessibility support
////////////////

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJButton();
        }
        return accessibleContext;
    }

    @SuppressWarnings("serial")
    protected class AccessibleJButton extends AccessibleAbstractButton{
        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.PUSH_BUTTON;
        }
    } // inner class AccessibleJButton
}
