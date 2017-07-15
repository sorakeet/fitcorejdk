/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import javax.swing.*;
import java.awt.*;

public class StyleConstants{
    public static final String ComponentElementName="component";
    public static final String IconElementName="icon";
    public static final Object NameAttribute=new StyleConstants("name");
    public static final Object ResolveAttribute=new StyleConstants("resolver");
    public static final Object ModelAttribute=new StyleConstants("model");
    // ---- character constants -----------------------------------
    public static final Object BidiLevel=new CharacterConstants("bidiLevel");
    public static final Object FontFamily=new FontConstants("family");
    public static final Object Family=FontFamily;
    public static final Object FontSize=new FontConstants("size");
    public static final Object Size=FontSize;
    public static final Object Bold=new FontConstants("bold");
    public static final Object Italic=new FontConstants("italic");
    public static final Object Underline=new CharacterConstants("underline");
    public static final Object StrikeThrough=new CharacterConstants("strikethrough");
    public static final Object Superscript=new CharacterConstants("superscript");
    public static final Object Subscript=new CharacterConstants("subscript");
    public static final Object Foreground=new ColorConstants("foreground");
    public static final Object Background=new ColorConstants("background");
    public static final Object ComponentAttribute=new CharacterConstants("component");
    public static final Object IconAttribute=new CharacterConstants("icon");
    public static final Object ComposedTextAttribute=new StyleConstants("composed text");
    public static final Object FirstLineIndent=new ParagraphConstants("FirstLineIndent");
    public static final Object LeftIndent=new ParagraphConstants("LeftIndent");
    public static final Object RightIndent=new ParagraphConstants("RightIndent");
    public static final Object LineSpacing=new ParagraphConstants("LineSpacing");
    public static final Object SpaceAbove=new ParagraphConstants("SpaceAbove");
    public static final Object SpaceBelow=new ParagraphConstants("SpaceBelow");
    public static final Object Alignment=new ParagraphConstants("Alignment");
    public static final Object TabSet=new ParagraphConstants("TabSet");
    public static final Object Orientation=new ParagraphConstants("Orientation");
    public static final int ALIGN_LEFT=0;
    public static final int ALIGN_CENTER=1;
    public static final int ALIGN_RIGHT=2;
    public static final int ALIGN_JUSTIFIED=3;
    // --- privates ---------------------------------------------
    static Object[] keys={
            NameAttribute,ResolveAttribute,BidiLevel,
            FontFamily,FontSize,Bold,Italic,Underline,
            StrikeThrough,Superscript,Subscript,Foreground,
            Background,ComponentAttribute,IconAttribute,
            FirstLineIndent,LeftIndent,RightIndent,LineSpacing,
            SpaceAbove,SpaceBelow,Alignment,TabSet,Orientation,
            ModelAttribute,ComposedTextAttribute
    };
    // --- character attribute accessors ---------------------------
    private String representation;

    StyleConstants(String representation){
        this.representation=representation;
    }

    public static int getBidiLevel(AttributeSet a){
        Integer o=(Integer)a.getAttribute(BidiLevel);
        if(o!=null){
            return o.intValue();
        }
        return 0;  // Level 0 is base level (non-embedded) left-to-right
    }

    public static void setBidiLevel(MutableAttributeSet a,int o){
        a.addAttribute(BidiLevel,Integer.valueOf(o));
    }

    public static Component getComponent(AttributeSet a){
        return (Component)a.getAttribute(ComponentAttribute);
    }

    public static void setComponent(MutableAttributeSet a,Component c){
        a.addAttribute(AbstractDocument.ElementNameAttribute,ComponentElementName);
        a.addAttribute(ComponentAttribute,c);
    }

    public static Icon getIcon(AttributeSet a){
        return (Icon)a.getAttribute(IconAttribute);
    }

    public static void setIcon(MutableAttributeSet a,Icon c){
        a.addAttribute(AbstractDocument.ElementNameAttribute,IconElementName);
        a.addAttribute(IconAttribute,c);
    }

    public static String getFontFamily(AttributeSet a){
        String family=(String)a.getAttribute(FontFamily);
        if(family==null){
            family="Monospaced";
        }
        return family;
    }

    public static void setFontFamily(MutableAttributeSet a,String fam){
        a.addAttribute(FontFamily,fam);
    }

    public static int getFontSize(AttributeSet a){
        Integer size=(Integer)a.getAttribute(FontSize);
        if(size!=null){
            return size.intValue();
        }
        return 12;
    }

    public static void setFontSize(MutableAttributeSet a,int s){
        a.addAttribute(FontSize,Integer.valueOf(s));
    }

    public static boolean isBold(AttributeSet a){
        Boolean bold=(Boolean)a.getAttribute(Bold);
        if(bold!=null){
            return bold.booleanValue();
        }
        return false;
    }

    public static void setBold(MutableAttributeSet a,boolean b){
        a.addAttribute(Bold,Boolean.valueOf(b));
    }

    public static boolean isItalic(AttributeSet a){
        Boolean italic=(Boolean)a.getAttribute(Italic);
        if(italic!=null){
            return italic.booleanValue();
        }
        return false;
    }

    public static void setItalic(MutableAttributeSet a,boolean b){
        a.addAttribute(Italic,Boolean.valueOf(b));
    }

    public static boolean isUnderline(AttributeSet a){
        Boolean underline=(Boolean)a.getAttribute(Underline);
        if(underline!=null){
            return underline.booleanValue();
        }
        return false;
    }

