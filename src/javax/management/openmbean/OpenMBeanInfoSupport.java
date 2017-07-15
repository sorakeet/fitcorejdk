/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.openmbean;
// java import
//

import javax.management.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class OpenMBeanInfoSupport
        extends MBeanInfo
        implements OpenMBeanInfo{
    static final long serialVersionUID=4349395935420511492L;
    // As this instance is immutable, these two values
    // need only be calculated once.
    private transient Integer myHashCode=null;
    private transient String myToString=null;

    public OpenMBeanInfoSupport(String className,
                                String description,
                                OpenMBeanAttributeInfo[] openAttributes,
                                OpenMBeanConstructorInfo[] openConstructors,
                                OpenMBeanOperationInfo[] openOperations,
                                MBeanNotificationInfo[] notifications){
        this(className,description,
                openAttributes,openConstructors,openOperations,notifications,
                (Descriptor)null);
    }

    public OpenMBeanInfoSupport(String className,
                                String description,
                                OpenMBeanAttributeInfo[] openAttributes,
                                OpenMBeanConstructorInfo[] openConstructors,
                                OpenMBeanOperationInfo[] openOperations,
                                MBeanNotificationInfo[] notifications,
                                Descriptor descriptor){
        super(className,
                description,
                attributeArray(openAttributes),
                constructorArray(openConstructors),
                operationArray(openOperations),
                (notifications==null)?null:notifications.clone(),
                descriptor);
    }

    private static MBeanAttributeInfo[]
    attributeArray(OpenMBeanAttributeInfo[] src){
        if(src==null)
            return null;
        MBeanAttributeInfo[] dst=new MBeanAttributeInfo[src.length];
        System.arraycopy(src,0,dst,0,src.length);
        // may throw an ArrayStoreException
        return dst;
    }

    private static MBeanConstructorInfo[]
    constructorArray(OpenMBeanConstructorInfo[] src){
        if(src==null)
            return null;
        MBeanConstructorInfo[] dst=new MBeanConstructorInfo[src.length];
        System.arraycopy(src,0,dst,0,src.length);
        // may throw an ArrayStoreException
        return dst;
    }

    private static MBeanOperationInfo[]
    operationArray(OpenMBeanOperationInfo[] src){
        if(src==null)
            return null;
        MBeanOperationInfo[] dst=new MBeanOperationInfo[src.length];
        System.arraycopy(src,0,dst,0,src.length);
        return dst;
    }

    public String toString(){
        // Calculate the string value if it has not yet been done (ie
        // 1st call to toString())
        //
        if(myToString==null){
            myToString=new StringBuilder()
                    .append(this.getClass().getName())
                    .append("(mbean_class_name=")
                    .append(this.getClassName())
                    .append(",attributes=")
                    .append(Arrays.asList(this.getAttributes()).toString())
                    .append(",constructors=")
                    .append(Arrays.asList(this.getConstructors()).toString())
                    .append(",operations=")
                    .append(Arrays.asList(this.getOperations()).toString())
                    .append(",notifications=")
                    .append(Arrays.asList(this.getNotifications()).toString())
                    .append(",descriptor=")
                    .append(this.getDescriptor())
                    .append(")")
                    .toString();
        }
        // return always the same string representation for this
        // instance (immutable)
        //
        return myToString;
    }

    public boolean equals(Object obj){
        // if obj is null, return false
        //
        if(obj==null){
            return false;
        }
        // if obj is not a OpenMBeanInfo, return false
        //
        OpenMBeanInfo other;
        try{
            other=(OpenMBeanInfo)obj;
        }catch(ClassCastException e){
            return false;
        }
        // Now, really test for equality between this OpenMBeanInfo
        // implementation and the other:
        //
        // their MBean className should be equal
        if(!Objects.equals(this.getClassName(),other.getClassName())){
            return false;
        }
        // their infos on attributes should be equal (order not
        // significant => equality between sets, not arrays or lists)
        if(!sameArrayContents(this.getAttributes(),other.getAttributes()))
            return false;
        // their infos on constructors should be equal (order not
        // significant => equality between sets, not arrays or lists)
        if(!sameArrayContents(this.getConstructors(),other.getConstructors()))
            return false;
        // their infos on operations should be equal (order not
        // significant => equality between sets, not arrays or lists)
        if(!sameArrayContents(this.getOperations(),other.getOperations()))
            return false;
        // their infos on notifications should be equal (order not
        // significant => equality between sets, not arrays or lists)
        if(!sameArrayContents(this.getNotifications(),other.getNotifications()))
            return false;
        // All tests for equality were successful
        //
        return true;
    }

    private static <T> boolean sameArrayContents(T[] a1,T[] a2){
        return (new HashSet<T>(Arrays.asList(a1))
                .equals(new HashSet<T>(Arrays.asList(a2))));
    }

    public int hashCode(){
        // Calculate the hash code value if it has not yet been done
        // (ie 1st call to hashCode())
        //
        if(myHashCode==null){
            int value=0;
            if(this.getClassName()!=null){
                value+=this.getClassName().hashCode();
            }
            value+=arraySetHash(this.getAttributes());
            value+=arraySetHash(this.getConstructors());
            value+=arraySetHash(this.getOperations());
            value+=arraySetHash(this.getNotifications());
            myHashCode=Integer.valueOf(value);
        }
        // return always the same hash code for this instance (immutable)
        //
        return myHashCode.intValue();
    }

    private static <T> int arraySetHash(T[] a){
        return new HashSet<T>(Arrays.asList(a)).hashCode();
    }
}
