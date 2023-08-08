package br.ufscar.dc.compiladores;

import org.antlr.v4.runtime.Token;

import br.ufscar.dc.compiladores.la.semantico.Escopos;
import br.ufscar.dc.compiladores.la.semantico.TabelaDeSimbolos;
import br.ufscar.dc.compiladores.la.semantico.laParser;
import br.ufscar.dc.compiladores.la.semantico.laParser.IdentificadorContext;
import br.ufscar.dc.compiladores.la.semantico.TabelaDeSimbolos.TipoVariavel;

public class VerificadorTipos {
  Escopos escoposAninhados;

  public VerificadorTipos(Escopos escoposAninhados) {
    this.escoposAninhados = escoposAninhados;
  }

  // verifica compatibilidade
  public static boolean isIncompativel(TipoVariavel retorno, TipoVariavel tipoTermo) {
    if (retorno == TipoVariavel.INVALIDO || tipoTermo == TipoVariavel.INVALIDO) {
      return true;
    }
    if (retorno == TipoVariavel.INTEIRO && tipoTermo == TipoVariavel.REAL) {
      return false;
    }
    if (retorno == TipoVariavel.REAL && tipoTermo == TipoVariavel.INTEIRO) {
      return false;
    }
    return (retorno != tipoTermo);
  }

  // verifica tipo de expressão verificando o tipo de seus termos de forma
  // recursiva
  public TipoVariavel verificaTipo(laParser.ExpressaoContext ctx) {
    TipoVariavel retorno = null;
    for (laParser.Termo_logicoContext termo : ctx.termo_logico()) {
      if (retorno == null) {
        retorno = verificaTipo(termo);
      } else {
        TipoVariavel tipoTermo = verificaTipo(termo);
        if (isIncompativel(retorno, tipoTermo)) {
          retorno = TipoVariavel.INVALIDO;
        }
      }
    }
    return retorno;
  }

  // verifica tipo de termo verificando o tipo de seus fatores de forma recursiva
  public TipoVariavel verificaTipo(laParser.Termo_logicoContext ctx) {
    TipoVariavel retorno = null;
    ;
    for (laParser.Fator_logicoContext fator : ctx.fator_logico()) {
      if (retorno == null) {
        retorno = verificaTipo(fator.parcela_logica());
      } else {
        TipoVariavel tipoTermo = verificaTipo(fator.parcela_logica());
        if (isIncompativel(retorno, tipoTermo)) {
          retorno = TipoVariavel.INVALIDO;
        }
      }
    }
    return retorno;
  }

  // verifica tipo de parcela_logica verificando o tipo de sua exp_relacional de
  // forma recursiva
  public TipoVariavel verificaTipo(laParser.Parcela_logicaContext ctx) {
    if (ctx.exp_relacional() != null) {
      return verificaTipo(ctx.exp_relacional());
    } else {
      return TipoVariavel.LOGICO;
    }
  }

  // verifica tipo de exp_relacional verificando o tipo de sua exp_arirmetica de
  // forma recursiva
  public TipoVariavel verificaTipo(laParser.Exp_relacionalContext ctx) {
    if (ctx.exp_aritmetica().size() > 1) {
      // #todo aqui pode existir um erro de tipos dentro de expressao aritmética
      // que seria causado por expressão aritméticas incompatíveis
      return TipoVariavel.LOGICO;
    }
    return verificaTipo(ctx.exp_aritmetica().get(0));
  }

  // verifica tipo de exp_arirmetica verificando o tipo de seus termos de forma
  // recursiva
  public TipoVariavel verificaTipo(laParser.Exp_aritmeticaContext ctx) {
    TipoVariavel retorno = null;
    for (laParser.TermoContext termo : ctx.termo()) {
      if (retorno == null) {
        retorno = verificaTipo(termo);
      } else {
        TipoVariavel tipoTermo = verificaTipo(termo);
        if (isIncompativel(retorno, tipoTermo)) {
          retorno = TipoVariavel.INVALIDO;
        }
      }
    }
    return retorno;
  }

  // verifica tipo de termo verificando o tipo de seus fatores de forma recursiva
  public TipoVariavel verificaTipo(laParser.TermoContext ctx) {
    TipoVariavel retorno = null;
    for (laParser.FatorContext fator : ctx.fator()) {
      if (retorno == null) {
        retorno = verificaTipo(fator);
      } else {
        TipoVariavel tipoTermo = verificaTipo(fator);
        if (isIncompativel(retorno, tipoTermo)) {
          retorno = TipoVariavel.INVALIDO;
        }
      }
    }
    return retorno;
  }

