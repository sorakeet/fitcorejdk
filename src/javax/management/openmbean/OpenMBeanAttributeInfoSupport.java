/**
 * Copyright (c) 2000, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.openmbean;
// java import
//

import com.sun.jmx.remote.util.EnvHelp;
import sun.reflect.misc.MethodUtil;
import sun.reflect.misc.ReflectUtil;

import javax.management.Descriptor;
import javax.management.DescriptorRead;
import javax.management.ImmutableDescriptor;
import javax.management.MBeanAttributeInfo;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class OpenMBeanAttributeInfoSupport
        extends MBeanAttributeInfo
        implements OpenMBeanAttributeInfo{
    static final long serialVersionUID=-4867215622149721849L;
    private final Object defaultValue;
    private final Set<?> legalValues;  // to be constructed unmodifiable
    private final Comparable<?> minValue;
    private final Comparable<?> maxValue;
    private OpenType<?> openType;
    // As this instance is immutable, these two values need only
    // be calculated once.
    private transient Integer myHashCode=null;
    private transient String myToString=null;

    public OpenMBeanAttributeInfoSupport(String name,
                                         String description,
                                         OpenType<?> openType,
                                         boolean isReadable,
                                         boolean isWritable,
                                         boolean isIs){
        this(name,description,openType,isReadable,isWritable,isIs,
                (Descriptor)null);
    }

    public OpenMBeanAttributeInfoSupport(String name,
                                         String description,
                                         OpenType<?> openType,
                                         boolean isReadable,
                                         boolean isWritable,
                                         boolean isIs,
                                         Descriptor descriptor){
        // Construct parent's state
        //
        super(name,
                (openType==null)?null:openType.getClassName(),
                description,
                isReadable,
                isWritable,
                isIs,
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

    public <T> OpenMBeanAttributeInfoSupport(String name,
                                             String description,
                                             OpenType<T> openType,
                                             boolean isReadable,
                                             boolean isWritable,
                                             boolean isIs,
                                             T defaultValue)
            throws OpenDataException{
        this(name,description,openType,isReadable,isWritable,isIs,
                defaultValue,(T[])null);
    }

    public <T> OpenMBeanAttributeInfoSupport(String name,
                                             String description,
                                             OpenType<T> openType,
                                             boolean isReadable,
                                             boolean isWritable,
                                             boolean isIs,
                                             T defaultValue,
                                             T[] legalValues)
            throws OpenDataException{
        this(name,description,openType,isReadable,isWritable,isIs,
                defaultValue,legalValues,null,null);
    }

    private <T> OpenMBeanAttributeInfoSupport(String name,
                                              String description,
                                              OpenType<T> openType,
                                              boolean isReadable,
                                              boolean isWritable,
                                              boolean isIs,
                                              T defaultValue,
                                              T[] legalValues,
                                              Comparable<T> minValue,
                                              Comparable<T> maxValue)
            throws OpenDataException{
        super(name,
                (openType==null)?null:openType.getClassName(),
                description,
                isReadable,
                isWritable,
                isIs,
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

    static void check(OpenMBeanParameterInfo info) throws OpenDataException{
        OpenType<?> openType=info.getOpenType();
        if(openType==null)
            throw new IllegalArgumentException("OpenType cannot be null");
        if(info.getName()==null||
                info.getName().trim().equals(""))
            throw new IllegalArgumentException("Name cannot be null or empty");
        if(info.getDescription()==null||
                info.getDescription().trim().equals(""))
            throw new IllegalArgumentException("Description cannot be null or empty");
        // Check and initialize defaultValue
        //
        if(info.hasDefaultValue()){
            // Default value not supported for ArrayType and TabularType
            // Cast to Object because "OpenType<T> instanceof" is illegal
            if(openType.isArray()||(Object)openType instanceof TabularType){
                throw new OpenDataException("Default value not supported "+
                        "for ArrayType and TabularType");
            }
            // Check defaultValue's class
            if(!openType.isValue(info.getDefaultValue())){
                final String msg=
                        "Argument defaultValue's class [\""+
                                info.getDefaultValue().getClass().getName()+
                                "\"] does not match the one defined in openType[\""+
                                openType.getClassName()+"\"]";
                throw new OpenDataException(msg);
            }
        }
        // Check that we don't have both legalValues and min or max
        //
        if(info.hasLegalValues()&&
                (info.hasMinValue()||info.hasMaxValue())){
            throw new OpenDataException("cannot have both legalValue and "+
                    "minValue or maxValue");
        }
        // Check minValue and maxValue
        if(info.hasMinValue()&&!openType.isValue(info.getMinValue())){
            final String msg=
                    "Type of minValue ["+info.getMinValue().getClass().getName()+
                            "] does not match OpenType ["+openType.getClassName()+"]";
            throw new OpenDataException(msg);
        }
        if(info.hasMaxValue()&&!openType.isValue(info.getMaxValue())){
            final String msg=
                    "Type of maxValue ["+info.getMaxValue().getClass().getName()+
                            "] does not match OpenType ["+openType.getClassName()+"]";
            throw new OpenDataException(msg);
        }
        // Check that defaultValue is a legal value
        //
        if(info.hasDefaultValue()){
            Object defaultValue=info.getDefaultValue();
            if(info.hasLegalValues()&&
                    !info.getLegalValues().contains(defaultValue)){
                throw new OpenDataException("defaultValue is not contained "+
                        "in legalValues");
            }
            // Check that minValue <= defaultValue <= maxValue
            //
            if(info.hasMinValue()){
                if(compare(info.getMinValue(),defaultValue)>0){
                    throw new OpenDataException("minValue cannot be greater "+
                            "than defaultValue");
                }
            }
            if(info.hasMaxValue()){
                if(compare(info.getMaxValue(),defaultValue)<0){
                    throw new OpenDataException("maxValue cannot be less "+
                            "than defaultValue");
                }
            }
        }
        // Check legalValues
        //
        if(info.hasLegalValues()){
            // legalValues not supported for TabularType and arrays
            if((Object)openType instanceof TabularType||openType.isArray()){
                throw new OpenDataException("Legal values not supported "+
                        "for TabularType and arrays");
            }
            // Check legalValues are valid with openType
            for(Object v : info.getLegalValues()){
                if(!openType.isValue(v)){
                    final String msg=
                            "Element of legalValues ["+v+
                                    "] is not a valid value for the specified openType ["+
                                    openType.toString()+"]";
                    throw new OpenDataException(msg);
                }
            }
        }
        // Check that, if both specified, minValue <= maxValue
        //
        if(info.hasMinValue()&&info.hasMaxValue()){
            if(compare(info.getMinValue(),info.getMaxValue())>0){
                throw new OpenDataException("minValue cannot be greater "+
                        "than maxValue");
            }
        }
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    static int compare(Object x,Object y){
        return ((Comparable)x).compareTo(y);
    }

    static <T> Descriptor makeDescriptor(OpenType<T> openType,
                                         T defaultValue,
                                         T[] legalValues,
                                         Comparable<T> minValue,
                                         Comparable<T> maxValue){
        Map<String,Object> map=new HashMap<String,Object>();
        if(defaultValue!=null)
            map.put("defaultValue",defaultValue);
        if(legalValues!=null){
            Set<T> set=new HashSet<T>();
            for(T v : legalValues)
                set.add(v);
            set=Collections.unmodifiableSet(set);
            map.put("legalValues",set);
        }
        if(minValue!=null)
            map.put("minValue",minValue);
        if(maxValue!=null)
            map.put("maxValue",maxValue);
        if(map.isEmpty()){
            return openType.getDescriptor();
        }else{
            map.put("openType",openType);
            return new ImmutableDescriptor(map);
        }
    }

    public <T> OpenMBeanAttributeInfoSupport(String name,
                                             String description,
                                             OpenType<T> openType,
                                             boolean isReadable,
                                             boolean isWritable,
                                             boolean isIs,
                                             T defaultValue,
                                             Comparable<T> minValue,
                                             Comparable<T> maxValue)
            throws OpenDataException{
        this(name,description,openType,isReadable,isWritable,isIs,
                defaultValue,null,minValue,maxValue);
    }

    static <T> T valueFrom(Descriptor d,String name,OpenType<T> openType){
        Object x=d.getFieldValue(name);
        if(x==null)
            return null;
        try{
            return convertFrom(x,openType);
        }catch(Exception e){
            final String msg=
                    "Cannot convert descriptor field "+name+"  to "+
                            openType.getTypeName();
            throw EnvHelp.initCause(new IllegalArgumentException(msg),e);
        }
    }

    static <T> Set<T> valuesFrom(Descriptor d,String name,
                                 OpenType<T> openType){
        Object x=d.getFieldValue(name);
        if(x==null)
            return null;
        Collection<?> coll;
        if(x instanceof Set<?>){
            Set<?> set=(Set<?>)x;
            boolean asis=true;
            for(Object element : set){
                if(!openType.isValue(element)){
                    asis=false;
                    break;
                }
            }
            if(asis)
                return cast(set);
            coll=set;
        }else if(x instanceof Object[]){
            coll=Arrays.asList((Object[])x);
        }else{
            final String msg=
                    "Descriptor value for "+name+" must be a Set or "+
                            "an array: "+x.getClass().getName();
            throw new IllegalArgumentException(msg);
        }
        Set<T> result=new HashSet<T>();
        for(Object element : coll)
            result.add(convertFrom(element,openType));
        return result;
    }

    static <T> Comparable<?> comparableValueFrom(Descriptor d,String name,
                                                 OpenType<T> openType){
        T t=valueFrom(d,name,openType);
        if(t==null||t instanceof Comparable<?>)
            return (Comparable<?>)t;
        final String msg=
                "Descriptor field "+name+" with value "+t+
                        " is not Comparable";
        throw new IllegalArgumentException(msg);
    }

    private static <T> T convertFrom(Object x,OpenType<T> openType){
        if(openType.isValue(x)){
            T t=OpenMBeanAttributeInfoSupport.<T>cast(x);
            return t;
        }
        return convertFromStrings(x,openType);
    }

    private static <T> T convertFromStrings(Object x,OpenType<T> openType){
        if(openType instanceof ArrayType<?>)
            return convertFromStringArray(x,openType);
        else if(x instanceof String)
            return convertFromString((String)x,openType);
        final String msg=
                "Cannot convert value "+x+" of type "+
                        x.getClass().getName()+" to type "+openType.getTypeName();
        throw new IllegalArgumentException(msg);
    }

    private static <T> T convertFromString(String s,OpenType<T> openType){
        Class<T> c;
        try{
            String className=openType.safeGetClassName();
            ReflectUtil.checkPackageAccess(className);
            c=cast(Class.forName(className));
        }catch(ClassNotFoundException e){
            throw new NoClassDefFoundError(e.toString());  // can't happen
        }
        // Look for: public static T valueOf(String)
        Method valueOf;
        try{
            // It is safe to call this plain Class.getMethod because the class "c"
            // was checked before by ReflectUtil.checkPackageAccess(openType.safeGetClassName());
            valueOf=c.getMethod("valueOf",String.class);
            if(!Modifier.isStatic(valueOf.getModifiers())||
                    valueOf.getReturnType()!=c)
                valueOf=null;
        }catch(NoSuchMethodException e){
            valueOf=null;
        }
        if(valueOf!=null){
            try{
                return c.cast(MethodUtil.invoke(valueOf,null,new Object[]{s}));
            }catch(Exception e){
                final String msg=
                        "Could not convert \""+s+"\" using method: "+valueOf;
                throw new IllegalArgumentException(msg,e);
            }
        }
        // Look for: public T(String)
        Constructor<T> con;
        try{
            // It is safe to call this plain Class.getConstructor because the class "c"
            // was checked before by ReflectUtil.checkPackageAccess(openType.safeGetClassName());
            con=c.getConstructor(String.class);
        }catch(NoSuchMethodException e){
            con=null;
        }
        if(con!=null){
            try{
                return con.newInstance(s);
            }catch(Exception e){
                final String msg=
                        "Could not convert \""+s+"\" using constructor: "+con;
                throw new IllegalArgumentException(msg,e);
            }
        }
        throw new IllegalArgumentException("Don't know how to convert "+
                "string to "+
                openType.getTypeName());
    }

    private static <T> T convertFromStringArray(Object x,
                                                OpenType<T> openType){
        ArrayType<?> arrayType=(ArrayType<?>)openType;
        OpenType<?> baseType=arrayType.getElementOpenType();
        int dim=arrayType.getDimension();
        String squareBrackets="[";
        for(int i=1;i<dim;i++)
            squareBrackets+="[";
        Class<?> stringArrayClass;
        Class<?> targetArrayClass;
        try{
            String baseClassName=baseType.safeGetClassName();
            // check access to the provided base type class name and bail out early
            ReflectUtil.checkPackageAccess(baseClassName);
            stringArrayClass=
                    Class.forName(squareBrackets+"Ljava.lang.String;");
            targetArrayClass=
                    Class.forName(squareBrackets+"L"+baseClassName+";");
        }catch(ClassNotFoundException e){
            throw new NoClassDefFoundError(e.toString());  // can't happen
        }
        if(!stringArrayClass.isInstance(x)){
            final String msg=
                    "Value for "+dim+"-dimensional array of "+
                            baseType.getTypeName()+" must be same type or a String "+
                            "array with same dimensions";
            throw new IllegalArgumentException(msg);
        }
        OpenType<?> componentOpenType;
        if(dim==1)
            componentOpenType=baseType;
        else{
            try{
                componentOpenType=new ArrayType<T>(dim-1,baseType);
            }catch(OpenDataException e){
                throw new IllegalArgumentException(e.getMessage(),e);
                // can't happen
            }
        }
        int n=Array.getLength(x);
        Object[] targetArray=(Object[])
                Array.newInstance(targetArrayClass.getComponentType(),n);
        for(int i=0;i<n;i++){
            Object stringish=Array.get(x,i);  // String or String[] etc
            Object converted=
                    convertFromStrings(stringish,componentOpenType);
            Array.set(targetArray,i,converted);
        }
        return OpenMBeanAttributeInfoSupport.<T>cast(targetArray);
    }

    private Object readResolve(){
        if(getDescriptor().getFieldNames().length==0){
            OpenType<Object> xopenType=cast(openType);
            Set<Object> xlegalValues=cast(legalValues);
            Comparable<Object> xminValue=cast(minValue);
            Comparable<Object> xmaxValue=cast(maxValue);
            return new OpenMBeanAttributeInfoSupport(
                    name,description,openType,
                    isReadable(),isWritable(),isIs(),
                    makeDescriptor(xopenType,defaultValue,xlegalValues,
                            xminValue,xmaxValue));
        }else
            return this;
    }

    static <T> Descriptor makeDescriptor(OpenType<T> openType,
                                         T defaultValue,
                                         Set<T> legalValues,
                                         Comparable<T> minValue,
                                         Comparable<T> maxValue){
        T[] legals;
        if(legalValues==null)
            legals=null;
        else{
            legals=cast(new Object[legalValues.size()]);
            legalValues.toArray(legals);
        }
        return makeDescriptor(openType,defaultValue,legals,minValue,maxValue);
    }

    @SuppressWarnings("unchecked")
    static <T> T cast(Object x){
        return (T)x;
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
        // Note: only comparable values have a minValue,
        // so that's not the case of arrays and tabulars (always null).
        return minValue;
    }

    public Comparable<?> getMaxValue(){
        // Note: only comparable values have a maxValue,
        // so that's not the case of arrays and tabulars (always null).
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
        return isValue(this,obj);
    }

    @SuppressWarnings({"unchecked","rawtypes"})  // cast to Comparable
    static boolean isValue(OpenMBeanParameterInfo info,Object obj){
        if(info.hasDefaultValue()&&obj==null)
            return true;
        return
                info.getOpenType().isValue(obj)&&
                        (!info.hasLegalValues()||info.getLegalValues().contains(obj))&&
                        (!info.hasMinValue()||
                                ((Comparable)info.getMinValue()).compareTo(obj)<=0)&&
                        (!info.hasMaxValue()||
                                ((Comparable)info.getMaxValue()).compareTo(obj)>=0);
    }

    public String toString(){
        // Calculate the string value if it has not yet been done
        // (ie 1st call to toString())
        //
        if(myToString==null)
            myToString=toString(this);
        // return always the same string representation for this
        // instance (immutable)
        //
        return myToString;
    }

    public boolean equals(Object obj){
        if(!(obj instanceof OpenMBeanAttributeInfo))
            return false;
        OpenMBeanAttributeInfo other=(OpenMBeanAttributeInfo)obj;
        return
                this.isReadable()==other.isReadable()&&
                        this.isWritable()==other.isWritable()&&
                        this.isIs()==other.isIs()&&
                        equal(this,other);
    }

    static boolean equal(OpenMBeanParameterInfo x1,OpenMBeanParameterInfo x2){
        if(x1 instanceof DescriptorRead){
            if(!(x2 instanceof DescriptorRead))
                return false;
            Descriptor d1=((DescriptorRead)x1).getDescriptor();
            Descriptor d2=((DescriptorRead)x2).getDescriptor();
            if(!d1.equals(d2))
                return false;
        }else if(x2 instanceof DescriptorRead)
            return false;
        return
                x1.getName().equals(x2.getName())&&
                        x1.getOpenType().equals(x2.getOpenType())&&
                        (x1.hasDefaultValue()?
                                x1.getDefaultValue().equals(x2.getDefaultValue()):
                                !x2.hasDefaultValue())&&
                        (x1.hasMinValue()?
                                x1.getMinValue().equals(x2.getMinValue()):
                                !x2.hasMinValue())&&
                        (x1.hasMaxValue()?
                                x1.getMaxValue().equals(x2.getMaxValue()):
                                !x2.hasMaxValue())&&
                        (x1.hasLegalValues()?
                                x1.getLegalValues().equals(x2.getLegalValues()):
                                !x2.hasLegalValues());
    }

    public int hashCode(){
        // Calculate the hash code value if it has not yet been done
        // (ie 1st call to hashCode())
        //
        if(myHashCode==null)
            myHashCode=hashCode(this);
        // return always the same hash code for this instance (immutable)
        //
        return myHashCode.intValue();
    }

    static int hashCode(OpenMBeanParameterInfo info){
        int value=0;
        value+=info.getName().hashCode();
        value+=info.getOpenType().hashCode();
        if(info.hasDefaultValue())
            value+=info.getDefaultValue().hashCode();
        if(info.hasMinValue())
            value+=info.getMinValue().hashCode();
        if(info.hasMaxValue())
            value+=info.getMaxValue().hashCode();
        if(info.hasLegalValues())
            value+=info.getLegalValues().hashCode();
        if(info instanceof DescriptorRead)
            value+=((DescriptorRead)info).getDescriptor().hashCode();
        return value;
    }

    static String toString(OpenMBeanParameterInfo info){
        Descriptor d=(info instanceof DescriptorRead)?
                ((DescriptorRead)info).getDescriptor():null;
        return
                info.getClass().getName()+
                        "(name="+info.getName()+
                        ",openType="+info.getOpenType()+
                        ",default="+info.getDefaultValue()+
                        ",minValue="+info.getMinValue()+
                        ",maxValue="+info.getMaxValue()+
                        ",legalValues="+info.getLegalValues()+
                        ((d==null)?"":",descriptor="+d)+
                        ")";
    }
}
