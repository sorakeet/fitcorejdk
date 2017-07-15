/**
 * Copyright (c) 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.basic;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;

public class BasicFormattedTextFieldUI extends BasicTextFieldUI{
    public static ComponentUI createUI(JComponent c){
        return new BasicFormattedTextFieldUI();
    }

    protected String getPropertyPrefix(){
        return "FormattedTextField";
    }
}
