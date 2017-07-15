package java.time;

import java.io.*;
import java.time.zone.ZoneRules;
import java.time.zone.ZoneRulesException;
import java.time.zone.ZoneRulesProvider;
import java.util.Objects;

final class ZoneRegion extends ZoneId implements Serializable{
    private static final long serialVersionUID=8386373296231747096L;
    private final String id;
    private final transient ZoneRules rules;

    //-------------------------------------------------------------------------
    ZoneRegion(String id,ZoneRules rules){
        this.id=id;
        this.rules=rules;
    }

    static ZoneRegion ofId(String zoneId,boolean checkAvailable){
        Objects.requireNonNull(zoneId,"zoneId");
        checkName(zoneId);
        ZoneRules rules=null;
        try{
            // always attempt load for better behavior after deserialization
            rules=ZoneRulesProvider.getRules(zoneId,true);
        }catch(ZoneRulesException ex){
            if(checkAvailable){
                throw ex;
            }
        }
        return new ZoneRegion(zoneId,rules);
    }

    private static void checkName(String zoneId){
        int n=zoneId.length();
        if(n<2){
            throw new DateTimeException("Invalid ID for region-based ZoneId, invalid format: "+zoneId);
        }
        for(int i=0;i<n;i++){
            char c=zoneId.charAt(i);
            if(c>='a'&&c<='z') continue;
            if(c>='A'&&c<='Z') continue;
            if(c=='/'&&i!=0) continue;
            if(c>='0'&&c<='9'&&i!=0) continue;
            if(c=='~'&&i!=0) continue;
            if(c=='.'&&i!=0) continue;
            if(c=='_'&&i!=0) continue;
            if(c=='+'&&i!=0) continue;
            if(c=='-'&&i!=0) continue;
            throw new DateTimeException("Invalid ID for region-based ZoneId, invalid format: "+zoneId);
        }
    }

    static ZoneId readExternal(DataInput in) throws IOException{
        String id=in.readUTF();
        return ZoneId.of(id,false);
    }

    //-----------------------------------------------------------------------
    @Override
    public String getId(){
        return id;
    }

    @Override
    public ZoneRules getRules(){
        // additional query for group provider when null allows for possibility
        // that the provider was updated after the ZoneId was created
        return (rules!=null?rules:ZoneRulesProvider.getRules(id,false));
    }

    @Override
    void write(DataOutput out) throws IOException{
        out.writeByte(Ser.ZONE_REGION_TYPE);
        writeExternal(out);
    }

    void writeExternal(DataOutput out) throws IOException{
        out.writeUTF(id);
    }

    //-----------------------------------------------------------------------
    private Object writeReplace(){
        return new Ser(Ser.ZONE_REGION_TYPE,this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException{
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }
}
