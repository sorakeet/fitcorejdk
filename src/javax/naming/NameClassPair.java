/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

public class NameClassPair implements java.io.Serializable{
    private static final long serialVersionUID=5620776610160863339L;
    private String name;
    private String className;
    private String fullName=null;
    private boolean isRel=true;

    public NameClassPair(String name,String className){
        this.name=name;
        this.className=className;
    }

    public NameClassPair(String name,String className,boolean isRelative){
        this.name=name;
        this.className=className;
        this.isRel=isRelative;
    }

    public String getNameInNamespace(){
        if(fullName==null){
            throw new UnsupportedOperationException();
        }
        return fullName;
    }

    public void setNameInNamespace(String fullName){
        this.fullName=fullName;
    }

    public String toString(){
        return (isRelative()?"":"(not relative)")+getName()+": "+
                getClassName();
    }

    public String getClassName(){
        return className;
    }

    public void setClassName(String name){
        this.className=name;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name=name;
    }

    public boolean isRelative(){
        return isRel;
    }

    public void setRelative(boolean r){
        isRel=r;
    }
}
