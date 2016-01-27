package com.sohu.cache.web.util;

/**
 * 分页对象
 * @author leifu
 * @Date 2015年2月10日
 * @Time 下午6:38:18
 */
public class Page implements java.io.Serializable {
    private static final long serialVersionUID = 7887139614696114877L;
    
    /**
     * 默认分页10个
     */
    public static final int DEFAULT_PAGE_SIZE = 10;
    
    /**
     * 当前页第一条数据的位置,从0开始
     */
    private int start;
    
    /**
     * 每页的记录数
     */
    private int pageSize = DEFAULT_PAGE_SIZE;
    
    /**
     * 总记录数
     */
    private int totalCount;
    
    public Page(int start, int totalCount) {
        this.start = start;
        this.totalCount = totalCount;
    }

    /**
     * 默认构造方法
     *
     * @param start     本页数据在数据库中的起始位置
     * @param totalSize 数据库中总记录条数
     * @param pageSize  本页容量
     */
    public Page(int start, int totalSize, int pageSize) {
        this.pageSize = pageSize;
        this.start = start;
        this.totalCount = totalSize;
    }

    /**
     * 取数据库中包含的总记录数
     */
    public int getTotalCount() {
        return this.totalCount;
    }

    /**
     * 取总页数
     */
    public int getTotalPageCount() {
        if (totalCount % pageSize == 0)
            return totalCount / pageSize;
        else
            return totalCount / pageSize + 1;
    }

    /**
     * 取每页数据容量
     */
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * 取当前页码,页码从1开始
     */
    public int getCurrentPageNo() {
        return (start / pageSize) + 1;
    }

    /**
     * 获取任一页第一条数据的位置，每页条数使用默认值
     */
    public static int getStartOfPage(int pageNo) {
        return getStartOfPage(pageNo, DEFAULT_PAGE_SIZE);
    }

    /**
     * 获取任一页第一条数据的位置,startIndex从0开始
     */
    public static int getStartOfPage(int pageNo, int pageSize) {
        return (pageNo - 1) * pageSize;
    }
    
    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    @Override
    public String toString() {
        return "Page [start=" + start + ", pageSize=" + pageSize + ", totalCount=" + totalCount + "]";
    }

}
