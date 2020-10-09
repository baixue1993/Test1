package com.example.tutorial.plugins.schedule;
import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.example.tutorial.plugins.util.AutoCleanUsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoCleanGruopJob implements JobRunner {
    public AutoCleanGruopJob(){
    }
    Logger log = LoggerFactory.getLogger(AutoCleanGruopJob.class);

    @Override
    public JobRunnerResponse runJob(JobRunnerRequest request) {
        AutoCleanUsers autoCleanUsers = new AutoCleanUsers();
        if(autoCleanUsers.hasHandled()){
            return JobRunnerResponse.success("用户清理成功");
        }
        return  JobRunnerResponse.failed("不需要清理用户");
    }
}
