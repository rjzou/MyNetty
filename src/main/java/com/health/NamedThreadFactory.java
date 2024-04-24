package com.health;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
  private final String prefixName;
  private final ThreadGroup threadGroup;
  private final boolean demon;
  private final AtomicInteger threadNumber = new AtomicInteger(1);


  public NamedThreadFactory(String prefixName) {
    this(prefixName,false);
  }

  @SuppressWarnings("removal")
  public NamedThreadFactory(String prefixName, boolean demon) {
    this.prefixName = prefixName;
    this.demon = demon;
    SecurityManager securityManager = System.getSecurityManager();
    this.threadGroup = securityManager != null ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
  }

  @Override
  public Thread newThread(@NotNull Runnable r) {
    Thread t = new Thread(threadGroup, r,prefixName+"-thread-"+threadNumber.incrementAndGet(), 0);
    t.setDaemon(demon);
    return t;
  }
}
