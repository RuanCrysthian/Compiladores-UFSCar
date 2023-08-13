package br.ufscar.dc.compiladores.la.semantico;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;

import br.ufscar.dc.compiladores.la.semantico.SymbolTable.TypeLAIdentifier;
import br.ufscar.dc.compiladores.la.semantico.SymbolTable.TypeLAVariable;
import br.ufscar.dc.compiladores.la.semantico.laParser.CmdAtribuicaoContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.CmdCasoContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.CmdContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.CmdEnquantoContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.CmdEscrevaContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.CmdFacaContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.CmdLeiaContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.CmdParaContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.CmdRetorneContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.CmdSeContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.Declaracao_globalContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.Exp_aritmeticaContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.Exp_relacionalContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.ExpressaoContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.FatorContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.Fator_logicoContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.IdentificadorContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.Item_selecaoContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.ParcelaContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.Parcela_logicaContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.Parcela_nao_unarioContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.Parcela_unarioContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.SelecaoContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.TermoContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.Termo_logicoContext;
import br.ufscar.dc.compiladores.la.semantico.laParser.VariavelContext;

public class LaC extends laBaseVisitor<Void> {
  Scope nestedScopes = new Scope();
  SymbolTable symbolTable;

  public StringBuilder output;

  public LaC() {
    output = new StringBuilder();
    this.symbolTable = new SymbolTable();
  }

  @Override
  public Void visitPrograma(laParser.ProgramaContext ctx) {
    appendHeaders();
    visitDeclaracoesLocaisGlobais(ctx.declaracoes().decl_local_global());
    output.append("\n");
    output.append("int main() {\n");
    visitDeclaracoesLocais(ctx.corpo().declaracao_local());
    visitComandos(ctx.corpo().cmd());
    output.append("return 0;\n");
    output.append("}\n");
    return null;
  }

  private void appendHeaders() {
    output.append("#include <stdio.h>\n");
    output.append("#include <stdlib.h>\n");
  }

  private void visitDeclaracoesLocaisGlobais(List<laParser.Decl_local_globalContext> decls) {
    decls.forEach(this::visitDecl_local_global);
  }

  private void visitDeclaracoesLocais(List<laParser.Declaracao_localContext> decls) {
    decls.forEach(this::visitDeclaracao_local);
  }

  private void visitComandos(List<laParser.CmdContext> comandos) {
    comandos.forEach(this::visitCmd);
  }

  public static String getCType(SymbolTable.TypeLAVariable val) {
    switch (val) {
      case LITERAL:
        return "char";
      case INTEIRO:
        return "int";
      case REAL:
        return "float";
      default:
        return null;
    }
  }

  public static String getCTypeSymbol(SymbolTable.TypeLAVariable val) {
    switch (val) {
      case LITERAL:
        return "s";
      case INTEIRO:
        return "d";
      case REAL:
        return "f";
      default:
        return null;
    }
  }

  Boolean defineTypeAndAddtoScope(String variableIdentifier, String variableType, SymbolTable symbolTable) {
    String declaration;
    SymbolTable.TypeLAIdentifier identifierType;

    switch (variableType) {
      case "inteiro":
        declaration = "int " + variableIdentifier + ";\n";
        identifierType = TypeLAIdentifier.VARIAVEL;
        break;
      case "literal":
        declaration = "char " + variableIdentifier + "[80];\n";
        identifierType = TypeLAIdentifier.VARIAVEL;
        break;
      case "real":
        declaration = "float " + variableIdentifier + ";\n";
        identifierType = TypeLAIdentifier.VARIAVEL;
        break;
      case "logico":
        declaration = "boolean " + variableIdentifier + ";\n";
        identifierType = TypeLAIdentifier.VARIAVEL;
        break;
      case "^logico":
        declaration = "boolean* " + variableIdentifier + ";\n";
        identifierType = TypeLAIdentifier.VARIAVEL;
        break;
      case "^real":
        declaration = "float* " + variableIdentifier + ";\n";
        identifierType = TypeLAIdentifier.VARIAVEL;
        break;
      case "^literal":
        declaration = "char* " + variableIdentifier + "[80];\n";
        identifierType = TypeLAIdentifier.VARIAVEL;
        break;
      case "^inteiro":
        declaration = "int* " + variableIdentifier + ";\n";
        identifierType = TypeLAIdentifier.VARIAVEL;
        break;
      default:
        return false;
    }

    output.append("        " + declaration);
    symbolTable.put(variableIdentifier, identifierType, getTypeLAVariable(variableType));
    return true;
  }

