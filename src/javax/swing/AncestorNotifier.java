/**
 * Copyright (c) 1997, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

@SuppressWarnings("serial")
class AncestorNotifier implements ComponentListener, PropertyChangeListener, Serializable{
    transient Component firstInvisibleAncestor;
    EventListenerList listenerList=new EventListenerList();
    JComponent root;

    AncestorNotifier(JComponent root){
        this.root=root;
        addListeners(root,true);
    }

    void addListeners(Component ancestor,boolean addToFirst){
        Component a;
        firstInvisibleAncestor=null;
        for(a=ancestor;
            firstInvisibleAncestor==null;
            a=a.getParent()){
            if(addToFirst||a!=ancestor){
                a.addComponentListener(this);
                if(a instanceof JComponent){
                    JComponent jAncestor=(JComponent)a;
                    jAncestor.addPropertyChangeListener(this);
                }
            }
            if(!a.isVisible()||a.getParent()==null||a instanceof Window){
                firstInvisibleAncestor=a;
            }
        }
        if(firstInvisibleAncestor instanceof Window&&
                firstInvisibleAncestor.isVisible()){
            firstInvisibleAncestor=null;
        }
    }

    void addAncestorListener(AncestorListener l){
        listenerList.add(AncestorListener.class,l);
    }

    void removeAncestorListener(AncestorListener l){
        listenerList.remove(AncestorListener.class,l);
    }

    AncestorListener[] getAncestorListeners(){
        return listenerList.getListeners(AncestorListener.class);
    }

    void removeAllListeners(){
        removeListeners(root);
    }

    void removeListeners(Component ancestor){
        Component a;
        for(a=ancestor;a!=null;a=a.getParent()){
            a.removeComponentListener(this);
            if(a instanceof JComponent){
                JComponent jAncestor=(JComponent)a;
                jAncestor.removePropertyChangeListener(this);
            }
            if(a==firstInvisibleAncestor||a instanceof Window){
                break;
            }
        }
    }

    public void componentResized(ComponentEvent e){
    }

    public void componentMoved(ComponentEvent e){
        Component source=e.getComponent();
        fireAncestorMoved(root,AncestorEvent.ANCESTOR_MOVED,
                (Container)source,source.getParent());
    }

    protected void fireAncestorMoved(JComponent source,int id,Container ancestor,Container ancestorParent){
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==AncestorListener.class){
                // Lazily create the event:
                AncestorEvent ancestorEvent=
                        new AncestorEvent(source,id,ancestor,ancestorParent);
                ((AncestorListener)listeners[i+1]).ancestorMoved(ancestorEvent);
            }
        }
    }

    public void componentShown(ComponentEvent e){
        Component ancestor=e.getComponent();
        if(ancestor==firstInvisibleAncestor){
            addListeners(ancestor,false);
            if(firstInvisibleAncestor==null){
                fireAncestorAdded(root,AncestorEvent.ANCESTOR_ADDED,
                        (Container)ancestor,ancestor.getParent());
            }
        }
    }

    protected void fireAncestorAdded(JComponent source,int id,Container ancestor,Container ancestorParent){
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==AncestorListener.class){
                // Lazily create the event:
                AncestorEvent ancestorEvent=
                        new AncestorEvent(source,id,ancestor,ancestorParent);
                ((AncestorListener)listeners[i+1]).ancestorAdded(ancestorEvent);
            }
        }
    }

    public void componentHidden(ComponentEvent e){
        Component ancestor=e.getComponent();
        boolean needsNotify=firstInvisibleAncestor==null;
        if(!(ancestor instanceof Window)){
            removeListeners(ancestor.getParent());
        }
        firstInvisibleAncestor=ancestor;
        if(needsNotify){
            fireAncestorRemoved(root,AncestorEvent.ANCESTOR_REMOVED,
                    (Container)ancestor,ancestor.getParent());
        }
    }

    protected void fireAncestorRemoved(JComponent source,int id,Container ancestor,Container ancestorParent){
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==AncestorListener.class){
                // Lazily create the event:
                AncestorEvent ancestorEvent=
                        new AncestorEvent(source,id,ancestor,ancestorParent);
                ((AncestorListener)listeners[i+1]).ancestorRemoved(ancestorEvent);
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt){
        String s=evt.getPropertyName();
        if(s!=null&&(s.equals("parent")||s.equals("ancestor"))){
            JComponent component=(JComponent)evt.getSource();
            if(evt.getNewValue()!=null){
                if(component==firstInvisibleAncestor){
                    addListeners(component,false);
                    if(firstInvisibleAncestor==null){
                        fireAncestorAdded(root,AncestorEvent.ANCESTOR_ADDED,
                                component,component.getParent());
                    }
                }
            }else{
                boolean needsNotify=firstInvisibleAncestor==null;
                Container oldParent=(Container)evt.getOldValue();
                removeListeners(oldParent);
                firstInvisibleAncestor=component;
                if(needsNotify){
                    fireAncestorRemoved(root,AncestorEvent.ANCESTOR_REMOVED,
                            component,oldParent);
                }
            }
        }
    }
}
