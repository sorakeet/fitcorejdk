/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

import com.sun.jmx.mbeanserver.Introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class MBeanOperationInfo extends MBeanFeatureInfo implements Cloneable{
    public static final int INFO=0;
    public static final int ACTION=1;
    public static final int ACTION_INFO=2;
    public static final int UNKNOWN=3;
    static final long serialVersionUID=-6178860474881375330L;
    static final MBeanOperationInfo[] NO_OPERATIONS=
            new MBeanOperationInfo[0];
    private final String type;
    private final MBeanParameterInfo[] signature;
    private final int impact;
    private final transient boolean arrayGettersSafe;

    public MBeanOperationInfo(String description,Method method){
        this(method.getName(),
                description,
                methodSignature(method),
                method.getReturnType().getName(),
                UNKNOWN,
                Introspector.descriptorForElement(method));
    }

    public MBeanOperationInfo(String name,
                              String description,
                              MBeanParameterInfo[] signature,
                              String type,
                              int impact,
                              Descriptor descriptor){
        super(name,description,descriptor);
        if(signature==null||signature.length==0)
            signature=MBeanParameterInfo.NO_PARAMS;
        else
            signature=signature.clone();
        this.signature=signature;
        this.type=type;
        this.impact=impact;
        this.arrayGettersSafe=
                MBeanInfo.arrayGettersSafe(this.getClass(),
                        MBeanOperationInfo.class);
    }

    private static MBeanParameterInfo[] methodSignature(Method method){
        final Class<?>[] classes=method.getParameterTypes();
        final Annotation[][] annots=method.getParameterAnnotations();
        return parameters(classes,annots);
    }

    static MBeanParameterInfo[] parameters(Class<?>[] classes,
                                           Annotation[][] annots){
        final MBeanParameterInfo[] params=
                new MBeanParameterInfo[classes.length];
        assert (classes.length==annots.length);
        for(int i=0;i<classes.length;i++){
            Descriptor d=Introspector.descriptorForAnnotations(annots[i]);
            final String pn="p"+(i+1);
            params[i]=
                    new MBeanParameterInfo(pn,classes[i].getName(),"",d);
        }
        return params;
    }

    public MBeanOperationInfo(String name,
                              String description,
                              MBeanParameterInfo[] signature,
                              String type,
                              int impact){
        this(name,description,signature,type,impact,(Descriptor)null);
    }

    @Override
    public Object clone(){
        try{
            return super.clone();
        }catch(CloneNotSupportedException e){
            // should not happen as this class is cloneable
            return null;
        }
    }

    @Override
    public String toString(){
        String impactString;
        switch(getImpact()){
            case ACTION:
                impactString="action";
                break;
            case ACTION_INFO:
                impactString="action/info";
                break;
            case INFO:
                impactString="info";
                break;
            case UNKNOWN:
                impactString="unknown";
                break;
            default:
                impactString="("+getImpact()+")";
        }
        return getClass().getName()+"["+
                "description="+getDescription()+", "+
                "name="+getName()+", "+
                "returnType="+getReturnType()+", "+
                "signature="+Arrays.asList(fastGetSignature())+", "+
                "impact="+impactString+", "+
                "descriptor="+getDescriptor()+
                "]";
    }

    public String getReturnType(){
        return type;
    }

    private MBeanParameterInfo[] fastGetSignature(){
        if(arrayGettersSafe){
            // if signature is null simply return an empty array .
            // see getSignature() above.
            //
            if(signature==null)
                return MBeanParameterInfo.NO_PARAMS;
            else return signature;
        }else return getSignature();
    }

    public MBeanParameterInfo[] getSignature(){
        // If MBeanOperationInfo was created in our implementation,
        // signature cannot be null - because our constructors replace
        // null with MBeanParameterInfo.NO_PARAMS;
        //
        // However, signature could be null if an  MBeanOperationInfo is
        // deserialized from a byte array produced by another implementation.
        // This is not very likely but possible, since the serial form says
        // nothing against it. (see 6373150)
        //
        if(signature==null)
            // if signature is null simply return an empty array .
            //
            return MBeanParameterInfo.NO_PARAMS;
        else if(signature.length==0)
            return signature;
        else
            return signature.clone();
    }

    public int getImpact(){
        return impact;
    }

    @Override
    public boolean equals(Object o){
        if(o==this)
            return true;
        if(!(o instanceof MBeanOperationInfo))
            return false;
        MBeanOperationInfo p=(MBeanOperationInfo)o;
        return (Objects.equals(p.getName(),getName())&&
                Objects.equals(p.getReturnType(),getReturnType())&&
                Objects.equals(p.getDescription(),getDescription())&&
                p.getImpact()==getImpact()&&
                Arrays.equals(p.fastGetSignature(),fastGetSignature())&&
                Objects.equals(p.getDescriptor(),getDescriptor()));
    }

    @Override
    public int hashCode(){
        return Objects.hash(getName(),getReturnType());
    }
}
