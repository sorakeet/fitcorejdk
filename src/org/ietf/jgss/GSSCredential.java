/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.ietf.jgss;

public interface GSSCredential extends Cloneable{
    public static final int INITIATE_AND_ACCEPT=0;
    public static final int INITIATE_ONLY=1;
    public static final int ACCEPT_ONLY=2;
    public static final int DEFAULT_LIFETIME=0;
    public static final int INDEFINITE_LIFETIME=Integer.MAX_VALUE;

    public void dispose() throws GSSException;

    public GSSName getName() throws GSSException;

    public GSSName getName(Oid mech) throws GSSException;

    public int getRemainingLifetime() throws GSSException;

    public int getRemainingInitLifetime(Oid mech) throws GSSException;

    public int getRemainingAcceptLifetime(Oid mech) throws GSSException;

    public int getUsage() throws GSSException;

    public int getUsage(Oid mech) throws GSSException;

    public Oid[] getMechs() throws GSSException;

    public void add(GSSName name,int initLifetime,int acceptLifetime,
                    Oid mech,int usage) throws GSSException;

    public int hashCode();

    public boolean equals(Object another);
}
