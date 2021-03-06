/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.io.Serializable;

public class SizeRequirements implements Serializable{
    public int minimum;
    public int preferred;
    public int maximum;
    public float alignment;

    public SizeRequirements(){
        minimum=0;
        preferred=0;
        maximum=0;
        alignment=0.5f;
    }

    public SizeRequirements(int min,int pref,int max,float a){
        minimum=min;
        preferred=pref;
        maximum=max;
        alignment=a>1.0f?1.0f:a<0.0f?0.0f:a;
    }

    public static SizeRequirements getTiledSizeRequirements(SizeRequirements[]
                                                                    children){
        SizeRequirements total=new SizeRequirements();
        for(int i=0;i<children.length;i++){
            SizeRequirements req=children[i];
            total.minimum=(int)Math.min((long)total.minimum+(long)req.minimum,Integer.MAX_VALUE);
            total.preferred=(int)Math.min((long)total.preferred+(long)req.preferred,Integer.MAX_VALUE);
            total.maximum=(int)Math.min((long)total.maximum+(long)req.maximum,Integer.MAX_VALUE);
        }
        return total;
    }

    public static SizeRequirements getAlignedSizeRequirements(SizeRequirements[]
                                                                      children){
        SizeRequirements totalAscent=new SizeRequirements();
        SizeRequirements totalDescent=new SizeRequirements();
        for(int i=0;i<children.length;i++){
            SizeRequirements req=children[i];
            int ascent=(int)(req.alignment*req.minimum);
            int descent=req.minimum-ascent;
            totalAscent.minimum=Math.max(ascent,totalAscent.minimum);
            totalDescent.minimum=Math.max(descent,totalDescent.minimum);
            ascent=(int)(req.alignment*req.preferred);
            descent=req.preferred-ascent;
            totalAscent.preferred=Math.max(ascent,totalAscent.preferred);
            totalDescent.preferred=Math.max(descent,totalDescent.preferred);
            ascent=(int)(req.alignment*req.maximum);
            descent=req.maximum-ascent;
            totalAscent.maximum=Math.max(ascent,totalAscent.maximum);
            totalDescent.maximum=Math.max(descent,totalDescent.maximum);
        }
        int min=(int)Math.min((long)totalAscent.minimum+(long)totalDescent.minimum,Integer.MAX_VALUE);
        int pref=(int)Math.min((long)totalAscent.preferred+(long)totalDescent.preferred,Integer.MAX_VALUE);
        int max=(int)Math.min((long)totalAscent.maximum+(long)totalDescent.maximum,Integer.MAX_VALUE);
        float alignment=0.0f;
        if(min>0){
            alignment=(float)totalAscent.minimum/min;
            alignment=alignment>1.0f?1.0f:alignment<0.0f?0.0f:alignment;
        }
        return new SizeRequirements(min,pref,max,alignment);
    }

    public static void calculateTiledPositions(int allocated,
                                               SizeRequirements total,
                                               SizeRequirements[] children,
                                               int[] offsets,
                                               int[] spans){
        calculateTiledPositions(allocated,total,children,offsets,spans,true);
    }

    public static void calculateTiledPositions(int allocated,
                                               SizeRequirements total,
                                               SizeRequirements[] children,
                                               int[] offsets,
                                               int[] spans,
                                               boolean forward){
        // The total argument turns out to be a bad idea since the
        // total of all the children can overflow the integer used to
        // hold the total.  The total must therefore be calculated and
        // stored in long variables.
        long min=0;
        long pref=0;
        long max=0;
        for(int i=0;i<children.length;i++){
            min+=children[i].minimum;
            pref+=children[i].preferred;
            max+=children[i].maximum;
        }
        if(allocated>=pref){
            expandedTile(allocated,min,pref,max,children,offsets,spans,forward);
        }else{
            compressedTile(allocated,min,pref,max,children,offsets,spans,forward);
        }
    }

