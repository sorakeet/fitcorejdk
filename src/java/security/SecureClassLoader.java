/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security;

import sun.security.util.Debug;

import java.util.HashMap;

public class SecureClassLoader extends ClassLoader{
    private static final Debug debug=Debug.getInstance("scl");

    static{
        ClassLoader.registerAsParallelCapable();
    }

    private final boolean initialized;
    // HashMap that maps CodeSource to ProtectionDomain
    // @GuardedBy("pdcache")
    private final HashMap<CodeSource,ProtectionDomain> pdcache=
            new HashMap<>(11);

    protected SecureClassLoader(ClassLoader parent){
        super(parent);
        // this is to make the stack depth consistent with 1.1
        SecurityManager security=System.getSecurityManager();
        if(security!=null){
            security.checkCreateClassLoader();
        }
        initialized=true;
    }

    protected SecureClassLoader(){
        super();
        // this is to make the stack depth consistent with 1.1
        SecurityManager security=System.getSecurityManager();
        if(security!=null){
            security.checkCreateClassLoader();
        }
        initialized=true;
    }

    protected final Class<?> defineClass(String name,
                                         byte[] b,int off,int len,
                                         CodeSource cs){
        return defineClass(name,b,off,len,getProtectionDomain(cs));
    }

    private ProtectionDomain getProtectionDomain(CodeSource cs){
        if(cs==null)
            return null;
        ProtectionDomain pd=null;
        synchronized(pdcache){
            pd=pdcache.get(cs);
            if(pd==null){
                PermissionCollection perms=getPermissions(cs);
                pd=new ProtectionDomain(cs,perms,this,null);
                pdcache.put(cs,pd);
                if(debug!=null){
                    debug.println(" getPermissions "+pd);
                    debug.println("");
                }
            }
        }
        return pd;
    }

    protected PermissionCollection getPermissions(CodeSource codesource){
        check();
        return new Permissions(); // ProtectionDomain defers the binding
    }

    private void check(){
        if(!initialized){
            throw new SecurityException("ClassLoader object not initialized");
        }
    }

    protected final Class<?> defineClass(String name,java.nio.ByteBuffer b,
                                         CodeSource cs){
        return defineClass(name,b,getProtectionDomain(cs));
    }
}
