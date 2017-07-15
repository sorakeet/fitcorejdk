/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.agent;

import com.sun.jmx.snmp.*;

import javax.management.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;

import static com.sun.jmx.defaults.JmxProperties.SNMP_ADAPTOR_LOGGER;

public abstract class SnmpMibTable extends SnmpMibNode
        implements NotificationBroadcaster, Serializable{
    //    private Vector indexes= new Vector();
    // private Vector oids= new Vector();
    private final static int Delta=16;
    // -------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------
    private final Vector<Object> entries=new Vector<>();
    private final Vector<ObjectName> entrynames=new Vector<>();
    // ----------------------------------------------------------------------
    // PROTECTED VARIABLES
    // ----------------------------------------------------------------------
    protected int nodeId=1;
    protected SnmpMib theMib;
    protected boolean creationEnabled=false;
    protected SnmpTableEntryFactory factory=null;
    // PACKAGE VARIABLES
    //------------------
    transient long sequenceNumber=0;
    // ----------------------------------------------------------------------
    // PRIVATE VARIABLES
    // ----------------------------------------------------------------------
    private int size=0;
    private int tablecount=0;
    private int tablesize=Delta;
    private SnmpOid tableoids[]=new SnmpOid[tablesize];
    // final Vector callbacks = new Vector();
    private Hashtable<NotificationListener,Vector<Object>> handbackTable=
            new Hashtable<>();
    private Hashtable<NotificationListener,Vector<NotificationFilter>>
            filterTable=new Hashtable<>();

    public SnmpMibTable(SnmpMib mib){
        this.theMib=mib;
        setCreationEnabled(false);
    }

    // public void addEntry(SnmpIndex index, Object entry)
    public void addEntry(SnmpOid rowOid,Object entry)
            throws SnmpStatusException{
        addEntry(rowOid,null,entry);
    }

    // protected synchronized void addEntry(SnmpIndex index, ObjectName name,
    //                                      Object entry)
    public synchronized void addEntry(SnmpOid oid,ObjectName name,
                                      Object entry)
            throws SnmpStatusException{
        if(isRegistrationRequired()==true&&name==null)
            throw new SnmpStatusException(SnmpStatusException.badValue);
        if(size==0){
            //            indexes.addElement(index);
            // XX oids.addElement(oid);
            insertOid(0,oid);
            if(entries!=null)
                entries.addElement(entry);
            if(entrynames!=null)
                entrynames.addElement(name);
            size++;
            // triggers callbacks on the entry factory
            //
            if(factory!=null){
                try{
                    factory.addEntryCb(0,oid,name,entry,this);
                }catch(SnmpStatusException x){
                    removeOid(0);
                    if(entries!=null)
                        entries.removeElementAt(0);
                    if(entrynames!=null)
                        entrynames.removeElementAt(0);
                    throw x;
                }
            }
            // sends the notifications
            //
            sendNotification(SnmpTableEntryNotification.SNMP_ENTRY_ADDED,
                    (new Date()).getTime(),entry,name);
            return;
        }
        // Get the insertion position ...
        //
        int pos=0;
        // bug jaw.00356.B : use oid rather than index to get the
        // insertion point.
        //
        pos=getInsertionPoint(oid,true);
        if(pos==size){
            // Add a new element in the vectors ...
            //
            //            indexes.addElement(index);
            // XX oids.addElement(oid);
            insertOid(tablecount,oid);
            if(entries!=null)
                entries.addElement(entry);
            if(entrynames!=null)
                entrynames.addElement(name);
            size++;
        }else{
            // Insert new element ...
            //
            try{
                //                indexes.insertElementAt(index, pos);
                // XX oids.insertElementAt(oid, pos);
                insertOid(pos,oid);
                if(entries!=null)
                    entries.insertElementAt(entry,pos);
                if(entrynames!=null)
                    entrynames.insertElementAt(name,pos);
                size++;
            }catch(ArrayIndexOutOfBoundsException e){
            }
        }
        // triggers callbacks on the entry factory
        //
        if(factory!=null){
            try{
                factory.addEntryCb(pos,oid,name,entry,this);
            }catch(SnmpStatusException x){
                removeOid(pos);
                if(entries!=null)
                    entries.removeElementAt(pos);
                if(entrynames!=null)
                    entrynames.removeElementAt(pos);
                throw x;
            }
        }
        // sends the notifications
        //
        sendNotification(SnmpTableEntryNotification.SNMP_ENTRY_ADDED,
                (new Date()).getTime(),entry,name);
    }

    public abstract boolean isRegistrationRequired();
    // EVENT STUFF
    //------------

    private int getInsertionPoint(SnmpOid oid,boolean fail)
            throws SnmpStatusException{
        final int failStatus=SnmpStatusException.snmpRspNotWritable;
        int low=0;
        int max=size-1;
        SnmpOid pos;
        int comp;
        int curr=low+(max-low)/2;
        while(low<=max){
            // XX pos= (SnmpOid) oids.elementAt(curr);
            pos=tableoids[curr];
            // never know ...we might find something ...
            //
            comp=oid.compareTo(pos);
            if(comp==0){
                if(fail)
                    throw new SnmpStatusException(failStatus,curr);
                else
                    return curr+1;
            }
            if(comp>0){
                low=curr+1;
            }else{
                max=curr-1;
            }
            curr=low+(max-low)/2;
        }
        return curr;
    }

    private void insertOid(int pos,SnmpOid oid){
        if(pos>=tablesize||tablecount==tablesize){
            // Vector must be enlarged
            // Save old vector
            final SnmpOid[] olde=tableoids;
            // Allocate larger vectors
            tablesize+=Delta;
            tableoids=new SnmpOid[tablesize];
            // Check pos validity
            if(pos>tablecount) pos=tablecount;
            if(pos<0) pos=0;
            final int l1=pos;
            final int l2=tablecount-pos;
            // Copy original vector up to `pos'
            if(l1>0)
                System.arraycopy(olde,0,tableoids,0,l1);
            // Copy original vector from `pos' to end, leaving
            // an empty room at `pos' in the new vector.
            if(l2>0)
                System.arraycopy(olde,l1,tableoids,
                        l1+1,l2);
        }else if(pos<tablecount){
            // Vector is large enough to accommodate one additional
            // entry.
            //
            // Shift vector, making an empty room at `pos'
            System.arraycopy(tableoids,pos,tableoids,
                    pos+1,tablecount-pos);
        }
        // Fill the gap at `pos'
        tableoids[pos]=oid;
        tablecount++;
    }

    public synchronized void removeEntry(SnmpOid rowOid,Object entry)
            throws SnmpStatusException{
        int pos=findObject(rowOid);
        if(pos==-1)
            return;
        removeEntry(pos,entry);
    }

    public synchronized Object getEntry(SnmpOid rowOid)
            throws SnmpStatusException{
        int pos=findObject(rowOid);
        if(pos==-1)
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        return entries.elementAt(pos);
    }
    // ----------------------------------------------------------------------
    // PROTECTED METHODS - RowStatus
    // ----------------------------------------------------------------------

    public synchronized ObjectName getEntryName(SnmpOid rowOid)
            throws SnmpStatusException{
        int pos=findObject(rowOid);
        if(entrynames==null) return null;
        if(pos==-1||pos>=entrynames.size())
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        return entrynames.elementAt(pos);
    }

    public Object[] getBasicEntries(){
        Object[] array=new Object[size];
        entries.copyInto(array);
        return array;
    }

    public int getSize(){
        return size;
    }

    @Override
    public synchronized void
    addNotificationListener(NotificationListener listener,
                            NotificationFilter filter,Object handback){
        // Check listener
        //
        if(listener==null){
            throw new IllegalArgumentException
                    ("Listener can't be null");
        }
        // looking for listener in handbackTable
        //
        Vector<Object> handbackList=handbackTable.get(listener);
        Vector<NotificationFilter> filterList=filterTable.get(listener);
        if(handbackList==null){
            handbackList=new Vector<>();
            filterList=new Vector<>();
            handbackTable.put(listener,handbackList);
            filterTable.put(listener,filterList);
        }
        // Add the handback and the filter
        //
        handbackList.addElement(handback);
        filterList.addElement(filter);
    }

    @Override
    public synchronized void
    removeNotificationListener(NotificationListener listener)
            throws ListenerNotFoundException{
        // looking for listener in handbackTable
        //
        Vector<?> handbackList=handbackTable.get(listener);
        if(handbackList==null){
            throw new ListenerNotFoundException("listener");
        }
        // If handback is null, remove the listener entry
        //
        handbackTable.remove(listener);
        filterTable.remove(listener);
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo(){
        String[] types={SnmpTableEntryNotification.SNMP_ENTRY_ADDED,
                SnmpTableEntryNotification.SNMP_ENTRY_REMOVED};
        MBeanNotificationInfo[] notifsInfo={
                new MBeanNotificationInfo
                        (types,"com.sun.jmx.snmp.agent.SnmpTableEntryNotification",
                                "Notifications sent by the SnmpMibTable")
        };
        return notifsInfo;
    }

    public void registerEntryFactory(SnmpTableEntryFactory factory){
        this.factory=factory;
    }

    protected long getNextVarEntryId(SnmpOid rowOid,
                                     long var,
                                     Object userData,
                                     int pduVersion)
            throws SnmpStatusException{
        long varid=var;
        do{
            varid=getNextVarEntryId(rowOid,varid,userData);
        }while(skipEntryVariable(rowOid,varid,userData,pduVersion));
        return varid;
    }

    protected boolean skipEntryVariable(SnmpOid rowOid,
                                        long var,
                                        Object userData,
                                        int pduVersion){
        return false;
    }

    protected SnmpOid getNextOid(SnmpOid oid,Object userData)
            throws SnmpStatusException{
        if(size==0){
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        }
        final SnmpOid resOid=oid;
        // Just a simple check to speed up retrieval of last element ...
        //
        // XX SnmpOid last= (SnmpOid) oids.lastElement();
        SnmpOid last=tableoids[tablecount-1];
        if(last.equals(resOid)){
            // Last element of the table ...
            //
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        }
        // First find the oid. This will allow to speed up retrieval process
        // during smart discovery of table (using the getNext) as the
        // management station will use the valid index returned during a
        // previous getNext ...
        //
        // Returns the position following the position at which resOid
        // is found, or the position at which resOid should be inserted.
        //
        final int newPos=getInsertionPoint(resOid,false);
        // If the position returned is not out of bound, we will find
        // the next element in the array.
        //
        if(newPos>-1&&newPos<size){
            try{
                // XX last = (SnmpOid) oids.elementAt(newPos);
                last=tableoids[newPos];
            }catch(ArrayIndexOutOfBoundsException e){
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
            }
        }else{
            // We are dealing with the last element of the table ..
            //
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        }
        return last;
    }
    // -------------------------------------------------------------------
    // PROTECTED METHODS - get next
    // -------------------------------------------------------------------

    protected SnmpOid getNextOid(Object userData)
            throws SnmpStatusException{
        if(size==0){
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        }
        // XX return (SnmpOid) oids.firstElement();
        return tableoids[0];
    }

    abstract protected long getNextVarEntryId(SnmpOid rowOid,long var,
                                              Object userData)
            throws SnmpStatusException;

    abstract protected boolean isReadableEntryId(SnmpOid rowOid,long var,
                                                 Object userData)
            throws SnmpStatusException;

    SnmpOid getNextOid(long[] oid,int pos,Object userData)
            throws SnmpStatusException{
        // Construct the sub-oid starting at pos.
        // This sub-oid correspond to the oid part just after the entry
        // variable oid.
        //
        final SnmpOid resOid=new SnmpEntryOid(oid,pos);
        return getNextOid(resOid,userData);
    }
    // -------------------------------------------------------------------
    // Abstract Protected Methods
    // -------------------------------------------------------------------

    // ---------------------------------------------------------------------
    //
    // Implements the method defined in SnmpMibNode.
    //
    // ---------------------------------------------------------------------
    @Override
    final synchronized void findHandlingNode(SnmpVarBind varbind,
                                             long[] oid,int depth,
                                             SnmpRequestTree handlers)
            throws SnmpStatusException{
        final int length=oid.length;
        if(handlers==null)
            throw new SnmpStatusException(SnmpStatusException.snmpRspGenErr);
        if(depth>=length)
            throw new SnmpStatusException(SnmpStatusException.noAccess);
        if(oid[depth]!=nodeId)
            throw new SnmpStatusException(SnmpStatusException.noAccess);
        if(depth+2>=length)
            throw new SnmpStatusException(SnmpStatusException.noAccess);
        // Checks that the oid is valid
        // validateOid(oid,depth);
        // Gets the part of the OID that identifies the entry
        final SnmpOid entryoid=new SnmpEntryOid(oid,depth+2);
        // Finds the entry: false means that the entry does not exists
        final Object data=handlers.getUserData();
        final boolean hasEntry=contains(entryoid,data);
        // Fails if the entry is not found and the table does not
        // not support creation.
        // We know that the entry does not exists if (isentry == false).
        if(!hasEntry){
            if(!handlers.isCreationAllowed()){
                // we're not doing a set
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
            }else if(!isCreationEnabled())
                // we're doing a set but creation is disabled.
                throw new
                        SnmpStatusException(SnmpStatusException.snmpRspNoAccess);
        }
        final long var=oid[depth+1];
        // Validate the entry id
        if(hasEntry){
            // The entry already exists - validate the id
            validateVarEntryId(entryoid,var,data);
        }
        // Registers this node for the identified entry.
        //
        if(handlers.isSetRequest()&&isRowStatus(entryoid,var,data))
            // We only try to identify the RowStatus for SET operations
            //
            handlers.add(this,depth,entryoid,varbind,(!hasEntry),varbind);
        else
            handlers.add(this,depth,entryoid,varbind,(!hasEntry));
    }

    protected boolean isRowStatus(SnmpOid rowOid,long var,
                                  Object userData){
        return false;
    }

    abstract protected void validateVarEntryId(SnmpOid rowOid,long var,
                                               Object userData)
            throws SnmpStatusException;

    // ---------------------------------------------------------------------
    //
    // Implements the method defined in SnmpMibNode. The algorithm is very
    // largely inspired from the original getNext() method.
    //
    // ---------------------------------------------------------------------
    @Override
    final synchronized long[] findNextHandlingNode(SnmpVarBind varbind,
                                                   long[] oid,
                                                   int pos,
                                                   int depth,
                                                   SnmpRequestTree handlers,
                                                   AcmChecker checker)
            throws SnmpStatusException{
        int length=oid.length;
        if(handlers==null){
            // This should be considered as a genErr, but we do not want to
            // abort the whole request, so we're going to throw
            // a noSuchObject...
            //
            throw new SnmpStatusException(SnmpStatusException.noSuchObject);
        }
        final Object data=handlers.getUserData();
        final int pduVersion=handlers.getRequestPduVersion();
        long var=-1;
        // If the querried oid contains less arcs than the OID of the
        // xxxEntry object, we must return the first leaf under the
        // first columnar object: the best way to do that is to reset
        // the queried oid:
        //   oid[0] = nodeId (arc of the xxxEntry object)
        //   pos    = 0 (points to the arc of the xxxEntry object)
        // then we just have to proceed...
        //
        if(pos>=length){
            // this will have the side effect to set
            //    oid[pos] = nodeId
            // and
            //    (pos+1) = length
            // so we won't fall into the "else if" cases below -
            // so using "else if" rather than "if ..." is guaranteed
            // to be safe.
            //
            oid=new long[1];
            oid[0]=nodeId;
            pos=0;
            length=1;
        }else if(oid[pos]>nodeId){
            // oid[pos] is expected to be the id of the xxxEntry ...
            // The id requested is greater than the id of the xxxEntry,
            // so we won't find the next element in this table... (any
            // element in this table will have a smaller OID)
            //
            throw new SnmpStatusException(SnmpStatusException.noSuchObject);
        }else if(oid[pos]<nodeId){
            // we must return the first leaf under the first columnar
            // object, so we are back to our first case where pos was
            // out of bounds... => reset the oid to contain only the
            // arc of the xxxEntry object.
            //
            oid=new long[1];
            oid[0]=nodeId;
            pos=0;
            length=0;
        }else if((pos+1)<length){
            // The arc at the position "pos+1" is the id of the columnar
            // object (ie: the id of the variable in the table entry)
            //
            var=oid[pos+1];
        }
        // Now that we've got everything right we can begin.
        SnmpOid entryoid;
        if(pos==(length-1)){
            // pos points to the last arc in the oid, and this arc is
            // guaranteed to be the xxxEntry id (we have handled all
            // the other possibilities before)
            //
            // We must therefore return the first leaf below the first
            // columnar object in the table.
            //
            // Get the first index. If an exception is raised,
            // then it means that the table is empty. We thus do not
            // have to catch the exception - we let it propagate to
            // the caller.
            //
            entryoid=getNextOid(data);
            var=getNextVarEntryId(entryoid,var,data,pduVersion);
        }else if(pos==(length-2)){
            // In that case we have (pos+1) = (length-1), so pos
            // points to the arc of the querried variable (columnar object).
            // Since the requested oid stops there, it means we have
            // to return the first leaf under this columnar object.
            //
            // So we first get the first index:
            // Note: if this raises an exception, this means that the table
            // is empty, so we can let the exception propagate to the caller.
            //
            entryoid=getNextOid(data);
            // XXX revisit: not exactly perfect:
            //     a specific row could be empty.. But we don't know
            //     how to make the difference! => tradeoff holes
            //     in tables can't be properly supported (all rows
            //     must have the same holes)
            //
            if(skipEntryVariable(entryoid,var,data,pduVersion)){
                var=getNextVarEntryId(entryoid,var,data,pduVersion);
            }
        }else{
            // So now there remain one last case, namely: some part of the
            // index is provided by the oid...
            // We build a possibly incomplete and invalid index from
            // the OID.
            // The piece of index provided should begin at pos+2
            //   oid[pos]   = id of the xxxEntry object,
            //   oid[pos+1] = id of the columnar object,
            //   oid[pos+2] ... oid[length-1] = piece of index.
            //
            // We get the next index following the provided index.
            // If this raises an exception, then it means that we have
            // reached the last index in the table, and we must then
            // try with the next columnar object.
            //
            // Bug fix 4269251
            // The SnmpIndex is defined to contain a valid oid:
            // this is not an SNMP requirement for the getNext request.
            // So we no more use the SnmpIndex but directly the SnmpOid.
            //
            try{
                entryoid=getNextOid(oid,pos+2,data);
                // If the variable must ne skipped, fall through...
                //
                // XXX revisit: not exactly perfect:
                //     a specific row could be empty.. But we don't know
                //     how to make the difference! => tradeoff holes
                //     in tables can't be properly supported (all rows
                //     must have the same holes)
                //
                if(skipEntryVariable(entryoid,var,data,pduVersion)){
                    throw new SnmpStatusException(SnmpStatusException.noSuchObject);
                }
            }catch(SnmpStatusException se){
                entryoid=getNextOid(data);
                var=getNextVarEntryId(entryoid,var,data,pduVersion);
            }
        }
        return findNextAccessibleOid(entryoid,
                varbind,
                oid,
                depth,
                handlers,
                checker,
                data,
                var);
    }

    // ---------------------------------------------------------------------
    //
    // Implements the method defined in SnmpMibNode.
    //
    // ---------------------------------------------------------------------
    @Override
    public void get(SnmpMibSubRequest req,int depth)
            throws SnmpStatusException{
        final boolean isnew=req.isNewEntry();
        final SnmpMibSubRequest r=req;
        // if the entry does not exists, then registers an error for
        // each varbind involved (nb: should not happen, the error
        // should have been registered earlier)
        if(isnew){
            SnmpVarBind var;
            for(Enumeration<SnmpVarBind> e=r.getElements();e.hasMoreElements();){
                var=e.nextElement();
                r.registerGetException(var,new SnmpStatusException(SnmpStatusException.noSuchInstance));
            }
        }
        final SnmpOid oid=r.getEntryOid();
        // SnmpIndex   index  = buildSnmpIndex(oid.longValue(false), 0);
        // get(req,index,depth+1);
        //
        get(req,oid,depth+1);
    }

    // ---------------------------------------------------------------------
    //
    // Implements the method defined in SnmpMibNode.
    //
    // ---------------------------------------------------------------------
    @Override
    public void set(SnmpMibSubRequest req,int depth)
            throws SnmpStatusException{
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpMibTable.class.getName(),
                    "set","Entering set");
        }
        final SnmpOid oid=req.getEntryOid();
        final int action=getRowAction(req,oid,depth+1);
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpMibTable.class.getName(),
                    "set","Calling set for "+req.getSize()+" varbinds");
        }
        set(req,oid,depth+1);
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpMibTable.class.getName(),
                    "set","Calling endRowAction");
        }
        endRowAction(req,oid,depth+1,action);
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpMibTable.class.getName(),
                    "set","RowAction finished");
        }
    }
    // ----------------------------------------------------------------------
    // PACKAGE METHODS
    // ----------------------------------------------------------------------

    // ---------------------------------------------------------------------
    //
    // Implements the method defined in SnmpMibNode.
    //
    // ---------------------------------------------------------------------
    @Override
    public void check(SnmpMibSubRequest req,int depth)
            throws SnmpStatusException{
        final SnmpOid oid=req.getEntryOid();
        final int action=getRowAction(req,oid,depth+1);
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpMibTable.class.getName(),
                    "check","Calling beginRowAction");
        }
        beginRowAction(req,oid,depth+1,action);
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpMibTable.class.getName(),
                    "check",
                    "Calling check for "+req.getSize()+" varbinds");
        }
        check(req,oid,depth+1);
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpMibTable.class.getName(),
                    "check","check finished");
        }
    }
    // ---------------------------------------------------------------------
    //
    // Register an exception when checking the RowStatus variable
    //
    // ---------------------------------------------------------------------

    protected int getRowAction(SnmpMibSubRequest req,SnmpOid rowOid,
                               int depth)
            throws SnmpStatusException{
        final boolean isnew=req.isNewEntry();
        final SnmpVarBind vb=req.getRowStatusVarBind();
        if(vb==null){
            if(isnew&&!hasRowStatus())
                return EnumRowStatus.createAndGo;
            else return EnumRowStatus.unspecified;
        }
        try{
            return mapRowStatus(rowOid,vb,req.getUserData());
        }catch(SnmpStatusException x){
            checkRowStatusFail(req,x.getStatus());
        }
        return EnumRowStatus.unspecified;
    }
    // ---------------------------------------------------------------------
    //
    // Register an exception when checking the RowStatus variable
    //
    // ---------------------------------------------------------------------

    public boolean hasRowStatus(){
        return false;
    }

    protected int mapRowStatus(SnmpOid rowOid,SnmpVarBind vbstatus,
                               Object userData)
            throws SnmpStatusException{
        final SnmpValue rsvalue=vbstatus.value;
        if(rsvalue instanceof SnmpInt)
            return ((SnmpInt)rsvalue).intValue();
        else
            throw new SnmpStatusException(
                    SnmpStatusException.snmpRspInconsistentValue);
    }

    static void checkRowStatusFail(SnmpMibSubRequest req,int errorStatus)
            throws SnmpStatusException{
        final SnmpVarBind statusvb=req.getRowStatusVarBind();
        final SnmpStatusException x=new SnmpStatusException(errorStatus);
        req.registerCheckException(statusvb,x);
    }

    protected synchronized void beginRowAction(SnmpMibSubRequest req,
                                               SnmpOid rowOid,int depth,int rowAction)
            throws SnmpStatusException{
        final boolean isnew=req.isNewEntry();
        final SnmpOid oid=rowOid;
        final int action=rowAction;
        switch(action){
            case EnumRowStatus.unspecified:
                if(isnew){
                    if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMibTable.class.getName(),
                                "beginRowAction","Failed to create row["+
                                        rowOid+"] : RowStatus = unspecified");
                    }
                    checkRowStatusFail(req,SnmpStatusException.snmpRspNoAccess);
                }
                break;
            case EnumRowStatus.createAndGo:
            case EnumRowStatus.createAndWait:
                if(isnew){
                    if(isCreationEnabled()){
                        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                    SnmpMibTable.class.getName(),
                                    "beginRowAction","Creating row["+rowOid+
                                            "] : RowStatus = createAndGo | createAndWait");
                        }
                        createNewEntry(req,oid,depth);
                    }else{
                        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                    SnmpMibTable.class.getName(),
                                    "beginRowAction","Can't create row["+rowOid+
                                            "] : RowStatus = createAndGo | createAndWait "+
                                            "but creation is disabled");
                        }
                        checkRowStatusFail(req,
                                SnmpStatusException.snmpRspNoAccess);
                    }
                }else{
                    if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMibTable.class.getName(),
                                "beginRowAction","Can't create row["+rowOid+
                                        "] : RowStatus = createAndGo | createAndWait "+
                                        "but row already exists");
                    }
                    checkRowStatusFail(req,
                            SnmpStatusException.snmpRspInconsistentValue);
                }
                break;
            case EnumRowStatus.destroy:
                if(isnew){
                    if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMibTable.class.getName(),
                                "beginRowAction",
                                "Warning: can't destroy row["+rowOid+
                                        "] : RowStatus = destroy but row does not exist");
                    }
                }else if(!isCreationEnabled()){
                    if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMibTable.class.getName(),
                                "beginRowAction",
                                "Can't destroy row["+rowOid+"] : "+
                                        "RowStatus = destroy but creation is disabled");
                    }
                    checkRowStatusFail(req,SnmpStatusException.snmpRspNoAccess);
                }
                checkRemoveTableRow(req,rowOid,depth);
                break;
            case EnumRowStatus.active:
            case EnumRowStatus.notInService:
                if(isnew){
                    if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMibTable.class.getName(),
                                "beginRowAction","Can't switch state of row["+
                                        rowOid+"] : specified RowStatus = active | "+
                                        "notInService but row does not exist");
                    }
                    checkRowStatusFail(req,
                            SnmpStatusException.snmpRspInconsistentValue);
                }
                checkRowStatusChange(req,rowOid,depth,action);
                break;
            case EnumRowStatus.notReady:
            default:
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                            SnmpMibTable.class.getName(),
                            "beginRowAction","Invalid RowStatus value for row["+
                                    rowOid+"] : specified RowStatus = "+action);
                }
                checkRowStatusFail(req,
                        SnmpStatusException.snmpRspInconsistentValue);
        }
    }

    public abstract void createNewEntry(SnmpMibSubRequest req,SnmpOid rowOid,
                                        int depth)
            throws SnmpStatusException;
    // ----------------------------------------------------------------------
    // PRIVATE METHODS
    // ----------------------------------------------------------------------

    public boolean isCreationEnabled(){
        return creationEnabled;
    }

    public void setCreationEnabled(boolean remoteCreationFlag){
        creationEnabled=remoteCreationFlag;
    }

    protected void checkRowStatusChange(SnmpMibSubRequest req,
                                        SnmpOid rowOid,int depth,
                                        int newStatus)
            throws SnmpStatusException{
    }

    protected void checkRemoveTableRow(SnmpMibSubRequest req,SnmpOid rowOid,
                                       int depth)
            throws SnmpStatusException{
    }

    abstract protected void check(SnmpMibSubRequest req,
                                  SnmpOid rowOid,int depth)
            throws SnmpStatusException;

    protected void endRowAction(SnmpMibSubRequest req,SnmpOid rowOid,
                                int depth,int rowAction)
            throws SnmpStatusException{
        final boolean isnew=req.isNewEntry();
        final SnmpOid oid=rowOid;
        final int action=rowAction;
        final Object data=req.getUserData();
        SnmpValue value=null;
        switch(action){
            case EnumRowStatus.unspecified:
                break;
            case EnumRowStatus.createAndGo:
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                            SnmpMibTable.class.getName(),
                            "endRowAction","Setting RowStatus to 'active' "+
                                    "for row["+rowOid+"] : requested RowStatus = "+
                                    "createAndGo");
                }
                value=setRowStatus(oid,EnumRowStatus.active,data);
                break;
            case EnumRowStatus.createAndWait:
                if(isRowReady(oid,data)){
                    if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMibTable.class.getName(),
                                "endRowAction",
                                "Setting RowStatus to 'notInService' for row["+
                                        rowOid+"] : requested RowStatus = createAndWait");
                    }
                    value=setRowStatus(oid,EnumRowStatus.notInService,data);
                }else{
                    if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMibTable.class.getName(),
                                "endRowAction","Setting RowStatus to 'notReady' "+
                                        "for row["+rowOid+"] : requested RowStatus = "+
                                        "createAndWait");
                    }
                    value=setRowStatus(oid,EnumRowStatus.notReady,data);
                }
                break;
            case EnumRowStatus.destroy:
                if(isnew){
                    if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMibTable.class.getName(),
                                "endRowAction",
                                "Warning: requested RowStatus = destroy, "+
                                        "but row["+rowOid+"] does not exist");
                    }
                }else{
                    if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMibTable.class.getName(),
                                "endRowAction","Destroying row["+rowOid+
                                        "] : requested RowStatus = destroy");
                    }
                }
                removeTableRow(req,oid,depth);
                break;
            case EnumRowStatus.active:
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                            SnmpMibTable.class.getName(),
                            "endRowAction",
                            "Setting RowStatus to 'active' for row["+
                                    rowOid+"] : requested RowStatus = active");
                }
                value=setRowStatus(oid,EnumRowStatus.active,data);
                break;
            case EnumRowStatus.notInService:
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                            SnmpMibTable.class.getName(),
                            "endRowAction",
                            "Setting RowStatus to 'notInService' for row["+
                                    rowOid+"] : requested RowStatus = notInService");
                }
                value=setRowStatus(oid,EnumRowStatus.notInService,data);
                break;
            case EnumRowStatus.notReady:
            default:
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                            SnmpMibTable.class.getName(),
                            "endRowAction","Invalid RowStatus value for row["+
                                    rowOid+"] : specified RowStatus = "+action);
                }
                setRowStatusFail(req,
                        SnmpStatusException.snmpRspInconsistentValue);
        }
        if(value!=null){
            final SnmpVarBind vb=req.getRowStatusVarBind();
            if(vb!=null) vb.value=value;
        }
    }

    protected SnmpValue setRowStatus(SnmpOid rowOid,int newStatus,
                                     Object userData)
            throws SnmpStatusException{
        return null;
    }

    protected boolean isRowReady(SnmpOid rowOid,Object userData)
            throws SnmpStatusException{
        return true;
    }

    protected void removeTableRow(SnmpMibSubRequest req,SnmpOid rowOid,
                                  int depth)
            throws SnmpStatusException{
        removeEntry(rowOid);
    }

    public void removeEntry(SnmpOid rowOid)
            throws SnmpStatusException{
        int pos=findObject(rowOid);
        if(pos==-1)
            return;
        removeEntry(pos,null);
    }

    public synchronized void removeEntry(int pos,Object entry)
            throws SnmpStatusException{
        if(pos==-1)
            return;
        if(pos>=size) return;
        Object obj=entry;
        if(entries!=null&&entries.size()>pos){
            obj=entries.elementAt(pos);
            entries.removeElementAt(pos);
        }
        ObjectName name=null;
        if(entrynames!=null&&entrynames.size()>pos){
            name=entrynames.elementAt(pos);
            entrynames.removeElementAt(pos);
        }
        final SnmpOid rowOid=tableoids[pos];
        removeOid(pos);
        size--;
        if(obj==null) obj=entry;
        if(factory!=null)
            factory.removeEntryCb(pos,rowOid,name,obj,this);
        sendNotification(SnmpTableEntryNotification.SNMP_ENTRY_REMOVED,
                (new Date()).getTime(),obj,name);
    }

    private void sendNotification(String type,long timeStamp,
                                  Object entry,ObjectName name){
        synchronized(this){
            sequenceNumber=sequenceNumber+1;
        }
        SnmpTableEntryNotification notif=
                new SnmpTableEntryNotification(type,this,sequenceNumber,
                        timeStamp,entry,name);
        this.sendNotification(notif);
    }

    private synchronized void sendNotification(Notification notification){
        // loop on listener
        //
        for(Enumeration<NotificationListener> k=handbackTable.keys();
            k.hasMoreElements();){
            NotificationListener listener=k.nextElement();
            // Get the associated handback list and the associated filter list
            //
            Vector<?> handbackList=handbackTable.get(listener);
            Vector<NotificationFilter> filterList=
                    filterTable.get(listener);
            // loop on handback
            //
            Enumeration<NotificationFilter> f=filterList.elements();
            for(Enumeration<?> h=handbackList.elements();
                h.hasMoreElements();){
                Object handback=h.nextElement();
                NotificationFilter filter=f.nextElement();
                if((filter==null)||
                        (filter.isNotificationEnabled(notification))){
                    listener.handleNotification(notification,handback);
                }
            }
        }
    }

    private void removeOid(int pos){
        if(pos>=tablecount) return;
        if(pos<0) return;
        final int l1=--tablecount-pos;
        tableoids[pos]=null;
        if(l1>0)
            System.arraycopy(tableoids,pos+1,tableoids,pos,l1);
        tableoids[tablecount]=null;
    }

    private int findObject(SnmpOid oid){
        int low=0;
        int max=size-1;
        SnmpOid pos;
        int comp;
        int curr=low+(max-low)/2;
        //System.out.println("Try to retrieve: " + oid.toString());
        while(low<=max){
            // XX pos = (SnmpOid) oids.elementAt(curr);
            pos=tableoids[curr];
            //System.out.println("Compare with" + pos.toString());
            // never know ...we might find something ...
            //
            comp=oid.compareTo(pos);
            if(comp==0)
                return curr;
            if(oid.equals(pos)==true){
                return curr;
            }
            if(comp>0){
                low=curr+1;
            }else{
                max=curr-1;
            }
            curr=low+(max-low)/2;
        }
        return -1;
    }

    static void setRowStatusFail(SnmpMibSubRequest req,int errorStatus)
            throws SnmpStatusException{
        final SnmpVarBind statusvb=req.getRowStatusVarBind();
        final SnmpStatusException x=new SnmpStatusException(errorStatus);
        req.registerSetException(statusvb,x);
    }

    abstract protected void set(SnmpMibSubRequest req,
                                SnmpOid rowOid,int depth)
            throws SnmpStatusException;

    abstract protected void get(SnmpMibSubRequest req,
                                SnmpOid rowOid,int depth)
            throws SnmpStatusException;

    protected boolean contains(SnmpOid oid,Object userData){
        return (findObject(oid)>-1);
    }

    private long[] findNextAccessibleOid(SnmpOid entryoid,
                                         SnmpVarBind varbind,long[] oid,
                                         int depth,SnmpRequestTree handlers,
                                         AcmChecker checker,Object data,
                                         long var)
            throws SnmpStatusException{
        final int pduVersion=handlers.getRequestPduVersion();
        // Loop on each var (column)
        while(true){
            // This should not happen. If it happens, (bug, or customized
            // methods returning garbage instead of raising an exception),
            // it probably means that there is nothing to return anyway.
            // So we throw the exception.
            // => will skip to next node in the MIB tree.
            //
            if(entryoid==null||var==-1){
                throw new SnmpStatusException(SnmpStatusException.noSuchObject);
            }
            // So here we know both the row (entryoid) and the column (var)
            //
            try{
                // Raising an exception here will make the catch() clause
                // switch to the next variable. If `var' is not readable
                // for this specific entry, it is not readable for any
                // other entry => skip to next column.
                //
                if(!isReadableEntryId(entryoid,var,data)){
                    throw new SnmpStatusException(SnmpStatusException.noSuchObject);
                }
                // Prepare the result and the ACM checker.
                //
                final long[] etable=entryoid.longValue(false);
                final int elength=etable.length;
                final long[] result=new long[depth+2+elength];
                result[0]=-1; // Bug detector!
                // Copy the entryOid at the end of `result'
                //
                System.arraycopy(etable,0,result,
                        depth+2,elength);
                // Set the node Id and var Id in result.
                //
                result[depth]=nodeId;
                result[depth+1]=var;
                // Append nodeId.varId.<rowOid> to ACM checker.
                //
                checker.add(depth,result,depth,elength+2);
                // No we're going to ACM check our OID.
                try{
                    checker.checkCurrentOid();
                    // No exception thrown by checker => this is all OK!
                    // we have it: register the handler and return the
                    // result.
                    //
                    handlers.add(this,depth,entryoid,varbind,false);
                    return result;
                }catch(SnmpStatusException e){
                    // Skip to the next entry. If an exception is
                    // thrown, will be catch by enclosing catch
                    // and a skip is done to the next var.
                    //
                    entryoid=getNextOid(entryoid,data);
                }finally{
                    // Clean the checker.
                    //
                    checker.remove(depth,elength+2);
                }
            }catch(SnmpStatusException e){
                // Catching an exception here means we have to skip to the
                // next column.
                //
                // Back to the first row.
                entryoid=getNextOid(data);
                // Find out the next column.
                //
                var=getNextVarEntryId(entryoid,var,data,pduVersion);
            }
            // This should not happen. If it happens, (bug, or customized
            // methods returning garbage instead of raising an exception),
            // it probably means that there is nothing to return anyway.
            // No need to continue, we throw an exception.
            // => will skip to next node in the MIB tree.
            //
            if(entryoid==null||var==-1){
                throw new SnmpStatusException(SnmpStatusException.noSuchObject);
            }
        }
    }

    final void validateOid(long[] oid,int pos) throws SnmpStatusException{
        final int length=oid.length;
        // Control the length of the oid
        //
        if(pos+2>=length){
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        }
        // Check that the entry identifier is specified
        //
        if(oid[pos]!=nodeId){
            throw new SnmpStatusException(SnmpStatusException.noSuchObject);
        }
    }
}
