/**
 * Copyright (c) 1996, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.io;

public class OptionalDataException extends ObjectStreamException{
    private static final long serialVersionUID=-8011121865681257820L;
    public int length;
    public boolean eof;

    OptionalDataException(int len){
        eof=false;
        length=len;
    }

    OptionalDataException(boolean end){
        length=0;
        eof=end;
    }
}
