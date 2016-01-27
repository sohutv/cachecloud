package com.sohu.cache.web.chart.model;

import com.sohu.cache.web.chart.key.ChartKeysUtil;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class ChartEntity {
    private Map<String, Object> chart;
    private Map<String, Object> title;
    private Map<String, Object> subtitle;
    private Map<String, Object> xAxis;
    private Map<String, Object> yAxis;
    private Map<String, Object> tooltip;
    private Map<String, Object> legend;
    private List<Map<String, Object>> series;

    public ChartEntity() {
        // 初始化chart
        Map<String, Object> chart = new LinkedHashMap<String, Object>();
        chart.put(ChartKeysUtil.ChartKey.RENDER_TO.getKey(), "container");
        this.setChart(chart);
        // 初始化title
        Map<String, Object> title = new LinkedHashMap<String, Object>();
        title.put(ChartKeysUtil.TitleKey.TEXT.getKey(), "请设置图表title");
        this.setTitle(title);
        // 初始化subtitle
        Map<String, Object> subtitle = new LinkedHashMap<String, Object>();
        this.setSubtitle(subtitle);
        // 初始化xAxis
        Map<String, Object> xAxis = new LinkedHashMap<String, Object>();
        this.setxAxis(xAxis);
        // 初始化yAxis
        Map<String, Object> yAxis = new LinkedHashMap<String, Object>();
        this.setyAxis(yAxis);
        // 初始化tooltip
        Map<String, Object> tooltip = new LinkedHashMap<String, Object>();
        this.setTooltip(tooltip);
        // 初始化legend
        Map<String, Object> legend = new LinkedHashMap<String, Object>();
        this.setLegend(legend);
        //初始化series
        List<Map<String, Object>> series = new LinkedList<Map<String, Object>>();
        this.setSeries(series);

    }

    /**
     * 所有曲线类型必须设置曲线类型
     */
    protected abstract void putChartType();

    /**
     * 指定容器，即页面div的id
     * 默认为container，用户可以覆盖
     *
     * @param container 页面div的id
     */
    public void renderTo(String container) {
        this.putChart(ChartKeysUtil.ChartKey.RENDER_TO.getKey(), container);
    }

    /**
     * 设置chart属性
     *
     * @param key
     * @param value
     */
    public void putChart(String key, Object value) {
        this.getChart().put(key, value);
    }

    /**
     * 设置title属性
     *
     * @param key
     * @param value
     */
    public void putTitle(String key, Object value) {
        this.getTitle().put(key, value);
    }

    /**
     * 设置subTitle属性
     *
     * @param key
     * @param value
     */
    public void putSubTitle(String key, Object value) {
        this.getSubtitle().put(key, value);
    }

    /**
     * @param key
     * @param value
     */
    public void putXAxis(String key, Object value) {
        this.getxAxis().put(key, value);
    }

    /**
     * @param key
     * @param value
     */
    public void putYAxis(String key, Object value) {
        this.getyAxis().put(key, value);
    }

    /**
     * @param key
     * @param value
     */
    public void putTooltip(String key, Object value) {
        this.getTooltip().put(key, value);
    }

    /**
     * @param key
     * @param value
     */
    public void putLegend(String key, Object value) {
        this.getLegend().put(key, value);
    }

    /**
     * @param series
     */
    public void putSeries(Map<String, Object> series) {
        this.getSeries().add(series);
    }










    // 以下是get set 方法
    public Map<String, Object> getChart() {
        return chart;
    }

    private void setChart(Map<String, Object> chart) {
        this.chart = chart;
    }

    public Map<String, Object> getTitle() {
        return title;
    }

    private void setTitle(Map<String, Object> title) {
        this.title = title;
    }

    public Map<String, Object> getSubtitle() {
        return subtitle;
    }

    private void setSubtitle(Map<String, Object> subtitle) {
        this.subtitle = subtitle;
    }

    public Map<String, Object> getxAxis() {
        return xAxis;
    }

    private void setxAxis(Map<String, Object> xAxis) {
        this.xAxis = xAxis;
    }

    public Map<String, Object> getyAxis() {
        return yAxis;
    }

    private void setyAxis(Map<String, Object> yAxis) {
        this.yAxis = yAxis;
    }

    public Map<String, Object> getTooltip() {
        return tooltip;
    }

    private void setTooltip(Map<String, Object> tooltip) {
        this.tooltip = tooltip;
    }

    public Map<String, Object> getLegend() {
        return legend;
    }

    private void setLegend(Map<String, Object> legend) {
        this.legend = legend;
    }

    public List<Map<String, Object>> getSeries() {
        return series;
    }

    private void setSeries(List<Map<String, Object>> series) {
        this.series = series;
    }
}