  private SymbolTable.TypeLAVariable getTypeLAVariable(String variableType) {
    switch (variableType) {
      case "inteiro":
        return TypeLAVariable.INTEIRO;
      case "literal":
        return TypeLAVariable.LITERAL;
      case "real":
        return TypeLAVariable.REAL;
      case "logico":
        return TypeLAVariable.LOGICO;
      case "^logico":
        return TypeLAVariable.PONT_LOGI;
      case "^real":
        return TypeLAVariable.PONT_REAL;
      case "^literal":
        return TypeLAVariable.PONT_LITE;
      case "^inteiro":
        return TypeLAVariable.PONT_INTE;
      default:
        return null;
    }
  }

  @Override
  public Void visitDeclaracao_local(laParser.Declaracao_localContext ctx) {
    if (ctx.IDENT() != null) {
      String identifier = ctx.IDENT().getText();
      SymbolTable currentScope = nestedScopes.getCurrentScope();

      if (ctx.tipo_basico() != null) {
        output.append("#define " + identifier + " " + ctx.valor_constante().getText());
        String constantType = ctx.tipo_basico().getText();
        switch (constantType) {
          case "inteiro":
            currentScope.put(identifier, SymbolTable.TypeLAIdentifier.CONSTANTE,
                TypeLAVariable.INTEIRO);
            break;
          case "literal":
            currentScope.put(identifier, SymbolTable.TypeLAIdentifier.CONSTANTE,
                TypeLAVariable.LITERAL);
            break;
          case "real":
            currentScope.put(identifier, SymbolTable.TypeLAIdentifier.CONSTANTE,
                TypeLAVariable.REAL);
            break;
          case "logico":
            currentScope.put(identifier, SymbolTable.TypeLAIdentifier.CONSTANTE,
                TypeLAVariable.LOGICO);
            break;
          default:
            break;
        }

      } else {
        var fieldsType = new SymbolTable();
        currentScope.put(identifier, SymbolTable.TypeLAIdentifier.TIPO, null, fieldsType);

        output.append("    typedef struct {\n");
        for (VariavelContext variable : ctx.tipo().registro().variavel()) {
          for (IdentificadorContext ctxIdentVariable : variable.identificador()) {
            String variableIdentifier = ctxIdentVariable.getText();
            String variableType = variable.tipo().getText();
            defineTypeAndAddtoScope(variableIdentifier, variableType, fieldsType);
          }
        }
        output.append("    } " + identifier + ";\n");
      }
    } else {
      if (ctx.variavel().tipo().registro() == null) {
        for (IdentificadorContext ctxIdentVariable : ctx.variavel().identificador()) {
          String variableIdentifier = "";
          for (TerminalNode ident : ctxIdentVariable.IDENT())
            variableIdentifier += ident.getText();
          SymbolTable currentScope = nestedScopes.getCurrentScope();
          String variableType = ctx.variavel().tipo().getText();

          if (!defineTypeAndAddtoScope(variableIdentifier, variableType, currentScope)) {
            SymbolTableEntry entry = currentScope.check(variableType);
            SymbolTable fieldsType = entry.argsRegFunc;
            currentScope.put(variableIdentifier,
                SymbolTable.TypeLAIdentifier.REGISTRO, null, fieldsType);
            output.append("    " + variableType + " " + ctxIdentVariable.getText() + ";\n");
          }
        }
      } else {
        output.append("    struct {\n");
        ArrayList<String> registerIdentifiers = new ArrayList<>();
        for (var ctxIdentReg : ctx.variavel().identificador()) {
          String identifierName = ctxIdentReg.getText();
          SymbolTable currentScope = nestedScopes.getCurrentScope();
          SymbolTable fields = new SymbolTable();
          currentScope.put(identifierName, SymbolTable.TypeLAIdentifier.REGISTRO, null,
              fields);
          registerIdentifiers.add(ctxIdentReg.getText());
        }
        boolean lock = false;
        for (VariavelContext ctxVariableRegister : ctx.variavel().tipo().registro().variavel()) {
          for (IdentificadorContext ctxVariableRegisterIdent : ctxVariableRegister.identificador()) {
            lock = false;
            String registerFieldName = ctxVariableRegisterIdent.getText();
            SymbolTable currentScope = nestedScopes.getCurrentScope();

            for (String registerIdentifier : registerIdentifiers) {
              SymbolTableEntry entry = currentScope.check(registerIdentifier);
              SymbolTable registerFields = entry.argsRegFunc;

              String variableType = ctxVariableRegister.tipo().getText();
              if (!lock) {
                defineTypeAndAddtoScope(registerFieldName, variableType, registerFields);
              }
            }
            lock = true;
          }
        }
        output.append("    }");
        for (String registerIdentifier : registerIdentifiers) {
          output.append(registerIdentifier);
        }
        output.append(";\n");
      }
    }
    return null;
  }

