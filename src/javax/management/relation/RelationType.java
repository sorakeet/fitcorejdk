/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.relation;

import java.io.Serializable;
import java.util.List;

public interface RelationType extends Serializable{
    //
    // Accessors
    //

    public String getRelationTypeName();

    public List<RoleInfo> getRoleInfos();

    public RoleInfo getRoleInfo(String roleInfoName)
            throws IllegalArgumentException,
            RoleInfoNotFoundException;
}
