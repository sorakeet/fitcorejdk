/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import sun.swing.SwingUtilities2;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorChooserComponentFactory;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.colorchooser.DefaultColorSelectionModel;
import javax.swing.plaf.ColorChooserUI;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Locale;

public class JColorChooser extends JComponent implements Accessible{
    public static final String SELECTION_MODEL_PROPERTY="selectionModel";
    public static final String PREVIEW_PANEL_PROPERTY="previewPanel";
    public static final String CHOOSER_PANELS_PROPERTY="chooserPanels";
    private static final String uiClassID="ColorChooserUI";
/////////////////
// Accessibility support
////////////////
    protected AccessibleContext accessibleContext=null;
    private ColorSelectionModel selectionModel;
    private JComponent previewPanel=ColorChooserComponentFactory.getPreviewPanel();
    private AbstractColorChooserPanel[] chooserPanels=new AbstractColorChooserPanel[0];
    private boolean dragEnabled;

    public JColorChooser(){
        this(Color.white);
    }

    public JColorChooser(Color initialColor){
        this(new DefaultColorSelectionModel(initialColor));
    }

    public JColorChooser(ColorSelectionModel model){
        selectionModel=model;
        updateUI();
        dragEnabled=false;
    }

    public void updateUI(){
        setUI((ColorChooserUI)UIManager.getUI(this));
    }

    public String getUIClassID(){
        return uiClassID;
    }

    protected String paramString(){
        StringBuffer chooserPanelsString=new StringBuffer("");
        for(int i=0;i<chooserPanels.length;i++){
            chooserPanelsString.append("["+chooserPanels[i].toString()
                    +"]");
        }
        String previewPanelString=(previewPanel!=null?
                previewPanel.toString():"");
        return super.paramString()+
                ",chooserPanels="+chooserPanelsString.toString()+
                ",previewPanel="+previewPanelString;
    }

    public static Color showDialog(Component component,
                                   String title,Color initialColor) throws HeadlessException{
        final JColorChooser pane=new JColorChooser(initialColor!=null?
                initialColor:Color.white);
        ColorTracker ok=new ColorTracker(pane);
        JDialog dialog=createDialog(component,title,true,pane,ok,null);
        dialog.addComponentListener(new ColorChooserDialog.DisposeOnClose());
        dialog.show(); // blocks until user brings dialog down...
        return ok.getColor();
    }

    public static JDialog createDialog(Component c,String title,boolean modal,
                                       JColorChooser chooserPane,ActionListener okListener,
                                       ActionListener cancelListener) throws HeadlessException{
        Window window=JOptionPane.getWindowForComponent(c);
        ColorChooserDialog dialog;
        if(window instanceof Frame){
            dialog=new ColorChooserDialog((Frame)window,title,modal,c,chooserPane,
                    okListener,cancelListener);
        }else{
            dialog=new ColorChooserDialog((Dialog)window,title,modal,c,chooserPane,
                    okListener,cancelListener);
        }
        dialog.getAccessibleContext().setAccessibleDescription(title);
        return dialog;
    }

    public ColorChooserUI getUI(){
        return (ColorChooserUI)ui;
    }

    public void setUI(ColorChooserUI ui){
        super.setUI(ui);
    }

    public Color getColor(){
        return selectionModel.getSelectedColor();
    }

    public void setColor(int c){
        setColor((c>>16)&0xFF,(c>>8)&0xFF,c&0xFF);
    }

    public void setColor(int r,int g,int b){
        setColor(new Color(r,g,b));
    }

    public void setColor(Color color){
        selectionModel.setSelectedColor(color);
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

    public JComponent getPreviewPanel(){
        return previewPanel;
    }

    public void setPreviewPanel(JComponent preview){
        if(previewPanel!=preview){
            JComponent oldPreview=previewPanel;
            previewPanel=preview;
            firePropertyChange(JColorChooser.PREVIEW_PANEL_PROPERTY,oldPreview,preview);
        }
    }

    public void addChooserPanel(AbstractColorChooserPanel panel){
        AbstractColorChooserPanel[] oldPanels=getChooserPanels();
        AbstractColorChooserPanel[] newPanels=new AbstractColorChooserPanel[oldPanels.length+1];
        System.arraycopy(oldPanels,0,newPanels,0,oldPanels.length);
        newPanels[newPanels.length-1]=panel;
        setChooserPanels(newPanels);
    }

    public AbstractColorChooserPanel[] getChooserPanels(){
        return chooserPanels;
    }

    public void setChooserPanels(AbstractColorChooserPanel[] panels){
        AbstractColorChooserPanel[] oldValue=chooserPanels;
        chooserPanels=panels;
        firePropertyChange(CHOOSER_PANELS_PROPERTY,oldValue,panels);
    }

    public AbstractColorChooserPanel removeChooserPanel(AbstractColorChooserPanel panel){
        int containedAt=-1;
        for(int i=0;i<chooserPanels.length;i++){
            if(chooserPanels[i]==panel){
                containedAt=i;
                break;
            }
        }
        if(containedAt==-1){
            throw new IllegalArgumentException("chooser panel not in this chooser");
        }
        AbstractColorChooserPanel[] newArray=new AbstractColorChooserPanel[chooserPanels.length-1];
        if(containedAt==chooserPanels.length-1){  // at end
            System.arraycopy(chooserPanels,0,newArray,0,newArray.length);
        }else if(containedAt==0){  // at start
            System.arraycopy(chooserPanels,1,newArray,0,newArray.length);
        }else{  // in middle
            System.arraycopy(chooserPanels,0,newArray,0,containedAt);
            System.arraycopy(chooserPanels,containedAt+1,
                    newArray,containedAt,(chooserPanels.length-containedAt-1));
        }
        setChooserPanels(newArray);
        return panel;
    }

    public ColorSelectionModel getSelectionModel(){
        return selectionModel;
    }

    public void setSelectionModel(ColorSelectionModel newModel){
        ColorSelectionModel oldModel=selectionModel;
        selectionModel=newModel;
        firePropertyChange(JColorChooser.SELECTION_MODEL_PROPERTY,oldModel,newModel);
    }

    private void writeObject(ObjectOutputStream s) throws IOException{
        s.defaultWriteObject();
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
            accessibleContext=new AccessibleJColorChooser();
        }
        return accessibleContext;
    }

