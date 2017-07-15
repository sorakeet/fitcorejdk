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
package com.sun.org.apache.xerces.internal.impl.xs;

import com.sun.org.apache.xerces.internal.util.MessageFormatter;
import com.sun.org.apache.xerces.internal.utils.SecuritySupport;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class XSMessageFormatter implements MessageFormatter{
    public static final String SCHEMA_DOMAIN="http://www.w3.org/TR/xml-schema-1";
    // private objects to cache the locale and resource bundle
    private Locale fLocale=null;
    private ResourceBundle fResourceBundle=null;

    public String formatMessage(Locale locale,String key,Object[] arguments)
            throws MissingResourceException{
        if(fResourceBundle==null||locale!=fLocale){
            if(locale!=null){
                fResourceBundle=SecuritySupport.getResourceBundle("com.sun.org.apache.xerces.internal.impl.msg.XMLSchemaMessages",locale);
                // memorize the most-recent locale
                fLocale=locale;
            }
            if(fResourceBundle==null)
                fResourceBundle=SecuritySupport.getResourceBundle("com.sun.org.apache.xerces.internal.impl.msg.XMLSchemaMessages");
        }
        String msg=fResourceBundle.getString(key);
        if(arguments!=null){
            try{
                msg=java.text.MessageFormat.format(msg,arguments);
            }catch(Exception e){
                msg=fResourceBundle.getString("FormatFailed");
                msg+=" "+fResourceBundle.getString(key);
            }
        }
        if(msg==null){
            msg=fResourceBundle.getString("BadMessageKey");
            throw new MissingResourceException(msg,"com.sun.org.apache.xerces.internal.impl.msg.SchemaMessages",key);
        }
        return msg;
    }
}
