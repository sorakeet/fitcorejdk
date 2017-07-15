/**
 * Copyright (c) 1996, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 * <p>
 * The original version of this source code and documentation
 * is copyrighted and owned by Taligent, Inc., a wholly-owned
 * subsidiary of IBM. These materials are provided under terms
 * of a License Agreement between Taligent and Sun. This technology
 * is protected by multiple US and International patents.
 * <p>
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 */
/**
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 *
 * The original version of this source code and documentation
 * is copyrighted and owned by Taligent, Inc., a wholly-owned
 * subsidiary of IBM. These materials are provided under terms
 * of a License Agreement between Taligent and Sun. This technology
 * is protected by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 *
 */
package java.util;

import sun.security.action.GetPropertyAction;
import sun.util.locale.*;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleResources;
import sun.util.locale.provider.LocaleServiceProviderPool;

import java.io.*;
import java.security.AccessController;
import java.text.MessageFormat;
import java.util.spi.LocaleNameProvider;

public final class Locale implements Cloneable, Serializable{
    static public final char PRIVATE_USE_EXTENSION='x';
    static public final char UNICODE_LOCALE_EXTENSION='u';
    static final long serialVersionUID=9149081749638150636L;
    static private final Cache LOCALECACHE=new Cache();
    static public final Locale ENGLISH=createConstant("en","");
    static public final Locale FRENCH=createConstant("fr","");
    static public final Locale GERMAN=createConstant("de","");
    static public final Locale ITALIAN=createConstant("it","");
    static public final Locale JAPANESE=createConstant("ja","");
    static public final Locale KOREAN=createConstant("ko","");
    static public final Locale CHINESE=createConstant("zh","");
    static public final Locale SIMPLIFIED_CHINESE=createConstant("zh","CN");
    static public final Locale CHINA=SIMPLIFIED_CHINESE;
    static public final Locale PRC=SIMPLIFIED_CHINESE;
    static public final Locale TRADITIONAL_CHINESE=createConstant("zh","TW");
    static public final Locale TAIWAN=TRADITIONAL_CHINESE;
    static public final Locale FRANCE=createConstant("fr","FR");
    static public final Locale GERMANY=createConstant("de","DE");
    static public final Locale ITALY=createConstant("it","IT");
    static public final Locale JAPAN=createConstant("ja","JP");
    static public final Locale KOREA=createConstant("ko","KR");
    static public final Locale UK=createConstant("en","GB");
    static public final Locale US=createConstant("en","US");
    static public final Locale CANADA=createConstant("en","CA");
    static public final Locale CANADA_FRENCH=createConstant("fr","CA");
    static public final Locale ROOT=createConstant("","");
    private static final int DISPLAY_LANGUAGE=0;
    private static final int DISPLAY_COUNTRY=1;
    private static final int DISPLAY_VARIANT=2;
    private static final int DISPLAY_SCRIPT=3;
    private static final ObjectStreamField[] serialPersistentFields={
            new ObjectStreamField("language",String.class),
            new ObjectStreamField("country",String.class),
            new ObjectStreamField("variant",String.class),
            new ObjectStreamField("hashcode",int.class),
            new ObjectStreamField("script",String.class),
            new ObjectStreamField("extensions",String.class),
    };
    private volatile static Locale defaultLocale=initDefault();
    private volatile static Locale defaultDisplayLocale=null;
    private volatile static Locale defaultFormatLocale=null;
    private static volatile String[] isoLanguages=null;
    private static volatile String[] isoCountries=null;
    // ================= privates =====================================
    private transient BaseLocale baseLocale;
    private transient LocaleExtensions localeExtensions;
    private transient volatile int hashCodeValue=0;
    private transient volatile String languageTag;

    private Locale(BaseLocale baseLocale,LocaleExtensions extensions){
        this.baseLocale=baseLocale;
        this.localeExtensions=extensions;
    }

    public Locale(String language,String country){
        this(language,country,"");
    }

    public Locale(String language,String country,String variant){
        if(language==null||country==null||variant==null){
            throw new NullPointerException();
        }
        baseLocale=BaseLocale.getInstance(convertOldISOCodes(language),"",country,variant);
        localeExtensions=getCompatibilityExtensions(language,"",country,variant);
    }

    private static String convertOldISOCodes(String language){
        // we accept both the old and the new ISO codes for the languages whose ISO
        // codes have changed, but we always store the OLD code, for backward compatibility
        language=LocaleUtils.toLowerString(language).intern();
        if(language=="he"){
            return "iw";
        }else if(language=="yi"){
            return "ji";
        }else if(language=="id"){
            return "in";
        }else{
            return language;
        }
    }

