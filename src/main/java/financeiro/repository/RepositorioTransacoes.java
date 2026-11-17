package financeiro.repository;

import financeiro.model.TipoTransacao;
import financeiro.model.Transacao;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RepositorioTransacoes {

    private static final String SEPARADOR = ";";
    private static final String CABECALHO = "id;tipo;descricao;categoria;valor;data";

    private final Path arquivo;

    public RepositorioTransacoes(Path arquivo) {

        this.arquivo = arquivo;
    }

    public List<Transacao> carregar() {
        List<Transacao> transacoes = new ArrayList<>();
        if (!Files.exists(arquivo)) {
            return transacoes;
        }
        try {
            List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
            for (int i = 1; i < linhas.size(); i++) {
                String linha = linhas.get(i);
                if (linha.isBlank()) continue;
                String[] c = linha.split(SEPARADOR, -1);
                transacoes.add(new Transacao(
                        Integer.parseInt(c[0]),
                        TipoTransacao.valueOf(c[1]),
                        c[2],
                        c[3],
                        new BigDecimal(c[4]),
                        LocalDate.parse(c[5])));
            }
        } catch (IOException | RuntimeException e) {
            System.out.println("Aviso: não consegui ler " + arquivo + " (" + e.getMessage() + ").");
        }
        return transacoes;
    }

    public void salvar(List<Transacao> transacaos) {
        List<String> linhas = new ArrayList<>();
        linhas.add(CABECALHO);
        for (Transacao t : transacaos) {
            linhas.add(String.join(SEPARADOR,
                    String.valueOf(t.id()),
                    t.tipo().name(),
                    limpar(t.descricao()),
                    limpar(t.categoria()),
                    t.valor().toPlainString(),
                    t.data().toString()));
        }
        try {
            Files.write(arquivo, linhas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Erro ao salvar: " + e.getMessage());
        }
    }

    private String limpar(String texto) {

        return texto.replace(SEPARADOR, ",").trim();
    }
}
