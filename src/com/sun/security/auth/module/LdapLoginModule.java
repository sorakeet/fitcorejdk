/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.security.auth.module;

import com.sun.security.auth.LdapPrincipal;
import com.sun.security.auth.UserPrincipal;

import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@jdk.Exported
public class LdapLoginModule implements LoginModule{
    // Use the default classloader for this class to load the prompt strings.
    private static final ResourceBundle rb=AccessController.doPrivileged(
            new PrivilegedAction<ResourceBundle>(){
                public ResourceBundle run(){
                    return ResourceBundle.getBundle(
                            "sun.security.util.AuthResources");
                }
            }
    );
    // Keys to retrieve the stored username and password
    private static final String USERNAME_KEY="javax.security.auth.login.name";
    private static final String PASSWORD_KEY=
            "javax.security.auth.login.password";
    // Option names
    private static final String USER_PROVIDER="userProvider";
    private static final String USER_FILTER="userFilter";
    private static final String AUTHC_IDENTITY="authIdentity";
    private static final String AUTHZ_IDENTITY="authzIdentity";
    // Used for the username token replacement
    private static final String USERNAME_TOKEN="{USERNAME}";
    private static final Pattern USERNAME_PATTERN=
            Pattern.compile("\\{USERNAME\\}");
    // Configurable options
    private String userProvider;
    private String userFilter;
    private String authcIdentity;
    private String authzIdentity;
    private String authzIdentityAttr=null;
    private boolean useSSL=true;
    private boolean authFirst=false;
    private boolean authOnly=false;
    private boolean useFirstPass=false;
    private boolean tryFirstPass=false;
    private boolean storePass=false;
    private boolean clearPass=false;
    private boolean debug=false;
    // Authentication status
    private boolean succeeded=false;
    private boolean commitSucceeded=false;
    // Supplied username and password
    private String username;
    private char[] password;
    // User's identities
    private LdapPrincipal ldapPrincipal;
    private UserPrincipal userPrincipal;
    private UserPrincipal authzPrincipal;
    // Initial state
    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map<String,Object> sharedState;
    private Map<String,?> options;
    private LdapContext ctx;
    private Matcher identityMatcher=null;
    private Matcher filterMatcher=null;
    private Hashtable<String,Object> ldapEnvironment;
    private SearchControls constraints=null;

    // Unchecked warning from (Map<String, Object>)sharedState is safe
    // since javax.security.auth.login.LoginContext passes a raw HashMap.
    @SuppressWarnings("unchecked")
    public void initialize(Subject subject,CallbackHandler callbackHandler,
                           Map<String,?> sharedState,Map<String,?> options){
        this.subject=subject;
        this.callbackHandler=callbackHandler;
        this.sharedState=(Map<String,Object>)sharedState;
        this.options=options;
        ldapEnvironment=new Hashtable<String,Object>(9);
        ldapEnvironment.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
        // Add any JNDI properties to the environment
        for(String key : options.keySet()){
            if(key.indexOf(".")>-1){
                ldapEnvironment.put(key,options.get(key));
            }
        }
        // initialize any configured options
        userProvider=(String)options.get(USER_PROVIDER);
        if(userProvider!=null){
            ldapEnvironment.put(Context.PROVIDER_URL,userProvider);
        }
        authcIdentity=(String)options.get(AUTHC_IDENTITY);
        if(authcIdentity!=null&&
                (authcIdentity.indexOf(USERNAME_TOKEN)!=-1)){
            identityMatcher=USERNAME_PATTERN.matcher(authcIdentity);
        }
        userFilter=(String)options.get(USER_FILTER);
        if(userFilter!=null){
            if(userFilter.indexOf(USERNAME_TOKEN)!=-1){
                filterMatcher=USERNAME_PATTERN.matcher(userFilter);
            }
            constraints=new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setReturningAttributes(new String[0]); //return no attrs
        }
        authzIdentity=(String)options.get(AUTHZ_IDENTITY);
        if(authzIdentity!=null&&
                authzIdentity.startsWith("{")&&authzIdentity.endsWith("}")){
            if(constraints!=null){
                authzIdentityAttr=
                        authzIdentity.substring(1,authzIdentity.length()-1);
                constraints.setReturningAttributes(
                        new String[]{authzIdentityAttr});
            }
            authzIdentity=null; // set later, from the specified attribute
        }
        // determine mode
        if(authcIdentity!=null){
            if(userFilter!=null){
                authFirst=true; // authentication-first mode
            }else{
                authOnly=true; // authentication-only mode
            }
        }
        if("false".equalsIgnoreCase((String)options.get("useSSL"))){
            useSSL=false;
            ldapEnvironment.remove(Context.SECURITY_PROTOCOL);
        }else{
            ldapEnvironment.put(Context.SECURITY_PROTOCOL,"ssl");
        }
        tryFirstPass=
                "true".equalsIgnoreCase((String)options.get("tryFirstPass"));
        useFirstPass=
                "true".equalsIgnoreCase((String)options.get("useFirstPass"));
        storePass="true".equalsIgnoreCase((String)options.get("storePass"));
        clearPass="true".equalsIgnoreCase((String)options.get("clearPass"));
        debug="true".equalsIgnoreCase((String)options.get("debug"));
        if(debug){
            if(authFirst){
                System.out.println("\t\t[LdapLoginModule] "+
                        "authentication-first mode; "+
                        (useSSL?"SSL enabled":"SSL disabled"));
            }else if(authOnly){
                System.out.println("\t\t[LdapLoginModule] "+
                        "authentication-only mode; "+
                        (useSSL?"SSL enabled":"SSL disabled"));
            }else{
                System.out.println("\t\t[LdapLoginModule] "+
                        "search-first mode; "+
                        (useSSL?"SSL enabled":"SSL disabled"));
            }
        }
    }

