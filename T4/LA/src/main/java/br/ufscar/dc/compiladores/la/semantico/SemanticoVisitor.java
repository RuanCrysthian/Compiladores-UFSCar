package br.ufscar.dc.compiladores.la.semantico;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.Token;

import br.ufscar.dc.compiladores.VerificadorTipos;
import br.ufscar.dc.compiladores.la.semantico.TabelaDeSimbolos.TipoEntradaTds;
import br.ufscar.dc.compiladores.la.semantico.TabelaDeSimbolos.TipoVariavel;

public class SemanticoVisitor extends laBaseVisitor<Void> {

  Escopos escoposAninhados = new Escopos();

  VerificadorTipos verificador;

  // atualiza verificador de tipos com o escopo atual
  void atualizaVerificador() {
    verificador = new VerificadorTipos(escoposAninhados);
  }

  // função para verificar se um identificador está sendo utilizado e, se não,
  // declara ele
  private Void doUpdateTable(String text, TipoEntradaTds tipoEntrada, TipoVariavel tipoObjeto, Token t) {
    if (escoposAninhados.obterEscopoAtual().existe(text)) {
      SemanticoUtils.adicionarErroSemantico(t, "identificador " + text + " ja declarado anteriormente");
    } else {
      escoposAninhados.obterEscopoAtual().adicionar(text, tipoEntrada, tipoObjeto);
    }
    return null;
  }

  // função para verificar se um identificador já está sendo usado
  private boolean verificaUsoIdentificador(laParser.IdentificadorContext ctx) {
    for (TabelaDeSimbolos escopo : escoposAninhados.percorrerEscoposAninhados()) {
      if (escopo.verificar(ctx.ident1.getText()) != null) {
        return true;
      }
    }
    return false;
  }

  // função para verificar se um identificador já está sendo usado
  private EntradaTabelaDeSimbolos getTdsIdentificador(String ident) {
    EntradaTabelaDeSimbolos etds = null;
    for (TabelaDeSimbolos escopo : escoposAninhados.percorrerEscoposAninhados()) {
      etds = escopo.verificar(ident);
      if (etds != null) {
        return etds;
      }
    }
    return etds;
  }

  // função para verificar o tipo de um identificador
  private TipoVariavel verificaTipoIdentificador(laParser.IdentificadorContext ctx) {
    TipoVariavel tipo = TipoVariavel.INVALIDO;
    for (TabelaDeSimbolos tds : escoposAninhados.percorrerEscoposAninhados()) {
      if (tds.verificar(ctx.ident1.getText()) != null) {
        tipo = tds.verificar(ctx.ident1.getText()).tipoObjeto;
        return tipo;
      }
    }
    return tipo;
  }

  // função que retorna uma lista com os tipos dos parâmetros de uma função
  // ordenados da forma que as variáveis aparecem
  private List<TipoVariavel> getTiposFunc(laParser.ParametrosContext ctx) {
    if (ctx.parametro() != null) {
      List<TipoVariavel> resp = new ArrayList<>();
      ctx.parametro().forEach(param -> {
        if (param.tipo_estendido() != null) {
          if (param.tipo_estendido().tipo_basico_ident() != null) {
            if (param.tipo_estendido().tipo_basico_ident().tipo_basico() != null) {
              resp.add(SemanticoUtils.verificarTipo(param.tipo_estendido().tipo_basico_ident().tipo_basico().getText(),
                  false));
            }
            if (param.tipo_estendido().tipo_basico_ident().IDENT() != null) {
              resp.add(TipoVariavel.TIPO);
            }
          }
        }
      });
      return resp;
    }
    return null;
  }

  @Override
  public Void visitPrograma(laParser.ProgramaContext ctx) {
    // Verificando se existe algum "retorne" (cmdRetorne) dentro da função principal
    // (main)
    if (ctx.corpo() != null) {
      if (ctx.corpo().cmd() != null) {
        ctx.corpo().cmd().forEach(cmd -> {
          if (cmd.cmdRetorne() != null) {
            SemanticoUtils.adicionarErroSemantico(cmd.cmdRetorne().getStart(),
                "comando retorne nao permitido nesse escopo");
          }
        });
      }
    }
    return super.visitPrograma(ctx);
  }

