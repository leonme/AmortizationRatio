package com.sap.mii.custom.action;

import java.math.BigDecimal;

/**
 * Created by Leon on 17/5/10.
 */
public class RNode {
    private String name;
    private BigDecimal meterValue;
    private BigDecimal ratio;
    private BigDecimal calcuValue;
    private BigDecimal lineLoss;
    private RNode childNode;

    public RNode(String name, BigDecimal meterValue) {
        this.name = name;
        this.meterValue = meterValue;
    }

    public RNode(String name) {
        this.name = name;
    }

    public RNode() {

    }

    public RNode(String name, BigDecimal meterValue, BigDecimal ratio, BigDecimal calcuValue, BigDecimal lineLoss) {

        this.name = name;
        this.meterValue = meterValue;
        this.ratio = ratio;
        this.calcuValue = calcuValue;
        this.lineLoss = lineLoss;
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getMeterValue() {
        return meterValue;
    }

    public void setMeterValue(BigDecimal meterValue) {
        this.meterValue = meterValue;
    }

    public BigDecimal getRatio() {
        return ratio;
    }

    public void setRatio(BigDecimal ratio) {
        this.ratio = ratio;
    }

    public BigDecimal getCalcuValue() {
        return calcuValue;
    }

    public void setCalcuValue(BigDecimal calcuValue) {
        this.calcuValue = calcuValue;
    }

    public BigDecimal getLineLoss() {
        return lineLoss;
    }

    public void setLineLoss(BigDecimal lineLoss) {
        this.lineLoss = lineLoss;
    }

    public RNode getChildNode() {
        return childNode;
    }

    public void setChildNode(RNode childNode) {
        this.childNode = childNode;
    }
}
