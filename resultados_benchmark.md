# Resultados de Benchmark — Sistema de Gestão Escolar

- **Data/Hora da Execução:** 2026-11-24 15:14:41
- **Sistema Operacional:** Windows 11 (amd64) — versão 10.0
- **Versão do Java / JVM:** 24.0.1 (Java HotSpot(TM) 64-Bit Server VM)
- **Núcleos Lógicos Disponíveis:** 8
- **Memória Heap Máxima (JVM):** 4032 MB
- **Protocolo de Medição:** 2 rodada(s) de warmup descartada(s) + 5 medições cronometradas por cenário

## Etapa 1 — Geração de Arquivos CSV (Sequencial vs. Concorrente)

| Cenário / Volume | Execução | Média (ms) | Mediana (ms) | Desvio Padrão (ms) | CV (%) | Vazão | Speedup | Eficiência |
| :--- | :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Pequeno (1k) | Sequencial | 2.79 | 2.73 | 0.42 | 15.2% | 358873 linhas/s | 1.00x | 100.0% |
| Pequeno (1k) | 1 thread(s) | 3.74 | 3.73 | 0.36 | 9.7% | 267258 linhas/s | 0.74x | 74.5% |
| Pequeno (1k) | 2 thread(s) | 5.08 | 5.25 | 0.54 | 10.6% | 196726 linhas/s | 0.55x | 27.4% |
| Pequeno (1k) | 4 thread(s) | 7.37 | 7.00 | 0.95 | 12.9% | 135742 linhas/s | 0.38x | 9.5% |
| Pequeno (1k) | 8 thread(s) | 11.68 | 11.49 | 0.44 | 3.8% | 85624 linhas/s | 0.24x | 3.0% |
| Médio (100k) | Sequencial | 36.50 | 33.30 | 4.65 | 12.7% | 2739606 linhas/s | 1.00x | 100.0% |
| Médio (100k) | 1 thread(s) | 46.42 | 47.07 | 9.04 | 19.5% | 2154124 linhas/s | 0.79x | 78.6% |
| Médio (100k) | 2 thread(s) | 31.24 | 33.35 | 4.10 | 13.1% | 3200920 linhas/s | 1.17x | 58.4% |
| Médio (100k) | 4 thread(s) | 27.67 | 24.87 | 5.18 | 18.7% | 3613717 linhas/s | 1.32x | 33.0% |
| Médio (100k) | 8 thread(s) | 32.73 | 33.08 | 1.86 | 5.7% | 3055338 linhas/s | 1.12x | 13.9% |
| Grande (1M) | Sequencial | 185.33 | 181.03 | 18.31 | 9.9% | 5395864 linhas/s | 1.00x | 100.0% |
| Grande (1M) | 1 thread(s) | 373.64 | 270.44 | 161.13 | 43.1% | 2676392 linhas/s | 0.50x | 49.6% |
| Grande (1M) | 2 thread(s) | 193.09 | 189.90 | 24.95 | 12.9% | 5179054 linhas/s | 0.96x | 48.0% |
| Grande (1M) | 4 thread(s) | 142.04 | 128.52 | 33.77 | 23.8% | 7040390 linhas/s | 1.30x | 32.6% |
| Grande (1M) | 8 thread(s) | 136.98 | 140.21 | 9.55 | 7.0% | 7300520 linhas/s | 1.35x | 16.9% |
| Gigante (5M) | Sequencial | 983.98 | 912.87 | 131.87 | 13.4% | 5081387 linhas/s | 1.00x | 100.0% |
| Gigante (5M) | 1 thread(s) | 1629.74 | 1641.57 | 420.59 | 25.8% | 3067966 linhas/s | 0.60x | 60.4% |
| Gigante (5M) | 2 thread(s) | 1018.44 | 1037.40 | 212.63 | 20.9% | 4909480 linhas/s | 0.97x | 48.3% |
| Gigante (5M) | 4 thread(s) | 711.45 | 717.59 | 136.28 | 19.2% | 7027916 linhas/s | 1.38x | 34.6% |
| Gigante (5M) | 8 thread(s) | 715.32 | 709.59 | 131.90 | 18.4% | 6989903 linhas/s | 1.38x | 17.2% |

