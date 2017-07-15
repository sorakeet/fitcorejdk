/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.naming.pcosnaming;

import com.sun.corba.se.impl.orbutil.CorbaResourceUtil;
import com.sun.corba.se.impl.orbutil.ORBConstants;
import com.sun.corba.se.spi.activation.InitialNameService;
import com.sun.corba.se.spi.activation.InitialNameServiceHelper;
import com.sun.corba.se.spi.orb.ORB;
import org.omg.CosNaming.NamingContext;

import java.io.File;
import java.util.Properties;

public class NameServer{
    private final static String dbName="names.db";
    private ORB orb;
    private File dbDir; // name server database directory

    protected NameServer(String args[]){
        // create the ORB Object
        Properties props=System.getProperties();
        props.put(ORBConstants.SERVER_ID_PROPERTY,"1000");
        props.put("org.omg.CORBA.ORBClass",
                "com.sun.corba.se.impl.orb.ORBImpl");
        orb=(ORB)org.omg.CORBA.ORB.init(args,props);
        // set up the database directory
        String dbDirName=props.getProperty(ORBConstants.DB_DIR_PROPERTY)+
                props.getProperty("file.separator")+dbName+
                props.getProperty("file.separator");
        dbDir=new File(dbDirName);
        if(!dbDir.exists()) dbDir.mkdir();
    }

    public static void main(String args[]){
        NameServer ns=new NameServer(args);
        ns.run();
    }

    protected void run(){
        try{
            // create the persistent name service
            NameService ns=new NameService(orb,dbDir);
            // add root naming context to initial naming
            NamingContext rootContext=ns.initialNamingContext();
            InitialNameService ins=InitialNameServiceHelper.narrow(
                    orb.resolve_initial_references(
                            ORBConstants.INITIAL_NAME_SERVICE_NAME));
            ins.bind("NameService",rootContext,true);
            System.out.println(CorbaResourceUtil.getText("pnameserv.success"));
            // wait for invocations
            orb.run();
        }catch(Exception ex){
            ex.printStackTrace(System.err);
        }
    }
}