  @Override
  public Void visitDeclaracao_local(laParser.Declaracao_localContext ctx) {
    // declaracao_local
    // se existe IDENT -> declaração de tipo ou constante
    // se não, é declaração de variavel e é tratado no visitVariavel
    if (ctx.IDENT() != null) {
      String ident = ctx.IDENT().getText();
      // 'constante' IDENT ':' tipo_basico '=' valor_constante
      if (ctx.tipo_basico() != null) {
        // verifica tipo
        TipoVariavel tipoVar = SemanticoUtils.verificarTipo(ctx.tipo_basico().getText(), false);
        // atualiza o escopo
        doUpdateTable(ident, TipoEntradaTds.VARIAVEL, tipoVar, ctx.tipo_basico().getStart());
      } else {
        // 'tipo' IDENT ':' tipo;
        // verifica se IDENT já existe no escopo
        if (escoposAninhados.obterEscopoAtual().existe(ident)) {
          SemanticoUtils.adicionarErroSemantico(ctx.IDENT().getSymbol(),
              "identificador " + ident + " ja declarado anteriormente");
        }
        // tipo -> registro
        // seta novo tipo como registro
        if (ctx.tipo().registro() != null) {
          TabelaDeSimbolos tabStruct = new TabelaDeSimbolos();
          // registro -> variavel
          // percorre variaveis dentro do registro
          for (laParser.VariavelContext var : ctx.tipo().registro().variavel()) {
            // verifica se o identificador da variável já foi usado
            if (tabStruct.existe(var.ident1.getText())) {
              SemanticoUtils.adicionarErroSemantico(var.ident1.getStart(),
                  "identificador " + var.ident1.getText() + " ja declarado anteriormente");
            }
            // verifica se existe registro dentro de registro
            if (var.tipo().registro() != null) {
              // #todo registro dentro de registro
            } else if (var.tipo().tipo_estendido() != null) {
              // tipo -> tipo_estendido
              // se o tipo é um tipo_estendido

              // verifica se é ponteiro
              boolean isPointer = false;
              if (var.tipo().tipo_estendido().getText().startsWith("^")) {
                isPointer = true;
              }

              // tipo -> tipo_estendido -> tipo_basico_ident -> tipo_basico
              // verifica se o tipo_estendido é um tipo_basico_ident que é um tipo_basico
              if (var.tipo().tipo_estendido().tipo_basico_ident().tipo_basico() != null) {
                // encontra o tipo básico
                TipoVariavel tipoVariavel = SemanticoUtils
                    .verificarTipo(var.tipo().tipo_estendido().tipo_basico_ident().getText(), isPointer);
                // adiciona identificadores, verificando se já não existem
                for (laParser.IdentificadorContext idCtx : var.identificador()) {
                  if (tabStruct.existe(idCtx.ident1.getText())) {
                    SemanticoUtils.adicionarErroSemantico(idCtx.getStart(),
                        "identificador " + idCtx.ident1.getText() + " ja declarado anteriormente");
                  } else {
                    tabStruct.adicionar(idCtx.ident1.getText(), TipoEntradaTds.VARIAVEL, tipoVariavel);
                  }
                }
              } else if (var.tipo().tipo_estendido().tipo_basico_ident().IDENT() != null) {
                // tipo -> tipo_estendido -> tipo_basico_ident -> IDENT
                // se o campo da struct é um tipo definido em runtime

                // #todo campo como tipo
              }
            }
          }
          // como é um registro, é criada uma tabela de simbolos e colocados os subcampos
          // da struct nela, com seus respectivos tipos
          // depois, essa tabela é associada ao identificador do registro.
          // quando uma declaração desse registro for feita, a tabela é colada e
          // associada para o novo identificador
          EntradaTabelaDeSimbolos etd = new EntradaTabelaDeSimbolos(TipoEntradaTds.TIPO, tabStruct);
          // salva o registro e sua tabela modelo no escopo
          escoposAninhados.obterEscopoAtual().adicionar(ctx.IDENT().getText(), etd);
        } else if (ctx.tipo().tipo_estendido() != null) {
          // tipo -> tipo_estendido
          // seta novo tipo como tipo_estendido

          // verifica se é ponteiro
          boolean isPointer = false;
          if (ctx.tipo().tipo_estendido().getText().startsWith("^")) {
            isPointer = true;
          }

          if (ctx.tipo().tipo_estendido().tipo_basico_ident().tipo_basico() != null) {
            escoposAninhados.obterEscopoAtual().adicionar(ctx.IDENT().getText(), TipoEntradaTds.TIPO, SemanticoUtils
                .verificarTipo(ctx.tipo().tipo_estendido().tipo_basico_ident().tipo_basico().getText(), isPointer));
          } else if (ctx.tipo().tipo_estendido().tipo_basico_ident().IDENT() != null) {
            // tipo -> tipo_estendido -> tipo_basico_ident -> IDENT
            // se o tipo sendo definido é um tipo definido em runtime

            // #todo tipo como um tipo
          }
        }
      }
    }
    // chama o super para chamar visitVariavel
    return super.visitDeclaracao_local(ctx);
  }

