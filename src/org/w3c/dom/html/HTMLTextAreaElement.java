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

public interface HTMLTextAreaElement extends HTMLElement{
    public String getDefaultValue();

    public void setDefaultValue(String defaultValue);

    public HTMLFormElement getForm();

    public String getAccessKey();

    public void setAccessKey(String accessKey);

    public int getCols();

    public void setCols(int cols);

    public boolean getDisabled();

    public void setDisabled(boolean disabled);

    public String getName();

    public void setName(String name);

    public boolean getReadOnly();

    public void setReadOnly(boolean readOnly);

    public int getRows();

    public void setRows(int rows);

    public int getTabIndex();

    public void setTabIndex(int tabIndex);

    public String getType();

    public String getValue();

    public void setValue(String value);

    public void blur();

    public void focus();

    public void select();
}
