/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.net;

import java.text.SimpleDateFormat;
import java.util.*;

public final class HttpCookie implements Cloneable{
    static final Map<String,CookieAttributeAssignor> assignors=
            new HashMap<>();
    static final TimeZone GMT=TimeZone.getTimeZone("GMT");
    // Since the positive and zero max-age have their meanings,
    // this value serves as a hint as 'not specify max-age'
    private final static long MAX_AGE_UNSPECIFIED=-1;
    // date formats used by Netscape's cookie draft
    // as well as formats seen on various sites
    private final static String[] COOKIE_DATE_FORMATS={
            "EEE',' dd-MMM-yyyy HH:mm:ss 'GMT'",
            "EEE',' dd MMM yyyy HH:mm:ss 'GMT'",
            "EEE MMM dd yyyy HH:mm:ss 'GMT'Z",
            "EEE',' dd-MMM-yy HH:mm:ss 'GMT'",
            "EEE',' dd MMM yy HH:mm:ss 'GMT'",
            "EEE MMM dd yy HH:mm:ss 'GMT'Z"
    };
    // constant strings represent set-cookie header token
    private final static String SET_COOKIE="set-cookie:";
    private final static String SET_COOKIE2="set-cookie2:";
    // ---------------- Private operations --------------
    // Note -- disabled for now to allow full Netscape compatibility
    // from RFC 2068, token special case characters
    //
    // private static final String tspecials = "()<>@,;:\\\"/[]?={} \t";
    private static final String tspecials=",; ";  // deliberately includes space

    static{
        assignors.put("comment",new CookieAttributeAssignor(){
            public void assign(HttpCookie cookie,
                               String attrName,
                               String attrValue){
                if(cookie.getComment()==null)
                    cookie.setComment(attrValue);
            }
        });
        assignors.put("commenturl",new CookieAttributeAssignor(){
            public void assign(HttpCookie cookie,
                               String attrName,
                               String attrValue){
                if(cookie.getCommentURL()==null)
                    cookie.setCommentURL(attrValue);
            }
        });
        assignors.put("discard",new CookieAttributeAssignor(){
            public void assign(HttpCookie cookie,
                               String attrName,
                               String attrValue){
                cookie.setDiscard(true);
            }
        });
        assignors.put("domain",new CookieAttributeAssignor(){
            public void assign(HttpCookie cookie,
                               String attrName,
                               String attrValue){
                if(cookie.getDomain()==null)
                    cookie.setDomain(attrValue);
            }
        });
        assignors.put("max-age",new CookieAttributeAssignor(){
            public void assign(HttpCookie cookie,
                               String attrName,
                               String attrValue){
                try{
                    long maxage=Long.parseLong(attrValue);
                    if(cookie.getMaxAge()==MAX_AGE_UNSPECIFIED)
                        cookie.setMaxAge(maxage);
                }catch(NumberFormatException ignored){
                    throw new IllegalArgumentException(
                            "Illegal cookie max-age attribute");
                }
            }
        });
        assignors.put("path",new CookieAttributeAssignor(){
            public void assign(HttpCookie cookie,
                               String attrName,
                               String attrValue){
                if(cookie.getPath()==null)
                    cookie.setPath(attrValue);
            }
        });
        assignors.put("port",new CookieAttributeAssignor(){
            public void assign(HttpCookie cookie,
                               String attrName,
                               String attrValue){
                if(cookie.getPortlist()==null)
                    cookie.setPortlist(attrValue==null?"":attrValue);
            }
        });
        assignors.put("secure",new CookieAttributeAssignor(){
            public void assign(HttpCookie cookie,
                               String attrName,
                               String attrValue){
                cookie.setSecure(true);
            }
        });
        assignors.put("httponly",new CookieAttributeAssignor(){
            public void assign(HttpCookie cookie,
                               String attrName,
                               String attrValue){
                cookie.setHttpOnly(true);
            }
        });
        assignors.put("version",new CookieAttributeAssignor(){
            public void assign(HttpCookie cookie,
                               String attrName,
                               String attrValue){
                try{
                    int version=Integer.parseInt(attrValue);
                    cookie.setVersion(version);
                }catch(NumberFormatException ignored){
                    // Just ignore bogus version, it will default to 0 or 1
                }
            }
        });
        assignors.put("expires",new CookieAttributeAssignor(){ // Netscape only
            public void assign(HttpCookie cookie,
                               String attrName,
                               String attrValue){
                if(cookie.getMaxAge()==MAX_AGE_UNSPECIFIED){
                    cookie.setMaxAge(cookie.expiryDate2DeltaSeconds(attrValue));
                }
            }
        });
    }

