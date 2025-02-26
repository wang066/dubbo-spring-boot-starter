package com.alibaba.dubbo.spring.boot;

import com.alibaba.dubbo.spring.boot.health.DubboHealthIndicator;
import com.alibaba.dubbo.spring.boot.server.DubboServer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CountDownLatch;

/**
 * Dubbo configuration
 * 启动了一个非守护线程，用来hold住dubbo服务。类似我常做的System.in.read。
 * @author xionghui
 * @version 2.0.0
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(DubboProperties.class)
public class DubboAutoConfiguration {

  /**
   * Start a non-daemon thread
   *
   * @return DubboServer
   */
  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(prefix = "spring.dubbo", name = "server", havingValue = "true")
  public DubboServer dubboServer() {
    final DubboServer dubboServer = new DubboServer();
    final CountDownLatch latch = new CountDownLatch(1);
    Thread awaitThread = new Thread("dubboServer") {

      @Override
      public void run() {
        latch.countDown();
        dubboServer.await();
      }
    };
    awaitThread.setContextClassLoader(this.getClass().getClassLoader());
    awaitThread.setDaemon(false);
    awaitThread.start();
    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }

    return dubboServer;
  }

  @Bean
  public DubboHealthIndicator dubboHealthIndicator() {
    return new DubboHealthIndicator();
  }
}
