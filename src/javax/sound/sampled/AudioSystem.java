/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.sampled;

import com.sun.media.sound.JDK13Services;

import javax.sound.sampled.spi.AudioFileReader;
import javax.sound.sampled.spi.AudioFileWriter;
import javax.sound.sampled.spi.FormatConversionProvider;
import javax.sound.sampled.spi.MixerProvider;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;

public class AudioSystem{
    public static final int NOT_SPECIFIED=-1;

    private AudioSystem(){
    }

    public static Mixer.Info[] getMixerInfo(){
        List infos=getMixerInfoList();
        Mixer.Info[] allInfos=(Mixer.Info[])infos.toArray(new Mixer.Info[infos.size()]);
        return allInfos;
    }

    //$$fb 2002-11-26: fix for 4757930: DOC: AudioSystem.getTarget/SourceLineInfo() is ambiguous
    public static Line.Info[] getSourceLineInfo(Line.Info info){
        Vector vector=new Vector();
        Line.Info[] currentInfoArray;
        Mixer mixer;
        Line.Info fullInfo=null;
        Mixer.Info[] infoArray=getMixerInfo();
        for(int i=0;i<infoArray.length;i++){
            mixer=getMixer(infoArray[i]);
            currentInfoArray=mixer.getSourceLineInfo(info);
            for(int j=0;j<currentInfoArray.length;j++){
                vector.addElement(currentInfoArray[j]);
            }
        }
        Line.Info[] returnedArray=new Line.Info[vector.size()];
        for(int i=0;i<returnedArray.length;i++){
            returnedArray[i]=(Line.Info)vector.get(i);
        }
        return returnedArray;
    }

    public static Line.Info[] getTargetLineInfo(Line.Info info){
        Vector vector=new Vector();
        Line.Info[] currentInfoArray;
        Mixer mixer;
        Line.Info fullInfo=null;
        Mixer.Info[] infoArray=getMixerInfo();
        for(int i=0;i<infoArray.length;i++){
            mixer=getMixer(infoArray[i]);
            currentInfoArray=mixer.getTargetLineInfo(info);
            for(int j=0;j<currentInfoArray.length;j++){
                vector.addElement(currentInfoArray[j]);
            }
        }
        Line.Info[] returnedArray=new Line.Info[vector.size()];
        for(int i=0;i<returnedArray.length;i++){
            returnedArray[i]=(Line.Info)vector.get(i);
        }
        return returnedArray;
    }

    public static boolean isLineSupported(Line.Info info){
        Mixer mixer;
        Mixer.Info[] infoArray=getMixerInfo();
        for(int i=0;i<infoArray.length;i++){
            if(infoArray[i]!=null){
                mixer=getMixer(infoArray[i]);
                if(mixer.isLineSupported(info)){
                    return true;
                }
            }
        }
        return false;
    }

