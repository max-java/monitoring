package com.tutrit.monitoring.controller;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;

@RestController
@Slf4j
public class JobController {

    @GetMapping("/job")
//    @Timed(value = "job_metric", description = "Time taken to return job metrics")
    public String jobMetrics() throws InterruptedException {
        log.info("job started ...");
        int sleepFor = ThreadLocalRandom.current().nextInt(500, 2500);
        Thread.sleep(sleepFor);
        return "Job is done in %s! ".formatted(sleepFor);
    }
}
