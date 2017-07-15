/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.relation;

import javax.management.ObjectName;
import java.util.List;
import java.util.Map;

public interface Relation{
    public List<ObjectName> getRole(String roleName)
            throws IllegalArgumentException,
            RoleNotFoundException,
            RelationServiceNotRegisteredException;

    public RoleResult getRoles(String[] roleNameArray)
            throws IllegalArgumentException,
            RelationServiceNotRegisteredException;

    public Integer getRoleCardinality(String roleName)
            throws IllegalArgumentException,
            RoleNotFoundException;

    public RoleResult getAllRoles()
            throws RelationServiceNotRegisteredException;

    public RoleList retrieveAllRoles();

    public void setRole(Role role)
            throws IllegalArgumentException,
            RoleNotFoundException,
            RelationTypeNotFoundException,
            InvalidRoleValueException,
            RelationServiceNotRegisteredException,
            RelationNotFoundException;

    public RoleResult setRoles(RoleList roleList)
            throws IllegalArgumentException,
            RelationServiceNotRegisteredException,
            RelationTypeNotFoundException,
            RelationNotFoundException;

    public void handleMBeanUnregistration(ObjectName objectName,
                                          String roleName)
            throws IllegalArgumentException,
            RoleNotFoundException,
            InvalidRoleValueException,
            RelationServiceNotRegisteredException,
            RelationTypeNotFoundException,
            RelationNotFoundException;

    public Map<ObjectName,List<String>> getReferencedMBeans();

    public String getRelationTypeName();

    public ObjectName getRelationServiceName();

    public String getRelationId();
}
