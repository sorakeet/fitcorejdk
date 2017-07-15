/**
 * Copyright (c) 1998, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang;

public class InheritableThreadLocal<T> extends ThreadLocal<T>{
    ThreadLocalMap getMap(Thread t){
        return t.inheritableThreadLocals;
    }

    void createMap(Thread t,T firstValue){
        t.inheritableThreadLocals=new ThreadLocalMap(this,firstValue);
    }

    protected T childValue(T parentValue){
        return parentValue;
    }
}
