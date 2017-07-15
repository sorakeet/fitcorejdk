/**
 * Copyright (c) 1997, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.image;

public interface TileObserver{
    public void tileUpdate(WritableRenderedImage source,
                           int tileX,int tileY,
                           boolean willBeWritable);
}
