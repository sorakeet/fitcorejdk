/**
 * Copyright (c) 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio;

import javax.imageio.metadata.IIOMetadata;

public interface ImageTranscoder{
    IIOMetadata convertStreamMetadata(IIOMetadata inData,
                                      ImageWriteParam param);

    IIOMetadata convertImageMetadata(IIOMetadata inData,
                                     ImageTypeSpecifier imageType,
                                     ImageWriteParam param);
}