    private static void compressedTile(int allocated,long min,long pref,long max,
                                       SizeRequirements[] request,
                                       int[] offsets,int[] spans,
                                       boolean forward){
        // ---- determine what we have to work with ----
        float totalPlay=Math.min(pref-allocated,pref-min);
        float factor=(pref-min==0)?0.0f:totalPlay/(pref-min);
        // ---- make the adjustments ----
        int totalOffset;
        if(forward){
            // lay out with offsets increasing from 0
            totalOffset=0;
            for(int i=0;i<spans.length;i++){
                offsets[i]=totalOffset;
                SizeRequirements req=request[i];
                float play=factor*(req.preferred-req.minimum);
                spans[i]=(int)(req.preferred-play);
                totalOffset=(int)Math.min((long)totalOffset+(long)spans[i],Integer.MAX_VALUE);
            }
        }else{
            // lay out with offsets decreasing from the end of the allocation
            totalOffset=allocated;
            for(int i=0;i<spans.length;i++){
                SizeRequirements req=request[i];
                float play=factor*(req.preferred-req.minimum);
                spans[i]=(int)(req.preferred-play);
                offsets[i]=totalOffset-spans[i];
                totalOffset=(int)Math.max((long)totalOffset-(long)spans[i],0);
            }
        }
    }

    private static void expandedTile(int allocated,long min,long pref,long max,
                                     SizeRequirements[] request,
                                     int[] offsets,int[] spans,
                                     boolean forward){
        // ---- determine what we have to work with ----
        float totalPlay=Math.min(allocated-pref,max-pref);
        float factor=(max-pref==0)?0.0f:totalPlay/(max-pref);
        // ---- make the adjustments ----
        int totalOffset;
        if(forward){
            // lay out with offsets increasing from 0
            totalOffset=0;
            for(int i=0;i<spans.length;i++){
                offsets[i]=totalOffset;
                SizeRequirements req=request[i];
                int play=(int)(factor*(req.maximum-req.preferred));
                spans[i]=(int)Math.min((long)req.preferred+(long)play,Integer.MAX_VALUE);
                totalOffset=(int)Math.min((long)totalOffset+(long)spans[i],Integer.MAX_VALUE);
            }
        }else{
            // lay out with offsets decreasing from the end of the allocation
            totalOffset=allocated;
            for(int i=0;i<spans.length;i++){
                SizeRequirements req=request[i];
                int play=(int)(factor*(req.maximum-req.preferred));
                spans[i]=(int)Math.min((long)req.preferred+(long)play,Integer.MAX_VALUE);
                offsets[i]=totalOffset-spans[i];
                totalOffset=(int)Math.max((long)totalOffset-(long)spans[i],0);
            }
        }
    }

    public static void calculateAlignedPositions(int allocated,
                                                 SizeRequirements total,
                                                 SizeRequirements[] children,
                                                 int[] offsets,
                                                 int[] spans){
        calculateAlignedPositions(allocated,total,children,offsets,spans,true);
    }

    public static void calculateAlignedPositions(int allocated,
                                                 SizeRequirements total,
                                                 SizeRequirements[] children,
                                                 int[] offsets,
                                                 int[] spans,
                                                 boolean normal){
        float totalAlignment=normal?total.alignment:1.0f-total.alignment;
        int totalAscent=(int)(allocated*totalAlignment);
        int totalDescent=allocated-totalAscent;
        for(int i=0;i<children.length;i++){
            SizeRequirements req=children[i];
            float alignment=normal?req.alignment:1.0f-req.alignment;
            int maxAscent=(int)(req.maximum*alignment);
            int maxDescent=req.maximum-maxAscent;
            int ascent=Math.min(totalAscent,maxAscent);
            int descent=Math.min(totalDescent,maxDescent);
            offsets[i]=totalAscent-ascent;
            spans[i]=(int)Math.min((long)ascent+(long)descent,Integer.MAX_VALUE);
        }
    }

    // This method was used by the JTable - which now uses a different technique.
    public static int[] adjustSizes(int delta,SizeRequirements[] children){
        return new int[0];
    }

    public String toString(){
        return "["+minimum+","+preferred+","+maximum+"]@"+alignment;
    }
}
