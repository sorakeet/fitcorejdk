/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import java.awt.*;
import java.beans.ConstructorProperties;

@SuppressWarnings("serial")
public class Box extends JComponent implements Accessible{
    public Box(int axis){
        super();
        super.setLayout(new BoxLayout(this,axis));
    }

    public static Box createHorizontalBox(){
        return new Box(BoxLayout.X_AXIS);
    }

    public static Box createVerticalBox(){
        return new Box(BoxLayout.Y_AXIS);
    }

    public static Component createRigidArea(Dimension d){
        return new Filler(d,d,d);
    }

    public static Component createHorizontalStrut(int width){
        return new Filler(new Dimension(width,0),new Dimension(width,0),
                new Dimension(width,Short.MAX_VALUE));
    }

    public static Component createVerticalStrut(int height){
        return new Filler(new Dimension(0,height),new Dimension(0,height),
                new Dimension(Short.MAX_VALUE,height));
    }

    public static Component createGlue(){
        return new Filler(new Dimension(0,0),new Dimension(0,0),
                new Dimension(Short.MAX_VALUE,Short.MAX_VALUE));
    }

    public static Component createHorizontalGlue(){
        return new Filler(new Dimension(0,0),new Dimension(0,0),
                new Dimension(Short.MAX_VALUE,0));
    }

    public static Component createVerticalGlue(){
        return new Filler(new Dimension(0,0),new Dimension(0,0),
                new Dimension(0,Short.MAX_VALUE));
    }

    public void setLayout(LayoutManager l){
        throw new AWTError("Illegal request");
    }

    protected void paintComponent(Graphics g){
        if(ui!=null){
            // On the off chance some one created a UI, honor it
            super.paintComponent(g);
        }else if(isOpaque()){
            g.setColor(getBackground());
            g.fillRect(0,0,getWidth(),getHeight());
        }
    }

    @SuppressWarnings("serial")
    public static class Filler extends JComponent implements Accessible{
        @ConstructorProperties({"minimumSize","preferredSize","maximumSize"})
        public Filler(Dimension min,Dimension pref,Dimension max){
            setMinimumSize(min);
            setPreferredSize(pref);
            setMaximumSize(max);
        }

        public void changeShape(Dimension min,Dimension pref,Dimension max){
            setMinimumSize(min);
            setPreferredSize(pref);
            setMaximumSize(max);
            revalidate();
        }
        // ---- Component methods ------------------------------------------

        public AccessibleContext getAccessibleContext(){
            if(accessibleContext==null){
                accessibleContext=new AccessibleBoxFiller();
            }
            return accessibleContext;
        }        protected void paintComponent(Graphics g){
            if(ui!=null){
                // On the off chance some one created a UI, honor it
                super.paintComponent(g);
            }else if(isOpaque()){
                g.setColor(getBackground());
                g.fillRect(0,0,getWidth(),getHeight());
            }
        }
/////////////////
// Accessibility support for Box$Filler
////////////////

        @SuppressWarnings("serial")
        protected class AccessibleBoxFiller extends AccessibleAWTComponent{
            // AccessibleContext methods
            //
            public AccessibleRole getAccessibleRole(){
                return AccessibleRole.FILLER;
            }
        }


    }
/////////////////
// Accessibility support for Box
////////////////

    @SuppressWarnings("serial")
    protected class AccessibleBox extends AccessibleAWTContainer{
        // AccessibleContext methods
        //
        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.FILLER;
        }
    } // inner class AccessibleBox    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleBox();
        }
        return accessibleContext;
    }


}
