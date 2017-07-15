/**
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.rtf;

import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

class RTFReader extends RTFParser{
    static final String TabAlignmentKey="tab_alignment";
    static final String TabLeaderKey="tab_leader";
    static Dictionary<String,String> textKeywords=null;
    static Dictionary<String,char[]> characterSets;
    static boolean useNeXTForAnsi=false;
    static private Dictionary<String,RTFAttribute> straightforwardAttributes;

    static{
        straightforwardAttributes=RTFAttributes.attributesByKeyword();
    }

    static{
        textKeywords=new Hashtable<String,String>();
        textKeywords.put("\\","\\");
        textKeywords.put("{","{");
        textKeywords.put("}","}");
        textKeywords.put(" ","\u00A0");  /** not in the spec... */
        textKeywords.put("~","\u00A0");  /** nonbreaking space */
        textKeywords.put("_","\u2011");  /** nonbreaking hyphen */
        textKeywords.put("bullet","\u2022");
        textKeywords.put("emdash","\u2014");
        textKeywords.put("emspace","\u2003");
        textKeywords.put("endash","\u2013");
        textKeywords.put("enspace","\u2002");
        textKeywords.put("ldblquote","\u201C");
        textKeywords.put("lquote","\u2018");
        textKeywords.put("ltrmark","\u200E");
        textKeywords.put("rdblquote","\u201D");
        textKeywords.put("rquote","\u2019");
        textKeywords.put("rtlmark","\u200F");
        textKeywords.put("tab","\u0009");
        textKeywords.put("zwj","\u200D");
        textKeywords.put("zwnj","\u200C");
        /** There is no Unicode equivalent to an optional hyphen, as far as
         I can tell. */
        textKeywords.put("-","\u2027");  /** TODO: optional hyphen */
    }

    static{
        characterSets=new Hashtable<String,char[]>();
    }

    StyledDocument target;
    Dictionary<Object,Object> parserState;
    Destination rtfDestination;
    MutableAttributeSet documentAttributes;
    Dictionary<Integer,String> fontTable;
    Color[] colorTable;
    Style[] characterStyles;
    Style[] paragraphStyles;
    Style[] sectionStyles;
    int rtfversion;
    boolean ignoreGroupIfUnknownKeyword;
    int skippingCharacters;
    private MockAttributeSet mockery;

    public RTFReader(StyledDocument destination){
        int i;
        target=destination;
        parserState=new Hashtable<Object,Object>();
        fontTable=new Hashtable<Integer,String>();
        rtfversion=-1;
        mockery=new MockAttributeSet();
        documentAttributes=new SimpleAttributeSet();
    }

    static char[] readCharset(java.net.URL href)
            throws IOException{
        return readCharset(href.openStream());
    }

    Color defaultColor(){
        return Color.black;
    }

    public boolean handleKeyword(String keyword){
        String item;
        boolean ignoreGroupIfUnknownKeywordSave=ignoreGroupIfUnknownKeyword;
        if(skippingCharacters>0){
            skippingCharacters--;
            return true;
        }
        ignoreGroupIfUnknownKeyword=false;
        if((item=textKeywords.get(keyword))!=null){
            handleText(item);
            return true;
        }
        if(keyword.equals("fonttbl")){
            setRTFDestination(new FonttblDestination());
            return true;
        }
        if(keyword.equals("colortbl")){
            setRTFDestination(new ColortblDestination());
            return true;
        }
        if(keyword.equals("stylesheet")){
            setRTFDestination(new StylesheetDestination());
            return true;
        }
        if(keyword.equals("info")){
            setRTFDestination(new InfoDestination());
            return false;
        }
        if(keyword.equals("mac")){
            setCharacterSet("mac");
            return true;
        }
        if(keyword.equals("ansi")){
            if(useNeXTForAnsi)
                setCharacterSet("NeXT");
            else
                setCharacterSet("ansi");
            return true;
        }
        if(keyword.equals("next")){
            setCharacterSet("NeXT");
            return true;
        }
        if(keyword.equals("pc")){
            setCharacterSet("cpg437"); /** IBM Code Page 437 */
            return true;
        }
        if(keyword.equals("pca")){
            setCharacterSet("cpg850"); /** IBM Code Page 850 */
            return true;
        }
        if(keyword.equals("*")){
            ignoreGroupIfUnknownKeyword=true;
            return true;
        }
        if(rtfDestination!=null){
            if(rtfDestination.handleKeyword(keyword))
                return true;
        }
        /** this point is reached only if the keyword is unrecognized */
        /** other destinations we don't understand and therefore ignore */
        if(keyword.equals("aftncn")||
                keyword.equals("aftnsep")||
                keyword.equals("aftnsepc")||
                keyword.equals("annotation")||
                keyword.equals("atnauthor")||
                keyword.equals("atnicn")||
                keyword.equals("atnid")||
                keyword.equals("atnref")||
                keyword.equals("atntime")||
                keyword.equals("atrfend")||
                keyword.equals("atrfstart")||
                keyword.equals("bkmkend")||
                keyword.equals("bkmkstart")||
                keyword.equals("datafield")||
                keyword.equals("do")||
                keyword.equals("dptxbxtext")||
                keyword.equals("falt")||
                keyword.equals("field")||
                keyword.equals("file")||
                keyword.equals("filetbl")||
                keyword.equals("fname")||
                keyword.equals("fontemb")||
                keyword.equals("fontfile")||
                keyword.equals("footer")||
                keyword.equals("footerf")||
                keyword.equals("footerl")||
                keyword.equals("footerr")||
                keyword.equals("footnote")||
                keyword.equals("ftncn")||
                keyword.equals("ftnsep")||
                keyword.equals("ftnsepc")||
                keyword.equals("header")||
                keyword.equals("headerf")||
                keyword.equals("headerl")||
                keyword.equals("headerr")||
                keyword.equals("keycode")||
                keyword.equals("nextfile")||
                keyword.equals("object")||
                keyword.equals("pict")||
                keyword.equals("pn")||
                keyword.equals("pnseclvl")||
                keyword.equals("pntxtb")||
                keyword.equals("pntxta")||
                keyword.equals("revtbl")||
                keyword.equals("rxe")||
                keyword.equals("tc")||
                keyword.equals("template")||
                keyword.equals("txe")||
                keyword.equals("xe")){
            ignoreGroupIfUnknownKeywordSave=true;
        }
        if(ignoreGroupIfUnknownKeywordSave){
            setRTFDestination(new DiscardingDestination());
        }
        return false;
    }

    protected void setRTFDestination(Destination newDestination){
        /** Check that setting the destination won't close the
         current destination (should never happen) */
        Dictionary previousState=(Dictionary)parserState.get("_savedState");
        if(previousState!=null){
            if(rtfDestination!=previousState.get("dst")){
                warning("Warning, RTF destination overridden, invalid RTF.");
                rtfDestination.close();
            }
        }
        rtfDestination=newDestination;
        parserState.put("dst",rtfDestination);
    }

    public boolean handleKeyword(String keyword,int parameter){
        boolean ignoreGroupIfUnknownKeywordSave=ignoreGroupIfUnknownKeyword;
        if(skippingCharacters>0){
            skippingCharacters--;
            return true;
        }
        ignoreGroupIfUnknownKeyword=false;
        if(keyword.equals("uc")){
            /** count of characters to skip after a unicode character */
            parserState.put("UnicodeSkip",Integer.valueOf(parameter));
            return true;
        }
        if(keyword.equals("u")){
            if(parameter<0)
                parameter=parameter+65536;
            handleText((char)parameter);
            Number skip=(Number)(parserState.get("UnicodeSkip"));
            if(skip!=null){
                skippingCharacters=skip.intValue();
            }else{
                skippingCharacters=1;
            }
            return true;
        }
        if(keyword.equals("rtf")){
            rtfversion=parameter;
            setRTFDestination(new DocumentDestination());
            return true;
        }
        if(keyword.startsWith("NeXT")||
                keyword.equals("private"))
            ignoreGroupIfUnknownKeywordSave=true;
        if(rtfDestination!=null){
            if(rtfDestination.handleKeyword(keyword,parameter))
                return true;
        }
        /** this point is reached only if the keyword is unrecognized */
        if(ignoreGroupIfUnknownKeywordSave){
            setRTFDestination(new DiscardingDestination());
        }
        return false;
    }

    public void handleText(String text){
        if(skippingCharacters>0){
            if(skippingCharacters>=text.length()){
                skippingCharacters-=text.length();
                return;
            }else{
                text=text.substring(skippingCharacters);
                skippingCharacters=0;
            }
        }
        if(rtfDestination!=null){
            rtfDestination.handleText(text);
            return;
        }
        warning("Text with no destination. oops.");
    }

    public void handleBinaryBlob(byte[] data){
        if(skippingCharacters>0){
            /** a blob only counts as one character for skipping purposes */
            skippingCharacters--;
            return;
        }
        /** someday, someone will want to do something with blobs */
    }

    public void begingroup(){
        if(skippingCharacters>0){
            /** TODO this indicates an error in the RTF. Log it? */
            skippingCharacters=0;
        }
        /** we do this little dance to avoid cloning the entire state stack and
         immediately throwing it away. */
        Object oldSaveState=parserState.get("_savedState");
        if(oldSaveState!=null)
            parserState.remove("_savedState");
        Dictionary<String,Object> saveState=(Dictionary<String,Object>)((Hashtable)parserState).clone();
        if(oldSaveState!=null)
            saveState.put("_savedState",oldSaveState);
        parserState.put("_savedState",saveState);
        if(rtfDestination!=null)
            rtfDestination.begingroup();
    }

    public void endgroup(){
        if(skippingCharacters>0){
            /** NB this indicates an error in the RTF. Log it? */
            skippingCharacters=0;
        }
        Dictionary<Object,Object> restoredState=(Dictionary<Object,Object>)parserState.get("_savedState");
        Destination restoredDestination=(Destination)restoredState.get("dst");
        if(restoredDestination!=rtfDestination){
            rtfDestination.close(); /** allow the destination to clean up */
            rtfDestination=restoredDestination;
        }
        Dictionary oldParserState=parserState;
        parserState=restoredState;
        if(rtfDestination!=null)
            rtfDestination.endgroup(oldParserState);
    }

    public void close()
            throws IOException{
        Enumeration docProps=documentAttributes.getAttributeNames();
        while(docProps.hasMoreElements()){
            Object propName=docProps.nextElement();
            target.putProperty(propName,
                    documentAttributes.getAttribute(propName));
        }
        /** RTFParser should have ensured that all our groups are closed */
        warning("RTF filter done.");
        super.close();
    }

    public void setCharacterSet(String name){
        Object set;
        try{
            set=getCharacterSet(name);
        }catch(Exception e){
            warning("Exception loading RTF character set \""+name+"\": "+e);
            set=null;
        }
        if(set!=null){
            translationTable=(char[])set;
        }else{
            warning("Unknown RTF character set \""+name+"\"");
            if(!name.equals("ansi")){
                try{
                    translationTable=(char[])getCharacterSet("ansi");
                }catch(IOException e){
                    throw new InternalError("RTFReader: Unable to find character set resources ("+e+")",e);
                }
            }
        }
        setTargetAttribute(Constants.RTFCharacterSet,name);
    }

    private void setTargetAttribute(String name,Object value){
//    target.changeAttributes(new LFDictionary(LFArray.arrayWithObject(value), LFArray.arrayWithObject(name)));
    }

    public static Object
    getCharacterSet(final String name)
            throws IOException{
        char[] set=characterSets.get(name);
        if(set==null){
            InputStream charsetStream=AccessController.doPrivileged(
                    new PrivilegedAction<InputStream>(){
                        public InputStream run(){
                            return RTFReader.class.getResourceAsStream("charsets/"+name+".txt");
                        }
                    });
            set=readCharset(charsetStream);
            defineCharacterSet(name,set);
        }
        return set;
    }

    public static void
    defineCharacterSet(String name,char[] table){
        if(table.length<256)
            throw new IllegalArgumentException("Translation table must have 256 entries.");
        characterSets.put(name,table);
    }

    static char[] readCharset(InputStream strm)
            throws IOException{
        char[] values=new char[256];
        int i;
        StreamTokenizer in=new StreamTokenizer(new BufferedReader(
                new InputStreamReader(strm,"ISO-8859-1")));
        in.eolIsSignificant(false);
        in.commentChar('#');
        in.slashSlashComments(true);
        in.slashStarComments(true);
        i=0;
        while(i<256){
            int ttype;
            try{
                ttype=in.nextToken();
            }catch(Exception e){
                throw new IOException("Unable to read from character set file ("+e+")");
            }
            if(ttype!=in.TT_NUMBER){
//          System.out.println("Bad token: type=" + ttype + " tok=" + in.sval);
                throw new IOException("Unexpected token in character set file");
//          continue;
            }
            values[i]=(char)(in.nval);
            i++;
        }
        return values;
    }

    interface Destination{
        void handleBinaryBlob(byte[] data);

        void handleText(String text);

        boolean handleKeyword(String keyword);

        boolean handleKeyword(String keyword,int parameter);

        void begingroup();

        void endgroup(Dictionary oldState);

        void close();
    }

    class DiscardingDestination implements Destination{
        public void handleBinaryBlob(byte[] data){
            /** Discard binary blobs. */
        }

        public void handleText(String text){
            /** Discard text. */
        }

        public boolean handleKeyword(String text){
            /** Accept and discard keywords. */
            return true;
        }

        public boolean handleKeyword(String text,int parameter){
            /** Accept and discard parameterized keywords. */
            return true;
        }

        public void begingroup(){
            /** Ignore groups --- the RTFReader will keep track of the
             current group level as necessary */
        }

        public void endgroup(Dictionary oldState){
            /** Ignore groups */
        }

        public void close(){
            /** No end-of-destination cleanup needed */
        }
    }

    class FonttblDestination implements Destination{
        int nextFontNumber;
        Integer fontNumberKey=null;
        String nextFontFamily;

        public void handleBinaryBlob(byte[] data){ /** Discard binary blobs. */}

        public void handleText(String text){
            int semicolon=text.indexOf(';');
            String fontName;
            if(semicolon>-1)
                fontName=text.substring(0,semicolon);
            else
                fontName=text;
            /** TODO: do something with the font family. */
            if(nextFontNumber==-1
                    &&fontNumberKey!=null){
                //font name might be broken across multiple calls
                fontName=fontTable.get(fontNumberKey)+fontName;
            }else{
                fontNumberKey=Integer.valueOf(nextFontNumber);
            }
            fontTable.put(fontNumberKey,fontName);
            nextFontNumber=-1;
            nextFontFamily=null;
        }

        public boolean handleKeyword(String keyword){
            if(keyword.charAt(0)=='f'){
                nextFontFamily=keyword.substring(1);
                return true;
            }
            return false;
        }

        public boolean handleKeyword(String keyword,int parameter){
            if(keyword.equals("f")){
                nextFontNumber=parameter;
                return true;
            }
            return false;
        }

        public void begingroup(){
        }

        public void endgroup(Dictionary oldState){
        }

        public void close(){
            Enumeration<Integer> nums=fontTable.keys();
            warning("Done reading font table.");
            while(nums.hasMoreElements()){
                Integer num=nums.nextElement();
                warning("Number "+num+": "+fontTable.get(num));
            }
        }
    }

    class ColortblDestination implements Destination{
        int red, green, blue;
        Vector<Color> proTemTable;

        public ColortblDestination(){
            red=0;
            green=0;
            blue=0;
            proTemTable=new Vector<Color>();
        }

        public void handleText(String text){
            int index;
            for(index=0;index<text.length();index++){
                if(text.charAt(index)==';'){
                    Color newColor;
                    newColor=new Color(red,green,blue);
                    proTemTable.addElement(newColor);
                }
            }
        }

        public void close(){
            int count=proTemTable.size();
            warning("Done reading color table, "+count+" entries.");
            colorTable=new Color[count];
            proTemTable.copyInto(colorTable);
        }

        public boolean handleKeyword(String keyword,int parameter){
            if(keyword.equals("red"))
                red=parameter;
            else if(keyword.equals("green"))
                green=parameter;
            else if(keyword.equals("blue"))
                blue=parameter;
            else
                return false;
            return true;
        }

        public boolean handleKeyword(String keyword){
            return false;
        }

        public void begingroup(){
        }

        public void endgroup(Dictionary oldState){
        }

        public void handleBinaryBlob(byte[] data){
        }
    }

    class StylesheetDestination
            extends DiscardingDestination
            implements Destination{
        Dictionary<Integer,StyleDefiningDestination> definedStyles;

        public StylesheetDestination(){
            definedStyles=new Hashtable<Integer,StyleDefiningDestination>();
        }

        public void begingroup(){
            setRTFDestination(new StyleDefiningDestination());
        }

        public void close(){
            Vector<Style> chrStyles=new Vector<Style>();
            Vector<Style> pgfStyles=new Vector<Style>();
            Vector<Style> secStyles=new Vector<Style>();
            Enumeration<StyleDefiningDestination> styles=definedStyles.elements();
            while(styles.hasMoreElements()){
                StyleDefiningDestination style;
                Style defined;
                style=styles.nextElement();
                defined=style.realize();
                warning("Style "+style.number+" ("+style.styleName+"): "+defined);
                String stype=(String)defined.getAttribute(Constants.StyleType);
                Vector<Style> toSet;
                if(stype.equals(Constants.STSection)){
                    toSet=secStyles;
                }else if(stype.equals(Constants.STCharacter)){
                    toSet=chrStyles;
                }else{
                    toSet=pgfStyles;
                }
                if(toSet.size()<=style.number)
                    toSet.setSize(style.number+1);
                toSet.setElementAt(defined,style.number);
            }
            if(!(chrStyles.isEmpty())){
                Style[] styleArray=new Style[chrStyles.size()];
                chrStyles.copyInto(styleArray);
                characterStyles=styleArray;
            }
            if(!(pgfStyles.isEmpty())){
                Style[] styleArray=new Style[pgfStyles.size()];
                pgfStyles.copyInto(styleArray);
                paragraphStyles=styleArray;
            }
            if(!(secStyles.isEmpty())){
                Style[] styleArray=new Style[secStyles.size()];
                secStyles.copyInto(styleArray);
                sectionStyles=styleArray;
            }
/** (old debugging code)
 int i, m;
 if (characterStyles != null) {
 m = characterStyles.length;
 for(i=0;i<m;i++)
 warnings.println("chrStyle["+i+"]="+characterStyles[i]);
 } else warnings.println("No character styles.");
 if (paragraphStyles != null) {
 m = paragraphStyles.length;
 for(i=0;i<m;i++)
 warnings.println("pgfStyle["+i+"]="+paragraphStyles[i]);
 } else warnings.println("No paragraph styles.");
 if (sectionStyles != null) {
 m = characterStyles.length;
 for(i=0;i<m;i++)
 warnings.println("secStyle["+i+"]="+sectionStyles[i]);
 } else warnings.println("No section styles.");
 */
        }

        class StyleDefiningDestination
                extends AttributeTrackingDestination
                implements Destination{
            final int STYLENUMBER_NONE=222;
            public String styleName;
            public int number;
            boolean additive;
            boolean characterStyle;
            boolean sectionStyle;
            int basedOn;
            int nextStyle;
            boolean hidden;
            Style realizedStyle;

            public StyleDefiningDestination(){
                additive=false;
                characterStyle=false;
                sectionStyle=false;
                styleName=null;
                number=0;
                basedOn=STYLENUMBER_NONE;
                nextStyle=STYLENUMBER_NONE;
                hidden=false;
            }

            public void handleText(String text){
                if(styleName!=null)
                    styleName=styleName+text;
                else
                    styleName=text;
            }

            public Style realize(){
                Style basis=null;
                Style next=null;
                if(realizedStyle!=null)
                    return realizedStyle;
                if(basedOn!=STYLENUMBER_NONE){
                    StyleDefiningDestination styleDest;
                    styleDest=definedStyles.get(Integer.valueOf(basedOn));
                    if(styleDest!=null&&styleDest!=this){
                        basis=styleDest.realize();
                    }
                }
                /** NB: Swing StyleContext doesn't allow distinct styles with
                 the same name; RTF apparently does. This may confuse the
                 user. */
                realizedStyle=target.addStyle(styleName,basis);
                if(characterStyle){
                    realizedStyle.addAttributes(currentTextAttributes());
                    realizedStyle.addAttribute(Constants.StyleType,
                            Constants.STCharacter);
                }else if(sectionStyle){
                    realizedStyle.addAttributes(currentSectionAttributes());
                    realizedStyle.addAttribute(Constants.StyleType,
                            Constants.STSection);
                }else{ /** must be a paragraph style */
                    realizedStyle.addAttributes(currentParagraphAttributes());
                    realizedStyle.addAttribute(Constants.StyleType,
                            Constants.STParagraph);
                }
                if(nextStyle!=STYLENUMBER_NONE){
                    StyleDefiningDestination styleDest;
                    styleDest=definedStyles.get(Integer.valueOf(nextStyle));
                    if(styleDest!=null){
                        next=styleDest.realize();
                    }
                }
                if(next!=null)
                    realizedStyle.addAttribute(Constants.StyleNext,next);
                realizedStyle.addAttribute(Constants.StyleAdditive,
                        Boolean.valueOf(additive));
                realizedStyle.addAttribute(Constants.StyleHidden,
                        Boolean.valueOf(hidden));
                return realizedStyle;
            }            public void close(){
                int semicolon=(styleName==null)?0:styleName.indexOf(';');
                if(semicolon>0)
                    styleName=styleName.substring(0,semicolon);
                definedStyles.put(Integer.valueOf(number),this);
                super.close();
            }

            public boolean handleKeyword(String keyword){
                if(keyword.equals("additive")){
                    additive=true;
                    return true;
                }
                if(keyword.equals("shidden")){
                    hidden=true;
                    return true;
                }
                return super.handleKeyword(keyword);
            }

            public boolean handleKeyword(String keyword,int parameter){
                if(keyword.equals("s")){
                    characterStyle=false;
                    sectionStyle=false;
                    number=parameter;
                }else if(keyword.equals("cs")){
                    characterStyle=true;
                    sectionStyle=false;
                    number=parameter;
                }else if(keyword.equals("ds")){
                    characterStyle=false;
                    sectionStyle=true;
                    number=parameter;
                }else if(keyword.equals("sbasedon")){
                    basedOn=parameter;
                }else if(keyword.equals("snext")){
                    nextStyle=parameter;
                }else{
                    return super.handleKeyword(keyword,parameter);
                }
                return true;
            }


        }
    }

    class InfoDestination
            extends DiscardingDestination
            implements Destination{
    }

    abstract class AttributeTrackingDestination implements Destination{
        MutableAttributeSet characterAttributes;
        MutableAttributeSet paragraphAttributes;
        MutableAttributeSet sectionAttributes;

        public AttributeTrackingDestination(){
            characterAttributes=rootCharacterAttributes();
            parserState.put("chr",characterAttributes);
            paragraphAttributes=rootParagraphAttributes();
            parserState.put("pgf",paragraphAttributes);
            sectionAttributes=rootSectionAttributes();
            parserState.put("sec",sectionAttributes);
        }

        protected MutableAttributeSet rootCharacterAttributes(){
            MutableAttributeSet set=new SimpleAttributeSet();
            /** TODO: default font */
            StyleConstants.setItalic(set,false);
            StyleConstants.setBold(set,false);
            StyleConstants.setUnderline(set,false);
            StyleConstants.setForeground(set,defaultColor());
            return set;
        }        abstract public void handleText(String text);

        protected MutableAttributeSet rootParagraphAttributes(){
            MutableAttributeSet set=new SimpleAttributeSet();
            StyleConstants.setLeftIndent(set,0f);
            StyleConstants.setRightIndent(set,0f);
            StyleConstants.setFirstLineIndent(set,0f);
            /** TODO: what should this be, really? */
            set.setResolveParent(target.getStyle(StyleContext.DEFAULT_STYLE));
            return set;
        }        public void handleBinaryBlob(byte[] data){
            /** This should really be in TextHandlingDestination, but
             * since *nobody* does anything with binary blobs, this
             * is more convenient. */
            warning("Unexpected binary data in RTF file.");
        }

        protected MutableAttributeSet rootSectionAttributes(){
            MutableAttributeSet set=new SimpleAttributeSet();
            return set;
        }        public void begingroup(){
            AttributeSet characterParent=currentTextAttributes();
            AttributeSet paragraphParent=currentParagraphAttributes();
            AttributeSet sectionParent=currentSectionAttributes();
            /** It would probably be more efficient to use the
             * resolver property of the attributes set for
             * implementing rtf groups,
             * but that's needed for styles. */
            /** update the cached attribute dictionaries */
            characterAttributes=new SimpleAttributeSet();
            characterAttributes.addAttributes(characterParent);
            parserState.put("chr",characterAttributes);
            paragraphAttributes=new SimpleAttributeSet();
            paragraphAttributes.addAttributes(paragraphParent);
            parserState.put("pgf",paragraphAttributes);
            sectionAttributes=new SimpleAttributeSet();
            sectionAttributes.addAttributes(sectionParent);
            parserState.put("sec",sectionAttributes);
        }

        public void endgroup(Dictionary oldState){
            characterAttributes=(MutableAttributeSet)parserState.get("chr");
            paragraphAttributes=(MutableAttributeSet)parserState.get("pgf");
            sectionAttributes=(MutableAttributeSet)parserState.get("sec");
        }

        public void close(){
        }

        public boolean handleKeyword(String keyword){
            if(keyword.equals("ulnone")){
                return handleKeyword("ul",0);
            }
            {
                RTFAttribute attr=straightforwardAttributes.get(keyword);
                if(attr!=null){
                    boolean ok;
                    switch(attr.domain()){
                        case RTFAttribute.D_CHARACTER:
                            ok=attr.set(characterAttributes);
                            break;
                        case RTFAttribute.D_PARAGRAPH:
                            ok=attr.set(paragraphAttributes);
                            break;
                        case RTFAttribute.D_SECTION:
                            ok=attr.set(sectionAttributes);
                            break;
                        case RTFAttribute.D_META:
                            mockery.backing=parserState;
                            ok=attr.set(mockery);
                            mockery.backing=null;
                            break;
                        case RTFAttribute.D_DOCUMENT:
                            ok=attr.set(documentAttributes);
                            break;
                        default:
                            /** should never happen */
                            ok=false;
                            break;
                    }
                    if(ok)
                        return true;
                }
            }
            if(keyword.equals("plain")){
                resetCharacterAttributes();
                return true;
            }
            if(keyword.equals("pard")){
                resetParagraphAttributes();
                return true;
            }
            if(keyword.equals("sectd")){
                resetSectionAttributes();
                return true;
            }
            return false;
        }

        public boolean handleKeyword(String keyword,int parameter){
            boolean booleanParameter=(parameter!=0);
            if(keyword.equals("fc"))
                keyword="cf"; /** whatEVER, dude. */
            if(keyword.equals("f")){
                parserState.put(keyword,Integer.valueOf(parameter));
                return true;
            }
            if(keyword.equals("cf")){
                parserState.put(keyword,Integer.valueOf(parameter));
                return true;
            }
            {
                RTFAttribute attr=straightforwardAttributes.get(keyword);
                if(attr!=null){
                    boolean ok;
                    switch(attr.domain()){
                        case RTFAttribute.D_CHARACTER:
                            ok=attr.set(characterAttributes,parameter);
                            break;
                        case RTFAttribute.D_PARAGRAPH:
                            ok=attr.set(paragraphAttributes,parameter);
                            break;
                        case RTFAttribute.D_SECTION:
                            ok=attr.set(sectionAttributes,parameter);
                            break;
                        case RTFAttribute.D_META:
                            mockery.backing=parserState;
                            ok=attr.set(mockery,parameter);
                            mockery.backing=null;
                            break;
                        case RTFAttribute.D_DOCUMENT:
                            ok=attr.set(documentAttributes,parameter);
                            break;
                        default:
                            /** should never happen */
                            ok=false;
                            break;
                    }
                    if(ok)
                        return true;
                }
            }
            if(keyword.equals("fs")){
                StyleConstants.setFontSize(characterAttributes,(parameter/2));
                return true;
            }
            /** TODO: superscript/subscript */
            if(keyword.equals("sl")){
                if(parameter==1000){  /** magic value! */
                    characterAttributes.removeAttribute(StyleConstants.LineSpacing);
                }else{
                    /** TODO: The RTF sl attribute has special meaning if it's
                     negative. Make sure that SwingText has the same special
                     meaning, or find a way to imitate that. When SwingText
                     handles this, also recognize the slmult keyword. */
                    StyleConstants.setLineSpacing(characterAttributes,
                            parameter/20f);
                }
                return true;
            }
            /** TODO: Other kinds of underlining */
            if(keyword.equals("tx")||keyword.equals("tb")){
                float tabPosition=parameter/20f;
                int tabAlignment, tabLeader;
                Number item;
                tabAlignment=TabStop.ALIGN_LEFT;
                item=(Number)(parserState.get("tab_alignment"));
                if(item!=null)
                    tabAlignment=item.intValue();
                tabLeader=TabStop.LEAD_NONE;
                item=(Number)(parserState.get("tab_leader"));
                if(item!=null)
                    tabLeader=item.intValue();
                if(keyword.equals("tb"))
                    tabAlignment=TabStop.ALIGN_BAR;
                parserState.remove("tab_alignment");
                parserState.remove("tab_leader");
                TabStop newStop=new TabStop(tabPosition,tabAlignment,tabLeader);
                Dictionary<Object,Object> tabs;
                Integer stopCount;
                tabs=(Dictionary<Object,Object>)parserState.get("_tabs");
                if(tabs==null){
                    tabs=new Hashtable<Object,Object>();
                    parserState.put("_tabs",tabs);
                    stopCount=Integer.valueOf(1);
                }else{
                    stopCount=(Integer)tabs.get("stop count");
                    stopCount=Integer.valueOf(1+stopCount.intValue());
                }
                tabs.put(stopCount,newStop);
                tabs.put("stop count",stopCount);
                parserState.remove("_tabs_immutable");
                return true;
            }
            if(keyword.equals("s")&&
                    paragraphStyles!=null){
                parserState.put("paragraphStyle",paragraphStyles[parameter]);
                return true;
            }
            if(keyword.equals("cs")&&
                    characterStyles!=null){
                parserState.put("characterStyle",characterStyles[parameter]);
                return true;
            }
            if(keyword.equals("ds")&&
                    sectionStyles!=null){
                parserState.put("sectionStyle",sectionStyles[parameter]);
                return true;
            }
            return false;
        }







        MutableAttributeSet currentTextAttributes(){
            MutableAttributeSet attributes=
                    new SimpleAttributeSet(characterAttributes);
            Integer fontnum;
            Integer stateItem;
            /** figure out the font name */
            /** TODO: catch exceptions for undefined attributes,
             bad font indices, etc.? (as it stands, it is the caller's
             job to clean up after corrupt RTF) */
            fontnum=(Integer)parserState.get("f");
            /** note setFontFamily() can not handle a null font */
            String fontFamily;
            if(fontnum!=null)
                fontFamily=fontTable.get(fontnum);
            else
                fontFamily=null;
            if(fontFamily!=null)
                StyleConstants.setFontFamily(attributes,fontFamily);
            else
                attributes.removeAttribute(StyleConstants.FontFamily);
            if(colorTable!=null){
                stateItem=(Integer)parserState.get("cf");
                if(stateItem!=null){
                    Color fg=colorTable[stateItem.intValue()];
                    StyleConstants.setForeground(attributes,fg);
                }else{
                    /** AttributeSet dies if you set a value to null */
                    attributes.removeAttribute(StyleConstants.Foreground);
                }
            }
            if(colorTable!=null){
                stateItem=(Integer)parserState.get("cb");
                if(stateItem!=null){
                    Color bg=colorTable[stateItem.intValue()];
                    attributes.addAttribute(StyleConstants.Background,
                            bg);
                }else{
                    /** AttributeSet dies if you set a value to null */
                    attributes.removeAttribute(StyleConstants.Background);
                }
            }
            Style characterStyle=(Style)parserState.get("characterStyle");
            if(characterStyle!=null)
                attributes.setResolveParent(characterStyle);
            /** Other attributes are maintained directly in "attributes" */
            return attributes;
        }

        MutableAttributeSet currentParagraphAttributes(){
            /** NB if there were a mutableCopy() method we should use it */
            MutableAttributeSet bld=new SimpleAttributeSet(paragraphAttributes);
            Integer stateItem;
            /*** Tab stops ***/
            TabStop tabs[];
            tabs=(TabStop[])parserState.get("_tabs_immutable");
            if(tabs==null){
                Dictionary workingTabs=(Dictionary)parserState.get("_tabs");
                if(workingTabs!=null){
                    int count=((Integer)workingTabs.get("stop count")).intValue();
                    tabs=new TabStop[count];
                    for(int ix=1;ix<=count;ix++)
                        tabs[ix-1]=(TabStop)workingTabs.get(Integer.valueOf(ix));
                    parserState.put("_tabs_immutable",tabs);
                }
            }
            if(tabs!=null)
                bld.addAttribute(Constants.Tabs,tabs);
            Style paragraphStyle=(Style)parserState.get("paragraphStyle");
            if(paragraphStyle!=null)
                bld.setResolveParent(paragraphStyle);
            return bld;
        }

        public AttributeSet currentSectionAttributes(){
            MutableAttributeSet attributes=new SimpleAttributeSet(sectionAttributes);
            Style sectionStyle=(Style)parserState.get("sectionStyle");
            if(sectionStyle!=null)
                attributes.setResolveParent(sectionStyle);
            return attributes;
        }

        protected void resetCharacterAttributes(){
            handleKeyword("f",0);
            handleKeyword("cf",0);
            handleKeyword("fs",24);  /** 12 pt. */
            Enumeration<RTFAttribute> attributes=straightforwardAttributes.elements();
            while(attributes.hasMoreElements()){
                RTFAttribute attr=attributes.nextElement();
                if(attr.domain()==RTFAttribute.D_CHARACTER)
                    attr.setDefault(characterAttributes);
            }
            handleKeyword("sl",1000);
            parserState.remove("characterStyle");
        }

        protected void resetParagraphAttributes(){
            parserState.remove("_tabs");
            parserState.remove("_tabs_immutable");
            parserState.remove("paragraphStyle");
            StyleConstants.setAlignment(paragraphAttributes,
                    StyleConstants.ALIGN_LEFT);
            Enumeration<RTFAttribute> attributes=straightforwardAttributes.elements();
            while(attributes.hasMoreElements()){
                RTFAttribute attr=attributes.nextElement();
                if(attr.domain()==RTFAttribute.D_PARAGRAPH)
                    attr.setDefault(characterAttributes);
            }
        }

        protected void resetSectionAttributes(){
            Enumeration<RTFAttribute> attributes=straightforwardAttributes.elements();
            while(attributes.hasMoreElements()){
                RTFAttribute attr=attributes.nextElement();
                if(attr.domain()==RTFAttribute.D_SECTION)
                    attr.setDefault(characterAttributes);
            }
            parserState.remove("sectionStyle");
        }
    }

    abstract class TextHandlingDestination
            extends AttributeTrackingDestination
            implements Destination{
        boolean inParagraph;

        public TextHandlingDestination(){
            super();
            inParagraph=false;
        }

        public void handleText(String text){
            if(!inParagraph)
                beginParagraph();
            deliverText(text,currentTextAttributes());
        }

        abstract void deliverText(String text,AttributeSet characterAttributes);

        public void close(){
            if(inParagraph)
                endParagraph();
            super.close();
        }

        public boolean handleKeyword(String keyword){
            if(keyword.equals("\r")||keyword.equals("\n")){
                keyword="par";
            }
            if(keyword.equals("par")){
//          warnings.println("Ending paragraph.");
                endParagraph();
                return true;
            }
            if(keyword.equals("sect")){
//          warnings.println("Ending section.");
                endSection();
                return true;
            }
            return super.handleKeyword(keyword);
        }

        protected void beginParagraph(){
            inParagraph=true;
        }

        protected void endParagraph(){
            AttributeSet pgfAttributes=currentParagraphAttributes();
            AttributeSet chrAttributes=currentTextAttributes();
            finishParagraph(pgfAttributes,chrAttributes);
            inParagraph=false;
        }

        abstract void finishParagraph(AttributeSet pgfA,AttributeSet chrA);

        abstract void endSection();
    }

    class DocumentDestination
            extends TextHandlingDestination
            implements Destination{
        public void deliverText(String text,AttributeSet characterAttributes){
            try{
                target.insertString(target.getLength(),
                        text,
                        currentTextAttributes());
            }catch(BadLocationException ble){
                /** This shouldn't be able to happen, of course */
                /** TODO is InternalError the correct error to throw? */
                throw new InternalError(ble.getMessage(),ble);
            }
        }

        public void finishParagraph(AttributeSet pgfAttributes,
                                    AttributeSet chrAttributes){
            int pgfEndPosition=target.getLength();
            try{
                target.insertString(pgfEndPosition,"\n",chrAttributes);
                target.setParagraphAttributes(pgfEndPosition,1,pgfAttributes,true);
            }catch(BadLocationException ble){
                /** This shouldn't be able to happen, of course */
                /** TODO is InternalError the correct error to throw? */
                throw new InternalError(ble.getMessage(),ble);
            }
        }

        public void endSection(){
            /** If we implemented sections, we'd end 'em here */
        }
    }
}
