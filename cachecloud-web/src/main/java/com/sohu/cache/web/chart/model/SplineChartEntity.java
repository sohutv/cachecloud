package com.sohu.cache.web.chart.model;

import com.sohu.cache.web.chart.key.ChartKeysUtil;

import java.util.*;

public class SplineChartEntity extends ChartEntity {
    public SplineChartEntity() {
        super();
        putChartType();
        setTooltipShared(true);
    }

    @Override
    protected void putChartType() {
        this.getChart().put(ChartKeysUtil.ChartKey.TYPE.getKey(), "spline");
    }

    public void setXAxisCategories(List<Object> xAxisCategories) {
        setXAxisCategories(xAxisCategories, 14);
    }

    public void setXAxisCategories(List<Object> xAxisCategories, int totalLabels) {
        setXAxisCategories(xAxisCategories, totalLabels, 0, -5);
    }

    public void setXAxisCategories(List<Object> xAxisCategories, int totalLabels, int rotation, int y) {
        putXAxis(ChartKeysUtil.XAxisKey.CATEGORIES.getKey(), xAxisCategories);
        if (xAxisCategories.size() >= totalLabels) {
            Map<String, Object> m = null;
            if (this.getxAxis().containsKey(ChartKeysUtil.XAxisKey.LABELS.getKey())) {
                m = (Map<String, Object>) this.getxAxis().get(ChartKeysUtil.XAxisKey.LABELS.getKey());
            }
            if (m == null) {
                m = new LinkedHashMap<String, Object>();
            }
            m.put(ChartKeysUtil.XAxisKey.LABELS_STEP.getKey(), xAxisCategories.size() / totalLabels + 1);
            m.put(ChartKeysUtil.XAxisKey.LABELS_ROTATION.getKey(), rotation);
            m.put(ChartKeysUtil.XAxisKey.LABELS_Y.getKey(), y);
            m.put(ChartKeysUtil.XAxisKey.MAX_STAGGER_LINES.getKey(), 1);
            putXAxis(ChartKeysUtil.XAxisKey.LABELS.getKey(), m);
        }
    }

    public void setYAxisTitle(String title) {
        if (this.getyAxis().containsKey(ChartKeysUtil.YAxisKey.TITLE.getKey())) {
            ((Map<String, Object>) this.getyAxis().get(ChartKeysUtil.YAxisKey.TITLE.getKey())).put(ChartKeysUtil.YAxisKey.TITLE_TEXT.getKey(), title);
        } else {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(ChartKeysUtil.YAxisKey.TITLE_TEXT.getKey(), title);
            putYAxis(ChartKeysUtil.YAxisKey.TITLE.getKey(), map);
        }
    }

    public void setTooltipCrosshairs(boolean crosshairs) {
        putTooltip(ChartKeysUtil.TooltipKey.CROSSHAIRS.getKey(), crosshairs);
    }

    public void setTooltipShared(boolean shared) {
        putTooltip(ChartKeysUtil.TooltipKey.SHARED.getKey(), shared);
    }

}
