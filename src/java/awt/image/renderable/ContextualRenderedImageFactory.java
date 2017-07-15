/**
 * Copyright (c) 1998, 2008, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;

public interface ContextualRenderedImageFactory extends RenderedImageFactory{
    RenderContext mapRenderContext(int i,
                                   RenderContext renderContext,
                                   ParameterBlock paramBlock,
                                   RenderableImage image);

    RenderedImage create(RenderContext renderContext,
                         ParameterBlock paramBlock);

    Rectangle2D getBounds2D(ParameterBlock paramBlock);

    Object getProperty(ParameterBlock paramBlock,String name);

    String[] getPropertyNames();

    boolean isDynamic();
}
