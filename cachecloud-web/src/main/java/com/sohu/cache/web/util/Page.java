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
     * 当前页数
     */
    private int pageNo;
    
    /**
     * 每页的记录数
     */
    private int pageSize;
    
    /**
     * 总记录数
     */
    private int totalCount;
    
    public Page(int pageNo, int pageSize, int totalCount) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
    }

    /**
     * 取总页数
     */
    public int getTotalPages() {
        if (totalCount % pageSize == 0)
            return totalCount / pageSize;
        else
            return totalCount / pageSize + 1;
    }

    /**
     * 获取任一页第一条数据的位置,startIndex从0开始
     */
    public int getStart() {
        return (pageNo - 1) * pageSize;
    }
    
    public int getNumberOfPages() {
        int totalPageCount = getTotalPages();
        return totalPageCount >= 10 ? 10 : totalPageCount;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

}
