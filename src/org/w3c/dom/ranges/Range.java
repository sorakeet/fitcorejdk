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
 * Copyright (c) 2000 World Wide Web Consortium,
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
 * Copyright (c) 2000 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 * See W3C License http://www.w3.org/Consortium/Legal/ for more details.
 */
package org.w3c.dom.ranges;

import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

public interface Range{
    // CompareHow
    public static final short START_TO_START=0;
    public static final short START_TO_END=1;
    public static final short END_TO_END=2;
    public static final short END_TO_START=3;

    public Node getStartContainer()
            throws DOMException;

    public int getStartOffset()
            throws DOMException;

    public Node getEndContainer()
            throws DOMException;

    public int getEndOffset()
            throws DOMException;

    public boolean getCollapsed()
            throws DOMException;

    public Node getCommonAncestorContainer()
            throws DOMException;

    public void setStart(Node refNode,
                         int offset)
            throws RangeException, DOMException;

    public void setEnd(Node refNode,
                       int offset)
            throws RangeException, DOMException;

    public void setStartBefore(Node refNode)
            throws RangeException, DOMException;

    public void setStartAfter(Node refNode)
            throws RangeException, DOMException;

    public void setEndBefore(Node refNode)
            throws RangeException, DOMException;

    public void setEndAfter(Node refNode)
            throws RangeException, DOMException;

    public void collapse(boolean toStart)
            throws DOMException;

    public void selectNode(Node refNode)
            throws RangeException, DOMException;

    public void selectNodeContents(Node refNode)
            throws RangeException, DOMException;

    public short compareBoundaryPoints(short how,
                                       Range sourceRange)
            throws DOMException;

    public void deleteContents()
            throws DOMException;

    public DocumentFragment extractContents()
            throws DOMException;

    public DocumentFragment cloneContents()
            throws DOMException;

    public void insertNode(Node newNode)
            throws DOMException, RangeException;

    public void surroundContents(Node newParent)
            throws DOMException, RangeException;

    public Range cloneRange()
            throws DOMException;

    public String toString()
            throws DOMException;

    public void detach()
            throws DOMException;
}
