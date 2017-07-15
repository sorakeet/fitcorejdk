/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.security.auth.module;

import com.sun.security.auth.SolarisNumericGroupPrincipal;
import com.sun.security.auth.SolarisNumericUserPrincipal;
import com.sun.security.auth.SolarisPrincipal;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.util.LinkedList;
import java.util.Map;

@jdk.Exported(false)
@Deprecated
public class SolarisLoginModule implements LoginModule{
    // initial state
    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map<String,?> sharedState;
    private Map<String,?> options;
    // configurable option
    private boolean debug=true;
    // SolarisSystem to retrieve underlying system info
    private SolarisSystem ss;
    // the authentication status
    private boolean succeeded=false;
    private boolean commitSucceeded=false;
    // Underlying system info
    private SolarisPrincipal userPrincipal;
    private SolarisNumericUserPrincipal UIDPrincipal;
    private SolarisNumericGroupPrincipal GIDPrincipal;
    private LinkedList<SolarisNumericGroupPrincipal> supplementaryGroups=
            new LinkedList<>();

    public void initialize(Subject subject,CallbackHandler callbackHandler,
                           Map<String,?> sharedState,
                           Map<String,?> options){
        this.subject=subject;
        this.callbackHandler=callbackHandler;
        this.sharedState=sharedState;
        this.options=options;
        // initialize any configured options
        debug="true".equalsIgnoreCase((String)options.get("debug"));
    }

    public boolean login() throws LoginException{
        long[] solarisGroups=null;
        ss=new SolarisSystem();
        if(ss==null){
            succeeded=false;
            throw new FailedLoginException
                    ("Failed in attempt to import "+
                            "the underlying system identity information");
        }else{
            userPrincipal=new SolarisPrincipal(ss.getUsername());
            UIDPrincipal=new SolarisNumericUserPrincipal(ss.getUid());
            GIDPrincipal=new SolarisNumericGroupPrincipal(ss.getGid(),true);
            if(ss.getGroups()!=null&&ss.getGroups().length>0)
                solarisGroups=ss.getGroups();
            for(int i=0;i<solarisGroups.length;i++){
                SolarisNumericGroupPrincipal ngp=
                        new SolarisNumericGroupPrincipal
                                (solarisGroups[i],false);
                if(!ngp.getName().equals(GIDPrincipal.getName()))
                    supplementaryGroups.add(ngp);
            }
            if(debug){
                System.out.println("\t\t[SolarisLoginModule]: "+
                        "succeeded importing info: ");
                System.out.println("\t\t\tuid = "+ss.getUid());
                System.out.println("\t\t\tgid = "+ss.getGid());
                solarisGroups=ss.getGroups();
                for(int i=0;i<solarisGroups.length;i++){
                    System.out.println("\t\t\tsupp gid = "+solarisGroups[i]);
                }
            }
            succeeded=true;
            return true;
        }
    }

    public boolean commit() throws LoginException{
        if(succeeded==false){
            if(debug){
                System.out.println("\t\t[SolarisLoginModule]: "+
                        "did not add any Principals to Subject "+
                        "because own authentication failed.");
            }
            return false;
        }
        if(subject.isReadOnly()){
            throw new LoginException("Subject is Readonly");
        }
        if(!subject.getPrincipals().contains(userPrincipal))
            subject.getPrincipals().add(userPrincipal);
        if(!subject.getPrincipals().contains(UIDPrincipal))
            subject.getPrincipals().add(UIDPrincipal);
        if(!subject.getPrincipals().contains(GIDPrincipal))
            subject.getPrincipals().add(GIDPrincipal);
        for(int i=0;i<supplementaryGroups.size();i++){
            if(!subject.getPrincipals().contains(supplementaryGroups.get(i)))
                subject.getPrincipals().add(supplementaryGroups.get(i));
        }
        if(debug){
            System.out.println("\t\t[SolarisLoginModule]: "+
                    "added SolarisPrincipal,");
            System.out.println("\t\t\t\tSolarisNumericUserPrincipal,");
            System.out.println("\t\t\t\tSolarisNumericGroupPrincipal(s),");
            System.out.println("\t\t\t to Subject");
        }
        commitSucceeded=true;
        return true;
    }

    public boolean abort() throws LoginException{
        if(debug){
            System.out.println("\t\t[SolarisLoginModule]: "+
                    "aborted authentication attempt");
        }
        if(succeeded==false){
            return false;
        }else if(succeeded==true&&commitSucceeded==false){
            // Clean out state
            succeeded=false;
            ss=null;
            userPrincipal=null;
            UIDPrincipal=null;
            GIDPrincipal=null;
            supplementaryGroups=
                    new LinkedList<SolarisNumericGroupPrincipal>();
        }else{
            // overall authentication succeeded and commit succeeded,
            // but someone else's commit failed
            logout();
        }
        return true;
    }

    public boolean logout() throws LoginException{
        if(debug){
            System.out.println("\t\t[SolarisLoginModule]: "+
                    "Entering logout");
        }
        if(subject.isReadOnly()){
            throw new LoginException("Subject is Readonly");
        }
        // remove the added Principals from the Subject
        subject.getPrincipals().remove(userPrincipal);
        subject.getPrincipals().remove(UIDPrincipal);
        subject.getPrincipals().remove(GIDPrincipal);
        for(int i=0;i<supplementaryGroups.size();i++){
            subject.getPrincipals().remove(supplementaryGroups.get(i));
        }
        // clean out state
        ss=null;
        succeeded=false;
        commitSucceeded=false;
        userPrincipal=null;
        UIDPrincipal=null;
        GIDPrincipal=null;
        supplementaryGroups=new LinkedList<SolarisNumericGroupPrincipal>();
        if(debug){
            System.out.println("\t\t[SolarisLoginModule]: "+
                    "logged out Subject");
        }
        return true;
    }
}
