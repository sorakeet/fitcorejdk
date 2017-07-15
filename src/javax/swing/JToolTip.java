/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.plaf.ToolTipUI;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Objects;

@SuppressWarnings("serial")
public class JToolTip extends JComponent implements Accessible{
    private static final String uiClassID="ToolTipUI";
    String tipText;
    JComponent component;

    public JToolTip(){
        setOpaque(true);
        updateUI();
    }

    public void updateUI(){
        setUI((ToolTipUI)UIManager.getUI(this));
    }

    public String getUIClassID(){
        return uiClassID;
    }

    // package private
    boolean alwaysOnTop(){
        return true;
    }

    protected String paramString(){
        String tipTextString=(tipText!=null?
                tipText:"");
        return super.paramString()+
                ",tipText="+tipTextString;
    }

    public ToolTipUI getUI(){
        return (ToolTipUI)ui;
    }

    public String getTipText(){
        return tipText;
    }

    public void setTipText(String tipText){
        String oldValue=this.tipText;
        this.tipText=tipText;
        firePropertyChange("tiptext",oldValue,tipText);
        if(!Objects.equals(oldValue,tipText)){
            revalidate();
            repaint();
        }
    }

    public JComponent getComponent(){
        return component;
    }

    public void setComponent(JComponent c){
        JComponent oldValue=this.component;
        component=c;
        firePropertyChange("component",oldValue,c);
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
/////////////////
// Accessibility support
////////////////

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJToolTip();
        }
        return accessibleContext;
    }

    @SuppressWarnings("serial")
    protected class AccessibleJToolTip extends AccessibleJComponent{
        public String getAccessibleDescription(){
            String description=accessibleDescription;
            // fallback to client property
            if(description==null){
                description=(String)getClientProperty(AccessibleContext.ACCESSIBLE_DESCRIPTION_PROPERTY);
            }
            if(description==null){
                description=getTipText();
            }
            return description;
        }

        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.TOOL_TIP;
        }
    }
}
