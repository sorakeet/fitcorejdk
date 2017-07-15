/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.dnd;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.FlavorMap;
import java.awt.datatransfer.SystemFlavorMap;
import java.awt.dnd.peer.DropTargetPeer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.peer.ComponentPeer;
import java.awt.peer.LightweightPeer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.TooManyListenersException;

public class DropTarget implements DropTargetListener, Serializable{
    private static final long serialVersionUID=-6283860791671019047L;
    int actions=DnDConstants.ACTION_COPY_OR_MOVE;
    boolean active=true;
    private DropTargetContext dropTargetContext=createDropTargetContext();
    private Component component;
    private transient ComponentPeer componentPeer;
    private transient ComponentPeer nativePeer;
    private transient DropTargetAutoScroller autoScroller;
    private transient DropTargetListener dtListener;
    private transient FlavorMap flavorMap;
    private transient boolean isDraggingInside;

    public DropTarget() throws HeadlessException{
        this(null,DnDConstants.ACTION_COPY_OR_MOVE,null,true,null);
    }

    public DropTarget(Component c,int ops,DropTargetListener dtl,
                      boolean act,FlavorMap fm)
            throws HeadlessException{
        if(GraphicsEnvironment.isHeadless()){
            throw new HeadlessException();
        }
        component=c;
        setDefaultActions(ops);
        if(dtl!=null) try{
            addDropTargetListener(dtl);
        }catch(TooManyListenersException tmle){
            // do nothing!
        }
        if(c!=null){
            c.setDropTarget(this);
            setActive(act);
        }
        if(fm!=null){
            flavorMap=fm;
        }else{
            flavorMap=SystemFlavorMap.getDefaultFlavorMap();
        }
    }

    public synchronized void addDropTargetListener(DropTargetListener dtl) throws TooManyListenersException{
        if(dtl==null) return;
        if(equals(dtl)) throw new IllegalArgumentException("DropTarget may not be its own Listener");
        if(dtListener==null)
            dtListener=dtl;
        else
            throw new TooManyListenersException();
    }

    public DropTarget(Component c,DropTargetListener dtl)
            throws HeadlessException{
        this(c,DnDConstants.ACTION_COPY_OR_MOVE,dtl,true,null);
    }

    public DropTarget(Component c,int ops,DropTargetListener dtl)
            throws HeadlessException{
        this(c,ops,dtl,true);
    }

    public DropTarget(Component c,int ops,DropTargetListener dtl,
                      boolean act)
            throws HeadlessException{
        this(c,ops,dtl,act,null);
    }

    public synchronized Component getComponent(){
        return component;
    }

    public synchronized void setComponent(Component c){
        if(component==c||component!=null&&component.equals(c))
            return;
        Component old;
        ComponentPeer oldPeer=null;
        if((old=component)!=null){
            clearAutoscroll();
            component=null;
            if(componentPeer!=null){
                oldPeer=componentPeer;
                removeNotify(componentPeer);
            }
            old.setDropTarget(null);
        }
        if((component=c)!=null) try{
            c.setDropTarget(this);
        }catch(Exception e){ // undo the change
            if(old!=null){
                old.setDropTarget(this);
                addNotify(oldPeer);
            }
        }
    }

    public void addNotify(ComponentPeer peer){
        if(peer==componentPeer) return;
        componentPeer=peer;
        for(Component c=component;
            c!=null&&peer instanceof LightweightPeer;c=c.getParent()){
            peer=c.getPeer();
        }
        if(peer instanceof DropTargetPeer){
            nativePeer=peer;
            ((DropTargetPeer)peer).addDropTarget(this);
        }else{
            nativePeer=null;
        }
    }

    public void removeNotify(ComponentPeer peer){
        if(nativePeer!=null)
            ((DropTargetPeer)nativePeer).removeDropTarget(this);
        componentPeer=nativePeer=null;
        synchronized(this){
            if(isDraggingInside){
                dragExit(new DropTargetEvent(getDropTargetContext()));
            }
        }
    }

    void doSetDefaultActions(int ops){
        actions=ops;
    }

    public int getDefaultActions(){
        return actions;
    }

    public void setDefaultActions(int ops){
        getDropTargetContext().setTargetActions(ops&(DnDConstants.ACTION_COPY_OR_MOVE|DnDConstants.ACTION_REFERENCE));
    }

    public DropTargetContext getDropTargetContext(){
        return dropTargetContext;
    }

    public boolean isActive(){
        return active;
    }

    public synchronized void setActive(boolean isActive){
        if(isActive!=active){
            active=isActive;
        }
        if(!active) clearAutoscroll();
    }

    protected void clearAutoscroll(){
        if(autoScroller!=null){
            autoScroller.stop();
            autoScroller=null;
        }
    }

    public synchronized void removeDropTargetListener(DropTargetListener dtl){
        if(dtl!=null&&dtListener!=null){
            if(dtListener.equals(dtl))
                dtListener=null;
            else
                throw new IllegalArgumentException("listener mismatch");
        }
    }

    public synchronized void dragEnter(DropTargetDragEvent dtde){
        isDraggingInside=true;
        if(!active) return;
        if(dtListener!=null){
            dtListener.dragEnter(dtde);
        }else
            dtde.getDropTargetContext().setTargetActions(DnDConstants.ACTION_NONE);
        initializeAutoscrolling(dtde.getLocation());
    }

