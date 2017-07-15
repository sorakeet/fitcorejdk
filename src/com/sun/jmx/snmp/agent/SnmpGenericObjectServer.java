/**
 * Copyright (c) 2000, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.agent;
// java imports
//

import com.sun.jmx.snmp.SnmpOid;
import com.sun.jmx.snmp.SnmpStatusException;
import com.sun.jmx.snmp.SnmpValue;
import com.sun.jmx.snmp.SnmpVarBind;

import javax.management.*;
import java.util.Enumeration;
import java.util.Iterator;
// jmx imports
//

public class SnmpGenericObjectServer{
    // ----------------------------------------------------------------------
    //
    //    Protected variables
    //
    // ----------------------------------------------------------------------
    protected final MBeanServer server;
    // ----------------------------------------------------------------------
    //
    // Constructors
    //
    // ----------------------------------------------------------------------

    public SnmpGenericObjectServer(MBeanServer server){
        this.server=server;
    }

    public void get(SnmpGenericMetaServer meta,ObjectName name,
                    SnmpMibSubRequest req,int depth)
            throws SnmpStatusException{
        // java.lang.System.out.println(">>>>>>>>> GET " + name);
        final int size=req.getSize();
        final Object data=req.getUserData();
        final String[] nameList=new String[size];
        final SnmpVarBind[] varList=new SnmpVarBind[size];
        final long[] idList=new long[size];
        int i=0;
        for(Enumeration<SnmpVarBind> e=req.getElements();e.hasMoreElements();){
            final SnmpVarBind var=e.nextElement();
            try{
                final long id=var.oid.getOidArc(depth);
                nameList[i]=meta.getAttributeName(id);
                varList[i]=var;
                idList[i]=id;
                // Check the access rights according to the MIB.
                // The MBean might be less restrictive (have a getter
                // while the MIB defines the variable as AFN)
                //
                meta.checkGetAccess(id,data);
                //java.lang.System.out.println(nameList[i] + " added.");
                i++;
            }catch(SnmpStatusException x){
                //java.lang.System.out.println("exception for " + nameList[i]);
                //x.printStackTrace();
                req.registerGetException(var,x);
            }
        }
        AttributeList result=null;
        int errorCode=SnmpStatusException.noSuchInstance;
        try{
            result=server.getAttributes(name,nameList);
        }catch(InstanceNotFoundException f){
            //java.lang.System.out.println(name + ": instance not found.");
            //f.printStackTrace();
            result=new AttributeList();
        }catch(ReflectionException r){
            //java.lang.System.out.println(name + ": reflexion error.");
            //r.printStackTrace();
            result=new AttributeList();
        }catch(Exception x){
            result=new AttributeList();
        }
        final Iterator<?> it=result.iterator();
        for(int j=0;j<i;j++){
            if(!it.hasNext()){
                //java.lang.System.out.println(name + "variable[" + j +
                //                           "] absent");
                final SnmpStatusException x=
                        new SnmpStatusException(errorCode);
                req.registerGetException(varList[j],x);
                continue;
            }
            final Attribute att=(Attribute)it.next();
            while((j<i)&&(!nameList[j].equals(att.getName()))){
                //java.lang.System.out.println(name + "variable[" +j +
                //                           "] not found");
                final SnmpStatusException x=
                        new SnmpStatusException(errorCode);
                req.registerGetException(varList[j],x);
                j++;
            }
            if(j==i) break;
            try{
                varList[j].value=
                        meta.buildSnmpValue(idList[j],att.getValue());
            }catch(SnmpStatusException x){
                req.registerGetException(varList[j],x);
            }
            //java.lang.System.out.println(att.getName() + " retrieved.");
        }
        //java.lang.System.out.println(">>>>>>>>> END GET");
    }

    public SnmpValue get(SnmpGenericMetaServer meta,ObjectName name,
                         long id,Object data)
            throws SnmpStatusException{
        final String attname=meta.getAttributeName(id);
        Object result=null;
        try{
            result=server.getAttribute(name,attname);
        }catch(MBeanException m){
            Exception t=m.getTargetException();
            if(t instanceof SnmpStatusException)
                throw (SnmpStatusException)t;
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        }catch(Exception e){
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        }
        return meta.buildSnmpValue(id,result);
    }

    public void set(SnmpGenericMetaServer meta,ObjectName name,
                    SnmpMibSubRequest req,int depth)
            throws SnmpStatusException{
        final int size=req.getSize();
        final AttributeList attList=new AttributeList(size);
        final String[] nameList=new String[size];
        final SnmpVarBind[] varList=new SnmpVarBind[size];
        final long[] idList=new long[size];
        int i=0;
        for(Enumeration<SnmpVarBind> e=req.getElements();e.hasMoreElements();){
            final SnmpVarBind var=e.nextElement();
            try{
                final long id=var.oid.getOidArc(depth);
                final String attname=meta.getAttributeName(id);
                final Object attvalue=
                        meta.buildAttributeValue(id,var.value);
                final Attribute att=new Attribute(attname,attvalue);
                attList.add(att);
                nameList[i]=attname;
                varList[i]=var;
                idList[i]=id;
                i++;
            }catch(SnmpStatusException x){
                req.registerSetException(var,x);
            }
        }
        AttributeList result;
        int errorCode=SnmpStatusException.noAccess;
        try{
            result=server.setAttributes(name,attList);
        }catch(InstanceNotFoundException f){
            result=new AttributeList();
            errorCode=SnmpStatusException.snmpRspInconsistentName;
        }catch(ReflectionException r){
            errorCode=SnmpStatusException.snmpRspInconsistentName;
            result=new AttributeList();
        }catch(Exception x){
            result=new AttributeList();
        }
        final Iterator<?> it=result.iterator();
        for(int j=0;j<i;j++){
            if(!it.hasNext()){
                final SnmpStatusException x=
                        new SnmpStatusException(errorCode);
                req.registerSetException(varList[j],x);
                continue;
            }
            final Attribute att=(Attribute)it.next();
            while((j<i)&&(!nameList[j].equals(att.getName()))){
                final SnmpStatusException x=
                        new SnmpStatusException(SnmpStatusException.noAccess);
                req.registerSetException(varList[j],x);
                j++;
            }
            if(j==i) break;
            try{
                varList[j].value=
                        meta.buildSnmpValue(idList[j],att.getValue());
            }catch(SnmpStatusException x){
                req.registerSetException(varList[j],x);
            }
        }
    }

    public SnmpValue set(SnmpGenericMetaServer meta,ObjectName name,
                         SnmpValue x,long id,Object data)
            throws SnmpStatusException{
        final String attname=meta.getAttributeName(id);
        final Object attvalue=
                meta.buildAttributeValue(id,x);
        final Attribute att=new Attribute(attname,attvalue);
        Object result=null;
        try{
            server.setAttribute(name,att);
            result=server.getAttribute(name,attname);
        }catch(InvalidAttributeValueException iv){
            throw new
                    SnmpStatusException(SnmpStatusException.snmpRspWrongValue);
        }catch(InstanceNotFoundException f){
            throw new
                    SnmpStatusException(SnmpStatusException.snmpRspInconsistentName);
        }catch(ReflectionException r){
            throw new
                    SnmpStatusException(SnmpStatusException.snmpRspInconsistentName);
        }catch(MBeanException m){
            Exception t=m.getTargetException();
            if(t instanceof SnmpStatusException)
                throw (SnmpStatusException)t;
            throw new
                    SnmpStatusException(SnmpStatusException.noAccess);
        }catch(Exception e){
            throw new
                    SnmpStatusException(SnmpStatusException.noAccess);
        }
        return meta.buildSnmpValue(id,result);
    }

    public void check(SnmpGenericMetaServer meta,ObjectName name,
                      SnmpMibSubRequest req,int depth)
            throws SnmpStatusException{
        final Object data=req.getUserData();
        for(Enumeration<SnmpVarBind> e=req.getElements();e.hasMoreElements();){
            final SnmpVarBind var=e.nextElement();
            try{
                final long id=var.oid.getOidArc(depth);
                // call meta.check() here, and meta.check will call check()
                check(meta,name,var.value,id,data);
            }catch(SnmpStatusException x){
                req.registerCheckException(var,x);
            }
        }
    }

    // XXX xxx ZZZ zzz Maybe we should go through the MBeanInfo here?
    public void check(SnmpGenericMetaServer meta,ObjectName name,
                      SnmpValue x,long id,Object data)
            throws SnmpStatusException{
        meta.checkSetAccess(x,id,data);
        try{
            final String attname=meta.getAttributeName(id);
            final Object attvalue=meta.buildAttributeValue(id,x);
            final Object[] params=new Object[1];
            final String[] signature=new String[1];
            params[0]=attvalue;
            signature[0]=attvalue.getClass().getName();
            server.invoke(name,"check"+attname,params,signature);
        }catch(SnmpStatusException e){
            throw e;
        }catch(InstanceNotFoundException i){
            throw new
                    SnmpStatusException(SnmpStatusException.snmpRspInconsistentName);
        }catch(ReflectionException r){
            // checkXXXX() not defined => do nothing
        }catch(MBeanException m){
            Exception t=m.getTargetException();
            if(t instanceof SnmpStatusException)
                throw (SnmpStatusException)t;
            throw new SnmpStatusException(SnmpStatusException.noAccess);
        }catch(Exception e){
            throw new
                    SnmpStatusException(SnmpStatusException.noAccess);
        }
    }

    public void registerTableEntry(SnmpMibTable meta,SnmpOid rowOid,
                                   ObjectName objname,Object entry)
            throws SnmpStatusException{
        if(objname==null)
            throw new
                    SnmpStatusException(SnmpStatusException.snmpRspInconsistentName);
        try{
            if(entry!=null&&!server.isRegistered(objname))
                server.registerMBean(entry,objname);
        }catch(InstanceAlreadyExistsException e){
            throw new
                    SnmpStatusException(SnmpStatusException.snmpRspInconsistentName);
        }catch(MBeanRegistrationException e){
            throw new SnmpStatusException(SnmpStatusException.snmpRspNoAccess);
        }catch(NotCompliantMBeanException e){
            throw new SnmpStatusException(SnmpStatusException.snmpRspGenErr);
        }catch(RuntimeOperationsException e){
            throw new SnmpStatusException(SnmpStatusException.snmpRspGenErr);
        }catch(Exception e){
            throw new SnmpStatusException(SnmpStatusException.snmpRspGenErr);
        }
    }
}
