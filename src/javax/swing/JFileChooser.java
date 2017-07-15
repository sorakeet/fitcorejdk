/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;
import javax.swing.plaf.FileChooserUI;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.util.Vector;

public class JFileChooser extends JComponent implements Accessible{
    // ************************
    // ***** Dialog Types *****
    // ************************
    public static final int OPEN_DIALOG=0;
    public static final int SAVE_DIALOG=1;
    public static final int CUSTOM_DIALOG=2;
    // ********************************
    // ***** Dialog Return Values *****
    // ********************************
    public static final int CANCEL_OPTION=1;
    public static final int APPROVE_OPTION=0;
    public static final int ERROR_OPTION=-1;
    // **********************************
    // ***** JFileChooser properties *****
    // **********************************
    public static final int FILES_ONLY=0;
    public static final int DIRECTORIES_ONLY=1;
    public static final int FILES_AND_DIRECTORIES=2;
    public static final String CANCEL_SELECTION="CancelSelection";
    public static final String APPROVE_SELECTION="ApproveSelection";
    public static final String APPROVE_BUTTON_TEXT_CHANGED_PROPERTY="ApproveButtonTextChangedProperty";
    public static final String APPROVE_BUTTON_TOOL_TIP_TEXT_CHANGED_PROPERTY="ApproveButtonToolTipTextChangedProperty";
    public static final String APPROVE_BUTTON_MNEMONIC_CHANGED_PROPERTY="ApproveButtonMnemonicChangedProperty";
    public static final String CONTROL_BUTTONS_ARE_SHOWN_CHANGED_PROPERTY="ControlButtonsAreShownChangedProperty";
    public static final String DIRECTORY_CHANGED_PROPERTY="directoryChanged";
    public static final String SELECTED_FILE_CHANGED_PROPERTY="SelectedFileChangedProperty";
    public static final String SELECTED_FILES_CHANGED_PROPERTY="SelectedFilesChangedProperty";
    public static final String MULTI_SELECTION_ENABLED_CHANGED_PROPERTY="MultiSelectionEnabledChangedProperty";
    public static final String FILE_SYSTEM_VIEW_CHANGED_PROPERTY="FileSystemViewChanged";
    public static final String FILE_VIEW_CHANGED_PROPERTY="fileViewChanged";
    public static final String FILE_HIDING_CHANGED_PROPERTY="FileHidingChanged";
    public static final String FILE_FILTER_CHANGED_PROPERTY="fileFilterChanged";
    public static final String FILE_SELECTION_MODE_CHANGED_PROPERTY="fileSelectionChanged";
    public static final String ACCESSORY_CHANGED_PROPERTY="AccessoryChangedProperty";
    public static final String ACCEPT_ALL_FILE_FILTER_USED_CHANGED_PROPERTY="acceptAllFileFilterUsedChanged";
    public static final String DIALOG_TITLE_CHANGED_PROPERTY="DialogTitleChangedProperty";
    public static final String DIALOG_TYPE_CHANGED_PROPERTY="DialogTypeChangedProperty";
    public static final String CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY="ChoosableFileFilterChangedProperty";
    private static final String uiClassID="FileChooserUI";
    private static final String SHOW_HIDDEN_PROP="awt.file.showHiddenFiles";
/////////////////
// Accessibility support
////////////////
    protected AccessibleContext accessibleContext=null;
    // ******************************
    // ***** instance variables *****
    // ******************************
    private String dialogTitle=null;
    private String approveButtonText=null;
    private String approveButtonToolTipText=null;
    private int approveButtonMnemonic=0;
    private Vector<FileFilter> filters=new Vector<FileFilter>(5);
    private JDialog dialog=null;
    private int dialogType=OPEN_DIALOG;
    private int returnValue=ERROR_OPTION;
    private JComponent accessory=null;
    private FileView fileView=null;
    private boolean controlsShown=true;
    private boolean useFileHiding=true;
    // Listens to changes in the native setting for showing hidden files.
    // The Listener is removed and the native setting is ignored if
    // setFileHidingEnabled() is ever called.
    private transient PropertyChangeListener showFilesListener=null;
    private int fileSelectionMode=FILES_ONLY;
    private boolean multiSelectionEnabled=false;
    private boolean useAcceptAllFileFilter=true;
    private boolean dragEnabled=false;
    private FileFilter fileFilter=null;
    private FileSystemView fileSystemView=null;
    private File currentDirectory=null;
    private File selectedFile=null;
    // *************************************
    // ***** JFileChooser Constructors *****
    // *************************************
    private File[] selectedFiles;