    public boolean login() throws LoginException{
        if(userProvider==null){
            throw new LoginException
                    ("Unable to locate the LDAP directory service");
        }
        if(debug){
            System.out.println("\t\t[LdapLoginModule] user provider: "+
                    userProvider);
        }
        // attempt the authentication
        if(tryFirstPass){
            try{
                // attempt the authentication by getting the
                // username and password from shared state
                attemptAuthentication(true);
                // authentication succeeded
                succeeded=true;
                if(debug){
                    System.out.println("\t\t[LdapLoginModule] "+
                            "tryFirstPass succeeded");
                }
                return true;
            }catch(LoginException le){
                // authentication failed -- try again below by prompting
                cleanState();
                if(debug){
                    System.out.println("\t\t[LdapLoginModule] "+
                            "tryFirstPass failed: "+le.toString());
                }
            }
        }else if(useFirstPass){
            try{
                // attempt the authentication by getting the
                // username and password from shared state
                attemptAuthentication(true);
                // authentication succeeded
                succeeded=true;
                if(debug){
                    System.out.println("\t\t[LdapLoginModule] "+
                            "useFirstPass succeeded");
                }
                return true;
            }catch(LoginException le){
                // authentication failed
                cleanState();
                if(debug){
                    System.out.println("\t\t[LdapLoginModule] "+
                            "useFirstPass failed");
                }
                throw le;
            }
        }
        // attempt the authentication by prompting for the username and pwd
        try{
            attemptAuthentication(false);
            // authentication succeeded
            succeeded=true;
            if(debug){
                System.out.println("\t\t[LdapLoginModule] "+
                        "authentication succeeded");
            }
            return true;
        }catch(LoginException le){
            cleanState();
            if(debug){
                System.out.println("\t\t[LdapLoginModule] "+
                        "authentication failed");
            }
            throw le;
        }
    }

    public boolean commit() throws LoginException{
        if(succeeded==false){
            return false;
        }else{
            if(subject.isReadOnly()){
                cleanState();
                throw new LoginException("Subject is read-only");
            }
            // add Principals to the Subject
            Set<Principal> principals=subject.getPrincipals();
            if(!principals.contains(ldapPrincipal)){
                principals.add(ldapPrincipal);
            }
            if(debug){
                System.out.println("\t\t[LdapLoginModule] "+
                        "added LdapPrincipal \""+
                        ldapPrincipal+
                        "\" to Subject");
            }
            if(!principals.contains(userPrincipal)){
                principals.add(userPrincipal);
            }
            if(debug){
                System.out.println("\t\t[LdapLoginModule] "+
                        "added UserPrincipal \""+
                        userPrincipal+
                        "\" to Subject");
            }
            if(authzPrincipal!=null&&
                    (!principals.contains(authzPrincipal))){
                principals.add(authzPrincipal);
                if(debug){
                    System.out.println("\t\t[LdapLoginModule] "+
                            "added UserPrincipal \""+
                            authzPrincipal+
                            "\" to Subject");
                }
            }
        }
        // in any case, clean out state
        cleanState();
        commitSucceeded=true;
        return true;
    }

