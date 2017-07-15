/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import sun.awt.AppContext;
import sun.reflect.misc.ReflectUtil;
import sun.security.action.GetPropertyAction;
import sun.swing.SwingUtilities2;
import sun.swing.UIAction;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleStateSet;
import javax.swing.event.MenuDragMouseEvent;
import javax.swing.plaf.UIResource;
import javax.swing.text.View;
import java.applet.Applet;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;

public class SwingUtilities implements SwingConstants{
    // Don't use String, as it's not guaranteed to be unique in a Hashtable.
    private static final Object sharedOwnerFrameKey=
            new StringBuffer("SwingUtilities.sharedOwnerFrame");
    // These states are system-wide, rather than AppContext wide.
    private static boolean canAccessEventQueue=false;
    private static boolean eventQueueTested=false;
    private static boolean suppressDropSupport;
    private static boolean checkedSuppressDropSupport;

    private SwingUtilities(){
        throw new Error("SwingUtilities is just a container for static methods");
    }

    static void installSwingDropTargetAsNecessary(Component c,
                                                  TransferHandler t){
        if(!getSuppressDropTarget()){
            DropTarget dropHandler=c.getDropTarget();
            if((dropHandler==null)||(dropHandler instanceof UIResource)){
                if(t==null){
                    c.setDropTarget(null);
                }else if(!GraphicsEnvironment.isHeadless()){
                    c.setDropTarget(new TransferHandler.SwingDropTarget(c));
                }
            }
        }
    }

    private static boolean getSuppressDropTarget(){
        if(!checkedSuppressDropSupport){
            suppressDropSupport=Boolean.valueOf(
                    AccessController.doPrivileged(
                            new GetPropertyAction("suppressSwingDropSupport")));
            checkedSuppressDropSupport=true;
        }
        return suppressDropSupport;
    }

    public static Rectangle getLocalBounds(Component aComponent){
        Rectangle b=new Rectangle(aComponent.getBounds());
        b.x=b.y=0;
        return b;
    }

    static Point convertScreenLocationToParent(Container parent,int x,int y){
        for(Container p=parent;p!=null;p=p.getParent()){
            if(p instanceof Window){
                Point point=new Point(x,y);
                SwingUtilities.convertPointFromScreen(point,parent);
                return point;
            }
        }
        throw new Error("convertScreenLocationToParent: no window ancestor");
    }

    public static void convertPointFromScreen(Point p,Component c){
        Rectangle b;
        int x, y;
        do{
            if(c instanceof JComponent){
                x=c.getX();
                y=c.getY();
            }else if(c instanceof Applet||
                    c instanceof Window){
                try{
                    Point pp=c.getLocationOnScreen();
                    x=pp.x;
                    y=pp.y;
                }catch(IllegalComponentStateException icse){
                    x=c.getX();
                    y=c.getY();
                }
            }else{
                x=c.getX();
                y=c.getY();
            }
            p.x-=x;
            p.y-=y;
            if(c instanceof Window||c instanceof Applet)
                break;
            c=c.getParent();
        }while(c!=null);
    }

    public static Point convertPoint(Component source,int x,int y,Component destination){
        Point point=new Point(x,y);
        return convertPoint(source,point,destination);
    }

    public static Point convertPoint(Component source,Point aPoint,Component destination){
        Point p;
        if(source==null&&destination==null)
            return aPoint;
        if(source==null){
            source=getWindowAncestor(destination);
            if(source==null)
                throw new Error("Source component not connected to component tree hierarchy");
        }
        p=new Point(aPoint);
        convertPointToScreen(p,source);
        if(destination==null){
            destination=getWindowAncestor(source);
            if(destination==null)
                throw new Error("Destination component not connected to component tree hierarchy");
        }
        convertPointFromScreen(p,destination);
        return p;
    }

    public static Window getWindowAncestor(Component c){
        for(Container p=c.getParent();p!=null;p=p.getParent()){
            if(p instanceof Window){
                return (Window)p;
            }
        }
        return null;
    }

    public static void convertPointToScreen(Point p,Component c){
        Rectangle b;
        int x, y;
        do{
            if(c instanceof JComponent){
                x=c.getX();
                y=c.getY();
            }else if(c instanceof Applet||
                    c instanceof Window){
                try{
                    Point pp=c.getLocationOnScreen();
                    x=pp.x;
                    y=pp.y;
                }catch(IllegalComponentStateException icse){
                    x=c.getX();
                    y=c.getY();
                }
            }else{
                x=c.getX();
                y=c.getY();
            }
            p.x+=x;
            p.y+=y;
            if(c instanceof Window||c instanceof Applet)
                break;
            c=c.getParent();
        }while(c!=null);
    }

    public static Rectangle convertRectangle(Component source,Rectangle aRectangle,Component destination){
        Point point=new Point(aRectangle.x,aRectangle.y);
        point=convertPoint(source,point,destination);
        return new Rectangle(point.x,point.y,aRectangle.width,aRectangle.height);
    }

    public static Container getAncestorOfClass(Class<?> c,Component comp){
        if(comp==null||c==null)
            return null;
        Container parent=comp.getParent();
        while(parent!=null&&!(c.isInstance(parent)))
            parent=parent.getParent();
        return parent;
    }

    public static Container getAncestorNamed(String name,Component comp){
        if(comp==null||name==null)
            return null;
        Container parent=comp.getParent();
        while(parent!=null&&!(name.equals(parent.getName())))
            parent=parent.getParent();
        return parent;
    }

