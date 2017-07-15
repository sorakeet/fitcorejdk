/**
 * Copyright (c) 2000, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.basic;

import sun.awt.datatransfer.DataTransferer;

import javax.swing.plaf.UIResource;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;

class BasicTransferable implements Transferable, UIResource{
    private static DataFlavor[] htmlFlavors;
    private static DataFlavor[] stringFlavors;
    private static DataFlavor[] plainFlavors;

    static{
        try{
            htmlFlavors=new DataFlavor[3];
            htmlFlavors[0]=new DataFlavor("text/html;class=java.lang.String");
            htmlFlavors[1]=new DataFlavor("text/html;class=java.io.Reader");
            htmlFlavors[2]=new DataFlavor("text/html;charset=unicode;class=java.io.InputStream");
            plainFlavors=new DataFlavor[3];
            plainFlavors[0]=new DataFlavor("text/plain;class=java.lang.String");
            plainFlavors[1]=new DataFlavor("text/plain;class=java.io.Reader");
            plainFlavors[2]=new DataFlavor("text/plain;charset=unicode;class=java.io.InputStream");
            stringFlavors=new DataFlavor[2];
            stringFlavors[0]=new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";class=java.lang.String");
            stringFlavors[1]=DataFlavor.stringFlavor;
        }catch(ClassNotFoundException cle){
            System.err.println("error initializing javax.swing.plaf.basic.BasicTranserable");
        }
    }

    protected String plainData;
    protected String htmlData;

    public BasicTransferable(String plainData,String htmlData){
        this.plainData=plainData;
        this.htmlData=htmlData;
    }

    public DataFlavor[] getTransferDataFlavors(){
        DataFlavor[] richerFlavors=getRicherFlavors();
        int nRicher=(richerFlavors!=null)?richerFlavors.length:0;
        int nHTML=(isHTMLSupported())?htmlFlavors.length:0;
        int nPlain=(isPlainSupported())?plainFlavors.length:0;
        int nString=(isPlainSupported())?stringFlavors.length:0;
        int nFlavors=nRicher+nHTML+nPlain+nString;
        DataFlavor[] flavors=new DataFlavor[nFlavors];
        // fill in the array
        int nDone=0;
        if(nRicher>0){
            System.arraycopy(richerFlavors,0,flavors,nDone,nRicher);
            nDone+=nRicher;
        }
        if(nHTML>0){
            System.arraycopy(htmlFlavors,0,flavors,nDone,nHTML);
            nDone+=nHTML;
        }
        if(nPlain>0){
            System.arraycopy(plainFlavors,0,flavors,nDone,nPlain);
            nDone+=nPlain;
        }
        if(nString>0){
            System.arraycopy(stringFlavors,0,flavors,nDone,nString);
            nDone+=nString;
        }
        return flavors;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor){
        DataFlavor[] flavors=getTransferDataFlavors();
        for(int i=0;i<flavors.length;i++){
            if(flavors[i].equals(flavor)){
                return true;
            }
        }
        return false;
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException{
        DataFlavor[] richerFlavors=getRicherFlavors();
        if(isRicherFlavor(flavor)){
            return getRicherData(flavor);
        }else if(isHTMLFlavor(flavor)){
            String data=getHTMLData();
            data=(data==null)?"":data;
            if(String.class.equals(flavor.getRepresentationClass())){
                return data;
            }else if(Reader.class.equals(flavor.getRepresentationClass())){
                return new StringReader(data);
            }else if(InputStream.class.equals(flavor.getRepresentationClass())){
                return createInputStream(flavor,data);
            }
            // fall through to unsupported
        }else if(isPlainFlavor(flavor)){
            String data=getPlainData();
            data=(data==null)?"":data;
            if(String.class.equals(flavor.getRepresentationClass())){
                return data;
            }else if(Reader.class.equals(flavor.getRepresentationClass())){
                return new StringReader(data);
            }else if(InputStream.class.equals(flavor.getRepresentationClass())){
                return createInputStream(flavor,data);
            }
            // fall through to unsupported
        }else if(isStringFlavor(flavor)){
            String data=getPlainData();
            data=(data==null)?"":data;
            return data;
        }
        throw new UnsupportedFlavorException(flavor);
    }

    private InputStream createInputStream(DataFlavor flavor,String data)
            throws IOException, UnsupportedFlavorException{
        String cs=DataTransferer.getTextCharset(flavor);
        if(cs==null){
            throw new UnsupportedFlavorException(flavor);
        }
        return new ByteArrayInputStream(data.getBytes(cs));
    }
    // --- richer subclass flavors ----------------------------------------------

    protected boolean isRicherFlavor(DataFlavor flavor){
        DataFlavor[] richerFlavors=getRicherFlavors();
        int nFlavors=(richerFlavors!=null)?richerFlavors.length:0;
        for(int i=0;i<nFlavors;i++){
            if(richerFlavors[i].equals(flavor)){
                return true;
            }
        }
        return false;
    }

    protected DataFlavor[] getRicherFlavors(){
        return null;
    }

    protected Object getRicherData(DataFlavor flavor) throws UnsupportedFlavorException{
        return null;
    }
    // --- html flavors ----------------------------------------------------------

    protected boolean isHTMLFlavor(DataFlavor flavor){
        DataFlavor[] flavors=htmlFlavors;
        for(int i=0;i<flavors.length;i++){
            if(flavors[i].equals(flavor)){
                return true;
            }
        }
        return false;
    }

    protected boolean isHTMLSupported(){
        return htmlData!=null;
    }

    protected String getHTMLData(){
        return htmlData;
    }
    // --- plain text flavors ----------------------------------------------------

    protected boolean isPlainFlavor(DataFlavor flavor){
        DataFlavor[] flavors=plainFlavors;
        for(int i=0;i<flavors.length;i++){
            if(flavors[i].equals(flavor)){
                return true;
            }
        }
        return false;
    }

    protected boolean isPlainSupported(){
        return plainData!=null;
    }

    protected String getPlainData(){
        return plainData;
    }
    // --- string flavorss --------------------------------------------------------

    protected boolean isStringFlavor(DataFlavor flavor){
        DataFlavor[] flavors=stringFlavors;
        for(int i=0;i<flavors.length;i++){
            if(flavors[i].equals(flavor)){
                return true;
            }
        }
        return false;
    }
}
