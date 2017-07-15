/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.print;

import java.util.Vector;

public class Book implements Pageable{
    /** Class Constants */
    /** Class Variables */
    private Vector mPages;

    public Book(){
        mPages=new Vector();
    }

    public int getNumberOfPages(){
        return mPages.size();
    }

    public PageFormat getPageFormat(int pageIndex)
            throws IndexOutOfBoundsException{
        return getPage(pageIndex).getPageFormat();
    }

    public Printable getPrintable(int pageIndex)
            throws IndexOutOfBoundsException{
        return getPage(pageIndex).getPrintable();
    }

    private BookPage getPage(int pageIndex)
            throws ArrayIndexOutOfBoundsException{
        return (BookPage)mPages.elementAt(pageIndex);
    }

    public void setPage(int pageIndex,Printable painter,PageFormat page)
            throws IndexOutOfBoundsException{
        if(painter==null){
            throw new NullPointerException("painter is null");
        }
        if(page==null){
            throw new NullPointerException("page is null");
        }
        mPages.setElementAt(new BookPage(painter,page),pageIndex);
    }

    public void append(Printable painter,PageFormat page){
        mPages.addElement(new BookPage(painter,page));
    }

    public void append(Printable painter,PageFormat page,int numPages){
        BookPage bookPage=new BookPage(painter,page);
        int pageIndex=mPages.size();
        int newSize=pageIndex+numPages;
        mPages.setSize(newSize);
        for(int i=pageIndex;i<newSize;i++){
            mPages.setElementAt(bookPage,i);
        }
    }

    private class BookPage{
        private PageFormat mFormat;
        private Printable mPainter;

        BookPage(Printable painter,PageFormat format){
            if(painter==null||format==null){
                throw new NullPointerException();
            }
            mFormat=format;
            mPainter=painter;
        }

        Printable getPrintable(){
            return mPainter;
        }

        PageFormat getPageFormat(){
            return mFormat;
        }
    }
}