    static{
        sun.misc.SharedSecrets.setJavaNetHttpCookieAccess(
                new sun.misc.JavaNetHttpCookieAccess(){
                    public List<HttpCookie> parse(String header){
                        return HttpCookie.parse(header,true);
                    }

                    public String header(HttpCookie cookie){
                        return cookie.header;
                    }
                }
        );
    }

    // ---------------- Fields --------------
    // The value of the cookie itself.
    private final String name;  // NAME= ... "$Name" style is reserved
    // The original header this cookie was consructed from, if it was
    // constructed by parsing a header, otherwise null.
    private final String header;
    // Hold the creation time (in seconds) of the http cookie for later
    // expiration calculation
    private final long whenCreated;
    private String value;       // value of NAME
    // Attributes encoded in the header's cookie fields.
    private String comment;     // Comment=VALUE ... describes cookie's use
    private String commentURL;  // CommentURL="http URL" ... describes cookie's use
    private boolean toDiscard;  // Discard ... discard cookie unconditionally
    private String domain;      // Domain=VALUE ... domain that sees cookie
    private long maxAge=MAX_AGE_UNSPECIFIED;  // Max-Age=VALUE ... cookies auto-expire
    // ---------------- Ctors --------------
    private String path;        // Path=VALUE ... URLs that see the cookie
    private String portlist;    // Port[="portlist"] ... the port cookie may be returned to
    private boolean secure;     // Secure ... e.g. use SSL
    private boolean httpOnly;   // HttpOnly ... i.e. not accessible to scripts
    // ---------------- Public operations --------------
    private int version=1;    // Version=1 ... RFC 2965 style

    public HttpCookie(String name,String value){
        this(name,value,null /**header*/);
    }

    private HttpCookie(String name,String value,String header){
        name=name.trim();
        if(name.length()==0||!isToken(name)||name.charAt(0)=='$'){
            throw new IllegalArgumentException("Illegal cookie name");
        }
        this.name=name;
        this.value=value;
        toDiscard=false;
        secure=false;
        whenCreated=System.currentTimeMillis();
        portlist=null;
        this.header=header;
    }

    private static boolean isToken(String value){
        int len=value.length();
        for(int i=0;i<len;i++){
            char c=value.charAt(i);
            if(c<0x20||c>=0x7f||tspecials.indexOf(c)!=-1)
                return false;
        }
        return true;
    }

    public static List<HttpCookie> parse(String header){
        return parse(header,false);
    }

    // Private version of parse() that will store the original header used to
    // create the cookie, in the cookie itself. This can be useful for filtering
    // Set-Cookie[2] headers, using the internal parsing logic defined in this
    // class.
    private static List<HttpCookie> parse(String header,boolean retainHeader){
        int version=guessCookieVersion(header);
        // if header start with set-cookie or set-cookie2, strip it off
        if(startsWithIgnoreCase(header,SET_COOKIE2)){
            header=header.substring(SET_COOKIE2.length());
        }else if(startsWithIgnoreCase(header,SET_COOKIE)){
            header=header.substring(SET_COOKIE.length());
        }
        List<HttpCookie> cookies=new ArrayList<>();
        // The Netscape cookie may have a comma in its expires attribute, while
        // the comma is the delimiter in rfc 2965/2109 cookie header string.
        // so the parse logic is slightly different
        if(version==0){
            // Netscape draft cookie
            HttpCookie cookie=parseInternal(header,retainHeader);
            cookie.setVersion(0);
            cookies.add(cookie);
        }else{
            // rfc2965/2109 cookie
            // if header string contains more than one cookie,
            // it'll separate them with comma
            List<String> cookieStrings=splitMultiCookies(header);
            for(String cookieStr : cookieStrings){
                HttpCookie cookie=parseInternal(cookieStr,retainHeader);
                cookie.setVersion(1);
                cookies.add(cookie);
            }
        }
        return cookies;
    }

    private static HttpCookie parseInternal(String header,
                                            boolean retainHeader){
        HttpCookie cookie=null;
        String namevaluePair=null;
        StringTokenizer tokenizer=new StringTokenizer(header,";");
        // there should always have at least on name-value pair;
        // it's cookie's name
        try{
            namevaluePair=tokenizer.nextToken();
            int index=namevaluePair.indexOf('=');
            if(index!=-1){
                String name=namevaluePair.substring(0,index).trim();
                String value=namevaluePair.substring(index+1).trim();
                if(retainHeader)
                    cookie=new HttpCookie(name,
                            stripOffSurroundingQuote(value),
                            header);
                else
                    cookie=new HttpCookie(name,
                            stripOffSurroundingQuote(value));
            }else{
                // no "=" in name-value pair; it's an error
                throw new IllegalArgumentException("Invalid cookie name-value pair");
            }
        }catch(NoSuchElementException ignored){
            throw new IllegalArgumentException("Empty cookie header string");
        }
        // remaining name-value pairs are cookie's attributes
        while(tokenizer.hasMoreTokens()){
            namevaluePair=tokenizer.nextToken();
            int index=namevaluePair.indexOf('=');
            String name, value;
            if(index!=-1){
                name=namevaluePair.substring(0,index).trim();
                value=namevaluePair.substring(index+1).trim();
            }else{
                name=namevaluePair.trim();
                value=null;
            }
            // assign attribute to cookie
            assignAttribute(cookie,name,value);
        }
        return cookie;
    }

