/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 * <p>
 * The original version of this source code and documentation is copyrighted
 * and owned by Taligent, Inc., a wholly-owned subsidiary of IBM. These
 * materials are provided under terms of a License Agreement between Taligent
 * and Sun. This technology is protected by multiple US and International
 * patents. This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 * <p>
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 * <p>
 * The original version of this source code and documentation is copyrighted
 * and owned by Taligent, Inc., a wholly-owned subsidiary of IBM. These
 * materials are provided under terms of a License Agreement between Taligent
 * and Sun. This technology is protected by multiple US and International
 * patents. This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 * <p>
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 * <p>
 * The original version of this source code and documentation is copyrighted
 * and owned by Taligent, Inc., a wholly-owned subsidiary of IBM. These
 * materials are provided under terms of a License Agreement between Taligent
 * and Sun. This technology is protected by multiple US and International
 * patents. This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 */
/**
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 *
 *   The original version of this source code and documentation is copyrighted
 * and owned by Taligent, Inc., a wholly-owned subsidiary of IBM. These
 * materials are provided under terms of a License Agreement between Taligent
 * and Sun. This technology is protected by multiple US and International
 * patents. This notice and attribution to Taligent may not be removed.
 *   Taligent is a registered trademark of Taligent, Inc.
 *
 */
package java.text;

import java.io.Serializable;

public abstract class Format implements Serializable, Cloneable{
    private static final long serialVersionUID=-299282585814624189L;

    protected Format(){
    }

    public AttributedCharacterIterator formatToCharacterIterator(Object obj){
        return createAttributedCharacterIterator(format(obj));
    }

    public final String format(Object obj){
        return format(obj,new StringBuffer(),new FieldPosition(0)).toString();
    }

    public abstract StringBuffer format(Object obj,StringBuffer toAppendTo,FieldPosition pos);

    AttributedCharacterIterator createAttributedCharacterIterator(String s){
        AttributedString as=new AttributedString(s);
        return as.getIterator();
    }

    public Object parseObject(String source) throws ParseException{
        ParsePosition pos=new ParsePosition(0);
        Object result=parseObject(source,pos);
        if(pos.index==0){
            throw new ParseException("Format.parseObject(String) failed",
                    pos.errorIndex);
        }
        return result;
    }

    public abstract Object parseObject(String source,ParsePosition pos);

    public Object clone(){
        try{
            return super.clone();
        }catch(CloneNotSupportedException e){
            // will never happen
            throw new InternalError(e);
        }
    }

    AttributedCharacterIterator createAttributedCharacterIterator(AttributedCharacterIterator[] iterators){
        AttributedString as=new AttributedString(iterators);
        return as.getIterator();
    }

    AttributedCharacterIterator createAttributedCharacterIterator(String string,AttributedCharacterIterator.Attribute key,Object value){
        AttributedString as=new AttributedString(string);
        as.addAttribute(key,value);
        return as.getIterator();
    }

    AttributedCharacterIterator createAttributedCharacterIterator(AttributedCharacterIterator iterator,AttributedCharacterIterator.Attribute key,Object value){
        AttributedString as=new AttributedString(iterator);
        as.addAttribute(key,value);
        return as.getIterator();
    }

    interface FieldDelegate{
        public void formatted(Field attr,Object value,int start,
                              int end,StringBuffer buffer);

        public void formatted(int fieldID,Field attr,Object value,
                              int start,int end,StringBuffer buffer);
    }

    public static class Field extends AttributedCharacterIterator.Attribute{
        // Proclaim serial compatibility with 1.4 FCS
        private static final long serialVersionUID=276966692217360283L;

        protected Field(String name){
            super(name);
        }
    }
}
