/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * Provides interfaces for generating RSA (Rivest, Shamir and
 * Adleman AsymmetricCipher algorithm)
 * keys as defined in the RSA Laboratory Technical Note
 * PKCS#1, and DSA (Digital Signature
 * Algorithm) keys as defined in NIST's FIPS-186.
 * <p>
 * Note that these interfaces are intended only for key
 * implementations whose key material is accessible and
 * available. These interfaces are not intended for key
 * implementations whose key material resides in
 * inaccessible, protected storage (such as in a
 * hardware device).
 * <p>
 * For more developer information on how to use these
 * interfaces, including information on how to design
 * {@code Key} classes for hardware devices, please refer
 * to these cryptographic provider developer guides:
 * <ul>
 * <li><a href=
 * "{@docRoot}/../technotes/guides/security/crypto/HowToImplAProvider.html">
 * <b>How to Implement a Provider for the
 * Java&trade; Cryptography Architecture
 * </b></a></li>
 * </ul>
 * <p>
 * <h2>Package Specification</h2>
 * <p>
 * <ul>
 * <li>PKCS #1: RSA Encryption Standard, Version 1.5, November 1993 </li>
 * <li>Federal Information Processing Standards Publication (FIPS PUB) 186:
 * Digital Signature Standard (DSS) </li>
 * </ul>
 * <p>
 * <h2>Related Documentation</h2>
 * <p>
 * For further documentation, please see:
 * <ul>
 * <li>
 * <a href=
 * "{@docRoot}/../technotes/guides/security/crypto/CryptoSpec.html">
 * <b>Java&trade;
 * Cryptography Architecture API Specification and Reference
 * </b></a></li>
 * </ul>
 *
 * @since JDK1.1
 */
/**
 * Provides interfaces for generating RSA (Rivest, Shamir and
 * Adleman AsymmetricCipher algorithm)
 * keys as defined in the RSA Laboratory Technical Note
 * PKCS#1, and DSA (Digital Signature
 * Algorithm) keys as defined in NIST's FIPS-186.
 * <p>
 * Note that these interfaces are intended only for key
 * implementations whose key material is accessible and
 * available. These interfaces are not intended for key
 * implementations whose key material resides in
 * inaccessible, protected storage (such as in a
 * hardware device).
 * <p>
 * For more developer information on how to use these
 * interfaces, including information on how to design
 * {@code Key} classes for hardware devices, please refer
 * to these cryptographic provider developer guides:
 * <ul>
 * <li><a href=
 * "{@docRoot}/../technotes/guides/security/crypto/HowToImplAProvider.html">
 * <b>How to Implement a Provider for the
 * Java&trade; Cryptography Architecture
 * </b></a></li>
 * </ul>
 * <p>
 * <h2>Package Specification</h2>
 * <p>
 * <ul>
 * <li>PKCS #1: RSA Encryption Standard, Version 1.5, November 1993 </li>
 * <li>Federal Information Processing Standards Publication (FIPS PUB) 186:
 * Digital Signature Standard (DSS) </li>
 * </ul>
 * <p>
 * <h2>Related Documentation</h2>
 * <p>
 * For further documentation, please see:
 * <ul>
 * <li>
 * <a href=
 * "{@docRoot}/../technotes/guides/security/crypto/CryptoSpec.html">
 * <b>Java&trade;
 * Cryptography Architecture API Specification and Reference
 * </b></a></li>
 * </ul>
 *
 * @since JDK1.1
 */
package java.security.interfaces;
