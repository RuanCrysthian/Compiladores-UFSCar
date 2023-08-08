package br.ufscar.dc.compiladores.la.semantico;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.Token;

public class LaUtils {
    // Creating a list to store semantic errors
    public static List<String> semanticErrors = new ArrayList<>();

    // Adding a semantic error to the list. It takes a Token and a message as parameters,
    // retrieves the line number from the token, and adds the formatted error to the list.
    public static void addSemanticError(Token t, String msg) {
        int line = t.getLine();
        semanticErrors.add(String.format("Linha %d: %s", line, msg));
    }

    // Verifying the type in the context of an identifier
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable symbolTable, laParser.IdentificadorContext ctx) {
        String identifier = ctx.getText();

        if (!symbolTable.exists(identifier)) {
            addSemanticError(ctx.IDENT(0).getSymbol(), "identificador " + identifier + " nao declarado\n");
            return SymbolTable.TypeLAVariable.NAO_DECLARADO;
        } else {
            SymbolTableEntry ident = symbolTable.check(identifier);
            return ident.variableType;
        }
    }

    // Verifying the type in context of an expression
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable symbolTable, laParser.ExpressaoContext ctx) {
        SymbolTable.TypeLAVariable ret = null;
        for (var tl : ctx.termo_logico()) {
            SymbolTable.TypeLAVariable aux = verifyType(symbolTable, tl);
            if (ret == null) {
                ret = aux;
            } else if (!verifyType(ret, aux)) {
                ret = SymbolTable.TypeLAVariable.INVALIDO;
            }
        }
        return ret;
    }

    // Verifying the type in context of a logical term
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable symbolTable, laParser.Termo_logicoContext ctx) {
        SymbolTable.TypeLAVariable ret = null;
        for (var fL : ctx.fator_logico()) {
            SymbolTable.TypeLAVariable aux = verifyType(symbolTable, fL);
            if (ret == null) {
                ret = aux;
            } else if (!verifyType(ret, aux)) {
                ret = SymbolTable.TypeLAVariable.INVALIDO;
            }
        }
        return ret;
    }

    // Verifying the type in context of a logical factor
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable table, laParser.Fator_logicoContext ctx) {
        return verifyType(table, ctx.parcela_logica());
    }

    // Verifying the type in context of a logical parcel
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable table, laParser.Parcela_logicaContext ctx) {
        if (ctx.exp_relacional() != null) {
            return verifyType(table, ctx.exp_relacional());
        } else {
            return SymbolTable.TypeLAVariable.LOGICO;
        }
    }

    // Verifying the type in context of a relational expression
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable table, laParser.Exp_relacionalContext ctx) {
        SymbolTable.TypeLAVariable ret = null;
        if (ctx.exp_aritmetica().size() == 1) {
            for (var ea : ctx.exp_aritmetica()) {
                var aux = verifyType(table, ea);
                if (ret == null) {
                    ret = aux;
                } else if (!verifyType(ret, aux)) {
                    ret = SymbolTable.TypeLAVariable.INVALIDO;
                }
            }
        } else {
            for (var ea : ctx.exp_aritmetica()) {
                verifyType(table, ea);
            }
            return SymbolTable.TypeLAVariable.LOGICO;
        }
        return ret;
    }

    // Verifying the type in context of an arithmetic expression
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable table, laParser.Exp_aritmeticaContext ctx) {
        SymbolTable.TypeLAVariable ret = null;
        for (var te : ctx.termo()) {
            var aux = verifyType(table, te);
            if (ret == null) {
                ret = aux;
            } else if (!verifyType(ret, aux)) {
                ret = SymbolTable.TypeLAVariable.INVALIDO;
            }
        }
        return ret;
    }

    // Verifying the type in context of a term
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable table, laParser.TermoContext ctx) {
        SymbolTable.TypeLAVariable ret = null;
        for (var fa : ctx.fator()) {
            var aux = verifyType(table, fa);
            if (ret == null) {
                ret = aux;
            } else if (!verifyType(ret, aux)) {
                ret = SymbolTable.TypeLAVariable.INVALIDO;
            }
        }
        return ret;
    }

    // Verifying the type in context of a factor
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable table, laParser.FatorContext ctx) {
        SymbolTable.TypeLAVariable ret = null;
        for (var pa : ctx.parcela()) {
            var aux = verifyType(table, pa);
            if (ret == null) {
                ret = aux;
            } else if (!verifyType(ret, aux)) {
                ret = SymbolTable.TypeLAVariable.INVALIDO;
            }
        }
        return ret;
    }

    // Verifying the type in context of a parcel
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable table, laParser.ParcelaContext ctx) {
        if (ctx.parcela_unario() != null) {
            return verifyType(table, ctx.parcela_unario());
        } else {
            return verifyType(table, ctx.parcela_nao_unario());
        }
    }

    // Verifying the type in context of a unary parcel
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable symbolTable, laParser.Parcela_unarioContext ctx) {
        if (ctx.NUM_INT() != null) {
            return SymbolTable.TypeLAVariable.INTEIRO;
        } else if (ctx.NUM_REAL() != null) {
            return SymbolTable.TypeLAVariable.REAL;
        } else if (ctx.IDENT() != null) {
            // function
            String identifier = ctx.IDENT().getText();
            if (!symbolTable.exists(identifier)) {
                addSemanticError(ctx.IDENT().getSymbol(), "identificador " + identifier + " nao declarado\n");
                return SymbolTable.TypeLAVariable.NAO_DECLARADO;
            }

            for (var exp : ctx.expressao()) {
                var aux = verifyType(symbolTable, exp);
                if (!verifyType(SymbolTable.TypeLAVariable.LOGICO, aux)) {
                    return SymbolTable.TypeLAVariable.INVALIDO;
                }
            }
            return symbolTable.check(identifier).variableType;
        }

        if (ctx.identificador() != null) {
            return verifyType(symbolTable, ctx.identificador());
        }

        if (ctx.IDENT() == null && ctx.expressao() != null) {
            for (var exp : ctx.expressao()) {
                return verifyType(symbolTable, exp);
            }
        }

        return null;
    }

    // Verifying if the assignment types are valid
    public static boolean verifyType(SymbolTable.TypeLAVariable tipo1, SymbolTable.TypeLAVariable tipo2) {
        if (tipo1 == tipo2 || tipo1 == SymbolTable.TypeLAVariable.NAO_DECLARADO || tipo2 == SymbolTable.TypeLAVariable.NAO_DECLARADO) {
            return true;
        }

        if (tipo1 == SymbolTable.TypeLAVariable.INVALIDO || tipo2 == SymbolTable.TypeLAVariable.INVALIDO) {
            return false;
        }

        if ((tipo1 == SymbolTable.TypeLAVariable.INTEIRO || tipo1 == SymbolTable.TypeLAVariable.REAL) &&
                (tipo2 == SymbolTable.TypeLAVariable.INTEIRO || tipo2 == SymbolTable.TypeLAVariable.REAL)) {
            return true;
        }

        return false;
    }

    // Verifying the type in context of a non-unary parcel
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable table, laParser.Parcela_nao_unarioContext ctx) {
        if (ctx.CADEIA() != null) {
            return SymbolTable.TypeLAVariable.LITERAL;
        }
        return null;
    }
}
