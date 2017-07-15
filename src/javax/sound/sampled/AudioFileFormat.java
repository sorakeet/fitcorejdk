/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.sampled;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AudioFileFormat{
    // INSTANCE VARIABLES
    private Type type;
    private int byteLength;
    private AudioFormat format;
    private int frameLength;
    private HashMap<String,Object> properties;

    public AudioFileFormat(Type type,AudioFormat format,int frameLength){
        this(type,AudioSystem.NOT_SPECIFIED,format,frameLength);
    }

    protected AudioFileFormat(Type type,int byteLength,AudioFormat format,int frameLength){
        this.type=type;
        this.byteLength=byteLength;
        this.format=format;
        this.frameLength=frameLength;
        this.properties=null;
    }

    public AudioFileFormat(Type type,AudioFormat format,
                           int frameLength,Map<String,Object> properties){
        this(type,AudioSystem.NOT_SPECIFIED,format,frameLength);
        this.properties=new HashMap<String,Object>(properties);
    }

    public Type getType(){
        return type;
    }

    public int getByteLength(){
        return byteLength;
    }

    public AudioFormat getFormat(){
        return format;
    }

    public int getFrameLength(){
        return frameLength;
    }

    public Map<String,Object> properties(){
        Map<String,Object> ret;
        if(properties==null){
            ret=new HashMap<String,Object>(0);
        }else{
            ret=(Map<String,Object>)(properties.clone());
        }
        return (Map<String,Object>)Collections.unmodifiableMap(ret);
    }

    public Object getProperty(String key){
        if(properties==null){
            return null;
        }
        return properties.get(key);
    }

    public static class Type{
        // FILE FORMAT TYPE DEFINES
        public static final Type WAVE=new Type("WAVE","wav");
        public static final Type AU=new Type("AU","au");
        public static final Type AIFF=new Type("AIFF","aif");
        public static final Type AIFC=new Type("AIFF-C","aifc");
        public static final Type SND=new Type("SND","snd");
        // INSTANCE VARIABLES
        private final String name;
        private final String extension;
        // CONSTRUCTOR

        public Type(String name,String extension){
            this.name=name;
            this.extension=extension;
        }
        // METHODS

        public final int hashCode(){
            if(toString()==null){
                return 0;
            }
            return toString().hashCode();
        }

        public final boolean equals(Object obj){
            if(toString()==null){
                return (obj!=null)&&(obj.toString()==null);
            }
            if(obj instanceof Type){
                return toString().equals(obj.toString());
            }
            return false;
        }

        public String getExtension(){
            return extension;
        }        public final String toString(){
            return name;
        }


    } // class Type    public String toString(){
        StringBuffer buf=new StringBuffer();
        //$$fb2002-11-01: fix for 4672864: AudioFileFormat.toString() throws unexpected NullPointerException
        if(type!=null){
            buf.append(type.toString()+" (."+type.getExtension()+") file");
        }else{
            buf.append("unknown file format");
        }
        if(byteLength!=AudioSystem.NOT_SPECIFIED){
            buf.append(", byte length: "+byteLength);
        }
        buf.append(", data format: "+format);
        if(frameLength!=AudioSystem.NOT_SPECIFIED){
            buf.append(", frame length: "+frameLength);
        }
        return new String(buf);
    }


} // class AudioFileFormat
