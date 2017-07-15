/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import sun.awt.AWTAccessor;

import java.awt.peer.FileDialogPeer;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;

public class FileDialog extends Dialog{
    public static final int LOAD=0;
    public static final int SAVE=1;
    private static final String base="filedlg";
    private static final long serialVersionUID=5035145889651310422L;
    private static int nameCounter=0;

    static{
        /** ensure that the necessary native libraries are loaded */
        Toolkit.loadLibraries();
        if(!GraphicsEnvironment.isHeadless()){
            initIDs();
        }
    }

    static{
        AWTAccessor.setFileDialogAccessor(
                new AWTAccessor.FileDialogAccessor(){
                    public void setFiles(FileDialog fileDialog,File files[]){
                        fileDialog.setFiles(files);
                    }

                    public void setFile(FileDialog fileDialog,String file){
                        fileDialog.file=("".equals(file))?null:file;
                    }

                    public void setDirectory(FileDialog fileDialog,String directory){
                        fileDialog.dir=("".equals(directory))?null:directory;
                    }

                    public boolean isMultipleMode(FileDialog fileDialog){
                        synchronized(fileDialog.getObjectLock()){
                            return fileDialog.multipleMode;
                        }
                    }
                });
    }

    int mode;
    String dir;
    String file;
    FilenameFilter filter;
    private File[] files;
    private boolean multipleMode=false;

    public FileDialog(Frame parent){
        this(parent,"",LOAD);
    }

    public FileDialog(Frame parent,String title,int mode){
        super(parent,title,true);
        this.setMode(mode);
        setLayout(null);
    }

    public FileDialog(Frame parent,String title){
        this(parent,title,LOAD);
    }

    public FileDialog(Dialog parent){
        this(parent,"",LOAD);
    }

    public FileDialog(Dialog parent,String title,int mode){
        super(parent,title,true);
        this.setMode(mode);
        setLayout(null);
    }

    public FileDialog(Dialog parent,String title){
        this(parent,title,LOAD);
    }

    private static native void initIDs();

    String constructComponentName(){
        synchronized(FileDialog.class){
            return base+nameCounter++;
        }
    }

    public void addNotify(){
        synchronized(getTreeLock()){
            if(parent!=null&&parent.getPeer()==null){
                parent.addNotify();
            }
            if(peer==null)
                peer=getToolkit().createFileDialog(this);
            super.addNotify();
        }
    }

    protected String paramString(){
        String str=super.paramString();
        str+=",dir= "+dir;
        str+=",file= "+file;
        return str+((mode==LOAD)?",load":",save");
    }

    public int getMode(){
        return mode;
    }

    public void setMode(int mode){
        switch(mode){
            case LOAD:
            case SAVE:
                this.mode=mode;
                break;
            default:
                throw new IllegalArgumentException("illegal file dialog mode");
        }
    }

    public String getDirectory(){
        return dir;
    }

    public void setDirectory(String dir){
        this.dir=(dir!=null&&dir.equals(""))?null:dir;
        FileDialogPeer peer=(FileDialogPeer)this.peer;
        if(peer!=null){
            peer.setDirectory(this.dir);
        }
    }

    public String getFile(){
        return file;
    }

    public void setFile(String file){
        this.file=(file!=null&&file.equals(""))?null:file;
        FileDialogPeer peer=(FileDialogPeer)this.peer;
        if(peer!=null){
            peer.setFile(this.file);
        }
    }

    public File[] getFiles(){
        synchronized(getObjectLock()){
            if(files!=null){
                return files.clone();
            }else{
                return new File[0];
            }
        }
    }

    private void setFiles(File files[]){
        synchronized(getObjectLock()){
            this.files=files;
        }
    }

    public boolean isMultipleMode(){
        synchronized(getObjectLock()){
            return multipleMode;
        }
    }

    public void setMultipleMode(boolean enable){
        synchronized(getObjectLock()){
            this.multipleMode=enable;
        }
    }

    public FilenameFilter getFilenameFilter(){
        return filter;
    }

    public synchronized void setFilenameFilter(FilenameFilter filter){
        this.filter=filter;
        FileDialogPeer peer=(FileDialogPeer)this.peer;
        if(peer!=null){
            peer.setFilenameFilter(filter);
        }
    }

    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException{
        s.defaultReadObject();
        // 1.1 Compatibility: "" is not converted to null in 1.1
        if(dir!=null&&dir.equals("")){
            dir=null;
        }
        if(file!=null&&file.equals("")){
            file=null;
        }
    }

    boolean postsOldMouseEvents(){
        return false;
    }
}
