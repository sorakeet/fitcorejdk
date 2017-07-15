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
 * $Id: XSLMessages.java,v 1.2.4.1 2005/09/09 07:41:10 pvedula Exp $
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
 * $Id: XSLMessages.java,v 1.2.4.1 2005/09/09 07:41:10 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.res;

import com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import com.sun.org.apache.xpath.internal.res.XPATHMessages;

import java.util.ListResourceBundle;

public class XSLMessages extends XPATHMessages{
    private static final String XSLT_ERROR_RESOURCES=
            "com.sun.org.apache.xalan.internal.res.XSLTErrorResources";
    private static ListResourceBundle XSLTBundle=null;

    public static String createMessage(String msgKey,Object args[]) //throws Exception
    {
        if(XSLTBundle==null){
            XSLTBundle=SecuritySupport.getResourceBundle(XSLT_ERROR_RESOURCES);
        }
        if(XSLTBundle!=null){
            return createMsg(XSLTBundle,msgKey,args);
        }else{
            return "Could not load any resource bundles.";
        }
    }

    public static String createWarning(String msgKey,Object args[]) //throws Exception
    {
        if(XSLTBundle==null){
            XSLTBundle=SecuritySupport.getResourceBundle(XSLT_ERROR_RESOURCES);
        }
        if(XSLTBundle!=null){
            return createMsg(XSLTBundle,msgKey,args);
        }else{
            return "Could not load any resource bundles.";
        }
    }
}
