/**
 * Copyright (c) 1996, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans;

import java.awt.*;
import java.awt.image.ImageProducer;
import java.net.URL;

public class SimpleBeanInfo implements BeanInfo{
    public BeanDescriptor getBeanDescriptor(){
        return null;
    }

    public EventSetDescriptor[] getEventSetDescriptors(){
        return null;
    }

    public int getDefaultEventIndex(){
        return -1;
    }

    public PropertyDescriptor[] getPropertyDescriptors(){
        return null;
    }

    public int getDefaultPropertyIndex(){
        return -1;
    }

    public MethodDescriptor[] getMethodDescriptors(){
        return null;
    }

    public BeanInfo[] getAdditionalBeanInfo(){
        return null;
    }

    public Image getIcon(int iconKind){
        return null;
    }

    public Image loadImage(final String resourceName){
        try{
            final URL url=getClass().getResource(resourceName);
            if(url!=null){
                final ImageProducer ip=(ImageProducer)url.getContent();
                if(ip!=null){
                    return Toolkit.getDefaultToolkit().createImage(ip);
                }
            }
        }catch(final Exception ignored){
        }
        return null;
    }
}
