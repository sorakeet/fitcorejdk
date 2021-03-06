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
 * $Id: ExsltBase.java,v 1.1.2.1 2005/08/01 02:08:51 jeffsuttor Exp $
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
 * $Id: ExsltBase.java,v 1.1.2.1 2005/08/01 02:08:51 jeffsuttor Exp $
 */
package com.sun.org.apache.xalan.internal.lib;

import com.sun.org.apache.xml.internal.dtm.ref.DTMNodeProxy;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class ExsltBase{
    protected static String toString(Node n){
        if(n instanceof DTMNodeProxy)
            return ((DTMNodeProxy)n).getStringValue();
        else{
            String value=n.getNodeValue();
            if(value==null){
                NodeList nodelist=n.getChildNodes();
                StringBuffer buf=new StringBuffer();
                for(int i=0;i<nodelist.getLength();i++){
                    Node childNode=nodelist.item(i);
                    buf.append(toString(childNode));
                }
                return buf.toString();
            }else
                return value;
        }
    }

    protected static double toNumber(Node n){
        double d=0.0;
        String str=toString(n);
        try{
            d=Double.valueOf(str).doubleValue();
        }catch(NumberFormatException e){
            d=Double.NaN;
        }
        return d;
    }
}
