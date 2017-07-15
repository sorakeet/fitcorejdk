/**
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// Copyright (c) 1995-96 by Cisco Systems, Inc.
package com.sun.jmx.snmp.daemon;
// JAVA imports
//

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;

import static com.sun.jmx.defaults.JmxProperties.SNMP_ADAPTOR_LOGGER;
// SNMP Runtime imports
//

final class SnmpSocket implements Runnable{
    int _socketPort=0;
    int responseBufSize=1024;
    // VARIABLES
    //----------
    private DatagramSocket _socket=null;
    private SnmpResponseHandler _dgramHdlr=null;
    private Thread _sockThread=null;
    private byte[] _buffer=null;
    private transient boolean isClosing=false;
    // CONSTRUCTORS
    //-------------

    public SnmpSocket(SnmpResponseHandler rspHdlr,InetAddress addr,int bufferSize) throws SocketException{
        super();
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER,SnmpSocket.class.getName(),
                    "constructor","Creating new SNMP datagram socket");
        }
        // TIME BOMB HERE
        _socket=new DatagramSocket(0,addr);
        _socketPort=_socket.getLocalPort();
        responseBufSize=bufferSize;
        _buffer=new byte[responseBufSize];
        _dgramHdlr=rspHdlr;
        _sockThread=new Thread(this,"SnmpSocket");
        _sockThread.start();
    }
    // PUBLIC METHODS
    //---------------

    public synchronized void sendPacket(byte[] buff,int length,InetAddress addr,int port) throws IOException{
        DatagramPacket dgrmpkt;
        dgrmpkt=new DatagramPacket(buff,length,addr,port);
        sendPacket(dgrmpkt);
    }

    public synchronized void sendPacket(DatagramPacket dgrmpkt) throws IOException{
        try{
            if(isValid()){
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINER,SnmpSocket.class.getName(),
                            "sendPacket","Sending DatagramPacket. Length = "+dgrmpkt.getLength()+
                                    " through socket = "+_socket.toString());
                }
                _socket.send(dgrmpkt);
            }else
                throw new IOException("Invalid state of SNMP datagram socket.");
        }catch(IOException e){
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpSocket.class.getName(),
                        "sendPacket","I/O error while sending",e);
            }
            throw e;
        }
    }

    public synchronized boolean isValid(){
        return _socket!=null&&_sockThread!=null&&_sockThread.isAlive();
    }

    @Override
    public void run(){
        Thread.currentThread().setPriority(8);
        while(true){
            try{
                DatagramPacket dgrmpkt=new DatagramPacket(_buffer,_buffer.length);
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINER,SnmpSocket.class.getName(),
                            "run","["+Thread.currentThread().toString()+"]:"+"Blocking for receiving packet");
                }
                _socket.receive(dgrmpkt);
                // If the corresponding session is being destroyed, stop handling received responses.
                //
                if(isClosing)
                    break;
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINER,SnmpSocket.class.getName(),
                            "run","["+Thread.currentThread().toString()+"]:"+"Received a packet");
                }
                if(dgrmpkt.getLength()<=0)
                    continue;
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINER,SnmpSocket.class.getName(),
                            "run","["+Thread.currentThread().toString()+"]:"+"Received a packet from : "+
                                    dgrmpkt.getAddress().toString()+", Length = "+dgrmpkt.getLength());
                }
                handleDatagram(dgrmpkt);
                // We are closing the snmp socket while handling the datagram.
                //
                if(isClosing)
                    break;
            }catch(IOException io){
                // If the IO exception has been generated because of closing this SNMP socket,
                // (call to _socket.close while _socket is blocked for receiving packet) simply terminate closing properly.
                //
                if(isClosing){
                    break;
                }
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpSocket.class.getName(),
                            "run","IOEXception while receiving datagram",io);
                }
            }catch(Exception e){
                // If the exception (NullPointerException) has been generated because of closing this SNMP socket,
                // (call to _socket = null while _socket is blocked for receiving packet) simply terminate closing properly.
                //
                if(isClosing){
                    break;
                }
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpSocket.class.getName(),
                            "run","Exception in socket thread...",e);
                }
            }catch(ThreadDeath d){
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpSocket.class.getName(),
                            "run","Socket Thread DEAD..."+toString(),d);
                }
                close();
                throw d;  // rethrow dead thread.
            }catch(Error err){
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpSocket.class.getName(),
                            "run","Got unexpected error",err);
                }
                handleJavaError(err);
            }
        }
    }

    public synchronized void close(){
        isClosing=true;
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER,SnmpSocket.class.getName(),
                    "close","Closing and destroying the SNMP datagram socket -> "+toString());
        }
        try{
            // We send an empty datagram packet to fix bug 4293791 (it's a jdk 1.1 bug)
            //
            DatagramSocket sn=new DatagramSocket(0);
            byte[] ob=new byte[1];
            DatagramPacket pk=new DatagramPacket(ob,1,InetAddress.getLocalHost(),_socketPort);
            sn.send(pk);
            sn.close();
        }catch(Exception e){
        }
        // First close the datagram socket.
        // This may generates an IO exception at the run method (_socket.receive).
        //
        if(_socket!=null){
            _socket.close();
            _socket=null;
        }
        // Then stop the thread socket.
        //
        if(_sockThread!=null&&_sockThread.isAlive()){
            _sockThread.interrupt();
            try{
                // Wait until the thread die.
                //
                _sockThread.join();
            }catch(InterruptedException e){
                // Ignore...
            }
            _sockThread=null;
        }
    }

    private synchronized void handleJavaError(Throwable thr){
        if(thr instanceof OutOfMemoryError){
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpSocket.class.getName(),
                        "handleJavaError","OutOfMemory error",thr);
            }
            Thread.yield();
            return;
        }
        if(_socket!=null){
            _socket.close();
            _socket=null;
        }
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpSocket.class.getName(),
                    "handleJavaError","Global Internal error");
        }
        Thread.yield();
    }
    // PRIVATE METHODS
    //----------------

    private synchronized void handleDatagram(DatagramPacket dgrmpkt){
        _dgramHdlr.processDatagram(dgrmpkt);
    }

    @Override
    protected synchronized void finalize(){
        close();
    }
}