    public static Component getDeepestComponentAt(Component parent,int x,int y){
        if(!parent.contains(x,y)){
            return null;
        }
        if(parent instanceof Container){
            Component components[]=((Container)parent).getComponents();
            for(Component comp : components){
                if(comp!=null&&comp.isVisible()){
                    Point loc=comp.getLocation();
                    if(comp instanceof Container){
                        comp=getDeepestComponentAt(comp,x-loc.x,y-loc.y);
                    }else{
                        comp=comp.getComponentAt(x-loc.x,y-loc.y);
                    }
                    if(comp!=null&&comp.isVisible()){
                        return comp;
                    }
                }
            }
        }
        return parent;
    }

    public static MouseEvent convertMouseEvent(Component source,
                                               MouseEvent sourceEvent,
                                               Component destination){
        Point p=convertPoint(source,new Point(sourceEvent.getX(),
                        sourceEvent.getY()),
                destination);
        Component newSource;
        if(destination!=null)
            newSource=destination;
        else
            newSource=source;
        MouseEvent newEvent;
        if(sourceEvent instanceof MouseWheelEvent){
            MouseWheelEvent sourceWheelEvent=(MouseWheelEvent)sourceEvent;
            newEvent=new MouseWheelEvent(newSource,
                    sourceWheelEvent.getID(),
                    sourceWheelEvent.getWhen(),
                    sourceWheelEvent.getModifiers()
                            |sourceWheelEvent.getModifiersEx(),
                    p.x,p.y,
                    sourceWheelEvent.getXOnScreen(),
                    sourceWheelEvent.getYOnScreen(),
                    sourceWheelEvent.getClickCount(),
                    sourceWheelEvent.isPopupTrigger(),
                    sourceWheelEvent.getScrollType(),
                    sourceWheelEvent.getScrollAmount(),
                    sourceWheelEvent.getWheelRotation());
        }else if(sourceEvent instanceof MenuDragMouseEvent){
            MenuDragMouseEvent sourceMenuDragEvent=(MenuDragMouseEvent)sourceEvent;
            newEvent=new MenuDragMouseEvent(newSource,
                    sourceMenuDragEvent.getID(),
                    sourceMenuDragEvent.getWhen(),
                    sourceMenuDragEvent.getModifiers()
                            |sourceMenuDragEvent.getModifiersEx(),
                    p.x,p.y,
                    sourceMenuDragEvent.getXOnScreen(),
                    sourceMenuDragEvent.getYOnScreen(),
                    sourceMenuDragEvent.getClickCount(),
                    sourceMenuDragEvent.isPopupTrigger(),
                    sourceMenuDragEvent.getPath(),
                    sourceMenuDragEvent.getMenuSelectionManager());
        }else{
            newEvent=new MouseEvent(newSource,
                    sourceEvent.getID(),
                    sourceEvent.getWhen(),
                    sourceEvent.getModifiers()
                            |sourceEvent.getModifiersEx(),
                    p.x,p.y,
                    sourceEvent.getXOnScreen(),
                    sourceEvent.getYOnScreen(),
                    sourceEvent.getClickCount(),
                    sourceEvent.isPopupTrigger(),
                    sourceEvent.getButton());
        }
        return newEvent;
    }

    public static Window windowForComponent(Component c){
        return getWindowAncestor(c);
    }

    public static boolean isDescendingFrom(Component a,Component b){
        if(a==b)
            return true;
        for(Container p=a.getParent();p!=null;p=p.getParent())
            if(p==b)
                return true;
        return false;
    }

    public static Rectangle computeIntersection(int x,int y,int width,int height,Rectangle dest){
        int x1=(x>dest.x)?x:dest.x;
        int x2=((x+width)<(dest.x+dest.width))?(x+width):(dest.x+dest.width);
        int y1=(y>dest.y)?y:dest.y;
        int y2=((y+height)<(dest.y+dest.height)?(y+height):(dest.y+dest.height));
        dest.x=x1;
        dest.y=y1;
        dest.width=x2-x1;
        dest.height=y2-y1;
        // If rectangles don't intersect, return zero'd intersection.
        if(dest.width<0||dest.height<0){
            dest.x=dest.y=dest.width=dest.height=0;
        }
        return dest;
    }

    public static Rectangle computeUnion(int x,int y,int width,int height,Rectangle dest){
        int x1=(x<dest.x)?x:dest.x;
        int x2=((x+width)>(dest.x+dest.width))?(x+width):(dest.x+dest.width);
        int y1=(y<dest.y)?y:dest.y;
        int y2=((y+height)>(dest.y+dest.height))?(y+height):(dest.y+dest.height);
        dest.x=x1;
        dest.y=y1;
        dest.width=(x2-x1);
        dest.height=(y2-y1);
        return dest;
    }

