/**
 * Copyright (c) 2005, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.nimbus;

import javax.swing.*;
import javax.swing.plaf.nimbus.AbstractRegionPainter.PaintContext.CacheMode;
import java.awt.*;

final class ToolBarSeparatorPainter extends AbstractRegionPainter{
    private static final int SPACE=3;
    private static final int INSET=2;

    @Override
    protected PaintContext getPaintContext(){
        //the paint context returned will have a few dummy values. The
        //implementation of doPaint doesn't bother with the "decode" methods
        //but calculates where to paint the circles manually. As such, we
        //only need to indicate in our PaintContext that we don't want this
        //to ever be cached
        return new PaintContext(
                new Insets(1,0,1,0),
                new Dimension(38,7),
                false,CacheMode.NO_CACHING,1,1);
    }

    @Override
    protected void doPaint(Graphics2D g,JComponent c,int width,int height,Object[] extendedCacheKeys){
        //it is assumed that in the normal orientation the separator renders
        //horizontally. Other code rotates it as necessary for a vertical
        //separator.
        g.setColor(c.getForeground());
        int y=height/2;
        for(int i=INSET;i<=width-INSET;i+=SPACE){
            g.fillRect(i,y,1,1);
        }
    }
}
