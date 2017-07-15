/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2003-2004 The Apache Software Foundation.
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
 * $Id: XSLOutputAttributes.java,v 1.2.4.1 2005/09/15 08:15:32 suresh_emailid Exp $
 */
/**
 * Copyright 2003-2004 The Apache Software Foundation.
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
 * $Id: XSLOutputAttributes.java,v 1.2.4.1 2005/09/15 08:15:32 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

import java.util.Vector;

interface XSLOutputAttributes{
    public String getDoctypePublic();

    public void setDoctypePublic(String doctype);

    public String getDoctypeSystem();

    public void setDoctypeSystem(String doctype);

    public String getEncoding();

    public void setEncoding(String encoding);

    public boolean getIndent();

    public void setIndent(boolean indent);

    public int getIndentAmount();

    public String getMediaType();

    public void setMediaType(String mediatype);

    public boolean getOmitXMLDeclaration();

    public void setOmitXMLDeclaration(boolean b);

    public String getStandalone();

    public void setStandalone(String standalone);

    public String getVersion();

    public void setVersion(String version);

    public void setCdataSectionElements(Vector URI_and_localNames);

    public void setDoctype(String system,String pub);
}
