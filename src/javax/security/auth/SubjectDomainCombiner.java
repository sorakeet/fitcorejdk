/**
 * Copyright (c) 1999, 2016, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth;

import sun.misc.JavaSecurityProtectionDomainAccess;
import sun.misc.SharedSecrets;

import java.lang.ref.WeakReference;
import java.security.*;
import java.util.Set;
import java.util.WeakHashMap;

public class SubjectDomainCombiner implements java.security.DomainCombiner{
    private static final sun.security.util.Debug debug=
            sun.security.util.Debug.getInstance("combiner",
                    "\t[SubjectDomainCombiner]");
    @SuppressWarnings("deprecation")
    // Note: check only at classloading time, not dynamically during combine()
    private static final boolean useJavaxPolicy=
            javax.security.auth.Policy.isCustomPolicySet(debug);
    // Relevant only when useJavaxPolicy is true
    private static final boolean allowCaching=
            (useJavaxPolicy&&cachePolicy());
    private static final JavaSecurityProtectionDomainAccess pdAccess=
            SharedSecrets.getJavaSecurityProtectionDomainAccess();
    private Subject subject;
    private WeakKeyValueMap<ProtectionDomain,ProtectionDomain> cachedPDs=
            new WeakKeyValueMap<>();
    private Set<Principal> principalSet;
    private Principal[] principals;

    public SubjectDomainCombiner(Subject subject){
        this.subject=subject;
        if(subject.isReadOnly()){
            principalSet=subject.getPrincipals();
            principals=principalSet.toArray
                    (new Principal[principalSet.size()]);
        }
    }

    private static boolean cachePolicy(){
        String s=AccessController.doPrivileged
                (new PrivilegedAction<String>(){
                    public String run(){
                        return Security.getProperty("cache.auth.policy");
                    }
                });
        if(s!=null){
            return Boolean.parseBoolean(s);
        }
        // cache by default
        return true;
    }

    public Subject getSubject(){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            sm.checkPermission(new AuthPermission
                    ("getSubjectFromDomainCombiner"));
        }
        return subject;
    }

    public ProtectionDomain[] combine(ProtectionDomain[] currentDomains,
                                      ProtectionDomain[] assignedDomains){
        if(debug!=null){
            if(subject==null){
                debug.println("null subject");
            }else{
                final Subject s=subject;
                AccessController.doPrivileged
                        (new PrivilegedAction<Void>(){
                            public Void run(){
                                debug.println(s.toString());
                                return null;
                            }
                        });
            }
            printInputDomains(currentDomains,assignedDomains);
        }
        if(currentDomains==null||currentDomains.length==0){
            // No need to optimize assignedDomains because it should
            // have been previously optimized (when it was set).
            // Note that we are returning a direct reference
            // to the input array - since ACC does not clone
            // the arrays when it calls combiner.combine,
            // multiple ACC instances may share the same
            // array instance in this case
            return assignedDomains;
        }
        // optimize currentDomains
        //
        // No need to optimize assignedDomains because it should
        // have been previously optimized (when it was set).
        currentDomains=optimize(currentDomains);
        if(debug!=null){
            debug.println("after optimize");
            printInputDomains(currentDomains,assignedDomains);
        }
        if(currentDomains==null&&assignedDomains==null){
            return null;
        }
        // maintain backwards compatibility for developers who provide
        // their own custom javax.security.auth.Policy implementations
        if(useJavaxPolicy){
            return combineJavaxPolicy(currentDomains,assignedDomains);
        }
        int cLen=(currentDomains==null?0:currentDomains.length);
        int aLen=(assignedDomains==null?0:assignedDomains.length);
        // the ProtectionDomains for the new AccessControlContext
        // that we will return
        ProtectionDomain[] newDomains=new ProtectionDomain[cLen+aLen];
        boolean allNew=true;
        synchronized(cachedPDs){
            if(!subject.isReadOnly()&&
                    !subject.getPrincipals().equals(principalSet)){
                // if the Subject was mutated, clear the PD cache
                Set<Principal> newSet=subject.getPrincipals();
                synchronized(newSet){
                    principalSet=new java.util.HashSet<Principal>(newSet);
                }
                principals=principalSet.toArray
                        (new Principal[principalSet.size()]);
                cachedPDs.clear();
                if(debug!=null){
                    debug.println("Subject mutated - clearing cache");
                }
            }
            ProtectionDomain subjectPd;
            for(int i=0;i<cLen;i++){
                ProtectionDomain pd=currentDomains[i];
                subjectPd=cachedPDs.getValue(pd);
                if(subjectPd==null){
                    if(pdAccess.getStaticPermissionsField(pd)){
                        // Need to keep static ProtectionDomain objects static
                        subjectPd=new ProtectionDomain(pd.getCodeSource(),
                                pd.getPermissions());
                    }else{
                        subjectPd=new ProtectionDomain(pd.getCodeSource(),
                                pd.getPermissions(),
                                pd.getClassLoader(),
                                principals);
                    }
                    cachedPDs.putValue(pd,subjectPd);
                }else{
                    allNew=false;
                }
                newDomains[i]=subjectPd;
            }
        }
        if(debug!=null){
            debug.println("updated current: ");
            for(int i=0;i<cLen;i++){
                debug.println("\tupdated["+i+"] = "+
                        printDomain(newDomains[i]));
            }
        }
        // now add on the assigned domains
        if(aLen>0){
            System.arraycopy(assignedDomains,0,newDomains,cLen,aLen);
            // optimize the result (cached PDs might exist in assignedDomains)
            if(!allNew){
                newDomains=optimize(newDomains);
            }
        }
        // if aLen == 0 || allNew, no need to further optimize newDomains
        if(debug!=null){
            if(newDomains==null||newDomains.length==0){
                debug.println("returning null");
            }else{
                debug.println("combinedDomains: ");
                for(int i=0;i<newDomains.length;i++){
                    debug.println("newDomain "+i+": "+
                            printDomain(newDomains[i]));
                }
            }
        }
        // return the new ProtectionDomains
        if(newDomains==null||newDomains.length==0){
            return null;
        }else{
            return newDomains;
        }
    }

    private ProtectionDomain[] combineJavaxPolicy(
            ProtectionDomain[] currentDomains,
            ProtectionDomain[] assignedDomains){
        if(!allowCaching){
            AccessController.doPrivileged
                    (new PrivilegedAction<Void>(){
                        @SuppressWarnings("deprecation")
                        public Void run(){
                            // Call refresh only caching is disallowed
                            javax.security.auth.Policy.getPolicy().refresh();
                            return null;
                        }
                    });
        }
        int cLen=(currentDomains==null?0:currentDomains.length);
        int aLen=(assignedDomains==null?0:assignedDomains.length);
        // the ProtectionDomains for the new AccessControlContext
        // that we will return
        ProtectionDomain[] newDomains=new ProtectionDomain[cLen+aLen];
        synchronized(cachedPDs){
            if(!subject.isReadOnly()&&
                    !subject.getPrincipals().equals(principalSet)){
                // if the Subject was mutated, clear the PD cache
                Set<Principal> newSet=subject.getPrincipals();
                synchronized(newSet){
                    principalSet=new java.util.HashSet<Principal>(newSet);
                }
                principals=principalSet.toArray
                        (new Principal[principalSet.size()]);
                cachedPDs.clear();
                if(debug!=null){
                    debug.println("Subject mutated - clearing cache");
                }
            }
            for(int i=0;i<cLen;i++){
                ProtectionDomain pd=currentDomains[i];
                ProtectionDomain subjectPd=cachedPDs.getValue(pd);
                if(subjectPd==null){
                    if(pdAccess.getStaticPermissionsField(pd)){
                        // keep static ProtectionDomain objects static
                        subjectPd=new ProtectionDomain(pd.getCodeSource(),
                                pd.getPermissions());
                    }else{
                        // XXX
                        // we must first add the original permissions.
                        // that way when we later add the new JAAS permissions,
                        // any unresolved JAAS-related permissions will
                        // automatically get resolved.
                        // get the original perms
                        Permissions perms=new Permissions();
                        PermissionCollection coll=pd.getPermissions();
                        java.util.Enumeration<Permission> e;
                        if(coll!=null){
                            synchronized(coll){
                                e=coll.elements();
                                while(e.hasMoreElements()){
                                    Permission newPerm=
                                            e.nextElement();
                                    perms.add(newPerm);
                                }
                            }
                        }
                        // get perms from the policy
                        final java.security.CodeSource finalCs=pd.getCodeSource();
                        final Subject finalS=subject;
                        PermissionCollection newPerms=
                                AccessController.doPrivileged
                                        (new PrivilegedAction<PermissionCollection>(){
                                            @SuppressWarnings("deprecation")
                                            public PermissionCollection run(){
                                                return
                                                        javax.security.auth.Policy.getPolicy().getPermissions
                                                                (finalS,finalCs);
                                            }
                                        });
                        // add the newly granted perms,
                        // avoiding duplicates
                        synchronized(newPerms){
                            e=newPerms.elements();
                            while(e.hasMoreElements()){
                                Permission newPerm=e.nextElement();
                                if(!perms.implies(newPerm)){
                                    perms.add(newPerm);
                                    if(debug!=null)
                                        debug.println(
                                                "Adding perm "+newPerm+"\n");
                                }
                            }
                        }
                        subjectPd=new ProtectionDomain
                                (finalCs,perms,pd.getClassLoader(),principals);
                    }
                    if(allowCaching)
                        cachedPDs.putValue(pd,subjectPd);
                }
                newDomains[i]=subjectPd;
            }
        }
        if(debug!=null){
            debug.println("updated current: ");
            for(int i=0;i<cLen;i++){
                debug.println("\tupdated["+i+"] = "+newDomains[i]);
            }
        }
        // now add on the assigned domains
        if(aLen>0){
            System.arraycopy(assignedDomains,0,newDomains,cLen,aLen);
        }
        if(debug!=null){
            if(newDomains==null||newDomains.length==0){
                debug.println("returning null");
            }else{
                debug.println("combinedDomains: ");
                for(int i=0;i<newDomains.length;i++){
                    debug.println("newDomain "+i+": "+
                            newDomains[i].toString());
                }
            }
        }
        // return the new ProtectionDomains
        if(newDomains==null||newDomains.length==0){
            return null;
        }else{
            return newDomains;
        }
    }

    private static ProtectionDomain[] optimize(ProtectionDomain[] domains){
        if(domains==null||domains.length==0)
            return null;
        ProtectionDomain[] optimized=new ProtectionDomain[domains.length];
        ProtectionDomain pd;
        int num=0;
        for(int i=0;i<domains.length;i++){
            // skip domains with AllPermission
            // XXX
            //
            //  if (domains[i].implies(ALL_PERMISSION))
            //  continue;
            // skip System Domains
            if((pd=domains[i])!=null){
                // remove duplicates
                boolean found=false;
                for(int j=0;j<num&&!found;j++){
                    found=(optimized[j]==pd);
                }
                if(!found){
                    optimized[num++]=pd;
                }
            }
        }
        // resize the array if necessary
        if(num>0&&num<domains.length){
            ProtectionDomain[] downSize=new ProtectionDomain[num];
            System.arraycopy(optimized,0,downSize,0,downSize.length);
            optimized=downSize;
        }
        return ((num==0||optimized.length==0)?null:optimized);
    }

    private static void printInputDomains(ProtectionDomain[] currentDomains,
                                          ProtectionDomain[] assignedDomains){
        if(currentDomains==null||currentDomains.length==0){
            debug.println("currentDomains null or 0 length");
        }else{
            for(int i=0;currentDomains!=null&&
                    i<currentDomains.length;i++){
                if(currentDomains[i]==null){
                    debug.println("currentDomain "+i+": SystemDomain");
                }else{
                    debug.println("currentDomain "+i+": "+
                            printDomain(currentDomains[i]));
                }
            }
        }
        if(assignedDomains==null||assignedDomains.length==0){
            debug.println("assignedDomains null or 0 length");
        }else{
            debug.println("assignedDomains = ");
            for(int i=0;assignedDomains!=null&&
                    i<assignedDomains.length;i++){
                if(assignedDomains[i]==null){
                    debug.println("assignedDomain "+i+": SystemDomain");
                }else{
                    debug.println("assignedDomain "+i+": "+
                            printDomain(assignedDomains[i]));
                }
            }
        }
    }

    private static String printDomain(final ProtectionDomain pd){
        if(pd==null){
            return "null";
        }
        return AccessController.doPrivileged(new PrivilegedAction<String>(){
            public String run(){
                return pd.toString();
            }
        });
    }

    private static class WeakKeyValueMap<K,V> extends
            WeakHashMap<K,WeakReference<V>>{
        public V getValue(K key){
            WeakReference<V> wr=super.get(key);
            if(wr!=null){
                return wr.get();
            }
            return null;
        }

        public V putValue(K key,V value){
            WeakReference<V> wr=super.put(key,new WeakReference<V>(value));
            if(wr!=null){
                return wr.get();
            }
            return null;
        }
    }
}
