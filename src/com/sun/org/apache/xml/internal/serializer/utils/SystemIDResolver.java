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
 * $Id: SystemIDResolver.java,v 1.1.4.1 2005/09/08 11:03:20 suresh_emailid Exp $
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
 * $Id: SystemIDResolver.java,v 1.1.4.1 2005/09/08 11:03:20 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.serializer.utils;

import com.sun.org.apache.xml.internal.serializer.utils.URI.MalformedURIException;

import javax.xml.transform.TransformerException;
import java.io.File;

public final class SystemIDResolver{
    public static String getAbsoluteURI(String urlString,String base)
            throws TransformerException{
        if(base==null)
            return getAbsoluteURI(urlString);
        String absoluteBase=getAbsoluteURI(base);
        URI uri=null;
        try{
            URI baseURI=new URI(absoluteBase);
            uri=new URI(baseURI,urlString);
        }catch(MalformedURIException mue){
            throw new TransformerException(mue);
        }
        return replaceChars(uri.toString());
    }

    public static String getAbsoluteURI(String systemId){
        String absoluteURI=systemId;
        if(isAbsoluteURI(systemId)){
            // Only process the systemId if it starts with "file:".
            if(systemId.startsWith("file:")){
                String str=systemId.substring(5);
                // Resolve the absolute path if the systemId starts with "file:///"
                // or "file:/". Don't do anything if it only starts with "file://".
                if(str!=null&&str.startsWith("/")){
                    if(str.startsWith("///")||!str.startsWith("//")){
                        // A Windows path containing a drive letter can be relative.
                        // A Unix path starting with "file:/" is always absolute.
                        int secondColonIndex=systemId.indexOf(':',5);
                        if(secondColonIndex>0){
                            String localPath=systemId.substring(secondColonIndex-1);
                            try{
                                if(!isAbsolutePath(localPath))
                                    absoluteURI=systemId.substring(0,secondColonIndex-1)+
                                            getAbsolutePathFromRelativePath(localPath);
                            }catch(SecurityException se){
                                return systemId;
                            }
                        }
                    }
                }else{
                    return getAbsoluteURIFromRelative(systemId.substring(5));
                }
                return replaceChars(absoluteURI);
            }else
                return systemId;
        }else
            return getAbsoluteURIFromRelative(systemId);
    }

    public static String getAbsoluteURIFromRelative(String localPath){
        if(localPath==null||localPath.length()==0)
            return "";
        // If the local path is a relative path, then it is resolved against
        // the "user.dir" system property.
        String absolutePath=localPath;
        if(!isAbsolutePath(localPath)){
            try{
                absolutePath=getAbsolutePathFromRelativePath(localPath);
            }
            // user.dir not accessible from applet
            catch(SecurityException se){
                return "file:"+localPath;
            }
        }
        String urlString;
        if(null!=absolutePath){
            if(absolutePath.startsWith(File.separator))
                urlString="file://"+absolutePath;
            else
                urlString="file:///"+absolutePath;
        }else
            urlString="file:"+localPath;
        return replaceChars(urlString);
    }

    private static String replaceChars(String str){
        StringBuffer buf=new StringBuffer(str);
        int length=buf.length();
        for(int i=0;i<length;i++){
            char currentChar=buf.charAt(i);
            // Replace space with "%20"
            if(currentChar==' '){
                buf.setCharAt(i,'%');
                buf.insert(i+1,"20");
                length=length+2;
                i=i+2;
            }
            // Replace backslash with forward slash
            else if(currentChar=='\\'){
                buf.setCharAt(i,'/');
            }
        }
        return buf.toString();
    }

    private static String getAbsolutePathFromRelativePath(String relativePath){
        return new File(relativePath).getAbsolutePath();
    }

    public static boolean isAbsoluteURI(String systemId){
        /** http://www.ietf.org/rfc/rfc2396.txt
         *   Authors should be aware that a path segment which contains a colon
         * character cannot be used as the first segment of a relative URI path
         * (e.g., "this:that"), because it would be mistaken for a scheme name.
         **/
        /**
         * %REVIEW% Can we assume here that systemId is a valid URI?
         * It looks like we cannot ( See discussion of this common problem in
         * Bugzilla Bug 22777 ).
         **/
        //"fix" for Bugzilla Bug 22777
        if(isWindowsAbsolutePath(systemId)){
            return false;
        }
        final int fragmentIndex=systemId.indexOf('#');
        final int queryIndex=systemId.indexOf('?');
        final int slashIndex=systemId.indexOf('/');
        final int colonIndex=systemId.indexOf(':');
        //finding substring  before '#', '?', and '/'
        int index=systemId.length()-1;
        if(fragmentIndex>0)
            index=fragmentIndex;
        if((queryIndex>0)&&(queryIndex<index))
            index=queryIndex;
        if((slashIndex>0)&&(slashIndex<index))
            index=slashIndex;
        // return true if there is ':' before '#', '?', and '/'
        return ((colonIndex>0)&&(colonIndex<index));
    }

    private static boolean isWindowsAbsolutePath(String systemId){
        if(!isAbsolutePath(systemId))
            return false;
        // On Windows, an absolute path starts with "[drive_letter]:\".
        if(systemId.length()>2
                &&systemId.charAt(1)==':'
                &&Character.isLetter(systemId.charAt(0))
                &&(systemId.charAt(2)=='\\'||systemId.charAt(2)=='/'))
            return true;
        else
            return false;
    }

    public static boolean isAbsolutePath(String systemId){
        if(systemId==null)
            return false;
        final File file=new File(systemId);
        return file.isAbsolute();
    }
}
