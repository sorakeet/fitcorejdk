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

public interface HTMLSelectElement extends HTMLElement{
    public String getType();

    public int getSelectedIndex();

    public void setSelectedIndex(int selectedIndex);

    public String getValue();

    public void setValue(String value);

    public int getLength();

    public HTMLFormElement getForm();

    public HTMLCollection getOptions();

    public boolean getDisabled();

    public void setDisabled(boolean disabled);

    public boolean getMultiple();

    public void setMultiple(boolean multiple);

    public String getName();

    public void setName(String name);

    public int getSize();

    public void setSize(int size);

    public int getTabIndex();

    public void setTabIndex(int tabIndex);

    public void add(HTMLElement element,
                    HTMLElement before)
            throws DOMException;

    public void remove(int index);

    public void blur();

    public void focus();
}
