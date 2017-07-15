/**
 * Copyright (c) 2005, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.peer;

public interface TrayIconPeer{
    void dispose();

    void setToolTip(String tooltip);

    void updateImage();

    void displayMessage(String caption,String text,String messageType);

    void showPopupMenu(int x,int y);
}
