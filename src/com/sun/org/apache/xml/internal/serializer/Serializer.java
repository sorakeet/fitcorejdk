/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2004 The Apache Software Foundation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * $Id: Serializer.java,v 1.2.4.1 2005/09/15 08:15:22 suresh_emailid Exp $
 */
/**
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * $Id: Serializer.java,v 1.2.4.1 2005/09/15 08:15:22 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

import org.xml.sax.ContentHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Properties;

public interface Serializer{
    public OutputStream getOutputStream();

    public void setOutputStream(OutputStream output);

    public Writer getWriter();

    public void setWriter(Writer writer);

    public Properties getOutputFormat();

    public void setOutputFormat(Properties format);

    public ContentHandler asContentHandler() throws IOException;

    public DOMSerializer asDOMSerializer() throws IOException;

    public boolean reset();
}