  @Override
  public Void visitDeclaracao_global(Declaracao_globalContext ctx) {
    String identifier = ctx.IDENT().getText();
    List<SymbolTable> scopes = nestedScopes.runNestedScopes();
    if (scopes.size() > 1) {
      nestedScopes.giveupScope();
    }
    SymbolTable globalScope = nestedScopes.getCurrentScope();
    if (ctx.tipo_estendido() != null) {
      nestedScopes.createNewScope();
      SymbolTable functionScope = nestedScopes.getCurrentScope();
      functionScope.setGlobal(globalScope);

      var returnType = ctx.tipo_estendido().getText();

      defineTypeAndAddtoScope(identifier, returnType, functionScope);
      output.append("(");

      boolean firstParameter = true;
      for (laParser.ParametroContext declaredParameter : ctx.parametros().parametro()) {
        String variableType = declaredParameter.tipo_estendido().getText();

        for (laParser.IdentificadorContext ident : declaredParameter.identificador()) {
          String parameterIdentifier = ident.getText();

          if (!firstParameter) {
            output.append(",");
          }
          switch (variableType) {
            case "inteiro":
              functionScope.put(parameterIdentifier,
                  SymbolTable.TypeLAIdentifier.VARIAVEL,
                  SymbolTable.TypeLAVariable.INTEIRO);
              output.append("int " + parameterIdentifier);
              break;
            case "literal":
              functionScope.put(parameterIdentifier,
                  SymbolTable.TypeLAIdentifier.VARIAVEL,
                  SymbolTable.TypeLAVariable.LITERAL);
              output.append("char* " + parameterIdentifier);
              break;
            case "real":
              functionScope.put(parameterIdentifier,
                  SymbolTable.TypeLAIdentifier.VARIAVEL,
                  SymbolTable.TypeLAVariable.REAL);
              output.append("float " + parameterIdentifier);
              break;
            case "logico":
              functionScope.put(parameterIdentifier,
                  SymbolTable.TypeLAIdentifier.VARIAVEL,
                  SymbolTable.TypeLAVariable.LOGICO);
              output.append("boolean " + parameterIdentifier);
              break;
            case "^logico":
              functionScope.put(parameterIdentifier,
                  SymbolTable.TypeLAIdentifier.VARIAVEL,
                  SymbolTable.TypeLAVariable.PONT_LOGI);
              output.append("boolean* " + parameterIdentifier);
              break;
            case "^real":
              functionScope.put(parameterIdentifier,
                  SymbolTable.TypeLAIdentifier.VARIAVEL,
                  SymbolTable.TypeLAVariable.PONT_REAL);
              output.append("float* " + parameterIdentifier);
              break;
            case "^literal":
              functionScope.put(parameterIdentifier,
                  SymbolTable.TypeLAIdentifier.VARIAVEL,
                  SymbolTable.TypeLAVariable.PONT_LITE);
              output.append("boolean* " + parameterIdentifier);
            case "^inteiro":
              functionScope.put(parameterIdentifier,
                  SymbolTable.TypeLAIdentifier.VARIAVEL,
                  SymbolTable.TypeLAVariable.PONT_INTE);
              output.append("int* " + parameterIdentifier);
            default:
              if (globalScope.exists(variableType) && globalScope.check(
                  variableType).identifierType == SymbolTable.TypeLAIdentifier.TIPO) {
                SymbolTableEntry fields = globalScope.check(variableType);
                SymbolTable nestedTableType = fields.argsRegFunc;

                functionScope.put(parameterIdentifier,
                    SymbolTable.TypeLAIdentifier.REGISTRO,
                    SymbolTable.TypeLAVariable.REGISTRO, nestedTableType,
                    variableType);
              }
              break;
          }
          firstParameter = false;

        }
      }
      output.append(") {\n");

    } else {
      nestedScopes.createNewScope();
      SymbolTable procScope = nestedScopes.getCurrentScope();
      procScope.setGlobal(globalScope);

      output.append("void " + identifier + "(");
      boolean firstParameter = true;

      for (laParser.ParametroContext declaredParameter : ctx.parametros().parametro()) {
        String variableType = declaredParameter.tipo_estendido().getText();

        for (laParser.IdentificadorContext ident : declaredParameter.identificador()) {
          String parameterIdentifier = ident.getText();

          if (!firstParameter) {
            output.append(",");
          }
          switch (variableType) {
            case "inteiro":
              procScope.put(parameterIdentifier,
                  SymbolTable.TypeLAIdentifier.VARIAVEL,
                  SymbolTable.TypeLAVariable.INTEIRO);
              output.append("int " + parameterIdentifier);
              break;
            case "literal":
              procScope.put(parameterIdentifier,
                  SymbolTable.TypeLAIdentifier.VARIAVEL,
                  SymbolTable.TypeLAVariable.LITERAL);
              output.append("char* " + parameterIdentifier);
              break;
            case "real":
              procScope.put(parameterIdentifier,
                  SymbolTable.TypeLAIdentifier.VARIAVEL,
                  SymbolTable.TypeLAVariable.REAL);
              output.append("float " + parameterIdentifier);
              break;
            case "logico":
              procScope.put(parameterIdentifier,
                  SymbolTable.TypeLAIdentifier.VARIAVEL,
                  SymbolTable.TypeLAVariable.LOGICO);
              output.append("boolean " + parameterIdentifier);
              break;
            case "^logico":
              procScope.put(parameterIdentifier,
                  SymbolTable.TypeLAIdentifier.VARIAVEL,
                  SymbolTable.TypeLAVariable.PONT_LOGI);
              output.append("boolean* " + parameterIdentifier);
              break;
            case "^real":
              procScope.put(parameterIdentifier,
                  SymbolTable.TypeLAIdentifier.VARIAVEL,
                  SymbolTable.TypeLAVariable.PONT_REAL);
              output.append("float* " + parameterIdentifier);
              break;
            case "^literal":
              procScope.put(parameterIdentifier,
                  SymbolTable.TypeLAIdentifier.VARIAVEL,
                  SymbolTable.TypeLAVariable.PONT_LITE);
              output.append("boolean* " + parameterIdentifier);
            case "^inteiro":
              procScope.put(parameterIdentifier,
                  SymbolTable.TypeLAIdentifier.VARIAVEL,
                  SymbolTable.TypeLAVariable.PONT_INTE);
              output.append("int* " + parameterIdentifier);
            default:
              if (globalScope.exists(variableType) && globalScope.check(
                  variableType).identifierType == SymbolTable.TypeLAIdentifier.TIPO) {
                SymbolTableEntry fields = globalScope.check(variableType);
                SymbolTable nestedTableType = fields.argsRegFunc;

                procScope.put(parameterIdentifier,
                    SymbolTable.TypeLAIdentifier.REGISTRO,
                    SymbolTable.TypeLAVariable.REGISTRO, nestedTableType,
                    variableType);
              }
              break;
          }
          firstParameter = false;
        }
      }
      output.append(") {\n");

    }
    ctx.cmd().forEach(cmd -> visitCmd(cmd));
    output.append("}\n");

    return null;
  }

