package br.ufscar.dc.compiladores.la.sintatico;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.CharStream;

public class Principal 
{
    public static void main( String[] args )
    {
         try {
            String arquivoSaida = args[1];
            CharStream cs = CharStreams.fromFileName(args[0]);

            try (PrintWriter pw = new PrintWriter(arquivoSaida)) {
                try {
                    laLexer lex = new laLexer(cs);
                    Token t = null;
                    while ((t = lex.nextToken()).getType() != Token.EOF) {
                        String nomeToken = laLexer.VOCABULARY.getDisplayName(t.getType());

                        if(nomeToken.equals("ERRO"))
                            throw new ParseCancellationException("Linha " + t.getLine() + ": " + t.getText() + " - simbolo nao identificado");
                            
                        else if(nomeToken.equals("CADEIA_NAO_FECHADA")) 
                            throw new ParseCancellationException("Linha " + t.getLine() + ": cadeia literal nao fechada");
                            
                        else if(nomeToken.equals("COMENTARIO_NAO_FECHADO")) 
                            throw new ParseCancellationException("Linha " + t.getLine() + ": comentario nao fechado");
                        
                    }
                    
                    lex.reset();
                    CommonTokenStream tokens = new CommonTokenStream(lex); 
                    laParser parser = new laParser(tokens); 
                    SyntaxErrorListener text = new SyntaxErrorListener();
                    parser.removeErrorListeners();
                    parser.addErrorListener(text);

                    parser.programa(); 
                    
                }   catch (ParseCancellationException e){
                    pw.println(e.getMessage());
                    pw.println("Fim da compilacao");
                }

            }catch(FileNotFoundException fnfe) {
                System.err.println("Diretório não existe:" + args[1]);
            }
        } catch (IOException e) { 
            e.printStackTrace();
        }
    }
}
