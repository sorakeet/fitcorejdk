/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.dnd;

import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

public class DragGestureEvent extends EventObject{
    private static final long serialVersionUID=9080172649166731306L;
    @SuppressWarnings("rawtypes")
    private transient List events;
    private DragSource dragSource;
    private Component component;
    private Point origin;
    private int action;

    public DragGestureEvent(DragGestureRecognizer dgr,int act,Point ori,
                            List<? extends InputEvent> evs){
        super(dgr);
        if((component=dgr.getComponent())==null)
            throw new IllegalArgumentException("null component");
        if((dragSource=dgr.getDragSource())==null)
            throw new IllegalArgumentException("null DragSource");
        if(evs==null||evs.isEmpty())
            throw new IllegalArgumentException("null or empty list of events");
        if(act!=DnDConstants.ACTION_COPY&&
                act!=DnDConstants.ACTION_MOVE&&
                act!=DnDConstants.ACTION_LINK)
            throw new IllegalArgumentException("bad action");
        if(ori==null) throw new IllegalArgumentException("null origin");
        events=evs;
        action=act;
        origin=ori;
    }

    public Component getComponent(){
        return component;
    }

    public DragSource getDragSource(){
        return dragSource;
    }

    public Point getDragOrigin(){
        return origin;
    }

    @SuppressWarnings("unchecked")
    public Iterator<InputEvent> iterator(){
        return events.iterator();
    }

    public Object[] toArray(){
        return events.toArray();
    }

    @SuppressWarnings("unchecked")
    public Object[] toArray(Object[] array){
        return events.toArray(array);
    }

    public int getDragAction(){
        return action;
    }

    public InputEvent getTriggerEvent(){
        return getSourceAsDragGestureRecognizer().getTriggerEvent();
    }

    public DragGestureRecognizer getSourceAsDragGestureRecognizer(){
        return (DragGestureRecognizer)getSource();
    }

    public void startDrag(Cursor dragCursor,Transferable transferable)
            throws InvalidDnDOperationException{
        dragSource.startDrag(this,dragCursor,transferable,null);
    }

    public void startDrag(Cursor dragCursor,Transferable transferable,DragSourceListener dsl) throws InvalidDnDOperationException{
        dragSource.startDrag(this,dragCursor,transferable,dsl);
    }

    public void startDrag(Cursor dragCursor,Image dragImage,Point imageOffset,Transferable transferable,DragSourceListener dsl) throws InvalidDnDOperationException{
        dragSource.startDrag(this,dragCursor,dragImage,imageOffset,transferable,dsl);
    }

    private void writeObject(ObjectOutputStream s) throws IOException{
        s.defaultWriteObject();
        s.writeObject(SerializationTester.test(events)?events:null);
    }

    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException{
        ObjectInputStream.GetField f=s.readFields();
        DragSource newDragSource=(DragSource)f.get("dragSource",null);
        if(newDragSource==null){
            throw new InvalidObjectException("null DragSource");
        }
        dragSource=newDragSource;
        Component newComponent=(Component)f.get("component",null);
        if(newComponent==null){
            throw new InvalidObjectException("null component");
        }
        component=newComponent;
        Point newOrigin=(Point)f.get("origin",null);
        if(newOrigin==null){
            throw new InvalidObjectException("null origin");
        }
        origin=newOrigin;
        int newAction=f.get("action",0);
        if(newAction!=DnDConstants.ACTION_COPY&&
                newAction!=DnDConstants.ACTION_MOVE&&
                newAction!=DnDConstants.ACTION_LINK){
            throw new InvalidObjectException("bad action");
        }
        action=newAction;
        // Pre-1.4 support. 'events' was previously non-transient
        List newEvents;
        try{
            newEvents=(List)f.get("events",null);
        }catch(IllegalArgumentException e){
            // 1.4-compatible byte stream. 'events' was written explicitly
            newEvents=(List)s.readObject();
        }
        // Implementation assumes 'events' is never null.
        if(newEvents!=null&&newEvents.isEmpty()){
            // Constructor treats empty events list as invalid value
            // Throw exception if serialized list is empty
            throw new InvalidObjectException("empty list of events");
        }else if(newEvents==null){
            newEvents=Collections.emptyList();
        }
        events=newEvents;
    }
}
