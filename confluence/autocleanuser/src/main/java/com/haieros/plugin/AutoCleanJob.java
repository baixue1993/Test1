package com.haieros.plugin;

import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AutoCleanJob implements JobRunner {
    private static final Logger Log = LoggerFactory.getLogger(AutoCleanJob.class);
    @Override
    public JobRunnerResponse runJob(JobRunnerRequest jobRunnerRequest) {
       AtuoCleanUser atuoCleanUser = new AtuoCleanUser();

       if( atuoCleanUser.cleanUsers()){
           return JobRunnerResponse.success("用户清理成功");
       }
       return  JobRunnerResponse.failed("不需要清理用户");
    }
}
