/**
 * Copyright (c) 2000, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

public class BufferCapabilities implements Cloneable{
    private ImageCapabilities frontCaps;
    private ImageCapabilities backCaps;
    private FlipContents flipContents;

    public BufferCapabilities(ImageCapabilities frontCaps,
                              ImageCapabilities backCaps,FlipContents flipContents){
        if(frontCaps==null||backCaps==null){
            throw new IllegalArgumentException(
                    "Image capabilities specified cannot be null");
        }
        this.frontCaps=frontCaps;
        this.backCaps=backCaps;
        this.flipContents=flipContents;
    }

    public ImageCapabilities getFrontBufferCapabilities(){
        return frontCaps;
    }

    public ImageCapabilities getBackBufferCapabilities(){
        return backCaps;
    }

    public boolean isPageFlipping(){
        return (getFlipContents()!=null);
    }

    public FlipContents getFlipContents(){
        return flipContents;
    }

    public boolean isFullScreenRequired(){
        return false;
    }

    public boolean isMultiBufferAvailable(){
        return false;
    }

    public Object clone(){
        try{
            return super.clone();
        }catch(CloneNotSupportedException e){
            // Since we implement Cloneable, this should never happen
            throw new InternalError(e);
        }
    }

    // Inner class FlipContents
    public static final class FlipContents extends AttributeValue{
        private static final String NAMES[]=
                {"undefined","background","prior","copied"};
        private static int I_UNDEFINED=0;
        public static final FlipContents UNDEFINED=
                new FlipContents(I_UNDEFINED);
        private static int I_BACKGROUND=1;
        public static final FlipContents BACKGROUND=
                new FlipContents(I_BACKGROUND);
        private static int I_PRIOR=2;
        public static final FlipContents PRIOR=
                new FlipContents(I_PRIOR);
        private static int I_COPIED=3;
        public static final FlipContents COPIED=
                new FlipContents(I_COPIED);

        private FlipContents(int type){
            super(type,NAMES);
        }
    } // Inner class FlipContents
}
