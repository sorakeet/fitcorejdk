/**
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind;

import java.security.BasicPermission;

public final class JAXBPermission extends BasicPermission{
    private static final long serialVersionUID=1L;

    public JAXBPermission(String name){
        super(name);
    }
}
