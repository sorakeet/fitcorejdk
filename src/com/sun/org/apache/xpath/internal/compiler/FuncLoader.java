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
 * $Id: FuncLoader.java,v 1.1.2.1 2005/08/01 01:30:35 jeffsuttor Exp $
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
 * $Id: FuncLoader.java,v 1.1.2.1 2005/08/01 01:30:35 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.compiler;

import com.sun.org.apache.xalan.internal.utils.ConfigurationError;
import com.sun.org.apache.xalan.internal.utils.ObjectFactory;
import com.sun.org.apache.xpath.internal.functions.Function;

import javax.xml.transform.TransformerException;

public class FuncLoader{
    private int m_funcID;
    private String m_funcName;

    public FuncLoader(String funcName,int funcID){
        super();
        m_funcID=funcID;
        m_funcName=funcName;
    }

    public String getName(){
        return m_funcName;
    }

    Function getFunction() throws TransformerException{
        try{
            String className=m_funcName;
            if(className.indexOf(".")<0){
                className="com.sun.org.apache.xpath.internal.functions."+className;
            }
            //hack for loading only built-in function classes.
            String subString=className.substring(0,className.lastIndexOf('.'));
            if(!(subString.equals("com.sun.org.apache.xalan.internal.templates")||
                    subString.equals("com.sun.org.apache.xpath.internal.functions"))){
                throw new TransformerException("Application can't install his own xpath function.");
            }
            return (Function)ObjectFactory.newInstance(className,true);
        }catch(ConfigurationError e){
            throw new TransformerException(e.getException());
        }
    }
}
