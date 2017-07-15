/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import java.awt.*;

public interface StyledDocument extends Document{
    public Style addStyle(String nm,Style parent);

    public void removeStyle(String nm);

    public Style getStyle(String nm);

    public void setCharacterAttributes(int offset,int length,AttributeSet s,boolean replace);

    public void setParagraphAttributes(int offset,int length,AttributeSet s,boolean replace);

    public void setLogicalStyle(int pos,Style s);

    public Style getLogicalStyle(int p);

    public Element getParagraphElement(int pos);

    public Element getCharacterElement(int pos);

    public Color getForeground(AttributeSet attr);

    public Color getBackground(AttributeSet attr);

    public Font getFont(AttributeSet attr);
}
