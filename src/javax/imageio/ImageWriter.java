/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio;

import javax.imageio.event.IIOWriteProgressListener;
import javax.imageio.event.IIOWriteWarningListener;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public abstract class ImageWriter implements ImageTranscoder{
    protected ImageWriterSpi originatingProvider=null;
    protected Object output=null;
    protected Locale[] availableLocales=null;
    protected Locale locale=null;
    protected List<IIOWriteWarningListener> warningListeners=null;
    protected List<Locale> warningLocales=null;
    protected List<IIOWriteProgressListener> progressListeners=null;
    private boolean abortFlag=false;

    protected ImageWriter(ImageWriterSpi originatingProvider){
        this.originatingProvider=originatingProvider;
    }

    public ImageWriteParam getDefaultWriteParam(){
        return new ImageWriteParam(getLocale());
    }

    public Locale getLocale(){
        return locale;
    }

    public void setLocale(Locale locale){
        if(locale!=null){
            Locale[] locales=getAvailableLocales();
            boolean found=false;
            if(locales!=null){
                for(int i=0;i<locales.length;i++){
                    if(locale.equals(locales[i])){
                        found=true;
                        break;
                    }
                }
            }
            if(!found){
                throw new IllegalArgumentException("Invalid locale!");
            }
        }
        this.locale=locale;
    }
    // Localization

    public Locale[] getAvailableLocales(){
        return (availableLocales==null)?
                null:(Locale[])availableLocales.clone();
    }

    public abstract IIOMetadata
    getDefaultStreamMetadata(ImageWriteParam param);

    public abstract IIOMetadata
    getDefaultImageMetadata(ImageTypeSpecifier imageType,
                            ImageWriteParam param);
    // Write params

    // comment inherited
    public abstract IIOMetadata convertStreamMetadata(IIOMetadata inData,
                                                      ImageWriteParam param);
    // Metadata

    // comment inherited
    public abstract IIOMetadata
    convertImageMetadata(IIOMetadata inData,
                         ImageTypeSpecifier imageType,
                         ImageWriteParam param);

    public int getNumThumbnailsSupported(ImageTypeSpecifier imageType,
                                         ImageWriteParam param,
                                         IIOMetadata streamMetadata,
                                         IIOMetadata imageMetadata){
        return 0;
    }

    public Dimension[] getPreferredThumbnailSizes(ImageTypeSpecifier imageType,
                                                  ImageWriteParam param,
                                                  IIOMetadata streamMetadata,
                                                  IIOMetadata imageMetadata){
        return null;
    }

    public boolean canWriteRasters(){
        return false;
    }
    // Thumbnails

    public void write(IIOImage image) throws IOException{
        write(null,image,null);
    }

    public abstract void write(IIOMetadata streamMetadata,
                               IIOImage image,
                               ImageWriteParam param) throws IOException;

    public void write(RenderedImage image) throws IOException{
        write(null,new IIOImage(image,null,null),null);
    }

    public boolean canWriteSequence(){
        return false;
    }

    public void prepareWriteSequence(IIOMetadata streamMetadata)
            throws IOException{
        unsupported();
    }

    // Check that the output has been set, then throw an
    // UnsupportedOperationException.
    private void unsupported(){
        if(getOutput()==null){
            throw new IllegalStateException("getOutput() == null!");
        }
        throw new UnsupportedOperationException("Unsupported write variant!");
    }

    public Object getOutput(){
        return output;
    }
    // Sequence writes

    public void setOutput(Object output){
        if(output!=null){
            ImageWriterSpi provider=getOriginatingProvider();
            if(provider!=null){
                Class[] classes=provider.getOutputTypes();
                boolean found=false;
                for(int i=0;i<classes.length;i++){
                    if(classes[i].isInstance(output)){
                        found=true;
                        break;
                    }
                }
                if(!found){
                    throw new IllegalArgumentException("Illegal output type!");
                }
            }
        }
        this.output=output;
    }

    public ImageWriterSpi getOriginatingProvider(){
        return originatingProvider;
    }

    public void writeToSequence(IIOImage image,ImageWriteParam param)
            throws IOException{
        unsupported();
    }

    public void endWriteSequence() throws IOException{
        unsupported();
    }
    // Metadata replacement

    public boolean canReplaceStreamMetadata() throws IOException{
        if(getOutput()==null){
            throw new IllegalStateException("getOutput() == null!");
        }
        return false;
    }

    public void replaceStreamMetadata(IIOMetadata streamMetadata)
            throws IOException{
        unsupported();
    }

    public boolean canReplaceImageMetadata(int imageIndex)
            throws IOException{
        if(getOutput()==null){
            throw new IllegalStateException("getOutput() == null!");
        }
        return false;
    }

    public void replaceImageMetadata(int imageIndex,
                                     IIOMetadata imageMetadata)
            throws IOException{
        unsupported();
    }
    // Image insertion

    public boolean canInsertImage(int imageIndex) throws IOException{
        if(getOutput()==null){
            throw new IllegalStateException("getOutput() == null!");
        }
        return false;
    }

    public void writeInsert(int imageIndex,
                            IIOImage image,
                            ImageWriteParam param) throws IOException{
        unsupported();
    }
    // Image removal

    public boolean canRemoveImage(int imageIndex) throws IOException{
        if(getOutput()==null){
            throw new IllegalStateException("getOutput() == null!");
        }
        return false;
    }

    public void removeImage(int imageIndex) throws IOException{
        unsupported();
    }
    // Empty images

    public boolean canWriteEmpty() throws IOException{
        if(getOutput()==null){
            throw new IllegalStateException("getOutput() == null!");
        }
        return false;
    }

    public void prepareWriteEmpty(IIOMetadata streamMetadata,
                                  ImageTypeSpecifier imageType,
                                  int width,int height,
                                  IIOMetadata imageMetadata,
                                  List<? extends BufferedImage> thumbnails,
                                  ImageWriteParam param) throws IOException{
        unsupported();
    }

    public void endWriteEmpty() throws IOException{
        if(getOutput()==null){
            throw new IllegalStateException("getOutput() == null!");
        }
        throw new IllegalStateException("No call to prepareWriteEmpty!");
    }

    public boolean canInsertEmpty(int imageIndex) throws IOException{
        if(getOutput()==null){
            throw new IllegalStateException("getOutput() == null!");
        }
        return false;
    }

    public void prepareInsertEmpty(int imageIndex,
                                   ImageTypeSpecifier imageType,
                                   int width,int height,
                                   IIOMetadata imageMetadata,
                                   List<? extends BufferedImage> thumbnails,
                                   ImageWriteParam param) throws IOException{
        unsupported();
    }

    public void endInsertEmpty() throws IOException{
        unsupported();
    }
    // Pixel replacement

    public boolean canReplacePixels(int imageIndex) throws IOException{
        if(getOutput()==null){
            throw new IllegalStateException("getOutput() == null!");
        }
        return false;
    }

    public void prepareReplacePixels(int imageIndex,
                                     Rectangle region) throws IOException{
        unsupported();
    }

    public void replacePixels(RenderedImage image,ImageWriteParam param)
            throws IOException{
        unsupported();
    }

    public void replacePixels(Raster raster,ImageWriteParam param)
            throws IOException{
        unsupported();
    }

    public void endReplacePixels() throws IOException{
        unsupported();
    }
    // Abort

    public synchronized void abort(){
        this.abortFlag=true;
    }

    protected synchronized boolean abortRequested(){
        return this.abortFlag;
    }

    public void addIIOWriteWarningListener(IIOWriteWarningListener listener){
        if(listener==null){
            return;
        }
        warningListeners=ImageReader.addToList(warningListeners,listener);
        warningLocales=ImageReader.addToList(warningLocales,getLocale());
    }
    // Listeners

    public void removeIIOWriteWarningListener(IIOWriteWarningListener listener){
        if(listener==null||warningListeners==null){
            return;
        }
        int index=warningListeners.indexOf(listener);
        if(index!=-1){
            warningListeners.remove(index);
            warningLocales.remove(index);
            if(warningListeners.size()==0){
                warningListeners=null;
                warningLocales=null;
            }
        }
    }

    public void
    addIIOWriteProgressListener(IIOWriteProgressListener listener){
        if(listener==null){
            return;
        }
        progressListeners=ImageReader.addToList(progressListeners,listener);
    }

    public void
    removeIIOWriteProgressListener(IIOWriteProgressListener listener){
        if(listener==null||progressListeners==null){
            return;
        }
        progressListeners=
                ImageReader.removeFromList(progressListeners,listener);
    }

    protected void processImageStarted(int imageIndex){
        if(progressListeners==null){
            return;
        }
        int numListeners=progressListeners.size();
        for(int i=0;i<numListeners;i++){
            IIOWriteProgressListener listener=
                    (IIOWriteProgressListener)progressListeners.get(i);
            listener.imageStarted(this,imageIndex);
        }
    }

    protected void processImageProgress(float percentageDone){
        if(progressListeners==null){
            return;
        }
        int numListeners=progressListeners.size();
        for(int i=0;i<numListeners;i++){
            IIOWriteProgressListener listener=
                    (IIOWriteProgressListener)progressListeners.get(i);
            listener.imageProgress(this,percentageDone);
        }
    }

    protected void processImageComplete(){
        if(progressListeners==null){
            return;
        }
        int numListeners=progressListeners.size();
        for(int i=0;i<numListeners;i++){
            IIOWriteProgressListener listener=
                    (IIOWriteProgressListener)progressListeners.get(i);
            listener.imageComplete(this);
        }
    }

    protected void processThumbnailStarted(int imageIndex,
                                           int thumbnailIndex){
        if(progressListeners==null){
            return;
        }
        int numListeners=progressListeners.size();
        for(int i=0;i<numListeners;i++){
            IIOWriteProgressListener listener=
                    (IIOWriteProgressListener)progressListeners.get(i);
            listener.thumbnailStarted(this,imageIndex,thumbnailIndex);
        }
    }

    protected void processThumbnailProgress(float percentageDone){
        if(progressListeners==null){
            return;
        }
        int numListeners=progressListeners.size();
        for(int i=0;i<numListeners;i++){
            IIOWriteProgressListener listener=
                    (IIOWriteProgressListener)progressListeners.get(i);
            listener.thumbnailProgress(this,percentageDone);
        }
    }

    protected void processThumbnailComplete(){
        if(progressListeners==null){
            return;
        }
        int numListeners=progressListeners.size();
        for(int i=0;i<numListeners;i++){
            IIOWriteProgressListener listener=
                    (IIOWriteProgressListener)progressListeners.get(i);
            listener.thumbnailComplete(this);
        }
    }

    protected void processWriteAborted(){
        if(progressListeners==null){
            return;
        }
        int numListeners=progressListeners.size();
        for(int i=0;i<numListeners;i++){
            IIOWriteProgressListener listener=
                    (IIOWriteProgressListener)progressListeners.get(i);
            listener.writeAborted(this);
        }
    }

    protected void processWarningOccurred(int imageIndex,
                                          String warning){
        if(warningListeners==null){
            return;
        }
        if(warning==null){
            throw new IllegalArgumentException("warning == null!");
        }
        int numListeners=warningListeners.size();
        for(int i=0;i<numListeners;i++){
            IIOWriteWarningListener listener=
                    (IIOWriteWarningListener)warningListeners.get(i);
            listener.warningOccurred(this,imageIndex,warning);
        }
    }

    protected void processWarningOccurred(int imageIndex,
                                          String baseName,
                                          String keyword){
        if(warningListeners==null){
            return;
        }
        if(baseName==null){
            throw new IllegalArgumentException("baseName == null!");
        }
        if(keyword==null){
            throw new IllegalArgumentException("keyword == null!");
        }
        int numListeners=warningListeners.size();
        for(int i=0;i<numListeners;i++){
            IIOWriteWarningListener listener=
                    (IIOWriteWarningListener)warningListeners.get(i);
            Locale locale=(Locale)warningLocales.get(i);
            if(locale==null){
                locale=Locale.getDefault();
            }
            /**
             * If an applet supplies an implementation of ImageWriter and
             * resource bundles, then the resource bundle will need to be
             * accessed via the applet class loader. So first try the context
             * class loader to locate the resource bundle.
             * If that throws MissingResourceException, then try the
             * system class loader.
             */
            ClassLoader loader=(ClassLoader)
                    java.security.AccessController.doPrivileged(
                            new java.security.PrivilegedAction(){
                                public Object run(){
                                    return Thread.currentThread().getContextClassLoader();
                                }
                            });
            ResourceBundle bundle=null;
            try{
                bundle=ResourceBundle.getBundle(baseName,locale,loader);
            }catch(MissingResourceException mre){
                try{
                    bundle=ResourceBundle.getBundle(baseName,locale);
                }catch(MissingResourceException mre1){
                    throw new IllegalArgumentException("Bundle not found!");
                }
            }
            String warning=null;
            try{
                warning=bundle.getString(keyword);
            }catch(ClassCastException cce){
                throw new IllegalArgumentException("Resource is not a String!");
            }catch(MissingResourceException mre){
                throw new IllegalArgumentException("Resource is missing!");
            }
            listener.warningOccurred(this,imageIndex,warning);
        }
    }

    public void reset(){
        setOutput(null);
        setLocale(null);
        removeAllIIOWriteWarningListeners();
        removeAllIIOWriteProgressListeners();
        clearAbortRequest();
    }

    protected synchronized void clearAbortRequest(){
        this.abortFlag=false;
    }

    public void removeAllIIOWriteWarningListeners(){
        this.warningListeners=null;
        this.warningLocales=null;
    }
    // State management

    public void removeAllIIOWriteProgressListeners(){
        this.progressListeners=null;
    }

    public void dispose(){
    }
}
