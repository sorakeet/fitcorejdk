/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

import java.util.Hashtable;

public interface Context{
// public static final:  JLS says recommended style is to omit these modifiers
// because they are the default
    String INITIAL_CONTEXT_FACTORY="java.naming.factory.initial";
    String OBJECT_FACTORIES="java.naming.factory.object";
    String STATE_FACTORIES="java.naming.factory.state";
    String URL_PKG_PREFIXES="java.naming.factory.url.pkgs";
    String PROVIDER_URL="java.naming.provider.url";
    String DNS_URL="java.naming.dns.url";
    String AUTHORITATIVE="java.naming.authoritative";
    String BATCHSIZE="java.naming.batchsize";
    String REFERRAL="java.naming.referral";
    String SECURITY_PROTOCOL="java.naming.security.protocol";
    String SECURITY_AUTHENTICATION="java.naming.security.authentication";
    String SECURITY_PRINCIPAL="java.naming.security.principal";
    String SECURITY_CREDENTIALS="java.naming.security.credentials";
    String LANGUAGE="java.naming.language";
    String APPLET="java.naming.applet";

    public Object lookup(Name name) throws NamingException;

    public Object lookup(String name) throws NamingException;

    public void bind(Name name,Object obj) throws NamingException;

    public void bind(String name,Object obj) throws NamingException;

    public void rebind(Name name,Object obj) throws NamingException;

    public void rebind(String name,Object obj) throws NamingException;

    public void unbind(Name name) throws NamingException;

    public void unbind(String name) throws NamingException;

    public void rename(Name oldName,Name newName) throws NamingException;

    public void rename(String oldName,String newName) throws NamingException;

    public NamingEnumeration<NameClassPair> list(Name name)
            throws NamingException;

    public NamingEnumeration<NameClassPair> list(String name)
            throws NamingException;

    public NamingEnumeration<Binding> listBindings(Name name)
            throws NamingException;

    public NamingEnumeration<Binding> listBindings(String name)
            throws NamingException;

    public void destroySubcontext(Name name) throws NamingException;

    public void destroySubcontext(String name) throws NamingException;

    public Context createSubcontext(Name name) throws NamingException;

    public Context createSubcontext(String name) throws NamingException;

    public Object lookupLink(Name name) throws NamingException;

    public Object lookupLink(String name) throws NamingException;

    public NameParser getNameParser(Name name) throws NamingException;

    public NameParser getNameParser(String name) throws NamingException;

    public Name composeName(Name name,Name prefix)
            throws NamingException;

    public String composeName(String name,String prefix)
            throws NamingException;

    public Object addToEnvironment(String propName,Object propVal)
            throws NamingException;

    public Object removeFromEnvironment(String propName)
            throws NamingException;

    public Hashtable<?,?> getEnvironment() throws NamingException;

    public void close() throws NamingException;

    public String getNameInNamespace() throws NamingException;
};
