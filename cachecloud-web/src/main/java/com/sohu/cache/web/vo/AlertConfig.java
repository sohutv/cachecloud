package com.sohu.cache.web.vo;

public class AlertConfig {

    private String value;

    private String info;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
            this.info = info;
        }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        result = prime * result + ((info == null) ? 0 : info.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AlertConfig other = (AlertConfig) obj;
        if (value == null) {
            if (other.value != null){
                return false;
            }
        } else if (!value.equals(other.value)){
            return false;
        }
        if (info == null) {
            if (other.info != null){
                return false;
            }
        } else if (!info.equals(other.info)){
            return false;
        }
        return true;
    }
}