    public static Rectangle[] computeDifference(Rectangle rectA,Rectangle rectB){
        if(rectB==null||!rectA.intersects(rectB)||isRectangleContainingRectangle(rectB,rectA)){
            return new Rectangle[0];
        }
        Rectangle t=new Rectangle();
        Rectangle a=null, b=null, c=null, d=null;
        Rectangle result[];
        int rectCount=0;
        /** rectA contains rectB */
        if(isRectangleContainingRectangle(rectA,rectB)){
            t.x=rectA.x;
            t.y=rectA.y;
            t.width=rectB.x-rectA.x;
            t.height=rectA.height;
            if(t.width>0&&t.height>0){
                a=new Rectangle(t);
                rectCount++;
            }
            t.x=rectB.x;
            t.y=rectA.y;
            t.width=rectB.width;
            t.height=rectB.y-rectA.y;
            if(t.width>0&&t.height>0){
                b=new Rectangle(t);
                rectCount++;
            }
            t.x=rectB.x;
            t.y=rectB.y+rectB.height;
            t.width=rectB.width;
            t.height=rectA.y+rectA.height-(rectB.y+rectB.height);
            if(t.width>0&&t.height>0){
                c=new Rectangle(t);
                rectCount++;
            }
            t.x=rectB.x+rectB.width;
            t.y=rectA.y;
            t.width=rectA.x+rectA.width-(rectB.x+rectB.width);
            t.height=rectA.height;
            if(t.width>0&&t.height>0){
                d=new Rectangle(t);
                rectCount++;
            }
        }else{
            /** 1 */
            if(rectB.x<=rectA.x&&rectB.y<=rectA.y){
                if((rectB.x+rectB.width)>(rectA.x+rectA.width)){
                    t.x=rectA.x;
                    t.y=rectB.y+rectB.height;
                    t.width=rectA.width;
                    t.height=rectA.y+rectA.height-(rectB.y+rectB.height);
                    if(t.width>0&&t.height>0){
                        a=t;
                        rectCount++;
                    }
                }else if((rectB.y+rectB.height)>(rectA.y+rectA.height)){
                    t.setBounds((rectB.x+rectB.width),rectA.y,
                            (rectA.x+rectA.width)-(rectB.x+rectB.width),rectA.height);
                    if(t.width>0&&t.height>0){
                        a=t;
                        rectCount++;
                    }
                }else{
                    t.setBounds((rectB.x+rectB.width),rectA.y,
                            (rectA.x+rectA.width)-(rectB.x+rectB.width),
                            (rectB.y+rectB.height)-rectA.y);
                    if(t.width>0&&t.height>0){
                        a=new Rectangle(t);
                        rectCount++;
                    }
                    t.setBounds(rectA.x,(rectB.y+rectB.height),rectA.width,
                            (rectA.y+rectA.height)-(rectB.y+rectB.height));
                    if(t.width>0&&t.height>0){
                        b=new Rectangle(t);
                        rectCount++;
                    }
                }
            }else if(rectB.x<=rectA.x&&(rectB.y+rectB.height)>=(rectA.y+rectA.height)){
                if((rectB.x+rectB.width)>(rectA.x+rectA.width)){
                    t.setBounds(rectA.x,rectA.y,rectA.width,rectB.y-rectA.y);
                    if(t.width>0&&t.height>0){
                        a=t;
                        rectCount++;
                    }
                }else{
                    t.setBounds(rectA.x,rectA.y,rectA.width,rectB.y-rectA.y);
                    if(t.width>0&&t.height>0){
                        a=new Rectangle(t);
                        rectCount++;
                    }
                    t.setBounds((rectB.x+rectB.width),rectB.y,
                            (rectA.x+rectA.width)-(rectB.x+rectB.width),
                            (rectA.y+rectA.height)-rectB.y);
                    if(t.width>0&&t.height>0){
                        b=new Rectangle(t);
                        rectCount++;
                    }
                }
            }else if(rectB.x<=rectA.x){
                if((rectB.x+rectB.width)>=(rectA.x+rectA.width)){
                    t.setBounds(rectA.x,rectA.y,rectA.width,rectB.y-rectA.y);
                    if(t.width>0&&t.height>0){
                        a=new Rectangle(t);
                        rectCount++;
                    }
                    t.setBounds(rectA.x,(rectB.y+rectB.height),rectA.width,
                            (rectA.y+rectA.height)-(rectB.y+rectB.height));
                    if(t.width>0&&t.height>0){
                        b=new Rectangle(t);
                        rectCount++;
                    }
                }else{
                    t.setBounds(rectA.x,rectA.y,rectA.width,rectB.y-rectA.y);
                    if(t.width>0&&t.height>0){
                        a=new Rectangle(t);
                        rectCount++;
                    }
                    t.setBounds((rectB.x+rectB.width),rectB.y,
                            (rectA.x+rectA.width)-(rectB.x+rectB.width),
                            rectB.height);
                    if(t.width>0&&t.height>0){
                        b=new Rectangle(t);
                        rectCount++;
                    }
                    t.setBounds(rectA.x,(rectB.y+rectB.height),rectA.width,
                            (rectA.y+rectA.height)-(rectB.y+rectB.height));
                    if(t.width>0&&t.height>0){
                        c=new Rectangle(t);
                        rectCount++;
                    }
                }
            }else if(rectB.x<=(rectA.x+rectA.width)&&(rectB.x+rectB.width)>(rectA.x+rectA.width)){
                if(rectB.y<=rectA.y&&(rectB.y+rectB.height)>(rectA.y+rectA.height)){
                    t.setBounds(rectA.x,rectA.y,rectB.x-rectA.x,rectA.height);
                    if(t.width>0&&t.height>0){
                        a=t;
                        rectCount++;
                    }
                }else if(rectB.y<=rectA.y){
                    t.setBounds(rectA.x,rectA.y,rectB.x-rectA.x,
                            (rectB.y+rectB.height)-rectA.y);
                    if(t.width>0&&t.height>0){
                        a=new Rectangle(t);
                        rectCount++;
                    }
                    t.setBounds(rectA.x,(rectB.y+rectB.height),rectA.width,
                            (rectA.y+rectA.height)-(rectB.y+rectB.height));
                    if(t.width>0&&t.height>0){
                        b=new Rectangle(t);
                        rectCount++;
                    }
                }else if((rectB.y+rectB.height)>(rectA.y+rectA.height)){
                    t.setBounds(rectA.x,rectA.y,rectA.width,rectB.y-rectA.y);
                    if(t.width>0&&t.height>0){
                        a=new Rectangle(t);
                        rectCount++;
                    }
                    t.setBounds(rectA.x,rectB.y,rectB.x-rectA.x,
                            (rectA.y+rectA.height)-rectB.y);
                    if(t.width>0&&t.height>0){
                        b=new Rectangle(t);
                        rectCount++;
                    }
                }else{
                    t.setBounds(rectA.x,rectA.y,rectA.width,rectB.y-rectA.y);
                    if(t.width>0&&t.height>0){
                        a=new Rectangle(t);
                        rectCount++;
                    }
                    t.setBounds(rectA.x,rectB.y,rectB.x-rectA.x,
                            rectB.height);
                    if(t.width>0&&t.height>0){
                        b=new Rectangle(t);
                        rectCount++;
                    }
                    t.setBounds(rectA.x,(rectB.y+rectB.height),rectA.width,
                            (rectA.y+rectA.height)-(rectB.y+rectB.height));
                    if(t.width>0&&t.height>0){
                        c=new Rectangle(t);
                        rectCount++;
                    }
                }
            }else if(rectB.x>=rectA.x&&(rectB.x+rectB.width)<=(rectA.x+rectA.width)){
                if(rectB.y<=rectA.y&&(rectB.y+rectB.height)>(rectA.y+rectA.height)){
                    t.setBounds(rectA.x,rectA.y,rectB.x-rectA.x,rectA.height);
                    if(t.width>0&&t.height>0){
                        a=new Rectangle(t);
                        rectCount++;
                    }
                    t.setBounds((rectB.x+rectB.width),rectA.y,
                            (rectA.x+rectA.width)-(rectB.x+rectB.width),rectA.height);
                    if(t.width>0&&t.height>0){
                        b=new Rectangle(t);
                        rectCount++;
                    }
                }else if(rectB.y<=rectA.y){
                    t.setBounds(rectA.x,rectA.y,rectB.x-rectA.x,rectA.height);
                    if(t.width>0&&t.height>0){
                        a=new Rectangle(t);
                        rectCount++;
                    }
                    t.setBounds(rectB.x,(rectB.y+rectB.height),
                            rectB.width,
                            (rectA.y+rectA.height)-(rectB.y+rectB.height));
                    if(t.width>0&&t.height>0){
                        b=new Rectangle(t);
                        rectCount++;
                    }
                    t.setBounds((rectB.x+rectB.width),rectA.y,
                            (rectA.x+rectA.width)-(rectB.x+rectB.width),rectA.height);
                    if(t.width>0&&t.height>0){
                        c=new Rectangle(t);
                        rectCount++;
                    }
                }else{
                    t.setBounds(rectA.x,rectA.y,rectB.x-rectA.x,rectA.height);
                    if(t.width>0&&t.height>0){
                        a=new Rectangle(t);
                        rectCount++;
                    }
                    t.setBounds(rectB.x,rectA.y,rectB.width,
                            rectB.y-rectA.y);
                    if(t.width>0&&t.height>0){
                        b=new Rectangle(t);
                        rectCount++;
                    }
                    t.setBounds((rectB.x+rectB.width),rectA.y,
                            (rectA.x+rectA.width)-(rectB.x+rectB.width),rectA.height);
                    if(t.width>0&&t.height>0){
                        c=new Rectangle(t);
                        rectCount++;
                    }
                }
            }
        }
        result=new Rectangle[rectCount];
        rectCount=0;
        if(a!=null)
            result[rectCount++]=a;
        if(b!=null)
            result[rectCount++]=b;
        if(c!=null)
            result[rectCount++]=c;
        if(d!=null)
            result[rectCount++]=d;
        return result;
    }

