/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.awt.*;
import java.beans.ConstructorProperties;
import java.io.Serializable;

public class OverlayLayout implements LayoutManager2, Serializable{
    private Container target;
    private SizeRequirements[] xChildren;
    private SizeRequirements[] yChildren;
    private SizeRequirements xTotal;
    private SizeRequirements yTotal;

    @ConstructorProperties({"target"})
    public OverlayLayout(Container target){
        this.target=target;
    }

    public final Container getTarget(){
        return this.target;
    }

    public void addLayoutComponent(String name,Component comp){
        invalidateLayout(comp.getParent());
    }

    public void removeLayoutComponent(Component comp){
        invalidateLayout(comp.getParent());
    }

    public Dimension preferredLayoutSize(Container target){
        checkContainer(target);
        checkRequests();
        Dimension size=new Dimension(xTotal.preferred,yTotal.preferred);
        Insets insets=target.getInsets();
        size.width+=insets.left+insets.right;
        size.height+=insets.top+insets.bottom;
        return size;
    }

    public Dimension minimumLayoutSize(Container target){
        checkContainer(target);
        checkRequests();
        Dimension size=new Dimension(xTotal.minimum,yTotal.minimum);
        Insets insets=target.getInsets();
        size.width+=insets.left+insets.right;
        size.height+=insets.top+insets.bottom;
        return size;
    }

    public void layoutContainer(Container target){
        checkContainer(target);
        checkRequests();
        int nChildren=target.getComponentCount();
        int[] xOffsets=new int[nChildren];
        int[] xSpans=new int[nChildren];
        int[] yOffsets=new int[nChildren];
        int[] ySpans=new int[nChildren];
        // determine the child placements
        Dimension alloc=target.getSize();
        Insets in=target.getInsets();
        alloc.width-=in.left+in.right;
        alloc.height-=in.top+in.bottom;
        SizeRequirements.calculateAlignedPositions(alloc.width,xTotal,
                xChildren,xOffsets,
                xSpans);
        SizeRequirements.calculateAlignedPositions(alloc.height,yTotal,
                yChildren,yOffsets,
                ySpans);
        // flush changes to the container
        for(int i=0;i<nChildren;i++){
            Component c=target.getComponent(i);
            c.setBounds(in.left+xOffsets[i],in.top+yOffsets[i],
                    xSpans[i],ySpans[i]);
        }
    }

    void checkRequests(){
        if(xChildren==null||yChildren==null){
            // The requests have been invalidated... recalculate
            // the request information.
            int n=target.getComponentCount();
            xChildren=new SizeRequirements[n];
            yChildren=new SizeRequirements[n];
            for(int i=0;i<n;i++){
                Component c=target.getComponent(i);
                Dimension min=c.getMinimumSize();
                Dimension typ=c.getPreferredSize();
                Dimension max=c.getMaximumSize();
                xChildren[i]=new SizeRequirements(min.width,typ.width,
                        max.width,
                        c.getAlignmentX());
                yChildren[i]=new SizeRequirements(min.height,typ.height,
                        max.height,
                        c.getAlignmentY());
            }
            xTotal=SizeRequirements.getAlignedSizeRequirements(xChildren);
            yTotal=SizeRequirements.getAlignedSizeRequirements(yChildren);
        }
    }

    public void addLayoutComponent(Component comp,Object constraints){
        invalidateLayout(comp.getParent());
    }

    public Dimension maximumLayoutSize(Container target){
        checkContainer(target);
        checkRequests();
        Dimension size=new Dimension(xTotal.maximum,yTotal.maximum);
        Insets insets=target.getInsets();
        size.width+=insets.left+insets.right;
        size.height+=insets.top+insets.bottom;
        return size;
    }

    public float getLayoutAlignmentX(Container target){
        checkContainer(target);
        checkRequests();
        return xTotal.alignment;
    }

    public float getLayoutAlignmentY(Container target){
        checkContainer(target);
        checkRequests();
        return yTotal.alignment;
    }

    public void invalidateLayout(Container target){
        checkContainer(target);
        xChildren=null;
        yChildren=null;
        xTotal=null;
        yTotal=null;
    }

    void checkContainer(Container target){
        if(this.target!=target){
            throw new AWTError("OverlayLayout can't be shared");
        }
    }
}
