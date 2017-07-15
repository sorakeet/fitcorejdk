/**
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

class Autoscroller implements ActionListener{
    private static Autoscroller sharedInstance=new Autoscroller();
    // As there can only ever be one autoscroller active these fields are
    // static. The Timer is recreated as necessary to target the appropriate
    // Autoscroller instance.
    private static MouseEvent event;
    private static Timer timer;
    private static JComponent component;

    Autoscroller(){
    }

    //
    // The public API, all methods are cover methods for an instance method
    //
    public static void stop(JComponent c){
        sharedInstance._stop(c);
    }

    public static boolean isRunning(JComponent c){
        return sharedInstance._isRunning(c);
    }

    public static void processMouseDragged(MouseEvent e){
        sharedInstance._processMouseDragged(e);
    }

    private boolean _isRunning(JComponent c){
        return (c==component&&timer!=null&&timer.isRunning());
    }
    //
    // Methods mirror the public static API
    //

    private void _processMouseDragged(MouseEvent e){
        JComponent component=(JComponent)e.getComponent();
        boolean stop=true;
        if(component.isShowing()){
            Rectangle visibleRect=component.getVisibleRect();
            stop=visibleRect.contains(e.getX(),e.getY());
        }
        if(stop){
            _stop(component);
        }else{
            start(component,e);
        }
    }

    private void start(JComponent c,MouseEvent e){
        Point screenLocation=c.getLocationOnScreen();
        if(component!=c){
            _stop(component);
        }
        component=c;
        event=new MouseEvent(component,e.getID(),e.getWhen(),
                e.getModifiers(),e.getX()+screenLocation.x,
                e.getY()+screenLocation.y,
                e.getXOnScreen(),
                e.getYOnScreen(),
                e.getClickCount(),e.isPopupTrigger(),
                MouseEvent.NOBUTTON);
        if(timer==null){
            timer=new Timer(100,this);
        }
        if(!timer.isRunning()){
            timer.start();
        }
    }

    private void _stop(JComponent c){
        if(component==c){
            if(timer!=null){
                timer.stop();
            }
            timer=null;
            event=null;
            component=null;
        }
    }

    //
    // ActionListener
    //
    public void actionPerformed(ActionEvent x){
        JComponent component=Autoscroller.component;
        if(component==null||!component.isShowing()||(event==null)){
            _stop(component);
            return;
        }
        Point screenLocation=component.getLocationOnScreen();
        MouseEvent e=new MouseEvent(component,event.getID(),
                event.getWhen(),event.getModifiers(),
                event.getX()-screenLocation.x,
                event.getY()-screenLocation.y,
                event.getXOnScreen(),
                event.getYOnScreen(),
                event.getClickCount(),
                event.isPopupTrigger(),
                MouseEvent.NOBUTTON);
        component.superProcessMouseMotionEvent(e);
    }
}
