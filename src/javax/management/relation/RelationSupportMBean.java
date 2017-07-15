/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.relation;

public interface RelationSupportMBean
        extends Relation{
    public Boolean isInRelationService();

    public void setRelationServiceManagementFlag(Boolean flag)
            throws IllegalArgumentException;
}
