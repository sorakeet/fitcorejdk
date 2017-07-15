/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * (C) Copyright Taligent, Inc. 1996 - 1997, All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998, All Rights Reserved
 * <p>
 * The original version of this source code and documentation is
 * copyrighted and owned by Taligent, Inc., a wholly-owned subsidiary
 * of IBM. These materials are provided under terms of a License
 * Agreement between Taligent and Sun. This technology is protected
 * by multiple US and International patents.
 * <p>
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 */
/**
 * (C) Copyright Taligent, Inc. 1996 - 1997, All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998, All Rights Reserved
 *
 * The original version of this source code and documentation is
 * copyrighted and owned by Taligent, Inc., a wholly-owned subsidiary
 * of IBM. These materials are provided under terms of a License
 * Agreement between Taligent and Sun. This technology is protected
 * by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 *
 */
package java.awt.font;

import java.io.InvalidObjectException;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.HashMap;
import java.util.Map;

public final class TextAttribute extends Attribute{
    //
    // For use with Font.
    //
    public static final TextAttribute FAMILY=
            new TextAttribute("family");
    public static final TextAttribute WEIGHT=
            new TextAttribute("weight");
    public static final Float WEIGHT_EXTRA_LIGHT=
            Float.valueOf(0.5f);
    public static final Float WEIGHT_LIGHT=
            Float.valueOf(0.75f);
    public static final Float WEIGHT_DEMILIGHT=
            Float.valueOf(0.875f);
    public static final Float WEIGHT_REGULAR=
            Float.valueOf(1.0f);
    public static final Float WEIGHT_SEMIBOLD=
            Float.valueOf(1.25f);
    public static final Float WEIGHT_MEDIUM=
            Float.valueOf(1.5f);
    public static final Float WEIGHT_DEMIBOLD=
            Float.valueOf(1.75f);
    public static final Float WEIGHT_BOLD=
            Float.valueOf(2.0f);
    public static final Float WEIGHT_HEAVY=
            Float.valueOf(2.25f);
    public static final Float WEIGHT_EXTRABOLD=
            Float.valueOf(2.5f);
    public static final Float WEIGHT_ULTRABOLD=
            Float.valueOf(2.75f);
    public static final TextAttribute WIDTH=
            new TextAttribute("width");
    public static final Float WIDTH_CONDENSED=
            Float.valueOf(0.75f);
    public static final Float WIDTH_SEMI_CONDENSED=
            Float.valueOf(0.875f);
    public static final Float WIDTH_REGULAR=
            Float.valueOf(1.0f);
    public static final Float WIDTH_SEMI_EXTENDED=
            Float.valueOf(1.25f);
    public static final Float WIDTH_EXTENDED=
            Float.valueOf(1.5f);
    public static final TextAttribute POSTURE=
            new TextAttribute("posture");
    public static final Float POSTURE_REGULAR=
            Float.valueOf(0.0f);
    public static final Float POSTURE_OBLIQUE=
            Float.valueOf(0.20f);
    public static final TextAttribute SIZE=
            new TextAttribute("size");
    public static final TextAttribute TRANSFORM=
            new TextAttribute("transform");
    public static final TextAttribute SUPERSCRIPT=
            new TextAttribute("superscript");
    public static final Integer SUPERSCRIPT_SUPER=
            Integer.valueOf(1);
    public static final Integer SUPERSCRIPT_SUB=
            Integer.valueOf(-1);
    public static final TextAttribute FONT=
            new TextAttribute("font");
    public static final TextAttribute CHAR_REPLACEMENT=
            new TextAttribute("char_replacement");
    //
    // Adornments added to text.
    //
    public static final TextAttribute FOREGROUND=
            new TextAttribute("foreground");
    public static final TextAttribute BACKGROUND=
            new TextAttribute("background");
    public static final TextAttribute UNDERLINE=
            new TextAttribute("underline");
    public static final Integer UNDERLINE_ON=
            Integer.valueOf(0);
    public static final TextAttribute STRIKETHROUGH=
            new TextAttribute("strikethrough");
    public static final Boolean STRIKETHROUGH_ON=
            Boolean.TRUE;
    //
    // Attributes use to control layout of text on a line.
    //
    public static final TextAttribute RUN_DIRECTION=
            new TextAttribute("run_direction");
    public static final Boolean RUN_DIRECTION_LTR=
            Boolean.FALSE;
    public static final Boolean RUN_DIRECTION_RTL=
            Boolean.TRUE;
    public static final TextAttribute BIDI_EMBEDDING=
            new TextAttribute("bidi_embedding");
    public static final TextAttribute JUSTIFICATION=
            new TextAttribute("justification");
    public static final Float JUSTIFICATION_FULL=
            Float.valueOf(1.0f);
    public static final Float JUSTIFICATION_NONE=
            Float.valueOf(0.0f);
    //
    // For use by input method.
    //
    public static final TextAttribute INPUT_METHOD_HIGHLIGHT=
            new TextAttribute("input method highlight");
    public static final TextAttribute INPUT_METHOD_UNDERLINE=
            new TextAttribute("input method underline");
    public static final Integer UNDERLINE_LOW_ONE_PIXEL=
            Integer.valueOf(1);
    public static final Integer UNDERLINE_LOW_TWO_PIXEL=
            Integer.valueOf(2);
    public static final Integer UNDERLINE_LOW_DOTTED=
            Integer.valueOf(3);
    public static final Integer UNDERLINE_LOW_GRAY=
            Integer.valueOf(4);
    public static final Integer UNDERLINE_LOW_DASHED=
            Integer.valueOf(5);
    public static final TextAttribute SWAP_COLORS=
            new TextAttribute("swap_colors");
    public static final Boolean SWAP_COLORS_ON=
            Boolean.TRUE;
    public static final TextAttribute NUMERIC_SHAPING=
            new TextAttribute("numeric_shaping");
    public static final TextAttribute KERNING=
            new TextAttribute("kerning");
    public static final Integer KERNING_ON=
            Integer.valueOf(1);
    public static final TextAttribute LIGATURES=
            new TextAttribute("ligatures");
    public static final Integer LIGATURES_ON=
            Integer.valueOf(1);
    public static final TextAttribute TRACKING=
            new TextAttribute("tracking");
    public static final Float TRACKING_TIGHT=
            Float.valueOf(-.04f);
    public static final Float TRACKING_LOOSE=
            Float.valueOf(.04f);
    // Serialization compatibility with Java 2 platform v1.2.
    // 1.2 will throw an InvalidObjectException if ever asked to
    // deserialize INPUT_METHOD_UNDERLINE.
    // This shouldn't happen in real life.
    static final long serialVersionUID=7744112784117861702L;
    // table of all instances in this class, used by readResolve
    private static final Map<String,TextAttribute>
            instanceMap=new HashMap<String,TextAttribute>(29);

    protected TextAttribute(String name){
        super(name);
        if(this.getClass()==TextAttribute.class){
            instanceMap.put(name,this);
        }
    }

    protected Object readResolve() throws InvalidObjectException{
        if(this.getClass()!=TextAttribute.class){
            throw new InvalidObjectException(
                    "subclass didn't correctly implement readResolve");
        }
        TextAttribute instance=instanceMap.get(getName());
        if(instance!=null){
            return instance;
        }else{
            throw new InvalidObjectException("unknown attribute name");
        }
    }
}
