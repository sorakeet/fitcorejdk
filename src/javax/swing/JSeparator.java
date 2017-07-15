/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.plaf.SeparatorUI;
import java.io.IOException;
import java.io.ObjectOutputStream;

@SuppressWarnings("serial")
public class JSeparator extends JComponent implements SwingConstants, Accessible{
    private static final String uiClassID="SeparatorUI";
    private int orientation=HORIZONTAL;

    public JSeparator(){
        this(HORIZONTAL);
    }

    public JSeparator(int orientation){
        checkOrientation(orientation);
        this.orientation=orientation;
        setFocusable(false);
        updateUI();
    }

    public void updateUI(){
        setUI((SeparatorUI)UIManager.getUI(this));
    }

    public String getUIClassID(){
        return uiClassID;
    }

    protected String paramString(){
        String orientationString=(orientation==HORIZONTAL?
                "HORIZONTAL":"VERTICAL");
        return super.paramString()+
                ",orientation="+orientationString;
    }

    private void checkOrientation(int orientation){
        switch(orientation){
            case VERTICAL:
            case HORIZONTAL:
                break;
            default:
                throw new IllegalArgumentException("orientation must be one of: VERTICAL, HORIZONTAL");
        }
    }

    public SeparatorUI getUI(){
        return (SeparatorUI)ui;
    }

    public void setUI(SeparatorUI ui){
        super.setUI(ui);
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

    public int getOrientation(){
        return this.orientation;
    }

    public void setOrientation(int orientation){
        if(this.orientation==orientation){
            return;
        }
        int oldValue=this.orientation;
        checkOrientation(orientation);
        this.orientation=orientation;
        firePropertyChange("orientation",oldValue,orientation);
        revalidate();
        repaint();
    }
/////////////////
// Accessibility support
////////////////

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJSeparator();
        }
        return accessibleContext;
    }

    @SuppressWarnings("serial")
    protected class AccessibleJSeparator extends AccessibleJComponent{
        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.SEPARATOR;
        }
    }
}
