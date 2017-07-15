/**
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.reflect;

import sun.reflect.annotation.AnnotationSupport;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Parameter implements AnnotatedElement{
    private final String name;
    private final int modifiers;
    private final Executable executable;
    private final int index;
    private transient volatile Type parameterTypeCache=null;
    private transient volatile Class<?> parameterClassCache=null;
    private transient Map<Class<? extends Annotation>,Annotation> declaredAnnotations;

    Parameter(String name,
              int modifiers,
              Executable executable,
              int index){
        this.name=name;
        this.modifiers=modifiers;
        this.executable=executable;
        this.index=index;
    }

    public int hashCode(){
        return executable.hashCode()^index;
    }

    public boolean equals(Object obj){
        if(obj instanceof Parameter){
            Parameter other=(Parameter)obj;
            return (other.executable.equals(executable)&&
                    other.index==index);
        }
        return false;
    }

    public String toString(){
        final StringBuilder sb=new StringBuilder();
        final Type type=getParameterizedType();
        final String typename=type.getTypeName();
        sb.append(Modifier.toString(getModifiers()));
        if(0!=modifiers)
            sb.append(' ');
        if(isVarArgs())
            sb.append(typename.replaceFirst("\\[\\]$","..."));
        else
            sb.append(typename);
        sb.append(' ');
        sb.append(getName());
        return sb.toString();
    }

    public int getModifiers(){
        return modifiers;
    }

    public String getName(){
        // Note: empty strings as paramete names are now outlawed.
        // The .equals("") is for compatibility with current JVM
        // behavior.  It may be removed at some point.
        if(name==null||name.equals(""))
            return "arg"+index;
        else
            return name;
    }

    public Type getParameterizedType(){
        Type tmp=parameterTypeCache;
        if(null==tmp){
            tmp=executable.getAllGenericParameterTypes()[index];
            parameterTypeCache=tmp;
        }
        return tmp;
    }

    public boolean isVarArgs(){
        return executable.isVarArgs()&&
                index==executable.getParameterCount()-1;
    }

    public boolean isNamePresent(){
        return executable.hasRealParameterData()&&name!=null;
    }

    public Executable getDeclaringExecutable(){
        return executable;
    }

    // Package-private accessor to the real name field.
    String getRealName(){
        return name;
    }

    public Class<?> getType(){
        Class<?> tmp=parameterClassCache;
        if(null==tmp){
            tmp=executable.getParameterTypes()[index];
            parameterClassCache=tmp;
        }
        return tmp;
    }

    public AnnotatedType getAnnotatedType(){
        // no caching for now
        return executable.getAnnotatedParameterTypes()[index];
    }

    public boolean isImplicit(){
        return Modifier.isMandated(getModifiers());
    }

    public boolean isSynthetic(){
        return Modifier.isSynthetic(getModifiers());
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass){
        Objects.requireNonNull(annotationClass);
        return annotationClass.cast(declaredAnnotations().get(annotationClass));
    }

    @Override
    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass){
        Objects.requireNonNull(annotationClass);
        return AnnotationSupport.getDirectlyAndIndirectlyPresent(declaredAnnotations(),annotationClass);
    }

    public Annotation[] getDeclaredAnnotations(){
        return executable.getParameterAnnotations()[index];
    }

    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass){
        // Only annotations on classes are inherited, for all other
        // objects getDeclaredAnnotation is the same as
        // getAnnotation.
        return getAnnotation(annotationClass);
    }

    @Override
    public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass){
        // Only annotations on classes are inherited, for all other
        // objects getDeclaredAnnotations is the same as
        // getAnnotations.
        return getAnnotationsByType(annotationClass);
    }

    public Annotation[] getAnnotations(){
        return getDeclaredAnnotations();
    }

    private synchronized Map<Class<? extends Annotation>,Annotation> declaredAnnotations(){
        if(null==declaredAnnotations){
            declaredAnnotations=
                    new HashMap<Class<? extends Annotation>,Annotation>();
            Annotation[] ann=getDeclaredAnnotations();
            for(int i=0;i<ann.length;i++)
                declaredAnnotations.put(ann[i].annotationType(),ann[i]);
        }
        return declaredAnnotations;
    }
}
