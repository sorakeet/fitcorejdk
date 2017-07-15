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
package javax.xml.stream.events;

import javax.xml.namespace.QName;
import java.io.Writer;

public interface XMLEvent extends javax.xml.stream.XMLStreamConstants{
    public int getEventType();

    javax.xml.stream.Location getLocation();

    public boolean isStartElement();

    public boolean isAttribute();

    public boolean isNamespace();

    public boolean isEndElement();

    public boolean isEntityReference();

    public boolean isProcessingInstruction();

    public boolean isCharacters();

    public boolean isStartDocument();

    public boolean isEndDocument();

    public StartElement asStartElement();

    public EndElement asEndElement();

    public Characters asCharacters();

    public QName getSchemaType();

    public void writeAsEncodedUnicode(Writer writer)
            throws javax.xml.stream.XMLStreamException;
}
