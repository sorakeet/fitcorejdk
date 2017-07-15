/**
 * Copyright (c) 1995, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.image;

import java.util.Hashtable;

public class FilteredImageSource implements ImageProducer{
    ImageProducer src;
    ImageFilter filter;
    private Hashtable proxies;

    public FilteredImageSource(ImageProducer orig,ImageFilter imgf){
        src=orig;
        filter=imgf;
    }

    public synchronized void addConsumer(ImageConsumer ic){
        if(proxies==null){
            proxies=new Hashtable();
        }
        if(!proxies.containsKey(ic)){
            ImageFilter imgf=filter.getFilterInstance(ic);
            proxies.put(ic,imgf);
            src.addConsumer(imgf);
        }
    }

    public synchronized boolean isConsumer(ImageConsumer ic){
        return (proxies!=null&&proxies.containsKey(ic));
    }

    public synchronized void removeConsumer(ImageConsumer ic){
        if(proxies!=null){
            ImageFilter imgf=(ImageFilter)proxies.get(ic);
            if(imgf!=null){
                src.removeConsumer(imgf);
                proxies.remove(ic);
                if(proxies.isEmpty()){
                    proxies=null;
                }
            }
        }
    }

    public void startProduction(ImageConsumer ic){
        if(proxies==null){
            proxies=new Hashtable();
        }
        ImageFilter imgf=(ImageFilter)proxies.get(ic);
        if(imgf==null){
            imgf=filter.getFilterInstance(ic);
            proxies.put(ic,imgf);
        }
        src.startProduction(imgf);
    }

    public void requestTopDownLeftRightResend(ImageConsumer ic){
        if(proxies!=null){
            ImageFilter imgf=(ImageFilter)proxies.get(ic);
            if(imgf!=null){
                imgf.resendTopDownLeftRight(src);
            }
        }
    }
}
