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

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.util.Iterator;

public interface StartElement extends XMLEvent{
    public QName getName();

    public Iterator getAttributes();

    public Iterator getNamespaces();

    public Attribute getAttributeByName(QName name);

    public NamespaceContext getNamespaceContext();

    public String getNamespaceURI(String prefix);
}
