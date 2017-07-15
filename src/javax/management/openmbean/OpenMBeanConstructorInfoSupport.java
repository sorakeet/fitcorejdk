/**
 * Copyright (c) 2000, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.openmbean;
// java import
//

import javax.management.Descriptor;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanParameterInfo;
import java.util.Arrays;

public class OpenMBeanConstructorInfoSupport
        extends MBeanConstructorInfo
        implements OpenMBeanConstructorInfo{
    static final long serialVersionUID=-4400441579007477003L;
    // As this instance is immutable,
    // these two values need only be calculated once.
    private transient Integer myHashCode=null;
    private transient String myToString=null;

    public OpenMBeanConstructorInfoSupport(String name,
                                           String description,
                                           OpenMBeanParameterInfo[] signature){
        this(name,description,signature,(Descriptor)null);
    }

    public OpenMBeanConstructorInfoSupport(String name,
                                           String description,
                                           OpenMBeanParameterInfo[] signature,
                                           Descriptor descriptor){
        super(name,
                description,
                arrayCopyCast(signature), // may throw an ArrayStoreException
                descriptor);
        // check parameters that should not be null or empty
        // (unfortunately it is not done in superclass :-( ! )
        //
        if(name==null||name.trim().equals("")){
            throw new IllegalArgumentException("Argument name cannot be "+
                    "null or empty");
        }
        if(description==null||description.trim().equals("")){
            throw new IllegalArgumentException("Argument description cannot "+
                    "be null or empty");
        }
    }

    private static MBeanParameterInfo[]
    arrayCopyCast(OpenMBeanParameterInfo[] src){
        if(src==null)
            return null;
        MBeanParameterInfo[] dst=new MBeanParameterInfo[src.length];
        System.arraycopy(src,0,dst,0,src.length);
        // may throw an ArrayStoreException
        return dst;
    }

    public String toString(){
        // Calculate the string value if it has not yet been done (ie
        // 1st call to toString())
        //
        if(myToString==null){
            myToString=new StringBuilder()
                    .append(this.getClass().getName())
                    .append("(name=")
                    .append(this.getName())
                    .append(",signature=")
                    .append(Arrays.asList(this.getSignature()).toString())
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
        // if obj is not a OpenMBeanConstructorInfo, return false
        //
        OpenMBeanConstructorInfo other;
        try{
            other=(OpenMBeanConstructorInfo)obj;
        }catch(ClassCastException e){
            return false;
        }
        // Now, really test for equality between this
        // OpenMBeanConstructorInfo implementation and the other:
        //
        // their Name should be equal
        if(!this.getName().equals(other.getName())){
            return false;
        }
        // their Signatures should be equal
        if(!Arrays.equals(this.getSignature(),other.getSignature())){
            return false;
        }
        // All tests for equality were successfull
        //
        return true;
    }

    public int hashCode(){
        // Calculate the hash code value if it has not yet been done
        // (ie 1st call to hashCode())
        //
        if(myHashCode==null){
            int value=0;
            value+=this.getName().hashCode();
            value+=Arrays.asList(this.getSignature()).hashCode();
            myHashCode=Integer.valueOf(value);
        }
        // return always the same hash code for this instance (immutable)
        //
        return myHashCode.intValue();
    }
}
