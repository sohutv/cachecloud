package com.sohu.cache.web.vo;

import org.apache.commons.collections4.MapUtils;

import java.util.Map;

/**
 * Created by hym on 14-10-10.
 */
public class MemcacheStatSlabVO {
    public MemcacheStatSlabVO(Map<String, String> map, String slab) {
        String slabPre = slab + ":";
        setCasBadval(MapUtils.getString(map, slabPre + "cas_badval", ""));
        setTotalChunks(MapUtils.getString(map, slabPre + "total_chunks", ""));
        setMemRequested(MapUtils.getString(map, slabPre + "mem_requested", ""));
        setCasHits(MapUtils.getString(map, slabPre + "cas_hits", ""));
        setTotalPages(MapUtils.getString(map, slabPre + "total_pages", ""));
        setUsedChunks(MapUtils.getString(map, slabPre + "used_chunks", ""));
        setFreeChunks(MapUtils.getString(map, slabPre + "free_chunks", ""));
        setIncrHits(MapUtils.getString(map, slabPre + "incr_hits", ""));
        setDeleteHits(MapUtils.getString(map, slabPre + "delete_hits", ""));
        setCmdSet(MapUtils.getString(map, slabPre + "cmd_set", ""));
        setGetHits(MapUtils.getString(map, slabPre + "get_hits", ""));
        setChunkSize(MapUtils.getString(map, slabPre + "chunk_size", ""));
        setDecrHits(MapUtils.getString(map, slabPre + "decr_hits", ""));
        setChunksPerPage(MapUtils.getString(map, slabPre + "chunks_per_page", ""));
        setFreeChunksEnd(MapUtils.getString(map, slabPre + "free_chunks_end", ""));
    }

    private String freeChunks = "";
    private String incrHits = "";
    private String deleteHits = "";
    private String cmdSet = "";
    private String getHits = "";
    private String chunkSize = "";
    private String decrHits = "";
    private String chunksPerPage = "";
    private String freeChunksEnd = "";
    private String usedChunks = "";
    private String totalPages = "";
    private String casHits = "";
    private String memRequested = "";
    private String casBadval = "";
    private String totalChunks = "";

    public String getFreeChunks() {
        return freeChunks;
    }

    public void setFreeChunks(String freeChunks) {
        this.freeChunks = freeChunks;
    }

    public String getIncrHits() {
        return incrHits;
    }

    public void setIncrHits(String incrHits) {
        this.incrHits = incrHits;
    }

    public String getDeleteHits() {
        return deleteHits;
    }

    public void setDeleteHits(String deleteHits) {
        this.deleteHits = deleteHits;
    }

    public String getCmdSet() {
        return cmdSet;
    }

    public void setCmdSet(String cmdSet) {
        this.cmdSet = cmdSet;
    }

    public String getGetHits() {
        return getHits;
    }

    public void setGetHits(String getHits) {
        this.getHits = getHits;
    }

    public String getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(String chunkSize) {
        this.chunkSize = chunkSize;
    }

    public String getDecrHits() {
        return decrHits;
    }

    public void setDecrHits(String decrHits) {
        this.decrHits = decrHits;
    }

    public String getChunksPerPage() {
        return chunksPerPage;
    }

    public void setChunksPerPage(String chunksPerPage) {
        this.chunksPerPage = chunksPerPage;
    }

    public String getFreeChunksEnd() {
        return freeChunksEnd;
    }

    public void setFreeChunksEnd(String freeChunksEnd) {
        this.freeChunksEnd = freeChunksEnd;
    }

    public String getUsedChunks() {
        return usedChunks;
    }

    public void setUsedChunks(String usedChunks) {
        this.usedChunks = usedChunks;
    }

    public String getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(String totalPages) {
        this.totalPages = totalPages;
    }

    public String getCasHits() {
        return casHits;
    }

    public void setCasHits(String casHits) {
        this.casHits = casHits;
    }

    public String getMemRequested() {
        return memRequested;
    }

    public void setMemRequested(String memRequested) {
        this.memRequested = memRequested;
    }

    public String getCasBadval() {
        return casBadval;
    }

    public void setCasBadval(String casBadval) {
        this.casBadval = casBadval;
    }

    public String getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(String totalChunks) {
        this.totalChunks = totalChunks;
    }

    @Override
    public String toString() {
        return "MemcacheStatSlabVO{" +
                "freeChunks='" + freeChunks + '\'' +
                ", incrHits='" + incrHits + '\'' +
                ", deleteHits='" + deleteHits + '\'' +
                ", cmdSet='" + cmdSet + '\'' +
                ", getHits='" + getHits + '\'' +
                ", chunkSize='" + chunkSize + '\'' +
                ", decrHits='" + decrHits + '\'' +
                ", chunksPerPage='" + chunksPerPage + '\'' +
                ", freeChunksEnd='" + freeChunksEnd + '\'' +
                ", usedChunks='" + usedChunks + '\'' +
                ", totalPages='" + totalPages + '\'' +
                ", casHits='" + casHits + '\'' +
                ", memRequested='" + memRequested + '\'' +
                ", casBadval='" + casBadval + '\'' +
                ", totalChunks='" + totalChunks + '\'' +
                '}';
    }
}
