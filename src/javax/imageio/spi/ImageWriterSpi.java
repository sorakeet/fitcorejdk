/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio.spi;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.RenderedImage;
import java.io.IOException;

public abstract class ImageWriterSpi extends ImageReaderWriterSpi{
    @Deprecated
    public static final Class[] STANDARD_OUTPUT_TYPE=
            {ImageOutputStream.class};
    protected Class[] outputTypes=null;
    protected String[] readerSpiNames=null;
    private Class writerClass=null;

    protected ImageWriterSpi(){
    }

    public ImageWriterSpi(String vendorName,
                          String version,
                          String[] names,
                          String[] suffixes,
                          String[] MIMETypes,
                          String writerClassName,
                          Class[] outputTypes,
                          String[] readerSpiNames,
                          boolean supportsStandardStreamMetadataFormat,
                          String nativeStreamMetadataFormatName,
                          String nativeStreamMetadataFormatClassName,
                          String[] extraStreamMetadataFormatNames,
                          String[] extraStreamMetadataFormatClassNames,
                          boolean supportsStandardImageMetadataFormat,
                          String nativeImageMetadataFormatName,
                          String nativeImageMetadataFormatClassName,
                          String[] extraImageMetadataFormatNames,
                          String[] extraImageMetadataFormatClassNames){
        super(vendorName,version,
                names,suffixes,MIMETypes,writerClassName,
                supportsStandardStreamMetadataFormat,
                nativeStreamMetadataFormatName,
                nativeStreamMetadataFormatClassName,
                extraStreamMetadataFormatNames,
                extraStreamMetadataFormatClassNames,
                supportsStandardImageMetadataFormat,
                nativeImageMetadataFormatName,
                nativeImageMetadataFormatClassName,
                extraImageMetadataFormatNames,
                extraImageMetadataFormatClassNames);
        if(outputTypes==null){
            throw new IllegalArgumentException
                    ("outputTypes == null!");
        }
        if(outputTypes.length==0){
            throw new IllegalArgumentException
                    ("outputTypes.length == 0!");
        }
        this.outputTypes=(outputTypes==STANDARD_OUTPUT_TYPE)?
                new Class<?>[]{ImageOutputStream.class}:
                outputTypes.clone();
        // If length == 0, leave it null
        if(readerSpiNames!=null&&readerSpiNames.length>0){
            this.readerSpiNames=(String[])readerSpiNames.clone();
        }
    }

    public boolean isFormatLossless(){
        return true;
    }

    public Class[] getOutputTypes(){
        return (Class[])outputTypes.clone();
    }

    public boolean canEncodeImage(RenderedImage im){
        return canEncodeImage(ImageTypeSpecifier.createFromRenderedImage(im));
    }

    public abstract boolean canEncodeImage(ImageTypeSpecifier type);

    public ImageWriter createWriterInstance() throws IOException{
        return createWriterInstance(null);
    }

    public abstract ImageWriter createWriterInstance(Object extension)
            throws IOException;

    public boolean isOwnWriter(ImageWriter writer){
        if(writer==null){
            throw new IllegalArgumentException("writer == null!");
        }
        String name=writer.getClass().getName();
        return name.equals(pluginClassName);
    }

    public String[] getImageReaderSpiNames(){
        return readerSpiNames==null?
                null:(String[])readerSpiNames.clone();
    }
}
