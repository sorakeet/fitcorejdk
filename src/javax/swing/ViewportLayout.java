/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.awt.*;
import java.io.Serializable;

public class ViewportLayout implements LayoutManager, Serializable{
    // Single instance used by JViewport.
    static ViewportLayout SHARED_INSTANCE=new ViewportLayout();

    public void addLayoutComponent(String name,Component c){
    }

    public void removeLayoutComponent(Component c){
    }

    public Dimension preferredLayoutSize(Container parent){
        Component view=((JViewport)parent).getView();
        if(view==null){
            return new Dimension(0,0);
        }else if(view instanceof Scrollable){
            return ((Scrollable)view).getPreferredScrollableViewportSize();
        }else{
            return view.getPreferredSize();
        }
    }

    public Dimension minimumLayoutSize(Container parent){
        return new Dimension(4,4);
    }

    public void layoutContainer(Container parent){
        JViewport vp=(JViewport)parent;
        Component view=vp.getView();
        Scrollable scrollableView=null;
        if(view==null){
            return;
        }else if(view instanceof Scrollable){
            scrollableView=(Scrollable)view;
        }
        /** All of the dimensions below are in view coordinates, except
         * vpSize which we're converting.
         */
        Insets insets=vp.getInsets();
        Dimension viewPrefSize=view.getPreferredSize();
        Dimension vpSize=vp.getSize();
        Dimension extentSize=vp.toViewCoordinates(vpSize);
        Dimension viewSize=new Dimension(viewPrefSize);
        if(scrollableView!=null){
            if(scrollableView.getScrollableTracksViewportWidth()){
                viewSize.width=vpSize.width;
            }
            if(scrollableView.getScrollableTracksViewportHeight()){
                viewSize.height=vpSize.height;
            }
        }
        Point viewPosition=vp.getViewPosition();
        /** If the new viewport size would leave empty space to the
         * right of the view, right justify the view or left justify
         * the view when the width of the view is smaller than the
         * container.
         */
        if(scrollableView==null||
                vp.getParent()==null||
                vp.getParent().getComponentOrientation().isLeftToRight()){
            if((viewPosition.x+extentSize.width)>viewSize.width){
                viewPosition.x=Math.max(0,viewSize.width-extentSize.width);
            }
        }else{
            if(extentSize.width>viewSize.width){
                viewPosition.x=viewSize.width-extentSize.width;
            }else{
                viewPosition.x=Math.max(0,Math.min(viewSize.width-extentSize.width,viewPosition.x));
            }
        }
        /** If the new viewport size would leave empty space below the
         * view, bottom justify the view or top justify the view when
         * the height of the view is smaller than the container.
         */
        if((viewPosition.y+extentSize.height)>viewSize.height){
            viewPosition.y=Math.max(0,viewSize.height-extentSize.height);
        }
        /** If we haven't been advised about how the viewports size
         * should change wrt to the viewport, i.e. if the view isn't
         * an instance of Scrollable, then adjust the views size as follows.
         *
         * If the origin of the view is showing and the viewport is
         * bigger than the views preferred size, then make the view
         * the same size as the viewport.
         */
        if(scrollableView==null){
            if((viewPosition.x==0)&&(vpSize.width>viewPrefSize.width)){
                viewSize.width=vpSize.width;
            }
            if((viewPosition.y==0)&&(vpSize.height>viewPrefSize.height)){
                viewSize.height=vpSize.height;
            }
        }
        vp.setViewPosition(viewPosition);
        vp.setViewSize(viewSize);
    }
}
