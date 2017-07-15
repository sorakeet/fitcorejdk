/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

import com.sun.jmx.mbeanserver.GetPropertyAction;
import com.sun.jmx.mbeanserver.Introspector;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.util.Objects;

@SuppressWarnings("serial")  // serialVersionUID not constant
public class MBeanAttributeInfo extends MBeanFeatureInfo implements Cloneable{
    static final MBeanAttributeInfo[] NO_ATTRIBUTES=
            new MBeanAttributeInfo[0];
    private static final long serialVersionUID;

    static{
        /** For complicated reasons, the serialVersionUID changed
         between JMX 1.0 and JMX 1.1, even though JMX 1.1 did not
         have compatibility code for this class.  So the
         serialization produced by this class with JMX 1.2 and
         jmx.serial.form=1.0 is not the same as that produced by
         this class with JMX 1.1 and jmx.serial.form=1.0.  However,
         the serialization without that property is the same, and
         that is the only form required by JMX 1.2.
         */
        long uid=8644704819898565848L;
        try{
            GetPropertyAction act=new GetPropertyAction("jmx.serial.form");
            String form=AccessController.doPrivileged(act);
            if("1.0".equals(form))
                uid=7043855487133450673L;
        }catch(Exception e){
            // OK: exception means no compat with 1.0, too bad
        }
        serialVersionUID=uid;
    }

    private final String attributeType;
    private final boolean isWrite;
    private final boolean isRead;
    private final boolean is;

    public MBeanAttributeInfo(String name,
                              String type,
                              String description,
                              boolean isReadable,
                              boolean isWritable,
                              boolean isIs){
        this(name,type,description,isReadable,isWritable,isIs,
                (Descriptor)null);
    }

    public MBeanAttributeInfo(String name,
                              String type,
                              String description,
                              boolean isReadable,
                              boolean isWritable,
                              boolean isIs,
                              Descriptor descriptor){
        super(name,description,descriptor);
        this.attributeType=type;
        this.isRead=isReadable;
        this.isWrite=isWritable;
        if(isIs&&!isReadable){
            throw new IllegalArgumentException("Cannot have an \"is\" getter "+
                    "for a non-readable attribute");
        }
        if(isIs&&!type.equals("java.lang.Boolean")&&
                !type.equals("boolean")){
            throw new IllegalArgumentException("Cannot have an \"is\" getter "+
                    "for a non-boolean attribute");
        }
        this.is=isIs;
    }

    public MBeanAttributeInfo(String name,
                              String description,
                              Method getter,
                              Method setter) throws IntrospectionException{
        this(name,
                attributeType(getter,setter),
                description,
                (getter!=null),
                (setter!=null),
                isIs(getter),
                ImmutableDescriptor.union(Introspector.descriptorForElement(getter),
                        Introspector.descriptorForElement(setter)));
    }

    private static boolean isIs(Method getter){
        return (getter!=null&&
                getter.getName().startsWith("is")&&
                (getter.getReturnType().equals(Boolean.TYPE)||
                        getter.getReturnType().equals(Boolean.class)));
    }

    private static String attributeType(Method getter,Method setter)
            throws IntrospectionException{
        Class<?> type=null;
        if(getter!=null){
            if(getter.getParameterTypes().length!=0){
                throw new IntrospectionException("bad getter arg count");
            }
            type=getter.getReturnType();
            if(type==Void.TYPE){
                throw new IntrospectionException("getter "+getter.getName()+
                        " returns void");
            }
        }
        if(setter!=null){
            Class<?> params[]=setter.getParameterTypes();
            if(params.length!=1){
                throw new IntrospectionException("bad setter arg count");
            }
            if(type==null)
                type=params[0];
            else if(type!=params[0]){
                throw new IntrospectionException("type mismatch between "+
                        "getter and setter");
            }
        }
        if(type==null){
            throw new IntrospectionException("getter and setter cannot "+
                    "both be null");
        }
        return type.getName();
    }

    public Object clone(){
        try{
            return super.clone();
        }catch(CloneNotSupportedException e){
            // should not happen as this class is cloneable
            return null;
        }
    }

    public String toString(){
        String access;
        if(isReadable()){
            if(isWritable())
                access="read/write";
            else
                access="read-only";
        }else if(isWritable())
            access="write-only";
        else
            access="no-access";
        return
                getClass().getName()+"["+
                        "description="+getDescription()+", "+
                        "name="+getName()+", "+
                        "type="+getType()+", "+
                        access+", "+
                        (isIs()?"isIs, ":"")+
                        "descriptor="+getDescriptor()+
                        "]";
    }

    public String getType(){
        return attributeType;
    }

    public boolean isReadable(){
        return isRead;
    }

    public boolean isWritable(){
        return isWrite;
    }

    public boolean isIs(){
        return is;
    }

    public boolean equals(Object o){
        if(o==this)
            return true;
        if(!(o instanceof MBeanAttributeInfo))
            return false;
        MBeanAttributeInfo p=(MBeanAttributeInfo)o;
        return (Objects.equals(p.getName(),getName())&&
                Objects.equals(p.getType(),getType())&&
                Objects.equals(p.getDescription(),getDescription())&&
                Objects.equals(p.getDescriptor(),getDescriptor())&&
                p.isReadable()==isReadable()&&
                p.isWritable()==isWritable()&&
                p.isIs()==isIs());
    }

    public int hashCode(){
        return Objects.hash(getName(),getType());
    }
}