    public static Clip getClip() throws LineUnavailableException{
        AudioFormat format=new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                AudioSystem.NOT_SPECIFIED,
                16,2,4,
                AudioSystem.NOT_SPECIFIED,true);
        DataLine.Info info=new DataLine.Info(Clip.class,format);
        return (Clip)AudioSystem.getLine(info);
    }

    public static Line getLine(Line.Info info) throws LineUnavailableException{
        LineUnavailableException lue=null;
        List providers=getMixerProviders();
        // 1: try from default mixer for this line class
        try{
            Mixer mixer=getDefaultMixer(providers,info);
            if(mixer!=null&&mixer.isLineSupported(info)){
                return mixer.getLine(info);
            }
        }catch(LineUnavailableException e){
            lue=e;
        }catch(IllegalArgumentException iae){
            // must not happen... but better to catch it here,
            // if plug-ins are badly written
        }
        // 2: if that doesn't work, try to find any mixing mixer
        for(int i=0;i<providers.size();i++){
            MixerProvider provider=(MixerProvider)providers.get(i);
            Mixer.Info[] infos=provider.getMixerInfo();
            for(int j=0;j<infos.length;j++){
                try{
                    Mixer mixer=provider.getMixer(infos[j]);
                    // see if this is an appropriate mixer which can mix
                    if(isAppropriateMixer(mixer,info,true)){
                        return mixer.getLine(info);
                    }
                }catch(LineUnavailableException e){
                    lue=e;
                }catch(IllegalArgumentException iae){
                    // must not happen... but better to catch it here,
                    // if plug-ins are badly written
                }
            }
        }
        // 3: if that didn't work, try to find any non-mixing mixer
        for(int i=0;i<providers.size();i++){
            MixerProvider provider=(MixerProvider)providers.get(i);
            Mixer.Info[] infos=provider.getMixerInfo();
            for(int j=0;j<infos.length;j++){
                try{
                    Mixer mixer=provider.getMixer(infos[j]);
                    // see if this is an appropriate mixer which can mix
                    if(isAppropriateMixer(mixer,info,false)){
                        return mixer.getLine(info);
                    }
                }catch(LineUnavailableException e){
                    lue=e;
                }catch(IllegalArgumentException iae){
                    // must not happen... but better to catch it here,
                    // if plug-ins are badly written
                }
            }
        }
        // if this line was supported but was not available, throw the last
        // LineUnavailableException we got (??).
        if(lue!=null){
            throw lue;
        }
        // otherwise, the requested line was not supported, so throw
        // an Illegal argument exception
        throw new IllegalArgumentException("No line matching "+
                info.toString()+" is supported.");
    }

    private static List getMixerProviders(){
        return getProviders(MixerProvider.class);
    }

    private static List getProviders(Class providerClass){
        return JDK13Services.getProviders(providerClass);
    }

    private static Mixer getDefaultMixer(List providers,Line.Info info){
        Class lineClass=info.getLineClass();
        String providerClassName=JDK13Services.getDefaultProviderClassName(lineClass);
        String instanceName=JDK13Services.getDefaultInstanceName(lineClass);
        Mixer mixer;
        if(providerClassName!=null){
            MixerProvider defaultProvider=getNamedProvider(providerClassName,providers);
            if(defaultProvider!=null){
                if(instanceName!=null){
                    mixer=getNamedMixer(instanceName,defaultProvider,info);
                    if(mixer!=null){
                        return mixer;
                    }
                }else{
                    mixer=getFirstMixer(defaultProvider,info,
                            false /** mixing not required*/);
                    if(mixer!=null){
                        return mixer;
                    }
                }
            }
        }
        /** Provider class not specified or
         provider class cannot be found, or
         provider class and instance specified and instance cannot be found or is not appropriate */
        if(instanceName!=null){
            mixer=getNamedMixer(instanceName,providers,info);
            if(mixer!=null){
                return mixer;
            }
        }
        /** No default are specified, or if something is specified, everything
         failed. */
        return null;
    }

    private static MixerProvider getNamedProvider(String providerClassName,
                                                  List providers){
        for(int i=0;i<providers.size();i++){
            MixerProvider provider=(MixerProvider)providers.get(i);
            if(provider.getClass().getName().equals(providerClassName)){
                return provider;
            }
        }
        return null;
    }

    private static Mixer getNamedMixer(String mixerName,
                                       MixerProvider provider,
                                       Line.Info info){
        Mixer.Info[] infos=provider.getMixerInfo();
        for(int i=0;i<infos.length;i++){
            if(infos[i].getName().equals(mixerName)){
                Mixer mixer=provider.getMixer(infos[i]);
                if(isAppropriateMixer(mixer,info,false)){
                    return mixer;
                }
            }
        }
        return null;
    }

    private static boolean isAppropriateMixer(Mixer mixer,
                                              Line.Info lineInfo,
                                              boolean isMixingRequired){
        if(!mixer.isLineSupported(lineInfo)){
            return false;
        }
        Class lineClass=lineInfo.getLineClass();
        if(isMixingRequired
                &&(SourceDataLine.class.isAssignableFrom(lineClass)||
                Clip.class.isAssignableFrom(lineClass))){
            int maxLines=mixer.getMaxLines(lineInfo);
            return ((maxLines==NOT_SPECIFIED)||(maxLines>1));
        }
        return true;
    }

    private static Mixer getNamedMixer(String mixerName,
                                       List providers,
                                       Line.Info info){
        for(int i=0;i<providers.size();i++){
            MixerProvider provider=(MixerProvider)providers.get(i);
            Mixer mixer=getNamedMixer(mixerName,provider,info);
            if(mixer!=null){
                return mixer;
            }
        }
        return null;
    }

    private static Mixer getFirstMixer(MixerProvider provider,
                                       Line.Info info,
                                       boolean isMixingRequired){
        Mixer.Info[] infos=provider.getMixerInfo();
        for(int j=0;j<infos.length;j++){
            Mixer mixer=provider.getMixer(infos[j]);
            if(isAppropriateMixer(mixer,info,isMixingRequired)){
                return mixer;
            }
        }
        return null;
    }

    public static Clip getClip(Mixer.Info mixerInfo) throws LineUnavailableException{
        AudioFormat format=new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                AudioSystem.NOT_SPECIFIED,
                16,2,4,
                AudioSystem.NOT_SPECIFIED,true);
        DataLine.Info info=new DataLine.Info(Clip.class,format);
        Mixer mixer=AudioSystem.getMixer(mixerInfo);
        return (Clip)mixer.getLine(info);
    }

    public static Mixer getMixer(Mixer.Info info){
        Mixer mixer=null;
        List providers=getMixerProviders();
        for(int i=0;i<providers.size();i++){
            try{
                return ((MixerProvider)providers.get(i)).getMixer(info);
            }catch(IllegalArgumentException e){
            }catch(NullPointerException e){
                // $$jb 08.20.99:  If the strings in the info object aren't
                // set, then Netscape (using jdk1.1.5) tends to throw
                // NPE's when doing some string manipulation.  This is
                // probably not the best fix, but is solves the problem
                // of the NPE in Netscape using local classes
                // $$jb 11.01.99: Replacing this patch.
            }
        }
        //$$fb if looking for default mixer, and not found yet, add a round of looking
        if(info==null){
            for(int i=0;i<providers.size();i++){
                try{
                    MixerProvider provider=(MixerProvider)providers.get(i);
                    Mixer.Info[] infos=provider.getMixerInfo();
                    // start from 0 to last device (do not reverse this order)
                    for(int ii=0;ii<infos.length;ii++){
                        try{
                            return provider.getMixer(infos[ii]);
                        }catch(IllegalArgumentException e){
                            // this is not a good default device :)
                        }
                    }
                }catch(IllegalArgumentException e){
                }catch(NullPointerException e){
                }
            }
        }
        throw new IllegalArgumentException("Mixer not supported: "
                +(info!=null?info.toString():"null"));
    }

    public static SourceDataLine getSourceDataLine(AudioFormat format)
            throws LineUnavailableException{
        DataLine.Info info=new DataLine.Info(SourceDataLine.class,format);
        return (SourceDataLine)AudioSystem.getLine(info);
    }

    public static SourceDataLine getSourceDataLine(AudioFormat format,
                                                   Mixer.Info mixerinfo)
            throws LineUnavailableException{
        DataLine.Info info=new DataLine.Info(SourceDataLine.class,format);
        Mixer mixer=AudioSystem.getMixer(mixerinfo);
        return (SourceDataLine)mixer.getLine(info);
    }

    public static TargetDataLine getTargetDataLine(AudioFormat format)
            throws LineUnavailableException{
        DataLine.Info info=new DataLine.Info(TargetDataLine.class,format);
        return (TargetDataLine)AudioSystem.getLine(info);
    }

    public static TargetDataLine getTargetDataLine(AudioFormat format,
                                                   Mixer.Info mixerinfo)
            throws LineUnavailableException{
        DataLine.Info info=new DataLine.Info(TargetDataLine.class,format);
        Mixer mixer=AudioSystem.getMixer(mixerinfo);
        return (TargetDataLine)mixer.getLine(info);
    }

    // $$fb 2002-04-12: fix for 4662082: behavior of AudioSystem.getTargetEncodings() methods doesn't match the spec
    public static AudioFormat.Encoding[] getTargetEncodings(AudioFormat.Encoding sourceEncoding){
        List codecs=getFormatConversionProviders();
        Vector encodings=new Vector();
        AudioFormat.Encoding encs[]=null;
        // gather from all the codecs
        for(int i=0;i<codecs.size();i++){
            FormatConversionProvider codec=(FormatConversionProvider)codecs.get(i);
            if(codec.isSourceEncodingSupported(sourceEncoding)){
                encs=codec.getTargetEncodings();
                for(int j=0;j<encs.length;j++){
                    encodings.addElement(encs[j]);
                }
            }
        }
        AudioFormat.Encoding encs2[]=(AudioFormat.Encoding[])encodings.toArray(new AudioFormat.Encoding[0]);
        return encs2;
    }

    private static List getFormatConversionProviders(){
        return getProviders(FormatConversionProvider.class);
    }

    // $$fb 2002-04-12: fix for 4662082: behavior of AudioSystem.getTargetEncodings() methods doesn't match the spec
    public static AudioFormat.Encoding[] getTargetEncodings(AudioFormat sourceFormat){
        List codecs=getFormatConversionProviders();
        Vector encodings=new Vector();
        int size=0;
        int index=0;
        AudioFormat.Encoding encs[]=null;
        // gather from all the codecs
        for(int i=0;i<codecs.size();i++){
            encs=((FormatConversionProvider)codecs.get(i)).getTargetEncodings(sourceFormat);
            size+=encs.length;
            encodings.addElement(encs);
        }
        // now build a new array
        AudioFormat.Encoding encs2[]=new AudioFormat.Encoding[size];
        for(int i=0;i<encodings.size();i++){
            encs=(AudioFormat.Encoding[])(encodings.get(i));
            for(int j=0;j<encs.length;j++){
                encs2[index++]=encs[j];
            }
        }
        return encs2;
    }

    public static boolean isConversionSupported(AudioFormat.Encoding targetEncoding,AudioFormat sourceFormat){
        List codecs=getFormatConversionProviders();
        for(int i=0;i<codecs.size();i++){
            FormatConversionProvider codec=(FormatConversionProvider)codecs.get(i);
            if(codec.isConversionSupported(targetEncoding,sourceFormat)){
                return true;
            }
        }
        return false;
    }

    public static AudioInputStream getAudioInputStream(AudioFormat.Encoding targetEncoding,
                                                       AudioInputStream sourceStream){
        List codecs=getFormatConversionProviders();
        for(int i=0;i<codecs.size();i++){
            FormatConversionProvider codec=(FormatConversionProvider)codecs.get(i);
            if(codec.isConversionSupported(targetEncoding,sourceStream.getFormat())){
                return codec.getAudioInputStream(targetEncoding,sourceStream);
            }
        }
        // we ran out of options, throw an exception
        throw new IllegalArgumentException("Unsupported conversion: "+targetEncoding+" from "+sourceStream.getFormat());
    }

    public static AudioFormat[] getTargetFormats(AudioFormat.Encoding targetEncoding,AudioFormat sourceFormat){
        List codecs=getFormatConversionProviders();
        Vector formats=new Vector();
        int size=0;
        int index=0;
        AudioFormat fmts[]=null;
        // gather from all the codecs
        for(int i=0;i<codecs.size();i++){
            FormatConversionProvider codec=(FormatConversionProvider)codecs.get(i);
            fmts=codec.getTargetFormats(targetEncoding,sourceFormat);
            size+=fmts.length;
            formats.addElement(fmts);
        }
        // now build a new array
        AudioFormat fmts2[]=new AudioFormat[size];
        for(int i=0;i<formats.size();i++){
            fmts=(AudioFormat[])(formats.get(i));
            for(int j=0;j<fmts.length;j++){
                fmts2[index++]=fmts[j];
            }
        }
        return fmts2;
    }

    public static boolean isConversionSupported(AudioFormat targetFormat,AudioFormat sourceFormat){
        List codecs=getFormatConversionProviders();
        for(int i=0;i<codecs.size();i++){
            FormatConversionProvider codec=(FormatConversionProvider)codecs.get(i);
            if(codec.isConversionSupported(targetFormat,sourceFormat)){
                return true;
            }
        }
        return false;
    }

    public static AudioInputStream getAudioInputStream(AudioFormat targetFormat,
                                                       AudioInputStream sourceStream){
        if(sourceStream.getFormat().matches(targetFormat)){
            return sourceStream;
        }
        List codecs=getFormatConversionProviders();
        for(int i=0;i<codecs.size();i++){
            FormatConversionProvider codec=(FormatConversionProvider)codecs.get(i);
            if(codec.isConversionSupported(targetFormat,sourceStream.getFormat())){
                return codec.getAudioInputStream(targetFormat,sourceStream);
            }
        }
        // we ran out of options...
        throw new IllegalArgumentException("Unsupported conversion: "+targetFormat+" from "+sourceStream.getFormat());
    }

    public static AudioFileFormat getAudioFileFormat(InputStream stream)
            throws UnsupportedAudioFileException, IOException{
        List providers=getAudioFileReaders();
        AudioFileFormat format=null;
        for(int i=0;i<providers.size();i++){
            AudioFileReader reader=(AudioFileReader)providers.get(i);
            try{
                format=reader.getAudioFileFormat(stream); // throws IOException
                break;
            }catch(UnsupportedAudioFileException e){
                continue;
            }
        }
        if(format==null){
            throw new UnsupportedAudioFileException("file is not a supported file type");
        }else{
            return format;
        }
    }

    private static List getAudioFileReaders(){
        return getProviders(AudioFileReader.class);
    }

    public static AudioFileFormat getAudioFileFormat(URL url)
            throws UnsupportedAudioFileException, IOException{
        List providers=getAudioFileReaders();
        AudioFileFormat format=null;
        for(int i=0;i<providers.size();i++){
            AudioFileReader reader=(AudioFileReader)providers.get(i);
            try{
                format=reader.getAudioFileFormat(url); // throws IOException
                break;
            }catch(UnsupportedAudioFileException e){
                continue;
            }
        }
        if(format==null){
            throw new UnsupportedAudioFileException("file is not a supported file type");
        }else{
            return format;
        }
    }
    // METHODS FOR INTERNAL IMPLEMENTATION USE

    public static AudioFileFormat getAudioFileFormat(File file)
            throws UnsupportedAudioFileException, IOException{
        List providers=getAudioFileReaders();
        AudioFileFormat format=null;
        for(int i=0;i<providers.size();i++){
            AudioFileReader reader=(AudioFileReader)providers.get(i);
            try{
                format=reader.getAudioFileFormat(file); // throws IOException
                break;
            }catch(UnsupportedAudioFileException e){
                continue;
            }
        }
        if(format==null){
            throw new UnsupportedAudioFileException("file is not a supported file type");
        }else{
            return format;
        }
    }

    public static AudioInputStream getAudioInputStream(InputStream stream)
            throws UnsupportedAudioFileException, IOException{
        List providers=getAudioFileReaders();
        AudioInputStream audioStream=null;
        for(int i=0;i<providers.size();i++){
            AudioFileReader reader=(AudioFileReader)providers.get(i);
            try{
                audioStream=reader.getAudioInputStream(stream); // throws IOException
                break;
            }catch(UnsupportedAudioFileException e){
                continue;
            }
        }
        if(audioStream==null){
            throw new UnsupportedAudioFileException("could not get audio input stream from input stream");
        }else{
            return audioStream;
        }
    }

    public static AudioInputStream getAudioInputStream(URL url)
            throws UnsupportedAudioFileException, IOException{
        List providers=getAudioFileReaders();
        AudioInputStream audioStream=null;
        for(int i=0;i<providers.size();i++){
            AudioFileReader reader=(AudioFileReader)providers.get(i);
            try{
                audioStream=reader.getAudioInputStream(url); // throws IOException
                break;
            }catch(UnsupportedAudioFileException e){
                continue;
            }
        }
        if(audioStream==null){
            throw new UnsupportedAudioFileException("could not get audio input stream from input URL");
        }else{
            return audioStream;
        }
    }

    public static AudioInputStream getAudioInputStream(File file)
            throws UnsupportedAudioFileException, IOException{
        List providers=getAudioFileReaders();
        AudioInputStream audioStream=null;
        for(int i=0;i<providers.size();i++){
            AudioFileReader reader=(AudioFileReader)providers.get(i);
            try{
                audioStream=reader.getAudioInputStream(file); // throws IOException
                break;
            }catch(UnsupportedAudioFileException e){
                continue;
            }
        }
        if(audioStream==null){
            throw new UnsupportedAudioFileException("could not get audio input stream from input file");
        }else{
            return audioStream;
        }
    }

    public static AudioFileFormat.Type[] getAudioFileTypes(){
        List providers=getAudioFileWriters();
        Set returnTypesSet=new HashSet();
        for(int i=0;i<providers.size();i++){
            AudioFileWriter writer=(AudioFileWriter)providers.get(i);
            AudioFileFormat.Type[] fileTypes=writer.getAudioFileTypes();
            for(int j=0;j<fileTypes.length;j++){
                returnTypesSet.add(fileTypes[j]);
            }
        }
        AudioFileFormat.Type returnTypes[]=(AudioFileFormat.Type[])
                returnTypesSet.toArray(new AudioFileFormat.Type[0]);
        return returnTypes;
    }

    private static List getAudioFileWriters(){
        return getProviders(AudioFileWriter.class);
    }

    public static boolean isFileTypeSupported(AudioFileFormat.Type fileType){
        List providers=getAudioFileWriters();
        for(int i=0;i<providers.size();i++){
            AudioFileWriter writer=(AudioFileWriter)providers.get(i);
            if(writer.isFileTypeSupported(fileType)){
                return true;
            }
        }
        return false;
    }

    public static AudioFileFormat.Type[] getAudioFileTypes(AudioInputStream stream){
        List providers=getAudioFileWriters();
        Set returnTypesSet=new HashSet();
        for(int i=0;i<providers.size();i++){
            AudioFileWriter writer=(AudioFileWriter)providers.get(i);
            AudioFileFormat.Type[] fileTypes=writer.getAudioFileTypes(stream);
            for(int j=0;j<fileTypes.length;j++){
                returnTypesSet.add(fileTypes[j]);
            }
        }
        AudioFileFormat.Type returnTypes[]=(AudioFileFormat.Type[])
                returnTypesSet.toArray(new AudioFileFormat.Type[0]);
        return returnTypes;
    }

    public static boolean isFileTypeSupported(AudioFileFormat.Type fileType,
                                              AudioInputStream stream){
        List providers=getAudioFileWriters();
        for(int i=0;i<providers.size();i++){
            AudioFileWriter writer=(AudioFileWriter)providers.get(i);
            if(writer.isFileTypeSupported(fileType,stream)){
                return true;
            }
        }
        return false;
    }

    public static int write(AudioInputStream stream,AudioFileFormat.Type fileType,
                            OutputStream out) throws IOException{
        List providers=getAudioFileWriters();
        int bytesWritten=0;
        boolean flag=false;
        for(int i=0;i<providers.size();i++){
            AudioFileWriter writer=(AudioFileWriter)providers.get(i);
            try{
                bytesWritten=writer.write(stream,fileType,out); // throws IOException
                flag=true;
                break;
            }catch(IllegalArgumentException e){
                // thrown if this provider cannot write the sequence, try the next
                continue;
            }
        }
        if(!flag){
            throw new IllegalArgumentException("could not write audio file: file type not supported: "+fileType);
        }else{
            return bytesWritten;
        }
    }

    public static int write(AudioInputStream stream,AudioFileFormat.Type fileType,
                            File out) throws IOException{
        List providers=getAudioFileWriters();
        int bytesWritten=0;
        boolean flag=false;
        for(int i=0;i<providers.size();i++){
            AudioFileWriter writer=(AudioFileWriter)providers.get(i);
            try{
                bytesWritten=writer.write(stream,fileType,out); // throws IOException
                flag=true;
                break;
            }catch(IllegalArgumentException e){
                // thrown if this provider cannot write the sequence, try the next
                continue;
            }
        }
        if(!flag){
            throw new IllegalArgumentException("could not write audio file: file type not supported: "+fileType);
        }else{
            return bytesWritten;
        }
    }

    private static List getMixerInfoList(){
        List providers=getMixerProviders();
        return getMixerInfoList(providers);
    }

    private static List getMixerInfoList(List providers){
        List infos=new ArrayList();
        Mixer.Info[] someInfos; // per-mixer
        Mixer.Info[] allInfos;  // for all mixers
        for(int i=0;i<providers.size();i++){
            someInfos=(Mixer.Info[])
                    ((MixerProvider)providers.get(i)).getMixerInfo();
            for(int j=0;j<someInfos.length;j++){
                infos.add(someInfos[j]);
            }
        }
        return infos;
    }
}
