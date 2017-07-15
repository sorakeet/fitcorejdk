/**
 * Copyright (c) 2000, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans;

import com.sun.beans.decoder.DocumentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class XMLDecoder implements AutoCloseable{
    private final AccessControlContext acc=AccessController.getContext();
    private final DocumentHandler handler=new DocumentHandler();
    private final InputSource input;
    private Object owner;
    private Object[] array;
    private int index;

    public XMLDecoder(InputStream in){
        this(in,null);
    }

    public XMLDecoder(InputStream in,Object owner){
        this(in,owner,null);
    }

    public XMLDecoder(InputStream in,Object owner,ExceptionListener exceptionListener){
        this(in,owner,exceptionListener,null);
    }

    public XMLDecoder(InputStream in,Object owner,
                      ExceptionListener exceptionListener,ClassLoader cl){
        this(new InputSource(in),owner,exceptionListener,cl);
    }

    private XMLDecoder(InputSource is,Object owner,ExceptionListener el,ClassLoader cl){
        this.input=is;
        this.owner=owner;
        setExceptionListener(el);
        this.handler.setClassLoader(cl);
        this.handler.setOwner(this);
    }

    public XMLDecoder(InputSource is){
        this(is,null,null,null);
    }

    public static DefaultHandler createHandler(Object owner,ExceptionListener el,ClassLoader cl){
        DocumentHandler handler=new DocumentHandler();
        handler.setOwner(owner);
        handler.setExceptionListener(el);
        handler.setClassLoader(cl);
        return handler;
    }

    public void close(){
        if(parsingComplete()){
            close(this.input.getCharacterStream());
            close(this.input.getByteStream());
        }
    }

    private void close(Closeable in){
        if(in!=null){
            try{
                in.close();
            }catch(IOException e){
                getExceptionListener().exceptionThrown(e);
            }
        }
    }

    public ExceptionListener getExceptionListener(){
        return this.handler.getExceptionListener();
    }

    public void setExceptionListener(ExceptionListener exceptionListener){
        if(exceptionListener==null){
            exceptionListener=Statement.defaultExceptionListener;
        }
        this.handler.setExceptionListener(exceptionListener);
    }

    private boolean parsingComplete(){
        if(this.input==null){
            return false;
        }
        if(this.array==null){
            if((this.acc==null)&&(null!=System.getSecurityManager())){
                throw new SecurityException("AccessControlContext is not set");
            }
            AccessController.doPrivileged(new PrivilegedAction<Void>(){
                public Void run(){
                    XMLDecoder.this.handler.parse(XMLDecoder.this.input);
                    return null;
                }
            },this.acc);
            this.array=this.handler.getObjects();
        }
        return true;
    }

    public Object readObject(){
        return (parsingComplete())
                ?this.array[this.index++]
                :null;
    }

    public Object getOwner(){
        return owner;
    }

    public void setOwner(Object owner){
        this.owner=owner;
    }
}
