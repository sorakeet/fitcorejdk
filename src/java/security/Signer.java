/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security;

@Deprecated
public abstract class Signer extends Identity{
    private static final long serialVersionUID=-1763464102261361480L;
    private PrivateKey privateKey;

    protected Signer(){
        super();
    }

    public Signer(String name){
        super(name);
    }

    public Signer(String name,IdentityScope scope)
            throws KeyManagementException{
        super(name,scope);
    }

    public PrivateKey getPrivateKey(){
        check("getSignerPrivateKey");
        return privateKey;
    }

    private static void check(String directive){
        SecurityManager security=System.getSecurityManager();
        if(security!=null){
            security.checkSecurityAccess(directive);
        }
    }

    public final void setKeyPair(KeyPair pair)
            throws InvalidParameterException, KeyException{
        check("setSignerKeyPair");
        final PublicKey pub=pair.getPublic();
        PrivateKey priv=pair.getPrivate();
        if(pub==null||priv==null){
            throw new InvalidParameterException();
        }
        try{
            AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Void>(){
                        public Void run() throws KeyManagementException{
                            setPublicKey(pub);
                            return null;
                        }
                    });
        }catch(PrivilegedActionException pae){
            throw (KeyManagementException)pae.getException();
        }
        privateKey=priv;
    }

    public String toString(){
        return "[Signer]"+super.toString();
    }

    String printKeys(){
        String keys="";
        PublicKey publicKey=getPublicKey();
        if(publicKey!=null&&privateKey!=null){
            keys="\tpublic and private keys initialized";
        }else{
            keys="\tno keys";
        }
        return keys;
    }
}
