/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import java.io.IOException;
import java.io.ObjectInputStream;

public class FlowLayout implements LayoutManager, java.io.Serializable{
    public static final int LEFT=0;
    public static final int CENTER=1;
    public static final int RIGHT=2;
    public static final int LEADING=3;
    public static final int TRAILING=4;
    private static final long serialVersionUID=-7262534875583282631L;
    //
    // the internal serial version which says which version was written
    // - 0 (default) for versions before the Java 2 platform, v1.2
    // - 1 for version >= Java 2 platform v1.2, which includes "newAlign" field
    //
    private static final int currentSerialVersion=1;
    int align;          // This is for 1.1 serialization compatibility
    int newAlign;       // This is the one we actually use
    int hgap;
    int vgap;
    private boolean alignOnBaseline;
    private int serialVersionOnStream=currentSerialVersion;

    public FlowLayout(){
        this(CENTER,5,5);
    }

    public FlowLayout(int align,int hgap,int vgap){
        this.hgap=hgap;
        this.vgap=vgap;
        setAlignment(align);
    }

    public FlowLayout(int align){
        this(align,5,5);
    }

    public int getAlignment(){
        return newAlign;
    }

    public void setAlignment(int align){
        this.newAlign=align;
        // this.align is used only for serialization compatibility,
        // so set it to a value compatible with the 1.1 version
        // of the class
        switch(align){
            case LEADING:
                this.align=LEFT;
                break;
            case TRAILING:
                this.align=RIGHT;
                break;
            default:
                this.align=align;
                break;
        }
    }

    public int getHgap(){
        return hgap;
    }

    public void setHgap(int hgap){
        this.hgap=hgap;
    }

    public int getVgap(){
        return vgap;
    }

    public void setVgap(int vgap){
        this.vgap=vgap;
    }

    public void addLayoutComponent(String name,Component comp){
    }

    public void removeLayoutComponent(Component comp){
    }

    public Dimension preferredLayoutSize(Container target){
        synchronized(target.getTreeLock()){
            Dimension dim=new Dimension(0,0);
            int nmembers=target.getComponentCount();
            boolean firstVisibleComponent=true;
            boolean useBaseline=getAlignOnBaseline();
            int maxAscent=0;
            int maxDescent=0;
            for(int i=0;i<nmembers;i++){
                Component m=target.getComponent(i);
                if(m.isVisible()){
                    Dimension d=m.getPreferredSize();
                    dim.height=Math.max(dim.height,d.height);
                    if(firstVisibleComponent){
                        firstVisibleComponent=false;
                    }else{
                        dim.width+=hgap;
                    }
                    dim.width+=d.width;
                    if(useBaseline){
                        int baseline=m.getBaseline(d.width,d.height);
                        if(baseline>=0){
                            maxAscent=Math.max(maxAscent,baseline);
                            maxDescent=Math.max(maxDescent,d.height-baseline);
                        }
                    }
                }
            }
            if(useBaseline){
                dim.height=Math.max(maxAscent+maxDescent,dim.height);
            }
            Insets insets=target.getInsets();
            dim.width+=insets.left+insets.right+hgap*2;
            dim.height+=insets.top+insets.bottom+vgap*2;
            return dim;
        }
    }

    public boolean getAlignOnBaseline(){
        return alignOnBaseline;
    }

    public void setAlignOnBaseline(boolean alignOnBaseline){
        this.alignOnBaseline=alignOnBaseline;
    }

    public Dimension minimumLayoutSize(Container target){
        synchronized(target.getTreeLock()){
            boolean useBaseline=getAlignOnBaseline();
            Dimension dim=new Dimension(0,0);
            int nmembers=target.getComponentCount();
            int maxAscent=0;
            int maxDescent=0;
            boolean firstVisibleComponent=true;
            for(int i=0;i<nmembers;i++){
                Component m=target.getComponent(i);
                if(m.visible){
                    Dimension d=m.getMinimumSize();
                    dim.height=Math.max(dim.height,d.height);
                    if(firstVisibleComponent){
                        firstVisibleComponent=false;
                    }else{
                        dim.width+=hgap;
                    }
                    dim.width+=d.width;
                    if(useBaseline){
                        int baseline=m.getBaseline(d.width,d.height);
                        if(baseline>=0){
                            maxAscent=Math.max(maxAscent,baseline);
                            maxDescent=Math.max(maxDescent,
                                    dim.height-baseline);
                        }
                    }
                }
            }
            if(useBaseline){
                dim.height=Math.max(maxAscent+maxDescent,dim.height);
            }
            Insets insets=target.getInsets();
            dim.width+=insets.left+insets.right+hgap*2;
            dim.height+=insets.top+insets.bottom+vgap*2;
            return dim;
        }
    }

