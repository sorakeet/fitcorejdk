/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.openmbean;

import java.io.ObjectStreamException;
import java.lang.reflect.Array;

public class ArrayType<T> extends OpenType<T>{
    static final long serialVersionUID=720504429830309770L;
    // indexes refering to columns in the PRIMITIVE_ARRAY_TYPES table.
    private static final int PRIMITIVE_WRAPPER_NAME_INDEX=0;
    private static final int PRIMITIVE_TYPE_NAME_INDEX=1;
    private static final int PRIMITIVE_TYPE_KEY_INDEX=2;
    private static final int PRIMITIVE_OPEN_TYPE_INDEX=3;
    private static final Object[][] PRIMITIVE_ARRAY_TYPES={
            {Boolean.class.getName(),boolean.class.getName(),"Z",SimpleType.BOOLEAN},
            {Character.class.getName(),char.class.getName(),"C",SimpleType.CHARACTER},
            {Byte.class.getName(),byte.class.getName(),"B",SimpleType.BYTE},
            {Short.class.getName(),short.class.getName(),"S",SimpleType.SHORT},
            {Integer.class.getName(),int.class.getName(),"I",SimpleType.INTEGER},
            {Long.class.getName(),long.class.getName(),"J",SimpleType.LONG},
            {Float.class.getName(),float.class.getName(),"F",SimpleType.FLOAT},
            {Double.class.getName(),double.class.getName(),"D",SimpleType.DOUBLE}
    };
    private int dimension;
    private OpenType<?> elementType;
    private boolean primitiveArray;
    private transient Integer myHashCode=null;       // As this instance is immutable, these two values
    private transient String myToString=null;       // need only be calculated once.

    public ArrayType(int dimension,
                     OpenType<?> elementType) throws OpenDataException{
        // Check and construct state defined by parent.
        // We can't use the package-private OpenType constructor because
        // we don't know if the elementType parameter is sane.
        super(buildArrayClassName(dimension,elementType),
                buildArrayClassName(dimension,elementType),
                buildArrayDescription(dimension,elementType));
        // Check and construct state specific to ArrayType
        //
        if(elementType.isArray()){
            ArrayType<?> at=(ArrayType<?>)elementType;
            this.dimension=at.getDimension()+dimension;
            this.elementType=at.getElementOpenType();
            this.primitiveArray=at.isPrimitiveArray();
        }else{
            this.dimension=dimension;
            this.elementType=elementType;
            this.primitiveArray=false;
        }
    }

    private static String buildArrayClassName(int dimension,
                                              OpenType<?> elementType)
            throws OpenDataException{
        boolean isPrimitiveArray=false;
        if(elementType.isArray()){
            isPrimitiveArray=((ArrayType<?>)elementType).isPrimitiveArray();
        }
        return buildArrayClassName(dimension,elementType,isPrimitiveArray);
    }

    private static String buildArrayClassName(int dimension,
                                              OpenType<?> elementType,
                                              boolean isPrimitiveArray)
            throws OpenDataException{
        if(dimension<1){
            throw new IllegalArgumentException(
                    "Value of argument dimension must be greater than 0");
        }
        StringBuilder result=new StringBuilder();
        String elementClassName=elementType.getClassName();
        // Add N (= dimension) additional '[' characters to the existing array
        for(int i=1;i<=dimension;i++){
            result.append('[');
        }
        if(elementType.isArray()){
            result.append(elementClassName);
        }else{
            if(isPrimitiveArray){
                final String key=getPrimitiveTypeKey(elementClassName);
                // Ideally we should throw an IllegalArgumentException here,
                // but for compatibility reasons we throw an OpenDataException.
                // (used to be thrown by OpenType() constructor).
                //
                if(key==null)
                    throw new OpenDataException("Element type is not primitive: "
                            +elementClassName);
                result.append(key);
            }else{
                result.append("L");
                result.append(elementClassName);
                result.append(';');
            }
        }
        return result.toString();
    }

    static String getPrimitiveTypeKey(String elementClassName){
        for(Object[] typeDescr : PRIMITIVE_ARRAY_TYPES){
            if(elementClassName.equals(typeDescr[PRIMITIVE_WRAPPER_NAME_INDEX]))
                return (String)typeDescr[PRIMITIVE_TYPE_KEY_INDEX];
        }
        return null;
    }

