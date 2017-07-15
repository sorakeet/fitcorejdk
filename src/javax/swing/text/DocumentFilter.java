/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

public class DocumentFilter{
    public void remove(FilterBypass fb,int offset,int length) throws
            BadLocationException{
        fb.remove(offset,length);
    }

    public void insertString(FilterBypass fb,int offset,String string,
                             AttributeSet attr) throws BadLocationException{
        fb.insertString(offset,string,attr);
    }

    public void replace(FilterBypass fb,int offset,int length,String text,
                        AttributeSet attrs) throws BadLocationException{
        fb.replace(offset,length,text,attrs);
    }

    public static abstract class FilterBypass{
        public abstract Document getDocument();

        public abstract void remove(int offset,int length) throws
                BadLocationException;

        public abstract void insertString(int offset,String string,
                                          AttributeSet attr) throws
                BadLocationException;

        public abstract void replace(int offset,int length,String string,
                                     AttributeSet attrs) throws
                BadLocationException;
    }
}
