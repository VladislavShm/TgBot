package com.tgbot.config;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

import static java.lang.System.getenv;

@Configuration
public class AmqpConfiguration {

    @Bean
    @SneakyThrows
    public ConnectionFactory connectionFactory(RabbitProperties rabbitProperties) {
        final CachingConnectionFactory factory = new CachingConnectionFactory();

        String uri = getenv("CLOUDAMQP_URL");
        if (StringUtils.isNotEmpty(uri)) {
            final URI ampqUrl = new URI(uri);

            factory.setUsername(ampqUrl.getUserInfo().split(":")[0]);
            factory.setPassword(ampqUrl.getUserInfo().split(":")[1]);
            factory.setHost(ampqUrl.getHost());
            factory.setPort(ampqUrl.getPort());
            factory.setVirtualHost(ampqUrl.getPath().substring(1));
            factory.getRabbitConnectionFactory().setUri(ampqUrl);
        } else {
            factory.setUsername(rabbitProperties.determineUsername());
            factory.setPassword(rabbitProperties.determinePassword());
            factory.setHost(rabbitProperties.determineHost());
            factory.setPort(rabbitProperties.determinePort());
        }

        return factory;
    }

    @Bean
    public Queue nftOwnerChangedQueue() {
        return new Queue("notification.nft-owner-changed");
    }
}
