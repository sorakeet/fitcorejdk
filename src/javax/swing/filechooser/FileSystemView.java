/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.filechooser;

import sun.awt.shell.ShellFolder;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
// PENDING(jeff) - need to provide a specification for
// how Mac/OS2/BeOS/etc file systems can modify FileSystemView
// to handle their particular type of file system.

public abstract class FileSystemView{
    static FileSystemView windowsFileSystemView=null;
    static FileSystemView unixFileSystemView=null;
    //static FileSystemView macFileSystemView = null;
    static FileSystemView genericFileSystemView=null;
    private boolean useSystemExtensionHiding=
            UIManager.getDefaults().getBoolean("FileChooser.useSystemExtensionHiding");

    public FileSystemView(){
        final WeakReference<FileSystemView> weakReference=new WeakReference<FileSystemView>(this);
        UIManager.addPropertyChangeListener(new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent evt){
                FileSystemView fileSystemView=weakReference.get();
                if(fileSystemView==null){
                    // FileSystemView was destroyed
                    UIManager.removePropertyChangeListener(this);
                }else{
                    if(evt.getPropertyName().equals("lookAndFeel")){
                        fileSystemView.useSystemExtensionHiding=
                                UIManager.getDefaults().getBoolean("FileChooser.useSystemExtensionHiding");
                    }
                }
            }
        });
    }

    public static FileSystemView getFileSystemView(){
        if(File.separatorChar=='\\'){
            if(windowsFileSystemView==null){
                windowsFileSystemView=new WindowsFileSystemView();
            }
            return windowsFileSystemView;
        }
        if(File.separatorChar=='/'){
            if(unixFileSystemView==null){
                unixFileSystemView=new UnixFileSystemView();
            }
            return unixFileSystemView;
        }
        // if(File.separatorChar == ':') {
        //    if(macFileSystemView == null) {
        //      macFileSystemView = new MacFileSystemView();
        //    }
        //    return macFileSystemView;
        //}
        if(genericFileSystemView==null){
            genericFileSystemView=new GenericFileSystemView();
        }
        return genericFileSystemView;
    }

    public boolean isRoot(File f){
        if(f==null||!f.isAbsolute()){
            return false;
        }
        File[] roots=getRoots();
        for(File root : roots){
            if(root.equals(f)){
                return true;
            }
        }
        return false;
    }

    public File[] getRoots(){
        // Don't cache this array, because filesystem might change
        File[] roots=(File[])ShellFolder.get("roots");
        for(int i=0;i<roots.length;i++){
            if(isFileSystemRoot(roots[i])){
                roots[i]=createFileSystemRoot(roots[i]);
            }
        }
        return roots;
    }

    public boolean isFileSystemRoot(File dir){
        return ShellFolder.isFileSystemRoot(dir);
    }

    protected File createFileSystemRoot(File f){
        return new FileSystemRoot(f);
    }

    public Boolean isTraversable(File f){
        return Boolean.valueOf(f.isDirectory());
    }

    public String getSystemDisplayName(File f){
        if(f==null){
            return null;
        }
        String name=f.getName();
        if(!name.equals("..")&&!name.equals(".")&&
                (useSystemExtensionHiding||!isFileSystem(f)||isFileSystemRoot(f))&&
                (f instanceof ShellFolder||f.exists())){
            try{
                name=getShellFolder(f).getDisplayName();
            }catch(FileNotFoundException e){
                return null;
            }
            if(name==null||name.length()==0){
                name=f.getPath(); // e.g. "/"
            }
        }
        return name;
    }

    public boolean isFileSystem(File f){
        if(f instanceof ShellFolder){
            ShellFolder sf=(ShellFolder)f;
            // Shortcuts to directories are treated as not being file system objects,
            // so that they are never returned by JFileChooser.
            return sf.isFileSystem()&&!(sf.isLink()&&sf.isDirectory());
        }else{
            return true;
        }
    }

    ShellFolder getShellFolder(File f) throws FileNotFoundException{
        if(!(f instanceof ShellFolder)&&!(f instanceof FileSystemRoot)&&isFileSystemRoot(f)){
            f=createFileSystemRoot(f);
        }
        try{
            return ShellFolder.getShellFolder(f);
        }catch(InternalError e){
            System.err.println("FileSystemView.getShellFolder: f="+f);
            e.printStackTrace();
            return null;
        }
    }

    public String getSystemTypeDescription(File f){
        return null;
    }

    public Icon getSystemIcon(File f){
        if(f==null){
            return null;
        }
        ShellFolder sf;
        try{
            sf=getShellFolder(f);
        }catch(FileNotFoundException e){
            return null;
        }
        Image img=sf.getIcon(false);
        if(img!=null){
            return new ImageIcon(img,sf.getFolderType());
        }else{
            return UIManager.getIcon(f.isDirectory()?"FileView.directoryIcon":"FileView.fileIcon");
        }
    }

    public boolean isParent(File folder,File file){
        if(folder==null||file==null){
            return false;
        }else if(folder instanceof ShellFolder){
            File parent=file.getParentFile();
            if(parent!=null&&parent.equals(folder)){
                return true;
            }
            File[] children=getFiles(folder,false);
            for(File child : children){
                if(file.equals(child)){
                    return true;
                }
            }
            return false;
        }else{
            return folder.equals(file.getParentFile());
        }
    }

    public File[] getFiles(File dir,boolean useFileHiding){
        List<File> files=new ArrayList<File>();
        // add all files in dir
        if(!(dir instanceof ShellFolder)){
            try{
                dir=getShellFolder(dir);
            }catch(FileNotFoundException e){
                return new File[0];
            }
        }
        File[] names=((ShellFolder)dir).listFiles(!useFileHiding);
        if(names==null){
            return new File[0];
        }
        for(File f : names){
            if(Thread.currentThread().isInterrupted()){
                break;
            }
            if(!(f instanceof ShellFolder)){
                if(isFileSystemRoot(f)){
                    f=createFileSystemRoot(f);
                }
                try{
                    f=ShellFolder.getShellFolder(f);
                }catch(FileNotFoundException e){
                    // Not a valid file (wouldn't show in native file chooser)
                    // Example: C:\pagefile.sys
                    continue;
                }catch(InternalError e){
                    // Not a valid file (wouldn't show in native file chooser)
                    // Example C:\Winnt\Profiles\joe\history\History.IE5
                    continue;
                }
            }
            if(!useFileHiding||!isHiddenFile(f)){
                files.add(f);
            }
        }
        return files.toArray(new File[files.size()]);
    }

    public boolean isHiddenFile(File f){
        return f.isHidden();
    }

    public File getChild(File parent,String fileName){
        if(parent instanceof ShellFolder){
            File[] children=getFiles(parent,false);
            for(File child : children){
                if(child.getName().equals(fileName)){
                    return child;
                }
            }
        }
        return createFileObject(parent,fileName);
    }

    public File createFileObject(File dir,String filename){
        if(dir==null){
            return new File(filename);
        }else{
            return new File(dir,filename);
        }
    }
    // Providing default implementations for the remaining methods
    // because most OS file systems will likely be able to use this
    // code. If a given OS can't, override these methods in its
    // implementation.

    public abstract File createNewFolder(File containingDir) throws IOException;

    public boolean isDrive(File dir){
        return false;
    }

    public boolean isFloppyDrive(File dir){
        return false;
    }

    public boolean isComputerNode(File dir){
        return ShellFolder.isComputerNode(dir);
    }

    public File getHomeDirectory(){
        return createFileObject(System.getProperty("user.home"));
    }

    public File createFileObject(String path){
        File f=new File(path);
        if(isFileSystemRoot(f)){
            f=createFileSystemRoot(f);
        }
        return f;
    }

    public File getDefaultDirectory(){
        File f=(File)ShellFolder.get("fileChooserDefaultFolder");
        if(isFileSystemRoot(f)){
            f=createFileSystemRoot(f);
        }
        return f;
    }

    public File getParentDirectory(File dir){
        if(dir==null||!dir.exists()){
            return null;
        }
        ShellFolder sf;
        try{
            sf=getShellFolder(dir);
        }catch(FileNotFoundException e){
            return null;
        }
        File psf=sf.getParentFile();
        if(psf==null){
            return null;
        }
        if(isFileSystem(psf)){
            File f=psf;
            if(!f.exists()){
                // This could be a node under "Network Neighborhood".
                File ppsf=psf.getParentFile();
                if(ppsf==null||!isFileSystem(ppsf)){
                    // We're mostly after the exists() override for windows below.
                    f=createFileSystemRoot(f);
                }
            }
            return f;
        }else{
            return psf;
        }
    }

    static class FileSystemRoot extends File{
        public FileSystemRoot(File f){
            super(f,"");
        }

        public FileSystemRoot(String s){
            super(s);
        }

        public String getName(){
            return getPath();
        }        public boolean isDirectory(){
            return true;
        }


    }
}

