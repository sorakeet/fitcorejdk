/**
 * Copyright (c) 2012, 2015, Oracle and/or its affiliates. All rights reserved.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * $Id: XSLTC.java,v 1.2.4.1 2005/09/05 09:51:38 pvedula Exp $
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * $Id: XSLTC.java,v 1.2.4.1 2005/09/05 09:51:38 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.compiler;

import com.sun.org.apache.bcel.internal.classfile.JavaClass;
import com.sun.org.apache.xalan.internal.XalanConstants;
import com.sun.org.apache.xalan.internal.utils.FeatureManager;
import com.sun.org.apache.xalan.internal.utils.FeatureManager.Feature;
import com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import com.sun.org.apache.xalan.internal.utils.XMLSecurityManager;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import com.sun.org.apache.xml.internal.dtm.DTM;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public final class XSLTC{
    // These define the various methods for outputting the translet
    public static final int FILE_OUTPUT=0;
    public static final int JAR_OUTPUT=1;
    public static final int BYTEARRAY_OUTPUT=2;
    public static final int CLASSLOADER_OUTPUT=3;
    public static final int BYTEARRAY_AND_FILE_OUTPUT=4;
    public static final int BYTEARRAY_AND_JAR_OUTPUT=5;
    private final FeatureManager _featureManager;
    private final Map<String,Class> _externalExtensionFunctions;
    // A reference to the main stylesheet parser object.
    private Parser _parser;
    // A reference to an external XMLReader (SAX parser) passed to us
    private XMLReader _reader=null;
    // A reference to an external SourceLoader (for use with include/import)
    private SourceLoader _loader=null;
    // A reference to the stylesheet being compiled.
    private Stylesheet _stylesheet;
    // Counters used by various classes to generate unique names.
    // private int _variableSerial     = 1;
    private int _modeSerial=1;
    private int _stylesheetSerial=1;
    private int _stepPatternSerial=1;
    private int _helperClassSerial=0;
    private int _attributeSetSerial=0;
    private int[] _numberFieldIndexes;
    // Name index tables
    private int _nextGType;  // Next available element type
    private Vector _namesIndex; // Index of all registered QNames
    private Map<String,Integer> _elements;   // Map of all registered elements
    private Map<String,Integer> _attributes; // Map of all registered attributes
    // Namespace index tables
    private int _nextNSType; // Next available namespace type
    private Vector _namespaceIndex; // Index of all registered namespaces
    private Map<String,Integer> _namespaces; // Map of all registered namespaces
    private Map<String,Integer> _namespacePrefixes;// Map of all registered namespace prefixes
    // All literal text in the stylesheet
    private Vector m_characterData;
    // Compiler options (passed from command line or XSLTC client)
    private boolean _debug=false;      // -x
    private String _jarFileName=null; // -j <jar-file-name>
    private String _className=null;   // -o <class-name>
    private String _packageName=null; // -p <package-name>
    private File _destDir=null;     // -d <directory-name>
    private int _outputType=FILE_OUTPUT; // by default
    private Vector _classes;
    private Vector _bcelClasses;
    private boolean _callsNodeset=false;
    private boolean _multiDocument=false;
    private boolean _hasIdCall=false;
    private boolean _templateInlining=false;
    private boolean _isSecureProcessing=false;
    private boolean _useServicesMechanism=true;
    private String _accessExternalStylesheet=XalanConstants.EXTERNAL_ACCESS_DEFAULT;
    private String _accessExternalDTD=XalanConstants.EXTERNAL_ACCESS_DEFAULT;
    private XMLSecurityManager _xmlSecurityManager;
    private ClassLoader _extensionClassLoader;

    public XSLTC(boolean useServicesMechanism,FeatureManager featureManager){
        _parser=new Parser(this,useServicesMechanism);
        _featureManager=featureManager;
        _extensionClassLoader=null;
        _externalExtensionFunctions=new HashMap<>();
    }

    public boolean isSecureProcessing(){
        return _isSecureProcessing;
    }

    public void setSecureProcessing(boolean flag){
        _isSecureProcessing=flag;
    }

    public boolean useServicesMechnism(){
        return _useServicesMechanism;
    }

    public void setServicesMechnism(boolean flag){
        _useServicesMechanism=flag;
    }

    public boolean getFeature(Feature name){
        return _featureManager.isFeatureEnabled(name);
    }

    public Object getProperty(String name){
        if(name.equals(XMLConstants.ACCESS_EXTERNAL_STYLESHEET)){
            return _accessExternalStylesheet;
        }else if(name.equals(XMLConstants.ACCESS_EXTERNAL_DTD)){
            return _accessExternalDTD;
        }else if(name.equals(XalanConstants.SECURITY_MANAGER)){
            return _xmlSecurityManager;
        }else if(name.equals(XalanConstants.JDK_EXTENSION_CLASSLOADER)){
            return _extensionClassLoader;
        }
        return null;
    }

    public void setProperty(String name,Object value){
        if(name.equals(XMLConstants.ACCESS_EXTERNAL_STYLESHEET)){
            _accessExternalStylesheet=(String)value;
        }else if(name.equals(XMLConstants.ACCESS_EXTERNAL_DTD)){
            _accessExternalDTD=(String)value;
        }else if(name.equals(XalanConstants.SECURITY_MANAGER)){
            _xmlSecurityManager=(XMLSecurityManager)value;
        }else if(name.equals(XalanConstants.JDK_EXTENSION_CLASSLOADER)){
            _extensionClassLoader=(ClassLoader)value;
            /** Clear the external extension functions HashMap if extension class
             loader was changed */
            _externalExtensionFunctions.clear();
        }
    }

    public Parser getParser(){
        return _parser;
    }

    public void setOutputType(int type){
        _outputType=type;
    }

    public Properties getOutputProperties(){
        return _parser.getOutputProperties();
    }

    public void init(){
        reset();
        _reader=null;
        _classes=new Vector();
        _bcelClasses=new Vector();
    }

    private void reset(){
        _nextGType=DTM.NTYPES;
        _elements=new HashMap<>();
        _attributes=new HashMap<>();
        _namespaces=new HashMap<>();
        _namespaces.put("",new Integer(_nextNSType));
        _namesIndex=new Vector(128);
        _namespaceIndex=new Vector(32);
        _namespacePrefixes=new HashMap<>();
        _stylesheet=null;
        _parser.init();
        //_variableSerial     = 1;
        _modeSerial=1;
        _stylesheetSerial=1;
        _stepPatternSerial=1;
        _helperClassSerial=0;
        _attributeSetSerial=0;
        _multiDocument=false;
        _hasIdCall=false;
        _numberFieldIndexes=new int[]{
                -1,         // LEVEL_SINGLE
                -1,         // LEVEL_MULTIPLE
                -1          // LEVEL_ANY
        };
        _externalExtensionFunctions.clear();
    }

    Class loadExternalFunction(String name) throws ClassNotFoundException{
        Class loaded=null;
        //Check if the function is not loaded already
        if(_externalExtensionFunctions.containsKey(name)){
            loaded=_externalExtensionFunctions.get(name);
        }else if(_extensionClassLoader!=null){
            loaded=Class.forName(name,true,_extensionClassLoader);
            setExternalExtensionFunctions(name,loaded);
        }
        if(loaded==null){
            throw new ClassNotFoundException(name);
        }
        //Return loaded class
        return (Class)loaded;
    }

    private void setExternalExtensionFunctions(String name,Class clazz){
        if(_isSecureProcessing&&clazz!=null&&!_externalExtensionFunctions.containsKey(name)){
            _externalExtensionFunctions.put(name,clazz);
        }
    }

    public Map<String,Class> getExternalExtensionFunctions(){
        return Collections.unmodifiableMap(_externalExtensionFunctions);
    }

    public void setSourceLoader(SourceLoader loader){
        _loader=loader;
    }

    public boolean getTemplateInlining(){
        return _templateInlining;
    }

    public void setTemplateInlining(boolean templateInlining){
        _templateInlining=templateInlining;
    }

    public void setPIParameters(String media,String title,String charset){
        _parser.setPIParameters(media,title,charset);
    }

    public boolean compile(URL url,String name){
        try{
            // Open input stream from URL and wrap inside InputSource
            final InputStream stream=url.openStream();
            final InputSource input=new InputSource(stream);
            input.setSystemId(url.toString());
            return compile(input,name);
        }catch(IOException e){
            _parser.reportError(Constants.FATAL,new ErrorMsg(ErrorMsg.JAXP_COMPILE_ERR,e));
            return false;
        }
    }

    public boolean compile(InputSource input,String name){
        try{
            // Reset globals in case we're called by compile(Vector v);
            reset();
            // The systemId may not be set, so we'll have to check the URL
            String systemId=null;
            if(input!=null){
                systemId=input.getSystemId();
            }
            // Set the translet class name if not already set
            if(_className==null){
                if(name!=null){
                    setClassName(name);
                }else if(systemId!=null&&!systemId.equals("")){
                    setClassName(Util.baseName(systemId));
                }
                // Ensure we have a non-empty class name at this point
                if(_className==null||_className.length()==0){
                    setClassName("GregorSamsa"); // default translet name
                }
            }
            // Get the root node of the abstract syntax tree
            SyntaxTreeNode element=null;
            if(_reader==null){
                element=_parser.parse(input);
            }else{
                element=_parser.parse(_reader,input);
            }
            // Compile the translet - this is where the work is done!
            if((!_parser.errorsFound())&&(element!=null)){
                // Create a Stylesheet element from the root node
                _stylesheet=_parser.makeStylesheet(element);
                _stylesheet.setSourceLoader(_loader);
                _stylesheet.setSystemId(systemId);
                _stylesheet.setParentStylesheet(null);
                _stylesheet.setTemplateInlining(_templateInlining);
                _parser.setCurrentStylesheet(_stylesheet);
                // Create AST under the Stylesheet element (parse & type-check)
                _parser.createAST(_stylesheet);
            }
            // Generate the bytecodes and output the translet class(es)
            if((!_parser.errorsFound())&&(_stylesheet!=null)){
                _stylesheet.setCallsNodeset(_callsNodeset);
                _stylesheet.setMultiDocument(_multiDocument);
                _stylesheet.setHasIdCall(_hasIdCall);
                // Class synchronization is needed for BCEL
                synchronized(getClass()){
                    _stylesheet.translate();
                }
            }
        }catch(Exception e){
            /**if (_debug)*/e.printStackTrace();
            _parser.reportError(Constants.FATAL,new ErrorMsg(ErrorMsg.JAXP_COMPILE_ERR,e));
        }catch(Error e){
            if(_debug) e.printStackTrace();
            _parser.reportError(Constants.FATAL,new ErrorMsg(ErrorMsg.JAXP_COMPILE_ERR,e));
        }finally{
            _reader=null; // reset this here to be sure it is not re-used
        }
        return !_parser.errorsFound();
    }

    public boolean compile(InputStream stream,String name){
        final InputSource input=new InputSource(stream);
        input.setSystemId(name); // We have nothing else!!!
        return compile(input,name);
    }

    public boolean compile(Vector stylesheets){
        // Get the number of stylesheets (ie. URLs) in the vector
        final int count=stylesheets.size();
        // Return straight away if the vector is empty
        if(count==0) return true;
        // Special handling needed if the URL count is one, becuase the
        // _className global must not be reset if it was set explicitly
        if(count==1){
            final Object url=stylesheets.firstElement();
            if(url instanceof URL)
                return compile((URL)url);
            else
                return false;
        }else{
            // Traverse all elements in the vector and compile
            final Enumeration urls=stylesheets.elements();
            while(urls.hasMoreElements()){
                _className=null; // reset, so that new name will be computed
                final Object url=urls.nextElement();
                if(url instanceof URL){
                    if(!compile((URL)url)) return false;
                }
            }
        }
        return true;
    }

    public boolean compile(URL url){
        try{
            // Open input stream from URL and wrap inside InputSource
            final InputStream stream=url.openStream();
            final InputSource input=new InputSource(stream);
            input.setSystemId(url.toString());
            return compile(input,_className);
        }catch(IOException e){
            _parser.reportError(Constants.FATAL,new ErrorMsg(ErrorMsg.JAXP_COMPILE_ERR,e));
            return false;
        }
    }

    public byte[][] compile(String name,InputSource input){
        return compile(name,input,BYTEARRAY_OUTPUT);
    }

    public byte[][] compile(String name,InputSource input,int outputType){
        _outputType=outputType;
        if(compile(input,name))
            return getBytecodes();
        else
            return null;
    }

    public byte[][] getBytecodes(){
        final int count=_classes.size();
        final byte[][] result=new byte[count][1];
        for(int i=0;i<count;i++)
            result[i]=(byte[])_classes.elementAt(i);
        return result;
    }

    public XMLReader getXMLReader(){
        return _reader;
    }

    public void setXMLReader(XMLReader reader){
        _reader=reader;
    }

    public Vector getErrors(){
        return _parser.getErrors();
    }

    public Vector getWarnings(){
        return _parser.getWarnings();
    }

    public void printErrors(){
        _parser.printErrors();
    }

    public void printWarnings(){
        _parser.printWarnings();
    }

    public boolean isMultiDocument(){
        return _multiDocument;
    }

    protected void setMultiDocument(boolean flag){
        _multiDocument=flag;
    }

    protected void setCallsNodeset(boolean flag){
        if(flag) setMultiDocument(flag);
        _callsNodeset=flag;
    }

    public boolean callsNodeset(){
        return _callsNodeset;
    }

    protected void setHasIdCall(boolean flag){
        _hasIdCall=flag;
    }

    public boolean hasIdCall(){
        return _hasIdCall;
    }

    public boolean setDestDirectory(String dstDirName){
        final File dir=new File(dstDirName);
        if(SecuritySupport.getFileExists(dir)||dir.mkdirs()){
            _destDir=dir;
            return true;
        }else{
            _destDir=null;
            return false;
        }
    }

    public void setPackageName(String packageName){
        _packageName=packageName;
        if(_className!=null) setClassName(_className);
    }

    public String getJarFileName(){
        return _jarFileName;
    }

    public void setJarFileName(String jarFileName){
        final String JAR_EXT=".jar";
        if(jarFileName.endsWith(JAR_EXT))
            _jarFileName=jarFileName;
        else
            _jarFileName=jarFileName+JAR_EXT;
        _outputType=JAR_OUTPUT;
    }

    public Stylesheet getStylesheet(){
        return _stylesheet;
    }

    public void setStylesheet(Stylesheet stylesheet){
        if(_stylesheet==null) _stylesheet=stylesheet;
    }

    public int registerAttribute(QName name){
        Integer code=_attributes.get(name.toString());
        if(code==null){
            code=_nextGType++;
            _attributes.put(name.toString(),code);
            final String uri=name.getNamespace();
            final String local="@"+name.getLocalPart();
            if((uri!=null)&&(!uri.equals("")))
                _namesIndex.addElement(uri+":"+local);
            else
                _namesIndex.addElement(local);
            if(name.getLocalPart().equals("*")){
                registerNamespace(name.getNamespace());
            }
        }
        return code.intValue();
    }

    public int registerNamespace(String namespaceURI){
        Integer code=_namespaces.get(namespaceURI);
        if(code==null){
            code=_nextNSType++;
            _namespaces.put(namespaceURI,code);
            _namespaceIndex.addElement(namespaceURI);
        }
        return code.intValue();
    }

    public int registerElement(QName name){
        // Register element (full QName)
        Integer code=_elements.get(name.toString());
        if(code==null){
            _elements.put(name.toString(),code=_nextGType++);
            _namesIndex.addElement(name.toString());
        }
        if(name.getLocalPart().equals("*")){
            registerNamespace(name.getNamespace());
        }
        return code.intValue();
    }

    public int registerNamespacePrefix(QName name){
        Integer code=_namespacePrefixes.get(name.toString());
        if(code==null){
            code=_nextGType++;
            _namespacePrefixes.put(name.toString(),code);
            final String uri=name.getNamespace();
            if((uri!=null)&&(!uri.equals(""))){
                // namespace::ext2:ped2 will be made empty in TypedNamespaceIterator
                _namesIndex.addElement("?");
            }else{
                _namesIndex.addElement("?"+name.getLocalPart());
            }
        }
        return code.intValue();
    }

    public int nextModeSerial(){
        return _modeSerial++;
    }

    public int nextStylesheetSerial(){
        return _stylesheetSerial++;
    }

    public int nextStepPatternSerial(){
        return _stepPatternSerial++;
    }

    public int[] getNumberFieldIndexes(){
        return _numberFieldIndexes;
    }

    public int nextHelperClassSerial(){
        return _helperClassSerial++;
    }

    public int nextAttributeSetSerial(){
        return _attributeSetSerial++;
    }

    public Vector getNamesIndex(){
        return _namesIndex;
    }

    public Vector getNamespaceIndex(){
        return _namespaceIndex;
    }

    public String getHelperClassName(){
        return getClassName()+'$'+_helperClassSerial++;
    }

    public String getClassName(){
        return _className;
    }

    public void setClassName(String className){
        final String base=Util.baseName(className);
        final String noext=Util.noExtName(base);
        String name=Util.toJavaName(noext);
        if(_packageName==null)
            _className=name;
        else
            _className=_packageName+'.'+name;
    }

    public void dumpClass(JavaClass clazz){
        if(_outputType==FILE_OUTPUT||
                _outputType==BYTEARRAY_AND_FILE_OUTPUT){
            File outFile=getOutputFile(clazz.getClassName());
            String parentDir=outFile.getParent();
            if(parentDir!=null){
                File parentFile=new File(parentDir);
                if(!SecuritySupport.getFileExists(parentFile))
                    parentFile.mkdirs();
            }
        }
        try{
            switch(_outputType){
                case FILE_OUTPUT:
                    clazz.dump(
                            new BufferedOutputStream(
                                    new FileOutputStream(
                                            getOutputFile(clazz.getClassName()))));
                    break;
                case JAR_OUTPUT:
                    _bcelClasses.addElement(clazz);
                    break;
                case BYTEARRAY_OUTPUT:
                case BYTEARRAY_AND_FILE_OUTPUT:
                case BYTEARRAY_AND_JAR_OUTPUT:
                case CLASSLOADER_OUTPUT:
                    ByteArrayOutputStream out=new ByteArrayOutputStream(2048);
                    clazz.dump(out);
                    _classes.addElement(out.toByteArray());
                    if(_outputType==BYTEARRAY_AND_FILE_OUTPUT)
                        clazz.dump(new BufferedOutputStream(
                                new FileOutputStream(getOutputFile(clazz.getClassName()))));
                    else if(_outputType==BYTEARRAY_AND_JAR_OUTPUT)
                        _bcelClasses.addElement(clazz);
                    break;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private File getOutputFile(String className){
        if(_destDir!=null)
            return new File(_destDir,classFileName(className));
        else
            return new File(classFileName(className));
    }

    private String classFileName(final String className){
        return className.replace('.',File.separatorChar)+".class";
    }

    private String entryName(File f) throws IOException{
        return f.getName().replace(File.separatorChar,'/');
    }

    public void outputToJar() throws IOException{
        // create the manifest
        final Manifest manifest=new Manifest();
        final java.util.jar.Attributes atrs=manifest.getMainAttributes();
        atrs.put(java.util.jar.Attributes.Name.MANIFEST_VERSION,"1.2");
        final Map map=manifest.getEntries();
        // create manifest
        Enumeration classes=_bcelClasses.elements();
        final String now=(new Date()).toString();
        final java.util.jar.Attributes.Name dateAttr=
                new java.util.jar.Attributes.Name("Date");
        while(classes.hasMoreElements()){
            final JavaClass clazz=(JavaClass)classes.nextElement();
            final String className=clazz.getClassName().replace('.','/');
            final java.util.jar.Attributes attr=new java.util.jar.Attributes();
            attr.put(dateAttr,now);
            map.put(className+".class",attr);
        }
        final File jarFile=new File(_destDir,_jarFileName);
        final JarOutputStream jos=
                new JarOutputStream(new FileOutputStream(jarFile),manifest);
        classes=_bcelClasses.elements();
        while(classes.hasMoreElements()){
            final JavaClass clazz=(JavaClass)classes.nextElement();
            final String className=clazz.getClassName().replace('.','/');
            jos.putNextEntry(new JarEntry(className+".class"));
            final ByteArrayOutputStream out=new ByteArrayOutputStream(2048);
            clazz.dump(out); // dump() closes it's output stream
            out.writeTo(jos);
        }
        jos.close();
    }

    public void setDebug(boolean debug){
        _debug=debug;
    }

    public boolean debug(){
        return _debug;
    }

    public String getCharacterData(int index){
        return ((StringBuffer)m_characterData.elementAt(index)).toString();
    }

    public int getCharacterDataCount(){
        return (m_characterData!=null)?m_characterData.size():0;
    }

    public int addCharacterData(String newData){
        StringBuffer currData;
        if(m_characterData==null){
            m_characterData=new Vector();
            currData=new StringBuffer();
            m_characterData.addElement(currData);
        }else{
            currData=(StringBuffer)m_characterData
                    .elementAt(m_characterData.size()-1);
        }
        // Character data could take up to three-times as much space when
        // written to the class file as UTF-8.  The maximum size for a
        // constant is 65535/3.  If we exceed that,
        // (We really should use some "bin packing".)
        if(newData.length()+currData.length()>21845){
            currData=new StringBuffer();
            m_characterData.addElement(currData);
        }
        int newDataOffset=currData.length();
        currData.append(newData);
        return newDataOffset;
    }
}
