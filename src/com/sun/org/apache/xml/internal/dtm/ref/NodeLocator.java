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
 * $Id: NodeLocator.java,v 1.2.4.1 2005/09/15 08:15:08 suresh_emailid Exp $
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
 * $Id: NodeLocator.java,v 1.2.4.1 2005/09/15 08:15:08 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm.ref;

import javax.xml.transform.SourceLocator;

public class NodeLocator implements SourceLocator{
    protected String m_publicId;
    protected String m_systemId;
    protected int m_lineNumber;
    protected int m_columnNumber;

    public NodeLocator(String publicId,String systemId,
                       int lineNumber,int columnNumber){
        this.m_publicId=publicId;
        this.m_systemId=systemId;
        this.m_lineNumber=lineNumber;
        this.m_columnNumber=columnNumber;
    }

    public String getPublicId(){
        return m_publicId;
    }

    public String getSystemId(){
        return m_systemId;
    }

    public int getLineNumber(){
        return m_lineNumber;
    }

    public int getColumnNumber(){
        return m_columnNumber;
    }

    public String toString(){
        return "file '"+m_systemId
                +"', line #"+m_lineNumber
                +", column #"+m_columnNumber;
    }
}
