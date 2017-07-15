/**
 * Copyright (c) 2000, 2007, Oracle and/or its affiliates. All rights reserved.
 * <p>
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// -- This file was mechanically generated: Do not edit! -- //
package java.nio.charset;

public class UnsupportedCharsetException
        extends IllegalArgumentException{
    private static final long serialVersionUID=1490765524727386367L;
    private String charsetName;

    public UnsupportedCharsetException(String charsetName){
        super(String.valueOf(charsetName));
        this.charsetName=charsetName;
    }

    public String getCharsetName(){
        return charsetName;
    }
}