    public JFileChooser(){
        this((File)null,(FileSystemView)null);
    }

    public JFileChooser(String currentDirectoryPath){
        this(currentDirectoryPath,(FileSystemView)null);
    }

    public JFileChooser(File currentDirectory){
        this(currentDirectory,(FileSystemView)null);
    }

    public JFileChooser(FileSystemView fsv){
        this((File)null,fsv);
    }

    public JFileChooser(File currentDirectory,FileSystemView fsv){
        setup(fsv);
        setCurrentDirectory(currentDirectory);
    }

    public JFileChooser(String currentDirectoryPath,FileSystemView fsv){
        setup(fsv);
        if(currentDirectoryPath==null){
            setCurrentDirectory(null);
        }else{
            setCurrentDirectory(fileSystemView.createFileObject(currentDirectoryPath));
        }
    }

    protected void setup(FileSystemView view){
        installShowFilesListener();
        installHierarchyListener();
        if(view==null){
            view=FileSystemView.getFileSystemView();
        }
        setFileSystemView(view);
        updateUI();
        if(isAcceptAllFileFilterUsed()){
            setFileFilter(getAcceptAllFileFilter());
        }
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    }

    private void installHierarchyListener(){
        addHierarchyListener(new HierarchyListener(){
            @Override
            public void hierarchyChanged(HierarchyEvent e){
                if((e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)
                        ==HierarchyEvent.PARENT_CHANGED){
                    JFileChooser fc=JFileChooser.this;
                    JRootPane rootPane=SwingUtilities.getRootPane(fc);
                    if(rootPane!=null){
                        rootPane.setDefaultButton(fc.getUI().getDefaultButton(fc));
                    }
                }
            }
        });
    }

    public boolean getDragEnabled(){
        return dragEnabled;
    }

    public void setDragEnabled(boolean b){
        if(b&&GraphicsEnvironment.isHeadless()){
            throw new HeadlessException();
        }
        dragEnabled=b;
    }
    // *****************************
    // ****** File Operations ******
    // *****************************

    public File getSelectedFile(){
        return selectedFile;
    }

    public void setSelectedFile(File file){
        File oldValue=selectedFile;
        selectedFile=file;
        if(selectedFile!=null){
            if(file.isAbsolute()&&!getFileSystemView().isParent(getCurrentDirectory(),selectedFile)){
                setCurrentDirectory(selectedFile.getParentFile());
            }
            if(!isMultiSelectionEnabled()||selectedFiles==null||selectedFiles.length==1){
                ensureFileIsVisible(selectedFile);
            }
        }
        firePropertyChange(SELECTED_FILE_CHANGED_PROPERTY,oldValue,selectedFile);
    }

    public File[] getSelectedFiles(){
        if(selectedFiles==null){
            return new File[0];
        }else{
            return selectedFiles.clone();
        }
    }

    public void setSelectedFiles(File[] selectedFiles){
        File[] oldValue=this.selectedFiles;
        if(selectedFiles==null||selectedFiles.length==0){
            selectedFiles=null;
            this.selectedFiles=null;
            setSelectedFile(null);
        }else{
            this.selectedFiles=selectedFiles.clone();
            setSelectedFile(this.selectedFiles[0]);
        }
        firePropertyChange(SELECTED_FILES_CHANGED_PROPERTY,oldValue,selectedFiles);
    }