class UnixFileSystemView extends FileSystemView{
    private static final String newFolderString=
            UIManager.getString("FileChooser.other.newFolder");
    private static final String newFolderNextString=
            UIManager.getString("FileChooser.other.newFolder.subsequent");

    public File createNewFolder(File containingDir) throws IOException{
        if(containingDir==null){
            throw new IOException("Containing directory is null:");
        }
        File newFolder;
        // Unix - using OpenWindows' default folder name. Can't find one for Motif/CDE.
        newFolder=createFileObject(containingDir,newFolderString);
        int i=1;
        while(newFolder.exists()&&i<100){
            newFolder=createFileObject(containingDir,MessageFormat.format(
                    newFolderNextString,new Integer(i)));
            i++;
        }
        if(newFolder.exists()){
            throw new IOException("Directory already exists:"+newFolder.getAbsolutePath());
        }else{
            newFolder.mkdirs();
        }
        return newFolder;
    }

    public boolean isFileSystemRoot(File dir){
        return dir!=null&&dir.getAbsolutePath().equals("/");
    }

    public boolean isDrive(File dir){
        return isFloppyDrive(dir);
    }

    public boolean isFloppyDrive(File dir){
        // Could be looking at the path for Solaris, but wouldn't be reliable.
        // For example:
        // return (dir != null && dir.getAbsolutePath().toLowerCase().startsWith("/floppy"));
        return false;
    }

