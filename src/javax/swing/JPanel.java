/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.plaf.PanelUI;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class JPanel extends JComponent implements Accessible{
    private static final String uiClassID="PanelUI";

    public JPanel(LayoutManager layout){
        this(layout,true);
    }

    public JPanel(LayoutManager layout,boolean isDoubleBuffered){
        setLayout(layout);
        setDoubleBuffered(isDoubleBuffered);
        setUIProperty("opaque",Boolean.TRUE);
        updateUI();
    }

    public void updateUI(){
        setUI((PanelUI)UIManager.getUI(this));
    }

    public String getUIClassID(){
        return uiClassID;
    }

    protected String paramString(){
        return super.paramString();
    }

    public JPanel(){
        this(true);
    }

    public JPanel(boolean isDoubleBuffered){
        this(new FlowLayout(),isDoubleBuffered);
    }

    public PanelUI getUI(){
        return (PanelUI)ui;
    }

    public void setUI(PanelUI ui){
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
/////////////////
// Accessibility support
////////////////

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJPanel();
        }
        return accessibleContext;
    }

    protected class AccessibleJPanel extends AccessibleJComponent{
        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.PANEL;
        }
    }
}