    public void changeToParentDirectory(){
        selectedFile=null;
        File oldValue=getCurrentDirectory();
        setCurrentDirectory(getFileSystemView().getParentDirectory(oldValue));
    }

    public File getCurrentDirectory(){
        return currentDirectory;
    }

    public void setCurrentDirectory(File dir){
        File oldValue=currentDirectory;
        if(dir!=null&&!dir.exists()){
            dir=currentDirectory;
        }
        if(dir==null){
            dir=getFileSystemView().getDefaultDirectory();
        }
        if(currentDirectory!=null){
            /** Verify the toString of object */
            if(this.currentDirectory.equals(dir)){
                return;
            }
        }
        File prev=null;
        while(!isTraversable(dir)&&prev!=dir){
            prev=dir;
            dir=getFileSystemView().getParentDirectory(dir);
        }
        currentDirectory=dir;
        firePropertyChange(DIRECTORY_CHANGED_PROPERTY,oldValue,currentDirectory);
    }

    public boolean isTraversable(File f){
        Boolean traversable=null;
        if(f!=null){
            if(getFileView()!=null){
                traversable=getFileView().isTraversable(f);
            }
            FileView uiFileView=getUI().getFileView(this);
            if(traversable==null&&uiFileView!=null){
                traversable=uiFileView.isTraversable(f);
            }
            if(traversable==null){
                traversable=getFileSystemView().isTraversable(f);
            }
        }
        return (traversable!=null&&traversable.booleanValue());
    }

    public FileView getFileView(){
        return fileView;
    }
    // **************************************
    // ***** JFileChooser Dialog methods *****
    // **************************************

    public void setFileView(FileView fileView){
        FileView oldValue=this.fileView;
        this.fileView=fileView;
        firePropertyChange(FILE_VIEW_CHANGED_PROPERTY,oldValue,fileView);
    }

    public FileChooserUI getUI(){
        return (FileChooserUI)ui;
    }

    public FileSystemView getFileSystemView(){
        return fileSystemView;
    }

    public void setFileSystemView(FileSystemView fsv){
        FileSystemView oldValue=fileSystemView;
        fileSystemView=fsv;
        firePropertyChange(FILE_SYSTEM_VIEW_CHANGED_PROPERTY,oldValue,fileSystemView);
    }
    // **************************
    // ***** Dialog Options *****
    // **************************

    public void ensureFileIsVisible(File f){
        getUI().ensureFileIsVisible(this,f);
    }

    public int showOpenDialog(Component parent) throws HeadlessException{
        setDialogType(OPEN_DIALOG);
        return showDialog(parent,null);
    }

