package java.util;

import sun.security.action.GetPropertyAction;
import sun.util.calendar.ZoneInfo;
import sun.util.calendar.ZoneInfoFile;
import sun.util.locale.provider.TimeZoneNameUtility;

import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.ZoneId;

abstract public class TimeZone implements Serializable, Cloneable{
    public static final int SHORT=0;
    public static final int LONG=1;
    // Proclaim serialization compatibility with JDK 1.1
    static final long serialVersionUID=3581463369166924961L;
    static final TimeZone NO_TIMEZONE=null;
    static final String GMT_ID="GMT";
    // Constants used internally; unit is milliseconds
    private static final int ONE_MINUTE=60*1000;
    private static final int ONE_HOUR=60*ONE_MINUTE;
    private static final int ONE_DAY=24*ONE_HOUR;
    private static final int GMT_ID_LENGTH=3;
    private static volatile TimeZone defaultTimeZone;
    // a static TimeZone we can reference if no AppContext is in place
    private static volatile TimeZone mainAppContextDefault;
    // =======================privates===============================
    private String ID;

    public TimeZone(){
    }

    private static String[] getDisplayNames(String id,Locale locale){
        return TimeZoneNameUtility.retrieveDisplayNames(id,locale);
    }

    public static synchronized TimeZone getTimeZone(String ID){
        return getTimeZone(ID,true);
    }

    private static TimeZone getTimeZone(String ID,boolean fallback){
        TimeZone tz=ZoneInfo.getTimeZone(ID);
        if(tz==null){
            tz=parseCustomTimeZone(ID);
            if(tz==null&&fallback){
                tz=new ZoneInfo(GMT_ID,0);
            }
        }
        return tz;
    }

    private static final TimeZone parseCustomTimeZone(String id){
        int length;
        // Error if the length of id isn't long enough or id doesn't
        // start with "GMT".
        if((length=id.length())<(GMT_ID_LENGTH+2)||
                id.indexOf(GMT_ID)!=0){
            return null;
        }
        ZoneInfo zi;
        // First, we try to find it in the cache with the given
        // id. Even the id is not normalized, the returned ZoneInfo
        // should have its normalized id.
        zi=ZoneInfoFile.getZoneInfo(id);
        if(zi!=null){
            return zi;
        }
        int index=GMT_ID_LENGTH;
        boolean negative=false;
        char c=id.charAt(index++);
        if(c=='-'){
            negative=true;
        }else if(c!='+'){
            return null;
        }
        int hours=0;
        int num=0;
        int countDelim=0;
        int len=0;
        while(index<length){
            c=id.charAt(index++);
            if(c==':'){
                if(countDelim>0){
                    return null;
                }
                if(len>2){
                    return null;
                }
                hours=num;
                countDelim++;
                num=0;
                len=0;
                continue;
            }
            if(c<'0'||c>'9'){
                return null;
            }
            num=num*10+(c-'0');
            len++;
        }
        if(index!=length){
            return null;
        }
        if(countDelim==0){
            if(len<=2){
                hours=num;
                num=0;
            }else{
                hours=num/100;
                num%=100;
            }
        }else{
            if(len!=2){
                return null;
            }
        }
        if(hours>23||num>59){
            return null;
        }
        int gmtOffset=(hours*60+num)*60*1000;
        if(gmtOffset==0){
            zi=ZoneInfoFile.getZoneInfo(GMT_ID);
            if(negative){
                zi.setID("GMT-00:00");
            }else{
                zi.setID("GMT+00:00");
            }
        }else{
            zi=ZoneInfoFile.getCustomTimeZone(id,negative?-gmtOffset:gmtOffset);
        }
        return zi;
    }

    public static TimeZone getTimeZone(ZoneId zoneId){
        String tzid=zoneId.getId(); // throws an NPE if null
        char c=tzid.charAt(0);
        if(c=='+'||c=='-'){
            tzid="GMT"+tzid;
        }else if(c=='Z'&&tzid.length()==1){
            tzid="UTC";
        }
        return getTimeZone(tzid,true);
    }

    public static synchronized String[] getAvailableIDs(int rawOffset){
        return ZoneInfo.getAvailableIDs(rawOffset);
    }

    public static synchronized String[] getAvailableIDs(){
        return ZoneInfo.getAvailableIDs();
    }

    public static TimeZone getDefault(){
        return (TimeZone)getDefaultRef().clone();
    }

    static TimeZone getDefaultRef(){
        TimeZone defaultZone=defaultTimeZone;
        if(defaultZone==null){
            // Need to initialize the default time zone.
            defaultZone=setDefaultZone();
            assert defaultZone!=null;
        }
        // Don't clone here.
        return defaultZone;
    }

