/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.openmbean;
// java import
//

import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.MBeanParameterInfo;
import java.util.Set;

import static javax.management.openmbean.OpenMBeanAttributeInfoSupport.*;
// OpenMBeanAttributeInfoSupport and this class are very similar
// but can't easily be refactored because there's no multiple inheritance.
// The best we can do for refactoring is to put a bunch of static methods
// in OpenMBeanAttributeInfoSupport and import them here.

public class OpenMBeanParameterInfoSupport
        extends MBeanParameterInfo
        implements OpenMBeanParameterInfo{
    static final long serialVersionUID=-7235016873758443122L;
    private OpenType<?> openType;
    private Object defaultValue=null;
    private Set<?> legalValues=null;  // to be constructed unmodifiable
    private Comparable<?> minValue=null;
    private Comparable<?> maxValue=null;
    // As this instance is immutable, these two values need only
    // be calculated once.
    private transient Integer myHashCode=null;        // As this instance is immutable, these two values
    private transient String myToString=null;        // need only be calculated once.

    public OpenMBeanParameterInfoSupport(String name,
                                         String description,
                                         OpenType<?> openType){
        this(name,description,openType,(Descriptor)null);
    }

    public OpenMBeanParameterInfoSupport(String name,
                                         String description,
                                         OpenType<?> openType,
                                         Descriptor descriptor){
        // Construct parent's state
        //
        super(name,
                (openType==null)?null:openType.getClassName(),
                description,
                ImmutableDescriptor.union(descriptor,(openType==null)?null:
                        openType.getDescriptor()));
        // Initialize this instance's specific state
        //
        this.openType=openType;
        descriptor=getDescriptor();  // replace null by empty
        this.defaultValue=valueFrom(descriptor,"defaultValue",openType);
        this.legalValues=valuesFrom(descriptor,"legalValues",openType);
        this.minValue=comparableValueFrom(descriptor,"minValue",openType);
        this.maxValue=comparableValueFrom(descriptor,"maxValue",openType);
        try{
            check(this);
        }catch(OpenDataException e){
            throw new IllegalArgumentException(e.getMessage(),e);
        }
    }

    public <T> OpenMBeanParameterInfoSupport(String name,
                                             String description,
                                             OpenType<T> openType,
                                             T defaultValue)
            throws OpenDataException{
        this(name,description,openType,defaultValue,(T[])null);
    }

    public <T> OpenMBeanParameterInfoSupport(String name,
                                             String description,
                                             OpenType<T> openType,
                                             T defaultValue,
                                             T[] legalValues)
            throws OpenDataException{
        this(name,description,openType,
                defaultValue,legalValues,null,null);
    }

    private <T> OpenMBeanParameterInfoSupport(String name,
                                              String description,
                                              OpenType<T> openType,
                                              T defaultValue,
                                              T[] legalValues,
                                              Comparable<T> minValue,
                                              Comparable<T> maxValue)
            throws OpenDataException{
        super(name,
                (openType==null)?null:openType.getClassName(),
                description,
                makeDescriptor(openType,
                        defaultValue,legalValues,minValue,maxValue));
        this.openType=openType;
        Descriptor d=getDescriptor();
        this.defaultValue=defaultValue;
        this.minValue=minValue;
        this.maxValue=maxValue;
        // We already converted the array into an unmodifiable Set
        // in the descriptor.
        this.legalValues=(Set<?>)d.getFieldValue("legalValues");
        check(this);
    }

    public <T> OpenMBeanParameterInfoSupport(String name,
                                             String description,
                                             OpenType<T> openType,
                                             T defaultValue,
                                             Comparable<T> minValue,
                                             Comparable<T> maxValue)
            throws OpenDataException{
        this(name,description,openType,
                defaultValue,null,minValue,maxValue);
    }

    private Object readResolve(){
        if(getDescriptor().getFieldNames().length==0){
            // This noise allows us to avoid "unchecked" warnings without
            // having to suppress them explicitly.
            OpenType<Object> xopenType=cast(openType);
            Set<Object> xlegalValues=cast(legalValues);
            Comparable<Object> xminValue=cast(minValue);
            Comparable<Object> xmaxValue=cast(maxValue);
            return new OpenMBeanParameterInfoSupport(
                    name,description,openType,
                    makeDescriptor(xopenType,defaultValue,xlegalValues,
                            xminValue,xmaxValue));
        }else
            return this;
    }

    public OpenType<?> getOpenType(){
        return openType;
    }

    public Object getDefaultValue(){
        // Special case for ArrayType and TabularType
        // [JF] TODO: clone it so that it cannot be altered,
        // [JF] TODO: if we decide to support defaultValue as an array itself.
        // [JF] As of today (oct 2000) it is not supported so
        // defaultValue is null for arrays. Nothing to do.
        return defaultValue;
    }

    public Set<?> getLegalValues(){
        // Special case for ArrayType and TabularType
        // [JF] TODO: clone values so that they cannot be altered,
        // [JF] TODO: if we decide to support LegalValues as an array itself.
        // [JF] As of today (oct 2000) it is not supported so
        // legalValues is null for arrays. Nothing to do.
        // Returns our legalValues Set (set was constructed unmodifiable)
        return (legalValues);
    }

    public Comparable<?> getMinValue(){
        // Note: only comparable values have a minValue, so that's not
        // the case of arrays and tabulars (always null).
        return minValue;
    }

    public Comparable<?> getMaxValue(){
        // Note: only comparable values have a maxValue, so that's not
        // the case of arrays and tabulars (always null).
        return maxValue;
    }

    public boolean hasDefaultValue(){
        return (defaultValue!=null);
    }

    public boolean hasLegalValues(){
        return (legalValues!=null);
    }

    public boolean hasMinValue(){
        return (minValue!=null);
    }

    public boolean hasMaxValue(){
        return (maxValue!=null);
    }

    public boolean isValue(Object obj){
        return OpenMBeanAttributeInfoSupport.isValue(this,obj);
        // compiler bug? should be able to omit class name here
        // also below in toString and hashCode
    }

    public String toString(){
        // Calculate the string value if it has not yet been done (ie
        // 1st call to toString())
        //
        if(myToString==null)
            myToString=OpenMBeanAttributeInfoSupport.toString(this);
        // return always the same string representation for this
        // instance (immutable)
        //
        return myToString;
    }

    public boolean equals(Object obj){
        if(!(obj instanceof OpenMBeanParameterInfo))
            return false;
        OpenMBeanParameterInfo other=(OpenMBeanParameterInfo)obj;
        return equal(this,other);
    }

    public int hashCode(){
        // Calculate the hash code value if it has not yet been done
        // (ie 1st call to hashCode())
        //
        if(myHashCode==null)
            myHashCode=OpenMBeanAttributeInfoSupport.hashCode(this);
        // return always the same hash code for this instance (immutable)
        //
        return myHashCode.intValue();
    }
}
