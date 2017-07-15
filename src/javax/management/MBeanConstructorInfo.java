/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

import com.sun.jmx.mbeanserver.Introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Objects;

public class MBeanConstructorInfo extends MBeanFeatureInfo implements Cloneable{
    static final long serialVersionUID=4433990064191844427L;
    static final MBeanConstructorInfo[] NO_CONSTRUCTORS=
            new MBeanConstructorInfo[0];
    private final transient boolean arrayGettersSafe;
    private final MBeanParameterInfo[] signature;

    public MBeanConstructorInfo(String description,Constructor<?> constructor){
        this(constructor.getName(),description,
                constructorSignature(constructor),
                Introspector.descriptorForElement(constructor));
    }

    public MBeanConstructorInfo(String name,
                                String description,
                                MBeanParameterInfo[] signature,
                                Descriptor descriptor){
        super(name,description,descriptor);
        if(signature==null||signature.length==0)
            signature=MBeanParameterInfo.NO_PARAMS;
        else
            signature=signature.clone();
        this.signature=signature;
        this.arrayGettersSafe=
                MBeanInfo.arrayGettersSafe(this.getClass(),
                        MBeanConstructorInfo.class);
    }

    private static MBeanParameterInfo[] constructorSignature(Constructor<?> cn){
        final Class<?>[] classes=cn.getParameterTypes();
        final Annotation[][] annots=cn.getParameterAnnotations();
        return MBeanOperationInfo.parameters(classes,annots);
    }

    public MBeanConstructorInfo(String name,
                                String description,
                                MBeanParameterInfo[] signature){
        this(name,description,signature,null);
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
        return
                getClass().getName()+"["+
                        "description="+getDescription()+", "+
                        "name="+getName()+", "+
                        "signature="+Arrays.asList(fastGetSignature())+", "+
                        "descriptor="+getDescriptor()+
                        "]";
    }

    private MBeanParameterInfo[] fastGetSignature(){
        if(arrayGettersSafe)
            return signature;
        else
            return getSignature();
    }

    public MBeanParameterInfo[] getSignature(){
        if(signature.length==0)
            return signature;
        else
            return signature.clone();
    }

    public boolean equals(Object o){
        if(o==this)
            return true;
        if(!(o instanceof MBeanConstructorInfo))
            return false;
        MBeanConstructorInfo p=(MBeanConstructorInfo)o;
        return (Objects.equals(p.getName(),getName())&&
                Objects.equals(p.getDescription(),getDescription())&&
                Arrays.equals(p.fastGetSignature(),fastGetSignature())&&
                Objects.equals(p.getDescriptor(),getDescriptor()));
    }

    public int hashCode(){
        return Objects.hash(getName())^Arrays.hashCode(fastGetSignature());
    }
}