    private static void assignAttribute(HttpCookie cookie,
                                        String attrName,
                                        String attrValue){
        // strip off the surrounding "-sign if there's any
        attrValue=stripOffSurroundingQuote(attrValue);
        CookieAttributeAssignor assignor=assignors.get(attrName.toLowerCase());
        if(assignor!=null){
            assignor.assign(cookie,attrName,attrValue);
        }else{
            // Ignore the attribute as per RFC 2965
        }
    }

    private static String stripOffSurroundingQuote(String str){
        if(str!=null&&str.length()>2&&
                str.charAt(0)=='"'&&str.charAt(str.length()-1)=='"'){
            return str.substring(1,str.length()-1);
        }
        if(str!=null&&str.length()>2&&
                str.charAt(0)=='\''&&str.charAt(str.length()-1)=='\''){
            return str.substring(1,str.length()-1);
        }
        return str;
    }

    private static int guessCookieVersion(String header){
        int version=0;
        header=header.toLowerCase();
        if(header.indexOf("expires=")!=-1){
            // only netscape cookie using 'expires'
            version=0;
        }else if(header.indexOf("version=")!=-1){
            // version is mandatory for rfc 2965/2109 cookie
            version=1;
        }else if(header.indexOf("max-age")!=-1){
            // rfc 2965/2109 use 'max-age'
            version=1;
        }else if(startsWithIgnoreCase(header,SET_COOKIE2)){
            // only rfc 2965 cookie starts with 'set-cookie2'
            version=1;
        }
        return version;
    }

    private static boolean startsWithIgnoreCase(String s,String start){
        if(s==null||start==null) return false;
        if(s.length()>=start.length()&&
                start.equalsIgnoreCase(s.substring(0,start.length()))){
            return true;
        }
        return false;
    }

    private static List<String> splitMultiCookies(String header){
        List<String> cookies=new ArrayList<String>();
        int quoteCount=0;
        int p, q;
        for(p=0,q=0;p<header.length();p++){
            char c=header.charAt(p);
            if(c=='"') quoteCount++;
            if(c==','&&(quoteCount%2==0)){
                // it is comma and not surrounding by double-quotes
                cookies.add(header.substring(q,p));
                q=p+1;
            }
        }
        cookies.add(header.substring(q));
        return cookies;
    }

    public static boolean domainMatches(String domain,String host){
        if(domain==null||host==null)
            return false;
        // if there's no embedded dot in domain and domain is not .local
        boolean isLocalDomain=".local".equalsIgnoreCase(domain);
        int embeddedDotInDomain=domain.indexOf('.');
        if(embeddedDotInDomain==0)
            embeddedDotInDomain=domain.indexOf('.',1);
        if(!isLocalDomain
                &&(embeddedDotInDomain==-1||
                embeddedDotInDomain==domain.length()-1))
            return false;
        // if the host name contains no dot and the domain name
        // is .local or host.local
        int firstDotInHost=host.indexOf('.');
        if(firstDotInHost==-1&&
                (isLocalDomain||
                        domain.equalsIgnoreCase(host+".local"))){
            return true;
        }
        int domainLength=domain.length();
        int lengthDiff=host.length()-domainLength;
        if(lengthDiff==0){
            // if the host name and the domain name are just string-compare euqal
            return host.equalsIgnoreCase(domain);
        }else if(lengthDiff>0){
            // need to check H & D component
            String H=host.substring(0,lengthDiff);
            String D=host.substring(lengthDiff);
            return (H.indexOf('.')==-1&&D.equalsIgnoreCase(domain));
        }else if(lengthDiff==-1){
            // if domain is actually .host
            return (domain.charAt(0)=='.'&&
                    host.equalsIgnoreCase(domain.substring(1)));
        }
        return false;
    }

    public boolean hasExpired(){
        if(maxAge==0) return true;
        // if not specify max-age, this cookie should be
        // discarded when user agent is to be closed, but
        // it is not expired.
        if(maxAge==MAX_AGE_UNSPECIFIED) return false;
        long deltaSecond=(System.currentTimeMillis()-whenCreated)/1000;
        if(deltaSecond>maxAge)
            return true;
        else
            return false;
    }

