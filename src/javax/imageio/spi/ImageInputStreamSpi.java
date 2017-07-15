/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio.spi;

import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;

public abstract class ImageInputStreamSpi extends IIOServiceProvider{
    protected Class<?> inputClass;

    protected ImageInputStreamSpi(){
    }

    public ImageInputStreamSpi(String vendorName,
                               String version,
                               Class<?> inputClass){
        super(vendorName,version);
        this.inputClass=inputClass;
    }

    public Class<?> getInputClass(){
        return inputClass;
    }

    public boolean canUseCacheFile(){
        return false;
    }

    public boolean needsCacheFile(){
        return false;
    }

    public ImageInputStream createInputStreamInstance(Object input)
            throws IOException{
        return createInputStreamInstance(input,true,null);
    }

    public abstract ImageInputStream
    createInputStreamInstance(Object input,
                              boolean useCache,
                              File cacheDir) throws IOException;
}
