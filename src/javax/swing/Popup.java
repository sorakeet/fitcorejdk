/**
 * Copyright (c) 1999, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import sun.awt.ModalExclude;

import java.awt.*;

public class Popup{
    private Component component;

    protected Popup(Component owner,Component contents,int x,int y){
        this();
        if(contents==null){
            throw new IllegalArgumentException("Contents must be non-null");
        }
        reset(owner,contents,x,y);
    }

    protected Popup(){
    }

    void reset(Component owner,Component contents,int ownerX,int ownerY){
        if(getComponent()==null){
            component=createComponent(owner);
        }
        Component c=getComponent();
        if(c instanceof JWindow){
            JWindow component=(JWindow)getComponent();
            component.setLocation(ownerX,ownerY);
            component.getContentPane().add(contents,BorderLayout.CENTER);
            component.invalidate();
            component.validate();
            if(component.isVisible()){
                // Do not call pack() if window is not visible to
                // avoid early native peer creation
                pack();
            }
        }
    }

    void pack(){
        Component component=getComponent();
        if(component instanceof Window){
            ((Window)component).pack();
        }
    }

    Component createComponent(Component owner){
        if(GraphicsEnvironment.isHeadless()){
            // Generally not useful, bail.
            return null;
        }
        return new HeavyWeightWindow(getParentWindow(owner));
    }

    private Window getParentWindow(Component owner){
        Window window=null;
        if(owner instanceof Window){
            window=(Window)owner;
        }else if(owner!=null){
            window=SwingUtilities.getWindowAncestor(owner);
        }
        if(window==null){
            window=new DefaultFrame();
        }
        return window;
    }

    Component getComponent(){
        return component;
    }

    @SuppressWarnings("deprecation")
    public void show(){
        Component component=getComponent();
        if(component!=null){
            component.show();
        }
    }

    @SuppressWarnings("deprecation")
    public void hide(){
        Component component=getComponent();
        if(component instanceof JWindow){
            component.hide();
            ((JWindow)component).getContentPane().removeAll();
        }
        dispose();
    }

    void dispose(){
        Component component=getComponent();
        Window window=SwingUtilities.getWindowAncestor(component);
        if(component instanceof JWindow){
            ((Window)component).dispose();
            component=null;
        }
        // If our parent is a DefaultFrame, we need to dispose it, too.
        if(window instanceof DefaultFrame){
            window.dispose();
        }
    }

    static class HeavyWeightWindow extends JWindow implements ModalExclude{
        HeavyWeightWindow(Window parent){
            super(parent);
            setFocusableWindowState(false);
            setType(Type.POPUP);
            // Popups are typically transient and most likely won't benefit
            // from true double buffering.  Turn it off here.
            getRootPane().setUseTrueDoubleBuffering(false);
            // Try to set "always-on-top" for the popup window.
            // Applets usually don't have sufficient permissions to do it.
            // In this case simply ignore the exception.
            try{
                setAlwaysOnTop(true);
            }catch(SecurityException se){
                // setAlwaysOnTop is restricted,
                // the exception is ignored
            }
        }

        public void update(Graphics g){
            paint(g);
        }

        public void show(){
            this.pack();
            if(getWidth()>0&&getHeight()>0){
                super.show();
            }
        }
    }

    static class DefaultFrame extends Frame{
    }
}