    private static synchronized TimeZone setDefaultZone(){
        TimeZone tz;
        // get the time zone ID from the system properties
        String zoneID=AccessController.doPrivileged(
                new GetPropertyAction("user.timezone"));
        // if the time zone ID is not set (yet), perform the
        // platform to Java time zone ID mapping.
        if(zoneID==null||zoneID.isEmpty()){
            String javaHome=AccessController.doPrivileged(
                    new GetPropertyAction("java.home"));
            try{
                zoneID=getSystemTimeZoneID(javaHome);
                if(zoneID==null){
                    zoneID=GMT_ID;
                }
            }catch(NullPointerException e){
                zoneID=GMT_ID;
            }
        }
        // Get the time zone for zoneID. But not fall back to
        // "GMT" here.
        tz=getTimeZone(zoneID,false);
        if(tz==null){
            // If the given zone ID is unknown in Java, try to
            // get the GMT-offset-based time zone ID,
            // a.k.a. custom time zone ID (e.g., "GMT-08:00").
            String gmtOffsetID=getSystemGMTOffsetID();
            if(gmtOffsetID!=null){
                zoneID=gmtOffsetID;
            }
            tz=getTimeZone(zoneID,true);
        }
        assert tz!=null;
        final String id=zoneID;
        AccessController.doPrivileged(new PrivilegedAction<Void>(){
            @Override
            public Void run(){
                System.setProperty("user.timezone",id);
                return null;
            }
        });
        defaultTimeZone=tz;
        return tz;
    }

    private static native String getSystemTimeZoneID(String javaHome);

    private static native String getSystemGMTOffsetID();

    public static void setDefault(TimeZone zone){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            sm.checkPermission(new PropertyPermission
                    ("user.timezone","write"));
        }
        defaultTimeZone=zone;
    }

    public abstract int getOffset(int era,int year,int month,int day,
                                  int dayOfWeek,int milliseconds);

    public int getOffset(long date){
        if(inDaylightTime(new Date(date))){
            return getRawOffset()+getDSTSavings();
        }
        return getRawOffset();
    }

    public abstract int getRawOffset();

    abstract public void setRawOffset(int offsetMillis);

    public int getDSTSavings(){
        if(useDaylightTime()){
            return 3600000;
        }
        return 0;
    }

    public abstract boolean useDaylightTime();

    abstract public boolean inDaylightTime(Date date);

    int getOffsets(long date,int[] offsets){
        int rawoffset=getRawOffset();
        int dstoffset=0;
        if(inDaylightTime(new Date(date))){
            dstoffset=getDSTSavings();
        }
        if(offsets!=null){
            offsets[0]=rawoffset;
            offsets[1]=dstoffset;
        }
        return rawoffset+dstoffset;
    }

    public final String getDisplayName(){
        return getDisplayName(false,LONG,
                Locale.getDefault(Locale.Category.DISPLAY));
    }

    public String getDisplayName(boolean daylight,int style,Locale locale){
        if(style!=SHORT&&style!=LONG){
            throw new IllegalArgumentException("Illegal style: "+style);
        }
        String id=getID();
        String name=TimeZoneNameUtility.retrieveDisplayName(id,daylight,style,locale);
        if(name!=null){
            return name;
        }
        if(id.startsWith("GMT")&&id.length()>3){
            char sign=id.charAt(3);
            if(sign=='+'||sign=='-'){
                return id;
            }
        }
        int offset=getRawOffset();
        if(daylight){
            offset+=getDSTSavings();
        }
        return ZoneInfoFile.toCustomID(offset);
    }

    public String getID(){
        return ID;
    }

    public void setID(String ID){
        if(ID==null){
            throw new NullPointerException();
        }
        this.ID=ID;
    }

    public final String getDisplayName(Locale locale){
        return getDisplayName(false,LONG,locale);
    }

    public final String getDisplayName(boolean daylight,int style){
        return getDisplayName(daylight,style,
                Locale.getDefault(Locale.Category.DISPLAY));
    }

    public boolean observesDaylightTime(){
        return useDaylightTime()||inDaylightTime(new Date());
    }

    public ZoneId toZoneId(){
        String id=getID();
        if(ZoneInfoFile.useOldMapping()&&id.length()==3){
            if("EST".equals(id))
                return ZoneId.of("America/New_York");
            if("MST".equals(id))
                return ZoneId.of("America/Denver");
            if("HST".equals(id))
                return ZoneId.of("America/Honolulu");
        }
        return ZoneId.of(id,ZoneId.SHORT_IDS);
    }

    public boolean hasSameRules(TimeZone other){
        return other!=null&&getRawOffset()==other.getRawOffset()&&
                useDaylightTime()==other.useDaylightTime();
    }

    public Object clone(){
        try{
            TimeZone other=(TimeZone)super.clone();
            other.ID=ID;
            return other;
        }catch(CloneNotSupportedException e){
            throw new InternalError(e);
        }
    }
}