  // verifica tipo de fator verificando o tipo de suas parcelas de forma recursiva
  public TipoVariavel verificaTipo(laParser.FatorContext ctx) {
    TipoVariavel retorno = null;
    for (laParser.ParcelaContext parc : ctx.parcela()) {
      if (retorno == null) {
        retorno = verificaTipo(parc);
      } else {
        TipoVariavel tipoTermo = verificaTipo(parc);
        if (isIncompativel(retorno, tipoTermo)) {
          retorno = TipoVariavel.INVALIDO;
        }
      }
    }
    return retorno;
  }

  // verifica tipo de parcela verificando o tipo de sua parcela unária ou não
  // unária de forma recursiva
  public TipoVariavel verificaTipo(laParser.ParcelaContext ctx) {
    if (ctx.parcela_unario() != null) {
      return verificaTipo(ctx.parcela_unario());
    } else {
      return verificaTipo(ctx.parcela_nao_unario());
    }
  }

  // verifica tipo de parcela unária
  public TipoVariavel verificaTipo(laParser.Parcela_unarioContext ctx) {
    if (ctx.identificador() != null) {
      for (TabelaDeSimbolos escopo : escoposAninhados.percorrerEscoposAninhados()) {
        ctx.identificador();
        if (escopo.verificar(IdentificadorContext.ident1.getText()) != null) {
          ctx.identificador();
          if (escopo
              .verificar(IdentificadorContext.ident1.getText()).tipoEntrada != TabelaDeSimbolos.TipoEntradaTds.REGISTRO
              && escopo.verificar(ctx.identificador().ident1.getText()).subTabela == null) {
            return escopo.verificar(ctx.identificador().ident1.getText()).tipoObjeto;
          } else {
            // se o identificador mais a esquerda for um registro
            TabelaDeSimbolos tds = escopo.verificar(ctx.identificador().ident1.getText()).subTabela;
            // se for um campo dentro do um registro, pega o tipo desse campo
            if (ctx.identificador().subIdent.size() > 0) {
              // #todo recursão em registro dentro de registro

              return tds.verificar(
                  ctx.identificador().subIdent.get(ctx.identificador().subIdent.size() - 1).getText()).tipoObjeto;
            }
            // se não, retorna que é um registro
            return escopo.verificar(ctx.identificador().ident1.getText()).tipoObjeto;
          }
        }
        return TipoVariavel.INVALIDO;
      }
    } else if (ctx.IDENT() != null) {
      // se for função, retorna o tipo de retorno da função
      for (TabelaDeSimbolos escopo : escoposAninhados.percorrerEscoposAninhados()) {
        if (escopo.verificar(ctx.IDENT().getText()) != null) {

          return escopo.verificar(ctx.IDENT().getText()).tipoObjeto;
        }
      }
      return TipoVariavel.INVALIDO;
    } else if (ctx.expNaoParametro != null) {
      return verificaTipo(ctx.expNaoParametro);
    } else if (ctx.NUM_INT() != null) {
      return TipoVariavel.INTEIRO;
    } else {
      return TipoVariavel.REAL;
    }
    return TipoVariavel.INVALIDO;
  }

  // verifica tipo de parcela não unária
  public TipoVariavel verificaTipo(laParser.Parcela_nao_unarioContext ctx) {
    if (ctx.CADEIA() != null) {
      return TipoVariavel.LITERAL;
    } else {
      TipoVariavel tipo = TipoVariavel.INVALIDO;
      for (TabelaDeSimbolos escopo : escoposAninhados.percorrerEscoposAninhados()) {
        if (escopo.verificar(ctx.identificador().ident1.getText()) != null) {
          // #todo pegar tipo do subcampo do registro
          tipo = escopo.verificar(ctx.identificador().ident1.getText()).tipoObjeto;
          break;
        }
      }
      switch (tipo) {
        case INTEIRO:
          return TipoVariavel.PONTEIRO_INTEIRO;
        case LITERAL:
          return TipoVariavel.PONTEIRO_LITERAL;
        case LOGICO:
          return TipoVariavel.PONTEIRO_LOGICO;
        case REAL:
          return TipoVariavel.PONTEIRO_REAL;
        default:
          return TipoVariavel.INVALIDO;
      }
    }
  }
}
