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
public class Role implements Serializable{
    // Serialization compatibility stuff:
    // Two serial forms are supported in this class. The selected form depends
    // on system property "jmx.serial.form":
    //  - "1.0" for JMX 1.0
    //  - any other value for JMX 1.1 and higher
    //
    // Serial version for old serial form
    private static final long oldSerialVersionUID=-1959486389343113026L;
    //
    // Serial version for new serial form
    private static final long newSerialVersionUID=-279985518429862552L;
    //
    // Serializable fields in old serial form
    private static final ObjectStreamField[] oldSerialPersistentFields=
            {
                    new ObjectStreamField("myName",String.class),
                    new ObjectStreamField("myObjNameList",ArrayList.class)
            };
    //
    // Serializable fields in new serial form
    private static final ObjectStreamField[] newSerialPersistentFields=
            {
                    new ObjectStreamField("name",String.class),
                    new ObjectStreamField("objectNameList",List.class)
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
    private String name=null;
    private List<ObjectName> objectNameList=new ArrayList<ObjectName>();
    //
    // Constructors
    //

    public Role(String roleName,
                List<ObjectName> roleValue)
            throws IllegalArgumentException{
        if(roleName==null||roleValue==null){
            String excMsg="Invalid parameter";
            throw new IllegalArgumentException(excMsg);
        }
        setRoleName(roleName);
        setRoleValue(roleValue);
        return;
    }
    //
    // Accessors
    //

    public static String roleValueToString(List<ObjectName> roleValue)
            throws IllegalArgumentException{
        if(roleValue==null){
            String excMsg="Invalid parameter";
            throw new IllegalArgumentException(excMsg);
        }
        StringBuilder result=new StringBuilder();
        for(ObjectName currObjName : roleValue){
            if(result.length()>0)
                result.append("\n");
            result.append(currObjName.toString());
        }
        return result.toString();
    }

    public String getRoleName(){
        return name;
    }

    public void setRoleName(String roleName)
            throws IllegalArgumentException{
        if(roleName==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        name=roleName;
        return;
    }

    public List<ObjectName> getRoleValue(){
        return objectNameList;
    }

    public void setRoleValue(List<ObjectName> roleValue)
            throws IllegalArgumentException{
        if(roleValue==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        objectNameList=new ArrayList<ObjectName>(roleValue);
        return;
    }
    //
    // Misc
    //

    public Object clone(){
        try{
            return new Role(name,objectNameList);
        }catch(IllegalArgumentException exc){
            return null; // can't happen
        }
    }

    public String toString(){
        StringBuilder result=new StringBuilder();
        result.append("role name: "+name+"; role value: ");
        for(Iterator<ObjectName> objNameIter=objectNameList.iterator();
            objNameIter.hasNext();){
            ObjectName currObjName=objNameIter.next();
            result.append(currObjName.toString());
            if(objNameIter.hasNext()){
                result.append(", ");
            }
        }
        return result.toString();
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        if(compat){
            // Read an object serialized in the old serial form
            //
            ObjectInputStream.GetField fields=in.readFields();
            name=(String)fields.get("myName",null);
            if(fields.defaulted("myName")){
                throw new NullPointerException("myName");
            }
            objectNameList=cast(fields.get("myObjNameList",null));
            if(fields.defaulted("myObjNameList")){
                throw new NullPointerException("myObjNameList");
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
            fields.put("myName",name);
            fields.put("myObjNameList",objectNameList);
            out.writeFields();
        }else{
            // Serializes this instance in the new serial form
            //
            out.defaultWriteObject();
        }
    }
}
