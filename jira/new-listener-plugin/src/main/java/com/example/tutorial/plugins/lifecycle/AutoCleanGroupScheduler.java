package com.example.tutorial.plugins.lifecycle;

import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.config.Schedule;
import com.example.tutorial.plugins.schedule.AutoCleanGruopJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import java.util.Date;

public class AutoCleanGroupScheduler implements LifecycleAware {
    private static final Logger log = LoggerFactory.getLogger(AutoCleanGroupScheduler.class);
    private static final String JOB_KEY_STRING = AutoCleanGroupScheduler.class.getName();
    private static final JobId JOB_ID;
    private static final JobRunnerKey JOB_RUNNER_KEY;
    private static final String OLD_JOB_KEY_STRING = "com.example.tutorial.plugins.lifecycle.AutoCleanGroupScheduler";
    private static final JobId OLD_JOB_ID;
    private static final JobRunnerKey OLD_JOB_RUNNER_KEY;
    private static final long SCHEDULER_START_DELAY = 86400000L;
    private static final long SCHEDULER_INTERVAL = 604800000L;
    private final SchedulerService schedulerService;

    public AutoCleanGroupScheduler(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    private JobConfig getJobConfig(JobRunnerKey jobRunnerKey) {
        return JobConfig.forJobRunnerKey(jobRunnerKey).withSchedule(Schedule.forCronExpression("0 0 3 ? * *"));
    }

    private void unschedule(JobId jobId, JobRunnerKey jobRunnerKey) {
        this.schedulerService.unscheduleJob(jobId);
        this.schedulerService.unregisterJobRunner(jobRunnerKey);
        log.warn("注销插件: ", jobId.toString());
    }

    /**
     * 当插件开始运行的时候
     */
    public void onStart() {
        log.warn("注册插件");
        this.unschedule(OLD_JOB_ID, OLD_JOB_RUNNER_KEY);
        this.schedulerService.registerJobRunner(JOB_RUNNER_KEY, new AutoCleanGruopJob());
        try {
            this.schedulerService.scheduleJob(JOB_ID, this.getJobConfig(JOB_RUNNER_KEY));
            log.warn("AutoCleanGruopJob 成功调度");
        } catch (SchedulerServiceException var2) {
            log.error("AutoCleanGruopJob 调度出错", var2);
        }

    }

    public void onStop() {
        this.unschedule(JOB_ID, JOB_RUNNER_KEY);
    }

    static {
        JOB_ID = JobId.of(JOB_KEY_STRING);
        JOB_RUNNER_KEY = JobRunnerKey.of(JOB_KEY_STRING);
        OLD_JOB_ID = JobId.of("com.example.tutorial.plugins.lifecycle.AutoCleanGroupScheduler");
        OLD_JOB_RUNNER_KEY = JobRunnerKey.of("com.example.tutorial.plugins.lifecycle.AutoCleanGroupScheduler");
    }
}