    private static LocaleExtensions getCompatibilityExtensions(String language,
                                                               String script,
                                                               String country,
                                                               String variant){
        LocaleExtensions extensions=null;
        // Special cases for backward compatibility support
        if(LocaleUtils.caseIgnoreMatch(language,"ja")
                &&script.length()==0
                &&LocaleUtils.caseIgnoreMatch(country,"jp")
                &&"JP".equals(variant)){
            // ja_JP_JP -> u-ca-japanese (calendar = japanese)
            extensions=LocaleExtensions.CALENDAR_JAPANESE;
        }else if(LocaleUtils.caseIgnoreMatch(language,"th")
                &&script.length()==0
                &&LocaleUtils.caseIgnoreMatch(country,"th")
                &&"TH".equals(variant)){
            // th_TH_TH -> u-nu-thai (numbersystem = thai)
            extensions=LocaleExtensions.NUMBER_THAI;
        }
        return extensions;
    }

    public Locale(String language){
        this(language,"","");
    }

    private static Locale createConstant(String lang,String country){
        BaseLocale base=BaseLocale.createInstance(lang,country);
        return getInstance(base,null);
    }

    static Locale getInstance(BaseLocale baseloc,LocaleExtensions extensions){
        LocaleKey key=new LocaleKey(baseloc,extensions);
        return LOCALECACHE.get(key);
    }

    static Locale getInstance(String language,String country,String variant){
        return getInstance(language,"",country,variant,null);
    }

    static Locale getInstance(String language,String script,String country,
                              String variant,LocaleExtensions extensions){
        if(language==null||script==null||country==null||variant==null){
            throw new NullPointerException();
        }
        if(extensions==null){
            extensions=getCompatibilityExtensions(language,script,country,variant);
        }
        BaseLocale baseloc=BaseLocale.getInstance(language,script,country,variant);
        return getInstance(baseloc,extensions);
    }

    private static Locale initDefault(){
        String language, region, script, country, variant;
        language=AccessController.doPrivileged(
                new GetPropertyAction("user.language","en"));
        // for compatibility, check for old user.region property
        region=AccessController.doPrivileged(
                new GetPropertyAction("user.region"));
        if(region!=null){
            // region can be of form country, country_variant, or _variant
            int i=region.indexOf('_');
            if(i>=0){
                country=region.substring(0,i);
                variant=region.substring(i+1);
            }else{
                country=region;
                variant="";
            }
            script="";
        }else{
            script=AccessController.doPrivileged(
                    new GetPropertyAction("user.script",""));
            country=AccessController.doPrivileged(
                    new GetPropertyAction("user.country",""));
            variant=AccessController.doPrivileged(
                    new GetPropertyAction("user.variant",""));
        }
        return getInstance(language,script,country,variant,null);
    }

    public static Locale[] getAvailableLocales(){
        return LocaleServiceProviderPool.getAllAvailableLocales();
    }

    public static String[] getISOCountries(){
        if(isoCountries==null){
            isoCountries=getISO2Table(LocaleISOData.isoCountryTable);
        }
        String[] result=new String[isoCountries.length];
        System.arraycopy(isoCountries,0,result,0,isoCountries.length);
        return result;
    }

    private static String[] getISO2Table(String table){
        int len=table.length()/5;
        String[] isoTable=new String[len];
        for(int i=0, j=0;i<len;i++,j+=5){
            isoTable[i]=table.substring(j,j+2);
        }
        return isoTable;
    }

    public static String[] getISOLanguages(){
        if(isoLanguages==null){
            isoLanguages=getISO2Table(LocaleISOData.isoLanguageTable);
        }
        String[] result=new String[isoLanguages.length];
        System.arraycopy(isoLanguages,0,result,0,isoLanguages.length);
        return result;
    }

    public static Locale forLanguageTag(String languageTag){
        LanguageTag tag=LanguageTag.parse(languageTag,null);
        InternalLocaleBuilder bldr=new InternalLocaleBuilder();
        bldr.setLanguageTag(tag);
        BaseLocale base=bldr.getBaseLocale();
        LocaleExtensions exts=bldr.getLocaleExtensions();
        if(exts==null&&base.getVariant().length()>0){
            exts=getCompatibilityExtensions(base.getLanguage(),base.getScript(),
                    base.getRegion(),base.getVariant());
        }
        return getInstance(base,exts);
    }