    public boolean isPrimitiveArray(){
        return primitiveArray;
    }

    private static String buildArrayDescription(int dimension,
                                                OpenType<?> elementType)
            throws OpenDataException{
        boolean isPrimitiveArray=false;
        if(elementType.isArray()){
            isPrimitiveArray=((ArrayType<?>)elementType).isPrimitiveArray();
        }
        return buildArrayDescription(dimension,elementType,isPrimitiveArray);
    }

    private static String buildArrayDescription(int dimension,
                                                OpenType<?> elementType,
                                                boolean isPrimitiveArray)
            throws OpenDataException{
        if(elementType.isArray()){
            ArrayType<?> at=(ArrayType<?>)elementType;
            dimension+=at.getDimension();
            elementType=at.getElementOpenType();
            isPrimitiveArray=at.isPrimitiveArray();
        }
        StringBuilder result=
                new StringBuilder(dimension+"-dimension array of ");
        final String elementClassName=elementType.getClassName();
        if(isPrimitiveArray){
            // Convert from wrapper type to primitive type
            final String primitiveType=
                    getPrimitiveTypeName(elementClassName);
            // Ideally we should throw an IllegalArgumentException here,
            // but for compatibility reasons we throw an OpenDataException.
            // (used to be thrown by OpenType() constructor).
            //
            if(primitiveType==null)
                throw new OpenDataException("Element is not a primitive type: "+
                        elementClassName);
            result.append(primitiveType);
        }else{
            result.append(elementClassName);
        }
        return result.toString();
    }

    static String getPrimitiveTypeName(String elementClassName){
        for(Object[] typeDescr : PRIMITIVE_ARRAY_TYPES){
            if(elementClassName.equals(typeDescr[PRIMITIVE_WRAPPER_NAME_INDEX]))
                return (String)typeDescr[PRIMITIVE_TYPE_NAME_INDEX];
        }
        return null;
    }

    public ArrayType(SimpleType<?> elementType,
                     boolean primitiveArray) throws OpenDataException{
        // Check and construct state defined by parent.
        // We can call the package-private OpenType constructor because the
        // set of SimpleTypes is fixed and SimpleType can't be subclassed.
        super(buildArrayClassName(1,elementType,primitiveArray),
                buildArrayClassName(1,elementType,primitiveArray),
                buildArrayDescription(1,elementType,primitiveArray),
                true);
        // Check and construct state specific to ArrayType
        //
        this.dimension=1;
        this.elementType=elementType;
        this.primitiveArray=primitiveArray;
    }

    ArrayType(String className,String typeName,String description,
              int dimension,OpenType<?> elementType,
              boolean primitiveArray){
        super(className,typeName,description,true);
        this.dimension=dimension;
        this.elementType=elementType;
        this.primitiveArray=primitiveArray;
    }

    static boolean isPrimitiveContentType(final String primitiveKey){
        for(Object[] typeDescr : PRIMITIVE_ARRAY_TYPES){
            if(typeDescr[PRIMITIVE_TYPE_KEY_INDEX].equals(primitiveKey)){
                return true;
            }
        }
        return false;
    }

    public static <E> ArrayType<E[]> getArrayType(OpenType<E> elementType)
            throws OpenDataException{
        return new ArrayType<E[]>(1,elementType);
    }

    @SuppressWarnings("unchecked")  // can't get appropriate T for primitive array
    public static <T> ArrayType<T> getPrimitiveArrayType(Class<T> arrayClass){
        // Check if the supplied parameter is an array
        //
        if(!arrayClass.isArray()){
            throw new IllegalArgumentException("arrayClass must be an array");
        }
        // Calculate array dimension and component type name
        //
        int n=1;
        Class<?> componentType=arrayClass.getComponentType();
        while(componentType.isArray()){
            n++;
            componentType=componentType.getComponentType();
        }
        String componentTypeName=componentType.getName();
        // Check if the array's component type is a primitive type
        //
        if(!componentType.isPrimitive()){
            throw new IllegalArgumentException(
                    "component type of the array must be a primitive type");
        }
        // Map component type name to corresponding SimpleType
        //
        final SimpleType<?> simpleType=
                getPrimitiveOpenType(componentTypeName);
        // Build primitive array
        //
        try{
            @SuppressWarnings("rawtypes")
            ArrayType at=new ArrayType(simpleType,true);
            if(n>1)
                at=new ArrayType<T>(n-1,at);
            return at;
        }catch(OpenDataException e){
            throw new IllegalArgumentException(e); // should not happen
        }
    }

