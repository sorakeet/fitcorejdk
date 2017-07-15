/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio.event;

import javax.imageio.ImageReader;
import java.util.EventListener;

public interface IIOReadProgressListener extends EventListener{
    void sequenceStarted(ImageReader source,int minIndex);

    void sequenceComplete(ImageReader source);

    void imageStarted(ImageReader source,int imageIndex);

    void imageProgress(ImageReader source,float percentageDone);

    void imageComplete(ImageReader source);

    void thumbnailStarted(ImageReader source,
                          int imageIndex,int thumbnailIndex);

    void thumbnailProgress(ImageReader source,float percentageDone);

    void thumbnailComplete(ImageReader source);

    void readAborted(ImageReader source);
}
