# Sistema de gestão escolar
# Sobre
Este repositório tem o objetivo de desenvolver um sistema de gestão
escolar de escopo reduzido, aplicando conceitos de programção Orientada
a objetos (POO). 
Além da implementação das funcionalidades do sistema, o projetos busca explorar
e comparar abordagens de programação sequencial e concorrente, analisando
seu desempenho diante de diferentes volumes de dados.
A proposta é observar como cada abordagem se comporta em operações de processamento
de dados, identificando possíveis diferenças de desempenho, vantagems e limitações.

## Objetivos
Demonstrar na prática as diferenças de desempenho e arquitetura entre a programação sequencial 
e concorrente em Java. Dessa maneira, exemplificar o impacto do volume de dados e da quantidade de 
threads no processamento em lote, além de comparar o controle de acesso simultâneo utilizando sincronização 
tradicional por locks com o modelo de atores com Apache Pekko.

## Funcionalidades
* **Processamento CSV:** Leitura e geração sequencial/concorrente com validação automática de dados.
* **Multithreading:** Processamento paralelo para leitura e escrita rápida de grandes volumes de registros.
* **Modelo de Atores (Apache Pekko):** Gerenciamento assíncrono e seguro de estado no repositório.
* **Controle de Concorrência:** Repositórios sincronizados com *locks* para evitar condições de corrida.
* **Domínio Escolar:** Validação de regras de negócio e geração de boletins.

## Tecnologias utilizadas
Java, Maven, Apache pekko(Atores Akka), Gemini Pro
CSV, JUnit 5

## Estrutura do projeto
A estrutura do projeto utilizou o padrão do Maven:
```text
sistema-gestao-escolar/
├── .gitignore
├── pom.xml
└── src/
    ├── main/
    │   └── java/
    │       ├── Classes/
    │       └── org/
    │           └── example/
    │               ├── atores/
    │               ├── dominio/
    │               ├── formatacao/
    │               ├── geracao/
    │               ├── leitura/
    │               └── repositorio/
    └── test/
        └── java/
```
## Como executar
### Pré-requisitos
* Java Development Kit (JDK) https://www.oracle.com/java/technologies/downloads/
* Maven https://maven.apache.org/ (para gerenciamento de dependências e build)

### Passo a Passo

   ```bash
   git clone https://github.com/lucaslms-web/sistema-gestao-escolar.git

   cd sistema-gestao-escola

   mvn clean install
   ``` 
## Experimentos e análise de desempenho
Leia: [Relatório Comum do Grupo — Sistema de Gestão Escolar](Relatório%20Comum%20do%20Grupo%20—%20Sistema%20de%20Gestão%20Escolar.pdf)

## Integrantes
Lucas, Matheus, Renan lemos