  @Override
  public Void visitCmdAtribuicao(laParser.CmdAtribuicaoContext ctx) {
    // verifica se está atribuindo para uma variável referenciada por um ponteiro
    boolean atribuindoParaTipoDoPointer = ctx.getText().startsWith("^");
    ;

    // verifica se o identificador usado existe
    boolean identificadorUsado = verificaUsoIdentificador(ctx.identificador());
    // se não existir, salva o erro
    if (!identificadorUsado) {
      SemanticoUtils.adicionarErroSemantico(ctx.getStart(),
          "identificador " + ctx.identificador().getText() + " nao declarado");
    } else { // caso exista, prossegue com a atribuição

      // atualiza o contexto (escopo) do verificador de tipos
      atualizaVerificador();
      // verifica o tipo da expressão
      TipoVariavel tipoVerificado = verificador.verificaTipo(ctx.expressao());

      // pega o nome do verificador, com ^ caso exista
      String identificador = ctx.identificador().getText();
      if (atribuindoParaTipoDoPointer) {
        identificador = "^" + identificador;
      }
      // se existir uma expressao inválida (tipo dela for inválido), retorna erro
      if (tipoVerificado == TipoVariavel.INVALIDO) {
        SemanticoUtils.adicionarErroSemantico(ctx.getStart(), "atribuicao nao compativel para " + identificador);
      } else if (ctx.identificador().subIdent.size() > 0) {
        // se a atribuição for para um campo de um registro

        // pega o nome do campo
        String subCampo = ctx.identificador().subIdent.get(0).getText();
        // resgata a tabela do registro e verifica se o campo existe nele
        EntradaTabelaDeSimbolos retornoIdent1 = escoposAninhados.obterEscopoAtual()
            .verificar(ctx.identificador().ident1.getText());
        TabelaDeSimbolos tab = retornoIdent1.subTabela;

        // se não existir, gera o erro
        if (tab.verificar(subCampo) == null) {
          SemanticoUtils.adicionarErroSemantico(ctx.getStart(), "identificador " + subCampo + " nao declarado");
        } else {
          // se existir, é resgatado o tipo do campo
          TipoVariavel tipoPai = tab.verificar(subCampo).tipoObjeto;
          // e verificada a compatibilidade com a variável para a qual o valor está sendo
          // atribuido
          if (VerificadorTipos.isIncompativel(tipoVerificado, tipoPai)) {
            SemanticoUtils.adicionarErroSemantico(ctx.getStart(), "atribuicao nao compativel para " + identificador);
          }
        }
      } else {
        // se a atribuição for para uma variável normal
        // resgata o tipo dela
        TipoVariavel tipoPai = verificaTipoIdentificador(ctx.identificador());
        // e verifica se a expressão é compatível com o tipo resgatado
        if (VerificadorTipos.isIncompativel(tipoVerificado, tipoPai)) {
          SemanticoUtils.adicionarErroSemantico(ctx.getStart(), "atribuicao nao compativel para " + identificador);
        }
      }
    }
    return null;

  }

