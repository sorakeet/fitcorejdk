/**
 * Copyright (c) 1998, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html.parser;

import sun.awt.AppContext;

import javax.swing.text.html.HTMLEditorKit;
import java.io.*;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class ParserDelegator extends HTMLEditorKit.Parser implements Serializable{
    private static final Object DTD_KEY=new Object();

    public ParserDelegator(){
        setDefaultDTD();
    }

    protected static void setDefaultDTD(){
        getDefaultDTD();
    }

    private static synchronized DTD getDefaultDTD(){
        AppContext appContext=AppContext.getAppContext();
        DTD dtd=(DTD)appContext.get(DTD_KEY);
        if(dtd==null){
            DTD _dtd=null;
            // (PENDING) Hate having to hard code!
            String nm="html32";
            try{
                _dtd=DTD.getDTD(nm);
            }catch(IOException e){
                // (PENDING) UGLY!
                System.out.println("Throw an exception: could not get default dtd: "+nm);
            }
            dtd=createDTD(_dtd,nm);
            appContext.put(DTD_KEY,dtd);
        }
        return dtd;
    }

    protected static DTD createDTD(DTD dtd,String name){
        InputStream in=null;
        boolean debug=true;
        try{
            String path=name+".bdtd";
            in=getResourceAsStream(path);
            if(in!=null){
                dtd.read(new DataInputStream(new BufferedInputStream(in)));
                dtd.putDTDHash(name,dtd);
            }
        }catch(Exception e){
            System.out.println(e);
        }
        return dtd;
    }

    static InputStream getResourceAsStream(final String name){
        return AccessController.doPrivileged(
                new PrivilegedAction<InputStream>(){
                    public InputStream run(){
                        return ParserDelegator.class.getResourceAsStream(name);
                    }
                });
    }

    public void parse(Reader r,HTMLEditorKit.ParserCallback cb,boolean ignoreCharSet) throws IOException{
        new DocumentParser(getDefaultDTD()).parse(r,cb,ignoreCharSet);
    }

    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException{
        s.defaultReadObject();
        setDefaultDTD();
    }
}
