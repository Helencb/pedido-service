package helen.com.pedidoservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "pedido.exchange";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue estoqueSolicitar() {
        return QueueBuilder.durable("estoque.solicitar")
                .withArgument("x-dead-letter-exchange", EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "estoque.falha")
                .build();
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
        return QueueBuilder.durable("email.solicitar")
                .withArgument("x-dead-letter-exchange", EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "email.falha")
                .build();
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
        return QueueBuilder.durable("nota.solicitar")
                .withArgument("x-dead-letter-exchange", EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "nota.falha")
                .build();
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
    public Binding bindEstoqueSolicitar(Queue estoqueSolicitar, TopicExchange exchange) {
        return BindingBuilder.bind(estoqueSolicitar).to(exchange).with("estoque.solicitar");
    }

    @Bean
    public Binding bindEstoqueSucesso(Queue estoqueSucesso, TopicExchange exchange) {
        return BindingBuilder.bind(estoqueSucesso).to(exchange).with("estoque.sucesso.#");
    }

    @Bean
    public Binding bindEstoqueFalha(Queue estoqueFalha, TopicExchange exchange) {
        return BindingBuilder.bind(estoqueFalha).to(exchange).with("estoque.falha.#");
    }

    @Bean
    public Binding bindEmailSolicitar(Queue emailSolicitar, TopicExchange exchange) {
        return BindingBuilder.bind(emailSolicitar).to(exchange).with("email.solicitar");
    }

    @Bean
    public Binding bindEmailSucesso(Queue emailSucesso, TopicExchange exchange) {
        return BindingBuilder.bind(emailSucesso).to(exchange).with("email.sucesso.#");
    }

    @Bean
    public Binding bindEmailFalha(Queue emailFalha, TopicExchange exchange) {
        return BindingBuilder.bind(emailFalha).to(exchange).with("email.falha.#");
    }

    @Bean
    public Binding bindNotaSolicitar(Queue notaSolicitar, TopicExchange exchange) {
        return BindingBuilder.bind(notaSolicitar).to(exchange).with("nota.solicitar");
    }

    @Bean
    public Binding bindNotaSucesso(Queue notaSucesso, TopicExchange exchange) {
        return BindingBuilder.bind(notaSucesso).to(exchange).with("nota.sucesso.#");
    }

    @Bean
    public Binding bindNotaFalha(Queue notaFalha, TopicExchange exchange) {
        return BindingBuilder.bind(notaFalha).to(exchange).with("nota.falha.#");
    }
    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}