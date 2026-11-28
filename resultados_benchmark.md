# Resultados de Benchmark — Sistema de Gestão Escolar

- **Data/Hora da Execução:** 2026-11-28 00:08:21
- **Sistema Operacional:** Windows 11 (amd64) — versão 10.0
- **Versão do Java / JVM:** 24.0.1 (Java HotSpot(TM) 64-Bit Server VM)
- **Núcleos Lógicos Disponíveis:** 8
- **Memória Heap Máxima (JVM):** 4032 MB
- **Protocolo de Medição:** 2 rodada(s) de warmup descartada(s) + 5 medições cronometradas por cenário

## Etapa 1 — Geração de Arquivos CSV (Sequencial vs. Concorrente)

| Cenário / Volume | Execução | Média (ms) | Mediana (ms) | Desvio Padrão (ms) | CV (%) | Vazão | Speedup | Eficiência |
| :--- | :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Pequeno (1k) | Sequencial | 1.73 | 1.90 | 0.39 | 22.7% | 576888 linhas/s | 1.00x | 100.0% |
| Pequeno (1k) | 1 thread(s) | 2.82 | 2.79 | 0.20 | 6.9% | 354401 linhas/s | 0.61x | 61.4% |
| Pequeno (1k) | 2 thread(s) | 3.92 | 3.96 | 0.45 | 11.4% | 255176 linhas/s | 0.44x | 22.1% |
| Pequeno (1k) | 4 thread(s) | 5.41 | 5.36 | 0.34 | 6.3% | 184968 linhas/s | 0.32x | 8.0% |
| Pequeno (1k) | 8 thread(s) | 9.22 | 9.09 | 0.50 | 5.4% | 108505 linhas/s | 0.19x | 2.4% |
| Médio (100k) | Sequencial | 20.17 | 19.19 | 2.60 | 12.9% | 4957799 linhas/s | 1.00x | 100.0% |
| Médio (100k) | 1 thread(s) | 23.14 | 22.24 | 1.90 | 8.2% | 4321880 linhas/s | 0.87x | 87.2% |
| Médio (100k) | 2 thread(s) | 15.10 | 15.16 | 0.29 | 1.9% | 6624245 linhas/s | 1.34x | 66.8% |
| Médio (100k) | 4 thread(s) | 15.72 | 15.68 | 1.34 | 8.5% | 6362764 linhas/s | 1.28x | 32.1% |
| Médio (100k) | 8 thread(s) | 19.88 | 20.94 | 2.04 | 10.3% | 5030728 linhas/s | 1.01x | 12.7% |
| Grande (1M) | Sequencial | 181.63 | 180.80 | 3.13 | 1.7% | 5505766 linhas/s | 1.00x | 100.0% |
| Grande (1M) | 1 thread(s) | 283.80 | 229.77 | 127.07 | 44.8% | 3523603 linhas/s | 0.64x | 64.0% |
| Grande (1M) | 2 thread(s) | 135.56 | 136.44 | 3.12 | 2.3% | 7376995 linhas/s | 1.34x | 67.0% |
| Grande (1M) | 4 thread(s) | 111.21 | 109.67 | 6.30 | 5.7% | 8992232 linhas/s | 1.63x | 40.8% |
| Grande (1M) | 8 thread(s) | 100.91 | 101.20 | 2.37 | 2.3% | 9909479 linhas/s | 1.80x | 22.5% |

## Etapa 2 — Leitura e Parsing de CSV (Sequencial vs. Concorrente)

| Cenário / Volume | Execução | Média (ms) | Mediana (ms) | Desvio Padrão (ms) | CV (%) | Vazão | Speedup | Eficiência |
| :--- | :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Pequeno (1k) | Sequencial | 3.59 | 3.51 | 0.87 | 24.2% | 278224 linhas/s | 1.00x | 100.0% |
| Pequeno (1k) | 1 thread(s) | 3.91 | 2.98 | 1.60 | 40.9% | 255451 linhas/s | 0.92x | 91.8% |
| Pequeno (1k) | 2 thread(s) | 3.69 | 3.80 | 0.48 | 13.0% | 270985 linhas/s | 0.97x | 48.7% |
| Pequeno (1k) | 4 thread(s) | 2.85 | 3.19 | 0.53 | 18.6% | 351008 linhas/s | 1.26x | 31.5% |
| Pequeno (1k) | 8 thread(s) | 3.02 | 3.12 | 0.39 | 12.9% | 331536 linhas/s | 1.19x | 14.9% |
| Médio (100k) | Sequencial | 65.89 | 63.66 | 8.13 | 12.3% | 1517693 linhas/s | 1.00x | 100.0% |
| Médio (100k) | 1 thread(s) | 62.55 | 57.77 | 11.45 | 18.3% | 1598734 linhas/s | 1.05x | 105.3% |
| Médio (100k) | 2 thread(s) | 42.61 | 42.93 | 5.77 | 13.5% | 2346795 linhas/s | 1.55x | 77.3% |
| Médio (100k) | 4 thread(s) | 33.48 | 33.03 | 4.91 | 14.7% | 2986628 linhas/s | 1.97x | 49.2% |
| Médio (100k) | 8 thread(s) | 34.49 | 33.00 | 3.42 | 9.9% | 2899709 linhas/s | 1.91x | 23.9% |
| Grande (1M) | Sequencial | 632.17 | 632.32 | 33.73 | 5.3% | 1581853 linhas/s | 1.00x | 100.0% |
| Grande (1M) | 1 thread(s) | 778.42 | 797.09 | 35.86 | 4.6% | 1284656 linhas/s | 0.81x | 81.2% |
| Grande (1M) | 2 thread(s) | 658.36 | 656.88 | 74.73 | 11.4% | 1518925 linhas/s | 0.96x | 48.0% |
| Grande (1M) | 4 thread(s) | 581.36 | 630.34 | 83.25 | 14.3% | 1720090 linhas/s | 1.09x | 27.2% |
| Grande (1M) | 8 thread(s) | 495.25 | 481.01 | 39.90 | 8.1% | 2019190 linhas/s | 1.28x | 16.0% |

