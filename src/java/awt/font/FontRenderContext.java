/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * @author Charlton Innovations, Inc.
 */
/**
 * @author Charlton Innovations, Inc.
 */
package java.awt.font;

import java.awt.geom.AffineTransform;

import static java.awt.RenderingHints.*;

public class FontRenderContext{
    private transient AffineTransform tx;
    private transient Object aaHintValue;
    private transient Object fmHintValue;
    private transient boolean defaulting;

    protected FontRenderContext(){
        aaHintValue=VALUE_TEXT_ANTIALIAS_DEFAULT;
        fmHintValue=VALUE_FRACTIONALMETRICS_DEFAULT;
        defaulting=true;
    }

    public FontRenderContext(AffineTransform tx,
                             boolean isAntiAliased,
                             boolean usesFractionalMetrics){
        if(tx!=null&&!tx.isIdentity()){
            this.tx=new AffineTransform(tx);
        }
        if(isAntiAliased){
            aaHintValue=VALUE_TEXT_ANTIALIAS_ON;
        }else{
            aaHintValue=VALUE_TEXT_ANTIALIAS_OFF;
        }
        if(usesFractionalMetrics){
            fmHintValue=VALUE_FRACTIONALMETRICS_ON;
        }else{
            fmHintValue=VALUE_FRACTIONALMETRICS_OFF;
        }
    }

    public FontRenderContext(AffineTransform tx,Object aaHint,Object fmHint){
        if(tx!=null&&!tx.isIdentity()){
            this.tx=new AffineTransform(tx);
        }
        try{
            if(KEY_TEXT_ANTIALIASING.isCompatibleValue(aaHint)){
                aaHintValue=aaHint;
            }else{
                throw new IllegalArgumentException("AA hint:"+aaHint);
            }
        }catch(Exception e){
            throw new IllegalArgumentException("AA hint:"+aaHint);
        }
        try{
            if(KEY_FRACTIONALMETRICS.isCompatibleValue(fmHint)){
                fmHintValue=fmHint;
            }else{
                throw new IllegalArgumentException("FM hint:"+fmHint);
            }
        }catch(Exception e){
            throw new IllegalArgumentException("FM hint:"+fmHint);
        }
    }

    public boolean isTransformed(){
        if(!defaulting){
            return tx!=null;
        }else{
            return !getTransform().isIdentity();
        }
    }

    public AffineTransform getTransform(){
        return (tx==null)?new AffineTransform():new AffineTransform(tx);
    }

    public int getTransformType(){
        if(!defaulting){
            if(tx==null){
                return AffineTransform.TYPE_IDENTITY;
            }else{
                return tx.getType();
            }
        }else{
            return getTransform().getType();
        }
    }

    public int hashCode(){
        int hash=tx==null?0:tx.hashCode();
        /** SunHints value objects have identity hashcode, so we can rely on
         * this to ensure that two equal FRC's have the same hashcode.
         */
        if(defaulting){
            hash+=getAntiAliasingHint().hashCode();
            hash+=getFractionalMetricsHint().hashCode();
        }else{
            hash+=aaHintValue.hashCode();
            hash+=fmHintValue.hashCode();
        }
        return hash;
    }

    public boolean equals(Object obj){
        try{
            return equals((FontRenderContext)obj);
        }catch(ClassCastException e){
            return false;
        }
    }

    public boolean equals(FontRenderContext rhs){
        if(this==rhs){
            return true;
        }
        if(rhs==null){
            return false;
        }
        /** if neither instance is a subclass, reference values directly. */
        if(!rhs.defaulting&&!defaulting){
            if(rhs.aaHintValue==aaHintValue&&
                    rhs.fmHintValue==fmHintValue){
                return tx==null?rhs.tx==null:tx.equals(rhs.tx);
            }
            return false;
        }else{
            return
                    rhs.getAntiAliasingHint()==getAntiAliasingHint()&&
                            rhs.getFractionalMetricsHint()==getFractionalMetricsHint()&&
                            rhs.getTransform().equals(getTransform());
        }
    }

    public Object getAntiAliasingHint(){
        if(defaulting){
            if(isAntiAliased()){
                return VALUE_TEXT_ANTIALIAS_ON;
            }else{
                return VALUE_TEXT_ANTIALIAS_OFF;
            }
        }
        return aaHintValue;
    }

    public boolean isAntiAliased(){
        return !(aaHintValue==VALUE_TEXT_ANTIALIAS_OFF||
                aaHintValue==VALUE_TEXT_ANTIALIAS_DEFAULT);
    }

    public Object getFractionalMetricsHint(){
        if(defaulting){
            if(usesFractionalMetrics()){
                return VALUE_FRACTIONALMETRICS_ON;
            }else{
                return VALUE_FRACTIONALMETRICS_OFF;
            }
        }
        return fmHintValue;
    }

    public boolean usesFractionalMetrics(){
        return !(fmHintValue==VALUE_FRACTIONALMETRICS_OFF||
                fmHintValue==VALUE_FRACTIONALMETRICS_DEFAULT);
    }
}
