/**
 * Copyright (c) 1996, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans;

import sun.reflect.misc.ReflectUtil;

import java.lang.ref.Reference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class PropertyDescriptor extends FeatureDescriptor{
    private final MethodRef readMethodRef=new MethodRef();
    private final MethodRef writeMethodRef=new MethodRef();
    private Reference<? extends Class<?>> propertyTypeRef;
    private Reference<? extends Class<?>> propertyEditorClassRef;
    private boolean bound;
    private boolean constrained;
    // The base name of the method name which will be prefixed with the
    // read and write method. If name == "foo" then the baseName is "Foo"
    private String baseName;
    private String writeMethodName;
    private String readMethodName;

    public PropertyDescriptor(String propertyName,Class<?> beanClass)
            throws IntrospectionException{
        this(propertyName,beanClass,
                Introspector.IS_PREFIX+NameGenerator.capitalize(propertyName),
                Introspector.SET_PREFIX+NameGenerator.capitalize(propertyName));
    }

    public PropertyDescriptor(String propertyName,Class<?> beanClass,
                              String readMethodName,String writeMethodName)
            throws IntrospectionException{
        if(beanClass==null){
            throw new IntrospectionException("Target Bean class is null");
        }
        if(propertyName==null||propertyName.length()==0){
            throw new IntrospectionException("bad property name");
        }
        if("".equals(readMethodName)||"".equals(writeMethodName)){
            throw new IntrospectionException("read or write method name should not be the empty string");
        }
        setName(propertyName);
        setClass0(beanClass);
        this.readMethodName=readMethodName;
        if(readMethodName!=null&&getReadMethod()==null){
            throw new IntrospectionException("Method not found: "+readMethodName);
        }
        this.writeMethodName=writeMethodName;
        if(writeMethodName!=null&&getWriteMethod()==null){
            throw new IntrospectionException("Method not found: "+writeMethodName);
        }
        // If this class or one of its base classes allow PropertyChangeListener,
        // then we assume that any properties we discover are "bound".
        // See Introspector.getTargetPropertyInfo() method.
        Class[] args={PropertyChangeListener.class};
        this.bound=null!=Introspector.findMethod(beanClass,"addPropertyChangeListener",args.length,args);
    }

    public PropertyDescriptor(String propertyName,Method readMethod,Method writeMethod)
            throws IntrospectionException{
        if(propertyName==null||propertyName.length()==0){
            throw new IntrospectionException("bad property name");
        }
        setName(propertyName);
        setReadMethod(readMethod);
        setWriteMethod(writeMethod);
    }

    PropertyDescriptor(Class<?> bean,String base,Method read,Method write) throws IntrospectionException{
        if(bean==null){
            throw new IntrospectionException("Target Bean class is null");
        }
        setClass0(bean);
        setName(Introspector.decapitalize(base));
        setReadMethod(read);
        setWriteMethod(write);
        this.baseName=base;
    }

    PropertyDescriptor(PropertyDescriptor x,PropertyDescriptor y){
        super(x,y);
        if(y.baseName!=null){
            baseName=y.baseName;
        }else{
            baseName=x.baseName;
        }
        if(y.readMethodName!=null){
            readMethodName=y.readMethodName;
        }else{
            readMethodName=x.readMethodName;
        }
        if(y.writeMethodName!=null){
            writeMethodName=y.writeMethodName;
        }else{
            writeMethodName=x.writeMethodName;
        }
        if(y.propertyTypeRef!=null){
            propertyTypeRef=y.propertyTypeRef;
        }else{
            propertyTypeRef=x.propertyTypeRef;
        }
        // Figure out the merged read method.
        Method xr=x.getReadMethod();
        Method yr=y.getReadMethod();
        // Normally give priority to y's readMethod.
        try{
            if(isAssignable(xr,yr)){
                setReadMethod(yr);
            }else{
                setReadMethod(xr);
            }
        }catch(IntrospectionException ex){
            // fall through
        }
        // However, if both x and y reference read methods in the same class,
        // give priority to a boolean "is" method over a boolean "get" method.
        if(xr!=null&&yr!=null&&
                xr.getDeclaringClass()==yr.getDeclaringClass()&&
                getReturnType(getClass0(),xr)==boolean.class&&
                getReturnType(getClass0(),yr)==boolean.class&&
                xr.getName().indexOf(Introspector.IS_PREFIX)==0&&
                yr.getName().indexOf(Introspector.GET_PREFIX)==0){
            try{
                setReadMethod(xr);
            }catch(IntrospectionException ex){
                // fall through
            }
        }
        Method xw=x.getWriteMethod();
        Method yw=y.getWriteMethod();
        try{
            if(yw!=null){
                setWriteMethod(yw);
            }else{
                setWriteMethod(xw);
            }
        }catch(IntrospectionException ex){
            // Fall through
        }
        if(y.getPropertyEditorClass()!=null){
            setPropertyEditorClass(y.getPropertyEditorClass());
        }else{
            setPropertyEditorClass(x.getPropertyEditorClass());
        }
        bound=x.bound|y.bound;
        constrained=x.constrained|y.constrained;
    }

    private boolean isAssignable(Method m1,Method m2){
        if(m1==null){
            return true; // choose second method
        }
        if(m2==null){
            return false; // choose first method
        }
        if(!m1.getName().equals(m2.getName())){
            return true; // choose second method by default
        }
        Class<?> type1=m1.getDeclaringClass();
        Class<?> type2=m2.getDeclaringClass();
        if(!type1.isAssignableFrom(type2)){
            return false; // choose first method: it declared later
        }
        type1=getReturnType(getClass0(),m1);
        type2=getReturnType(getClass0(),m2);
        if(!type1.isAssignableFrom(type2)){
            return false; // choose first method: it overrides return type
        }
        Class<?>[] args1=getParameterTypes(getClass0(),m1);
        Class<?>[] args2=getParameterTypes(getClass0(),m2);
        if(args1.length!=args2.length){
            return true; // choose second method by default
        }
        for(int i=0;i<args1.length;i++){
            if(!args1[i].isAssignableFrom(args2[i])){
                return false; // choose first method: it overrides parameter
            }
        }
        return true; // choose second method
    }

    PropertyDescriptor(PropertyDescriptor old){
        super(old);
        propertyTypeRef=old.propertyTypeRef;
        this.readMethodRef.set(old.readMethodRef.get());
        this.writeMethodRef.set(old.writeMethodRef.get());
        propertyEditorClassRef=old.propertyEditorClassRef;
        writeMethodName=old.writeMethodName;
        readMethodName=old.readMethodName;
        baseName=old.baseName;
        bound=old.bound;
        constrained=old.constrained;
    }

    public synchronized Class<?> getPropertyType(){
        Class<?> type=getPropertyType0();
        if(type==null){
            try{
                type=findPropertyType(getReadMethod(),getWriteMethod());
                setPropertyType(type);
            }catch(IntrospectionException ex){
                // Fall
            }
        }
        return type;
    }

    private void setPropertyType(Class<?> type){
        this.propertyTypeRef=getWeakReference(type);
    }

    public synchronized Method getWriteMethod(){
        Method writeMethod=this.writeMethodRef.get();
        if(writeMethod==null){
            Class<?> cls=getClass0();
            if(cls==null||(writeMethodName==null&&!this.writeMethodRef.isSet())){
                // The write method was explicitly set to null.
                return null;
            }
            // We need the type to fetch the correct method.
            Class<?> type=getPropertyType0();
            if(type==null){
                try{
                    // Can't use getPropertyType since it will lead to recursive loop.
                    type=findPropertyType(getReadMethod(),null);
                    setPropertyType(type);
                }catch(IntrospectionException ex){
                    // Without the correct property type we can't be guaranteed
                    // to find the correct method.
                    return null;
                }
            }
            if(writeMethodName==null){
                writeMethodName=Introspector.SET_PREFIX+getBaseName();
            }
            Class<?>[] args=(type==null)?null:new Class<?>[]{type};
            writeMethod=Introspector.findMethod(cls,writeMethodName,1,args);
            if(writeMethod!=null){
                if(!writeMethod.getReturnType().equals(void.class)){
                    writeMethod=null;
                }
            }
            try{
                setWriteMethod(writeMethod);
            }catch(IntrospectionException ex){
                // fall through
            }
        }
        return writeMethod;
    }

    public synchronized void setWriteMethod(Method writeMethod)
            throws IntrospectionException{
        this.writeMethodRef.set(writeMethod);
        if(writeMethod==null){
            writeMethodName=null;
            return;
        }
        // Set the property type - which validates the method
        setPropertyType(findPropertyType(getReadMethod(),writeMethod));
        setClass0(writeMethod.getDeclaringClass());
        writeMethodName=writeMethod.getName();
        setTransient(writeMethod.getAnnotation(Transient.class));
    }

    public synchronized Method getReadMethod(){
        Method readMethod=this.readMethodRef.get();
        if(readMethod==null){
            Class<?> cls=getClass0();
            if(cls==null||(readMethodName==null&&!this.readMethodRef.isSet())){
                // The read method was explicitly set to null.
                return null;
            }
            String nextMethodName=Introspector.GET_PREFIX+getBaseName();
            if(readMethodName==null){
                Class<?> type=getPropertyType0();
                if(type==boolean.class||type==null){
                    readMethodName=Introspector.IS_PREFIX+getBaseName();
                }else{
                    readMethodName=nextMethodName;
                }
            }
            // Since there can be multiple write methods but only one getter
            // method, find the getter method first so that you know what the
            // property type is.  For booleans, there can be "is" and "get"
            // methods.  If an "is" method exists, this is the official
            // reader method so look for this one first.
            readMethod=Introspector.findMethod(cls,readMethodName,0);
            if((readMethod==null)&&!readMethodName.equals(nextMethodName)){
                readMethodName=nextMethodName;
                readMethod=Introspector.findMethod(cls,readMethodName,0);
            }
            try{
                setReadMethod(readMethod);
            }catch(IntrospectionException ex){
                // fall
            }
        }
        return readMethod;
    }

    private Class<?> getPropertyType0(){
        return (this.propertyTypeRef!=null)
                ?this.propertyTypeRef.get()
                :null;
    }

    public synchronized void setReadMethod(Method readMethod)
            throws IntrospectionException{
        this.readMethodRef.set(readMethod);
        if(readMethod==null){
            readMethodName=null;
            return;
        }
        // The property type is determined by the read method.
        setPropertyType(findPropertyType(readMethod,this.writeMethodRef.get()));
        setClass0(readMethod.getDeclaringClass());
        readMethodName=readMethod.getName();
        setTransient(readMethod.getAnnotation(Transient.class));
    }

    void setClass0(Class<?> clz){
        if(getClass0()!=null&&clz.isAssignableFrom(getClass0())){
            // don't replace a subclass with a superclass
            return;
        }
        super.setClass0(clz);
    }

    void appendTo(StringBuilder sb){
        appendTo(sb,"bound",this.bound);
        appendTo(sb,"constrained",this.constrained);
        appendTo(sb,"propertyEditorClass",this.propertyEditorClassRef);
        appendTo(sb,"propertyType",this.propertyTypeRef);
        appendTo(sb,"readMethod",this.readMethodRef.get());
        appendTo(sb,"writeMethod",this.writeMethodRef.get());
    }

    private Class<?> findPropertyType(Method readMethod,Method writeMethod)
            throws IntrospectionException{
        Class<?> propertyType=null;
        try{
            if(readMethod!=null){
                Class<?>[] params=getParameterTypes(getClass0(),readMethod);
                if(params.length!=0){
                    throw new IntrospectionException("bad read method arg count: "
                            +readMethod);
                }
                propertyType=getReturnType(getClass0(),readMethod);
                if(propertyType==Void.TYPE){
                    throw new IntrospectionException("read method "+
                            readMethod.getName()+" returns void");
                }
            }
            if(writeMethod!=null){
                Class<?>[] params=getParameterTypes(getClass0(),writeMethod);
                if(params.length!=1){
                    throw new IntrospectionException("bad write method arg count: "
                            +writeMethod);
                }
                if(propertyType!=null&&!params[0].isAssignableFrom(propertyType)){
                    throw new IntrospectionException("type mismatch between read and write methods");
                }
                propertyType=params[0];
            }
        }catch(IntrospectionException ex){
            throw ex;
        }
        return propertyType;
    }

    // Calculate once since capitalize() is expensive.
    String getBaseName(){
        if(baseName==null){
            baseName=NameGenerator.capitalize(getName());
        }
        return baseName;
    }

    public boolean isBound(){
        return bound;
    }

    public void setBound(boolean bound){
        this.bound=bound;
    }

    public boolean isConstrained(){
        return constrained;
    }

    public void setConstrained(boolean constrained){
        this.constrained=constrained;
    }

    public PropertyEditor createPropertyEditor(Object bean){
        Object editor=null;
        final Class<?> cls=getPropertyEditorClass();
        if(cls!=null&&PropertyEditor.class.isAssignableFrom(cls)
                &&ReflectUtil.isPackageAccessible(cls)){
            Constructor<?> ctor=null;
            if(bean!=null){
                try{
                    ctor=cls.getConstructor(new Class<?>[]{Object.class});
                }catch(Exception ex){
                    // Fall through
                }
            }
            try{
                if(ctor==null){
                    editor=cls.newInstance();
                }else{
                    editor=ctor.newInstance(new Object[]{bean});
                }
            }catch(Exception ex){
                // Fall through
            }
        }
        return (PropertyEditor)editor;
    }

    public Class<?> getPropertyEditorClass(){
        return (this.propertyEditorClassRef!=null)
                ?this.propertyEditorClassRef.get()
                :null;
    }

    public void setPropertyEditorClass(Class<?> propertyEditorClass){
        this.propertyEditorClassRef=getWeakReference(propertyEditorClass);
    }

    boolean compareMethods(Method a,Method b){
        // Note: perhaps this should be a protected method in FeatureDescriptor
        if((a==null)!=(b==null)){
            return false;
        }
        if(a!=null&&b!=null){
            if(!a.equals(b)){
                return false;
            }
        }
        return true;
    }

    void updateGenericsFor(Class<?> type){
        setClass0(type);
        try{
            setPropertyType(findPropertyType(this.readMethodRef.get(),this.writeMethodRef.get()));
        }catch(IntrospectionException exception){
            setPropertyType(null);
        }
    }

    public int hashCode(){
        int result=7;
        result=37*result+((getPropertyType()==null)?0:
                getPropertyType().hashCode());
        result=37*result+((getReadMethod()==null)?0:
                getReadMethod().hashCode());
        result=37*result+((getWriteMethod()==null)?0:
                getWriteMethod().hashCode());
        result=37*result+((getPropertyEditorClass()==null)?0:
                getPropertyEditorClass().hashCode());
        result=37*result+((writeMethodName==null)?0:
                writeMethodName.hashCode());
        result=37*result+((readMethodName==null)?0:
                readMethodName.hashCode());
        result=37*result+getName().hashCode();
        result=37*result+((bound==false)?0:1);
        result=37*result+((constrained==false)?0:1);
        return result;
    }

    public boolean equals(Object obj){
        if(this==obj){
            return true;
        }
        if(obj!=null&&obj instanceof PropertyDescriptor){
            PropertyDescriptor other=(PropertyDescriptor)obj;
            Method otherReadMethod=other.getReadMethod();
            Method otherWriteMethod=other.getWriteMethod();
            if(!compareMethods(getReadMethod(),otherReadMethod)){
                return false;
            }
            if(!compareMethods(getWriteMethod(),otherWriteMethod)){
                return false;
            }
            if(getPropertyType()==other.getPropertyType()&&
                    getPropertyEditorClass()==other.getPropertyEditorClass()&&
                    bound==other.isBound()&&constrained==other.isConstrained()&&
                    writeMethodName==other.writeMethodName&&
                    readMethodName==other.readMethodName){
                return true;
            }
        }
        return false;
    }
}
