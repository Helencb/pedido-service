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

## ⚙️ Tecnologias Utilizadas

- Java 17+
- Spring Boot
- Spring AMQP (RabbitMQ)
- Maven
- Lombok
- MySQL (ou outro banco relacional)

---

## 📎 Observações

Este projeto faz parte do desenvolvimento de um sistema maior chamado Natura Garden, com foco em vendas de plantas e arquitetura escalável.

---
