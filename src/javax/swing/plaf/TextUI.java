/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf;

import javax.swing.text.*;
import java.awt.*;

public abstract class TextUI extends ComponentUI{
    public abstract Rectangle modelToView(JTextComponent t,int pos) throws BadLocationException;

    public abstract Rectangle modelToView(JTextComponent t,int pos,Position.Bias bias) throws BadLocationException;

    public abstract int viewToModel(JTextComponent t,Point pt);

    public abstract int viewToModel(JTextComponent t,Point pt,
                                    Position.Bias[] biasReturn);

    public abstract int getNextVisualPositionFrom(JTextComponent t,
                                                  int pos,Position.Bias b,
                                                  int direction,Position.Bias[] biasRet)
            throws BadLocationException;

    public abstract void damageRange(JTextComponent t,int p0,int p1);

    public abstract void damageRange(JTextComponent t,int p0,int p1,
                                     Position.Bias firstBias,
                                     Position.Bias secondBias);

    public abstract EditorKit getEditorKit(JTextComponent t);

    public abstract View getRootView(JTextComponent t);

    public String getToolTipText(JTextComponent t,Point pt){
        return null;
    }
}
