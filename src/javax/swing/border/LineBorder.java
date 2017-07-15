/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.border;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.beans.ConstructorProperties;

public class LineBorder extends AbstractBorder{
    private static Border blackLine;
    private static Border grayLine;
    protected int thickness;
    protected Color lineColor;
    protected boolean roundedCorners;

    public LineBorder(Color color){
        this(color,1,false);
    }

    @ConstructorProperties({"lineColor","thickness","roundedCorners"})
    public LineBorder(Color color,int thickness,boolean roundedCorners){
        lineColor=color;
        this.thickness=thickness;
        this.roundedCorners=roundedCorners;
    }

    public LineBorder(Color color,int thickness){
        this(color,thickness,false);
    }

    public static Border createBlackLineBorder(){
        if(blackLine==null){
            blackLine=new LineBorder(Color.black,1);
        }
        return blackLine;
    }

    public static Border createGrayLineBorder(){
        if(grayLine==null){
            grayLine=new LineBorder(Color.gray,1);
        }
        return grayLine;
    }

    public void paintBorder(Component c,Graphics g,int x,int y,int width,int height){
        if((this.thickness>0)&&(g instanceof Graphics2D)){
            Graphics2D g2d=(Graphics2D)g;
            Color oldColor=g2d.getColor();
            g2d.setColor(this.lineColor);
            Shape outer;
            Shape inner;
            int offs=this.thickness;
            int size=offs+offs;
            if(this.roundedCorners){
                float arc=.2f*offs;
                outer=new RoundRectangle2D.Float(x,y,width,height,offs,offs);
                inner=new RoundRectangle2D.Float(x+offs,y+offs,width-size,height-size,arc,arc);
            }else{
                outer=new Rectangle2D.Float(x,y,width,height);
                inner=new Rectangle2D.Float(x+offs,y+offs,width-size,height-size);
            }
            Path2D path=new Path2D.Float(Path2D.WIND_EVEN_ODD);
            path.append(outer,false);
            path.append(inner,false);
            g2d.fill(path);
            g2d.setColor(oldColor);
        }
    }

    public Insets getBorderInsets(Component c,Insets insets){
        insets.set(thickness,thickness,thickness,thickness);
        return insets;
    }

    public boolean isBorderOpaque(){
        return !roundedCorners;
    }

    public Color getLineColor(){
        return lineColor;
    }

    public int getThickness(){
        return thickness;
    }

    public boolean getRoundedCorners(){
        return roundedCorners;
    }
}
