/**
 * Copyright (c) 1996, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans;

import java.lang.ref.Reference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class EventSetDescriptor extends FeatureDescriptor{
    private MethodDescriptor[] listenerMethodDescriptors;
    private MethodDescriptor addMethodDescriptor;
    private MethodDescriptor removeMethodDescriptor;
    private MethodDescriptor getMethodDescriptor;
    private Reference<Method[]> listenerMethodsRef;
    private Reference<? extends Class<?>> listenerTypeRef;
    private boolean unicast;
    private boolean inDefaultEventSet=true;

    public EventSetDescriptor(Class<?> sourceClass,String eventSetName,
                              Class<?> listenerType,String listenerMethodName)
            throws IntrospectionException{
        this(sourceClass,eventSetName,listenerType,
                new String[]{listenerMethodName},
                Introspector.ADD_PREFIX+getListenerClassName(listenerType),
                Introspector.REMOVE_PREFIX+getListenerClassName(listenerType),
                Introspector.GET_PREFIX+getListenerClassName(listenerType)+"s");
        String eventName=NameGenerator.capitalize(eventSetName)+"Event";
        Method[] listenerMethods=getListenerMethods();
        if(listenerMethods.length>0){
            Class[] args=getParameterTypes(getClass0(),listenerMethods[0]);
            // Check for EventSet compliance. Special case for vetoableChange. See 4529996
            if(!"vetoableChange".equals(eventSetName)&&!args[0].getName().endsWith(eventName)){
                throw new IntrospectionException("Method \""+listenerMethodName+
                        "\" should have argument \""+
                        eventName+"\"");
            }
        }
    }

    private static String getListenerClassName(Class<?> cls){
        String className=cls.getName();
        return className.substring(className.lastIndexOf('.')+1);
    }

    public EventSetDescriptor(Class<?> sourceClass,
                              String eventSetName,
                              Class<?> listenerType,
                              String listenerMethodNames[],
                              String addListenerMethodName,
                              String removeListenerMethodName,
                              String getListenerMethodName)
            throws IntrospectionException{
        if(sourceClass==null||eventSetName==null||listenerType==null){
            throw new NullPointerException();
        }
        setName(eventSetName);
        setClass0(sourceClass);
        setListenerType(listenerType);
        Method[] listenerMethods=new Method[listenerMethodNames.length];
        for(int i=0;i<listenerMethodNames.length;i++){
            // Check for null names
            if(listenerMethodNames[i]==null){
                throw new NullPointerException();
            }
            listenerMethods[i]=getMethod(listenerType,listenerMethodNames[i],1);
        }
        setListenerMethods(listenerMethods);
        setAddListenerMethod(getMethod(sourceClass,addListenerMethodName,1));
        setRemoveListenerMethod(getMethod(sourceClass,removeListenerMethodName,1));
        // Be more forgiving of not finding the getListener method.
        Method method=Introspector.findMethod(sourceClass,getListenerMethodName,0);
        if(method!=null){
            setGetListenerMethod(method);
        }
    }

    private static Method getMethod(Class<?> cls,String name,int args)
            throws IntrospectionException{
        if(name==null){
            return null;
        }
        Method method=Introspector.findMethod(cls,name,args);
        if((method==null)||Modifier.isStatic(method.getModifiers())){
            throw new IntrospectionException("Method not found: "+name+
                    " on class "+cls.getName());
        }
        return method;
    }

    public synchronized Method[] getListenerMethods(){
        Method[] methods=getListenerMethods0();
        if(methods==null){
            if(listenerMethodDescriptors!=null){
                methods=new Method[listenerMethodDescriptors.length];
                for(int i=0;i<methods.length;i++){
                    methods[i]=listenerMethodDescriptors[i].getMethod();
                }
            }
            setListenerMethods(methods);
        }
        return methods;
    }

    private void setListenerMethods(Method[] methods){
        if(methods==null){
            return;
        }
        if(listenerMethodDescriptors==null){
            listenerMethodDescriptors=new MethodDescriptor[methods.length];
            for(int i=0;i<methods.length;i++){
                listenerMethodDescriptors[i]=new MethodDescriptor(methods[i]);
            }
        }
        this.listenerMethodsRef=getSoftReference(methods);
    }

    private Method[] getListenerMethods0(){
        return (this.listenerMethodsRef!=null)
                ?this.listenerMethodsRef.get()
                :null;
    }

    public EventSetDescriptor(Class<?> sourceClass,
                              String eventSetName,
                              Class<?> listenerType,
                              String listenerMethodNames[],
                              String addListenerMethodName,
                              String removeListenerMethodName)
            throws IntrospectionException{
        this(sourceClass,eventSetName,listenerType,
                listenerMethodNames,addListenerMethodName,
                removeListenerMethodName,null);
    }

    public EventSetDescriptor(String eventSetName,
                              Class<?> listenerType,
                              Method listenerMethods[],
                              Method addListenerMethod,
                              Method removeListenerMethod)
            throws IntrospectionException{
        this(eventSetName,listenerType,listenerMethods,
                addListenerMethod,removeListenerMethod,null);
    }

    public EventSetDescriptor(String eventSetName,
                              Class<?> listenerType,
                              Method listenerMethods[],
                              Method addListenerMethod,
                              Method removeListenerMethod,
                              Method getListenerMethod)
            throws IntrospectionException{
        setName(eventSetName);
        setListenerMethods(listenerMethods);
        setAddListenerMethod(addListenerMethod);
        setRemoveListenerMethod(removeListenerMethod);
        setGetListenerMethod(getListenerMethod);
        setListenerType(listenerType);
    }

    public EventSetDescriptor(String eventSetName,
                              Class<?> listenerType,
                              MethodDescriptor listenerMethodDescriptors[],
                              Method addListenerMethod,
                              Method removeListenerMethod)
            throws IntrospectionException{
        setName(eventSetName);
        this.listenerMethodDescriptors=(listenerMethodDescriptors!=null)
                ?listenerMethodDescriptors.clone()
                :null;
        setAddListenerMethod(addListenerMethod);
        setRemoveListenerMethod(removeListenerMethod);
        setListenerType(listenerType);
    }

    EventSetDescriptor(EventSetDescriptor x,EventSetDescriptor y){
        super(x,y);
        listenerMethodDescriptors=x.listenerMethodDescriptors;
        if(y.listenerMethodDescriptors!=null){
            listenerMethodDescriptors=y.listenerMethodDescriptors;
        }
        listenerTypeRef=x.listenerTypeRef;
        if(y.listenerTypeRef!=null){
            listenerTypeRef=y.listenerTypeRef;
        }
        addMethodDescriptor=x.addMethodDescriptor;
        if(y.addMethodDescriptor!=null){
            addMethodDescriptor=y.addMethodDescriptor;
        }
        removeMethodDescriptor=x.removeMethodDescriptor;
        if(y.removeMethodDescriptor!=null){
            removeMethodDescriptor=y.removeMethodDescriptor;
        }
        getMethodDescriptor=x.getMethodDescriptor;
        if(y.getMethodDescriptor!=null){
            getMethodDescriptor=y.getMethodDescriptor;
        }
        unicast=y.unicast;
        if(!x.inDefaultEventSet||!y.inDefaultEventSet){
            inDefaultEventSet=false;
        }
    }

    EventSetDescriptor(EventSetDescriptor old){
        super(old);
        if(old.listenerMethodDescriptors!=null){
            int len=old.listenerMethodDescriptors.length;
            listenerMethodDescriptors=new MethodDescriptor[len];
            for(int i=0;i<len;i++){
                listenerMethodDescriptors[i]=new MethodDescriptor(
                        old.listenerMethodDescriptors[i]);
            }
        }
        listenerTypeRef=old.listenerTypeRef;
        addMethodDescriptor=old.addMethodDescriptor;
        removeMethodDescriptor=old.removeMethodDescriptor;
        getMethodDescriptor=old.getMethodDescriptor;
        unicast=old.unicast;
        inDefaultEventSet=old.inDefaultEventSet;
    }

    public Class<?> getListenerType(){
        return (this.listenerTypeRef!=null)
                ?this.listenerTypeRef.get()
                :null;
    }

    private void setListenerType(Class<?> cls){
        this.listenerTypeRef=getWeakReference(cls);
    }

    public synchronized MethodDescriptor[] getListenerMethodDescriptors(){
        return (this.listenerMethodDescriptors!=null)
                ?this.listenerMethodDescriptors.clone()
                :null;
    }

    public synchronized Method getAddListenerMethod(){
        return getMethod(this.addMethodDescriptor);
    }

    private synchronized void setAddListenerMethod(Method method){
        if(method==null){
            return;
        }
        if(getClass0()==null){
            setClass0(method.getDeclaringClass());
        }
        addMethodDescriptor=new MethodDescriptor(method);
        setTransient(method.getAnnotation(Transient.class));
    }

    private static Method getMethod(MethodDescriptor descriptor){
        return (descriptor!=null)
                ?descriptor.getMethod()
                :null;
    }

    public synchronized Method getRemoveListenerMethod(){
        return getMethod(this.removeMethodDescriptor);
    }

    private synchronized void setRemoveListenerMethod(Method method){
        if(method==null){
            return;
        }
        if(getClass0()==null){
            setClass0(method.getDeclaringClass());
        }
        removeMethodDescriptor=new MethodDescriptor(method);
        setTransient(method.getAnnotation(Transient.class));
    }

    public synchronized Method getGetListenerMethod(){
        return getMethod(this.getMethodDescriptor);
    }

    private synchronized void setGetListenerMethod(Method method){
        if(method==null){
            return;
        }
        if(getClass0()==null){
            setClass0(method.getDeclaringClass());
        }
        getMethodDescriptor=new MethodDescriptor(method);
        setTransient(method.getAnnotation(Transient.class));
    }

    public boolean isUnicast(){
        return unicast;
    }

    public void setUnicast(boolean unicast){
        this.unicast=unicast;
    }

    public boolean isInDefaultEventSet(){
        return inDefaultEventSet;
    }

    public void setInDefaultEventSet(boolean inDefaultEventSet){
        this.inDefaultEventSet=inDefaultEventSet;
    }

    void appendTo(StringBuilder sb){
        appendTo(sb,"unicast",this.unicast);
        appendTo(sb,"inDefaultEventSet",this.inDefaultEventSet);
        appendTo(sb,"listenerType",this.listenerTypeRef);
        appendTo(sb,"getListenerMethod",getMethod(this.getMethodDescriptor));
        appendTo(sb,"addListenerMethod",getMethod(this.addMethodDescriptor));
        appendTo(sb,"removeListenerMethod",getMethod(this.removeMethodDescriptor));
    }
}
