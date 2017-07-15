/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio.plugins.bmp;

import com.sun.imageio.plugins.bmp.BMPCompressionTypes;
import com.sun.imageio.plugins.bmp.BMPConstants;

import javax.imageio.ImageWriteParam;
import java.util.Locale;

public class BMPImageWriteParam extends ImageWriteParam{
    private boolean topDown=false;

    public BMPImageWriteParam(){
        this(null);
    }

    public BMPImageWriteParam(Locale locale){
        super(locale);
        // Set compression types ("BI_RGB" denotes uncompressed).
        compressionTypes=BMPCompressionTypes.getCompressionTypes();
        // Set compression flag.
        canWriteCompressed=true;
        compressionMode=MODE_COPY_FROM_METADATA;
        compressionType=compressionTypes[BMPConstants.BI_RGB];
    }

    public boolean isTopDown(){
        return topDown;
    }

    public void setTopDown(boolean topDown){
        this.topDown=topDown;
    }
}
