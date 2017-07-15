/**
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.image;

import java.awt.*;

public abstract class VolatileImage extends Image implements Transparency{
    // Return codes for validate() method
    public static final int IMAGE_OK=0;
    public static final int IMAGE_RESTORED=1;
    public static final int IMAGE_INCOMPATIBLE=2;
    protected int transparency=TRANSLUCENT;

    public abstract int getWidth();

    public abstract int getHeight();
    // Image overrides

    public ImageProducer getSource(){
        // REMIND: Make sure this functionality is in line with the
        // spec.  In particular, we are returning the Source for a
        // static image (the snapshot), not a changing image (the
        // VolatileImage).  So if the user expects the Source to be
        // up-to-date with the current contents of the VolatileImage,
        // they will be disappointed...
        // REMIND: This assumes that getSnapshot() returns something
        // valid and not the default null object returned by this class
        // (so it assumes that the actual VolatileImage object is
        // subclassed off something that does the right thing
        // (e.g., SunVolatileImage).
        return getSnapshot().getSource();
    }
    // REMIND: if we want any decent performance for getScaledInstance(),
    // we should override the Image implementation of it...

    public abstract BufferedImage getSnapshot();

    public Graphics getGraphics(){
        return createGraphics();
    }
    // Volatile management methods

    public abstract Graphics2D createGraphics();

    public abstract int validate(GraphicsConfiguration gc);

    public abstract boolean contentsLost();

    public abstract ImageCapabilities getCapabilities();

    public int getTransparency(){
        return transparency;
    }
}
