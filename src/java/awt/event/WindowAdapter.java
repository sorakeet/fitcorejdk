/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.event;

public abstract class WindowAdapter
        implements WindowListener, WindowStateListener, WindowFocusListener{
    public void windowOpened(WindowEvent e){
    }

    public void windowClosing(WindowEvent e){
    }

    public void windowClosed(WindowEvent e){
    }

    public void windowIconified(WindowEvent e){
    }

    public void windowDeiconified(WindowEvent e){
    }

    public void windowActivated(WindowEvent e){
    }

    public void windowDeactivated(WindowEvent e){
    }

    public void windowStateChanged(WindowEvent e){
    }

    public void windowGainedFocus(WindowEvent e){
    }

    public void windowLostFocus(WindowEvent e){
    }
}
