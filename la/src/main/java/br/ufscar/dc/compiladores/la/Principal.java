package br.ufscar.dc.compiladores.la;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.IOException;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;


/**
 * Hello world!
 *
 */
public class Principal
{
    public static void main( String[] args )
    {
        try {
            String arquivoSaida = args[1];
            CharStream cs = CharStreams.fromFileName(args[0]);
            try (PrintWriter pw = new PrintWriter(arquivoSaida)) {
                la lexico = new la(cs);

                Token t = null;

                while ((t = lexico.nextToken()).getType() != Token.EOF) {
                    String nomeToken = la.VOCABULARY.getDisplayName(t.getType());

                    if(nomeToken.equals("ERRO")) {
                        pw.println("Linha " + t.getLine() + ": " + t.getText() + " - simbolo nao identificado");
                        break;
                    }
                    else if(nomeToken.equals("COMENTARIO_NAO_FECHADO")) {
                        pw.println("Linha " + t.getLine() + ": comentario nao fechado");
                        break;
                    }
                    else if(nomeToken.equals("CADEIA_NAO_FECHADA")) {
                        pw.println("Linha " + t.getLine() + ": cadeia literal nao fechada");
                        break;
                    }
                    else if (nomeToken.equals("PALAVRA_CHAVE")){
                        pw.println("<'" + t.getText() + "','" + t.getText() + "'>");
                    }
                     else if (nomeToken.equals("OPERADOR_ARITMETICO")){
                        pw.println("<'" + t.getText() + "','" + t.getText() + "'>");
                    }
                     else if (nomeToken.equals("OPERADOR_RELACIONAL")){
                        pw.println("<'" + t.getText() + "','" + t.getText() + "'>");
                    }
                     else if (nomeToken.equals("OPERADOR_BOOLEANO")){
                        pw.println("<'" + t.getText() + "','" + t.getText() + "'>");
                    }
                     else {
                        pw.println("<'" + t.getText() + "'," + nomeToken + ">");
                    }
                }
            }catch(FileNotFoundException fnfe) {
                System.err.println("Arquivo não encontrado: "+ args[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
