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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class JCheckBox extends JToggleButton implements Accessible{
    public static final String BORDER_PAINTED_FLAT_CHANGED_PROPERTY="borderPaintedFlat";
    private static final String uiClassID="CheckBoxUI";
    private boolean flat=false;

    public JCheckBox(Icon icon){
        this(null,icon,false);
    }

    public JCheckBox(String text,Icon icon,boolean selected){
        super(text,icon,selected);
        setUIProperty("borderPainted",Boolean.FALSE);
        setHorizontalAlignment(LEADING);
    }

    public JCheckBox(Icon icon,boolean selected){
        this(null,icon,selected);
    }

    public JCheckBox(String text){
        this(text,null,false);
    }

    public JCheckBox(Action a){
        this();
        setAction(a);
    }

    public JCheckBox(){
        this(null,null,false);
    }

    public JCheckBox(String text,boolean selected){
        this(text,null,selected);
    }

    public JCheckBox(String text,Icon icon){
        this(text,icon,false);
    }

    public boolean isBorderPaintedFlat(){
        return flat;
    }

    public void setBorderPaintedFlat(boolean b){
        boolean oldValue=flat;
        flat=b;
        firePropertyChange(BORDER_PAINTED_FLAT_CHANGED_PROPERTY,oldValue,flat);
        if(b!=oldValue){
            revalidate();
            repaint();
        }
    }

    void setIconFromAction(Action a){
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

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException{
        s.defaultReadObject();
        if(getUIClassID().equals(uiClassID)){
            updateUI();
        }
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
/////////////////
// Accessibility support
////////////////

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJCheckBox();
        }
        return accessibleContext;
    }

    protected class AccessibleJCheckBox extends AccessibleJToggleButton{
        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.CHECK_BOX;
        }
    } // inner class AccessibleJCheckBox
}
