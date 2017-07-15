/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

@SuppressWarnings("serial")
public class BadLocationException extends Exception{
    private int offs;

    public BadLocationException(String s,int offs){
        super(s);
        this.offs=offs;
    }

    public int offsetRequested(){
        return offs;
    }
}
