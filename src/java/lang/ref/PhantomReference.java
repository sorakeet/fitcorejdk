/**
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.ref;

public class PhantomReference<T> extends Reference<T>{
    public PhantomReference(T referent,ReferenceQueue<? super T> q){
        super(referent,q);
    }

    public T get(){
        return null;
    }
}
