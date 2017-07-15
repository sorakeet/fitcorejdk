/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import java.awt.RenderingHints.Key;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

public abstract class Graphics2D extends Graphics{
    protected Graphics2D(){
    }

    public abstract void draw(Shape s);

    public abstract boolean drawImage(Image img,
                                      AffineTransform xform,
                                      ImageObserver obs);

    public abstract void drawImage(BufferedImage img,
                                   BufferedImageOp op,
                                   int x,
                                   int y);

    public abstract void drawRenderedImage(RenderedImage img,
                                           AffineTransform xform);

    public abstract void drawRenderableImage(RenderableImage img,
                                             AffineTransform xform);

    public abstract void drawString(String str,float x,float y);

    public abstract void drawString(AttributedCharacterIterator iterator,
                                    float x,float y);

    public abstract void drawGlyphVector(GlyphVector g,float x,float y);

    public abstract void fill(Shape s);

    public abstract boolean hit(Rectangle rect,
                                Shape s,
                                boolean onStroke);

    public abstract GraphicsConfiguration getDeviceConfiguration();

    public abstract void setRenderingHint(Key hintKey,Object hintValue);

    public abstract Object getRenderingHint(Key hintKey);

    public abstract void addRenderingHints(Map<?,?> hints);

    public abstract RenderingHints getRenderingHints();

    public abstract void setRenderingHints(Map<?,?> hints);

    public abstract void translate(int x,int y);

    public void draw3DRect(int x,int y,int width,int height,
                           boolean raised){
        Paint p=getPaint();
        Color c=getColor();
        Color brighter=c.brighter();
        Color darker=c.darker();
        setColor(raised?brighter:darker);
        //drawLine(x, y, x, y + height);
        fillRect(x,y,1,height+1);
        //drawLine(x + 1, y, x + width - 1, y);
        fillRect(x+1,y,width-1,1);
        setColor(raised?darker:brighter);
        //drawLine(x + 1, y + height, x + width, y + height);
        fillRect(x+1,y+height,width,1);
        //drawLine(x + width, y, x + width, y + height - 1);
        fillRect(x+width,y,1,height);
        setPaint(p);
    }

    public void fill3DRect(int x,int y,int width,int height,
                           boolean raised){
        Paint p=getPaint();
        Color c=getColor();
        Color brighter=c.brighter();
        Color darker=c.darker();
        if(!raised){
            setColor(darker);
        }else if(p!=c){
            setColor(c);
        }
        fillRect(x+1,y+1,width-2,height-2);
        setColor(raised?brighter:darker);
        //drawLine(x, y, x, y + height - 1);
        fillRect(x,y,1,height);
        //drawLine(x + 1, y, x + width - 2, y);
        fillRect(x+1,y,width-2,1);
        setColor(raised?darker:brighter);
        //drawLine(x + 1, y + height - 1, x + width - 1, y + height - 1);
        fillRect(x+1,y+height-1,width-1,1);
        //drawLine(x + width - 1, y, x + width - 1, y + height - 2);
        fillRect(x+width-1,y,1,height-1);
        setPaint(p);
    }

    public abstract void drawString(String str,int x,int y);

    public abstract void drawString(AttributedCharacterIterator iterator,
                                    int x,int y);

    public abstract Paint getPaint();

    public abstract void setPaint(Paint paint);

    public abstract void translate(double tx,double ty);

    public abstract void rotate(double theta);

    public abstract void rotate(double theta,double x,double y);

    public abstract void scale(double sx,double sy);

    public abstract void shear(double shx,double shy);

    public abstract void transform(AffineTransform Tx);

    public abstract AffineTransform getTransform();

    public abstract void setTransform(AffineTransform Tx);

    public abstract Composite getComposite();

    public abstract void setComposite(Composite comp);

    public abstract Color getBackground();

    public abstract void setBackground(Color color);

    public abstract Stroke getStroke();

    public abstract void setStroke(Stroke s);

    public abstract void clip(Shape s);

    public abstract FontRenderContext getFontRenderContext();
}