    static SimpleType<?> getPrimitiveOpenType(String primitiveTypeName){
        for(Object[] typeDescr : PRIMITIVE_ARRAY_TYPES){
            if(primitiveTypeName.equals(typeDescr[PRIMITIVE_TYPE_NAME_INDEX]))
                return (SimpleType<?>)typeDescr[PRIMITIVE_OPEN_TYPE_INDEX];
        }
        return null;
    }

    public boolean isValue(Object obj){
        // if obj is null, return false
        //
        if(obj==null){
            return false;
        }
        Class<?> objClass=obj.getClass();
        String objClassName=objClass.getName();
        // if obj is not an array, return false
        //
        if(!objClass.isArray()){
            return false;
        }
        // Test if obj's class name is the same as for the array values that this instance describes
        // (this is fine if elements are of simple types, which are final classes)
        //
        if(this.getClassName().equals(objClassName)){
            return true;
        }
        // In case this ArrayType instance describes an array of classes implementing the TabularData or CompositeData interface,
        // we first check for the assignability of obj to such an array of TabularData or CompositeData,
        // which ensures that:
        //  . obj is of the the same dimension as this ArrayType instance,
        //  . it is declared as an array of elements which are either all TabularData or all CompositeData.
        //
        // If the assignment check is positive,
        // then we have to check that each element in obj is of the same TabularType or CompositeType
        // as the one described by this ArrayType instance.
        //
        // [About assignment check, note that the call below returns true: ]
        // [Class.forName("[Lpackage.CompositeData;").isAssignableFrom(Class.forName("[Lpackage.CompositeDataImpl;)")); ]
        //
        if((this.elementType.getClassName().equals(TabularData.class.getName()))||
                (this.elementType.getClassName().equals(CompositeData.class.getName()))){
            boolean isTabular=
                    (elementType.getClassName().equals(TabularData.class.getName()));
            int[] dims=new int[getDimension()];
            Class<?> elementClass=isTabular?TabularData.class:CompositeData.class;
            Class<?> targetClass=Array.newInstance(elementClass,dims).getClass();
            // assignment check: return false if negative
            if(!targetClass.isAssignableFrom(objClass)){
                return false;
            }
            // check that all elements in obj are valid values for this ArrayType
            if(!checkElementsType((Object[])obj,this.dimension)){ // we know obj's dimension is this.dimension
                return false;
            }
            return true;
        }
        // if previous tests did not return, then obj is not a value for this ArrayType instance
        return false;
    }

    @Override
    boolean isAssignableFrom(OpenType<?> ot){
        if(!(ot instanceof ArrayType<?>))
            return false;
        ArrayType<?> at=(ArrayType<?>)ot;
        return (at.getDimension()==getDimension()&&
                at.isPrimitiveArray()==isPrimitiveArray()&&
                at.getElementOpenType().isAssignableFrom(getElementOpenType()));
    }

    public int getDimension(){
        return dimension;
    }

    public OpenType<?> getElementOpenType(){
        return elementType;
    }

    public boolean equals(Object obj){
        // if obj is null, return false
        //
        if(obj==null){
            return false;
        }
        // if obj is not an ArrayType, return false
        //
        if(!(obj instanceof ArrayType<?>))
            return false;
        ArrayType<?> other=(ArrayType<?>)obj;
        // if other's dimension is different than this instance's, return false
        //
        if(this.dimension!=other.dimension){
            return false;
        }
        // Test if other's elementType field is the same as for this instance
        //
        if(!this.elementType.equals(other.elementType)){
            return false;
        }
        // Test if other's primitiveArray flag is the same as for this instance
        //
        return this.primitiveArray==other.primitiveArray;
    }