  @Override
  public Void visitIdentificador(laParser.IdentificadorContext ctx) {
    // verifica se o identificador mais a esquerda existe, e resgata a sua entrada
    EntradaTabelaDeSimbolos identificador = escoposAninhados.obterEscopoAtual().verificar(ctx.ident1.getText());
    // se não existir, gera erro
    if (identificador == null) {
      SemanticoUtils.adicionarErroSemantico(ctx.getStart(), "identificador " + ctx.getText() + " nao declarado");
      return null;
    }
    // se existir, verifica se é um registro
    if (ctx.subIdent.size() > 0) {
      // se for, utiliza a sua tabela de registro para verificar se o campo do
      // registro existe
      TabelaDeSimbolos tab = identificador.subTabela;
      // lógica genérica para verificar para qualquer quantidade de registros
      // aninhados
      for (Token t : ctx.subIdent) {
        if (tab.verificar(t.getText()) == null) {
          SemanticoUtils.adicionarErroSemantico(t, "identificador " + ctx.getText() + " nao declarado");
          break;
        }
        tab = tab.verificar(t.getText()).subTabela;
      }
    }

    return null;
  }

  @Override
  public Void visitParametro(laParser.ParametroContext ctx) {
    // parametro -> tipo_estendido
    if (ctx.tipo_estendido() != null) {

      TipoVariavel tipo = TipoVariavel.INVALIDO;
      // verifica se é uma variável resgatada por um ponteiro
      boolean isPointer = ctx.tipo_estendido().getText().startsWith("^");
      // parametro -> tipo_estendido -> tipo_basico_ident -> tipo_basico

      // caso os parâmetros forem de um tipo basico
      if (ctx.tipo_estendido().tipo_basico_ident().tipo_basico() != null) {
        // resgata tipo dos parâmetros
        tipo = SemanticoUtils.verificarTipo(ctx.tipo_estendido().tipo_basico_ident().tipo_basico().getText(),
            isPointer);
        // salva os paramêtros no escopo
        for (br.ufscar.dc.compiladores.la.semantico.laParser.IdentificadorContext identificadorContext : ctx
            .identificador()) {
          doUpdateTable(identificadorContext.getText(), TipoEntradaTds.VARIAVEL, tipo, identificadorContext.getStart());
        }
      } else if (ctx.tipo_estendido().tipo_basico_ident().IDENT() != null) {
        // parametro -> tipo_estendido -> tipo_basico_ident -> IDENT

        // caso os parâmetros forem de um tipo definido em runtime
        // pega o tipo
        String identTipo = ctx.tipo_estendido().tipo_basico_ident().IDENT().getText();
        // descobre se o tipo existe (foi declarado em runtime)
        EntradaTabelaDeSimbolos etd = null;
        for (TabelaDeSimbolos escopo : escoposAninhados.percorrerEscoposAninhados()) {
          etd = escopo.verificar(identTipo);
          if (etd != null) {
            break;
          }
        }
        // verifica se o tipo existe
        if (etd == null) {
          // gera erro caso não exista
          SemanticoUtils.adicionarErroSemantico(ctx.getStart(), "tipo " + identTipo + " nao declarado");
        } else {
          // caso exista, continua com as declarações
          // salvando os parâmetros no escopo
          for (br.ufscar.dc.compiladores.semantico.laParser.IdentificadorContext identificadorContext : ctx
              .identificador()) {
            etd.tipoEntrada = TipoEntradaTds.TIPO;
            escoposAninhados.obterEscopoAtual().adicionar(identificadorContext.ident1.getText(), etd);
          }
        }
      }
    }
    return null;
  }

  @Override
  public Void visitParcela_unario(laParser.Parcela_unarioContext ctx) {
    // parcela_unario -> IDENT: é uma função/procedimento
    if (ctx.IDENT() != null) {
      // resgata a entrada da tabela de símbolos
      EntradaTabelaDeSimbolos etd = getTdsIdentificador(ctx.IDENT().getText());
      // gera erro se ela não existir
      if (etd == null) {
        SemanticoUtils.adicionarErroSemantico(ctx.getStart(),
            "identificador " + ctx.IDENT().getText() + " nao declarado");
      }
      // verifica se a quantidade de expressoes (parametros) é compatível com os
      // definidos na função
      if (ctx.expressao().size() != etd.tiposFuncao.size()) {
        SemanticoUtils.adicionarErroSemantico(ctx.getStart(),
            "incompatibilidade de parametros na chamada de " + ctx.IDENT().getText());
        return super.visitParcela_unario(ctx);
      }
      // se não tiver parametros não precisa fazer nada
      if (ctx.expressao().isEmpty()) {
        return super.visitParcela_unario(ctx);
      }
      // se tiver é necessário verificar se todos são compatíveis
      VerificadorTipos verificador = new VerificadorTipos(escoposAninhados);
      for (int i = 0; i < etd.tiposFuncao.size(); i++) {
        if (verificador.verificaTipo(ctx.expressao().get(i)) != etd.tiposFuncao.get(i)) {
          SemanticoUtils.adicionarErroSemantico(ctx.getStart(),
              "incompatibilidade de parametros na chamada de " + ctx.IDENT().getText());
          return super.visitParcela_unario(ctx);
        }
      }

    }
    // chama super para visitar o resto das possibilidades caso não seja uma
    // função/parâmetro
    return super.visitParcela_unario(ctx);
  }

