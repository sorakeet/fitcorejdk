/**
 * Copyright (c) 2009, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import sun.awt.AWTAccessor;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.border.Border;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;

public final class JLayer<V extends Component>
        extends JComponent
        implements Scrollable, PropertyChangeListener, Accessible{
    private static final LayerEventController eventController=
            new LayerEventController();
    private V view;
    // this field is necessary because JComponent.ui is transient
    // when layerUI is serializable
    private LayerUI<? super V> layerUI;
    private JPanel glassPane;
    private long eventMask;
    private transient boolean isPainting;
    private transient boolean isPaintingImmediately;

    public JLayer(){
        this(null);
    }

    public JLayer(V view){
        this(view,new LayerUI<V>());
    }

    public JLayer(V view,LayerUI<V> ui){
        setGlassPane(createGlassPane());
        setView(view);
        setUI(ui);
    }

    public JPanel createGlassPane(){
        return new DefaultLayerGlassPane();
    }    public V getView(){
        return view;
    }

    protected void addImpl(Component comp,Object constraints,int index){
        throw new UnsupportedOperationException(
                "Adding components to JLayer is not supported, "+
                        "use setView() or setGlassPane() instead");
    }    public void setView(V view){
        Component oldView=getView();
        if(oldView!=null){
            super.remove(oldView);
        }
        if(view!=null){
            super.addImpl(view,null,getComponentCount());
        }
        this.view=view;
        firePropertyChange("view",oldView,view);
        revalidate();
        repaint();
    }

    public void remove(Component comp){
        if(comp==null){
            super.remove(comp);
        }else if(comp==getView()){
            setView(null);
        }else if(comp==getGlassPane()){
            setGlassPane(null);
        }else{
            super.remove(comp);
        }
    }    public void setUI(LayerUI<? super V> ui){
        this.layerUI=ui;
        super.setUI(ui);
    }

    public void removeAll(){
        if(view!=null){
            setView(null);
        }
        if(glassPane!=null){
            setGlassPane(null);
        }
    }    public LayerUI<? super V> getUI(){
        return layerUI;
    }

    public void setLayout(LayoutManager mgr){
        if(mgr!=null){
            throw new IllegalArgumentException("JLayer.setLayout() not supported");
        }
    }    public JPanel getGlassPane(){
        return glassPane;
    }

    public void doLayout(){
        if(getUI()!=null){
            getUI().doLayout(this);
        }
    }    public void setGlassPane(JPanel glassPane){
        Component oldGlassPane=getGlassPane();
        boolean isGlassPaneVisible=false;
        if(oldGlassPane!=null){
            isGlassPaneVisible=oldGlassPane.isVisible();
            super.remove(oldGlassPane);
        }
        if(glassPane!=null){
            AWTAccessor.getComponentAccessor().setMixingCutoutShape(glassPane,
                    new Rectangle());
            glassPane.setVisible(isGlassPaneVisible);
            super.addImpl(glassPane,null,0);
        }
        this.glassPane=glassPane;
        firePropertyChange("glassPane",oldGlassPane,glassPane);
        revalidate();
        repaint();
    }

    public void propertyChange(PropertyChangeEvent evt){
        if(getUI()!=null){
            getUI().applyPropertyChange(evt,this);
        }
    }

    public void updateUI(){
        if(getUI()!=null){
            getUI().updateUI(this);
        }
    }

    protected void paintComponent(Graphics g){
    }    public void setBorder(Border border){
        if(border!=null){
            throw new IllegalArgumentException("JLayer.setBorder() not supported");
        }
    }

    public void paint(Graphics g){
        if(!isPainting){
            isPainting=true;
            try{
                super.paintComponent(g);
            }finally{
                isPainting=false;
            }
        }else{
            super.paint(g);
        }
    }

    public Dimension getPreferredScrollableViewportSize(){
        if(getView() instanceof Scrollable){
            return ((Scrollable)getView()).getPreferredScrollableViewportSize();
        }
        return getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,int orientation,
                                          int direction){
        if(getView() instanceof Scrollable){
            return ((Scrollable)getView()).getScrollableUnitIncrement(
                    visibleRect,orientation,direction);
        }
        return 1;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,
                                           int orientation,int direction){
        if(getView() instanceof Scrollable){
            return ((Scrollable)getView()).getScrollableBlockIncrement(visibleRect,
                    orientation,direction);
        }
        return (orientation==SwingConstants.VERTICAL)?visibleRect.height:
                visibleRect.width;
    }    protected boolean isPaintingOrigin(){
        return true;
    }

    public boolean getScrollableTracksViewportWidth(){
        if(getView() instanceof Scrollable){
            return ((Scrollable)getView()).getScrollableTracksViewportWidth();
        }
        return false;
    }    public void paintImmediately(int x,int y,int w,int h){
        if(!isPaintingImmediately&&getUI()!=null){
            isPaintingImmediately=true;
            try{
                getUI().paintImmediately(x,y,w,h,this);
            }finally{
                isPaintingImmediately=false;
            }
        }else{
            super.paintImmediately(x,y,w,h);
        }
    }

    public boolean getScrollableTracksViewportHeight(){
        if(getView() instanceof Scrollable){
            return ((Scrollable)getView()).getScrollableTracksViewportHeight();
        }
        return false;
    }

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException{
        s.defaultReadObject();
        if(layerUI!=null){
            setUI(layerUI);
        }
        if(eventMask!=0){
            eventController.updateAWTEventListener(0,eventMask);
        }
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJComponent(){
                public AccessibleRole getAccessibleRole(){
                    return AccessibleRole.PANEL;
                }
            };
        }
        return accessibleContext;
    }    public boolean isOptimizedDrawingEnabled(){
        return false;
    }

    private static class LayerEventController implements AWTEventListener{
        private static final long ACCEPTED_EVENTS=
                AWTEvent.COMPONENT_EVENT_MASK|
                        AWTEvent.CONTAINER_EVENT_MASK|
                        AWTEvent.FOCUS_EVENT_MASK|
                        AWTEvent.KEY_EVENT_MASK|
                        AWTEvent.MOUSE_WHEEL_EVENT_MASK|
                        AWTEvent.MOUSE_MOTION_EVENT_MASK|
                        AWTEvent.MOUSE_EVENT_MASK|
                        AWTEvent.INPUT_METHOD_EVENT_MASK|
                        AWTEvent.HIERARCHY_EVENT_MASK|
                        AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK;
        private ArrayList<Long> layerMaskList=
                new ArrayList<Long>();
        private long currentEventMask;

        @SuppressWarnings("unchecked")
        public void eventDispatched(AWTEvent event){
            Object source=event.getSource();
            if(source instanceof Component){
                Component component=(Component)source;
                while(component!=null){
                    if(component instanceof JLayer){
                        JLayer l=(JLayer)component;
                        LayerUI ui=l.getUI();
                        if(ui!=null&&
                                isEventEnabled(l.getLayerEventMask(),event.getID())&&
                                (!(event instanceof InputEvent)||!((InputEvent)event).isConsumed())){
                            ui.eventDispatched(event,l);
                        }
                    }
                    component=component.getParent();
                }
            }
        }

        private boolean isEventEnabled(long eventMask,int id){
            return (((eventMask&AWTEvent.COMPONENT_EVENT_MASK)!=0&&
                    id>=ComponentEvent.COMPONENT_FIRST&&
                    id<=ComponentEvent.COMPONENT_LAST)
                    ||((eventMask&AWTEvent.CONTAINER_EVENT_MASK)!=0&&
                    id>=ContainerEvent.CONTAINER_FIRST&&
                    id<=ContainerEvent.CONTAINER_LAST)
                    ||((eventMask&AWTEvent.FOCUS_EVENT_MASK)!=0&&
                    id>=FocusEvent.FOCUS_FIRST&&
                    id<=FocusEvent.FOCUS_LAST)
                    ||((eventMask&AWTEvent.KEY_EVENT_MASK)!=0&&
                    id>=KeyEvent.KEY_FIRST&&
                    id<=KeyEvent.KEY_LAST)
                    ||((eventMask&AWTEvent.MOUSE_WHEEL_EVENT_MASK)!=0&&
                    id==MouseEvent.MOUSE_WHEEL)
                    ||((eventMask&AWTEvent.MOUSE_MOTION_EVENT_MASK)!=0&&
                    (id==MouseEvent.MOUSE_MOVED||
                            id==MouseEvent.MOUSE_DRAGGED))
                    ||((eventMask&AWTEvent.MOUSE_EVENT_MASK)!=0&&
                    id!=MouseEvent.MOUSE_MOVED&&
                    id!=MouseEvent.MOUSE_DRAGGED&&
                    id!=MouseEvent.MOUSE_WHEEL&&
                    id>=MouseEvent.MOUSE_FIRST&&
                    id<=MouseEvent.MOUSE_LAST)
                    ||((eventMask&AWTEvent.INPUT_METHOD_EVENT_MASK)!=0&&
                    id>=InputMethodEvent.INPUT_METHOD_FIRST&&
                    id<=InputMethodEvent.INPUT_METHOD_LAST)
                    ||((eventMask&AWTEvent.HIERARCHY_EVENT_MASK)!=0&&
                    id==HierarchyEvent.HIERARCHY_CHANGED)
                    ||((eventMask&AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK)!=0&&
                    (id==HierarchyEvent.ANCESTOR_MOVED||
                            id==HierarchyEvent.ANCESTOR_RESIZED)));
        }

        private void updateAWTEventListener(long oldEventMask,long newEventMask){
            if(oldEventMask!=0){
                layerMaskList.remove(oldEventMask);
            }
            if(newEventMask!=0){
                layerMaskList.add(newEventMask);
            }
            long combinedMask=0;
            for(Long mask : layerMaskList){
                combinedMask|=mask;
            }
            // filter out all unaccepted events
            combinedMask&=ACCEPTED_EVENTS;
            if(combinedMask==0){
                removeAWTEventListener();
            }else if(getCurrentEventMask()!=combinedMask){
                removeAWTEventListener();
                addAWTEventListener(combinedMask);
            }
            currentEventMask=combinedMask;
        }

        private long getCurrentEventMask(){
            return currentEventMask;
        }

        private void addAWTEventListener(final long eventMask){
            AccessController.doPrivileged(new PrivilegedAction<Void>(){
                public Void run(){
                    Toolkit.getDefaultToolkit().
                            addAWTEventListener(LayerEventController.this,eventMask);
                    return null;
                }
            });
        }

        private void removeAWTEventListener(){
            AccessController.doPrivileged(new PrivilegedAction<Void>(){
                public Void run(){
                    Toolkit.getDefaultToolkit().
                            removeAWTEventListener(LayerEventController.this);
                    return null;
                }
            });
        }
    }

    private static class DefaultLayerGlassPane extends JPanel{
        public DefaultLayerGlassPane(){
            setOpaque(false);
        }

        public boolean contains(int x,int y){
            for(int i=0;i<getComponentCount();i++){
                Component c=getComponent(i);
                Point point=SwingUtilities.convertPoint(this,new Point(x,y),c);
                if(c.isVisible()&&c.contains(point)){
                    return true;
                }
            }
            if(getMouseListeners().length==0
                    &&getMouseMotionListeners().length==0
                    &&getMouseWheelListeners().length==0
                    &&!isCursorSet()){
                return false;
            }
            return super.contains(x,y);
        }
    }    public void setLayerEventMask(long layerEventMask){
        long oldEventMask=getLayerEventMask();
        this.eventMask=layerEventMask;
        firePropertyChange("layerEventMask",oldEventMask,layerEventMask);
        if(layerEventMask!=oldEventMask){
            disableEvents(oldEventMask);
            enableEvents(eventMask);
            if(isDisplayable()){
                eventController.updateAWTEventListener(
                        oldEventMask,layerEventMask);
            }
        }
    }

    public long getLayerEventMask(){
        return eventMask;
    }















    public void addNotify(){
        super.addNotify();
        eventController.updateAWTEventListener(0,eventMask);
    }

    public void removeNotify(){
        super.removeNotify();
        eventController.updateAWTEventListener(eventMask,0);
    }








}
