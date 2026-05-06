# ViceVirtue - Rastreador de Hábitos e Vícios

ViceVirtue é um aplicativo Android focado em privacidade e uso local, projetado para ajudar usuários a gerenciarem seus hábitos, distinguindo entre **Vícios** (hábitos negativos a serem evitados) e **Virtudes** (hábitos positivos a serem cultivados).

## Conceitos Fundamentais

- **Vícios (Vices):** Hábitos que você deseja abandonar. A contagem de dias (streak) representa o tempo decorrido desde a última "falha".
- **Virtudes (Virtues):** Hábitos que você deseja construir. A contagem de dias (streak) representa os dias consecutivos em que você "triunfou".

## Principais Funcionalidades

- **Aba Principal:** Visão geral de todos os itens monitorados com suas sequências atuais, registro rápido de eventos e uma seção colapsável de histórico recente.
- **Sistema de Comentários:** Registro detalhado opcional. Adicione descrições ou motivos para cada entrada para contextualizar seu progresso. Pode ser ativado/desativado nas configurações.
- **Personalização de Tema:** Suporte para temas Claro, Escuro e do Sistema, configurável diretamente nas configurações.
- **Visualização de Detalhes:** Histórico completo de um hábito específico, permitindo editar/excluir entradas e atualizar descrições/motivos.
- **Histórico Global:** Visão consolidada de todas as atividades, agrupadas por data e com suporte a filtros por tipo.
- **Suporte Multi-idioma:** Suporte total para Inglês e Português (Brasil).
- **Interface Responsiva:** Desenvolvida com Jetpack Compose seguindo o tema personalizado "Vice/Virtue" (Vermelho para Vícios, Azul para Virtudes).
- **Widget de Tela Inicial:** Acompanhe seu hábito favorito diretamente da tela inicial com botões de registro rápido.
- **Lembretes Inteligentes:** Agende notificações personalizadas. Vícios lembram você de lutar ("Lembre-se de lutar contra..."), enquanto Virtudes incentivam a prática ("Lembre-se de praticar...").
- **Integridade de Dados:** Garantia de nomes exclusivos para hábitos (insensível a maiúsculas/minúsculas).

## Stack Tecnológica

- **Linguagem:** Kotlin 2.0.21
- **Framework de UI:** Jetpack Compose (Material 3)
- **Arquitetura:** MVVM + Clean Architecture (Arquitetura Limpa)
- **Banco de Dados:** Room (SQLite Local)
- **Injeção de Dependência:** Hilt
- **Framework de Widget:** Jetpack Glance
- **Gerenciamento de Estado:** DataStore para vinculação de widgets
- **Navegação:** Compose Navigation

## Primeiros Passos

1. Abra o projeto no Android Studio (recomenda-se a versão Koala ou superior).
2. Sincronize o projeto com os arquivos do Gradle.
3. Execute o aplicativo em um emulador ou dispositivo físico (API mínima 26).

## Localização

Todas as strings estão externalizadas em `res/values/strings.xml` e `res/values-pt/strings.xml`.

## Licença

Projeto Privado / Interno
