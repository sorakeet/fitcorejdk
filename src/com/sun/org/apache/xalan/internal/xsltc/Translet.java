/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * $Id: Translet.java,v 1.2.4.1 2005/08/31 10:46:27 pvedula Exp $
 */
/**
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * $Id: Translet.java,v 1.2.4.1 2005/08/31 10:46:27 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc;

import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;

public interface Translet{
    public void transform(DOM document,SerializationHandler handler)
            throws TransletException;

    public void transform(DOM document,SerializationHandler[] handlers)
            throws TransletException;

    public void transform(DOM document,DTMAxisIterator iterator,
                          SerializationHandler handler)
            throws TransletException;

    public Object addParameter(String name,Object value);

    public void buildKeys(DOM document,DTMAxisIterator iterator,
                          SerializationHandler handler,int root)
            throws TransletException;

    public void addAuxiliaryClass(Class auxClass);

    public Class getAuxiliaryClass(String className);

    public String[] getNamesArray();

    public String[] getUrisArray();

    public int[] getTypesArray();

    public String[] getNamespaceArray();

    public boolean useServicesMechnism();

    public void setServicesMechnism(boolean flag);
}