## Etapa 2 — Leitura e Parsing de CSV (Sequencial vs. Concorrente)

| Cenário / Volume | Execução | Média (ms) | Mediana (ms) | Desvio Padrão (ms) | CV (%) | Vazão | Speedup | Eficiência |
| :--- | :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Pequeno (1k) | Sequencial | 4.83 | 4.23 | 1.31 | 27.1% | 207064 linhas/s | 1.00x | 100.0% |
| Pequeno (1k) | 1 thread(s) | 4.70 | 4.94 | 0.83 | 17.7% | 212854 linhas/s | 1.03x | 102.8% |
| Pequeno (1k) | 2 thread(s) | 3.51 | 3.45 | 0.30 | 8.6% | 285289 linhas/s | 1.38x | 68.9% |
| Pequeno (1k) | 4 thread(s) | 3.34 | 3.23 | 0.26 | 7.8% | 299125 linhas/s | 1.44x | 36.1% |
| Pequeno (1k) | 8 thread(s) | 3.84 | 3.59 | 0.85 | 22.2% | 260428 linhas/s | 1.26x | 15.7% |
| Médio (100k) | Sequencial | 76.16 | 77.50 | 1.99 | 2.6% | 1313070 linhas/s | 1.00x | 100.0% |
| Médio (100k) | 1 thread(s) | 59.89 | 60.23 | 4.18 | 7.0% | 1669731 linhas/s | 1.27x | 127.2% |
| Médio (100k) | 2 thread(s) | 43.15 | 44.58 | 4.07 | 9.4% | 2317518 linhas/s | 1.76x | 88.2% |
| Médio (100k) | 4 thread(s) | 36.34 | 36.46 | 4.70 | 12.9% | 2751469 linhas/s | 2.10x | 52.4% |
| Médio (100k) | 8 thread(s) | 35.98 | 33.33 | 6.22 | 17.3% | 2778999 linhas/s | 2.12x | 26.5% |
| Grande (1M) | Sequencial | 708.12 | 689.00 | 61.93 | 8.7% | 1412184 linhas/s | 1.00x | 100.0% |
| Grande (1M) | 1 thread(s) | 794.71 | 786.96 | 47.15 | 5.9% | 1258318 linhas/s | 0.89x | 89.1% |
| Grande (1M) | 2 thread(s) | 619.85 | 631.99 | 111.93 | 18.1% | 1613300 linhas/s | 1.14x | 57.1% |
| Grande (1M) | 4 thread(s) | 586.64 | 599.85 | 113.50 | 19.3% | 1704635 linhas/s | 1.21x | 30.2% |
| Grande (1M) | 8 thread(s) | 485.39 | 521.21 | 103.79 | 21.4% | 2060220 linhas/s | 1.46x | 18.2% |
| Gigante (5M) | Sequencial | 4499.62 | 4448.03 | 269.83 | 6.0% | 1111206 linhas/s | 1.00x | 100.0% |
| Gigante (5M) | 1 thread(s) | 4252.21 | 4209.87 | 173.54 | 4.1% | 1175860 linhas/s | 1.06x | 105.8% |
| Gigante (5M) | 2 thread(s) | 3320.93 | 3295.65 | 161.31 | 4.9% | 1505601 linhas/s | 1.35x | 67.7% |
| Gigante (5M) | 4 thread(s) | 2955.59 | 2994.87 | 136.60 | 4.6% | 1691711 linhas/s | 1.52x | 38.1% |
| Gigante (5M) | 8 thread(s) | 2800.52 | 2795.20 | 90.58 | 3.2% | 1785381 linhas/s | 1.61x | 20.1% |

## Etapa 3 — Repositório Concorrente: Sequencial vs. Locks vs. Atores (70% Leitura / 20% Escrita / 10% Remoção)

