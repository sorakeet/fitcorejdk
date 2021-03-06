/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.ior;

import com.sun.corba.se.spi.ior.MakeImmutable;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

public class FreezableList extends AbstractList{
    private List delegate=null;
    private boolean immutable=false;

    public FreezableList(List delegate){
        this(delegate,false);
    }

    public FreezableList(List delegate,boolean immutable){
        this.delegate=delegate;
        this.immutable=immutable;
    }

    public void makeImmutable(){
        immutable=true;
    }

    public boolean isImmutable(){
        return immutable;
    }

    public void makeElementsImmutable(){
        Iterator iter=iterator();
        while(iter.hasNext()){
            Object obj=iter.next();
            if(obj instanceof MakeImmutable){
                MakeImmutable element=(MakeImmutable)obj;
                element.makeImmutable();
            }
        }
    }

    public int size(){
        return delegate.size();
    }

    public Object get(int index){
        return delegate.get(index);
    }
    // Methods overridden from AbstractList

    public Object set(int index,Object element){
        if(immutable)
            throw new UnsupportedOperationException();
        return delegate.set(index,element);
    }

    public void add(int index,Object element){
        if(immutable)
            throw new UnsupportedOperationException();
        delegate.add(index,element);
    }

    public Object remove(int index){
        if(immutable)
            throw new UnsupportedOperationException();
        return delegate.remove(index);
    }

    // We also override subList so that the result is a FreezableList.
    public List subList(int fromIndex,int toIndex){
        List list=delegate.subList(fromIndex,toIndex);
        List result=new FreezableList(list,immutable);
        return result;
    }

    public boolean equals(Object obj){
        if(obj==null)
            return false;
        if(!(obj instanceof FreezableList))
            return false;
        FreezableList other=(FreezableList)obj;
        return delegate.equals(other.delegate)&&
                (immutable==other.immutable);
    }

    public int hashCode(){
        return delegate.hashCode();
    }
}
