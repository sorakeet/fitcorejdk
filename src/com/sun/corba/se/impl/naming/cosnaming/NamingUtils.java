/**
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.naming.cosnaming;

import org.omg.CosNaming.NameComponent;

import java.io.*;

public class NamingUtils{
    public static boolean debug=false;

    ;
    public static PrintStream debugStream;
    public static PrintStream errStream;

    // Do not instantiate this class
    private NamingUtils(){
    }

    public static void errprint(String msg){
        if(errStream!=null)
            errStream.println(msg);
        else
            System.err.println(msg);
    }

    public static void printException(Exception e){
        if(errStream!=null)
            e.printStackTrace(errStream);
        else
            e.printStackTrace();
    }

    public static void makeDebugStream(File logFile)
            throws IOException{
        // Create an outputstream for debugging
        OutputStream logOStream=
                new FileOutputStream(logFile);
        DataOutputStream logDStream=
                new DataOutputStream(logOStream);
        debugStream=new PrintStream(logDStream);
        // Emit first message
        debugStream.println("Debug Stream Enabled.");
    }

    public static void makeErrStream(File errFile)
            throws IOException{
        if(debug){
            // Create an outputstream for errors
            OutputStream errOStream=
                    new FileOutputStream(errFile);
            DataOutputStream errDStream=
                    new DataOutputStream(errOStream);
            errStream=new PrintStream(errDStream);
            dprint("Error stream setup completed.");
        }
    }

    public static void dprint(String msg){
        if(debug&&debugStream!=null)
            debugStream.println(msg);
    }

    static String getDirectoryStructuredName(NameComponent[] name){
        StringBuffer directoryStructuredName=new StringBuffer("/");
        for(int i=0;i<name.length;i++){
            directoryStructuredName.append(name[i].id+"."+name[i].kind);
        }
        return directoryStructuredName.toString();
    }
}
