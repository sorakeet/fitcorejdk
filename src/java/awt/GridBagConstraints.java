/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

public class GridBagConstraints implements Cloneable, java.io.Serializable{
    public static final int RELATIVE=-1;
    public static final int REMAINDER=0;
    public static final int NONE=0;
    public static final int BOTH=1;
    public static final int HORIZONTAL=2;
    public static final int VERTICAL=3;
    public static final int CENTER=10;
    public static final int NORTH=11;
    public static final int NORTHEAST=12;
    public static final int EAST=13;
    public static final int SOUTHEAST=14;
    public static final int SOUTH=15;
    public static final int SOUTHWEST=16;
    public static final int WEST=17;
    public static final int NORTHWEST=18;
    public static final int PAGE_START=19;
    public static final int PAGE_END=20;
    public static final int LINE_START=21;
    public static final int LINE_END=22;
    public static final int FIRST_LINE_START=23;
    public static final int FIRST_LINE_END=24;
    public static final int LAST_LINE_START=25;
    public static final int LAST_LINE_END=26;
    public static final int BASELINE=0x100;
    public static final int BASELINE_LEADING=0x200;
    public static final int BASELINE_TRAILING=0x300;
    public static final int ABOVE_BASELINE=0x400;
    public static final int ABOVE_BASELINE_LEADING=0x500;
    public static final int ABOVE_BASELINE_TRAILING=0x600;
    public static final int BELOW_BASELINE=0x700;
    public static final int BELOW_BASELINE_LEADING=0x800;
    public static final int BELOW_BASELINE_TRAILING=0x900;
    private static final long serialVersionUID=-1000070633030801713L;
    public int gridx;
    public int gridy;
    public int gridwidth;
    public int gridheight;
    public double weightx;
    public double weighty;
    public int anchor;
    public int fill;
    public Insets insets;
    public int ipadx;
    public int ipady;
    int tempX;
    int tempY;
    int tempWidth;
    int tempHeight;
    int minWidth;
    int minHeight;
    // The following fields are only used if the anchor is
    // one of BASELINE, BASELINE_LEADING or BASELINE_TRAILING.
    // ascent and descent include the insets and ipady values.
    transient int ascent;
    transient int descent;
    transient Component.BaselineResizeBehavior baselineResizeBehavior;
    // The folllowing two fields are used if the baseline type is
    // CENTER_OFFSET.
    // centerPadding is either 0 or 1 and indicates if
    // the height needs to be padded by one when calculating where the
    // baseline lands
    transient int centerPadding;
    // Where the baseline lands relative to the center of the component.
    transient int centerOffset;

    public GridBagConstraints(){
        gridx=RELATIVE;
        gridy=RELATIVE;
        gridwidth=1;
        gridheight=1;
        weightx=0;
        weighty=0;
        anchor=CENTER;
        fill=NONE;
        insets=new Insets(0,0,0,0);
        ipadx=0;
        ipady=0;
    }

    public GridBagConstraints(int gridx,int gridy,
                              int gridwidth,int gridheight,
                              double weightx,double weighty,
                              int anchor,int fill,
                              Insets insets,int ipadx,int ipady){
        this.gridx=gridx;
        this.gridy=gridy;
        this.gridwidth=gridwidth;
        this.gridheight=gridheight;
        this.fill=fill;
        this.ipadx=ipadx;
        this.ipady=ipady;
        this.insets=insets;
        this.anchor=anchor;
        this.weightx=weightx;
        this.weighty=weighty;
    }

    public Object clone(){
        try{
            GridBagConstraints c=(GridBagConstraints)super.clone();
            c.insets=(Insets)insets.clone();
            return c;
        }catch(CloneNotSupportedException e){
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
    }

    boolean isVerticallyResizable(){
        return (fill==BOTH||fill==VERTICAL);
    }
}
