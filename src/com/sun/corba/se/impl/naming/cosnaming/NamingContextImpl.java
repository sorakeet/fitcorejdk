/**
 * Copyright (c) 1996, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.naming.cosnaming;
// Imports for Logging

import com.sun.corba.se.impl.logging.NamingSystemException;
import com.sun.corba.se.impl.naming.namingutil.INSURLHandler;
import com.sun.corba.se.impl.orbutil.LogKeywords;
import com.sun.corba.se.spi.logging.CORBALogDomains;
import com.sun.corba.se.spi.orb.ORB;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.Object;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextExtPackage.InvalidAddress;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

import java.util.logging.Level;
import java.util.logging.Logger;
// Import general CORBA classes
// Import org.omg.CosNaming classes

public abstract class NamingContextImpl
        extends NamingContextExtPOA
        implements NamingContextDataStore{
    // Debugging aids.
    public static final boolean debug=false;
    private static NamingSystemException staticWrapper=
            NamingSystemException.get(CORBALogDomains.NAMING_UPDATE);
    protected POA nsPOA;
    transient protected ORB orb;
    private Logger readLogger, updateLogger, lifecycleLogger;
    private NamingSystemException wrapper;
    // The grammer for Parsing and Building Interoperable Stringified Names
    // are implemented in this class
    private InterOperableNamingImpl insImpl;

    public NamingContextImpl(ORB orb,POA poa) throws Exception{
        super();
        this.orb=orb;
        wrapper=NamingSystemException.get(orb,
                CORBALogDomains.NAMING_UPDATE);
        insImpl=new InterOperableNamingImpl();
        this.nsPOA=poa;
        readLogger=orb.getLogger(CORBALogDomains.NAMING_READ);
        updateLogger=orb.getLogger(CORBALogDomains.NAMING_UPDATE);
        lifecycleLogger=orb.getLogger(
                CORBALogDomains.NAMING_LIFECYCLE);
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
            if((n[1].id.length()==0)&&(n[1].kind.length()==0)){
                throw new InvalidName();
            }
            NamingContext context=resolveFirstAsContext(impl,n);
            // Compute restOfName = name[1..length]
            NameComponent[] tail=new NameComponent[n.length-1];
            System.arraycopy(n,1,tail,0,n.length-1);
            // Resolve rest of name in context
            try{
                // First try to resolve using the local call, this should work
                // most of the time unless there are federated naming contexts.
                Servant servant=impl.getNSPOA().reference_to_servant(
                        context);
                return doResolve(((NamingContextDataStore)servant),tail);
            }catch(Exception e){
                return context.resolve(tail);
            }
        }
    }

    public POA getNSPOA(){
        return nsPOA;
    }

    public void bind(NameComponent[] n,Object obj)
            throws NotFound,
            CannotProceed,
            InvalidName,
            AlreadyBound{
        if(obj==null){
            updateLogger.warning(LogKeywords.NAMING_BIND+
                    " unsuccessful because NULL Object cannot be Bound ");
            throw wrapper.objectIsNull();
        }
        // doBind implements all four flavors of binding
        NamingContextDataStore impl=(NamingContextDataStore)this;
        doBind(impl,n,obj,false,BindingType.nobject);
        if(updateLogger.isLoggable(Level.FINE)){
            // isLoggable call to make sure that we save some precious
            // processor cycles, if there is no need to log.
            updateLogger.fine(LogKeywords.NAMING_BIND_SUCCESS+" Name = "+
                    NamingUtils.getDirectoryStructuredName(n));
        }
    }

    public void bind_context(NameComponent[] n,NamingContext nc)
            throws NotFound,
            CannotProceed,
            InvalidName,
            AlreadyBound{
        if(nc==null){
            updateLogger.warning(LogKeywords.NAMING_BIND_FAILURE+
                    " NULL Context cannot be Bound ");
            throw new BAD_PARAM("Naming Context should not be null ");
        }
        // doBind implements all four flavors of binding
        NamingContextDataStore impl=(NamingContextDataStore)this;
        doBind(impl,n,nc,false,BindingType.ncontext);
        if(updateLogger.isLoggable(Level.FINE)){
            // isLoggable call to make sure that we save some precious
            // processor cycles, if there is no need to log.
            updateLogger.fine(LogKeywords.NAMING_BIND_SUCCESS+" Name = "+
                    NamingUtils.getDirectoryStructuredName(n));
        }
    }

    public void rebind(NameComponent[] n,Object obj)
            throws NotFound,
            CannotProceed,
            InvalidName{
        if(obj==null){
            updateLogger.warning(LogKeywords.NAMING_REBIND_FAILURE+
                    " NULL Object cannot be Bound ");
            throw wrapper.objectIsNull();
        }
        try{
            // doBind implements all four flavors of binding
            NamingContextDataStore impl=(NamingContextDataStore)this;
            doBind(impl,n,obj,true,BindingType.nobject);
        }catch(AlreadyBound ex){
            updateLogger.warning(LogKeywords.NAMING_REBIND_FAILURE+
                    NamingUtils.getDirectoryStructuredName(n)+
                    " is already bound to a Naming Context");
            // This should not happen
            throw wrapper.namingCtxRebindAlreadyBound(ex);
        }
        if(updateLogger.isLoggable(Level.FINE)){
            // isLoggable call to make sure that we save some precious
            // processor cycles, if there is no need to log.
            updateLogger.fine(LogKeywords.NAMING_REBIND_SUCCESS+" Name = "+
                    NamingUtils.getDirectoryStructuredName(n));
        }
    }

    public void rebind_context(NameComponent[] n,NamingContext nc)
            throws NotFound,
            CannotProceed,
            InvalidName{
        if(nc==null){
            updateLogger.warning(LogKeywords.NAMING_REBIND_FAILURE+
                    " NULL Context cannot be Bound ");
            throw wrapper.objectIsNull();
        }
        try{
            // doBind implements all four flavors of binding
            NamingContextDataStore impl=(NamingContextDataStore)this;
            doBind(impl,n,nc,true,BindingType.ncontext);
        }catch(AlreadyBound ex){
            // This should not happen
            updateLogger.warning(LogKeywords.NAMING_REBIND_FAILURE+
                    NamingUtils.getDirectoryStructuredName(n)+
                    " is already bound to a CORBA Object");
            throw wrapper.namingCtxRebindctxAlreadyBound(ex);
        }
        if(updateLogger.isLoggable(Level.FINE)){
            // isLoggable call to make sure that we save some precious
            // processor cycles, if there is no need to log.
            updateLogger.fine(LogKeywords.NAMING_REBIND_SUCCESS+" Name = "+
                    NamingUtils.getDirectoryStructuredName(n));
        }
    }

    public Object resolve(NameComponent[] n)
            throws NotFound,
            CannotProceed,
            InvalidName{
        // doResolve actually resolves
        NamingContextDataStore impl=(NamingContextDataStore)this;
        Object obj=doResolve(impl,n);
        if(obj!=null){
            if(readLogger.isLoggable(Level.FINE)){
                readLogger.fine(LogKeywords.NAMING_RESOLVE_SUCCESS+
                        " Name: "+NamingUtils.getDirectoryStructuredName(n));
            }
        }else{
            readLogger.warning(LogKeywords.NAMING_RESOLVE_FAILURE+
                    " Name: "+NamingUtils.getDirectoryStructuredName(n));
        }
        return obj;
    }

    public void unbind(NameComponent[] n)
            throws NotFound,
            CannotProceed,
            InvalidName{
        // doUnbind actually unbinds
        NamingContextDataStore impl=(NamingContextDataStore)this;
        doUnbind(impl,n);
        if(updateLogger.isLoggable(Level.FINE)){
            // isLoggable call to make sure that we save some precious
            // processor cycles, if there is no need to log.
            updateLogger.fine(LogKeywords.NAMING_UNBIND_SUCCESS+
                    " Name: "+NamingUtils.getDirectoryStructuredName(n));
        }
    }

    public void list(int how_many,BindingListHolder bl,
                     BindingIteratorHolder bi){
        // List actually generates the list
        NamingContextDataStore impl=(NamingContextDataStore)this;
        synchronized(impl){
            impl.List(how_many,bl,bi);
        }
        if(readLogger.isLoggable(Level.FINE)&&(bl.value!=null)){
            // isLoggable call to make sure that we save some precious
            // processor cycles, if there is no need to log.
            readLogger.fine(LogKeywords.NAMING_LIST_SUCCESS+
                    "list("+how_many+") -> bindings["+bl.value.length+
                    "] + iterator: "+bi.value);
        }
    }

    public synchronized NamingContext new_context(){
        // Create actually creates a new naming context
        lifecycleLogger.fine("Creating New Naming Context ");
        NamingContextDataStore impl=(NamingContextDataStore)this;
        synchronized(impl){
            NamingContext nctx=impl.NewContext();
            if(nctx!=null){
                lifecycleLogger.fine(LogKeywords.LIFECYCLE_CREATE_SUCCESS);
            }else{
                // If naming context is null, then that must be a serious
                // error.
                lifecycleLogger.severe(LogKeywords.LIFECYCLE_CREATE_FAILURE);
            }
            return nctx;
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
        if(updateLogger.isLoggable(Level.FINE)){
            // isLoggable call to make sure that we save some precious
            // processor cycles, if there is no need to log.
            updateLogger.fine(LogKeywords.NAMING_BIND+
                    "New Context Bound To "+
                    NamingUtils.getDirectoryStructuredName(n));
        }
        return rnc;
    }

    public void destroy()
            throws NotEmpty{
        lifecycleLogger.fine("Destroying Naming Context ");
        NamingContextDataStore impl=(NamingContextDataStore)this;
        synchronized(impl){
            if(impl.IsEmpty()==true){
                // The context is empty so it can be destroyed
                impl.Destroy();
                lifecycleLogger.fine(LogKeywords.LIFECYCLE_DESTROY_SUCCESS);
            }else{
                // This context is not empty!
                // Not a fatal error, warning should do.
                lifecycleLogger.warning(LogKeywords.LIFECYCLE_DESTROY_FAILURE+
                        " NamingContext children are not destroyed still..");
                throw new NotEmpty();
            }
        }
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
            if((n[0].id.length()==0)&&(n[0].kind.length()==0)){
                throw new InvalidName();
            }
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

    public static void doBind(NamingContextDataStore impl,
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
            if((n[0].id.length()==0)&&(n[0].kind.length()==0)){
                throw new InvalidName();
            }
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
                                throw new NotFound(
                                        NotFoundReason.not_context,n);
                            }
                        }else{
                            // Previously a Context was bound and now trying to
                            // bind Object. It is invalid.
                            if(bt.value()==BindingType.nobject.value()){
                                throw new NotFound(
                                        NotFoundReason.not_object,n);
                            }
                        }
                        impl.Unbind(n[0]);
                    }
                }else{
                    if(impl.Resolve(n[0],bth)!=null)
                        // "Resistence is futile." [Borg pickup line]
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
                    // Narrow to a naming context using Java casts. It must
                    // work.
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
                    throw staticWrapper.namingCtxBadBindingtype();
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

    public String to_string(NameComponent[] n)
            throws InvalidName{
        // Name valid?
        if((n==null)||(n.length==0)){
            throw new InvalidName();
        }
        NamingContextDataStore impl=(NamingContextDataStore)this;
        String theStringifiedName=insImpl.convertToString(n);
        if(theStringifiedName==null){
            throw new InvalidName();
        }
        return theStringifiedName;
    }

    public NameComponent[] to_name(String sn)
            throws InvalidName{
        // Name valid?
        if((sn==null)||(sn.length()==0)){
            throw new InvalidName();
        }
        NamingContextDataStore impl=(NamingContextDataStore)this;
        NameComponent[] theNameComponents=
                insImpl.convertToNameComponent(sn);
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
            throw new
                    InvalidAddress();
        }
        NamingContextDataStore impl=(NamingContextDataStore)this;
        String urlBasedAddress=null;
        urlBasedAddress=insImpl.createURLBasedAddress(addr,sn);
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
        NamingContextDataStore impl=(NamingContextDataStore)this;
        NameComponent[] theNameComponents=
                insImpl.convertToNameComponent(sn);
        if((theNameComponents==null)||(theNameComponents.length==0)){
            throw new InvalidName();
        }
        theObject=resolve(theNameComponents);
        return theObject;
    }
}
