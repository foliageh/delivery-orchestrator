package com.delivery.config;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import dev.snowdrop.boot.narayana.core.jms.GenericXAConnectionFactoryWrapper;
import dev.snowdrop.boot.narayana.core.properties.NarayanaProperties;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Session;
import jakarta.transaction.TransactionManager;
import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.jms.XAConnectionFactoryWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJms
@EnableTransactionManagement
public class JmsConfig {
    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    @Value("${spring.activemq.user}")
    private String username;

    @Value("${spring.activemq.password}")
    private String password;

    @Bean
    //@ConditionalOnMissingBean(XAConnectionFactoryWrapper.class)
    public XAConnectionFactoryWrapper xaConnectionFactoryWrapper(TransactionManager transactionManager, XARecoveryModule xaRecoveryModule, NarayanaProperties narayanaProperties) {
        return new GenericXAConnectionFactoryWrapper(transactionManager, xaRecoveryModule, narayanaProperties.getRecoveryJmsCredentials());
    }

    @Bean
    public ConnectionFactory connectionFactory(XAConnectionFactoryWrapper wrapper) throws Exception {
        ActiveMQXAConnectionFactory connectionFactory = new ActiveMQXAConnectionFactory();
        connectionFactory.setBrokerURL(brokerUrl);
        connectionFactory.setUserName(username);
        connectionFactory.setPassword(password);
        connectionFactory.setTrustAllPackages(true);
        return wrapper.wrapConnectionFactory(connectionFactory);
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setSessionTransacted(true);
        template.setMessageConverter(jacksonJmsMessageConverter());
        return template;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
            ConnectionFactory connectionFactory,
            PlatformTransactionManager transactionManager,
            DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setTransactionManager(transactionManager);
        factory.setSessionTransacted(true);
        factory.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
        factory.setMessageConverter(jacksonJmsMessageConverter());
        factory.setConcurrency("3-10");
        return factory;
    }
}
