/**
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
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Copyright (c) 2004 World Wide Web Consortium,
 * <p>
 * (Massachusetts Institute of Technology, European Research Consortium for
 * Informatics and Mathematics, Keio University). All Rights Reserved. This
 * work is distributed under the W3C(r) Software License [1] in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
 */
/**
 *
 *
 *
 *
 *
 * Copyright (c) 2004 World Wide Web Consortium,
 *
 * (Massachusetts Institute of Technology, European Research Consortium for
 * Informatics and Mathematics, Keio University). All Rights Reserved. This
 * work is distributed under the W3C(r) Software License [1] in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
 */
package org.w3c.dom;

public interface Element extends Node{
    public String getTagName();

    public String getAttribute(String name);

    public void setAttribute(String name,
                             String value)
            throws DOMException;

    public void removeAttribute(String name)
            throws DOMException;

    public Attr getAttributeNode(String name);

    public Attr setAttributeNode(Attr newAttr)
            throws DOMException;

    public Attr removeAttributeNode(Attr oldAttr)
            throws DOMException;

    public NodeList getElementsByTagName(String name);

    public String getAttributeNS(String namespaceURI,
                                 String localName)
            throws DOMException;

    public void setAttributeNS(String namespaceURI,
                               String qualifiedName,
                               String value)
            throws DOMException;

    public void removeAttributeNS(String namespaceURI,
                                  String localName)
            throws DOMException;

    public Attr getAttributeNodeNS(String namespaceURI,
                                   String localName)
            throws DOMException;

    public Attr setAttributeNodeNS(Attr newAttr)
            throws DOMException;

    public NodeList getElementsByTagNameNS(String namespaceURI,
                                           String localName)
            throws DOMException;

    public boolean hasAttribute(String name);

    public boolean hasAttributeNS(String namespaceURI,
                                  String localName)
            throws DOMException;

    public TypeInfo getSchemaTypeInfo();

    public void setIdAttribute(String name,
                               boolean isId)
            throws DOMException;

    public void setIdAttributeNS(String namespaceURI,
                                 String localName,
                                 boolean isId)
            throws DOMException;

    public void setIdAttributeNode(Attr idAttr,
                                   boolean isId)
            throws DOMException;
}
