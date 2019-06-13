package org.javamaster.b2c.test.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author yudong
 * @date 2019/6/12
 */
public class Apple {
    private String color;
    private int weight;
    private boolean good;

    public Apple() {
    }

    public Apple(String color, int weight) {
        this.color = color;
        this.weight = weight;
    }

    public Apple(String color, int weight, boolean good) {
        this.color = color;
        this.weight = weight;
        this.good = good;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean isGood() {
        return good;
    }

    public void setGood(boolean good) {
        this.good = good;
    }
}