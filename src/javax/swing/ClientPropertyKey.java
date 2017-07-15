/**
 * Copyright (c) 2006, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import sun.awt.AWTAccessor;

enum ClientPropertyKey{
    JComponent_INPUT_VERIFIER(true),
    JComponent_TRANSFER_HANDLER(true),
    JComponent_ANCESTOR_NOTIFIER(true),
    PopupFactory_FORCE_HEAVYWEIGHT_POPUP(true);
    static{
        AWTAccessor.setClientPropertyKeyAccessor(
                new AWTAccessor.ClientPropertyKeyAccessor(){
                    public Object getJComponent_TRANSFER_HANDLER(){
                        return JComponent_TRANSFER_HANDLER;
                    }
                });
    }

    private final boolean reportValueNotSerializable;

    private ClientPropertyKey(){
        this(false);
    }

    private ClientPropertyKey(boolean reportValueNotSerializable){
        this.reportValueNotSerializable=reportValueNotSerializable;
    }

    public boolean getReportValueNotSerializable(){
        return reportValueNotSerializable;
    }
}
