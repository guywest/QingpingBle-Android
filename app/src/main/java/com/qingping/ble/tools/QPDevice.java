package com.qingping.ble.tools;

public class QPDevice {
    //名称
    private String name;
    //mac地址
    private String mac;
    //是否为绑定包
    private boolean isBind;

    //productId
    private String productId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public boolean isBind() {
        return isBind;
    }

    public void setBind(boolean bind) {
        isBind = bind;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    @Override
    public String toString() {
        return "QPDevice{" +
                "name='" + name + '\'' +
                ", mac='" + mac + '\'' +
                ", isBind=" + isBind +
                ", productId='" + productId + '\'' +
                '}';
    }
}
