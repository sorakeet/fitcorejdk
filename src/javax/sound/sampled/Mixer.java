/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.sampled;

public interface Mixer extends Line{
    public Info getMixerInfo();

    public Line.Info[] getSourceLineInfo();

    public Line.Info[] getTargetLineInfo();

    public Line.Info[] getSourceLineInfo(Line.Info info);

    public Line.Info[] getTargetLineInfo(Line.Info info);

    public boolean isLineSupported(Line.Info info);

    public Line getLine(Line.Info info) throws LineUnavailableException;

    //$$fb 2002-04-12: fix for 4667258: behavior of Mixer.getMaxLines(Line.Info) method doesn't match the spec
    public int getMaxLines(Line.Info info);

    public Line[] getSourceLines();

    public Line[] getTargetLines();

    public void synchronize(Line[] lines,boolean maintainSync);

    public void unsynchronize(Line[] lines);

    public boolean isSynchronizationSupported(Line[] lines,boolean maintainSync);

    public static class Info{
        private final String name;
        private final String vendor;
        private final String description;
        private final String version;

        protected Info(String name,String vendor,String description,String version){
            this.name=name;
            this.vendor=vendor;
            this.description=description;
            this.version=version;
        }

        public final int hashCode(){
            return super.hashCode();
        }

        public final boolean equals(Object obj){
            return super.equals(obj);
        }

        public final String toString(){
            return (name+", version "+version);
        }

        public final String getName(){
            return name;
        }

        public final String getVendor(){
            return vendor;
        }

        public final String getDescription(){
            return description;
        }

        public final String getVersion(){
            return version;
        }
    } // class Info
}
