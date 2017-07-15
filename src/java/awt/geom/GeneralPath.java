/**
 * Copyright (c) 1996, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.geom;

import java.awt.*;

public final class GeneralPath extends Path2D.Float{
    private static final long serialVersionUID=-8327096662768731142L;

    public GeneralPath(){
        super(WIND_NON_ZERO,INIT_SIZE);
    }

    public GeneralPath(int rule){
        super(rule,INIT_SIZE);
    }

    public GeneralPath(int rule,int initialCapacity){
        super(rule,initialCapacity);
    }

    public GeneralPath(Shape s){
        super(s,null);
    }

    GeneralPath(int windingRule,
                byte[] pointTypes,
                int numTypes,
                float[] pointCoords,
                int numCoords){
        // used to construct from native
        this.windingRule=windingRule;
        this.pointTypes=pointTypes;
        this.numTypes=numTypes;
        this.floatCoords=pointCoords;
        this.numCoords=numCoords;
    }
}
