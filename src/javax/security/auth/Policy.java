/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth;

import sun.security.util.Debug;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.Security;
import java.util.Objects;

@Deprecated
public abstract class Policy{
    private final static String AUTH_POLICY=
            "sun.security.provider.AuthPolicyFile";
    private static Policy policy;
    // true if a custom (not AUTH_POLICY) system-wide policy object is set
    private static boolean isCustomPolicy;
    private final java.security.AccessControlContext acc=
            AccessController.getContext();

    protected Policy(){
    }

    public static Policy getPolicy(){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null) sm.checkPermission(new AuthPermission("getPolicy"));
        return getPolicyNoCheck();
    }

    static Policy getPolicyNoCheck(){
        if(policy==null){
            synchronized(Policy.class){
                if(policy==null){
                    String policy_class=null;
                    policy_class=AccessController.doPrivileged
                            (new PrivilegedAction<String>(){
                                public String run(){
                                    return Security.getProperty
                                            ("auth.policy.provider");
                                }
                            });
                    if(policy_class==null){
                        policy_class=AUTH_POLICY;
                    }
                    try{
                        final String finalClass=policy_class;
                        Policy untrustedImpl=AccessController.doPrivileged(
                                new PrivilegedExceptionAction<Policy>(){
                                    public Policy run() throws ClassNotFoundException,
                                            InstantiationException,
                                            IllegalAccessException{
                                        Class<? extends Policy> implClass=Class.forName(
                                                finalClass,false,
                                                Thread.currentThread().getContextClassLoader()
                                        ).asSubclass(Policy.class);
                                        return implClass.newInstance();
                                    }
                                });
                        AccessController.doPrivileged(
                                new PrivilegedExceptionAction<Void>(){
                                    public Void run(){
                                        setPolicy(untrustedImpl);
                                        isCustomPolicy=!finalClass.equals(AUTH_POLICY);
                                        return null;
                                    }
                                },Objects.requireNonNull(untrustedImpl.acc)
                        );
                    }catch(Exception e){
                        throw new SecurityException
                                (sun.security.util.ResourcesMgr.getString
                                        ("unable.to.instantiate.Subject.based.policy"));
                    }
                }
            }
        }
        return policy;
    }

    public static void setPolicy(Policy policy){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null) sm.checkPermission(new AuthPermission("setPolicy"));
        Policy.policy=policy;
        // all non-null policy objects are assumed to be custom
        isCustomPolicy=policy!=null?true:false;
    }

    static boolean isCustomPolicySet(Debug debug){
        if(policy!=null){
            if(debug!=null&&isCustomPolicy){
                debug.println("Providing backwards compatibility for "+
                        "javax.security.auth.policy implementation: "+
                        policy.toString());
            }
            return isCustomPolicy;
        }
        // check if custom policy has been set using auth.policy.provider prop
        String policyClass=AccessController.doPrivileged
                (new PrivilegedAction<String>(){
                    public String run(){
                        return Security.getProperty("auth.policy.provider");
                    }
                });
        if(policyClass!=null&&!policyClass.equals(AUTH_POLICY)){
            if(debug!=null){
                debug.println("Providing backwards compatibility for "+
                        "javax.security.auth.policy implementation: "+
                        policyClass);
            }
            return true;
        }
        return false;
    }

    public abstract java.security.PermissionCollection getPermissions
            (Subject subject,
             java.security.CodeSource cs);

    public abstract void refresh();
}