  @Override
  public Void visitDeclaracao_global(laParser.Declaracao_globalContext ctx) {
    List<TipoVariavel> tiposParametros = getTiposFunc(ctx.parametros());
    // caso esteja declarando uma função, salva ela no contexto com seus parâmetros
    if (ctx.tipo_estendido() != null) {
      escoposAninhados.obterEscopoAtual().adicionar(ctx.IDENT().getText(), TipoEntradaTds.FUNCAO,
          SemanticoUtils.verificarTipo(ctx.tipo_estendido().getText(), false), tiposParametros);
    } else { // caso seja um procedimento, salva como procedimento e verifica se não existe
             // nenhum "retorne" indesejado
      escoposAninhados.obterEscopoAtual().adicionar(ctx.IDENT().getText(), TipoEntradaTds.PROCEDIMENTO,
          TipoVariavel.INVALIDO, tiposParametros);
      ctx.cmd().forEach(cmd -> {
        if (cmd.cmdRetorne() != null) {
          SemanticoUtils.adicionarErroSemantico(cmd.cmdRetorne().getStart(),
              "comando retorne nao permitido nesse escopo");
        }
      });
    }
    // cria novo escopo (da função/procedimento)
    escoposAninhados.criarNovoEscopo();
    // visita todas as partes da expressão
    visitParametros(ctx.parametros());
    ctx.declaracao_local().forEach(loc -> {
      visitDeclaracao_local(loc);
    });
    ctx.cmd().forEach(cmd -> {
      visitCmd(cmd);
    });
    // apaga o escopo da função
    escoposAninhados.abandonarEscopo();
    return null;
  }

