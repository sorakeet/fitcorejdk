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
 * Copyright (c) 2009 by Oracle Corporation. All Rights Reserved.
 */
/** Copyright (c) 2009 by Oracle Corporation. All Rights Reserved.
 */
package javax.xml.stream;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

public interface XMLStreamReader extends XMLStreamConstants{
    public Object getProperty(String name) throws IllegalArgumentException;

    public int next() throws XMLStreamException;

    public void require(int type,String namespaceURI,String localName) throws XMLStreamException;

    public String getElementText() throws XMLStreamException;

    public int nextTag() throws XMLStreamException;

    public boolean hasNext() throws XMLStreamException;

    public void close() throws XMLStreamException;

    public String getNamespaceURI(String prefix);

    public boolean isStartElement();

    public boolean isEndElement();

    public boolean isCharacters();

    public boolean isWhiteSpace();

    public String getAttributeValue(String namespaceURI,
                                    String localName);

    public int getAttributeCount();

    public QName getAttributeName(int index);

    public String getAttributeNamespace(int index);

    public String getAttributeLocalName(int index);

    public String getAttributePrefix(int index);

    public String getAttributeType(int index);

    public String getAttributeValue(int index);

    public boolean isAttributeSpecified(int index);

    public int getNamespaceCount();

    public String getNamespacePrefix(int index);

    public String getNamespaceURI(int index);

    public NamespaceContext getNamespaceContext();
    //  public XMLStreamReader subReader() throws XMLStreamException;
    //  public void recycle() throws XMLStreamException;

    public int getEventType();

    public String getText();

    public char[] getTextCharacters();

    public int getTextCharacters(int sourceStart,char[] target,int targetStart,int length)
            throws XMLStreamException;
    //public Reader getTextStream();

    public int getTextStart();

    public int getTextLength();

    public String getEncoding();

    public boolean hasText();

    public Location getLocation();

    public QName getName();

    public String getLocalName();

    public boolean hasName();

    public String getNamespaceURI();

    public String getPrefix();

    public String getVersion();

    public boolean isStandalone();

    public boolean standaloneSet();

    public String getCharacterEncodingScheme();

    public String getPITarget();

    public String getPIData();
}
