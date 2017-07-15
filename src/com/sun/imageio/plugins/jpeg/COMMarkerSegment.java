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
import java.io.UnsupportedEncodingException;

class COMMarkerSegment extends MarkerSegment{
    private static final String ENCODING="ISO-8859-1";

    COMMarkerSegment(JPEGBuffer buffer) throws IOException{
        super(buffer);
        loadData(buffer);
    }

    COMMarkerSegment(String comment){
        super(JPEG.COM);
        data=comment.getBytes(); // Default encoding
    }

    COMMarkerSegment(Node node) throws IIOInvalidTreeException{
        super(JPEG.COM);
        if(node instanceof IIOMetadataNode){
            IIOMetadataNode ourNode=(IIOMetadataNode)node;
            data=(byte[])ourNode.getUserObject();
        }
        if(data==null){
            String comment=
                    node.getAttributes().getNamedItem("comment").getNodeValue();
            if(comment!=null){
                data=comment.getBytes(); // Default encoding
            }else{
                throw new IIOInvalidTreeException("Empty comment node!",node);
            }
        }
    }

    IIOMetadataNode getNativeNode(){
        IIOMetadataNode node=new IIOMetadataNode("com");
        node.setAttribute("comment",getComment());
        if(data!=null){
            node.setUserObject(data.clone());
        }
        return node;
    }

    String getComment(){
        try{
            return new String(data,ENCODING);
        }catch(UnsupportedEncodingException e){
        }  // Won't happen
        return null;
    }

    void write(ImageOutputStream ios) throws IOException{
        length=2+data.length;
        writeTag(ios);
        ios.write(data);
    }

    void print(){
        printTag("COM");
        System.out.println("<"+getComment()+">");
    }
}
