/**
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.im;

public final class InputSubset extends Character.Subset{
    public static final InputSubset LATIN
            =new InputSubset("LATIN");
    public static final InputSubset LATIN_DIGITS
            =new InputSubset("LATIN_DIGITS");
    public static final InputSubset TRADITIONAL_HANZI
            =new InputSubset("TRADITIONAL_HANZI");
    public static final InputSubset SIMPLIFIED_HANZI
            =new InputSubset("SIMPLIFIED_HANZI");
    public static final InputSubset KANJI
            =new InputSubset("KANJI");
    public static final InputSubset HANJA
            =new InputSubset("HANJA");
    public static final InputSubset HALFWIDTH_KATAKANA
            =new InputSubset("HALFWIDTH_KATAKANA");
    public static final InputSubset FULLWIDTH_LATIN
            =new InputSubset("FULLWIDTH_LATIN");
    public static final InputSubset FULLWIDTH_DIGITS
            =new InputSubset("FULLWIDTH_DIGITS");

    private InputSubset(String name){
        super(name);
    }
}
