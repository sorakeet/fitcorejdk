/**
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
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
 * (C) Copyright IBM Corp. 1998 - All Rights Reserved
 * <p>
 * The original version of this source code and documentation is copyrighted
 * and owned by IBM, Inc. These materials are provided under terms of a
 * License Agreement between IBM and Sun. This technology is protected by
 * multiple US and International patents. This notice and attribution to IBM
 * may not be removed.
 */
/**
 * (C) Copyright IBM Corp. 1998 - All Rights Reserved
 *
 * The original version of this source code and documentation is copyrighted
 * and owned by IBM, Inc. These materials are provided under terms of a
 * License Agreement between IBM and Sun. This technology is protected by
 * multiple US and International patents. This notice and attribution to IBM
 * may not be removed.
 *
 */
package java.awt;

import java.util.Locale;
import java.util.ResourceBundle;

public final class ComponentOrientation implements java.io.Serializable{
    private static final long serialVersionUID=-4113291392143563828L;
    // Internal constants used in the implementation
    private static final int UNK_BIT=1;
    private static final int HORIZ_BIT=2;
    public static final ComponentOrientation RIGHT_TO_LEFT=
            new ComponentOrientation(HORIZ_BIT);
    private static final int LTR_BIT=4;
    public static final ComponentOrientation LEFT_TO_RIGHT=
            new ComponentOrientation(HORIZ_BIT|LTR_BIT);
    public static final ComponentOrientation UNKNOWN=
            new ComponentOrientation(HORIZ_BIT|LTR_BIT|UNK_BIT);
    private int orientation;

    private ComponentOrientation(int value){
        orientation=value;
    }

    @Deprecated
    public static ComponentOrientation getOrientation(ResourceBundle bdl){
        ComponentOrientation result=null;
        try{
            result=(ComponentOrientation)bdl.getObject("Orientation");
        }catch(Exception e){
        }
        if(result==null){
            result=getOrientation(bdl.getLocale());
        }
        if(result==null){
            result=getOrientation(Locale.getDefault());
        }
        return result;
    }

    public static ComponentOrientation getOrientation(Locale locale){
        // A more flexible implementation would consult a ResourceBundle
        // to find the appropriate orientation.  Until pluggable locales
        // are introduced however, the flexiblity isn't really needed.
        // So we choose efficiency instead.
        String lang=locale.getLanguage();
        if("iw".equals(lang)||"ar".equals(lang)
                ||"fa".equals(lang)||"ur".equals(lang)){
            return RIGHT_TO_LEFT;
        }else{
            return LEFT_TO_RIGHT;
        }
    }

    public boolean isHorizontal(){
        return (orientation&HORIZ_BIT)!=0;
    }

    public boolean isLeftToRight(){
        return (orientation&LTR_BIT)!=0;
    }
}
