# budget-voice-ai

Assistente financeiro por voz construído com Spring Boot e Spring AI. Recebe um comando de voz, entende a intenção, executa a operação de negócio correspondente e responde em áudio.

Evoluído a partir do projeto final do módulo Spring AI da [trilha Spring Boot da DIO](https://github.com/digitalinnovationone/dio-spring-boot-learning-track).

## Sumário

- [Visão geral](#visão-geral)
- [Arquitetura](#arquitetura)
- [Decisões de projeto](#decisões-de-projeto)
- [API](#api)
- [Executando localmente](#executando-localmente)
- [Testes](#testes)
- [Próximos passos](#próximos-passos)

## Visão geral

O sistema processa comandos de voz relacionados a transações financeiras. Fluxo:

1. Cliente envia um arquivo de áudio;
2. `TranscriptionModel` (Whisper) converte o áudio em texto;
3. Um `ChatClient` interpreta a intenção e seleciona uma ferramenta de aplicação (`@Tool`) para executá-la;
4. A ferramenta executa a regra de negócio (persistência ou consulta);
5. `TextToSpeechModel` converte a resposta final em áudio, devolvido ao cliente.

O mesmo conjunto de casos de uso atende tanto aos endpoints REST quanto às ferramentas de IA — não existe lógica de negócio duplicada entre os dois caminhos de entrada.

## Arquitetura

A regra que guia a organização: um caso de uso não sabe se foi chamado por um controller REST ou por um modelo de linguagem. Isso mantém o comportamento de negócio testável e consistente independentemente do canal de entrada.

## Decisões de projeto

**Validação na camada de aplicação, não no controller.**
`PersistTransactionUseCase` valida descrição, valor e categoria antes de persistir, lançando `InvalidTransactionException`. Se a validação estivesse no `TransactionController`, o fluxo de voz (que não passa por lá) ficaria sem proteção. Um `@RestControllerAdvice` (`TransactionExceptionHandler`) traduz a exceção de domínio em `400 Bad Request` para o cliente REST.

**Consultas agregadas como novos casos de uso, não como lógica ad-hoc no controller.**
`GetTotalSpentByCategoryUseCase` e `ListAllTransactionsUseCase` foram adicionados como serviços de aplicação próprios — reutilizáveis por REST e por Tool Calling — em vez de somar valores diretamente no controller ou no prompt.

**Prompt como parte do contrato do sistema, não como texto solto.**
O prompt do sistema foi atualizado para orientar explicitamente quando usar cada nova ferramenta e para pedir esclarecimento em vez de inferir valores ausentes — tratando o prompt como uma peça de configuração que evolui junto com as ferramentas disponíveis, e não como um detalhe de implementação isolado.

## API

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/transactions` | Cria uma transação (validada) |
| `GET` | `/transactions` | Lista todas as transações |
| `GET` | `/transactions/{category}` | Lista transações de uma categoria |
| `GET` | `/transactions/{category}/total` | Total gasto em uma categoria |
| `POST` | `/transactions/ai` | Recebe áudio, executa o fluxo completo, retorna áudio |

Ferramentas expostas ao modelo via Tool Calling: `persist-transaction`, `list-transactions-by-category`, `list-all-transactions`, `get-total-spent-by-category`.

## Executando localmente

```bash
export OPENAI_API_KEY="sua_api_key_aqui"
./gradlew bootRun
```

Exemplo de uso via REST:

```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"description":"Compras no mercado","category":"GROCERIES","amount":5000}'

curl http://localhost:8080/transactions/GROCERIES/total
```

Exemplo de uso via voz:

```bash
curl -X POST http://localhost:8080/transactions/ai \
  -F "file=@audio.mp3" --output resposta.mp3
```

## Testes

```bash
./gradlew test --tests "dio.budgeting.application.*"
```

Testes unitários (JUnit 5, Mockito, AssertJ) cobrem as validações de `PersistTransactionUseCase` e o comportamento dos dois novos casos de uso, sem exigir credenciais de IA. Os testes de integração existentes (`*IT`), que exercitam o modelo real da OpenAI, permanecem opcionais e dependem de `OPENAI_API_KEY`.

## Próximos passos

Caso o projeto continuasse evoluindo, os pontos seguintes seriam a sequência natural: paginação nas listagens, filtro de transações por período, e um teste de integração cobrindo o roteamento de intenção entre as quatro ferramentas registradas no `ChatClient`.

---

Stack: Java 25, Spring Boot, Spring AI, Spring Data JPA, MySQL, Gradle, JUnit 5, Docker Compose.
