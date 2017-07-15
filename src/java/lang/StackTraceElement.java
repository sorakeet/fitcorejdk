/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang;

import java.util.Objects;

public final class StackTraceElement implements java.io.Serializable{
    private static final long serialVersionUID=6992337162326171013L;
    // Normally initialized by VM (public constructor added in 1.5)
    private String declaringClass;
    private String methodName;
    private String fileName;
    private int lineNumber;

    public StackTraceElement(String declaringClass,String methodName,
                             String fileName,int lineNumber){
        this.declaringClass=Objects.requireNonNull(declaringClass,"Declaring class is null");
        this.methodName=Objects.requireNonNull(methodName,"Method name is null");
        this.fileName=fileName;
        this.lineNumber=lineNumber;
    }

    public String getFileName(){
        return fileName;
    }

    public int getLineNumber(){
        return lineNumber;
    }

    public String getMethodName(){
        return methodName;
    }

    public int hashCode(){
        int result=31*declaringClass.hashCode()+methodName.hashCode();
        result=31*result+Objects.hashCode(fileName);
        result=31*result+lineNumber;
        return result;
    }

    public boolean equals(Object obj){
        if(obj==this)
            return true;
        if(!(obj instanceof StackTraceElement))
            return false;
        StackTraceElement e=(StackTraceElement)obj;
        return e.declaringClass.equals(declaringClass)&&
                e.lineNumber==lineNumber&&
                Objects.equals(methodName,e.methodName)&&
                Objects.equals(fileName,e.fileName);
    }

    public String toString(){
        return getClassName()+"."+methodName+
                (isNativeMethod()?"(Native Method)":
                        (fileName!=null&&lineNumber>=0?
                                "("+fileName+":"+lineNumber+")":
                                (fileName!=null?"("+fileName+")":"(Unknown Source)")));
    }

    public String getClassName(){
        return declaringClass;
    }

    public boolean isNativeMethod(){
        return lineNumber==-2;
    }
}