    public int hashCode(){
        // Calculate the hash code value if it has not yet been done (ie 1st call to hashCode())
        //
        if(myHashCode==null){
            int value=0;
            value+=dimension;
            value+=elementType.hashCode();
            value+=Boolean.valueOf(primitiveArray).hashCode();
            myHashCode=Integer.valueOf(value);
        }
        // return always the same hash code for this instance (immutable)
        //
        return myHashCode.intValue();
    }

    public String toString(){
        // Calculate the string representation if it has not yet been done (ie 1st call to toString())
        //
        if(myToString==null){
            myToString=getClass().getName()+
                    "(name="+getTypeName()+
                    ",dimension="+dimension+
                    ",elementType="+elementType+
                    ",primitiveArray="+primitiveArray+")";
        }
        // return always the same string representation for this instance (immutable)
        //
        return myToString;
    }

    private boolean checkElementsType(Object[] x_dim_Array,int dim){
        // if the elements of x_dim_Array are themselves array: go down recursively....
        if(dim>1){
            for(int i=0;i<x_dim_Array.length;i++){
                if(!checkElementsType((Object[])x_dim_Array[i],dim-1)){
                    return false;
                }
            }
            return true;
        }
        // ...else, for a non-empty array, each element must be a valid value: either null or of the right openType
        else{
            for(int i=0;i<x_dim_Array.length;i++){
                if((x_dim_Array[i]!=null)&&(!this.getElementOpenType().isValue(x_dim_Array[i]))){
                    return false;
                }
            }
            return true;
        }
    }

    private Object readResolve() throws ObjectStreamException{
        if(primitiveArray){
            return convertFromWrapperToPrimitiveTypes();
        }else{
            return this;
        }
    }

    private <T> ArrayType<T> convertFromWrapperToPrimitiveTypes(){
        String cn=getClassName();
        String tn=getTypeName();
        String d=getDescription();
        for(Object[] typeDescr : PRIMITIVE_ARRAY_TYPES){
            if(cn.indexOf((String)typeDescr[PRIMITIVE_WRAPPER_NAME_INDEX])!=-1){
                cn=cn.replaceFirst(
                        "L"+typeDescr[PRIMITIVE_WRAPPER_NAME_INDEX]+";",
                        (String)typeDescr[PRIMITIVE_TYPE_KEY_INDEX]);
                tn=tn.replaceFirst(
                        "L"+typeDescr[PRIMITIVE_WRAPPER_NAME_INDEX]+";",
                        (String)typeDescr[PRIMITIVE_TYPE_KEY_INDEX]);
                d=d.replaceFirst(
                        (String)typeDescr[PRIMITIVE_WRAPPER_NAME_INDEX],
                        (String)typeDescr[PRIMITIVE_TYPE_NAME_INDEX]);
                break;
            }
        }
        return new ArrayType<T>(cn,tn,d,
                dimension,elementType,primitiveArray);
    }

    private Object writeReplace() throws ObjectStreamException{
        if(primitiveArray){
            return convertFromPrimitiveToWrapperTypes();
        }else{
            return this;
        }
    }

    private <T> ArrayType<T> convertFromPrimitiveToWrapperTypes(){
        String cn=getClassName();
        String tn=getTypeName();
        String d=getDescription();
        for(Object[] typeDescr : PRIMITIVE_ARRAY_TYPES){
            if(cn.indexOf((String)typeDescr[PRIMITIVE_TYPE_KEY_INDEX])!=-1){
                cn=cn.replaceFirst(
                        (String)typeDescr[PRIMITIVE_TYPE_KEY_INDEX],
                        "L"+typeDescr[PRIMITIVE_WRAPPER_NAME_INDEX]+";");
                tn=tn.replaceFirst(
                        (String)typeDescr[PRIMITIVE_TYPE_KEY_INDEX],
                        "L"+typeDescr[PRIMITIVE_WRAPPER_NAME_INDEX]+";");
                d=d.replaceFirst(
                        (String)typeDescr[PRIMITIVE_TYPE_NAME_INDEX],
                        (String)typeDescr[PRIMITIVE_WRAPPER_NAME_INDEX]);
                break;
            }
        }
        return new ArrayType<T>(cn,tn,d,
                dimension,elementType,primitiveArray);
    }
}
