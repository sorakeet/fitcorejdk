/**
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * (C) Copyright IBM Corp. 2005, All Rights Reserved.
 */
/**
 * (C) Copyright IBM Corp. 2005, All Rights Reserved.
 */
package java.awt.font;

import java.awt.geom.Point2D;

public abstract class LayoutPath{
    public abstract boolean pointToPath(Point2D point,Point2D location);

    public abstract void pathToPoint(Point2D location,boolean preceding,
                                     Point2D point);
}
