/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio.event;

import javax.imageio.ImageWriter;
import java.util.EventListener;

public interface IIOWriteWarningListener extends EventListener{
    void warningOccurred(ImageWriter source,
                         int imageIndex,
                         String warning);
}
