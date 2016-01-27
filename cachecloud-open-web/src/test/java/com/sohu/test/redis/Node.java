package com.sohu.test.redis;

/**
 * Created by yijunzhang on 14-5-27.
 */
public class Node {

    private String host;

    private int port;

    /**
     * Master=0
     * Slave =1;
     */
    private int type;

    /**
     * 是否被选中 0：未选中,1 选中
     */
    private int selected = 0;

    public Node(String host, int port, int type) {
        this.host = host;
        this.port = port;
        this.type = type;
    }

    @Override
    public String toString() {
        return "Node{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", type=" + type +
                ", selected=" + selected +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;

        Node node = (Node) o;

        if (port != node.port) return false;
        if (type != node.type) return false;
        if (host != null ? !host.equals(node.host) : node.host != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        result = 31 * result + type;
        return result;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getType() {
        return type;
    }

    public boolean isSelected() {
        return selected == 1;
    }

    public void selected() {
        this.selected = 1;
    }
}
