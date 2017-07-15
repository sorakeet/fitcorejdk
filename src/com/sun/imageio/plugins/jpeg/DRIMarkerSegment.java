/**
 * Copyright (c) 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.imageio.plugins.jpeg;

import org.w3c.dom.Node;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;

class DRIMarkerSegment extends MarkerSegment{
    int restartInterval=0;

    DRIMarkerSegment(JPEGBuffer buffer)
            throws IOException{
        super(buffer);
        restartInterval=(buffer.buf[buffer.bufPtr++]&0xff)<<8;
        restartInterval|=buffer.buf[buffer.bufPtr++]&0xff;
        buffer.bufAvail-=length;
    }

    DRIMarkerSegment(Node node) throws IIOInvalidTreeException{
        super(JPEG.DRI);
        updateFromNativeNode(node,true);
    }

    void updateFromNativeNode(Node node,boolean fromScratch)
            throws IIOInvalidTreeException{
        restartInterval=getAttributeValue(node,null,"interval",
                0,65535,true);
    }

    IIOMetadataNode getNativeNode(){
        IIOMetadataNode node=new IIOMetadataNode("dri");
        node.setAttribute("interval",Integer.toString(restartInterval));
        return node;
    }

    void write(ImageOutputStream ios) throws IOException{
        // We don't write DRI segments; the IJG library does.
    }

    void print(){
        printTag("DRI");
        System.out.println("Interval: "
                +Integer.toString(restartInterval));
    }
}
