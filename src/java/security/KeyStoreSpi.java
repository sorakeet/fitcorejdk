/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security;

import javax.crypto.SecretKey;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore.CallbackHandlerProtection;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.ProtectionParameter;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Enumeration;

public abstract class KeyStoreSpi{
    public abstract Date engineGetCreationDate(String alias);

    public abstract void engineSetKeyEntry(String alias,byte[] key,
                                           Certificate[] chain)
            throws KeyStoreException;

    public abstract void engineDeleteEntry(String alias)
            throws KeyStoreException;

    public abstract Enumeration<String> engineAliases();

    public abstract int engineSize();

    public abstract String engineGetCertificateAlias(Certificate cert);

    public abstract void engineStore(OutputStream stream,char[] password)
            throws IOException, NoSuchAlgorithmException, CertificateException;

    public void engineStore(KeyStore.LoadStoreParameter param)
            throws IOException, NoSuchAlgorithmException,
            CertificateException{
        throw new UnsupportedOperationException();
    }

    public void engineLoad(KeyStore.LoadStoreParameter param)
            throws IOException, NoSuchAlgorithmException,
            CertificateException{
        if(param==null){
            engineLoad((InputStream)null,(char[])null);
            return;
        }
        if(param instanceof KeyStore.SimpleLoadStoreParameter){
            ProtectionParameter protection=param.getProtectionParameter();
            char[] password;
            if(protection instanceof PasswordProtection){
                password=((PasswordProtection)protection).getPassword();
            }else if(protection instanceof CallbackHandlerProtection){
                CallbackHandler handler=
                        ((CallbackHandlerProtection)protection).getCallbackHandler();
                PasswordCallback callback=
                        new PasswordCallback("Password: ",false);
                try{
                    handler.handle(new Callback[]{callback});
                }catch(UnsupportedCallbackException e){
                    throw new NoSuchAlgorithmException
                            ("Could not obtain password",e);
                }
                password=callback.getPassword();
                callback.clearPassword();
                if(password==null){
                    throw new NoSuchAlgorithmException
                            ("No password provided");
                }
            }else{
                throw new NoSuchAlgorithmException("ProtectionParameter must"
                        +" be PasswordProtection or CallbackHandlerProtection");
            }
            engineLoad(null,password);
            return;
        }
        throw new UnsupportedOperationException();
    }

    public abstract void engineLoad(InputStream stream,char[] password)
            throws IOException, NoSuchAlgorithmException, CertificateException;

    public KeyStore.Entry engineGetEntry(String alias,
                                         ProtectionParameter protParam)
            throws KeyStoreException, NoSuchAlgorithmException,
            UnrecoverableEntryException{
        if(!engineContainsAlias(alias)){
            return null;
        }
        if(protParam==null){
            if(engineIsCertificateEntry(alias)){
                return new KeyStore.TrustedCertificateEntry
                        (engineGetCertificate(alias));
            }else{
                throw new UnrecoverableKeyException
                        ("requested entry requires a password");
            }
        }
        if(protParam instanceof PasswordProtection){
            if(engineIsCertificateEntry(alias)){
                throw new UnsupportedOperationException
                        ("trusted certificate entries are not password-protected");
            }else if(engineIsKeyEntry(alias)){
                PasswordProtection pp=
                        (PasswordProtection)protParam;
                char[] password=pp.getPassword();
                Key key=engineGetKey(alias,password);
                if(key instanceof PrivateKey){
                    Certificate[] chain=engineGetCertificateChain(alias);
                    return new KeyStore.PrivateKeyEntry((PrivateKey)key,chain);
                }else if(key instanceof SecretKey){
                    return new KeyStore.SecretKeyEntry((SecretKey)key);
                }
            }
        }
        throw new UnsupportedOperationException();
    }

    public abstract Key engineGetKey(String alias,char[] password)
            throws NoSuchAlgorithmException, UnrecoverableKeyException;

    public abstract Certificate[] engineGetCertificateChain(String alias);

    public abstract Certificate engineGetCertificate(String alias);

    public abstract boolean engineContainsAlias(String alias);

    public abstract boolean engineIsKeyEntry(String alias);

    public abstract boolean engineIsCertificateEntry(String alias);

    public void engineSetEntry(String alias,KeyStore.Entry entry,
                               ProtectionParameter protParam)
            throws KeyStoreException{
        // get password
        if(protParam!=null&&
                !(protParam instanceof PasswordProtection)){
            throw new KeyStoreException("unsupported protection parameter");
        }
        PasswordProtection pProtect=null;
        if(protParam!=null){
            pProtect=(PasswordProtection)protParam;
        }
        // set entry
        if(entry instanceof KeyStore.TrustedCertificateEntry){
            if(protParam!=null&&pProtect.getPassword()!=null){
                // pre-1.5 style setCertificateEntry did not allow password
                throw new KeyStoreException
                        ("trusted certificate entries are not password-protected");
            }else{
                KeyStore.TrustedCertificateEntry tce=
                        (KeyStore.TrustedCertificateEntry)entry;
                engineSetCertificateEntry(alias,tce.getTrustedCertificate());
                return;
            }
        }else if(entry instanceof KeyStore.PrivateKeyEntry){
            if(pProtect==null||pProtect.getPassword()==null){
                // pre-1.5 style setKeyEntry required password
                throw new KeyStoreException
                        ("non-null password required to create PrivateKeyEntry");
            }else{
                engineSetKeyEntry
                        (alias,
                                ((KeyStore.PrivateKeyEntry)entry).getPrivateKey(),
                                pProtect.getPassword(),
                                ((KeyStore.PrivateKeyEntry)entry).getCertificateChain());
                return;
            }
        }else if(entry instanceof KeyStore.SecretKeyEntry){
            if(pProtect==null||pProtect.getPassword()==null){
                // pre-1.5 style setKeyEntry required password
                throw new KeyStoreException
                        ("non-null password required to create SecretKeyEntry");
            }else{
                engineSetKeyEntry
                        (alias,
                                ((KeyStore.SecretKeyEntry)entry).getSecretKey(),
                                pProtect.getPassword(),
                                (Certificate[])null);
                return;
            }
        }
        throw new KeyStoreException
                ("unsupported entry type: "+entry.getClass().getName());
    }

    public abstract void engineSetKeyEntry(String alias,Key key,
                                           char[] password,
                                           Certificate[] chain)
            throws KeyStoreException;

    public abstract void engineSetCertificateEntry(String alias,
                                                   Certificate cert)
            throws KeyStoreException;

    public boolean
    engineEntryInstanceOf(String alias,
                          Class<? extends KeyStore.Entry> entryClass){
        if(entryClass==KeyStore.TrustedCertificateEntry.class){
            return engineIsCertificateEntry(alias);
        }
        if(entryClass==KeyStore.PrivateKeyEntry.class){
            return engineIsKeyEntry(alias)&&
                    engineGetCertificate(alias)!=null;
        }
        if(entryClass==KeyStore.SecretKeyEntry.class){
            return engineIsKeyEntry(alias)&&
                    engineGetCertificate(alias)==null;
        }
        return false;
    }
}
