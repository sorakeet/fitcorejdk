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

public interface HTMLObjectElement extends HTMLElement{
    public HTMLFormElement getForm();

    public String getCode();

    public void setCode(String code);

    public String getAlign();

    public void setAlign(String align);

    public String getArchive();

    public void setArchive(String archive);

    public String getBorder();

    public void setBorder(String border);

    public String getCodeBase();

    public void setCodeBase(String codeBase);

    public String getCodeType();

    public void setCodeType(String codeType);

    public String getData();

    public void setData(String data);

    public boolean getDeclare();

    public void setDeclare(boolean declare);

    public String getHeight();

    public void setHeight(String height);

    public String getHspace();

    public void setHspace(String hspace);

    public String getName();

    public void setName(String name);

    public String getStandby();

    public void setStandby(String standby);

    public int getTabIndex();

    public void setTabIndex(int tabIndex);

    public String getType();

    public void setType(String type);

    public String getUseMap();

    public void setUseMap(String useMap);

    public String getVspace();

    public void setVspace(String vspace);

    public String getWidth();

    public void setWidth(String width);

    public Document getContentDocument();
}
