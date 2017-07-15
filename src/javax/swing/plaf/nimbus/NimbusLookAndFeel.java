/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.nimbus;

import sun.security.action.GetPropertyAction;
import sun.swing.ImageIconUIResource;
import sun.swing.plaf.GTKKeybindings;
import sun.swing.plaf.WindowsKeybindings;
import sun.swing.plaf.synth.SynthIcon;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyle;
import javax.swing.plaf.synth.SynthStyleFactory;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.AccessController;
import java.util.HashMap;
import java.util.Map;

import static java.awt.BorderLayout.*;

public class NimbusLookAndFeel extends SynthLookAndFeel{
    private static final String[] COMPONENT_KEYS=new String[]{"ArrowButton","Button",
            "CheckBox","CheckBoxMenuItem","ColorChooser","ComboBox",
            "DesktopPane","DesktopIcon","EditorPane","FileChooser",
            "FormattedTextField","InternalFrame",
            "InternalFrameTitlePane","Label","List","Menu",
            "MenuBar","MenuItem","OptionPane","Panel",
            "PasswordField","PopupMenu","PopupMenuSeparator",
            "ProgressBar","RadioButton","RadioButtonMenuItem",
            "RootPane","ScrollBar","ScrollBarTrack","ScrollBarThumb",
            "ScrollPane","Separator","Slider","SliderTrack",
            "SliderThumb","Spinner","SplitPane","TabbedPane",
            "Table","TableHeader","TextArea","TextField","TextPane",
            "ToggleButton","ToolBar","ToolTip","Tree","Viewport"};
    private NimbusDefaults defaults;
    private UIDefaults uiDefaults;
    private DefaultsListener defaultsListener=new DefaultsListener();
    private Map<String,Map<String,Object>> compiledDefaults=null;
    private boolean defaultListenerAdded=false;

    public NimbusLookAndFeel(){
        super();
        defaults=new NimbusDefaults();
    }

    public static NimbusStyle getStyle(JComponent c,Region r){
        return (NimbusStyle)SynthLookAndFeel.getStyle(c,r);
    }

    static Object resolveToolbarConstraint(JToolBar toolbar){
        //NOTE: we don't worry about component orientation or PAGE_END etc
        //because the BasicToolBarUI always uses an absolute position of
        //NORTH/SOUTH/EAST/WEST.
        if(toolbar!=null){
            Container parent=toolbar.getParent();
            if(parent!=null){
                LayoutManager m=parent.getLayout();
                if(m instanceof BorderLayout){
                    BorderLayout b=(BorderLayout)m;
                    Object con=b.getConstraints(toolbar);
                    if(con==SOUTH||con==EAST||con==WEST){
                        return con;
                    }
                    return NORTH;
                }
            }
        }
        return NORTH;
    }

    @Override
    public void initialize(){
        super.initialize();
        defaults.initialize();
        // create synth style factory
        setStyleFactory(new SynthStyleFactory(){
            @Override
            public SynthStyle getStyle(JComponent c,Region r){
                return defaults.getStyle(c,r);
            }
        });
    }

    @Override
    public void uninitialize(){
        super.uninitialize();
        defaults.uninitialize();
        // clear all cached images to free memory
        ImageCache.getInstance().flush();
        UIManager.getDefaults().removePropertyChangeListener(defaultsListener);
    }

