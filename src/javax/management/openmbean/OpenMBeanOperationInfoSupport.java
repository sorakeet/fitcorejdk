/**
 * Copyright (c) 2000, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.openmbean;
// java import
//

import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import java.util.Arrays;

public class OpenMBeanOperationInfoSupport
        extends MBeanOperationInfo
        implements OpenMBeanOperationInfo{
    static final long serialVersionUID=4996859732565369366L;
    private OpenType<?> returnOpenType;
    // As this instance is immutable,
    // these two values need only be calculated once.
    private transient Integer myHashCode=null;
    private transient String myToString=null;

    public OpenMBeanOperationInfoSupport(String name,
                                         String description,
                                         OpenMBeanParameterInfo[] signature,
                                         OpenType<?> returnOpenType,
                                         int impact){
        this(name,description,signature,returnOpenType,impact,
                (Descriptor)null);
    }

    public OpenMBeanOperationInfoSupport(String name,
                                         String description,
                                         OpenMBeanParameterInfo[] signature,
                                         OpenType<?> returnOpenType,
                                         int impact,
                                         Descriptor descriptor){
        super(name,
                description,
                arrayCopyCast(signature),
                // must prevent NPE here - we will throw IAE later on if
                // returnOpenType is null
                (returnOpenType==null)?null:returnOpenType.getClassName(),
                impact,
                ImmutableDescriptor.union(descriptor,
                        // must prevent NPE here - we will throw IAE later on if
                        // returnOpenType is null
                        (returnOpenType==null)?null:returnOpenType.getDescriptor()));
        // check parameters that should not be null or empty
        // (unfortunately it is not done in superclass :-( ! )
        //
        if(name==null||name.trim().equals("")){
            throw new IllegalArgumentException("Argument name cannot "+
                    "be null or empty");
        }
        if(description==null||description.trim().equals("")){
            throw new IllegalArgumentException("Argument description cannot "+
                    "be null or empty");
        }
        if(returnOpenType==null){
            throw new IllegalArgumentException("Argument returnOpenType "+
                    "cannot be null");
        }
        if(impact!=ACTION&&impact!=ACTION_INFO&&impact!=INFO&&
                impact!=UNKNOWN){
            throw new IllegalArgumentException("Argument impact can only be "+
                    "one of ACTION, ACTION_INFO, "+
                    "INFO, or UNKNOWN: "+impact);
        }
        this.returnOpenType=returnOpenType;
    }

    // Converts an array of OpenMBeanParameterInfo objects extending
    // MBeanParameterInfo into an array of MBeanParameterInfo.
    //
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
        // Calculate the hash code value if it has not yet been done
        // (ie 1st call to toString())
        //
        if(myToString==null){
            myToString=new StringBuilder()
                    .append(this.getClass().getName())
                    .append("(name=")
                    .append(this.getName())
                    .append(",signature=")
                    .append(Arrays.asList(this.getSignature()).toString())
                    .append(",return=")
                    .append(this.getReturnOpenType().toString())
                    .append(",impact=")
                    .append(this.getImpact())
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
    // [JF]: should we add constructor with java.lang.reflect.Method
    // method parameter ?  would need to add consistency check between
    // OpenType<?> returnOpenType and method.getReturnType().

    public boolean equals(Object obj){
        // if obj is null, return false
        //
        if(obj==null){
            return false;
        }
        // if obj is not a OpenMBeanOperationInfo, return false
        //
        OpenMBeanOperationInfo other;
        try{
            other=(OpenMBeanOperationInfo)obj;
        }catch(ClassCastException e){
            return false;
        }
        // Now, really test for equality between this
        // OpenMBeanOperationInfo implementation and the other:
        //
        // their Name should be equal
        if(!this.getName().equals(other.getName())){
            return false;
        }
        // their Signatures should be equal
        if(!Arrays.equals(this.getSignature(),other.getSignature())){
            return false;
        }
        // their return open types should be equal
        if(!this.getReturnOpenType().equals(other.getReturnOpenType())){
            return false;
        }
        // their impacts should be equal
        if(this.getImpact()!=other.getImpact()){
            return false;
        }
        // All tests for equality were successfull
        //
        return true;
    }

    public OpenType<?> getReturnOpenType(){
        return returnOpenType;
    }

    public int hashCode(){
        // Calculate the hash code value if it has not yet been done
        // (ie 1st call to hashCode())
        //
        if(myHashCode==null){
            int value=0;
            value+=this.getName().hashCode();
            value+=Arrays.asList(this.getSignature()).hashCode();
            value+=this.getReturnOpenType().hashCode();
            value+=this.getImpact();
            myHashCode=Integer.valueOf(value);
        }
        // return always the same hash code for this instance (immutable)
        //
        return myHashCode.intValue();
    }

    private Object readResolve(){
        if(getDescriptor().getFieldNames().length==0){
            // This constructor will construct the expected default Descriptor.
            //
            return new OpenMBeanOperationInfoSupport(
                    name,description,arrayCopyCast(getSignature()),
                    returnOpenType,getImpact());
        }else
            return this;
    }

    // Converts an array of MBeanParameterInfo objects implementing
    // OpenMBeanParameterInfo into an array of OpenMBeanParameterInfo.
    //
    private static OpenMBeanParameterInfo[]
    arrayCopyCast(MBeanParameterInfo[] src){
        if(src==null)
            return null;
        OpenMBeanParameterInfo[] dst=new OpenMBeanParameterInfo[src.length];
        System.arraycopy(src,0,dst,0,src.length);
        // may throw an ArrayStoreException
        return dst;
    }
}
