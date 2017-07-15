/**
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.synth;

import sun.awt.AppContext;

import javax.swing.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Region{
    public static final Region ARROW_BUTTON=new Region("ArrowButton",false);
    public static final Region BUTTON=new Region("Button",false);
    public static final Region CHECK_BOX=new Region("CheckBox",false);
    public static final Region CHECK_BOX_MENU_ITEM=new Region("CheckBoxMenuItem",false);
    public static final Region COLOR_CHOOSER=new Region("ColorChooser",false);
    public static final Region COMBO_BOX=new Region("ComboBox",false);
    public static final Region DESKTOP_PANE=new Region("DesktopPane",false);
    public static final Region DESKTOP_ICON=new Region("DesktopIcon",false);
    public static final Region EDITOR_PANE=new Region("EditorPane",false);
    public static final Region FILE_CHOOSER=new Region("FileChooser",false);
    public static final Region FORMATTED_TEXT_FIELD=new Region("FormattedTextField",false);
    public static final Region INTERNAL_FRAME=new Region("InternalFrame",false);
    public static final Region INTERNAL_FRAME_TITLE_PANE=new Region("InternalFrameTitlePane",false);
    public static final Region LABEL=new Region("Label",false);
    public static final Region LIST=new Region("List",false);
    public static final Region MENU=new Region("Menu",false);
    public static final Region MENU_BAR=new Region("MenuBar",false);
    public static final Region MENU_ITEM=new Region("MenuItem",false);
    public static final Region MENU_ITEM_ACCELERATOR=new Region("MenuItemAccelerator",true);
    public static final Region OPTION_PANE=new Region("OptionPane",false);
    public static final Region PANEL=new Region("Panel",false);
    public static final Region PASSWORD_FIELD=new Region("PasswordField",false);
    public static final Region POPUP_MENU=new Region("PopupMenu",false);
    public static final Region POPUP_MENU_SEPARATOR=new Region("PopupMenuSeparator",false);
    public static final Region PROGRESS_BAR=new Region("ProgressBar",false);
    public static final Region RADIO_BUTTON=new Region("RadioButton",false);
    public static final Region RADIO_BUTTON_MENU_ITEM=new Region("RadioButtonMenuItem",false);
    public static final Region ROOT_PANE=new Region("RootPane",false);
    public static final Region SCROLL_BAR=new Region("ScrollBar",false);
    public static final Region SCROLL_BAR_TRACK=new Region("ScrollBarTrack",true);
    public static final Region SCROLL_BAR_THUMB=new Region("ScrollBarThumb",true);
    public static final Region SCROLL_PANE=new Region("ScrollPane",false);
    public static final Region SEPARATOR=new Region("Separator",false);
    public static final Region SLIDER=new Region("Slider",false);
    public static final Region SLIDER_TRACK=new Region("SliderTrack",true);
    public static final Region SLIDER_THUMB=new Region("SliderThumb",true);
    public static final Region SPINNER=new Region("Spinner",false);
    public static final Region SPLIT_PANE=new Region("SplitPane",false);
    public static final Region SPLIT_PANE_DIVIDER=new Region("SplitPaneDivider",true);
    public static final Region TABBED_PANE=new Region("TabbedPane",false);
    public static final Region TABBED_PANE_TAB=new Region("TabbedPaneTab",true);
    public static final Region TABBED_PANE_TAB_AREA=new Region("TabbedPaneTabArea",true);
    public static final Region TABBED_PANE_CONTENT=new Region("TabbedPaneContent",true);
    public static final Region TABLE=new Region("Table",false);
    public static final Region TABLE_HEADER=new Region("TableHeader",false);
    public static final Region TEXT_AREA=new Region("TextArea",false);
    public static final Region TEXT_FIELD=new Region("TextField",false);
    public static final Region TEXT_PANE=new Region("TextPane",false);
    public static final Region TOGGLE_BUTTON=new Region("ToggleButton",false);
    public static final Region TOOL_BAR=new Region("ToolBar",false);
    public static final Region TOOL_BAR_CONTENT=new Region("ToolBarContent",true);
    public static final Region TOOL_BAR_DRAG_WINDOW=new Region("ToolBarDragWindow",false);
    public static final Region TOOL_TIP=new Region("ToolTip",false);
    public static final Region TOOL_BAR_SEPARATOR=new Region("ToolBarSeparator",false);
    public static final Region TREE=new Region("Tree",false);
    public static final Region TREE_CELL=new Region("TreeCell",true);
    public static final Region VIEWPORT=new Region("Viewport",false);
    private static final Object UI_TO_REGION_MAP_KEY=new Object();
    private static final Object LOWER_CASE_NAME_MAP_KEY=new Object();
    private final String name;
    private final boolean subregion;

    protected Region(String name,String ui,boolean subregion){
        this(name,subregion);
        if(ui!=null){
            getUItoRegionMap().put(ui,this);
        }
    }

    private Region(String name,boolean subregion){
        if(name==null){
            throw new NullPointerException("You must specify a non-null name");
        }
        this.name=name;
        this.subregion=subregion;
    }

    static Region getRegion(JComponent c){
        return getUItoRegionMap().get(c.getUIClassID());
    }

    private static Map<String,Region> getUItoRegionMap(){
        AppContext context=AppContext.getAppContext();
        Map<String,Region> map=(Map<String,Region>)context.get(UI_TO_REGION_MAP_KEY);
        if(map==null){
            map=new HashMap<String,Region>();
            map.put("ArrowButtonUI",ARROW_BUTTON);
            map.put("ButtonUI",BUTTON);
            map.put("CheckBoxUI",CHECK_BOX);
            map.put("CheckBoxMenuItemUI",CHECK_BOX_MENU_ITEM);
            map.put("ColorChooserUI",COLOR_CHOOSER);
            map.put("ComboBoxUI",COMBO_BOX);
            map.put("DesktopPaneUI",DESKTOP_PANE);
            map.put("DesktopIconUI",DESKTOP_ICON);
            map.put("EditorPaneUI",EDITOR_PANE);
            map.put("FileChooserUI",FILE_CHOOSER);
            map.put("FormattedTextFieldUI",FORMATTED_TEXT_FIELD);
            map.put("InternalFrameUI",INTERNAL_FRAME);
            map.put("InternalFrameTitlePaneUI",INTERNAL_FRAME_TITLE_PANE);
            map.put("LabelUI",LABEL);
            map.put("ListUI",LIST);
            map.put("MenuUI",MENU);
            map.put("MenuBarUI",MENU_BAR);
            map.put("MenuItemUI",MENU_ITEM);
            map.put("OptionPaneUI",OPTION_PANE);
            map.put("PanelUI",PANEL);
            map.put("PasswordFieldUI",PASSWORD_FIELD);
            map.put("PopupMenuUI",POPUP_MENU);
            map.put("PopupMenuSeparatorUI",POPUP_MENU_SEPARATOR);
            map.put("ProgressBarUI",PROGRESS_BAR);
            map.put("RadioButtonUI",RADIO_BUTTON);
            map.put("RadioButtonMenuItemUI",RADIO_BUTTON_MENU_ITEM);
            map.put("RootPaneUI",ROOT_PANE);
            map.put("ScrollBarUI",SCROLL_BAR);
            map.put("ScrollPaneUI",SCROLL_PANE);
            map.put("SeparatorUI",SEPARATOR);
            map.put("SliderUI",SLIDER);
            map.put("SpinnerUI",SPINNER);
            map.put("SplitPaneUI",SPLIT_PANE);
            map.put("TabbedPaneUI",TABBED_PANE);
            map.put("TableUI",TABLE);
            map.put("TableHeaderUI",TABLE_HEADER);
            map.put("TextAreaUI",TEXT_AREA);
            map.put("TextFieldUI",TEXT_FIELD);
            map.put("TextPaneUI",TEXT_PANE);
            map.put("ToggleButtonUI",TOGGLE_BUTTON);
            map.put("ToolBarUI",TOOL_BAR);
            map.put("ToolTipUI",TOOL_TIP);
            map.put("ToolBarSeparatorUI",TOOL_BAR_SEPARATOR);
            map.put("TreeUI",TREE);
            map.put("ViewportUI",VIEWPORT);
            context.put(UI_TO_REGION_MAP_KEY,map);
        }
        return map;
    }

    static void registerUIs(UIDefaults table){
        for(Object key : getUItoRegionMap().keySet()){
            table.put(key,"javax.swing.plaf.synth.SynthLookAndFeel");
        }
    }

    public boolean isSubregion(){
        return subregion;
    }

    public String getName(){
        return name;
    }

    String getLowerCaseName(){
        Map<Region,String> lowerCaseNameMap=getLowerCaseNameMap();
        String lowerCaseName=lowerCaseNameMap.get(this);
        if(lowerCaseName==null){
            lowerCaseName=name.toLowerCase(Locale.ENGLISH);
            lowerCaseNameMap.put(this,lowerCaseName);
        }
        return lowerCaseName;
    }

    private static Map<Region,String> getLowerCaseNameMap(){
        AppContext context=AppContext.getAppContext();
        Map<Region,String> map=(Map<Region,String>)context.get(LOWER_CASE_NAME_MAP_KEY);
        if(map==null){
            map=new HashMap<Region,String>();
            context.put(LOWER_CASE_NAME_MAP_KEY,map);
        }
        return map;
    }

    @Override
    public String toString(){
        return name;
    }
}
