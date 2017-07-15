/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.directory;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

public interface DirContext extends Context{
    public final static int ADD_ATTRIBUTE=1;
    public final static int REPLACE_ATTRIBUTE=2;
    public final static int REMOVE_ATTRIBUTE=3;

    public Attributes getAttributes(Name name) throws NamingException;

    public Attributes getAttributes(String name) throws NamingException;

    public Attributes getAttributes(Name name,String[] attrIds)
            throws NamingException;

    public Attributes getAttributes(String name,String[] attrIds)
            throws NamingException;

    public void modifyAttributes(Name name,int mod_op,Attributes attrs)
            throws NamingException;

    public void modifyAttributes(String name,int mod_op,Attributes attrs)
            throws NamingException;

    public void modifyAttributes(Name name,ModificationItem[] mods)
            throws NamingException;

    public void modifyAttributes(String name,ModificationItem[] mods)
            throws NamingException;

    public void bind(Name name,Object obj,Attributes attrs)
            throws NamingException;

    public void bind(String name,Object obj,Attributes attrs)
            throws NamingException;

    public void rebind(Name name,Object obj,Attributes attrs)
            throws NamingException;

    public void rebind(String name,Object obj,Attributes attrs)
            throws NamingException;

    public DirContext createSubcontext(Name name,Attributes attrs)
            throws NamingException;

    public DirContext createSubcontext(String name,Attributes attrs)
            throws NamingException;
// -------------------- schema operations

    public DirContext getSchema(Name name) throws NamingException;

    public DirContext getSchema(String name) throws NamingException;

    public DirContext getSchemaClassDefinition(Name name)
            throws NamingException;

    public DirContext getSchemaClassDefinition(String name)
            throws NamingException;
// -------------------- search operations

    public NamingEnumeration<SearchResult>
    search(Name name,
           Attributes matchingAttributes,
           String[] attributesToReturn)
            throws NamingException;

    public NamingEnumeration<SearchResult>
    search(String name,
           Attributes matchingAttributes,
           String[] attributesToReturn)
            throws NamingException;

    public NamingEnumeration<SearchResult>
    search(Name name,Attributes matchingAttributes)
            throws NamingException;

    public NamingEnumeration<SearchResult>
    search(String name,Attributes matchingAttributes)
            throws NamingException;

    public NamingEnumeration<SearchResult>
    search(Name name,
           String filter,
           SearchControls cons)
            throws NamingException;

    public NamingEnumeration<SearchResult>
    search(String name,
           String filter,
           SearchControls cons)
            throws NamingException;

    public NamingEnumeration<SearchResult>
    search(Name name,
           String filterExpr,
           Object[] filterArgs,
           SearchControls cons)
            throws NamingException;

    public NamingEnumeration<SearchResult>
    search(String name,
           String filterExpr,
           Object[] filterArgs,
           SearchControls cons)
            throws NamingException;
}
