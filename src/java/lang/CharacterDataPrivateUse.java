/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang;

class CharacterDataPrivateUse extends CharacterData{
    static final CharacterData instance=new CharacterDataPrivateUse();

    private CharacterDataPrivateUse(){
    }

    int getProperties(int ch){
        return 0;
    }

    int getType(int ch){
        return (ch&0xFFFE)==0xFFFE
                ?Character.UNASSIGNED
                :Character.PRIVATE_USE;
    }

    boolean isWhitespace(int ch){
        return false;
    }

    boolean isMirrored(int ch){
        return false;
    }

    boolean isJavaIdentifierStart(int ch){
        return false;
    }

    boolean isJavaIdentifierPart(int ch){
        return false;
    }

    boolean isUnicodeIdentifierStart(int ch){
        return false;
    }

    boolean isUnicodeIdentifierPart(int ch){
        return false;
    }

    boolean isIdentifierIgnorable(int ch){
        return false;
    }

    int toLowerCase(int ch){
        return ch;
    }

    int toUpperCase(int ch){
        return ch;
    }

    int toTitleCase(int ch){
        return ch;
    }

    int digit(int ch,int radix){
        return -1;
    }

    int getNumericValue(int ch){
        return -1;
    }

    byte getDirectionality(int ch){
        return (ch&0xFFFE)==0xFFFE
                ?Character.DIRECTIONALITY_UNDEFINED
                :Character.DIRECTIONALITY_LEFT_TO_RIGHT;
    }

    ;
}


