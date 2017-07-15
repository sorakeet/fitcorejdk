/**
 * Copyright (c) 2003, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans;

public class IndexedPropertyChangeEvent extends PropertyChangeEvent{
    private static final long serialVersionUID=-320227448495806870L;
    private int index;

    public IndexedPropertyChangeEvent(Object source,String propertyName,
                                      Object oldValue,Object newValue,
                                      int index){
        super(source,propertyName,oldValue,newValue);
        this.index=index;
    }

    void appendTo(StringBuilder sb){
        sb.append("; index=").append(getIndex());
    }

    public int getIndex(){
        return index;
    }
}
