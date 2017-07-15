/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.basic;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.beans.PropertyChangeEvent;

public class BasicTextPaneUI extends BasicEditorPaneUI{
    public BasicTextPaneUI(){
        super();
    }

    public static ComponentUI createUI(JComponent c){
        return new BasicTextPaneUI();
    }

    protected String getPropertyPrefix(){
        return "TextPane";
    }

    public void installUI(JComponent c){
        super.installUI(c);
    }

    protected void propertyChange(PropertyChangeEvent evt){
        super.propertyChange(evt);
    }
}
