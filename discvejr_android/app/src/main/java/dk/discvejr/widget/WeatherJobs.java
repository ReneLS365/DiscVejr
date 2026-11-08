package dk.discvejr.widget;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

/** Periodic background updates plus immediate refresh on demand. */
public final class WeatherJobs {
    private static final int PERIODIC_ID = 4121;
    private static final int IMMEDIATE_ID = 4122;
    private WeatherJobs() {}

    public static void ensurePeriodic(Context ctx) {
        JobScheduler jobs = (JobScheduler) ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobs == null) return;
        if (jobs.getPendingJob(PERIODIC_ID) == null) {
            JobInfo job = new JobInfo.Builder(PERIODIC_ID, new ComponentName(ctx, WeatherJob.class))
                    .setPeriodic(30L * 60 * 1000)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPersisted(true)
                    .build();
            jobs.schedule(job);
        }
    }

    public static void refresh(Context ctx) {
        ensurePeriodic(ctx);
        JobScheduler jobs = (JobScheduler) ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobs == null) return;
        JobInfo job = new JobInfo.Builder(IMMEDIATE_ID, new ComponentName(ctx, WeatherJob.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setOverrideDeadline(0)
                .build();
        jobs.schedule(job);
    }
}
