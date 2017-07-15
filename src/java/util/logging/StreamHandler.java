/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.logging;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class StreamHandler extends Handler{
    private OutputStream output;
    private boolean doneHeader;
    private volatile Writer writer;

    public StreamHandler(){
        sealed=false;
        configure();
        sealed=true;
    }

    // Private method to configure a StreamHandler from LogManager
    // properties and/or default values as specified in the class
    // javadoc.
    private void configure(){
        LogManager manager=LogManager.getLogManager();
        String cname=getClass().getName();
        setLevel(manager.getLevelProperty(cname+".level",Level.INFO));
        setFilter(manager.getFilterProperty(cname+".filter",null));
        setFormatter(manager.getFormatterProperty(cname+".formatter",new SimpleFormatter()));
        try{
            setEncoding(manager.getStringProperty(cname+".encoding",null));
        }catch(Exception ex){
            try{
                setEncoding(null);
            }catch(Exception ex2){
                // doing a setEncoding with null should always work.
                // assert false;
            }
        }
    }

    public StreamHandler(OutputStream out,Formatter formatter){
        sealed=false;
        configure();
        setFormatter(formatter);
        setOutputStream(out);
        sealed=true;
    }

    protected synchronized void setOutputStream(OutputStream out) throws SecurityException{
        if(out==null){
            throw new NullPointerException();
        }
        flushAndClose();
        output=out;
        doneHeader=false;
        String encoding=getEncoding();
        if(encoding==null){
            writer=new OutputStreamWriter(output);
        }else{
            try{
                writer=new OutputStreamWriter(output,encoding);
            }catch(UnsupportedEncodingException ex){
                // This shouldn't happen.  The setEncoding method
                // should have validated that the encoding is OK.
                throw new Error("Unexpected exception "+ex);
            }
        }
    }

    private synchronized void flushAndClose() throws SecurityException{
        checkPermission();
        if(writer!=null){
            try{
                if(!doneHeader){
                    writer.write(getFormatter().getHead(this));
                    doneHeader=true;
                }
                writer.write(getFormatter().getTail(this));
                writer.flush();
                writer.close();
            }catch(Exception ex){
                // We don't want to throw an exception here, but we
                // report the exception to any registered ErrorManager.
                reportError(null,ex,ErrorManager.CLOSE_FAILURE);
            }
            writer=null;
            output=null;
        }
    }

    @Override
    public synchronized void publish(LogRecord record){
        if(!isLoggable(record)){
            return;
        }
        String msg;
        try{
            msg=getFormatter().format(record);
        }catch(Exception ex){
            // We don't want to throw an exception here, but we
            // report the exception to any registered ErrorManager.
            reportError(null,ex,ErrorManager.FORMAT_FAILURE);
            return;
        }
        try{
            if(!doneHeader){
                writer.write(getFormatter().getHead(this));
                doneHeader=true;
            }
            writer.write(msg);
        }catch(Exception ex){
            // We don't want to throw an exception here, but we
            // report the exception to any registered ErrorManager.
            reportError(null,ex,ErrorManager.WRITE_FAILURE);
        }
    }

    @Override
    public synchronized void flush(){
        if(writer!=null){
            try{
                writer.flush();
            }catch(Exception ex){
                // We don't want to throw an exception here, but we
                // report the exception to any registered ErrorManager.
                reportError(null,ex,ErrorManager.FLUSH_FAILURE);
            }
        }
    }

    @Override
    public synchronized void close() throws SecurityException{
        flushAndClose();
    }

    @Override
    public synchronized void setEncoding(String encoding)
            throws SecurityException, UnsupportedEncodingException{
        super.setEncoding(encoding);
        if(output==null){
            return;
        }
        // Replace the current writer with a writer for the new encoding.
        flush();
        if(encoding==null){
            writer=new OutputStreamWriter(output);
        }else{
            writer=new OutputStreamWriter(output,encoding);
        }
    }

    @Override
    public boolean isLoggable(LogRecord record){
        if(writer==null||record==null){
            return false;
        }
        return super.isLoggable(record);
    }
}
