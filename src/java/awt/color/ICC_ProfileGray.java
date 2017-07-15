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
 * *********************************************************************
 * *********************************************************************
 * *********************************************************************
 * ** COPYRIGHT (c) Eastman Kodak Company, 1997                      ***
 * ** As  an unpublished  work pursuant to Title 17 of the United    ***
 * ** States Code.  All rights reserved.                             ***
 * *********************************************************************
 * *********************************************************************
 **********************************************************************/
/**
 **********************************************************************
 **********************************************************************
 **********************************************************************
 *** COPYRIGHT (c) Eastman Kodak Company, 1997                      ***
 *** As  an unpublished  work pursuant to Title 17 of the United    ***
 *** States Code.  All rights reserved.                             ***
 **********************************************************************
 **********************************************************************
 **********************************************************************/
package java.awt.color;

import sun.java2d.cmm.Profile;
import sun.java2d.cmm.ProfileDeferralInfo;

public class ICC_ProfileGray
        extends ICC_Profile{
    static final long serialVersionUID=-1124721290732002649L;

    ICC_ProfileGray(Profile p){
        super(p);
    }

    ICC_ProfileGray(ProfileDeferralInfo pdi){
        super(pdi);
    }

    public float[] getMediaWhitePoint(){
        return super.getMediaWhitePoint();
    }

    public float getGamma(){
        float theGamma;
        theGamma=super.getGamma(ICC_Profile.icSigGrayTRCTag);
        return theGamma;
    }

    public short[] getTRC(){
        short[] theTRC;
        theTRC=super.getTRC(ICC_Profile.icSigGrayTRCTag);
        return theTRC;
    }
}
