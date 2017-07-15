/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth;

import sun.security.util.ResourcesMgr;

import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

public final class PrivateCredentialPermission extends Permission{
    private static final long serialVersionUID=5284372143517237068L;
    private static final CredOwner[] EMPTY_PRINCIPALS=new CredOwner[0];
    private String credentialClass;
    private Set<Principal> principals;  // ignored - kept around for compatibility
    private transient CredOwner[] credOwners;
    private boolean testing=false;

    PrivateCredentialPermission(String credentialClass,
                                Set<Principal> principals){
        super(credentialClass);
        this.credentialClass=credentialClass;
        synchronized(principals){
            if(principals.size()==0){
                this.credOwners=EMPTY_PRINCIPALS;
            }else{
                this.credOwners=new CredOwner[principals.size()];
                int index=0;
                Iterator<Principal> i=principals.iterator();
                while(i.hasNext()){
                    Principal p=i.next();
                    this.credOwners[index++]=new CredOwner
                            (p.getClass().getName(),
                                    p.getName());
                }
            }
        }
    }

    public PrivateCredentialPermission(String name,String actions){
        super(name);
        if(!"read".equalsIgnoreCase(actions))
            throw new IllegalArgumentException
                    (ResourcesMgr.getString("actions.can.only.be.read."));
        init(name);
    }

    private void init(String name){
        if(name==null||name.trim().length()==0){
            throw new IllegalArgumentException("invalid empty name");
        }
        ArrayList<CredOwner> pList=new ArrayList<>();
        StringTokenizer tokenizer=new StringTokenizer(name," ",true);
        String principalClass=null;
        String principalName=null;
        if(testing)
            System.out.println("whole name = "+name);
        // get the Credential Class
        credentialClass=tokenizer.nextToken();
        if(testing)
            System.out.println("Credential Class = "+credentialClass);
        if(tokenizer.hasMoreTokens()==false){
            MessageFormat form=new MessageFormat(ResourcesMgr.getString
                    ("permission.name.name.syntax.invalid."));
            Object[] source={name};
            throw new IllegalArgumentException
                    (form.format(source)+ResourcesMgr.getString
                            ("Credential.Class.not.followed.by.a.Principal.Class.and.Name"));
        }
        while(tokenizer.hasMoreTokens()){
            // skip delimiter
            tokenizer.nextToken();
            // get the Principal Class
            principalClass=tokenizer.nextToken();
            if(testing)
                System.out.println("    Principal Class = "+principalClass);
            if(tokenizer.hasMoreTokens()==false){
                MessageFormat form=new MessageFormat(ResourcesMgr.getString
                        ("permission.name.name.syntax.invalid."));
                Object[] source={name};
                throw new IllegalArgumentException
                        (form.format(source)+ResourcesMgr.getString
                                ("Principal.Class.not.followed.by.a.Principal.Name"));
            }
            // skip delimiter
            tokenizer.nextToken();
            // get the Principal Name
            principalName=tokenizer.nextToken();
            if(!principalName.startsWith("\"")){
                MessageFormat form=new MessageFormat(ResourcesMgr.getString
                        ("permission.name.name.syntax.invalid."));
                Object[] source={name};
                throw new IllegalArgumentException
                        (form.format(source)+ResourcesMgr.getString
                                ("Principal.Name.must.be.surrounded.by.quotes"));
            }
            if(!principalName.endsWith("\"")){
                // we have a name with spaces in it --
                // keep parsing until we find the end quote,
                // and keep the spaces in the name
                while(tokenizer.hasMoreTokens()){
                    principalName=principalName+tokenizer.nextToken();
                    if(principalName.endsWith("\""))
                        break;
                }
                if(!principalName.endsWith("\"")){
                    MessageFormat form=new MessageFormat
                            (ResourcesMgr.getString
                                    ("permission.name.name.syntax.invalid."));
                    Object[] source={name};
                    throw new IllegalArgumentException
                            (form.format(source)+ResourcesMgr.getString
                                    ("Principal.Name.missing.end.quote"));
                }
            }
            if(testing)
                System.out.println("\tprincipalName = '"+principalName+"'");
            principalName=principalName.substring
                    (1,principalName.length()-1);
            if(principalClass.equals("*")&&
                    !principalName.equals("*")){
                throw new IllegalArgumentException(ResourcesMgr.getString
                        ("PrivateCredentialPermission.Principal.Class.can.not.be.a.wildcard.value.if.Principal.Name.is.not.a.wildcard.value"));
            }
            if(testing)
                System.out.println("\tprincipalName = '"+principalName+"'");
            pList.add(new CredOwner(principalClass,principalName));
        }
        this.credOwners=new CredOwner[pList.size()];
        pList.toArray(this.credOwners);
    }

