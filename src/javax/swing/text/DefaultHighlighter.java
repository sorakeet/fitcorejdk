/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import javax.swing.*;
import javax.swing.plaf.TextUI;
import java.awt.*;
import java.util.Vector;

public class DefaultHighlighter extends LayeredHighlighter{
    public static final LayerPainter DefaultPainter=new DefaultHighlightPainter(null);
    // ---- Highlighter methods ----------------------------------------------
    // ---- member variables --------------------------------------------
    private final static Highlight[] noHighlights=
            new Highlight[0];
    private Vector<HighlightInfo> highlights=new Vector<HighlightInfo>();
    private JTextComponent component;
    private boolean drawsLayeredHighlights;
    private SafeDamager safeDamager=new SafeDamager();

    public DefaultHighlighter(){
        drawsLayeredHighlights=true;
    }

    public void install(JTextComponent c){
        component=c;
        removeAllHighlights();
    }

    public void deinstall(JTextComponent c){
        component=null;
    }

    public void paint(Graphics g){
        // PENDING(prinz) - should cull ranges not visible
        int len=highlights.size();
        for(int i=0;i<len;i++){
            HighlightInfo info=highlights.elementAt(i);
            if(!(info instanceof LayeredHighlightInfo)){
                // Avoid allocing unless we need it.
                Rectangle a=component.getBounds();
                Insets insets=component.getInsets();
                a.x=insets.left;
                a.y=insets.top;
                a.width-=insets.left+insets.right;
                a.height-=insets.top+insets.bottom;
                for(;i<len;i++){
                    info=highlights.elementAt(i);
                    if(!(info instanceof LayeredHighlightInfo)){
                        HighlightPainter p=info.getPainter();
                        p.paint(g,info.getStartOffset(),info.getEndOffset(),
                                a,component);
                    }
                }
            }
        }
    }

    public Object addHighlight(int p0,int p1,HighlightPainter p) throws BadLocationException{
        if(p0<0){
            throw new BadLocationException("Invalid start offset",p0);
        }
        if(p1<p0){
            throw new BadLocationException("Invalid end offset",p1);
        }
        Document doc=component.getDocument();
        HighlightInfo i=(getDrawsLayeredHighlights()&&
                (p instanceof LayerPainter))?
                new LayeredHighlightInfo():new HighlightInfo();
        i.painter=p;
        i.p0=doc.createPosition(p0);
        i.p1=doc.createPosition(p1);
        highlights.addElement(i);
        safeDamageRange(p0,p1);
        return i;
    }

    public void removeHighlight(Object tag){
        if(tag instanceof LayeredHighlightInfo){
            LayeredHighlightInfo lhi=(LayeredHighlightInfo)tag;
            if(lhi.width>0&&lhi.height>0){
                component.repaint(lhi.x,lhi.y,lhi.width,lhi.height);
            }
        }else{
            HighlightInfo info=(HighlightInfo)tag;
            safeDamageRange(info.p0,info.p1);
        }
        highlights.removeElement(tag);
    }

    public void removeAllHighlights(){
        TextUI mapper=component.getUI();
        if(getDrawsLayeredHighlights()){
            int len=highlights.size();
            if(len!=0){
                int minX=0;
                int minY=0;
                int maxX=0;
                int maxY=0;
                int p0=-1;
                int p1=-1;
                for(int i=0;i<len;i++){
                    HighlightInfo hi=highlights.elementAt(i);
                    if(hi instanceof LayeredHighlightInfo){
                        LayeredHighlightInfo info=(LayeredHighlightInfo)hi;
                        minX=Math.min(minX,info.x);
                        minY=Math.min(minY,info.y);
                        maxX=Math.max(maxX,info.x+info.width);
                        maxY=Math.max(maxY,info.y+info.height);
                    }else{
                        if(p0==-1){
                            p0=hi.p0.getOffset();
                            p1=hi.p1.getOffset();
                        }else{
                            p0=Math.min(p0,hi.p0.getOffset());
                            p1=Math.max(p1,hi.p1.getOffset());
                        }
                    }
                }
                if(minX!=maxX&&minY!=maxY){
                    component.repaint(minX,minY,maxX-minX,maxY-minY);
                }
                if(p0!=-1){
                    try{
                        safeDamageRange(p0,p1);
                    }catch(BadLocationException e){
                    }
                }
                highlights.removeAllElements();
            }
        }else if(mapper!=null){
            int len=highlights.size();
            if(len!=0){
                int p0=Integer.MAX_VALUE;
                int p1=0;
                for(int i=0;i<len;i++){
                    HighlightInfo info=highlights.elementAt(i);
                    p0=Math.min(p0,info.p0.getOffset());
                    p1=Math.max(p1,info.p1.getOffset());
                }
                try{
                    safeDamageRange(p0,p1);
                }catch(BadLocationException e){
                }
                highlights.removeAllElements();
            }
        }
    }