    public static final boolean isRectangleContainingRectangle(Rectangle a,Rectangle b){
        return b.x>=a.x&&(b.x+b.width)<=(a.x+a.width)&&
                b.y>=a.y&&(b.y+b.height)<=(a.y+a.height);
    }

    public static boolean isLeftMouseButton(MouseEvent anEvent){
        return ((anEvent.getModifiersEx()&InputEvent.BUTTON1_DOWN_MASK)!=0||
                anEvent.getButton()==MouseEvent.BUTTON1);
    }

    public static boolean isMiddleMouseButton(MouseEvent anEvent){
        return ((anEvent.getModifiersEx()&InputEvent.BUTTON2_DOWN_MASK)!=0||
                anEvent.getButton()==MouseEvent.BUTTON2);
    }

    public static boolean isRightMouseButton(MouseEvent anEvent){
        return ((anEvent.getModifiersEx()&InputEvent.BUTTON3_DOWN_MASK)!=0||
                anEvent.getButton()==MouseEvent.BUTTON3);
    }

    public static int computeStringWidth(FontMetrics fm,String str){
        // You can't assume that a string's width is the sum of its
        // characters' widths in Java2D -- it may be smaller due to
        // kerning, etc.
        return SwingUtilities2.stringWidth(null,fm,str);
    }

