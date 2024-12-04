package com.MetricInsighter.demo.service;

import com.MetricInsighter.demo.Repository.ContainerInfoRepository;
import com.MetricInsighter.demo.Repository.ContainerStatsRepository;
import com.MetricInsighter.demo.domain.ContainerStatsEntity;
import com.MetricInsighter.demo.dto.response.MeasureDetailDto;
import com.MetricInsighter.demo.dto.response.MeasurementDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceMetricService {


      private final ContainerStatsRepository containerStatsRepository;

      private final ContainerInfoRepository containerInfoRepository;

      public ResponseEntity<?> getPerformanceMetricByApplicationName(String applicationName){
          List<ContainerStatsEntity> allByOrderByNameAscIdAsc = containerStatsRepository.findAllByNameOrderByNameAscIdAsc(applicationName);
          Map<Integer, List<ContainerStatsEntity>> map = new HashMap<>();
          for(ContainerStatsEntity containerStatsEntity: allByOrderByNameAscIdAsc){
              if(map.get(containerStatsEntity.getNoOfDeployment())==null){
                  List<ContainerStatsEntity> containerStatsEntities = new ArrayList<>();
                  containerStatsEntities.add(containerStatsEntity);
                  map.put(containerStatsEntity.getNoOfDeployment(),containerStatsEntities);
              }else{
                  List<ContainerStatsEntity> containerStatsEntities = map.get(containerStatsEntity.getNoOfDeployment());
                  containerStatsEntities.add(containerStatsEntity);
              }
          }
          List<MeasurementDto> meanCPUusage = getMeanCPUusage(map);
          return ResponseEntity.ok(meanCPUusage);
      }

      private List<MeasurementDto> getMeanCPUusage(Map<Integer, List<ContainerStatsEntity>> dataMap){
          List<MeasurementDto> metricList = new ArrayList<>();
          MeasurementDto meanCpuUsage = new MeasurementDto();
          List<MeasureDetailDto> meanCpuUsageList = new ArrayList<>();

          MeasurementDto meanMemoryUsage = meanCpuUsage.clone();
          List<MeasureDetailDto> meanMemoryList = new ArrayList<>();

          MeasurementDto percentageMeasurementDto = meanCpuUsage.clone();
          List<MeasureDetailDto> meanPercentageList = new ArrayList<>();

          meanMemoryUsage.setMetric("Mean Memory usage");
          meanCpuUsage.setMetric("Mean CPU usage");
          percentageMeasurementDto.setMetric("Mean Memory Usage Percentage");

          for (Map.Entry<Integer, List<ContainerStatsEntity>> entry : dataMap.entrySet()) {


              double memoryUsage = 0.00;
              int averageCount = 0;
              double cpuUsage = 0.00;
              double percentageOfCPUusge = 0.00;

              MeasureDetailDto memoryMeasurementDetailsDto = new MeasureDetailDto();
              MeasureDetailDto cpuMeasurementDetailsDto = memoryMeasurementDetailsDto.clone();
              MeasureDetailDto percentageMeasurementDetailsDto = memoryMeasurementDetailsDto.clone();

              for (ContainerStatsEntity stats :entry.getValue()) {

                  memoryUsage = memoryUsage +  Double.parseDouble(stats.getMemoryUsage().substring(0, stats.getMemoryUsage().length() - 3));
                  cpuUsage = cpuUsage + Double.parseDouble(stats.getMemoryPercentage().substring(0, stats.getMemoryPercentage().length() - 1));
                  percentageOfCPUusge = percentageOfCPUusge + Double.parseDouble(stats.getCpuUsage().substring(0, stats.getCpuUsage().length() - 1));

                  averageCount++;
              }


              memoryMeasurementDetailsDto.setMeasurement(memoryUsage/averageCount);
              memoryMeasurementDetailsDto.setDeploymentNumber(entry.getKey());
              meanMemoryList.add(memoryMeasurementDetailsDto);
              meanMemoryUsage.setMeasureDetailDto(meanMemoryList);


              cpuMeasurementDetailsDto.setMeasurement(memoryUsage/averageCount);
              cpuMeasurementDetailsDto.setDeploymentNumber(entry.getKey());
              meanCpuUsageList.add(cpuMeasurementDetailsDto);
              meanCpuUsage.setMeasureDetailDto(meanCpuUsageList);

              percentageMeasurementDetailsDto.setDeploymentNumber(entry.getKey());
              percentageMeasurementDetailsDto.setMeasurement(percentageOfCPUusge/averageCount);
              meanPercentageList.add(percentageMeasurementDetailsDto);
              percentageMeasurementDto.setMeasureDetailDto(meanPercentageList);

          }
          metricList.add(meanCpuUsage);
          metricList.add(meanMemoryUsage);
          metricList.add(percentageMeasurementDto);
          return metricList;

      }

      public Object getApplication(){
          return containerInfoRepository.findAll();
      }


}
