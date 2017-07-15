/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.source.util;

import com.sun.source.tree.CompilationUnitTree;

import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

@jdk.Exported
public final class TaskEvent{
    private Kind kind;

    ;
    private JavaFileObject file;
    private CompilationUnitTree unit;
    private TypeElement clazz;

    public TaskEvent(Kind kind){
        this(kind,null,null,null);
    }

    private TaskEvent(Kind kind,JavaFileObject file,CompilationUnitTree unit,TypeElement clazz){
        this.kind=kind;
        this.file=file;
        this.unit=unit;
        this.clazz=clazz;
    }

    public TaskEvent(Kind kind,JavaFileObject sourceFile){
        this(kind,sourceFile,null,null);
    }

    public TaskEvent(Kind kind,CompilationUnitTree unit){
        this(kind,unit.getSourceFile(),unit,null);
    }

    public TaskEvent(Kind kind,CompilationUnitTree unit,TypeElement clazz){
        this(kind,unit.getSourceFile(),unit,clazz);
    }

    public Kind getKind(){
        return kind;
    }

    public JavaFileObject getSourceFile(){
        return file;
    }

    public CompilationUnitTree getCompilationUnit(){
        return unit;
    }

    public TypeElement getTypeElement(){
        return clazz;
    }

    public String toString(){
        return "TaskEvent["
                +kind+","
                +file+","
                // the compilation unit is identified by the file
                +clazz+"]";
    }
    @jdk.Exported
    public enum Kind{
        PARSE,
        ENTER,
        ANALYZE,
        GENERATE,
        ANNOTATION_PROCESSING,
        ANNOTATION_PROCESSING_ROUND
    }
}
