/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security;

import java.io.*;

public final class SignedObject implements Serializable{
    private static final long serialVersionUID=720502720485447167L;
    private byte[] content;
    private byte[] signature;
    private String thealgorithm;

    public SignedObject(Serializable object,PrivateKey signingKey,
                        Signature signingEngine)
            throws IOException, InvalidKeyException, SignatureException{
        // creating a stream pipe-line, from a to b
        ByteArrayOutputStream b=new ByteArrayOutputStream();
        ObjectOutput a=new ObjectOutputStream(b);
        // write and flush the object content to byte array
        a.writeObject(object);
        a.flush();
        a.close();
        this.content=b.toByteArray();
        b.close();
        // now sign the encapsulated object
        this.sign(signingKey,signingEngine);
    }

    private void sign(PrivateKey signingKey,Signature signingEngine)
            throws InvalidKeyException, SignatureException{
        // initialize the signing engine
        signingEngine.initSign(signingKey);
        signingEngine.update(this.content.clone());
        this.signature=signingEngine.sign().clone();
        this.thealgorithm=signingEngine.getAlgorithm();
    }

    public Object getObject()
            throws IOException, ClassNotFoundException{
        // creating a stream pipe-line, from b to a
        ByteArrayInputStream b=new ByteArrayInputStream(this.content);
        ObjectInput a=new ObjectInputStream(b);
        Object obj=a.readObject();
        b.close();
        a.close();
        return obj;
    }

    public byte[] getSignature(){
        return this.signature.clone();
    }

    public String getAlgorithm(){
        return this.thealgorithm;
    }

    public boolean verify(PublicKey verificationKey,
                          Signature verificationEngine)
            throws InvalidKeyException, SignatureException{
        verificationEngine.initVerify(verificationKey);
        verificationEngine.update(this.content.clone());
        return verificationEngine.verify(this.signature.clone());
    }

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException{
        ObjectInputStream.GetField fields=s.readFields();
        content=((byte[])fields.get("content",null)).clone();
        signature=((byte[])fields.get("signature",null)).clone();
        thealgorithm=(String)fields.get("thealgorithm",null);
    }
}