## Etapa 3 — Repositório Concorrente: Sequencial vs. Locks vs. Atores (70% Leitura / 20% Escrita / 10% Remoção)

| Cenário / Volume | Execução | Média (ms) | Mediana (ms) | Desvio Padrão (ms) | CV (%) | Vazão | Speedup | Eficiência |
| :--- | :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 100k ops (Base 100k) | Sequencial | 9.80 | 8.98 | 2.66 | 27.2% | 10200168 ops/s | 1.00x | 100.0% |
| 100k ops (Base 100k) | Locks (1t) | 12.67 | 12.75 | 0.59 | 4.6% | 7895152 ops/s | 0.77x | 77.4% |
| 100k ops (Base 100k) | Locks (2t) | 39.32 | 33.91 | 14.09 | 35.8% | 2543300 ops/s | 0.25x | 12.5% |
| 100k ops (Base 100k) | Locks (4t) | 37.63 | 37.99 | 0.82 | 2.2% | 2657344 ops/s | 0.26x | 6.5% |
| 100k ops (Base 100k) | Locks (8t) | 49.24 | 44.68 | 10.55 | 21.4% | 2030718 ops/s | 0.20x | 2.5% |
| 100k ops (Base 100k) | Atores (1t) | 833.01 | 849.15 | 52.35 | 6.3% | 120047 ops/s | 0.01x | 1.2% |
| 100k ops (Base 100k) | Atores (2t) | 529.42 | 478.76 | 104.03 | 19.6% | 188884 ops/s | 0.02x | 0.9% |
| 100k ops (Base 100k) | Atores (4t) | 433.56 | 432.13 | 19.09 | 4.4% | 230647 ops/s | 0.02x | 0.6% |
| 100k ops (Base 100k) | Atores (8t) | 461.34 | 462.79 | 65.93 | 14.3% | 216759 ops/s | 0.02x | 0.3% |
| 500k ops (Base 100k) | Sequencial | 46.87 | 45.78 | 2.26 | 4.8% | 10666726 ops/s | 1.00x | 100.0% |
| 500k ops (Base 100k) | Locks (1t) | 63.45 | 58.98 | 15.28 | 24.1% | 7879759 ops/s | 0.74x | 73.9% |
| 500k ops (Base 100k) | Locks (2t) | 158.80 | 158.37 | 5.01 | 3.2% | 3148564 ops/s | 0.30x | 14.8% |
| 500k ops (Base 100k) | Locks (4t) | 266.08 | 276.16 | 33.03 | 12.4% | 1879122 ops/s | 0.18x | 4.4% |
| 500k ops (Base 100k) | Locks (8t) | 253.35 | 247.25 | 15.41 | 6.1% | 1973578 ops/s | 0.19x | 2.3% |
| 500k ops (Base 100k) | Atores (1t) | 4151.74 | 4199.06 | 110.88 | 2.7% | 120431 ops/s | 0.01x | 1.1% |
| 500k ops (Base 100k) | Atores (2t) | 2199.76 | 2165.42 | 74.21 | 3.4% | 227297 ops/s | 0.02x | 1.1% |
| 500k ops (Base 100k) | Atores (4t) | 2099.47 | 2089.46 | 82.06 | 3.9% | 238155 ops/s | 0.02x | 0.6% |
| 500k ops (Base 100k) | Atores (8t) | 2030.01 | 2023.49 | 39.07 | 1.9% | 246304 ops/s | 0.02x | 0.3% |

## Resumo Executivo dos Resultados

- **Etapa 1 (Geração):** Maior speedup de **1.80x** em `Grande (1M)` com `8 thread(s)` (vazão de **9909479 linhas/s**, mediana de 101.20 ms).
- **Etapa 2 (Leitura):** Maior speedup de **1.97x** em `Médio (100k)` com `4 thread(s)` (vazão de **2986628 linhas/s**, mediana de 33.03 ms).
- **Etapa 3 (Repositório):** Maior vazão de **10666726 ops/s** em `500k ops (Base 100k)` com `Sequencial` (speedup de **1.00x**, mediana de 45.78 ms).
