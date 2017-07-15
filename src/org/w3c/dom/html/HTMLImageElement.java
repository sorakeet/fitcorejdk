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

public interface HTMLImageElement extends HTMLElement{
    public String getLowSrc();

    public void setLowSrc(String lowSrc);

    public String getName();

    public void setName(String name);

    public String getAlign();

    public void setAlign(String align);

    public String getAlt();

    public void setAlt(String alt);

    public String getBorder();

    public void setBorder(String border);

    public String getHeight();

    public void setHeight(String height);

    public String getHspace();

    public void setHspace(String hspace);

    public boolean getIsMap();

    public void setIsMap(boolean isMap);

    public String getLongDesc();

    public void setLongDesc(String longDesc);

    public String getSrc();

    public void setSrc(String src);

    public String getUseMap();

    public void setUseMap(String useMap);

    public String getVspace();

    public void setVspace(String vspace);

    public String getWidth();

    public void setWidth(String width);
}
