/**
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.relation;

import com.sun.jmx.mbeanserver.GetPropertyAction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.security.AccessController;
import java.util.*;

import static com.sun.jmx.defaults.JmxProperties.RELATION_LOGGER;
import static com.sun.jmx.mbeanserver.Util.cast;

@SuppressWarnings("serial")  // serialVersionUID not constant
public class RelationTypeSupport implements RelationType{
    // Serialization compatibility stuff:
    // Two serial forms are supported in this class. The selected form depends
    // on system property "jmx.serial.form":
    //  - "1.0" for JMX 1.0
    //  - any other value for JMX 1.1 and higher
    //
    // Serial version for old serial form
    private static final long oldSerialVersionUID=-8179019472410837190L;
    //
    // Serial version for new serial form
    private static final long newSerialVersionUID=4611072955724144607L;
    //
    // Serializable fields in old serial form
    private static final ObjectStreamField[] oldSerialPersistentFields=
            {
                    new ObjectStreamField("myTypeName",String.class),
                    new ObjectStreamField("myRoleName2InfoMap",HashMap.class),
                    new ObjectStreamField("myIsInRelServFlg",boolean.class)
            };
    //
    // Serializable fields in new serial form
    private static final ObjectStreamField[] newSerialPersistentFields=
            {
                    new ObjectStreamField("typeName",String.class),
                    new ObjectStreamField("roleName2InfoMap",Map.class),
                    new ObjectStreamField("isInRelationService",boolean.class)
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
    private String typeName=null;
    private Map<String,RoleInfo> roleName2InfoMap=
            new HashMap<String,RoleInfo>();
    private boolean isInRelationService=false;
    //
    // Constructors
    //

    public RelationTypeSupport(String relationTypeName,
                               RoleInfo[] roleInfoArray)
            throws IllegalArgumentException,
            InvalidRelationTypeException{
        if(relationTypeName==null||roleInfoArray==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationTypeSupport.class.getName(),
                "RelationTypeSupport",relationTypeName);
        // Can throw InvalidRelationTypeException, ClassNotFoundException
        // and NotCompliantMBeanException
        initMembers(relationTypeName,roleInfoArray);
        RELATION_LOGGER.exiting(RelationTypeSupport.class.getName(),
                "RelationTypeSupport");
        return;
    }

    // Initializes the members, i.e. type name and role info list.
    //
    // -param relationTypeName  Name of relation type
    // -param roleInfoArray  List of role definitions (RoleInfo objects)
    //
    // -exception IllegalArgumentException  if null parameter
    // -exception InvalidRelationTypeException  If:
    //  - the same name has been used for two different roles
    //  - no role info provided
    //  - one null role info provided
    private void initMembers(String relationTypeName,
                             RoleInfo[] roleInfoArray)
            throws IllegalArgumentException,
            InvalidRelationTypeException{
        if(relationTypeName==null||roleInfoArray==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationTypeSupport.class.getName(),
                "initMembers",relationTypeName);
        typeName=relationTypeName;
        // Verifies role infos before setting them
        // Can throw InvalidRelationTypeException
        checkRoleInfos(roleInfoArray);
        for(int i=0;i<roleInfoArray.length;i++){
            RoleInfo currRoleInfo=roleInfoArray[i];
            roleName2InfoMap.put(currRoleInfo.getName(),
                    new RoleInfo(currRoleInfo));
        }
        RELATION_LOGGER.exiting(RelationTypeSupport.class.getName(),
                "initMembers");
        return;
    }
    //
    // Accessors
    //

    // Checks the given RoleInfo array to verify that:
    // - the array is not empty
    // - it does not contain a null element
    // - a given role name is used only for one RoleInfo
    //
    // -param roleInfoArray  array to be checked
    //
    // -exception IllegalArgumentException
    // -exception InvalidRelationTypeException  If:
    //  - the same name has been used for two different roles
    //  - no role info provided
    //  - one null role info provided
    static void checkRoleInfos(RoleInfo[] roleInfoArray)
            throws IllegalArgumentException,
            InvalidRelationTypeException{
        if(roleInfoArray==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        if(roleInfoArray.length==0){
            // No role info provided
            String excMsg="No role info provided.";
            throw new InvalidRelationTypeException(excMsg);
        }
        Set<String> roleNames=new HashSet<String>();
        for(int i=0;i<roleInfoArray.length;i++){
            RoleInfo currRoleInfo=roleInfoArray[i];
            if(currRoleInfo==null){
                String excMsg="Null role info provided.";
                throw new InvalidRelationTypeException(excMsg);
            }
            String roleName=currRoleInfo.getName();
            // Checks if the role info has already been described
            if(roleNames.contains(roleName)){
                StringBuilder excMsgStrB=new StringBuilder();
                String excMsg="Two role infos provided for role ";
                excMsgStrB.append(excMsg);
                excMsgStrB.append(roleName);
                throw new InvalidRelationTypeException(excMsgStrB.toString());
            }
            roleNames.add(roleName);
        }
        return;
    }

    protected RelationTypeSupport(String relationTypeName){
        if(relationTypeName==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationTypeSupport.class.getName(),
                "RelationTypeSupport",relationTypeName);
        typeName=relationTypeName;
        RELATION_LOGGER.exiting(RelationTypeSupport.class.getName(),
                "RelationTypeSupport");
        return;
    }

    public String getRelationTypeName(){
        return typeName;
    }
    //
    // Misc
    //

    public List<RoleInfo> getRoleInfos(){
        return new ArrayList<RoleInfo>(roleName2InfoMap.values());
    }

    public RoleInfo getRoleInfo(String roleInfoName)
            throws IllegalArgumentException,
            RoleInfoNotFoundException{
        if(roleInfoName==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationTypeSupport.class.getName(),
                "getRoleInfo",roleInfoName);
        // No null RoleInfo allowed, so use get()
        RoleInfo result=roleName2InfoMap.get(roleInfoName);
        if(result==null){
            StringBuilder excMsgStrB=new StringBuilder();
            String excMsg="No role info for role ";
            excMsgStrB.append(excMsg);
            excMsgStrB.append(roleInfoName);
            throw new RoleInfoNotFoundException(excMsgStrB.toString());
        }
        RELATION_LOGGER.exiting(RelationTypeSupport.class.getName(),
                "getRoleInfo");
        return result;
    }

    protected void addRoleInfo(RoleInfo roleInfo)
            throws IllegalArgumentException,
            InvalidRelationTypeException{
        if(roleInfo==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationTypeSupport.class.getName(),
                "addRoleInfo",roleInfo);
        if(isInRelationService){
            // Trying to update a declared relation type
            String excMsg="Relation type cannot be updated as it is declared in the Relation Service.";
            throw new RuntimeException(excMsg);
        }
        String roleName=roleInfo.getName();
        // Checks if the role info has already been described
        if(roleName2InfoMap.containsKey(roleName)){
            StringBuilder excMsgStrB=new StringBuilder();
            String excMsg="Two role infos provided for role ";
            excMsgStrB.append(excMsg);
            excMsgStrB.append(roleName);
            throw new InvalidRelationTypeException(excMsgStrB.toString());
        }
        roleName2InfoMap.put(roleName,new RoleInfo(roleInfo));
        RELATION_LOGGER.exiting(RelationTypeSupport.class.getName(),
                "addRoleInfo");
        return;
    }

    // Sets the internal flag to specify that the relation type has been
    // declared in the Relation Service
    void setRelationServiceFlag(boolean flag){
        isInRelationService=flag;
        return;
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        if(compat){
            // Read an object serialized in the old serial form
            //
            ObjectInputStream.GetField fields=in.readFields();
            typeName=(String)fields.get("myTypeName",null);
            if(fields.defaulted("myTypeName")){
                throw new NullPointerException("myTypeName");
            }
            roleName2InfoMap=cast(fields.get("myRoleName2InfoMap",null));
            if(fields.defaulted("myRoleName2InfoMap")){
                throw new NullPointerException("myRoleName2InfoMap");
            }
            isInRelationService=fields.get("myIsInRelServFlg",false);
            if(fields.defaulted("myIsInRelServFlg")){
                throw new NullPointerException("myIsInRelServFlg");
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
            fields.put("myTypeName",typeName);
            fields.put("myRoleName2InfoMap",roleName2InfoMap);
            fields.put("myIsInRelServFlg",isInRelationService);
            out.writeFields();
        }else{
            // Serializes this instance in the new serial form
            //
            out.defaultWriteObject();
        }
    }
}
