/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.accessibility.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.*;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class JEditorPane extends JTextComponent{
    public static final String W3C_LENGTH_UNITS="JEditorPane.w3cLengthUnits";
    public static final String HONOR_DISPLAY_PROPERTIES="JEditorPane.honorDisplayProperties";
    final static String PostDataProperty="javax.swing.JEditorPane.postdata";
    static final Map<String,String> defaultEditorKitMap=new HashMap<String,String>(0);
    private static final Object kitRegistryKey=
            new StringBuffer("JEditorPane.kitRegistry");
    private static final Object kitTypeRegistryKey=
            new StringBuffer("JEditorPane.kitTypeRegistry");
    private static final Object kitLoaderRegistryKey=
            new StringBuffer("JEditorPane.kitLoaderRegistry");
    private static final String uiClassID="EditorPaneUI";
    // --- variables ---------------------------------------
    private SwingWorker<URL,Object> pageLoader;
    private EditorKit kit;
    private boolean isUserSetEditorKit;
    private Hashtable<String,Object> pageProperties;
    private Hashtable<String,EditorKit> typeHandlers;

    public JEditorPane(){
        super();
        setFocusCycleRoot(true);
        setFocusTraversalPolicy(new LayoutFocusTraversalPolicy(){
            public Component getComponentAfter(Container focusCycleRoot,
                                               Component aComponent){
                if(focusCycleRoot!=JEditorPane.this||
                        (!isEditable()&&getComponentCount()>0)){
                    return super.getComponentAfter(focusCycleRoot,
                            aComponent);
                }else{
                    Container rootAncestor=getFocusCycleRootAncestor();
                    return (rootAncestor!=null)
                            ?rootAncestor.getFocusTraversalPolicy().
                            getComponentAfter(rootAncestor,
                                    JEditorPane.this)
                            :null;
                }
            }

            public Component getComponentBefore(Container focusCycleRoot,
                                                Component aComponent){
                if(focusCycleRoot!=JEditorPane.this||
                        (!isEditable()&&getComponentCount()>0)){
                    return super.getComponentBefore(focusCycleRoot,
                            aComponent);
                }else{
                    Container rootAncestor=getFocusCycleRootAncestor();
                    return (rootAncestor!=null)
                            ?rootAncestor.getFocusTraversalPolicy().
                            getComponentBefore(rootAncestor,
                                    JEditorPane.this)
                            :null;
                }
            }

            protected boolean accept(Component aComponent){
                return (aComponent!=JEditorPane.this)
                        ?super.accept(aComponent)
                        :false;
            }

            public Component getDefaultComponent(Container focusCycleRoot){
                return (focusCycleRoot!=JEditorPane.this||
                        (!isEditable()&&getComponentCount()>0))
                        ?super.getDefaultComponent(focusCycleRoot)
                        :null;
            }
        });
        LookAndFeel.installProperty(this,
                "focusTraversalKeysForward",
                JComponent.
                        getManagingFocusForwardTraversalKeys());
        LookAndFeel.installProperty(this,
                "focusTraversalKeysBackward",
                JComponent.
                        getManagingFocusBackwardTraversalKeys());
    }

    public JEditorPane(URL initialPage) throws IOException{
        this();
        setPage(initialPage);
    }

    public JEditorPane(String url) throws IOException{
        this();
        setPage(url);
    }

    public JEditorPane(String type,String text){
        this();
        setContentType(type);
        setText(text);
    }

    public static EditorKit createEditorKitForContentType(String type){
        Hashtable<String,EditorKit> kitRegistry=getKitRegisty();
        EditorKit k=kitRegistry.get(type);
        if(k==null){
            // try to dynamically load the support
            String classname=getKitTypeRegistry().get(type);
            ClassLoader loader=getKitLoaderRegistry().get(type);
            try{
                Class c;
                if(loader!=null){
                    c=loader.loadClass(classname);
                }else{
                    // Will only happen if developer has invoked
                    // registerEditorKitForContentType(type, class, null).
                    c=Class.forName(classname,true,Thread.currentThread().
                            getContextClassLoader());
                }
                k=(EditorKit)c.newInstance();
                kitRegistry.put(type,k);
            }catch(Throwable e){
                k=null;
            }
        }
        // create a copy of the prototype or null if there
        // is no prototype.
        if(k!=null){
            return (EditorKit)k.clone();
        }
        return null;
    }

    public static void registerEditorKitForContentType(String type,String classname){
        registerEditorKitForContentType(type,classname,Thread.currentThread().
                getContextClassLoader());
    }

    public static void registerEditorKitForContentType(String type,String classname,ClassLoader loader){
        getKitTypeRegistry().put(type,classname);
        if(loader!=null){
            getKitLoaderRegistry().put(type,loader);
        }else{
            getKitLoaderRegistry().remove(type);
        }
        getKitRegisty().remove(type);
    }

    public static String getEditorKitClassNameForContentType(String type){
        return getKitTypeRegistry().get(type);
    }

    private static Hashtable<String,String> getKitTypeRegistry(){
        loadDefaultKitsIfNecessary();
        return (Hashtable)SwingUtilities.appContextGet(kitTypeRegistryKey);
    }

    private static Hashtable<String,ClassLoader> getKitLoaderRegistry(){
        loadDefaultKitsIfNecessary();
        return (Hashtable)SwingUtilities.appContextGet(kitLoaderRegistryKey);
    }

    private static Hashtable<String,EditorKit> getKitRegisty(){
        Hashtable ht=(Hashtable)SwingUtilities.appContextGet(kitRegistryKey);
        if(ht==null){
            ht=new Hashtable(3);
            SwingUtilities.appContextPut(kitRegistryKey,ht);
        }
        return ht;
    }

    private static void loadDefaultKitsIfNecessary(){
        if(SwingUtilities.appContextGet(kitTypeRegistryKey)==null){
            synchronized(defaultEditorKitMap){
                if(defaultEditorKitMap.size()==0){
                    defaultEditorKitMap.put("text/plain",
                            "javax.swing.JEditorPane$PlainEditorKit");
                    defaultEditorKitMap.put("text/html",
                            "javax.swing.text.html.HTMLEditorKit");
                    defaultEditorKitMap.put("text/rtf",
                            "javax.swing.text.rtf.RTFEditorKit");
                    defaultEditorKitMap.put("application/rtf",
                            "javax.swing.text.rtf.RTFEditorKit");
                }
            }
            Hashtable ht=new Hashtable();
            SwingUtilities.appContextPut(kitTypeRegistryKey,ht);
            ht=new Hashtable();
            SwingUtilities.appContextPut(kitLoaderRegistryKey,ht);
            for(String key : defaultEditorKitMap.keySet()){
                registerEditorKitForContentType(key,defaultEditorKitMap.get(key));
            }
        }
    }

    public synchronized void addHyperlinkListener(HyperlinkListener listener){
        listenerList.add(HyperlinkListener.class,listener);
    }

    public synchronized void removeHyperlinkListener(HyperlinkListener listener){
        listenerList.remove(HyperlinkListener.class,listener);
    }

    public synchronized HyperlinkListener[] getHyperlinkListeners(){
        return listenerList.getListeners(HyperlinkListener.class);
    }

    public void fireHyperlinkUpdate(HyperlinkEvent e){
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==HyperlinkListener.class){
                ((HyperlinkListener)listeners[i+1]).hyperlinkUpdate(e);
            }
        }
    }

    public void setPage(URL page) throws IOException{
        if(page==null){
            throw new IOException("invalid url");
        }
        URL loaded=getPage();
        // reset scrollbar
        if(!page.equals(loaded)&&page.getRef()==null){
            scrollRectToVisible(new Rectangle(0,0,1,1));
        }
        boolean reloaded=false;
        Object postData=getPostData();
        if((loaded==null)||!loaded.sameFile(page)||(postData!=null)){
            // different url or POST method, load the new content
            int p=getAsynchronousLoadPriority(getDocument());
            if(p<0){
                // open stream synchronously
                InputStream in=getStream(page);
                if(kit!=null){
                    Document doc=initializeModel(kit,page);
                    // At this point, one could either load up the model with no
                    // view notifications slowing it down (i.e. best synchronous
                    // behavior) or set the model and start to feed it on a separate
                    // thread (best asynchronous behavior).
                    p=getAsynchronousLoadPriority(doc);
                    if(p>=0){
                        // load asynchronously
                        setDocument(doc);
                        synchronized(this){
                            pageLoader=new PageLoader(doc,in,loaded,page);
                            pageLoader.execute();
                        }
                        return;
                    }
                    read(in,doc);
                    setDocument(doc);
                    reloaded=true;
                }
            }else{
                // we may need to cancel background loading
                if(pageLoader!=null){
                    pageLoader.cancel(true);
                }
                // Do everything in a background thread.
                // Model initialization is deferred to that thread, too.
                pageLoader=new PageLoader(null,null,loaded,page);
                pageLoader.execute();
                return;
            }
        }
        final String reference=page.getRef();
        if(reference!=null){
            if(!reloaded){
                scrollToReference(reference);
            }else{
                // Have to scroll after painted.
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        scrollToReference(reference);
                    }
                });
            }
            getDocument().putProperty(Document.StreamDescriptionProperty,page);
        }
        firePropertyChange("page",loaded,page);
    }

    private Document initializeModel(EditorKit kit,URL page){
        Document doc=kit.createDefaultDocument();
        if(pageProperties!=null){
            // transfer properties discovered in stream to the
            // document property collection.
            for(Enumeration<String> e=pageProperties.keys();e.hasMoreElements();){
                String key=e.nextElement();
                doc.putProperty(key,pageProperties.get(key));
            }
            pageProperties.clear();
        }
        if(doc.getProperty(Document.StreamDescriptionProperty)==null){
            doc.putProperty(Document.StreamDescriptionProperty,page);
        }
        return doc;
    }

    private int getAsynchronousLoadPriority(Document doc){
        return (doc instanceof AbstractDocument?
                ((AbstractDocument)doc).getAsynchronousLoadPriority():-1);
    }

    public void read(InputStream in,Object desc) throws IOException{
        if(desc instanceof HTMLDocument&&
                kit instanceof HTMLEditorKit){
            HTMLDocument hdoc=(HTMLDocument)desc;
            setDocument(hdoc);
            read(in,hdoc);
        }else{
            String charset=(String)getClientProperty("charset");
            Reader r=(charset!=null)?new InputStreamReader(in,charset):
                    new InputStreamReader(in);
            super.read(r,desc);
        }
    }

    void read(InputStream in,Document doc) throws IOException{
        if(!Boolean.TRUE.equals(doc.getProperty("IgnoreCharsetDirective"))){
            final int READ_LIMIT=1024*10;
            in=new BufferedInputStream(in,READ_LIMIT);
            in.mark(READ_LIMIT);
        }
        try{
            String charset=(String)getClientProperty("charset");
            Reader r=(charset!=null)?new InputStreamReader(in,charset):
                    new InputStreamReader(in);
            kit.read(r,doc,0);
        }catch(BadLocationException e){
            throw new IOException(e.getMessage());
        }catch(ChangedCharSetException changedCharSetException){
            String charSetSpec=changedCharSetException.getCharSetSpec();
            if(changedCharSetException.keyEqualsCharSet()){
                putClientProperty("charset",charSetSpec);
            }else{
                setCharsetFromContentTypeParameters(charSetSpec);
            }
            try{
                in.reset();
            }catch(IOException exception){
                //mark was invalidated
                in.close();
                URL url=(URL)doc.getProperty(Document.StreamDescriptionProperty);
                if(url!=null){
                    URLConnection conn=url.openConnection();
                    in=conn.getInputStream();
                }else{
                    //there is nothing we can do to recover stream
                    throw changedCharSetException;
                }
            }
            try{
                doc.remove(0,doc.getLength());
            }catch(BadLocationException e){
            }
            doc.putProperty("IgnoreCharsetDirective",Boolean.valueOf(true));
            read(in,doc);
        }
    }

    protected InputStream getStream(URL page) throws IOException{
        final URLConnection conn=page.openConnection();
        if(conn instanceof HttpURLConnection){
            HttpURLConnection hconn=(HttpURLConnection)conn;
            hconn.setInstanceFollowRedirects(false);
            Object postData=getPostData();
            if(postData!=null){
                handlePostData(hconn,postData);
            }
            int response=hconn.getResponseCode();
            boolean redirect=(response>=300&&response<=399);
            /**
             * In the case of a redirect, we want to actually change the URL
             * that was input to the new, redirected URL
             */
            if(redirect){
                String loc=conn.getHeaderField("Location");
                if(loc.startsWith("http",0)){
                    page=new URL(loc);
                }else{
                    page=new URL(page,loc);
                }
                return getStream(page);
            }
        }
        // Connection properties handler should be forced to run on EDT,
        // as it instantiates the EditorKit.
        if(SwingUtilities.isEventDispatchThread()){
            handleConnectionProperties(conn);
        }else{
            try{
                SwingUtilities.invokeAndWait(new Runnable(){
                    public void run(){
                        handleConnectionProperties(conn);
                    }
                });
            }catch(InterruptedException e){
                throw new RuntimeException(e);
            }catch(InvocationTargetException e){
                throw new RuntimeException(e);
            }
        }
        return conn.getInputStream();
    }

    private void handleConnectionProperties(URLConnection conn){
        if(pageProperties==null){
            pageProperties=new Hashtable<String,Object>();
        }
        String type=conn.getContentType();
        if(type!=null){
            setContentType(type);
            pageProperties.put("content-type",type);
        }
        pageProperties.put(Document.StreamDescriptionProperty,conn.getURL());
        String enc=conn.getContentEncoding();
        if(enc!=null){
            pageProperties.put("content-encoding",enc);
        }
    }

    private Object getPostData(){
        return getDocument().getProperty(PostDataProperty);
    }

    private void handlePostData(HttpURLConnection conn,Object postData)
            throws IOException{
        conn.setDoOutput(true);
        DataOutputStream os=null;
        try{
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            os=new DataOutputStream(conn.getOutputStream());
            os.writeBytes((String)postData);
        }finally{
            if(os!=null){
                os.close();
            }
        }
    }

    public void scrollToReference(String reference){
        Document d=getDocument();
        if(d instanceof HTMLDocument){
            HTMLDocument doc=(HTMLDocument)d;
            HTMLDocument.Iterator iter=doc.getIterator(HTML.Tag.A);
            for(;iter.isValid();iter.next()){
                AttributeSet a=iter.getAttributes();
                String nm=(String)a.getAttribute(HTML.Attribute.NAME);
                if((nm!=null)&&nm.equals(reference)){
                    // found a matching reference in the document.
                    try{
                        int pos=iter.getStartOffset();
                        Rectangle r=modelToView(pos);
                        if(r!=null){
                            // the view is visible, scroll it to the
                            // center of the current visible area.
                            Rectangle vis=getVisibleRect();
                            //r.y -= (vis.height / 2);
                            r.height=vis.height;
                            scrollRectToVisible(r);
                            setCaretPosition(pos);
                        }
                    }catch(BadLocationException ble){
                        UIManager.getLookAndFeel().provideErrorFeedback(JEditorPane.this);
                    }
                }
            }
        }
    }
    // --- java.awt.Component methods --------------------------

    public URL getPage(){
        return (URL)getDocument().getProperty(Document.StreamDescriptionProperty);
    }
    // --- JTextComponent methods -----------------------------

    public void setPage(String url) throws IOException{
        if(url==null){
            throw new IOException("invalid url");
        }
        URL page=new URL(url);
        setPage(page);
    }    public void setText(String t){
        try{
            Document doc=getDocument();
            doc.remove(0,doc.getLength());
            if(t==null||t.equals("")){
                return;
            }
            Reader r=new StringReader(t);
            EditorKit kit=getEditorKit();
            kit.read(r,doc,0);
        }catch(IOException ioe){
            UIManager.getLookAndFeel().provideErrorFeedback(JEditorPane.this);
        }catch(BadLocationException ble){
            UIManager.getLookAndFeel().provideErrorFeedback(JEditorPane.this);
        }
    }

    public final String getContentType(){
        return (kit!=null)?kit.getContentType():null;
    }    public String getText(){
        String txt;
        try{
            StringWriter buf=new StringWriter();
            write(buf);
            txt=buf.toString();
        }catch(IOException ioe){
            txt=null;
        }
        return txt;
    }
    // --- Scrollable  ----------------------------------------

    public final void setContentType(String type){
        // The type could have optional info is part of it,
        // for example some charset info.  We need to strip that
        // of and save it.
        int parm=type.indexOf(";");
        if(parm>-1){
            // Save the paramList.
            String paramList=type.substring(parm);
            // update the content type string.
            type=type.substring(0,parm).trim();
            if(type.toLowerCase().startsWith("text/")){
                setCharsetFromContentTypeParameters(paramList);
            }
        }
        if((kit==null)||(!type.equals(kit.getContentType()))
                ||!isUserSetEditorKit){
            EditorKit k=getEditorKitForContentType(type);
            if(k!=null&&k!=kit){
                setEditorKit(k);
                isUserSetEditorKit=false;
            }
        }
    }    public boolean getScrollableTracksViewportWidth(){
        Container parent=SwingUtilities.getUnwrappedParent(this);
        if(parent instanceof JViewport){
            JViewport port=(JViewport)parent;
            TextUI ui=getUI();
            int w=port.getWidth();
            Dimension min=ui.getMinimumSize(this);
            Dimension max=ui.getMaximumSize(this);
            if((w>=min.width)&&(w<=max.width)){
                return true;
            }
        }
        return false;
    }

    private void setCharsetFromContentTypeParameters(String paramlist){
        String charset;
        try{
            // paramlist is handed to us with a leading ';', strip it.
            int semi=paramlist.indexOf(';');
            if(semi>-1&&semi<paramlist.length()-1){
                paramlist=paramlist.substring(semi+1);
            }
            if(paramlist.length()>0){
                // parse the paramlist into attr-value pairs & get the
                // charset pair's value
                HeaderParser hdrParser=new HeaderParser(paramlist);
                charset=hdrParser.findValue("charset");
                if(charset!=null){
                    putClientProperty("charset",charset);
                }
            }
        }catch(IndexOutOfBoundsException e){
            // malformed parameter list, use charset we have
        }catch(NullPointerException e){
            // malformed parameter list, use charset we have
        }catch(Exception e){
            // malformed parameter list, use charset we have; but complain
            System.err.println("JEditorPane.getCharsetFromContentTypeParameters failed on: "+paramlist);
            e.printStackTrace();
        }
    }    public boolean getScrollableTracksViewportHeight(){
        Container parent=SwingUtilities.getUnwrappedParent(this);
        if(parent instanceof JViewport){
            JViewport port=(JViewport)parent;
            TextUI ui=getUI();
            int h=port.getHeight();
            Dimension min=ui.getMinimumSize(this);
            if(h>=min.height){
                Dimension max=ui.getMaximumSize(this);
                if(h<=max.height){
                    return true;
                }
            }
        }
        return false;
    }
    // --- Serialization ------------------------------------

    public EditorKit getEditorKitForContentType(String type){
        if(typeHandlers==null){
            typeHandlers=new Hashtable<String,EditorKit>(3);
        }
        EditorKit k=typeHandlers.get(type);
        if(k==null){
            k=createEditorKitForContentType(type);
            if(k!=null){
                setEditorKitForContentType(type,k);
            }
        }
        if(k==null){
            k=createDefaultEditorKit();
        }
        return k;
    }

    public void setEditorKitForContentType(String type,EditorKit k){
        if(typeHandlers==null){
            typeHandlers=new Hashtable<String,EditorKit>(3);
        }
        typeHandlers.put(type,k);
    }

    @Override
    public void replaceSelection(String content){
        if(!isEditable()){
            UIManager.getLookAndFeel().provideErrorFeedback(JEditorPane.this);
            return;
        }
        EditorKit kit=getEditorKit();
        if(kit instanceof StyledEditorKit){
            try{
                Document doc=getDocument();
                Caret caret=getCaret();
                boolean composedTextSaved=saveComposedText(caret.getDot());
                int p0=Math.min(caret.getDot(),caret.getMark());
                int p1=Math.max(caret.getDot(),caret.getMark());
                if(doc instanceof AbstractDocument){
                    ((AbstractDocument)doc).replace(p0,p1-p0,content,
                            ((StyledEditorKit)kit).getInputAttributes());
                }else{
                    if(p0!=p1){
                        doc.remove(p0,p1-p0);
                    }
                    if(content!=null&&content.length()>0){
                        doc.insertString(p0,content,((StyledEditorKit)kit).
                                getInputAttributes());
                    }
                }
                if(composedTextSaved){
                    restoreComposedText();
                }
            }catch(BadLocationException e){
                UIManager.getLookAndFeel().provideErrorFeedback(JEditorPane.this);
            }
        }else{
            super.replaceSelection(content);
        }
    }

    public EditorKit getEditorKit(){
        if(kit==null){
            kit=createDefaultEditorKit();
            isUserSetEditorKit=false;
        }
        return kit;
    }

    protected EditorKit createDefaultEditorKit(){
        return new PlainEditorKit();
    }

    public void setEditorKit(EditorKit kit){
        EditorKit old=this.kit;
        isUserSetEditorKit=true;
        if(old!=null){
            old.deinstall(this);
        }
        this.kit=kit;
        if(this.kit!=null){
            this.kit.install(this);
            setDocument(this.kit.createDefaultDocument());
        }
        firePropertyChange("editorKit",old,kit);
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

    public String getUIClassID(){
        return uiClassID;
    }

    public Dimension getPreferredSize(){
        Dimension d=super.getPreferredSize();
        Container parent=SwingUtilities.getUnwrappedParent(this);
        if(parent instanceof JViewport){
            JViewport port=(JViewport)parent;
            TextUI ui=getUI();
            int prefWidth=d.width;
            int prefHeight=d.height;
            if(!getScrollableTracksViewportWidth()){
                int w=port.getWidth();
                Dimension min=ui.getMinimumSize(this);
                if(w!=0&&w<min.width){
                    // Only adjust to min if we have a valid size
                    prefWidth=min.width;
                }
            }
            if(!getScrollableTracksViewportHeight()){
                int h=port.getHeight();
                Dimension min=ui.getMinimumSize(this);
                if(h!=0&&h<min.height){
                    // Only adjust to min if we have a valid size
                    prefHeight=min.height;
                }
            }
            if(prefWidth!=d.width||prefHeight!=d.height){
                d=new Dimension(prefWidth,prefHeight);
            }
        }
        return d;
    }

    static class PlainEditorKit extends DefaultEditorKit implements ViewFactory{
        public ViewFactory getViewFactory(){
            return this;
        }

        public View create(Element elem){
            Document doc=elem.getDocument();
            Object i18nFlag
                    =doc.getProperty("i18n"/**AbstractDocument.I18NProperty*/);
            if((i18nFlag!=null)&&i18nFlag.equals(Boolean.TRUE)){
                // build a view that support bidi
                return createI18N(elem);
            }else{
                return new WrappedPlainView(elem);
            }
        }

        View createI18N(Element elem){
            String kind=elem.getName();
            if(kind!=null){
                if(kind.equals(AbstractDocument.ContentElementName)){
                    return new PlainParagraph(elem);
                }else if(kind.equals(AbstractDocument.ParagraphElementName)){
                    return new BoxView(elem,View.Y_AXIS);
                }
            }
            return null;
        }

        static class PlainParagraph extends javax.swing.text.ParagraphView{
            PlainParagraph(Element elem){
                super(elem);
                layoutPool=new LogicalView(elem);
                layoutPool.setParent(this);
            }

            protected void setPropertiesFromAttributes(){
                Component c=getContainer();
                if((c!=null)
                        &&(!c.getComponentOrientation().isLeftToRight())){
                    setJustification(StyleConstants.ALIGN_RIGHT);
                }else{
                    setJustification(StyleConstants.ALIGN_LEFT);
                }
            }

            public int getFlowSpan(int index){
                Component c=getContainer();
                if(c instanceof JTextArea){
                    JTextArea area=(JTextArea)c;
                    if(!area.getLineWrap()){
                        // no limit if unwrapped
                        return Integer.MAX_VALUE;
                    }
                }
                return super.getFlowSpan(index);
            }

            protected SizeRequirements calculateMinorAxisRequirements(int axis,
                                                                      SizeRequirements r){
                SizeRequirements req
                        =super.calculateMinorAxisRequirements(axis,r);
                Component c=getContainer();
                if(c instanceof JTextArea){
                    JTextArea area=(JTextArea)c;
                    if(!area.getLineWrap()){
                        // min is pref if unwrapped
                        req.minimum=req.preferred;
                    }
                }
                return req;
            }

            static class LogicalView extends CompositeView{
                LogicalView(Element elem){
                    super(elem);
                }

                protected void loadChildren(ViewFactory f){
                    Element elem=getElement();
                    if(elem.getElementCount()>0){
                        super.loadChildren(f);
                    }else{
                        View v=new GlyphView(elem);
                        append(v);
                    }
                }

                protected boolean isBefore(int x,int y,Rectangle alloc){
                    return false;
                }

                protected boolean isAfter(int x,int y,Rectangle alloc){
                    return false;
                }

                protected View getViewAtPoint(int x,int y,Rectangle alloc){
                    return null;
                }

                protected void childAllocation(int index,Rectangle a){
                }
                // The following methods don't do anything useful, they
                // simply keep the class from being abstract.

                protected int getViewIndexAtPosition(int pos){
                    Element elem=getElement();
                    if(elem.getElementCount()>0){
                        return elem.getElementIndex(pos);
                    }
                    return 0;
                }

                public float getPreferredSpan(int axis){
                    if(getViewCount()!=1)
                        throw new Error("One child view is assumed.");
                    View v=getView(0);
                    //((GlyphView)v).setGlyphPainter(null);
                    return v.getPreferredSpan(axis);
                }

                public void paint(Graphics g,Shape allocation){
                }

                protected boolean
                updateChildren(DocumentEvent.ElementChange ec,
                               DocumentEvent e,ViewFactory f){
                    return false;
                }

                protected void forwardUpdateToView(View v,DocumentEvent e,
                                                   Shape a,ViewFactory f){
                    v.setParent(this);
                    super.forwardUpdateToView(v,e,a,f);
                }
            }
        }
    }

    static class HeaderParser{
        String raw;
        String[][] tab;

        public HeaderParser(String raw){
            this.raw=raw;
            tab=new String[10][2];
            parse();
        }

        private void parse(){
            if(raw!=null){
                raw=raw.trim();
                char[] ca=raw.toCharArray();
                int beg=0, end=0, i=0;
                boolean inKey=true;
                boolean inQuote=false;
                int len=ca.length;
                while(end<len){
                    char c=ca[end];
                    if(c=='='){ // end of a key
                        tab[i][0]=new String(ca,beg,end-beg).toLowerCase();
                        inKey=false;
                        end++;
                        beg=end;
                    }else if(c=='\"'){
                        if(inQuote){
                            tab[i++][1]=new String(ca,beg,end-beg);
                            inQuote=false;
                            do{
                                end++;
                            }while(end<len&&(ca[end]==' '||ca[end]==','));
                            inKey=true;
                            beg=end;
                        }else{
                            inQuote=true;
                            end++;
                            beg=end;
                        }
                    }else if(c==' '||c==','){ // end key/val, of whatever we're in
                        if(inQuote){
                            end++;
                            continue;
                        }else if(inKey){
                            tab[i++][0]=(new String(ca,beg,end-beg)).toLowerCase();
                        }else{
                            tab[i++][1]=(new String(ca,beg,end-beg));
                        }
                        while(end<len&&(ca[end]==' '||ca[end]==',')){
                            end++;
                        }
                        inKey=true;
                        beg=end;
                    }else{
                        end++;
                    }
                }
                // get last key/val, if any
                if(--end>beg){
                    if(!inKey){
                        if(ca[end]=='\"'){
                            tab[i++][1]=(new String(ca,beg,end-beg));
                        }else{
                            tab[i++][1]=(new String(ca,beg,end-beg+1));
                        }
                    }else{
                        tab[i][0]=(new String(ca,beg,end-beg+1)).toLowerCase();
                    }
                }else if(end==beg){
                    if(!inKey){
                        if(ca[end]=='\"'){
                            tab[i++][1]=String.valueOf(ca[end-1]);
                        }else{
                            tab[i++][1]=String.valueOf(ca[end]);
                        }
                    }else{
                        tab[i][0]=String.valueOf(ca[end]).toLowerCase();
                    }
                }
            }
        }

        public String findKey(int i){
            if(i<0||i>10)
                return null;
            return tab[i][0];
        }

        public String findValue(int i){
            if(i<0||i>10)
                return null;
            return tab[i][1];
        }

        public String findValue(String key){
            return findValue(key,null);
        }

        public String findValue(String k,String Default){
            if(k==null)
                return Default;
            k=k.toLowerCase();
            for(int i=0;i<10;++i){
                if(tab[i][0]==null){
                    return Default;
                }else if(k.equals(tab[i][0])){
                    return tab[i][1];
                }
            }
            return Default;
        }

        public int findInt(String k,int Default){
            try{
                return Integer.parseInt(findValue(k,String.valueOf(Default)));
            }catch(Throwable t){
                return Default;
            }
        }
    }

    class PageLoader extends SwingWorker<URL,Object>{
        InputStream in;
        URL old;
        URL page;
        Document doc;
        PageLoader(Document doc,InputStream in,URL old,URL page){
            this.in=in;
            this.old=old;
            this.page=page;
            this.doc=doc;
        }

        protected URL doInBackground(){
            boolean pageLoaded=false;
            try{
                if(in==null){
                    in=getStream(page);
                    if(kit==null){
                        // We received document of unknown content type.
                        UIManager.getLookAndFeel().
                                provideErrorFeedback(JEditorPane.this);
                        return old;
                    }
                }
                if(doc==null){
                    try{
                        SwingUtilities.invokeAndWait(new Runnable(){
                            public void run(){
                                doc=initializeModel(kit,page);
                                setDocument(doc);
                            }
                        });
                    }catch(InvocationTargetException ex){
                        UIManager.getLookAndFeel().provideErrorFeedback(
                                JEditorPane.this);
                        return old;
                    }catch(InterruptedException ex){
                        UIManager.getLookAndFeel().provideErrorFeedback(
                                JEditorPane.this);
                        return old;
                    }
                }
                read(in,doc);
                URL page=(URL)doc.getProperty(Document.StreamDescriptionProperty);
                String reference=page.getRef();
                if(reference!=null){
                    // scroll the page if necessary, but do it on the
                    // event thread... that is the only guarantee that
                    // modelToView can be safely called.
                    Runnable callScrollToReference=new Runnable(){
                        public void run(){
                            URL u=(URL)getDocument().getProperty
                                    (Document.StreamDescriptionProperty);
                            String ref=u.getRef();
                            scrollToReference(ref);
                        }
                    };
                    SwingUtilities.invokeLater(callScrollToReference);
                }
                pageLoaded=true;
            }catch(IOException ioe){
                UIManager.getLookAndFeel().provideErrorFeedback(JEditorPane.this);
            }finally{
                if(pageLoaded){
                    SwingUtilities.invokeLater(new Runnable(){
                        public void run(){
                            JEditorPane.this.firePropertyChange("page",old,page);
                        }
                    });
                }
                return (pageLoaded?page:old);
            }
        }
    }

    protected class AccessibleJEditorPane extends AccessibleJTextComponent{
        public String getAccessibleDescription(){
            String description=accessibleDescription;
            // fallback to client property
            if(description==null){
                description=(String)getClientProperty(AccessibleContext.ACCESSIBLE_DESCRIPTION_PROPERTY);
            }
            if(description==null){
                description=JEditorPane.this.getContentType();
            }
            return description;
        }

        public AccessibleStateSet getAccessibleStateSet(){
            AccessibleStateSet states=super.getAccessibleStateSet();
            states.add(AccessibleState.MULTI_LINE);
            return states;
        }
    }

    protected class AccessibleJEditorPaneHTML extends AccessibleJEditorPane{
        private AccessibleContext accessibleContext;

        protected AccessibleJEditorPaneHTML(){
            HTMLEditorKit kit=(HTMLEditorKit)JEditorPane.this.getEditorKit();
            accessibleContext=kit.getAccessibleContext();
        }        public AccessibleText getAccessibleText(){
            return new JEditorPaneAccessibleHypertextSupport();
        }

        public Accessible getAccessibleAt(Point p){
            if(accessibleContext!=null&&p!=null){
                try{
                    AccessibleComponent acomp=
                            accessibleContext.getAccessibleComponent();
                    if(acomp!=null){
                        return acomp.getAccessibleAt(p);
                    }else{
                        return null;
                    }
                }catch(IllegalComponentStateException e){
                    return null;
                }
            }else{
                return null;
            }
        }

        public int getAccessibleChildrenCount(){
            if(accessibleContext!=null){
                return accessibleContext.getAccessibleChildrenCount();
            }else{
                return 0;
            }
        }

        public Accessible getAccessibleChild(int i){
            if(accessibleContext!=null){
                return accessibleContext.getAccessibleChild(i);
            }else{
                return null;
            }
        }


    }

    protected class JEditorPaneAccessibleHypertextSupport
            extends AccessibleJEditorPane implements AccessibleHypertext{
        LinkVector hyperlinks;
        boolean linksValid=false;

        public JEditorPaneAccessibleHypertextSupport(){
            hyperlinks=new LinkVector();
            Document d=JEditorPane.this.getDocument();
            if(d!=null){
                d.addDocumentListener(new DocumentListener(){
                    public void insertUpdate(DocumentEvent theEvent){
                        linksValid=false;
                    }

                    public void removeUpdate(DocumentEvent theEvent){
                        linksValid=false;
                    }

                    public void changedUpdate(DocumentEvent theEvent){
                        linksValid=false;
                    }
                });
            }
        }

        public int getLinkCount(){
            if(linksValid==false){
                buildLinkTable();
            }
            return hyperlinks.size();
        }

        private void buildLinkTable(){
            hyperlinks.removeAllElements();
            Document d=JEditorPane.this.getDocument();
            if(d!=null){
                ElementIterator ei=new ElementIterator(d);
                Element e;
                AttributeSet as;
                AttributeSet anchor;
                String href;
                while((e=ei.next())!=null){
                    if(e.isLeaf()){
                        as=e.getAttributes();
                        anchor=(AttributeSet)as.getAttribute(HTML.Tag.A);
                        href=(anchor!=null)?
                                (String)anchor.getAttribute(HTML.Attribute.HREF):null;
                        if(href!=null){
                            hyperlinks.addElement(new HTMLLink(e));
                        }
                    }
                }
            }
            linksValid=true;
        }

        public AccessibleHyperlink getLink(int linkIndex){
            if(linksValid==false){
                buildLinkTable();
            }
            if(linkIndex>=0&&linkIndex<hyperlinks.size()){
                return hyperlinks.elementAt(linkIndex);
            }else{
                return null;
            }
        }

        public int getLinkIndex(int charIndex){
            if(linksValid==false){
                buildLinkTable();
            }
            Element e=null;
            Document doc=JEditorPane.this.getDocument();
            if(doc!=null){
                for(e=doc.getDefaultRootElement();!e.isLeaf();){
                    int index=e.getElementIndex(charIndex);
                    e=e.getElement(index);
                }
            }
            // don't need to verify that it's an HREF element; if
            // not, then it won't be in the hyperlinks Vector, and
            // so indexOf will return -1 in any case
            return hyperlinks.baseElementIndex(e);
        }

        public String getLinkText(int linkIndex){
            if(linksValid==false){
                buildLinkTable();
            }
            Element e=(Element)hyperlinks.elementAt(linkIndex);
            if(e!=null){
                Document d=JEditorPane.this.getDocument();
                if(d!=null){
                    try{
                        return d.getText(e.getStartOffset(),
                                e.getEndOffset()-e.getStartOffset());
                    }catch(BadLocationException exception){
                        return null;
                    }
                }
            }
            return null;
        }

        public class HTMLLink extends AccessibleHyperlink{
            Element element;

            public HTMLLink(Element e){
                element=e;
            }

            public boolean isValid(){
                return JEditorPaneAccessibleHypertextSupport.this.linksValid;
            }

            public int getAccessibleActionCount(){
                return 1;
            }

            public boolean doAccessibleAction(int i){
                if(i==0&&isValid()==true){
                    URL u=(URL)getAccessibleActionObject(i);
                    if(u!=null){
                        HyperlinkEvent linkEvent=
                                new HyperlinkEvent(JEditorPane.this,HyperlinkEvent.EventType.ACTIVATED,u);
                        JEditorPane.this.fireHyperlinkUpdate(linkEvent);
                        return true;
                    }
                }
                return false;  // link invalid or i != 0
            }

            public String getAccessibleActionDescription(int i){
                if(i==0&&isValid()==true){
                    Document d=JEditorPane.this.getDocument();
                    if(d!=null){
                        try{
                            return d.getText(getStartIndex(),
                                    getEndIndex()-getStartIndex());
                        }catch(BadLocationException exception){
                            return null;
                        }
                    }
                }
                return null;
            }

            public Object getAccessibleActionObject(int i){
                if(i==0&&isValid()==true){
                    AttributeSet as=element.getAttributes();
                    AttributeSet anchor=
                            (AttributeSet)as.getAttribute(HTML.Tag.A);
                    String href=(anchor!=null)?
                            (String)anchor.getAttribute(HTML.Attribute.HREF):null;
                    if(href!=null){
                        URL u;
                        try{
                            u=new URL(JEditorPane.this.getPage(),href);
                        }catch(MalformedURLException m){
                            u=null;
                        }
                        return u;
                    }
                }
                return null;  // link invalid or i != 0
            }

            public Object getAccessibleActionAnchor(int i){
                return getAccessibleActionDescription(i);
            }

            public int getStartIndex(){
                return element.getStartOffset();
            }

            public int getEndIndex(){
                return element.getEndOffset();
            }
        }

        private class LinkVector extends Vector<HTMLLink>{
            public int baseElementIndex(Element e){
                HTMLLink l;
                for(int i=0;i<elementCount;i++){
                    l=elementAt(i);
                    if(l.element==e){
                        return i;
                    }
                }
                return -1;
            }
        }
    }    protected String paramString(){
        String kitString=(kit!=null?
                kit.toString():"");
        String typeHandlersString=(typeHandlers!=null?
                typeHandlers.toString():"");
        return super.paramString()+
                ",kit="+kitString+
                ",typeHandlers="+typeHandlersString;
    }
/////////////////
// Accessibility support
////////////////

    public AccessibleContext getAccessibleContext(){
        if(getEditorKit() instanceof HTMLEditorKit){
            if(accessibleContext==null||accessibleContext.getClass()!=
                    AccessibleJEditorPaneHTML.class){
                accessibleContext=new AccessibleJEditorPaneHTML();
            }
        }else if(accessibleContext==null||accessibleContext.getClass()!=
                AccessibleJEditorPane.class){
            accessibleContext=new AccessibleJEditorPane();
        }
        return accessibleContext;
    }










}
