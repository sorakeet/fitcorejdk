/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio.event;

import javax.imageio.ImageReader;
import java.util.EventListener;

public interface IIOReadWarningListener extends EventListener{
    void warningOccurred(ImageReader source,String warning);
}
