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
 * $Id: XMLMessages.java,v 1.2.4.1 2005/09/15 07:45:48 suresh_emailid Exp $
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
 * $Id: XMLMessages.java,v 1.2.4.1 2005/09/15 07:45:48 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.res;

import com.sun.org.apache.xalan.internal.utils.SecuritySupport;

import java.util.ListResourceBundle;
import java.util.Locale;

public class XMLMessages{
    protected static final String BAD_CODE="BAD_CODE";
    protected static final String FORMAT_FAILED="FORMAT_FAILED";
    private static final String XML_ERROR_RESOURCES=
            "com.sun.org.apache.xml.internal.res.XMLErrorResources";
    private static ListResourceBundle XMLBundle=null;
    protected Locale fLocale=Locale.getDefault();

    public static final String createXMLMessage(String msgKey,Object args[]){
        if(XMLBundle==null){
            XMLBundle=SecuritySupport.getResourceBundle(XML_ERROR_RESOURCES);
        }
        if(XMLBundle!=null){
            return createMsg(XMLBundle,msgKey,args);
        }else
            return "Could not load any resource bundles.";
    }

    public static final String createMsg(ListResourceBundle fResourceBundle,
                                         String msgKey,Object args[])  //throws Exception
    {
        String fmsg=null;
        boolean throwex=false;
        String msg=null;
        if(msgKey!=null)
            msg=fResourceBundle.getString(msgKey);
        if(msg==null){
            msg=fResourceBundle.getString(BAD_CODE);
            throwex=true;
        }
        if(args!=null){
            try{
                // Do this to keep format from crying.
                // This is better than making a bunch of conditional
                // code all over the place.
                int n=args.length;
                for(int i=0;i<n;i++){
                    if(null==args[i])
                        args[i]="";
                }
                fmsg=java.text.MessageFormat.format(msg,args);
            }catch(Exception e){
                fmsg=fResourceBundle.getString(FORMAT_FAILED);
                fmsg+=" "+msg;
            }
        }else
            fmsg=msg;
        if(throwex){
            throw new RuntimeException(fmsg);
        }
        return fmsg;
    }

    public Locale getLocale(){
        return fLocale;
    }

    public void setLocale(Locale locale){
        fLocale=locale;
    }
}
