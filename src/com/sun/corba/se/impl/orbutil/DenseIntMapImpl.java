/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.orbutil;

import java.util.ArrayList;

public class DenseIntMapImpl{
    private ArrayList list=new ArrayList();

    public Object get(int key){
        checkKey(key);
        Object result=null;
        if(key<list.size())
            result=list.get(key);
        return result;
    }

    private void checkKey(int key){
        if(key<0)
            throw new IllegalArgumentException("Key must be >= 0.");
    }

    public void set(int key,Object value){
        checkKey(key);
        extend(key);
        list.set(key,value);
    }

    private void extend(int index){
        if(index>=list.size()){
            list.ensureCapacity(index+1);
            int max=list.size();
            while(max++<=index)
                list.add(null);
        }
    }
}
