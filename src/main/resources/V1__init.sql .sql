CREATE TABLE item_pedido
(
    id         BINARY(16)   NOT NULL,
    produto_id BINARY(16)   NOT NULL,
    nome       VARCHAR(120) NOT NULL,
    quantidade INT          NOT NULL,
    pedido_id  BINARY(16)   NOT NULL,
    CONSTRAINT pk_itempedido PRIMARY KEY (id)
);

CREATE TABLE outbox_event
(
    id             BINARY(16)    NOT NULL,
    aggregate_id   BINARY(16)    NOT NULL,
    event_type     VARCHAR(255) NOT NULL,
    routing_key    VARCHAR(120) NOT NULL,
    payload        TEXT         NOT NULL,
    correlation_id VARCHAR(120) NOT NULL,
    trace_id       VARCHAR(120) NOT NULL,
    status         VARCHAR(30)  NOT NULL,
    attempts       INT          NOT NULL,
    created_at     datetime     NOT NULL,
    published_at   datetime NULL,
    last_error     VARCHAR(1000) NULL,
    CONSTRAINT pk_outbox_event PRIMARY KEY (id)
);

CREATE TABLE pedidos
(
    id         BINARY(16)  NOT NULL,
    cliente_id BINARY(16)  NOT NULL,
    ativo      BIT(1)      NOT NULL,
    status     VARCHAR(40) NOT NULL,
    CONSTRAINT pk_pedidos PRIMARY KEY (id)
);

CREATE TABLE processed_event
(
    event_id     BINARY(16)   NOT NULL,
    consumer     VARCHAR(120) NOT NULL,
    processed_at datetime     NOT NULL,
    CONSTRAINT pk_processed_event PRIMARY KEY (event_id, consumer)
);

ALTER TABLE item_pedido
    ADD CONSTRAINT FK_ITEMPEDIDO_ON_PEDIDO FOREIGN KEY (pedido_id) REFERENCES pedidos (id);