  @Override
  public Void visitCmdChamada(laParser.CmdChamadaContext ctx) {
    output.append("    " + ctx.getText() + ";\n");

    return null;
  }

  @Override
  public Void visitCmdAtribuicao(CmdAtribuicaoContext ctx) {
    var currentScope = nestedScopes.getCurrentScope();

    String[] atribuition = ctx.getText().split("<-");

    try {
      if (atribuition[0].contains("^")) {
        output.append("*");
      }
      SymbolTable.TypeLAVariable variableType = LaUtils.verifyType(currentScope, ctx.identificador());

      if (variableType == SymbolTable.TypeLAVariable.LITERAL) {
        output.append("strcpy(");
        visitIdentificador(ctx.identificador());
        output.append("," + ctx.expressao().getText() + ");\n");
      } else {
        visitIdentificador(ctx.identificador());
        output.append(" = ");
        output.append(ctx.expressao().getText());
        output.append(";\n");
      }
    } catch (Exception e) {
      output.append(e.getMessage());
    }

    return null;
  }

  @Override
  public Void visitCmdLeia(CmdLeiaContext ctx) {
    SymbolTable currentScope = nestedScopes.getCurrentScope();

    for (IdentificadorContext id : ctx.identificador()) {
      handleLeiaIdentifier(id, currentScope);
    }

    return null;
  }

