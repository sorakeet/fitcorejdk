/**
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.imageio.stream;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;

public class StreamFinalizer{
    private ImageInputStream stream;

    public StreamFinalizer(ImageInputStream stream){
        this.stream=stream;
    }

    protected void finalize() throws Throwable{
        try{
            stream.close();
        }catch(IOException e){
        }finally{
            stream=null;
            super.finalize();
        }
    }
}
