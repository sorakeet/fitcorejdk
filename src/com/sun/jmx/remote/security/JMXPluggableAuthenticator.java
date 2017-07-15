/**
 * Copyright (c) 2004, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.remote.security;

import com.sun.jmx.remote.util.ClassLogger;
import com.sun.jmx.remote.util.EnvHelp;

import javax.management.remote.JMXAuthenticator;
import javax.security.auth.AuthPermission;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class JMXPluggableAuthenticator implements JMXAuthenticator{
    private static final String LOGIN_CONFIG_PROP=
            "jmx.remote.x.login.config";
    private static final String LOGIN_CONFIG_NAME="JMXPluggableAuthenticator";
    private static final String PASSWORD_FILE_PROP=
            "jmx.remote.x.password.file";
    private static final ClassLogger logger=
            new ClassLogger("javax.management.remote.misc",LOGIN_CONFIG_NAME);
    private LoginContext loginContext;
    private String username;
    private String password;
    public JMXPluggableAuthenticator(Map<?,?> env){
        String loginConfigName=null;
        String passwordFile=null;
        if(env!=null){
            loginConfigName=(String)env.get(LOGIN_CONFIG_PROP);
            passwordFile=(String)env.get(PASSWORD_FILE_PROP);
        }
        try{
            if(loginConfigName!=null){
                // use the supplied JAAS login configuration
                loginContext=
                        new LoginContext(loginConfigName,new JMXCallbackHandler());
            }else{
                // use the default JAAS login configuration (file-based)
                SecurityManager sm=System.getSecurityManager();
                if(sm!=null){
                    sm.checkPermission(
                            new AuthPermission("createLoginContext."+
                                    LOGIN_CONFIG_NAME));
                }
                final String pf=passwordFile;
                try{
                    loginContext=AccessController.doPrivileged(
                            new PrivilegedExceptionAction<LoginContext>(){
                                public LoginContext run() throws LoginException{
                                    return new LoginContext(
                                            LOGIN_CONFIG_NAME,
                                            null,
                                            new JMXCallbackHandler(),
                                            new FileLoginConfig(pf));
                                }
                            });
                }catch(PrivilegedActionException pae){
                    throw (LoginException)pae.getException();
                }
            }
        }catch(LoginException le){
            authenticationFailure("authenticate",le);
        }catch(SecurityException se){
            authenticationFailure("authenticate",se);
        }
    }

    private static void authenticationFailure(String method,
                                              Exception exception)
            throws SecurityException{
        String msg;
        SecurityException se;
        if(exception instanceof SecurityException){
            msg=exception.getMessage();
            se=(SecurityException)exception;
        }else{
            msg="Authentication failed! "+exception.getMessage();
            final SecurityException e=new SecurityException(msg);
            EnvHelp.initCause(e,exception);
            se=e;
        }
        logException(method,msg,se);
        throw se;
    }

    private static void logException(String method,
                                     String message,
                                     Exception e){
        if(logger.traceOn()){
            logger.trace(method,message);
        }
        if(logger.debugOn()){
            logger.debug(method,e);
        }
    }

    public Subject authenticate(Object credentials){
        // Verify that credentials is of type String[].
        //
        if(!(credentials instanceof String[])){
            // Special case for null so we get a more informative message
            if(credentials==null)
                authenticationFailure("authenticate","Credentials required");
            final String message=
                    "Credentials should be String[] instead of "+
                            credentials.getClass().getName();
            authenticationFailure("authenticate",message);
        }
        // Verify that the array contains two elements.
        //
        final String[] aCredentials=(String[])credentials;
        if(aCredentials.length!=2){
            final String message=
                    "Credentials should have 2 elements not "+
                            aCredentials.length;
            authenticationFailure("authenticate",message);
        }
        // Verify that username exists and the associated
        // password matches the one supplied by the client.
        //
        username=aCredentials[0];
        password=aCredentials[1];
        if(username==null||password==null){
            final String message="Username or password is null";
            authenticationFailure("authenticate",message);
        }
        // Perform authentication
        try{
            loginContext.login();
            final Subject subject=loginContext.getSubject();
            AccessController.doPrivileged(new PrivilegedAction<Void>(){
                public Void run(){
                    subject.setReadOnly();
                    return null;
                }
            });
            return subject;
        }catch(LoginException le){
            authenticationFailure("authenticate",le);
        }
        return null;
    }

    private static void authenticationFailure(String method,String message)
            throws SecurityException{
        final String msg="Authentication failed! "+message;
        final SecurityException e=new SecurityException(msg);
        logException(method,msg,e);
        throw e;
    }

    private static class FileLoginConfig extends Configuration{
        // The classname of the login module for file-based authentication
        private static final String FILE_LOGIN_MODULE=
                FileLoginModule.class.getName();
        // The option that identifies the password file to use
        private static final String PASSWORD_FILE_OPTION="passwordFile";
        // The JAAS configuration for file-based authentication
        private AppConfigurationEntry[] entries;

        public FileLoginConfig(String passwordFile){
            Map<String,String> options;
            if(passwordFile!=null){
                options=new HashMap<String,String>(1);
                options.put(PASSWORD_FILE_OPTION,passwordFile);
            }else{
                options=Collections.emptyMap();
            }
            entries=new AppConfigurationEntry[]{
                    new AppConfigurationEntry(FILE_LOGIN_MODULE,
                            AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                            options)
            };
        }

        public AppConfigurationEntry[] getAppConfigurationEntry(String name){
            return name.equals(LOGIN_CONFIG_NAME)?entries:null;
        }

        public void refresh(){
            // the configuration is fixed
        }
    }

    private final class JMXCallbackHandler implements CallbackHandler{
        public void handle(Callback[] callbacks)
                throws IOException, UnsupportedCallbackException{
            for(int i=0;i<callbacks.length;i++){
                if(callbacks[i] instanceof NameCallback){
                    ((NameCallback)callbacks[i]).setName(username);
                }else if(callbacks[i] instanceof PasswordCallback){
                    ((PasswordCallback)callbacks[i])
                            .setPassword(password.toCharArray());
                }else{
                    throw new UnsupportedCallbackException
                            (callbacks[i],"Unrecognized Callback");
                }
            }
        }
    }
}
