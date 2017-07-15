/**
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.imageio.plugins.common;

import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.io.IOException;

public class ReaderUtil{
    public static int[] computeUpdatedPixels(Rectangle sourceRegion,
                                             Point destinationOffset,
                                             int dstMinX,
                                             int dstMinY,
                                             int dstMaxX,
                                             int dstMaxY,
                                             int sourceXSubsampling,
                                             int sourceYSubsampling,
                                             int passXStart,
                                             int passYStart,
                                             int passWidth,
                                             int passHeight,
                                             int passPeriodX,
                                             int passPeriodY){
        int[] vals=new int[6];
        computeUpdatedPixels(sourceRegion.x,sourceRegion.width,
                destinationOffset.x,
                dstMinX,dstMaxX,sourceXSubsampling,
                passXStart,passWidth,passPeriodX,
                vals,0);
        computeUpdatedPixels(sourceRegion.y,sourceRegion.height,
                destinationOffset.y,
                dstMinY,dstMaxY,sourceYSubsampling,
                passYStart,passHeight,passPeriodY,
                vals,1);
        return vals;
    }

    // Helper for computeUpdatedPixels method
    private static void computeUpdatedPixels(int sourceOffset,
                                             int sourceExtent,
                                             int destinationOffset,
                                             int dstMin,
                                             int dstMax,
                                             int sourceSubsampling,
                                             int passStart,
                                             int passExtent,
                                             int passPeriod,
                                             int[] vals,
                                             int offset){
        // We need to satisfy the congruences:
        // dst = destinationOffset + (src - sourceOffset)/sourceSubsampling
        //
        // src - passStart == 0 (mod passPeriod)
        // src - sourceOffset == 0 (mod sourceSubsampling)
        //
        // subject to the inequalities:
        //
        // src >= passStart
        // src < passStart + passExtent
        // src >= sourceOffset
        // src < sourceOffset + sourceExtent
        // dst >= dstMin
        // dst <= dstmax
        //
        // where
        //
        // dst = destinationOffset + (src - sourceOffset)/sourceSubsampling
        //
        // For now we use a brute-force approach although we could
        // attempt to analyze the congruences.  If passPeriod and
        // sourceSubsamling are relatively prime, the period will be
        // their product.  If they share a common factor, either the
        // period will be equal to the larger value, or the sequences
        // will be completely disjoint, depending on the relationship
        // between passStart and sourceOffset.  Since we only have to do this
        // twice per image (once each for X and Y), it seems cheap enough
        // to do it the straightforward way.
        boolean gotPixel=false;
        int firstDst=-1;
        int secondDst=-1;
        int lastDst=-1;
        for(int i=0;i<passExtent;i++){
            int src=passStart+i*passPeriod;
            if(src<sourceOffset){
                continue;
            }
            if((src-sourceOffset)%sourceSubsampling!=0){
                continue;
            }
            if(src>=sourceOffset+sourceExtent){
                break;
            }
            int dst=destinationOffset+
                    (src-sourceOffset)/sourceSubsampling;
            if(dst<dstMin){
                continue;
            }
            if(dst>dstMax){
                break;
            }
            if(!gotPixel){
                firstDst=dst; // Record smallest valid pixel
                gotPixel=true;
            }else if(secondDst==-1){
                secondDst=dst; // Record second smallest valid pixel
            }
            lastDst=dst; // Record largest valid pixel
        }
        vals[offset]=firstDst;
        // If we never saw a valid pixel, set width to 0
        if(!gotPixel){
            vals[offset+2]=0;
        }else{
            vals[offset+2]=lastDst-firstDst+1;
        }
        // The period is given by the difference of any two adjacent pixels
        vals[offset+4]=Math.max(secondDst-firstDst,1);
    }

    public static int readMultiByteInteger(ImageInputStream iis)
            throws IOException{
        int value=iis.readByte();
        int result=value&0x7f;
        while((value&0x80)==0x80){
            result<<=7;
            value=iis.readByte();
            result|=(value&0x7f);
        }
        return result;
    }
}
