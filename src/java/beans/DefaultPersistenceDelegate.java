/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans;

import sun.reflect.misc.MethodUtil;
import sun.reflect.misc.ReflectUtil;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.EventListener;
import java.util.Objects;

public class DefaultPersistenceDelegate extends PersistenceDelegate{
    private static final String[] EMPTY={};
    private final String[] constructor;
    private Boolean definesEquals;

    public DefaultPersistenceDelegate(){
        this.constructor=EMPTY;
    }

    public DefaultPersistenceDelegate(String[] constructorPropertyNames){
        this.constructor=(constructorPropertyNames==null)?EMPTY:constructorPropertyNames.clone();
    }

    protected boolean mutatesTo(Object oldInstance,Object newInstance){
        // Assume the instance is either mutable or a singleton
        // if it has a nullary constructor.
        return (constructor.length==0)||!definesEquals(oldInstance)?
                super.mutatesTo(oldInstance,newInstance):
                oldInstance.equals(newInstance);
    }

    private boolean definesEquals(Object instance){
        if(definesEquals!=null){
            return (definesEquals==Boolean.TRUE);
        }else{
            boolean result=definesEquals(instance.getClass());
            definesEquals=result?Boolean.TRUE:Boolean.FALSE;
            return result;
        }
    }

    private static boolean definesEquals(Class<?> type){
        try{
            return type==type.getMethod("equals",Object.class).getDeclaringClass();
        }catch(NoSuchMethodException e){
            return false;
        }
    }

    protected Expression instantiate(Object oldInstance,Encoder out){
        int nArgs=constructor.length;
        Class<?> type=oldInstance.getClass();
        Object[] constructorArgs=new Object[nArgs];
        for(int i=0;i<nArgs;i++){
            try{
                Method method=findMethod(type,this.constructor[i]);
                constructorArgs[i]=MethodUtil.invoke(method,oldInstance,new Object[0]);
            }catch(Exception e){
                out.getExceptionListener().exceptionThrown(e);
            }
        }
        return new Expression(oldInstance,oldInstance.getClass(),"new",constructorArgs);
    }

    private Method findMethod(Class<?> type,String property){
        if(property==null){
            throw new IllegalArgumentException("Property name is null");
        }
        PropertyDescriptor pd=getPropertyDescriptor(type,property);
        if(pd==null){
            throw new IllegalStateException("Could not find property by the name "+property);
        }
        Method method=pd.getReadMethod();
        if(method==null){
            throw new IllegalStateException("Could not find getter for the property "+property);
        }
        return method;
    }

    private static PropertyDescriptor getPropertyDescriptor(Class<?> type,String property){
        try{
            for(PropertyDescriptor pd : Introspector.getBeanInfo(type).getPropertyDescriptors()){
                if(property.equals(pd.getName()))
                    return pd;
            }
        }catch(IntrospectionException exception){
        }
        return null;
    }

    protected void initialize(Class<?> type,
                              Object oldInstance,Object newInstance,
                              Encoder out){
        // System.out.println("DefulatPD:initialize" + type);
        super.initialize(type,oldInstance,newInstance,out);
        if(oldInstance.getClass()==type){ // !type.isInterface()) {
            initBean(type,oldInstance,newInstance,out);
        }
    }

