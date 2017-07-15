/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AttributeList extends ArrayList<Object>{
    private static final long serialVersionUID=-4077085769279709076L;
    private transient volatile boolean typeSafe;
    private transient volatile boolean tainted;

    public AttributeList(){
        super();
    }

    public AttributeList(int initialCapacity){
        super(initialCapacity);
    }

    public AttributeList(AttributeList list){
        super(list);
    }

    public AttributeList(List<Attribute> list){
        // Check for null parameter
        //
        if(list==null)
            throw new IllegalArgumentException("Null parameter");
        // Check for non-Attribute objects
        //
        adding(list);
        // Build the List<Attribute>
        //
        super.addAll(list);
    }

    private void adding(Collection<?> c){
        for(Object x : c)
            adding(x);
    }

    private void adding(Object x){
        if(x==null||x instanceof Attribute)
            return;
        if(typeSafe)
            throw new IllegalArgumentException("Not an Attribute: "+x);
        else
            tainted=true;
    }

    @SuppressWarnings("unchecked")
    public List<Attribute> asList(){
        typeSafe=true;
        if(tainted)
            adding((Collection<?>)this);  // will throw IllegalArgumentException
        return (List<Attribute>)(List<?>)this;
    }

    public void add(Attribute object){
        super.add(object);
    }

    public void add(int index,Attribute object){
        try{
            super.add(index,object);
        }catch(IndexOutOfBoundsException e){
            throw new RuntimeOperationsException(e,
                    "The specified index is out of range");
        }
    }

    public void set(int index,Attribute object){
        try{
            super.set(index,object);
        }catch(IndexOutOfBoundsException e){
            throw new RuntimeOperationsException(e,
                    "The specified index is out of range");
        }
    }

    public boolean addAll(AttributeList list){
        return (super.addAll(list));
    }

    public boolean addAll(int index,AttributeList list){
        try{
            return super.addAll(index,list);
        }catch(IndexOutOfBoundsException e){
            throw new RuntimeOperationsException(e,
                    "The specified index is out of range");
        }
    }

    @Override
    public Object set(int index,Object element){
        adding(element);
        return super.set(index,element);
    }

    @Override
    public boolean add(Object element){
        adding(element);
        return super.add(element);
    }

    @Override
    public void add(int index,Object element){
        adding(element);
        super.add(index,element);
    }

    @Override
    public boolean addAll(Collection<?> c){
        adding(c);
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index,Collection<?> c){
        adding(c);
        return super.addAll(index,c);
    }
}
