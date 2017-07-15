/**
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 */
/**
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 */
package com.sun.corba.se.impl.corba;

import com.sun.corba.se.spi.orb.ORB;
///////////////////////////////////////////////////////////////////////////
// helper class for deferred invocations

public class AsynchInvoke implements Runnable{
    private RequestImpl _req;
    private ORB _orb;
    private boolean _notifyORB;

    public AsynchInvoke(ORB o,RequestImpl reqToInvokeOn,boolean n){
        _orb=o;
        _req=reqToInvokeOn;
        _notifyORB=n;
    }

    ;

    public void run(){
        // do the actual invocation
        _req.doInvocation();
        // for the asynchronous case, note that the response has been
        // received.
        synchronized(_req){
            // update local boolean indicator
            _req.gotResponse=true;
            // notify any client waiting on a 'get_response'
            _req.notify();
        }
        if(_notifyORB==true){
            _orb.notifyORB();
        }
    }
};
///////////////////////////////////////////////////////////////////////////
