/**
 * Copyright (c) 2003, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

public class PointerInfo{
    private final GraphicsDevice device;
    private final Point location;

    PointerInfo(final GraphicsDevice device,final Point location){
        this.device=device;
        this.location=location;
    }

    public GraphicsDevice getDevice(){
        return device;
    }

    public Point getLocation(){
        return location;
    }
}