    public boolean abort() throws LoginException{
        if(debug)
            System.out.println("\t\t[LdapLoginModule] "+
                    "aborted authentication");
        if(succeeded==false){
            return false;
        }else if(succeeded==true&&commitSucceeded==false){
            // Clean out state
            succeeded=false;
            cleanState();
            ldapPrincipal=null;
            userPrincipal=null;
            authzPrincipal=null;
        }else{
            // overall authentication succeeded and commit succeeded,
            // but someone else's commit failed
            logout();
        }
        return true;
    }

    public boolean logout() throws LoginException{
        if(subject.isReadOnly()){
            cleanState();
            throw new LoginException("Subject is read-only");
        }
        Set<Principal> principals=subject.getPrincipals();
        principals.remove(ldapPrincipal);
        principals.remove(userPrincipal);
        if(authzIdentity!=null){
            principals.remove(authzPrincipal);
        }
        // clean out state
        cleanState();
        succeeded=false;
        commitSucceeded=false;
        ldapPrincipal=null;
        userPrincipal=null;
        authzPrincipal=null;
        if(debug){
            System.out.println("\t\t[LdapLoginModule] logged out Subject");
        }
        return true;
    }

    private void attemptAuthentication(boolean getPasswdFromSharedState)
            throws LoginException{
        // first get the username and password
        getUsernamePassword(getPasswdFromSharedState);
        if(password==null||password.length==0){
            throw (LoginException)
                    new FailedLoginException("No password was supplied");
        }
        String dn="";
        if(authFirst||authOnly){
            String id=replaceUsernameToken(identityMatcher,authcIdentity);
            // Prepare to bind using user's username and password
            ldapEnvironment.put(Context.SECURITY_CREDENTIALS,password);
            ldapEnvironment.put(Context.SECURITY_PRINCIPAL,id);
            if(debug){
                System.out.println("\t\t[LdapLoginModule] "+
                        "attempting to authenticate user: "+username);
            }
            try{
                // Connect to the LDAP server (using simple bind)
                ctx=new InitialLdapContext(ldapEnvironment,null);
            }catch(NamingException e){
                throw (LoginException)
                        new FailedLoginException("Cannot bind to LDAP server")
                                .initCause(e);
            }
            // Authentication has succeeded
            // Locate the user's distinguished name
            if(userFilter!=null){
                dn=findUserDN(ctx);
            }else{
                dn=id;
            }
        }else{
            try{
                // Connect to the LDAP server (using anonymous bind)
                ctx=new InitialLdapContext(ldapEnvironment,null);
            }catch(NamingException e){
                throw (LoginException)
                        new FailedLoginException("Cannot connect to LDAP server")
                                .initCause(e);
            }
            // Locate the user's distinguished name
            dn=findUserDN(ctx);
            try{
                // Prepare to bind using user's distinguished name and password
                ctx.addToEnvironment(Context.SECURITY_AUTHENTICATION,"simple");
                ctx.addToEnvironment(Context.SECURITY_PRINCIPAL,dn);
                ctx.addToEnvironment(Context.SECURITY_CREDENTIALS,password);
                if(debug){
                    System.out.println("\t\t[LdapLoginModule] "+
                            "attempting to authenticate user: "+username);
                }
                // Connect to the LDAP server (using simple bind)
                ctx.reconnect(null);
                // Authentication has succeeded
            }catch(NamingException e){
                throw (LoginException)
                        new FailedLoginException("Cannot bind to LDAP server")
                                .initCause(e);
            }
        }
        // Save input as shared state only if authentication succeeded
        if(storePass&&
                !sharedState.containsKey(USERNAME_KEY)&&
                !sharedState.containsKey(PASSWORD_KEY)){
            sharedState.put(USERNAME_KEY,username);
            sharedState.put(PASSWORD_KEY,password);
        }
        // Create the user principals
        userPrincipal=new UserPrincipal(username);
        if(authzIdentity!=null){
            authzPrincipal=new UserPrincipal(authzIdentity);
        }
        try{
            ldapPrincipal=new LdapPrincipal(dn);
        }catch(InvalidNameException e){
            if(debug){
                System.out.println("\t\t[LdapLoginModule] "+
                        "cannot create LdapPrincipal: bad DN");
            }
            throw (LoginException)
                    new FailedLoginException("Cannot create LdapPrincipal")
                            .initCause(e);
        }
    }

