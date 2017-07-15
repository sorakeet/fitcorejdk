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
 * Copyright (c) 2002 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 * See W3C License http://www.w3.org/Consortium/Legal/ for more details.
 */
/**
 *
 *
 *
 *
 *
 * Copyright (c) 2002 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 * See W3C License http://www.w3.org/Consortium/Legal/ for more details.
 */
package org.w3c.dom.xpath;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

public interface XPathResult{
    // XPathResultType
    public static final short ANY_TYPE=0;
    public static final short NUMBER_TYPE=1;
    public static final short STRING_TYPE=2;
    public static final short BOOLEAN_TYPE=3;
    public static final short UNORDERED_NODE_ITERATOR_TYPE=4;
    public static final short ORDERED_NODE_ITERATOR_TYPE=5;
    public static final short UNORDERED_NODE_SNAPSHOT_TYPE=6;
    public static final short ORDERED_NODE_SNAPSHOT_TYPE=7;
    public static final short ANY_UNORDERED_NODE_TYPE=8;
    public static final short FIRST_ORDERED_NODE_TYPE=9;

    public short getResultType();

    public double getNumberValue()
            throws XPathException;

    public String getStringValue()
            throws XPathException;

    public boolean getBooleanValue()
            throws XPathException;

    public Node getSingleNodeValue()
            throws XPathException;

    public boolean getInvalidIteratorState();

    public int getSnapshotLength()
            throws XPathException;

    public Node iterateNext()
            throws XPathException, DOMException;

    public Node snapshotItem(int index)
            throws XPathException;
}
