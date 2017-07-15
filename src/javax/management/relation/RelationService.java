/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.relation;

import javax.management.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.sun.jmx.defaults.JmxProperties.RELATION_LOGGER;
import static com.sun.jmx.mbeanserver.Util.cast;

public class RelationService extends NotificationBroadcasterSupport
        implements RelationServiceMBean, MBeanRegistration, NotificationListener{
    // Map associating:
    //       <ObjectName> -> HashMap
    // the value HashMap mapping:
    //       <relation id> -> ArrayList of <role name>
    // to track where a given MBean is referenced.
    private final Map<ObjectName,Map<String,List<String>>>
            myRefedMBeanObjName2RelIdsMap=
            new HashMap<ObjectName,Map<String,List<String>>>();
    // Internal counter to provide sequence numbers for notifications sent by:
    // - the Relation Service
    // - a relation handled by the Relation Service
    private final AtomicLong atomicSeqNo=new AtomicLong();
    //
    // Private members
    //
    // Map associating:
    //      <relation id> -> <RelationSupport object/ObjectName>
    // depending if the relation has been created using createRelation()
    // method (so internally handled) or is an MBean added as a relation by the
    // user
    private Map<String,Object> myRelId2ObjMap=new HashMap<String,Object>();
    // Map associating:
    //      <relation id> -> <relation type name>
    private Map<String,String> myRelId2RelTypeMap=new HashMap<String,String>();
    // Map associating:
    //      <relation MBean Object Name> -> <relation id>
    private Map<ObjectName,String> myRelMBeanObjName2RelIdMap=
            new HashMap<ObjectName,String>();
    // Map associating:
    //       <relation type name> -> <RelationType object>
    private Map<String,RelationType> myRelType2ObjMap=
            new HashMap<String,RelationType>();
    // Map associating:
    //       <relation type name> -> ArrayList of <relation id>
    // to list all the relations of a given type
    private Map<String,List<String>> myRelType2RelIdsMap=
            new HashMap<String,List<String>>();
    // Flag to indicate if, when a notification is received for the
    // unregistration of an MBean referenced in a relation, if an immediate
    // "purge" of the relations (look for the relations no
    // longer valid) has to be performed , or if that will be performed only
    // when the purgeRelations method will be explicitly called.
    // true is immediate purge.
    private boolean myPurgeFlag=true;
    // ObjectName used to register the Relation Service in the MBean Server
    private ObjectName myObjName=null;
    // MBean Server where the Relation Service is registered
    private MBeanServer myMBeanServer=null;
    // Filter registered in the MBean Server with the Relation Service to be
    // informed of referenced MBean deregistrations
    private MBeanServerNotificationFilter myUnregNtfFilter=null;
    // List of unregistration notifications received (storage used if purge
    // of relations when unregistering a referenced MBean is not immediate but
    // on user request)
    private List<MBeanServerNotification> myUnregNtfList=
            new ArrayList<MBeanServerNotification>();
    //
    // Constructor
    //

    public RelationService(boolean immediatePurgeFlag){
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "RelationService");
        setPurgeFlag(immediatePurgeFlag);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "RelationService");
        return;
    }

    // Pre-registration: retrieves its ObjectName and MBean Server
    //
    // No exception thrown.
    public ObjectName preRegister(MBeanServer server,
                                  ObjectName name)
            throws Exception{
        myMBeanServer=server;
        myObjName=name;
        return name;
    }    public void isActive()
            throws RelationServiceNotRegisteredException{
        if(myMBeanServer==null){
            // MBean Server not set by preRegister(): relation service not
            // registered
            String excMsg=
                    "Relation Service not registered in the MBean Server.";
            throw new RelationServiceNotRegisteredException(excMsg);
        }
        return;
    }
    //
    // MBeanRegistration interface
    //

    // Post-registration: does nothing
    public void postRegister(Boolean registrationDone){
        return;
    }

    // Pre-unregistration: does nothing
    public void preDeregister()
            throws Exception{
        return;
    }

    // Post-unregistration: does nothing
    public void postDeregister(){
        return;
    }

    public void handleNotification(Notification notif,
                                   Object handback){
        if(notif==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "handleNotification",notif);
        if(notif instanceof MBeanServerNotification){
            MBeanServerNotification mbsNtf=(MBeanServerNotification)notif;
            String ntfType=notif.getType();
            if(ntfType.equals(
                    MBeanServerNotification.UNREGISTRATION_NOTIFICATION)){
                ObjectName mbeanName=
                        ((MBeanServerNotification)notif).getMBeanName();
                // Note: use a flag to block access to
                // myRefedMBeanObjName2RelIdsMap only for a quick access
                boolean isRefedMBeanFlag=false;
                synchronized(myRefedMBeanObjName2RelIdsMap){
                    if(myRefedMBeanObjName2RelIdsMap.containsKey(mbeanName)){
                        // Unregistration of a referenced MBean
                        synchronized(myUnregNtfList){
                            myUnregNtfList.add(mbsNtf);
                        }
                        isRefedMBeanFlag=true;
                    }
                    if(isRefedMBeanFlag&&myPurgeFlag){
                        // Immediate purge
                        // Can throw RelationServiceNotRegisteredException
                        // but assume that will be fine :)
                        try{
                            purgeRelations();
                        }catch(Exception exc){
                            throw new RuntimeException(exc.getMessage());
                        }
                    }
                }
                // Note: do both tests as a relation can be an MBean and be
                //       itself referenced in another relation :)
                String relId;
                synchronized(myRelMBeanObjName2RelIdMap){
                    relId=myRelMBeanObjName2RelIdMap.get(mbeanName);
                }
                if(relId!=null){
                    // Unregistration of a relation MBean
                    // Can throw RelationTypeNotFoundException,
                    // RelationServiceNotRegisteredException
                    //
                    // Shall not throw RelationTypeNotFoundException or
                    // InstanceNotFoundException
                    try{
                        removeRelation(relId);
                    }catch(Exception exc){
                        throw new RuntimeException(exc.getMessage());
                    }
                }
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "handleNotification");
        return;
    }
    //
    // Accessors
    //

    public MBeanNotificationInfo[] getNotificationInfo(){
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "getNotificationInfo");
        String ntfClass="javax.management.relation.RelationNotification";
        String[] ntfTypes=new String[]{
                RelationNotification.RELATION_BASIC_CREATION,
                RelationNotification.RELATION_MBEAN_CREATION,
                RelationNotification.RELATION_BASIC_UPDATE,
                RelationNotification.RELATION_MBEAN_UPDATE,
                RelationNotification.RELATION_BASIC_REMOVAL,
                RelationNotification.RELATION_MBEAN_REMOVAL,
        };
        String ntfDesc="Sent when a relation is created, updated or deleted.";
        MBeanNotificationInfo ntfInfo=
                new MBeanNotificationInfo(ntfTypes,ntfClass,ntfDesc);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "getNotificationInfo");
        return new MBeanNotificationInfo[]{ntfInfo};
    }    public boolean getPurgeFlag(){
        return myPurgeFlag;
    }

    // Checks, for the unregistration of an MBean referenced in the roles given
    // in parameter, if the relation has to be removed or not, regarding
    // expected minimum role cardinality and current number of
    // references in each role after removal of the current one.
    // If the relation is kept, calls handleMBeanUnregistration() callback of
    // the relation to update it.
    //
    // -param relationId  relation id
    // -param objectName  ObjectName of the unregistered MBean
    // -param roleNameList  list of names of roles where the unregistered
    //  MBean is referenced.
    //
    // -exception IllegalArgumentException  if null parameter
    // -exception RelationServiceNotRegisteredException  if the Relation
    //  Service is not registered in the MBean Server
    // -exception RelationNotFoundException  if unknown relation id
    // -exception RoleNotFoundException  if one role given as parameter does
    //  not exist in the relation
    private void handleReferenceUnregistration(String relationId,
                                               ObjectName objectName,
                                               List<String> roleNameList)
            throws IllegalArgumentException,
            RelationServiceNotRegisteredException,
            RelationNotFoundException,
            RoleNotFoundException{
        if(relationId==null||
                roleNameList==null||
                objectName==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "handleReferenceUnregistration",
                new Object[]{relationId,objectName,roleNameList});
        // Can throw RelationServiceNotRegisteredException
        isActive();
        // Retrieves the relation type name of the relation
        // Can throw RelationNotFoundException
        String currRelTypeName=getRelationTypeName(relationId);
        // Retrieves the relation
        // Can throw RelationNotFoundException, but already detected above
        Object relObj=getRelation(relationId);
        // Flag to specify if the relation has to be deleted
        boolean deleteRelFlag=false;
        for(String currRoleName : roleNameList){
            if(deleteRelFlag){
                break;
            }
            // Retrieves number of MBeans currently referenced in role
            // BEWARE! Do not use getRole() as role may be not readable
            //
            // Can throw RelationNotFoundException (but already checked),
            // RoleNotFoundException
            int currRoleRefNbr=
                    (getRoleCardinality(relationId,currRoleName)).intValue();
            // Retrieves new number of element in role
            int currRoleNewRefNbr=currRoleRefNbr-1;
            // Retrieves role info for that role
            //
            // Shall not throw RelationTypeNotFoundException or
            // RoleInfoNotFoundException
            RoleInfo currRoleInfo;
            try{
                currRoleInfo=getRoleInfo(currRelTypeName,
                        currRoleName);
            }catch(RelationTypeNotFoundException exc1){
                throw new RuntimeException(exc1.getMessage());
            }catch(RoleInfoNotFoundException exc2){
                throw new RuntimeException(exc2.getMessage());
            }
            // Checks with expected minimum number of elements
            boolean chkMinFlag=currRoleInfo.checkMinDegree(currRoleNewRefNbr);
            if(!chkMinFlag){
                // The relation has to be deleted
                deleteRelFlag=true;
            }
        }
        if(deleteRelFlag){
            // Removes the relation
            removeRelation(relationId);
        }else{
            // Updates each role in the relation using
            // handleMBeanUnregistration() callback
            //
            // BEWARE: this roleNameList list MUST BE A COPY of a role name
            //         list for a referenced MBean in a relation, NOT a
            //         reference to an original one part of the
            //         myRefedMBeanObjName2RelIdsMap!!!! Because each role
            //         which name is in that list will be updated (potentially
            //         using setRole(). So the Relation Service will update the
            //         myRefedMBeanObjName2RelIdsMap to refelect the new role
            //         value!
            for(String currRoleName : roleNameList){
                if(relObj instanceof RelationSupport){
                    // Internal relation
                    // Can throw RoleNotFoundException (but already checked)
                    //
                    // Shall not throw
                    // RelationTypeNotFoundException,
                    // InvalidRoleValueException (value was correct, removing
                    // one reference shall not invalidate it, else detected
                    // above)
                    try{
                        ((RelationSupport)relObj).handleMBeanUnregistrationInt(
                                objectName,
                                currRoleName,
                                true,
                                this);
                    }catch(RelationTypeNotFoundException exc3){
                        throw new RuntimeException(exc3.getMessage());
                    }catch(InvalidRoleValueException exc4){
                        throw new RuntimeException(exc4.getMessage());
                    }
                }else{
                    // Relation MBean
                    Object[] params=new Object[2];
                    params[0]=objectName;
                    params[1]=currRoleName;
                    String[] signature=new String[2];
                    signature[0]="javax.management.ObjectName";
                    signature[1]="java.lang.String";
                    // Shall not throw InstanceNotFoundException, or
                    // MBeanException (wrapping RoleNotFoundException or
                    // MBeanException or InvalidRoleValueException) or
                    // ReflectionException
                    try{
                        myMBeanServer.invoke(((ObjectName)relObj),
                                "handleMBeanUnregistration",
                                params,
                                signature);
                    }catch(InstanceNotFoundException exc1){
                        throw new RuntimeException(exc1.getMessage());
                    }catch(ReflectionException exc3){
                        throw new RuntimeException(exc3.getMessage());
                    }catch(MBeanException exc2){
                        Exception wrappedExc=exc2.getTargetException();
                        throw new RuntimeException(wrappedExc.getMessage());
                    }
                }
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "handleReferenceUnregistration");
        return;
    }    public void setPurgeFlag(boolean purgeFlag){
        myPurgeFlag=purgeFlag;
        return;
    }
    //
    // Relation type handling
    //

    public void createRelationType(String relationTypeName,
                                   RoleInfo[] roleInfoArray)
            throws IllegalArgumentException,
            InvalidRelationTypeException{
        if(relationTypeName==null||roleInfoArray==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "createRelationType",relationTypeName);
        // Can throw an InvalidRelationTypeException
        RelationType relType=
                new RelationTypeSupport(relationTypeName,roleInfoArray);
        addRelationTypeInt(relType);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "createRelationType");
        return;
    }

    public void addRelationType(RelationType relationTypeObj)
            throws IllegalArgumentException,
            InvalidRelationTypeException{
        if(relationTypeObj==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "addRelationType");
        // Checks the role infos
        List<RoleInfo> roleInfoList=relationTypeObj.getRoleInfos();
        if(roleInfoList==null){
            String excMsg="No role info provided.";
            throw new InvalidRelationTypeException(excMsg);
        }
        RoleInfo[] roleInfoArray=new RoleInfo[roleInfoList.size()];
        int i=0;
        for(RoleInfo currRoleInfo : roleInfoList){
            roleInfoArray[i]=currRoleInfo;
            i++;
        }
        // Can throw InvalidRelationTypeException
        RelationTypeSupport.checkRoleInfos(roleInfoArray);
        addRelationTypeInt(relationTypeObj);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "addRelationType");
        return;
    }

    public List<String> getAllRelationTypeNames(){
        ArrayList<String> result;
        synchronized(myRelType2ObjMap){
            result=new ArrayList<String>(myRelType2ObjMap.keySet());
        }
        return result;
    }

    public List<RoleInfo> getRoleInfos(String relationTypeName)
            throws IllegalArgumentException,
            RelationTypeNotFoundException{
        if(relationTypeName==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "getRoleInfos",relationTypeName);
        // Can throw a RelationTypeNotFoundException
        RelationType relType=getRelationType(relationTypeName);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "getRoleInfos");
        return relType.getRoleInfos();
    }

    public RoleInfo getRoleInfo(String relationTypeName,
                                String roleInfoName)
            throws IllegalArgumentException,
            RelationTypeNotFoundException,
            RoleInfoNotFoundException{
        if(relationTypeName==null||roleInfoName==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "getRoleInfo",new Object[]{relationTypeName,roleInfoName});
        // Can throw a RelationTypeNotFoundException
        RelationType relType=getRelationType(relationTypeName);
        // Can throw a RoleInfoNotFoundException
        RoleInfo roleInfo=relType.getRoleInfo(roleInfoName);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "getRoleInfo");
        return roleInfo;
    }

    public void removeRelationType(String relationTypeName)
            throws RelationServiceNotRegisteredException,
            IllegalArgumentException,
            RelationTypeNotFoundException{
        // Can throw RelationServiceNotRegisteredException
        isActive();
        if(relationTypeName==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "removeRelationType",relationTypeName);
        // Checks if the relation type to be removed exists
        // Can throw a RelationTypeNotFoundException
        RelationType relType=getRelationType(relationTypeName);
        // Retrieves the relation ids for relations of that type
        List<String> relIdList=null;
        synchronized(myRelType2RelIdsMap){
            // Note: take a copy of the list as it is a part of a map that
            //       will be updated by removeRelation() below.
            List<String> relIdList1=
                    myRelType2RelIdsMap.get(relationTypeName);
            if(relIdList1!=null){
                relIdList=new ArrayList<String>(relIdList1);
            }
        }
        // Removes the relation type from all maps
        synchronized(myRelType2ObjMap){
            myRelType2ObjMap.remove(relationTypeName);
        }
        synchronized(myRelType2RelIdsMap){
            myRelType2RelIdsMap.remove(relationTypeName);
        }
        // Removes all relations of that type
        if(relIdList!=null){
            for(String currRelId : relIdList){
                // Note: will remove it from myRelId2RelTypeMap :)
                //
                // Can throw RelationServiceNotRegisteredException (detected
                // above)
                // Shall not throw a RelationNotFoundException
                try{
                    removeRelation(currRelId);
                }catch(RelationNotFoundException exc1){
                    throw new RuntimeException(exc1.getMessage());
                }
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "removeRelationType");
        return;
    }
    //
    // Relation handling
    //

    public void createRelation(String relationId,
                               String relationTypeName,
                               RoleList roleList)
            throws RelationServiceNotRegisteredException,
            IllegalArgumentException,
            RoleNotFoundException,
            InvalidRelationIdException,
            RelationTypeNotFoundException,
            InvalidRoleValueException{
        // Can throw RelationServiceNotRegisteredException
        isActive();
        if(relationId==null||
                relationTypeName==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "createRelation",
                new Object[]{relationId,relationTypeName,roleList});
        // Creates RelationSupport object
        // Can throw InvalidRoleValueException
        RelationSupport relObj=new RelationSupport(relationId,
                myObjName,
                relationTypeName,
                roleList);
        // Adds relation object as a relation into the Relation Service
        // Can throw RoleNotFoundException, InvalidRelationId,
        // RelationTypeNotFoundException, InvalidRoleValueException
        //
        // Cannot throw MBeanException
        addRelationInt(true,
                relObj,
                null,
                relationId,
                relationTypeName,
                roleList);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "createRelation");
        return;
    }

    public void addRelation(ObjectName relationObjectName)
            throws IllegalArgumentException,
            RelationServiceNotRegisteredException,
            NoSuchMethodException,
            InvalidRelationIdException,
            InstanceNotFoundException,
            InvalidRelationServiceException,
            RelationTypeNotFoundException,
            RoleNotFoundException,
            InvalidRoleValueException{
        if(relationObjectName==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "addRelation",relationObjectName);
        // Can throw RelationServiceNotRegisteredException
        isActive();
        // Checks that the relation MBean implements the Relation interface.
        // It will also check that the provided ObjectName corresponds to a
        // registered MBean (else will throw an InstanceNotFoundException)
        if((!(myMBeanServer.isInstanceOf(relationObjectName,"javax.management.relation.Relation")))){
            String excMsg="This MBean does not implement the Relation interface.";
            throw new NoSuchMethodException(excMsg);
        }
        // Checks there is a relation id in the relation MBean (its uniqueness
        // is checked in addRelationInt())
        // Can throw InstanceNotFoundException (but detected above)
        // No MBeanException as no exception raised by this method, and no
        // ReflectionException
        String relId;
        try{
            relId=(String)(myMBeanServer.getAttribute(relationObjectName,
                    "RelationId"));
        }catch(MBeanException exc1){
            throw new RuntimeException(
                    (exc1.getTargetException()).getMessage());
        }catch(ReflectionException exc2){
            throw new RuntimeException(exc2.getMessage());
        }catch(AttributeNotFoundException exc3){
            throw new RuntimeException(exc3.getMessage());
        }
        if(relId==null){
            String excMsg="This MBean does not provide a relation id.";
            throw new InvalidRelationIdException(excMsg);
        }
        // Checks that the Relation Service where the relation MBean is
        // expected to be added is the current one
        // Can throw InstanceNotFoundException (but detected above)
        // No MBeanException as no exception raised by this method, no
        // ReflectionException
        ObjectName relServObjName;
        try{
            relServObjName=(ObjectName)
                    (myMBeanServer.getAttribute(relationObjectName,
                            "RelationServiceName"));
        }catch(MBeanException exc1){
            throw new RuntimeException(
                    (exc1.getTargetException()).getMessage());
        }catch(ReflectionException exc2){
            throw new RuntimeException(exc2.getMessage());
        }catch(AttributeNotFoundException exc3){
            throw new RuntimeException(exc3.getMessage());
        }
        boolean badRelServFlag=false;
        if(relServObjName==null){
            badRelServFlag=true;
        }else if(!(relServObjName.equals(myObjName))){
            badRelServFlag=true;
        }
        if(badRelServFlag){
            String excMsg="The Relation Service referenced in the MBean is not the current one.";
            throw new InvalidRelationServiceException(excMsg);
        }
        // Checks that a relation type has been specified for the relation
        // Can throw InstanceNotFoundException (but detected above)
        // No MBeanException as no exception raised by this method, no
        // ReflectionException
        String relTypeName;
        try{
            relTypeName=(String)(myMBeanServer.getAttribute(relationObjectName,
                    "RelationTypeName"));
        }catch(MBeanException exc1){
            throw new RuntimeException(
                    (exc1.getTargetException()).getMessage());
        }catch(ReflectionException exc2){
            throw new RuntimeException(exc2.getMessage());
        }catch(AttributeNotFoundException exc3){
            throw new RuntimeException(exc3.getMessage());
        }
        if(relTypeName==null){
            String excMsg="No relation type provided.";
            throw new RelationTypeNotFoundException(excMsg);
        }
        // Retrieves all roles without considering read mode
        // Can throw InstanceNotFoundException (but detected above)
        // No MBeanException as no exception raised by this method, no
        // ReflectionException
        RoleList roleList;
        try{
            roleList=(RoleList)(myMBeanServer.invoke(relationObjectName,
                    "retrieveAllRoles",
                    null,
                    null));
        }catch(MBeanException exc1){
            throw new RuntimeException(
                    (exc1.getTargetException()).getMessage());
        }catch(ReflectionException exc2){
            throw new RuntimeException(exc2.getMessage());
        }
        // Can throw RoleNotFoundException, InvalidRelationIdException,
        // RelationTypeNotFoundException, InvalidRoleValueException
        addRelationInt(false,
                null,
                relationObjectName,
                relId,
                relTypeName,
                roleList);
        // Adds relation MBean ObjectName in map
        synchronized(myRelMBeanObjName2RelIdMap){
            myRelMBeanObjName2RelIdMap.put(relationObjectName,relId);
        }
        // Updates flag to specify that the relation is managed by the Relation
        // Service
        // This flag and setter are inherited from RelationSupport and not parts
        // of the Relation interface, so may be not supported.
        try{
            myMBeanServer.setAttribute(relationObjectName,
                    new Attribute(
                            "RelationServiceManagementFlag",
                            Boolean.TRUE));
        }catch(Exception exc){
            // OK : The flag is not supported.
        }
        // Updates listener information to received notification for
        // unregistration of this MBean
        List<ObjectName> newRefList=new ArrayList<ObjectName>();
        newRefList.add(relationObjectName);
        updateUnregistrationListener(newRefList,null);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "addRelation");
        return;
    }

    public ObjectName isRelationMBean(String relationId)
            throws IllegalArgumentException,
            RelationNotFoundException{
        if(relationId==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "isRelationMBean",relationId);
        // Can throw RelationNotFoundException
        Object result=getRelation(relationId);
        if(result instanceof ObjectName){
            return ((ObjectName)result);
        }else{
            return null;
        }
    }

    public String isRelation(ObjectName objectName)
            throws IllegalArgumentException{
        if(objectName==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "isRelation",objectName);
        String result=null;
        synchronized(myRelMBeanObjName2RelIdMap){
            String relId=myRelMBeanObjName2RelIdMap.get(objectName);
            if(relId!=null){
                result=relId;
            }
        }
        return result;
    }

    public Boolean hasRelation(String relationId)
            throws IllegalArgumentException{
        if(relationId==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "hasRelation",relationId);
        try{
            // Can throw RelationNotFoundException
            Object result=getRelation(relationId);
            return true;
        }catch(RelationNotFoundException exc){
            return false;
        }
    }

    public List<String> getAllRelationIds(){
        List<String> result;
        synchronized(myRelId2ObjMap){
            result=new ArrayList<String>(myRelId2ObjMap.keySet());
        }
        return result;
    }

    public Integer checkRoleReading(String roleName,
                                    String relationTypeName)
            throws IllegalArgumentException,
            RelationTypeNotFoundException{
        if(roleName==null||relationTypeName==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "checkRoleReading",new Object[]{roleName,relationTypeName});
        Integer result;
        // Can throw a RelationTypeNotFoundException
        RelationType relType=getRelationType(relationTypeName);
        try{
            // Can throw a RoleInfoNotFoundException to be transformed into
            // returned value RoleStatus.NO_ROLE_WITH_NAME
            RoleInfo roleInfo=relType.getRoleInfo(roleName);
            result=checkRoleInt(1,
                    roleName,
                    null,
                    roleInfo,
                    false);
        }catch(RoleInfoNotFoundException exc){
            result=Integer.valueOf(RoleStatus.NO_ROLE_WITH_NAME);
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "checkRoleReading");
        return result;
    }

    public Integer checkRoleWriting(Role role,
                                    String relationTypeName,
                                    Boolean initFlag)
            throws IllegalArgumentException,
            RelationTypeNotFoundException{
        if(role==null||
                relationTypeName==null||
                initFlag==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "checkRoleWriting",
                new Object[]{role,relationTypeName,initFlag});
        // Can throw a RelationTypeNotFoundException
        RelationType relType=getRelationType(relationTypeName);
        String roleName=role.getRoleName();
        List<ObjectName> roleValue=role.getRoleValue();
        boolean writeChkFlag=true;
        if(initFlag.booleanValue()){
            writeChkFlag=false;
        }
        RoleInfo roleInfo;
        try{
            roleInfo=relType.getRoleInfo(roleName);
        }catch(RoleInfoNotFoundException exc){
            RELATION_LOGGER.exiting(RelationService.class.getName(),
                    "checkRoleWriting");
            return Integer.valueOf(RoleStatus.NO_ROLE_WITH_NAME);
        }
        Integer result=checkRoleInt(2,
                roleName,
                roleValue,
                roleInfo,
                writeChkFlag);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "checkRoleWriting");
        return result;
    }

    public void sendRelationCreationNotification(String relationId)
            throws IllegalArgumentException,
            RelationNotFoundException{
        if(relationId==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "sendRelationCreationNotification",relationId);
        // Message
        StringBuilder ntfMsg=new StringBuilder("Creation of relation ");
        ntfMsg.append(relationId);
        // Can throw RelationNotFoundException
        sendNotificationInt(1,
                ntfMsg.toString(),
                relationId,
                null,
                null,
                null,
                null);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "sendRelationCreationNotification");
        return;
    }

    public void sendRoleUpdateNotification(String relationId,
                                           Role newRole,
                                           List<ObjectName> oldValue)
            throws IllegalArgumentException,
            RelationNotFoundException{
        if(relationId==null||
                newRole==null||
                oldValue==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        if(!(oldValue instanceof ArrayList<?>))
            oldValue=new ArrayList<ObjectName>(oldValue);
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "sendRoleUpdateNotification",
                new Object[]{relationId,newRole,oldValue});
        String roleName=newRole.getRoleName();
        List<ObjectName> newRoleVal=newRole.getRoleValue();
        // Message
        String newRoleValString=Role.roleValueToString(newRoleVal);
        String oldRoleValString=Role.roleValueToString(oldValue);
        StringBuilder ntfMsg=new StringBuilder("Value of role ");
        ntfMsg.append(roleName);
        ntfMsg.append(" has changed\nOld value:\n");
        ntfMsg.append(oldRoleValString);
        ntfMsg.append("\nNew value:\n");
        ntfMsg.append(newRoleValString);
        // Can throw a RelationNotFoundException
        sendNotificationInt(2,
                ntfMsg.toString(),
                relationId,
                null,
                roleName,
                newRoleVal,
                oldValue);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "sendRoleUpdateNotification");
    }

    public void sendRelationRemovalNotification(String relationId,
                                                List<ObjectName> unregMBeanList)
            throws IllegalArgumentException,
            RelationNotFoundException{
        if(relationId==null){
            String excMsg="Invalid parameter";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "sendRelationRemovalNotification",
                new Object[]{relationId,unregMBeanList});
        // Can throw RelationNotFoundException
        sendNotificationInt(3,
                "Removal of relation "+relationId,
                relationId,
                unregMBeanList,
                null,
                null,
                null);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "sendRelationRemovalNotification");
        return;
    }

    public void updateRoleMap(String relationId,
                              Role newRole,
                              List<ObjectName> oldValue)
            throws IllegalArgumentException,
            RelationServiceNotRegisteredException,
            RelationNotFoundException{
        if(relationId==null||
                newRole==null||
                oldValue==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "updateRoleMap",new Object[]{relationId,newRole,oldValue});
        // Can throw RelationServiceNotRegisteredException
        isActive();
        // Verifies the relation has been added in the Relation Service
        // Can throw a RelationNotFoundException
        Object result=getRelation(relationId);
        String roleName=newRole.getRoleName();
        List<ObjectName> newRoleValue=newRole.getRoleValue();
        // Note: no need to test if oldValue not null before cloning,
        //       tested above.
        List<ObjectName> oldRoleValue=
                new ArrayList<ObjectName>(oldValue);
        // List of ObjectNames of new referenced MBeans
        List<ObjectName> newRefList=new ArrayList<ObjectName>();
        for(ObjectName currObjName : newRoleValue){
            // Checks if this ObjectName was already present in old value
            // Note: use copy (oldRoleValue) instead of original
            //       oldValue to speed up, as oldRoleValue is decreased
            //       by removing unchanged references :)
            int currObjNamePos=oldRoleValue.indexOf(currObjName);
            if(currObjNamePos==-1){
                // New reference to an ObjectName
                // Stores this reference into map
                // Returns true if new reference, false if MBean already
                // referenced
                boolean isNewFlag=addNewMBeanReference(currObjName,
                        relationId,
                        roleName);
                if(isNewFlag){
                    // Adds it into list of new reference
                    newRefList.add(currObjName);
                }
            }else{
                // MBean was already referenced in old value
                // Removes it from old value (local list) to ignore it when
                // looking for remove MBean references
                oldRoleValue.remove(currObjNamePos);
            }
        }
        // List of ObjectNames of MBeans no longer referenced
        List<ObjectName> obsRefList=new ArrayList<ObjectName>();
        // Each ObjectName remaining in oldRoleValue is an ObjectName no longer
        // referenced in new value
        for(ObjectName currObjName : oldRoleValue){
            // Removes MBean reference from map
            // Returns true if the MBean is no longer referenced in any
            // relation
            boolean noLongerRefFlag=removeMBeanReference(currObjName,
                    relationId,
                    roleName,
                    false);
            if(noLongerRefFlag){
                // Adds it into list of references to be removed
                obsRefList.add(currObjName);
            }
        }
        // To avoid having one listener per ObjectName of referenced MBean,
        // and to increase performances, there is only one listener recording
        // all ObjectNames of interest
        updateUnregistrationListener(newRefList,obsRefList);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "updateRoleMap");
        return;
    }

    public void removeRelation(String relationId)
            throws RelationServiceNotRegisteredException,
            IllegalArgumentException,
            RelationNotFoundException{
        // Can throw RelationServiceNotRegisteredException
        isActive();
        if(relationId==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "removeRelation",relationId);
        // Checks there is a relation with this id
        // Can throw RelationNotFoundException
        Object result=getRelation(relationId);
        // Removes it from listener filter
        if(result instanceof ObjectName){
            List<ObjectName> obsRefList=new ArrayList<ObjectName>();
            obsRefList.add((ObjectName)result);
            // Can throw a RelationServiceNotRegisteredException
            updateUnregistrationListener(null,obsRefList);
        }
        // Sends a notification
        // Note: has to be done FIRST as needs the relation to be still in the
        //       Relation Service
        // No RelationNotFoundException as checked above
        // Revisit [cebro] Handle CIM "Delete" and "IfDeleted" qualifiers:
        //   deleting the relation can mean to delete referenced MBeans. In
        //   that case, MBeans to be unregistered are put in a list sent along
        //   with the notification below
        // Can throw a RelationNotFoundException (but detected above)
        sendRelationRemovalNotification(relationId,null);
        // Removes the relation from various internal maps
        //  - MBean reference map
        // Retrieves the MBeans referenced in this relation
        // Note: here we cannot use removeMBeanReference() because it would
        //       require to know the MBeans referenced in the relation. For
        //       that it would be necessary to call 'getReferencedMBeans()'
        //       on the relation itself. Ok if it is an internal one, but if
        //       it is an MBean, it is possible it is already unregistered, so
        //       not available through the MBean Server.
        List<ObjectName> refMBeanList=new ArrayList<ObjectName>();
        // List of MBeans no longer referenced in any relation, to be
        // removed fom the map
        List<ObjectName> nonRefObjNameList=new ArrayList<ObjectName>();
        synchronized(myRefedMBeanObjName2RelIdsMap){
            for(ObjectName currRefObjName :
                    myRefedMBeanObjName2RelIdsMap.keySet()){
                // Retrieves relations where the MBean is referenced
                Map<String,List<String>> relIdMap=
                        myRefedMBeanObjName2RelIdsMap.get(currRefObjName);
                if(relIdMap.containsKey(relationId)){
                    relIdMap.remove(relationId);
                    refMBeanList.add(currRefObjName);
                }
                if(relIdMap.isEmpty()){
                    // MBean no longer referenced
                    // Note: do not remove it here because pointed by the
                    //       iterator!
                    nonRefObjNameList.add(currRefObjName);
                }
            }
            // Cleans MBean reference map by removing MBeans no longer
            // referenced
            for(ObjectName currRefObjName : nonRefObjNameList){
                myRefedMBeanObjName2RelIdsMap.remove(currRefObjName);
            }
        }
        // - Relation id to object map
        synchronized(myRelId2ObjMap){
            myRelId2ObjMap.remove(relationId);
        }
        if(result instanceof ObjectName){
            // - ObjectName to relation id map
            synchronized(myRelMBeanObjName2RelIdMap){
                myRelMBeanObjName2RelIdMap.remove((ObjectName)result);
            }
        }
        // Relation id to relation type name map
        // First retrieves the relation type name
        String relTypeName;
        synchronized(myRelId2RelTypeMap){
            relTypeName=myRelId2RelTypeMap.get(relationId);
            myRelId2RelTypeMap.remove(relationId);
        }
        // - Relation type name to relation id map
        synchronized(myRelType2RelIdsMap){
            List<String> relIdList=myRelType2RelIdsMap.get(relTypeName);
            if(relIdList!=null){
                // Can be null if called from removeRelationType()
                relIdList.remove(relationId);
                if(relIdList.isEmpty()){
                    // No other relation of that type
                    myRelType2RelIdsMap.remove(relTypeName);
                }
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "removeRelation");
        return;
    }

    public void purgeRelations()
            throws RelationServiceNotRegisteredException{
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "purgeRelations");
        // Can throw RelationServiceNotRegisteredException
        isActive();
        // Revisit [cebro] Handle the CIM "Delete" and "IfDeleted" qualifier:
        //    if the unregistered MBean has the "IfDeleted" qualifier,
        //    possible that the relation itself or other referenced MBeans
        //    have to be removed (then a notification would have to be sent
        //    to inform that they should be unregistered.
        // Clones the list of notifications to be able to still receive new
        // notifications while proceeding those ones
        List<MBeanServerNotification> localUnregNtfList;
        synchronized(myRefedMBeanObjName2RelIdsMap){
            localUnregNtfList=
                    new ArrayList<MBeanServerNotification>(myUnregNtfList);
            // Resets list
            myUnregNtfList=new ArrayList<MBeanServerNotification>();
        }
        // Updates the listener filter to avoid receiving notifications for
        // those MBeans again
        // Makes also a local "myRefedMBeanObjName2RelIdsMap" map, mapping
        // ObjectName -> relId -> roles, to remove the MBean from the global
        // map
        // List of references to be removed from the listener filter
        List<ObjectName> obsRefList=new ArrayList<ObjectName>();
        // Map including ObjectNames for unregistered MBeans, with
        // referencing relation ids and roles
        Map<ObjectName,Map<String,List<String>>> localMBean2RelIdMap=
                new HashMap<ObjectName,Map<String,List<String>>>();
        synchronized(myRefedMBeanObjName2RelIdsMap){
            for(MBeanServerNotification currNtf : localUnregNtfList){
                ObjectName unregMBeanName=currNtf.getMBeanName();
                // Adds the unregsitered MBean in the list of references to
                // remove from the listener filter
                obsRefList.add(unregMBeanName);
                // Retrieves the associated map of relation ids and roles
                Map<String,List<String>> relIdMap=
                        myRefedMBeanObjName2RelIdsMap.get(unregMBeanName);
                localMBean2RelIdMap.put(unregMBeanName,relIdMap);
                myRefedMBeanObjName2RelIdsMap.remove(unregMBeanName);
            }
        }
        // Updates the listener
        // Can throw RelationServiceNotRegisteredException
        updateUnregistrationListener(null,obsRefList);
        for(MBeanServerNotification currNtf : localUnregNtfList){
            ObjectName unregMBeanName=currNtf.getMBeanName();
            // Retrieves the relations where the MBean is referenced
            Map<String,List<String>> localRelIdMap=
                    localMBean2RelIdMap.get(unregMBeanName);
            // List of relation ids where the unregistered MBean is
            // referenced
            for(Map.Entry<String,List<String>> currRel :
                    localRelIdMap.entrySet()){
                final String currRelId=currRel.getKey();
                // List of roles of the relation where the MBean is
                // referenced
                List<String> localRoleNameList=currRel.getValue();
                // Checks if the relation has to be removed or not,
                // regarding expected minimum role cardinality and current
                // number of references after removal of the current one
                // If the relation is kept, calls
                // handleMBeanUnregistration() callback of the relation to
                // update it
                //
                // Can throw RelationServiceNotRegisteredException
                //
                // Shall not throw RelationNotFoundException,
                // RoleNotFoundException, MBeanException
                try{
                    handleReferenceUnregistration(currRelId,
                            unregMBeanName,
                            localRoleNameList);
                }catch(RelationNotFoundException exc1){
                    throw new RuntimeException(exc1.getMessage());
                }catch(RoleNotFoundException exc2){
                    throw new RuntimeException(exc2.getMessage());
                }
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "purgeRelations");
        return;
    }

    public Map<String,List<String>>
    findReferencingRelations(ObjectName mbeanName,
                             String relationTypeName,
                             String roleName)
            throws IllegalArgumentException{
        if(mbeanName==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "findReferencingRelations",
                new Object[]{mbeanName,relationTypeName,roleName});
        Map<String,List<String>> result=new HashMap<String,List<String>>();
        synchronized(myRefedMBeanObjName2RelIdsMap){
            // Retrieves the relations referencing the MBean
            Map<String,List<String>> relId2RoleNamesMap=
                    myRefedMBeanObjName2RelIdsMap.get(mbeanName);
            if(relId2RoleNamesMap!=null){
                // Relation Ids where the MBean is referenced
                Set<String> allRelIdSet=relId2RoleNamesMap.keySet();
                // List of relation ids of interest regarding the selected
                // relation type
                List<String> relIdList;
                if(relationTypeName==null){
                    // Considers all relations
                    relIdList=new ArrayList<String>(allRelIdSet);
                }else{
                    relIdList=new ArrayList<String>();
                    // Considers only the relation ids for relations of given
                    // type
                    for(String currRelId : allRelIdSet){
                        // Retrieves its relation type
                        String currRelTypeName;
                        synchronized(myRelId2RelTypeMap){
                            currRelTypeName=
                                    myRelId2RelTypeMap.get(currRelId);
                        }
                        if(currRelTypeName.equals(relationTypeName)){
                            relIdList.add(currRelId);
                        }
                    }
                }
                // Now looks at the roles where the MBean is expected to be
                // referenced
                for(String currRelId : relIdList){
                    // Retrieves list of role names where the MBean is
                    // referenced
                    List<String> currRoleNameList=
                            relId2RoleNamesMap.get(currRelId);
                    if(roleName==null){
                        // All roles to be considered
                        // Note: no need to test if list not null before
                        //       cloning, MUST be not null else bug :(
                        result.put(currRelId,
                                new ArrayList<String>(currRoleNameList));
                    }else if(currRoleNameList.contains(roleName)){
                        // Filters only the relations where the MBean is
                        // referenced in // given role
                        List<String> dummyList=new ArrayList<String>();
                        dummyList.add(roleName);
                        result.put(currRelId,dummyList);
                    }
                }
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "findReferencingRelations");
        return result;
    }

    public Map<ObjectName,List<String>>
    findAssociatedMBeans(ObjectName mbeanName,
                         String relationTypeName,
                         String roleName)
            throws IllegalArgumentException{
        if(mbeanName==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "findAssociatedMBeans",
                new Object[]{mbeanName,relationTypeName,roleName});
        // Retrieves the map <relation id> -> <role names> for those
        // criterias
        Map<String,List<String>> relId2RoleNamesMap=
                findReferencingRelations(mbeanName,
                        relationTypeName,
                        roleName);
        Map<ObjectName,List<String>> result=
                new HashMap<ObjectName,List<String>>();
        for(String currRelId : relId2RoleNamesMap.keySet()){
            // Retrieves ObjectNames of MBeans referenced in this relation
            //
            // Shall not throw a RelationNotFoundException if incorrect status
            // of maps :(
            Map<ObjectName,List<String>> objName2RoleNamesMap;
            try{
                objName2RoleNamesMap=getReferencedMBeans(currRelId);
            }catch(RelationNotFoundException exc){
                throw new RuntimeException(exc.getMessage());
            }
            // For each MBean associated to given one in a relation, adds the
            // association <ObjectName> -> <relation id> into result map
            for(ObjectName currObjName : objName2RoleNamesMap.keySet()){
                if(!(currObjName.equals(mbeanName))){
                    // Sees if this MBean is already associated to the given
                    // one in another relation
                    List<String> currRelIdList=result.get(currObjName);
                    if(currRelIdList==null){
                        currRelIdList=new ArrayList<String>();
                        currRelIdList.add(currRelId);
                        result.put(currObjName,currRelIdList);
                    }else{
                        currRelIdList.add(currRelId);
                    }
                }
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "findAssociatedMBeans");
        return result;
    }

    public List<String> findRelationsOfType(String relationTypeName)
            throws IllegalArgumentException,
            RelationTypeNotFoundException{
        if(relationTypeName==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "findRelationsOfType");
        // Can throw RelationTypeNotFoundException
        RelationType relType=getRelationType(relationTypeName);
        List<String> result;
        synchronized(myRelType2RelIdsMap){
            List<String> result1=myRelType2RelIdsMap.get(relationTypeName);
            if(result1==null)
                result=new ArrayList<String>();
            else
                result=new ArrayList<String>(result1);
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "findRelationsOfType");
        return result;
    }

    public List<ObjectName> getRole(String relationId,
                                    String roleName)
            throws RelationServiceNotRegisteredException,
            IllegalArgumentException,
            RelationNotFoundException,
            RoleNotFoundException{
        if(relationId==null||roleName==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "getRole",new Object[]{relationId,roleName});
        // Can throw RelationServiceNotRegisteredException
        isActive();
        // Can throw a RelationNotFoundException
        Object relObj=getRelation(relationId);
        List<ObjectName> result;
        if(relObj instanceof RelationSupport){
            // Internal relation
            // Can throw RoleNotFoundException
            result=cast(
                    ((RelationSupport)relObj).getRoleInt(roleName,
                            true,
                            this,
                            false));
        }else{
            // Relation MBean
            Object[] params=new Object[1];
            params[0]=roleName;
            String[] signature=new String[1];
            signature[0]="java.lang.String";
            // Can throw MBeanException wrapping a RoleNotFoundException:
            // throw wrapped exception
            //
            // Shall not throw InstanceNotFoundException or ReflectionException
            try{
                List<ObjectName> invokeResult=cast(
                        myMBeanServer.invoke(((ObjectName)relObj),
                                "getRole",
                                params,
                                signature));
                if(invokeResult==null||invokeResult instanceof ArrayList<?>)
                    result=invokeResult;
                else
                    result=new ArrayList<ObjectName>(invokeResult);
            }catch(InstanceNotFoundException exc1){
                throw new RuntimeException(exc1.getMessage());
            }catch(ReflectionException exc2){
                throw new RuntimeException(exc2.getMessage());
            }catch(MBeanException exc3){
                Exception wrappedExc=exc3.getTargetException();
                if(wrappedExc instanceof RoleNotFoundException){
                    throw ((RoleNotFoundException)wrappedExc);
                }else{
                    throw new RuntimeException(wrappedExc.getMessage());
                }
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),"getRole");
        return result;
    }

    public RoleResult getRoles(String relationId,
                               String[] roleNameArray)
            throws RelationServiceNotRegisteredException,
            IllegalArgumentException,
            RelationNotFoundException{
        if(relationId==null||roleNameArray==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "getRoles",relationId);
        // Can throw RelationServiceNotRegisteredException
        isActive();
        // Can throw a RelationNotFoundException
        Object relObj=getRelation(relationId);
        RoleResult result;
        if(relObj instanceof RelationSupport){
            // Internal relation
            result=((RelationSupport)relObj).getRolesInt(roleNameArray,
                    true,
                    this);
        }else{
            // Relation MBean
            Object[] params=new Object[1];
            params[0]=roleNameArray;
            String[] signature=new String[1];
            try{
                signature[0]=(roleNameArray.getClass()).getName();
            }catch(Exception exc){
                // OK : This is an array of java.lang.String
                //      so this should never happen...
            }
            // Shall not throw InstanceNotFoundException, ReflectionException
            // or MBeanException
            try{
                result=(RoleResult)
                        (myMBeanServer.invoke(((ObjectName)relObj),
                                "getRoles",
                                params,
                                signature));
            }catch(InstanceNotFoundException exc1){
                throw new RuntimeException(exc1.getMessage());
            }catch(ReflectionException exc2){
                throw new RuntimeException(exc2.getMessage());
            }catch(MBeanException exc3){
                throw new
                        RuntimeException((exc3.getTargetException()).getMessage());
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),"getRoles");
        return result;
    }

    public RoleResult getAllRoles(String relationId)
            throws IllegalArgumentException,
            RelationNotFoundException,
            RelationServiceNotRegisteredException{
        if(relationId==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "getRoles",relationId);
        // Can throw a RelationNotFoundException
        Object relObj=getRelation(relationId);
        RoleResult result;
        if(relObj instanceof RelationSupport){
            // Internal relation
            result=((RelationSupport)relObj).getAllRolesInt(true,this);
        }else{
            // Relation MBean
            // Shall not throw any Exception
            try{
                result=(RoleResult)
                        (myMBeanServer.getAttribute(((ObjectName)relObj),
                                "AllRoles"));
            }catch(Exception exc){
                throw new RuntimeException(exc.getMessage());
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),"getRoles");
        return result;
    }

    public Integer getRoleCardinality(String relationId,
                                      String roleName)
            throws IllegalArgumentException,
            RelationNotFoundException,
            RoleNotFoundException{
        if(relationId==null||roleName==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "getRoleCardinality",new Object[]{relationId,roleName});
        // Can throw a RelationNotFoundException
        Object relObj=getRelation(relationId);
        Integer result;
        if(relObj instanceof RelationSupport){
            // Internal relation
            // Can throw RoleNotFoundException
            result=((RelationSupport)relObj).getRoleCardinality(roleName);
        }else{
            // Relation MBean
            Object[] params=new Object[1];
            params[0]=roleName;
            String[] signature=new String[1];
            signature[0]="java.lang.String";
            // Can throw MBeanException wrapping RoleNotFoundException:
            // throw wrapped exception
            //
            // Shall not throw InstanceNotFoundException or ReflectionException
            try{
                result=(Integer)
                        (myMBeanServer.invoke(((ObjectName)relObj),
                                "getRoleCardinality",
                                params,
                                signature));
            }catch(InstanceNotFoundException exc1){
                throw new RuntimeException(exc1.getMessage());
            }catch(ReflectionException exc2){
                throw new RuntimeException(exc2.getMessage());
            }catch(MBeanException exc3){
                Exception wrappedExc=exc3.getTargetException();
                if(wrappedExc instanceof RoleNotFoundException){
                    throw ((RoleNotFoundException)wrappedExc);
                }else{
                    throw new RuntimeException(wrappedExc.getMessage());
                }
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "getRoleCardinality");
        return result;
    }

    public void setRole(String relationId,
                        Role role)
            throws RelationServiceNotRegisteredException,
            IllegalArgumentException,
            RelationNotFoundException,
            RoleNotFoundException,
            InvalidRoleValueException{
        if(relationId==null||role==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "setRole",new Object[]{relationId,role});
        // Can throw RelationServiceNotRegisteredException
        isActive();
        // Can throw a RelationNotFoundException
        Object relObj=getRelation(relationId);
        if(relObj instanceof RelationSupport){
            // Internal relation
            // Can throw RoleNotFoundException,
            // InvalidRoleValueException and
            // RelationServiceNotRegisteredException
            //
            // Shall not throw RelationTypeNotFoundException
            // (as relation exists in the RS, its relation type is known)
            try{
                ((RelationSupport)relObj).setRoleInt(role,
                        true,
                        this,
                        false);
            }catch(RelationTypeNotFoundException exc){
                throw new RuntimeException(exc.getMessage());
            }
        }else{
            // Relation MBean
            Object[] params=new Object[1];
            params[0]=role;
            String[] signature=new String[1];
            signature[0]="javax.management.relation.Role";
            // Can throw MBeanException wrapping RoleNotFoundException,
            // InvalidRoleValueException
            //
            // Shall not MBeanException wrapping an MBeanException wrapping
            // RelationTypeNotFoundException, or ReflectionException, or
            // InstanceNotFoundException
            try{
                myMBeanServer.setAttribute(((ObjectName)relObj),
                        new Attribute("Role",role));
            }catch(InstanceNotFoundException exc1){
                throw new RuntimeException(exc1.getMessage());
            }catch(ReflectionException exc3){
                throw new RuntimeException(exc3.getMessage());
            }catch(MBeanException exc2){
                Exception wrappedExc=exc2.getTargetException();
                if(wrappedExc instanceof RoleNotFoundException){
                    throw ((RoleNotFoundException)wrappedExc);
                }else if(wrappedExc instanceof InvalidRoleValueException){
                    throw ((InvalidRoleValueException)wrappedExc);
                }else{
                    throw new RuntimeException(wrappedExc.getMessage());
                }
            }catch(AttributeNotFoundException exc4){
                throw new RuntimeException(exc4.getMessage());
            }catch(InvalidAttributeValueException exc5){
                throw new RuntimeException(exc5.getMessage());
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),"setRole");
        return;
    }

    public RoleResult setRoles(String relationId,
                               RoleList roleList)
            throws RelationServiceNotRegisteredException,
            IllegalArgumentException,
            RelationNotFoundException{
        if(relationId==null||roleList==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "setRoles",new Object[]{relationId,roleList});
        // Can throw RelationServiceNotRegisteredException
        isActive();
        // Can throw a RelationNotFoundException
        Object relObj=getRelation(relationId);
        RoleResult result;
        if(relObj instanceof RelationSupport){
            // Internal relation
            // Can throw RelationServiceNotRegisteredException
            //
            // Shall not throw RelationTypeNotFoundException (as relation is
            // known, its relation type exists)
            try{
                result=((RelationSupport)relObj).setRolesInt(roleList,
                        true,
                        this);
            }catch(RelationTypeNotFoundException exc){
                throw new RuntimeException(exc.getMessage());
            }
        }else{
            // Relation MBean
            Object[] params=new Object[1];
            params[0]=roleList;
            String[] signature=new String[1];
            signature[0]="javax.management.relation.RoleList";
            // Shall not throw InstanceNotFoundException or an MBeanException
            // or ReflectionException
            try{
                result=(RoleResult)
                        (myMBeanServer.invoke(((ObjectName)relObj),
                                "setRoles",
                                params,
                                signature));
            }catch(InstanceNotFoundException exc1){
                throw new RuntimeException(exc1.getMessage());
            }catch(ReflectionException exc3){
                throw new RuntimeException(exc3.getMessage());
            }catch(MBeanException exc2){
                throw new
                        RuntimeException((exc2.getTargetException()).getMessage());
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),"setRoles");
        return result;
    }

    public Map<ObjectName,List<String>>
    getReferencedMBeans(String relationId)
            throws IllegalArgumentException,
            RelationNotFoundException{
        if(relationId==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "getReferencedMBeans",relationId);
        // Can throw a RelationNotFoundException
        Object relObj=getRelation(relationId);
        Map<ObjectName,List<String>> result;
        if(relObj instanceof RelationSupport){
            // Internal relation
            result=((RelationSupport)relObj).getReferencedMBeans();
        }else{
            // Relation MBean
            // No Exception
            try{
                result=cast(
                        myMBeanServer.getAttribute(((ObjectName)relObj),
                                "ReferencedMBeans"));
            }catch(Exception exc){
                throw new RuntimeException(exc.getMessage());
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "getReferencedMBeans");
        return result;
    }

    public String getRelationTypeName(String relationId)
            throws IllegalArgumentException,
            RelationNotFoundException{
        if(relationId==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "getRelationTypeName",relationId);
        // Can throw a RelationNotFoundException
        Object relObj=getRelation(relationId);
        String result;
        if(relObj instanceof RelationSupport){
            // Internal relation
            result=((RelationSupport)relObj).getRelationTypeName();
        }else{
            // Relation MBean
            // No Exception
            try{
                result=(String)
                        (myMBeanServer.getAttribute(((ObjectName)relObj),
                                "RelationTypeName"));
            }catch(Exception exc){
                throw new RuntimeException(exc.getMessage());
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "getRelationTypeName");
        return result;
    }
    //
    // NotificationListener Interface
    //


    //
    // NotificationBroadcaster interface
    //


    //
    // Misc
    //

    // Adds given object as a relation type.
    //
    // -param relationTypeObj  relation type object
    //
    // -exception IllegalArgumentException  if null parameter
    // -exception InvalidRelationTypeException  if there is already a relation
    //  type with that name
    private void addRelationTypeInt(RelationType relationTypeObj)
            throws IllegalArgumentException,
            InvalidRelationTypeException{
        if(relationTypeObj==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "addRelationTypeInt");
        String relTypeName=relationTypeObj.getRelationTypeName();
        // Checks that there is not already a relation type with that name
        // existing in the Relation Service
        try{
            // Can throw a RelationTypeNotFoundException (in fact should ;)
            RelationType relType=getRelationType(relTypeName);
            if(relType!=null){
                String excMsg="There is already a relation type in the Relation Service with name ";
                StringBuilder excMsgStrB=new StringBuilder(excMsg);
                excMsgStrB.append(relTypeName);
                throw new InvalidRelationTypeException(excMsgStrB.toString());
            }
        }catch(RelationTypeNotFoundException exc){
            // OK : The RelationType could not be found.
        }
        // Adds the relation type
        synchronized(myRelType2ObjMap){
            myRelType2ObjMap.put(relTypeName,relationTypeObj);
        }
        if(relationTypeObj instanceof RelationTypeSupport){
            ((RelationTypeSupport)relationTypeObj).setRelationServiceFlag(true);
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "addRelationTypeInt");
        return;
    }

    // Retrieves relation type with given name
    //
    // -param relationTypeName  expected name of a relation type created in the
    //  Relation Service
    //
    // -return RelationType object corresponding to given name
    //
    // -exception IllegalArgumentException  if null parameter
    // -exception RelationTypeNotFoundException  if no relation type for that
    //  name created in Relation Service
    //
    RelationType getRelationType(String relationTypeName)
            throws IllegalArgumentException,
            RelationTypeNotFoundException{
        if(relationTypeName==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "getRelationType",relationTypeName);
        // No null relation type accepted, so can use get()
        RelationType relType;
        synchronized(myRelType2ObjMap){
            relType=(myRelType2ObjMap.get(relationTypeName));
        }
        if(relType==null){
            String excMsg="No relation type created in the Relation Service with the name ";
            StringBuilder excMsgStrB=new StringBuilder(excMsg);
            excMsgStrB.append(relationTypeName);
            throw new RelationTypeNotFoundException(excMsgStrB.toString());
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "getRelationType");
        return relType;
    }

    // Retrieves relation corresponding to given relation id.
    // Returns either:
    // - a RelationSupport object if the relation is internal
    // or
    // - the ObjectName of the corresponding MBean
    //
    // -param relationId  expected relation id
    //
    // -return RelationSupport object or ObjectName of relation with given id
    //
    // -exception IllegalArgumentException  if null parameter
    // -exception RelationNotFoundException  if no relation for that
    //  relation id created in Relation Service
    //
    Object getRelation(String relationId)
            throws IllegalArgumentException,
            RelationNotFoundException{
        if(relationId==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "getRelation",relationId);
        // No null relation  accepted, so can use get()
        Object rel;
        synchronized(myRelId2ObjMap){
            rel=myRelId2ObjMap.get(relationId);
        }
        if(rel==null){
            String excMsg="No relation associated to relation id "+relationId;
            throw new RelationNotFoundException(excMsg);
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "getRelation");
        return rel;
    }

    // Adds a new MBean reference (reference to an ObjectName) in the
    // referenced MBean map (myRefedMBeanObjName2RelIdsMap).
    //
    // -param objectName  ObjectName of new referenced MBean
    // -param relationId  relation id of the relation where the MBean is
    //  referenced
    // -param roleName  name of the role where the MBean is referenced
    //
    // -return boolean:
    //  - true  if the MBean was not referenced before, so really a new
    //    reference
    //  - false else
    //
    // -exception IllegalArgumentException  if null parameter
    private boolean addNewMBeanReference(ObjectName objectName,
                                         String relationId,
                                         String roleName)
            throws IllegalArgumentException{
        if(objectName==null||
                relationId==null||
                roleName==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "addNewMBeanReference",
                new Object[]{objectName,relationId,roleName});
        boolean isNewFlag=false;
        synchronized(myRefedMBeanObjName2RelIdsMap){
            // Checks if the MBean was already referenced
            // No null value allowed, use get() directly
            Map<String,List<String>> mbeanRefMap=
                    myRefedMBeanObjName2RelIdsMap.get(objectName);
            if(mbeanRefMap==null){
                // MBean not referenced in any relation yet
                isNewFlag=true;
                // List of roles where the MBean is referenced in given
                // relation
                List<String> roleNames=new ArrayList<String>();
                roleNames.add(roleName);
                // Map of relations where the MBean is referenced
                mbeanRefMap=new HashMap<String,List<String>>();
                mbeanRefMap.put(relationId,roleNames);
                myRefedMBeanObjName2RelIdsMap.put(objectName,mbeanRefMap);
            }else{
                // MBean already referenced in at least another relation
                // Checks if already referenced in another role in current
                // relation
                List<String> roleNames=mbeanRefMap.get(relationId);
                if(roleNames==null){
                    // MBean not referenced in current relation
                    // List of roles where the MBean is referenced in given
                    // relation
                    roleNames=new ArrayList<String>();
                    roleNames.add(roleName);
                    // Adds new reference done in current relation
                    mbeanRefMap.put(relationId,roleNames);
                }else{
                    // MBean already referenced in current relation in another
                    // role
                    // Adds new reference done
                    roleNames.add(roleName);
                }
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "addNewMBeanReference");
        return isNewFlag;
    }

    // Removes an obsolete MBean reference (reference to an ObjectName) in
    // the referenced MBean map (myRefedMBeanObjName2RelIdsMap).
    //
    // -param objectName  ObjectName of MBean no longer referenced
    // -param relationId  relation id of the relation where the MBean was
    //  referenced
    // -param roleName  name of the role where the MBean was referenced
    // -param allRolesFlag  flag, if true removes reference to MBean for all
    //  roles in the relation, not only for the one above
    //
    // -return boolean:
    //  - true  if the MBean is no longer reference in any relation
    //  - false else
    //
    // -exception IllegalArgumentException  if null parameter
    private boolean removeMBeanReference(ObjectName objectName,
                                         String relationId,
                                         String roleName,
                                         boolean allRolesFlag)
            throws IllegalArgumentException{
        if(objectName==null||
                relationId==null||
                roleName==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "removeMBeanReference",
                new Object[]{objectName,relationId,roleName,allRolesFlag});
        boolean noLongerRefFlag=false;
        synchronized(myRefedMBeanObjName2RelIdsMap){
            // Retrieves the set of relations (designed via their relation ids)
            // where the MBean is referenced
            // Note that it is possible that the MBean has already been removed
            // from the internal map: this is the case when the MBean is
            // unregistered, the role is updated, then we arrive here.
            Map<String,List<String>> mbeanRefMap=
                    (myRefedMBeanObjName2RelIdsMap.get(objectName));
            if(mbeanRefMap==null){
                // The MBean is no longer referenced
                RELATION_LOGGER.exiting(RelationService.class.getName(),
                        "removeMBeanReference");
                return true;
            }
            List<String> roleNames=null;
            if(!allRolesFlag){
                // Now retrieves the roles of current relation where the MBean
                // was referenced
                roleNames=mbeanRefMap.get(relationId);
                // Removes obsolete reference to role
                int obsRefIdx=roleNames.indexOf(roleName);
                if(obsRefIdx!=-1){
                    roleNames.remove(obsRefIdx);
                }
            }
            // Checks if there is still at least one role in current relation
            // where the MBean is referenced
            if(roleNames.isEmpty()||allRolesFlag){
                // MBean no longer referenced in current relation: removes
                // entry
                mbeanRefMap.remove(relationId);
            }
            // Checks if the MBean is still referenced in at least on relation
            if(mbeanRefMap.isEmpty()){
                // MBean no longer referenced in any relation: removes entry
                myRefedMBeanObjName2RelIdsMap.remove(objectName);
                noLongerRefFlag=true;
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "removeMBeanReference");
        return noLongerRefFlag;
    }

    // Updates the listener registered to the MBean Server to be informed of
    // referenced MBean deregistrations
    //
    // -param newRefList  ArrayList of ObjectNames for new references done
    //  to MBeans (can be null)
    // -param obsoleteRefList  ArrayList of ObjectNames for obsolete references
    //  to MBeans (can be null)
    //
    // -exception RelationServiceNotRegisteredException  if the Relation
    //  Service is not registered in the MBean Server.
    private void updateUnregistrationListener(List<ObjectName> newRefList,
                                              List<ObjectName> obsoleteRefList)
            throws RelationServiceNotRegisteredException{
        if(newRefList!=null&&obsoleteRefList!=null){
            if(newRefList.isEmpty()&&obsoleteRefList.isEmpty()){
                // Nothing to do :)
                return;
            }
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "updateUnregistrationListener",
                new Object[]{newRefList,obsoleteRefList});
        // Can throw RelationServiceNotRegisteredException
        isActive();
        if(newRefList!=null||obsoleteRefList!=null){
            boolean newListenerFlag=false;
            if(myUnregNtfFilter==null){
                // Initialize it to be able to synchronise it :)
                myUnregNtfFilter=new MBeanServerNotificationFilter();
                newListenerFlag=true;
            }
            synchronized(myUnregNtfFilter){
                // Enables ObjectNames in newRefList
                if(newRefList!=null){
                    for(ObjectName newObjName : newRefList)
                        myUnregNtfFilter.enableObjectName(newObjName);
                }
                if(obsoleteRefList!=null){
                    // Disables ObjectNames in obsoleteRefList
                    for(ObjectName obsObjName : obsoleteRefList)
                        myUnregNtfFilter.disableObjectName(obsObjName);
                }
// Under test
                if(newListenerFlag){
                    try{
                        myMBeanServer.addNotificationListener(
                                MBeanServerDelegate.DELEGATE_NAME,
                                this,
                                myUnregNtfFilter,
                                null);
                    }catch(InstanceNotFoundException exc){
                        throw new
                                RelationServiceNotRegisteredException(exc.getMessage());
                    }
                }
// End test
//              if (!newListenerFlag) {
                // The Relation Service was already registered as a
                // listener:
                // removes it
                // Shall not throw InstanceNotFoundException (as the
                // MBean Server Delegate is expected to exist) or
                // ListenerNotFoundException (as it has been checked above
                // that the Relation Service is registered)
//                  try {
//                      myMBeanServer.removeNotificationListener(
//                              MBeanServerDelegate.DELEGATE_NAME,
//                              this);
//                  } catch (InstanceNotFoundException exc1) {
//                      throw new RuntimeException(exc1.getMessage());
//                  } catch (ListenerNotFoundException exc2) {
//                      throw new
//                          RelationServiceNotRegisteredException(exc2.getMessage());
//                  }
//              }
                // Adds Relation Service with current filter
                // Can throw InstanceNotFoundException if the Relation
                // Service is not registered, to be transformed into
                // RelationServiceNotRegisteredException
                //
                // Assume that there will not be any InstanceNotFoundException
                // for the MBean Server Delegate :)
//              try {
//                  myMBeanServer.addNotificationListener(
//                              MBeanServerDelegate.DELEGATE_NAME,
//                              this,
//                              myUnregNtfFilter,
//                              null);
//              } catch (InstanceNotFoundException exc) {
//                  throw new
//                     RelationServiceNotRegisteredException(exc.getMessage());
//              }
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "updateUnregistrationListener");
        return;
    }

    // Adds a relation (being either a RelationSupport object or an MBean
    // referenced using its ObjectName) in the Relation Service.
    // Will send a notification RelationNotification with type:
    // - RelationNotification.RELATION_BASIC_CREATION for internal relation
    //   creation
    // - RelationNotification.RELATION_MBEAN_CREATION for an MBean being added
    //   as a relation.
    //
    // -param relationBaseFlag  flag true if the relation is a RelationSupport
    //  object, false if it is an MBean
    // -param relationObj  RelationSupport object (if relation is internal)
    // -param relationObjName  ObjectName of the MBean to be added as a relation
    //  (only for the relation MBean)
    // -param relationId  relation identifier, to uniquely identify the relation
    //  inside the Relation Service
    // -param relationTypeName  name of the relation type (has to be created
    //  in the Relation Service)
    // -param roleList  role list to initialize roles of the relation
    //  (can be null)
    //
    // -exception IllegalArgumentException  if null paramater
    // -exception RelationServiceNotRegisteredException  if the Relation
    //  Service is not registered in the MBean Server
    // -exception RoleNotFoundException  if a value is provided for a role
    //  that does not exist in the relation type
    // -exception InvalidRelationIdException  if relation id already used
    // -exception RelationTypeNotFoundException  if relation type not known in
    //  Relation Service
    // -exception InvalidRoleValueException if:
    //  - the same role name is used for two different roles
    //  - the number of referenced MBeans in given value is less than
    //    expected minimum degree
    //  - the number of referenced MBeans in provided value exceeds expected
    //    maximum degree
    //  - one referenced MBean in the value is not an Object of the MBean
    //    class expected for that role
    //  - an MBean provided for that role does not exist
    private void addRelationInt(boolean relationBaseFlag,
                                RelationSupport relationObj,
                                ObjectName relationObjName,
                                String relationId,
                                String relationTypeName,
                                RoleList roleList)
            throws IllegalArgumentException,
            RelationServiceNotRegisteredException,
            RoleNotFoundException,
            InvalidRelationIdException,
            RelationTypeNotFoundException,
            InvalidRoleValueException{
        if(relationId==null||
                relationTypeName==null||
                (relationBaseFlag&&
                        (relationObj==null||
                                relationObjName!=null))||
                (!relationBaseFlag&&
                        (relationObjName==null||
                                relationObj!=null))){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "addRelationInt",new Object[]{relationBaseFlag,relationObj,
                        relationObjName,relationId,relationTypeName,roleList});
        // Can throw RelationServiceNotRegisteredException
        isActive();
        // Checks if there is already a relation with given id
        try{
            // Can throw a RelationNotFoundException (in fact should :)
            Object rel=getRelation(relationId);
            if(rel!=null){
                // There is already a relation with that id
                String excMsg="There is already a relation with id ";
                StringBuilder excMsgStrB=new StringBuilder(excMsg);
                excMsgStrB.append(relationId);
                throw new InvalidRelationIdException(excMsgStrB.toString());
            }
        }catch(RelationNotFoundException exc){
            // OK : The Relation could not be found.
        }
        // Retrieves the relation type
        // Can throw RelationTypeNotFoundException
        RelationType relType=getRelationType(relationTypeName);
        // Checks that each provided role conforms to its role info provided in
        // the relation type
        // First retrieves a local list of the role infos of the relation type
        // to see which roles have not been initialized
        // Note: no need to test if list not null before cloning, not allowed
        //       to have an empty relation type.
        List<RoleInfo> roleInfoList=new ArrayList<RoleInfo>(relType.getRoleInfos());
        if(roleList!=null){
            for(Role currRole : roleList.asList()){
                String currRoleName=currRole.getRoleName();
                List<ObjectName> currRoleValue=currRole.getRoleValue();
                // Retrieves corresponding role info
                // Can throw a RoleInfoNotFoundException to be converted into a
                // RoleNotFoundException
                RoleInfo roleInfo;
                try{
                    roleInfo=relType.getRoleInfo(currRoleName);
                }catch(RoleInfoNotFoundException exc){
                    throw new RoleNotFoundException(exc.getMessage());
                }
                // Checks that role conforms to role info,
                Integer status=checkRoleInt(2,
                        currRoleName,
                        currRoleValue,
                        roleInfo,
                        false);
                int pbType=status.intValue();
                if(pbType!=0){
                    // A problem has occurred: throws appropriate exception
                    // here InvalidRoleValueException
                    throwRoleProblemException(pbType,currRoleName);
                }
                // Removes role info for that list from list of role infos for
                // roles to be defaulted
                int roleInfoIdx=roleInfoList.indexOf(roleInfo);
                // Note: no need to check if != -1, MUST be there :)
                roleInfoList.remove(roleInfoIdx);
            }
        }
        // Initializes roles not initialized by roleList
        // Can throw InvalidRoleValueException
        initializeMissingRoles(relationBaseFlag,
                relationObj,
                relationObjName,
                relationId,
                relationTypeName,
                roleInfoList);
        // Creation of relation successfull!!!!
        // Updates internal maps
        // Relation id to object map
        synchronized(myRelId2ObjMap){
            if(relationBaseFlag){
                // Note: do not clone relation object, created by us :)
                myRelId2ObjMap.put(relationId,relationObj);
            }else{
                myRelId2ObjMap.put(relationId,relationObjName);
            }
        }
        // Relation id to relation type name map
        synchronized(myRelId2RelTypeMap){
            myRelId2RelTypeMap.put(relationId,
                    relationTypeName);
        }
        // Relation type to relation id map
        synchronized(myRelType2RelIdsMap){
            List<String> relIdList=
                    myRelType2RelIdsMap.get(relationTypeName);
            boolean firstRelFlag=false;
            if(relIdList==null){
                firstRelFlag=true;
                relIdList=new ArrayList<String>();
            }
            relIdList.add(relationId);
            if(firstRelFlag){
                myRelType2RelIdsMap.put(relationTypeName,relIdList);
            }
        }
        // Referenced MBean to relation id map
        // Only role list parameter used, as default initialization of roles
        // done automatically in initializeMissingRoles() sets each
        // uninitialized role to an empty value.
        for(Role currRole : roleList.asList()){
            // Creates a dummy empty ArrayList of ObjectNames to be the old
            // role value :)
            List<ObjectName> dummyList=new ArrayList<ObjectName>();
            // Will not throw a RelationNotFoundException (as the RelId2Obj map
            // has been updated above) so catch it :)
            try{
                updateRoleMap(relationId,currRole,dummyList);
            }catch(RelationNotFoundException exc){
                // OK : The Relation could not be found.
            }
        }
        // Sends a notification for relation creation
        // Will not throw RelationNotFoundException so catch it :)
        try{
            sendRelationCreationNotification(relationId);
        }catch(RelationNotFoundException exc){
            // OK : The Relation could not be found.
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "addRelationInt");
        return;
    }

    // Checks that given role conforms to given role info.
    //
    // -param chkType  type of check:
    //  - 1: read, just check read access
    //  - 2: write, check value and write access if writeChkFlag
    // -param roleName  role name
    // -param roleValue  role value
    // -param roleInfo  corresponding role info
    // -param writeChkFlag  boolean to specify a current write access and
    //  to check it
    //
    // -return Integer with value:
    //  - 0: ok
    //  - RoleStatus.NO_ROLE_WITH_NAME
    //  - RoleStatus.ROLE_NOT_READABLE
    //  - RoleStatus.ROLE_NOT_WRITABLE
    //  - RoleStatus.LESS_THAN_MIN_ROLE_DEGREE
    //  - RoleStatus.MORE_THAN_MAX_ROLE_DEGREE
    //  - RoleStatus.REF_MBEAN_OF_INCORRECT_CLASS
    //  - RoleStatus.REF_MBEAN_NOT_REGISTERED
    //
    // -exception IllegalArgumentException  if null parameter
    private Integer checkRoleInt(int chkType,
                                 String roleName,
                                 List<ObjectName> roleValue,
                                 RoleInfo roleInfo,
                                 boolean writeChkFlag)
            throws IllegalArgumentException{
        if(roleName==null||
                roleInfo==null||
                (chkType==2&&roleValue==null)){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "checkRoleInt",new Object[]{chkType,roleName,
                        roleValue,roleInfo,writeChkFlag});
        // Compares names
        String expName=roleInfo.getName();
        if(!(roleName.equals(expName))){
            RELATION_LOGGER.exiting(RelationService.class.getName(),
                    "checkRoleInt");
            return Integer.valueOf(RoleStatus.NO_ROLE_WITH_NAME);
        }
        // Checks read access if required
        if(chkType==1){
            boolean isReadable=roleInfo.isReadable();
            if(!isReadable){
                RELATION_LOGGER.exiting(RelationService.class.getName(),
                        "checkRoleInt");
                return Integer.valueOf(RoleStatus.ROLE_NOT_READABLE);
            }else{
                // End of check :)
                RELATION_LOGGER.exiting(RelationService.class.getName(),
                        "checkRoleInt");
                return new Integer(0);
            }
        }
        // Checks write access if required
        if(writeChkFlag){
            boolean isWritable=roleInfo.isWritable();
            if(!isWritable){
                RELATION_LOGGER.exiting(RelationService.class.getName(),
                        "checkRoleInt");
                return new Integer(RoleStatus.ROLE_NOT_WRITABLE);
            }
        }
        int refNbr=roleValue.size();
        // Checks minimum cardinality
        boolean chkMinFlag=roleInfo.checkMinDegree(refNbr);
        if(!chkMinFlag){
            RELATION_LOGGER.exiting(RelationService.class.getName(),
                    "checkRoleInt");
            return new Integer(RoleStatus.LESS_THAN_MIN_ROLE_DEGREE);
        }
        // Checks maximum cardinality
        boolean chkMaxFlag=roleInfo.checkMaxDegree(refNbr);
        if(!chkMaxFlag){
            RELATION_LOGGER.exiting(RelationService.class.getName(),
                    "checkRoleInt");
            return new Integer(RoleStatus.MORE_THAN_MAX_ROLE_DEGREE);
        }
        // Verifies that each referenced MBean is registered in the MBean
        // Server and that it is an instance of the class specified in the
        // role info, or of a subclass of it
        // Note that here again this is under the assumption that
        // referenced MBeans, relation MBeans and the Relation Service are
        // registered in the same MBean Server.
        String expClassName=roleInfo.getRefMBeanClassName();
        for(ObjectName currObjName : roleValue){
            // Checks it is registered
            if(currObjName==null){
                RELATION_LOGGER.exiting(RelationService.class.getName(),
                        "checkRoleInt");
                return new Integer(RoleStatus.REF_MBEAN_NOT_REGISTERED);
            }
            // Checks if it is of the correct class
            // Can throw an InstanceNotFoundException, if MBean not registered
            try{
                boolean classSts=myMBeanServer.isInstanceOf(currObjName,
                        expClassName);
                if(!classSts){
                    RELATION_LOGGER.exiting(RelationService.class.getName(),
                            "checkRoleInt");
                    return new Integer(RoleStatus.REF_MBEAN_OF_INCORRECT_CLASS);
                }
            }catch(InstanceNotFoundException exc){
                RELATION_LOGGER.exiting(RelationService.class.getName(),
                        "checkRoleInt");
                return new Integer(RoleStatus.REF_MBEAN_NOT_REGISTERED);
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "checkRoleInt");
        return new Integer(0);
    }
    // Initializes roles associated to given role infos to default value (empty
    // ArrayList of ObjectNames) in given relation.
    // It will succeed for every role except if the role info has a minimum
    // cardinality greater than 0. In that case, an InvalidRoleValueException
    // will be raised.
    //
    // -param relationBaseFlag  flag true if the relation is a RelationSupport
    //  object, false if it is an MBean
    // -param relationObj  RelationSupport object (if relation is internal)
    // -param relationObjName  ObjectName of the MBean to be added as a relation
    //  (only for the relation MBean)
    // -param relationId  relation id
    // -param relationTypeName  name of the relation type (has to be created
    //  in the Relation Service)
    // -param roleInfoList  list of role infos for roles to be defaulted
    //
    // -exception IllegalArgumentException  if null paramater
    // -exception RelationServiceNotRegisteredException  if the Relation
    //  Service is not registered in the MBean Server
    // -exception InvalidRoleValueException  if role must have a non-empty
    //  value

    // Revisit [cebro] Handle CIM qualifiers as REQUIRED to detect roles which
    //    should have been initialized by the user
    private void initializeMissingRoles(boolean relationBaseFlag,
                                        RelationSupport relationObj,
                                        ObjectName relationObjName,
                                        String relationId,
                                        String relationTypeName,
                                        List<RoleInfo> roleInfoList)
            throws IllegalArgumentException,
            RelationServiceNotRegisteredException,
            InvalidRoleValueException{
        if((relationBaseFlag&&
                (relationObj==null||
                        relationObjName!=null))||
                (!relationBaseFlag&&
                        (relationObjName==null||
                                relationObj!=null))||
                relationId==null||
                relationTypeName==null||
                roleInfoList==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "initializeMissingRoles",new Object[]{relationBaseFlag,
                        relationObj,relationObjName,relationId,relationTypeName,
                        roleInfoList});
        // Can throw RelationServiceNotRegisteredException
        isActive();
        // For each role info (corresponding to a role not initialized by the
        // role list provided by the user), try to set in the relation a role
        // with an empty list of ObjectNames.
        // A check is performed to verify that the role can be set to an
        // empty value, according to its minimum cardinality
        for(RoleInfo currRoleInfo : roleInfoList){
            String roleName=currRoleInfo.getName();
            // Creates an empty value
            List<ObjectName> emptyValue=new ArrayList<ObjectName>();
            // Creates a role
            Role role=new Role(roleName,emptyValue);
            if(relationBaseFlag){
                // Internal relation
                // Can throw InvalidRoleValueException
                //
                // Will not throw RoleNotFoundException (role to be
                // initialized), or RelationNotFoundException, or
                // RelationTypeNotFoundException
                try{
                    relationObj.setRoleInt(role,true,this,false);
                }catch(RoleNotFoundException exc1){
                    throw new RuntimeException(exc1.getMessage());
                }catch(RelationNotFoundException exc2){
                    throw new RuntimeException(exc2.getMessage());
                }catch(RelationTypeNotFoundException exc3){
                    throw new RuntimeException(exc3.getMessage());
                }
            }else{
                // Relation is an MBean
                // Use standard setRole()
                Object[] params=new Object[1];
                params[0]=role;
                String[] signature=new String[1];
                signature[0]="javax.management.relation.Role";
                // Can throw MBeanException wrapping
                // InvalidRoleValueException. Returns the target exception to
                // be homogeneous.
                //
                // Will not throw MBeanException (wrapping
                // RoleNotFoundException or MBeanException) or
                // InstanceNotFoundException, or ReflectionException
                //
                // Again here the assumption is that the Relation Service and
                // the relation MBeans are registered in the same MBean Server.
                try{
                    myMBeanServer.setAttribute(relationObjName,
                            new Attribute("Role",role));
                }catch(InstanceNotFoundException exc1){
                    throw new RuntimeException(exc1.getMessage());
                }catch(ReflectionException exc3){
                    throw new RuntimeException(exc3.getMessage());
                }catch(MBeanException exc2){
                    Exception wrappedExc=exc2.getTargetException();
                    if(wrappedExc instanceof InvalidRoleValueException){
                        throw ((InvalidRoleValueException)wrappedExc);
                    }else{
                        throw new RuntimeException(wrappedExc.getMessage());
                    }
                }catch(AttributeNotFoundException exc4){
                    throw new RuntimeException(exc4.getMessage());
                }catch(InvalidAttributeValueException exc5){
                    throw new RuntimeException(exc5.getMessage());
                }
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "initializeMissingRoles");
        return;
    }

    // Throws an exception corresponding to a given problem type
    //
    // -param pbType  possible problem, defined in RoleUnresolved
    // -param roleName  role name
    //
    // -exception IllegalArgumentException  if null parameter
    // -exception RoleNotFoundException  for problems:
    //  - NO_ROLE_WITH_NAME
    //  - ROLE_NOT_READABLE
    //  - ROLE_NOT_WRITABLE
    // -exception InvalidRoleValueException  for problems:
    //  - LESS_THAN_MIN_ROLE_DEGREE
    //  - MORE_THAN_MAX_ROLE_DEGREE
    //  - REF_MBEAN_OF_INCORRECT_CLASS
    //  - REF_MBEAN_NOT_REGISTERED
    static void throwRoleProblemException(int pbType,
                                          String roleName)
            throws IllegalArgumentException,
            RoleNotFoundException,
            InvalidRoleValueException{
        if(roleName==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        // Exception type: 1 = RoleNotFoundException
        //                 2 = InvalidRoleValueException
        int excType=0;
        String excMsgPart=null;
        switch(pbType){
            case RoleStatus.NO_ROLE_WITH_NAME:
                excMsgPart=" does not exist in relation.";
                excType=1;
                break;
            case RoleStatus.ROLE_NOT_READABLE:
                excMsgPart=" is not readable.";
                excType=1;
                break;
            case RoleStatus.ROLE_NOT_WRITABLE:
                excMsgPart=" is not writable.";
                excType=1;
                break;
            case RoleStatus.LESS_THAN_MIN_ROLE_DEGREE:
                excMsgPart=" has a number of MBean references less than the expected minimum degree.";
                excType=2;
                break;
            case RoleStatus.MORE_THAN_MAX_ROLE_DEGREE:
                excMsgPart=" has a number of MBean references greater than the expected maximum degree.";
                excType=2;
                break;
            case RoleStatus.REF_MBEAN_OF_INCORRECT_CLASS:
                excMsgPart=" has an MBean reference to an MBean not of the expected class of references for that role.";
                excType=2;
                break;
            case RoleStatus.REF_MBEAN_NOT_REGISTERED:
                excMsgPart=" has a reference to null or to an MBean not registered.";
                excType=2;
                break;
        }
        // No default as we must have been in one of those cases
        StringBuilder excMsgStrB=new StringBuilder(roleName);
        excMsgStrB.append(excMsgPart);
        String excMsg=excMsgStrB.toString();
        if(excType==1){
            throw new RoleNotFoundException(excMsg);
        }else if(excType==2){
            throw new InvalidRoleValueException(excMsg);
        }
    }

    // Sends a notification of given type, with given parameters
    //
    // -param intNtfType  integer to represent notification type:
    //  - 1 : create
    //  - 2 : update
    //  - 3 : delete
    // -param message  human-readable message
    // -param relationId  relation id of the created/updated/deleted relation
    // -param unregMBeanList  list of ObjectNames of referenced MBeans
    //  expected to be unregistered due to relation removal (only for removal,
    //  due to CIM qualifiers, can be null)
    // -param roleName  role name
    // -param roleNewValue  role new value (ArrayList of ObjectNames)
    // -param oldValue  old role value (ArrayList of ObjectNames)
    //
    // -exception IllegalArgument  if null parameter
    // -exception RelationNotFoundException  if no relation for given id
    private void sendNotificationInt(int intNtfType,
                                     String message,
                                     String relationId,
                                     List<ObjectName> unregMBeanList,
                                     String roleName,
                                     List<ObjectName> roleNewValue,
                                     List<ObjectName> oldValue)
            throws IllegalArgumentException,
            RelationNotFoundException{
        if(message==null||
                relationId==null||
                (intNtfType!=3&&unregMBeanList!=null)||
                (intNtfType==2&&
                        (roleName==null||
                                roleNewValue==null||
                                oldValue==null))){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "sendNotificationInt",new Object[]{intNtfType,message,
                        relationId,unregMBeanList,roleName,roleNewValue,oldValue});
        // Relation type name
        // Note: do not use getRelationTypeName() as if it is a relation MBean
        //       it is already unregistered.
        String relTypeName;
        synchronized(myRelId2RelTypeMap){
            relTypeName=(myRelId2RelTypeMap.get(relationId));
        }
        // ObjectName (for a relation MBean)
        // Can also throw a RelationNotFoundException, but detected above
        ObjectName relObjName=isRelationMBean(relationId);
        String ntfType=null;
        if(relObjName!=null){
            switch(intNtfType){
                case 1:
                    ntfType=RelationNotification.RELATION_MBEAN_CREATION;
                    break;
                case 2:
                    ntfType=RelationNotification.RELATION_MBEAN_UPDATE;
                    break;
                case 3:
                    ntfType=RelationNotification.RELATION_MBEAN_REMOVAL;
                    break;
            }
        }else{
            switch(intNtfType){
                case 1:
                    ntfType=RelationNotification.RELATION_BASIC_CREATION;
                    break;
                case 2:
                    ntfType=RelationNotification.RELATION_BASIC_UPDATE;
                    break;
                case 3:
                    ntfType=RelationNotification.RELATION_BASIC_REMOVAL;
                    break;
            }
        }
        // Sequence number
        Long seqNo=atomicSeqNo.incrementAndGet();
        // Timestamp
        Date currDate=new Date();
        long timeStamp=currDate.getTime();
        RelationNotification ntf=null;
        if(ntfType.equals(RelationNotification.RELATION_BASIC_CREATION)||
                ntfType.equals(RelationNotification.RELATION_MBEAN_CREATION)||
                ntfType.equals(RelationNotification.RELATION_BASIC_REMOVAL)||
                ntfType.equals(RelationNotification.RELATION_MBEAN_REMOVAL))
            // Creation or removal
            ntf=new RelationNotification(ntfType,
                    this,
                    seqNo.longValue(),
                    timeStamp,
                    message,
                    relationId,
                    relTypeName,
                    relObjName,
                    unregMBeanList);
        else if(ntfType.equals(RelationNotification.RELATION_BASIC_UPDATE)
                ||
                ntfType.equals(RelationNotification.RELATION_MBEAN_UPDATE)){
            // Update
            ntf=new RelationNotification(ntfType,
                    this,
                    seqNo.longValue(),
                    timeStamp,
                    message,
                    relationId,
                    relTypeName,
                    relObjName,
                    roleName,
                    roleNewValue,
                    oldValue);
        }
        sendNotification(ntf);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "sendNotificationInt");
        return;
    }


}
