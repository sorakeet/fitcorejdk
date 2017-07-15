/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.accessibility;

import javax.swing.text.AttributeSet;
import java.awt.*;

public interface AccessibleText{
    public static final int CHARACTER=1;
    public static final int WORD=2;
    public static final int SENTENCE=3;

    public int getIndexAtPoint(Point p);

    public Rectangle getCharacterBounds(int i);

    public int getCharCount();

    public int getCaretPosition();

    public String getAtIndex(int part,int index);

    public String getAfterIndex(int part,int index);

    public String getBeforeIndex(int part,int index);

    public AttributeSet getCharacterAttribute(int i);

    public int getSelectionStart();

    public int getSelectionEnd();

    public String getSelectedText();
}
