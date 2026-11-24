# Resultados de Benchmark — Sistema de Gestão Escolar

- **Data/Hora da Execução:** 2026-11-24 01:51:43
- **Sistema Operacional:** Windows 11 (amd64) — versão 10.0
- **Versão do Java / JVM:** 24.0.1 (Java HotSpot(TM) 64-Bit Server VM)
- **Núcleos Lógicos Disponíveis:** 8
- **Memória Heap Máxima (JVM):** 4032 MB
- **Protocolo de Medição:** 1 rodada(s) de warmup descartada(s) + 3 medições cronometradas por cenário

## Etapa 1 — Geração de Arquivos CSV (Sequencial vs Concorrente)

| Cenário / Volume | Execução | Média (ms) | Desvio Padrão (ms) | Vazão | Speedup |
| :--- | :--- | ---: | ---: | ---: | ---: |
| Pequeno (1k) | Sequencial | 9.28 | 3.24 | 107784 linhas/s | 1.00x |
| Pequeno (1k) | 1 thread(s) | 13.07 | 0.44 | 76507 linhas/s | 0.71x |
| Pequeno (1k) | 2 thread(s) | 8.97 | 1.70 | 111445 linhas/s | 1.03x |
| Pequeno (1k) | 4 thread(s) | 10.50 | 1.65 | 95194 linhas/s | 0.88x |
| Pequeno (1k) | 8 thread(s) | 15.50 | 3.02 | 64508 linhas/s | 0.60x |
| Médio (100k) | Sequencial | 165.22 | 9.31 | 605242 linhas/s | 1.00x |
| Médio (100k) | 1 thread(s) | 121.85 | 19.95 | 820699 linhas/s | 1.36x |
| Médio (100k) | 2 thread(s) | 72.10 | 18.03 | 1386922 linhas/s | 2.29x |
| Médio (100k) | 4 thread(s) | 50.36 | 11.22 | 1985866 linhas/s | 3.28x |
| Médio (100k) | 8 thread(s) | 48.96 | 4.08 | 2042332 linhas/s | 3.37x |
| Grande (1M) | Sequencial | 709.62 | 27.03 | 1409208 linhas/s | 1.00x |
| Grande (1M) | 1 thread(s) | 718.99 | 32.11 | 1390846 linhas/s | 0.99x |
| Grande (1M) | 2 thread(s) | 477.18 | 6.89 | 2095657 linhas/s | 1.49x |
| Grande (1M) | 4 thread(s) | 411.56 | 69.02 | 2429782 linhas/s | 1.72x |
| Grande (1M) | 8 thread(s) | 394.96 | 49.52 | 2531925 linhas/s | 1.80x |
| Gigante (5M) | Sequencial | 3721.71 | 173.38 | 1343467 linhas/s | 1.00x |
| Gigante (5M) | 1 thread(s) | 3900.39 | 360.27 | 1281922 linhas/s | 0.95x |
| Gigante (5M) | 2 thread(s) | 2962.81 | 230.13 | 1687587 linhas/s | 1.26x |
| Gigante (5M) | 4 thread(s) | 3371.06 | 546.98 | 1483211 linhas/s | 1.10x |
| Gigante (5M) | 8 thread(s) | 2999.67 | 580.31 | 1666849 linhas/s | 1.24x |

## Etapa 2 — Leitura e Parsing de CSV (Sequencial vs Concorrente)

