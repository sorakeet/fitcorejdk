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
 * $Id: CoroutineParser.java,v 1.2.4.1 2005/09/15 08:14:59 suresh_emailid Exp $
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
 * $Id: CoroutineParser.java,v 1.2.4.1 2005/09/15 08:14:59 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm.ref;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public interface CoroutineParser{
    public int getParserCoroutineID();

    public CoroutineManager getCoroutineManager();

    public void setContentHandler(ContentHandler handler);

    public void setLexHandler(org.xml.sax.ext.LexicalHandler handler);

    //================================================================
    public Object doParse(InputSource source,int appCoroutine);

    public Object doMore(boolean parsemore,int appCoroutine);

    public void doTerminate(int appCoroutine);

    public void init(CoroutineManager co,int appCoroutineID,XMLReader parser);
} // class CoroutineParser