    protected class AccessibleJColorChooser extends AccessibleJComponent{
        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.COLOR_CHOOSER;
        }
    } // inner class AccessibleJColorChooser
}

class ColorChooserDialog extends JDialog{
    private Color initialColor;
    private JColorChooser chooserPane;
    private JButton cancelButton;

    public ColorChooserDialog(Dialog owner,String title,boolean modal,
                              Component c,JColorChooser chooserPane,
                              ActionListener okListener,ActionListener cancelListener)
            throws HeadlessException{
        super(owner,title,modal);
        initColorChooserDialog(c,chooserPane,okListener,cancelListener);
    }

    protected void initColorChooserDialog(Component c,JColorChooser chooserPane,
                                          ActionListener okListener,ActionListener cancelListener){
        //setResizable(false);
        this.chooserPane=chooserPane;
        Locale locale=getLocale();
        String okString=UIManager.getString("ColorChooser.okText",locale);
        String cancelString=UIManager.getString("ColorChooser.cancelText",locale);
        String resetString=UIManager.getString("ColorChooser.resetText",locale);
        Container contentPane=getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(chooserPane,BorderLayout.CENTER);
        /**
         * Create Lower button panel
         */
        JPanel buttonPane=new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton okButton=new JButton(okString);
        getRootPane().setDefaultButton(okButton);
        okButton.getAccessibleContext().setAccessibleDescription(okString);
        okButton.setActionCommand("OK");
        okButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                hide();
            }
        });
        if(okListener!=null){
            okButton.addActionListener(okListener);
        }
        buttonPane.add(okButton);
        cancelButton=new JButton(cancelString);
        cancelButton.getAccessibleContext().setAccessibleDescription(cancelString);
        // The following few lines are used to register esc to close the dialog
        Action cancelKeyAction=new AbstractAction(){
            public void actionPerformed(ActionEvent e){
                ((AbstractButton)e.getSource()).fireActionPerformed(e);
            }
        };
        KeyStroke cancelKeyStroke=KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0);
        InputMap inputMap=cancelButton.getInputMap(JComponent.
                WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap=cancelButton.getActionMap();
        if(inputMap!=null&&actionMap!=null){
            inputMap.put(cancelKeyStroke,"cancel");
            actionMap.put("cancel",cancelKeyAction);
        }
        // end esc handling
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                hide();
            }
        });
        if(cancelListener!=null){
            cancelButton.addActionListener(cancelListener);
        }
        buttonPane.add(cancelButton);
        JButton resetButton=new JButton(resetString);
        resetButton.getAccessibleContext().setAccessibleDescription(resetString);
        resetButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                reset();
            }
        });
        int mnemonic=SwingUtilities2.getUIDefaultsInt("ColorChooser.resetMnemonic",locale,-1);
        if(mnemonic!=-1){
            resetButton.setMnemonic(mnemonic);
        }
        buttonPane.add(resetButton);
        contentPane.add(buttonPane,BorderLayout.SOUTH);
        if(JDialog.isDefaultLookAndFeelDecorated()){
            boolean supportsWindowDecorations=
                    UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if(supportsWindowDecorations){
                getRootPane().setWindowDecorationStyle(JRootPane.COLOR_CHOOSER_DIALOG);
            }
        }
        applyComponentOrientation(((c==null)?getRootPane():c).getComponentOrientation());
        pack();
        setLocationRelativeTo(c);
        this.addWindowListener(new Closer());
    }

    public void reset(){
        chooserPane.setColor(initialColor);
    }

    public ColorChooserDialog(Frame owner,String title,boolean modal,
                              Component c,JColorChooser chooserPane,
                              ActionListener okListener,ActionListener cancelListener)
            throws HeadlessException{
        super(owner,title,modal);
        initColorChooserDialog(c,chooserPane,okListener,cancelListener);
    }

    public void show(){
        initialColor=chooserPane.getColor();
        super.show();
    }

    static class DisposeOnClose extends ComponentAdapter implements Serializable{
        public void componentHidden(ComponentEvent e){
            Window w=(Window)e.getComponent();
            w.dispose();
        }
    }

    class Closer extends WindowAdapter implements Serializable{
        public void windowClosing(WindowEvent e){
            cancelButton.doClick(0);
            Window w=e.getWindow();
            w.hide();
        }
    }
}

class ColorTracker implements ActionListener, Serializable{
    JColorChooser chooser;
    Color color;

    public ColorTracker(JColorChooser c){
        chooser=c;
    }

    public Color getColor(){
        return color;
    }    public void actionPerformed(ActionEvent e){
        color=chooser.getColor();
    }


}
