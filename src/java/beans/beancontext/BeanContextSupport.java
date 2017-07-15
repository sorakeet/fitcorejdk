/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans.beancontext;

import java.awt.*;
import java.beans.*;
import java.io.*;
import java.net.URL;
import java.util.*;

public class BeanContextSupport extends BeanContextChildSupport
        implements BeanContext,
        Serializable,
        PropertyChangeListener,
        VetoableChangeListener{
    // Fix for bug 4282900 to pass JCK regression test
    static final long serialVersionUID=-4879613978649577204L;
    protected transient HashMap children;
    protected transient ArrayList bcmListeners;
    //
    protected Locale locale;
    protected boolean okToUseGui;
    protected boolean designTime;
    private int serializable=0; // children serializable
    private transient PropertyChangeListener childPCL;
    private transient VetoableChangeListener childVCL;
    private transient boolean serializing;

    public BeanContextSupport(BeanContext peer,Locale lcle,boolean dtime){
        this(peer,lcle,dtime,true);
    }

    public BeanContextSupport(BeanContext peer,Locale lcle,boolean dTime,boolean visible){
        super(peer);
        locale=lcle!=null?lcle:Locale.getDefault();
        designTime=dTime;
        okToUseGui=visible;
        initialize();
    }

    protected synchronized void initialize(){
        children=new HashMap(serializable+1);
        bcmListeners=new ArrayList(1);
        childPCL=new PropertyChangeListener(){
            /**
             * this adaptor is used by the BeanContextSupport class to forward
             * property changes from a child to the BeanContext, avoiding
             * accidential serialization of the BeanContext by a badly
             * behaved Serializable child.
             */
            public void propertyChange(PropertyChangeEvent pce){
                BeanContextSupport.this.propertyChange(pce);
            }
        };
        childVCL=new VetoableChangeListener(){
            /**
             * this adaptor is used by the BeanContextSupport class to forward
             * vetoable changes from a child to the BeanContext, avoiding
             * accidential serialization of the BeanContext by a badly
             * behaved Serializable child.
             */
            public void vetoableChange(PropertyChangeEvent pce) throws PropertyVetoException{
                BeanContextSupport.this.vetoableChange(pce);
            }
        };
    }

    public BeanContextSupport(BeanContext peer,Locale lcle){
        this(peer,lcle,false,true);
    }    public void vetoableChange(PropertyChangeEvent pce) throws PropertyVetoException{
        String propertyName=pce.getPropertyName();
        Object source=pce.getSource();
        synchronized(children){
            if("beanContext".equals(propertyName)&&
                    containsKey(source)&&
                    !getBeanContextPeer().equals(pce.getNewValue())
                    ){
                if(!validatePendingRemove(source)){
                    throw new PropertyVetoException("current BeanContext vetoes setBeanContext()",pce);
                }else ((BCSChild)children.get(source)).setRemovePending(true);
            }
        }
    }

    public BeanContextSupport(BeanContext peer){
        this(peer,null,false,true);
    }    public void propertyChange(PropertyChangeEvent pce){
        String propertyName=pce.getPropertyName();
        Object source=pce.getSource();
        synchronized(children){
            if("beanContext".equals(propertyName)&&
                    containsKey(source)&&
                    ((BCSChild)children.get(source)).isRemovePending()){
                BeanContext bc=getBeanContextPeer();
                if(bc.equals(pce.getOldValue())&&!bc.equals(pce.getNewValue())){
                    remove(source,false);
                }else{
                    ((BCSChild)children.get(source)).setRemovePending(false);
                }
            }
        }
    }

    public BeanContextSupport(){
        this(null,null,false,true);
    }    public BeanContext getBeanContextPeer(){
        return (BeanContext)getBeanContextChildPeer();
    }

    protected static final PropertyChangeListener getChildPropertyChangeListener(Object child){
        try{
            return (PropertyChangeListener)child;
        }catch(ClassCastException cce){
            return null;
        }
    }    public boolean containsKey(Object o){
        synchronized(children){
            return children.containsKey(o);
        }
    }

    protected static final VetoableChangeListener getChildVetoableChangeListener(Object child){
        try{
            return (VetoableChangeListener)child;
        }catch(ClassCastException cce){
            return null;
        }
    }    protected boolean remove(Object targetChild,boolean callChildSetBC){
        if(targetChild==null) throw new IllegalArgumentException();
        synchronized(BeanContext.globalHierarchyLock){
            if(!containsKey(targetChild)) return false;
            if(!validatePendingRemove(targetChild)){
                throw new IllegalStateException();
            }
            BCSChild bcsc=(BCSChild)children.get(targetChild);
            BCSChild pbcsc=null;
            Object peer=null;
            // we are required to notify the child that it is no longer nested here if
            // it implements java.beans.beancontext.BeanContextChild
            synchronized(targetChild){
                if(callChildSetBC){
                    BeanContextChild cbcc=getChildBeanContextChild(targetChild);
                    if(cbcc!=null) synchronized(cbcc){
                        cbcc.removePropertyChangeListener("beanContext",childPCL);
                        cbcc.removeVetoableChangeListener("beanContext",childVCL);
                        try{
                            cbcc.setBeanContext(null);
                        }catch(PropertyVetoException pve1){
                            cbcc.addPropertyChangeListener("beanContext",childPCL);
                            cbcc.addVetoableChangeListener("beanContext",childVCL);
                            throw new IllegalStateException();
                        }
                    }
                }
                synchronized(children){
                    children.remove(targetChild);
                    if(bcsc.isProxyPeer()){
                        pbcsc=(BCSChild)children.get(peer=bcsc.getProxyPeer());
                        children.remove(peer);
                    }
                }
                if(getChildSerializable(targetChild)!=null) serializable--;
                childJustRemovedHook(targetChild,bcsc);
                if(peer!=null){
                    if(getChildSerializable(peer)!=null) serializable--;
                    childJustRemovedHook(peer,pbcsc);
                }
            }
            fireChildrenRemoved(new BeanContextMembershipEvent(getBeanContextPeer(),peer==null?new Object[]{targetChild}:new Object[]{targetChild,peer}));
        }
        return true;
    }

    protected static final BeanContextMembershipListener getChildBeanContextMembershipListener(Object child){
        try{
            return (BeanContextMembershipListener)child;
        }catch(ClassCastException cce){
            return null;
        }
    }    protected boolean validatePendingRemove(Object targetChild){
        return true;
    }

    protected static final boolean classEquals(Class first,Class second){
        return first.equals(second)||first.getName().equals(second.getName());
    }    protected void childJustRemovedHook(Object child,BCSChild bcsc){
    }

    public Object instantiateChild(String beanName)
            throws IOException, ClassNotFoundException{
        BeanContext bc=getBeanContextPeer();
        return Beans.instantiate(bc.getClass().getClassLoader(),beanName,bc);
    }    protected static final Serializable getChildSerializable(Object child){
        try{
            return (Serializable)child;
        }catch(ClassCastException cce){
            return null;
        }
    }

    public InputStream getResourceAsStream(String name,BeanContextChild bcc){
        if(name==null) throw new NullPointerException("name");
        if(bcc==null) throw new NullPointerException("bcc");
        if(containsKey(bcc)){
            ClassLoader cl=bcc.getClass().getClassLoader();
            return cl!=null?cl.getResourceAsStream(name)
                    :ClassLoader.getSystemResourceAsStream(name);
        }else throw new IllegalArgumentException("Not a valid child");
    }    protected static final BeanContextChild getChildBeanContextChild(Object child){
        try{
            BeanContextChild bcc=(BeanContextChild)child;
            if(child instanceof BeanContextChild&&child instanceof BeanContextProxy)
                throw new IllegalArgumentException("child cannot implement both BeanContextChild and BeanContextProxy");
            else
                return bcc;
        }catch(ClassCastException cce){
            try{
                return ((BeanContextProxy)child).getBeanContextProxy();
            }catch(ClassCastException cce1){
                return null;
            }
        }
    }

    public URL getResource(String name,BeanContextChild bcc){
        if(name==null) throw new NullPointerException("name");
        if(bcc==null) throw new NullPointerException("bcc");
        if(containsKey(bcc)){
            ClassLoader cl=bcc.getClass().getClassLoader();
            return cl!=null?cl.getResource(name)
                    :ClassLoader.getSystemResource(name);
        }else throw new IllegalArgumentException("Not a valid child");
    }    protected final void fireChildrenRemoved(BeanContextMembershipEvent bcme){
        Object[] copy;
        synchronized(bcmListeners){
            copy=bcmListeners.toArray();
        }
        for(int i=0;i<copy.length;i++)
            ((BeanContextMembershipListener)copy[i]).childrenRemoved(bcme);
    }

    public void addBeanContextMembershipListener(BeanContextMembershipListener bcml){
        if(bcml==null) throw new NullPointerException("listener");
        synchronized(bcmListeners){
            if(bcmListeners.contains(bcml))
                return;
            else
                bcmListeners.add(bcml);
        }
    }

    public void removeBeanContextMembershipListener(BeanContextMembershipListener bcml){
        if(bcml==null) throw new NullPointerException("listener");
        synchronized(bcmListeners){
            if(!bcmListeners.contains(bcml))
                return;
            else
                bcmListeners.remove(bcml);
        }
    }

    public int size(){
        synchronized(children){
            return children.size();
        }
    }

    public boolean isEmpty(){
        synchronized(children){
            return children.isEmpty();
        }
    }

    public boolean contains(Object o){
        synchronized(children){
            return children.containsKey(o);
        }
    }

    public Iterator iterator(){
        synchronized(children){
            return new BCSIterator(children.keySet().iterator());
        }
    }

    public Object[] toArray(){
        synchronized(children){
            return children.keySet().toArray();
        }
    }

    public Object[] toArray(Object[] arry){
        synchronized(children){
            return children.keySet().toArray(arry);
        }
    }

    public boolean add(Object targetChild){
        if(targetChild==null) throw new IllegalArgumentException();
        // The specification requires that we do nothing if the child
        // is already nested herein.
        if(children.containsKey(targetChild)) return false; // test before locking
        synchronized(BeanContext.globalHierarchyLock){
            if(children.containsKey(targetChild)) return false; // check again
            if(!validatePendingAdd(targetChild)){
                throw new IllegalStateException();
            }
            // The specification requires that we invoke setBeanContext() on the
            // newly added child if it implements the java.beans.beancontext.BeanContextChild interface
            BeanContextChild cbcc=getChildBeanContextChild(targetChild);
            BeanContextChild bccp=null;
            synchronized(targetChild){
                if(targetChild instanceof BeanContextProxy){
                    bccp=((BeanContextProxy)targetChild).getBeanContextProxy();
                    if(bccp==null) throw new NullPointerException("BeanContextPeer.getBeanContextProxy()");
                }
                BCSChild bcsc=createBCSChild(targetChild,bccp);
                BCSChild pbcsc=null;
                synchronized(children){
                    children.put(targetChild,bcsc);
                    if(bccp!=null) children.put(bccp,pbcsc=createBCSChild(bccp,targetChild));
                }
                if(cbcc!=null) synchronized(cbcc){
                    try{
                        cbcc.setBeanContext(getBeanContextPeer());
                    }catch(PropertyVetoException pve){
                        synchronized(children){
                            children.remove(targetChild);
                            if(bccp!=null) children.remove(bccp);
                        }
                        throw new IllegalStateException();
                    }
                    cbcc.addPropertyChangeListener("beanContext",childPCL);
                    cbcc.addVetoableChangeListener("beanContext",childVCL);
                }
                Visibility v=getChildVisibility(targetChild);
                if(v!=null){
                    if(okToUseGui)
                        v.okToUseGui();
                    else
                        v.dontUseGui();
                }
                if(getChildSerializable(targetChild)!=null) serializable++;
                childJustAddedHook(targetChild,bcsc);
                if(bccp!=null){
                    v=getChildVisibility(bccp);
                    if(v!=null){
                        if(okToUseGui)
                            v.okToUseGui();
                        else
                            v.dontUseGui();
                    }
                    if(getChildSerializable(bccp)!=null) serializable++;
                    childJustAddedHook(bccp,pbcsc);
                }
            }
            // The specification requires that we fire a notification of the change
            fireChildrenAdded(new BeanContextMembershipEvent(getBeanContextPeer(),bccp==null?new Object[]{targetChild}:new Object[]{targetChild,bccp}));
        }
        return true;
    }    public synchronized void setDesignTime(boolean dTime){
        if(designTime!=dTime){
            designTime=dTime;
            firePropertyChange("designMode",Boolean.valueOf(!dTime),Boolean.valueOf(dTime));
        }
    }

    protected BCSChild createBCSChild(Object targetChild,Object peer){
        return new BCSChild(targetChild,peer);
    }

    public boolean remove(Object targetChild){
        return remove(targetChild,true);
    }    public synchronized boolean isDesignTime(){
        return designTime;
    }

    public boolean containsAll(Collection c){
        synchronized(children){
            Iterator i=c.iterator();
            while(i.hasNext())
                if(!contains(i.next()))
                    return false;
            return true;
        }
    }

    public boolean addAll(Collection c){
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection c){
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection c){
        throw new UnsupportedOperationException();
    }    public synchronized boolean needsGui(){
        BeanContext bc=getBeanContextPeer();
        if(bc!=this){
            if(bc instanceof Visibility) return ((Visibility)bc).needsGui();
            if(bc instanceof Container||bc instanceof Component)
                return true;
        }
        synchronized(children){
            for(Iterator i=children.keySet().iterator();i.hasNext();){
                Object c=i.next();
                try{
                    return ((Visibility)c).needsGui();
                }catch(ClassCastException cce){
                    // do nothing ...
                }
                if(c instanceof Container||c instanceof Component)
                    return true;
            }
        }
        return false;
    }

    public void clear(){
        throw new UnsupportedOperationException();
    }

    protected boolean validatePendingAdd(Object targetChild){
        return true;
    }    public synchronized void dontUseGui(){
        if(okToUseGui){
            okToUseGui=false;
            // lets also tell the Children that can that they may not use their GUI's
            synchronized(children){
                for(Iterator i=children.keySet().iterator();i.hasNext();){
                    Visibility v=getChildVisibility(i.next());
                    if(v!=null) v.dontUseGui();
                }
            }
        }
    }

    protected void childJustAddedHook(Object child,BCSChild bcsc){
    }

    protected final void fireChildrenAdded(BeanContextMembershipEvent bcme){
        Object[] copy;
        synchronized(bcmListeners){
            copy=bcmListeners.toArray();
        }
        for(int i=0;i<copy.length;i++)
            ((BeanContextMembershipListener)copy[i]).childrenAdded(bcme);
    }    public synchronized void okToUseGui(){
        if(!okToUseGui){
            okToUseGui=true;
            // lets also tell the Children that can that they may use their GUI's
            synchronized(children){
                for(Iterator i=children.keySet().iterator();i.hasNext();){
                    Visibility v=getChildVisibility(i.next());
                    if(v!=null) v.okToUseGui();
                }
            }
        }
    }

    public synchronized Locale getLocale(){
        return locale;
    }

    public synchronized void setLocale(Locale newLocale) throws PropertyVetoException{
        if((locale!=null&&!locale.equals(newLocale))&&newLocale!=null){
            Locale old=locale;
            fireVetoableChange("locale",old,newLocale); // throws
            locale=newLocale;
            firePropertyChange("locale",old,newLocale);
        }
    }    public boolean avoidingGui(){
        return !okToUseGui&&needsGui();
    }

    public boolean isSerializing(){
        return serializing;
    }

    protected Iterator bcsChildren(){
        synchronized(children){
            return children.values().iterator();
        }
    }

    private synchronized void writeObject(ObjectOutputStream oos) throws IOException, ClassNotFoundException{
        serializing=true;
        synchronized(BeanContext.globalHierarchyLock){
            try{
                oos.defaultWriteObject(); // serialize the BeanContextSupport object
                bcsPreSerializationHook(oos);
                if(serializable>0&&this.equals(getBeanContextPeer()))
                    writeChildren(oos);
                serialize(oos,(Collection)bcmListeners);
            }finally{
                serializing=false;
            }
        }
    }

    protected void bcsPreSerializationHook(ObjectOutputStream oos) throws IOException{
    }

    protected final void serialize(ObjectOutputStream oos,Collection coll) throws IOException{
        int count=0;
        Object[] objects=coll.toArray();
        for(int i=0;i<objects.length;i++){
            if(objects[i] instanceof Serializable)
                count++;
            else
                objects[i]=null;
        }
        oos.writeInt(count); // number of subsequent objects
        for(int i=0;count>0;i++){
            Object o=objects[i];
            if(o!=null){
                oos.writeObject(o);
                count--;
            }
        }
    }

    public final void writeChildren(ObjectOutputStream oos) throws IOException{
        if(serializable<=0) return;
        boolean prev=serializing;
        serializing=true;
        int count=0;
        synchronized(children){
            Iterator i=children.entrySet().iterator();
            while(i.hasNext()&&count<serializable){
                Map.Entry entry=(Map.Entry)i.next();
                if(entry.getKey() instanceof Serializable){
                    try{
                        oos.writeObject(entry.getKey());   // child
                        oos.writeObject(entry.getValue()); // BCSChild
                    }catch(IOException ioe){
                        serializing=prev;
                        throw ioe;
                    }
                    count++;
                }
            }
        }
        serializing=prev;
        if(count!=serializable){
            throw new IOException("wrote different number of children than expected");
        }
    }

    private synchronized void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException{
        synchronized(BeanContext.globalHierarchyLock){
            ois.defaultReadObject();
            initialize();
            bcsPreDeserializationHook(ois);
            if(serializable>0&&this.equals(getBeanContextPeer()))
                readChildren(ois);
            deserialize(ois,bcmListeners=new ArrayList(1));
        }
    }

    protected void bcsPreDeserializationHook(ObjectInputStream ois) throws IOException, ClassNotFoundException{
    }

    protected final void deserialize(ObjectInputStream ois,Collection coll) throws IOException, ClassNotFoundException{
        int count=0;
        count=ois.readInt();
        while(count-->0){
            coll.add(ois.readObject());
        }
    }

    public final void readChildren(ObjectInputStream ois) throws IOException, ClassNotFoundException{
        int count=serializable;
        while(count-->0){
            Object child=null;
            BCSChild bscc=null;
            try{
                child=ois.readObject();
                bscc=(BCSChild)ois.readObject();
            }catch(IOException ioe){
                continue;
            }catch(ClassNotFoundException cnfe){
                continue;
            }
            synchronized(child){
                BeanContextChild bcc=null;
                try{
                    bcc=(BeanContextChild)child;
                }catch(ClassCastException cce){
                    // do nothing;
                }
                if(bcc!=null){
                    try{
                        bcc.setBeanContext(getBeanContextPeer());
                        bcc.addPropertyChangeListener("beanContext",childPCL);
                        bcc.addVetoableChangeListener("beanContext",childVCL);
                    }catch(PropertyVetoException pve){
                        continue;
                    }
                }
                childDeserializedHook(child,bscc);
            }
        }
    }

    protected void childDeserializedHook(Object child,BCSChild bcsc){
        synchronized(children){
            children.put(child,bcsc);
        }
    }

    protected final Object[] copyChildren(){
        synchronized(children){
            return children.keySet().toArray();
        }
    }

    protected static final class BCSIterator implements Iterator{
        private Iterator src;

        BCSIterator(Iterator i){
            super();
            src=i;
        }

        public boolean hasNext(){
            return src.hasNext();
        }

        public Object next(){
            return src.next();
        }

        public void remove(){ /** do nothing */}
    }

    protected class BCSChild implements Serializable{
        private static final long serialVersionUID=-5815286101609939109L;
        private Object child;
        private Object proxyPeer;
        private transient boolean removePending;

        BCSChild(Object bcc,Object peer){
            super();
            child=bcc;
            proxyPeer=peer;
        }

        Object getChild(){
            return child;
        }

        boolean isRemovePending(){
            return removePending;
        }

        void setRemovePending(boolean v){
            removePending=v;
        }

        boolean isProxyPeer(){
            return proxyPeer!=null;
        }

        Object getProxyPeer(){
            return proxyPeer;
        }
    }    protected static final Visibility getChildVisibility(Object child){
        try{
            return (Visibility)child;
        }catch(ClassCastException cce){
            return null;
        }
    }


































}
