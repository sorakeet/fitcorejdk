/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.cert;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.Security;
import java.util.Date;

public abstract class X509Certificate extends Certificate{
    private static final String X509_PROVIDER="cert.provider.x509v1";
    private static String X509Provider;

    static{
        X509Provider=AccessController.doPrivileged(
                new PrivilegedAction<String>(){
                    public String run(){
                        return Security.getProperty(X509_PROVIDER);
                    }
                }
        );
    }

    public static final X509Certificate getInstance(InputStream inStream)
            throws CertificateException{
        return getInst((Object)inStream);
    }

    private static final X509Certificate getInst(Object value)
            throws CertificateException{
        /**
         * This turns out not to work for now. To run under JDK1.2 we would
         * need to call beginPrivileged() but we can't do that and run
         * under JDK1.1.
         */
        String className=X509Provider;
        if(className==null||className.length()==0){
            // shouldn't happen, but assume corrupted properties file
            // provide access to sun implementation
            className="com.sun.security.cert.internal.x509.X509V1CertImpl";
        }
        try{
            Class<?>[] params=null;
            if(value instanceof InputStream){
                params=new Class<?>[]{InputStream.class};
            }else if(value instanceof byte[]){
                params=new Class<?>[]{value.getClass()};
            }else
                throw new CertificateException("Unsupported argument type");
            Class<?> certClass=Class.forName(className);
            // get the appropriate constructor and instantiate it
            Constructor<?> cons=certClass.getConstructor(params);
            // get a new instance
            Object obj=cons.newInstance(new Object[]{value});
            return (X509Certificate)obj;
        }catch(ClassNotFoundException e){
            throw new CertificateException("Could not find class: "+e);
        }catch(IllegalAccessException e){
            throw new CertificateException("Could not access class: "+e);
        }catch(InstantiationException e){
            throw new CertificateException("Problems instantiating: "+e);
        }catch(InvocationTargetException e){
            throw new CertificateException("InvocationTargetException: "
                    +e.getTargetException());
        }catch(NoSuchMethodException e){
            throw new CertificateException("Could not find class method: "
                    +e.getMessage());
        }
    }

    public static final X509Certificate getInstance(byte[] certData)
            throws CertificateException{
        return getInst((Object)certData);
    }

    public abstract void checkValidity()
            throws CertificateExpiredException, CertificateNotYetValidException;

    public abstract void checkValidity(Date date)
            throws CertificateExpiredException, CertificateNotYetValidException;

    public abstract int getVersion();

    public abstract BigInteger getSerialNumber();

    public abstract Principal getIssuerDN();

    public abstract Principal getSubjectDN();

    public abstract Date getNotBefore();

    public abstract Date getNotAfter();

    public abstract String getSigAlgName();

    public abstract String getSigAlgOID();

    public abstract byte[] getSigAlgParams();
}
