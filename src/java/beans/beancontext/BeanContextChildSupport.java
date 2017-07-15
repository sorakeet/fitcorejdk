/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans.beancontext;

import java.beans.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class BeanContextChildSupport implements BeanContextChild, BeanContextServicesListener, Serializable{
    static final long serialVersionUID=6328947014421475877L;
    public BeanContextChild beanContextChildPeer;
    protected PropertyChangeSupport pcSupport;
    protected VetoableChangeSupport vcSupport;
    protected transient BeanContext beanContext;    public synchronized void setBeanContext(BeanContext bc) throws PropertyVetoException{
        if(bc==beanContext) return;
        BeanContext oldValue=beanContext;
        BeanContext newValue=bc;
        if(!rejectedSetBCOnce){
            if(rejectedSetBCOnce=!validatePendingSetBeanContext(bc)){
                throw new PropertyVetoException(
                        "setBeanContext() change rejected:",
                        new PropertyChangeEvent(beanContextChildPeer,"beanContext",oldValue,newValue)
                );
            }
            try{
                fireVetoableChange("beanContext",
                        oldValue,
                        newValue
                );
            }catch(PropertyVetoException pve){
                rejectedSetBCOnce=true;
                throw pve; // re-throw
            }
        }
        if(beanContext!=null) releaseBeanContextResources();
        beanContext=newValue;
        rejectedSetBCOnce=false;
        firePropertyChange("beanContext",
                oldValue,
                newValue
        );
        if(beanContext!=null) initializeBeanContextResources();
    }
    protected transient boolean rejectedSetBCOnce;

    public BeanContextChildSupport(){
        super();
        beanContextChildPeer=this;
        pcSupport=new PropertyChangeSupport(beanContextChildPeer);
        vcSupport=new VetoableChangeSupport(beanContextChildPeer);
    }    public synchronized BeanContext getBeanContext(){
        return beanContext;
    }

    public BeanContextChildSupport(BeanContextChild bcc){
        super();
        beanContextChildPeer=(bcc!=null)?bcc:this;
        pcSupport=new PropertyChangeSupport(beanContextChildPeer);
        vcSupport=new VetoableChangeSupport(beanContextChildPeer);
    }

    public void serviceRevoked(BeanContextServiceRevokedEvent bcsre){
    }    public void addPropertyChangeListener(String name,PropertyChangeListener pcl){
        pcSupport.addPropertyChangeListener(name,pcl);
    }

    public void serviceAvailable(BeanContextServiceAvailableEvent bcsae){
    }

    public BeanContextChild getBeanContextChildPeer(){
        return beanContextChildPeer;
    }    public void removePropertyChangeListener(String name,PropertyChangeListener pcl){
        pcSupport.removePropertyChangeListener(name,pcl);
    }

    public boolean isDelegated(){
        return !this.equals(beanContextChildPeer);
    }

    private void writeObject(ObjectOutputStream oos) throws IOException{
        /**
         * don't serialize if we are delegated and the delegator is not also
         * serializable.
         */
        if(!equals(beanContextChildPeer)&&!(beanContextChildPeer instanceof Serializable))
            throw new IOException("BeanContextChildSupport beanContextChildPeer not Serializable");
        else
            oos.defaultWriteObject();
    }    public void addVetoableChangeListener(String name,VetoableChangeListener vcl){
        vcSupport.addVetoableChangeListener(name,vcl);
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException{
        ois.defaultReadObject();
    }

    public void removeVetoableChangeListener(String name,VetoableChangeListener vcl){
        vcSupport.removeVetoableChangeListener(name,vcl);
    }











    public void firePropertyChange(String name,Object oldValue,Object newValue){
        pcSupport.firePropertyChange(name,oldValue,newValue);
    }

    public void fireVetoableChange(String name,Object oldValue,Object newValue) throws PropertyVetoException{
        vcSupport.fireVetoableChange(name,oldValue,newValue);
    }

    public boolean validatePendingSetBeanContext(BeanContext newValue){
        return true;
    }

    protected void releaseBeanContextResources(){
        // do nothing
    }

    protected void initializeBeanContextResources(){
        // do nothing
    }
}
