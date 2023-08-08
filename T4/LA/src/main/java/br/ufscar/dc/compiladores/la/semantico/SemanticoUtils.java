package br.ufscar.dc.compiladores.la.semantico;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.Token;
import br.ufscar.dc.compiladores.la.semantico.TabelaDeSimbolos.TipoVariavel;

public class SemanticoUtils {

  public static List<String> errosSemanticos = new ArrayList<>();

  public static void adicionarErroSemantico(Token t, String mensagem) {
    int linha = t.getLine();
    errosSemanticos.add(String.format("Linha %d: %s", linha, mensagem));
  }

  public static TabelaDeSimbolos.TipoVariavel verificarTipo(String strTipoVar, boolean isPointer) {
    TabelaDeSimbolos.TipoVariavel tipoVar = TipoVariavel.INVALIDO;
    switch (strTipoVar) {
      case "literal":
        if (isPointer) {
          tipoVar = TipoVariavel.PONTEIRO_LITERAL;
        } else {
          tipoVar = TipoVariavel.LITERAL;
        }
        break;
      case "inteiro":
        if (isPointer) {
          tipoVar = TipoVariavel.PONTEIRO_INTEIRO;
        } else {
          tipoVar = TipoVariavel.INTEIRO;
        }
        break;
      case "real":
        if (isPointer) {
          tipoVar = TipoVariavel.PONTEIRO_REAL;
        } else {
          tipoVar = TipoVariavel.REAL;
        }
        break;
      case "logico":
        if (isPointer) {
          tipoVar = TipoVariavel.PONTEIRO_LOGICO;
        } else {
          tipoVar = TipoVariavel.LOGICO;
        }
        break;
      default:
        break;
    }
    return tipoVar;
  }

}
