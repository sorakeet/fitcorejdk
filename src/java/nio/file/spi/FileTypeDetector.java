/**
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.file.spi;

import java.io.IOException;
import java.nio.file.Path;

public abstract class FileTypeDetector{
    protected FileTypeDetector(){
        this(checkPermission());
    }

    private static Void checkPermission(){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null)
            sm.checkPermission(new RuntimePermission("fileTypeDetector"));
        return null;
    }

    private FileTypeDetector(Void ignore){
    }

    public abstract String probeContentType(Path path)
            throws IOException;
}
