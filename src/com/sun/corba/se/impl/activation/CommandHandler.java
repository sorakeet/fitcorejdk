/**
 * Copyright (c) 1997, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.activation;

import org.omg.CORBA.ORB;

import java.io.PrintStream;

public interface CommandHandler{
    public final static boolean shortHelp=true;
    public final static boolean longHelp=false;
    public final static boolean parseError=true;
    public final static boolean commandDone=false;

    String getCommandName();

    void printCommandHelp(PrintStream out,boolean helpType);

    boolean processCommand(String[] cmd,ORB orb,PrintStream out);
}
