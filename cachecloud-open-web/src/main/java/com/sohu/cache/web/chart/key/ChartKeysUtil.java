package com.sohu.cache.web.chart.key;

/**
 * Created by hym on 14-7-27.
 */
public class ChartKeysUtil {
    public enum ChartKey {
        RENDER_TO("renderTo"), TYPE("type");
        private String key;

        ChartKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public enum TitleKey {
        TEXT("text");
        private String key;

        TitleKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public enum SubTitleKey {
        TEXT("text");
        private String key;

        SubTitleKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public enum XAxisKey {
        CATEGORIES("categories"),
        LABELS("labels"),LABELS_STEP("step"),LABELS_ROTATION("rotation"), LABELS_Y("y"),
        MAX_STAGGER_LINES("maxStaggerLines");
        private String key;

        XAxisKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public enum YAxisKey {
        TITLE("title"), TITLE_TEXT("text"),
        PLOTLINES("plotLines"), PLOTLINES_VALUE("value"), PLOTLINES_WIDTH("width"), PLOTLINES_COLOR("color");
        private String key;

        YAxisKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public enum TooltipKey {
        VALUESUFFIX("valueSuffix"),CROSSHAIRS("crosshairs"),SHARED("shared");
        private String key;

        TooltipKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public enum LegendKey {
        LAYOUT("layout"), ALIGN("align"), VERTICALALIGN("verticalAlign"), BORDERWIDTH("borderWidth");
        private String key;

        LegendKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public enum SeriesKey {
        DATA("data"), NAME("name");
        private String key;

        SeriesKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

}
