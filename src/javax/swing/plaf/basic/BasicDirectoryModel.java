/**
 * Copyright (c) 1998, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.basic;

import sun.awt.shell.ShellFolder;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.filechooser.FileSystemView;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;

public class BasicDirectoryModel extends AbstractListModel<Object> implements PropertyChangeListener{
    private JFileChooser filechooser=null;
    // PENDING(jeff) pick the size more sensibly
    private Vector<File> fileCache=new Vector<File>(50);
    private LoadFilesThread loadThread=null;
    private Vector<File> files=null;
    private Vector<File> directories=null;
    private int fetchID=0;
    private PropertyChangeSupport changeSupport;
    private boolean busy=false;

    public BasicDirectoryModel(JFileChooser filechooser){
        this.filechooser=filechooser;
        validateFileCache();
    }

    public void validateFileCache(){
        File currentDirectory=filechooser.getCurrentDirectory();
        if(currentDirectory==null){
            return;
        }
        if(loadThread!=null){
            loadThread.interrupt();
            loadThread.cancelRunnables();
        }
        setBusy(true,++fetchID);
        loadThread=new LoadFilesThread(currentDirectory,fetchID);
        loadThread.start();
    }

    private synchronized void setBusy(final boolean busy,int fid){
        if(fid==fetchID){
            boolean oldValue=this.busy;
            this.busy=busy;
            if(changeSupport!=null&&busy!=oldValue){
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        firePropertyChange("busy",!busy,busy);
                    }
                });
            }
        }
    }

    protected void firePropertyChange(String propertyName,
                                      Object oldValue,Object newValue){
        if(changeSupport!=null){
            changeSupport.firePropertyChange(propertyName,
                    oldValue,newValue);
        }
    }

    public void propertyChange(PropertyChangeEvent e){
        String prop=e.getPropertyName();
        if(prop==JFileChooser.DIRECTORY_CHANGED_PROPERTY||
                prop==JFileChooser.FILE_VIEW_CHANGED_PROPERTY||
                prop==JFileChooser.FILE_FILTER_CHANGED_PROPERTY||
                prop==JFileChooser.FILE_HIDING_CHANGED_PROPERTY||
                prop==JFileChooser.FILE_SELECTION_MODE_CHANGED_PROPERTY){
            validateFileCache();
        }else if("UI".equals(prop)){
            Object old=e.getOldValue();
            if(old instanceof BasicFileChooserUI){
                BasicFileChooserUI ui=(BasicFileChooserUI)old;
                BasicDirectoryModel model=ui.getModel();
                if(model!=null){
                    model.invalidateFileCache();
                }
            }
        }else if("JFileChooserDialogIsClosingProperty".equals(prop)){
            invalidateFileCache();
        }
    }

    public void invalidateFileCache(){
        if(loadThread!=null){
            loadThread.interrupt();
            loadThread.cancelRunnables();
            loadThread=null;
        }
    }

    public Vector<File> getDirectories(){
        synchronized(fileCache){
            if(directories!=null){
                return directories;
            }
            Vector fls=getFiles();
            return directories;
        }
    }

    public Vector<File> getFiles(){
        synchronized(fileCache){
            if(files!=null){
                return files;
            }
            files=new Vector<File>();
            directories=new Vector<File>();
            directories.addElement(filechooser.getFileSystemView().createFileObject(
                    filechooser.getCurrentDirectory(),"..")
            );
            for(int i=0;i<getSize();i++){
                File f=fileCache.get(i);
                if(filechooser.isTraversable(f)){
                    directories.add(f);
                }else{
                    files.add(f);
                }
            }
            return files;
        }
    }

    public int getSize(){
        return fileCache.size();
    }

    public Object getElementAt(int index){
        return fileCache.get(index);
    }

    public boolean renameFile(File oldFile,File newFile){
        synchronized(fileCache){
            if(oldFile.renameTo(newFile)){
                validateFileCache();
                return true;
            }
            return false;
        }
    }

    public void fireContentsChanged(){
        // System.out.println("BasicDirectoryModel: firecontentschanged");
        fireContentsChanged(this,0,getSize()-1);
    }

    public boolean contains(Object o){
        return fileCache.contains(o);
    }

    public int indexOf(Object o){
        return fileCache.indexOf(o);
    }

    public void intervalAdded(ListDataEvent e){
    }

    public void intervalRemoved(ListDataEvent e){
    }

    protected void sort(Vector<? extends File> v){
        ShellFolder.sort(v);
    }

    // Obsolete - not used
    protected boolean lt(File a,File b){
        // First ignore case when comparing
        int diff=a.getName().toLowerCase().compareTo(b.getName().toLowerCase());
        if(diff!=0){
            return diff<0;
        }else{
            // May differ in case (e.g. "mail" vs. "Mail")
            return a.getName().compareTo(b.getName())<0;
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener){
        if(changeSupport==null){
            changeSupport=new PropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener){
        if(changeSupport!=null){
            changeSupport.removePropertyChangeListener(listener);
        }
    }

    public PropertyChangeListener[] getPropertyChangeListeners(){
        if(changeSupport==null){
            return new PropertyChangeListener[0];
        }
        return changeSupport.getPropertyChangeListeners();
    }

    class LoadFilesThread extends Thread{
        File currentDirectory=null;
        int fid;
        Vector<DoChangeContents> runnables=new Vector<DoChangeContents>(10);

        public LoadFilesThread(File currentDirectory,int fid){
            super("Basic L&F File Loading Thread");
            this.currentDirectory=currentDirectory;
            this.fid=fid;
        }

        public void run(){
            run0();
            setBusy(false,fid);
        }

        public void run0(){
            FileSystemView fileSystem=filechooser.getFileSystemView();
            if(isInterrupted()){
                return;
            }
            File[] list=fileSystem.getFiles(currentDirectory,filechooser.isFileHidingEnabled());
            if(isInterrupted()){
                return;
            }
            final Vector<File> newFileCache=new Vector<File>();
            Vector<File> newFiles=new Vector<File>();
            // run through the file list, add directories and selectable files to fileCache
            // Note that this block must be OUTSIDE of Invoker thread because of
            // deadlock possibility with custom synchronized FileSystemView
            for(File file : list){
                if(filechooser.accept(file)){
                    boolean isTraversable=filechooser.isTraversable(file);
                    if(isTraversable){
                        newFileCache.addElement(file);
                    }else if(filechooser.isFileSelectionEnabled()){
                        newFiles.addElement(file);
                    }
                    if(isInterrupted()){
                        return;
                    }
                }
            }
            // First sort alphabetically by filename
            sort(newFileCache);
            sort(newFiles);
            newFileCache.addAll(newFiles);
            // To avoid loads of synchronizations with Invoker and improve performance we
            // execute the whole block on the COM thread
            DoChangeContents doChangeContents=ShellFolder.invoke(new Callable<DoChangeContents>(){
                public DoChangeContents call(){
                    int newSize=newFileCache.size();
                    int oldSize=fileCache.size();
                    if(newSize>oldSize){
                        //see if interval is added
                        int start=oldSize;
                        int end=newSize;
                        for(int i=0;i<oldSize;i++){
                            if(!newFileCache.get(i).equals(fileCache.get(i))){
                                start=i;
                                for(int j=i;j<newSize;j++){
                                    if(newFileCache.get(j).equals(fileCache.get(i))){
                                        end=j;
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                        if(start>=0&&end>start
                                &&newFileCache.subList(end,newSize).equals(fileCache.subList(start,oldSize))){
                            if(isInterrupted()){
                                return null;
                            }
                            return new DoChangeContents(newFileCache.subList(start,end),start,null,0,fid);
                        }
                    }else if(newSize<oldSize){
                        //see if interval is removed
                        int start=-1;
                        int end=-1;
                        for(int i=0;i<newSize;i++){
                            if(!newFileCache.get(i).equals(fileCache.get(i))){
                                start=i;
                                end=i+oldSize-newSize;
                                break;
                            }
                        }
                        if(start>=0&&end>start
                                &&fileCache.subList(end,oldSize).equals(newFileCache.subList(start,newSize))){
                            if(isInterrupted()){
                                return null;
                            }
                            return new DoChangeContents(null,0,new Vector(fileCache.subList(start,end)),start,fid);
                        }
                    }
                    if(!fileCache.equals(newFileCache)){
                        if(isInterrupted()){
                            cancelRunnables(runnables);
                        }
                        return new DoChangeContents(newFileCache,0,fileCache,0,fid);
                    }
                    return null;
                }
            });
            if(doChangeContents!=null){
                runnables.addElement(doChangeContents);
                SwingUtilities.invokeLater(doChangeContents);
            }
        }

        public void cancelRunnables(Vector<DoChangeContents> runnables){
            for(DoChangeContents runnable : runnables){
                runnable.cancel();
            }
        }

        public void cancelRunnables(){
            cancelRunnables(runnables);
        }
    }

    class DoChangeContents implements Runnable{
        private List<File> addFiles;
        private List<File> remFiles;
        private boolean doFire=true;
        private int fid;
        private int addStart=0;
        private int remStart=0;

        public DoChangeContents(List<File> addFiles,int addStart,List<File> remFiles,int remStart,int fid){
            this.addFiles=addFiles;
            this.addStart=addStart;
            this.remFiles=remFiles;
            this.remStart=remStart;
            this.fid=fid;
        }

        synchronized void cancel(){
            doFire=false;
        }

        public synchronized void run(){
            if(fetchID==fid&&doFire){
                int remSize=(remFiles==null)?0:remFiles.size();
                int addSize=(addFiles==null)?0:addFiles.size();
                synchronized(fileCache){
                    if(remSize>0){
                        fileCache.removeAll(remFiles);
                    }
                    if(addSize>0){
                        fileCache.addAll(addStart,addFiles);
                    }
                    files=null;
                    directories=null;
                }
                if(remSize>0&&addSize==0){
                    fireIntervalRemoved(BasicDirectoryModel.this,remStart,remStart+remSize-1);
                }else if(addSize>0&&remSize==0&&addStart+addSize<=fileCache.size()){
                    fireIntervalAdded(BasicDirectoryModel.this,addStart,addStart+addSize-1);
                }else{
                    fireContentsChanged();
                }
            }
        }
    }
}
