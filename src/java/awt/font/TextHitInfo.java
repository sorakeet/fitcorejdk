/**
 * Copyright (c) 1997, 1998, Oracle and/or its affiliates. All rights reserved.
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
 *
 */
package java.awt.font;

public final class TextHitInfo{
    private int charIndex;
    private boolean isLeadingEdge;

    private TextHitInfo(int charIndex,boolean isLeadingEdge){
        this.charIndex=charIndex;
        this.isLeadingEdge=isLeadingEdge;
    }

    public static TextHitInfo beforeOffset(int offset){
        return new TextHitInfo(offset-1,false);
    }

    public static TextHitInfo afterOffset(int offset){
        return new TextHitInfo(offset,true);
    }

    public int getCharIndex(){
        return charIndex;
    }

    public boolean isLeadingEdge(){
        return isLeadingEdge;
    }

    public int getInsertionIndex(){
        return isLeadingEdge?charIndex:charIndex+1;
    }

    public int hashCode(){
        return charIndex;
    }

    public boolean equals(Object obj){
        return (obj instanceof TextHitInfo)&&equals((TextHitInfo)obj);
    }

    public boolean equals(TextHitInfo hitInfo){
        return hitInfo!=null&&charIndex==hitInfo.charIndex&&
                isLeadingEdge==hitInfo.isLeadingEdge;
    }

    public String toString(){
        return "TextHitInfo["+charIndex+(isLeadingEdge?"L":"T")+"]";
    }

    public TextHitInfo getOtherHit(){
        if(isLeadingEdge){
            return trailing(charIndex-1);
        }else{
            return leading(charIndex+1);
        }
    }

    public static TextHitInfo leading(int charIndex){
        return new TextHitInfo(charIndex,true);
    }

    public static TextHitInfo trailing(int charIndex){
        return new TextHitInfo(charIndex,false);
    }

    public TextHitInfo getOffsetHit(int delta){
        return new TextHitInfo(charIndex+delta,isLeadingEdge);
    }
}
