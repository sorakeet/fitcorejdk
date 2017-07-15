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

import org.w3c.dom.DOMException;

public interface HTMLTableElement extends HTMLElement{
    public HTMLTableCaptionElement getCaption();

    public void setCaption(HTMLTableCaptionElement caption);

    public HTMLTableSectionElement getTHead();

    public void setTHead(HTMLTableSectionElement tHead);

    public HTMLTableSectionElement getTFoot();

    public void setTFoot(HTMLTableSectionElement tFoot);

    public HTMLCollection getRows();

    public HTMLCollection getTBodies();

    public String getAlign();

    public void setAlign(String align);

    public String getBgColor();

    public void setBgColor(String bgColor);

    public String getBorder();

    public void setBorder(String border);

    public String getCellPadding();

    public void setCellPadding(String cellPadding);

    public String getCellSpacing();

    public void setCellSpacing(String cellSpacing);

    public String getFrame();

    public void setFrame(String frame);

    public String getRules();

    public void setRules(String rules);

    public String getSummary();

    public void setSummary(String summary);

    public String getWidth();

    public void setWidth(String width);

    public HTMLElement createTHead();

    public void deleteTHead();

    public HTMLElement createTFoot();

    public void deleteTFoot();

    public HTMLElement createCaption();

    public void deleteCaption();

    public HTMLElement insertRow(int index)
            throws DOMException;

    public void deleteRow(int index)
            throws DOMException;
}
