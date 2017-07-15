/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
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
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.impl.dv;

import com.sun.org.apache.xerces.internal.utils.SecuritySupport;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class DatatypeException extends Exception{
    static final long serialVersionUID=1940805832730465578L;
    // used to store error code and error substitution arguments
    protected String key;
    protected Object[] args;

    public DatatypeException(String key,Object[] args){
        super(key);
        this.key=key;
        this.args=args;
    }

    public String getKey(){
        return key;
    }

    public Object[] getArgs(){
        return args;
    }

    public String getMessage(){
        ResourceBundle resourceBundle=null;
        resourceBundle=SecuritySupport.getResourceBundle("com.sun.org.apache.xerces.internal.impl.msg.XMLSchemaMessages");
        if(resourceBundle==null)
            throw new MissingResourceException("Property file not found!","com.sun.org.apache.xerces.internal.impl.msg.XMLSchemaMessages",key);
        String msg=resourceBundle.getString(key);
        if(msg==null){
            msg=resourceBundle.getString("BadMessageKey");
            throw new MissingResourceException(msg,"com.sun.org.apache.xerces.internal.impl.msg.XMLSchemaMessages",key);
        }
        if(args!=null){
            try{
                msg=java.text.MessageFormat.format(msg,args);
            }catch(Exception e){
                msg=resourceBundle.getString("FormatFailed");
                msg+=" "+resourceBundle.getString(key);
            }
        }
        return msg;
    }
}
