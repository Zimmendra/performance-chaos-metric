
package com.microservice.resiliency.analyser.service.model;
import jakarta.persistence.*;
        import lombok.*;

        import java.time.LocalDateTime;

@Entity
@Table(name = "service_metrics")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serviceName;
    private String deploymentId;

    private int totalRequests;
    private int successfulRequests;
    private int failedRequests;
    private double avgResponseTime;
    private LocalDateTime timestamp;

}
