package com.tutrit.monitoring.metric;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Aspect
@Component
public class ExecutionTimeAspect {

    private final Counter jobCounter;
    private final AtomicLong currentExecutionTime;
//    private final Gauge jobGauge;
//    private final DistributionSummary distributionJobHistogram;
//    private final DistributionSummary distributionJobSummary;
    private final Timer jobTimer;
    private final FunctionTimer jobFunctionTimer;
    private final FunctionCounter jobFunctionCounter;
    private final AtomicLong jobExecutionTime;
    private final AtomicLong jobCount;
    private final ObjectMapper objectMapper;

    public ExecutionTimeAspect(MeterRegistry meterRegistry, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.jobExecutionTime = new AtomicLong(0);
        this.jobCount = new AtomicLong(0);
        this.currentExecutionTime = new AtomicLong(0);

        // Counter for job execution count
        this.jobCounter = meterRegistry.counter("@mikas.job.execution.count", "endpoint", "/job");

        // Gauge for current execution time
//        this.jobGauge = Gauge.builder("@mikas.job.execution.time.current", currentExecutionTime, AtomicLong::doubleValue)
//                .description("Current execution time of job")
//                .register(meterRegistry);
//
//        // Histogram for distribution of execution times
//        this.distributionJobHistogram = DistributionSummary.builder("@mikas.job.execution.time.histogram")
//                .description("Distribution of job execution times")
//                .register(meterRegistry);
//
//        // Summary for recording execution times with percentiles
//        this.distributionJobSummary = DistributionSummary.builder("@mikas.job.execution.time.summary")
//                .description("Distribution summary of job execution times")
//                .percentilePrecision(2)
//                .register(meterRegistry);

//         Timer for measuring execution time
        this.jobTimer = Timer.builder("@mikas.job.execution.time")
                .description("Time taken to execute job")
                .register(meterRegistry);

        // FunctionTimer for total time spent on jobs and total job count
        this.jobFunctionTimer = FunctionTimer.builder("@mikas.job.function.timer", this,
                        ExecutionTimeAspect::getJobCount, ExecutionTimeAspect::getJobExecutionTime, TimeUnit.NANOSECONDS)
                .description("Total time spent on jobs and total job count")
                .register(meterRegistry);

        // FunctionCounter for counting job executions
        this.jobFunctionCounter = FunctionCounter.builder("@mikas.job.function.counter", jobCount,
                        AtomicLong::doubleValue)
                .description("Total job executions counted")
                .register(meterRegistry);
    }

    @Pointcut("execution(* com.tutrit.monitoring.controller.JobController.jobMetrics(..))")
    public void monitorJob() {}

    @Around("monitorJob()")
    public Object measureExecutionTimeAndRecordIt(ProceedingJoinPoint pjp) throws Throwable {
//        jobCounter.increment(); // Increment job execution count

        long start = System.nanoTime();
        try {
            Object result = jobTimer.recordCallable(() -> {
                try {
                    return pjp.proceed();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
            long duration = System.nanoTime() - start;
            double durationSeconds = duration / 1_000_000_000.0;

//            currentExecutionTime.set(duration); // Update gauge with current execution time
//            distributionJobHistogram.record(durationSeconds); // Record duration in histogram
//            distributionJobSummary.record(durationSeconds); // Record duration in summary
//            jobExecutionTime.addAndGet(duration); // Update total execution time
//            jobCount.incrementAndGet(); // Increment job execution count for FunctionTimer

            return result;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Around("monitorJob()")
    public Object measureExecutionTimeAndLogIt(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.nanoTime();
        try {
            return pjp.proceed();
        } finally {
            long duration = System.nanoTime() - start;
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("job", "execution_time");
            logMap.put("duration_sec", duration / 1_000_000_000.0);

            try {
                String logJson = objectMapper.writeValueAsString(logMap);
                log.info(logJson);
            } catch (JsonProcessingException e) {
                log.error("Error processing JSON", e);
            }
        }
    }

    private static long getJobCount(ExecutionTimeAspect aspect) {
        return aspect.jobCount.get();
    }

    private static double getJobExecutionTime(ExecutionTimeAspect aspect) {
        return aspect.jobExecutionTime.get();
    }

}