    private static String formatList(String[] stringList,String listPattern,String listCompositionPattern){
        // If we have no list patterns, compose the list in a simple,
        // non-localized way.
        if(listPattern==null||listCompositionPattern==null){
            StringBuilder result=new StringBuilder();
            for(int i=0;i<stringList.length;++i){
                if(i>0){
                    result.append(',');
                }
                result.append(stringList[i]);
            }
            return result.toString();
        }
        // Compose the list down to three elements if necessary
        if(stringList.length>3){
            MessageFormat format=new MessageFormat(listCompositionPattern);
            stringList=composeList(format,stringList);
        }
        // Rebuild the argument list with the list length as the first element
        Object[] args=new Object[stringList.length+1];
        System.arraycopy(stringList,0,args,1,stringList.length);
        args[0]=new Integer(stringList.length);
        // Format it using the pattern in the resource
        MessageFormat format=new MessageFormat(listPattern);
        return format.format(args);
    }

    private static String[] composeList(MessageFormat format,String[] list){
        if(list.length<=3) return list;
        // Use the given format to compose the first two elements into one
        String[] listItems={list[0],list[1]};
        String newItem=format.format(listItems);
        // Form a new list one element shorter
        String[] newList=new String[list.length-1];
        System.arraycopy(list,2,newList,1,newList.length-1);
        newList[0]=newItem;
        // Recurse
        return composeList(format,newList);
    }

    public static List<Locale> filter(List<LanguageRange> priorityList,
                                      Collection<Locale> locales){
        return filter(priorityList,locales,FilteringMode.AUTOSELECT_FILTERING);
    }

    public static List<Locale> filter(List<LanguageRange> priorityList,
                                      Collection<Locale> locales,
                                      FilteringMode mode){
        return LocaleMatcher.filter(priorityList,locales,mode);
    }

    public static List<String> filterTags(List<LanguageRange> priorityList,
                                          Collection<String> tags){
        return filterTags(priorityList,tags,FilteringMode.AUTOSELECT_FILTERING);
    }

    public static List<String> filterTags(List<LanguageRange> priorityList,
                                          Collection<String> tags,
                                          FilteringMode mode){
        return LocaleMatcher.filterTags(priorityList,tags,mode);
    }

    public static Locale lookup(List<LanguageRange> priorityList,
                                Collection<Locale> locales){
        return LocaleMatcher.lookup(priorityList,locales);
    }

    public static String lookupTag(List<LanguageRange> priorityList,
                                   Collection<String> tags){
        return LocaleMatcher.lookupTag(priorityList,tags);
    }

    public String getLanguage(){
        return baseLocale.getLanguage();
    }    @Override
    public final String toString(){
        boolean l=(baseLocale.getLanguage().length()!=0);
        boolean s=(baseLocale.getScript().length()!=0);
        boolean r=(baseLocale.getRegion().length()!=0);
        boolean v=(baseLocale.getVariant().length()!=0);
        boolean e=(localeExtensions!=null&&localeExtensions.getID().length()!=0);
        StringBuilder result=new StringBuilder(baseLocale.getLanguage());
        if(r||(l&&(v||s||e))){
            result.append('_')
                    .append(baseLocale.getRegion()); // This may just append '_'
        }
        if(v&&(l||r)){
            result.append('_')
                    .append(baseLocale.getVariant());
        }
        if(s&&(l||r)){
            result.append("_#")
                    .append(baseLocale.getScript());
        }
        if(e&&(l||r)){
            result.append('_');
            if(!s){
                result.append('#');
            }
            result.append(localeExtensions.getID());
        }
        return result.toString();
    }

    public String getScript(){
        return baseLocale.getScript();
    }

    public String getCountry(){
        return baseLocale.getRegion();
    }

    public String getVariant(){
        return baseLocale.getVariant();
    }

    public Locale stripExtensions(){
        return hasExtensions()?Locale.getInstance(baseLocale,null):this;
    }

    public boolean hasExtensions(){
        return localeExtensions!=null;
    }

    public String getExtension(char key){
        if(!LocaleExtensions.isValidKey(key)){
            throw new IllegalArgumentException("Ill-formed extension key: "+key);
        }
        return hasExtensions()?localeExtensions.getExtensionValue(key):null;
    }

    public Set<Character> getExtensionKeys(){
        if(!hasExtensions()){
            return Collections.emptySet();
        }
        return localeExtensions.getKeys();
    }

    public Set<String> getUnicodeLocaleAttributes(){
        if(!hasExtensions()){
            return Collections.emptySet();
        }
        return localeExtensions.getUnicodeLocaleAttributes();
    }

    public String getUnicodeLocaleType(String key){
        if(!isUnicodeExtensionKey(key)){
            throw new IllegalArgumentException("Ill-formed Unicode locale key: "+key);
        }
        return hasExtensions()?localeExtensions.getUnicodeLocaleType(key):null;
    }