    public String getComment(){
        return comment;
    }

    public void setComment(String purpose){
        comment=purpose;
    }

    public String getCommentURL(){
        return commentURL;
    }

    public void setCommentURL(String purpose){
        commentURL=purpose;
    }

    public boolean getDiscard(){
        return toDiscard;
    }

    public void setDiscard(boolean discard){
        toDiscard=discard;
    }

    public long getMaxAge(){
        return maxAge;
    }

    public void setMaxAge(long expiry){
        maxAge=expiry;
    }

    public boolean getSecure(){
        return secure;
    }

    public void setSecure(boolean flag){
        secure=flag;
    }

    public boolean isHttpOnly(){
        return httpOnly;
    }

    public void setHttpOnly(boolean httpOnly){
        this.httpOnly=httpOnly;
    }

    @Override
    public int hashCode(){
        int h1=name.toLowerCase().hashCode();
        int h2=(domain!=null)?domain.toLowerCase().hashCode():0;
        int h3=(path!=null)?path.hashCode():0;
        return h1+h2+h3;
    }

    @Override
    public boolean equals(Object obj){
        if(obj==this)
            return true;
        if(!(obj instanceof HttpCookie))
            return false;
        HttpCookie other=(HttpCookie)obj;
        // One http cookie equals to another cookie (RFC 2965 sec. 3.3.3) if:
        //   1. they come from same domain (case-insensitive),
        //   2. have same name (case-insensitive),
        //   3. and have same path (case-sensitive).
        return equalsIgnoreCase(getName(),other.getName())&&
                equalsIgnoreCase(getDomain(),other.getDomain())&&
                Objects.equals(getPath(),other.getPath());
    }

    @Override
    public Object clone(){
        try{
            return super.clone();
        }catch(CloneNotSupportedException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public String toString(){
        if(getVersion()>0){
            return toRFC2965HeaderString();
        }else{
            return toNetscapeHeaderString();
        }
    }

    public int getVersion(){
        return version;
    }

    public void setVersion(int v){
        if(v!=0&&v!=1){
            throw new IllegalArgumentException("cookie version should be 0 or 1");
        }
        version=v;
    }

    private String toNetscapeHeaderString(){
        return getName()+"="+getValue();
    }

    private String toRFC2965HeaderString(){
        StringBuilder sb=new StringBuilder();
        sb.append(getName()).append("=\"").append(getValue()).append('"');
        if(getPath()!=null)
            sb.append(";$Path=\"").append(getPath()).append('"');
        if(getDomain()!=null)
            sb.append(";$Domain=\"").append(getDomain()).append('"');
        if(getPortlist()!=null)
            sb.append(";$Port=\"").append(getPortlist()).append('"');
        return sb.toString();
    }

    public String getPortlist(){
        return portlist;
    }

    public void setPortlist(String ports){
        portlist=ports;
    }

    public String getValue(){
        return value;
    }

    public void setValue(String newValue){
        value=newValue;
    }

    public String getDomain(){
        return domain;
    }

    public void setDomain(String pattern){
        if(pattern!=null)
            domain=pattern.toLowerCase();
        else
            domain=pattern;
    }

    public String getPath(){
        return path;
    }

    public void setPath(String uri){
        path=uri;
    }

    public String getName(){
        return name;
    }

    private static boolean equalsIgnoreCase(String s,String t){
        if(s==t) return true;
        if((s!=null)&&(t!=null)){
            return s.equalsIgnoreCase(t);
        }
        return false;
    }

    private String header(){
        return header;
    }

    private long expiryDate2DeltaSeconds(String dateString){
        Calendar cal=new GregorianCalendar(GMT);
        for(int i=0;i<COOKIE_DATE_FORMATS.length;i++){
            SimpleDateFormat df=new SimpleDateFormat(COOKIE_DATE_FORMATS[i],
                    Locale.US);
            cal.set(1970,0,1,0,0,0);
            df.setTimeZone(GMT);
            df.setLenient(false);
            df.set2DigitYearStart(cal.getTime());
            try{
                cal.setTime(df.parse(dateString));
                if(!COOKIE_DATE_FORMATS[i].contains("yyyy")){
                    // 2-digit years following the standard set
                    // out it rfc 6265
                    int year=cal.get(Calendar.YEAR);
                    year%=100;
                    if(year<70){
                        year+=2000;
                    }else{
                        year+=1900;
                    }
                    cal.set(Calendar.YEAR,year);
                }
                return (cal.getTimeInMillis()-whenCreated)/1000;
            }catch(Exception e){
                // Ignore, try the next date format
            }
        }
        return 0;
    }

    static interface CookieAttributeAssignor{
        public void assign(HttpCookie cookie,
                           String attrName,
                           String attrValue);
    }
}
