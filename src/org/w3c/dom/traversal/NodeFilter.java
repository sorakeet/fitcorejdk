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
package org.w3c.dom.traversal;

import org.w3c.dom.Node;

public interface NodeFilter{
    // Constants returned by acceptNode
    public static final short FILTER_ACCEPT=1;
    public static final short FILTER_REJECT=2;
    public static final short FILTER_SKIP=3;
    // Constants for whatToShow
    public static final int SHOW_ALL=0xFFFFFFFF;
    public static final int SHOW_ELEMENT=0x00000001;
    public static final int SHOW_ATTRIBUTE=0x00000002;
    public static final int SHOW_TEXT=0x00000004;
    public static final int SHOW_CDATA_SECTION=0x00000008;
    public static final int SHOW_ENTITY_REFERENCE=0x00000010;
    public static final int SHOW_ENTITY=0x00000020;
    public static final int SHOW_PROCESSING_INSTRUCTION=0x00000040;
    public static final int SHOW_COMMENT=0x00000080;
    public static final int SHOW_DOCUMENT=0x00000100;
    public static final int SHOW_DOCUMENT_TYPE=0x00000200;
    public static final int SHOW_DOCUMENT_FRAGMENT=0x00000400;
    public static final int SHOW_NOTATION=0x00000800;

    public short acceptNode(Node n);
}
