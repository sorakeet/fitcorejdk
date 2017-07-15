/**
 * Copyright (c) 1998, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html;

import javax.swing.text.*;
import java.awt.*;
import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;

public class MinimalHTMLWriter extends AbstractWriter{
    private static final int BOLD=0x01;
    private static final int ITALIC=0x02;
    private static final int UNDERLINE=0x04;
    // Used to map StyleConstants to CSS.
    private static final CSS css=new CSS();
    int startOffset=0;
    int endOffset=0;
    private int fontMask=0;
    private AttributeSet fontAttributes;
    private Hashtable<String,String> styleNameMapping;

    public MinimalHTMLWriter(Writer w,StyledDocument doc){
        super(w,doc);
    }

    public MinimalHTMLWriter(Writer w,StyledDocument doc,int pos,int len){
        super(w,doc,pos,len);
    }

    public void write() throws IOException, BadLocationException{
        styleNameMapping=new Hashtable<String,String>();
        writeStartTag("<html>");
        writeHeader();
        writeBody();
        writeEndTag("</html>");
    }

    protected void text(Element elem) throws IOException, BadLocationException{
        String contentStr=getText(elem);
        if((contentStr.length()>0)&&
                (contentStr.charAt(contentStr.length()-1)==NEWLINE)){
            contentStr=contentStr.substring(0,contentStr.length()-1);
        }
        if(contentStr.length()>0){
            write(contentStr);
        }
    }

    protected void writeAttributes(AttributeSet attr) throws IOException{
        Enumeration attributeNames=attr.getAttributeNames();
        while(attributeNames.hasMoreElements()){
            Object name=attributeNames.nextElement();
            if((name instanceof StyleConstants.ParagraphConstants)||
                    (name instanceof StyleConstants.CharacterConstants)||
                    (name instanceof StyleConstants.FontConstants)||
                    (name instanceof StyleConstants.ColorConstants)){
                indent();
                write(name.toString());
                write(':');
                write(css.styleConstantsValueToCSSValue
                        ((StyleConstants)name,attr.getAttribute(name)).
                        toString());
                write(';');
                write(NEWLINE);
            }
        }
    }

    protected void writeHeader() throws IOException{
        writeStartTag("<head>");
        writeStartTag("<style>");
        writeStartTag("<!--");
        writeStyles();
        writeEndTag("-->");
        writeEndTag("</style>");
        writeEndTag("</head>");
    }

    protected void writeStyles() throws IOException{
        /**
         *  Access to DefaultStyledDocument done to workaround
         *  a missing API in styled document to access the
         *  stylenames.
         */
        DefaultStyledDocument styledDoc=((DefaultStyledDocument)getDocument());
        Enumeration styleNames=styledDoc.getStyleNames();
        while(styleNames.hasMoreElements()){
            Style s=styledDoc.getStyle((String)styleNames.nextElement());
            /** PENDING: Once the name attribute is removed
             from the list we check check for 0. **/
            if(s.getAttributeCount()==1&&
                    s.isDefined(StyleConstants.NameAttribute)){
                continue;
            }
            indent();
            write("p."+addStyleName(s.getName()));
            write(" {\n");
            incrIndent();
            writeAttributes(s);
            decrIndent();
            indent();
            write("}\n");
        }
    }

    protected void writeBody() throws IOException, BadLocationException{
        ElementIterator it=getElementIterator();
        /**
         This will be a section element for a styled document.
         We represent this element in HTML as the body tags.
         Therefore we ignore it.
         */
        it.current();
        Element next;
        writeStartTag("<body>");
        boolean inContent=false;
        while((next=it.next())!=null){
            if(!inRange(next)){
                continue;
            }
            if(next instanceof AbstractDocument.BranchElement){
                if(inContent){
                    writeEndParagraph();
                    inContent=false;
                    fontMask=0;
                }
                writeStartParagraph(next);
            }else if(isText(next)){
                writeContent(next,!inContent);
                inContent=true;
            }else{
                writeLeaf(next);
                inContent=true;
            }
        }
        if(inContent){
            writeEndParagraph();
        }
        writeEndTag("</body>");
    }

    protected void writeEndParagraph() throws IOException{
        writeEndMask(fontMask);
        if(inFontTag()){
            endSpanTag();
        }else{
            write(NEWLINE);
        }
        writeEndTag("</p>");
    }

    protected void writeStartParagraph(Element elem) throws IOException{
        AttributeSet attr=elem.getAttributes();
        Object resolveAttr=attr.getAttribute(StyleConstants.ResolveAttribute);
        if(resolveAttr instanceof StyleContext.NamedStyle){
            writeStartTag("<p class="+mapStyleName(((StyleContext.NamedStyle)resolveAttr).getName())+">");
        }else{
            writeStartTag("<p>");
        }
    }

    protected void writeLeaf(Element elem) throws IOException{
        indent();
        if(elem.getName()==StyleConstants.IconElementName){
            writeImage(elem);
        }else if(elem.getName()==StyleConstants.ComponentElementName){
            writeComponent(elem);
        }
    }

    protected void writeImage(Element elem) throws IOException{
    }

    protected void writeComponent(Element elem) throws IOException{
    }

    protected boolean isText(Element elem){
        return (elem.getName()==AbstractDocument.ContentElementName);
    }

    protected void writeContent(Element elem,boolean needsIndenting)
            throws IOException, BadLocationException{
        AttributeSet attr=elem.getAttributes();
        writeNonHTMLAttributes(attr);
        if(needsIndenting){
            indent();
        }
        writeHTMLTags(attr);
        text(elem);
    }

    protected void writeHTMLTags(AttributeSet attr) throws IOException{
        int oldMask=fontMask;
        setFontMask(attr);
        int endMask=0;
        int startMask=0;
        if((oldMask&BOLD)!=0){
            if((fontMask&BOLD)==0){
                endMask|=BOLD;
            }
        }else if((fontMask&BOLD)!=0){
            startMask|=BOLD;
        }
        if((oldMask&ITALIC)!=0){
            if((fontMask&ITALIC)==0){
                endMask|=ITALIC;
            }
        }else if((fontMask&ITALIC)!=0){
            startMask|=ITALIC;
        }
        if((oldMask&UNDERLINE)!=0){
            if((fontMask&UNDERLINE)==0){
                endMask|=UNDERLINE;
            }
        }else if((fontMask&UNDERLINE)!=0){
            startMask|=UNDERLINE;
        }
        writeEndMask(endMask);
        writeStartMask(startMask);
    }

    private void setFontMask(AttributeSet attr){
        if(StyleConstants.isBold(attr)){
            fontMask|=BOLD;
        }
        if(StyleConstants.isItalic(attr)){
            fontMask|=ITALIC;
        }
        if(StyleConstants.isUnderline(attr)){
            fontMask|=UNDERLINE;
        }
    }

    private void writeStartMask(int mask) throws IOException{
        if(mask!=0){
            if((mask&UNDERLINE)!=0){
                write("<u>");
            }
            if((mask&ITALIC)!=0){
                write("<i>");
            }
            if((mask&BOLD)!=0){
                write("<b>");
            }
        }
    }

    private void writeEndMask(int mask) throws IOException{
        if(mask!=0){
            if((mask&BOLD)!=0){
                write("</b>");
            }
            if((mask&ITALIC)!=0){
                write("</i>");
            }
            if((mask&UNDERLINE)!=0){
                write("</u>");
            }
        }
    }

    protected void writeNonHTMLAttributes(AttributeSet attr) throws IOException{
        String style="";
        String separator="; ";
        if(inFontTag()&&fontAttributes.isEqual(attr)){
            return;
        }
        boolean first=true;
        Color color=(Color)attr.getAttribute(StyleConstants.Foreground);
        if(color!=null){
            style+="color: "+css.styleConstantsValueToCSSValue
                    ((StyleConstants)StyleConstants.Foreground,
                            color);
            first=false;
        }
        Integer size=(Integer)attr.getAttribute(StyleConstants.FontSize);
        if(size!=null){
            if(!first){
                style+=separator;
            }
            style+="font-size: "+size.intValue()+"pt";
            first=false;
        }
        String family=(String)attr.getAttribute(StyleConstants.FontFamily);
        if(family!=null){
            if(!first){
                style+=separator;
            }
            style+="font-family: "+family;
            first=false;
        }
        if(style.length()>0){
            if(fontMask!=0){
                writeEndMask(fontMask);
                fontMask=0;
            }
            startSpanTag(style);
            fontAttributes=attr;
        }else if(fontAttributes!=null){
            writeEndMask(fontMask);
            fontMask=0;
            endSpanTag();
        }
    }

    protected void startFontTag(String style) throws IOException{
        boolean callIndent=false;
        if(inFontTag()){
            endFontTag();
            callIndent=true;
        }
        writeStartTag("<font style=\""+style+"\">");
        if(callIndent){
            indent();
        }
    }

    protected void writeStartTag(String tag) throws IOException{
        indent();
        write(tag);
        write(NEWLINE);
        incrIndent();
    }

    protected boolean inFontTag(){
        return (fontAttributes!=null);
    }

    protected void endFontTag() throws IOException{
        write(NEWLINE);
        writeEndTag("</font>");
        fontAttributes=null;
    }

    protected void writeEndTag(String endTag) throws IOException{
        decrIndent();
        indent();
        write(endTag);
        write(NEWLINE);
    }

    private void startSpanTag(String style) throws IOException{
        boolean callIndent=false;
        if(inFontTag()){
            endSpanTag();
            callIndent=true;
        }
        writeStartTag("<span style=\""+style+"\">");
        if(callIndent){
            indent();
        }
    }

    private void endSpanTag() throws IOException{
        write(NEWLINE);
        writeEndTag("</span>");
        fontAttributes=null;
    }

    private String addStyleName(String style){
        if(styleNameMapping==null){
            return style;
        }
        StringBuilder sb=null;
        for(int counter=style.length()-1;counter>=0;counter--){
            if(!isValidCharacter(style.charAt(counter))){
                if(sb==null){
                    sb=new StringBuilder(style);
                }
                sb.setCharAt(counter,'a');
            }
        }
        String mappedName=(sb!=null)?sb.toString():style;
        while(styleNameMapping.get(mappedName)!=null){
            mappedName=mappedName+'x';
        }
        styleNameMapping.put(style,mappedName);
        return mappedName;
    }

    private String mapStyleName(String style){
        if(styleNameMapping==null){
            return style;
        }
        String retValue=styleNameMapping.get(style);
        return (retValue==null)?style:retValue;
    }

    private boolean isValidCharacter(char character){
        return ((character>='a'&&character<='z')||
                (character>='A'&&character<='Z'));
    }
}
