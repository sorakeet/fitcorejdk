/**
 * Copyright (c) 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import java.awt.*;

public abstract class LayeredHighlighter implements Highlighter{
    public abstract void paintLayeredHighlights(Graphics g,int p0,int p1,
                                                Shape viewBounds,
                                                JTextComponent editor,
                                                View view);

    static public abstract class LayerPainter implements HighlightPainter{
        public abstract Shape paintLayer(Graphics g,int p0,int p1,
                                         Shape viewBounds,JTextComponent editor,
                                         View view);
    }
}
