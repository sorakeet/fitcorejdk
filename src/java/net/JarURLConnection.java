/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.net;

import sun.net.www.ParseUtil;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public abstract class JarURLConnection extends URLConnection{
    protected URLConnection jarFileURLConnection;
    private URL jarFileURL;
    private String entryName;

    protected JarURLConnection(URL url) throws MalformedURLException{
        super(url);
        parseSpecs(url);
    }

    private void parseSpecs(URL url) throws MalformedURLException{
        String spec=url.getFile();
        int separator=spec.indexOf("!/");
        /**
         * REMIND: we don't handle nested JAR URLs
         */
        if(separator==-1){
            throw new MalformedURLException("no !/ found in url spec:"+spec);
        }
        jarFileURL=new URL(spec.substring(0,separator++));
        entryName=null;
        /** if ! is the last letter of the innerURL, entryName is null */
        if(++separator!=spec.length()){
            entryName=spec.substring(separator,spec.length());
            entryName=ParseUtil.decode(entryName);
        }
    }

    public URL getJarFileURL(){
        return jarFileURL;
    }

    public String getEntryName(){
        return entryName;
    }

    public Attributes getAttributes() throws IOException{
        JarEntry e=getJarEntry();
        return e!=null?e.getAttributes():null;
    }

    public JarEntry getJarEntry() throws IOException{
        return getJarFile().getJarEntry(entryName);
    }

    public abstract JarFile getJarFile() throws IOException;

    public Attributes getMainAttributes() throws IOException{
        Manifest man=getManifest();
        return man!=null?man.getMainAttributes():null;
    }

    public Manifest getManifest() throws IOException{
        return getJarFile().getManifest();
    }

    public java.security.cert.Certificate[] getCertificates()
            throws IOException{
        JarEntry e=getJarEntry();
        return e!=null?e.getCertificates():null;
    }
}
