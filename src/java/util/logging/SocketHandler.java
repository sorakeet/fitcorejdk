/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.logging;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class SocketHandler extends StreamHandler{
    private Socket sock;
    private String host;
    private int port;

    public SocketHandler() throws IOException{
        // We are going to use the logging defaults.
        sealed=false;
        configure();
        try{
            connect();
        }catch(IOException ix){
            System.err.println("SocketHandler: connect failed to "+host+":"+port);
            throw ix;
        }
        sealed=true;
    }

    // Private method to configure a SocketHandler from LogManager
    // properties and/or default values as specified in the class
    // javadoc.
    private void configure(){
        LogManager manager=LogManager.getLogManager();
        String cname=getClass().getName();
        setLevel(manager.getLevelProperty(cname+".level",Level.ALL));
        setFilter(manager.getFilterProperty(cname+".filter",null));
        setFormatter(manager.getFormatterProperty(cname+".formatter",new XMLFormatter()));
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
        port=manager.getIntProperty(cname+".port",0);
        host=manager.getStringProperty(cname+".host",null);
    }

    private void connect() throws IOException{
        // Check the arguments are valid.
        if(port==0){
            throw new IllegalArgumentException("Bad port: "+port);
        }
        if(host==null){
            throw new IllegalArgumentException("Null host name: "+host);
        }
        // Try to open a new socket.
        sock=new Socket(host,port);
        OutputStream out=sock.getOutputStream();
        BufferedOutputStream bout=new BufferedOutputStream(out);
        setOutputStream(bout);
    }

    public SocketHandler(String host,int port) throws IOException{
        sealed=false;
        configure();
        sealed=true;
        this.port=port;
        this.host=host;
        connect();
    }

    @Override
    public synchronized void publish(LogRecord record){
        if(!isLoggable(record)){
            return;
        }
        super.publish(record);
        flush();
    }

    @Override
    public synchronized void close() throws SecurityException{
        super.close();
        if(sock!=null){
            try{
                sock.close();
            }catch(IOException ix){
                // drop through.
            }
        }
        sock=null;
    }
}
