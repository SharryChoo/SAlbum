package com.sharry.lib.media.recorder;

import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 12/29/2018 4:40 PM
 */
public class AVSPoolExecutor extends ThreadPoolExecutor {

    private static final String TAG = AVSPoolExecutor.class.getSimpleName();
    //    Thread args
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int INIT_THREAD_COUNT = CPU_COUNT + 1;
    private static final int MAX_THREAD_COUNT = INIT_THREAD_COUNT;
    private static final long SURPLUS_THREAD_LIFE = 30L;

    private static AVSPoolExecutor sInstance;

    static {
        sInstance = new AVSPoolExecutor(
                INIT_THREAD_COUNT,
                MAX_THREAD_COUNT,
                SURPLUS_THREAD_LIFE,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(64),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(@NonNull Runnable r) {
                        Thread thread = new Thread(r, AVSPoolExecutor.class.getSimpleName());
                        thread.setDaemon(false);
                        return thread;
                    }
                }
        );
    }

    public static AVSPoolExecutor getInstance() {
        return sInstance;
    }

    private AVSPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                            BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory,
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        Log.e(TAG, "Task rejected, too many task!");
                    }
                });
    }

    /**
     * Handle exceptions when thread has completed.
     *
     * @param r the runnable that has completed
     * @param t the exception that caused termination, or null if
     */
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t == null && r instanceof Future<?>) {
            try {
                ((Future<?>) r).get();
            } catch (CancellationException ce) {
                t = ce;
            } catch (ExecutionException ee) {
                t = ee.getCause();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt(); // ignore/resetMatrix
            }
        }
        if (t != null) {
            Log.e(TAG, "Running task appeared exception! Thread [" +
                    Thread.currentThread().getName() + "], because [" + t.getMessage() + "]\n" +
                    t.getMessage());
        }
    }

}