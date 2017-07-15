/**
 * Copyright (c) 2000, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.openmbean;

import com.sun.jmx.mbeanserver.GetPropertyAction;

import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class OpenType<T> implements Serializable{
    public static final List<String> ALLOWED_CLASSNAMES_LIST=
            Collections.unmodifiableList(
                    Arrays.asList(
                            "java.lang.Void",
                            "java.lang.Boolean",
                            "java.lang.Character",
                            "java.lang.Byte",
                            "java.lang.Short",
                            "java.lang.Integer",
                            "java.lang.Long",
                            "java.lang.Float",
                            "java.lang.Double",
                            "java.lang.String",
                            "java.math.BigDecimal",
                            "java.math.BigInteger",
                            "java.util.Date",
                            "javax.management.ObjectName",
                            CompositeData.class.getName(),        // better refer to these two class names like this, rather than hardcoding a string,
                            TabularData.class.getName()));       // in case the package of these classes should change (who knows...)
    @Deprecated
    public static final String[] ALLOWED_CLASSNAMES=
            ALLOWED_CLASSNAMES_LIST.toArray(new String[0]);
    static final long serialVersionUID=-9195195325186646468L;
    private String className;
    private String description;
    private String typeName;
    private transient boolean isArray=false;
    private transient Descriptor descriptor;

    protected OpenType(String className,
                       String typeName,
                       String description) throws OpenDataException{
        checkClassNameOverride();
        this.typeName=valid("typeName",typeName);
        this.description=valid("description",description);
        this.className=validClassName(className);
        this.isArray=(this.className!=null&&this.className.startsWith("["));
    }

    private void checkClassNameOverride() throws SecurityException{
        if(this.getClass().getClassLoader()==null)
            return;  // We trust bootstrap classes.
        if(overridesGetClassName(this.getClass())){
            final GetPropertyAction getExtendOpenTypes=
                    new GetPropertyAction("jmx.extend.open.types");
            if(AccessController.doPrivileged(getExtendOpenTypes)==null){
                throw new SecurityException("Cannot override getClassName() "+
                        "unless -Djmx.extend.open.types");
            }
        }
    }

    private static boolean overridesGetClassName(final Class<?> c){
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>(){
            public Boolean run(){
                try{
                    return (c.getMethod("getClassName").getDeclaringClass()!=
                            OpenType.class);
                }catch(Exception e){
                    return true;  // fail safe
                }
            }
        });
    }

    private static String validClassName(String className) throws OpenDataException{
        className=valid("className",className);
        // Check if className describes an array class, and determines its elements' class name.
        // (eg: a 3-dimensional array of Strings has for class name: "[[[Ljava.lang.String;")
        //
        int n=0;
        while(className.startsWith("[",n)){
            n++;
        }
        String eltClassName; // class name of array elements
        boolean isPrimitiveArray=false;
        if(n>0){
            if(className.startsWith("L",n)&&className.endsWith(";")){
                // removes the n leading '[' + the 'L' characters
                // and the last ';' character
                eltClassName=className.substring(n+1,className.length()-1);
            }else if(n==className.length()-1){
                // removes the n leading '[' characters
                eltClassName=className.substring(n,className.length());
                isPrimitiveArray=true;
            }else{
                throw new OpenDataException("Argument className=\""+className+
                        "\" is not a valid class name");
            }
        }else{
            // not an array
            eltClassName=className;
        }
        // Check that eltClassName's value is one of the allowed basic data types for open data
        //
        boolean ok=false;
        if(isPrimitiveArray){
            ok=ArrayType.isPrimitiveContentType(eltClassName);
        }else{
            ok=ALLOWED_CLASSNAMES_LIST.contains(eltClassName);
        }
        if(!ok){
            throw new OpenDataException("Argument className=\""+className+
                    "\" is not one of the allowed Java class names for open data.");
        }
        return className;
    }

    private static String valid(String argName,String argValue){
        if(argValue==null||(argValue=argValue.trim()).equals(""))
            throw new IllegalArgumentException("Argument "+argName+
                    " cannot be null or empty");
        return argValue;
    }

    OpenType(String className,String typeName,String description,
             boolean isArray){
        this.className=valid("className",className);
        this.typeName=valid("typeName",typeName);
        this.description=valid("description",description);
        this.isArray=isArray;
    }

    synchronized Descriptor getDescriptor(){
        if(descriptor==null){
            descriptor=new ImmutableDescriptor(new String[]{"openType"},
                    new Object[]{this});
        }
        return descriptor;
    }

    public String getClassName(){
        return className;
    }

    // A version of getClassName() that can only be called from within this
    // package and that cannot be overridden.
    String safeGetClassName(){
        return className;
    }

    public String getTypeName(){
        return typeName;
    }

    public String getDescription(){
        return description;
    }

    public boolean isArray(){
        return isArray;
    }

    public abstract boolean isValue(Object obj);

    boolean isAssignableFrom(OpenType<?> ot){
        return this.equals(ot);
    }

    public abstract int hashCode();

    public abstract boolean equals(Object obj);

    public abstract String toString();

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        checkClassNameOverride();
        ObjectInputStream.GetField fields=in.readFields();
        final String classNameField;
        final String descriptionField;
        final String typeNameField;
        try{
            classNameField=
                    validClassName((String)fields.get("className",null));
            descriptionField=
                    valid("description",(String)fields.get("description",null));
            typeNameField=
                    valid("typeName",(String)fields.get("typeName",null));
        }catch(Exception e){
            IOException e2=new InvalidObjectException(e.getMessage());
            e2.initCause(e);
            throw e2;
        }
        className=classNameField;
        description=descriptionField;
        typeName=typeNameField;
        isArray=(className.startsWith("["));
    }
}
