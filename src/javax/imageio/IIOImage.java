/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio;

import javax.imageio.metadata.IIOMetadata;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.util.List;

public class IIOImage{
    protected RenderedImage image;
    protected Raster raster;
    protected List<? extends BufferedImage> thumbnails=null;
    protected IIOMetadata metadata;

    public IIOImage(RenderedImage image,
                    List<? extends BufferedImage> thumbnails,
                    IIOMetadata metadata){
        if(image==null){
            throw new IllegalArgumentException("image == null!");
        }
        this.image=image;
        this.raster=null;
        this.thumbnails=thumbnails;
        this.metadata=metadata;
    }

    public IIOImage(Raster raster,
                    List<? extends BufferedImage> thumbnails,
                    IIOMetadata metadata){
        if(raster==null){
            throw new IllegalArgumentException("raster == null!");
        }
        this.raster=raster;
        this.image=null;
        this.thumbnails=thumbnails;
        this.metadata=metadata;
    }

    public RenderedImage getRenderedImage(){
        synchronized(this){
            return image;
        }
    }

    public void setRenderedImage(RenderedImage image){
        synchronized(this){
            if(image==null){
                throw new IllegalArgumentException("image == null!");
            }
            this.image=image;
            this.raster=null;
        }
    }

    public boolean hasRaster(){
        synchronized(this){
            return (raster!=null);
        }
    }

    public Raster getRaster(){
        synchronized(this){
            return raster;
        }
    }

    public void setRaster(Raster raster){
        synchronized(this){
            if(raster==null){
                throw new IllegalArgumentException("raster == null!");
            }
            this.raster=raster;
            this.image=null;
        }
    }

    public int getNumThumbnails(){
        return thumbnails==null?0:thumbnails.size();
    }

    public BufferedImage getThumbnail(int index){
        if(thumbnails==null){
            throw new IndexOutOfBoundsException("No thumbnails available!");
        }
        return (BufferedImage)thumbnails.get(index);
    }

    public List<? extends BufferedImage> getThumbnails(){
        return thumbnails;
    }

    public void setThumbnails(List<? extends BufferedImage> thumbnails){
        this.thumbnails=thumbnails;
    }

    public IIOMetadata getMetadata(){
        return metadata;
    }

    public void setMetadata(IIOMetadata metadata){
        this.metadata=metadata;
    }
}
