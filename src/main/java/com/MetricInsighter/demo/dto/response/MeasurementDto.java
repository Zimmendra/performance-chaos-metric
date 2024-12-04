package com.MetricInsighter.demo.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class MeasurementDto implements Cloneable {

    private String metric;
    private List<MeasureDetailDto> measureDetailDto;

    @Override
    public MeasurementDto clone() {
        try {
            MeasurementDto cloned = (MeasurementDto) super.clone();
            if (this.measureDetailDto != null) {
                cloned.setMeasureDetailDto(
                        this.measureDetailDto.stream()
                                .map(MeasureDetailDto::clone)
                                .collect(Collectors.toList())
                );
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // This should never happen since we implement Cloneable
        }
    }
}