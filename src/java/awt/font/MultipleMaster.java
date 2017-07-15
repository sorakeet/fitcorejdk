/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.font;

import java.awt.*;

public interface MultipleMaster{
    public int getNumDesignAxes();

    public float[] getDesignAxisRanges();

    public float[] getDesignAxisDefaults();

    public String[] getDesignAxisNames();

    public Font deriveMMFont(float[] axes);

    public Font deriveMMFont(
            float[] glyphWidths,
            float avgStemWidth,
            float typicalCapHeight,
            float typicalXHeight,
            float italicAngle);
}
