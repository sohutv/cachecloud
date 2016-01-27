package com.sohu.cache.web.vo;

import org.apache.commons.collections4.MapUtils;

import java.util.Map;

/**
 * Created by hym on 14-10-10.
 */
public class MemcacheStatItemVO {
    private String evictedNonzero = "";
    private String outOfMemory = "";
    private String reclaimed = "";
    private String age = "";
    private String evictedTime = "";
    private String number = "";
    private String tailRepairs = "";
    private String evicted = "";

    public MemcacheStatItemVO() {
    }

    public MemcacheStatItemVO(Map<String, String> map, String item) {
        String itemPre = "items:" + item + ":";
        setEvicted(MapUtils.getString(map, itemPre + "evicted", ""));
        setEvictedNonzero(MapUtils.getString(map, itemPre + "evicted_nonzero", ""));
        setEvictedTime(MapUtils.getString(map, itemPre + "evicted_time", ""));
        setAge(MapUtils.getString(map, itemPre + "age", ""));
        setReclaimed(MapUtils.getString(map, itemPre + "reclaimed", ""));
        setTailRepairs(MapUtils.getString(map, itemPre + "tailrepairs", ""));
        setNumber(MapUtils.getString(map, itemPre + "number", ""));
        setOutOfMemory(MapUtils.getString(map, itemPre + "outofmemory", ""));
    }

    public String getEvictedNonzero() {
        return evictedNonzero;
    }

    public void setEvictedNonzero(String evictedNonzero) {
        this.evictedNonzero = evictedNonzero;
    }

    public String getOutOfMemory() {
        return outOfMemory;
    }

    public void setOutOfMemory(String outOfMemory) {
        this.outOfMemory = outOfMemory;
    }

    public String getReclaimed() {
        return reclaimed;
    }

    public void setReclaimed(String reclaimed) {
        this.reclaimed = reclaimed;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getEvictedTime() {
        return evictedTime;
    }

    public void setEvictedTime(String evictedTime) {
        this.evictedTime = evictedTime;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getTailRepairs() {
        return tailRepairs;
    }

    public void setTailRepairs(String tailRepairs) {
        this.tailRepairs = tailRepairs;
    }

    public String getEvicted() {
        return evicted;
    }

    public void setEvicted(String evicted) {
        this.evicted = evicted;
    }

    @Override
    public String toString() {
        return "MemcacheStatItemVO{" +
                "evictedNonzero='" + evictedNonzero + '\'' +
                ", outOfMemory='" + outOfMemory + '\'' +
                ", reclaimed='" + reclaimed + '\'' +
                ", age='" + age + '\'' +
                ", evictedTime='" + evictedTime + '\'' +
                ", number='" + number + '\'' +
                ", tailRepairs='" + tailRepairs + '\'' +
                ", evicted='" + evicted + '\'' +
                '}';
    }
}