    public void layoutContainer(Container target){
        synchronized(target.getTreeLock()){
            Insets insets=target.getInsets();
            int maxwidth=target.width-(insets.left+insets.right+hgap*2);
            int nmembers=target.getComponentCount();
            int x=0, y=insets.top+vgap;
            int rowh=0, start=0;
            boolean ltr=target.getComponentOrientation().isLeftToRight();
            boolean useBaseline=getAlignOnBaseline();
            int[] ascent=null;
            int[] descent=null;
            if(useBaseline){
                ascent=new int[nmembers];
                descent=new int[nmembers];
            }
            for(int i=0;i<nmembers;i++){
                Component m=target.getComponent(i);
                if(m.isVisible()){
                    Dimension d=m.getPreferredSize();
                    m.setSize(d.width,d.height);
                    if(useBaseline){
                        int baseline=m.getBaseline(d.width,d.height);
                        if(baseline>=0){
                            ascent[i]=baseline;
                            descent[i]=d.height-baseline;
                        }else{
                            ascent[i]=-1;
                        }
                    }
                    if((x==0)||((x+d.width)<=maxwidth)){
                        if(x>0){
                            x+=hgap;
                        }
                        x+=d.width;
                        rowh=Math.max(rowh,d.height);
                    }else{
                        rowh=moveComponents(target,insets.left+hgap,y,
                                maxwidth-x,rowh,start,i,ltr,
                                useBaseline,ascent,descent);
                        x=d.width;
                        y+=vgap+rowh;
                        rowh=d.height;
                        start=i;
                    }
                }
            }
            moveComponents(target,insets.left+hgap,y,maxwidth-x,rowh,
                    start,nmembers,ltr,useBaseline,ascent,descent);
        }
    }

    private int moveComponents(Container target,int x,int y,int width,int height,
                               int rowStart,int rowEnd,boolean ltr,
                               boolean useBaseline,int[] ascent,
                               int[] descent){
        switch(newAlign){
            case LEFT:
                x+=ltr?0:width;
                break;
            case CENTER:
                x+=width/2;
                break;
            case RIGHT:
                x+=ltr?width:0;
                break;
            case LEADING:
                break;
            case TRAILING:
                x+=width;
                break;
        }
        int maxAscent=0;
        int nonbaselineHeight=0;
        int baselineOffset=0;
        if(useBaseline){
            int maxDescent=0;
            for(int i=rowStart;i<rowEnd;i++){
                Component m=target.getComponent(i);
                if(m.visible){
                    if(ascent[i]>=0){
                        maxAscent=Math.max(maxAscent,ascent[i]);
                        maxDescent=Math.max(maxDescent,descent[i]);
                    }else{
                        nonbaselineHeight=Math.max(m.getHeight(),
                                nonbaselineHeight);
                    }
                }
            }
            height=Math.max(maxAscent+maxDescent,nonbaselineHeight);
            baselineOffset=(height-maxAscent-maxDescent)/2;
        }
        for(int i=rowStart;i<rowEnd;i++){
            Component m=target.getComponent(i);
            if(m.isVisible()){
                int cy;
                if(useBaseline&&ascent[i]>=0){
                    cy=y+baselineOffset+maxAscent-ascent[i];
                }else{
                    cy=y+(height-m.height)/2;
                }
                if(ltr){
                    m.setLocation(x,cy);
                }else{
                    m.setLocation(target.width-x-m.width,cy);
                }
                x+=m.width+hgap;
            }
        }
        return height;
    }

    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException{
        stream.defaultReadObject();
        if(serialVersionOnStream<1){
            // "newAlign" field wasn't present, so use the old "align" field.
            setAlignment(this.align);
        }
        serialVersionOnStream=currentSerialVersion;
    }

    public String toString(){
        String str="";
        switch(align){
            case LEFT:
                str=",align=left";
                break;
            case CENTER:
                str=",align=center";
                break;
            case RIGHT:
                str=",align=right";
                break;
            case LEADING:
                str=",align=leading";
                break;
            case TRAILING:
                str=",align=trailing";
                break;
        }
        return getClass().getName()+"[hgap="+hgap+",vgap="+vgap+str+"]";
    }
}
