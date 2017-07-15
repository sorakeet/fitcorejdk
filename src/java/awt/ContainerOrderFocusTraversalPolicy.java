/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import sun.util.logging.PlatformLogger;

import java.util.ArrayList;
import java.util.List;

public class ContainerOrderFocusTraversalPolicy extends FocusTraversalPolicy
        implements java.io.Serializable{
    private static final PlatformLogger log=PlatformLogger.getLogger("java.awt.ContainerOrderFocusTraversalPolicy");
    private static final long serialVersionUID=486933713763926351L;
    final private int FORWARD_TRAVERSAL=0;
    final private int BACKWARD_TRAVERSAL=1;
    private boolean implicitDownCycleTraversal=true;
    transient private Container cachedRoot;
    transient private List<Component> cachedCycle;

    /**
     * We suppose to use getFocusTraversalCycle & getComponentIndex methods in order
     * to divide the policy into two parts:
     * 1) Making the focus traversal cycle.
     * 2) Traversing the cycle.
     * The 1st point assumes producing a list of components representing the focus
     * traversal cycle. The two methods mentioned above should implement this logic.
     * The 2nd point assumes implementing the common concepts of operating on the
     * cycle: traversing back and forth, retrieving the initial/default/first/last
     * component. These concepts are described in the AWT Focus Spec and they are
     * applied to the FocusTraversalPolicy in general.
     * Thus, a descendant of this policy may wish to not reimplement the logic of
     * the 2nd point but just override the implementation of the 1st one.
     * A striking example of such a descendant is the javax.swing.SortingFocusTraversalPolicy.
     */
    private List<Component> getFocusTraversalCycle(Container aContainer){
        List<Component> cycle=new ArrayList<Component>();
        enumerateCycle(aContainer,cycle);
        return cycle;
    }

    private int getComponentIndex(List<Component> cycle,Component aComponent){
        return cycle.indexOf(aComponent);
    }

    private void enumerateCycle(Container container,List<Component> cycle){
        if(!(container.isVisible()&&container.isDisplayable())){
            return;
        }
        cycle.add(container);
        Component[] components=container.getComponents();
        for(int i=0;i<components.length;i++){
            Component comp=components[i];
            if(comp instanceof Container){
                Container cont=(Container)comp;
                if(!cont.isFocusCycleRoot()&&!cont.isFocusTraversalPolicyProvider()){
                    enumerateCycle(cont,cycle);
                    continue;
                }
            }
            cycle.add(comp);
        }
    }

    private Container getTopmostProvider(Container focusCycleRoot,Component aComponent){
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
        synchronized(aContainer.getTreeLock()){
            if(!(aContainer.isVisible()&&aContainer.isDisplayable())){
                return null;
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
        synchronized(aContainer.getTreeLock()){
            if(!(aContainer.isVisible()&&aContainer.isDisplayable())){
                return null;
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
            Component comp=null;
            Component tryComp=null;
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
        synchronized(aContainer.getTreeLock()){
            if(!(aContainer.isVisible()&&aContainer.isDisplayable())){
                return null;
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
        synchronized(aContainer.getTreeLock()){
            if(!(aContainer.isVisible()&&aContainer.isDisplayable())){
                return null;
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

    protected boolean accept(Component aComponent){
        if(!aComponent.canBeFocusOwner()){
            return false;
        }
        // Verify that the Component is recursively enabled. Disabling a
        // heavyweight Container disables its children, whereas disabling
        // a lightweight Container does not.
        if(!(aComponent instanceof Window)){
            for(Container enableTest=aComponent.getParent();
                enableTest!=null;
                enableTest=enableTest.getParent()){
                if(!(enableTest.isEnabled()||enableTest.isLightweight())){
                    return false;
                }
                if(enableTest instanceof Window){
                    break;
                }
            }
        }
        return true;
    }
}
