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
 */
/**
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.org.apache.xerces.internal.impl.xs.opti;

import org.w3c.dom.DOMException;
import org.w3c.dom.Text;

public class DefaultText extends NodeImpl implements Text{
    // CharacterData methods

    public String getData()
            throws DOMException{
        return null;
    }

    public void setData(String data)
            throws DOMException{
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }

    public int getLength(){
        return 0;
    }

    public String substringData(int offset,
                                int count)
            throws DOMException{
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }

    public void appendData(String arg)
            throws DOMException{
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }

    public void insertData(int offset,
                           String arg)
            throws DOMException{
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }

    public void deleteData(int offset,
                           int count)
            throws DOMException{
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }

    public void replaceData(int offset,
                            int count,
                            String arg)
            throws DOMException{
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }

    // Text node methods
    public Text splitText(int offset)
            throws DOMException{
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }

    public boolean isElementContentWhitespace(){
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }

    public String getWholeText(){
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }

    public Text replaceWholeText(String content) throws DOMException{
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }
}
