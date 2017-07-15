/**
 * Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.protocol;

import com.sun.corba.se.impl.encoding.MarshalInputStream;
import com.sun.corba.se.impl.encoding.MarshalOutputStream;
import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.pept.protocol.MessageMediator;
import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.ior.ObjectKey;
import com.sun.corba.se.spi.logging.CORBALogDomains;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.protocol.CorbaMessageMediator;
import com.sun.corba.se.spi.protocol.CorbaServerRequestDispatcher;
import org.omg.CORBA.SystemException;

import java.util.Iterator;

public class BootstrapServerRequestDispatcher
        implements CorbaServerRequestDispatcher{
    private static final boolean debug=false;
    ORBUtilSystemException wrapper;
    private ORB orb;

    public BootstrapServerRequestDispatcher(ORB orb){
        this.orb=orb;
        this.wrapper=ORBUtilSystemException.get(orb,
                CORBALogDomains.RPC_PROTOCOL);
    }

    public void dispatch(MessageMediator messageMediator){
        CorbaMessageMediator request=(CorbaMessageMediator)messageMediator;
        CorbaMessageMediator response=null;
        try{
            MarshalInputStream is=(MarshalInputStream)
                    request.getInputObject();
            String method=request.getOperationName();
            response=request.getProtocolHandler().createResponse(request,null);
            MarshalOutputStream os=(MarshalOutputStream)
                    response.getOutputObject();
            if(method.equals("get")){
                // Get the name of the requested service
                String serviceKey=is.read_string();
                // Look it up
                org.omg.CORBA.Object serviceObject=
                        orb.getLocalResolver().resolve(serviceKey);
                // Write reply value
                os.write_Object(serviceObject);
            }else if(method.equals("list")){
                java.util.Set keys=orb.getLocalResolver().list();
                os.write_long(keys.size());
                Iterator iter=keys.iterator();
                while(iter.hasNext()){
                    String obj=(String)iter.next();
                    os.write_string(obj);
                }
            }else{
                throw wrapper.illegalBootstrapOperation(method);
            }
        }catch(SystemException ex){
            // Marshal the exception thrown
            response=request.getProtocolHandler().createSystemExceptionResponse(
                    request,ex,null);
        }catch(RuntimeException ex){
            // Unknown exception
            SystemException sysex=wrapper.bootstrapRuntimeException(ex);
            response=request.getProtocolHandler().createSystemExceptionResponse(
                    request,sysex,null);
        }catch(Exception ex){
            // Unknown exception
            SystemException sysex=wrapper.bootstrapException(ex);
            response=request.getProtocolHandler().createSystemExceptionResponse(
                    request,sysex,null);
        }
        return;
    }

    public IOR locate(ObjectKey objectKey){
        return null;
    }

    public int getId(){
        throw wrapper.genericNoImpl();
    }
}
