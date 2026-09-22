# 🌱 Pedido Service - Natura Garden

Microsserviço responsável pelo gerenciamento de pedidos da aplicação **Natura Garden**, um sistema de vendas de plantas baseado em arquitetura de microsserviços com comunicação assíncrona utilizando RabbitMQ.

---

## 📌 Sobre o Projeto

O **Pedido Service** é o serviço central da aplicação, responsável por:

- Criar pedidos
- Iniciar o fluxo de processamento (Saga)
- Orquestrar comunicação com outros microsserviços:
  - Estoque Service
  - Nota Fiscal Service
  - Email Service

---

## 🏗️ Arquitetura

Este projeto segue o padrão de **Microsserviços com comunicação assíncrona** utilizando **RabbitMQ**.

### 🔄 Fluxo da Saga

1. Pedido é criado
2. Evento `PedidoCriadoEvent` é enviado
3. Estoque Service processa:
   - ✔ Sucesso → envia `EstoqueConfirmadoEvent`
   - ❌ Falha → envia `EstoqueFalhouEvent`
4. Se sucesso:
   - Pedido Service solicita emissão de nota
5. Nota Service:
   - ✔ Sucesso → envia `NotaEmitidaEvent`
   - ❌ Falha → compensação
6. Email Service:
   - Envia confirmação ao cliente

---

## 🔗 Integração síncrona com o produto-service

Antes de criar um pedido, o `pedido-service` valida cada item chamando `GET /produtos/{id}` no [produto-service](https://github.com/Helencb/produto-service) via **OpenFeign**, confirmando que o produto existe e está ativo — antes disso, o serviço confiava cegamente no `produtoId`/`nome` enviados pelo cliente, sem checar se o produto era real.

```java
@FeignClient(name = "PRODUCT-SERVICE")
public interface ProdutoClient {
    @GetMapping("/produtos/{id}")
    ApiResponse<ProdutoDTO> buscarPorId(@PathVariable("id") UUID id);
}
```

A resolução de `PRODUCT-SERVICE` é feita via **Eureka** (Spring Cloud LoadBalancer), não por URL fixa — por isso o `pedido-service` também se registra no [Eureka Server](https://github.com/Helencb/eureka-server) como `ORDER-SERVICE` (nome exigido pela rota `/api/orders/**` do [api_gateway](https://github.com/Helencb/api_gateway)):

```properties
eureka.client.service-url.defaultZone=${EUREKA_URL:http://admin:123456@localhost:8761/eureka/}
eureka.instance.appname=ORDER-SERVICE
```

**Efeito colateral:** criar um pedido agora depende do `produto-service` estar no ar. Se ele cair ou o produto não existir/estiver inativo, a criação falha com `409` antes mesmo de persistir o pedido — não existe fallback/circuit breaker configurado ainda.

> Não inclui validação de preço: `ItemPedido`/`ItemPedidoDTO` não têm campo de valor, então o pedido continua sem capturar o preço real do produto no momento da compra. Fica como próximo passo (exigiria migration Flyway nova).

---

## ⚙️ Tecnologias Utilizadas

- Java 17+
- Spring Boot
- Spring AMQP (RabbitMQ)
- Spring Cloud (Eureka Client, OpenFeign)
- Maven
- Lombok
- MySQL (ou outro banco relacional)

---

## 📎 Observações

Este projeto faz parte do desenvolvimento de um sistema maior chamado Natura Garden, com foco em vendas de plantas e arquitetura escalável.

---