    @Override
    public UIDefaults getDefaults(){
        if(uiDefaults==null){
            // Detect platform
            String osName=getSystemProperty("os.name");
            boolean isWindows=osName!=null&&osName.contains("Windows");
            // We need to call super for basic's properties file.
            uiDefaults=super.getDefaults();
            defaults.initializeDefaults(uiDefaults);
            // Install Keybindings
            if(isWindows){
                WindowsKeybindings.installKeybindings(uiDefaults);
            }else{
                GTKKeybindings.installKeybindings(uiDefaults);
            }
            // Add Titled Border
            uiDefaults.put("TitledBorder.titlePosition",
                    TitledBorder.ABOVE_TOP);
            uiDefaults.put("TitledBorder.border",new BorderUIResource(
                    new LoweredBorder()));
            uiDefaults.put("TitledBorder.titleColor",
                    getDerivedColor("text",0.0f,0.0f,0.23f,0,true));
            uiDefaults.put("TitledBorder.font",
                    new NimbusDefaults.DerivedFont("defaultFont",
                            1f,true,null));
            // Choose Dialog button positions
            uiDefaults.put("OptionPane.isYesLast",!isWindows);
            // Store Table ScrollPane Corner Component
            uiDefaults.put("Table.scrollPaneCornerComponent",
                    new UIDefaults.ActiveValue(){
                        @Override
                        public Object createValue(UIDefaults table){
                            return new TableScrollPaneCorner();
                        }
                    });
            // Setup the settings for ToolBarSeparator which is custom
            // installed for Nimbus
            uiDefaults.put("ToolBarSeparator[Enabled].backgroundPainter",
                    new ToolBarSeparatorPainter());
            // Populate UIDefaults with a standard set of properties
            for(String componentKey : COMPONENT_KEYS){
                String key=componentKey+".foreground";
                if(!uiDefaults.containsKey(key)){
                    uiDefaults.put(key,
                            new NimbusProperty(componentKey,"textForeground"));
                }
                key=componentKey+".background";
                if(!uiDefaults.containsKey(key)){
                    uiDefaults.put(key,
                            new NimbusProperty(componentKey,"background"));
                }
                key=componentKey+".font";
                if(!uiDefaults.containsKey(key)){
                    uiDefaults.put(key,
                            new NimbusProperty(componentKey,"font"));
                }
                key=componentKey+".disabledText";
                if(!uiDefaults.containsKey(key)){
                    uiDefaults.put(key,
                            new NimbusProperty(componentKey,"Disabled",
                                    "textForeground"));
                }
                key=componentKey+".disabled";
                if(!uiDefaults.containsKey(key)){
                    uiDefaults.put(key,
                            new NimbusProperty(componentKey,"Disabled",
                                    "background"));
                }
            }
            // FileView icon keys are used by some applications, we don't have
            // a computer icon at the moment so using home icon for now
            uiDefaults.put("FileView.computerIcon",
                    new LinkProperty("FileChooser.homeFolderIcon"));
            uiDefaults.put("FileView.directoryIcon",
                    new LinkProperty("FileChooser.directoryIcon"));
            uiDefaults.put("FileView.fileIcon",
                    new LinkProperty("FileChooser.fileIcon"));
            uiDefaults.put("FileView.floppyDriveIcon",
                    new LinkProperty("FileChooser.floppyDriveIcon"));
            uiDefaults.put("FileView.hardDriveIcon",
                    new LinkProperty("FileChooser.hardDriveIcon"));
        }
        return uiDefaults;
    }

    @Override
    public String getDescription(){
        return "Nimbus Look and Feel";
    }

    @Override
    public String getName(){
        return "Nimbus";
    }

    @Override
    public String getID(){
        return "Nimbus";
    }

    @Override
    public boolean shouldUpdateStyleOnAncestorChanged(){
        return true;
    }

    @Override
    protected boolean shouldUpdateStyleOnEvent(PropertyChangeEvent ev){
        String eName=ev.getPropertyName();
        // These properties affect style cached inside NimbusDefaults (6860433)
        if("name"==eName||
                "ancestor"==eName||
                "Nimbus.Overrides"==eName||
                "Nimbus.Overrides.InheritDefaults"==eName||
                "JComponent.sizeVariant"==eName){
            JComponent c=(JComponent)ev.getSource();
            defaults.clearOverridesCache(c);
            return true;
        }
        return super.shouldUpdateStyleOnEvent(ev);
    }

    private String getSystemProperty(String key){
        return AccessController.doPrivileged(new GetPropertyAction(key));
    }

    public Color getDerivedColor(String uiDefaultParentName,
                                 float hOffset,float sOffset,
                                 float bOffset,int aOffset,
                                 boolean uiResource){
        return defaults.getDerivedColor(uiDefaultParentName,hOffset,sOffset,
                bOffset,aOffset,uiResource);
    }

    public void register(Region region,String prefix){
        defaults.register(region,prefix);
    }

    @Override
    public Icon getDisabledIcon(JComponent component,Icon icon){
        if(icon instanceof SynthIcon){
            SynthIcon si=(SynthIcon)icon;
            BufferedImage img=EffectUtils.createCompatibleTranslucentImage(
                    si.getIconWidth(),si.getIconHeight());
            Graphics2D gfx=img.createGraphics();
            si.paintIcon(component,gfx,0,0);
            gfx.dispose();
            return new ImageIconUIResource(GrayFilter.createDisabledImage(img));
        }else{
            return super.getDisabledIcon(component,icon);
        }
    }

