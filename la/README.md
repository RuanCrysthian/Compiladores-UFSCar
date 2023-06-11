# Trabalho 1

Aluno: Ruan Crysthian Lima Ferraz - RA: 790866

Analisador Léxico da linguagem LA que foi desenvolvida pelo prof. Dr. Jander.

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
- Exemplo: ``` /home/ruan/Documents/Compiladores-UFSCar/la/1.casos_teste_t1/entrada/1-algoritmo_2-2_apostila_LA.txt ```

2. Arquivo de saída.
- Também é necessário informar todo o caminho do arquivo
- Exemplo: ```/home/ruan/Documents/Compiladores-UFSCar/la/1.casos_teste_t1/teste/saida.txt ```

### Como executar

```
java -jar la/target/la-1.0-SNAPSHOT-jar-with-dependencies.jar <input> <output>
```

Exemplo: 

```
java -jar {/home/ruan/Documents/Compiladores-UFSCar/la/target/la-1.0-SNAPSHOT-jar-with-dependencies.jar /home/ruan/Documents/Compiladores-UFSCar/la/1.casos_teste_t1/entrada/1-algoritmo_2-2_apostila_LA.txt /home/ruan/Documents/Compiladores-UFSCar/la/1.casos_teste_t1/teste/saida.txt
```
