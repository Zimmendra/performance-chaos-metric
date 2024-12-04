package com.MetricInsighter.demo.dto.response;

import lombok.Data;

@Data
public class MeasureDetailDto implements Cloneable {

    private Integer deploymentNumber;
    private Double measurement;

    @Override
    public MeasureDetailDto clone() {
        try {
            return (MeasureDetailDto) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // This should never happen since we implement Cloneable
        }
    }
}
