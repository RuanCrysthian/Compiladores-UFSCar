# Trabalho 5

Aluno: Ruan Crysthian Lima Ferraz - RA: 790866

Analisador Semântico da linguagem LA que foi desenvolvida pelo prof. Dr. Jander.

## Dependências
OpenJDK >= 17.0.7

Antlr4

Maven >= 3.6.3

---

## Build

Gerar o *.jar*:

```
mvn package
```

Será gerado o seguinte arquivo na pasta target: ```la-1.0-SNAPSHOT-jar-with-dependencies.jar```

Para executá-lo será preciso de duas depêndecias:

1. Arquivo de entrada para teste
- OBS: deve ser passado todo o caminho do arquivo, isto é, desde a raiz.
- Exemplo: ```/Downloads/casos-de-teste//home/ruan/Ufscar/casos-de-teste/3.casos_teste_t4/entrada/1.algoritmo_2-2_apostila_LA.txt```

2. Arquivo de saída.
- Também é necessário informar todo o caminho do arquivo
- Exemplo: ```/home/ruan/Documents/Compiladores-UFSCar/T4/LA/src/teste.txt ```

### Como executar

```
java -jar la-1.0-SNAPSHOT-jar-with-dependencies.jar <input> <output>
```

Exemplo:

```
java -jar //home/ruan/Ufscar/Compiladores-UFSCar/T4/LA/target/LA-1.0-SNAPSHOT-jar-with-dependencies.jar /Downloads/casos-de-teste//home/ruan/Ufscar/casos-de-teste/3.casos_teste_t4/entrada/1.algoritmo_2-2_apostila_LA.txt /home/ruan/Documents/Compiladores-UFSCar/T4/LA/src/teste.txt
```
