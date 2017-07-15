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
package org.w3c.dom.css;

import org.w3c.dom.DOMException;

public interface CSSPrimitiveValue extends CSSValue{
    // UnitTypes
    public static final short CSS_UNKNOWN=0;
    public static final short CSS_NUMBER=1;
    public static final short CSS_PERCENTAGE=2;
    public static final short CSS_EMS=3;
    public static final short CSS_EXS=4;
    public static final short CSS_PX=5;
    public static final short CSS_CM=6;
    public static final short CSS_MM=7;
    public static final short CSS_IN=8;
    public static final short CSS_PT=9;
    public static final short CSS_PC=10;
    public static final short CSS_DEG=11;
    public static final short CSS_RAD=12;
    public static final short CSS_GRAD=13;
    public static final short CSS_MS=14;
    public static final short CSS_S=15;
    public static final short CSS_HZ=16;
    public static final short CSS_KHZ=17;
    public static final short CSS_DIMENSION=18;
    public static final short CSS_STRING=19;
    public static final short CSS_URI=20;
    public static final short CSS_IDENT=21;
    public static final short CSS_ATTR=22;
    public static final short CSS_COUNTER=23;
    public static final short CSS_RECT=24;
    public static final short CSS_RGBCOLOR=25;

    public short getPrimitiveType();

    public void setFloatValue(short unitType,
                              float floatValue)
            throws DOMException;

    public float getFloatValue(short unitType)
            throws DOMException;

    public void setStringValue(short stringType,
                               String stringValue)
            throws DOMException;

    public String getStringValue()
            throws DOMException;

    public Counter getCounterValue()
            throws DOMException;

    public Rect getRectValue()
            throws DOMException;

    public RGBColor getRGBColorValue()
            throws DOMException;
}
