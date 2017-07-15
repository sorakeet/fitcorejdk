/**
 * Copyright (c) 2000, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.relation;

import com.sun.jmx.mbeanserver.GetPropertyAction;

import java.io.*;
import java.security.AccessController;
import java.util.Iterator;

@SuppressWarnings("serial")
public class RoleResult implements Serializable{
    // Serialization compatibility stuff:
    // Two serial forms are supported in this class. The selected form depends
    // on system property "jmx.serial.form":
    //  - "1.0" for JMX 1.0
    //  - any other value for JMX 1.1 and higher
    //
    // Serial version for old serial form
    private static final long oldSerialVersionUID=3786616013762091099L;
    //
    // Serial version for new serial form
    private static final long newSerialVersionUID=-6304063118040985512L;
    //
    // Serializable fields in old serial form
    private static final ObjectStreamField[] oldSerialPersistentFields=
            {
                    new ObjectStreamField("myRoleList",RoleList.class),
                    new ObjectStreamField("myRoleUnresList",RoleUnresolvedList.class)
            };
    //
    // Serializable fields in new serial form
    private static final ObjectStreamField[] newSerialPersistentFields=
            {
                    new ObjectStreamField("roleList",RoleList.class),
                    new ObjectStreamField("unresolvedRoleList",RoleUnresolvedList.class)
            };
    //
    // Actual serial version and serial form
    private static final long serialVersionUID;
    private static final ObjectStreamField[] serialPersistentFields;
    private static boolean compat=false;

    static{
        try{
            GetPropertyAction act=new GetPropertyAction("jmx.serial.form");
            String form=AccessController.doPrivileged(act);
            compat=(form!=null&&form.equals("1.0"));
        }catch(Exception e){
            // OK : Too bad, no compat with 1.0
        }
        if(compat){
            serialPersistentFields=oldSerialPersistentFields;
            serialVersionUID=oldSerialVersionUID;
        }else{
            serialPersistentFields=newSerialPersistentFields;
            serialVersionUID=newSerialVersionUID;
        }
    }
    //
    // END Serialization compatibility stuff
    //
    // Private members
    //
    private RoleList roleList=null;
    private RoleUnresolvedList unresolvedRoleList=null;
    //
    // Constructor
    //

    public RoleResult(RoleList list,
                      RoleUnresolvedList unresolvedList){
        setRoles(list);
        setRolesUnresolved(unresolvedList);
        return;
    }
    //
    // Accessors
    //

    public RoleList getRoles(){
        return roleList;
    }

    public void setRoles(RoleList list){
        if(list!=null){
            roleList=new RoleList();
            for(Iterator<?> roleIter=list.iterator();
                roleIter.hasNext();){
                Role currRole=(Role)(roleIter.next());
                roleList.add((Role)(currRole.clone()));
            }
        }else{
            roleList=null;
        }
        return;
    }

    public RoleUnresolvedList getRolesUnresolved(){
        return unresolvedRoleList;
    }

    public void setRolesUnresolved(RoleUnresolvedList unresolvedList){
        if(unresolvedList!=null){
            unresolvedRoleList=new RoleUnresolvedList();
            for(Iterator<?> roleUnresIter=unresolvedList.iterator();
                roleUnresIter.hasNext();){
                RoleUnresolved currRoleUnres=
                        (RoleUnresolved)(roleUnresIter.next());
                unresolvedRoleList.add((RoleUnresolved)(currRoleUnres.clone()));
            }
        }else{
            unresolvedRoleList=null;
        }
        return;
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        if(compat){
            // Read an object serialized in the old serial form
            //
            ObjectInputStream.GetField fields=in.readFields();
            roleList=(RoleList)fields.get("myRoleList",null);
            if(fields.defaulted("myRoleList")){
                throw new NullPointerException("myRoleList");
            }
            unresolvedRoleList=(RoleUnresolvedList)fields.get("myRoleUnresList",null);
            if(fields.defaulted("myRoleUnresList")){
                throw new NullPointerException("myRoleUnresList");
            }
        }else{
            // Read an object serialized in the new serial form
            //
            in.defaultReadObject();
        }
    }

    private void writeObject(ObjectOutputStream out)
            throws IOException{
        if(compat){
            // Serializes this instance in the old serial form
            //
            ObjectOutputStream.PutField fields=out.putFields();
            fields.put("myRoleList",roleList);
            fields.put("myRoleUnresList",unresolvedRoleList);
            out.writeFields();
        }else{
            // Serializes this instance in the new serial form
            //
            out.defaultWriteObject();
        }
    }
}
