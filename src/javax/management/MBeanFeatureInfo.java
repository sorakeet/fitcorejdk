/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

import java.io.*;
import java.util.Objects;

public class MBeanFeatureInfo implements Serializable, DescriptorRead{
    static final long serialVersionUID=3952882688968447265L;
    protected String name;
    protected String description;
    private transient Descriptor descriptor;

    public MBeanFeatureInfo(String name,String description){
        this(name,description,null);
    }

    public MBeanFeatureInfo(String name,String description,
                            Descriptor descriptor){
        this.name=name;
        this.description=description;
        this.descriptor=descriptor;
    }

    public int hashCode(){
        return getName().hashCode()^getDescription().hashCode()^
                getDescriptor().hashCode();
    }

    public boolean equals(Object o){
        if(o==this)
            return true;
        if(!(o instanceof MBeanFeatureInfo))
            return false;
        MBeanFeatureInfo p=(MBeanFeatureInfo)o;
        return (Objects.equals(p.getName(),getName())&&
                Objects.equals(p.getDescription(),getDescription())&&
                Objects.equals(p.getDescriptor(),getDescriptor()));
    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    public Descriptor getDescriptor(){
        return (Descriptor)ImmutableDescriptor.nonNullDescriptor(descriptor).clone();
    }

    private void writeObject(ObjectOutputStream out) throws IOException{
        out.defaultWriteObject();
        if(descriptor!=null&&
                descriptor.getClass()==ImmutableDescriptor.class){
            out.write(1);
            final String[] names=descriptor.getFieldNames();
            out.writeObject(names);
            out.writeObject(descriptor.getFieldValues(names));
        }else{
            out.write(0);
            out.writeObject(descriptor);
        }
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        in.defaultReadObject();
        switch(in.read()){
            case 1:
                final String[] names=(String[])in.readObject();
                final Object[] values=(Object[])in.readObject();
                descriptor=(names.length==0)?
                        ImmutableDescriptor.EMPTY_DESCRIPTOR:
                        new ImmutableDescriptor(names,values);
                break;
            case 0:
                descriptor=(Descriptor)in.readObject();
                if(descriptor==null){
                    descriptor=ImmutableDescriptor.EMPTY_DESCRIPTOR;
                }
                break;
            case -1: // from an earlier version of the JMX API
                descriptor=ImmutableDescriptor.EMPTY_DESCRIPTOR;
                break;
            default:
                throw new StreamCorruptedException("Got unexpected byte.");
        }
    }
}
