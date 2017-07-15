/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.management;

import sun.management.LockInfoCompositeData;

import javax.management.openmbean.CompositeData;

public class LockInfo{
    private String className;
    private int identityHashCode;

    public LockInfo(String className,int identityHashCode){
        if(className==null){
            throw new NullPointerException("Parameter className cannot be null");
        }
        this.className=className;
        this.identityHashCode=identityHashCode;
    }

    LockInfo(Object lock){
        this.className=lock.getClass().getName();
        this.identityHashCode=System.identityHashCode(lock);
    }

    public static LockInfo from(CompositeData cd){
        if(cd==null){
            return null;
        }
        if(cd instanceof LockInfoCompositeData){
            return ((LockInfoCompositeData)cd).getLockInfo();
        }else{
            return LockInfoCompositeData.toLockInfo(cd);
        }
    }

    public String getClassName(){
        return className;
    }

    public int getIdentityHashCode(){
        return identityHashCode;
    }

    public String toString(){
        return className+'@'+Integer.toHexString(identityHashCode);
    }
}