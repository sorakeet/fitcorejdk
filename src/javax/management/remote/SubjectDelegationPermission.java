/**
 * Copyright (c) 2003, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.remote;

import java.security.BasicPermission;

public final class SubjectDelegationPermission extends BasicPermission{
    private static final long serialVersionUID=1481618113008682343L;

    public SubjectDelegationPermission(String name){
        super(name);
    }

    public SubjectDelegationPermission(String name,String actions){
        super(name,actions);
        if(actions!=null)
            throw new IllegalArgumentException("Non-null actions");
    }
}
