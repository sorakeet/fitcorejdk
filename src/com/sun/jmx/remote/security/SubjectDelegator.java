/**
 * Copyright (c) 2003, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.remote.security;

import javax.management.remote.SubjectDelegationPermission;
import javax.security.auth.Subject;
import java.security.*;
import java.util.*;

public class SubjectDelegator{
    public static synchronized boolean
    checkRemoveCallerContext(Subject subject){
        try{
            for(Principal p : getSubjectPrincipals(subject)){
                final String pname=
                        p.getClass().getName()+"."+p.getName();
                final Permission sdp=
                        new SubjectDelegationPermission(pname);
                AccessController.checkPermission(sdp);
            }
        }catch(SecurityException e){
            return false;
        }
        return true;
    }

    public AccessControlContext
    delegatedContext(AccessControlContext authenticatedACC,
                     Subject delegatedSubject,
                     boolean removeCallerContext)
            throws SecurityException{
        if(System.getSecurityManager()!=null&&authenticatedACC==null){
            throw new SecurityException("Illegal AccessControlContext: null");
        }
        // Check if the subject delegation permission allows the
        // authenticated subject to assume the identity of each
        // principal in the delegated subject
        //
        Collection<Principal> ps=getSubjectPrincipals(delegatedSubject);
        final Collection<Permission> permissions=new ArrayList<>(ps.size());
        for(Principal p : ps){
            final String pname=p.getClass().getName()+"."+p.getName();
            permissions.add(new SubjectDelegationPermission(pname));
        }
        PrivilegedAction<Void> action=
                new PrivilegedAction<Void>(){
                    public Void run(){
                        for(Permission sdp : permissions){
                            AccessController.checkPermission(sdp);
                        }
                        return null;
                    }
                };
        AccessController.doPrivileged(action,authenticatedACC);
        return getDelegatedAcc(delegatedSubject,removeCallerContext);
    }

    private AccessControlContext getDelegatedAcc(Subject delegatedSubject,boolean removeCallerContext){
        if(removeCallerContext){
            return JMXSubjectDomainCombiner.getDomainCombinerContext(delegatedSubject);
        }else{
            return JMXSubjectDomainCombiner.getContext(delegatedSubject);
        }
    }

    private static Collection<Principal> getSubjectPrincipals(Subject subject){
        if(subject.isReadOnly()){
            return subject.getPrincipals();
        }
        List<Principal> principals=Arrays.asList(subject.getPrincipals().toArray(new Principal[0]));
        return Collections.unmodifiableList(principals);
    }
}
