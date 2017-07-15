/**
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.imageio.stream;

import sun.java2d.DisposerRecord;

import java.io.Closeable;
import java.io.IOException;

public class CloseableDisposerRecord implements DisposerRecord{
    private Closeable closeable;

    public CloseableDisposerRecord(Closeable closeable){
        this.closeable=closeable;
    }

    public synchronized void dispose(){
        if(closeable!=null){
            try{
                closeable.close();
            }catch(IOException e){
            }finally{
                closeable=null;
            }
        }
    }
}
