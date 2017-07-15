/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2005 The Apache Software Foundation.
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
 * $Id: FuncExtFunctionAvailable.java,v 1.2.4.1 2005/09/14 20:05:08 jeffsuttor Exp $
 */
/**
 * Copyright 1999-2005 The Apache Software Foundation.
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
 * $Id: FuncExtFunctionAvailable.java,v 1.2.4.1 2005/09/14 20:05:08 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.functions;

import com.sun.org.apache.xalan.internal.templates.Constants;
import com.sun.org.apache.xpath.internal.ExtensionsProvider;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.compiler.FunctionTable;
import com.sun.org.apache.xpath.internal.objects.XBoolean;
import com.sun.org.apache.xpath.internal.objects.XObject;

public class FuncExtFunctionAvailable extends FunctionOneArg{
    static final long serialVersionUID=5118814314918592241L;
    transient private FunctionTable m_functionTable=null;

    public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException{
        String prefix;
        String namespace;
        String methName;
        String fullName=m_arg0.execute(xctxt).str();
        int indexOfNSSep=fullName.indexOf(':');
        if(indexOfNSSep<0){
            prefix="";
            namespace=Constants.S_XSLNAMESPACEURL;
            methName=fullName;
        }else{
            prefix=fullName.substring(0,indexOfNSSep);
            namespace=xctxt.getNamespaceContext().getNamespaceForPrefix(prefix);
            if(null==namespace)
                return XBoolean.S_FALSE;
            methName=fullName.substring(indexOfNSSep+1);
        }
        if(namespace.equals(Constants.S_XSLNAMESPACEURL)){
            try{
                if(null==m_functionTable) m_functionTable=new FunctionTable();
                return m_functionTable.functionAvailable(methName)?XBoolean.S_TRUE:XBoolean.S_FALSE;
            }catch(Exception e){
                return XBoolean.S_FALSE;
            }
        }else{
            //dml
            ExtensionsProvider extProvider=(ExtensionsProvider)xctxt.getOwnerObject();
            return extProvider.functionAvailable(namespace,methName)
                    ?XBoolean.S_TRUE:XBoolean.S_FALSE;
        }
    }

    public void setFunctionTable(FunctionTable aTable){
        m_functionTable=aTable;
    }
}
