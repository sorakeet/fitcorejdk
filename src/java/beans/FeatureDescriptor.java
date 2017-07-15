/**
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans;

import com.sun.beans.TypeResolver;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map.Entry;

public class FeatureDescriptor{
    private static final String TRANSIENT="transient";
    private Reference<? extends Class<?>> classRef;
    private boolean expert;
    private boolean hidden;
    private boolean preferred;
    private String shortDescription;
    private String name;
    private String displayName;
    private Hashtable<String,Object> table;

    public FeatureDescriptor(){
    }

    FeatureDescriptor(FeatureDescriptor x,FeatureDescriptor y){
        expert=x.expert|y.expert;
        hidden=x.hidden|y.hidden;
        preferred=x.preferred|y.preferred;
        name=y.name;
        shortDescription=x.shortDescription;
        if(y.shortDescription!=null){
            shortDescription=y.shortDescription;
        }
        displayName=x.displayName;
        if(y.displayName!=null){
            displayName=y.displayName;
        }
        classRef=x.classRef;
        if(y.classRef!=null){
            classRef=y.classRef;
        }
        addTable(x.table);
        addTable(y.table);
    }

    private void addTable(Hashtable<String,Object> table){
        if((table!=null)&&!table.isEmpty()){
            getTable().putAll(table);
        }
    }

    private Hashtable<String,Object> getTable(){
        if(this.table==null){
            this.table=new Hashtable<>();
        }
        return this.table;
    }

    FeatureDescriptor(FeatureDescriptor old){
        expert=old.expert;
        hidden=old.hidden;
        preferred=old.preferred;
        name=old.name;
        shortDescription=old.shortDescription;
        displayName=old.displayName;
        classRef=old.classRef;
        addTable(old.table);
    }

    static <T> Reference<T> getSoftReference(T object){
        return (object!=null)
                ?new SoftReference<>(object)
                :null;
    }

    static Class<?> getReturnType(Class<?> base,Method method){
        if(base==null){
            base=method.getDeclaringClass();
        }
        return TypeResolver.erase(TypeResolver.resolveInClass(base,method.getGenericReturnType()));
    }

    static Class<?>[] getParameterTypes(Class<?> base,Method method){
        if(base==null){
            base=method.getDeclaringClass();
        }
        return TypeResolver.erase(TypeResolver.resolveInClass(base,method.getGenericParameterTypes()));
    }

    static void appendTo(StringBuilder sb,String name,Reference<?> reference){
        if(reference!=null){
            appendTo(sb,name,reference.get());
        }
    }

    static void appendTo(StringBuilder sb,String name,Object value){
        if(value!=null){
            sb.append("; ").append(name).append("=").append(value);
        }
    }

    public boolean isExpert(){
        return expert;
    }

    public void setExpert(boolean expert){
        this.expert=expert;
    }

    public boolean isHidden(){
        return hidden;
    }

    public void setHidden(boolean hidden){
        this.hidden=hidden;
    }

    public boolean isPreferred(){
        return preferred;
    }
    // Package private methods for recreating the weak/soft referent

    public void setPreferred(boolean preferred){
        this.preferred=preferred;
    }

    public String getShortDescription(){
        if(shortDescription==null){
            return getDisplayName();
        }
        return shortDescription;
    }

    public String getDisplayName(){
        if(displayName==null){
            return getName();
        }
        return displayName;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name=name;
    }

    public void setDisplayName(String displayName){
        this.displayName=displayName;
    }

    public void setShortDescription(String text){
        shortDescription=text;
    }

    public Enumeration<String> attributeNames(){
        return getTable().keys();
    }

    boolean isTransient(){
        Object value=getValue(TRANSIENT);
        return (value instanceof Boolean)
                ?(Boolean)value
                :false;
    }

    void setTransient(Transient annotation){
        if((annotation!=null)&&(null==getValue(TRANSIENT))){
            setValue(TRANSIENT,annotation.value());
        }
    }

    public void setValue(String attributeName,Object value){
        getTable().put(attributeName,value);
    }

    public Object getValue(String attributeName){
        return (this.table!=null)
                ?this.table.get(attributeName)
                :null;
    }

    Class<?> getClass0(){
        return (this.classRef!=null)
                ?this.classRef.get()
                :null;
    }

    void setClass0(Class<?> cls){
        this.classRef=getWeakReference(cls);
    }

    static <T> Reference<T> getWeakReference(T object){
        return (object!=null)
                ?new WeakReference<>(object)
                :null;
    }

    public String toString(){
        StringBuilder sb=new StringBuilder(getClass().getName());
        sb.append("[name=").append(this.name);
        appendTo(sb,"displayName",this.displayName);
        appendTo(sb,"shortDescription",this.shortDescription);
        appendTo(sb,"preferred",this.preferred);
        appendTo(sb,"hidden",this.hidden);
        appendTo(sb,"expert",this.expert);
        if((this.table!=null)&&!this.table.isEmpty()){
            sb.append("; values={");
            for(Entry<String,Object> entry : this.table.entrySet()){
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("; ");
            }
            sb.setLength(sb.length()-2);
            sb.append("}");
        }
        appendTo(sb);
        return sb.append("]").toString();
    }

    void appendTo(StringBuilder sb){
    }

    static void appendTo(StringBuilder sb,String name,boolean value){
        if(value){
            sb.append("; ").append(name);
        }
    }
}
