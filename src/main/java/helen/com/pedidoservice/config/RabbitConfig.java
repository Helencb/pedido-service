package helen.com.pedidoservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "pedido.exchange";
    public static final String RETRY_EXCHANGE = "pedido.retry.exchange";
    public static final String DLQ_EXCHANGE = "pedido.dlq.exchange";

    @Value("${app.messaging.retry.ttl-ms:30000}")
    private int retryTtlMs;

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public TopicExchange retryExchange() {
        return new TopicExchange(RETRY_EXCHANGE);
    }

    @Bean
    public TopicExchange dlqExchange() {
        return new TopicExchange(DLQ_EXCHANGE);
    }

    @Bean
    public Queue estoqueSolicitar() {
        return buildRequestQueue("estoque.solicitar", "estoque.solicitar.retry");
    }

    @Bean
    public Queue estoqueSolicitarRetry() {
        return buildRetryQueue("estoque.solicitar.retry", "estoque.solicitar");
    }

    @Bean
    public Queue estoqueSolicitarDlq() {
        return QueueBuilder.durable("estoque.solicitar.dlq").build();
    }

    @Bean
    public Queue estoqueSucesso() {
        return QueueBuilder.durable("estoque.sucesso").build();
    }

    @Bean
    public Queue estoqueFalha() {
        return QueueBuilder.durable("estoque.falha").build();
    }

    @Bean
    public Queue emailSolicitar() {
        return buildRequestQueue("email.solicitar", "email.solicitar.retry");
    }

    @Bean
    public Queue emailSolicitarRetry() {
        return buildRetryQueue("email.solicitar.retry", "email.solicitar");
    }

    @Bean
    public Queue emailSolicitarDlq() {
        return QueueBuilder.durable("email.solicitar.dlq").build();
    }

    @Bean
    public Queue emailSucesso() {
        return QueueBuilder.durable("email.sucesso").build();
    }

    @Bean
    public Queue emailFalha() {
        return QueueBuilder.durable("email.falha").build();
    }

    @Bean
    public Queue notaSolicitar() {
        return buildRequestQueue("nota.solicitar", "nota.solicitar.retry");
    }

    @Bean
    public Queue notaSolicitarRetry() {
        return buildRetryQueue("nota.solicitar.retry", "nota.solicitar");
    }

    @Bean
    public Queue notaSolicitarDlq() {
        return QueueBuilder.durable("nota.solicitar.dlq").build();
    }

    @Bean
    public Queue notaSucesso() {
        return QueueBuilder.durable("nota.sucesso").build();
    }

    @Bean
    public Queue notaFalha() {
        return QueueBuilder.durable("nota.falha").build();
    }

    @Bean
    public Binding bindEstoqueSolicitar() {
        return BindingBuilder.bind(estoqueSolicitar()).to(exchange()).with("estoque.solicitar");
    }

    @Bean
    public Binding bindEstoqueSolicitarRetry() {
        return BindingBuilder.bind(estoqueSolicitarRetry()).to(retryExchange()).with("estoque.solicitar.retry");
    }

    @Bean
    public Binding bindEstoqueSolicitarDlq() {
        return BindingBuilder.bind(estoqueSolicitarDlq()).to(dlqExchange()).with("estoque.solicitar.dlq");
    }

    @Bean
    public Binding bindEstoqueSucesso() {
        return BindingBuilder.bind(estoqueSucesso()).to(exchange()).with("estoque.sucesso");
    }

    @Bean
    public Binding bindEstoqueFalha() {
        return BindingBuilder.bind(estoqueFalha()).to(exchange()).with("estoque.falha");
    }

    @Bean
    public Binding bindEmailSolicitar() {
        return BindingBuilder.bind(emailSolicitar()).to(exchange()).with("email.solicitar");
    }

    @Bean
    public Binding bindEmailSolicitarRetry() {
        return BindingBuilder.bind(emailSolicitarRetry()).to(retryExchange()).with("email.solicitar.retry");
    }

    @Bean
    public Binding bindEmailSolicitarDlq() {
        return BindingBuilder.bind(emailSolicitarDlq()).to(dlqExchange()).with("email.solicitar.dlq");
    }

    @Bean
    public Binding bindEmailSucesso() {
        return BindingBuilder.bind(emailSucesso()).to(exchange()).with("email.sucesso");
    }

    @Bean
    public Binding bindEmailFalha() {
        return BindingBuilder.bind(emailFalha()).to(exchange()).with("email.falha");
    }

    @Bean
    public Binding bindNotaSolicitar() {
        return BindingBuilder.bind(notaSolicitar()).to(exchange()).with("nota.solicitar");
    }

    @Bean
    public Binding bindNotaSolicitarRetry() {
        return BindingBuilder.bind(notaSolicitarRetry()).to(retryExchange()).with("nota.solicitar.retry");
    }

    @Bean
    public Binding bindNotaSolicitarDlq() {
        return BindingBuilder.bind(notaSolicitarDlq()).to(dlqExchange()).with("nota.solicitar.dlq");
    }

    @Bean
    public Binding bindNotaSucesso() {
        return BindingBuilder.bind(notaSucesso()).to(exchange()).with("nota.sucesso");
    }

    @Bean
    public Binding bindNotaFalha() {
        return BindingBuilder.bind(notaFalha()).to(exchange()).with("nota.falha");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }

    private Queue buildRequestQueue(String queueName, String retryRoutingKey) {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", RETRY_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", retryRoutingKey)
                .build();
    }

    private Queue buildRetryQueue(String queueName, String originalRoutingKey) {
        return QueueBuilder.durable(queueName)
                .withArgument("x-message-ttl", retryTtlMs)
                .withArgument("x-dead-letter-exchange", EXCHANGE)
                .withArgument("x-dead-letter-routing-key", originalRoutingKey)
                .withArgument("x-overflow", "reject-publish")
                .build();
    }
   }