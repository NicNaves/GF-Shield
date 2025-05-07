# 🇧🇷 GF-Shield: Seleção de Features Distribuída para IDS

## 📃 Visão Geral

**GF-Shield** é uma arquitetura distribuída baseada na metaheurística **GRASP-FS**, voltada para a seleção de atributos em sistemas de detecção de intrusões (IDS). O sistema é dividido em duas partes principais:

* **DRG (Distributed RCL Generator)** — Gera listas candidatas (RCL) com diferentes algoritmos de seleção de features.
* **DLS (Distributed Local Search)** — Aplica buscas locais distribuídas sobre essas listas para encontrar a melhor solução.

---

## 🧠 Algoritmos Utilizados (DRG)

Cada algoritmo é um microsserviço independente e envia soluções para o Kafka:

* Information Gain (**porta 8089**)
* Gain Ratio (**porta 8088**)
* Symmetrical Uncertainty (**porta 8087**)
* Relief (**porta 8086**)

---

## 🔍 Parâmetros de Entrada

Todos os algoritmos DRG recebem os seguintes parâmetros via `x-www-form-urlencoded`:

* **maxGenerations**: número de soluções geradas por execução.
* **rclCutoff**: número máximo de atributos na RCL.
* **sampleSize**: número de atributos sorteados da RCL por solução.
* **datasetTrainingName**: nome do arquivo `.arff` de treino (deve estar em `/datasets`).
* **datasetTestingName**: nome do arquivo `.arff` de teste.
* **classifierName** *(opcional)*: classificador Weka a ser utilizado (`J48`, `NB`, `RF`, etc).

---

## 🔁 Microsserviços de Busca Local (DLS)

Executam otimizações sobre as soluções do DRG:

* BitFlip (**8082**)
* IWSS (**8083**)
* IWSSR (**8084**)
* Verify (**8085**)
* RVND (**8090**)
* VND (**8091**)

---

## 🚀 Arquitetura Geral

![Arquitetura Full](./figures/ArquiteturaFull.drawio.png)

### DRG: Gera listas RCL a partir dos datasets.

### DLS: Aplica busca local sobre essas listas utilizando diferentes estratégias.

#### Fluxo:

```
[datasets/] → [Microsserviços DRG] → [Kafka - Initial Solutions Topic]
                                     → [Microsserviços DLS]
                                     → [Kafka - Best Solutions Topic]
                                     → [IDS]
```

---

## 🚚 Tecnologias

* Java 17
* Spring Boot 2.7.x
* Apache Kafka
* Weka
* Docker & Docker Compose
* Conduktor Console

---

## 📦 Execução com Docker Compose

### 1. Estrutura Esperada

```
project-root/
├── datasets/         # Dataset compartilhado (entrada)
├── metrics/          # Resultados (.csv) gerados (saída)
├── docker-compose.yml
```

### 2. Rodar o Projeto

```bash
docker-compose up --build
```

### 3. Visualizar Tópicos Kafka

Acesse [http://localhost:8080](http://localhost:8080) via navegador (Conduktor).

---

## 📃 Resultados

Todos os resultados (F1-score, tempo, features, etc.) são armazenados automaticamente nos arquivos `.csv` da pasta `/metrics`.

---

## 🤝 Colaboradores

* [Silvio Ereno Quincozes](https://github.com/sequincozes)
* [Estêvão Filipe Cardoso](https://github.com/EstevaoFCardoso)

---

# 🇺🇸 GF-Shield: Scalable Feature Selection for Intrusion Detection

## 📃 Overview

**GF-Shield** is a distributed architecture based on the **GRASP-FS** metaheuristic to perform feature selection for Intrusion Detection Systems (IDS). It consists of two major components:

* **DRG (Distributed RCL Generator)** — Generates Restricted Candidate Lists (RCL) using different feature selection algorithms.
* **DLS (Distributed Local Search)** — Applies local search algorithms in a distributed way to improve those solutions.

---

## 🧠 Feature Selection Algorithms (DRG)

Each algorithm runs as an independent microservice and sends solutions to Kafka:

* Information Gain (**port 8089**)
* Gain Ratio (**port 8088**)
* Symmetrical Uncertainty (**port 8087**)
* Relief (**port 8086**)

---

## 🔍 Input Parameters

All DRG microservices expect the following parameters via `x-www-form-urlencoded`:

* **maxGenerations**: number of solutions to generate.
* **rclCutoff**: max number of features per RCL.
* **sampleSize**: number of features selected from the RCL per solution.
* **datasetTrainingName**: name of the `.arff` training file (must be in `/datasets`).
* **datasetTestingName**: name of the `.arff` test file.
* **classifierName** *(optional)*: Weka classifier to use (`J48`, `NB`, `RF`, etc).

---

## 🔁 Local Search Microservices (DLS)

These microservices optimize the initial solutions:

* BitFlip (**8082**)
* IWSS (**8083**)
* IWSSR (**8084**)
* Verify (**8085**)
* RVND (**8090**)
* VND (**8091**)

---

## 🚀 Architecture

![Arquitetura Full](./figures/ArquiteturaFull.drawio.png)

### DRG: Generates RCL lists from training datasets.

### DLS: Receives and improves those lists using local search.

#### Flow:

```
[datasets/] → [DRG Microservices] → [Kafka - Initial Solutions Topic]
                                 → [DLS Microservices]
                                 → [Kafka - Best Solutions Topic]
                                 → [IDS]
```

---

## 🚚 Technologies

* Java 17
* Spring Boot 2.7.x
* Apache Kafka
* Weka
* Docker & Docker Compose
* Conduktor Console

---

## 📦 Running with Docker Compose

### 1. Expected Structure

```
project-root/
├── datasets/         # Shared input
├── metrics/          # CSV output
├── docker-compose.yml
```

### 2. Start

```bash
docker-compose up --build
```

### 3. Access Kafka Console

Go to [http://localhost:8080](http://localhost:8080) to explore topics in Conduktor.

---

## 📃 Output

All results (F1-score, features used, time, etc.) are saved into `.csv` files under the `/metrics` volume.

---

## 🤝 Contributors

* [Silvio Ereno Quincozes](https://github.com/sequincozes)
* [Estêvão Filipe Cardoso](https://github.com/EstevaoFCardoso)
