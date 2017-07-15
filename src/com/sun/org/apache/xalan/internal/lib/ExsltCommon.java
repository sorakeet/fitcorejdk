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
 * $Id: ExsltCommon.java,v 1.2.4.1 2005/09/15 02:45:24 jeffsuttor Exp $
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
 * $Id: ExsltCommon.java,v 1.2.4.1 2005/09/15 02:45:24 jeffsuttor Exp $
 */
package com.sun.org.apache.xalan.internal.lib;

import com.sun.org.apache.xalan.internal.extensions.ExpressionContext;
import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import com.sun.org.apache.xml.internal.dtm.ref.DTMNodeIterator;
import com.sun.org.apache.xpath.internal.NodeSet;

public class ExsltCommon{
    public static String objectType(Object obj){
        if(obj instanceof String)
            return "string";
        else if(obj instanceof Boolean)
            return "boolean";
        else if(obj instanceof Number)
            return "number";
        else if(obj instanceof DTMNodeIterator){
            DTMIterator dtmI=((DTMNodeIterator)obj).getDTMIterator();
            if(dtmI instanceof com.sun.org.apache.xpath.internal.axes.RTFIterator)
                return "RTF";
            else
                return "node-set";
        }else
            return "unknown";
    }

    public static NodeSet nodeSet(ExpressionContext myProcessor,Object rtf){
        return Extensions.nodeset(myProcessor,rtf);
    }
}
