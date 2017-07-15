/**
 * Copyright (c) 1995, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.image;

import java.util.Hashtable;

public class ImageFilter implements ImageConsumer, Cloneable{
    protected ImageConsumer consumer;

    public ImageFilter getFilterInstance(ImageConsumer ic){
        ImageFilter instance=(ImageFilter)clone();
        instance.consumer=ic;
        return instance;
    }

    public Object clone(){
        try{
            return super.clone();
        }catch(CloneNotSupportedException e){
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
    }

    public void setDimensions(int width,int height){
        consumer.setDimensions(width,height);
    }

    public void setProperties(Hashtable<?,?> props){
        Hashtable<Object,Object> p=(Hashtable<Object,Object>)props.clone();
        Object o=p.get("filters");
        if(o==null){
            p.put("filters",toString());
        }else if(o instanceof String){
            p.put("filters",((String)o)+toString());
        }
        consumer.setProperties(p);
    }

    public void setColorModel(ColorModel model){
        consumer.setColorModel(model);
    }

    public void setHints(int hints){
        consumer.setHints(hints);
    }

    public void setPixels(int x,int y,int w,int h,
                          ColorModel model,byte pixels[],int off,
                          int scansize){
        consumer.setPixels(x,y,w,h,model,pixels,off,scansize);
    }

    public void setPixels(int x,int y,int w,int h,
                          ColorModel model,int pixels[],int off,
                          int scansize){
        consumer.setPixels(x,y,w,h,model,pixels,off,scansize);
    }

    public void imageComplete(int status){
        consumer.imageComplete(status);
    }

    public void resendTopDownLeftRight(ImageProducer ip){
        ip.requestTopDownLeftRightResend(this);
    }
}