  private void handleLeiaIdentifier(IdentificadorContext id, SymbolTable currentScope) {
    SymbolTable.TypeLAVariable variableType = currentScope.check(id.getText()).variableType;

    if (variableType != SymbolTable.TypeLAVariable.LITERAL) {
      output.append("scanf(\"%" + getCTypeSymbol(variableType) + "\", &" + id.getText() + ");\n");
    } else {
      output.append("gets(");
      visitIdentificador(id);
      output.append(");\n");
    }
  }

  @Override
  public Void visitCmdEscreva(CmdEscrevaContext ctx) {
    for (ExpressaoContext exp : ctx.expressao()) {
      SymbolTable currentScope = nestedScopes.getCurrentScope();
      SymbolTable.TypeLAVariable variableType = LaUtils.verifyType(currentScope, exp);
      String cType = getCTypeSymbol(variableType);
      if (currentScope.exists(exp.getText())) {
        variableType = currentScope.check(exp.getText()).variableType;
        cType = getCTypeSymbol(variableType);
      }
      output.append("printf(\"%" + cType + "\", " + exp.getText() + ");\n");
    }
    return null;
  }

  @Override
  public Void visitCorpo(laParser.CorpoContext ctx) {
    List<SymbolTable> scopes = nestedScopes.runNestedScopes();
    if (scopes.size() > 1) {
      nestedScopes.giveupScope();
    }

    return super.visitCorpo(ctx);
  }

  @Override
  public Void visitCmdRetorne(CmdRetorneContext ctx) {
    output.append("return ");
    visitExpressao(ctx.expressao());
    output.append(";\n");
    return null;
  }

  @Override
  public Void visitCmdSe(CmdSeContext ctx) {
    output.append("if (");
    visitExpressao(ctx.expressao());
    output.append(") {\n");

    for (CmdContext cmd : ctx.cmd()) {
      visitCmd(cmd);
    }

    output.append("}\n");

    if (ctx.getText().contains("senao")) {
      output.append("else {\n");

      for (CmdContext cmd : ctx.cmdElse) {
        visitCmd(cmd);
      }

      output.append("}\n");
    }

    return null;
  }

  @Override
  public Void visitExpressao(ExpressaoContext ctx) {
    if (ctx.termo_logico() != null) {
      visitTermo_logico(ctx.termo_logico(0));

      for (int i = 1; i < ctx.termo_logico().size(); i++) {
        output.append(" || ");
        visitTermo_logico(ctx.termo_logico(i));
      }
    }

    return null;
  }

  @Override
  public Void visitTermo_logico(Termo_logicoContext ctx) {
    visitFator_logico(ctx.fator_logico(0));

    for (int i = 1; i < ctx.fator_logico().size(); i++) {
      output.append(" && ");
      visitFator_logico(ctx.fator_logico(i));
    }

    return null;
  }

  @Override
  public Void visitFator_logico(Fator_logicoContext ctx) {
    if (ctx.getText().startsWith("nao")) {
      output.append("!");
    }

    visitParcela_logica(ctx.parcela_logica());

    return null;
  }

  @Override
  public Void visitParcela_logica(Parcela_logicaContext ctx) {
    if (ctx.exp_relacional() != null) {
      visitExp_relacional(ctx.exp_relacional());
    } else {
      if (ctx.getText().equals("verdadeiro")) {
        output.append("true");
      } else {
        output.append("false");
      }
    }

    return null;
  }

  @Override
  public Void visitExp_relacional(Exp_relacionalContext ctx) {
    visitExp_aritmetica(ctx.exp_aritmetica(0));

    for (int i = 1; i < ctx.exp_aritmetica().size(); i++) {
      output.append(" ");
      if (ctx.op_relacional().getText().equals("=")) {
        output.append("==");
      } else {
        output.append(ctx.op_relacional().getText());
      }
      output.append(" ");
      visitExp_aritmetica(ctx.exp_aritmetica(i));
    }

    return null;
  }

  @Override
  public Void visitExp_aritmetica(Exp_aritmeticaContext ctx) {
    visitTermo(ctx.termo(0));

    for (int i = 1; i < ctx.termo().size(); i++) {
      output.append(" ");
      output.append(ctx.op1(i - 1).getText());
      output.append(" ");
      visitTermo(ctx.termo(i));
    }

    return null;
  }

