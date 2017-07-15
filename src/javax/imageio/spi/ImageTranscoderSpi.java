/**
 * Copyright (c) 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio.spi;

import javax.imageio.ImageTranscoder;

public abstract class ImageTranscoderSpi extends IIOServiceProvider{
    protected ImageTranscoderSpi(){
    }

    public ImageTranscoderSpi(String vendorName,
                              String version){
        super(vendorName,version);
    }

    public abstract String getReaderServiceProviderName();

    public abstract String getWriterServiceProviderName();

    public abstract ImageTranscoder createTranscoderInstance();
}
