package com.example.ps.payu.config

import com.newrelic.api.agent.NewRelic
import mu.KLogging
import org.slf4j.MDC
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskDecorator
import org.springframework.scheduling.annotation.AsyncConfigurerSupport
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@EnableAsync(proxyTargetClass = true)
@Configuration
class AsyncConfig : AsyncConfigurerSupport() {

    override fun getAsyncExecutor(): Executor = ThreadPoolTaskExecutor().apply {
        logger.info("Configure async executor")
        this.setTaskDecorator(MdcTaskDecorator())
        this.corePoolSize = CORE_POOL_SIZE
        this.initialize()
    }

    companion object : KLogging()
}

class MdcTaskDecorator : TaskDecorator {

    override fun decorate(runnable: Runnable): Runnable {
        val contextMap = MDC.getCopyOfContextMap()
        // NewRelic transaction handling is experimental here, needs to be tested on stage
        val token = NewRelic.getAgent().transaction.token
        return Runnable {
            try {
                MDC.setContextMap(contextMap)
                token.link()
                runnable.run()
            } finally {
                token.expire()
                MDC.clear()
            }
        }
    }
}

private const val CORE_POOL_SIZE = 2
