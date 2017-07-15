package java.time;

import java.io.*;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.temporal.*;
import java.time.zone.ZoneRules;
import java.time.zone.ZoneRulesException;
import java.time.zone.ZoneRulesProvider;
import java.util.*;

public abstract class ZoneId implements Serializable{
    public static final Map<String,String> SHORT_IDS;
    private static final long serialVersionUID=8352817235686L;

    static{
        Map<String,String> map=new HashMap<>(64);
        map.put("ACT","Australia/Darwin");
        map.put("AET","Australia/Sydney");
        map.put("AGT","America/Argentina/Buenos_Aires");
        map.put("ART","Africa/Cairo");
        map.put("AST","America/Anchorage");
        map.put("BET","America/Sao_Paulo");
        map.put("BST","Asia/Dhaka");
        map.put("CAT","Africa/Harare");
        map.put("CNT","America/St_Johns");
        map.put("CST","America/Chicago");
        map.put("CTT","Asia/Shanghai");
        map.put("EAT","Africa/Addis_Ababa");
        map.put("ECT","Europe/Paris");
        map.put("IET","America/Indiana/Indianapolis");
        map.put("IST","Asia/Kolkata");
        map.put("JST","Asia/Tokyo");
        map.put("MIT","Pacific/Apia");
        map.put("NET","Asia/Yerevan");
        map.put("NST","Pacific/Auckland");
        map.put("PLT","Asia/Karachi");
        map.put("PNT","America/Phoenix");
        map.put("PRT","America/Puerto_Rico");
        map.put("PST","America/Los_Angeles");
        map.put("SST","Pacific/Guadalcanal");
        map.put("VST","Asia/Ho_Chi_Minh");
        map.put("EST","-05:00");
        map.put("MST","-07:00");
        map.put("HST","-10:00");
        /**存放在不可更改的 map 中*/
        SHORT_IDS=Collections.unmodifiableMap(map);
    }

    ZoneId(){
        if(getClass()!=ZoneOffset.class&&getClass()!=ZoneRegion.class){
            throw new AssertionError("无效的子类(Invalid subclass)");
        }
    }

    /*
    * 这是默认唯一方法
    * */
    public static ZoneId systemDefault(){
        return TimeZone.getDefault().toZoneId();
    }

    public static Set<String> getAvailableZoneIds(){
        return ZoneRulesProvider.getAvailableZoneIds();
    }

    public static ZoneId of(String zoneId,Map<String,String> aliasMap){
        Objects.requireNonNull(zoneId,"zoneId");
        Objects.requireNonNull(aliasMap,"aliasMap");
        String id=aliasMap.get(zoneId);
        id=(id!=null?id:zoneId);
        return of(id);
    }

    public static ZoneId of(String zoneId){
        return of(zoneId,true);
    }

    static ZoneId of(String zoneId,boolean checkAvailable){
        Objects.requireNonNull(zoneId,"zoneId");
        if(zoneId.length()<=1||zoneId.startsWith("+")||zoneId.startsWith("-")){
            return ZoneOffset.of(zoneId);
        }else if(zoneId.startsWith("UTC")||zoneId.startsWith("GMT")){
            return ofWithPrefix(zoneId,3,checkAvailable);
        }else if(zoneId.startsWith("UT")){
            return ofWithPrefix(zoneId,2,checkAvailable);
        }
        return ZoneRegion.ofId(zoneId,checkAvailable);
    }

    private static ZoneId ofWithPrefix(String zoneId,int prefixLength,boolean checkAvailable){
        String prefix=zoneId.substring(0,prefixLength);
        if(zoneId.length()==prefixLength){
            return ofOffset(prefix,ZoneOffset.UTC);
        }
        if(zoneId.charAt(prefixLength)!='+'&&zoneId.charAt(prefixLength)!='-'){
            return ZoneRegion.ofId(zoneId,checkAvailable);  // drop through to ZoneRulesProvider
        }
        try{
            ZoneOffset offset=ZoneOffset.of(zoneId.substring(prefixLength));
            if(offset==ZoneOffset.UTC){
                return ofOffset(prefix,offset);
            }
            return ofOffset(prefix,offset);
        }catch(DateTimeException ex){
            throw new DateTimeException("Invalid ID for offset-based ZoneId: "+zoneId,ex);
        }
    }

    public static ZoneId ofOffset(String prefix,ZoneOffset offset){
        Objects.requireNonNull(prefix,"prefix");
        Objects.requireNonNull(offset,"offset");
        if(prefix.length()==0){
            return offset;
        }
        if(!prefix.equals("GMT")&&!prefix.equals("UTC")&&!prefix.equals("UT")){
            throw new IllegalArgumentException("prefix should be GMT, UTC or UT, is: "+prefix);
        }
        if(offset.getTotalSeconds()!=0){
            prefix=prefix.concat(offset.getId());
        }
        return new ZoneRegion(prefix,offset.getRules());
    }

    public static ZoneId from(TemporalAccessor temporal){
        ZoneId obj=temporal.query(TemporalQueries.zone());
        if(obj==null){
            throw new DateTimeException("Unable to obtain ZoneId from TemporalAccessor: "+
                    temporal+" of type "+temporal.getClass().getName());
        }
        return obj;
    }

    public String getDisplayName(TextStyle style,Locale locale){
        return new DateTimeFormatterBuilder().appendZoneText(style).toFormatter(locale).format(toTemporal());
    }

    private TemporalAccessor toTemporal(){
        return new TemporalAccessor(){
            @Override
            public boolean isSupported(TemporalField field){
                return false;
            }

            @Override
            public long getLong(TemporalField field){
                throw new UnsupportedTemporalTypeException("Unsupported field: "+field);
            }

            @SuppressWarnings("unchecked")
            @Override
            public <R> R query(TemporalQuery<R> query){
                if(query==TemporalQueries.zoneId()){
                    return (R)ZoneId.this;
                }
                return TemporalAccessor.super.query(query);
            }
        };
    }

    public ZoneId normalized(){
        try{
            ZoneRules rules=getRules();
            if(rules.isFixedOffset()){
                return rules.getOffset(Instant.EPOCH);
            }
        }catch(ZoneRulesException ex){
            // invalid ZoneRegion is not important to this method
        }
        return this;
    }

    public abstract ZoneRules getRules();

    @Override
    public int hashCode(){
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object obj){
        if(this==obj){
            return true;
        }
        if(obj instanceof ZoneId){
            ZoneId other=(ZoneId)obj;
            return getId().equals(other.getId());
        }
        return false;
    }

    @Override
    public String toString(){
        return getId();
    }

    public abstract String getId();

    private void readObject(ObjectInputStream s) throws InvalidObjectException{
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    private Object writeReplace(){
        return new Ser(Ser.ZONE_REGION_TYPE,this);
    }

    abstract void write(DataOutput out) throws IOException;
}
