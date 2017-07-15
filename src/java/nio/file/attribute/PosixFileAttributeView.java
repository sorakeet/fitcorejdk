/**
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.file.attribute;

import java.io.IOException;
import java.util.Set;

public interface PosixFileAttributeView
        extends BasicFileAttributeView, FileOwnerAttributeView{
    @Override
    String name();

    @Override
    PosixFileAttributes readAttributes() throws IOException;

    void setPermissions(Set<PosixFilePermission> perms) throws IOException;

    void setGroup(GroupPrincipal group) throws IOException;
}
