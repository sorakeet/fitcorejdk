/**
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import java.io.Serializable;

public abstract class GraphicsConfigTemplate implements Serializable{
    public static final int REQUIRED=1;
    public static final int PREFERRED=2;
    public static final int UNNECESSARY=3;
    private static final long serialVersionUID=-8061369279557787079L;

    public GraphicsConfigTemplate(){
    }

    public abstract GraphicsConfiguration
    getBestConfiguration(GraphicsConfiguration[] gc);

    public abstract boolean
    isGraphicsConfigSupported(GraphicsConfiguration gc);
}
