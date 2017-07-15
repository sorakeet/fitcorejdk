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

public interface HTMLAnchorElement extends HTMLElement{
    public String getAccessKey();

    public void setAccessKey(String accessKey);

    public String getCharset();

    public void setCharset(String charset);

    public String getCoords();

    public void setCoords(String coords);

    public String getHref();

    public void setHref(String href);

    public String getHreflang();

    public void setHreflang(String hreflang);

    public String getName();

    public void setName(String name);

    public String getRel();

    public void setRel(String rel);

    public String getRev();

    public void setRev(String rev);

    public String getShape();

    public void setShape(String shape);

    public int getTabIndex();

    public void setTabIndex(int tabIndex);

    public String getTarget();

    public void setTarget(String target);

    public String getType();

    public void setType(String type);

    public void blur();

    public void focus();
}
