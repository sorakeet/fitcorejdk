/**
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.file.attribute;

import java.io.IOException;
import java.util.List;

public interface AclFileAttributeView
        extends FileOwnerAttributeView{
    @Override
    String name();

    List<AclEntry> getAcl() throws IOException;

    void setAcl(List<AclEntry> acl) throws IOException;
}
