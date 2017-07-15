/**
 * Copyright (c) 1997, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.IPAcl;
// java import
//

import com.sun.jmx.snmp.InetAddressAcl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.acl.AclEntry;
import java.security.acl.NotOwnerException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;

import static com.sun.jmx.defaults.JmxProperties.SNMP_LOGGER;
// SNMP Runtime import
//

public class SnmpAcl implements InetAddressAcl, Serializable{
    static final PermissionImpl READ=new PermissionImpl("READ");
    static final PermissionImpl WRITE=new PermissionImpl("WRITE");
    private static final long serialVersionUID=-6702287103824397063L;
    // PRIVATE VARIABLES
    //------------------
    private AclImpl acl=null;
    private boolean alwaysAuthorized=false;
    private String authorizedListFile=null;
    private Hashtable<InetAddress,Vector<String>> trapDestList=null;
    private Hashtable<InetAddress,Vector<String>> informDestList=null;
    private PrincipalImpl owner=null;

    public SnmpAcl(String Owner)
            throws UnknownHostException, IllegalArgumentException{
        this(Owner,null);
    }

    public SnmpAcl(String Owner,String aclFileName)
            throws UnknownHostException, IllegalArgumentException{
        trapDestList=new Hashtable<InetAddress,Vector<String>>();
        informDestList=new Hashtable<InetAddress,Vector<String>>();
        // PrincipalImpl() take the current host as entry
        owner=new PrincipalImpl();
        try{
            acl=new AclImpl(owner,Owner);
            AclEntry ownEntry=new AclEntryImpl(owner);
            ownEntry.addPermission(READ);
            ownEntry.addPermission(WRITE);
            acl.addEntry(owner,ownEntry);
        }catch(NotOwnerException ex){
            if(SNMP_LOGGER.isLoggable(Level.FINEST)){
                SNMP_LOGGER.logp(Level.FINEST,SnmpAcl.class.getName(),
                        "SnmpAcl(String,String)",
                        "Should never get NotOwnerException as the owner "+
                                "is built in this constructor");
            }
        }
        if(aclFileName==null) setDefaultFileName();
        else setAuthorizedListFile(aclFileName);
        readAuthorizedListFile();
    }

    private void readAuthorizedListFile(){
        alwaysAuthorized=false;
        if(authorizedListFile==null){
            if(SNMP_LOGGER.isLoggable(Level.FINER)){
                SNMP_LOGGER.logp(Level.FINER,SnmpAcl.class.getName(),
                        "readAuthorizedListFile","alwaysAuthorized set to true");
            }
            alwaysAuthorized=true;
        }else{
            // Read the file content
            Parser parser=null;
            try{
                parser=new Parser(new FileInputStream(getAuthorizedListFile()));
            }catch(FileNotFoundException e){
                if(SNMP_LOGGER.isLoggable(Level.FINEST)){
                    SNMP_LOGGER.logp(Level.FINEST,SnmpAcl.class.getName(),
                            "readAuthorizedListFile",
                            "The specified file was not found, authorize everybody");
                }
                alwaysAuthorized=true;
                return;
            }
            try{
                JDMSecurityDefs n=parser.SecurityDefs();
                n.buildAclEntries(owner,acl);
                n.buildTrapEntries(trapDestList);
                n.buildInformEntries(informDestList);
            }catch(ParseException e){
                if(SNMP_LOGGER.isLoggable(Level.FINEST)){
                    SNMP_LOGGER.logp(Level.FINEST,SnmpAcl.class.getName(),
                            "readAuthorizedListFile","Got parsing exception",e);
                }
                throw new IllegalArgumentException(e.getMessage());
            }catch(Error err){
                if(SNMP_LOGGER.isLoggable(Level.FINEST)){
                    SNMP_LOGGER.logp(Level.FINEST,SnmpAcl.class.getName(),
                            "readAuthorizedListFile","Got unexpected error",err);
                }
                throw new IllegalArgumentException(err.getMessage());
            }
            for(Enumeration<AclEntry> e=acl.entries();e.hasMoreElements();){
                AclEntryImpl aa=(AclEntryImpl)e.nextElement();
                if(SNMP_LOGGER.isLoggable(Level.FINER)){
                    SNMP_LOGGER.logp(Level.FINER,SnmpAcl.class.getName(),
                            "readAuthorizedListFile",
                            "===> "+aa.getPrincipal().toString());
                }
                for(Enumeration<java.security.acl.Permission> eee=aa.permissions();eee.hasMoreElements();){
                    java.security.acl.Permission perm=eee.nextElement();
                    if(SNMP_LOGGER.isLoggable(Level.FINER)){
                        SNMP_LOGGER.logp(Level.FINER,SnmpAcl.class.getName(),
                                "readAuthorizedListFile","perm = "+perm);
                    }
                }
            }
        }
    }

    public String getAuthorizedListFile(){
        return authorizedListFile;
    }

    public void setAuthorizedListFile(String filename)
            throws IllegalArgumentException{
        File file=new File(filename);
        if(!file.isFile()){
            if(SNMP_LOGGER.isLoggable(Level.FINEST)){
                SNMP_LOGGER.logp(Level.FINEST,SnmpAcl.class.getName(),
                        "setAuthorizedListFile","ACL file not found: "+filename);
            }
            throw new
                    IllegalArgumentException("The specified file ["+file+"] "+
                    "doesn't exist or is not a file, "+
                    "no configuration loaded");
        }
        if(SNMP_LOGGER.isLoggable(Level.FINER)){
            SNMP_LOGGER.logp(Level.FINER,SnmpAcl.class.getName(),
                    "setAuthorizedListFile","Default file set to "+filename);
        }
        authorizedListFile=filename;
    }

    private void setDefaultFileName(){
        try{
            setAuthorizedListFile(getDefaultAclFileName());
        }catch(IllegalArgumentException x){
            // OK...
        }
    }

    public static String getDefaultAclFileName(){
        final String fileSeparator=
                System.getProperty("file.separator");
        final StringBuffer defaultAclName=
                new StringBuffer(System.getProperty("java.home")).
                        append(fileSeparator).append("lib").append(fileSeparator).
                        append("snmp.acl");
        return defaultAclName.toString();
    }

    static public PermissionImpl getREAD(){
        return READ;
    }

    static public PermissionImpl getWRITE(){
        return WRITE;
    }

    public Enumeration<AclEntry> entries(){
        return acl.entries();
    }

    public Enumeration<String> communities(){
        HashSet<String> set=new HashSet<String>();
        Vector<String> res=new Vector<String>();
        for(Enumeration<AclEntry> e=acl.entries();e.hasMoreElements();){
            AclEntryImpl entry=(AclEntryImpl)e.nextElement();
            for(Enumeration<String> cs=entry.communities();
                cs.hasMoreElements();){
                set.add(cs.nextElement());
            }
        }
        String[] objs=set.toArray(new String[0]);
        for(int i=0;i<objs.length;i++)
            res.addElement(objs[i]);
        return res.elements();
    }

    public String getName(){
        return acl.getName();
    }

    public boolean checkReadPermission(InetAddress address){
        if(alwaysAuthorized) return (true);
        PrincipalImpl p=new PrincipalImpl(address);
        return acl.checkPermission(p,READ);
    }

    public boolean checkReadPermission(InetAddress address,String community){
        if(alwaysAuthorized) return (true);
        PrincipalImpl p=new PrincipalImpl(address);
        return acl.checkPermission(p,community,READ);
    }

    public boolean checkCommunity(String community){
        return acl.checkCommunity(community);
    }

    public boolean checkWritePermission(InetAddress address){
        if(alwaysAuthorized) return (true);
        PrincipalImpl p=new PrincipalImpl(address);
        return acl.checkPermission(p,WRITE);
    }

    public boolean checkWritePermission(InetAddress address,String community){
        if(alwaysAuthorized) return (true);
        PrincipalImpl p=new PrincipalImpl(address);
        return acl.checkPermission(p,community,WRITE);
    }

    public Enumeration<InetAddress> getTrapDestinations(){
        return trapDestList.keys();
    }

    public Enumeration<String> getTrapCommunities(InetAddress i){
        Vector<String> list=null;
        if((list=trapDestList.get(i))!=null){
            if(SNMP_LOGGER.isLoggable(Level.FINER)){
                SNMP_LOGGER.logp(Level.FINER,SnmpAcl.class.getName(),
                        "getTrapCommunities","["+i.toString()+"] is in list");
            }
            return list.elements();
        }else{
            list=new Vector<>();
            if(SNMP_LOGGER.isLoggable(Level.FINER)){
                SNMP_LOGGER.logp(Level.FINER,SnmpAcl.class.getName(),
                        "getTrapCommunities","["+i.toString()+"] is not in list");
            }
            return list.elements();
        }
    }

    public Enumeration<InetAddress> getInformDestinations(){
        return informDestList.keys();
    }

    public Enumeration<String> getInformCommunities(InetAddress i){
        Vector<String> list=null;
        if((list=informDestList.get(i))!=null){
            if(SNMP_LOGGER.isLoggable(Level.FINER)){
                SNMP_LOGGER.logp(Level.FINER,SnmpAcl.class.getName(),
                        "getInformCommunities","["+i.toString()+"] is in list");
            }
            return list.elements();
        }else{
            list=new Vector<>();
            if(SNMP_LOGGER.isLoggable(Level.FINER)){
                SNMP_LOGGER.logp(Level.FINER,SnmpAcl.class.getName(),
                        "getInformCommunities","["+i.toString()+"] is not in list");
            }
            return list.elements();
        }
    }

    public void rereadTheFile() throws NotOwnerException, UnknownHostException{
        alwaysAuthorized=false;
        acl.removeAll(owner);
        trapDestList.clear();
        informDestList.clear();
        AclEntry ownEntry=new AclEntryImpl(owner);
        ownEntry.addPermission(READ);
        ownEntry.addPermission(WRITE);
        acl.addEntry(owner,ownEntry);
        readAuthorizedListFile();
    }
}
