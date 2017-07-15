/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.metal;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicToolBarUI;
import java.awt.*;
import java.awt.event.ContainerListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MetalToolBarUI extends BasicToolBarUI{
    private static List<WeakReference<JComponent>> components=new ArrayList<WeakReference<JComponent>>();
    private static Border nonRolloverBorder;
    protected ContainerListener contListener;
    protected PropertyChangeListener rolloverListener;
    private JMenuBar lastMenuBar;

    static boolean doesMenuBarBorderToolBar(JMenuBar c){
        JToolBar tb=(JToolBar)MetalToolBarUI.
                findRegisteredComponentOfType(c,JToolBar.class);
        if(tb!=null&&tb.getOrientation()==JToolBar.HORIZONTAL){
            JRootPane rp=SwingUtilities.getRootPane(c);
            Point point=new Point(0,0);
            point=SwingUtilities.convertPoint(c,point,rp);
            int menuX=point.x;
            int menuY=point.y;
            point.x=point.y=0;
            point=SwingUtilities.convertPoint(tb,point,rp);
            return (point.x==menuX&&menuY+c.getHeight()==point.y&&
                    c.getWidth()==tb.getWidth());
        }
        return false;
    }

    synchronized static Object findRegisteredComponentOfType(JComponent from,
                                                             Class target){
        JRootPane rp=SwingUtilities.getRootPane(from);
        if(rp!=null){
            for(int counter=components.size()-1;counter>=0;counter--){
                Object component=((WeakReference)components.get(counter)).
                        get();
                if(component==null){
                    // WeakReference has gone away, remove the WeakReference
                    components.remove(counter);
                }else if(target.isInstance(component)&&SwingUtilities.
                        getRootPane((Component)component)==rp){
                    return component;
                }
            }
        }
        return null;
    }

    public static ComponentUI createUI(JComponent c){
        return new MetalToolBarUI();
    }

    public void installUI(JComponent c){
        super.installUI(c);
        register(c);
    }

    synchronized static void register(JComponent c){
        if(c==null){
            // Exception is thrown as convenience for callers that are
            // typed to throw an NPE.
            throw new NullPointerException("JComponent must be non-null");
        }
        components.add(new WeakReference<JComponent>(c));
    }

    public void uninstallUI(JComponent c){
        super.uninstallUI(c);
        nonRolloverBorder=null;
        unregister(c);
    }

    synchronized static void unregister(JComponent c){
        for(int counter=components.size()-1;counter>=0;counter--){
            // Search for the component, removing any flushed references
            // along the way.
            JComponent target=components.get(counter).get();
            if(target==c||target==null){
                components.remove(counter);
            }
        }
    }

    protected void installListeners(){
        super.installListeners();
        contListener=createContainerListener();
        if(contListener!=null){
            toolBar.addContainerListener(contListener);
        }
        rolloverListener=createRolloverListener();
        if(rolloverListener!=null){
            toolBar.addPropertyChangeListener(rolloverListener);
        }
    }

    protected void uninstallListeners(){
        super.uninstallListeners();
        if(contListener!=null){
            toolBar.removeContainerListener(contListener);
        }
        rolloverListener=createRolloverListener();
        if(rolloverListener!=null){
            toolBar.removePropertyChangeListener(rolloverListener);
        }
    }

    protected Border createRolloverBorder(){
        return super.createRolloverBorder();
    }

    protected Border createNonRolloverBorder(){
        return super.createNonRolloverBorder();
    }

    protected void setBorderToNonRollover(Component c){
        if(c instanceof JToggleButton&&!(c instanceof JCheckBox)){
            // 4735514, 4886944: The method createNonRolloverToggleBorder() is
            // private in BasicToolBarUI so we can't override it. We still need
            // to call super from this method so that it can save away the
            // original border and then we install ours.
            // Before calling super we get a handle to the old border, because
            // super will install a non-UIResource border that we can't
            // distinguish from one provided by an application.
            JToggleButton b=(JToggleButton)c;
            Border border=b.getBorder();
            super.setBorderToNonRollover(c);
            if(border instanceof UIResource){
                if(nonRolloverBorder==null){
                    nonRolloverBorder=createNonRolloverToggleBorder();
                }
                b.setBorder(nonRolloverBorder);
            }
        }else{
            super.setBorderToNonRollover(c);
        }
    }

    private Border createNonRolloverToggleBorder(){
        return createNonRolloverBorder();
    }

    protected MouseInputListener createDockingListener(){
        return new MetalDockingListener(toolBar);
    }

    protected ContainerListener createContainerListener(){
        return null;
    }

    protected PropertyChangeListener createRolloverListener(){
        return null;
    }

    protected void setDragOffset(Point p){
        if(!GraphicsEnvironment.isHeadless()){
            if(dragWindow==null){
                dragWindow=createDragWindow(toolBar);
            }
            dragWindow.setOffset(p);
        }
    }

    public void update(Graphics g,JComponent c){
        if(g==null){
            throw new NullPointerException("graphics must be non-null");
        }
        if(c.isOpaque()&&(c.getBackground() instanceof UIResource)&&
                ((JToolBar)c).getOrientation()==
                        JToolBar.HORIZONTAL&&UIManager.get(
                "MenuBar.gradient")!=null){
            JRootPane rp=SwingUtilities.getRootPane(c);
            JMenuBar mb=(JMenuBar)findRegisteredComponentOfType(
                    c,JMenuBar.class);
            if(mb!=null&&mb.isOpaque()&&
                    (mb.getBackground() instanceof UIResource)){
                Point point=new Point(0,0);
                point=SwingUtilities.convertPoint(c,point,rp);
                int x=point.x;
                int y=point.y;
                point.x=point.y=0;
                point=SwingUtilities.convertPoint(mb,point,rp);
                if(point.x==x&&y==point.y+mb.getHeight()&&
                        mb.getWidth()==c.getWidth()&&
                        MetalUtils.drawGradient(c,g,"MenuBar.gradient",
                                0,-mb.getHeight(),c.getWidth(),c.getHeight()+
                                        mb.getHeight(),true)){
                    setLastMenuBar(mb);
                    paint(g,c);
                    return;
                }
            }
            if(MetalUtils.drawGradient(c,g,"MenuBar.gradient",
                    0,0,c.getWidth(),c.getHeight(),true)){
                setLastMenuBar(null);
                paint(g,c);
                return;
            }
        }
        setLastMenuBar(null);
        super.update(g,c);
    }

    private void setLastMenuBar(JMenuBar lastMenuBar){
        if(MetalLookAndFeel.usingOcean()){
            if(this.lastMenuBar!=lastMenuBar){
                // The menubar we previously touched has changed, force it
                // to repaint.
                if(this.lastMenuBar!=null){
                    this.lastMenuBar.repaint();
                }
                if(lastMenuBar!=null){
                    lastMenuBar.repaint();
                }
                this.lastMenuBar=lastMenuBar;
            }
        }
    }

    // No longer used. Cannot remove for compatibility reasons
    protected class MetalContainerListener
            extends ToolBarContListener{
    }

    // No longer used. Cannot remove for compatibility reasons
    protected class MetalRolloverListener
            extends PropertyListener{
    }

    protected class MetalDockingListener extends DockingListener{
        private boolean pressedInBumps=false;

        public MetalDockingListener(JToolBar t){
            super(t);
        }

        public void mousePressed(MouseEvent e){
            super.mousePressed(e);
            if(!toolBar.isEnabled()){
                return;
            }
            pressedInBumps=false;
            Rectangle bumpRect=new Rectangle();
            if(toolBar.getOrientation()==JToolBar.HORIZONTAL){
                int x=MetalUtils.isLeftToRight(toolBar)?0:toolBar.getSize().width-14;
                bumpRect.setBounds(x,0,14,toolBar.getSize().height);
            }else{  // vertical
                bumpRect.setBounds(0,0,toolBar.getSize().width,14);
            }
            if(bumpRect.contains(e.getPoint())){
                pressedInBumps=true;
                Point dragOffset=e.getPoint();
                if(!MetalUtils.isLeftToRight(toolBar)){
                    dragOffset.x-=(toolBar.getSize().width
                            -toolBar.getPreferredSize().width);
                }
                setDragOffset(dragOffset);
            }
        }

        public void mouseDragged(MouseEvent e){
            if(pressedInBumps){
                super.mouseDragged(e);
            }
        }
    } // end class MetalDockingListener
}
