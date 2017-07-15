/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.sun.org.apache.xml.internal.security.utils;

import java.io.*;
import java.security.SecurityPermission;

public class JavaUtils{
    private static final SecurityPermission REGISTER_PERMISSION=
            new SecurityPermission(
                    "com.sun.org.apache.xml.internal.security.register");
    private static java.util.logging.Logger log=
            java.util.logging.Logger.getLogger(JavaUtils.class.getName());

    private JavaUtils(){
        // we don't allow instantiation
    }

    public static byte[] getBytesFromFile(String fileName)
            throws FileNotFoundException, IOException{
        byte refBytes[]=null;
        FileInputStream fisRef=null;
        UnsyncByteArrayOutputStream baos=null;
        try{
            fisRef=new FileInputStream(fileName);
            baos=new UnsyncByteArrayOutputStream();
            byte buf[]=new byte[1024];
            int len;
            while((len=fisRef.read(buf))>0){
                baos.write(buf,0,len);
            }
            refBytes=baos.toByteArray();
        }finally{
            if(baos!=null){
                baos.close();
            }
            if(fisRef!=null){
                fisRef.close();
            }
        }
        return refBytes;
    }

    public static void writeBytesToFilename(String filename,byte[] bytes){
        FileOutputStream fos=null;
        try{
            if(filename!=null&&bytes!=null){
                File f=new File(filename);
                fos=new FileOutputStream(f);
                fos.write(bytes);
                fos.close();
            }else{
                if(log.isLoggable(java.util.logging.Level.FINE)){
                    log.log(java.util.logging.Level.FINE,"writeBytesToFilename got null byte[] pointed");
                }
            }
        }catch(IOException ex){
            if(fos!=null){
                try{
                    fos.close();
                }catch(IOException ioe){
                    if(log.isLoggable(java.util.logging.Level.FINE)){
                        log.log(java.util.logging.Level.FINE,ioe.getMessage(),ioe);
                    }
                }
            }
        }
    }

    public static byte[] getBytesFromStream(InputStream inputStream) throws IOException{
        UnsyncByteArrayOutputStream baos=null;
        byte[] retBytes=null;
        try{
            baos=new UnsyncByteArrayOutputStream();
            byte buf[]=new byte[4*1024];
            int len;
            while((len=inputStream.read(buf))>0){
                baos.write(buf,0,len);
            }
            retBytes=baos.toByteArray();
        }finally{
            baos.close();
        }
        return retBytes;
    }

    public static byte[] convertDsaASN1toXMLDSIG(byte[] asn1Bytes,int size)
            throws IOException{
        if(asn1Bytes[0]!=48||asn1Bytes[1]!=asn1Bytes.length-2
                ||asn1Bytes[2]!=2){
            throw new IOException("Invalid ASN.1 format of DSA signature");
        }
        byte rLength=asn1Bytes[3];
        int i;
        for(i=rLength;i>0&&asn1Bytes[4+rLength-i]==0;i--) ;
        byte sLength=asn1Bytes[5+rLength];
        int j;
        for(j=sLength;
            j>0&&asn1Bytes[6+rLength+sLength-j]==0;j--)
            ;
        if(i>size||asn1Bytes[4+rLength]!=2||j>size){
            throw new IOException("Invalid ASN.1 format of DSA signature");
        }else{
            byte[] xmldsigBytes=new byte[size*2];
            System.arraycopy(asn1Bytes,4+rLength-i,xmldsigBytes,
                    size-i,i);
            System.arraycopy(asn1Bytes,6+rLength+sLength-j,
                    xmldsigBytes,size*2-j,j);
            return xmldsigBytes;
        }
    }

    public static byte[] convertDsaXMLDSIGtoASN1(byte[] xmldsigBytes,int size)
            throws IOException{
        int totalSize=size*2;
        if(xmldsigBytes.length!=totalSize){
            throw new IOException("Invalid XMLDSIG format of DSA signature");
        }
        int i;
        for(i=size;i>0&&xmldsigBytes[size-i]==0;i--) ;
        int j=i;
        if(xmldsigBytes[size-i]<0){
            j++;
        }
        int k;
        for(k=size;k>0&&xmldsigBytes[totalSize-k]==0;k--) ;
        int l=k;
        if(xmldsigBytes[totalSize-k]<0){
            l++;
        }
        byte[] asn1Bytes=new byte[6+j+l];
        asn1Bytes[0]=48;
        asn1Bytes[1]=(byte)(4+j+l);
        asn1Bytes[2]=2;
        asn1Bytes[3]=(byte)j;
        System.arraycopy(xmldsigBytes,size-i,asn1Bytes,4+j-i,i);
        asn1Bytes[4+j]=2;
        asn1Bytes[5+j]=(byte)l;
        System.arraycopy(xmldsigBytes,totalSize-k,asn1Bytes,
                6+j+l-k,k);
        return asn1Bytes;
    }

    public static void checkRegisterPermission(){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            sm.checkPermission(REGISTER_PERMISSION);
        }
    }
}
