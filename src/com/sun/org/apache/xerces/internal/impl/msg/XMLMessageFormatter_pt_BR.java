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
 */
/**
 * Copyright 1999-2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.impl.msg;

import com.sun.org.apache.xerces.internal.util.MessageFormatter;
import com.sun.org.apache.xerces.internal.utils.SecuritySupport;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class XMLMessageFormatter_pt_BR implements MessageFormatter{
    public static final String XML_DOMAIN="http://www.w3.org/TR/1998/REC-xml-19980210";
    public static final String XMLNS_DOMAIN="http://www.w3.org/TR/1999/REC-xml-names-19990114";
    // private objects to cache the locale and resource bundle
    private Locale fLocale=null;
    private ResourceBundle fResourceBundle=null;
    //
    // MessageFormatter methods
    //

    public String formatMessage(Locale locale,String key,Object[] arguments)
            throws MissingResourceException{
        if(fResourceBundle==null||locale!=fLocale){
            if(locale!=null){
                fResourceBundle=SecuritySupport.getResourceBundle("com.sun.org.apache.xerces.internal.impl.msg.XMLMessages",locale);
                // memorize the most-recent locale
                fLocale=locale;
            }
            if(fResourceBundle==null)
                fResourceBundle=SecuritySupport.getResourceBundle("com.sun.org.apache.xerces.internal.impl.msg.XMLMessages");
        }
        // format message
        String msg;
        try{
            msg=fResourceBundle.getString(key);
            if(arguments!=null){
                try{
                    msg=java.text.MessageFormat.format(msg,arguments);
                }catch(Exception e){
                    msg=fResourceBundle.getString("FormatFailed");
                    msg+=" "+fResourceBundle.getString(key);
                }
            }
        }
        // error
        catch(MissingResourceException e){
            msg=fResourceBundle.getString("BadMessageKey");
            throw new MissingResourceException(key,msg,key);
        }
        // no message
        if(msg==null){
            msg=key;
            if(arguments.length>0){
                StringBuffer str=new StringBuffer(msg);
                str.append('?');
                for(int i=0;i<arguments.length;i++){
                    if(i>0){
                        str.append('&');
                    }
                    str.append(String.valueOf(arguments[i]));
                }
            }
        }
        return msg;
    }
}
