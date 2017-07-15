/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import javax.swing.event.ChangeListener;
import java.awt.*;

public interface Caret{
    public void install(JTextComponent c);

    public void deinstall(JTextComponent c);

    public void paint(Graphics g);

    public void addChangeListener(ChangeListener l);

    public void removeChangeListener(ChangeListener l);

    public boolean isVisible();

    public void setVisible(boolean v);

    public boolean isSelectionVisible();

    public void setSelectionVisible(boolean v);

    public Point getMagicCaretPosition();

    public void setMagicCaretPosition(Point p);

    public int getBlinkRate();

    public void setBlinkRate(int rate);

    public int getDot();

    public void setDot(int dot);

    public int getMark();

    public void moveDot(int dot);
};