| Cenário / Volume | Execução | Média (ms) | Desvio Padrão (ms) | Vazão | Speedup |
| :--- | :--- | ---: | ---: | ---: | ---: |
| Pequeno (1k) | Sequencial | 10.99 | 1.82 | 90993 linhas/s | 1.00x |
| Pequeno (1k) | 1 thread(s) | 7.34 | 1.80 | 136156 linhas/s | 1.50x |
| Pequeno (1k) | 2 thread(s) | 5.41 | 0.86 | 184940 linhas/s | 2.03x |
| Pequeno (1k) | 4 thread(s) | 5.72 | 0.19 | 174791 linhas/s | 1.92x |
| Pequeno (1k) | 8 thread(s) | 3.17 | 1.91 | 315222 linhas/s | 3.46x |
| Médio (100k) | Sequencial | 96.24 | 1.48 | 1039120 linhas/s | 1.00x |
| Médio (100k) | 1 thread(s) | 85.41 | 6.34 | 1170861 linhas/s | 1.13x |
| Médio (100k) | 2 thread(s) | 63.83 | 10.70 | 1566612 linhas/s | 1.51x |
| Médio (100k) | 4 thread(s) | 48.61 | 2.31 | 2057169 linhas/s | 1.98x |
| Médio (100k) | 8 thread(s) | 48.01 | 2.29 | 2082983 linhas/s | 2.00x |
| Grande (1M) | Sequencial | 856.94 | 15.19 | 1166943 linhas/s | 1.00x |
| Grande (1M) | 1 thread(s) | 881.09 | 56.20 | 1134957 linhas/s | 0.97x |
| Grande (1M) | 2 thread(s) | 715.05 | 29.56 | 1398499 linhas/s | 1.20x |
| Grande (1M) | 4 thread(s) | 611.82 | 19.86 | 1634461 linhas/s | 1.40x |
| Grande (1M) | 8 thread(s) | 600.05 | 4.48 | 1666538 linhas/s | 1.43x |
| Gigante (5M) | Sequencial | 4868.98 | 239.65 | 1026909 linhas/s | 1.00x |
| Gigante (5M) | 1 thread(s) | 4847.02 | 36.51 | 1031561 linhas/s | 1.00x |
| Gigante (5M) | 2 thread(s) | 3775.02 | 165.26 | 1324496 linhas/s | 1.29x |
| Gigante (5M) | 4 thread(s) | 3243.37 | 164.90 | 1541605 linhas/s | 1.50x |
| Gigante (5M) | 8 thread(s) | 2950.93 | 64.52 | 1694381 linhas/s | 1.65x |

## Etapa 3 — Repositório Concorrente: Sequencial vs. Locks vs. Atores (70% Leitura / 20% Escrita / 10% Remoção)

| Cenário / Volume | Execução | Média (ms) | Desvio Padrão (ms) | Vazão | Speedup |
| :--- | :--- | ---: | ---: | ---: | ---: |
| 100k ops (Base 100k) | Sequencial | 15.60 | 9.97 | 6409804 ops/s | 1.00x |
| 100k ops (Base 100k) | Locks (1t) | 18.63 | 10.38 | 5367514 ops/s | 0.84x |
| 100k ops (Base 100k) | Locks (2t) | 30.36 | 6.43 | 3293536 ops/s | 0.51x |
| 100k ops (Base 100k) | Locks (4t) | 47.83 | 14.04 | 2090779 ops/s | 0.33x |
| 100k ops (Base 100k) | Locks (8t) | 39.53 | 2.04 | 2529626 ops/s | 0.39x |
| 100k ops (Base 100k) | Atores (1t) | 850.17 | 117.92 | 117623 ops/s | 0.02x |
| 100k ops (Base 100k) | Atores (2t) | 409.71 | 21.74 | 244073 ops/s | 0.04x |
| 100k ops (Base 100k) | Atores (4t) | 381.64 | 31.76 | 262029 ops/s | 0.04x |
| 100k ops (Base 100k) | Atores (8t) | 412.00 | 19.33 | 242717 ops/s | 0.04x |
| 500k ops (Base 100k) | Sequencial | 48.95 | 2.84 | 10215172 ops/s | 1.00x |
| 500k ops (Base 100k) | Locks (1t) | 64.41 | 8.17 | 7762677 ops/s | 0.76x |
| 500k ops (Base 100k) | Locks (2t) | 149.71 | 14.61 | 3339734 ops/s | 0.33x |
| 500k ops (Base 100k) | Locks (4t) | 207.24 | 18.08 | 2412608 ops/s | 0.24x |
| 500k ops (Base 100k) | Locks (8t) | 242.63 | 1.15 | 2060783 ops/s | 0.20x |
| 500k ops (Base 100k) | Atores (1t) | 3868.04 | 91.47 | 129265 ops/s | 0.01x |
| 500k ops (Base 100k) | Atores (2t) | 2096.18 | 54.60 | 238529 ops/s | 0.02x |
| 500k ops (Base 100k) | Atores (4t) | 2031.30 | 80.79 | 246147 ops/s | 0.02x |
| 500k ops (Base 100k) | Atores (8t) | 1993.87 | 32.35 | 250768 ops/s | 0.02x |

