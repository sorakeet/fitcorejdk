/**
 * Copyright (c) 2003, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.remote.security;

import javax.security.auth.Subject;
import javax.security.auth.SubjectDomainCombiner;
import java.security.*;

public class JMXSubjectDomainCombiner extends SubjectDomainCombiner{
    private static final CodeSource nullCodeSource=
            new CodeSource(null,(java.security.cert.Certificate[])null);
    private static final ProtectionDomain pdNoPerms=
            new ProtectionDomain(nullCodeSource,new Permissions(),null,null);

    public JMXSubjectDomainCombiner(Subject s){
        super(s);
    }

    public static AccessControlContext getContext(Subject subject){
        return new AccessControlContext(AccessController.getContext(),
                new JMXSubjectDomainCombiner(subject));
    }

    public static AccessControlContext
    getDomainCombinerContext(Subject subject){
        return new AccessControlContext(
                new AccessControlContext(new ProtectionDomain[0]),
                new JMXSubjectDomainCombiner(subject));
    }

    public ProtectionDomain[] combine(ProtectionDomain[] current,
                                      ProtectionDomain[] assigned){
        // Add a new ProtectionDomain with the null codesource/signers, and
        // the empty permission set, to the end of the array containing the
        // 'current' protections domains, i.e. the ones that will be augmented
        // with the permissions granted to the set of principals present in
        // the supplied subject.
        //
        ProtectionDomain[] newCurrent;
        if(current==null||current.length==0){
            newCurrent=new ProtectionDomain[1];
            newCurrent[0]=pdNoPerms;
        }else{
            newCurrent=new ProtectionDomain[current.length+1];
            for(int i=0;i<current.length;i++){
                newCurrent[i]=current[i];
            }
            newCurrent[current.length]=pdNoPerms;
        }
        return super.combine(newCurrent,assigned);
    }
}
