/**
 * Copyright (c) 1999, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.spi;

import com.sun.naming.internal.FactoryEnumeration;
import com.sun.naming.internal.ResourceManager;

import javax.naming.*;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import java.util.Hashtable;

public class DirectoryManager extends NamingManager{
    DirectoryManager(){
    }

    @SuppressWarnings("unchecked")
    public static DirContext getContinuationDirContext(
            CannotProceedException cpe) throws NamingException{
        Hashtable<Object,Object> env=(Hashtable<Object,Object>)cpe.getEnvironment();
        if(env==null){
            env=new Hashtable<>(7);
        }else{
            // Make a (shallow) copy of the environment.
            env=(Hashtable<Object,Object>)env.clone();
        }
        env.put(CPE,cpe);
        return (new ContinuationDirContext(cpe,env));
    }

    public static Object
    getObjectInstance(Object refInfo,Name name,Context nameCtx,
                      Hashtable<?,?> environment,Attributes attrs)
            throws Exception{
        ObjectFactory factory;
        ObjectFactoryBuilder builder=getObjectFactoryBuilder();
        if(builder!=null){
            // builder must return non-null factory
            factory=builder.createObjectFactory(refInfo,environment);
            if(factory instanceof DirObjectFactory){
                return ((DirObjectFactory)factory).getObjectInstance(
                        refInfo,name,nameCtx,environment,attrs);
            }else{
                return factory.getObjectInstance(refInfo,name,nameCtx,
                        environment);
            }
        }
        // use reference if possible
        Reference ref=null;
        if(refInfo instanceof Reference){
            ref=(Reference)refInfo;
        }else if(refInfo instanceof Referenceable){
            ref=((Referenceable)(refInfo)).getReference();
        }
        Object answer;
        if(ref!=null){
            String f=ref.getFactoryClassName();
            if(f!=null){
                // if reference identifies a factory, use exclusively
                factory=getObjectFactoryFromReference(ref,f);
                if(factory instanceof DirObjectFactory){
                    return ((DirObjectFactory)factory).getObjectInstance(
                            ref,name,nameCtx,environment,attrs);
                }else if(factory!=null){
                    return factory.getObjectInstance(ref,name,nameCtx,
                            environment);
                }
                // No factory found, so return original refInfo.
                // Will reach this point if factory class is not in
                // class path and reference does not contain a URL for it
                return refInfo;
            }else{
                // if reference has no factory, check for addresses
                // containing URLs
                // ignore name & attrs params; not used in URL factory
                answer=processURLAddrs(ref,name,nameCtx,environment);
                if(answer!=null){
                    return answer;
                }
            }
        }
        // try using any specified factories
        answer=createObjectFromFactories(refInfo,name,nameCtx,
                environment,attrs);
        return (answer!=null)?answer:refInfo;
    }

    private static Object createObjectFromFactories(Object obj,Name name,
                                                    Context nameCtx,Hashtable<?,?> environment,Attributes attrs)
            throws Exception{
        FactoryEnumeration factories=ResourceManager.getFactories(
                Context.OBJECT_FACTORIES,environment,nameCtx);
        if(factories==null)
            return null;
        ObjectFactory factory;
        Object answer=null;
        // Try each factory until one succeeds
        while(answer==null&&factories.hasMore()){
            factory=(ObjectFactory)factories.next();
            if(factory instanceof DirObjectFactory){
                answer=((DirObjectFactory)factory).
                        getObjectInstance(obj,name,nameCtx,environment,attrs);
            }else{
                answer=
                        factory.getObjectInstance(obj,name,nameCtx,environment);
            }
        }
        return answer;
    }

    public static DirStateFactory.Result
    getStateToBind(Object obj,Name name,Context nameCtx,
                   Hashtable<?,?> environment,Attributes attrs)
            throws NamingException{
        // Get list of state factories
        FactoryEnumeration factories=ResourceManager.getFactories(
                Context.STATE_FACTORIES,environment,nameCtx);
        if(factories==null){
            // no factories to try; just return originals
            return new DirStateFactory.Result(obj,attrs);
        }
        // Try each factory until one succeeds
        StateFactory factory;
        Object objanswer;
        DirStateFactory.Result answer=null;
        while(answer==null&&factories.hasMore()){
            factory=(StateFactory)factories.next();
            if(factory instanceof DirStateFactory){
                answer=((DirStateFactory)factory).
                        getStateToBind(obj,name,nameCtx,environment,attrs);
            }else{
                objanswer=
                        factory.getStateToBind(obj,name,nameCtx,environment);
                if(objanswer!=null){
                    answer=new DirStateFactory.Result(objanswer,attrs);
                }
            }
        }
        return (answer!=null)?answer:
                new DirStateFactory.Result(obj,attrs); // nothing new
    }
}
