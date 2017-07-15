/**
 * Copyright (c) 2001, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.orbutil.concurrent;

public class SyncUtil{
    private SyncUtil(){
    }

    public static void acquire(Sync sync){
        boolean held=false;
        while(!held){
            try{
                sync.acquire();
                held=true;
            }catch(InterruptedException exc){
                held=false;
            }
        }
    }
}