    public static String layoutCompoundLabel(JComponent c,
                                             FontMetrics fm,
                                             String text,
                                             Icon icon,
                                             int verticalAlignment,
                                             int horizontalAlignment,
                                             int verticalTextPosition,
                                             int horizontalTextPosition,
                                             Rectangle viewR,
                                             Rectangle iconR,
                                             Rectangle textR,
                                             int textIconGap){
        boolean orientationIsLeftToRight=true;
        int hAlign=horizontalAlignment;
        int hTextPos=horizontalTextPosition;
        if(c!=null){
            if(!(c.getComponentOrientation().isLeftToRight())){
                orientationIsLeftToRight=false;
            }
        }
        // Translate LEADING/TRAILING values in horizontalAlignment
        // to LEFT/RIGHT values depending on the components orientation
        switch(horizontalAlignment){
            case LEADING:
                hAlign=(orientationIsLeftToRight)?LEFT:RIGHT;
                break;
            case TRAILING:
                hAlign=(orientationIsLeftToRight)?RIGHT:LEFT;
                break;
        }
        // Translate LEADING/TRAILING values in horizontalTextPosition
        // to LEFT/RIGHT values depending on the components orientation
        switch(horizontalTextPosition){
            case LEADING:
                hTextPos=(orientationIsLeftToRight)?LEFT:RIGHT;
                break;
            case TRAILING:
                hTextPos=(orientationIsLeftToRight)?RIGHT:LEFT;
                break;
        }
        return layoutCompoundLabelImpl(c,
                fm,
                text,
                icon,
                verticalAlignment,
                hAlign,
                verticalTextPosition,
                hTextPos,
                viewR,
                iconR,
                textR,
                textIconGap);
    }

    private static String layoutCompoundLabelImpl(
            JComponent c,
            FontMetrics fm,
            String text,
            Icon icon,
            int verticalAlignment,
            int horizontalAlignment,
            int verticalTextPosition,
            int horizontalTextPosition,
            Rectangle viewR,
            Rectangle iconR,
            Rectangle textR,
            int textIconGap){
        /** Initialize the icon bounds rectangle iconR.
         */
        if(icon!=null){
            iconR.width=icon.getIconWidth();
            iconR.height=icon.getIconHeight();
        }else{
            iconR.width=iconR.height=0;
        }
        /** Initialize the text bounds rectangle textR.  If a null
         * or and empty String was specified we substitute "" here
         * and use 0,0,0,0 for textR.
         */
        boolean textIsEmpty=(text==null)||text.equals("");
        int lsb=0;
        int rsb=0;
        /** Unless both text and icon are non-null, we effectively ignore
         * the value of textIconGap.
         */
        int gap;
        View v;
        if(textIsEmpty){
            textR.width=textR.height=0;
            text="";
            gap=0;
        }else{
            int availTextWidth;
            gap=(icon==null)?0:textIconGap;
            if(horizontalTextPosition==CENTER){
                availTextWidth=viewR.width;
            }else{
                availTextWidth=viewR.width-(iconR.width+gap);
            }
            v=(c!=null)?(View)c.getClientProperty("html"):null;
            if(v!=null){
                textR.width=Math.min(availTextWidth,
                        (int)v.getPreferredSpan(View.X_AXIS));
                textR.height=(int)v.getPreferredSpan(View.Y_AXIS);
            }else{
                textR.width=SwingUtilities2.stringWidth(c,fm,text);
                lsb=SwingUtilities2.getLeftSideBearing(c,fm,text);
                if(lsb<0){
                    // If lsb is negative, add it to the width and later
                    // adjust the x location. This gives more space than is
                    // actually needed.
                    // This is done like this for two reasons:
                    // 1. If we set the width to the actual bounds all
                    //    callers would have to account for negative lsb
                    //    (pref size calculations ONLY look at width of
                    //    textR)
                    // 2. You can do a drawString at the returned location
                    //    and the text won't be clipped.
                    textR.width-=lsb;
                }
                if(textR.width>availTextWidth){
                    text=SwingUtilities2.clipString(c,fm,text,
                            availTextWidth);
                    textR.width=SwingUtilities2.stringWidth(c,fm,text);
                }
                textR.height=fm.getHeight();
            }
        }
        /** Compute textR.x,y given the verticalTextPosition and
         * horizontalTextPosition properties
         */
        if(verticalTextPosition==TOP){
            if(horizontalTextPosition!=CENTER){
                textR.y=0;
            }else{
                textR.y=-(textR.height+gap);
            }
        }else if(verticalTextPosition==CENTER){
            textR.y=(iconR.height/2)-(textR.height/2);
        }else{ // (verticalTextPosition == BOTTOM)
            if(horizontalTextPosition!=CENTER){
                textR.y=iconR.height-textR.height;
            }else{
                textR.y=(iconR.height+gap);
            }
        }
        if(horizontalTextPosition==LEFT){
            textR.x=-(textR.width+gap);
        }else if(horizontalTextPosition==CENTER){
            textR.x=(iconR.width/2)-(textR.width/2);
        }else{ // (horizontalTextPosition == RIGHT)
            textR.x=(iconR.width+gap);
        }
        // WARNING: DefaultTreeCellEditor uses a shortened version of
        // this algorithm to position it's Icon. If you change how this
        // is calculated, be sure and update DefaultTreeCellEditor too.
        /** labelR is the rectangle that contains iconR and textR.
         * Move it to its proper position given the labelAlignment
         * properties.
         *
         * To avoid actually allocating a Rectangle, Rectangle.union
         * has been inlined below.
         */
        int labelR_x=Math.min(iconR.x,textR.x);
        int labelR_width=Math.max(iconR.x+iconR.width,
                textR.x+textR.width)-labelR_x;
        int labelR_y=Math.min(iconR.y,textR.y);
        int labelR_height=Math.max(iconR.y+iconR.height,
                textR.y+textR.height)-labelR_y;
        int dx, dy;
        if(verticalAlignment==TOP){
            dy=viewR.y-labelR_y;
        }else if(verticalAlignment==CENTER){
            dy=(viewR.y+(viewR.height/2))-(labelR_y+(labelR_height/2));
        }else{ // (verticalAlignment == BOTTOM)
            dy=(viewR.y+viewR.height)-(labelR_y+labelR_height);
        }
        if(horizontalAlignment==LEFT){
            dx=viewR.x-labelR_x;
        }else if(horizontalAlignment==RIGHT){
            dx=(viewR.x+viewR.width)-(labelR_x+labelR_width);
        }else{ // (horizontalAlignment == CENTER)
            dx=(viewR.x+(viewR.width/2))-
                    (labelR_x+(labelR_width/2));
        }
        /** Translate textR and glypyR by dx,dy.
         */
        textR.x+=dx;
        textR.y+=dy;
        iconR.x+=dx;
        iconR.y+=dy;
        if(lsb<0){
            // lsb is negative. Shift the x location so that the text is
            // visually drawn at the right location.
            textR.x-=lsb;
            textR.width+=lsb;
        }
        if(rsb>0){
            textR.width-=rsb;
        }
        return text;
    }