    public void changeHighlight(Object tag,int p0,int p1) throws BadLocationException{
        if(p0<0){
            throw new BadLocationException("Invalid beginning of the range",p0);
        }
        if(p1<p0){
            throw new BadLocationException("Invalid end of the range",p1);
        }
        Document doc=component.getDocument();
        if(tag instanceof LayeredHighlightInfo){
            LayeredHighlightInfo lhi=(LayeredHighlightInfo)tag;
            if(lhi.width>0&&lhi.height>0){
                component.repaint(lhi.x,lhi.y,lhi.width,lhi.height);
            }
            // Mark the highlights region as invalid, it will reset itself
            // next time asked to paint.
            lhi.width=lhi.height=0;
            lhi.p0=doc.createPosition(p0);
            lhi.p1=doc.createPosition(p1);
            safeDamageRange(Math.min(p0,p1),Math.max(p0,p1));
        }else{
            HighlightInfo info=(HighlightInfo)tag;
            int oldP0=info.p0.getOffset();
            int oldP1=info.p1.getOffset();
            if(p0==oldP0){
                safeDamageRange(Math.min(oldP1,p1),
                        Math.max(oldP1,p1));
            }else if(p1==oldP1){
                safeDamageRange(Math.min(p0,oldP0),
                        Math.max(p0,oldP0));
            }else{
                safeDamageRange(oldP0,oldP1);
                safeDamageRange(p0,p1);
            }
            info.p0=doc.createPosition(p0);
            info.p1=doc.createPosition(p1);
        }
    }

    public Highlight[] getHighlights(){
        int size=highlights.size();
        if(size==0){
            return noHighlights;
        }
        Highlight[] h=new Highlight[size];
        highlights.copyInto(h);
        return h;
    }

    private void safeDamageRange(int a0,int a1) throws BadLocationException{
        Document doc=component.getDocument();
        safeDamageRange(doc.createPosition(a0),doc.createPosition(a1));
    }

    private void safeDamageRange(final Position p0,final Position p1){
        safeDamager.damageRange(p0,p1);
    }

    public boolean getDrawsLayeredHighlights(){
        return drawsLayeredHighlights;
    }

    public void setDrawsLayeredHighlights(boolean newValue){
        drawsLayeredHighlights=newValue;
    }

    public void paintLayeredHighlights(Graphics g,int p0,int p1,
                                       Shape viewBounds,
                                       JTextComponent editor,View view){
        for(int counter=highlights.size()-1;counter>=0;counter--){
            HighlightInfo tag=highlights.elementAt(counter);
            if(tag instanceof LayeredHighlightInfo){
                LayeredHighlightInfo lhi=(LayeredHighlightInfo)tag;
                int start=lhi.getStartOffset();
                int end=lhi.getEndOffset();
                if((p0<start&&p1>start)||
                        (p0>=start&&p0<end)){
                    lhi.paintLayeredHighlights(g,p0,p1,viewBounds,
                            editor,view);
                }
            }
        }
    }

    public static class DefaultHighlightPainter extends LayerPainter{
        private Color color;

        public DefaultHighlightPainter(Color c){
            color=c;
        }
        // --- HighlightPainter methods ---------------------------------------

        public void paint(Graphics g,int offs0,int offs1,Shape bounds,JTextComponent c){
            Rectangle alloc=bounds.getBounds();
            try{
                // --- determine locations ---
                TextUI mapper=c.getUI();
                Rectangle p0=mapper.modelToView(c,offs0);
                Rectangle p1=mapper.modelToView(c,offs1);
                // --- render ---
                Color color=getColor();
                if(color==null){
                    g.setColor(c.getSelectionColor());
                }else{
                    g.setColor(color);
                }
                if(p0.y==p1.y){
                    // same line, render a rectangle
                    Rectangle r=p0.union(p1);
                    g.fillRect(r.x,r.y,r.width,r.height);
                }else{
                    // different lines
                    int p0ToMarginWidth=alloc.x+alloc.width-p0.x;
                    g.fillRect(p0.x,p0.y,p0ToMarginWidth,p0.height);
                    if((p0.y+p0.height)!=p1.y){
                        g.fillRect(alloc.x,p0.y+p0.height,alloc.width,
                                p1.y-(p0.y+p0.height));
                    }
                    g.fillRect(alloc.x,p1.y,(p1.x-alloc.x),p1.height);
                }
            }catch(BadLocationException e){
                // can't render
            }
        }

