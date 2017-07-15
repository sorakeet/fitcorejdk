/**
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.management;

public enum MemoryType{
    HEAP("Heap memory"),
    NON_HEAP("Non-heap memory");
    private static final long serialVersionUID=6992337162326171013L;
    private final String description;

    private MemoryType(String s){
        this.description=s;
    }

    public String toString(){
        return description;
    }
}
