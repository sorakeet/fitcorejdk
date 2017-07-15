/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth.login;

import sun.security.util.PendingException;
import sun.security.util.ResourcesMgr;

import javax.security.auth.AuthPermission;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class LoginContext{
    private static final String INIT_METHOD="initialize";
    private static final String LOGIN_METHOD="login";
    private static final String COMMIT_METHOD="commit";
    private static final String ABORT_METHOD="abort";
    private static final String LOGOUT_METHOD="logout";
    private static final String OTHER="other";
    private static final String DEFAULT_HANDLER=
            "auth.login.defaultCallbackHandler";
    private static final Class<?>[] PARAMS={};
    private static final sun.security.util.Debug debug=
            sun.security.util.Debug.getInstance("logincontext","\t[LoginContext]");
    private Subject subject=null;
    private boolean subjectProvided=false;
    private boolean loginSucceeded=false;
    private CallbackHandler callbackHandler;
    private Map<String,?> state=new HashMap<String,Object>();
    private Configuration config;
    private AccessControlContext creatorAcc=null;  // customized config only
    private ModuleInfo[] moduleStack;
    private ClassLoader contextClassLoader=null;
    // state saved in the event a user-specified asynchronous exception
    // was specified and thrown
    private int moduleIndex=0;
    private LoginException firstError=null;
    private LoginException firstRequiredError=null;
    private boolean success=false;

    public LoginContext(String name) throws LoginException{
        init(name);
        loadDefaultCallbackHandler();
    }

    private void init(String name) throws LoginException{
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null&&creatorAcc==null){
            sm.checkPermission(new AuthPermission
                    ("createLoginContext."+name));
        }
        if(name==null)
            throw new LoginException
                    (ResourcesMgr.getString("Invalid.null.input.name"));
        // get the Configuration
        if(config==null){
            config=AccessController.doPrivileged
                    (new java.security.PrivilegedAction<Configuration>(){
                        public Configuration run(){
                            return Configuration.getConfiguration();
                        }
                    });
        }
        // get the LoginModules configured for this application
        AppConfigurationEntry[] entries=config.getAppConfigurationEntry(name);
        if(entries==null){
            if(sm!=null&&creatorAcc==null){
                sm.checkPermission(new AuthPermission
                        ("createLoginContext."+OTHER));
            }
            entries=config.getAppConfigurationEntry(OTHER);
            if(entries==null){
                MessageFormat form=new MessageFormat(ResourcesMgr.getString
                        ("No.LoginModules.configured.for.name"));
                Object[] source={name};
                throw new LoginException(form.format(source));
            }
        }
        moduleStack=new ModuleInfo[entries.length];
        for(int i=0;i<entries.length;i++){
            // clone returned array
            moduleStack[i]=new ModuleInfo
                    (new AppConfigurationEntry
                            (entries[i].getLoginModuleName(),
                                    entries[i].getControlFlag(),
                                    entries[i].getOptions()),
                            null);
        }
        contextClassLoader=AccessController.doPrivileged
                (new java.security.PrivilegedAction<ClassLoader>(){
                    public ClassLoader run(){
                        ClassLoader loader=
                                Thread.currentThread().getContextClassLoader();
                        if(loader==null){
                            // Don't use bootstrap class loader directly to ensure
                            // proper package access control!
                            loader=ClassLoader.getSystemClassLoader();
                        }
                        return loader;
                    }
                });
    }

    private void loadDefaultCallbackHandler() throws LoginException{
        // get the default handler class
        try{
            final ClassLoader finalLoader=contextClassLoader;
            this.callbackHandler=AccessController.doPrivileged(
                    new java.security.PrivilegedExceptionAction<CallbackHandler>(){
                        public CallbackHandler run() throws Exception{
                            String defaultHandler=java.security.Security.getProperty
                                    (DEFAULT_HANDLER);
                            if(defaultHandler==null||defaultHandler.length()==0)
                                return null;
                            Class<? extends CallbackHandler> c=Class.forName(
                                    defaultHandler,true,
                                    finalLoader).asSubclass(CallbackHandler.class);
                            return c.newInstance();
                        }
                    });
        }catch(java.security.PrivilegedActionException pae){
            throw new LoginException(pae.getException().toString());
        }
        // secure it with the caller's ACC
        if(this.callbackHandler!=null&&creatorAcc==null){
            this.callbackHandler=new SecureCallbackHandler
                    (AccessController.getContext(),
                            this.callbackHandler);
        }
    }

    public LoginContext(String name,CallbackHandler callbackHandler)
            throws LoginException{
        init(name);
        if(callbackHandler==null)
            throw new LoginException(ResourcesMgr.getString
                    ("invalid.null.CallbackHandler.provided"));
        this.callbackHandler=new SecureCallbackHandler
                (AccessController.getContext(),
                        callbackHandler);
    }

    public LoginContext(String name,Subject subject,
                        CallbackHandler callbackHandler) throws LoginException{
        this(name,subject);
        if(callbackHandler==null)
            throw new LoginException(ResourcesMgr.getString
                    ("invalid.null.CallbackHandler.provided"));
        this.callbackHandler=new SecureCallbackHandler
                (AccessController.getContext(),
                        callbackHandler);
    }

    public LoginContext(String name,Subject subject)
            throws LoginException{
        init(name);
        if(subject==null)
            throw new LoginException
                    (ResourcesMgr.getString("invalid.null.Subject.provided"));
        this.subject=subject;
        subjectProvided=true;
        loadDefaultCallbackHandler();
    }

    public LoginContext(String name,Subject subject,
                        CallbackHandler callbackHandler,
                        Configuration config) throws LoginException{
        this.config=config;
        if(config!=null){
            creatorAcc=AccessController.getContext();
        }
        init(name);
        if(subject!=null){
            this.subject=subject;
            subjectProvided=true;
        }
        if(callbackHandler==null){
            loadDefaultCallbackHandler();
        }else if(creatorAcc==null){
            this.callbackHandler=new SecureCallbackHandler
                    (AccessController.getContext(),
                            callbackHandler);
        }else{
            this.callbackHandler=callbackHandler;
        }
    }

    public void login() throws LoginException{
        loginSucceeded=false;
        if(subject==null){
            subject=new Subject();
        }
        try{
            // module invoked in doPrivileged
            invokePriv(LOGIN_METHOD);
            invokePriv(COMMIT_METHOD);
            loginSucceeded=true;
        }catch(LoginException le){
            try{
                invokePriv(ABORT_METHOD);
            }catch(LoginException le2){
                throw le;
            }
            throw le;
        }
    }

    private void invokePriv(final String methodName) throws LoginException{
        try{
            AccessController.doPrivileged
                    (new java.security.PrivilegedExceptionAction<Void>(){
                        public Void run() throws LoginException{
                            invoke(methodName);
                            return null;
                        }
                    },creatorAcc);
        }catch(java.security.PrivilegedActionException pae){
            throw (LoginException)pae.getException();
        }
    }

    private void invoke(String methodName) throws LoginException{
        // start at moduleIndex
        // - this can only be non-zero if methodName is LOGIN_METHOD
        for(int i=moduleIndex;i<moduleStack.length;i++,moduleIndex++){
            try{
                int mIndex=0;
                Method[] methods=null;
                if(moduleStack[i].module!=null){
                    methods=moduleStack[i].module.getClass().getMethods();
                }else{
                    // instantiate the LoginModule
                    //
                    // Allow any object to be a LoginModule as long as it
                    // conforms to the interface.
                    Class<?> c=Class.forName(
                            moduleStack[i].entry.getLoginModuleName(),
                            true,
                            contextClassLoader);
                    Constructor<?> constructor=c.getConstructor(PARAMS);
                    Object[] args={};
                    moduleStack[i].module=constructor.newInstance(args);
                    // call the LoginModule's initialize method
                    methods=moduleStack[i].module.getClass().getMethods();
                    for(mIndex=0;mIndex<methods.length;mIndex++){
                        if(methods[mIndex].getName().equals(INIT_METHOD)){
                            break;
                        }
                    }
                    Object[] initArgs={subject,
                            callbackHandler,
                            state,
                            moduleStack[i].entry.getOptions()};
                    // invoke the LoginModule initialize method
                    //
                    // Throws ArrayIndexOutOfBoundsException if no such
                    // method defined.  May improve to use LoginException in
                    // the future.
                    methods[mIndex].invoke(moduleStack[i].module,initArgs);
                }
                // find the requested method in the LoginModule
                for(mIndex=0;mIndex<methods.length;mIndex++){
                    if(methods[mIndex].getName().equals(methodName)){
                        break;
                    }
                }
                // set up the arguments to be passed to the LoginModule method
                Object[] args={};
                // invoke the LoginModule method
                //
                // Throws ArrayIndexOutOfBoundsException if no such
                // method defined.  May improve to use LoginException in
                // the future.
                boolean status=((Boolean)methods[mIndex].invoke
                        (moduleStack[i].module,args)).booleanValue();
                if(status==true){
                    // if SUFFICIENT, return if no prior REQUIRED errors
                    if(!methodName.equals(ABORT_METHOD)&&
                            !methodName.equals(LOGOUT_METHOD)&&
                            moduleStack[i].entry.getControlFlag()==
                                    AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT&&
                            firstRequiredError==null){
                        // clear state
                        clearState();
                        if(debug!=null)
                            debug.println(methodName+" SUFFICIENT success");
                        return;
                    }
                    if(debug!=null)
                        debug.println(methodName+" success");
                    success=true;
                }else{
                    if(debug!=null)
                        debug.println(methodName+" ignored");
                }
            }catch(NoSuchMethodException nsme){
                MessageFormat form=new MessageFormat(ResourcesMgr.getString
                        ("unable.to.instantiate.LoginModule.module.because.it.does.not.provide.a.no.argument.constructor"));
                Object[] source={moduleStack[i].entry.getLoginModuleName()};
                throwException(null,new LoginException(form.format(source)));
            }catch(InstantiationException ie){
                throwException(null,new LoginException(ResourcesMgr.getString
                        ("unable.to.instantiate.LoginModule.")+
                        ie.getMessage()));
            }catch(ClassNotFoundException cnfe){
                throwException(null,new LoginException(ResourcesMgr.getString
                        ("unable.to.find.LoginModule.class.")+
                        cnfe.getMessage()));
            }catch(IllegalAccessException iae){
                throwException(null,new LoginException(ResourcesMgr.getString
                        ("unable.to.access.LoginModule.")+
                        iae.getMessage()));
            }catch(InvocationTargetException ite){
                // failure cases
                LoginException le;
                if(ite.getCause() instanceof PendingException&&
                        methodName.equals(LOGIN_METHOD)){
                    // XXX
                    //
                    // if a module's LOGIN_METHOD threw a PendingException
                    // then immediately throw it.
                    //
                    // when LoginContext is called again,
                    // the module that threw the exception is invoked first
                    // (the module list is not invoked from the start).
                    // previously thrown exception state is still present.
                    //
                    // it is assumed that the module which threw
                    // the exception can have its
                    // LOGIN_METHOD invoked twice in a row
                    // without any commit/abort in between.
                    //
                    // in all cases when LoginContext returns
                    // (either via natural return or by throwing an exception)
                    // we need to call clearState before returning.
                    // the only time that is not true is in this case -
                    // do not call throwException here.
                    throw (PendingException)ite.getCause();
                }else if(ite.getCause() instanceof LoginException){
                    le=(LoginException)ite.getCause();
                }else if(ite.getCause() instanceof SecurityException){
                    // do not want privacy leak
                    // (e.g., sensitive file path in exception msg)
                    le=new LoginException("Security Exception");
                    le.initCause(new SecurityException());
                    if(debug!=null){
                        debug.println
                                ("original security exception with detail msg "+
                                        "replaced by new exception with empty detail msg");
                        debug.println("original security exception: "+
                                ite.getCause().toString());
                    }
                }else{
                    // capture an unexpected LoginModule exception
                    java.io.StringWriter sw=new java.io.StringWriter();
                    ite.getCause().printStackTrace
                            (new java.io.PrintWriter(sw));
                    sw.flush();
                    le=new LoginException(sw.toString());
                }
                if(moduleStack[i].entry.getControlFlag()==
                        AppConfigurationEntry.LoginModuleControlFlag.REQUISITE){
                    if(debug!=null)
                        debug.println(methodName+" REQUISITE failure");
                    // if REQUISITE, then immediately throw an exception
                    if(methodName.equals(ABORT_METHOD)||
                            methodName.equals(LOGOUT_METHOD)){
                        if(firstRequiredError==null)
                            firstRequiredError=le;
                    }else{
                        throwException(firstRequiredError,le);
                    }
                }else if(moduleStack[i].entry.getControlFlag()==
                        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED){
                    if(debug!=null)
                        debug.println(methodName+" REQUIRED failure");
                    // mark down that a REQUIRED module failed
                    if(firstRequiredError==null)
                        firstRequiredError=le;
                }else{
                    if(debug!=null)
                        debug.println(methodName+" OPTIONAL failure");
                    // mark down that an OPTIONAL module failed
                    if(firstError==null)
                        firstError=le;
                }
            }
        }
        // we went thru all the LoginModules.
        if(firstRequiredError!=null){
            // a REQUIRED module failed -- return the error
            throwException(firstRequiredError,null);
        }else if(success==false&&firstError!=null){
            // no module succeeded -- return the first error
            throwException(firstError,null);
        }else if(success==false){
            // no module succeeded -- all modules were IGNORED
            throwException(new LoginException
                            (ResourcesMgr.getString("Login.Failure.all.modules.ignored")),
                    null);
        }else{
            // success
            clearState();
            return;
        }
    }

    private void clearState(){
        moduleIndex=0;
        firstError=null;
        firstRequiredError=null;
        success=false;
    }

    private void throwException(LoginException originalError,LoginException le)
            throws LoginException{
        // first clear state
        clearState();
        // throw the exception
        LoginException error=(originalError!=null)?originalError:le;
        throw error;
    }

    public void logout() throws LoginException{
        if(subject==null){
            throw new LoginException(ResourcesMgr.getString
                    ("null.subject.logout.called.before.login"));
        }
        // module invoked in doPrivileged
        invokePriv(LOGOUT_METHOD);
    }

    public Subject getSubject(){
        if(!loginSucceeded&&!subjectProvided)
            return null;
        return subject;
    }

    private static class SecureCallbackHandler implements CallbackHandler{
        private final AccessControlContext acc;
        private final CallbackHandler ch;

        SecureCallbackHandler(AccessControlContext acc,
                              CallbackHandler ch){
            this.acc=acc;
            this.ch=ch;
        }

        public void handle(final Callback[] callbacks)
                throws java.io.IOException, UnsupportedCallbackException{
            try{
                AccessController.doPrivileged
                        (new java.security.PrivilegedExceptionAction<Void>(){
                            public Void run() throws java.io.IOException,
                                    UnsupportedCallbackException{
                                ch.handle(callbacks);
                                return null;
                            }
                        },acc);
            }catch(java.security.PrivilegedActionException pae){
                if(pae.getException() instanceof java.io.IOException){
                    throw (java.io.IOException)pae.getException();
                }else{
                    throw (UnsupportedCallbackException)pae.getException();
                }
            }
        }
    }

    private static class ModuleInfo{
        AppConfigurationEntry entry;
        Object module;

        ModuleInfo(AppConfigurationEntry newEntry,Object newModule){
            this.entry=newEntry;
            this.module=newModule;
        }
    }
}
