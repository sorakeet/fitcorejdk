/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth.kerberos;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.*;

public final class ServicePermission extends Permission
        implements java.io.Serializable{
    private static final long serialVersionUID=-1227585031618624935L;
    private final static int INITIATE=0x1;
    private final static int ACCEPT=0x2;
    private final static int ALL=INITIATE|ACCEPT;
    private final static int NONE=0x0;
    // the actions mask
    private transient int mask;
    private String actions; // Left null as long as possible, then
    // created and re-used in the getAction function.

    public ServicePermission(String servicePrincipal,String action){
        // Note: servicePrincipal can be "@REALM" which means any principal in
        // this realm implies it. action can be "-" which means any
        // action implies it.
        super(servicePrincipal);
        init(servicePrincipal,getMask(action));
    }

    private void init(String servicePrincipal,int mask){
        if(servicePrincipal==null)
            throw new NullPointerException("service principal can't be null");
        if((mask&ALL)!=mask)
            throw new IllegalArgumentException("invalid actions mask");
        this.mask=mask;
    }

    private static int getMask(String action){
        if(action==null){
            throw new NullPointerException("action can't be null");
        }
        if(action.equals("")){
            throw new IllegalArgumentException("action can't be empty");
        }
        int mask=NONE;
        char[] a=action.toCharArray();
        if(a.length==1&&a[0]=='-'){
            return mask;
        }
        int i=a.length-1;
        while(i!=-1){
            char c;
            // skip whitespace
            while((i!=-1)&&((c=a[i])==' '||
                    c=='\r'||
                    c=='\n'||
                    c=='\f'||
                    c=='\t'))
                i--;
            // check for the known strings
            int matchlen;
            if(i>=7&&(a[i-7]=='i'||a[i-7]=='I')&&
                    (a[i-6]=='n'||a[i-6]=='N')&&
                    (a[i-5]=='i'||a[i-5]=='I')&&
                    (a[i-4]=='t'||a[i-4]=='T')&&
                    (a[i-3]=='i'||a[i-3]=='I')&&
                    (a[i-2]=='a'||a[i-2]=='A')&&
                    (a[i-1]=='t'||a[i-1]=='T')&&
                    (a[i]=='e'||a[i]=='E')){
                matchlen=8;
                mask|=INITIATE;
            }else if(i>=5&&(a[i-5]=='a'||a[i-5]=='A')&&
                    (a[i-4]=='c'||a[i-4]=='C')&&
                    (a[i-3]=='c'||a[i-3]=='C')&&
                    (a[i-2]=='e'||a[i-2]=='E')&&
                    (a[i-1]=='p'||a[i-1]=='P')&&
                    (a[i]=='t'||a[i]=='T')){
                matchlen=6;
                mask|=ACCEPT;
            }else{
                // parse error
                throw new IllegalArgumentException(
                        "invalid permission: "+action);
            }
            // make sure we didn't just match the tail of a word
            // like "ackbarfaccept".  Also, skip to the comma.
            boolean seencomma=false;
            while(i>=matchlen&&!seencomma){
                switch(a[i-matchlen]){
                    case ',':
                        seencomma=true;
                        break;
                    case ' ':
                    case '\r':
                    case '\n':
                    case '\f':
                    case '\t':
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "invalid permission: "+action);
                }
                i--;
            }
            // point i at the location of the comma minus one (or -1).
            i-=matchlen;
        }
        return mask;
    }

    public boolean implies(Permission p){
        if(!(p instanceof ServicePermission))
            return false;
        ServicePermission that=(ServicePermission)p;
        return ((this.mask&that.mask)==that.mask)&&
                impliesIgnoreMask(that);
    }

    boolean impliesIgnoreMask(ServicePermission p){
        return ((this.getName().equals("*"))||
                this.getName().equals(p.getName())||
                (p.getName().startsWith("@")&&
                        this.getName().endsWith(p.getName())));
    }

    public boolean equals(Object obj){
        if(obj==this)
            return true;
        if(!(obj instanceof ServicePermission))
            return false;
        ServicePermission that=(ServicePermission)obj;
        return ((this.mask&that.mask)==that.mask)&&
                this.getName().equals(that.getName());
    }

    public int hashCode(){
        return (getName().hashCode()^mask);
    }

    public String getActions(){
        if(actions==null)
            actions=getActions(this.mask);
        return actions;
    }

    private static String getActions(int mask){
        StringBuilder sb=new StringBuilder();
        boolean comma=false;
        if((mask&INITIATE)==INITIATE){
            if(comma) sb.append(',');
            else comma=true;
            sb.append("initiate");
        }
        if((mask&ACCEPT)==ACCEPT){
            if(comma) sb.append(',');
            else comma=true;
            sb.append("accept");
        }
        return sb.toString();
    }

    public PermissionCollection newPermissionCollection(){
        return new KrbServicePermissionCollection();
    }

    int getMask(){
        return mask;
    }

    private void writeObject(ObjectOutputStream s)
            throws IOException{
        // Write out the actions. The superclass takes care of the name
        // call getActions to make sure actions field is initialized
        if(actions==null)
            getActions();
        s.defaultWriteObject();
    }

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException{
        // Read in the action, then initialize the rest
        s.defaultReadObject();
        init(getName(),getMask(actions));
    }
    /**
     public static void main(String args[]) throws Exception {
     ServicePermission this_ =
     new ServicePermission(args[0], "accept");
     ServicePermission that_ =
     new ServicePermission(args[1], "accept,initiate");
     System.out.println("-----\n");
     System.out.println("this.implies(that) = " + this_.implies(that_));
     System.out.println("-----\n");
     System.out.println("this = "+this_);
     System.out.println("-----\n");
     System.out.println("that = "+that_);
     System.out.println("-----\n");

     KrbServicePermissionCollection nps =
     new KrbServicePermissionCollection();
     nps.add(this_);
     nps.add(new ServicePermission("nfs/example.com@EXAMPLE.COM",
     "accept"));
     nps.add(new ServicePermission("host/example.com@EXAMPLE.COM",
     "initiate"));
     System.out.println("nps.implies(that) = " + nps.implies(that_));
     System.out.println("-----\n");

     Enumeration e = nps.elements();

     while (e.hasMoreElements()) {
     ServicePermission x =
     (ServicePermission) e.nextElement();
     System.out.println("nps.e = " + x);
     }

     }
     */
}

