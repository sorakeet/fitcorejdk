/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import java.awt.*;

public interface Highlighter{
    public void install(JTextComponent c);

    public void deinstall(JTextComponent c);

    public void paint(Graphics g);

    public Object addHighlight(int p0,int p1,HighlightPainter p) throws BadLocationException;

    public void removeHighlight(Object tag);

    public void removeAllHighlights();

    public void changeHighlight(Object tag,int p0,int p1) throws BadLocationException;

    public Highlight[] getHighlights();

    public interface HighlightPainter{
        public void paint(Graphics g,int p0,int p1,Shape bounds,JTextComponent c);
    }

    public interface Highlight{
        public int getStartOffset();

        public int getEndOffset();

        public HighlightPainter getPainter();
    }
};
