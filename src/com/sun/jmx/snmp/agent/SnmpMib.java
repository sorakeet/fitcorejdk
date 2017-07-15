/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.agent;

import com.sun.jmx.snmp.SnmpDefinitions;
import com.sun.jmx.snmp.SnmpOid;
import com.sun.jmx.snmp.SnmpStatusException;
import com.sun.jmx.snmp.SnmpVarBind;

import javax.management.*;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Level;

import static com.sun.jmx.defaults.JmxProperties.SNMP_ADAPTOR_LOGGER;

public abstract class SnmpMib extends SnmpMibAgent implements Serializable{
    // --------------------------------------------------------------------
    // PROTECTED VARIABLES
    // --------------------------------------------------------------------
    protected SnmpMibOid root;
    // --------------------------------------------------------------------
    // POLYMORHIC METHODS
    // --------------------------------------------------------------------
    // --------------------------------------------------------------------
    // PRIVATE VARIABLES
    // --------------------------------------------------------------------
    private transient long[] rootOid=null;

    public SnmpMib(){
        root=new SnmpMibOid();
    }

    protected String getGroupOid(String groupName,String defaultOid){
        return defaultOid;
    }

    protected ObjectName getGroupObjectName(String name,String oid,
                                            String defaultName)
            throws MalformedObjectNameException{
        return new ObjectName(defaultName);
    }

    protected void registerGroupNode(String groupName,String groupOid,
                                     ObjectName groupObjName,SnmpMibNode node,
                                     Object group,MBeanServer server)
            throws NotCompliantMBeanException, MBeanRegistrationException,
            InstanceAlreadyExistsException, IllegalAccessException{
        root.registerNode(groupOid,node);
        if(server!=null&&groupObjName!=null&&group!=null)
            server.registerMBean(group,groupObjName);
    }
    // --------------------------------------------------------------------
    // PUBLIC METHODS
    // --------------------------------------------------------------------

    public abstract void registerTableMeta(String name,SnmpMibTable table);

    public abstract SnmpMibTable getRegisteredTableMeta(String name);

