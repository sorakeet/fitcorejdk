/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.naming.pcosnaming;

import com.sun.corba.se.impl.logging.NamingSystemException;
import com.sun.corba.se.impl.naming.cosnaming.NamingContextDataStore;
import com.sun.corba.se.impl.naming.cosnaming.NamingUtils;
import com.sun.corba.se.impl.naming.namingutil.INSURLHandler;
import com.sun.corba.se.impl.orbutil.ORBConstants;
import com.sun.corba.se.spi.logging.CORBALogDomains;
import com.sun.corba.se.spi.orb.ORB;
import org.omg.CORBA.*;
import org.omg.CORBA.Object;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextExtPackage.InvalidAddress;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.ServantRetentionPolicyValue;

import java.io.Serializable;
import java.util.Hashtable;

public class NamingContextImpl
        extends NamingContextExtPOA
        implements NamingContextDataStore, Serializable{
    private static POA biPOA=null;
    // Debugging aids.
    private static boolean debug;
    // The ObjectKey will be in the format NC<Index> which uniquely identifies
    // The NamingContext internaly
    private final String objKey;
    // Hash table contains all the entries in the NamingContexts. The
    // CORBA.Object references will be stored in the form of IOR strings
    // and the Child Naming Contexts will have it's key as the entry in the
    // table. This table is written into File everytime an update is made
    // on this context.
    private final Hashtable theHashtable=new Hashtable();
    // The ORB is required to do string_to_object() operations
    // All the references are stored in the files in the form of IOR strings
    private transient ORB orb;
    // The NameServiceHandle is required to get the ObjectId from the
    // NamingContext's references. These references are created using
    // POA in the NameService.
    private transient NameService theNameServiceHandle;
    // ServantManager is the single point of contact to Read, Write and
    // Update the NamingContextFile
    private transient ServantManagerImpl theServantManagerImplHandle;
    // All the INS (Interoperable Naming Service) methods are defined in this class
    // All the calls to INS will be delegated to this class.
    private transient com.sun.corba.se.impl.naming.cosnaming.InterOperableNamingImpl insImpl;
    private transient NamingSystemException readWrapper;
    private transient NamingSystemException updateWrapper;

    public NamingContextImpl(ORB orb,String objKey,
                             NameService theNameService,ServantManagerImpl theServantManagerImpl)
            throws Exception{
        super();
        this.orb=orb;
        readWrapper=NamingSystemException.get(orb,
                CORBALogDomains.NAMING_READ);
        updateWrapper=NamingSystemException.get(orb,
                CORBALogDomains.NAMING_UPDATE);
        debug=true; // orb.namingDebugFlag ;
        this.objKey=objKey;
        theNameServiceHandle=theNameService;
        theServantManagerImplHandle=theServantManagerImpl;
        insImpl=
                new com.sun.corba.se.impl.naming.cosnaming.InterOperableNamingImpl();
    }

    public void setRootNameService(NameService theNameService){
        theNameServiceHandle=theNameService;
    }

    public void setORB(ORB theOrb){
        orb=theOrb;
    }

    public void setServantManagerImpl(
            ServantManagerImpl theServantManagerImpl){
        theServantManagerImplHandle=theServantManagerImpl;
    }

    public void bind(NameComponent[] n,Object obj)
            throws NotFound,
            CannotProceed,
            InvalidName,
            AlreadyBound{
        if(obj==null){
            throw updateWrapper.objectIsNull();
        }
        if(debug)
            dprint("bind "+nameToString(n)+" to "+obj);
        // doBind implements all four flavors of binding
        NamingContextDataStore impl=(NamingContextDataStore)this;
        doBind(impl,n,obj,false,BindingType.nobject);
    }

    public void bind_context(NameComponent[] n,NamingContext nc)
            throws NotFound,
            CannotProceed,
            InvalidName,
            AlreadyBound{
        if(nc==null){
            throw updateWrapper.objectIsNull();
        }
        // doBind implements all four flavors of binding
        NamingContextDataStore impl=(NamingContextDataStore)this;
        doBind(impl,n,nc,false,BindingType.ncontext);
    }

    public void rebind(NameComponent[] n,Object obj)
            throws NotFound,
            CannotProceed,
            InvalidName{
        if(obj==null){
            throw updateWrapper.objectIsNull();
        }
        try{
            if(debug)
                dprint("rebind "+nameToString(n)+" to "+obj);
            // doBind implements all four flavors of binding
            NamingContextDataStore impl=(NamingContextDataStore)this;
            doBind(impl,n,obj,true,BindingType.nobject);
        }catch(AlreadyBound ex){
            // This should not happen
            throw updateWrapper.namingCtxRebindAlreadyBound(ex);
        }
    }

    public void rebind_context(NameComponent[] n,NamingContext nc)
            throws NotFound,
            CannotProceed,
            InvalidName{
        try{
            if(debug)
                dprint("rebind_context "+nameToString(n)+" to "+nc);
            // doBind implements all four flavors of binding
            NamingContextDataStore impl=(NamingContextDataStore)this;
            doBind(impl,n,nc,true,BindingType.ncontext);
        }catch(AlreadyBound ex){
            // This should not happen
            throw updateWrapper.namingCtxRebindAlreadyBound(ex);
        }
    }

    public Object resolve(NameComponent[] n)
            throws NotFound,
            CannotProceed,
            InvalidName{
        if(debug)
            dprint("resolve "+nameToString(n));
        // doResolve actually resolves
        NamingContextDataStore impl=(NamingContextDataStore)this;
        return doResolve(impl,n);
    }

    public void unbind(NameComponent[] n)
            throws NotFound,
            CannotProceed,
            InvalidName{
        if(debug)
            dprint("unbind "+nameToString(n));
        // doUnbind actually unbinds
        NamingContextDataStore impl=(NamingContextDataStore)this;
        doUnbind(impl,n);
    }

    public void list(int how_many,BindingListHolder bl,BindingIteratorHolder bi){
        if(debug)
            dprint("list("+how_many+")");
        // List actually generates the list
        NamingContextDataStore impl=(NamingContextDataStore)this;
        synchronized(impl){
            impl.List(how_many,bl,bi);
        }
        if(debug&&bl.value!=null)
            dprint("list("+how_many+") -> bindings["+bl.value.length+
                    "] + iterator: "+bi.value);
    }

    public synchronized NamingContext new_context(){
        // Create actually creates a new naming context
        if(debug)
            dprint("new_context()");
        NamingContextDataStore impl=(NamingContextDataStore)this;
        synchronized(impl){
            return impl.NewContext();
        }
    }

    public NamingContext bind_new_context(NameComponent[] n)
            throws NotFound,
            AlreadyBound,
            CannotProceed,
            InvalidName{
        NamingContext nc=null;
        NamingContext rnc=null;
        try{
            if(debug)
                dprint("bind_new_context "+nameToString(n));
            // The obvious solution:
            nc=this.new_context();
            this.bind_context(n,nc);
            rnc=nc;
            nc=null;
        }finally{
            try{
                if(nc!=null)
                    nc.destroy();
            }catch(NotEmpty e){
            }
        }
        return rnc;
    }

    public void destroy()
            throws NotEmpty{
        if(debug)
            dprint("destroy ");
        NamingContextDataStore impl=(NamingContextDataStore)this;
        synchronized(impl){
            if(impl.IsEmpty()==true)
                // The context is empty so it can be destroyed
                impl.Destroy();
            else
                // This context is not empty!
                throw new NotEmpty();
        }
    }

    public static void doUnbind(NamingContextDataStore impl,
                                NameComponent[] n)
            throws NotFound,
            CannotProceed,
            InvalidName{
        // Name valid?
        if(n.length<1)
            throw new InvalidName();
        // Unbind here?
        if(n.length==1){
            // The identifier must be set
            if((n[0].id.length()==0)&&(n[0].kind.length()==0))
                throw new InvalidName();
            Object objRef=null;
            synchronized(impl){
                // Yes: unbind in this context
                objRef=impl.Unbind(n[0]);
            }
            if(objRef==null)
                // It was not bound
                throw new NotFound(NotFoundReason.missing_node,n);
            // Done
            return;
        }else{
            // No: unbind in a different context
            // Resolve first  - must be resolveable
            NamingContext context=resolveFirstAsContext(impl,n);
            // Compute tail
            NameComponent[] tail=new NameComponent[n.length-1];
            System.arraycopy(n,1,tail,0,n.length-1);
            // Propagate unbind to this context
            context.unbind(tail);
        }
    }

    public static Object doResolve(NamingContextDataStore impl,
                                   NameComponent[] n)
            throws NotFound,
            CannotProceed,
            InvalidName{
        Object obj=null;
        BindingTypeHolder bth=new BindingTypeHolder();
        // Length must be greater than 0
        if(n.length<1)
            throw new InvalidName();
        // The identifier must be set
        if(n.length==1){
            synchronized(impl){
                // Resolve first level in this context
                obj=impl.Resolve(n[0],bth);
            }
            if(obj==null){
                // Object was not found
                throw new NotFound(NotFoundReason.missing_node,n);
            }
            return obj;
        }else{
            // n.length > 1
            if((n[1].id.length()==0)&&(n[1].kind.length()==0))
                throw new InvalidName();
            NamingContext context=resolveFirstAsContext(impl,n);
            // Compute restOfName = name[1..length]
            NameComponent[] tail=new NameComponent[n.length-1];
            System.arraycopy(n,1,tail,0,n.length-1);
            // Resolve rest of name in context
            return context.resolve(tail);
        }
    }

    private void doBind(NamingContextDataStore impl,
                        NameComponent[] n,
                        Object obj,
                        boolean rebind,
                        BindingType bt)
            throws NotFound,
            CannotProceed,
            InvalidName,
            AlreadyBound{
        // Valid name?
        if(n.length<1)
            throw new InvalidName();
        // At bottom level?
        if(n.length==1){
            // The identifier must be set
            if((n[0].id.length()==0)&&(n[0].kind.length()==0))
                throw new InvalidName();
            // Ensure synchronization of backend
            synchronized(impl){
                // Yes: bind object in this context under the name
                BindingTypeHolder bth=new BindingTypeHolder();
                if(rebind){
                    Object objRef=impl.Resolve(n[0],bth);
                    if(objRef!=null){
                        // Refer Naming Service Doc:00-11-01 section 2.2.3.4
                        // If there is an object already bound with the name
                        // and the binding type is not ncontext a NotFound
                        // Exception with a reason of not a context has to be
                        // raised.
                        // Fix for bug Id: 4384628
                        if(bth.value.value()==BindingType.nobject.value()){
                            if(bt.value()==BindingType.ncontext.value()){
                                throw new NotFound(NotFoundReason.not_context,n);
                            }
                        }else{
                            // Previously a Context was bound and now trying to
                            // bind Object. It is invalid.
                            if(bt.value()==BindingType.nobject.value()){
                                throw new NotFound(NotFoundReason.not_object,n);
                            }
                        }
                        impl.Unbind(n[0]);
                    }
                }else{
                    if(impl.Resolve(n[0],bth)!=null)
                        throw new AlreadyBound();
                }
                // Now there are no other bindings under this name
                impl.Bind(n[0],obj,bt);
            }
        }else{
            // No: bind in a different context
            NamingContext context=resolveFirstAsContext(impl,n);
            // Compute tail
            NameComponent[] tail=new NameComponent[n.length-1];
            System.arraycopy(n,1,tail,0,n.length-1);
            // How should we propagate the bind
            switch(bt.value()){
                case BindingType._nobject:{
                    // Bind as object
                    if(rebind)
                        context.rebind(tail,obj);
                    else
                        context.bind(tail,obj);
                }
                break;
                case BindingType._ncontext:{
                    // Narrow to a naming context using Java casts. It must work.
                    NamingContext objContext=(NamingContext)obj;
                    // Bind as context
                    if(rebind)
                        context.rebind_context(tail,objContext);
                    else
                        context.bind_context(tail,objContext);
                }
                break;
                default:
                    // This should not happen
                    throw updateWrapper.namingCtxBadBindingtype();
            }
        }
    }

    protected static NamingContext resolveFirstAsContext(NamingContextDataStore impl,
                                                         NameComponent[] n)
            throws NotFound{
        Object topRef=null;
        BindingTypeHolder bth=new BindingTypeHolder();
        NamingContext context=null;
        synchronized(impl){
            // Resolve first  - must be resolveable
            topRef=impl.Resolve(n[0],bth);
            if(topRef==null){
                // It was not bound
                throw new NotFound(NotFoundReason.missing_node,n);
            }
        }
        // Was it bound as a context?
        if(bth.value!=BindingType.ncontext){
            // It was not a context
            throw new NotFound(NotFoundReason.not_context,n);
        }
        // Narrow to a naming context
        try{
            context=NamingContextHelper.narrow(topRef);
        }catch(BAD_PARAM ex){
            // It was not a context
            throw new NotFound(NotFoundReason.not_context,n);
        }
        // Hmm. must be ok
        return context;
    }

    public static String nameToString(NameComponent[] name){
        StringBuffer s=new StringBuffer("{");
        if(name!=null||name.length>0){
            for(int i=0;i<name.length;i++){
                if(i>0)
                    s.append(",");
                s.append("[").
                        append(name[i].id).
                        append(",").
                        append(name[i].kind).
                        append("]");
            }
        }
        s.append("}");
        return s.toString();
    }

    private static void dprint(String msg){
        NamingUtils.dprint("NamingContextImpl("+
                Thread.currentThread().getName()+" at "+
                System.currentTimeMillis()+
                " ems): "+msg);
    }

    public void Bind(NameComponent n,Object obj,BindingType bt){
        if(obj==null){
            // Raise a Valid Exception and Return
            return;
        }
        InternalBindingKey key=new InternalBindingKey(n);
        InternalBindingValue value;
        try{
            if(bt.value()==BindingType._nobject){
                // If the BindingType is an ObjectRef then Stringify this ref and
                // Store it in InternalBindingValue instance. This is required
                // because the Object References has to be stored in file
                value=new InternalBindingValue(bt,orb.object_to_string(obj));
                value.setObjectRef(obj);
            }else{
                // If the BindingType is a NamingContext then get it's object key
                // from the NameService and store it in the Internal Binding Value instance
                String theNCKey=theNameServiceHandle.getObjectKey(obj);
                value=new InternalBindingValue(bt,theNCKey);
                value.setObjectRef(obj);
            }
            InternalBindingValue oldValue=
                    (InternalBindingValue)this.theHashtable.put(key,value);
            if(oldValue!=null){
                // There was an entry with this name in the Hashtable and hence throw CTX_ALREADY_BOUND
                // exception
                throw updateWrapper.namingCtxRebindAlreadyBound();
            }else{
                try{
                    // Everything went smooth so update the NamingContext file with the
                    // latest Hashtable image
                    theServantManagerImplHandle.updateContext(objKey,this);
                }catch(Exception e){
                    // Something went wrong while updating the context
                    // so speak the error
                    throw updateWrapper.bindUpdateContextFailed(e);
                }
            }
        }catch(Exception e){
            // Something went wrong while Binding the Object Reference
            // Speak the error again.
            throw updateWrapper.bindFailure(e);
        }
    }

    public Object Resolve(NameComponent n,BindingTypeHolder bth)
            throws SystemException{
        if((n.id.length()==0)&&(n.kind.length()==0)){
            // If the NameComponent list has no entry then it means the current
            // context was requested
            bth.value=BindingType.ncontext;
            return theNameServiceHandle.getObjectReferenceFromKey(
                    this.objKey);
        }
        InternalBindingKey key=new InternalBindingKey(n);
        InternalBindingValue value=
                (InternalBindingValue)this.theHashtable.get(key);
        if(value==null){
            // No entry was found for the given name and hence return NULL
            // NamingContextDataStore throws appropriate exception if
            // required.
            return null;
        }
        Object theObjectFromStringifiedReference=null;
        bth.value=value.theBindingType;
        try{
            // Check whether the entry found in the Hashtable starts with NC
            // Which means it's a name context. So get the NamingContext reference
            // from ServantManager, which would either return from the cache or
            // read it from the File.
            if(value.strObjectRef.startsWith("NC")){
                bth.value=BindingType.ncontext;
                return theNameServiceHandle.getObjectReferenceFromKey(value.strObjectRef);
            }else{
                // Else, It is a Object Reference. Check whether Object Reference
                // can be obtained directly, If not then convert the stringified
                // reference to object and return.
                theObjectFromStringifiedReference=value.getObjectRef();
                if(theObjectFromStringifiedReference==null){
                    try{
                        theObjectFromStringifiedReference=
                                orb.string_to_object(value.strObjectRef);
                        value.setObjectRef(theObjectFromStringifiedReference);
                    }catch(Exception e){
                        throw readWrapper.resolveConversionFailure(
                                CompletionStatus.COMPLETED_MAYBE,e);
                    }
                }
            }
        }catch(Exception e){
            throw readWrapper.resolveFailure(
                    CompletionStatus.COMPLETED_MAYBE,e);
        }
        return theObjectFromStringifiedReference;
    }

    public Object Unbind(NameComponent n) throws SystemException{
        try{
            InternalBindingKey key=new InternalBindingKey(n);
            InternalBindingValue value=null;
            try{
                value=(InternalBindingValue)this.theHashtable.remove(key);
            }catch(Exception e){
                // Ignore the exception in Hashtable.remove
            }
            theServantManagerImplHandle.updateContext(objKey,this);
            if(value==null){
                return null;
            }
            if(value.strObjectRef.startsWith("NC")){
                theServantManagerImplHandle.readInContext(value.strObjectRef);
                Object theObjectFromStringfiedReference=
                        theNameServiceHandle.getObjectReferenceFromKey(value.strObjectRef);
                return theObjectFromStringfiedReference;
            }else{
                Object theObjectFromStringifiedReference=value.getObjectRef();
                if(theObjectFromStringifiedReference==null){
                    theObjectFromStringifiedReference=
                            orb.string_to_object(value.strObjectRef);
                }
                return theObjectFromStringifiedReference;
            }
        }catch(Exception e){
            throw updateWrapper.unbindFailure(CompletionStatus.COMPLETED_MAYBE,e);
        }
    }

    public void List(int how_many,BindingListHolder bl,
                     BindingIteratorHolder bi) throws SystemException{
        if(biPOA==null){
            createbiPOA();
        }
        try{
            PersistentBindingIterator bindingIterator=
                    new PersistentBindingIterator(this.orb,
                            (Hashtable)this.theHashtable.clone(),biPOA);
            // Have it set the binding list
            bindingIterator.list(how_many,bl);
            byte[] objectId=biPOA.activate_object(bindingIterator);
            Object obj=biPOA.id_to_reference(objectId);
            // Get the object reference for the binding iterator servant
            BindingIterator bindingRef=
                    BindingIteratorHelper.narrow(obj);
            bi.value=bindingRef;
        }catch(SystemException e){
            throw e;
        }catch(Exception e){
            throw readWrapper.transNcListGotExc(e);
        }
    }

    private synchronized void createbiPOA(){
        if(biPOA!=null){
            return;
        }
        try{
            POA rootPOA=(POA)orb.resolve_initial_references(
                    ORBConstants.ROOT_POA_NAME);
            rootPOA.the_POAManager().activate();
            int i=0;
            Policy[] poaPolicy=new Policy[3];
            poaPolicy[i++]=rootPOA.create_lifespan_policy(
                    LifespanPolicyValue.TRANSIENT);
            poaPolicy[i++]=rootPOA.create_id_assignment_policy(
                    IdAssignmentPolicyValue.SYSTEM_ID);
            poaPolicy[i++]=rootPOA.create_servant_retention_policy(
                    ServantRetentionPolicyValue.RETAIN);
            biPOA=rootPOA.create_POA("BindingIteratorPOA",null,poaPolicy);
            biPOA.the_POAManager().activate();
        }catch(Exception e){
            throw readWrapper.namingCtxBindingIteratorCreate(e);
        }
    }

    public NamingContext NewContext() throws SystemException{
        try{
            return theNameServiceHandle.NewContext();
        }catch(SystemException e){
            throw e;
        }catch(Exception e){
            throw updateWrapper.transNcNewctxGotExc(e);
        }
    }

    public void Destroy() throws SystemException{
        // XXX note that orb.disconnect is illegal here, since the
        // POA is used.  However, there may be some associated state
        // that needs to be cleaned up in ServerManagerImpl which we will
        // look into further at another time.
        /**
         // XXX This needs to be replaced by cleaning up the
         // file that backs up the naming context.  No explicit
         // action is necessary at the POA level, since this is
         // created with the non-retain policy.
         /**
         try { orb.disconnect(
         theNameServiceHandle.getObjectReferenceFromKey( this.objKey ) );
         } catch( org.omg.CORBA.SystemException e ) {
         throw e;
         } catch( Exception e ) {
         throw updateWrapper.transNcDestroyGotEx( e ) ;
         }
         */
    }

    public boolean IsEmpty(){
        return this.theHashtable.isEmpty();
    }

    public POA getNSPOA(){
        return theNameServiceHandle.getNSPOA();
    }

    public String to_string(NameComponent[] n)
            throws InvalidName{
        // Name valid?
        if((n==null)||(n.length==0)){
            throw new InvalidName();
        }
        String theStringifiedName=getINSImpl().convertToString(n);
        if(theStringifiedName==null){
            throw new InvalidName();
        }
        return theStringifiedName;
    }

    com.sun.corba.se.impl.naming.cosnaming.InterOperableNamingImpl getINSImpl(){
        if(insImpl==null){
            // insImpl will be null if the NamingContext graph is rebuilt from
            // the persistence store.
            insImpl=
                    new com.sun.corba.se.impl.naming.cosnaming.InterOperableNamingImpl();
        }
        return insImpl;
    }

    public NameComponent[] to_name(String sn)
            throws InvalidName{
        // Name valid?
        if((sn==null)||(sn.length()==0)){
            throw new InvalidName();
        }
        NameComponent[] theNameComponents=
                getINSImpl().convertToNameComponent(sn);
        if((theNameComponents==null)||(theNameComponents.length==0)){
            throw new InvalidName();
        }
        for(int i=0;i<theNameComponents.length;i++){
            // If there is a name component whose id and kind null or
            // zero length string, then an invalid name exception needs to be
            // raised.
            if(((theNameComponents[i].id==null)
                    ||(theNameComponents[i].id.length()==0))
                    &&((theNameComponents[i].kind==null)
                    ||(theNameComponents[i].kind.length()==0))){
                throw new InvalidName();
            }
        }
        return theNameComponents;
    }

    public String to_url(String addr,String sn)
            throws InvalidAddress,
            InvalidName{
        // Name valid?
        if((sn==null)||(sn.length()==0)){
            throw new InvalidName();
        }
        if(addr==null){
            throw new InvalidAddress();
        }
        String urlBasedAddress=null;
        try{
            urlBasedAddress=getINSImpl().createURLBasedAddress(addr,sn);
        }catch(Exception e){
            urlBasedAddress=null;
        }
        // Extra check to see that corba name url created is valid as per
        // INS spec grammer.
        try{
            INSURLHandler.getINSURLHandler().parseURL(urlBasedAddress);
        }catch(BAD_PARAM e){
            throw new
                    InvalidAddress();
        }
        return urlBasedAddress;
    }

    public Object resolve_str(String sn)
            throws NotFound,
            CannotProceed,
            InvalidName{
        Object theObject=null;
        // Name valid?
        if((sn==null)||(sn.length()==0)){
            throw new InvalidName();
        }
        NameComponent[] theNameComponents=
                getINSImpl().convertToNameComponent(sn);
        if((theNameComponents==null)||(theNameComponents.length==0)){
            throw new InvalidName();
        }
        theObject=resolve(theNameComponents);
        return theObject;
    }

    public void printSize(){
        System.out.println("Hashtable Size = "+theHashtable.size());
        java.util.Enumeration e=theHashtable.keys();
        for(;e.hasMoreElements();){
            InternalBindingValue thevalue=
                    (InternalBindingValue)this.theHashtable.get(e.nextElement());
            if(thevalue!=null){
                System.out.println("value = "+thevalue.strObjectRef);
            }
        }
    }
}
