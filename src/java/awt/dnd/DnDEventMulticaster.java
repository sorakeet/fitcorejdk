/**
 * Copyright (c) 2001, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.dnd;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.EventListener;

class DnDEventMulticaster extends AWTEventMulticaster
        implements DragSourceListener, DragSourceMotionListener{
    protected DnDEventMulticaster(EventListener a,EventListener b){
        super(a,b);
    }

    public static DragSourceListener add(DragSourceListener a,
                                         DragSourceListener b){
        return (DragSourceListener)addInternal(a,b);
    }

    protected static EventListener addInternal(EventListener a,EventListener b){
        if(a==null) return b;
        if(b==null) return a;
        return new DnDEventMulticaster(a,b);
    }

    public static DragSourceMotionListener add(DragSourceMotionListener a,
                                               DragSourceMotionListener b){
        return (DragSourceMotionListener)addInternal(a,b);
    }

    public static DragSourceListener remove(DragSourceListener l,
                                            DragSourceListener oldl){
        return (DragSourceListener)removeInternal(l,oldl);
    }

    public static DragSourceMotionListener remove(DragSourceMotionListener l,
                                                  DragSourceMotionListener ol){
        return (DragSourceMotionListener)removeInternal(l,ol);
    }

    protected static EventListener removeInternal(EventListener l,EventListener oldl){
        if(l==oldl||l==null){
            return null;
        }else if(l instanceof DnDEventMulticaster){
            return ((DnDEventMulticaster)l).remove(oldl);
        }else{
            return l;           // it's not here
        }
    }

    protected static void save(ObjectOutputStream s,String k,EventListener l)
            throws IOException{
        AWTEventMulticaster.save(s,k,l);
    }

    public void dragEnter(DragSourceDragEvent dsde){
        ((DragSourceListener)a).dragEnter(dsde);
        ((DragSourceListener)b).dragEnter(dsde);
    }

    public void dragOver(DragSourceDragEvent dsde){
        ((DragSourceListener)a).dragOver(dsde);
        ((DragSourceListener)b).dragOver(dsde);
    }

    public void dropActionChanged(DragSourceDragEvent dsde){
        ((DragSourceListener)a).dropActionChanged(dsde);
        ((DragSourceListener)b).dropActionChanged(dsde);
    }

    public void dragExit(DragSourceEvent dse){
        ((DragSourceListener)a).dragExit(dse);
        ((DragSourceListener)b).dragExit(dse);
    }

    public void dragDropEnd(DragSourceDropEvent dsde){
        ((DragSourceListener)a).dragDropEnd(dsde);
        ((DragSourceListener)b).dragDropEnd(dsde);
    }

    public void dragMouseMoved(DragSourceDragEvent dsde){
        ((DragSourceMotionListener)a).dragMouseMoved(dsde);
        ((DragSourceMotionListener)b).dragMouseMoved(dsde);
    }

    protected EventListener remove(EventListener oldl){
        if(oldl==a) return b;
        if(oldl==b) return a;
        EventListener a2=removeInternal(a,oldl);
        EventListener b2=removeInternal(b,oldl);
        if(a2==a&&b2==b){
            return this;        // it's not here
        }
        return addInternal(a2,b2);
    }
}
