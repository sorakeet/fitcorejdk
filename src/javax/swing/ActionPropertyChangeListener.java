/**
 * Copyright (c) 1999, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

abstract class ActionPropertyChangeListener<T extends JComponent>
        implements PropertyChangeListener, Serializable{
    private static ReferenceQueue<JComponent> queue;
    // WeakReference's aren't serializable.
    private transient OwnedWeakReference<T> target;
    // The Component's that reference an Action do so through a strong
    // reference, so that there is no need to check for serialized.
    private Action action;

    public ActionPropertyChangeListener(T c,Action a){
        super();
        setTarget(c);
        this.action=a;
    }

    public final void propertyChange(PropertyChangeEvent e){
        T target=getTarget();
        if(target==null){
            getAction().removePropertyChangeListener(this);
        }else{
            actionPropertyChanged(target,getAction(),e);
        }
    }

    protected abstract void actionPropertyChanged(T target,Action action,
                                                  PropertyChangeEvent e);

    public T getTarget(){
        if(target==null){
            // Will only happen if serialized and real target was null
            return null;
        }
        return this.target.get();
    }

    private void setTarget(T c){
        ReferenceQueue<JComponent> queue=getQueue();
        // Check to see whether any old buttons have
        // been enqueued for GC.  If so, look up their
        // PCL instance and remove it from its Action.
        OwnedWeakReference<?> r;
        while((r=(OwnedWeakReference)queue.poll())!=null){
            ActionPropertyChangeListener<?> oldPCL=r.getOwner();
            Action oldAction=oldPCL.getAction();
            if(oldAction!=null){
                oldAction.removePropertyChangeListener(oldPCL);
            }
        }
        this.target=new OwnedWeakReference<T>(c,queue,this);
    }

    private static ReferenceQueue<JComponent> getQueue(){
        synchronized(ActionPropertyChangeListener.class){
            if(queue==null){
                queue=new ReferenceQueue<JComponent>();
            }
        }
        return queue;
    }

    public Action getAction(){
        return action;
    }

    private void writeObject(ObjectOutputStream s) throws IOException{
        s.defaultWriteObject();
        s.writeObject(getTarget());
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException{
        s.defaultReadObject();
        T target=(T)s.readObject();
        if(target!=null){
            setTarget(target);
        }
    }

    private static class OwnedWeakReference<U extends JComponent> extends
            WeakReference<U>{
        private ActionPropertyChangeListener<?> owner;

        OwnedWeakReference(U target,ReferenceQueue<? super U> queue,
                           ActionPropertyChangeListener<?> owner){
            super(target,queue);
            this.owner=owner;
        }

        public ActionPropertyChangeListener<?> getOwner(){
            return owner;
        }
    }
}
