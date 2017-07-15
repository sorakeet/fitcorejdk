/**
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.datatype;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

class SecuritySupport{
    ClassLoader getContextClassLoader(){
        return (ClassLoader)
                AccessController.doPrivileged(new PrivilegedAction(){
                    public Object run(){
                        ClassLoader cl=null;
                        try{
                            cl=Thread.currentThread().getContextClassLoader();
                        }catch(SecurityException ex){
                        }
                        return cl;
                    }
                });
    }

    String getSystemProperty(final String propName){
        return (String)
                AccessController.doPrivileged(new PrivilegedAction(){
                    public Object run(){
                        return System.getProperty(propName);
                    }
                });
    }

    FileInputStream getFileInputStream(final File file)
            throws FileNotFoundException{
        try{
            return (FileInputStream)
                    AccessController.doPrivileged(new PrivilegedExceptionAction(){
                        public Object run() throws FileNotFoundException{
                            return new FileInputStream(file);
                        }
                    });
        }catch(PrivilegedActionException e){
            throw (FileNotFoundException)e.getException();
        }
    }

    InputStream getResourceAsStream(final ClassLoader cl,
                                    final String name){
        return (InputStream)
                AccessController.doPrivileged(new PrivilegedAction(){
                    public Object run(){
                        InputStream ris;
                        if(cl==null){
                            ris=Object.class.getResourceAsStream(name);
                        }else{
                            ris=cl.getResourceAsStream(name);
                        }
                        return ris;
                    }
                });
    }

    boolean doesFileExist(final File f){
        return ((Boolean)
                AccessController.doPrivileged(new PrivilegedAction(){
                    public Object run(){
                        return new Boolean(f.exists());
                    }
                })).booleanValue();
    }
}
