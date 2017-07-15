/**
 * Copyright (c) 2005, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.nimbus;

import java.awt.*;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class ImageCache{
    // Singleton Instance
    private static final ImageCache instance=new ImageCache();
    // Ordered Map keyed by args hash, ordered by most recent accessed entry.
    private final LinkedHashMap<Integer,PixelCountSoftReference> map=
            new LinkedHashMap<Integer,PixelCountSoftReference>(16,0.75f,true);
    // Maximum number of pixels to cache, this is used if maxCount
    private final int maxPixelCount;
    // Maximum cached image size in pxiels
    private final int maxSingleImagePixelSize;
    // The current number of pixels stored in the cache
    private int currentPixelCount=0;
    // Lock for concurrent access to map
    private ReadWriteLock lock=new ReentrantReadWriteLock();
    // Reference queue for tracking lost softreferences to images in the cache
    private ReferenceQueue<Image> referenceQueue=new ReferenceQueue<Image>();

    public ImageCache(){
        this.maxPixelCount=(8*1024*1024)/4; // 8Mb of pixels
        this.maxSingleImagePixelSize=300*300;
    }

    public ImageCache(int maxPixelCount,int maxSingleImagePixelSize){
        this.maxPixelCount=maxPixelCount;
        this.maxSingleImagePixelSize=maxSingleImagePixelSize;
    }

    static ImageCache getInstance(){
        return instance;
    }

    public void flush(){
        lock.readLock().lock();
        try{
            map.clear();
        }finally{
            lock.readLock().unlock();
        }
    }

    public Image getImage(GraphicsConfiguration config,int w,int h,Object... args){
        lock.readLock().lock();
        try{
            PixelCountSoftReference ref=map.get(hash(config,w,h,args));
            // check reference has not been lost and the key truly matches, in case of false positive hash match
            if(ref!=null&&ref.equals(config,w,h,args)){
                return ref.get();
            }else{
                return null;
            }
        }finally{
            lock.readLock().unlock();
        }
    }

    private int hash(GraphicsConfiguration config,int w,int h,Object... args){
        int hash;
        hash=(config!=null?config.hashCode():0);
        hash=31*hash+w;
        hash=31*hash+h;
        hash=31*hash+Arrays.deepHashCode(args);
        return hash;
    }

    public boolean setImage(Image image,GraphicsConfiguration config,int w,int h,Object... args){
        if(!isImageCachable(w,h)) return false;
        int hash=hash(config,w,h,args);
        lock.writeLock().lock();
        try{
            PixelCountSoftReference ref=map.get(hash);
            // check if currently in map
            if(ref!=null&&ref.get()==image){
                return true;
            }
            // clear out old
            if(ref!=null){
                currentPixelCount-=ref.pixelCount;
                map.remove(hash);
            }
            // add new image to pixel count
            int newPixelCount=image.getWidth(null)*image.getHeight(null);
            currentPixelCount+=newPixelCount;
            // clean out lost references if not enough space
            if(currentPixelCount>maxPixelCount){
                while((ref=(PixelCountSoftReference)referenceQueue.poll())!=null){
                    //reference lost
                    map.remove(ref.hash);
                    currentPixelCount-=ref.pixelCount;
                }
            }
            // remove old items till there is enough free space
            if(currentPixelCount>maxPixelCount){
                Iterator<Map.Entry<Integer,PixelCountSoftReference>> mapIter=map.entrySet().iterator();
                while((currentPixelCount>maxPixelCount)&&mapIter.hasNext()){
                    Map.Entry<Integer,PixelCountSoftReference> entry=mapIter.next();
                    mapIter.remove();
                    Image img=entry.getValue().get();
                    if(img!=null) img.flush();
                    currentPixelCount-=entry.getValue().pixelCount;
                }
            }
            // finaly put new in map
            map.put(hash,new PixelCountSoftReference(image,referenceQueue,newPixelCount,hash,config,w,h,args));
            return true;
        }finally{
            lock.writeLock().unlock();
        }
    }

    public boolean isImageCachable(int w,int h){
        return (w*h)<maxSingleImagePixelSize;
    }

    private static class PixelCountSoftReference extends SoftReference<Image>{
        private final int pixelCount;
        private final int hash;
        // key parts
        private final GraphicsConfiguration config;
        private final int w;
        private final int h;
        private final Object[] args;

        public PixelCountSoftReference(Image referent,ReferenceQueue<? super Image> q,int pixelCount,int hash,
                                       GraphicsConfiguration config,int w,int h,Object[] args){
            super(referent,q);
            this.pixelCount=pixelCount;
            this.hash=hash;
            this.config=config;
            this.w=w;
            this.h=h;
            this.args=args;
        }

        public boolean equals(GraphicsConfiguration config,int w,int h,Object[] args){
            return config==this.config&&
                    w==this.w&&
                    h==this.h&&
                    Arrays.equals(args,this.args);
        }
    }
}
