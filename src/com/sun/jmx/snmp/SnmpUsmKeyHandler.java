/**
 * Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public interface SnmpUsmKeyHandler{
    public static int DES_KEY_SIZE=16;
    public static int DES_DELTA_SIZE=16;

    public byte[] password_to_key(String algoName,String password) throws IllegalArgumentException;

    public byte[] localizeAuthKey(String algoName,byte[] key,SnmpEngineId engineId) throws IllegalArgumentException;

    public byte[] localizePrivKey(String algoName,byte[] key,SnmpEngineId engineId,int keysize) throws IllegalArgumentException;

    public byte[] calculateAuthDelta(String algoName,byte[] oldKey,byte[] newKey,byte[] random) throws IllegalArgumentException;

    public byte[] calculatePrivDelta(String algoName,byte[] oldKey,byte[] newKey,byte[] random,int deltaSize) throws IllegalArgumentException;
}