        public Color getColor(){
            return color;
        }

        // --- LayerPainter methods ----------------------------
        public Shape paintLayer(Graphics g,int offs0,int offs1,
                                Shape bounds,JTextComponent c,View view){
            Color color=getColor();
            if(color==null){
                g.setColor(c.getSelectionColor());
            }else{
                g.setColor(color);
            }
            Rectangle r;
            if(offs0==view.getStartOffset()&&
                    offs1==view.getEndOffset()){
                // Contained in view, can just use bounds.
                if(bounds instanceof Rectangle){
                    r=(Rectangle)bounds;
                }else{
                    r=bounds.getBounds();
                }
            }else{
                // Should only render part of View.
                try{
                    // --- determine locations ---
                    Shape shape=view.modelToView(offs0,Position.Bias.Forward,
                            offs1,Position.Bias.Backward,
                            bounds);
                    r=(shape instanceof Rectangle)?
                            (Rectangle)shape:shape.getBounds();
                }catch(BadLocationException e){
                    // can't render
                    r=null;
                }
            }
            if(r!=null){
                // If we are asked to highlight, we should draw something even
                // if the model-to-view projection is of zero width (6340106).
                r.width=Math.max(r.width,1);
                g.fillRect(r.x,r.y,r.width,r.height);
            }
            return r;
        }
    }

    class HighlightInfo implements Highlight{
        Position p0;
        Position p1;
        HighlightPainter painter;

        public int getStartOffset(){
            return p0.getOffset();
        }

        public int getEndOffset(){
            return p1.getOffset();
        }

        public HighlightPainter getPainter(){
            return painter;
        }
    }

    class LayeredHighlightInfo extends HighlightInfo{
        int x;
        int y;
        int width;
        int height;

        void paintLayeredHighlights(Graphics g,int p0,int p1,
                                    Shape viewBounds,JTextComponent editor,
                                    View view){
            int start=getStartOffset();
            int end=getEndOffset();
            // Restrict the region to what we represent
            p0=Math.max(start,p0);
            p1=Math.min(end,p1);
            // Paint the appropriate region using the painter and union
            // the effected region with our bounds.
            union(((LayerPainter)painter).paintLayer
                    (g,p0,p1,viewBounds,editor,view));
        }

        void union(Shape bounds){
            if(bounds==null)
                return;
            Rectangle alloc;
            if(bounds instanceof Rectangle){
                alloc=(Rectangle)bounds;
            }else{
                alloc=bounds.getBounds();
            }
            if(width==0||height==0){
                x=alloc.x;
                y=alloc.y;
                width=alloc.width;
                height=alloc.height;
            }else{
                width=Math.max(x+width,alloc.x+alloc.width);
                height=Math.max(y+height,alloc.y+alloc.height);
                x=Math.min(x,alloc.x);
                width-=x;
                y=Math.min(y,alloc.y);
                height-=y;
            }
        }
    }

    class SafeDamager implements Runnable{
        private Vector<Position> p0=new Vector<Position>(10);
        private Vector<Position> p1=new Vector<Position>(10);
        private Document lastDoc=null;

        public synchronized void run(){
            if(component!=null){
                TextUI mapper=component.getUI();
                if(mapper!=null&&lastDoc==component.getDocument()){
                    // the Document should be the same to properly
                    // display highlights
                    int len=p0.size();
                    for(int i=0;i<len;i++){
                        mapper.damageRange(component,
                                p0.get(i).getOffset(),
                                p1.get(i).getOffset());
                    }
                }
            }
            p0.clear();
            p1.clear();
            // release reference
            lastDoc=null;
        }

        public synchronized void damageRange(Position pos0,Position pos1){
            if(component==null){
                p0.clear();
                lastDoc=null;
                return;
            }
            boolean addToQueue=p0.isEmpty();
            Document curDoc=component.getDocument();
            if(curDoc!=lastDoc){
                if(!p0.isEmpty()){
                    p0.clear();
                    p1.clear();
                }
                lastDoc=curDoc;
            }
            p0.add(pos0);
            p1.add(pos1);
            if(addToQueue){
                SwingUtilities.invokeLater(this);
            }
        }
    }
}
