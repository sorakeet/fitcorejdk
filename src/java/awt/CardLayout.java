/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class CardLayout implements LayoutManager2,
        Serializable{
    private static final long serialVersionUID=-4328196481005934313L;
    private static final ObjectStreamField[] serialPersistentFields={
            new ObjectStreamField("tab",Hashtable.class),
            new ObjectStreamField("hgap",Integer.TYPE),
            new ObjectStreamField("vgap",Integer.TYPE),
            new ObjectStreamField("vector",Vector.class),
            new ObjectStreamField("currentCard",Integer.TYPE)
    };
    Vector<Card> vector=new Vector<>();
    int currentCard=0;
    int hgap;
    int vgap;

    public CardLayout(){
        this(0,0);
    }

    public CardLayout(int hgap,int vgap){
        this.hgap=hgap;
        this.vgap=vgap;
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

    public void addLayoutComponent(Component comp,Object constraints){
        synchronized(comp.getTreeLock()){
            if(constraints==null){
                constraints="";
            }
            if(constraints instanceof String){
                addLayoutComponent((String)constraints,comp);
            }else{
                throw new IllegalArgumentException("cannot add to layout: constraint must be a string");
            }
        }
    }

    @Deprecated
    public void addLayoutComponent(String name,Component comp){
        synchronized(comp.getTreeLock()){
            if(!vector.isEmpty()){
                comp.setVisible(false);
            }
            for(int i=0;i<vector.size();i++){
                if(((Card)vector.get(i)).name.equals(name)){
                    ((Card)vector.get(i)).comp=comp;
                    return;
                }
            }
            vector.add(new Card(name,comp));
        }
    }

    public void removeLayoutComponent(Component comp){
        synchronized(comp.getTreeLock()){
            for(int i=0;i<vector.size();i++){
                if(((Card)vector.get(i)).comp==comp){
                    // if we remove current component we should show next one
                    if(comp.isVisible()&&(comp.getParent()!=null)){
                        next(comp.getParent());
                    }
                    vector.remove(i);
                    // correct currentCard if this is necessary
                    if(currentCard>i){
                        currentCard--;
                    }
                    break;
                }
            }
        }
    }

    public Dimension preferredLayoutSize(Container parent){
        synchronized(parent.getTreeLock()){
            Insets insets=parent.getInsets();
            int ncomponents=parent.getComponentCount();
            int w=0;
            int h=0;
            for(int i=0;i<ncomponents;i++){
                Component comp=parent.getComponent(i);
                Dimension d=comp.getPreferredSize();
                if(d.width>w){
                    w=d.width;
                }
                if(d.height>h){
                    h=d.height;
                }
            }
            return new Dimension(insets.left+insets.right+w+hgap*2,
                    insets.top+insets.bottom+h+vgap*2);
        }
    }

    public Dimension minimumLayoutSize(Container parent){
        synchronized(parent.getTreeLock()){
            Insets insets=parent.getInsets();
            int ncomponents=parent.getComponentCount();
            int w=0;
            int h=0;
            for(int i=0;i<ncomponents;i++){
                Component comp=parent.getComponent(i);
                Dimension d=comp.getMinimumSize();
                if(d.width>w){
                    w=d.width;
                }
                if(d.height>h){
                    h=d.height;
                }
            }
            return new Dimension(insets.left+insets.right+w+hgap*2,
                    insets.top+insets.bottom+h+vgap*2);
        }
    }

    public void layoutContainer(Container parent){
        synchronized(parent.getTreeLock()){
            Insets insets=parent.getInsets();
            int ncomponents=parent.getComponentCount();
            Component comp=null;
            boolean currentFound=false;
            for(int i=0;i<ncomponents;i++){
                comp=parent.getComponent(i);
                comp.setBounds(hgap+insets.left,vgap+insets.top,
                        parent.width-(hgap*2+insets.left+insets.right),
                        parent.height-(vgap*2+insets.top+insets.bottom));
                if(comp.isVisible()){
                    currentFound=true;
                }
            }
            if(!currentFound&&ncomponents>0){
                parent.getComponent(0).setVisible(true);
            }
        }
    }

    public void next(Container parent){
        synchronized(parent.getTreeLock()){
            checkLayout(parent);
            int ncomponents=parent.getComponentCount();
            for(int i=0;i<ncomponents;i++){
                Component comp=parent.getComponent(i);
                if(comp.isVisible()){
                    comp.setVisible(false);
                    currentCard=(i+1)%ncomponents;
                    comp=parent.getComponent(currentCard);
                    comp.setVisible(true);
                    parent.validate();
                    return;
                }
            }
            showDefaultComponent(parent);
        }
    }

    void checkLayout(Container parent){
        if(parent.getLayout()!=this){
            throw new IllegalArgumentException("wrong parent for CardLayout");
        }
    }

    void showDefaultComponent(Container parent){
        if(parent.getComponentCount()>0){
            currentCard=0;
            parent.getComponent(0).setVisible(true);
            parent.validate();
        }
    }

    public Dimension maximumLayoutSize(Container target){
        return new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE);
    }

    public float getLayoutAlignmentX(Container parent){
        return 0.5f;
    }

    public float getLayoutAlignmentY(Container parent){
        return 0.5f;
    }

    public void invalidateLayout(Container target){
    }

    public void first(Container parent){
        synchronized(parent.getTreeLock()){
            checkLayout(parent);
            int ncomponents=parent.getComponentCount();
            for(int i=0;i<ncomponents;i++){
                Component comp=parent.getComponent(i);
                if(comp.isVisible()){
                    comp.setVisible(false);
                    break;
                }
            }
            if(ncomponents>0){
                currentCard=0;
                parent.getComponent(0).setVisible(true);
                parent.validate();
            }
        }
    }

    public void previous(Container parent){
        synchronized(parent.getTreeLock()){
            checkLayout(parent);
            int ncomponents=parent.getComponentCount();
            for(int i=0;i<ncomponents;i++){
                Component comp=parent.getComponent(i);
                if(comp.isVisible()){
                    comp.setVisible(false);
                    currentCard=((i>0)?i-1:ncomponents-1);
                    comp=parent.getComponent(currentCard);
                    comp.setVisible(true);
                    parent.validate();
                    return;
                }
            }
            showDefaultComponent(parent);
        }
    }

    public void last(Container parent){
        synchronized(parent.getTreeLock()){
            checkLayout(parent);
            int ncomponents=parent.getComponentCount();
            for(int i=0;i<ncomponents;i++){
                Component comp=parent.getComponent(i);
                if(comp.isVisible()){
                    comp.setVisible(false);
                    break;
                }
            }
            if(ncomponents>0){
                currentCard=ncomponents-1;
                parent.getComponent(currentCard).setVisible(true);
                parent.validate();
            }
        }
    }

    public void show(Container parent,String name){
        synchronized(parent.getTreeLock()){
            checkLayout(parent);
            Component next=null;
            int ncomponents=vector.size();
            for(int i=0;i<ncomponents;i++){
                Card card=(Card)vector.get(i);
                if(card.name.equals(name)){
                    next=card.comp;
                    currentCard=i;
                    break;
                }
            }
            if((next!=null)&&!next.isVisible()){
                ncomponents=parent.getComponentCount();
                for(int i=0;i<ncomponents;i++){
                    Component comp=parent.getComponent(i);
                    if(comp.isVisible()){
                        comp.setVisible(false);
                        break;
                    }
                }
                next.setVisible(true);
                parent.validate();
            }
        }
    }

    public String toString(){
        return getClass().getName()+"[hgap="+hgap+",vgap="+vgap+"]";
    }

    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException{
        ObjectInputStream.GetField f=s.readFields();
        hgap=f.get("hgap",0);
        vgap=f.get("vgap",0);
        if(f.defaulted("vector")){
            //  pre-1.4 stream
            Hashtable<String,Component> tab=(Hashtable)f.get("tab",null);
            vector=new Vector<>();
            if(tab!=null&&!tab.isEmpty()){
                for(Enumeration<String> e=tab.keys();e.hasMoreElements();){
                    String key=(String)e.nextElement();
                    Component comp=(Component)tab.get(key);
                    vector.add(new Card(key,comp));
                    if(comp.isVisible()){
                        currentCard=vector.size()-1;
                    }
                }
            }
        }else{
            vector=(Vector)f.get("vector",null);
            currentCard=f.get("currentCard",0);
        }
    }

    private void writeObject(ObjectOutputStream s)
            throws IOException{
        Hashtable<String,Component> tab=new Hashtable<>();
        int ncomponents=vector.size();
        for(int i=0;i<ncomponents;i++){
            Card card=(Card)vector.get(i);
            tab.put(card.name,card.comp);
        }
        ObjectOutputStream.PutField f=s.putFields();
        f.put("hgap",hgap);
        f.put("vgap",vgap);
        f.put("vector",vector);
        f.put("currentCard",currentCard);
        f.put("tab",tab);
        s.writeFields();
    }

    class Card implements Serializable{
        static final long serialVersionUID=6640330810709497518L;
        public String name;
        public Component comp;

        public Card(String cardName,Component cardComponent){
            name=cardName;
            comp=cardComponent;
        }
    }
}
