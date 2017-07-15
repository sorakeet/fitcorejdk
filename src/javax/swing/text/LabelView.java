/**
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import javax.swing.event.DocumentEvent;
import java.awt.*;

public class LabelView extends GlyphView implements TabableView{
    // --- variables ------------------------------------------------
    private Font font;
    private Color fg;
    private Color bg;
    private boolean underline;
    private boolean strike;
    private boolean superscript;
    private boolean subscript;

    public LabelView(Element elem){
        super(elem);
    }

    @Deprecated
    protected FontMetrics getFontMetrics(){
        sync();
        Container c=getContainer();
        return (c!=null)?c.getFontMetrics(font):
                Toolkit.getDefaultToolkit().getFontMetrics(font);
    }

    final void sync(){
        if(font==null){
            setPropertiesFromAttributes();
        }
    }

    protected void setPropertiesFromAttributes(){
        AttributeSet attr=getAttributes();
        if(attr!=null){
            Document d=getDocument();
            if(d instanceof StyledDocument){
                StyledDocument doc=(StyledDocument)d;
                font=doc.getFont(attr);
                fg=doc.getForeground(attr);
                if(attr.isDefined(StyleConstants.Background)){
                    bg=doc.getBackground(attr);
                }else{
                    bg=null;
                }
                setUnderline(StyleConstants.isUnderline(attr));
                setStrikeThrough(StyleConstants.isStrikeThrough(attr));
                setSuperscript(StyleConstants.isSuperscript(attr));
                setSubscript(StyleConstants.isSubscript(attr));
            }else{
                throw new StateInvariantError("LabelView needs StyledDocument");
            }
        }
    }

    public Color getBackground(){
        sync();
        return bg;
    }

    protected void setBackground(Color bg){
        this.bg=bg;
    }

    public Color getForeground(){
        sync();
        return fg;
    }

    public Font getFont(){
        sync();
        return font;
    }

    public boolean isUnderline(){
        sync();
        return underline;
    }
    // --- View methods ---------------------------------------------

    protected void setUnderline(boolean u){
        underline=u;
    }

    public boolean isStrikeThrough(){
        sync();
        return strike;
    }

    protected void setStrikeThrough(boolean s){
        strike=s;
    }

    public boolean isSubscript(){
        sync();
        return subscript;
    }

    protected void setSubscript(boolean s){
        subscript=s;
    }

    public boolean isSuperscript(){
        sync();
        return superscript;
    }

    protected void setSuperscript(boolean s){
        superscript=s;
    }

    public void changedUpdate(DocumentEvent e,Shape a,ViewFactory f){
        font=null;
        super.changedUpdate(e,a,f);
    }
}
