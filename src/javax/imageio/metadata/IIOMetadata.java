/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio.metadata;

import org.w3c.dom.Node;

import java.lang.reflect.Method;

public abstract class IIOMetadata{
    protected boolean standardFormatSupported;
    protected String nativeMetadataFormatName=null;
    protected String nativeMetadataFormatClassName=null;
    protected String[] extraMetadataFormatNames=null;
    protected String[] extraMetadataFormatClassNames=null;
    protected IIOMetadataController defaultController=null;
    protected IIOMetadataController controller=null;

    protected IIOMetadata(){
    }

    protected IIOMetadata(boolean standardMetadataFormatSupported,
                          String nativeMetadataFormatName,
                          String nativeMetadataFormatClassName,
                          String[] extraMetadataFormatNames,
                          String[] extraMetadataFormatClassNames){
        this.standardFormatSupported=standardMetadataFormatSupported;
        this.nativeMetadataFormatName=nativeMetadataFormatName;
        this.nativeMetadataFormatClassName=nativeMetadataFormatClassName;
        if(extraMetadataFormatNames!=null){
            if(extraMetadataFormatNames.length==0){
                throw new IllegalArgumentException
                        ("extraMetadataFormatNames.length == 0!");
            }
            if(extraMetadataFormatClassNames==null){
                throw new IllegalArgumentException
                        ("extraMetadataFormatNames != null && extraMetadataFormatClassNames == null!");
            }
            if(extraMetadataFormatClassNames.length!=
                    extraMetadataFormatNames.length){
                throw new IllegalArgumentException
                        ("extraMetadataFormatClassNames.length != extraMetadataFormatNames.length!");
            }
            this.extraMetadataFormatNames=
                    (String[])extraMetadataFormatNames.clone();
            this.extraMetadataFormatClassNames=
                    (String[])extraMetadataFormatClassNames.clone();
        }else{
            if(extraMetadataFormatClassNames!=null){
                throw new IllegalArgumentException
                        ("extraMetadataFormatNames == null && extraMetadataFormatClassNames != null!");
            }
        }
    }

    public abstract boolean isReadOnly();

    public String[] getMetadataFormatNames(){
        String nativeName=getNativeMetadataFormatName();
        String standardName=isStandardMetadataFormatSupported()?
                IIOMetadataFormatImpl.standardMetadataFormatName:null;
        String[] extraNames=getExtraMetadataFormatNames();
        int numFormats=0;
        if(nativeName!=null){
            ++numFormats;
        }
        if(standardName!=null){
            ++numFormats;
        }
        if(extraNames!=null){
            numFormats+=extraNames.length;
        }
        if(numFormats==0){
            return null;
        }
        String[] formats=new String[numFormats];
        int index=0;
        if(nativeName!=null){
            formats[index++]=nativeName;
        }
        if(standardName!=null){
            formats[index++]=standardName;
        }
        if(extraNames!=null){
            for(int i=0;i<extraNames.length;i++){
                formats[index++]=extraNames[i];
            }
        }
        return formats;
    }

    public boolean isStandardMetadataFormatSupported(){
        return standardFormatSupported;
    }

    public String getNativeMetadataFormatName(){
        return nativeMetadataFormatName;
    }

    public String[] getExtraMetadataFormatNames(){
        if(extraMetadataFormatNames==null){
            return null;
        }
        return (String[])extraMetadataFormatNames.clone();
    }

