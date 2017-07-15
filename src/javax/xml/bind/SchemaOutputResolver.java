/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind;

import javax.xml.transform.Result;
import java.io.IOException;

public abstract class SchemaOutputResolver{
    public abstract Result createOutput(String namespaceUri,String suggestedFileName) throws IOException;
}
