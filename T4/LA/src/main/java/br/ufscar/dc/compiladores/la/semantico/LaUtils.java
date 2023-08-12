package br.ufscar.dc.compiladores.la.semantico;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import br.ufscar.dc.compiladores.la.semantico.laParser.ExpressaoContext;

public class LaUtils {
    // Creating a list to store semantic errors
    public static List<String> semanticErrors = new ArrayList<>();

    // Adding a semantic error to the list. It takes a Token and a message as
    // parameters,
    // retrieves the line number from the token, and adds the formatted error to the
    // list.
    public static void addSemanticError(Token t, String msg) {
        int line = t.getLine();
        semanticErrors.add(String.format("Linha %d: %s", line, msg));
    }

    // Verifying the type in the context of an identifier
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable symbolTable, laParser.IdentificadorContext ctx) {
        String identifier = ctx.getText();

        if (identifier.contains("[") || identifier.contains("]")) {
            return verifyArrayIdentifier(symbolTable, ctx);
        } else {
            return verifyNonArrayIdentifier(symbolTable, ctx);
        }
    }

    private static SymbolTable.TypeLAVariable verifyNonArrayIdentifier(SymbolTable symbolTable,
            laParser.IdentificadorContext ctx) {
        String identifier = ctx.getText();
        String[] parts = identifier.split("\\.");

        if (!symbolTable.exists(parts[0])) {
            addSemanticError(ctx.IDENT(0).getSymbol(), "identificador " + identifier + " nao declarado\n");
            return SymbolTable.TypeLAVariable.NAO_DECLARADO;
        }

        SymbolTableEntry entry = symbolTable.check(parts[0]);
        if (parts.length > 1 && entry.identifierType == SymbolTable.TypeLAIdentifier.REGISTRO) {
            SymbolTable fields = entry.argsRegFunc;
            String fieldName = parts[1];

            if (!fields.exists(fieldName)) {
                addSemanticError(ctx.IDENT(0).getSymbol(), "identificador " + identifier + " nao declarado\n");
                return SymbolTable.TypeLAVariable.NAO_DECLARADO;
            }

            SymbolTableEntry fieldEntry = fields.check(fieldName);
            return fieldEntry.variableType;
        }

        return entry.variableType;
    }

    private static SymbolTable.TypeLAVariable verifyArrayIdentifier(SymbolTable symbolTable,
            laParser.IdentificadorContext ctx) {
        String identifierNoDim = ctx.IDENT().stream().map(ParseTree::getText).collect(Collectors.joining());

        for (laParser.Exp_aritmeticaContext xp : ctx.dimensao().exp_aritmetica()) {
            verifyType(symbolTable, xp);
        }

        if (!symbolTable.exists(identifierNoDim)) {
            addSemanticError(ctx.IDENT(0).getSymbol(), "identificador " + identifierNoDim + " nao declarado\n");
            return SymbolTable.TypeLAVariable.NAO_DECLARADO;
        }

        SymbolTableEntry entry = symbolTable.check(identifierNoDim);
        return entry.variableType;
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
        }
        if (ctx.NUM_REAL() != null) {
            return SymbolTable.TypeLAVariable.REAL;
        }
        if (ctx.IDENT() != null) {
            return verifyFunctionCall(symbolTable, ctx);
        }
        if (ctx.identificador() != null) {
            return verifyType(symbolTable, ctx.identificador());
        }
        if (ctx.expressao() != null) {
            for (ExpressaoContext exp : ctx.expressao()) {
                return verifyType(symbolTable, exp);
            }
        }

        return null; // You might want to handle this case accordingly
    }

    private static SymbolTable.TypeLAVariable verifyFunctionCall(SymbolTable symbolTable,
            laParser.Parcela_unarioContext ctx) {
        String functionName = ctx.IDENT().getText();

        if (!symbolTable.exists(functionName)) {
            addSemanticError(ctx.IDENT().getSymbol(), "identificador " + functionName + " nao declarado\n");
            return SymbolTable.TypeLAVariable.NAO_DECLARADO;
        }

        SymbolTableEntry function = symbolTable.check(functionName);
        ArrayList<SymbolTable.TypeLAVariable> parameterTypes = new ArrayList<>();

        for (ExpressaoContext exp : ctx.expressao()) {
            parameterTypes.add(verifyType(symbolTable, exp));
        }

        if (!function.argsRegFunc.validType(parameterTypes)) {
            addSemanticError(ctx.IDENT().getSymbol(),
                    "incompatibilidade de parametros na chamada de " + functionName + "\n");
        }

        return mapReturnType(function.functionType);
    }

    private static SymbolTable.TypeLAVariable mapReturnType(String returnType) {
        Map<String, SymbolTable.TypeLAVariable> returnTypeMappings = new HashMap<>();
        returnTypeMappings.put("inteiro", SymbolTable.TypeLAVariable.INTEIRO);
        returnTypeMappings.put("literal", SymbolTable.TypeLAVariable.LITERAL);
        returnTypeMappings.put("real", SymbolTable.TypeLAVariable.REAL);
        returnTypeMappings.put("logico", SymbolTable.TypeLAVariable.LOGICO);
        returnTypeMappings.put("^logico", SymbolTable.TypeLAVariable.PONT_LOGI);
        returnTypeMappings.put("^real", SymbolTable.TypeLAVariable.PONT_REAL);
        returnTypeMappings.put("^literal", SymbolTable.TypeLAVariable.PONT_LITE);
        returnTypeMappings.put("^inteiro", SymbolTable.TypeLAVariable.PONT_INTE);

        return returnTypeMappings.getOrDefault(returnType, SymbolTable.TypeLAVariable.REGISTRO);
    }

    // Verifying if the assignment types are valid
    public static boolean verifyType(SymbolTable.TypeLAVariable tipo1, SymbolTable.TypeLAVariable tipo2) {
        if (tipo1 == tipo2 || tipo1 == SymbolTable.TypeLAVariable.NAO_DECLARADO
                || tipo2 == SymbolTable.TypeLAVariable.NAO_DECLARADO) {
            return true;
        }

        if (tipo1 == SymbolTable.TypeLAVariable.INVALIDO || tipo2 == SymbolTable.TypeLAVariable.INVALIDO) {
            return false;
        }

        if ((tipo1 == SymbolTable.TypeLAVariable.INTEIRO || tipo1 == SymbolTable.TypeLAVariable.REAL) &&
                (tipo2 == SymbolTable.TypeLAVariable.INTEIRO || tipo2 == SymbolTable.TypeLAVariable.REAL)) {
            return true;
        }

        if ((tipo1 == SymbolTable.TypeLAVariable.PONT_INTE || tipo1 == SymbolTable.TypeLAVariable.PONT_REAL
                || tipo1 == SymbolTable.TypeLAVariable.PONT_LOGI) &&
                tipo2 == SymbolTable.TypeLAVariable.ENDERECO) {
            return true;
        }

        return false;
    }

    // Verifying the type in context of a non-unary parcel
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable table, laParser.Parcela_nao_unarioContext ctx) {
        if (ctx.CADEIA() != null) {
            return SymbolTable.TypeLAVariable.LITERAL;
        } else {
            return verifyAddressOrIdentifier(table, ctx);
        }
    }

    private static SymbolTable.TypeLAVariable verifyAddressOrIdentifier(SymbolTable table,
            laParser.Parcela_nao_unarioContext ctx) {
        SymbolTable.TypeLAVariable ret = verifyType(table, ctx.identificador());

        if (ctx.getText().contains("&")) {
            return SymbolTable.TypeLAVariable.ENDERECO;
        }

        return ret;
    }
}