final class KrbServicePermissionCollection extends PermissionCollection
        implements java.io.Serializable{
    private static final long serialVersionUID=-4118834211490102011L;
    // Need to maintain serialization interoperability with earlier releases,
    // which had the serializable field:
    // private Vector permissions;
    private static final ObjectStreamField[] serialPersistentFields={
            new ObjectStreamField("permissions",Vector.class),
    };
    // Not serialized; see serialization section at end of class
    private transient List<Permission> perms;

    public KrbServicePermissionCollection(){
        perms=new ArrayList<Permission>();
    }

    public void add(Permission permission){
        if(!(permission instanceof ServicePermission))
            throw new IllegalArgumentException("invalid permission: "+
                    permission);
        if(isReadOnly())
            throw new SecurityException("attempt to add a Permission to a readonly PermissionCollection");
        synchronized(this){
            perms.add(0,permission);
        }
    }

    public boolean implies(Permission permission){
        if(!(permission instanceof ServicePermission))
            return false;
        ServicePermission np=(ServicePermission)permission;
        int desired=np.getMask();
        if(desired==0){
            for(Permission p : perms){
                ServicePermission sp=(ServicePermission)p;
                if(sp.impliesIgnoreMask(np)){
                    return true;
                }
            }
            return false;
        }
        int effective=0;
        int needed=desired;
        synchronized(this){
            int len=perms.size();
            // need to deal with the case where the needed permission has
            // more than one action and the collection has individual permissions
            // that sum up to the needed.
            for(int i=0;i<len;i++){
                ServicePermission x=(ServicePermission)perms.get(i);
                //System.out.println("  trying "+x);
                if(((needed&x.getMask())!=0)&&x.impliesIgnoreMask(np)){
                    effective|=x.getMask();
                    if((effective&desired)==desired)
                        return true;
                    needed=(desired^effective);
                }
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