    public static String layoutCompoundLabel(
            FontMetrics fm,
            String text,
            Icon icon,
            int verticalAlignment,
            int horizontalAlignment,
            int verticalTextPosition,
            int horizontalTextPosition,
            Rectangle viewR,
            Rectangle iconR,
            Rectangle textR,
            int textIconGap){
        return layoutCompoundLabelImpl(null,fm,text,icon,
                verticalAlignment,
                horizontalAlignment,
                verticalTextPosition,
                horizontalTextPosition,
                viewR,iconR,textR,textIconGap);
    }

    public static void paintComponent(Graphics g,Component c,Container p,Rectangle r){
        paintComponent(g,c,p,r.x,r.y,r.width,r.height);
    }

    public static void paintComponent(Graphics g,Component c,Container p,int x,int y,int w,int h){
        getCellRendererPane(c,p).paintComponent(g,c,p,x,y,w,h,false);
    }

    private static CellRendererPane getCellRendererPane(Component c,Container p){
        Container shell=c.getParent();
        if(shell instanceof CellRendererPane){
            if(shell.getParent()!=p){
                p.add(shell);
            }
        }else{
            shell=new CellRendererPane();
            shell.add(c);
            p.add(shell);
        }
        return (CellRendererPane)shell;
    }

    public static void updateComponentTreeUI(Component c){
        updateComponentTreeUI0(c);
        c.invalidate();
        c.validate();
        c.repaint();
    }

    private static void updateComponentTreeUI0(Component c){
        if(c instanceof JComponent){
            JComponent jc=(JComponent)c;
            jc.updateUI();
            JPopupMenu jpm=jc.getComponentPopupMenu();
            if(jpm!=null){
                updateComponentTreeUI(jpm);
            }
        }
        Component[] children=null;
        if(c instanceof JMenu){
            children=((JMenu)c).getMenuComponents();
        }else if(c instanceof Container){
            children=((Container)c).getComponents();
        }
        if(children!=null){
            for(Component child : children){
                updateComponentTreeUI0(child);
            }
        }
    }

    public static void invokeLater(Runnable doRun){
        EventQueue.invokeLater(doRun);
    }

    public static void invokeAndWait(final Runnable doRun)
            throws InterruptedException, InvocationTargetException{
        EventQueue.invokeAndWait(doRun);
    }

    public static boolean isEventDispatchThread(){
        return EventQueue.isDispatchThread();
    }

    public static int getAccessibleIndexInParent(Component c){
        return c.getAccessibleContext().getAccessibleIndexInParent();
    }