| Cenário / Volume | Execução | Média (ms) | Mediana (ms) | Desvio Padrão (ms) | CV (%) | Vazão | Speedup | Eficiência |
| :--- | :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 100k ops (Base 100k) | Sequencial | 11.02 | 10.43 | 1.76 | 16.0% | 9073982 ops/s | 1.00x | 100.0% |
| 100k ops (Base 100k) | Locks (1t) | 21.97 | 16.47 | 11.19 | 50.9% | 4551508 ops/s | 0.50x | 50.2% |
| 100k ops (Base 100k) | Locks (2t) | 37.69 | 31.77 | 9.92 | 26.3% | 2653486 ops/s | 0.29x | 14.6% |
| 100k ops (Base 100k) | Locks (4t) | 51.20 | 45.77 | 13.66 | 26.7% | 1952988 ops/s | 0.22x | 5.4% |
| 100k ops (Base 100k) | Locks (8t) | 64.82 | 53.99 | 21.03 | 32.4% | 1542748 ops/s | 0.17x | 2.1% |
| 100k ops (Base 100k) | Atores (1t) | 876.08 | 865.88 | 81.54 | 9.3% | 114145 ops/s | 0.01x | 1.3% |
| 100k ops (Base 100k) | Atores (2t) | 683.19 | 593.31 | 259.60 | 38.0% | 146373 ops/s | 0.02x | 0.8% |
| 100k ops (Base 100k) | Atores (4t) | 424.86 | 414.81 | 40.05 | 9.4% | 235370 ops/s | 0.03x | 0.6% |
| 100k ops (Base 100k) | Atores (8t) | 433.47 | 412.01 | 47.54 | 11.0% | 230697 ops/s | 0.03x | 0.3% |
| 500k ops (Base 100k) | Sequencial | 50.72 | 47.96 | 10.48 | 20.7% | 9857854 ops/s | 1.00x | 100.0% |
| 500k ops (Base 100k) | Locks (1t) | 63.34 | 58.24 | 15.43 | 24.4% | 7894133 ops/s | 0.80x | 80.1% |
| 500k ops (Base 100k) | Locks (2t) | 153.93 | 150.77 | 11.13 | 7.2% | 3248269 ops/s | 0.33x | 16.5% |
| 500k ops (Base 100k) | Locks (4t) | 225.63 | 208.52 | 27.42 | 12.2% | 2216013 ops/s | 0.22x | 5.6% |
| 500k ops (Base 100k) | Locks (8t) | 242.80 | 233.53 | 13.78 | 5.7% | 2059309 ops/s | 0.21x | 2.6% |
| 500k ops (Base 100k) | Atores (1t) | 3939.13 | 3891.82 | 191.74 | 4.9% | 126931 ops/s | 0.01x | 1.3% |
| 500k ops (Base 100k) | Atores (2t) | 2176.31 | 2136.83 | 88.83 | 4.1% | 229747 ops/s | 0.02x | 1.2% |
| 500k ops (Base 100k) | Atores (4t) | 2078.88 | 2050.16 | 98.39 | 4.7% | 240515 ops/s | 0.02x | 0.6% |
| 500k ops (Base 100k) | Atores (8t) | 1999.33 | 1989.31 | 64.52 | 3.2% | 250084 ops/s | 0.03x | 0.3% |

## Resumo Executivo dos Resultados

- **Etapa 1 (Geração):** Maior speedup de **1.38x** em `Gigante (5M)` com `4 thread(s)` (vazão de **7027916 linhas/s**, mediana de 717.59 ms).
- **Etapa 2 (Leitura):** Maior speedup de **2.12x** em `Médio (100k)` com `8 thread(s)` (vazão de **2778999 linhas/s**, mediana de 33.33 ms).
- **Etapa 3 (Repositório):** Maior vazão de **9857854 ops/s** em `500k ops (Base 100k)` com `Sequencial` (speedup de **1.00x**, mediana de 47.96 ms).