  @Override
  public Void visitTermo(TermoContext ctx) {
    visitFator(ctx.fator(0));

    for (int i = 1; i < ctx.fator().size(); i++) {
      output.append(" ");
      output.append(ctx.op2(i - 1).getText());
      output.append(" ");
      visitFator(ctx.fator(i));
    }

    return null;
  }

  @Override
  public Void visitFator(FatorContext ctx) {
    visitParcela(ctx.parcela(0));

    for (int i = 1; i < ctx.parcela().size(); i++) {
      output.append(" ");
      output.append(ctx.op3(i - 1).getText());
      output.append(" ");
      visitParcela(ctx.parcela(i));
    }

    return null;
  }

  @Override
  public Void visitParcela(ParcelaContext ctx) {
    if (ctx.parcela_unario() != null) {
      if (ctx.op_unario() != null) {
        output.append(ctx.op_unario().getText());
      }
      visitParcela_unario(ctx.parcela_unario());
    } else {
      visitParcela_nao_unario(ctx.parcela_nao_unario());
    }

    return null;
  }

  @Override
  public Void visitParcela_unario(Parcela_unarioContext ctx) {
    if (ctx.IDENT() != null) {
      output.append(ctx.IDENT().getText());
      output.append("(");
      for (int i = 0; i < ctx.expressao().size(); i++) {
        visitExpressao(ctx.expressao(i));
        if (i < ctx.expressao().size() - 1) {
          output.append(", ");
        }
      }
      output.append(")");
    } else if (ctx.AP() != null) {
      output.append("(");
      ctx.expressao().forEach(exp -> {
        visitExpressao(exp);
        if (exp != ctx.expressao(ctx.expressao().size() - 1)) {
          output.append(", ");
        }
      });
      output.append(")");
    } else {
      output.append(ctx.getText());
    }

    return null;
  }

  @Override
  public Void visitParcela_nao_unario(Parcela_nao_unarioContext ctx) {
    output.append(ctx.getText());
    return null;
  }

  @Override
  public Void visitCmdCaso(CmdCasoContext ctx) {
    output.append("switch (");
    visit(ctx.exp_aritmetica());
    output.append(") {\n");
    visit(ctx.selecao());

    if (ctx.getText().contains("senao")) {
      output.append("    default:\n");
      ctx.cmd().forEach(cmd -> visitCmd(cmd));
      output.append("    }\n");
    }

    return null;
  }

  @Override
  public Void visitSelecao(SelecaoContext ctx) {
    ctx.item_selecao().forEach(var -> visitItem_selecao(var));
    return null;
  }

  @Override
  public Void visitItem_selecao(Item_selecaoContext ctx) {
    String[] intervalo = ctx.constantes().getText().split("\\.\\.");
    int first = Integer.parseInt(intervalo[0]);
    int last = intervalo.length > 1 ? Integer.parseInt(intervalo[1]) : first;

    for (int i = first; i <= last; i++) {
      output.append("case " + i + ":\n");
      ctx.cmd().forEach(var -> visitCmd(var));
      output.append("    break;\n");
    }

    return null;
  }

  @Override
  public Void visitCmdPara(CmdParaContext ctx) {
    String id = ctx.IDENT().getText();

    output.append("for (" + id + " = ");
    visitExp_aritmetica(ctx.exp_aritmetica(0));
    output.append("; " + id + " <= ");
    visitExp_aritmetica(ctx.exp_aritmetica(1));
    output.append("; " + id + "++) {\n");

    ctx.cmd().forEach(var -> visitCmd(var));

    output.append("}\n");

    return null;
  }

  @Override
  public Void visitCmdEnquanto(CmdEnquantoContext ctx) {
    output.append("while(");
    visitExpressao(ctx.expressao());
    output.append("){\n");
    ctx.cmd().forEach(var -> visitCmd(var));
    output.append("}\n");
    return null;
  }

  @Override
  public Void visitCmdFaca(CmdFacaContext ctx) {
    output.append("do{\n");
    ctx.cmd().forEach(var -> visitCmd(var));
    output.append("} while(");
    visitExpressao(ctx.expressao());
    output.append(");\n");
    return null;
  }

  @Override
  public Void visitIdentificador(IdentificadorContext ctx) {
    output.append(" ");
    int i = 0;
    for (TerminalNode id : ctx.IDENT()) {
      if (i++ > 0)
        output.append(".");
      output.append(id.getText());
    }
    visitDimensao(ctx.dimensao());
    return null;
  }
}
