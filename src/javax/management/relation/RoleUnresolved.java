/**
 * Copyright (c) 2000, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.relation;

import com.sun.jmx.mbeanserver.GetPropertyAction;

import javax.management.ObjectName;
import java.io.*;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.sun.jmx.mbeanserver.Util.cast;

@SuppressWarnings("serial")  // serialVersionUID not constant
public class RoleUnresolved implements Serializable{
    // Serialization compatibility stuff:
    // Two serial forms are supported in this class. The selected form depends
    // on system property "jmx.serial.form":
    //  - "1.0" for JMX 1.0
    //  - any other value for JMX 1.1 and higher
    //
    // Serial version for old serial form
    private static final long oldSerialVersionUID=-9026457686611660144L;
    //
    // Serial version for new serial form
    private static final long newSerialVersionUID=-48350262537070138L;
    //
    // Serializable fields in old serial form
    private static final ObjectStreamField[] oldSerialPersistentFields=
            {
                    new ObjectStreamField("myRoleName",String.class),
                    new ObjectStreamField("myRoleValue",ArrayList.class),
                    new ObjectStreamField("myPbType",int.class)
            };
    //
    // Serializable fields in new serial form
    private static final ObjectStreamField[] newSerialPersistentFields=
            {
                    new ObjectStreamField("roleName",String.class),
                    new ObjectStreamField("roleValue",List.class),
                    new ObjectStreamField("problemType",int.class)
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
    private String roleName=null;
    private List<ObjectName> roleValue=null;
    private int problemType;
    //
    // Constructor
    //

    public RoleUnresolved(String name,
                          List<ObjectName> value,
                          int pbType)
            throws IllegalArgumentException{
        if(name==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        setRoleName(name);
        setRoleValue(value);
        // Can throw IllegalArgumentException
        setProblemType(pbType);
        return;
    }
    //
    // Accessors
    //

    public String getRoleName(){
        return roleName;
    }

    public void setRoleName(String name)
            throws IllegalArgumentException{
        if(name==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        roleName=name;
        return;
    }

    public List<ObjectName> getRoleValue(){
        return roleValue;
    }

    public void setRoleValue(List<ObjectName> value){
        if(value!=null){
            roleValue=new ArrayList<ObjectName>(value);
        }else{
            roleValue=null;
        }
        return;
    }

    public int getProblemType(){
        return problemType;
    }

    public void setProblemType(int pbType)
            throws IllegalArgumentException{
        if(!(RoleStatus.isRoleStatus(pbType))){
            String excMsg="Incorrect problem type.";
            throw new IllegalArgumentException(excMsg);
        }
        problemType=pbType;
        return;
    }

    public Object clone(){
        try{
            return new RoleUnresolved(roleName,roleValue,problemType);
        }catch(IllegalArgumentException exc){
            return null; // :)
        }
    }

    public String toString(){
        StringBuilder result=new StringBuilder();
        result.append("role name: "+roleName);
        if(roleValue!=null){
            result.append("; value: ");
            for(Iterator<ObjectName> objNameIter=roleValue.iterator();
                objNameIter.hasNext();){
                ObjectName currObjName=objNameIter.next();
                result.append(currObjName.toString());
                if(objNameIter.hasNext()){
                    result.append(", ");
                }
            }
        }
        result.append("; problem type: "+problemType);
        return result.toString();
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        if(compat){
            // Read an object serialized in the old serial form
            //
            ObjectInputStream.GetField fields=in.readFields();
            roleName=(String)fields.get("myRoleName",null);
            if(fields.defaulted("myRoleName")){
                throw new NullPointerException("myRoleName");
            }
            roleValue=cast(fields.get("myRoleValue",null));
            if(fields.defaulted("myRoleValue")){
                throw new NullPointerException("myRoleValue");
            }
            problemType=fields.get("myPbType",0);
            if(fields.defaulted("myPbType")){
                throw new NullPointerException("myPbType");
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
            fields.put("myRoleName",roleName);
            fields.put("myRoleValue",roleValue);
            fields.put("myPbType",problemType);
            out.writeFields();
        }else{
            // Serializes this instance in the new serial form
            //
            out.defaultWriteObject();
        }
    }
}