    // Implements the method defined in SnmpMibAgent. See SnmpMibAgent
    // for java-doc
    //
    @Override
    public void get(SnmpMibRequest req) throws SnmpStatusException{
        // Builds the request tree: creation is not allowed, operation
        // is not atomic.
        final int reqType=SnmpDefinitions.pduGetRequestPdu;
        SnmpRequestTree handlers=getHandlers(req,false,false,reqType);
        SnmpRequestTree.Handler h=null;
        SnmpMibNode meta=null;
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpMib.class.getName(),
                    "get","Processing handlers for GET... ");
        }
        // For each sub-request stored in the request-tree, invoke the
        // get() method.
        for(Enumeration<SnmpRequestTree.Handler> eh=handlers.getHandlers();eh.hasMoreElements();){
            h=eh.nextElement();
            // Gets the Meta node. It can be either a Group Meta or a
            // Table Meta.
            //
            meta=handlers.getMetaNode(h);
            // Gets the depth of the Meta node in the OID tree
            final int depth=handlers.getOidDepth(h);
            for(Enumeration<SnmpMibSubRequest> rqs=handlers.getSubRequests(h);
                rqs.hasMoreElements();){
                // Invoke the get() operation.
                meta.get(rqs.nextElement(),depth);
            }
        }
    }

    // Implements the method defined in SnmpMibAgent. See SnmpMibAgent
    // for java-doc
    //
    @Override
    public void getNext(SnmpMibRequest req) throws SnmpStatusException{
        // Build the request tree for the operation
        // The subrequest stored in the request tree are valid GET requests
        SnmpRequestTree handlers=getGetNextHandlers(req);
        SnmpRequestTree.Handler h;
        SnmpMibNode meta;
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpMib.class.getName(),
                    "getNext","Processing handlers for GET-NEXT... ");
        }
        // Now invoke get() for each subrequest of the request tree.
        for(Enumeration<SnmpRequestTree.Handler> eh=handlers.getHandlers();eh.hasMoreElements();){
            h=eh.nextElement();
            // Gets the Meta node. It can be either a Group Meta or a
            // Table Meta.
            //
            meta=handlers.getMetaNode(h);
            // Gets the depth of the Meta node in the OID tree
            int depth=handlers.getOidDepth(h);
            for(Enumeration<SnmpMibSubRequest> rqs=handlers.getSubRequests(h);
                rqs.hasMoreElements();){
                // Invoke the get() operation
                meta.get(rqs.nextElement(),depth);
            }
        }
    }

    // Implements the method defined in SnmpMibAgent. See SnmpMibAgent
    // for java-doc
    //
    @Override
    public void getBulk(SnmpMibRequest req,int nonRepeat,int maxRepeat)
            throws SnmpStatusException{
        getBulkWithGetNext(req,nonRepeat,maxRepeat);
    }

    // Implements the method defined in SnmpMibAgent. See SnmpMibAgent
    // for java-doc
    //
    @Override
    public void set(SnmpMibRequest req) throws SnmpStatusException{
        SnmpRequestTree handlers=null;
        // Optimization: we're going to get the whole SnmpRequestTree
        // built in the "check" method, so that we don't have to rebuild
        // it here.
        //
        if(req instanceof SnmpMibRequestImpl)
            handlers=((SnmpMibRequestImpl)req).getRequestTree();
        // Optimization didn't work: we have to rebuild the tree.
        //
        // Builds the request tree: creation is not allowed, operation
        // is atomic.
        //
        final int reqType=SnmpDefinitions.pduSetRequestPdu;
        if(handlers==null) handlers=getHandlers(req,false,true,reqType);
        handlers.switchCreationFlag(false);
        handlers.setPduType(reqType);
        SnmpRequestTree.Handler h;
        SnmpMibNode meta;
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpMib.class.getName(),
                    "set","Processing handlers for SET... ");
        }
        // For each sub-request stored in the request-tree, invoke the
        // get() method.
        for(Enumeration<SnmpRequestTree.Handler> eh=handlers.getHandlers();eh.hasMoreElements();){
            h=eh.nextElement();
            // Gets the Meta node. It can be either a Group Meta or a
            // Table Meta.
            //
            meta=handlers.getMetaNode(h);
            // Gets the depth of the Meta node in the OID tree
            final int depth=handlers.getOidDepth(h);
            for(Enumeration<SnmpMibSubRequest> rqs=handlers.getSubRequests(h);
                rqs.hasMoreElements();){
                // Invoke the set() operation
                meta.set(rqs.nextElement(),depth);
            }
        }
    }
    // --------------------------------------------------------------------
    // PRIVATE METHODS
    //---------------------------------------------------------------------

    // Implements the method defined in SnmpMibAgent. See SnmpMibAgent
    // for java-doc
    //
    @Override
    public void check(SnmpMibRequest req) throws SnmpStatusException{
        final int reqType=SnmpDefinitions.pduWalkRequest;
        // Builds the request tree: creation is allowed, operation
        // is atomic.
        SnmpRequestTree handlers=getHandlers(req,true,true,reqType);
        SnmpRequestTree.Handler h;
        SnmpMibNode meta;
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpMib.class.getName(),
                    "check","Processing handlers for CHECK... ");
        }
        // For each sub-request stored in the request-tree, invoke the
        // check() method.
        for(Enumeration<SnmpRequestTree.Handler> eh=handlers.getHandlers();eh.hasMoreElements();){
            h=eh.nextElement();
            // Gets the Meta node. It can be either a Group Meta or a
            // Table Meta.
            //
            meta=handlers.getMetaNode(h);
            // Gets the depth of the Meta node in the OID tree
            final int depth=handlers.getOidDepth(h);
            for(Enumeration<SnmpMibSubRequest> rqs=handlers.getSubRequests(h);
                rqs.hasMoreElements();){
                // Invoke the check() operation
                meta.check(rqs.nextElement(),depth);
            }
        }
        // Optimization: we're going to pass the whole SnmpRequestTree
        // to the "set" method, so that we don't have to rebuild it there.
        //
        if(req instanceof SnmpMibRequestImpl){
            ((SnmpMibRequestImpl)req).setRequestTree(handlers);
        }
    }

    @Override
    public long[] getRootOid(){
        if(rootOid==null){
            Vector<Integer> list=new Vector<>(10);
            // Ask the tree to do the job !
            //
            root.getRootOid(list);
            // Now format the result
            //
            rootOid=new long[list.size()];
            int i=0;
            for(Enumeration<Integer> e=list.elements();e.hasMoreElements();){
                Integer val=e.nextElement();
                rootOid[i++]=val.longValue();
            }
        }
        return rootOid.clone();
    }

    private SnmpRequestTree getGetNextHandlers(SnmpMibRequest req)
            throws SnmpStatusException{
        // Creates an empty request tree, no entry creation is allowed (false)
        SnmpRequestTree handlers=new
                SnmpRequestTree(req,false,SnmpDefinitions.pduGetNextRequestPdu);
        // Sets the getNext flag: if version=V2, status exception are
        // transformed in  endOfMibView
        handlers.setGetNextFlag();
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpMib.class.getName(),
                    "getGetNextHandlers","Received MIB request : "+req);
        }
        AcmChecker checker=new AcmChecker(req);
        int index=0;
        SnmpVarBind var=null;
        final int ver=req.getVersion();
        SnmpOid original=null;
        // For each varbind, finds the handling node.
        // This function has the side effect of transforming a GET-NEXT
        // request into a valid GET request, replacing the OIDs in the
        // original GET-NEXT request with the OID of the first leaf that
        // follows.
        for(Enumeration<SnmpVarBind> e=req.getElements();e.hasMoreElements();index++){
            var=e.nextElement();
            SnmpOid result;
            try{
                // Find the node handling the OID that follows the varbind
                // OID. `result' contains this next leaf OID.
                //ACM loop.
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                            SnmpMib.class.getName(),
                            "getGetNextHandlers"," Next OID of : "+var.oid);
                }
                result=new SnmpOid(root.findNextHandlingNode
                        (var,var.oid.longValue(false),0,
                                0,handlers,checker));
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                            SnmpMib.class.getName(),
                            "getGetNextHandlers"," is : "+result);
                }
                // We replace the varbind original OID with the OID of the
                // leaf object we have to return.
                var.oid=result;
            }catch(SnmpStatusException x){
                // if (isDebugOn())
                //    debug("getGetNextHandlers",
                //        "Couldn't find a handling node for "
                //        + var.oid.toString());
                if(ver==SnmpDefinitions.snmpVersionOne){
                    if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMib.class.getName(),
                                "getGetNextHandlers",
                                "\tThrowing exception "+x.toString());
                    }
                    // The index in the exception must correspond to the
                    // SNMP index ...
                    //
                    throw new SnmpStatusException(x,index+1);
                }
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                            SnmpMib.class.getName(),
                            "getGetNextHandlers",
                            "Exception : "+x.getStatus());
                }
                var.setSnmpValue(SnmpVarBind.endOfMibView);
            }
        }
        return handlers;
    }

    private SnmpRequestTree getHandlers(SnmpMibRequest req,
                                        boolean createflag,boolean atomic,
                                        int type)
            throws SnmpStatusException{
        // Build an empty request tree
        SnmpRequestTree handlers=
                new SnmpRequestTree(req,createflag,type);
        int index=0;
        SnmpVarBind var;
        final int ver=req.getVersion();
        // For each varbind in the list finds its handling node.
        for(Enumeration<SnmpVarBind> e=req.getElements();e.hasMoreElements();index++){
            var=e.nextElement();
            try{
                // Find the handling node for this varbind.
                root.findHandlingNode(var,var.oid.longValue(false),
                        0,handlers);
            }catch(SnmpStatusException x){
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                            SnmpMib.class.getName(),
                            "getHandlers",
                            "Couldn't find a handling node for "+
                                    var.oid.toString());
                }
                // If the operation is atomic (Check/Set) or the version
                // is V1 we must generate an exception.
                //
                if(ver==SnmpDefinitions.snmpVersionOne){
                    if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMib.class.getName(),
                                "getHandlers","\tV1: Throwing exception");
                    }
                    // The index in the exception must correspond to the
                    // SNMP index ...
                    //
                    final SnmpStatusException sse=
                            new SnmpStatusException(x,index+1);
                    sse.initCause(x);
                    throw sse;
                }else if((type==SnmpDefinitions.pduWalkRequest)||
                        (type==SnmpDefinitions.pduSetRequestPdu)){
                    final int status=
                            SnmpRequestTree.mapSetException(x.getStatus(),ver);
                    if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMib.class.getName(),
                                "getHandlers","\tSET: Throwing exception");
                    }
                    final SnmpStatusException sse=
                            new SnmpStatusException(status,index+1);
                    sse.initCause(x);
                    throw sse;
                }else if(atomic){
                    // Should never come here...
                    if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMib.class.getName(),
                                "getHandlers","\tATOMIC: Throwing exception");
                    }
                    final SnmpStatusException sse=
                            new SnmpStatusException(x,index+1);
                    sse.initCause(x);
                    throw sse;
                }
                final int status=
                        SnmpRequestTree.mapGetException(x.getStatus(),ver);
                if(status==SnmpStatusException.noSuchInstance){
                    if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMib.class.getName(),
                                "getHandlers",
                                "\tGET: Registering noSuchInstance");
                    }
                    var.value=SnmpVarBind.noSuchInstance;
                }else if(status==SnmpStatusException.noSuchObject){
                    if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMib.class.getName(),
                                "getHandlers",
                                "\tGET: Registering noSuchObject");
                    }
                    var.value=SnmpVarBind.noSuchObject;
                }else{
                    if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMib.class.getName(),
                                "getHandlers",
                                "\tGET: Registering global error: "+status);
                    }
                    final SnmpStatusException sse=
                            new SnmpStatusException(status,index+1);
                    sse.initCause(x);
                    throw sse;
                }
            }
        }
        return handlers;
    }
}
