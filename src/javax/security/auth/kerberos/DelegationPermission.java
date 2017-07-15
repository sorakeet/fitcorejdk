/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth.kerberos;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.security.BasicPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.*;

public final class DelegationPermission extends BasicPermission
        implements java.io.Serializable{
    private static final long serialVersionUID=883133252142523922L;
    private transient String subordinate, service;

    public DelegationPermission(String principals){
        super(principals);
        init(principals);
    }

    private void init(String target){
        StringTokenizer t=null;
        if(!target.startsWith("\"")){
            throw new IllegalArgumentException
                    ("service principal ["+target+
                            "] syntax invalid: "+
                            "improperly quoted");
        }else{
            t=new StringTokenizer(target,"\"",false);
            subordinate=t.nextToken();
            if(t.countTokens()==2){
                t.nextToken();  // bypass whitespace
                service=t.nextToken();
            }else if(t.countTokens()>0){
                throw new IllegalArgumentException
                        ("service principal ["+t.nextToken()+
                                "] syntax invalid: "+
                                "improperly quoted");
            }
        }
    }

    public DelegationPermission(String principals,String actions){
        super(principals,actions);
        init(principals);
    }

    private synchronized void writeObject(ObjectOutputStream s)
            throws IOException{
        s.defaultWriteObject();
    }    public boolean implies(Permission p){
        if(!(p instanceof DelegationPermission))
            return false;
        DelegationPermission that=(DelegationPermission)p;
        if(this.subordinate.equals(that.subordinate)&&
                this.service.equals(that.service))
            return true;
        return false;
    }

    private synchronized void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException{
        // Read in the action, then initialize the rest
        s.defaultReadObject();
        init(getName());
    }    public boolean equals(Object obj){
        if(obj==this)
            return true;
        if(!(obj instanceof DelegationPermission))
            return false;
        DelegationPermission that=(DelegationPermission)obj;
        return implies(that);
    }

    public int hashCode(){
        return getName().hashCode();
    }

    public PermissionCollection newPermissionCollection(){
        return new KrbDelegationPermissionCollection();
    }




    /**
     public static void main(String args[]) throws Exception {
     DelegationPermission this_ =
     new DelegationPermission(args[0]);
     DelegationPermission that_ =
     new DelegationPermission(args[1]);
     System.out.println("-----\n");
     System.out.println("this.implies(that) = " + this_.implies(that_));
     System.out.println("-----\n");
     System.out.println("this = "+this_);
     System.out.println("-----\n");
     System.out.println("that = "+that_);
     System.out.println("-----\n");

     KrbDelegationPermissionCollection nps =
     new KrbDelegationPermissionCollection();
     nps.add(this_);
     nps.add(new DelegationPermission("\"host/foo.example.com@EXAMPLE.COM\" \"CN=Gary Ellison/OU=JSN/O=SUNW/L=Palo Alto/ST=CA/C=US\""));
     try {
     nps.add(new DelegationPermission("host/foo.example.com@EXAMPLE.COM \"CN=Gary Ellison/OU=JSN/O=SUNW/L=Palo Alto/ST=CA/C=US\""));
     } catch (Exception e) {
     System.err.println(e);
     }

     System.out.println("nps.implies(that) = " + nps.implies(that_));
     System.out.println("-----\n");

     Enumeration e = nps.elements();

     while (e.hasMoreElements()) {
     DelegationPermission x =
     (DelegationPermission) e.nextElement();
     System.out.println("nps.e = " + x);
     }
     }
     */
}

final class KrbDelegationPermissionCollection extends PermissionCollection
        implements java.io.Serializable{
    private static final long serialVersionUID=-3383936936589966948L;
    // Need to maintain serialization interoperability with earlier releases,
    // which had the serializable field:
    //    private Vector permissions;
    private static final ObjectStreamField[] serialPersistentFields={
            new ObjectStreamField("permissions",Vector.class),
    };
    // Not serialized; see serialization section at end of class.
    private transient List<Permission> perms;

    public KrbDelegationPermissionCollection(){
        perms=new ArrayList<Permission>();
    }

    public void add(Permission permission){
        if(!(permission instanceof DelegationPermission))
            throw new IllegalArgumentException("invalid permission: "+
                    permission);
        if(isReadOnly())
            throw new SecurityException("attempt to add a Permission to a readonly PermissionCollection");
        synchronized(this){
            perms.add(0,permission);
        }
    }

    public boolean implies(Permission permission){
        if(!(permission instanceof DelegationPermission))
            return false;
        synchronized(this){
            for(Permission x : perms){
                if(x.implies(permission))
                    return true;
            }
        }
        return false;
    }

    public Enumeration<Permission> elements(){
        // Convert Iterator into Enumeration
        synchronized(this){
            return Collections.enumeration(perms);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException{
        // Don't call out.defaultWriteObject()
        // Write out Vector
        Vector<Permission> permissions=new Vector<>(perms.size());
        synchronized(this){
            permissions.addAll(perms);
        }
        ObjectOutputStream.PutField pfields=out.putFields();
        pfields.put("permissions",permissions);
        out.writeFields();
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        // Don't call defaultReadObject()
        // Read in serialized fields
        ObjectInputStream.GetField gfields=in.readFields();
        // Get the one we want
        Vector<Permission> permissions=
                (Vector<Permission>)gfields.get("permissions",null);
        perms=new ArrayList<Permission>(permissions.size());
        perms.addAll(permissions);
    }
}