    // Write out the properties of this instance.
    private void initBean(Class<?> type,Object oldInstance,Object newInstance,Encoder out){
        for(Field field : type.getFields()){
            if(!ReflectUtil.isPackageAccessible(field.getDeclaringClass())){
                continue;
            }
            int mod=field.getModifiers();
            if(Modifier.isFinal(mod)||Modifier.isStatic(mod)||Modifier.isTransient(mod)){
                continue;
            }
            try{
                Expression oldGetExp=new Expression(field,"get",new Object[]{oldInstance});
                Expression newGetExp=new Expression(field,"get",new Object[]{newInstance});
                Object oldValue=oldGetExp.getValue();
                Object newValue=newGetExp.getValue();
                out.writeExpression(oldGetExp);
                if(!Objects.equals(newValue,out.get(oldValue))){
                    out.writeStatement(new Statement(field,"set",new Object[]{oldInstance,oldValue}));
                }
            }catch(Exception exception){
                out.getExceptionListener().exceptionThrown(exception);
            }
        }
        BeanInfo info;
        try{
            info=Introspector.getBeanInfo(type);
        }catch(IntrospectionException exception){
            return;
        }
        // Properties
        for(PropertyDescriptor d : info.getPropertyDescriptors()){
            if(d.isTransient()){
                continue;
            }
            try{
                doProperty(type,d,oldInstance,newInstance,out);
            }catch(Exception e){
                out.getExceptionListener().exceptionThrown(e);
            }
        }
        // Listeners
        /**
         Pending(milne). There is a general problem with the archival of
         listeners which is unresolved as of 1.4. Many of the methods
         which install one object inside another (typically "add" methods
         or setters) automatically install a listener on the "child" object
         so that its "parent" may respond to changes that are made to it.
         For example the JTable:setModel() method automatically adds a
         TableModelListener (the JTable itself in this case) to the supplied
         table model.

         We do not need to explicitly add these listeners to the model in an
         archive as they will be added automatically by, in the above case,
         the JTable's "setModel" method. In some cases, we must specifically
         avoid trying to do this since the listener may be an inner class
         that cannot be instantiated using public API.

         No general mechanism currently
         exists for differentiating between these kind of listeners and
         those which were added explicitly by the user. A mechanism must
         be created to provide a general means to differentiate these
         special cases so as to provide reliable persistence of listeners
         for the general case.
         */
        if(!java.awt.Component.class.isAssignableFrom(type)){
            return; // Just handle the listeners of Components for now.
        }
        for(EventSetDescriptor d : info.getEventSetDescriptors()){
            if(d.isTransient()){
                continue;
            }
            Class<?> listenerType=d.getListenerType();
            // The ComponentListener is added automatically, when
            // Contatiner:add is called on the parent.
            if(listenerType==java.awt.event.ComponentListener.class){
                continue;
            }
            // JMenuItems have a change listener added to them in
            // their "add" methods to enable accessibility support -
            // see the add method in JMenuItem for details. We cannot
            // instantiate this instance as it is a private inner class
            // and do not need to do this anyway since it will be created
            // and installed by the "add" method. Special case this for now,
            // ignoring all change listeners on JMenuItems.
            if(listenerType==javax.swing.event.ChangeListener.class&&
                    type==javax.swing.JMenuItem.class){
                continue;
            }
            EventListener[] oldL=new EventListener[0];
            EventListener[] newL=new EventListener[0];
            try{
                Method m=d.getGetListenerMethod();
                oldL=(EventListener[])MethodUtil.invoke(m,oldInstance,new Object[]{});
                newL=(EventListener[])MethodUtil.invoke(m,newInstance,new Object[]{});
            }catch(Exception e2){
                try{
                    Method m=type.getMethod("getListeners",new Class<?>[]{Class.class});
                    oldL=(EventListener[])MethodUtil.invoke(m,oldInstance,new Object[]{listenerType});
                    newL=(EventListener[])MethodUtil.invoke(m,newInstance,new Object[]{listenerType});
                }catch(Exception e3){
                    return;
                }
            }
            // Asssume the listeners are in the same order and that there are no gaps.
            // Eventually, this may need to do true differencing.
            String addListenerMethodName=d.getAddListenerMethod().getName();
            for(int i=newL.length;i<oldL.length;i++){
                // System.out.println("Adding listener: " + addListenerMethodName + oldL[i]);
                invokeStatement(oldInstance,addListenerMethodName,new Object[]{oldL[i]},out);
            }
            String removeListenerMethodName=d.getRemoveListenerMethod().getName();
            for(int i=oldL.length;i<newL.length;i++){
                invokeStatement(oldInstance,removeListenerMethodName,new Object[]{newL[i]},out);
            }
        }
    }

    private void doProperty(Class<?> type,PropertyDescriptor pd,Object oldInstance,Object newInstance,Encoder out) throws Exception{
        Method getter=pd.getReadMethod();
        Method setter=pd.getWriteMethod();
        if(getter!=null&&setter!=null){
            Expression oldGetExp=new Expression(oldInstance,getter.getName(),new Object[]{});
            Expression newGetExp=new Expression(newInstance,getter.getName(),new Object[]{});
            Object oldValue=oldGetExp.getValue();
            Object newValue=newGetExp.getValue();
            out.writeExpression(oldGetExp);
            if(!Objects.equals(newValue,out.get(oldValue))){
                // Search for a static constant with this value;
                Object e=(Object[])pd.getValue("enumerationValues");
                if(e instanceof Object[]&&Array.getLength(e)%3==0){
                    Object[] a=(Object[])e;
                    for(int i=0;i<a.length;i=i+3){
                        try{
                            Field f=type.getField((String)a[i]);
                            if(f.get(null).equals(oldValue)){
                                out.remove(oldValue);
                                out.writeExpression(new Expression(oldValue,f,"get",new Object[]{null}));
                            }
                        }catch(Exception ex){
                        }
                    }
                }
                invokeStatement(oldInstance,setter.getName(),new Object[]{oldValue},out);
            }
        }
    }

    static void invokeStatement(Object instance,String methodName,Object[] args,Encoder out){
        out.writeStatement(new Statement(instance,methodName,args));
    }
}
