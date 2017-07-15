/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security;

import sun.security.jca.GetInstance;
import sun.security.util.Debug;
import sun.security.util.SecurityConstants;

import java.util.Enumeration;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;

public abstract class Policy{
    public static final PermissionCollection UNSUPPORTED_EMPTY_COLLECTION=
            new UnsupportedEmptyCollection();
    private static final Debug debug=Debug.getInstance("policy");
    // PolicyInfo is stored in an AtomicReference
    private static AtomicReference<PolicyInfo> policy=
            new AtomicReference<>(new PolicyInfo(null,false));
    // Cache mapping ProtectionDomain.Key to PermissionCollection
    private WeakHashMap<ProtectionDomain.Key,PermissionCollection> pdMapping;

    static boolean isSet(){
        PolicyInfo pi=policy.get();
        return pi.policy!=null&&pi.initialized==true;
    }

    public static Policy getPolicy(){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null)
            sm.checkPermission(SecurityConstants.GET_POLICY_PERMISSION);
        return getPolicyNoCheck();
    }

    static Policy getPolicyNoCheck(){
        PolicyInfo pi=policy.get();
        // Use double-check idiom to avoid locking if system-wide policy is
        // already initialized
        if(pi.initialized==false||pi.policy==null){
            synchronized(Policy.class){
                PolicyInfo pinfo=policy.get();
                if(pinfo.policy==null){
                    String policy_class=AccessController.doPrivileged(
                            new PrivilegedAction<String>(){
                                public String run(){
                                    return Security.getProperty("policy.provider");
                                }
                            });
                    if(policy_class==null){
                        policy_class="sun.security.provider.PolicyFile";
                    }
                    try{
                        pinfo=new PolicyInfo(
                                (Policy)Class.forName(policy_class).newInstance(),
                                true);
                    }catch(Exception e){
                        /**
                         * The policy_class seems to be an extension
                         * so we have to bootstrap loading it via a policy
                         * provider that is on the bootclasspath.
                         * If it loads then shift gears to using the configured
                         * provider.
                         */
                        // install the bootstrap provider to avoid recursion
                        Policy polFile=new sun.security.provider.PolicyFile();
                        pinfo=new PolicyInfo(polFile,false);
                        policy.set(pinfo);
                        final String pc=policy_class;
                        Policy pol=AccessController.doPrivileged(
                                new PrivilegedAction<Policy>(){
                                    public Policy run(){
                                        try{
                                            ClassLoader cl=
                                                    ClassLoader.getSystemClassLoader();
                                            // we want the extension loader
                                            ClassLoader extcl=null;
                                            while(cl!=null){
                                                extcl=cl;
                                                cl=cl.getParent();
                                            }
                                            return (extcl!=null?(Policy)Class.forName(
                                                    pc,true,extcl).newInstance():null);
                                        }catch(Exception e){
                                            if(debug!=null){
                                                debug.println("policy provider "+
                                                        pc+
                                                        " not available");
                                                e.printStackTrace();
                                            }
                                            return null;
                                        }
                                    }
                                });
                        /**
                         * if it loaded install it as the policy provider. Otherwise
                         * continue to use the system default implementation
                         */
                        if(pol!=null){
                            pinfo=new PolicyInfo(pol,true);
                        }else{
                            if(debug!=null){
                                debug.println("using sun.security.provider.PolicyFile");
                            }
                            pinfo=new PolicyInfo(polFile,true);
                        }
                    }
                    policy.set(pinfo);
                }
                return pinfo.policy;
            }
        }
        return pi.policy;
    }

    public static void setPolicy(Policy p){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null) sm.checkPermission(
                new SecurityPermission("setPolicy"));
        if(p!=null){
            initPolicy(p);
        }
        synchronized(Policy.class){
            policy.set(new PolicyInfo(p,p!=null));
        }
    }

    private static void initPolicy(final Policy p){
        /**
         * A policy provider not on the bootclasspath could trigger
         * security checks fulfilling a call to either Policy.implies
         * or Policy.getPermissions. If this does occur the provider
         * must be able to answer for it's own ProtectionDomain
         * without triggering additional security checks, otherwise
         * the policy implementation will end up in an infinite
         * recursion.
         *
         * To mitigate this, the provider can collect it's own
         * ProtectionDomain and associate a PermissionCollection while
         * it is being installed. The currently installed policy
         * provider (if there is one) will handle calls to
         * Policy.implies or Policy.getPermissions during this
         * process.
         *
         * This Policy superclass caches away the ProtectionDomain and
         * statically binds permissions so that legacy Policy
         * implementations will continue to function.
         */
        ProtectionDomain policyDomain=
                AccessController.doPrivileged(new PrivilegedAction<ProtectionDomain>(){
                    public ProtectionDomain run(){
                        return p.getClass().getProtectionDomain();
                    }
                });
        /**
         * Collect the permissions granted to this protection domain
         * so that the provider can be security checked while processing
         * calls to Policy.implies or Policy.getPermissions.
         */
        PermissionCollection policyPerms=null;
        synchronized(p){
            if(p.pdMapping==null){
                p.pdMapping=new WeakHashMap<>();
            }
        }
        if(policyDomain.getCodeSource()!=null){
            Policy pol=policy.get().policy;
            if(pol!=null){
                policyPerms=pol.getPermissions(policyDomain);
            }
            if(policyPerms==null){ // assume it has all
                policyPerms=new Permissions();
                policyPerms.add(SecurityConstants.ALL_PERMISSION);
            }
            synchronized(p.pdMapping){
                // cache of pd to permissions
                p.pdMapping.put(policyDomain.key,policyPerms);
            }
        }
        return;
    }

    public static Policy getInstance(String type,Parameters params)
            throws NoSuchAlgorithmException{
        checkPermission(type);
        try{
            GetInstance.Instance instance=GetInstance.getInstance("Policy",
                    PolicySpi.class,
                    type,
                    params);
            return new PolicyDelegate((PolicySpi)instance.impl,
                    instance.provider,
                    type,
                    params);
        }catch(NoSuchAlgorithmException nsae){
            return handleException(nsae);
        }
    }

    private static void checkPermission(String type){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            sm.checkPermission(new SecurityPermission("createPolicy."+type));
        }
    }

    private static Policy handleException(NoSuchAlgorithmException nsae)
            throws NoSuchAlgorithmException{
        Throwable cause=nsae.getCause();
        if(cause instanceof IllegalArgumentException){
            throw (IllegalArgumentException)cause;
        }
        throw nsae;
    }

    public static Policy getInstance(String type,
                                     Parameters params,
                                     String provider)
            throws NoSuchProviderException, NoSuchAlgorithmException{
        if(provider==null||provider.length()==0){
            throw new IllegalArgumentException("missing provider");
        }
        checkPermission(type);
        try{
            GetInstance.Instance instance=GetInstance.getInstance("Policy",
                    PolicySpi.class,
                    type,
                    params,
                    provider);
            return new PolicyDelegate((PolicySpi)instance.impl,
                    instance.provider,
                    type,
                    params);
        }catch(NoSuchAlgorithmException nsae){
            return handleException(nsae);
        }
    }

    public static Policy getInstance(String type,
                                     Parameters params,
                                     Provider provider)
            throws NoSuchAlgorithmException{
        if(provider==null){
            throw new IllegalArgumentException("missing provider");
        }
        checkPermission(type);
        try{
            GetInstance.Instance instance=GetInstance.getInstance("Policy",
                    PolicySpi.class,
                    type,
                    params,
                    provider);
            return new PolicyDelegate((PolicySpi)instance.impl,
                    instance.provider,
                    type,
                    params);
        }catch(NoSuchAlgorithmException nsae){
            return handleException(nsae);
        }
    }

    public Provider getProvider(){
        return null;
    }

    public String getType(){
        return null;
    }

    public Parameters getParameters(){
        return null;
    }

    public boolean implies(ProtectionDomain domain,Permission permission){
        PermissionCollection pc;
        if(pdMapping==null){
            initPolicy(this);
        }
        synchronized(pdMapping){
            pc=pdMapping.get(domain.key);
        }
        if(pc!=null){
            return pc.implies(permission);
        }
        pc=getPermissions(domain);
        if(pc==null){
            return false;
        }
        synchronized(pdMapping){
            // cache it
            pdMapping.put(domain.key,pc);
        }
        return pc.implies(permission);
    }

    public PermissionCollection getPermissions(ProtectionDomain domain){
        PermissionCollection pc=null;
        if(domain==null)
            return new Permissions();
        if(pdMapping==null){
            initPolicy(this);
        }
        synchronized(pdMapping){
            pc=pdMapping.get(domain.key);
        }
        if(pc!=null){
            Permissions perms=new Permissions();
            synchronized(pc){
                for(Enumeration<Permission> e=pc.elements();e.hasMoreElements();){
                    perms.add(e.nextElement());
                }
            }
            return perms;
        }
        pc=getPermissions(domain.getCodeSource());
        if(pc==null||pc==UNSUPPORTED_EMPTY_COLLECTION){
            pc=new Permissions();
        }
        addStaticPerms(pc,domain.getPermissions());
        return pc;
    }

    public PermissionCollection getPermissions(CodeSource codesource){
        return Policy.UNSUPPORTED_EMPTY_COLLECTION;
    }

    private void addStaticPerms(PermissionCollection perms,
                                PermissionCollection statics){
        if(statics!=null){
            synchronized(statics){
                Enumeration<Permission> e=statics.elements();
                while(e.hasMoreElements()){
                    perms.add(e.nextElement());
                }
            }
        }
    }

    public void refresh(){
    }

    public static interface Parameters{
    }

    // Information about the system-wide policy.
    private static class PolicyInfo{
        // the system-wide policy
        final Policy policy;
        // a flag indicating if the system-wide policy has been initialized
        final boolean initialized;

        PolicyInfo(Policy policy,boolean initialized){
            this.policy=policy;
            this.initialized=initialized;
        }
    }

    private static class PolicyDelegate extends Policy{
        private PolicySpi spi;
        private Provider p;
        private String type;
        private Parameters params;

        private PolicyDelegate(PolicySpi spi,Provider p,
                               String type,Parameters params){
            this.spi=spi;
            this.p=p;
            this.type=type;
            this.params=params;
        }

        @Override
        public Provider getProvider(){
            return p;
        }

        @Override
        public String getType(){
            return type;
        }

        @Override
        public Parameters getParameters(){
            return params;
        }

        @Override
        public PermissionCollection getPermissions(CodeSource codesource){
            return spi.engineGetPermissions(codesource);
        }

        @Override
        public PermissionCollection getPermissions(ProtectionDomain domain){
            return spi.engineGetPermissions(domain);
        }

        @Override
        public boolean implies(ProtectionDomain domain,Permission perm){
            return spi.engineImplies(domain,perm);
        }

        @Override
        public void refresh(){
            spi.engineRefresh();
        }
    }

    private static class UnsupportedEmptyCollection
            extends PermissionCollection{
        private static final long serialVersionUID=-8492269157353014774L;
        private Permissions perms;

        public UnsupportedEmptyCollection(){
            this.perms=new Permissions();
            perms.setReadOnly();
        }

        @Override
        public void add(Permission permission){
            perms.add(permission);
        }

        @Override
        public boolean implies(Permission permission){
            return perms.implies(permission);
        }

        @Override
        public Enumeration<Permission> elements(){
            return perms.elements();
        }
    }
}
