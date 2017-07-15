/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * *******************************************************************
 * *********************************************************************
 * *********************************************************************
 * ** COPYRIGHT (c) Eastman Kodak Company, 1997                      ***
 * ** As  an unpublished  work pursuant to Title 17 of the United    ***
 * ** States Code.  All rights reserved.                             ***
 * *********************************************************************
 * *********************************************************************
 **********************************************************************/
/** ********************************************************************
 **********************************************************************
 **********************************************************************
 *** COPYRIGHT (c) Eastman Kodak Company, 1997                      ***
 *** As  an unpublished  work pursuant to Title 17 of the United    ***
 *** States Code.  All rights reserved.                             ***
 **********************************************************************
 **********************************************************************
 **********************************************************************/
package java.awt.image.renderable;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.util.Vector;

public class RenderableImageOp implements RenderableImage{
    ParameterBlock paramBlock;
    ContextualRenderedImageFactory myCRIF;
    Rectangle2D boundingBox;

    public RenderableImageOp(ContextualRenderedImageFactory CRIF,
                             ParameterBlock paramBlock){
        this.myCRIF=CRIF;
        this.paramBlock=(ParameterBlock)paramBlock.clone();
    }

    public Vector<RenderableImage> getSources(){
        return getRenderableSources();
    }

    private Vector getRenderableSources(){
        Vector sources=null;
        if(paramBlock.getNumSources()>0){
            sources=new Vector();
            int i=0;
            while(i<paramBlock.getNumSources()){
                Object o=paramBlock.getSource(i);
                if(o instanceof RenderableImage){
                    sources.add((RenderableImage)o);
                    i++;
                }else{
                    break;
                }
            }
        }
        return sources;
    }

    public Object getProperty(String name){
        return myCRIF.getProperty(paramBlock,name);
    }

    public String[] getPropertyNames(){
        return myCRIF.getPropertyNames();
    }

    public boolean isDynamic(){
        return myCRIF.isDynamic();
    }

    public float getWidth(){
        if(boundingBox==null){
            boundingBox=myCRIF.getBounds2D(paramBlock);
        }
        return (float)boundingBox.getWidth();
    }

    public float getHeight(){
        if(boundingBox==null){
            boundingBox=myCRIF.getBounds2D(paramBlock);
        }
        return (float)boundingBox.getHeight();
    }

    public float getMinX(){
        if(boundingBox==null){
            boundingBox=myCRIF.getBounds2D(paramBlock);
        }
        return (float)boundingBox.getMinX();
    }

    public float getMinY(){
        if(boundingBox==null){
            boundingBox=myCRIF.getBounds2D(paramBlock);
        }
        return (float)boundingBox.getMinY();
    }

    public RenderedImage createScaledRendering(int w,int h,
                                               RenderingHints hints){
        // DSR -- code to try to get a unit scale
        double sx=(double)w/getWidth();
        double sy=(double)h/getHeight();
        if(Math.abs(sx/sy-1.0)<0.01){
            sx=sy;
        }
        AffineTransform usr2dev=AffineTransform.getScaleInstance(sx,sy);
        RenderContext newRC=new RenderContext(usr2dev,hints);
        return createRendering(newRC);
    }

    public RenderedImage createDefaultRendering(){
        AffineTransform usr2dev=new AffineTransform(); // Identity
        RenderContext newRC=new RenderContext(usr2dev);
        return createRendering(newRC);
    }

    public RenderedImage createRendering(RenderContext renderContext){
        RenderedImage image=null;
        RenderContext rcOut=null;
        // Clone the original ParameterBlock; if the ParameterBlock
        // contains RenderableImage sources, they will be replaced by
        // RenderedImages.
        ParameterBlock renderedParamBlock=(ParameterBlock)paramBlock.clone();
        Vector sources=getRenderableSources();
        try{
            // This assumes that if there is no renderable source, that there
            // is a rendered source in paramBlock
            if(sources!=null){
                Vector renderedSources=new Vector();
                for(int i=0;i<sources.size();i++){
                    rcOut=myCRIF.mapRenderContext(i,renderContext,
                            paramBlock,this);
                    RenderedImage rdrdImage=
                            ((RenderableImage)sources.elementAt(i)).createRendering(rcOut);
                    if(rdrdImage==null){
                        return null;
                    }
                    // Add this rendered image to the ParameterBlock's
                    // list of RenderedImages.
                    renderedSources.addElement(rdrdImage);
                }
                if(renderedSources.size()>0){
                    renderedParamBlock.setSources(renderedSources);
                }
            }
            return myCRIF.create(renderContext,renderedParamBlock);
        }catch(ArrayIndexOutOfBoundsException e){
            // This should never happen
            return null;
        }
    }

    public ParameterBlock setParameterBlock(ParameterBlock paramBlock){
        ParameterBlock oldParamBlock=this.paramBlock;
        this.paramBlock=(ParameterBlock)paramBlock.clone();
        return oldParamBlock;
    }

    public ParameterBlock getParameterBlock(){
        return paramBlock;
    }
}
