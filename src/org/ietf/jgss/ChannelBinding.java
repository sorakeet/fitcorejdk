/**
 * Copyright (c) 2000, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.ietf.jgss;

import java.net.InetAddress;
import java.util.Arrays;

public class ChannelBinding{
    private InetAddress initiator;
    private InetAddress acceptor;
    private byte[] appData;

    public ChannelBinding(byte[] appData){
        this(null,null,appData);
    }

    public ChannelBinding(InetAddress initAddr,InetAddress acceptAddr,
                          byte[] appData){
        initiator=initAddr;
        acceptor=acceptAddr;
        if(appData!=null){
            this.appData=new byte[appData.length];
            System.arraycopy(appData,0,this.appData,0,
                    appData.length);
        }
    }

    public InetAddress getInitiatorAddress(){
        return initiator;
    }

    public InetAddress getAcceptorAddress(){
        return acceptor;
    }

    public byte[] getApplicationData(){
        if(appData==null){
            return null;
        }
        byte[] retVal=new byte[appData.length];
        System.arraycopy(appData,0,retVal,0,appData.length);
        return retVal;
    }

    public int hashCode(){
        if(initiator!=null)
            return initiator.hashCode();
        else if(acceptor!=null)
            return acceptor.hashCode();
        else if(appData!=null)
            return new String(appData).hashCode();
        else
            return 1;
    }

    public boolean equals(Object obj){
        if(this==obj)
            return true;
        if(!(obj instanceof ChannelBinding))
            return false;
        ChannelBinding cb=(ChannelBinding)obj;
        if((initiator!=null&&cb.initiator==null)||
                (initiator==null&&cb.initiator!=null))
            return false;
        if(initiator!=null&&!initiator.equals(cb.initiator))
            return false;
        if((acceptor!=null&&cb.acceptor==null)||
                (acceptor==null&&cb.acceptor!=null))
            return false;
        if(acceptor!=null&&!acceptor.equals(cb.acceptor))
            return false;
        return Arrays.equals(appData,cb.appData);
    }
}
