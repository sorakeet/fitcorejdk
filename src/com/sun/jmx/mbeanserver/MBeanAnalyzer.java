/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.mbeanserver;

import javax.management.NotCompliantMBeanException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static com.sun.jmx.mbeanserver.Util.*;

class MBeanAnalyzer<M>{
    private Map<String,List<M>> opMap=newInsertionOrderMap();
    private Map<String,AttrMethods<M>> attrMap=newInsertionOrderMap();

    private MBeanAnalyzer(Class<?> mbeanType,
                          MBeanIntrospector<M> introspector)
            throws NotCompliantMBeanException{
        if(!mbeanType.isInterface()){
            throw new NotCompliantMBeanException("Not an interface: "+
                    mbeanType.getName());
        }else if(!Modifier.isPublic(mbeanType.getModifiers())&&
                !Introspector.ALLOW_NONPUBLIC_MBEAN){
            throw new NotCompliantMBeanException("Interface is not public: "+
                    mbeanType.getName());
        }
        try{
            initMaps(mbeanType,introspector);
        }catch(Exception x){
            throw Introspector.throwException(mbeanType,x);
        }
    }

    // Introspect the mbeanInterface and initialize this object's maps.
    //
    private void initMaps(Class<?> mbeanType,
                          MBeanIntrospector<M> introspector) throws Exception{
        final List<Method> methods1=introspector.getMethods(mbeanType);
        final List<Method> methods=eliminateCovariantMethods(methods1);
        /** Run through the methods to detect inconsistencies and to enable
         us to give getter and setter together to visitAttribute. */
        for(Method m : methods){
            final String name=m.getName();
            final int nParams=m.getParameterTypes().length;
            final M cm=introspector.mFrom(m);
            String attrName="";
            if(name.startsWith("get"))
                attrName=name.substring(3);
            else if(name.startsWith("is")
                    &&m.getReturnType()==boolean.class)
                attrName=name.substring(2);
            if(attrName.length()!=0&&nParams==0
                    &&m.getReturnType()!=void.class){
                // It's a getter
                // Check we don't have both isX and getX
                AttrMethods<M> am=attrMap.get(attrName);
                if(am==null)
                    am=new AttrMethods<M>();
                else{
                    if(am.getter!=null){
                        final String msg="Attribute "+attrName+
                                " has more than one getter";
                        throw new NotCompliantMBeanException(msg);
                    }
                }
                am.getter=cm;
                attrMap.put(attrName,am);
            }else if(name.startsWith("set")&&name.length()>3
                    &&nParams==1&&
                    m.getReturnType()==void.class){
                // It's a setter
                attrName=name.substring(3);
                AttrMethods<M> am=attrMap.get(attrName);
                if(am==null)
                    am=new AttrMethods<M>();
                else if(am.setter!=null){
                    final String msg="Attribute "+attrName+
                            " has more than one setter";
                    throw new NotCompliantMBeanException(msg);
                }
                am.setter=cm;
                attrMap.put(attrName,am);
            }else{
                // It's an operation
                List<M> cms=opMap.get(name);
                if(cms==null)
                    cms=newList();
                cms.add(cm);
                opMap.put(name,cms);
            }
        }
        /** Check that getters and setters are consistent. */
        for(Map.Entry<String,AttrMethods<M>> entry : attrMap.entrySet()){
            AttrMethods<M> am=entry.getValue();
            if(!introspector.consistent(am.getter,am.setter)){
                final String msg="Getter and setter for "+entry.getKey()+
                        " have inconsistent types";
                throw new NotCompliantMBeanException(msg);
            }
        }
    }

    static List<Method>
    eliminateCovariantMethods(List<Method> startMethods){
        // We are assuming that you never have very many methods with the
        // same name, so it is OK to use algorithms that are quadratic
        // in the number of methods with the same name.
        final int len=startMethods.size();
        final Method[] sorted=startMethods.toArray(new Method[len]);
        Arrays.sort(sorted,MethodOrder.instance);
        final Set<Method> overridden=newSet();
        for(int i=1;i<len;i++){
            final Method m0=sorted[i-1];
            final Method m1=sorted[i];
            // Methods that don't have the same name can't override each other
            if(!m0.getName().equals(m1.getName())) continue;
            // Methods that have the same name and same signature override
            // each other. In that case, the second method overrides the first,
            // due to the way we have sorted them in MethodOrder.
            if(Arrays.equals(m0.getParameterTypes(),
                    m1.getParameterTypes())){
                if(!overridden.add(m0))
                    throw new RuntimeException("Internal error: duplicate Method");
            }
        }
        final List<Method> methods=newList(startMethods);
        methods.removeAll(overridden);
        return methods;
    }

    // Currently it's two different but equivalent objects.  This only
    // really impacts proxy generation.  For MBean creation, the
    // cached PerInterface object for an MBean interface means that
    // an analyzer will not be recreated for a second MBean using the
    // same interface.
    static <M> MBeanAnalyzer<M> analyzer(Class<?> mbeanType,
                                         MBeanIntrospector<M> introspector)
            throws NotCompliantMBeanException{
        return new MBeanAnalyzer<M>(mbeanType,introspector);
    }

    void visit(MBeanVisitor<M> visitor){
        // visit attributes
        for(Map.Entry<String,AttrMethods<M>> entry : attrMap.entrySet()){
            String name=entry.getKey();
            AttrMethods<M> am=entry.getValue();
            visitor.visitAttribute(name,am.getter,am.setter);
        }
        // visit operations
        for(Map.Entry<String,List<M>> entry : opMap.entrySet()){
            for(M m : entry.getValue())
                visitor.visitOperation(entry.getKey(),m);
        }
    }

    static interface MBeanVisitor<M>{
        public void visitAttribute(String attributeName,
                                   M getter,
                                   M setter);

        public void visitOperation(String operationName,
                                   M operation);
    }

    private static class AttrMethods<M>{
        M getter;
        M setter;
    }

    private static class MethodOrder implements Comparator<Method>{
        public final static MethodOrder instance=new MethodOrder();

        public int compare(Method a,Method b){
            final int cmp=a.getName().compareTo(b.getName());
            if(cmp!=0) return cmp;
            final Class<?>[] aparams=a.getParameterTypes();
            final Class<?>[] bparams=b.getParameterTypes();
            if(aparams.length!=bparams.length)
                return aparams.length-bparams.length;
            if(!Arrays.equals(aparams,bparams)){
                return Arrays.toString(aparams).
                        compareTo(Arrays.toString(bparams));
            }
            final Class<?> aret=a.getReturnType();
            final Class<?> bret=b.getReturnType();
            if(aret==bret) return 0;
            // Super type comes first: Object, Number, Integer
            if(aret.isAssignableFrom(bret))
                return -1;
            return +1;      // could assert bret.isAssignableFrom(aret)
        }
    }
}
