/**
 * Copyright (c) 2000, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio.event;

import javax.imageio.ImageWriter;
import java.util.EventListener;

public interface IIOWriteProgressListener extends EventListener{
    void imageStarted(ImageWriter source,int imageIndex);

    void imageProgress(ImageWriter source,
                       float percentageDone);

    void imageComplete(ImageWriter source);

    void thumbnailStarted(ImageWriter source,
                          int imageIndex,int thumbnailIndex);

    void thumbnailProgress(ImageWriter source,float percentageDone);

    void thumbnailComplete(ImageWriter source);

    void writeAborted(ImageWriter source);
}