    public IIOMetadataFormat getMetadataFormat(String formatName){
        if(formatName==null){
            throw new IllegalArgumentException("formatName == null!");
        }
        if(standardFormatSupported
                &&formatName.equals
                (IIOMetadataFormatImpl.standardMetadataFormatName)){
            return IIOMetadataFormatImpl.getStandardFormatInstance();
        }
        String formatClassName=null;
        if(formatName.equals(nativeMetadataFormatName)){
            formatClassName=nativeMetadataFormatClassName;
        }else if(extraMetadataFormatNames!=null){
            for(int i=0;i<extraMetadataFormatNames.length;i++){
                if(formatName.equals(extraMetadataFormatNames[i])){
                    formatClassName=extraMetadataFormatClassNames[i];
                    break;  // out of for
                }
            }
        }
        if(formatClassName==null){
            throw new IllegalArgumentException("Unsupported format name");
        }
        try{
            Class cls=null;
            final Object o=this;
            // firstly we try to use classloader used for loading
            // the IIOMetadata implemantation for this plugin.
            ClassLoader loader=(ClassLoader)
                    java.security.AccessController.doPrivileged(
                            new java.security.PrivilegedAction(){
                                public Object run(){
                                    return o.getClass().getClassLoader();
                                }
                            });
            try{
                cls=Class.forName(formatClassName,true,
                        loader);
            }catch(ClassNotFoundException e){
                // we failed to load IIOMetadataFormat class by
                // using IIOMetadata classloader.Next try is to
                // use thread context classloader.
                loader=(ClassLoader)
                        java.security.AccessController.doPrivileged(
                                new java.security.PrivilegedAction(){
                                    public Object run(){
                                        return Thread.currentThread().getContextClassLoader();
                                    }
                                });
                try{
                    cls=Class.forName(formatClassName,true,
                            loader);
                }catch(ClassNotFoundException e1){
                    // finally we try to use system classloader in case
                    // if we failed to load IIOMetadataFormat implementation
                    // class above.
                    cls=Class.forName(formatClassName,true,
                            ClassLoader.getSystemClassLoader());
                }
            }
            Method meth=cls.getMethod("getInstance");
            return (IIOMetadataFormat)meth.invoke(null);
        }catch(Exception e){
            RuntimeException ex=
                    new IllegalStateException("Can't obtain format");
            ex.initCause(e);
            throw ex;
        }
    }

    public abstract Node getAsTree(String formatName);

    protected final IIOMetadataNode getStandardTree(){
        IIOMetadataNode root=new IIOMetadataNode
                (IIOMetadataFormatImpl.standardMetadataFormatName);
        append(root,getStandardChromaNode());
        append(root,getStandardCompressionNode());
        append(root,getStandardDataNode());
        append(root,getStandardDimensionNode());
        append(root,getStandardDocumentNode());
        append(root,getStandardTextNode());
        append(root,getStandardTileNode());
        append(root,getStandardTransparencyNode());
        return root;
    }

    protected IIOMetadataNode getStandardChromaNode(){
        return null;
    }

    protected IIOMetadataNode getStandardCompressionNode(){
        return null;
    }

    protected IIOMetadataNode getStandardDataNode(){
        return null;
    }

    protected IIOMetadataNode getStandardDimensionNode(){
        return null;
    }

    protected IIOMetadataNode getStandardDocumentNode(){
        return null;
    }

    protected IIOMetadataNode getStandardTextNode(){
        return null;
    }

    protected IIOMetadataNode getStandardTileNode(){
        return null;
    }

    protected IIOMetadataNode getStandardTransparencyNode(){
        return null;
    }

    private void append(IIOMetadataNode root,IIOMetadataNode node){
        if(node!=null){
            root.appendChild(node);
        }
    }

    public void setFromTree(String formatName,Node root)
            throws IIOInvalidTreeException{
        reset();
        mergeTree(formatName,root);
    }

    public abstract void mergeTree(String formatName,Node root)
            throws IIOInvalidTreeException;

    public abstract void reset();

    public IIOMetadataController getDefaultController(){
        return defaultController;
    }

    public boolean activateController(){
        if(!hasController()){
            throw new IllegalStateException("hasController() == false!");
        }
        return getController().activate(this);
    }

    public boolean hasController(){
        return (getController()!=null);
    }

    public IIOMetadataController getController(){
        return controller;
    }

    public void setController(IIOMetadataController controller){
        this.controller=controller;
    }
}
