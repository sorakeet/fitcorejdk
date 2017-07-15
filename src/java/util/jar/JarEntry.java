/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.jar;

import java.io.IOException;
import java.security.CodeSigner;
import java.security.cert.Certificate;
import java.util.zip.ZipEntry;

public class JarEntry extends ZipEntry{
    Attributes attr;
    Certificate[] certs;
    CodeSigner[] signers;

    public JarEntry(String name){
        super(name);
    }

    public JarEntry(JarEntry je){
        this((ZipEntry)je);
        this.attr=je.attr;
        this.certs=je.certs;
        this.signers=je.signers;
    }

    public JarEntry(ZipEntry ze){
        super(ze);
    }

    public Attributes getAttributes() throws IOException{
        return attr;
    }

    public Certificate[] getCertificates(){
        return certs==null?null:certs.clone();
    }

    public CodeSigner[] getCodeSigners(){
        return signers==null?null:signers.clone();
    }
}
