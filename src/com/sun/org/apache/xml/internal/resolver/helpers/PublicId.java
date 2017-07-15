/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2004 The Apache Software Foundation or its licensors,
 * as applicable.
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
// PublicId.java - Information about public identifiers
/**
 * Copyright 2001-2004 The Apache Software Foundation or its licensors,
 * as applicable.
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
package com.sun.org.apache.xml.internal.resolver.helpers;

public abstract class PublicId{
    protected PublicId(){
    }

    public static String encodeURN(String publicId){
        String urn=PublicId.normalize(publicId);
        urn=PublicId.stringReplace(urn,"%","%25");
        urn=PublicId.stringReplace(urn,";","%3B");
        urn=PublicId.stringReplace(urn,"'","%27");
        urn=PublicId.stringReplace(urn,"?","%3F");
        urn=PublicId.stringReplace(urn,"#","%23");
        urn=PublicId.stringReplace(urn,"+","%2B");
        urn=PublicId.stringReplace(urn," ","+");
        urn=PublicId.stringReplace(urn,"::",";");
        urn=PublicId.stringReplace(urn,":","%3A");
        urn=PublicId.stringReplace(urn,"//",":");
        urn=PublicId.stringReplace(urn,"/","%2F");
        return "urn:publicid:"+urn;
    }

    public static String normalize(String publicId){
        String normal=publicId.replace('\t',' ');
        normal=normal.replace('\r',' ');
        normal=normal.replace('\n',' ');
        normal=normal.trim();
        int pos;
        while((pos=normal.indexOf("  "))>=0){
            normal=normal.substring(0,pos)+normal.substring(pos+1);
        }
        return normal;
    }

    private static String stringReplace(String str,
                                        String oldStr,
                                        String newStr){
        String result="";
        int pos=str.indexOf(oldStr);
        //    System.out.println(str + ": " + oldStr + " => " + newStr);
        while(pos>=0){
            //      System.out.println(str + " (" + pos + ")");
            result+=str.substring(0,pos);
            result+=newStr;
            str=str.substring(pos+1);
            pos=str.indexOf(oldStr);
        }
        return result+str;
    }

    public static String decodeURN(String urn){
        String publicId="";
        if(urn.startsWith("urn:publicid:")){
            publicId=urn.substring(13);
        }else{
            return urn;
        }
        publicId=PublicId.stringReplace(publicId,"%2F","/");
        publicId=PublicId.stringReplace(publicId,":","//");
        publicId=PublicId.stringReplace(publicId,"%3A",":");
        publicId=PublicId.stringReplace(publicId,";","::");
        publicId=PublicId.stringReplace(publicId,"+"," ");
        publicId=PublicId.stringReplace(publicId,"%2B","+");
        publicId=PublicId.stringReplace(publicId,"%23","#");
        publicId=PublicId.stringReplace(publicId,"%3F","?");
        publicId=PublicId.stringReplace(publicId,"%27","'");
        publicId=PublicId.stringReplace(publicId,"%3B",";");
        publicId=PublicId.stringReplace(publicId,"%25","%");
        return publicId;
    }
}
