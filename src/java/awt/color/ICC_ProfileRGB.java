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

public class ICC_ProfileRGB
        extends ICC_Profile{
    public static final int REDCOMPONENT=0;
    public static final int GREENCOMPONENT=1;
    public static final int BLUECOMPONENT=2;
    static final long serialVersionUID=8505067385152579334L;

    ICC_ProfileRGB(Profile p){
        super(p);
    }

    ICC_ProfileRGB(ProfileDeferralInfo pdi){
        super(pdi);
    }

    public float[] getMediaWhitePoint(){
        return super.getMediaWhitePoint();
    }

    public float getGamma(int component){
        float theGamma;
        int theSignature;
        switch(component){
            case REDCOMPONENT:
                theSignature=ICC_Profile.icSigRedTRCTag;
                break;
            case GREENCOMPONENT:
                theSignature=ICC_Profile.icSigGreenTRCTag;
                break;
            case BLUECOMPONENT:
                theSignature=ICC_Profile.icSigBlueTRCTag;
                break;
            default:
                throw new IllegalArgumentException("Must be Red, Green, or Blue");
        }
        theGamma=super.getGamma(theSignature);
        return theGamma;
    }

    public short[] getTRC(int component){
        short[] theTRC;
        int theSignature;
        switch(component){
            case REDCOMPONENT:
                theSignature=ICC_Profile.icSigRedTRCTag;
                break;
            case GREENCOMPONENT:
                theSignature=ICC_Profile.icSigGreenTRCTag;
                break;
            case BLUECOMPONENT:
                theSignature=ICC_Profile.icSigBlueTRCTag;
                break;
            default:
                throw new IllegalArgumentException("Must be Red, Green, or Blue");
        }
        theTRC=super.getTRC(theSignature);
        return theTRC;
    }

    public float[][] getMatrix(){
        float[][] theMatrix=new float[3][3];
        float[] tmpMatrix;
        tmpMatrix=getXYZTag(ICC_Profile.icSigRedColorantTag);
        theMatrix[0][0]=tmpMatrix[0];
        theMatrix[1][0]=tmpMatrix[1];
        theMatrix[2][0]=tmpMatrix[2];
        tmpMatrix=getXYZTag(ICC_Profile.icSigGreenColorantTag);
        theMatrix[0][1]=tmpMatrix[0];
        theMatrix[1][1]=tmpMatrix[1];
        theMatrix[2][1]=tmpMatrix[2];
        tmpMatrix=getXYZTag(ICC_Profile.icSigBlueColorantTag);
        theMatrix[0][2]=tmpMatrix[0];
        theMatrix[1][2]=tmpMatrix[1];
        theMatrix[2][2]=tmpMatrix[2];
        return theMatrix;
    }
}
