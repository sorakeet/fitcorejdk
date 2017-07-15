/**
 * Copyright (c) 1997, 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf;

import javax.swing.*;
import java.awt.event.MouseEvent;

public abstract class PopupMenuUI extends ComponentUI{
    public boolean isPopupTrigger(MouseEvent e){
        return e.isPopupTrigger();
    }

    public Popup getPopup(JPopupMenu popup,int x,int y){
        PopupFactory popupFactory=PopupFactory.getSharedInstance();
        return popupFactory.getPopup(popup.getInvoker(),popup,x,y);
    }
}
