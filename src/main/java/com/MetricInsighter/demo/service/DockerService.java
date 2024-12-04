package com.MetricInsighter.demo.service;

import com.MetricInsighter.demo.Repository.ContainerInfoRepository;
import com.MetricInsighter.demo.domain.ContainerInfoEntity;
import com.MetricInsighter.demo.dto.request.BuildImage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

@Slf4j
@Service
public class DockerService {


    @Autowired
    private ContainerInfoRepository containerInfoRepository;  // Inject the repository to access ContainerInfoEntity

    // Other methods ...


    public ResponseEntity<String> runDockerBuild(@RequestBody BuildImage request) {
        try {

            ContainerInfoEntity existingByDockerfilePath = containerInfoRepository.findByDockerfilePath(request.getDockerfilePath());
            ContainerInfoEntity existingByImageName = containerInfoRepository.findByImageName(request.getImageName());

            if (existingByDockerfilePath != null && !existingByDockerfilePath.getImageName().equals(request.getImageName())) {
                return ResponseEntity.badRequest().body("DockerfilePath is already used");
            }

            if (existingByImageName != null && !existingByImageName.getDockerfilePath().equals(request.getDockerfilePath())) {
                return ResponseEntity.badRequest().body("Image Name is already taken");
            }

            List<ContainerInfoEntity> existingContainers = containerInfoRepository.findByImageNameOrDockerfilePathOrBuildContextPath(
                    request.getImageName(), request.getDockerfilePath(), request.getBuildContextPath());

            if (!existingContainers.isEmpty() && existingContainers.get(0).isActive()) {
                return ResponseEntity.badRequest().body("The Docker image is active");
            }
            String output = executeCommand("docker", "build", "-t", request.getImageName(), "-f", request.getDockerfilePath(), request.getBuildContextPath());

            if (!output.contains("Error: Command execution failed with exit code")) {
                boolean isNewDeployment = isDuplicateBuild(request.getImageName(), request.getDockerfilePath(), request.getBuildContextPath());

                // Save the new image details into the database
                saveContainerInfo(request.getImageName(), request.getDockerfilePath(), request.getBuildContextPath(), isNewDeployment);

                return ResponseEntity.ok("Docker build executed successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Docker build failed: " + output);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    public String runDockerContainer(String containerName, String imageName, int port) {
        // Set container to active
        ContainerInfoEntity container = getContainerInfoByImageName(imageName);
        if (container != null) {
            container.setContainerName(containerName);
            container.setActive(true);
            containerInfoRepository.save(container);
        }

        log.info("Running container with name: {} and image: {} on port: {}", containerName, imageName, port);
        return executeCommand("docker", "run", "-d", "-p", port + ":" + port, "--name", containerName, imageName);
    }

    public String stopDockerContainer(String containerName) {
        // Set container to inactive
        ContainerInfoEntity container = getContainerInfoByContainerName(containerName);
        if (container != null) {
            container.setActive(false);
            containerInfoRepository.save(container);
        }

        log.info("Stopping container: {}", containerName);
        return executeCommand("docker", "stop", containerName);
    }

    public String removeDockerContainer(String containerName) {
        log.info("Removing container: {}", containerName);
        return executeCommand("docker", "rm", containerName);
    }

    public String removeDockerImage(String imageName) {
        log.info("Removing image: {}", imageName);
        return executeCommand("docker", "rmi", imageName);
    }

    // Helper method to check for duplicate build
    private boolean isDuplicateBuild(String imageName, String dockerfilePath, String buildContext) {
        List<ContainerInfoEntity> existingContainers = containerInfoRepository.findByImageNameOrDockerfilePathOrBuildContextPath(imageName, dockerfilePath, buildContext);
        return !existingContainers.isEmpty();  // Return true if duplicates exist
    }

    private void saveContainerInfo(String imageName, String dockerfilePath, String buildContext,Boolean isNewDeployment) {
        ContainerInfoEntity containerInfo = null;
        containerInfo = containerInfoRepository.findByImageName(imageName);
        if(Boolean.TRUE.equals(isNewDeployment)){
            containerInfo.setDeploymentCount(containerInfo.getDeploymentCount() + 1);
            containerInfo.setActive(false);
        }else{
            containerInfo = new ContainerInfoEntity();
            containerInfo.setImageName(imageName);
            containerInfo.setDockerfilePath(dockerfilePath);
            containerInfo.setBuildContextPath(buildContext);
            containerInfo.setDeploymentCount(1);  // New deployment count starts at 1
            containerInfo.setActive(false);
        }
        containerInfoRepository.save(containerInfo);
    }

    private ContainerInfoEntity getContainerInfoByImageName(String imageName) {
        return containerInfoRepository.findByImageName(imageName);  // Get container info by imageName
    }

    private ContainerInfoEntity getContainerInfoByContainerName(String containerName) {
        return containerInfoRepository.findByContainerName(containerName);  // Get container info by container name
    }

    private String executeCommand(String... command) {
        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            // Logging the complete command for debugging
            log.info("Executing command: {}", String.join(" ", command));

            // Start the process and capture the output
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Wait for the process to finish
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                output.append("Error: Command execution failed with exit code ").append(exitCode);
            }
        } catch (Exception e) {
            log.error("Error executing Docker command", e);
            output.append("Error executing command: ").append(e.getMessage());
        }
        return output.toString();
    }
}
