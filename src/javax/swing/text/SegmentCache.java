/**
 * Copyright (c) 2001, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import java.util.ArrayList;
import java.util.List;

class SegmentCache{
    private static SegmentCache sharedCache=new SegmentCache();
    private List<Segment> segments;

    public SegmentCache(){
        segments=new ArrayList<Segment>(11);
    }

    public static Segment getSharedSegment(){
        return getSharedInstance().getSegment();
    }

    public static SegmentCache getSharedInstance(){
        return sharedCache;
    }

    public static void releaseSharedSegment(Segment segment){
        getSharedInstance().releaseSegment(segment);
    }

    public Segment getSegment(){
        synchronized(this){
            int size=segments.size();
            if(size>0){
                return segments.remove(size-1);
            }
        }
        return new CachedSegment();
    }

    public void releaseSegment(Segment segment){
        if(segment instanceof CachedSegment){
            synchronized(this){
                segment.array=null;
                segment.count=0;
                segments.add(segment);
            }
        }
    }

    private static class CachedSegment extends Segment{
    }
}
