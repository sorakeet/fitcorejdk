/**
 * Copyright (c) 1996, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.datatransfer;

import sun.awt.datatransfer.DataTransferer;
import sun.reflect.misc.ReflectUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

import static sun.security.util.SecurityConstants.GET_CLASSLOADER_PERMISSION;

public class DataFlavor implements Externalizable, Cloneable{
    public static final DataFlavor stringFlavor=createConstant(String.class,"Unicode String");
    public static final DataFlavor imageFlavor=createConstant("image/x-java-image; class=java.awt.Image","Image");
    @Deprecated
    public static final DataFlavor plainTextFlavor=createConstant("text/plain; charset=unicode; class=java.io.InputStream","Plain Text");
    public static final String javaSerializedObjectMimeType="application/x-java-serialized-object";
    public static final DataFlavor javaFileListFlavor=createConstant("application/x-java-file-list;class=java.util.List",null);
    public static final String javaJVMLocalObjectMimeType="application/x-java-jvm-local-objectref";
    public static final String javaRemoteObjectMimeType="application/x-java-remote-object";
    private static final long serialVersionUID=8367026044764648243L;
    private static final Class<InputStream> ioInputStreamClass=InputStream.class;
    public static DataFlavor selectionHtmlFlavor=initHtmlDataFlavor("selection");
    public static DataFlavor fragmentHtmlFlavor=initHtmlDataFlavor("fragment");
    public static DataFlavor allHtmlFlavor=initHtmlDataFlavor("all");
    private static Comparator<DataFlavor> textFlavorComparator;
    transient int atom;
    MimeType mimeType;
    private String humanPresentableName;
    private Class<?> representationClass;

    public DataFlavor(){
        super();
    }

    public DataFlavor(Class<?> representationClass,String humanPresentableName){
        this("application","x-java-serialized-object",null,representationClass,humanPresentableName);
        if(representationClass==null){
            throw new NullPointerException("representationClass");
        }
    }

    private DataFlavor(String primaryType,String subType,MimeTypeParameterList params,Class<?> representationClass,String humanPresentableName){
        super();
        if(primaryType==null){
            throw new NullPointerException("primaryType");
        }
        if(subType==null){
            throw new NullPointerException("subType");
        }
        if(representationClass==null){
            throw new NullPointerException("representationClass");
        }
        if(params==null) params=new MimeTypeParameterList();
        params.set("class",representationClass.getName());
        if(humanPresentableName==null){
            humanPresentableName=params.get("humanPresentableName");
            if(humanPresentableName==null)
                humanPresentableName=primaryType+"/"+subType;
        }
        try{
            mimeType=new MimeType(primaryType,subType,params);
        }catch(MimeTypeParseException mtpe){
            throw new IllegalArgumentException("MimeType Parse Exception: "+mtpe.getMessage());
        }
        this.representationClass=representationClass;
        this.humanPresentableName=humanPresentableName;
        mimeType.removeParameter("humanPresentableName");
    }

    public DataFlavor(String mimeType,String humanPresentableName){
        super();
        if(mimeType==null){
            throw new NullPointerException("mimeType");
        }
        try{
            initialize(mimeType,humanPresentableName,this.getClass().getClassLoader());
        }catch(MimeTypeParseException mtpe){
            throw new IllegalArgumentException("failed to parse:"+mimeType);
        }catch(ClassNotFoundException cnfe){
            throw new IllegalArgumentException("can't find specified class: "+cnfe.getMessage());
        }
    }

    private void initialize(String mimeType,String humanPresentableName,ClassLoader classLoader) throws MimeTypeParseException, ClassNotFoundException{
        if(mimeType==null){
            throw new NullPointerException("mimeType");
        }
        this.mimeType=new MimeType(mimeType); // throws
        String rcn=getParameter("class");
        if(rcn==null){
            if("application/x-java-serialized-object".equals(this.mimeType.getBaseType()))
                throw new IllegalArgumentException("no representation class specified for:"+mimeType);
            else
                representationClass=InputStream.class; // default
        }else{ // got a class name
            representationClass=DataFlavor.tryToLoadClass(rcn,classLoader);
        }
        this.mimeType.setParameter("class",representationClass.getName());
        if(humanPresentableName==null){
            humanPresentableName=this.mimeType.getParameter("humanPresentableName");
            if(humanPresentableName==null)
                humanPresentableName=this.mimeType.getPrimaryType()+"/"+this.mimeType.getSubType();
        }
        this.humanPresentableName=humanPresentableName; // set it.
        this.mimeType.removeParameter("humanPresentableName"); // just in case
    }

    protected final static Class<?> tryToLoadClass(String className,
                                                   ClassLoader fallback)
            throws ClassNotFoundException{
        ReflectUtil.checkPackageAccess(className);
        try{
            SecurityManager sm=System.getSecurityManager();
            if(sm!=null){
                sm.checkPermission(GET_CLASSLOADER_PERMISSION);
            }
            ClassLoader loader=ClassLoader.getSystemClassLoader();
            try{
                // bootstrap class loader and system class loader if present
                return Class.forName(className,true,loader);
            }catch(ClassNotFoundException exception){
                // thread context class loader if and only if present
                loader=Thread.currentThread().getContextClassLoader();
                if(loader!=null){
                    try{
                        return Class.forName(className,true,loader);
                    }catch(ClassNotFoundException e){
                        // fallback to user's class loader
                    }
                }
            }
        }catch(SecurityException exception){
            // ignore secured class loaders
        }
        return Class.forName(className,true,fallback);
    }

    public String getParameter(String paramName){
        if(paramName.equals("humanPresentableName")){
            return humanPresentableName;
        }else{
            return (mimeType!=null)
                    ?mimeType.getParameter(paramName):null;
        }
    }

    public DataFlavor(String mimeType,String humanPresentableName,ClassLoader classLoader) throws ClassNotFoundException{
        super();
        if(mimeType==null){
            throw new NullPointerException("mimeType");
        }
        try{
            initialize(mimeType,humanPresentableName,classLoader);
        }catch(MimeTypeParseException mtpe){
            throw new IllegalArgumentException("failed to parse:"+mimeType);
        }
    }

    public DataFlavor(String mimeType) throws ClassNotFoundException{
        super();
        if(mimeType==null){
            throw new NullPointerException("mimeType");
        }
        try{
            initialize(mimeType,null,this.getClass().getClassLoader());
        }catch(MimeTypeParseException mtpe){
            throw new IllegalArgumentException("failed to parse:"+mimeType);
        }
    }

    static private DataFlavor createConstant(Class<?> rc,String prn){
        try{
            return new DataFlavor(rc,prn);
        }catch(Exception e){
            return null;
        }
    }

    static private DataFlavor createConstant(String mt,String prn){
        try{
            return new DataFlavor(mt,prn);
        }catch(Exception e){
            return null;
        }
    }

    static private DataFlavor initHtmlDataFlavor(String htmlFlavorType){
        try{
            return new DataFlavor("text/html; class=java.lang.String;document="+
                    htmlFlavorType+";charset=Unicode");
        }catch(Exception e){
            return null;
        }
    }

    public static final DataFlavor getTextPlainUnicodeFlavor(){
        String encoding=null;
        DataTransferer transferer=DataTransferer.getInstance();
        if(transferer!=null){
            encoding=transferer.getDefaultUnicodeEncoding();
        }
        return new DataFlavor(
                "text/plain;charset="+encoding
                        +";class=java.io.InputStream","Plain Text");
    }

    public static final DataFlavor selectBestTextFlavor(
            DataFlavor[] availableFlavors){
        if(availableFlavors==null||availableFlavors.length==0){
            return null;
        }
        if(textFlavorComparator==null){
            textFlavorComparator=new TextFlavorComparator();
        }
        DataFlavor bestFlavor=
                (DataFlavor)Collections.max(Arrays.asList(availableFlavors),
                        textFlavorComparator);
        if(!bestFlavor.isFlavorTextType()){
            return null;
        }
        return bestFlavor;
    }

    public Reader getReaderForText(Transferable transferable)
            throws UnsupportedFlavorException, IOException{
        Object transferObject=transferable.getTransferData(this);
        if(transferObject==null){
            throw new IllegalArgumentException
                    ("getTransferData() returned null");
        }
        if(transferObject instanceof Reader){
            return (Reader)transferObject;
        }else if(transferObject instanceof String){
            return new StringReader((String)transferObject);
        }else if(transferObject instanceof CharBuffer){
            CharBuffer buffer=(CharBuffer)transferObject;
            int size=buffer.remaining();
            char[] chars=new char[size];
            buffer.get(chars,0,size);
            return new CharArrayReader(chars);
        }else if(transferObject instanceof char[]){
            return new CharArrayReader((char[])transferObject);
        }
        InputStream stream=null;
        if(transferObject instanceof InputStream){
            stream=(InputStream)transferObject;
        }else if(transferObject instanceof ByteBuffer){
            ByteBuffer buffer=(ByteBuffer)transferObject;
            int size=buffer.remaining();
            byte[] bytes=new byte[size];
            buffer.get(bytes,0,size);
            stream=new ByteArrayInputStream(bytes);
        }else if(transferObject instanceof byte[]){
            stream=new ByteArrayInputStream((byte[])transferObject);
        }
        if(stream==null){
            throw new IllegalArgumentException("transfer data is not Reader, String, CharBuffer, char array, InputStream, ByteBuffer, or byte array");
        }
        String encoding=getParameter("charset");
        return (encoding==null)
                ?new InputStreamReader(stream)
                :new InputStreamReader(stream,encoding);
    }

    public String getMimeType(){
        return (mimeType!=null)?mimeType.toString():null;
    }

    public String getHumanPresentableName(){
        return humanPresentableName;
    }

    public void setHumanPresentableName(String humanPresentableName){
        this.humanPresentableName=humanPresentableName;
    }

    @Deprecated
    public boolean equals(String s){
        if(s==null||mimeType==null)
            return false;
        return isMimeTypeEqual(s);
    }

    public boolean isMimeTypeEqual(String mimeType){
        // JCK Test DataFlavor0117: if 'mimeType' is null, throw NPE
        if(mimeType==null){
            throw new NullPointerException("mimeType");
        }
        if(this.mimeType==null){
            return false;
        }
        try{
            return this.mimeType.match(new MimeType(mimeType));
        }catch(MimeTypeParseException mtpe){
            return false;
        }
    }

    public int hashCode(){
        int total=0;
        if(representationClass!=null){
            total+=representationClass.hashCode();
        }
        if(mimeType!=null){
            String primaryType=mimeType.getPrimaryType();
            if(primaryType!=null){
                total+=primaryType.hashCode();
            }
            // Do not add subType.hashCode() to the total. equals uses
            // MimeType.match which reports a match if one or both of the
            // subTypes is '*', regardless of the other subType.
            if("text".equals(primaryType)){
                if(DataTransferer.doesSubtypeSupportCharset(this)
                        &&representationClass!=null
                        &&!isStandardTextRepresentationClass()){
                    String charset=DataTransferer.canonicalName(getParameter("charset"));
                    if(charset!=null){
                        total+=charset.hashCode();
                    }
                }
                if("html".equals(getSubType())){
                    String document=this.getParameter("document");
                    if(document!=null){
                        total+=document.hashCode();
                    }
                }
            }
        }
        return total;
    }

    public boolean equals(Object o){
        return ((o instanceof DataFlavor)&&equals((DataFlavor)o));
    }

    public boolean equals(DataFlavor that){
        if(that==null){
            return false;
        }
        if(this==that){
            return true;
        }
        if(!Objects.equals(this.getRepresentationClass(),that.getRepresentationClass())){
            return false;
        }
        if(mimeType==null){
            if(that.mimeType!=null){
                return false;
            }
        }else{
            if(!mimeType.match(that.mimeType)){
                return false;
            }
            if("text".equals(getPrimaryType())){
                if(DataTransferer.doesSubtypeSupportCharset(this)
                        &&representationClass!=null
                        &&!isStandardTextRepresentationClass()){
                    String thisCharset=
                            DataTransferer.canonicalName(this.getParameter("charset"));
                    String thatCharset=
                            DataTransferer.canonicalName(that.getParameter("charset"));
                    if(!Objects.equals(thisCharset,thatCharset)){
                        return false;
                    }
                }
                if("html".equals(getSubType())){
                    String thisDocument=this.getParameter("document");
                    String thatDocument=that.getParameter("document");
                    if(!Objects.equals(thisDocument,thatDocument)){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public Class<?> getRepresentationClass(){
        return representationClass;
    }

    public String getPrimaryType(){
        return (mimeType!=null)?mimeType.getPrimaryType():null;
    }

    public Object clone() throws CloneNotSupportedException{
        Object newObj=super.clone();
        if(mimeType!=null){
            ((DataFlavor)newObj).mimeType=(MimeType)mimeType.clone();
        }
        return newObj;
    } // clone()

    public String toString(){
        String string=getClass().getName();
        string+="["+paramString()+"]";
        return string;
    }

    private String paramString(){
        String params="";
        params+="mimetype=";
        if(mimeType==null){
            params+="null";
        }else{
            params+=mimeType.getBaseType();
        }
        params+=";representationclass=";
        if(representationClass==null){
            params+="null";
        }else{
            params+=representationClass.getName();
        }
        if(DataTransferer.isFlavorCharsetTextType(this)&&
                (isRepresentationClassInputStream()||
                        isRepresentationClassByteBuffer()||
                        byte[].class.equals(representationClass))){
            params+=";charset="+DataTransferer.getTextCharset(this);
        }
        return params;
    }

    public boolean isRepresentationClassInputStream(){
        return ioInputStreamClass.isAssignableFrom(representationClass);
    }

    public boolean isRepresentationClassByteBuffer(){
        return ByteBuffer.class.isAssignableFrom(representationClass);
    }

    public String getSubType(){
        return (mimeType!=null)?mimeType.getSubType():null;
    }

    private boolean isStandardTextRepresentationClass(){
        return isRepresentationClassReader()
                ||String.class.equals(representationClass)
                ||isRepresentationClassCharBuffer()
                ||char[].class.equals(representationClass);
    }

    public boolean isRepresentationClassReader(){
        return Reader.class.isAssignableFrom(representationClass);
    }

    public boolean isRepresentationClassCharBuffer(){
        return CharBuffer.class.isAssignableFrom(representationClass);
    }

    public boolean match(DataFlavor that){
        return equals(that);
    }

    public final boolean isMimeTypeEqual(DataFlavor dataFlavor){
        return isMimeTypeEqual(dataFlavor.mimeType);
    }

    private boolean isMimeTypeEqual(MimeType mtype){
        if(this.mimeType==null){
            return (mtype==null);
        }
        return mimeType.match(mtype);
    }

    public boolean isMimeTypeSerializedObject(){
        return isMimeTypeEqual(javaSerializedObjectMimeType);
    }

    public final String getDefaultRepresentationClassAsString(){
        return getDefaultRepresentationClass().getName();
    }

    public final Class<?> getDefaultRepresentationClass(){
        return ioInputStreamClass;
    }

    public boolean isFlavorSerializedObjectType(){
        return isRepresentationClassSerializable()&&isMimeTypeEqual(javaSerializedObjectMimeType);
    }

    public boolean isRepresentationClassSerializable(){
        return Serializable.class.isAssignableFrom(representationClass);
    }

    public boolean isFlavorRemoteObjectType(){
        return isRepresentationClassRemote()
                &&isRepresentationClassSerializable()
                &&isMimeTypeEqual(javaRemoteObjectMimeType);
    }

    public boolean isRepresentationClassRemote(){
        return DataTransferer.isRemote(representationClass);
    }

    public boolean isFlavorJavaFileListType(){
        if(mimeType==null||representationClass==null)
            return false;
        return java.util.List.class.isAssignableFrom(representationClass)&&
                mimeType.match(javaFileListFlavor.mimeType);
    }

    public boolean isFlavorTextType(){
        return (DataTransferer.isFlavorCharsetTextType(this)||
                DataTransferer.isFlavorNoncharsetTextType(this));
    }

    public synchronized void writeExternal(ObjectOutput os) throws IOException{
        if(mimeType!=null){
            mimeType.setParameter("humanPresentableName",humanPresentableName);
            os.writeObject(mimeType);
            mimeType.removeParameter("humanPresentableName");
        }else{
            os.writeObject(null);
        }
        os.writeObject(representationClass);
    }

    public synchronized void readExternal(ObjectInput is) throws IOException, ClassNotFoundException{
        String rcn=null;
        mimeType=(MimeType)is.readObject();
        if(mimeType!=null){
            humanPresentableName=
                    mimeType.getParameter("humanPresentableName");
            mimeType.removeParameter("humanPresentableName");
            rcn=mimeType.getParameter("class");
            if(rcn==null){
                throw new IOException("no class parameter specified in: "+
                        mimeType);
            }
        }
        try{
            representationClass=(Class)is.readObject();
        }catch(OptionalDataException ode){
            if(!ode.eof||ode.length!=0){
                throw ode;
            }
            // Ensure backward compatibility.
            // Old versions didn't write the representation class to the stream.
            if(rcn!=null){
                representationClass=
                        DataFlavor.tryToLoadClass(rcn,getClass().getClassLoader());
            }
        }
    }

    @Deprecated
    protected String normalizeMimeTypeParameter(String parameterName,String parameterValue){
        return parameterValue;
    }

    @Deprecated
    protected String normalizeMimeType(String mimeType){
        return mimeType;
    }

    static class TextFlavorComparator
            extends DataTransferer.DataFlavorComparator{
        public int compare(Object obj1,Object obj2){
            DataFlavor flavor1=(DataFlavor)obj1;
            DataFlavor flavor2=(DataFlavor)obj2;
            if(flavor1.isFlavorTextType()){
                if(flavor2.isFlavorTextType()){
                    return super.compare(obj1,obj2);
                }else{
                    return 1;
                }
            }else if(flavor2.isFlavorTextType()){
                return -1;
            }else{
                return 0;
            }
        }
    }
} // class DataFlavor
