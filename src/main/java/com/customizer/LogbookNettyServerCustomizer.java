package com.customizer;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.http.netty.channel.ChannelPipelineCustomizer;
import io.micronaut.http.server.netty.NettyServerCustomizer;
import io.netty.channel.Channel;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.netty.LogbookServerHandler;

@Slf4j
@Singleton
public class LogbookNettyServerCustomizer
        implements BeanCreatedEventListener<NettyServerCustomizer.Registry> { // (1)

    @Inject
    private RequestContext requestContext;

    @Override
    public NettyServerCustomizer.Registry onCreated(
            BeanCreatedEvent<NettyServerCustomizer.Registry> event) {

        NettyServerCustomizer.Registry registry = event.getBean();
        registry.register(new Customizer(null));
        return registry;
    }

    private class Customizer implements NettyServerCustomizer {
        private final Channel channel;

        Customizer(Channel channel) {
            this.channel = channel;
        }

        @Override
        public NettyServerCustomizer specializeForChannel(Channel channel, ChannelRole role) {
            return new Customizer(channel);
        }

        @Override
        public void onStreamPipelineBuilt() {
            channel.pipeline().addBefore(
                    ChannelPipelineCustomizer.HANDLER_MICRONAUT_INBOUND,
                    "logbook",
                    new LogbookServerHandler(
                            Logbook.builder()
                                    .condition(httpRequest -> {
                                        try {
                                            return requestContext.getMagicUser();
                                        } catch (Exception ex) {
                                            log.error("LogbookServerHandler error: {}", ex.getMessage(), ex);
                                            return true;
                                        }
                                    })
                                    .build()
                    )
            );
        }
    }
}
