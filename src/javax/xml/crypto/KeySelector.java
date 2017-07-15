/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * $Id: KeySelector.java,v 1.6 2005/05/10 15:47:42 mullan Exp $
 */
/**
 * $Id: KeySelector.java,v 1.6 2005/05/10 15:47:42 mullan Exp $
 */
package javax.xml.crypto;

import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import java.security.Key;

public abstract class KeySelector{
    protected KeySelector(){
    }

    public static KeySelector singletonKeySelector(Key key){
        return new SingletonKeySelector(key);
    }

    public abstract KeySelectorResult select(KeyInfo keyInfo,Purpose purpose,
                                             AlgorithmMethod method,XMLCryptoContext context)
            throws KeySelectorException;

    public static class Purpose{
        public static final Purpose SIGN=new Purpose("sign");
        public static final Purpose VERIFY=new Purpose("verify");
        public static final Purpose ENCRYPT=new Purpose("encrypt");
        public static final Purpose DECRYPT=new Purpose("decrypt");
        private final String name;
        private Purpose(String name){
            this.name=name;
        }

        public String toString(){
            return name;
        }
    }

    private static class SingletonKeySelector extends KeySelector{
        private final Key key;

        SingletonKeySelector(Key key){
            if(key==null){
                throw new NullPointerException();
            }
            this.key=key;
        }

        public KeySelectorResult select(KeyInfo keyInfo,Purpose purpose,
                                        AlgorithmMethod method,XMLCryptoContext context)
                throws KeySelectorException{
            return new KeySelectorResult(){
                public Key getKey(){
                    return key;
                }
            };
        }
    }
}
