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
 * $Id: XPATHMessages.java,v 1.2.4.1 2005/09/01 14:57:34 pvedula Exp $
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
 * $Id: XPATHMessages.java,v 1.2.4.1 2005/09/01 14:57:34 pvedula Exp $
 */
package com.sun.org.apache.xpath.internal.res;

import com.sun.org.apache.bcel.internal.util.SecuritySupport;
import com.sun.org.apache.xml.internal.res.XMLMessages;

import java.util.ListResourceBundle;

public class XPATHMessages extends XMLMessages{
    private static final String XPATH_ERROR_RESOURCES=
            "com.sun.org.apache.xpath.internal.res.XPATHErrorResources";
    private static ListResourceBundle XPATHBundle=null;

    public static final String createXPATHMessage(String msgKey,Object args[]) //throws Exception
    {
        if(XPATHBundle==null){
            XPATHBundle=SecuritySupport.getResourceBundle(XPATH_ERROR_RESOURCES);
        }
        if(XPATHBundle!=null){
            return createXPATHMsg(XPATHBundle,msgKey,args);
        }else{
            return "Could not load any resource bundles.";
        }
    }

    public static final String createXPATHMsg(ListResourceBundle fResourceBundle,
                                              String msgKey,Object args[]) //throws Exception
    {
        String fmsg=null;
        boolean throwex=false;
        String msg=null;
        if(msgKey!=null){
            msg=fResourceBundle.getString(msgKey);
        }
        if(msg==null){
            msg=fResourceBundle.getString(XPATHErrorResources.BAD_CODE);
            throwex=true;
        }
        if(args!=null){
            try{
                // Do this to keep format from crying.
                // This is better than making a bunch of conditional
                // code all over the place.
                int n=args.length;
                for(int i=0;i<n;i++){
                    if(null==args[i]){
                        args[i]="";
                    }
                }
                fmsg=java.text.MessageFormat.format(msg,args);
            }catch(Exception e){
                fmsg=fResourceBundle.getString(XPATHErrorResources.FORMAT_FAILED);
                fmsg+=" "+msg;
            }
        }else{
            fmsg=msg;
        }
        if(throwex){
            throw new RuntimeException(fmsg);
        }
        return fmsg;
    }

    public static final String createXPATHWarning(String msgKey,Object args[]) //throws Exception
    {
        if(XPATHBundle==null){
            XPATHBundle=SecuritySupport.getResourceBundle(XPATH_ERROR_RESOURCES);
        }
        if(XPATHBundle!=null){
            return createXPATHMsg(XPATHBundle,msgKey,args);
        }else{
            return "Could not load any resource bundles.";
        }
    }
}
