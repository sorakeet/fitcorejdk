/**
 * Copyright (c) 1996, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

public abstract class PrintJob{
    public abstract Graphics getGraphics();

    public abstract Dimension getPageDimension();

    public abstract int getPageResolution();

    public abstract boolean lastPageFirst();

    public void finalize(){
        end();
    }

    public abstract void end();
}