    private String findUserDN(LdapContext ctx) throws LoginException{
        String userDN="";
        // Locate the user's LDAP entry
        if(userFilter!=null){
            if(debug){
                System.out.println("\t\t[LdapLoginModule] "+
                        "searching for entry belonging to user: "+username);
            }
        }else{
            if(debug){
                System.out.println("\t\t[LdapLoginModule] "+
                        "cannot search for entry belonging to user: "+username);
            }
            throw (LoginException)
                    new FailedLoginException("Cannot find user's LDAP entry");
        }
        try{
            NamingEnumeration<SearchResult> results=ctx.search("",
                    replaceUsernameToken(filterMatcher,userFilter),constraints);
            // Extract the distinguished name of the user's entry
            // (Use the first entry if more than one is returned)
            if(results.hasMore()){
                SearchResult entry=results.next();
                userDN=entry.getNameInNamespace();
                if(debug){
                    System.out.println("\t\t[LdapLoginModule] found entry: "+
                            userDN);
                }
                // Extract a value from user's authorization identity attribute
                if(authzIdentityAttr!=null){
                    Attribute attr=
                            entry.getAttributes().get(authzIdentityAttr);
                    if(attr!=null){
                        Object val=attr.get();
                        if(val instanceof String){
                            authzIdentity=(String)val;
                        }
                    }
                }
                results.close();
            }else{
                // Bad username
                if(debug){
                    System.out.println("\t\t[LdapLoginModule] user's entry "+
                            "not found");
                }
            }
        }catch(NamingException e){
            // ignore
        }
        if(userDN.equals("")){
            throw (LoginException)
                    new FailedLoginException("Cannot find user's LDAP entry");
        }else{
            return userDN;
        }
    }

    private String replaceUsernameToken(Matcher matcher,String string){
        return matcher!=null?matcher.replaceAll(username):string;
    }

    private void getUsernamePassword(boolean getPasswdFromSharedState)
            throws LoginException{
        if(getPasswdFromSharedState){
            // use the password saved by the first module in the stack
            username=(String)sharedState.get(USERNAME_KEY);
            password=(char[])sharedState.get(PASSWORD_KEY);
            return;
        }
        // prompt for a username and password
        if(callbackHandler==null)
            throw new LoginException("No CallbackHandler available "+
                    "to acquire authentication information from the user");
        Callback[] callbacks=new Callback[2];
        callbacks[0]=new NameCallback(rb.getString("username."));
        callbacks[1]=new PasswordCallback(rb.getString("password."),false);
        try{
            callbackHandler.handle(callbacks);
            username=((NameCallback)callbacks[0]).getName();
            char[] tmpPassword=((PasswordCallback)callbacks[1]).getPassword();
            password=new char[tmpPassword.length];
            System.arraycopy(tmpPassword,0,
                    password,0,tmpPassword.length);
            ((PasswordCallback)callbacks[1]).clearPassword();
        }catch(java.io.IOException ioe){
            throw new LoginException(ioe.toString());
        }catch(UnsupportedCallbackException uce){
            throw new LoginException("Error: "+uce.getCallback().toString()+
                    " not available to acquire authentication information"+
                    " from the user");
        }
    }

    private void cleanState(){
        username=null;
        if(password!=null){
            Arrays.fill(password,' ');
            password=null;
        }
        try{
            if(ctx!=null){
                ctx.close();
            }
        }catch(NamingException e){
            // ignore
        }
        ctx=null;
        if(clearPass){
            sharedState.remove(USERNAME_KEY);
            sharedState.remove(PASSWORD_KEY);
        }
    }
}