    public String getCredentialClass(){
        return credentialClass;
    }

    public String[][] getPrincipals(){
        if(credOwners==null||credOwners.length==0){
            return new String[0][0];
        }
        String[][] pArray=new String[credOwners.length][2];
        for(int i=0;i<credOwners.length;i++){
            pArray[i][0]=credOwners[i].principalClass;
            pArray[i][1]=credOwners[i].principalName;
        }
        return pArray;
    }    public boolean implies(Permission p){
        if(p==null||!(p instanceof PrivateCredentialPermission))
            return false;
        PrivateCredentialPermission that=(PrivateCredentialPermission)p;
        if(!impliesCredentialClass(credentialClass,that.credentialClass))
            return false;
        return impliesPrincipalSet(credOwners,that.credOwners);
    }

    private void readObject(java.io.ObjectInputStream s) throws
            java.io.IOException,
            ClassNotFoundException{
        s.defaultReadObject();
        // perform new initialization from the permission name
        if(getName().indexOf(" ")==-1&&getName().indexOf("\"")==-1){
            // name only has a credential class specified
            credentialClass=getName();
            credOwners=EMPTY_PRINCIPALS;
        }else{
            // perform regular initialization
            init(getName());
        }
    }    public boolean equals(Object obj){
        if(obj==this)
            return true;
        if(!(obj instanceof PrivateCredentialPermission))
            return false;
        PrivateCredentialPermission that=(PrivateCredentialPermission)obj;
        return (this.implies(that)&&that.implies(this));
    }

    static class CredOwner implements java.io.Serializable{
        private static final long serialVersionUID=-5607449830436408266L;
        String principalClass;
        String principalName;

        CredOwner(String principalClass,String principalName){
            this.principalClass=principalClass;
            this.principalName=principalName;
        }

        public boolean implies(Object obj){
            if(obj==null||!(obj instanceof CredOwner))
                return false;
            CredOwner that=(CredOwner)obj;
            if(principalClass.equals("*")||
                    principalClass.equals(that.principalClass)){
                if(principalName.equals("*")||
                        principalName.equals(that.principalName)){
                    return true;
                }
            }
            /**
             * XXX no code yet to support a.b.*
             */
            return false;
        }

        public String toString(){
            MessageFormat form=new MessageFormat(ResourcesMgr.getString
                    ("CredOwner.Principal.Class.class.Principal.Name.name"));
            Object[] source={principalClass,principalName};
            return (form.format(source));
        }
    }    public int hashCode(){
        return this.credentialClass.hashCode();
    }

    public String getActions(){
        return "read";
    }

    public PermissionCollection newPermissionCollection(){
        return null;
    }



    private boolean impliesCredentialClass(String thisC,String thatC){
        // this should never happen
        if(thisC==null||thatC==null)
            return false;
        if(testing)
            System.out.println("credential class comparison: "+
                    thisC+"/"+thatC);
        if(thisC.equals("*"))
            return true;
        /**
         * XXX let's not enable this for now --
         *      if people want it, we'll enable it later
         */
        /**
         if (thisC.endsWith("*")) {
         String cClass = thisC.substring(0, thisC.length() - 2);
         return thatC.startsWith(cClass);
         }
         */
        return thisC.equals(thatC);
    }

    private boolean impliesPrincipalSet(CredOwner[] thisP,CredOwner[] thatP){
        // this should never happen
        if(thisP==null||thatP==null)
            return false;
        if(thatP.length==0)
            return true;
        if(thisP.length==0)
            return false;
        for(int i=0;i<thisP.length;i++){
            boolean foundMatch=false;
            for(int j=0;j<thatP.length;j++){
                if(thisP[i].implies(thatP[j])){
                    foundMatch=true;
                    break;
                }
            }
            if(!foundMatch){
                return false;
            }
        }
        return true;
    }




}
