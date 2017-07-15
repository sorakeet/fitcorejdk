/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

public class IconUIResource implements Icon, UIResource, Serializable{
    private Icon delegate;

    public IconUIResource(Icon delegate){
        if(delegate==null){
            throw new IllegalArgumentException("null delegate icon argument");
        }
        this.delegate=delegate;
    }

    public void paintIcon(Component c,Graphics g,int x,int y){
        delegate.paintIcon(c,g,x,y);
    }

    public int getIconWidth(){
        return delegate.getIconWidth();
    }

    public int getIconHeight(){
        return delegate.getIconHeight();
    }
}
