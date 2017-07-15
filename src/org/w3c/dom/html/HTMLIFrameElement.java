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
 * PURPOSE. See W3C License http://www.w3.org/Consortium/Legal/ for more
 * details.
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
 * PURPOSE. See W3C License http://www.w3.org/Consortium/Legal/ for more
 * details.
 */
package org.w3c.dom.html;

import org.w3c.dom.Document;

public interface HTMLIFrameElement extends HTMLElement{
    public String getAlign();

    public void setAlign(String align);

    public String getFrameBorder();

    public void setFrameBorder(String frameBorder);

    public String getHeight();

    public void setHeight(String height);

    public String getLongDesc();

    public void setLongDesc(String longDesc);

    public String getMarginHeight();

    public void setMarginHeight(String marginHeight);

    public String getMarginWidth();

    public void setMarginWidth(String marginWidth);

    public String getName();

    public void setName(String name);

    public String getScrolling();

    public void setScrolling(String scrolling);

    public String getSrc();

    public void setSrc(String src);

    public String getWidth();

    public void setWidth(String width);

    public Document getContentDocument();
}
