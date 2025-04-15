# Teste para Candidatos - Spring e Hibernate

Este projeto contém uma aplicação simples de gerenciamento de produtos, clientes e pedidos, desenvolvida com Spring e Hibernate. A aplicação contém bugs intencionais que devem ser identificados e corrigidos pelo candidato.

## Descrição do Projeto

A aplicação permite:
- Cadastrar, consultar, atualizar e excluir produtos
- Cadastrar, consultar, atualizar e excluir clientes
- Criar pedidos associados a clientes
- Adicionar, remover e atualizar itens de pedidos
- Calcular valores totais de pedidos e estoque

## Problemas Conhecidos

A aplicação apresenta os seguintes problemas que precisam ser corrigidos:

### Problemas com Produtos

1. **Validação de Produtos Incompleta**: O sistema permite a criação de produtos sem informações obrigatórias, o que pode causar inconsistências no catálogo e problemas na exibição para os clientes.

2. **Atualização de Preço não Funciona**: Quando um gestor tenta atualizar o preço de um produto, a alteração não é aplicada, mantendo o preço antigo. Isso impede ajustes de preços necessários para promoções ou reajustes.

3. **Aceitação de Valores Negativos e Zero**: O sistema aceita preços negativos e zero para produtos, o que não faz sentido do ponto de vista de negócio e pode causar problemas nos cálculos de pedidos e relatórios financeiros.

4. **Validação de Faixa de Preço Ausente**: Ao buscar produtos por faixa de preço, o sistema aceita valores inválidos (mínimo maior que máximo), retornando resultados incorretos que podem confundir os usuários.

5. **Exclusão de Produtos Inexistentes**: O sistema retorna sucesso ao tentar excluir produtos que não existem, o que pode gerar confusão para os usuários e logs de operação imprecisos.

### Problemas com Clientes

1. **Validação de E-mail Insuficiente**: O sistema aceita e-mails em formato inválido, o que pode causar falhas na comunicação com os clientes e problemas de integridade dos dados.

2. **Falha na Listagem de Clientes**: A funcionalidade de listar todos os clientes apresenta erros, impedindo que gestores visualizem a base de clientes completa.

3. **Falha na Busca de Clientes com Pedidos**: A funcionalidade que deveria mostrar apenas clientes que realizaram pedidos não funciona corretamente, dificultando a análise de clientes ativos.

4. **Vulnerabilidade na Busca de Clientes**: A busca de clientes por nome está vulnerável a ataques, permitindo acesso não autorizado a informações confidenciais.

5. **Atualização de Clientes com Problemas**: O sistema falha ao tentar atualizar informações de clientes, impedindo a correção de dados cadastrais.

### Problemas com Pedidos

1. **Criação de Pedidos com Falhas**: O sistema apresenta erros ao criar novos pedidos, impedindo o registro correto das vendas.

2. **Adição de Itens a Pedidos não Funciona**: Não é possível adicionar produtos a um pedido existente, limitando a capacidade dos clientes de complementarem suas compras.

3. **Cancelamento de Pedidos não Efetivo**: A funcionalidade de cancelar pedidos não altera o status do pedido para "Cancelado", mantendo-o como "Pendente", o que causa confusão no controle de estoque e faturamento.

4. **Finalização de Pedidos não Atualiza Estoque**: Quando um pedido é finalizado, o sistema não reduz o estoque dos produtos vendidos, causando divergências no controle de inventário.

5. **Aceitação de Quantidades Negativas**: O sistema permite adicionar itens com quantidades negativas aos pedidos, o que não faz sentido do ponto de vista de negócio e pode causar problemas nos cálculos de valores e estoque.

## Testes Disponíveis

O projeto inclui testes unitários e de integração para ajudar na identificação dos bugs.

### Cenários de Erro de Regra de Negócio
Os testes incluem cenários específicos para verificar erros de regra de negócio, como:
- Produtos com preço negativo ou zero
- Clientes com email inválido
- Pedidos com itens inválidos ou sem itens
- Atualização de estoque com valores negativos
- Exclusão de produtos que estão em uso em pedidos
- Cálculos incorretos de valores totais

## Instruções para os Candidatos

1. Execute os testes para verificar os problemas existentes
2. Identifique e corrija os bugs encontrados
3. Envie o código corrigido para avaliação

## Tecnologias Utilizadas

- Java 11
- Spring Framework 4.3.9
- Hibernate 5.2.10
- H2 Database
- JUnit 5

## Executando o Projeto

```bash
mvn clean install
mvn test
```

Boa sorte!
