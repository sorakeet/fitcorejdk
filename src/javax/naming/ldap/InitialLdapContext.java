/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.ldap;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.NotContextException;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

public class InitialLdapContext extends InitialDirContext implements LdapContext{
    private static final String
            BIND_CONTROLS_PROPERTY="java.naming.ldap.control.connect";

    public InitialLdapContext() throws NamingException{
        super(null);
    }

    @SuppressWarnings("unchecked")
    public InitialLdapContext(Hashtable<?,?> environment,
                              Control[] connCtls)
            throws NamingException{
        super(true); // don't initialize yet
        // Clone environment since caller owns it.
        Hashtable<Object,Object> env=(environment==null)
                ?new Hashtable<>(11)
                :(Hashtable<Object,Object>)environment.clone();
        // Put connect controls into environment.  Copy them first since
        // caller owns the array.
        if(connCtls!=null){
            Control[] copy=new Control[connCtls.length];
            System.arraycopy(connCtls,0,copy,0,connCtls.length);
            env.put(BIND_CONTROLS_PROPERTY,copy);
        }
        // set version to LDAPv3
        env.put("java.naming.ldap.version","3");
        // Initialize with updated environment
        init(env);
    }

    public ExtendedResponse extendedOperation(ExtendedRequest request)
            throws NamingException{
        return getDefaultLdapInitCtx().extendedOperation(request);
    }
// LdapContext methods
// Most Javadoc is deferred to the LdapContext interface.

    private LdapContext getDefaultLdapInitCtx() throws NamingException{
        Context answer=getDefaultInitCtx();
        if(!(answer instanceof LdapContext)){
            if(answer==null){
                throw new NoInitialContextException();
            }else{
                throw new NotContextException(
                        "Not an instance of LdapContext");
            }
        }
        return (LdapContext)answer;
    }

    public LdapContext newInstance(Control[] reqCtls)
            throws NamingException{
        return getDefaultLdapInitCtx().newInstance(reqCtls);
    }

    public void reconnect(Control[] connCtls) throws NamingException{
        getDefaultLdapInitCtx().reconnect(connCtls);
    }

    public Control[] getConnectControls() throws NamingException{
        return getDefaultLdapInitCtx().getConnectControls();
    }

    public void setRequestControls(Control[] requestControls)
            throws NamingException{
        getDefaultLdapInitCtx().setRequestControls(requestControls);
    }

    public Control[] getRequestControls() throws NamingException{
        return getDefaultLdapInitCtx().getRequestControls();
    }

    public Control[] getResponseControls() throws NamingException{
        return getDefaultLdapInitCtx().getResponseControls();
    }
}
