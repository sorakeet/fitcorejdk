/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.security.auth.module;

import com.sun.security.auth.UnixNumericGroupPrincipal;
import com.sun.security.auth.UnixNumericUserPrincipal;
import com.sun.security.auth.UnixPrincipal;

import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.LinkedList;
import java.util.Map;
import java.util.ResourceBundle;

@jdk.Exported
public class JndiLoginModule implements LoginModule{
    private static final ResourceBundle rb=AccessController.doPrivileged(
            new PrivilegedAction<ResourceBundle>(){
                public ResourceBundle run(){
                    return ResourceBundle.getBundle(
                            "sun.security.util.AuthResources");
                }
            }
    );
    private static final String CRYPT="{crypt}";
    private static final String USER_PWD="userPassword";
    private static final String USER_UID="uidNumber";
    private static final String USER_GID="gidNumber";
    private static final String GROUP_ID="gidNumber";
    private static final String NAME="javax.security.auth.login.name";
    private static final String PWD="javax.security.auth.login.password";
    public final String USER_PROVIDER="user.provider.url";
    public final String GROUP_PROVIDER="group.provider.url";
    DirContext ctx;
    // configurable options
    private boolean debug=false;
    private boolean strongDebug=false;
    private String userProvider;
    private String groupProvider;
    private boolean useFirstPass=false;
    private boolean tryFirstPass=false;
    private boolean storePass=false;
    private boolean clearPass=false;
    // the authentication status
    private boolean succeeded=false;
    private boolean commitSucceeded=false;
    // username, password, and JNDI context
    private String username;
    private char[] password;
    // the user (assume it is a UnixPrincipal)
    private UnixPrincipal userPrincipal;
    private UnixNumericUserPrincipal UIDPrincipal;
    private UnixNumericGroupPrincipal GIDPrincipal;
    private LinkedList<UnixNumericGroupPrincipal> supplementaryGroups=
            new LinkedList<>();
    // initial state
    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map<String,Object> sharedState;
    private Map<String,?> options;

    // Unchecked warning from (Map<String, Object>)sharedState is safe
    // since javax.security.auth.login.LoginContext passes a raw HashMap.
    // Unchecked warnings from options.get(String) are safe since we are
    // passing known keys.
    @SuppressWarnings("unchecked")
    public void initialize(Subject subject,CallbackHandler callbackHandler,
                           Map<String,?> sharedState,
                           Map<String,?> options){
        this.subject=subject;
        this.callbackHandler=callbackHandler;
        this.sharedState=(Map<String,Object>)sharedState;
        this.options=options;
        // initialize any configured options
        debug="true".equalsIgnoreCase((String)options.get("debug"));
        strongDebug=
                "true".equalsIgnoreCase((String)options.get("strongDebug"));
        userProvider=(String)options.get(USER_PROVIDER);
        groupProvider=(String)options.get(GROUP_PROVIDER);
        tryFirstPass=
                "true".equalsIgnoreCase((String)options.get("tryFirstPass"));
        useFirstPass=
                "true".equalsIgnoreCase((String)options.get("useFirstPass"));
        storePass=
                "true".equalsIgnoreCase((String)options.get("storePass"));
        clearPass=
                "true".equalsIgnoreCase((String)options.get("clearPass"));
    }

    public boolean login() throws LoginException{
        if(userProvider==null){
            throw new LoginException
                    ("Error: Unable to locate JNDI user provider");
        }
        if(groupProvider==null){
            throw new LoginException
                    ("Error: Unable to locate JNDI group provider");
        }
        if(debug){
            System.out.println("\t\t[JndiLoginModule] user provider: "+
                    userProvider);
            System.out.println("\t\t[JndiLoginModule] group provider: "+
                    groupProvider);
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
                    System.out.println("\t\t[JndiLoginModule] "+
                            "tryFirstPass succeeded");
                }
                return true;
            }catch(LoginException le){
                // authentication failed -- try again below by prompting
                cleanState();
                if(debug){
                    System.out.println("\t\t[JndiLoginModule] "+
                            "tryFirstPass failed with:"+
                            le.toString());
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
                    System.out.println("\t\t[JndiLoginModule] "+
                            "useFirstPass succeeded");
                }
                return true;
            }catch(LoginException le){
                // authentication failed
                cleanState();
                if(debug){
                    System.out.println("\t\t[JndiLoginModule] "+
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
                System.out.println("\t\t[JndiLoginModule] "+
                        "regular authentication succeeded");
            }
            return true;
        }catch(LoginException le){
            cleanState();
            if(debug){
                System.out.println("\t\t[JndiLoginModule] "+
                        "regular authentication failed");
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
                throw new LoginException("Subject is Readonly");
            }
            // add Principals to the Subject
            if(!subject.getPrincipals().contains(userPrincipal))
                subject.getPrincipals().add(userPrincipal);
            if(!subject.getPrincipals().contains(UIDPrincipal))
                subject.getPrincipals().add(UIDPrincipal);
            if(!subject.getPrincipals().contains(GIDPrincipal))
                subject.getPrincipals().add(GIDPrincipal);
            for(int i=0;i<supplementaryGroups.size();i++){
                if(!subject.getPrincipals().contains
                        (supplementaryGroups.get(i)))
                    subject.getPrincipals().add(supplementaryGroups.get(i));
            }
            if(debug){
                System.out.println("\t\t[JndiLoginModule]: "+
                        "added UnixPrincipal,");
                System.out.println("\t\t\t\tUnixNumericUserPrincipal,");
                System.out.println("\t\t\t\tUnixNumericGroupPrincipal(s),");
                System.out.println("\t\t\t to Subject");
            }
        }
        // in any case, clean out state
        cleanState();
        commitSucceeded=true;
        return true;
    }

