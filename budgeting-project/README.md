# Desafio DIO — Spring AI: Assistente de Orçamento (Budgeting API)

Este repositório é a minha evolução do projeto final do módulo **Spring AI** da trilha [dio-spring-boot-learning-track](https://github.com/digitalinnovationone/dio-spring-boot-learning-track), da DIO.

## O que o projeto faz

É uma API de orçamento pessoal que entende comandos de voz sobre gastos. O fluxo principal:

1. O cliente envia um áudio (`POST /transactions/ai`);
2. O áudio é transcrito em texto (`TranscriptionModel`, Whisper);
3. Um `ChatClient` interpreta a intenção e escolhe uma ferramenta (`@Tool`) para executar;
4. A ferramenta persiste ou consulta transações financeiras;
5. A resposta final é convertida em áudio (`TextToSpeechModel`) e devolvida ao cliente.

A arquitetura segue camadas limpas (domain / application / infrastructure), com os mesmos casos de uso servindo tanto os endpoints REST quanto as ferramentas de IA.

## Melhoria implementada

Escolhi combinar duas frentes da lista de evoluções sugeridas, porque uma sozinha ficava pequena demais e as duas juntas se completam bem:

### 1. Novos tipos de consulta financeira

- **`GetTotalSpentByCategoryUseCase`** — nova ferramenta `get-total-spent-by-category`, que soma o valor total gasto em uma categoria e informa quantas transações a compõem. Endpoint REST equivalente: `GET /transactions/{category}/total`.
- **`ListAllTransactionsUseCase`** — nova ferramenta `list-all-transactions`, que lista todas as transações sem filtro de categoria (o projeto base só permitia listar por categoria). Endpoint REST equivalente: `GET /transactions`.

Ambas foram registradas no `ChatClient` junto com as ferramentas já existentes, então o assistente por voz agora também responde perguntas como *"quanto eu gastei com farmácia?"* ou *"lista todos os meus gastos"*.

### 2. Validações antes de salvar uma transação

`PersistTransactionUseCase` agora valida a entrada antes de persistir:

- descrição não pode ser vazia/em branco;
- valor deve ser maior que zero;
- categoria é obrigatória.

Qualquer violação lança `InvalidTransactionException` (nova exceção de domínio), capturada por um `@RestControllerAdvice` (`TransactionExceptionHandler`) que devolve `400 Bad Request` com uma mensagem clara, em vez de deixar o erro estourar como `500`. Também atualizei o prompt do sistema para o assistente pedir esclarecimento em vez de inventar valores quando faltar informação.

## Tecnologias usadas

- Java 25 + Spring Boot
- Spring AI (`ChatClient`, `TranscriptionModel`, `TextToSpeechModel`, Tool Calling)
- Spring Data JPA + MySQL
- Gradle
- JUnit 5 + Mockito + AssertJ (testes unitários)
- Docker Compose (ambiente de desenvolvimento)

## Como executar

Defina sua chave da OpenAI:

```bash
export OPENAI_API_KEY="sua_api_key_aqui"
```

Suba a aplicação:

```bash
./gradlew bootRun
```

## Como testar o fluxo principal

**Testes unitários (não exigem chave de API):**

```bash
./gradlew test --tests "dio.budgeting.application.*"
```

Cobrem as validações de `PersistTransactionUseCase` e as regras dos novos casos de uso (`GetTotalSpentByCategoryUseCase`, `ListAllTransactionsUseCase`).

**Manualmente, via REST:**

```bash
# Criar uma transação
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"description":"Compras no mercado","category":"GROCERIES","amount":5000}'

# Listar todas as transações (novo)
curl http://localhost:8080/transactions

# Listar transações por categoria
curl http://localhost:8080/transactions/GROCERIES

# Total gasto por categoria (novo)
curl http://localhost:8080/transactions/GROCERIES/total

# Enviar uma transação inválida (dispara 400)
curl -i -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"description":"","category":"GROCERIES","amount":0}'
```

**Via voz (fluxo completo com IA):**

```bash
curl -X POST http://localhost:8080/transactions/ai \
  -F "file=@audio.mp3" \
  --output resposta.mp3
```

Peça, por exemplo: *"gastei 30 reais no mercado"*, depois *"quanto eu já gastei em mercado?"* ou *"lista todas as minhas transações"* — o assistente deve escolher a ferramenta certa para cada pedido.

## O que aprendi durante o desafio

Ficou muito mais claro como o Tool Calling do Spring AI depende inteiramente da qualidade das anotações `@Tool`/`@ToolParam` e do prompt do sistema: o modelo escolhe a ferramenta certa quando a descrição é objetiva, e erra ou hesita quando ela é vaga. Também reforcei a importância de manter a validação na camada de aplicação (não no controller nem no domínio "puro"), porque assim a mesma regra vale tanto para quem chama a API por REST quanto para quem chama por voz — o que seria fácil de quebrar se eu tivesse colocado a validação só no `TransactionController`.

---

Baseado no projeto do módulo 05 (Spring AI) da trilha [dio-spring-boot-learning-track](https://github.com/digitalinnovationone/dio-spring-boot-learning-track).