    // Duplicate of sun.util.locale.UnicodeLocaleExtension.isKey in order to
    // avoid its class loading.
    private static boolean isUnicodeExtensionKey(String s){
        // 2alphanum
        return (s.length()==2)&&LocaleUtils.isAlphaNumericString(s);
    }

    public Set<String> getUnicodeLocaleKeys(){
        if(localeExtensions==null){
            return Collections.emptySet();
        }
        return localeExtensions.getUnicodeLocaleKeys();
    }

    BaseLocale getBaseLocale(){
        return baseLocale;
    }

    LocaleExtensions getLocaleExtensions(){
        return localeExtensions;
    }

    public String toLanguageTag(){
        if(languageTag!=null){
            return languageTag;
        }
        LanguageTag tag=LanguageTag.parseLocale(baseLocale,localeExtensions);
        StringBuilder buf=new StringBuilder();
        String subtag=tag.getLanguage();
        if(subtag.length()>0){
            buf.append(LanguageTag.canonicalizeLanguage(subtag));
        }
        subtag=tag.getScript();
        if(subtag.length()>0){
            buf.append(LanguageTag.SEP);
            buf.append(LanguageTag.canonicalizeScript(subtag));
        }
        subtag=tag.getRegion();
        if(subtag.length()>0){
            buf.append(LanguageTag.SEP);
            buf.append(LanguageTag.canonicalizeRegion(subtag));
        }
        List<String> subtags=tag.getVariants();
        for(String s : subtags){
            buf.append(LanguageTag.SEP);
            // preserve casing
            buf.append(s);
        }
        subtags=tag.getExtensions();
        for(String s : subtags){
            buf.append(LanguageTag.SEP);
            buf.append(LanguageTag.canonicalizeExtension(s));
        }
        subtag=tag.getPrivateuse();
        if(subtag.length()>0){
            if(buf.length()>0){
                buf.append(LanguageTag.SEP);
            }
            buf.append(LanguageTag.PRIVATEUSE).append(LanguageTag.SEP);
            // preserve casing
            buf.append(subtag);
        }
        String langTag=buf.toString();
        synchronized(this){
            if(languageTag==null){
                languageTag=langTag;
            }
        }
        return languageTag;
    }

    public String getISO3Language() throws MissingResourceException{
        String lang=baseLocale.getLanguage();
        if(lang.length()==3){
            return lang;
        }
        String language3=getISO3Code(lang,LocaleISOData.isoLanguageTable);
        if(language3==null){
            throw new MissingResourceException("Couldn't find 3-letter language code for "
                    +lang,"FormatData_"+toString(),"ShortLanguage");
        }
        return language3;
    }

    private static String getISO3Code(String iso2Code,String table){
        int codeLength=iso2Code.length();
        if(codeLength==0){
            return "";
        }
        int tableLength=table.length();
        int index=tableLength;
        if(codeLength==2){
            char c1=iso2Code.charAt(0);
            char c2=iso2Code.charAt(1);
            for(index=0;index<tableLength;index+=5){
                if(table.charAt(index)==c1
                        &&table.charAt(index+1)==c2){
                    break;
                }
            }
        }
        return index<tableLength?table.substring(index+2,index+5):null;
    }

    public String getISO3Country() throws MissingResourceException{
        String country3=getISO3Code(baseLocale.getRegion(),LocaleISOData.isoCountryTable);
        if(country3==null){
            throw new MissingResourceException("Couldn't find 3-letter country code for "
                    +baseLocale.getRegion(),"FormatData_"+toString(),"ShortCountry");
        }
        return country3;
    }

    @Override
    public int hashCode(){
        int hc=hashCodeValue;
        if(hc==0){
            hc=baseLocale.hashCode();
            if(localeExtensions!=null){
                hc^=localeExtensions.hashCode();
            }
            hashCodeValue=hc;
        }
        return hc;
    }    @Override
    public Object clone(){
        try{
            Locale that=(Locale)super.clone();
            return that;
        }catch(CloneNotSupportedException e){
            throw new InternalError(e);
        }
    }

    public final String getDisplayLanguage(){
        return getDisplayLanguage(getDefault(Category.DISPLAY));
    }

    public static Locale getDefault(Category category){
        // do not synchronize this method - see 4071298
        switch(category){
            case DISPLAY:
                if(defaultDisplayLocale==null){
                    synchronized(Locale.class){
                        if(defaultDisplayLocale==null){
                            defaultDisplayLocale=initDefault(category);
                        }
                    }
                }
                return defaultDisplayLocale;
            case FORMAT:
                if(defaultFormatLocale==null){
                    synchronized(Locale.class){
                        if(defaultFormatLocale==null){
                            defaultFormatLocale=initDefault(category);
                        }
                    }
                }
                return defaultFormatLocale;
            default:
                assert false:"Unknown Category";
        }
        return getDefault();
    }
    // Overrides