    public synchronized void dragOver(DropTargetDragEvent dtde){
        if(!active) return;
        if(dtListener!=null&&active) dtListener.dragOver(dtde);
        updateAutoscroll(dtde.getLocation());
    }

    public synchronized void dropActionChanged(DropTargetDragEvent dtde){
        if(!active) return;
        if(dtListener!=null) dtListener.dropActionChanged(dtde);
        updateAutoscroll(dtde.getLocation());
    }

    public synchronized void dragExit(DropTargetEvent dte){
        isDraggingInside=false;
        if(!active) return;
        if(dtListener!=null&&active) dtListener.dragExit(dte);
        clearAutoscroll();
    }

    public synchronized void drop(DropTargetDropEvent dtde){
        isDraggingInside=false;
        clearAutoscroll();
        if(dtListener!=null&&active)
            dtListener.drop(dtde);
        else{ // we should'nt get here ...
            dtde.rejectDrop();
        }
    }

    protected void updateAutoscroll(Point dragCursorLocn){
        if(autoScroller!=null) autoScroller.updateLocation(dragCursorLocn);
    }

    protected void initializeAutoscrolling(Point p){
        if(component==null||!(component instanceof Autoscroll)) return;
        autoScroller=createDropTargetAutoScroller(component,p);
    }

    protected DropTargetAutoScroller createDropTargetAutoScroller(Component c,Point p){
        return new DropTargetAutoScroller(c,p);
    }

    public FlavorMap getFlavorMap(){
        return flavorMap;
    }

    public void setFlavorMap(FlavorMap fm){
        flavorMap=fm==null?SystemFlavorMap.getDefaultFlavorMap():fm;
    }

    private void writeObject(ObjectOutputStream s) throws IOException{
        s.defaultWriteObject();
        s.writeObject(SerializationTester.test(dtListener)
                ?dtListener:null);
    }

    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException{
        ObjectInputStream.GetField f=s.readFields();
        try{
            dropTargetContext=
                    (DropTargetContext)f.get("dropTargetContext",null);
        }catch(IllegalArgumentException e){
            // Pre-1.4 support. 'dropTargetContext' was previously transient
        }
        if(dropTargetContext==null){
            dropTargetContext=createDropTargetContext();
        }
        component=(Component)f.get("component",null);
        actions=f.get("actions",DnDConstants.ACTION_COPY_OR_MOVE);
        active=f.get("active",true);
        // Pre-1.4 support. 'dtListener' was previously non-transient
        try{
            dtListener=(DropTargetListener)f.get("dtListener",null);
        }catch(IllegalArgumentException e){
            // 1.4-compatible byte stream. 'dtListener' was written explicitly
            dtListener=(DropTargetListener)s.readObject();
        }
    }

    protected DropTargetContext createDropTargetContext(){
        return new DropTargetContext(this);
    }

    protected static class DropTargetAutoScroller implements ActionListener{
        private Component component;
        private Autoscroll autoScroll;
        private Timer timer;
        private Point locn;
        private Point prev;
        private Rectangle outer=new Rectangle();
        private Rectangle inner=new Rectangle();
        private int hysteresis=10;

        protected DropTargetAutoScroller(Component c,Point p){
            super();
            component=c;
            autoScroll=(Autoscroll)component;
            Toolkit t=Toolkit.getDefaultToolkit();
            Integer initial=Integer.valueOf(100);
            Integer interval=Integer.valueOf(100);
            try{
                initial=(Integer)t.getDesktopProperty("DnD.Autoscroll.initialDelay");
            }catch(Exception e){
                // ignore
            }
            try{
                interval=(Integer)t.getDesktopProperty("DnD.Autoscroll.interval");
            }catch(Exception e){
                // ignore
            }
            timer=new Timer(interval.intValue(),this);
            timer.setCoalesce(true);
            timer.setInitialDelay(initial.intValue());
            locn=p;
            prev=p;
            try{
                hysteresis=((Integer)t.getDesktopProperty("DnD.Autoscroll.cursorHysteresis")).intValue();
            }catch(Exception e){
                // ignore
            }
            timer.start();
        }

        protected synchronized void updateLocation(Point newLocn){
            prev=locn;
            locn=newLocn;
            if(Math.abs(locn.x-prev.x)>hysteresis||
                    Math.abs(locn.y-prev.y)>hysteresis){
                if(timer.isRunning()) timer.stop();
            }else{
                if(!timer.isRunning()) timer.start();
            }
        }

        protected void stop(){
            timer.stop();
        }

        public synchronized void actionPerformed(ActionEvent e){
            updateRegion();
            if(outer.contains(locn)&&!inner.contains(locn))
                autoScroll.autoscroll(locn);
        }

        private void updateRegion(){
            Insets i=autoScroll.getAutoscrollInsets();
            Dimension size=component.getSize();
            if(size.width!=outer.width||size.height!=outer.height)
                outer.reshape(0,0,size.width,size.height);
            if(inner.x!=i.left||inner.y!=i.top)
                inner.setLocation(i.left,i.top);
            int newWidth=size.width-(i.left+i.right);
            int newHeight=size.height-(i.top+i.bottom);
            if(newWidth!=inner.width||newHeight!=inner.height)
                inner.setSize(newWidth,newHeight);
        }
    }
}
