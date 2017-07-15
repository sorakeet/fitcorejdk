/**
 * Copyright (c) 1999, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.loading;
// java import

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MLetContent{
    private Map<String,String> attributes;
    private List<String> types;
    private List<String> values;
    private URL documentURL;
    private URL baseURL;

    public MLetContent(URL url,Map<String,String> attributes,
                       List<String> types,List<String> values){
        this.documentURL=url;
        this.attributes=Collections.unmodifiableMap(attributes);
        this.types=Collections.unmodifiableList(types);
        this.values=Collections.unmodifiableList(values);
        // Initialize baseURL
        //
        String att=getParameter("codebase");
        if(att!=null){
            if(!att.endsWith("/")){
                att+="/";
            }
            try{
                baseURL=new URL(documentURL,att);
            }catch(MalformedURLException e){
                // OK : Move to next block as baseURL could not be initialized.
            }
        }
        if(baseURL==null){
            String file=documentURL.getFile();
            int i=file.lastIndexOf('/');
            if(i>=0&&i<file.length()-1){
                try{
                    baseURL=new URL(documentURL,file.substring(0,i+1));
                }catch(MalformedURLException e){
                    // OK : Move to next block as baseURL could not be initialized.
                }
            }
        }
        if(baseURL==null)
            baseURL=documentURL;
    }
    // GETTERS AND SETTERS
    //--------------------

    private String getParameter(String name){
        return attributes.get(name.toLowerCase());
    }

    public Map<String,String> getAttributes(){
        return attributes;
    }

    public URL getDocumentBase(){
        return documentURL;
    }

    public URL getCodeBase(){
        return baseURL;
    }

    public String getJarFiles(){
        return getParameter("archive");
    }

    public String getCode(){
        return getParameter("code");
    }

    public String getSerializedObject(){
        return getParameter("object");
    }

    public String getName(){
        return getParameter("name");
    }

    public String getVersion(){
        return getParameter("version");
    }

    public List<String> getParameterTypes(){
        return types;
    }

    public List<String> getParameterValues(){
        return values;
    }
}