    protected final Color getDerivedColor(Color color1,Color color2,
                                          float midPoint){
        return getDerivedColor(color1,color2,midPoint,true);
    }

    protected final Color getDerivedColor(Color color1,Color color2,
                                          float midPoint,boolean uiResource){
        int argb=deriveARGB(color1,color2,midPoint);
        if(uiResource){
            return new ColorUIResource(argb);
        }else{
            return new Color(argb);
        }
    }

    static int deriveARGB(Color color1,Color color2,float midPoint){
        int r=color1.getRed()+
                Math.round((color2.getRed()-color1.getRed())*midPoint);
        int g=color1.getGreen()+
                Math.round((color2.getGreen()-color1.getGreen())*midPoint);
        int b=color1.getBlue()+
                Math.round((color2.getBlue()-color1.getBlue())*midPoint);
        int a=color1.getAlpha()+
                Math.round((color2.getAlpha()-color1.getAlpha())*midPoint);
        return ((a&0xFF)<<24)|
                ((r&0xFF)<<16)|
                ((g&0xFF)<<8)|
                (b&0xFF);
    }

    Map<String,Object> getDefaultsForPrefix(String prefix){
        if(compiledDefaults==null){
            compiledDefaults=new HashMap<String,Map<String,Object>>();
            for(Map.Entry<Object,Object> entry : UIManager.getDefaults().entrySet()){
                if(entry.getKey() instanceof String){
                    addDefault((String)entry.getKey(),entry.getValue());
                }
            }
            if(!defaultListenerAdded){
                UIManager.getDefaults().addPropertyChangeListener(defaultsListener);
                defaultListenerAdded=true;
            }
        }
        return compiledDefaults.get(prefix);
    }

    private void addDefault(String key,Object value){
        if(compiledDefaults==null){
            return;
        }
        String prefix=parsePrefix(key);
        if(prefix!=null){
            Map<String,Object> keys=compiledDefaults.get(prefix);
            if(keys==null){
                keys=new HashMap<String,Object>();
                compiledDefaults.put(prefix,keys);
            }
            keys.put(key,value);
        }
    }

    static String parsePrefix(String key){
        if(key==null){
            return null;
        }
        boolean inquotes=false;
        for(int i=0;i<key.length();i++){
            char c=key.charAt(i);
            if(c=='"'){
                inquotes=!inquotes;
            }else if((c=='['||c=='.')&&!inquotes){
                return key.substring(0,i);
            }
        }
        return null;
    }

    private class LinkProperty implements UIDefaults.ActiveValue, UIResource{
        private String dstPropName;

        private LinkProperty(String dstPropName){
            this.dstPropName=dstPropName;
        }

        @Override
        public Object createValue(UIDefaults table){
            return UIManager.get(dstPropName);
        }
    }

    private class NimbusProperty implements UIDefaults.ActiveValue, UIResource{
        private String prefix;
        private String state=null;
        private String suffix;
        private boolean isFont;

        private NimbusProperty(String prefix,String state,String suffix){
            this(prefix,suffix);
            this.state=state;
        }

        private NimbusProperty(String prefix,String suffix){
            this.prefix=prefix;
            this.suffix=suffix;
            isFont="font".equals(suffix);
        }

        @Override
        public Object createValue(UIDefaults table){
            Object obj=null;
            // check specified state
            if(state!=null){
                obj=uiDefaults.get(prefix+"["+state+"]."+suffix);
            }
            // check enabled state
            if(obj==null){
                obj=uiDefaults.get(prefix+"[Enabled]."+suffix);
            }
            // check for defaults
            if(obj==null){
                if(isFont){
                    obj=uiDefaults.get("defaultFont");
                }else{
                    obj=uiDefaults.get(suffix);
                }
            }
            return obj;
        }
    }

    private class DefaultsListener implements PropertyChangeListener{
        @Override
        public void propertyChange(PropertyChangeEvent ev){
            String key=ev.getPropertyName();
            if("UIDefaults".equals(key)){
                compiledDefaults=null;
            }else{
                addDefault(key,ev.getNewValue());
            }
        }
    }
}
