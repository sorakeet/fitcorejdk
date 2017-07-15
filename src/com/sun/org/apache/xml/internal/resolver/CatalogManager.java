/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2004 The Apache Software Foundation or its licensors,
 * as applicable.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// CatalogManager.java - Access CatalogManager.properties
/**
 * Copyright 2001-2004 The Apache Software Foundation or its licensors,
 * as applicable.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.org.apache.xml.internal.resolver;

import com.sun.org.apache.xerces.internal.utils.SecuritySupport;
import com.sun.org.apache.xml.internal.resolver.helpers.BootstrapResolver;
import com.sun.org.apache.xml.internal.resolver.helpers.Debug;
import sun.reflect.misc.ReflectUtil;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class CatalogManager{
    private static String pFiles="xml.catalog.files";
    private static String pVerbosity="xml.catalog.verbosity";
    private static String pPrefer="xml.catalog.prefer";
    private static String pStatic="xml.catalog.staticCatalog";
    private static String pAllowPI="xml.catalog.allowPI";
    private static String pClassname="xml.catalog.className";
    private static String pIgnoreMissing="xml.catalog.ignoreMissing";
    private static CatalogManager staticManager=new CatalogManager();
    private static Catalog staticCatalog=null;
    public Debug debug=null;
    private BootstrapResolver bResolver=new BootstrapResolver();
    private boolean ignoreMissingProperties
            =(SecuritySupport.getSystemProperty(pIgnoreMissing)!=null
            ||SecuritySupport.getSystemProperty(pFiles)!=null);
    private ResourceBundle resources;
    private String propertyFile="CatalogManager.properties";
    private URL propertyFileURI=null;
    private String defaultCatalogFiles="./xcatalog";
    private String catalogFiles=null;
    private boolean fromPropertiesFile=false;
    private int defaultVerbosity=1;
    private Integer verbosity=null;
    private boolean defaultPreferPublic=true;
    private Boolean preferPublic=null;
    private boolean defaultUseStaticCatalog=true;
    private Boolean useStaticCatalog=null;
    private boolean defaultOasisXMLCatalogPI=true;
    private Boolean oasisXMLCatalogPI=null;
    private boolean defaultRelativeCatalogs=true;
    private Boolean relativeCatalogs=null;
    private String catalogClassName=null;
    private boolean useServicesMechanism;

    public CatalogManager(){
        init();
    }

    private void init(){
        debug=new Debug();
        // Note that we don't setDebug() here; we do that lazily. Either the
        // user will set it explicitly, or we'll do it automagically if they
        // read from the propertyFile for some other reason. That way, there's
        // no attempt to read from the file before the caller has had a chance
        // to avoid it.
        if(System.getSecurityManager()==null){
            useServicesMechanism=true;
        }
    }

    public CatalogManager(String propertyFile){
        this.propertyFile=propertyFile;
        init();
    }

    public static CatalogManager getStaticManager(){
        return staticManager;
    }

    public BootstrapResolver getBootstrapResolver(){
        return bResolver;
    }

    public void setBootstrapResolver(BootstrapResolver resolver){
        bResolver=resolver;
    }

    public boolean getIgnoreMissingProperties(){
        return ignoreMissingProperties;
    }

    public void setIgnoreMissingProperties(boolean ignore){
        ignoreMissingProperties=ignore;
    }

    public void ignoreMissingProperties(boolean ignore){
        setIgnoreMissingProperties(ignore);
    }

    public int verbosity(){
        return getVerbosity();
    }

    public int getVerbosity(){
        if(verbosity==null){
            verbosity=new Integer(queryVerbosity());
        }
        return verbosity.intValue();
    }

    private int queryVerbosity(){
        String defaultVerbStr=Integer.toString(defaultVerbosity);
        String verbStr=SecuritySupport.getSystemProperty(pVerbosity);
        if(verbStr==null){
            if(resources==null) readProperties();
            if(resources!=null){
                try{
                    verbStr=resources.getString("verbosity");
                }catch(MissingResourceException e){
                    verbStr=defaultVerbStr;
                }
            }else{
                verbStr=defaultVerbStr;
            }
        }
        int verb=defaultVerbosity;
        try{
            verb=Integer.parseInt(verbStr.trim());
        }catch(Exception e){
            System.err.println("Cannot parse verbosity: \""+verbStr+"\"");
        }
        // This is a bit of a hack. After we've successfully got the verbosity,
        // we have to use it to set the default debug level,
        // if the user hasn't already set the default debug level.
        if(verbosity==null){
            debug.setDebug(verb);
            verbosity=new Integer(verb);
        }
        return verb;
    }

    private synchronized void readProperties(){
        try{
            propertyFileURI=CatalogManager.class.getResource("/"+propertyFile);
            InputStream in=
                    CatalogManager.class.getResourceAsStream("/"+propertyFile);
            if(in==null){
                if(!ignoreMissingProperties){
                    System.err.println("Cannot find "+propertyFile);
                    // there's no reason to give this warning more than once
                    ignoreMissingProperties=true;
                }
                return;
            }
            resources=new PropertyResourceBundle(in);
        }catch(MissingResourceException mre){
            if(!ignoreMissingProperties){
                System.err.println("Cannot read "+propertyFile);
            }
        }catch(java.io.IOException e){
            if(!ignoreMissingProperties){
                System.err.println("Failure trying to read "+propertyFile);
            }
        }
        // This is a bit of a hack. After we've successfully read the properties,
        // use them to set the default debug level, if the user hasn't already set
        // the default debug level.
        if(verbosity==null){
            try{
                String verbStr=resources.getString("verbosity");
                int verb=Integer.parseInt(verbStr.trim());
                debug.setDebug(verb);
                verbosity=new Integer(verb);
            }catch(Exception e){
                // nop
            }
        }
    }

    public void setVerbosity(int verbosity){
        this.verbosity=new Integer(verbosity);
        debug.setDebug(verbosity);
    }

    public Vector catalogFiles(){
        return getCatalogFiles();
    }

    public Vector getCatalogFiles(){
        if(catalogFiles==null){
            catalogFiles=queryCatalogFiles();
        }
        StringTokenizer files=new StringTokenizer(catalogFiles,";");
        Vector catalogs=new Vector();
        while(files.hasMoreTokens()){
            String catalogFile=files.nextToken();
            URL absURI=null;
            if(fromPropertiesFile&&!relativeCatalogs()){
                try{
                    absURI=new URL(propertyFileURI,catalogFile);
                    catalogFile=absURI.toString();
                }catch(MalformedURLException mue){
                    absURI=null;
                }
            }
            catalogs.add(catalogFile);
        }
        return catalogs;
    }

    public boolean relativeCatalogs(){
        return getRelativeCatalogs();
    }

    public boolean getRelativeCatalogs(){
        if(relativeCatalogs==null){
            relativeCatalogs=new Boolean(queryRelativeCatalogs());
        }
        return relativeCatalogs.booleanValue();
    }

    private boolean queryRelativeCatalogs(){
        if(resources==null) readProperties();
        if(resources==null) return defaultRelativeCatalogs;
        try{
            String allow=resources.getString("relative-catalogs");
            return (allow.equalsIgnoreCase("true")
                    ||allow.equalsIgnoreCase("yes")
                    ||allow.equalsIgnoreCase("1"));
        }catch(MissingResourceException e){
            return defaultRelativeCatalogs;
        }
    }

    public void setRelativeCatalogs(boolean relative){
        relativeCatalogs=new Boolean(relative);
    }

    private String queryCatalogFiles(){
        String catalogList=SecuritySupport.getSystemProperty(pFiles);
        fromPropertiesFile=false;
        if(catalogList==null){
            if(resources==null) readProperties();
            if(resources!=null){
                try{
                    catalogList=resources.getString("catalogs");
                    fromPropertiesFile=true;
                }catch(MissingResourceException e){
                    System.err.println(propertyFile+": catalogs not found.");
                    catalogList=null;
                }
            }
        }
        if(catalogList==null){
            catalogList=defaultCatalogFiles;
        }
        return catalogList;
    }

    public void setCatalogFiles(String fileList){
        catalogFiles=fileList;
        fromPropertiesFile=false;
    }

    public boolean preferPublic(){
        return getPreferPublic();
    }

    public boolean getPreferPublic(){
        if(preferPublic==null){
            preferPublic=new Boolean(queryPreferPublic());
        }
        return preferPublic.booleanValue();
    }

    private boolean queryPreferPublic(){
        String prefer=SecuritySupport.getSystemProperty(pPrefer);
        if(prefer==null){
            if(resources==null) readProperties();
            if(resources==null) return defaultPreferPublic;
            try{
                prefer=resources.getString("prefer");
            }catch(MissingResourceException e){
                return defaultPreferPublic;
            }
        }
        if(prefer==null){
            return defaultPreferPublic;
        }
        return (prefer.equalsIgnoreCase("public"));
    }

    public void setPreferPublic(boolean preferPublic){
        this.preferPublic=new Boolean(preferPublic);
    }

    public boolean staticCatalog(){
        return getUseStaticCatalog();
    }

    public boolean getUseStaticCatalog(){
        if(useStaticCatalog==null){
            useStaticCatalog=new Boolean(queryUseStaticCatalog());
        }
        return useStaticCatalog.booleanValue();
    }

    private boolean queryUseStaticCatalog(){
        String staticCatalog=SecuritySupport.getSystemProperty(pStatic);
        if(staticCatalog==null){
            if(resources==null) readProperties();
            if(resources==null) return defaultUseStaticCatalog;
            try{
                staticCatalog=resources.getString("static-catalog");
            }catch(MissingResourceException e){
                return defaultUseStaticCatalog;
            }
        }
        if(staticCatalog==null){
            return defaultUseStaticCatalog;
        }
        return (staticCatalog.equalsIgnoreCase("true")
                ||staticCatalog.equalsIgnoreCase("yes")
                ||staticCatalog.equalsIgnoreCase("1"));
    }

    public void setUseStaticCatalog(boolean useStatic){
        useStaticCatalog=new Boolean(useStatic);
    }

    public Catalog getPrivateCatalog(){
        Catalog catalog=staticCatalog;
        if(useStaticCatalog==null){
            useStaticCatalog=new Boolean(getUseStaticCatalog());
        }
        if(catalog==null||!useStaticCatalog.booleanValue()){
            try{
                String catalogClassName=getCatalogClassName();
                if(catalogClassName==null){
                    catalog=new Catalog();
                }else{
                    try{
                        catalog=(Catalog)ReflectUtil.forName(catalogClassName).newInstance();
                    }catch(ClassNotFoundException cnfe){
                        debug.message(1,"Catalog class named '"
                                +catalogClassName
                                +"' could not be found. Using default.");
                        catalog=new Catalog();
                    }catch(ClassCastException cnfe){
                        debug.message(1,"Class named '"
                                +catalogClassName
                                +"' is not a Catalog. Using default.");
                        catalog=new Catalog();
                    }
                }
                catalog.setCatalogManager(this);
                catalog.setupReaders();
                catalog.loadSystemCatalogs();
            }catch(Exception ex){
                ex.printStackTrace();
            }
            if(useStaticCatalog.booleanValue()){
                staticCatalog=catalog;
            }
        }
        return catalog;
    }

    public Catalog getCatalog(){
        Catalog catalog=staticCatalog;
        if(useStaticCatalog==null){
            useStaticCatalog=new Boolean(getUseStaticCatalog());
        }
        if(catalog==null||!useStaticCatalog.booleanValue()){
            catalog=getPrivateCatalog();
            if(useStaticCatalog.booleanValue()){
                staticCatalog=catalog;
            }
        }
        return catalog;
    }

    public boolean useServicesMechanism(){
        return useServicesMechanism;
    }

    public boolean allowOasisXMLCatalogPI(){
        return getAllowOasisXMLCatalogPI();
    }

    public boolean getAllowOasisXMLCatalogPI(){
        if(oasisXMLCatalogPI==null){
            oasisXMLCatalogPI=new Boolean(queryAllowOasisXMLCatalogPI());
        }
        return oasisXMLCatalogPI.booleanValue();
    }

    public boolean queryAllowOasisXMLCatalogPI(){
        String allow=SecuritySupport.getSystemProperty(pAllowPI);
        if(allow==null){
            if(resources==null) readProperties();
            if(resources==null) return defaultOasisXMLCatalogPI;
            try{
                allow=resources.getString("allow-oasis-xml-catalog-pi");
            }catch(MissingResourceException e){
                return defaultOasisXMLCatalogPI;
            }
        }
        if(allow==null){
            return defaultOasisXMLCatalogPI;
        }
        return (allow.equalsIgnoreCase("true")
                ||allow.equalsIgnoreCase("yes")
                ||allow.equalsIgnoreCase("1"));
    }

    public void setAllowOasisXMLCatalogPI(boolean allowPI){
        oasisXMLCatalogPI=new Boolean(allowPI);
    }

    public String catalogClassName(){
        return getCatalogClassName();
    }

    public String getCatalogClassName(){
        if(catalogClassName==null){
            catalogClassName=queryCatalogClassName();
        }
        return catalogClassName;
    }

    public String queryCatalogClassName(){
        String className=SecuritySupport.getSystemProperty(pClassname);
        if(className==null){
            if(resources==null) readProperties();
            if(resources==null) return null;
            try{
                return resources.getString("catalog-class-name");
            }catch(MissingResourceException e){
                return null;
            }
        }
        return className;
    }

    public void setCatalogClassName(String className){
        catalogClassName=className;
    }
}
