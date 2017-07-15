/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * *******************************************************************
 * *********************************************************************
 * *********************************************************************
 * ** COPYRIGHT (c) Eastman Kodak Company, 1997                      ***
 * ** As  an unpublished  work pursuant to Title 17 of the United    ***
 * ** States Code.  All rights reserved.                             ***
 * *********************************************************************
 * *********************************************************************
 **********************************************************************/
/** ********************************************************************
 **********************************************************************
 **********************************************************************
 *** COPYRIGHT (c) Eastman Kodak Company, 1997                      ***
 *** As  an unpublished  work pursuant to Title 17 of the United    ***
 *** States Code.  All rights reserved.                             ***
 **********************************************************************
 **********************************************************************
 **********************************************************************/
package java.awt.image.renderable;

import java.awt.*;
import java.awt.image.RenderedImage;
import java.util.Vector;

public interface RenderableImage{
    static final String HINTS_OBSERVED="HINTS_OBSERVED";

    Vector<RenderableImage> getSources();

    Object getProperty(String name);

    String[] getPropertyNames();

    boolean isDynamic();

    float getWidth();

    float getHeight();

    float getMinX();

    float getMinY();

    RenderedImage createScaledRendering(int w,int h,RenderingHints hints);

    RenderedImage createDefaultRendering();

    RenderedImage createRendering(RenderContext renderContext);
}
