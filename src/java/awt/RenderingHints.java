/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import sun.awt.SunHints;

import java.lang.ref.WeakReference;
import java.util.*;

public class RenderingHints
        implements Map<Object,Object>, Cloneable{
    public static final Key KEY_ANTIALIASING=
            SunHints.KEY_ANTIALIASING;
    public static final Object VALUE_ANTIALIAS_ON=
            SunHints.VALUE_ANTIALIAS_ON;
    public static final Object VALUE_ANTIALIAS_OFF=
            SunHints.VALUE_ANTIALIAS_OFF;
    public static final Object VALUE_ANTIALIAS_DEFAULT=
            SunHints.VALUE_ANTIALIAS_DEFAULT;
    public static final Key KEY_RENDERING=
            SunHints.KEY_RENDERING;
    public static final Object VALUE_RENDER_SPEED=
            SunHints.VALUE_RENDER_SPEED;
    public static final Object VALUE_RENDER_QUALITY=
            SunHints.VALUE_RENDER_QUALITY;
    public static final Object VALUE_RENDER_DEFAULT=
            SunHints.VALUE_RENDER_DEFAULT;
    public static final Key KEY_DITHERING=
            SunHints.KEY_DITHERING;
    public static final Object VALUE_DITHER_DISABLE=
            SunHints.VALUE_DITHER_DISABLE;
    public static final Object VALUE_DITHER_ENABLE=
            SunHints.VALUE_DITHER_ENABLE;
    public static final Object VALUE_DITHER_DEFAULT=
            SunHints.VALUE_DITHER_DEFAULT;
    public static final Key KEY_TEXT_ANTIALIASING=
            SunHints.KEY_TEXT_ANTIALIASING;
    public static final Object VALUE_TEXT_ANTIALIAS_ON=
            SunHints.VALUE_TEXT_ANTIALIAS_ON;
    public static final Object VALUE_TEXT_ANTIALIAS_OFF=
            SunHints.VALUE_TEXT_ANTIALIAS_OFF;
    public static final Object VALUE_TEXT_ANTIALIAS_DEFAULT=
            SunHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
    public static final Object VALUE_TEXT_ANTIALIAS_GASP=
            SunHints.VALUE_TEXT_ANTIALIAS_GASP;
    public static final Object VALUE_TEXT_ANTIALIAS_LCD_HRGB=
            SunHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB;
    public static final Object VALUE_TEXT_ANTIALIAS_LCD_HBGR=
            SunHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR;
    public static final Object VALUE_TEXT_ANTIALIAS_LCD_VRGB=
            SunHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB;
    public static final Object VALUE_TEXT_ANTIALIAS_LCD_VBGR=
            SunHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR;
    public static final Key KEY_TEXT_LCD_CONTRAST=
            SunHints.KEY_TEXT_ANTIALIAS_LCD_CONTRAST;
    public static final Key KEY_FRACTIONALMETRICS=
            SunHints.KEY_FRACTIONALMETRICS;
    public static final Object VALUE_FRACTIONALMETRICS_OFF=
            SunHints.VALUE_FRACTIONALMETRICS_OFF;
    public static final Object VALUE_FRACTIONALMETRICS_ON=
            SunHints.VALUE_FRACTIONALMETRICS_ON;
    public static final Object VALUE_FRACTIONALMETRICS_DEFAULT=
            SunHints.VALUE_FRACTIONALMETRICS_DEFAULT;
    public static final Key KEY_INTERPOLATION=
            SunHints.KEY_INTERPOLATION;
    public static final Object VALUE_INTERPOLATION_NEAREST_NEIGHBOR=
            SunHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
    public static final Object VALUE_INTERPOLATION_BILINEAR=
            SunHints.VALUE_INTERPOLATION_BILINEAR;
    public static final Object VALUE_INTERPOLATION_BICUBIC=
            SunHints.VALUE_INTERPOLATION_BICUBIC;
    public static final Key KEY_ALPHA_INTERPOLATION=
            SunHints.KEY_ALPHA_INTERPOLATION;
    public static final Object VALUE_ALPHA_INTERPOLATION_SPEED=
            SunHints.VALUE_ALPHA_INTERPOLATION_SPEED;
    public static final Object VALUE_ALPHA_INTERPOLATION_QUALITY=
            SunHints.VALUE_ALPHA_INTERPOLATION_QUALITY;
    public static final Object VALUE_ALPHA_INTERPOLATION_DEFAULT=
            SunHints.VALUE_ALPHA_INTERPOLATION_DEFAULT;
    public static final Key KEY_COLOR_RENDERING=
            SunHints.KEY_COLOR_RENDERING;
    public static final Object VALUE_COLOR_RENDER_SPEED=
            SunHints.VALUE_COLOR_RENDER_SPEED;
    public static final Object VALUE_COLOR_RENDER_QUALITY=
            SunHints.VALUE_COLOR_RENDER_QUALITY;
    public static final Object VALUE_COLOR_RENDER_DEFAULT=
            SunHints.VALUE_COLOR_RENDER_DEFAULT;
    public static final Key KEY_STROKE_CONTROL=
            SunHints.KEY_STROKE_CONTROL;
    public static final Object VALUE_STROKE_DEFAULT=
            SunHints.VALUE_STROKE_DEFAULT;
    public static final Object VALUE_STROKE_NORMALIZE=
            SunHints.VALUE_STROKE_NORMALIZE;
    public static final Object VALUE_STROKE_PURE=
            SunHints.VALUE_STROKE_PURE;
    HashMap<Object,Object> hintmap=new HashMap<>(7);

    public RenderingHints(Map<Key,?> init){
        if(init!=null){
            hintmap.putAll(init);
        }
    }

    public RenderingHints(Key key,Object value){
        hintmap.put(key,value);
    }

    public int size(){
        return hintmap.size();
    }

    public boolean isEmpty(){
        return hintmap.isEmpty();
    }

    public boolean containsKey(Object key){
        return hintmap.containsKey((Key)key);
    }

    public boolean containsValue(Object value){
        return hintmap.containsValue(value);
    }

    public Object get(Object key){
        return hintmap.get((Key)key);
    }

    public Object put(Object key,Object value){
        if(!((Key)key).isCompatibleValue(value)){
            throw new IllegalArgumentException(value+
                    " incompatible with "+
                    key);
        }
        return hintmap.put((Key)key,value);
    }

    public Object remove(Object key){
        return hintmap.remove((Key)key);
    }

    public void putAll(Map<?,?> m){
        // ## javac bug?
        //if (m instanceof RenderingHints) {
        if(RenderingHints.class.isInstance(m)){
            //hintmap.putAll(((RenderingHints) m).hintmap);
            for(Entry<?,?> entry : m.entrySet())
                hintmap.put(entry.getKey(),entry.getValue());
        }else{
            // Funnel each key/value pair through our protected put method
            for(Entry<?,?> entry : m.entrySet())
                put(entry.getKey(),entry.getValue());
        }
    }

    public void clear(){
        hintmap.clear();
    }

    public Set<Object> keySet(){
        return hintmap.keySet();
    }

    public Collection<Object> values(){
        return hintmap.values();
    }

    public Set<Entry<Object,Object>> entrySet(){
        return Collections.unmodifiableMap(hintmap).entrySet();
    }

    public void add(RenderingHints hints){
        hintmap.putAll(hints.hintmap);
    }

    public abstract static class Key{
        private static HashMap<Object,Object> identitymap=new HashMap<>(17);
        private int privatekey;

        protected Key(int privatekey){
            this.privatekey=privatekey;
            recordIdentity(this);
        }

        private synchronized static void recordIdentity(Key k){
            Object identity=k.getIdentity();
            Object otherref=identitymap.get(identity);
            if(otherref!=null){
                Key otherkey=(Key)((WeakReference)otherref).get();
                if(otherkey!=null&&otherkey.getClass()==k.getClass()){
                    throw new IllegalArgumentException(identity+
                            " already registered");
                }
                // Note that this system can fail in a mostly harmless
                // way.  If we end up generating the same identity
                // String for 2 different classes (a very rare case)
                // then we correctly avoid throwing the exception above,
                // but we are about to drop through to a statement that
                // will replace the entry for the old Key subclass with
                // an entry for the new Key subclass.  At that time the
                // old subclass will be vulnerable to someone generating
                // a duplicate Key instance for it.  We could bail out
                // of the method here and let the old identity keep its
                // record in the map, but we are more likely to see a
                // duplicate key go by for the new class than the old
                // one since the new one is probably still in the
                // initialization stage.  In either case, the probability
                // of loading 2 classes in the same VM with the same name
                // and identityHashCode should be nearly impossible.
            }
            // Note: Use a weak reference to avoid holding on to extra
            // objects and classes after they should be unloaded.
            identitymap.put(identity,new WeakReference<Key>(k));
        }

        private String getIdentity(){
            // Note that the identity string is dependent on 3 variables:
            //     - the name of the subclass of Key
            //     - the identityHashCode of the subclass of Key
            //     - the integer key of the Key
            // It is theoretically possible for 2 distinct keys to collide
            // along all 3 of those attributes in the context of multiple
            // class loaders, but that occurrence will be extremely rare and
            // we account for that possibility below in the recordIdentity
            // method by slightly relaxing our uniqueness guarantees if we
            // end up in that situation.
            return getClass().getName()+"@"+
                    Integer.toHexString(System.identityHashCode(getClass()))+":"+
                    Integer.toHexString(privatekey);
        }

        public abstract boolean isCompatibleValue(Object val);

        protected final int intKey(){
            return privatekey;
        }

        public final int hashCode(){
            return super.hashCode();
        }

        public final boolean equals(Object o){
            return this==o;
        }
    }

    public boolean equals(Object o){
        if(o instanceof RenderingHints){
            return hintmap.equals(((RenderingHints)o).hintmap);
        }else if(o instanceof Map){
            return hintmap.equals(o);
        }
        return false;
    }

    public int hashCode(){
        return hintmap.hashCode();
    }

    @SuppressWarnings("unchecked")
    public Object clone(){
        RenderingHints rh;
        try{
            rh=(RenderingHints)super.clone();
            if(hintmap!=null){
                rh.hintmap=(HashMap<Object,Object>)hintmap.clone();
            }
        }catch(CloneNotSupportedException e){
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
        return rh;
    }

    public String toString(){
        if(hintmap==null){
            return getClass().getName()+"@"+
                    Integer.toHexString(hashCode())+
                    " (0 hints)";
        }
        return hintmap.toString();
    }
}
