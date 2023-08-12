package br.ufscar.dc.compiladores.la.semantico;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.IOException;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.misc.ParseCancellationException;

/**
 * Hello world!
 *
 */
public class Main {
    public static void main(String[] args) {
        try {
            String arquivoSaida = args[1];
            CharStream cs = CharStreams.fromFileName(args[0]);

            try (PrintWriter pw = new PrintWriter(arquivoSaida)) {
                try {
                    laLexer lex = new laLexer(cs);
                    Token t = null;
                    while ((t = lex.nextToken()).getType() != Token.EOF) {
                        String nomeToken = laLexer.VOCABULARY.getDisplayName(t.getType());
                        switch (nomeToken) {
                            case "ERRO":
                                throw new ParseCancellationException(
                                        "Linha " + t.getLine() + ": " + t.getText() + " - simbolo nao identificado");
                            case "CADEIA_NAO_FECHADA":
                                throw new ParseCancellationException(
                                        "Linha " + t.getLine() + ": cadeia literal nao fechada");
                            case "COMENTARIO_NAO_FECHADO":
                                throw new ParseCancellationException(
                                        "Linha " + t.getLine() + ": comentario nao fechado");

                        }
                    }
                    lex.reset();
                    CommonTokenStream tokens = new CommonTokenStream(lex);
                    laParser parser = new laParser(tokens);
                    SyntaxErrorListener mcel = new SyntaxErrorListener();
                    parser.removeErrorListeners();
                    parser.addErrorListener(mcel);
                    var programa = parser.programa();
                    laVisitor<Void> semantic = new LaSemanticAnalyzer();
                    semantic.visitPrograma(programa);
                    if (!LaUtils.semanticErrors.isEmpty()) {
                        for (var s : LaUtils.semanticErrors) {
                            pw.write(s);
                        }
                        pw.write("Fim da compilacao\n");
                    }
                } catch (ParseCancellationException e) {
                    pw.println(e.getMessage());
                    pw.println("Fim da compilacao");
                }
            } catch (FileNotFoundException fnfe) {
                System.err.println("O arquivo/diretório não existe:" + args[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