    public static boolean isStrikeThrough(AttributeSet a){
        Boolean strike=(Boolean)a.getAttribute(StrikeThrough);
        if(strike!=null){
            return strike.booleanValue();
        }
        return false;
    }

    public static boolean isSuperscript(AttributeSet a){
        Boolean superscript=(Boolean)a.getAttribute(Superscript);
        if(superscript!=null){
            return superscript.booleanValue();
        }
        return false;
    }

    public static boolean isSubscript(AttributeSet a){
        Boolean subscript=(Boolean)a.getAttribute(Subscript);
        if(subscript!=null){
            return subscript.booleanValue();
        }
        return false;
    }

    public static void setUnderline(MutableAttributeSet a,boolean b){
        a.addAttribute(Underline,Boolean.valueOf(b));
    }

    public static void setStrikeThrough(MutableAttributeSet a,boolean b){
        a.addAttribute(StrikeThrough,Boolean.valueOf(b));
    }

    public static void setSuperscript(MutableAttributeSet a,boolean b){
        a.addAttribute(Superscript,Boolean.valueOf(b));
    }

    public static void setSubscript(MutableAttributeSet a,boolean b){
        a.addAttribute(Subscript,Boolean.valueOf(b));
    }

    public static Color getForeground(AttributeSet a){
        Color fg=(Color)a.getAttribute(Foreground);
        if(fg==null){
            fg=Color.black;
        }
        return fg;
    }

    public static void setForeground(MutableAttributeSet a,Color fg){
        a.addAttribute(Foreground,fg);
    }
    // --- paragraph attribute accessors ----------------------------

    public static Color getBackground(AttributeSet a){
        Color fg=(Color)a.getAttribute(Background);
        if(fg==null){
            fg=Color.black;
        }
        return fg;
    }

    public static void setBackground(MutableAttributeSet a,Color fg){
        a.addAttribute(Background,fg);
    }

    public static float getFirstLineIndent(AttributeSet a){
        Float indent=(Float)a.getAttribute(FirstLineIndent);
        if(indent!=null){
            return indent.floatValue();
        }
        return 0;
    }

    public static void setFirstLineIndent(MutableAttributeSet a,float i){
        a.addAttribute(FirstLineIndent,new Float(i));
    }

    public static float getRightIndent(AttributeSet a){
        Float indent=(Float)a.getAttribute(RightIndent);
        if(indent!=null){
            return indent.floatValue();
        }
        return 0;
    }

    public static void setRightIndent(MutableAttributeSet a,float i){
        a.addAttribute(RightIndent,new Float(i));
    }

    public static float getLeftIndent(AttributeSet a){
        Float indent=(Float)a.getAttribute(LeftIndent);
        if(indent!=null){
            return indent.floatValue();
        }
        return 0;
    }

    public static void setLeftIndent(MutableAttributeSet a,float i){
        a.addAttribute(LeftIndent,new Float(i));
    }

    public static float getLineSpacing(AttributeSet a){
        Float space=(Float)a.getAttribute(LineSpacing);
        if(space!=null){
            return space.floatValue();
        }
        return 0;
    }

    public static void setLineSpacing(MutableAttributeSet a,float i){
        a.addAttribute(LineSpacing,new Float(i));
    }

    public static float getSpaceAbove(AttributeSet a){
        Float space=(Float)a.getAttribute(SpaceAbove);
        if(space!=null){
            return space.floatValue();
        }
        return 0;
    }

    public static void setSpaceAbove(MutableAttributeSet a,float i){
        a.addAttribute(SpaceAbove,new Float(i));
    }

    public static float getSpaceBelow(AttributeSet a){
        Float space=(Float)a.getAttribute(SpaceBelow);
        if(space!=null){
            return space.floatValue();
        }
        return 0;
    }

    public static void setSpaceBelow(MutableAttributeSet a,float i){
        a.addAttribute(SpaceBelow,new Float(i));
    }

    public static int getAlignment(AttributeSet a){
        Integer align=(Integer)a.getAttribute(Alignment);
        if(align!=null){
            return align.intValue();
        }
        return ALIGN_LEFT;
    }

    public static void setAlignment(MutableAttributeSet a,int align){
        a.addAttribute(Alignment,Integer.valueOf(align));
    }

    public static TabSet getTabSet(AttributeSet a){
        TabSet tabs=(TabSet)a.getAttribute(TabSet);
        // PENDING: should this return a default?
        return tabs;
    }

    public static void setTabSet(MutableAttributeSet a,TabSet tabs){
        a.addAttribute(TabSet,tabs);
    }

    public String toString(){
        return representation;
    }

    public static class ParagraphConstants extends StyleConstants
            implements AttributeSet.ParagraphAttribute{
        private ParagraphConstants(String representation){
            super(representation);
        }
    }

    public static class CharacterConstants extends StyleConstants
            implements AttributeSet.CharacterAttribute{
        private CharacterConstants(String representation){
            super(representation);
        }
    }

    public static class ColorConstants extends StyleConstants
            implements AttributeSet.ColorAttribute, AttributeSet.CharacterAttribute{
        private ColorConstants(String representation){
            super(representation);
        }
    }

    public static class FontConstants extends StyleConstants
            implements AttributeSet.FontAttribute, AttributeSet.CharacterAttribute{
        private FontConstants(String representation){
            super(representation);
        }
    }
}