    public boolean abort() throws LoginException{
        if(debug)
            System.out.println("\t\t[JndiLoginModule]: "+
                    "aborted authentication failed");
        if(succeeded==false){
            return false;
        }else if(succeeded==true&&commitSucceeded==false){
            // Clean out state
            succeeded=false;
            cleanState();
            userPrincipal=null;
            UIDPrincipal=null;
            GIDPrincipal=null;
            supplementaryGroups=new LinkedList<UnixNumericGroupPrincipal>();
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
            throw new LoginException("Subject is Readonly");
        }
        subject.getPrincipals().remove(userPrincipal);
        subject.getPrincipals().remove(UIDPrincipal);
        subject.getPrincipals().remove(GIDPrincipal);
        for(int i=0;i<supplementaryGroups.size();i++){
            subject.getPrincipals().remove(supplementaryGroups.get(i));
        }
        // clean out state
        cleanState();
        succeeded=false;
        commitSucceeded=false;
        userPrincipal=null;
        UIDPrincipal=null;
        GIDPrincipal=null;
        supplementaryGroups=new LinkedList<UnixNumericGroupPrincipal>();
        if(debug){
            System.out.println("\t\t[JndiLoginModule]: "+
                    "logged out Subject");
        }
        return true;
    }

    private void attemptAuthentication(boolean getPasswdFromSharedState)
            throws LoginException{
        String encryptedPassword=null;
        // first get the username and password
        getUsernamePassword(getPasswdFromSharedState);
        try{
            // get the user's passwd entry from the user provider URL
            InitialContext iCtx=new InitialContext();
            ctx=(DirContext)iCtx.lookup(userProvider);
            /**
             SearchControls controls = new SearchControls
             (SearchControls.ONELEVEL_SCOPE,
             0,
             5000,
             new String[] { USER_PWD },
             false,
             false);
             */
            SearchControls controls=new SearchControls();
            NamingEnumeration<SearchResult> ne=ctx.search("",
                    "(uid="+username+")",
                    controls);
            if(ne.hasMore()){
                SearchResult result=ne.next();
                Attributes attributes=result.getAttributes();
                // get the password
                // this module works only if the LDAP directory server
                // is configured to permit read access to the userPassword
                // attribute. The directory administrator need to grant
                // this access.
                //
                // A workaround would be to make the server do authentication
                // by setting the Context.SECURITY_PRINCIPAL
                // and Context.SECURITY_CREDENTIALS property.
                // However, this would make it not work with systems that
                // don't do authentication at the server (like NIS).
                //
                // Setting the SECURITY_* properties and using "simple"
                // authentication for LDAP is recommended only for secure
                // channels. For nonsecure channels, SSL is recommended.
                Attribute pwd=attributes.get(USER_PWD);
                String encryptedPwd=new String((byte[])pwd.get(),"UTF8");
                encryptedPassword=encryptedPwd.substring(CRYPT.length());
                // check the password
                if(verifyPassword
                        (encryptedPassword,new String(password))==true){
                    // authentication succeeded
                    if(debug)
                        System.out.println("\t\t[JndiLoginModule] "+
                                "attemptAuthentication() succeeded");
                }else{
                    // authentication failed
                    if(debug)
                        System.out.println("\t\t[JndiLoginModule] "+
                                "attemptAuthentication() failed");
                    throw new FailedLoginException("Login incorrect");
                }
                // save input as shared state only if
                // authentication succeeded
                if(storePass&&
                        !sharedState.containsKey(NAME)&&
                        !sharedState.containsKey(PWD)){
                    sharedState.put(NAME,username);
                    sharedState.put(PWD,password);
                }
                // create the user principal
                userPrincipal=new UnixPrincipal(username);
                // get the UID
                Attribute uid=attributes.get(USER_UID);
                String uidNumber=(String)uid.get();
                UIDPrincipal=new UnixNumericUserPrincipal(uidNumber);
                if(debug&&uidNumber!=null){
                    System.out.println("\t\t[JndiLoginModule] "+
                            "user: '"+username+"' has UID: "+
                            uidNumber);
                }
                // get the GID
                Attribute gid=attributes.get(USER_GID);
                String gidNumber=(String)gid.get();
                GIDPrincipal=new UnixNumericGroupPrincipal
                        (gidNumber,true);
                if(debug&&gidNumber!=null){
                    System.out.println("\t\t[JndiLoginModule] "+
                            "user: '"+username+"' has GID: "+
                            gidNumber);
                }
                // get the supplementary groups from the group provider URL
                ctx=(DirContext)iCtx.lookup(groupProvider);
                ne=ctx.search("",new BasicAttributes("memberUid",username));
                while(ne.hasMore()){
                    result=ne.next();
                    attributes=result.getAttributes();
                    gid=attributes.get(GROUP_ID);
                    String suppGid=(String)gid.get();
                    if(!gidNumber.equals(suppGid)){
                        UnixNumericGroupPrincipal suppPrincipal=
                                new UnixNumericGroupPrincipal(suppGid,false);
                        supplementaryGroups.add(suppPrincipal);
                        if(debug&&suppGid!=null){
                            System.out.println("\t\t[JndiLoginModule] "+
                                    "user: '"+username+
                                    "' has Supplementary Group: "+
                                    suppGid);
                        }
                    }
                }
            }else{
                // bad username
                if(debug){
                    System.out.println("\t\t[JndiLoginModule]: User not found");
                }
                throw new FailedLoginException("User not found");
            }
        }catch(NamingException ne){
            // bad username
            if(debug){
                System.out.println("\t\t[JndiLoginModule]:  User not found");
                ne.printStackTrace();
            }
            throw new FailedLoginException("User not found");
        }catch(java.io.UnsupportedEncodingException uee){
            // password stored in incorrect format
            if(debug){
                System.out.println("\t\t[JndiLoginModule]:  "+
                        "password incorrectly encoded");
                uee.printStackTrace();
            }
            throw new LoginException("Login failure due to incorrect "+
                    "password encoding in the password database");
        }
        // authentication succeeded
    }

