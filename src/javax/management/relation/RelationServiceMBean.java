/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.relation;

import javax.management.InstanceNotFoundException;
import javax.management.ObjectName;
import java.util.List;
import java.util.Map;

public interface RelationServiceMBean{
    public void isActive()
            throws RelationServiceNotRegisteredException;
    //
    // Accessors
    //

    public boolean getPurgeFlag();

    public void setPurgeFlag(boolean purgeFlag);
    //
    // Relation type handling
    //

    public void createRelationType(String relationTypeName,
                                   RoleInfo[] roleInfoArray)
            throws IllegalArgumentException,
            InvalidRelationTypeException;

    public void addRelationType(RelationType relationTypeObj)
            throws IllegalArgumentException,
            InvalidRelationTypeException;

    public List<String> getAllRelationTypeNames();

    public List<RoleInfo> getRoleInfos(String relationTypeName)
            throws IllegalArgumentException,
            RelationTypeNotFoundException;

    public RoleInfo getRoleInfo(String relationTypeName,
                                String roleInfoName)
            throws IllegalArgumentException,
            RelationTypeNotFoundException,
            RoleInfoNotFoundException;

    public void removeRelationType(String relationTypeName)
            throws RelationServiceNotRegisteredException,
            IllegalArgumentException,
            RelationTypeNotFoundException;
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
            InvalidRoleValueException;

    public void addRelation(ObjectName relationObjectName)
            throws IllegalArgumentException,
            RelationServiceNotRegisteredException,
            NoSuchMethodException,
            InvalidRelationIdException,
            InstanceNotFoundException,
            InvalidRelationServiceException,
            RelationTypeNotFoundException,
            RoleNotFoundException,
            InvalidRoleValueException;

    public ObjectName isRelationMBean(String relationId)
            throws IllegalArgumentException,
            RelationNotFoundException;

    public String isRelation(ObjectName objectName)
            throws IllegalArgumentException;

    public Boolean hasRelation(String relationId)
            throws IllegalArgumentException;

    public List<String> getAllRelationIds();

    public Integer checkRoleReading(String roleName,
                                    String relationTypeName)
            throws IllegalArgumentException,
            RelationTypeNotFoundException;

    public Integer checkRoleWriting(Role role,
                                    String relationTypeName,
                                    Boolean initFlag)
            throws IllegalArgumentException,
            RelationTypeNotFoundException;

    public void sendRelationCreationNotification(String relationId)
            throws IllegalArgumentException,
            RelationNotFoundException;

    public void sendRoleUpdateNotification(String relationId,
                                           Role newRole,
                                           List<ObjectName> oldRoleValue)
            throws IllegalArgumentException,
            RelationNotFoundException;

    public void sendRelationRemovalNotification(String relationId,
                                                List<ObjectName> unregMBeanList)
            throws IllegalArgumentException,
            RelationNotFoundException;

    public void updateRoleMap(String relationId,
                              Role newRole,
                              List<ObjectName> oldRoleValue)
            throws IllegalArgumentException,
            RelationServiceNotRegisteredException,
            RelationNotFoundException;

    public void removeRelation(String relationId)
            throws RelationServiceNotRegisteredException,
            IllegalArgumentException,
            RelationNotFoundException;

    public void purgeRelations()
            throws RelationServiceNotRegisteredException;

    public Map<String,List<String>>
    findReferencingRelations(ObjectName mbeanName,
                             String relationTypeName,
                             String roleName)
            throws IllegalArgumentException;

    public Map<ObjectName,List<String>>
    findAssociatedMBeans(ObjectName mbeanName,
                         String relationTypeName,
                         String roleName)
            throws IllegalArgumentException;

    public List<String> findRelationsOfType(String relationTypeName)
            throws IllegalArgumentException,
            RelationTypeNotFoundException;

    public List<ObjectName> getRole(String relationId,
                                    String roleName)
            throws RelationServiceNotRegisteredException,
            IllegalArgumentException,
            RelationNotFoundException,
            RoleNotFoundException;

    public RoleResult getRoles(String relationId,
                               String[] roleNameArray)
            throws RelationServiceNotRegisteredException,
            IllegalArgumentException,
            RelationNotFoundException;

    public RoleResult getAllRoles(String relationId)
            throws IllegalArgumentException,
            RelationNotFoundException,
            RelationServiceNotRegisteredException;

    public Integer getRoleCardinality(String relationId,
                                      String roleName)
            throws IllegalArgumentException,
            RelationNotFoundException,
            RoleNotFoundException;

    public void setRole(String relationId,
                        Role role)
            throws RelationServiceNotRegisteredException,
            IllegalArgumentException,
            RelationNotFoundException,
            RoleNotFoundException,
            InvalidRoleValueException,
            RelationTypeNotFoundException;

    public RoleResult setRoles(String relationId,
                               RoleList roleList)
            throws RelationServiceNotRegisteredException,
            IllegalArgumentException,
            RelationNotFoundException;

    public Map<ObjectName,List<String>> getReferencedMBeans(String relationId)
            throws IllegalArgumentException,
            RelationNotFoundException;

    public String getRelationTypeName(String relationId)
            throws IllegalArgumentException,
            RelationNotFoundException;
}
