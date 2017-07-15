/**
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
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
 * $Id: XMLCryptoContext.java,v 1.6 2005/05/10 15:47:42 mullan Exp $
 */
/**
 * $Id: XMLCryptoContext.java,v 1.6 2005/05/10 15:47:42 mullan Exp $
 */
package javax.xml.crypto;

public interface XMLCryptoContext{
    String getBaseURI();

    void setBaseURI(String baseURI);

    KeySelector getKeySelector();

    void setKeySelector(KeySelector ks);

    URIDereferencer getURIDereferencer();

    void setURIDereferencer(URIDereferencer dereferencer);

    String getNamespacePrefix(String namespaceURI,String defaultPrefix);

    String putNamespacePrefix(String namespaceURI,String prefix);

    String getDefaultNamespacePrefix();

    void setDefaultNamespacePrefix(String defaultPrefix);

    Object setProperty(String name,Object value);

    Object getProperty(String name);

    Object get(Object key);

    Object put(Object key,Object value);
}
