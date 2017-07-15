/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * (C) Copyright IBM Corp. 1999-2003 - All Rights Reserved
 * <p>
 * The original version of this source code and documentation is
 * copyrighted and owned by IBM. These materials are provided
 * under terms of a License Agreement between IBM and Sun.
 * This technology is protected by multiple US and International
 * patents. This notice and attribution to IBM may not be removed.
 */
/**
 * (C) Copyright IBM Corp. 1999-2003 - All Rights Reserved
 *
 * The original version of this source code and documentation is
 * copyrighted and owned by IBM. These materials are provided
 * under terms of a License Agreement between IBM and Sun.
 * This technology is protected by multiple US and International
 * patents. This notice and attribution to IBM may not be removed.
 */
package java.text;

import sun.text.bidi.BidiBase;

public final class Bidi{
    public static final int DIRECTION_LEFT_TO_RIGHT=0;
    public static final int DIRECTION_RIGHT_TO_LEFT=1;
    public static final int DIRECTION_DEFAULT_LEFT_TO_RIGHT=-2;
    public static final int DIRECTION_DEFAULT_RIGHT_TO_LEFT=-1;
    private BidiBase bidiBase;

    public Bidi(String paragraph,int flags){
        if(paragraph==null){
            throw new IllegalArgumentException("paragraph is null");
        }
        bidiBase=new BidiBase(paragraph.toCharArray(),0,null,0,paragraph.length(),flags);
    }

    public Bidi(AttributedCharacterIterator paragraph){
        if(paragraph==null){
            throw new IllegalArgumentException("paragraph is null");
        }
        bidiBase=new BidiBase(0,0);
        bidiBase.setPara(paragraph);
    }

    public Bidi(char[] text,int textStart,byte[] embeddings,int embStart,int paragraphLength,int flags){
        if(text==null){
            throw new IllegalArgumentException("text is null");
        }
        if(paragraphLength<0){
            throw new IllegalArgumentException("bad length: "+paragraphLength);
        }
        if(textStart<0||paragraphLength>text.length-textStart){
            throw new IllegalArgumentException("bad range: "+textStart+
                    " length: "+paragraphLength+
                    " for text of length: "+text.length);
        }
        if(embeddings!=null&&(embStart<0||paragraphLength>embeddings.length-embStart)){
            throw new IllegalArgumentException("bad range: "+embStart+
                    " length: "+paragraphLength+
                    " for embeddings of length: "+text.length);
        }
        bidiBase=new BidiBase(text,textStart,embeddings,embStart,paragraphLength,flags);
    }

    public static boolean requiresBidi(char[] text,int start,int limit){
        return BidiBase.requiresBidi(text,start,limit);
    }

    public static void reorderVisually(byte[] levels,int levelStart,Object[] objects,int objectStart,int count){
        BidiBase.reorderVisually(levels,levelStart,objects,objectStart,count);
    }

    public Bidi createLineBidi(int lineStart,int lineLimit){
        AttributedString astr=new AttributedString("");
        Bidi newBidi=new Bidi(astr.getIterator());
        return bidiBase.setLine(this,bidiBase,newBidi,newBidi.bidiBase,lineStart,lineLimit);
    }

    public boolean isMixed(){
        return bidiBase.isMixed();
    }

    public boolean isLeftToRight(){
        return bidiBase.isLeftToRight();
    }

    public boolean isRightToLeft(){
        return bidiBase.isRightToLeft();
    }

    public int getLength(){
        return bidiBase.getLength();
    }

    public boolean baseIsLeftToRight(){
        return bidiBase.baseIsLeftToRight();
    }

    public int getBaseLevel(){
        return bidiBase.getParaLevel();
    }

    public int getLevelAt(int offset){
        return bidiBase.getLevelAt(offset);
    }

    public int getRunCount(){
        return bidiBase.countRuns();
    }

    public int getRunLevel(int run){
        return bidiBase.getRunLevel(run);
    }

    public int getRunStart(int run){
        return bidiBase.getRunStart(run);
    }

    public int getRunLimit(int run){
        return bidiBase.getRunLimit(run);
    }

    public String toString(){
        return bidiBase.toString();
    }
}
