/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

import java.util.Objects;

public class MBeanParameterInfo extends MBeanFeatureInfo implements Cloneable{
    static final long serialVersionUID=7432616882776782338L;
    static final MBeanParameterInfo[] NO_PARAMS=new MBeanParameterInfo[0];
    private final String type;

    public MBeanParameterInfo(String name,
                              String type,
                              String description){
        this(name,type,description,(Descriptor)null);
    }

    public MBeanParameterInfo(String name,
                              String type,
                              String description,
                              Descriptor descriptor){
        super(name,description,descriptor);
        this.type=type;
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
                        "type="+getType()+", "+
                        "descriptor="+getDescriptor()+
                        "]";
    }

    public String getType(){
        return type;
    }

    public boolean equals(Object o){
        if(o==this)
            return true;
        if(!(o instanceof MBeanParameterInfo))
            return false;
        MBeanParameterInfo p=(MBeanParameterInfo)o;
        return (Objects.equals(p.getName(),getName())&&
                Objects.equals(p.getType(),getType())&&
                Objects.equals(p.getDescription(),getDescription())&&
                Objects.equals(p.getDescriptor(),getDescriptor()));
    }

    public int hashCode(){
        return Objects.hash(getName(),getType());
    }
}
