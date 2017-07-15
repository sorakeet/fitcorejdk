/**
 * Copyright (c) 2005, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.element;

public enum ElementKind{
    PACKAGE,
    // Declared types
    ENUM,
    CLASS,
    ANNOTATION_TYPE,
    INTERFACE,
    // Variables
    ENUM_CONSTANT,
    FIELD,
    PARAMETER,
    LOCAL_VARIABLE,
    EXCEPTION_PARAMETER,
    // Executables
    METHOD,
    CONSTRUCTOR,
    STATIC_INIT,
    INSTANCE_INIT,
    TYPE_PARAMETER,
    OTHER,
    RESOURCE_VARIABLE;

    public boolean isClass(){
        return this==CLASS||this==ENUM;
    }

    public boolean isInterface(){
        return this==INTERFACE||this==ANNOTATION_TYPE;
    }

    public boolean isField(){
        return this==FIELD||this==ENUM_CONSTANT;
    }
}