    public boolean isComputerNode(File dir){
        if(dir!=null){
            String parent=dir.getParent();
            if(parent!=null&&parent.equals("/net")){
                return true;
            }
        }
        return false;
    }
}

class WindowsFileSystemView extends FileSystemView{
    private static final String newFolderString=
            UIManager.getString("FileChooser.win32.newFolder");
    private static final String newFolderNextString=
            UIManager.getString("FileChooser.win32.newFolder.subsequent");

    public Boolean isTraversable(File f){
        return Boolean.valueOf(isFileSystemRoot(f)||isComputerNode(f)||f.isDirectory());
    }

    public String getSystemTypeDescription(File f){
        if(f==null){
            return null;
        }
        try{
            return getShellFolder(f).getFolderType();
        }catch(FileNotFoundException e){
            return null;
        }
    }

    public File getChild(File parent,String fileName){
        if(fileName.startsWith("\\")
                &&!fileName.startsWith("\\\\")
                &&isFileSystem(parent)){
            //Path is relative to the root of parent's drive
            String path=parent.getAbsolutePath();
            if(path.length()>=2
                    &&path.charAt(1)==':'
                    &&Character.isLetter(path.charAt(0))){
                return createFileObject(path.substring(0,2)+fileName);
            }
        }
        return super.getChild(parent,fileName);
    }

    public File getHomeDirectory(){
        File[] roots=getRoots();
        return (roots.length==0)?null:roots[0];
    }

    public File createNewFolder(File containingDir) throws IOException{
        if(containingDir==null){
            throw new IOException("Containing directory is null:");
        }
        // Using NT's default folder name
        File newFolder=createFileObject(containingDir,newFolderString);
        int i=2;
        while(newFolder.exists()&&i<100){
            newFolder=createFileObject(containingDir,MessageFormat.format(
                    newFolderNextString,new Integer(i)));
            i++;
        }
        if(newFolder.exists()){
            throw new IOException("Directory already exists:"+newFolder.getAbsolutePath());
        }else{
            newFolder.mkdirs();
        }
        return newFolder;
    }

    public boolean isDrive(File dir){
        return isFileSystemRoot(dir);
    }

    public boolean isFloppyDrive(final File dir){
        String path=AccessController.doPrivileged(new PrivilegedAction<String>(){
            public String run(){
                return dir.getAbsolutePath();
            }
        });
        return path!=null&&(path.equals("A:\\")||path.equals("B:\\"));
    }

    public File createFileObject(String path){
        // Check for missing backslash after drive letter such as "C:" or "C:filename"
        if(path.length()>=2&&path.charAt(1)==':'&&Character.isLetter(path.charAt(0))){
            if(path.length()==2){
                path+="\\";
            }else if(path.charAt(2)!='\\'){
                path=path.substring(0,2)+"\\"+path.substring(2);
            }
        }
        return super.createFileObject(path);
    }

    protected File createFileSystemRoot(File f){
        // Problem: Removable drives on Windows return false on f.exists()
        // Workaround: Override exists() to always return true.
        return new FileSystemRoot(f){
            public boolean exists(){
                return true;
            }
        };
    }
}

class GenericFileSystemView extends FileSystemView{
    private static final String newFolderString=
            UIManager.getString("FileChooser.other.newFolder");

    public File createNewFolder(File containingDir) throws IOException{
        if(containingDir==null){
            throw new IOException("Containing directory is null:");
        }
        // Using NT's default folder name
        File newFolder=createFileObject(containingDir,newFolderString);
        if(newFolder.exists()){
            throw new IOException("Directory already exists:"+newFolder.getAbsolutePath());
        }else{
            newFolder.mkdirs();
        }
        return newFolder;
    }
}
