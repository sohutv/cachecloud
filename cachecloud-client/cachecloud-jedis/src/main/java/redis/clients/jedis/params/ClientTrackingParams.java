package redis.clients.jedis.params;

import redis.clients.jedis.Protocol;
import redis.clients.jedis.util.SafeEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author wenruiwu
 * @create 2020/6/15 14:26
 * @description
 */
public class ClientTrackingParams extends Params {

    private static final String REDIRECT = "REDIRECT";
    private static final String BCAST = "BCAST";
    private static final String PREFIX = "PREFIX";
    private static final String OPTIN = "OPTIN";
    private static final String OPTOUT = "OPTOUT";

    private final List<String> prefixList = new ArrayList<>();

    public ClientTrackingParams id(Long redirectId){
        addParam(REDIRECT, redirectId);
        return this;
    }

    public ClientTrackingParams bcast(){
        addParam(BCAST);
        return this;
    }

    public ClientTrackingParams prefix(String prefix){
        //addParam(PREFIX, prefix);
        prefixList.add(prefix);
        return this;
    }

    public ClientTrackingParams optin(){
        addParam(OPTIN);
        return this;
    }

    public ClientTrackingParams optout(){
        addParam(OPTOUT);
        return this;
    }

    public byte[][] getByteParams(byte[]... args){
      ArrayList<byte[]> byteParams = new ArrayList<byte[]>();
      for (byte[] arg : args) {
        byteParams.add(arg);
      }
      if(contains(REDIRECT)){
        byteParams.add(SafeEncoder.encode(REDIRECT));
        byteParams.add(Protocol.toByteArray((long)getParam(REDIRECT)));
      }
      if(contains(BCAST)){
        byteParams.add(SafeEncoder.encode(BCAST));
      }
      if(prefixList.size()>0){
        for(String prefix : prefixList){
          byteParams.add(SafeEncoder.encode(PREFIX));
          byteParams.add(SafeEncoder.encode(prefix));
        }
      }
      if(contains(OPTIN)){
        byteParams.add(SafeEncoder.encode(OPTIN));
      }
      if(contains(OPTOUT)){
        byteParams.add(SafeEncoder.encode(OPTOUT));
      }
      return byteParams.toArray(new byte[byteParams.size()][]);
    }


    public enum Latch {
        ON, OFF;

        public final byte[] raw;

        Latch() {
            raw = SafeEncoder.encode(this.name().toLowerCase(Locale.ENGLISH));
        }
    }
}
