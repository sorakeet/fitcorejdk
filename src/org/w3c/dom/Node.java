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

public interface Node{
    // NodeType
    public static final short ELEMENT_NODE=1;
    public static final short ATTRIBUTE_NODE=2;
    public static final short TEXT_NODE=3;
    public static final short CDATA_SECTION_NODE=4;
    public static final short ENTITY_REFERENCE_NODE=5;
    public static final short ENTITY_NODE=6;
    public static final short PROCESSING_INSTRUCTION_NODE=7;
    public static final short COMMENT_NODE=8;
    public static final short DOCUMENT_NODE=9;
    public static final short DOCUMENT_TYPE_NODE=10;
    public static final short DOCUMENT_FRAGMENT_NODE=11;
    public static final short NOTATION_NODE=12;
    // DocumentPosition
    public static final short DOCUMENT_POSITION_DISCONNECTED=0x01;
    public static final short DOCUMENT_POSITION_PRECEDING=0x02;
    public static final short DOCUMENT_POSITION_FOLLOWING=0x04;
    public static final short DOCUMENT_POSITION_CONTAINS=0x08;
    public static final short DOCUMENT_POSITION_CONTAINED_BY=0x10;
    public static final short DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC=0x20;

    public String getNodeName();

    public String getNodeValue()
            throws DOMException;

    public void setNodeValue(String nodeValue)
            throws DOMException;

    public short getNodeType();

    public Node getParentNode();

    public NodeList getChildNodes();

    public Node getFirstChild();

    public Node getLastChild();

    public Node getPreviousSibling();

    public Node getNextSibling();

    public NamedNodeMap getAttributes();

    public Document getOwnerDocument();

    public Node insertBefore(Node newChild,
                             Node refChild)
            throws DOMException;

    public Node replaceChild(Node newChild,
                             Node oldChild)
            throws DOMException;

    public Node removeChild(Node oldChild)
            throws DOMException;

    public Node appendChild(Node newChild)
            throws DOMException;

    public boolean hasChildNodes();

    public Node cloneNode(boolean deep);

    public void normalize();

    public boolean isSupported(String feature,
                               String version);

    public String getNamespaceURI();

    public String getPrefix();

    public void setPrefix(String prefix)
            throws DOMException;

    public String getLocalName();

    public boolean hasAttributes();

    public String getBaseURI();

    public short compareDocumentPosition(Node other)
            throws DOMException;

    public String getTextContent()
            throws DOMException;

    public void setTextContent(String textContent)
            throws DOMException;

    public boolean isSameNode(Node other);

    public String lookupPrefix(String namespaceURI);

    public boolean isDefaultNamespace(String namespaceURI);

    public String lookupNamespaceURI(String prefix);

    public boolean isEqualNode(Node arg);

    public Object getFeature(String feature,
                             String version);

    public Object setUserData(String key,
                              Object data,
                              UserDataHandler handler);

    public Object getUserData(String key);
}
