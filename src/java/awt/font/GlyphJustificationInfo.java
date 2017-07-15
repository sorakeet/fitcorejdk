/**
 * Copyright (c) 1997, 1999, Oracle and/or its affiliates. All rights reserved.
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
 * (C) Copyright Taligent, Inc. 1996 - 1997, All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998, All Rights Reserved
 * <p>
 * The original version of this source code and documentation is
 * copyrighted and owned by Taligent, Inc., a wholly-owned subsidiary
 * of IBM. These materials are provided under terms of a License
 * Agreement between Taligent and Sun. This technology is protected
 * by multiple US and International patents.
 * <p>
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 */
/**
 * (C) Copyright Taligent, Inc. 1996 - 1997, All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998, All Rights Reserved
 *
 * The original version of this source code and documentation is
 * copyrighted and owned by Taligent, Inc., a wholly-owned subsidiary
 * of IBM. These materials are provided under terms of a License
 * Agreement between Taligent and Sun. This technology is protected
 * by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 */
package java.awt.font;

public final class GlyphJustificationInfo{
    public static final int PRIORITY_KASHIDA=0;
    public static final int PRIORITY_WHITESPACE=1;
    public static final int PRIORITY_INTERCHAR=2;
    public static final int PRIORITY_NONE=3;
    public final float weight;
    public final int growPriority;
    public final boolean growAbsorb;
    public final float growLeftLimit;
    public final float growRightLimit;
    public final int shrinkPriority;
    public final boolean shrinkAbsorb;
    public final float shrinkLeftLimit;
    public final float shrinkRightLimit;

    public GlyphJustificationInfo(float weight,
                                  boolean growAbsorb,
                                  int growPriority,
                                  float growLeftLimit,
                                  float growRightLimit,
                                  boolean shrinkAbsorb,
                                  int shrinkPriority,
                                  float shrinkLeftLimit,
                                  float shrinkRightLimit){
        if(weight<0){
            throw new IllegalArgumentException("weight is negative");
        }
        if(!priorityIsValid(growPriority)){
            throw new IllegalArgumentException("Invalid grow priority");
        }
        if(growLeftLimit<0){
            throw new IllegalArgumentException("growLeftLimit is negative");
        }
        if(growRightLimit<0){
            throw new IllegalArgumentException("growRightLimit is negative");
        }
        if(!priorityIsValid(shrinkPriority)){
            throw new IllegalArgumentException("Invalid shrink priority");
        }
        if(shrinkLeftLimit<0){
            throw new IllegalArgumentException("shrinkLeftLimit is negative");
        }
        if(shrinkRightLimit<0){
            throw new IllegalArgumentException("shrinkRightLimit is negative");
        }
        this.weight=weight;
        this.growAbsorb=growAbsorb;
        this.growPriority=growPriority;
        this.growLeftLimit=growLeftLimit;
        this.growRightLimit=growRightLimit;
        this.shrinkAbsorb=shrinkAbsorb;
        this.shrinkPriority=shrinkPriority;
        this.shrinkLeftLimit=shrinkLeftLimit;
        this.shrinkRightLimit=shrinkRightLimit;
    }

    private static boolean priorityIsValid(int priority){
        return priority>=PRIORITY_KASHIDA&&priority<=PRIORITY_NONE;
    }
}