    public int showDialog(Component parent,String approveButtonText)
            throws HeadlessException{
        if(dialog!=null){
            // Prevent to show second instance of dialog if the previous one still exists
            return JFileChooser.ERROR_OPTION;
        }
        if(approveButtonText!=null){
            setApproveButtonText(approveButtonText);
            setDialogType(CUSTOM_DIALOG);
        }
        dialog=createDialog(parent);
        dialog.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                returnValue=CANCEL_OPTION;
            }
        });
        returnValue=ERROR_OPTION;
        rescanCurrentDirectory();
        dialog.show();
        firePropertyChange("JFileChooserDialogIsClosingProperty",dialog,null);
        // Remove all components from dialog. The MetalFileChooserUI.installUI() method (and other LAFs)
        // registers AWT listener for dialogs and produces memory leaks. It happens when
        // installUI invoked after the showDialog method.
        dialog.getContentPane().removeAll();
        dialog.dispose();
        dialog=null;
        return returnValue;
    }

    public void rescanCurrentDirectory(){
        getUI().rescanCurrentDirectory(this);
    }

    protected JDialog createDialog(Component parent) throws HeadlessException{
        FileChooserUI ui=getUI();
        String title=ui.getDialogTitle(this);
        putClientProperty(AccessibleContext.ACCESSIBLE_DESCRIPTION_PROPERTY,
                title);
        JDialog dialog;
        Window window=JOptionPane.getWindowForComponent(parent);
        if(window instanceof Frame){
            dialog=new JDialog((Frame)window,title,true);
        }else{
            dialog=new JDialog((Dialog)window,title,true);
        }
        dialog.setComponentOrientation(this.getComponentOrientation());
        Container contentPane=dialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(this,BorderLayout.CENTER);
        if(JDialog.isDefaultLookAndFeelDecorated()){
            boolean supportsWindowDecorations=
                    UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if(supportsWindowDecorations){
                dialog.getRootPane().setWindowDecorationStyle(JRootPane.FILE_CHOOSER_DIALOG);
            }
        }
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        return dialog;
    }

    public int showSaveDialog(Component parent) throws HeadlessException{
        setDialogType(SAVE_DIALOG);
        return showDialog(parent,null);
    }
    // ************************************
    // ***** JFileChooser View Options *****
    // ************************************

    public boolean getControlButtonsAreShown(){
        return controlsShown;
    }

    public void setControlButtonsAreShown(boolean b){
        if(controlsShown==b){
            return;
        }
        boolean oldValue=controlsShown;
        controlsShown=b;
        firePropertyChange(CONTROL_BUTTONS_ARE_SHOWN_CHANGED_PROPERTY,oldValue,controlsShown);
    }

    public int getDialogType(){
        return dialogType;
    }

    // PENDING(jeff) - fire button text change property
    public void setDialogType(int dialogType){
        if(this.dialogType==dialogType){
            return;
        }
        if(!(dialogType==OPEN_DIALOG||dialogType==SAVE_DIALOG||dialogType==CUSTOM_DIALOG)){
            throw new IllegalArgumentException("Incorrect Dialog Type: "+dialogType);
        }
        int oldValue=this.dialogType;
        this.dialogType=dialogType;
        if(dialogType==OPEN_DIALOG||dialogType==SAVE_DIALOG){
            setApproveButtonText(null);
        }
        firePropertyChange(DIALOG_TYPE_CHANGED_PROPERTY,oldValue,dialogType);
    }

    public String getDialogTitle(){
        return dialogTitle;
    }

    public void setDialogTitle(String dialogTitle){
        String oldValue=this.dialogTitle;
        this.dialogTitle=dialogTitle;
        if(dialog!=null){
            dialog.setTitle(dialogTitle);
        }
        firePropertyChange(DIALOG_TITLE_CHANGED_PROPERTY,oldValue,dialogTitle);
    }

    public String getApproveButtonToolTipText(){
        return approveButtonToolTipText;
    }

    public void setApproveButtonToolTipText(String toolTipText){
        if(approveButtonToolTipText==toolTipText){
            return;
        }
        String oldValue=approveButtonToolTipText;
        approveButtonToolTipText=toolTipText;
        firePropertyChange(APPROVE_BUTTON_TOOL_TIP_TEXT_CHANGED_PROPERTY,oldValue,approveButtonToolTipText);
    }

    public int getApproveButtonMnemonic(){
        return approveButtonMnemonic;
    }

    public void setApproveButtonMnemonic(char mnemonic){
        int vk=(int)mnemonic;
        if(vk>='a'&&vk<='z'){
            vk-=('a'-'A');
        }
        setApproveButtonMnemonic(vk);
    }

    public void setApproveButtonMnemonic(int mnemonic){
        if(approveButtonMnemonic==mnemonic){
            return;
        }
        int oldValue=approveButtonMnemonic;
        approveButtonMnemonic=mnemonic;
        firePropertyChange(APPROVE_BUTTON_MNEMONIC_CHANGED_PROPERTY,oldValue,approveButtonMnemonic);
    }

    public String getApproveButtonText(){
        return approveButtonText;
    }

    // PENDING(jeff) - have ui set this on dialog type change
    public void setApproveButtonText(String approveButtonText){
        if(this.approveButtonText==approveButtonText){
            return;
        }
        String oldValue=this.approveButtonText;
        this.approveButtonText=approveButtonText;
        firePropertyChange(APPROVE_BUTTON_TEXT_CHANGED_PROPERTY,oldValue,approveButtonText);
    }

    public FileFilter[] getChoosableFileFilters(){
        FileFilter[] filterArray=new FileFilter[filters.size()];
        filters.copyInto(filterArray);
        return filterArray;
    }

    public void addChoosableFileFilter(FileFilter filter){
        if(filter!=null&&!filters.contains(filter)){
            FileFilter[] oldValue=getChoosableFileFilters();
            filters.addElement(filter);
            firePropertyChange(CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY,oldValue,getChoosableFileFilters());
            if(fileFilter==null&&filters.size()==1){
                setFileFilter(filter);
            }
        }
    }

    public boolean removeChoosableFileFilter(FileFilter f){
        int index=filters.indexOf(f);
        if(index>=0){
            if(getFileFilter()==f){
                FileFilter aaff=getAcceptAllFileFilter();
                if(isAcceptAllFileFilterUsed()&&(aaff!=f)){
                    // choose default filter if it is used
                    setFileFilter(aaff);
                }else if(index>0){
                    // choose the first filter, because it is not removed
                    setFileFilter(filters.get(0));
                }else if(filters.size()>1){
                    // choose the second filter, because the first one is removed
                    setFileFilter(filters.get(1));
                }else{
                    // no more filters
                    setFileFilter(null);
                }
            }
            FileFilter[] oldValue=getChoosableFileFilters();
            filters.removeElement(f);
            firePropertyChange(CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY,oldValue,getChoosableFileFilters());
            return true;
        }else{
            return false;
        }
    }

    public void resetChoosableFileFilters(){
        FileFilter[] oldValue=getChoosableFileFilters();
        setFileFilter(null);
        filters.removeAllElements();
        if(isAcceptAllFileFilterUsed()){
            addChoosableFileFilter(getAcceptAllFileFilter());
        }
        firePropertyChange(CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY,oldValue,getChoosableFileFilters());
    }

    public FileFilter getAcceptAllFileFilter(){
        FileFilter filter=null;
        if(getUI()!=null){
            filter=getUI().getAcceptAllFileFilter(this);
        }
        return filter;
    }

    public boolean isAcceptAllFileFilterUsed(){
        return useAcceptAllFileFilter;
    }

    public void setAcceptAllFileFilterUsed(boolean b){
        boolean oldValue=useAcceptAllFileFilter;
        useAcceptAllFileFilter=b;
        if(!b){
            removeChoosableFileFilter(getAcceptAllFileFilter());
        }else{
            removeChoosableFileFilter(getAcceptAllFileFilter());
            addChoosableFileFilter(getAcceptAllFileFilter());
        }
        firePropertyChange(ACCEPT_ALL_FILE_FILTER_USED_CHANGED_PROPERTY,oldValue,useAcceptAllFileFilter);
    }

    public JComponent getAccessory(){
        return accessory;
    }

    public void setAccessory(JComponent newAccessory){
        JComponent oldValue=accessory;
        accessory=newAccessory;
        firePropertyChange(ACCESSORY_CHANGED_PROPERTY,oldValue,accessory);
    }

    public int getFileSelectionMode(){
        return fileSelectionMode;
    }

    public void setFileSelectionMode(int mode){
        if(fileSelectionMode==mode){
            return;
        }
        if((mode==FILES_ONLY)||(mode==DIRECTORIES_ONLY)||(mode==FILES_AND_DIRECTORIES)){
            int oldValue=fileSelectionMode;
            fileSelectionMode=mode;
            firePropertyChange(FILE_SELECTION_MODE_CHANGED_PROPERTY,oldValue,fileSelectionMode);
        }else{
            throw new IllegalArgumentException("Incorrect Mode for file selection: "+mode);
        }
    }

    public boolean isFileSelectionEnabled(){
        return ((fileSelectionMode==FILES_ONLY)||(fileSelectionMode==FILES_AND_DIRECTORIES));
    }

    public boolean isDirectorySelectionEnabled(){
        return ((fileSelectionMode==DIRECTORIES_ONLY)||(fileSelectionMode==FILES_AND_DIRECTORIES));
    }

    public boolean isMultiSelectionEnabled(){
        return multiSelectionEnabled;
    }

    public void setMultiSelectionEnabled(boolean b){
        if(multiSelectionEnabled==b){
            return;
        }
        boolean oldValue=multiSelectionEnabled;
        multiSelectionEnabled=b;
        firePropertyChange(MULTI_SELECTION_ENABLED_CHANGED_PROPERTY,oldValue,multiSelectionEnabled);
    }
    // ******************************
    // *****FileView delegation *****
    // ******************************
    // NOTE: all of the following methods attempt to delegate
    // first to the client set fileView, and if <code>null</code> is returned
    // (or there is now client defined fileView) then calls the
    // UI's default fileView.

    public boolean isFileHidingEnabled(){
        return useFileHiding;
    }

    public void setFileHidingEnabled(boolean b){
        // Dump showFilesListener since we'll ignore it from now on
        if(showFilesListener!=null){
            Toolkit.getDefaultToolkit().removePropertyChangeListener(SHOW_HIDDEN_PROP,showFilesListener);
            showFilesListener=null;
        }
        boolean oldValue=useFileHiding;
        useFileHiding=b;
        firePropertyChange(FILE_HIDING_CHANGED_PROPERTY,oldValue,useFileHiding);
    }

    public FileFilter getFileFilter(){
        return fileFilter;
    }

    public void setFileFilter(FileFilter filter){
        FileFilter oldValue=fileFilter;
        fileFilter=filter;
        if(filter!=null){
            if(isMultiSelectionEnabled()&&selectedFiles!=null&&selectedFiles.length>0){
                Vector<File> fList=new Vector<File>();
                boolean failed=false;
                for(File file : selectedFiles){
                    if(filter.accept(file)){
                        fList.add(file);
                    }else{
                        failed=true;
                    }
                }
                if(failed){
                    setSelectedFiles((fList.size()==0)?null:fList.toArray(new File[fList.size()]));
                }
            }else if(selectedFile!=null&&!filter.accept(selectedFile)){
                setSelectedFile(null);
            }
        }
        firePropertyChange(FILE_FILTER_CHANGED_PROPERTY,oldValue,fileFilter);
    }

    public String getName(File f){
        String filename=null;
        if(f!=null){
            if(getFileView()!=null){
                filename=getFileView().getName(f);
            }
            FileView uiFileView=getUI().getFileView(this);
            if(filename==null&&uiFileView!=null){
                filename=uiFileView.getName(f);
            }
        }
        return filename;
    }

    public String getDescription(File f){
        String description=null;
        if(f!=null){
            if(getFileView()!=null){
                description=getFileView().getDescription(f);
            }
            FileView uiFileView=getUI().getFileView(this);
            if(description==null&&uiFileView!=null){
                description=uiFileView.getDescription(f);
            }
        }
        return description;
    }

    public String getTypeDescription(File f){
        String typeDescription=null;
        if(f!=null){
            if(getFileView()!=null){
                typeDescription=getFileView().getTypeDescription(f);
            }
            FileView uiFileView=getUI().getFileView(this);
            if(typeDescription==null&&uiFileView!=null){
                typeDescription=uiFileView.getTypeDescription(f);
            }
        }
        return typeDescription;
    }

    public Icon getIcon(File f){
        Icon icon=null;
        if(f!=null){
            if(getFileView()!=null){
                icon=getFileView().getIcon(f);
            }
            FileView uiFileView=getUI().getFileView(this);
            if(icon==null&&uiFileView!=null){
                icon=uiFileView.getIcon(f);
            }
        }
        return icon;
    }
    // **************************
    // ***** Event Handling *****
    // **************************

    public boolean accept(File f){
        boolean shown=true;
        if(f!=null&&fileFilter!=null){
            shown=fileFilter.accept(f);
        }
        return shown;
    }

    public void approveSelection(){
        returnValue=APPROVE_OPTION;
        if(dialog!=null){
            dialog.setVisible(false);
        }
        fireActionPerformed(APPROVE_SELECTION);
    }

    protected void fireActionPerformed(String command){
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        long mostRecentEventTime=EventQueue.getMostRecentEventTime();
        int modifiers=0;
        AWTEvent currentEvent=EventQueue.getCurrentEvent();
        if(currentEvent instanceof InputEvent){
            modifiers=((InputEvent)currentEvent).getModifiers();
        }else if(currentEvent instanceof ActionEvent){
            modifiers=((ActionEvent)currentEvent).getModifiers();
        }
        ActionEvent e=null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==ActionListener.class){
                // Lazily create the event:
                if(e==null){
                    e=new ActionEvent(this,ActionEvent.ACTION_PERFORMED,
                            command,mostRecentEventTime,
                            modifiers);
                }
                ((ActionListener)listeners[i+1]).actionPerformed(e);
            }
        }
    }

    public void cancelSelection(){
        returnValue=CANCEL_OPTION;
        if(dialog!=null){
            dialog.setVisible(false);
        }
        fireActionPerformed(CANCEL_SELECTION);
    }

    public void addActionListener(ActionListener l){
        listenerList.add(ActionListener.class,l);
    }

    public void removeActionListener(ActionListener l){
        listenerList.remove(ActionListener.class,l);
    }

    public ActionListener[] getActionListeners(){
        return listenerList.getListeners(ActionListener.class);
    }
    // *********************************
    // ***** Pluggable L&F methods *****
    // *********************************

    public void updateUI(){
        if(isAcceptAllFileFilterUsed()){
            removeChoosableFileFilter(getAcceptAllFileFilter());
        }
        FileChooserUI ui=((FileChooserUI)UIManager.getUI(this));
        if(fileSystemView==null){
            // We were probably deserialized
            setFileSystemView(FileSystemView.getFileSystemView());
        }
        setUI(ui);
        if(isAcceptAllFileFilterUsed()){
            addChoosableFileFilter(getAcceptAllFileFilter());
        }
    }

    public String getUIClassID(){
        return uiClassID;
    }

    protected String paramString(){
        String approveButtonTextString=(approveButtonText!=null?
                approveButtonText:"");
        String dialogTitleString=(dialogTitle!=null?
                dialogTitle:"");
        String dialogTypeString;
        if(dialogType==OPEN_DIALOG){
            dialogTypeString="OPEN_DIALOG";
        }else if(dialogType==SAVE_DIALOG){
            dialogTypeString="SAVE_DIALOG";
        }else if(dialogType==CUSTOM_DIALOG){
            dialogTypeString="CUSTOM_DIALOG";
        }else dialogTypeString="";
        String returnValueString;
        if(returnValue==CANCEL_OPTION){
            returnValueString="CANCEL_OPTION";
        }else if(returnValue==APPROVE_OPTION){
            returnValueString="APPROVE_OPTION";
        }else if(returnValue==ERROR_OPTION){
            returnValueString="ERROR_OPTION";
        }else returnValueString="";
        String useFileHidingString=(useFileHiding?
                "true":"false");
        String fileSelectionModeString;
        if(fileSelectionMode==FILES_ONLY){
            fileSelectionModeString="FILES_ONLY";
        }else if(fileSelectionMode==DIRECTORIES_ONLY){
            fileSelectionModeString="DIRECTORIES_ONLY";
        }else if(fileSelectionMode==FILES_AND_DIRECTORIES){
            fileSelectionModeString="FILES_AND_DIRECTORIES";
        }else fileSelectionModeString="";
        String currentDirectoryString=(currentDirectory!=null?
                currentDirectory.toString():"");
        String selectedFileString=(selectedFile!=null?
                selectedFile.toString():"");
        return super.paramString()+
                ",approveButtonText="+approveButtonTextString+
                ",currentDirectory="+currentDirectoryString+
                ",dialogTitle="+dialogTitleString+
                ",dialogType="+dialogTypeString+
                ",fileSelectionMode="+fileSelectionModeString+
                ",returnValue="+returnValueString+
                ",selectedFile="+selectedFileString+
                ",useFileHiding="+useFileHidingString;
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        in.defaultReadObject();
        installShowFilesListener();
    }

    private void installShowFilesListener(){
        // Track native setting for showing hidden files
        Toolkit tk=Toolkit.getDefaultToolkit();
        Object showHiddenProperty=tk.getDesktopProperty(SHOW_HIDDEN_PROP);
        if(showHiddenProperty instanceof Boolean){
            useFileHiding=!((Boolean)showHiddenProperty).booleanValue();
            showFilesListener=new WeakPCL(this);
            tk.addPropertyChangeListener(SHOW_HIDDEN_PROP,showFilesListener);
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException{
        FileSystemView fsv=null;
        if(isAcceptAllFileFilterUsed()){
            //The AcceptAllFileFilter is UI specific, it will be reset by
            //updateUI() after deserialization
            removeChoosableFileFilter(getAcceptAllFileFilter());
        }
        if(fileSystemView.equals(FileSystemView.getFileSystemView())){
            //The default FileSystemView is platform specific, it will be
            //reset by updateUI() after deserialization
            fsv=fileSystemView;
            fileSystemView=null;
        }
        s.defaultWriteObject();
        if(fsv!=null){
            fileSystemView=fsv;
        }
        if(isAcceptAllFileFilterUsed()){
            addChoosableFileFilter(getAcceptAllFileFilter());
        }
        if(getUIClassID().equals(uiClassID)){
            byte count=JComponent.getWriteObjCounter(this);
            JComponent.setWriteObjCounter(this,--count);
            if(count==0&&ui!=null){
                ui.installUI(this);
            }
        }
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJFileChooser();
        }
        return accessibleContext;
    }

    private static class WeakPCL implements PropertyChangeListener{
        WeakReference<JFileChooser> jfcRef;

        public WeakPCL(JFileChooser jfc){
            jfcRef=new WeakReference<JFileChooser>(jfc);
        }

        public void propertyChange(PropertyChangeEvent ev){
            assert ev.getPropertyName().equals(SHOW_HIDDEN_PROP);
            JFileChooser jfc=jfcRef.get();
            if(jfc==null){
                // Our JFileChooser is no longer around, so we no longer need to
                // listen for PropertyChangeEvents.
                Toolkit.getDefaultToolkit().removePropertyChangeListener(SHOW_HIDDEN_PROP,this);
            }else{
                boolean oldValue=jfc.useFileHiding;
                jfc.useFileHiding=!((Boolean)ev.getNewValue()).booleanValue();
                jfc.firePropertyChange(FILE_HIDING_CHANGED_PROPERTY,oldValue,jfc.useFileHiding);
            }
        }
    }

    protected class AccessibleJFileChooser extends AccessibleJComponent{
        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.FILE_CHOOSER;
        }
    } // inner class AccessibleJFileChooser
}