    public static Locale getDefault(){
        // do not synchronize this method - see 4071298
        return defaultLocale;
    }

    public static synchronized void setDefault(Locale newLocale){
        setDefault(Category.DISPLAY,newLocale);
        setDefault(Category.FORMAT,newLocale);
        defaultLocale=newLocale;
    }    @Override
    public boolean equals(Object obj){
        if(this==obj)                      // quick check
            return true;
        if(!(obj instanceof Locale))
            return false;
        BaseLocale otherBase=((Locale)obj).baseLocale;
        if(!baseLocale.equals(otherBase)){
            return false;
        }
        if(localeExtensions==null){
            return ((Locale)obj).localeExtensions==null;
        }
        return localeExtensions.equals(((Locale)obj).localeExtensions);
    }

    public static synchronized void setDefault(Category category,
                                               Locale newLocale){
        if(category==null)
            throw new NullPointerException("Category cannot be NULL");
        if(newLocale==null)
            throw new NullPointerException("Can't set default locale to NULL");
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null) sm.checkPermission(new PropertyPermission
                ("user.language","write"));
        switch(category){
            case DISPLAY:
                defaultDisplayLocale=newLocale;
                break;
            case FORMAT:
                defaultFormatLocale=newLocale;
                break;
            default:
                assert false:"Unknown Category";
        }
    }

    private static Locale initDefault(Category category){
        return getInstance(
                AccessController.doPrivileged(
                        new GetPropertyAction(category.languageKey,defaultLocale.getLanguage())),
                AccessController.doPrivileged(
                        new GetPropertyAction(category.scriptKey,defaultLocale.getScript())),
                AccessController.doPrivileged(
                        new GetPropertyAction(category.countryKey,defaultLocale.getCountry())),
                AccessController.doPrivileged(
                        new GetPropertyAction(category.variantKey,defaultLocale.getVariant())),
                null);
    }

    public String getDisplayLanguage(Locale inLocale){
        return getDisplayString(baseLocale.getLanguage(),inLocale,DISPLAY_LANGUAGE);
    }

    private String getDisplayString(String code,Locale inLocale,int type){
        if(code.length()==0){
            return "";
        }
        if(inLocale==null){
            throw new NullPointerException();
        }
        LocaleServiceProviderPool pool=
                LocaleServiceProviderPool.getPool(LocaleNameProvider.class);
        String key=(type==DISPLAY_VARIANT?"%%"+code:code);
        String result=pool.getLocalizedObject(
                LocaleNameGetter.INSTANCE,
                inLocale,key,type,code);
        if(result!=null){
            return result;
        }
        return code;
    }

    public String getDisplayScript(){
        return getDisplayScript(getDefault(Category.DISPLAY));
    }

    public String getDisplayScript(Locale inLocale){
        return getDisplayString(baseLocale.getScript(),inLocale,DISPLAY_SCRIPT);
    }

    public final String getDisplayCountry(){
        return getDisplayCountry(getDefault(Category.DISPLAY));
    }

    public String getDisplayCountry(Locale inLocale){
        return getDisplayString(baseLocale.getRegion(),inLocale,DISPLAY_COUNTRY);
    }

    public final String getDisplayVariant(){
        return getDisplayVariant(getDefault(Category.DISPLAY));
    }

    public String getDisplayVariant(Locale inLocale){
        if(baseLocale.getVariant().length()==0)
            return "";
        LocaleResources lr=LocaleProviderAdapter.forJRE().getLocaleResources(inLocale);
        String names[]=getDisplayVariantArray(inLocale);
        // Get the localized patterns for formatting a list, and use
        // them to format the list.
        return formatList(names,
                lr.getLocaleName("ListPattern"),
                lr.getLocaleName("ListCompositionPattern"));
    }

    public final String getDisplayName(){
        return getDisplayName(getDefault(Category.DISPLAY));
    }

    public String getDisplayName(Locale inLocale){
        LocaleResources lr=LocaleProviderAdapter.forJRE().getLocaleResources(inLocale);
        String languageName=getDisplayLanguage(inLocale);
        String scriptName=getDisplayScript(inLocale);
        String countryName=getDisplayCountry(inLocale);
        String[] variantNames=getDisplayVariantArray(inLocale);
        // Get the localized patterns for formatting a display name.
        String displayNamePattern=lr.getLocaleName("DisplayNamePattern");
        String listPattern=lr.getLocaleName("ListPattern");
        String listCompositionPattern=lr.getLocaleName("ListCompositionPattern");
        // The display name consists of a main name, followed by qualifiers.
        // Typically, the format is "MainName (Qualifier, Qualifier)" but this
        // depends on what pattern is stored in the display locale.
        String mainName=null;
        String[] qualifierNames=null;
        // The main name is the language, or if there is no language, the script,
        // then if no script, the country. If there is no language/script/country
        // (an anomalous situation) then the display name is simply the variant's
        // display name.
        if(languageName.length()==0&&scriptName.length()==0&&countryName.length()==0){
            if(variantNames.length==0){
                return "";
            }else{
                return formatList(variantNames,listPattern,listCompositionPattern);
            }
        }
        ArrayList<String> names=new ArrayList<>(4);
        if(languageName.length()!=0){
            names.add(languageName);
        }
        if(scriptName.length()!=0){
            names.add(scriptName);
        }
        if(countryName.length()!=0){
            names.add(countryName);
        }
        if(variantNames.length!=0){
            names.addAll(Arrays.asList(variantNames));
        }
        // The first one in the main name
        mainName=names.get(0);
        // Others are qualifiers
        int numNames=names.size();
        qualifierNames=(numNames>1)?
                names.subList(1,numNames).toArray(new String[numNames-1]):new String[0];
        // Create an array whose first element is the number of remaining
        // elements.  This serves as a selector into a ChoiceFormat pattern from
        // the resource.  The second and third elements are the main name and
        // the qualifier; if there are no qualifiers, the third element is
        // unused by the format pattern.
        Object[] displayNames={
                new Integer(qualifierNames.length!=0?2:1),
                mainName,
                // We could also just call formatList() and have it handle the empty
                // list case, but this is more efficient, and we want it to be
                // efficient since all the language-only locales will not have any
                // qualifiers.
                qualifierNames.length!=0?formatList(qualifierNames,listPattern,listCompositionPattern):null
        };
        if(displayNamePattern!=null){
            return new MessageFormat(displayNamePattern).format(displayNames);
        }else{
            // If we cannot get the message format pattern, then we use a simple
            // hard-coded pattern.  This should not occur in practice unless the
            // installation is missing some core files (FormatData etc.).
            StringBuilder result=new StringBuilder();
            result.append((String)displayNames[1]);
            if(displayNames.length>2){
                result.append(" (");
                result.append((String)displayNames[2]);
                result.append(')');
            }
            return result.toString();
        }
    }

    private String[] getDisplayVariantArray(Locale inLocale){
        // Split the variant name into tokens separated by '_'.
        StringTokenizer tokenizer=new StringTokenizer(baseLocale.getVariant(),"_");
        String[] names=new String[tokenizer.countTokens()];
        // For each variant token, lookup the display name.  If
        // not found, use the variant name itself.
        for(int i=0;i<names.length;++i){
            names[i]=getDisplayString(tokenizer.nextToken(),
                    inLocale,DISPLAY_VARIANT);
        }
        return names;
    }

    private void writeObject(ObjectOutputStream out) throws IOException{
        ObjectOutputStream.PutField fields=out.putFields();
        fields.put("language",baseLocale.getLanguage());
        fields.put("script",baseLocale.getScript());
        fields.put("country",baseLocale.getRegion());
        fields.put("variant",baseLocale.getVariant());
        fields.put("extensions",localeExtensions==null?"":localeExtensions.getID());
        fields.put("hashcode",-1); // place holder just for backward support
        out.writeFields();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
        ObjectInputStream.GetField fields=in.readFields();
        String language=(String)fields.get("language","");
        String script=(String)fields.get("script","");
        String country=(String)fields.get("country","");
        String variant=(String)fields.get("variant","");
        String extStr=(String)fields.get("extensions","");
        baseLocale=BaseLocale.getInstance(convertOldISOCodes(language),script,country,variant);
        if(extStr.length()>0){
            try{
                InternalLocaleBuilder bldr=new InternalLocaleBuilder();
                bldr.setExtensions(extStr);
                localeExtensions=bldr.getLocaleExtensions();
            }catch(LocaleSyntaxException e){
                throw new IllformedLocaleException(e.getMessage());
            }
        }else{
            localeExtensions=null;
        }
    }

    private Object readResolve() throws ObjectStreamException{
        return getInstance(baseLocale.getLanguage(),baseLocale.getScript(),
                baseLocale.getRegion(),baseLocale.getVariant(),localeExtensions);
    }

    public enum Category{
        DISPLAY("user.language.display",
                "user.script.display",
                "user.country.display",
                "user.variant.display"),
        FORMAT("user.language.format",
                "user.script.format",
                "user.country.format",
                "user.variant.format");
        final String languageKey;
        final String scriptKey;
        final String countryKey;
        final String variantKey;

        Category(String languageKey,String scriptKey,String countryKey,String variantKey){
            this.languageKey=languageKey;
            this.scriptKey=scriptKey;
            this.countryKey=countryKey;
            this.variantKey=variantKey;
        }
    }

    public static enum FilteringMode{
        AUTOSELECT_FILTERING,
        EXTENDED_FILTERING,
        IGNORE_EXTENDED_RANGES,
        MAP_EXTENDED_RANGES,
        REJECT_EXTENDED_RANGES
    }

    private static class Cache extends LocaleObjectCache<LocaleKey,Locale>{
        private Cache(){
        }

        @Override
        protected Locale createObject(LocaleKey key){
            return new Locale(key.base,key.exts);
        }
    }

    private static final class LocaleKey{
        private final BaseLocale base;
        private final LocaleExtensions exts;
        private final int hash;

        private LocaleKey(BaseLocale baseLocale,LocaleExtensions extensions){
            base=baseLocale;
            exts=extensions;
            // Calculate the hash value here because it's always used.
            int h=base.hashCode();
            if(exts!=null){
                h^=exts.hashCode();
            }
            hash=h;
        }

        @Override
        public int hashCode(){
            return hash;
        }

        @Override
        public boolean equals(Object obj){
            if(this==obj){
                return true;
            }
            if(!(obj instanceof LocaleKey)){
                return false;
            }
            LocaleKey other=(LocaleKey)obj;
            if(hash!=other.hash||!base.equals(other.base)){
                return false;
            }
            if(exts==null){
                return other.exts==null;
            }
            return exts.equals(other.exts);
        }
    }

    private static class LocaleNameGetter
            implements LocaleServiceProviderPool.LocalizedObjectGetter<LocaleNameProvider,String>{
        private static final LocaleNameGetter INSTANCE=new LocaleNameGetter();

        @Override
        public String getObject(LocaleNameProvider localeNameProvider,
                                Locale locale,
                                String key,
                                Object... params){
            assert params.length==2;
            int type=(Integer)params[0];
            String code=(String)params[1];
            switch(type){
                case DISPLAY_LANGUAGE:
                    return localeNameProvider.getDisplayLanguage(code,locale);
                case DISPLAY_COUNTRY:
                    return localeNameProvider.getDisplayCountry(code,locale);
                case DISPLAY_VARIANT:
                    return localeNameProvider.getDisplayVariant(code,locale);
                case DISPLAY_SCRIPT:
                    return localeNameProvider.getDisplayScript(code,locale);
                default:
                    assert false; // shouldn't happen
            }
            return null;
        }
    }

    public static final class Builder{
        private final InternalLocaleBuilder localeBuilder;

        public Builder(){
            localeBuilder=new InternalLocaleBuilder();
        }

        public Builder setLocale(Locale locale){
            try{
                localeBuilder.setLocale(locale.baseLocale,locale.localeExtensions);
            }catch(LocaleSyntaxException e){
                throw new IllformedLocaleException(e.getMessage(),e.getErrorIndex());
            }
            return this;
        }

        public Builder setLanguageTag(String languageTag){
            ParseStatus sts=new ParseStatus();
            LanguageTag tag=LanguageTag.parse(languageTag,sts);
            if(sts.isError()){
                throw new IllformedLocaleException(sts.getErrorMessage(),sts.getErrorIndex());
            }
            localeBuilder.setLanguageTag(tag);
            return this;
        }

        public Builder setLanguage(String language){
            try{
                localeBuilder.setLanguage(language);
            }catch(LocaleSyntaxException e){
                throw new IllformedLocaleException(e.getMessage(),e.getErrorIndex());
            }
            return this;
        }

        public Builder setScript(String script){
            try{
                localeBuilder.setScript(script);
            }catch(LocaleSyntaxException e){
                throw new IllformedLocaleException(e.getMessage(),e.getErrorIndex());
            }
            return this;
        }

        public Builder setRegion(String region){
            try{
                localeBuilder.setRegion(region);
            }catch(LocaleSyntaxException e){
                throw new IllformedLocaleException(e.getMessage(),e.getErrorIndex());
            }
            return this;
        }

        public Builder setVariant(String variant){
            try{
                localeBuilder.setVariant(variant);
            }catch(LocaleSyntaxException e){
                throw new IllformedLocaleException(e.getMessage(),e.getErrorIndex());
            }
            return this;
        }

        public Builder setExtension(char key,String value){
            try{
                localeBuilder.setExtension(key,value);
            }catch(LocaleSyntaxException e){
                throw new IllformedLocaleException(e.getMessage(),e.getErrorIndex());
            }
            return this;
        }

        public Builder setUnicodeLocaleKeyword(String key,String type){
            try{
                localeBuilder.setUnicodeLocaleKeyword(key,type);
            }catch(LocaleSyntaxException e){
                throw new IllformedLocaleException(e.getMessage(),e.getErrorIndex());
            }
            return this;
        }

        public Builder addUnicodeLocaleAttribute(String attribute){
            try{
                localeBuilder.addUnicodeLocaleAttribute(attribute);
            }catch(LocaleSyntaxException e){
                throw new IllformedLocaleException(e.getMessage(),e.getErrorIndex());
            }
            return this;
        }

        public Builder removeUnicodeLocaleAttribute(String attribute){
            try{
                localeBuilder.removeUnicodeLocaleAttribute(attribute);
            }catch(LocaleSyntaxException e){
                throw new IllformedLocaleException(e.getMessage(),e.getErrorIndex());
            }
            return this;
        }

        public Builder clear(){
            localeBuilder.clear();
            return this;
        }

        public Builder clearExtensions(){
            localeBuilder.clearExtensions();
            return this;
        }

        public Locale build(){
            BaseLocale baseloc=localeBuilder.getBaseLocale();
            LocaleExtensions extensions=localeBuilder.getLocaleExtensions();
            if(extensions==null&&baseloc.getVariant().length()>0){
                extensions=getCompatibilityExtensions(baseloc.getLanguage(),baseloc.getScript(),
                        baseloc.getRegion(),baseloc.getVariant());
            }
            return Locale.getInstance(baseloc,extensions);
        }
    }

    public static final class LanguageRange{
        public static final double MAX_WEIGHT=1.0;
        public static final double MIN_WEIGHT=0.0;
        private final String range;
        private final double weight;
        private volatile int hash=0;

        public LanguageRange(String range){
            this(range,MAX_WEIGHT);
        }

        public LanguageRange(String range,double weight){
            if(range==null){
                throw new NullPointerException();
            }
            if(weight<MIN_WEIGHT||weight>MAX_WEIGHT){
                throw new IllegalArgumentException("weight="+weight);
            }
            range=range.toLowerCase();
            // Do syntax check.
            boolean isIllFormed=false;
            String[] subtags=range.split("-");
            if(isSubtagIllFormed(subtags[0],true)
                    ||range.endsWith("-")){
                isIllFormed=true;
            }else{
                for(int i=1;i<subtags.length;i++){
                    if(isSubtagIllFormed(subtags[i],false)){
                        isIllFormed=true;
                        break;
                    }
                }
            }
            if(isIllFormed){
                throw new IllegalArgumentException("range="+range);
            }
            this.range=range;
            this.weight=weight;
        }

        private static boolean isSubtagIllFormed(String subtag,
                                                 boolean isFirstSubtag){
            if(subtag.equals("")||subtag.length()>8){
                return true;
            }else if(subtag.equals("*")){
                return false;
            }
            char[] charArray=subtag.toCharArray();
            if(isFirstSubtag){ // ALPHA
                for(char c : charArray){
                    if(c<'a'||c>'z'){
                        return true;
                    }
                }
            }else{ // ALPHA / DIGIT
                for(char c : charArray){
                    if(c<'0'||(c>'9'&&c<'a')||c>'z'){
                        return true;
                    }
                }
            }
            return false;
        }

        public static List<LanguageRange> parse(String ranges,
                                                Map<String,List<String>> map){
            return mapEquivalents(parse(ranges),map);
        }

        public static List<LanguageRange> parse(String ranges){
            return LocaleMatcher.parse(ranges);
        }

        public static List<LanguageRange> mapEquivalents(
                List<LanguageRange> priorityList,
                Map<String,List<String>> map){
            return LocaleMatcher.mapEquivalents(priorityList,map);
        }

        public String getRange(){
            return range;
        }

        public double getWeight(){
            return weight;
        }

        @Override
        public int hashCode(){
            if(hash==0){
                int result=17;
                result=37*result+range.hashCode();
                long bitsWeight=Double.doubleToLongBits(weight);
                result=37*result+(int)(bitsWeight^(bitsWeight>>>32));
                hash=result;
            }
            return hash;
        }

        @Override
        public boolean equals(Object obj){
            if(this==obj){
                return true;
            }
            if(!(obj instanceof LanguageRange)){
                return false;
            }
            LanguageRange other=(LanguageRange)obj;
            return hash==other.hash
                    &&range.equals(other.range)
                    &&weight==other.weight;
        }
    }

    ;






}
