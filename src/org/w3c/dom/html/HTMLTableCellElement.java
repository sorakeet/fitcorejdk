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

public interface HTMLTableCellElement extends HTMLElement{
    public int getCellIndex();

    public String getAbbr();

    public void setAbbr(String abbr);

    public String getAlign();

    public void setAlign(String align);

    public String getAxis();

    public void setAxis(String axis);

    public String getBgColor();

    public void setBgColor(String bgColor);

    public String getCh();

    public void setCh(String ch);

    public String getChOff();

    public void setChOff(String chOff);

    public int getColSpan();

    public void setColSpan(int colSpan);

    public String getHeaders();

    public void setHeaders(String headers);

    public String getHeight();

    public void setHeight(String height);

    public boolean getNoWrap();

    public void setNoWrap(boolean noWrap);

    public int getRowSpan();

    public void setRowSpan(int rowSpan);

    public String getScope();

    public void setScope(String scope);

    public String getVAlign();

    public void setVAlign(String vAlign);

    public String getWidth();

    public void setWidth(String width);
}
