package io.axoniq.labs.chat;

import com.rabbitmq.client.Channel;
import com.zaxxer.hikari.HikariDataSource;
import org.axonframework.amqp.eventhandling.spring.SpringAMQPMessageSource;
import org.axonframework.boot.DistributedCommandBusProperties;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.distributed.RoutingStrategy;
import org.axonframework.serialization.Serializer;
import org.axonframework.springcloud.commandhandling.SpringCloudHttpBackupCommandRouter;
import org.axonframework.springcloud.commandhandling.SpringHttpCommandBusConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import test.TestControllerImpl;
import test.TestControllerInterface;

import javax.sql.DataSource;
import java.sql.SQLException;

@SpringBootApplication
@EnableDiscoveryClient
public class ChatScalingOutApplication {

    private static final Logger logger = LoggerFactory.getLogger(ChatScalingOutApplication.class);

    @Autowired
    private DistributedCommandBusProperties properties;

    public static void main(String[] args) throws SQLException {
        SpringApplication.run(ChatScalingOutApplication.class, args);
    }

    @Autowired
    public PlatformTransactionManager platformTransactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource dataSource() {
        return new HikariDataSource();
    }


    @Bean
    public TestControllerInterface instance_will_not_be_treated_as_controller() {
        return new TestControllerImpl();
    }

    //    @Bean
    public TestControllerImpl instance_will_be_treated_as_controller() {
        return new TestControllerImpl();
    }


//    @Configuration
//    public static class AmqpConfiguration {

    @Bean
    public Exchange eventsExchange() {
        return ExchangeBuilder.fanoutExchange("events").build();
    }

    @Bean
    public Queue participantsEventsQueue() {
        return QueueBuilder.durable("participant-events").build();
    }

    @Bean
    public Binding participantsEventsBinding() {
        return BindingBuilder.bind(participantsEventsQueue()).to(eventsExchange()).with("*").noargs();
    }

    @Autowired
    public void configure(AmqpAdmin admin) {
        admin.declareExchange(eventsExchange());
        admin.declareQueue(participantsEventsQueue());
        admin.declareBinding(participantsEventsBinding());
    }

    @Bean("participantEvents")
    public SpringAMQPMessageSource participantsEvents(Serializer serializer) {
        return new SpringAMQPMessageSource(serializer) {
            @RabbitListener(queues = "participant-events", exclusive = true)
            @Override
            public void onMessage(Message message, Channel channel) throws Exception {
                super.onMessage(message, channel);
            }
        };
    }
//    }

    @Bean
    public SpringHttpCommandBusConnector springHttpCommandBusConnector2(@Qualifier("localSegment") CommandBus localSegment,
                                                                        RestTemplate restTemplate,
                                                                        @Qualifier("messageSerializer") Serializer serializer) {
        return new SpringHttpCommandBusConnector(localSegment, restTemplate, serializer);
    }

    @Bean
    @Primary
    public SpringCloudHttpBackupCommandRouter springCloudHttpBackupCommandRouter(DiscoveryClient discoveryClient,
                                                                                 RestTemplate restTemplate,
                                                                                 RoutingStrategy routingStrategy) {
        return new SpringCloudHttpBackupCommandRouter(discoveryClient,
                routingStrategy,
                restTemplate,
                properties.getSpringCloud().getFallbackUrl());
    }

    @Configuration
    @EnableSwagger2
    public static class SwaggerConfig {
        @Bean
        public Docket api() {
            return new Docket(DocumentationType.SWAGGER_2)
                    .select()
                    .apis(RequestHandlerSelectors.any())
                    .paths(PathSelectors.any())
                    .build();
        }
    }

//    // Example function providing a Spring Cloud Connector
//    @Bean
//    public CommandRouter springCloudCommandRouter(DiscoveryClient discoveryClient) {
//        return new SpringCloudCommandRouter(discoveryClient, new AnnotationRoutingStrategy());
//    }
//
//    @Bean
//    public CommandBusConnector springHttpCommandBusConnector(@Qualifier("localSegment") CommandBus localSegment,
//                                                             Serializer serializer) {
//        return new SpringHttpCommandBusConnector(localSegment, new RestTemplate(), serializer);
//    }
//
//    @Primary // to make sure this CommandBus implementation is used for autowiring
//    @Bean
//    public DistributedCommandBus springCloudDistributedCommandBus(CommandRouter commandRouter,
//                                                                  CommandBusConnector commandBusConnector) {
//        return new DistributedCommandBus(commandRouter, commandBusConnector);
//    }

}
