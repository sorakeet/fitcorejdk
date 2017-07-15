/**
 * Copyright (c) 2003, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.presentation.rmi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class IDLTypesUtil{
    public static final int VALID_TYPE=0;
    public static final int INVALID_TYPE=1;
    public static final boolean FOLLOW_RMIC=true;
    private static final String GET_PROPERTY_PREFIX="get";
    private static final String SET_PROPERTY_PREFIX="set";
    private static final String IS_PROPERTY_PREFIX="is";

    public boolean isArray(Class c){
        boolean arrayType=false;
        if(c==null){
            throw new IllegalArgumentException();
        }
        if(c.isArray()){
            Class componentType=c.getComponentType();
            arrayType=
                    (isPrimitive(componentType)||isRemoteInterface(componentType)||
                            isEntity(componentType)||isException(componentType)||
                            isValue(componentType)||isObjectReference(componentType));
        }
        return arrayType;
    }

    public boolean isRemoteInterface(Class c){
        boolean remoteInterface=true;
        try{
            validateRemoteInterface(c);
        }catch(IDLTypeException ite){
            remoteInterface=false;
        }
        return remoteInterface;
    }

    public void validateRemoteInterface(Class c) throws IDLTypeException{
        if(c==null){
            throw new IllegalArgumentException();
        }
        if(!c.isInterface()){
            String msg="Class "+c+" must be a java interface.";
            throw new IDLTypeException(msg);
        }
        if(!java.rmi.Remote.class.isAssignableFrom(c)){
            String msg="Class "+c+" must extend java.rmi.Remote, "+
                    "either directly or indirectly.";
            throw new IDLTypeException(msg);
        }
        // Get all methods, including super-interface methods.
        Method[] methods=c.getMethods();
        for(int i=0;i<methods.length;i++){
            Method next=methods[i];
            validateExceptions(next);
        }
        // Removed because of bug 4989053
        // validateDirectInterfaces(c);
        validateConstants(c);
        return;
    }

    private void validateExceptions(Method method) throws IDLTypeException{
        Class[] exceptions=method.getExceptionTypes();
        boolean declaresRemoteExceptionOrSuperClass=false;
        // Section 1.2.3, #2
        for(int eIndex=0;eIndex<exceptions.length;eIndex++){
            Class exception=exceptions[eIndex];
            if(isRemoteExceptionOrSuperClass(exception)){
                declaresRemoteExceptionOrSuperClass=true;
                break;
            }
        }
        if(!declaresRemoteExceptionOrSuperClass){
            String msg="Method '"+method+"' must throw at least one "+
                    "exception of type java.rmi.RemoteException or one of its "+
                    "super-classes";
            throw new IDLTypeException(msg);
        }
        // Section 1.2.3, #4
        // See also bug 4972402
        // For all exceptions E in exceptions,
        // (isCheckedException(E) => (isValue(E) || RemoteException.isAssignableFrom( E ) )
        for(int eIndex=0;eIndex<exceptions.length;eIndex++){
            Class exception=exceptions[eIndex];
            if(isCheckedException(exception)&&!isValue(exception)&&
                    !isRemoteException(exception)){
                String msg="Exception '"+exception+"' on method '"+
                        method+"' is not a allowed RMI/IIOP exception type";
                throw new IDLTypeException(msg);
            }
        }
        return;
    }

    public boolean isValue(Class c){
        if(c==null){
            throw new IllegalArgumentException();
        }
        return
                (!c.isInterface()&&
                        java.io.Serializable.class.isAssignableFrom(c)&&
                        !java.rmi.Remote.class.isAssignableFrom(c));
    }

    public boolean isRemoteException(Class c){
        if(c==null){
            throw new IllegalArgumentException();
        }
        return java.rmi.RemoteException.class.isAssignableFrom(c);
    }

    public boolean isCheckedException(Class c){
        if(c==null){
            throw new IllegalArgumentException();
        }
        return Throwable.class.isAssignableFrom(c)&&
                !RuntimeException.class.isAssignableFrom(c)&&
                !Error.class.isAssignableFrom(c);
    }

    private boolean isRemoteExceptionOrSuperClass(Class c){
        return
                ((c==java.rmi.RemoteException.class)||
                        (c==java.io.IOException.class)||
                        (c==Exception.class)||
                        (c==Throwable.class));
    }

    private void validateConstants(final Class c)
            throws IDLTypeException{
        Field[] fields=null;
        try{
            fields=(Field[])
                    java.security.AccessController.doPrivileged
                            (new java.security.PrivilegedExceptionAction(){
                                public Object run() throws Exception{
                                    return c.getFields();
                                }
                            });
        }catch(java.security.PrivilegedActionException pae){
            IDLTypeException ite=new IDLTypeException();
            ite.initCause(pae);
            throw ite;
        }
        for(int i=0;i<fields.length;i++){
            Field next=fields[i];
            Class fieldType=next.getType();
            if((fieldType!=String.class)&&
                    !isPrimitive(fieldType)){
                String msg="Constant field '"+next.getName()+
                        "' in class '"+next.getDeclaringClass().getName()+
                        "' has invalid type' "+next.getType()+"'. Constants"+
                        " in RMI/IIOP interfaces can only have primitive"+
                        " types and java.lang.String types.";
                throw new IDLTypeException(msg);
            }
        }
        return;
    }

    public boolean isPrimitive(Class c){
        if(c==null){
            throw new IllegalArgumentException();
        }
        return c.isPrimitive();
    }

    public boolean isException(Class c){
        if(c==null){
            throw new IllegalArgumentException();
        }
        // Must be a checked exception, not including RemoteException or
        // its subclasses.
        return isCheckedException(c)&&!isRemoteException(c)&&isValue(c);
    }

    public boolean isObjectReference(Class c){
        if(c==null){
            throw new IllegalArgumentException();
        }
        return (c.isInterface()&&
                org.omg.CORBA.Object.class.isAssignableFrom(c));
    }

    public boolean isEntity(Class c){
        if(c==null){
            throw new IllegalArgumentException();
        }
        Class superClass=c.getSuperclass();
        return (!c.isInterface()&&
                (superClass!=null)&&
                (org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c)));
    }

    public boolean isPropertyAccessorMethod(Method m,Class c){
        String methodName=m.getName();
        Class returnType=m.getReturnType();
        Class[] parameters=m.getParameterTypes();
        Class[] exceptionTypes=m.getExceptionTypes();
        String propertyType=null;
        if(methodName.startsWith(GET_PROPERTY_PREFIX)){
            if((parameters.length==0)&&(returnType!=Void.TYPE)&&
                    !readHasCorrespondingIsProperty(m,c)){
                propertyType=GET_PROPERTY_PREFIX;
            }
        }else if(methodName.startsWith(SET_PROPERTY_PREFIX)){
            if((returnType==Void.TYPE)&&(parameters.length==1)){
                if(hasCorrespondingReadProperty(m,c,GET_PROPERTY_PREFIX)||
                        hasCorrespondingReadProperty(m,c,IS_PROPERTY_PREFIX)){
                    propertyType=SET_PROPERTY_PREFIX;
                }
            }
        }else if(methodName.startsWith(IS_PROPERTY_PREFIX)){
            if((parameters.length==0)&&(returnType==Boolean.TYPE)&&
                    !isHasCorrespondingReadProperty(m,c)){
                propertyType=IS_PROPERTY_PREFIX;
            }
        }
        // Some final checks that apply to all properties.
        if(propertyType!=null){
            if(!validPropertyExceptions(m)||
                    (methodName.length()<=propertyType.length())){
                propertyType=null;
            }
        }
        return (propertyType!=null);
    }

    private boolean hasCorrespondingReadProperty
            (Method writeProperty,Class c,String readPropertyPrefix){
        String writePropertyMethodName=writeProperty.getName();
        Class[] writePropertyParameters=writeProperty.getParameterTypes();
        boolean foundReadProperty=false;
        try{
            // Look for a valid corresponding Read property
            String readPropertyMethodName=
                    writePropertyMethodName.replaceFirst
                            (SET_PROPERTY_PREFIX,readPropertyPrefix);
            Method readPropertyMethod=c.getMethod(readPropertyMethodName,
                    new Class[]{});
            foundReadProperty=
                    (isPropertyAccessorMethod(readPropertyMethod,c)&&
                            (readPropertyMethod.getReturnType()==
                                    writePropertyParameters[0]));
        }catch(Exception e){
            // ignore. this means we didn't find a corresponding get property.
        }
        return foundReadProperty;
    }

    private boolean readHasCorrespondingIsProperty(Method readProperty,
                                                   Class c){
        if(FOLLOW_RMIC)
            return false;
        String readPropertyMethodName=readProperty.getName();
        boolean foundIsProperty=false;
        try{
            // Look for a valid corresponding Is property
            String isPropertyMethodName=
                    readPropertyMethodName.replaceFirst(GET_PROPERTY_PREFIX,
                            IS_PROPERTY_PREFIX);
            Method isPropertyMethod=c.getMethod(isPropertyMethodName,
                    new Class[]{});
            foundIsProperty=isPropertyAccessorMethod(isPropertyMethod,
                    c);
        }catch(Exception e){
            // ignore. this means we didn't find a corresponding Is property.
        }
        return foundIsProperty;
    }

    private boolean isHasCorrespondingReadProperty(Method readProperty,
                                                   Class c){
        if(!FOLLOW_RMIC)
            return false;
        String readPropertyMethodName=readProperty.getName();
        boolean foundIsProperty=false;
        try{
            // Look for a valid corresponding Read property
            String isPropertyMethodName=
                    readPropertyMethodName.replaceFirst(IS_PROPERTY_PREFIX,
                            GET_PROPERTY_PREFIX);
            Method isPropertyMethod=c.getMethod(isPropertyMethodName,
                    new Class[]{});
            foundIsProperty=isPropertyAccessorMethod(isPropertyMethod,
                    c);
        }catch(Exception e){
            // ignore. this means we didn't find a corresponding read property.
        }
        return foundIsProperty;
    }

    public String getAttributeNameForProperty(String propertyName){
        String attributeName=null;
        String prefix=null;
        if(propertyName.startsWith(GET_PROPERTY_PREFIX)){
            prefix=GET_PROPERTY_PREFIX;
        }else if(propertyName.startsWith(SET_PROPERTY_PREFIX)){
            prefix=SET_PROPERTY_PREFIX;
        }else if(propertyName.startsWith(IS_PROPERTY_PREFIX)){
            prefix=IS_PROPERTY_PREFIX;
        }
        if((prefix!=null)&&(prefix.length()<propertyName.length())){
            String remainder=propertyName.substring(prefix.length());
            if((remainder.length()>=2)&&
                    Character.isUpperCase(remainder.charAt(0))&&
                    Character.isUpperCase(remainder.charAt(1))){
                // don't set the first letter to lower-case if the
                // first two are upper-case
                attributeName=remainder;
            }else{
                attributeName=Character.toLowerCase(remainder.charAt(0))+
                        remainder.substring(1);
            }
        }
        return attributeName;
    }

    public IDLType getPrimitiveIDLTypeMapping(Class c){
        if(c==null){
            throw new IllegalArgumentException();
        }
        if(c.isPrimitive()){
            if(c==Void.TYPE){
                return new IDLType(c,"void");
            }else if(c==Boolean.TYPE){
                return new IDLType(c,"boolean");
            }else if(c==Character.TYPE){
                return new IDLType(c,"wchar");
            }else if(c==Byte.TYPE){
                return new IDLType(c,"octet");
            }else if(c==Short.TYPE){
                return new IDLType(c,"short");
            }else if(c==Integer.TYPE){
                return new IDLType(c,"long");
            }else if(c==Long.TYPE){
                return new IDLType(c,"long_long");
            }else if(c==Float.TYPE){
                return new IDLType(c,"float");
            }else if(c==Double.TYPE){
                return new IDLType(c,"double");
            }
        }
        return null;
    }

    public IDLType getSpecialCaseIDLTypeMapping(Class c){
        if(c==null){
            throw new IllegalArgumentException();
        }
        if(c==Object.class){
            return new IDLType(c,new String[]{"java","lang"},
                    "Object");
        }else if(c==String.class){
            return new IDLType(c,new String[]{"CORBA"},
                    "WStringValue");
        }else if(c==Class.class){
            return new IDLType(c,new String[]{"javax","rmi","CORBA"},
                    "ClassDesc");
        }else if(c==java.io.Serializable.class){
            return new IDLType(c,new String[]{"java","io"},
                    "Serializable");
        }else if(c==java.io.Externalizable.class){
            return new IDLType(c,new String[]{"java","io"},
                    "Externalizable");
        }else if(c==java.rmi.Remote.class){
            return new IDLType(c,new String[]{"java","rmi"},
                    "Remote");
        }else if(c==org.omg.CORBA.Object.class){
            return new IDLType(c,"Object");
        }else{
            return null;
        }
    }

    private boolean validPropertyExceptions(Method method){
        Class[] exceptions=method.getExceptionTypes();
        for(int eIndex=0;eIndex<exceptions.length;eIndex++){
            Class exception=exceptions[eIndex];
            if(isCheckedException(exception)&&!isRemoteException(exception))
                return false;
        }
        return true;
    }

    private void validateDirectInterfaces(Class c) throws IDLTypeException{
        Class[] directInterfaces=c.getInterfaces();
        if(directInterfaces.length<2){
            return;
        }
        Set allMethodNames=new HashSet();
        Set currentMethodNames=new HashSet();
        for(int i=0;i<directInterfaces.length;i++){
            Class next=directInterfaces[i];
            Method[] methods=next.getMethods();
            // Comparison is based on method names only.  First collect
            // all methods from current interface, eliminating duplicate
            // names.
            currentMethodNames.clear();
            for(int m=0;m<methods.length;m++){
                currentMethodNames.add(methods[m].getName());
            }
            // Now check each method against list of all unique method
            // names processed so far.
            for(Iterator iter=currentMethodNames.iterator();iter.hasNext();){
                String methodName=(String)iter.next();
                if(allMethodNames.contains(methodName)){
                    String msg="Class "+c+" inherits method "+
                            methodName+" from multiple direct interfaces.";
                    throw new IDLTypeException(msg);
                }else{
                    allMethodNames.add(methodName);
                }
            }
        }
        return;
    }
}
