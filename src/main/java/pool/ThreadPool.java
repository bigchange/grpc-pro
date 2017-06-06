package pool;

import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import task.Message;

/**
 * Created by Jerry on 2017/6/5.
 */
public class ThreadPool {

  public ThreadPool() {}

  private static ExecutorService executorService = Executors.newFixedThreadPool(20);

  private static ExecutorService executorServiceTwo = Executors.newFixedThreadPool(500);

  private static ExecutorCompletionService<Message> ecs = new ExecutorCompletionService<>(executorService);

  private static ExecutorCompletionService<Message> ecsTwo = new ExecutorCompletionService<>
  (executorServiceTwo);


  public static ExecutorService getExecutorService() {
    return executorService;
  }

  public static ExecutorCompletionService getEcs() {
    return ecs;
  }

  public static ExecutorService getExecutorServiceTwo() {
    return executorServiceTwo;
  }

  public static ExecutorCompletionService<Message> getEcsTwo() {
    return ecsTwo;
  }
}
