/**
 * Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.synth;

import javax.swing.*;
import java.awt.*;

public interface SynthUI extends SynthConstants{
    public SynthContext getContext(JComponent c);

    public void paintBorder(SynthContext context,Graphics g,int x,
                            int y,int w,int h);
}
