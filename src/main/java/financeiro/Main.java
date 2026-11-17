package financeiro;

import financeiro.model.TipoTransacao;
import financeiro.model.Transacao;
import financeiro.repository.RepositorioTransacoes;
import financeiro.service.ServicoFinanceiro;

import javax.sound.midi.SysexMessage;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private static final Locale BR = Locale.forLanguageTag("pt-BR");
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(BR);
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter MES_BR = DateTimeFormatter.ofPattern("MM/yyyy");

    private final Scanner entrada = new Scanner(System.in);
    private final ServicoFinanceiro servico =
            new ServicoFinanceiro(new RepositorioTransacoes(Path.of("transacoes.csv")));

    public static void main(String[] args) {
        new Main().executar();
    }

    private void executar() {
        System.out.println("=== Controle Financeiro Pessoal ===\n");
        while (true) {
            System.out.println("Saldo atual: " + dinheiro(servico.saldoGeral()));
            System.out.println("1 - Adicionar receita");
            System.out.println("2 - Adicionar despesa");
            System.out.println("3 - Ver extrato do mês");
            System.out.println("4 - Resumo do mês por categoria");
            System.out.println("5 - Remover transação");
            System.out.println("0 - Sair");

            switch (lerTexto("Escolha uma opção: ")) {
                case "1" -> adicionar(TipoTransacao.RECEITA);
                case "2" -> adicionar(TipoTransacao.DESPESA);
                case "3" -> mostrarExtrato();
                case "4" -> mostrarResumo();
                case "5" -> remover();
                case "0" -> {
                    System.out.println("Até mais!");
                    return;
                }
                default -> System.out.println("Opção inválida.");
            }
            System.out.println();
        }
    }

    private void adicionar(TipoTransacao tipo) {
        try {
            String descricao = lerTexto("Descrição: ");
            String categoria = lerTexto("Categoria (Enter = Outros): ");
            BigDecimal valor = new BigDecimal(lerTexto("Valor (ex: 12,50): ").replace(",", "."));
            String textoData = lerTexto("Data dd/mm/aaaa (Enter = hoje): ");
            LocalDate data = textoData.isEmpty() ? LocalDate.now() : LocalDate.parse(textoData, DATA_BR);

            Transacao nova = servico.adicionar(tipo, descricao, categoria, valor, data);
            System.out.println("Transação #" + nova.id() + " adicionada.");
        } catch (DateTimeParseException e) {
            System.out.println("Data inválida. Use o formato dd/mm/aaaa.");
        } catch (NumberFormatException e) {
            System.out.println("Valor inválido. Digite só números, ex: 12,50.");
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void mostrarExtrato() {
        YearMonth mes = lerMes();
        List<Transacao> lista = servico.listarDoMes(mes);
        if (lista.isEmpty()) {
            System.out.println("Nenhuma transação em " + mes.format(MES_BR) + ".");
            return;
        }

        System.out.println("\n--- Extrato de" + mes.format(MES_BR) + " ---");
        for (Transacao t : lista) {
            System.out.printf("#%-3d %s  %-7s  %-20s  %-12s %s%n",
                    t.id(),
                    t.data().format(MES_BR),
                    t.tipo(),
                    t.descricao(),
                    t.categoria(),
                    dinheiro(t.valorComSinal()));
        }

        BigDecimal receitas = servico.total(lista,TipoTransacao.RECEITA);
        BigDecimal despesas = servico.total(lista, TipoTransacao.DESPESA);
        System.out.println("Receitas: " + dinheiro(receitas));
        System.out.println("Despesas: " + dinheiro(despesas));
        System.out.println("Saldo do mês: " + dinheiro(receitas.subtract(despesas)));
    }

    private void mostrarResumo() {
        YearMonth mes = lerMes();
        Map<String, BigDecimal> porCategoria = servico.despesasPorCategoria(mes);
        if (porCategoria.isEmpty()) {
            System.out.println("Nenhuma despesa em " + mes.format(MES_BR) + ".");
            return;
        }

        System.out.println("\n--- Despesas por categoria em " + mes.format(MES_BR) + " ---");
        for (Map.Entry<String, BigDecimal> item : porCategoria.entrySet()) {
            System.out.printf("%-15s %s%n", item.getKey(), dinheiro(item.getValue()));
        }
    }

    private void remover() {
        mostrarExtrato();
        try {
            int id = Integer.parseInt(lerTexto("Id da transação a remover: "));
            if (servico.remover(id)) {
                System.out.println("Transação #" + id + " removida.");
            } else {
                System.out.println("Não encontrei a transação #" + id + ".");
            }
        } catch (NumberFormatException e) {
            System.out.println("Id inválido.");
        }
    }

    private YearMonth lerMes() {
        String texto = lerTexto("Mês mm/aaaa (Enter = mês atual): ");
        if (texto.isEmpty()) {
            return YearMonth.now();
        }
        try {
            return  YearMonth.parse(texto, MES_BR);
        } catch (DateTimeParseException e) {
            System.out.println("Mês inválido, usando o mês atual.");
            return  YearMonth.now();
        }
    }

    private String lerTexto(String rotulo) {
        System.out.println(rotulo);
        return entrada.nextLine().trim();
    }

    private String dinheiro(BigDecimal valor) {
        return MOEDA.format(valor);
    }
}
