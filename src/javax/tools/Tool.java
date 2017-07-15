/**
 * Copyright (c) 2005, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.tools;

import javax.lang.model.SourceVersion;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public interface Tool{
    int run(InputStream in,OutputStream out,OutputStream err,String... arguments);

    Set<SourceVersion> getSourceVersions();
}
