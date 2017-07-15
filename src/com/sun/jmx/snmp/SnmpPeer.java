/**
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// Copyright (c) 1995-96 by Cisco Systems, Inc.
package com.sun.jmx.snmp;
// java imports
//

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class SnmpPeer implements Serializable{
    // PUBLIC VARIABLES
    //-----------------
    public static final int defaultSnmpRequestPktSize=2*1024;
    public static final int defaultSnmpResponsePktSize=8*1024;
    private static final long serialVersionUID=-5554565062847175999L;
    InetAddress _devAddrList[]=null;
    int _addrIndex=0;
    // PRIVATE VARIABLES
    //------------------
    private int maxVarBindLimit=25;
    private int portNum=161;
    private int maxTries=3;
    private int timeout=3000;
    private SnmpPduFactory pduFactory=new SnmpPduFactoryBER();
    private long _maxrtt;
    private long _minrtt;
    private long _avgrtt;
    private SnmpParams _snmpParameter=new SnmpParameters();
    private InetAddress _devAddr=null;
    private int maxSnmpPacketSize=defaultSnmpRequestPktSize;
    private boolean customPduFactory=false;
    // CONSTRUCTORS
    //-------------

    public SnmpPeer(String host) throws UnknownHostException{
        this(host,161);
    }

    public SnmpPeer(String host,int port) throws UnknownHostException{
        useIPAddress(host);
        portNum=port;
    }

    final public synchronized void useIPAddress(String ipaddr) throws UnknownHostException{
        _devAddr=InetAddress.getByName(ipaddr);
    }

    public SnmpPeer(InetAddress netaddr,int port){
        _devAddr=netaddr;
        portNum=port;
    }
    // PUBLIC METHODS
    //---------------

    public SnmpPeer(InetAddress netaddr){
        _devAddr=netaddr;
    }

    final public synchronized String ipAddressInUse(){
        byte[] adr=_devAddr.getAddress();
        return
                (adr[0]&0xFF)+"."+(adr[1]&0xFF)+"."+
                        (adr[2]&0xFF)+"."+(adr[3]&0xFF);
    }

    final public synchronized void useAddressList(InetAddress adrList[]){
        _devAddrList=(adrList!=null)?adrList.clone():null;
        _addrIndex=0;
        useNextAddress();
    }

    final public synchronized void useNextAddress(){
        if(_devAddrList==null)
            return;
/** NPCTE fix for bug 4486059, esc 0 MR 03-August-2001 */
/**      if (_addrIndex > _devAddrList.length) */
        if(_addrIndex>_devAddrList.length-1)
/** end of NPCTE fix for bugid 4486059 */
            _addrIndex=0;
        _devAddr=_devAddrList[_addrIndex++];
    }

    public boolean allowSnmpSets(){
        return _snmpParameter.allowSnmpSets();
    }

    final public InetAddress[] getDestAddrList(){
        return _devAddrList==null?null:_devAddrList.clone();
    }

    final public int getTimeout(){
        return timeout;
    }

    final public synchronized void setTimeout(int newTimeout){
        if(newTimeout<0)
            throw new IllegalArgumentException();
        timeout=newTimeout;
    }

    final public int getMaxTries(){
        return maxTries;
    }

    final public synchronized void setMaxTries(int newMaxTries){
        if(newMaxTries<0)
            throw new IllegalArgumentException();
        maxTries=newMaxTries;
    }

    final public String getDevName(){
        return getDestAddr().getHostName();
    }

    final public InetAddress getDestAddr(){
        return _devAddr;
    }

    @Override
    public String toString(){
        // For security and performance reasons we don't call getHostName here
        // Use getDevName() explicitly when necessary.
        return "Peer/Port : "+getDestAddr().getHostAddress()+"/"+getDestPort();
    }

    final public int getDestPort(){
        return portNum;
    }

    final public synchronized void setDestPort(int newPort){
        portNum=newPort;
    }

    @Override
    protected void finalize(){
        _devAddr=null;
        _devAddrList=null;
        _snmpParameter=null;
    }

    final public synchronized int getVarBindLimit(){
        return maxVarBindLimit;
    }

    final public synchronized void setVarBindLimit(int limit){
        maxVarBindLimit=limit;
    }

    public SnmpParams getParams(){
        return _snmpParameter;
    }

    public void setParams(SnmpParams params){
        _snmpParameter=params;
    }

    final public int getMaxSnmpPktSize(){
        return maxSnmpPacketSize;
    }

    final public synchronized void setMaxSnmpPktSize(int newsize){
        maxSnmpPacketSize=newsize;
    }

    boolean isCustomPduFactory(){
        return customPduFactory;
    }

    public long getMinRtt(){
        return _minrtt;
    }

    public long getMaxRtt(){
        return _maxrtt;
    }

    public long getAvgRtt(){
        return _avgrtt;
    }
    // PRIVATE METHODS
    //----------------

    private void updateRttStats(long tm){
        if(_minrtt>tm)
            _minrtt=tm;
        else if(_maxrtt<tm)
            _maxrtt=tm;
        else
            _avgrtt=tm;  // to do later.
    }
}
