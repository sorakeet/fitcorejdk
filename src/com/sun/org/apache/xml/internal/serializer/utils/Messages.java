/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2004 The Apache Software Foundation.
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
 * $Id: Messages.java,v 1.1.4.1 2005/09/08 11:03:10 suresh_emailid Exp $
 */
/**
 * Copyright 2004 The Apache Software Foundation.
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
 * $Id: Messages.java,v 1.1.4.1 2005/09/08 11:03:10 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.serializer.utils;

import com.sun.org.apache.xalan.internal.utils.SecuritySupport;

import java.util.ListResourceBundle;
import java.util.Locale;

public final class Messages{
    private final Locale m_locale=Locale.getDefault();
    private ListResourceBundle m_resourceBundle;
    private String m_resourceBundleName;

    Messages(String resourceBundle){
        m_resourceBundleName=resourceBundle;
    }

    private Locale getLocale(){
        return m_locale;
    }

    public final String createMessage(String msgKey,Object args[]){
        if(m_resourceBundle==null)
            m_resourceBundle=SecuritySupport.getResourceBundle(m_resourceBundleName);
        if(m_resourceBundle!=null){
            return createMsg(m_resourceBundle,msgKey,args);
        }else
            return "Could not load the resource bundles: "+m_resourceBundleName;
    }

    private final String createMsg(
            ListResourceBundle fResourceBundle,
            String msgKey,
            Object args[]) //throws Exception
    {
        String fmsg=null;
        boolean throwex=false;
        String msg=null;
        if(msgKey!=null)
            msg=fResourceBundle.getString(msgKey);
        else
            msgKey="";
        if(msg==null){
            throwex=true;
            /** The message is not in the bundle . . . this is bad,
             * so try to get the message that the message is not in the bundle
             */
            try{
                msg=
                        java.text.MessageFormat.format(
                                MsgKey.BAD_MSGKEY,
                                new Object[]{msgKey,m_resourceBundleName});
            }catch(Exception e){
                /** even the message that the message is not in the bundle is
                 * not there ... this is really bad
                 */
                msg=
                        "The message key '"
                                +msgKey
                                +"' is not in the message class '"
                                +m_resourceBundleName+"'";
            }
        }else if(args!=null){
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
                // if we get past the line above we have create the message ... hurray!
            }catch(Exception e){
                throwex=true;
                try{
                    // Get the message that the format failed.
                    fmsg=
                            java.text.MessageFormat.format(
                                    MsgKey.BAD_MSGFORMAT,
                                    new Object[]{msgKey,m_resourceBundleName});
                    fmsg+=" "+msg;
                }catch(Exception formatfailed){
                    // We couldn't even get the message that the format of
                    // the message failed ... so fall back to English.
                    fmsg=
                            "The format of message '"
                                    +msgKey
                                    +"' in message class '"
                                    +m_resourceBundleName
                                    +"' failed.";
                }
            }
        }else
            fmsg=msg;
        if(throwex){
            throw new RuntimeException(fmsg);
        }
        return fmsg;
    }
}
