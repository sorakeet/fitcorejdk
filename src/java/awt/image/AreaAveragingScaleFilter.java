/**
 * Copyright (c) 1996, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.image;

public class AreaAveragingScaleFilter extends ReplicateScaleFilter{
    private static final ColorModel rgbmodel=ColorModel.getRGBdefault();
    private static final int neededHints=(TOPDOWNLEFTRIGHT
            |COMPLETESCANLINES);
    private boolean passthrough;
    private float reds[], greens[], blues[], alphas[];
    private int savedy;
    private int savedyrem;

    public AreaAveragingScaleFilter(int width,int height){
        super(width,height);
    }

    public void setHints(int hints){
        passthrough=((hints&neededHints)!=neededHints);
        super.setHints(hints);
    }

    public void setPixels(int x,int y,int w,int h,
                          ColorModel model,byte pixels[],int off,
                          int scansize){
        if(passthrough){
            super.setPixels(x,y,w,h,model,pixels,off,scansize);
        }else{
            accumPixels(x,y,w,h,model,pixels,off,scansize);
        }
    }

    private void accumPixels(int x,int y,int w,int h,
                             ColorModel model,Object pixels,int off,
                             int scansize){
        if(reds==null){
            makeAccumBuffers();
        }
        int sy=y;
        int syrem=destHeight;
        int dy, dyrem;
        if(sy==0){
            dy=0;
            dyrem=0;
        }else{
            dy=savedy;
            dyrem=savedyrem;
        }
        while(sy<y+h){
            int amty;
            if(dyrem==0){
                for(int i=0;i<destWidth;i++){
                    alphas[i]=reds[i]=greens[i]=blues[i]=0f;
                }
                dyrem=srcHeight;
            }
            if(syrem<dyrem){
                amty=syrem;
            }else{
                amty=dyrem;
            }
            int sx=0;
            int dx=0;
            int sxrem=0;
            int dxrem=srcWidth;
            float a=0f, r=0f, g=0f, b=0f;
            while(sx<w){
                if(sxrem==0){
                    sxrem=destWidth;
                    int rgb;
                    if(pixels instanceof byte[]){
                        rgb=((byte[])pixels)[off+sx]&0xff;
                    }else{
                        rgb=((int[])pixels)[off+sx];
                    }
                    // getRGB() always returns non-premultiplied components
                    rgb=model.getRGB(rgb);
                    a=rgb>>>24;
                    r=(rgb>>16)&0xff;
                    g=(rgb>>8)&0xff;
                    b=rgb&0xff;
                    // premultiply the components if necessary
                    if(a!=255.0f){
                        float ascale=a/255.0f;
                        r*=ascale;
                        g*=ascale;
                        b*=ascale;
                    }
                }
                int amtx;
                if(sxrem<dxrem){
                    amtx=sxrem;
                }else{
                    amtx=dxrem;
                }
                float mult=((float)amtx)*amty;
                alphas[dx]+=mult*a;
                reds[dx]+=mult*r;
                greens[dx]+=mult*g;
                blues[dx]+=mult*b;
                if((sxrem-=amtx)==0){
                    sx++;
                }
                if((dxrem-=amtx)==0){
                    dx++;
                    dxrem=srcWidth;
                }
            }
            if((dyrem-=amty)==0){
                int outpix[]=calcRow();
                do{
                    consumer.setPixels(0,dy,destWidth,1,
                            rgbmodel,outpix,0,destWidth);
                    dy++;
                }while((syrem-=amty)>=amty&&amty==srcHeight);
            }else{
                syrem-=amty;
            }
            if(syrem==0){
                syrem=destHeight;
                sy++;
                off+=scansize;
            }
        }
        savedyrem=dyrem;
        savedy=dy;
    }

    private void makeAccumBuffers(){
        reds=new float[destWidth];
        greens=new float[destWidth];
        blues=new float[destWidth];
        alphas=new float[destWidth];
    }

    private int[] calcRow(){
        float origmult=((float)srcWidth)*srcHeight;
        if(outpixbuf==null||!(outpixbuf instanceof int[])){
            outpixbuf=new int[destWidth];
        }
        int[] outpix=(int[])outpixbuf;
        for(int x=0;x<destWidth;x++){
            float mult=origmult;
            int a=Math.round(alphas[x]/mult);
            if(a<=0){
                a=0;
            }else if(a>=255){
                a=255;
            }else{
                // un-premultiply the components (by modifying mult here, we
                // are effectively doing the divide by mult and divide by
                // alpha in the same step)
                mult=alphas[x]/255;
            }
            int r=Math.round(reds[x]/mult);
            int g=Math.round(greens[x]/mult);
            int b=Math.round(blues[x]/mult);
            if(r<0){
                r=0;
            }else if(r>255){
                r=255;
            }
            if(g<0){
                g=0;
            }else if(g>255){
                g=255;
            }
            if(b<0){
                b=0;
            }else if(b>255){
                b=255;
            }
            outpix[x]=(a<<24|r<<16|g<<8|b);
        }
        return outpix;
    }

    public void setPixels(int x,int y,int w,int h,
                          ColorModel model,int pixels[],int off,
                          int scansize){
        if(passthrough){
            super.setPixels(x,y,w,h,model,pixels,off,scansize);
        }else{
            accumPixels(x,y,w,h,model,pixels,off,scansize);
        }
    }
}
