/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import sun.security.action.GetPropertyAction;
import sun.util.logging.PlatformLogger;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.List;

public class SortingFocusTraversalPolicy
        extends InternalFrameFocusTraversalPolicy{
    // Delegate our fitness test to ContainerOrder so that we only have to
    // code the algorithm once.
    private static final SwingContainerOrderFocusTraversalPolicy
            fitnessTestPolicy=new SwingContainerOrderFocusTraversalPolicy();
    private static final boolean legacySortingFTPEnabled;
    private static final Method legacyMergeSortMethod;

    static{
        legacySortingFTPEnabled="true".equals(AccessController.doPrivileged(
                new GetPropertyAction("swing.legacySortingFTPEnabled","true")));
        legacyMergeSortMethod=legacySortingFTPEnabled?
                AccessController.doPrivileged(new PrivilegedAction<Method>(){
                    public Method run(){
                        try{
                            Class c=Class.forName("java.util.Arrays");
                            Method m=c.getDeclaredMethod("legacyMergeSort",new Class[]{Object[].class,Comparator.class});
                            m.setAccessible(true);
                            return m;
                        }catch(ClassNotFoundException|NoSuchMethodException e){
                            // using default sorting algo
                            return null;
                        }
                    }
                }):
                null;
    }

    final private int FORWARD_TRAVERSAL=0;
    final private int BACKWARD_TRAVERSAL=1;
    private Comparator<? super Component> comparator;
    private boolean implicitDownCycleTraversal=true;
    private PlatformLogger log=PlatformLogger.getLogger("javax.swing.SortingFocusTraversalPolicy");
    transient private Container cachedRoot;
    transient private List<Component> cachedCycle;

    protected SortingFocusTraversalPolicy(){
    }

    public SortingFocusTraversalPolicy(Comparator<? super Component> comparator){
        this.comparator=comparator;
    }

    private List<Component> getFocusTraversalCycle(Container aContainer){
        List<Component> cycle=new ArrayList<Component>();
        enumerateAndSortCycle(aContainer,cycle);
        return cycle;
    }

    private int getComponentIndex(List<Component> cycle,Component aComponent){
        int index;
        try{
            index=Collections.binarySearch(cycle,aComponent,comparator);
        }catch(ClassCastException e){
            if(log.isLoggable(PlatformLogger.Level.FINE)){
                log.fine("### During the binary search for "+aComponent+" the exception occurred: ",e);
            }
            return -1;
        }
        if(index<0){
            // Fix for 5070991.
            // A workaround for a transitivity problem caused by ROW_TOLERANCE,
            // because of that the component may be missed in the binary search.
            // Try to search it again directly.
            index=cycle.indexOf(aComponent);
        }
        return index;
    }

    private void enumerateAndSortCycle(Container focusCycleRoot,List<Component> cycle){
        if(focusCycleRoot.isShowing()){
            enumerateCycle(focusCycleRoot,cycle);
            if(!legacySortingFTPEnabled||
                    !legacySort(cycle,comparator)){
                Collections.sort(cycle,comparator);
            }
        }
    }

    private boolean legacySort(List<Component> l,Comparator<? super Component> c){
        if(legacyMergeSortMethod==null)
            return false;
        Object[] a=l.toArray();
        try{
            legacyMergeSortMethod.invoke(null,a,c);
        }catch(IllegalAccessException|InvocationTargetException e){
            return false;
        }
        ListIterator<Component> i=l.listIterator();
        for(Object e : a){
            i.next();
            i.set((Component)e);
        }
        return true;
    }

    private void enumerateCycle(Container container,List<Component> cycle){
        if(!(container.isVisible()&&container.isDisplayable())){
            return;
        }
        cycle.add(container);
        Component[] components=container.getComponents();
        for(Component comp : components){
            if(comp instanceof Container){
                Container cont=(Container)comp;
                if(!cont.isFocusCycleRoot()&&
                        !cont.isFocusTraversalPolicyProvider()&&
                        !((cont instanceof JComponent)&&((JComponent)cont).isManagingFocus())){
                    enumerateCycle(cont,cycle);
                    continue;
                }
            }
            cycle.add(comp);
        }
    }

    Container getTopmostProvider(Container focusCycleRoot,Component aComponent){
        Container aCont=aComponent.getParent();
        Container ftp=null;
        while(aCont!=focusCycleRoot&&aCont!=null){
            if(aCont.isFocusTraversalPolicyProvider()){
                ftp=aCont;
            }
            aCont=aCont.getParent();
        }
        if(aCont==null){
            return null;
        }
        return ftp;
    }

    private Component getComponentDownCycle(Component comp,int traversalDirection){
        Component retComp=null;
        if(comp instanceof Container){
            Container cont=(Container)comp;
            if(cont.isFocusCycleRoot()){
                if(getImplicitDownCycleTraversal()){
                    retComp=cont.getFocusTraversalPolicy().getDefaultComponent(cont);
                    if(retComp!=null&&log.isLoggable(PlatformLogger.Level.FINE)){
                        log.fine("### Transfered focus down-cycle to "+retComp+
                                " in the focus cycle root "+cont);
                    }
                }else{
                    return null;
                }
            }else if(cont.isFocusTraversalPolicyProvider()){
                retComp=(traversalDirection==FORWARD_TRAVERSAL?
                        cont.getFocusTraversalPolicy().getDefaultComponent(cont):
                        cont.getFocusTraversalPolicy().getLastComponent(cont));
                if(retComp!=null&&log.isLoggable(PlatformLogger.Level.FINE)){
                    log.fine("### Transfered focus to "+retComp+" in the FTP provider "+cont);
                }
            }
        }
        return retComp;
    }

    public Component getComponentAfter(Container aContainer,Component aComponent){
        if(log.isLoggable(PlatformLogger.Level.FINE)){
            log.fine("### Searching in "+aContainer+" for component after "+aComponent);
        }
        if(aContainer==null||aComponent==null){
            throw new IllegalArgumentException("aContainer and aComponent cannot be null");
        }
        if(!aContainer.isFocusTraversalPolicyProvider()&&!aContainer.isFocusCycleRoot()){
            throw new IllegalArgumentException("aContainer should be focus cycle root or focus traversal policy provider");
        }else if(aContainer.isFocusCycleRoot()&&!aComponent.isFocusCycleRoot(aContainer)){
            throw new IllegalArgumentException("aContainer is not a focus cycle root of aComponent");
        }
        // Before all the ckecks below we first see if it's an FTP provider or a focus cycle root.
        // If it's the case just go down cycle (if it's set to "implicit").
        Component comp=getComponentDownCycle(aComponent,FORWARD_TRAVERSAL);
        if(comp!=null){
            return comp;
        }
        // See if the component is inside of policy provider.
        Container provider=getTopmostProvider(aContainer,aComponent);
        if(provider!=null){
            if(log.isLoggable(PlatformLogger.Level.FINE)){
                log.fine("### Asking FTP "+provider+" for component after "+aComponent);
            }
            // FTP knows how to find component after the given. We don't.
            FocusTraversalPolicy policy=provider.getFocusTraversalPolicy();
            Component afterComp=policy.getComponentAfter(provider,aComponent);
            // Null result means that we overstepped the limit of the FTP's cycle.
            // In that case we must quit the cycle, otherwise return the component found.
            if(afterComp!=null){
                if(log.isLoggable(PlatformLogger.Level.FINE)){
                    log.fine("### FTP returned "+afterComp);
                }
                return afterComp;
            }
            aComponent=provider;
        }
        List<Component> cycle=getFocusTraversalCycle(aContainer);
        if(log.isLoggable(PlatformLogger.Level.FINE)){
            log.fine("### Cycle is "+cycle+", component is "+aComponent);
        }
        int index=getComponentIndex(cycle,aComponent);
        if(index<0){
            if(log.isLoggable(PlatformLogger.Level.FINE)){
                log.fine("### Didn't find component "+aComponent+" in a cycle "+aContainer);
            }
            return getFirstComponent(aContainer);
        }
        for(index++;index<cycle.size();index++){
            comp=cycle.get(index);
            if(accept(comp)){
                return comp;
            }else if((comp=getComponentDownCycle(comp,FORWARD_TRAVERSAL))!=null){
                return comp;
            }
        }
        if(aContainer.isFocusCycleRoot()){
            this.cachedRoot=aContainer;
            this.cachedCycle=cycle;
            comp=getFirstComponent(aContainer);
            this.cachedRoot=null;
            this.cachedCycle=null;
            return comp;
        }
        return null;
    }

    public Component getComponentBefore(Container aContainer,Component aComponent){
        if(aContainer==null||aComponent==null){
            throw new IllegalArgumentException("aContainer and aComponent cannot be null");
        }
        if(!aContainer.isFocusTraversalPolicyProvider()&&!aContainer.isFocusCycleRoot()){
            throw new IllegalArgumentException("aContainer should be focus cycle root or focus traversal policy provider");
        }else if(aContainer.isFocusCycleRoot()&&!aComponent.isFocusCycleRoot(aContainer)){
            throw new IllegalArgumentException("aContainer is not a focus cycle root of aComponent");
        }
        // See if the component is inside of policy provider.
        Container provider=getTopmostProvider(aContainer,aComponent);
        if(provider!=null){
            if(log.isLoggable(PlatformLogger.Level.FINE)){
                log.fine("### Asking FTP "+provider+" for component after "+aComponent);
            }
            // FTP knows how to find component after the given. We don't.
            FocusTraversalPolicy policy=provider.getFocusTraversalPolicy();
            Component beforeComp=policy.getComponentBefore(provider,aComponent);
            // Null result means that we overstepped the limit of the FTP's cycle.
            // In that case we must quit the cycle, otherwise return the component found.
            if(beforeComp!=null){
                if(log.isLoggable(PlatformLogger.Level.FINE)){
                    log.fine("### FTP returned "+beforeComp);
                }
                return beforeComp;
            }
            aComponent=provider;
            // If the provider is traversable it's returned.
            if(accept(aComponent)){
                return aComponent;
            }
        }
        List<Component> cycle=getFocusTraversalCycle(aContainer);
        if(log.isLoggable(PlatformLogger.Level.FINE)){
            log.fine("### Cycle is "+cycle+", component is "+aComponent);
        }
        int index=getComponentIndex(cycle,aComponent);
        if(index<0){
            if(log.isLoggable(PlatformLogger.Level.FINE)){
                log.fine("### Didn't find component "+aComponent+" in a cycle "+aContainer);
            }
            return getLastComponent(aContainer);
        }
        Component comp;
        Component tryComp;
        for(index--;index>=0;index--){
            comp=cycle.get(index);
            if(comp!=aContainer&&(tryComp=getComponentDownCycle(comp,BACKWARD_TRAVERSAL))!=null){
                return tryComp;
            }else if(accept(comp)){
                return comp;
            }
        }
        if(aContainer.isFocusCycleRoot()){
            this.cachedRoot=aContainer;
            this.cachedCycle=cycle;
            comp=getLastComponent(aContainer);
            this.cachedRoot=null;
            this.cachedCycle=null;
            return comp;
        }
        return null;
    }

    public Component getFirstComponent(Container aContainer){
        List<Component> cycle;
        if(log.isLoggable(PlatformLogger.Level.FINE)){
            log.fine("### Getting first component in "+aContainer);
        }
        if(aContainer==null){
            throw new IllegalArgumentException("aContainer cannot be null");
        }
        if(this.cachedRoot==aContainer){
            cycle=this.cachedCycle;
        }else{
            cycle=getFocusTraversalCycle(aContainer);
        }
        if(cycle.size()==0){
            if(log.isLoggable(PlatformLogger.Level.FINE)){
                log.fine("### Cycle is empty");
            }
            return null;
        }
        if(log.isLoggable(PlatformLogger.Level.FINE)){
            log.fine("### Cycle is "+cycle);
        }
        for(Component comp : cycle){
            if(accept(comp)){
                return comp;
            }else if(comp!=aContainer&&
                    (comp=getComponentDownCycle(comp,FORWARD_TRAVERSAL))!=null){
                return comp;
            }
        }
        return null;
    }

    public Component getLastComponent(Container aContainer){
        List<Component> cycle;
        if(log.isLoggable(PlatformLogger.Level.FINE)){
            log.fine("### Getting last component in "+aContainer);
        }
        if(aContainer==null){
            throw new IllegalArgumentException("aContainer cannot be null");
        }
        if(this.cachedRoot==aContainer){
            cycle=this.cachedCycle;
        }else{
            cycle=getFocusTraversalCycle(aContainer);
        }
        if(cycle.size()==0){
            if(log.isLoggable(PlatformLogger.Level.FINE)){
                log.fine("### Cycle is empty");
            }
            return null;
        }
        if(log.isLoggable(PlatformLogger.Level.FINE)){
            log.fine("### Cycle is "+cycle);
        }
        for(int i=cycle.size()-1;i>=0;i--){
            Component comp=cycle.get(i);
            if(accept(comp)){
                return comp;
            }else if(comp instanceof Container&&comp!=aContainer){
                Container cont=(Container)comp;
                if(cont.isFocusTraversalPolicyProvider()){
                    Component retComp=cont.getFocusTraversalPolicy().getLastComponent(cont);
                    if(retComp!=null){
                        return retComp;
                    }
                }
            }
        }
        return null;
    }

    public Component getDefaultComponent(Container aContainer){
        return getFirstComponent(aContainer);
    }

    public boolean getImplicitDownCycleTraversal(){
        return implicitDownCycleTraversal;
    }

    public void setImplicitDownCycleTraversal(boolean implicitDownCycleTraversal){
        this.implicitDownCycleTraversal=implicitDownCycleTraversal;
    }

    protected Comparator<? super Component> getComparator(){
        return comparator;
    }

    protected void setComparator(Comparator<? super Component> comparator){
        this.comparator=comparator;
    }

    protected boolean accept(Component aComponent){
        return fitnessTestPolicy.accept(aComponent);
    }
}

// Create our own subclass and change accept to public so that we can call
// accept.
class SwingContainerOrderFocusTraversalPolicy
        extends java.awt.ContainerOrderFocusTraversalPolicy{
    public boolean accept(Component aComponent){
        return super.accept(aComponent);
    }
}
