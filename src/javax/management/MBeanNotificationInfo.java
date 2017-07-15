/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Objects;

public class MBeanNotificationInfo extends MBeanFeatureInfo implements Cloneable{
    static final long serialVersionUID=-3888371564530107064L;
    static final MBeanNotificationInfo[] NO_NOTIFICATIONS=
            new MBeanNotificationInfo[0];
    private static final String[] NO_TYPES=new String[0];
    private final transient boolean arrayGettersSafe;
    private String[] types;

    public MBeanNotificationInfo(String[] notifTypes,
                                 String name,
                                 String description){
        this(notifTypes,name,description,null);
    }

    public MBeanNotificationInfo(String[] notifTypes,
                                 String name,
                                 String description,
                                 Descriptor descriptor){
        super(name,description,descriptor);
        /** We do not validate the notifTypes, since the spec just says
         they are dot-separated, not that they must look like Java
         classes.  E.g. the spec doesn't forbid "sun.prob.25" as a
         notifType, though it doesn't explicitly allow it
         either.  */
        this.types=(notifTypes!=null&&notifTypes.length>0)?
                notifTypes.clone():NO_TYPES;
        this.arrayGettersSafe=
                MBeanInfo.arrayGettersSafe(this.getClass(),
                        MBeanNotificationInfo.class);
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
                        "notifTypes="+Arrays.asList(fastGetNotifTypes())+", "+
                        "descriptor="+getDescriptor()+
                        "]";
    }

    private String[] fastGetNotifTypes(){
        if(arrayGettersSafe)
            return types;
        else
            return getNotifTypes();
    }

    public String[] getNotifTypes(){
        if(types.length==0)
            return NO_TYPES;
        else
            return types.clone();
    }

    public boolean equals(Object o){
        if(o==this)
            return true;
        if(!(o instanceof MBeanNotificationInfo))
            return false;
        MBeanNotificationInfo p=(MBeanNotificationInfo)o;
        return (Objects.equals(p.getName(),getName())&&
                Objects.equals(p.getDescription(),getDescription())&&
                Objects.equals(p.getDescriptor(),getDescriptor())&&
                Arrays.equals(p.fastGetNotifTypes(),fastGetNotifTypes()));
    }

    public int hashCode(){
        int hash=getName().hashCode();
        for(int i=0;i<types.length;i++)
            hash^=types[i].hashCode();
        return hash;
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException{
        ObjectInputStream.GetField gf=ois.readFields();
        String[] t=(String[])gf.get("types",null);
        types=(t!=null&&t.length!=0)?t.clone():NO_TYPES;
    }
}
