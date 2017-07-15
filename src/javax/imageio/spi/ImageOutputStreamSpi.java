/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio.spi;

import javax.imageio.stream.ImageOutputStream;
import java.io.File;
import java.io.IOException;

public abstract class ImageOutputStreamSpi extends IIOServiceProvider{
    protected Class<?> outputClass;

    protected ImageOutputStreamSpi(){
    }

    public ImageOutputStreamSpi(String vendorName,
                                String version,
                                Class<?> outputClass){
        super(vendorName,version);
        this.outputClass=outputClass;
    }

    public Class<?> getOutputClass(){
        return outputClass;
    }

    public boolean canUseCacheFile(){
        return false;
    }

    public boolean needsCacheFile(){
        return false;
    }

    public ImageOutputStream createOutputStreamInstance(Object output)
            throws IOException{
        return createOutputStreamInstance(output,true,null);
    }

    public abstract ImageOutputStream createOutputStreamInstance(Object output,
                                                                 boolean useCache,
                                                                 File cacheDir)
            throws IOException;
}