    public static Accessible getAccessibleAt(Component c,Point p){
        if(c instanceof Container){
            return c.getAccessibleContext().getAccessibleComponent().getAccessibleAt(p);
        }else if(c instanceof Accessible){
            Accessible a=(Accessible)c;
            if(a!=null){
                AccessibleContext ac=a.getAccessibleContext();
                if(ac!=null){
                    AccessibleComponent acmp;
                    Point location;
                    int nchildren=ac.getAccessibleChildrenCount();
                    for(int i=0;i<nchildren;i++){
                        a=ac.getAccessibleChild(i);
                        if((a!=null)){
                            ac=a.getAccessibleContext();
                            if(ac!=null){
                                acmp=ac.getAccessibleComponent();
                                if((acmp!=null)&&(acmp.isShowing())){
                                    location=acmp.getLocation();
                                    Point np=new Point(p.x-location.x,
                                            p.y-location.y);
                                    if(acmp.contains(np)){
                                        return a;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return (Accessible)c;
        }
        return null;
    }

    public static AccessibleStateSet getAccessibleStateSet(Component c){
        return c.getAccessibleContext().getAccessibleStateSet();
    }

    public static int getAccessibleChildrenCount(Component c){
        return c.getAccessibleContext().getAccessibleChildrenCount();
    }

    public static Accessible getAccessibleChild(Component c,int i){
        return c.getAccessibleContext().getAccessibleChild(i);
    }

    @Deprecated
    public static Component findFocusOwner(Component c){
        Component focusOwner=KeyboardFocusManager.
                getCurrentKeyboardFocusManager().getFocusOwner();
        // verify focusOwner is a descendant of c
        for(Component temp=focusOwner;temp!=null;
            temp=(temp instanceof Window)?null:temp.getParent()){
            if(temp==c){
                return focusOwner;
            }
        }
        return null;
    }

    public static JRootPane getRootPane(Component c){
        if(c instanceof RootPaneContainer){
            return ((RootPaneContainer)c).getRootPane();
        }
        for(;c!=null;c=c.getParent()){
            if(c instanceof JRootPane){
                return (JRootPane)c;
            }
        }
        return null;
    }

    public static Component getRoot(Component c){
        Component applet=null;
        for(Component p=c;p!=null;p=p.getParent()){
            if(p instanceof Window){
                return p;
            }
            if(p instanceof Applet){
                applet=p;
            }
        }
        return applet;
    }

    static JComponent getPaintingOrigin(JComponent c){
        Container p=c;
        while((p=p.getParent()) instanceof JComponent){
            JComponent jp=(JComponent)p;
            if(jp.isPaintingOrigin()){
                return jp;
            }
        }
        return null;
    }

    public static boolean processKeyBindings(KeyEvent event){
        if(event!=null){
            if(event.isConsumed()){
                return false;
            }
            Component component=event.getComponent();
            boolean pressed=(event.getID()==KeyEvent.KEY_PRESSED);
            if(!isValidKeyEventForKeyBindings(event)){
                return false;
            }
            // Find the first JComponent in the ancestor hierarchy, and
            // invoke processKeyBindings on it
            while(component!=null){
                if(component instanceof JComponent){
                    return ((JComponent)component).processKeyBindings(
                            event,pressed);
                }
                if((component instanceof Applet)||
                        (component instanceof Window)){
                    // No JComponents, if Window or Applet parent, process
                    // WHEN_IN_FOCUSED_WINDOW bindings.
                    return JComponent.processKeyBindingsForAllComponents(
                            event,(Container)component,pressed);
                }
                component=component.getParent();
            }
        }
        return false;
    }

    static boolean isValidKeyEventForKeyBindings(KeyEvent e){
        return true;
    }

    public static boolean notifyAction(Action action,KeyStroke ks,
                                       KeyEvent event,Object sender,
                                       int modifiers){
        if(action==null){
            return false;
        }
        if(action instanceof UIAction){
            if(!((UIAction)action).isEnabled(sender)){
                return false;
            }
        }else if(!action.isEnabled()){
            return false;
        }
        Object commandO;
        boolean stayNull;
        // Get the command object.
        commandO=action.getValue(Action.ACTION_COMMAND_KEY);
        if(commandO==null&&(action instanceof JComponent.ActionStandin)){
            // ActionStandin is used for historical reasons to support
            // registerKeyboardAction with a null value.
            stayNull=true;
        }else{
            stayNull=false;
        }
        // Convert it to a string.
        String command;
        if(commandO!=null){
            command=commandO.toString();
        }else if(!stayNull&&event.getKeyChar()!=KeyEvent.CHAR_UNDEFINED){
            command=String.valueOf(event.getKeyChar());
        }else{
            // Do null for undefined chars, or if registerKeyboardAction
            // was called with a null.
            command=null;
        }
        action.actionPerformed(new ActionEvent(sender,
                ActionEvent.ACTION_PERFORMED,command,event.getWhen(),
                modifiers));
        return true;
    }

    public static void replaceUIInputMap(JComponent component,int type,
                                         InputMap uiInputMap){
        InputMap map=component.getInputMap(type,(uiInputMap!=null));
        while(map!=null){
            InputMap parent=map.getParent();
            if(parent==null||(parent instanceof UIResource)){
                map.setParent(uiInputMap);
                return;
            }
            map=parent;
        }
    }

    public static void replaceUIActionMap(JComponent component,
                                          ActionMap uiActionMap){
        ActionMap map=component.getActionMap((uiActionMap!=null));
        while(map!=null){
            ActionMap parent=map.getParent();
            if(parent==null||(parent instanceof UIResource)){
                map.setParent(uiActionMap);
                return;
            }
            map=parent;
        }
    }

    public static InputMap getUIInputMap(JComponent component,int condition){
        InputMap map=component.getInputMap(condition,false);
        while(map!=null){
            InputMap parent=map.getParent();
            if(parent instanceof UIResource){
                return parent;
            }
            map=parent;
        }
        return null;
    }

    public static ActionMap getUIActionMap(JComponent component){
        ActionMap map=component.getActionMap(false);
        while(map!=null){
            ActionMap parent=map.getParent();
            if(parent instanceof UIResource){
                return parent;
            }
            map=parent;
        }
        return null;
    }

    static WindowListener getSharedOwnerFrameShutdownListener() throws HeadlessException{
        Frame sharedOwnerFrame=getSharedOwnerFrame();
        return (WindowListener)sharedOwnerFrame;
    }

    static Frame getSharedOwnerFrame() throws HeadlessException{
        Frame sharedOwnerFrame=
                (Frame)SwingUtilities.appContextGet(sharedOwnerFrameKey);
        if(sharedOwnerFrame==null){
            sharedOwnerFrame=new SharedOwnerFrame();
            SwingUtilities.appContextPut(sharedOwnerFrameKey,
                    sharedOwnerFrame);
        }
        return sharedOwnerFrame;
    }
    // REMIND(aim): phase out use of 4 methods below since they
    // are just private covers for AWT methods (?)

    static Object appContextGet(Object key){
        return AppContext.getAppContext().get(key);
    }

    static void appContextPut(Object key,Object value){
        AppContext.getAppContext().put(key,value);
    }

    static void appContextRemove(Object key){
        AppContext.getAppContext().remove(key);
    }

    static Class<?> loadSystemClass(String className) throws ClassNotFoundException{
        ReflectUtil.checkPackageAccess(className);
        return Class.forName(className,true,Thread.currentThread().
                getContextClassLoader());
    }

    static boolean isLeftToRight(Component c){
        return c.getComponentOrientation().isLeftToRight();
    }

    static boolean doesIconReferenceImage(Icon icon,Image image){
        Image iconImage=(icon!=null&&(icon instanceof ImageIcon))?
                ((ImageIcon)icon).getImage():null;
        return (iconImage==image);
    }

    static int findDisplayedMnemonicIndex(String text,int mnemonic){
        if(text==null||mnemonic=='\0'){
            return -1;
        }
        char uc=Character.toUpperCase((char)mnemonic);
        char lc=Character.toLowerCase((char)mnemonic);
        int uci=text.indexOf(uc);
        int lci=text.indexOf(lc);
        if(uci==-1){
            return lci;
        }else if(lci==-1){
            return uci;
        }else{
            return (lci<uci)?lci:uci;
        }
    }

    public static Rectangle calculateInnerArea(JComponent c,Rectangle r){
        if(c==null){
            return null;
        }
        Rectangle rect=r;
        Insets insets=c.getInsets();
        if(rect==null){
            rect=new Rectangle();
        }
        rect.x=insets.left;
        rect.y=insets.top;
        rect.width=c.getWidth()-insets.left-insets.right;
        rect.height=c.getHeight()-insets.top-insets.bottom;
        return rect;
    }

    static void updateRendererOrEditorUI(Object rendererOrEditor){
        if(rendererOrEditor==null){
            return;
        }
        Component component=null;
        if(rendererOrEditor instanceof Component){
            component=(Component)rendererOrEditor;
        }
        if(rendererOrEditor instanceof DefaultCellEditor){
            component=((DefaultCellEditor)rendererOrEditor).getComponent();
        }
        if(component!=null){
            SwingUtilities.updateComponentTreeUI(component);
        }
    }

    public static Container getUnwrappedParent(Component component){
        Container parent=component.getParent();
        while(parent instanceof JLayer){
            parent=parent.getParent();
        }
        return parent;
    }

    public static Component getUnwrappedView(JViewport viewport){
        Component view=viewport.getView();
        while(view instanceof JLayer){
            view=((JLayer)view).getView();
        }
        return view;
    }

    static Container getValidateRoot(Container c,boolean visibleOnly){
        Container root=null;
        for(;c!=null;c=c.getParent()){
            if(!c.isDisplayable()||c instanceof CellRendererPane){
                return null;
            }
            if(c.isValidateRoot()){
                root=c;
                break;
            }
        }
        if(root==null){
            return null;
        }
        for(;c!=null;c=c.getParent()){
            if(!c.isDisplayable()||(visibleOnly&&!c.isVisible())){
                return null;
            }
            if(c instanceof Window||c instanceof Applet){
                return root;
            }
        }
        return null;
    }

    static class SharedOwnerFrame extends Frame implements WindowListener{
        public void addNotify(){
            super.addNotify();
            installListeners();
        }

        void installListeners(){
            Window[] windows=getOwnedWindows();
            for(Window window : windows){
                if(window!=null){
                    window.removeWindowListener(this);
                    window.addWindowListener(this);
                }
            }
        }

        public void windowOpened(WindowEvent e){
        }

        public void windowClosing(WindowEvent e){
        }

        public void windowClosed(WindowEvent e){
            synchronized(getTreeLock()){
                Window[] windows=getOwnedWindows();
                for(Window window : windows){
                    if(window!=null){
                        if(window.isDisplayable()){
                            return;
                        }
                        window.removeWindowListener(this);
                    }
                }
                dispose();
            }
        }

        public void windowIconified(WindowEvent e){
        }

        public void windowDeiconified(WindowEvent e){
        }

        public void windowActivated(WindowEvent e){
        }

        public void windowDeactivated(WindowEvent e){
        }

        public void show(){
            // This frame can never be shown
        }

        public void dispose(){
            try{
                getToolkit().getSystemEventQueue();
                super.dispose();
            }catch(Exception e){
                // untrusted code not allowed to dispose
            }
        }
    }
}