  @Override
  public Void visitVariavel(laParser.VariavelContext ctx) {
    TipoVariavel tipo = TipoVariavel.INVALIDO;
    // variavel -> tipo -> registro
    if (ctx.tipo().registro() != null) {
      // se o tipo da{,s} variáve{l,is} é um registro
      TabelaDeSimbolos tabStruct = new TabelaDeSimbolos();

      // itera entre as variaveis do registro
      for (laParser.VariavelContext var : ctx.tipo().registro().variavel()) {
        // verifica se o identificador já não foi utilizado
        if (tabStruct.existe(var.ident1.ident1.getText())) {
          SemanticoUtils.adicionarErroSemantico(var.ident1.getStart(),
              "identificador " + var.ident1.ident1.getText() + " ja declarado anteriormente");
        }

        if (var.tipo().registro() != null) {
          // #todo registro dentro de registro
        } else if (var.tipo().tipo_estendido() != null) {
          // registro -> variavel -> tipo -> tipo_estendido
          boolean isPointer = false;
          // verifica se é um ponteiro
          if (var.tipo().tipo_estendido().getText().startsWith("^")) {
            isPointer = true;
          }
          // registro -> variavel -> tipo -> tipo_estendido -> tipo_basico_ident ->
          // tipo_basico
          if (var.tipo().tipo_estendido().tipo_basico_ident().tipo_basico() != null) {
            // resgata o tipo
            TipoVariavel tipoVariavel = SemanticoUtils
                .verificarTipo(var.tipo().tipo_estendido().tipo_basico_ident().getText(), isPointer);
            // passa pelas variáveis do campo adicionando no tabStruct (tabela do registro)
            // caso o identificador já não tenha sido utilizado
            var.identificador().forEach(idCtx -> {
              if (tabStruct.existe(idCtx.ident1.getText())) {
                SemanticoUtils.adicionarErroSemantico(idCtx.getStart(),
                    "identificador " + idCtx.ident1.getText() + " ja declarado anteriormente");
              } else {
                tabStruct.adicionar(idCtx.ident1.getText(), TipoEntradaTds.VARIAVEL, tipoVariavel);
              }
            });
          } else if (var.tipo().tipo_estendido().tipo_basico_ident().IDENT() != null) {
            // #todo variavel do registro sendo um tipo definido em runtime
          }
        }
      }
      // cria a entrada utilizando a tabStruct (tabela de símbolos do registro
      // definido)
      EntradaTabelaDeSimbolos etd = new EntradaTabelaDeSimbolos(TipoEntradaTds.REGISTRO, tabStruct);
      // e salva para cada identificador declarado
      // dessa forma o registro é transformado em uma tabela de símbolos e
      // essa tabela é associada as variáveis que são do tipo do registro
      ctx.identificador().forEach(identificadorContext -> {
        escoposAninhados.obterEscopoAtual().adicionar(identificadorContext.ident1.getText(), etd);
      });
    } else if (ctx.tipo().tipo_estendido() != null) {
      // variavel -> tipo -> tipo_estendido

      // verifica se é ponteiro
      boolean isPointer = ctx.tipo().tipo_estendido().getText().startsWith("^");
      // variavel -> tipo -> tipo_estendido -> tipo_basico_ident -> tipo_basico
      if (ctx.tipo().tipo_estendido().tipo_basico_ident().tipo_basico() != null) {
        // resgata o tipo
        tipo = SemanticoUtils.verificarTipo(ctx.tipo().tipo_estendido().tipo_basico_ident().tipo_basico().getText(),
            isPointer);
        // salva as variáveis no escopo usando o tipo encontrado se o tipo for válido,
        // se não gera erro
        if (tipo == TipoVariavel.INVALIDO) {
          SemanticoUtils.adicionarErroSemantico(ctx.tipo().getStart(),
              "tipo " + ctx.tipo().getText() + " nao declarado");
        } else {
          for (br.ufscar.dc.compiladores.la.semantico.laParser.IdentificadorContext identificadorContext : ctx
              .identificador()) {
            doUpdateTable(identificadorContext.ident1.getText(), TipoEntradaTds.VARIAVEL, tipo,
                identificadorContext.getStart());
          }
        }
      } else if (ctx.tipo().tipo_estendido().tipo_basico_ident().IDENT() != null) {
        // variavel -> tipo -> tipo_estendido -> tipo_basico_ident -> IDENT

        // se o tipo da variável foi definido em runtime
        // resgata ele
        String identTipo = ctx.tipo().tipo_estendido().tipo_basico_ident().IDENT().getText();

        // verificar se tipo existe, se existir
        // resgata a EntradaTabelaDeSimbolos associada ao tipo
        // de modo equivalente a criação de registros
        EntradaTabelaDeSimbolos etd = getTdsIdentificador(identTipo);
        // se não existir, gera erro
        if (etd == null) {
          SemanticoUtils.adicionarErroSemantico(ctx.getStart(), "tipo " + identTipo + " nao declarado");
          // declara as variáveis como inválidas
          for (br.ufscar.dc.compiladores.semantico.laParser.IdentificadorContext identificadorContext : ctx
              .identificador()) {
            doUpdateTable(identificadorContext.ident1.getText(), TipoEntradaTds.VARIAVEL, TipoVariavel.INVALIDO,
                identificadorContext.getStart());
          }
        } else {
          // se existir, adiciona as variáveis no escopo com o tipo inferido
          for (br.ufscar.dc.compiladores.semantico.laParser.IdentificadorContext identificadorContext : ctx
              .identificador()) {
            // verifica se o identificador da variável já não foi utilizado
            if (escoposAninhados.obterEscopoAtual().verificar(identificadorContext.getText()) != null) {
              SemanticoUtils.adicionarErroSemantico(ctx.getStart(),
                  "identificador " + identificadorContext.ident1.getText() + " ja declarado anteriormente");
            } else {
              etd.tipoEntrada = TipoEntradaTds.TIPO;
              etd.tipoObjeto = TipoVariavel.TIPO;
              escoposAninhados.obterEscopoAtual().adicionar(identificadorContext.ident1.getText(), etd);
            }
          }
        }
      }
    }

    return null;
  }
}
