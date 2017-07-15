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
package com.sun.org.apache.xml.internal.security.keys.storage.implementations;

import com.sun.org.apache.xml.internal.security.keys.storage.StorageResolverException;
import com.sun.org.apache.xml.internal.security.keys.storage.StorageResolverSpi;
import com.sun.org.apache.xml.internal.security.utils.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CertsInFilesystemDirectoryResolver extends StorageResolverSpi{
    private static java.util.logging.Logger log=
            java.util.logging.Logger.getLogger(
                    CertsInFilesystemDirectoryResolver.class.getName()
            );
    private String merlinsCertificatesDir=null;
    private List<X509Certificate> certs=new ArrayList<X509Certificate>();

    public CertsInFilesystemDirectoryResolver(String directoryName)
            throws StorageResolverException{
        this.merlinsCertificatesDir=directoryName;
        this.readCertsFromHarddrive();
    }

    private void readCertsFromHarddrive() throws StorageResolverException{
        File certDir=new File(this.merlinsCertificatesDir);
        List<String> al=new ArrayList<String>();
        String[] names=certDir.list();
        for(int i=0;i<names.length;i++){
            String currentFileName=names[i];
            if(currentFileName.endsWith(".crt")){
                al.add(names[i]);
            }
        }
        CertificateFactory cf=null;
        try{
            cf=CertificateFactory.getInstance("X.509");
        }catch(CertificateException ex){
            throw new StorageResolverException("empty",ex);
        }
        if(cf==null){
            throw new StorageResolverException("empty");
        }
        for(int i=0;i<al.size();i++){
            String filename=certDir.getAbsolutePath()+File.separator+al.get(i);
            File file=new File(filename);
            boolean added=false;
            String dn=null;
            FileInputStream fis=null;
            try{
                fis=new FileInputStream(file);
                X509Certificate cert=
                        (X509Certificate)cf.generateCertificate(fis);
                //add to ArrayList
                cert.checkValidity();
                this.certs.add(cert);
                dn=cert.getSubjectX500Principal().getName();
                added=true;
            }catch(FileNotFoundException ex){
                if(log.isLoggable(java.util.logging.Level.FINE)){
                    log.log(java.util.logging.Level.FINE,"Could not add certificate from file "+filename,ex);
                }
            }catch(CertificateNotYetValidException ex){
                if(log.isLoggable(java.util.logging.Level.FINE)){
                    log.log(java.util.logging.Level.FINE,"Could not add certificate from file "+filename,ex);
                }
            }catch(CertificateExpiredException ex){
                if(log.isLoggable(java.util.logging.Level.FINE)){
                    log.log(java.util.logging.Level.FINE,"Could not add certificate from file "+filename,ex);
                }
            }catch(CertificateException ex){
                if(log.isLoggable(java.util.logging.Level.FINE)){
                    log.log(java.util.logging.Level.FINE,"Could not add certificate from file "+filename,ex);
                }
            }finally{
                try{
                    if(fis!=null){
                        fis.close();
                    }
                }catch(IOException ex){
                    if(log.isLoggable(java.util.logging.Level.FINE)){
                        log.log(java.util.logging.Level.FINE,"Could not add certificate from file "+filename,ex);
                    }
                }
            }
            if(added&&log.isLoggable(java.util.logging.Level.FINE)){
                log.log(java.util.logging.Level.FINE,"Added certificate: "+dn);
            }
        }
    }

    public static void main(String unused[]) throws Exception{
        CertsInFilesystemDirectoryResolver krs=
                new CertsInFilesystemDirectoryResolver(
                        "data/ie/baltimore/merlin-examples/merlin-xmldsig-eighteen/certs");
        for(Iterator<Certificate> i=krs.getIterator();i.hasNext();){
            X509Certificate cert=(X509Certificate)i.next();
            byte[] ski=
                    com.sun.org.apache.xml.internal.security.keys.content.x509.XMLX509SKI.getSKIBytesFromCert(cert);
            System.out.println();
            System.out.println("Base64(SKI())=                 \""
                    +Base64.encode(ski)+"\"");
            System.out.println("cert.getSerialNumber()=        \""
                    +cert.getSerialNumber().toString()+"\"");
            System.out.println("cert.getSubjectX500Principal().getName()= \""
                    +cert.getSubjectX500Principal().getName()+"\"");
            System.out.println("cert.getIssuerX500Principal().getName()=  \""
                    +cert.getIssuerX500Principal().getName()+"\"");
        }
    }

    public Iterator<Certificate> getIterator(){
        return new FilesystemIterator(this.certs);
    }

    private static class FilesystemIterator implements Iterator<Certificate>{
        List<X509Certificate> certs=null;
        int i;

        public FilesystemIterator(List<X509Certificate> certs){
            this.certs=certs;
            this.i=0;
        }

        public boolean hasNext(){
            return (this.i<this.certs.size());
        }

        public Certificate next(){
            return this.certs.get(this.i++);
        }

        public void remove(){
            throw new UnsupportedOperationException("Can't remove keys from KeyStore");
        }
    }
}