    private void getUsernamePassword(boolean getPasswdFromSharedState)
            throws LoginException{
        if(getPasswdFromSharedState){
            // use the password saved by the first module in the stack
            username=(String)sharedState.get(NAME);
            password=(char[])sharedState.get(PWD);
            return;
        }
        // prompt for a username and password
        if(callbackHandler==null)
            throw new LoginException("Error: no CallbackHandler available "+
                    "to garner authentication information from the user");
        String protocol=userProvider.substring(0,userProvider.indexOf(":"));
        Callback[] callbacks=new Callback[2];
        callbacks[0]=new NameCallback(protocol+" "
                +rb.getString("username."));
        callbacks[1]=new PasswordCallback(protocol+" "+
                rb.getString("password."),
                false);
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
                    " not available to garner authentication information "+
                    "from the user");
        }
        // print debugging information
        if(strongDebug){
            System.out.println("\t\t[JndiLoginModule] "+
                    "user entered username: "+
                    username);
            System.out.print("\t\t[JndiLoginModule] "+
                    "user entered password: ");
            for(int i=0;i<password.length;i++)
                System.out.print(password[i]);
            System.out.println();
        }
    }

    private boolean verifyPassword(String encryptedPassword,String password){
        if(encryptedPassword==null)
            return false;
        Crypt c=new Crypt();
        try{
            byte oldCrypt[]=encryptedPassword.getBytes("UTF8");
            byte newCrypt[]=c.crypt(password.getBytes("UTF8"),
                    oldCrypt);
            if(newCrypt.length!=oldCrypt.length)
                return false;
            for(int i=0;i<newCrypt.length;i++){
                if(oldCrypt[i]!=newCrypt[i])
                    return false;
            }
        }catch(java.io.UnsupportedEncodingException uee){
            // cannot happen, but return false just to be safe
            return false;
        }
        return true;
    }

    private void cleanState(){
        username=null;
        if(password!=null){
            for(int i=0;i<password.length;i++)
                password[i]=' ';
            password=null;
        }
        ctx=null;
        if(clearPass){
            sharedState.remove(NAME);
            sharedState.remove(PWD);
        }
    }
}
