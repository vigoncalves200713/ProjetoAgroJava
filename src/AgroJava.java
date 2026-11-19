import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/** Sistema Integrado do Agronegócio (AgroJava). */
public class AgroJava {
    private static final int DIAS_DA_SEMANA = 7;
    private static final int LINHAS_CAMPO = 4;
    private static final int COLUNAS_CAMPO = 4;
    private static final double LIMITE_IRRIGACAO = 30.0;
    private static final Path ARQUIVO_CHUVAS = Paths.get("historico_chuvas.csv");
    private static final Path ARQUIVO_UMIDADE = Paths.get("mapa_umidade.csv");
    private static final Path ARQUIVO_TALHOES = Paths.get("talhoes_classificados.csv");
    private static final Path ARQUIVO_RELATORIO = Paths.get("relatorio_fazenda.txt");
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter
            .ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final String[] DIAS = {
        "Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira",
        "Sexta-feira", "Sábado", "Domingo"
    };
    private final Scanner scanner;
    private final double[] chuvas = new double[DIAS_DA_SEMANA];
    private final double[][] umidades = new double[LINHAS_CAMPO][COLUNAS_CAMPO];
    private boolean dadosCadastrados;
    private String dataSemana;

    public AgroJava() {
        scanner = new Scanner(System.in).useLocale(Locale.US);
    }

    public static void main(String[] args) {
        new AgroJava().executar();
    }

    private void executar() {
        int opcao;
        do {
            exibirMenu();
            opcao = lerInteiro("Escolha uma opção: ", 1, 8);
            switch (opcao) {
                case 1: cadastrarDados(); break;
                case 2: exibirMapaDoCampo(); break;
                case 3: exibirRelatorioDeAlertas(); break;
                case 4: System.out.println("\nAgroJava encerrado. Até mais!"); break;
                case 5: consultarHistoricoChuvas(); break;
                case 6: identificarSemanaMaisChuvosa(); break;
                case 7: gerarDashboardClimatico(); break;
                case 8: exportarRelatorio(); break;
                default: throw new IllegalStateException("Opção inválida");
            }
        } while (opcao != 4);
        scanner.close();
    }

    private void exibirMenu() {
        System.out.println("\n========================================");
        System.out.println("       SISTEMA AGROJAVA");
        System.out.println("========================================");
        System.out.println("1) Cadastrar Dados da Semana e Talhões");
        System.out.println("2) Exibir Mapa do Campo");
        System.out.println("3) Relatório de Alertas de Irrigação");
        System.out.println("4) Sair");
        System.out.println("5) Consultar Histórico de Chuvas");
        System.out.println("6) Identificar Semana Mais Chuvosa");
        System.out.println("7) Dashboard Climático");
        System.out.println("8) Exportar Relatório Gerencial");
        System.out.println("========================================");
    }

    private void cadastrarDados() {
        dataSemana = lerData("Data da semana (dd/MM/aaaa): ");
        System.out.println("\n--- REGISTRO DE CHUVAS (mm) ---");
        for (int i = 0; i < chuvas.length; i++) {
            chuvas[i] = lerDecimal(DIAS[i] + ": ", 0.0, Double.MAX_VALUE);
        }
        System.out.println("\n--- MAPEAMENTO DE UMIDADE DO SOLO (%) ---");
        for (int linha = 0; linha < umidades.length; linha++) {
            for (int coluna = 0; coluna < umidades[linha].length; coluna++) {
                umidades[linha][coluna] = lerDecimal(
                        "Talhão " + identificadorTalhao(linha, coluna) + " (%): ", 0.0, 100.0);
            }
        }
        dadosCadastrados = true;
        salvarSemanaAgricola();
        salvarMapaTalhoes();
        exibirResumoDeChuvas();
        System.out.println("Dados cadastrados e arquivos CSV atualizados com sucesso.");
    }

    private void salvarSemanaAgricola() {
        double total = somar(chuvas);
        double media = total / chuvas.length;
        try {
            boolean escreverCabecalho = !Files.exists(ARQUIVO_CHUVAS)
                    || Files.size(ARQUIVO_CHUVAS) == 0;
            try (BufferedWriter writer = Files.newBufferedWriter(ARQUIVO_CHUVAS,
                    StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND)) {
                if (escreverCabecalho) {
                    writer.write("data,seg,ter,qua,qui,sex,sab,dom,media,total");
                    writer.newLine();
                }
                writer.write(dataSemana);
                for (double chuva : chuvas) writer.write("," + formatarNumero(chuva));
                writer.write("," + formatarNumero(media) + "," + formatarNumero(total));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Não foi possível salvar o histórico de chuvas: " + e.getMessage());
        }
    }

    private void salvarMapaTalhoes() {
        try (BufferedWriter writer = Files.newBufferedWriter(ARQUIVO_UMIDADE,
                StandardCharsets.UTF_8)) {
            writer.write("talhao,umidade");
            writer.newLine();
            for (int l = 0; l < LINHAS_CAMPO; l++) {
                for (int c = 0; c < COLUNAS_CAMPO; c++) {
                    writer.write(identificadorTalhao(l, c) + "," + formatarNumero(umidades[l][c]));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Não foi possível salvar o mapa de umidade: " + e.getMessage());
        }
        try (BufferedWriter writer = Files.newBufferedWriter(ARQUIVO_TALHOES,
                StandardCharsets.UTF_8)) {
            writer.write("talhao,umidade,status");
            writer.newLine();
            for (int l = 0; l < LINHAS_CAMPO; l++) {
                for (int c = 0; c < COLUNAS_CAMPO; c++) {
                    writer.write(identificadorTalhao(l, c) + ","
                            + formatarNumero(umidades[l][c]) + "," + classificarTalhao(umidades[l][c]));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Não foi possível salvar as classificações: " + e.getMessage());
        }
    }

    private void exibirResumoDeChuvas() {
        double total = somar(chuvas);
        int indiceMaiorChuva = 0;
        for (int i = 1; i < chuvas.length; i++) {
            if (chuvas[i] > chuvas[indiceMaiorChuva]) indiceMaiorChuva = i;
        }
        System.out.printf(Locale.US, "Média semanal de chuva: %.2f mm%n", total / chuvas.length);
        System.out.printf(Locale.US, "Maior índice: %s (%.2f mm)%n",
                DIAS[indiceMaiorChuva], chuvas[indiceMaiorChuva]);
    }

    private void exibirMapaDoCampo() {
        if (!verificarDadosCadastrados()) return;
        System.out.println("\n--- MAPA DE UMIDADE DO CAMPO (%) ---");
        System.out.print("      ");
        for (int c = 0; c < COLUNAS_CAMPO; c++) System.out.printf("%-9s", "Coluna " + (c + 1));
        System.out.println();
        for (int l = 0; l < LINHAS_CAMPO; l++) {
            System.out.printf("Linha %d ", l + 1);
            for (int c = 0; c < COLUNAS_CAMPO; c++) {
                System.out.printf(Locale.US, "%8.2f%% ", umidades[l][c]);
            }
            System.out.println();
        }
    }

    private void exibirRelatorioDeAlertas() {
        if (!verificarDadosCadastrados()) return;
        System.out.println("\n===== ALERTAS DE IRRIGAÇÃO =====");
        int quantidade = 0;
        for (int l = 0; l < LINHAS_CAMPO; l++) {
            for (int c = 0; c < COLUNAS_CAMPO; c++) {
                if (umidades[l][c] <= LIMITE_IRRIGACAO) {
                    quantidade++;
                    System.out.printf(Locale.US, "%s - %s (%.2f%%)%n",
                            identificadorTalhao(l, c), classificarTalhao(umidades[l][c]), umidades[l][c]);
                }
            }
        }
        if (quantidade == 0) System.out.println("Nenhum talhão necessita de intervenção.");
        System.out.println("Total de alertas: " + quantidade);
    }

    private void consultarHistoricoChuvas() {
        List<Semana> semanas = carregarHistorico();
        System.out.println("\n===== HISTÓRICO DE CHUVAS =====");
        if (semanas.isEmpty()) {
            System.out.println("Nenhuma semana registrada.");
            return;
        }
        for (Semana semana : semanas) {
            System.out.printf(Locale.US, "Semana: %s | Média: %.2f mm | Total: %.2f mm%n",
                    semana.data, semana.media, semana.total);
        }
    }

    private void identificarSemanaMaisChuvosa() {
        List<Semana> semanas = carregarHistorico();
        if (semanas.isEmpty()) {
            System.out.println("\nNão há semanas cadastradas no histórico.");
            return;
        }
        Semana maior = semanas.get(0);
        for (Semana semana : semanas) if (semana.total > maior.total) maior = semana;
        System.out.printf(Locale.US, "\nSemana mais chuvosa: %s%nTotal: %.2f mm%n", maior.data, maior.total);
    }

    private void gerarDashboardClimatico() {
        List<Semana> semanas = carregarHistorico();
        System.out.println("\n===== DASHBOARD CLIMÁTICO =====");
        if (semanas.isEmpty()) {
            System.out.println("Nenhuma semana registrada.");
            return;
        }
        double acumulado = 0.0;
        Semana maior = semanas.get(0);
        Semana menor = semanas.get(0);
        for (Semana semana : semanas) {
            acumulado += semana.total;
            if (semana.total > maior.total) maior = semana;
            if (semana.total < menor.total) menor = semana;
        }
        System.out.println("Semanas cadastradas: " + semanas.size());
        System.out.printf(Locale.US, "Volume acumulado: %.2f mm%n", acumulado);
        System.out.printf(Locale.US, "Semana mais chuvosa: %s (%.2f mm)%n", maior.data, maior.total);
        System.out.printf(Locale.US, "Semana mais seca: %s (%.2f mm)%n", menor.data, menor.total);
        System.out.printf(Locale.US, "Média geral semanal: %.2f mm%n", acumulado / semanas.size());
    }

    private void exportarRelatorio() {
        if (!verificarDadosCadastrados()) return;
        String relatorio = montarRelatorioGerencial();
        try (BufferedWriter writer = Files.newBufferedWriter(ARQUIVO_RELATORIO,
                StandardCharsets.UTF_8)) {
            writer.write(relatorio);
            System.out.println("Relatório exportado para " + ARQUIVO_RELATORIO + ".");
        } catch (IOException e) {
            System.out.println("Não foi possível exportar o relatório: " + e.getMessage());
        }
    }

    private String montarRelatorioGerencial() {
        double soma = 0.0;
        int criticos = 0;
        int adequados = 0;
        int alertas = 0;
        int lMaisSeco = 0, cMaisSeco = 0, lMaisUmido = 0, cMaisUmido = 0;
        for (int l = 0; l < LINHAS_CAMPO; l++) {
            for (int c = 0; c < COLUNAS_CAMPO; c++) {
                double umidade = umidades[l][c];
                soma += umidade;
                if (umidade < 20.0) criticos++;
                if (umidade > LIMITE_IRRIGACAO) adequados++;
                if (umidade <= LIMITE_IRRIGACAO) alertas++;
                if (umidade < umidades[lMaisSeco][cMaisSeco]) { lMaisSeco = l; cMaisSeco = c; }
                if (umidade > umidades[lMaisUmido][cMaisUmido]) { lMaisUmido = l; cMaisUmido = c; }
            }
        }
        double media = soma / (LINHAS_CAMPO * COLUNAS_CAMPO);
        double percentualIrrigacao = 100.0 * alertas / (LINHAS_CAMPO * COLUNAS_CAMPO);
        return String.format(Locale.US,
                "===== RELATÓRIO DA FAZENDA =====%nData: %s%nMédia Geral: %.2f%%%n"
                + "Talhões Críticos: %d%nTalhões Adequados: %d%n"
                + "Necessidade de Irrigação: %.2f%% (%d de 16 talhões)%n"
                + "Talhão mais seco: %s - %.2f%%%nTalhão mais úmido: %s - %.2f%%%n",
                LocalDate.now().format(FORMATO_DATA), media, criticos, adequados,
                percentualIrrigacao, alertas,
                identificadorTalhao(lMaisSeco, cMaisSeco), umidades[lMaisSeco][cMaisSeco],
                identificadorTalhao(lMaisUmido, cMaisUmido), umidades[lMaisUmido][cMaisUmido]);
    }

    private List<Semana> carregarHistorico() {
        List<Semana> semanas = new ArrayList<>();
        if (!Files.exists(ARQUIVO_CHUVAS)) return semanas;
        try (BufferedReader reader = Files.newBufferedReader(ARQUIVO_CHUVAS, StandardCharsets.UTF_8)) {
            String linha;
            boolean cabecalho = true;
            while ((linha = reader.readLine()) != null) {
                if (cabecalho) { cabecalho = false; continue; }
                String[] campos = linha.split(",");
                if (campos.length < 10) continue;
                try {
                    semanas.add(new Semana(campos[0], Double.parseDouble(campos[8]),
                            Double.parseDouble(campos[9])));
                } catch (NumberFormatException ignorado) {
                    // Ignora linhas inválidas sem impedir a leitura dos demais registros.
                }
            }
        } catch (IOException e) {
            System.out.println("Não foi possível ler o histórico: " + e.getMessage());
        }
        return semanas;
    }

    private String classificarTalhao(double umidade) {
        if (umidade < 20.0) return "Crítico";
        if (umidade <= LIMITE_IRRIGACAO) return "Atenção";
        return "Adequado";
    }

    private String identificadorTalhao(int linha, int coluna) {
        return String.valueOf((char) ('A' + linha)) + (coluna + 1);
    }

    private boolean verificarDadosCadastrados() {
        if (!dadosCadastrados) {
            System.out.println("\nCadastre os dados primeiro pela opção 1.");
            return false;
        }
        return true;
    }

    private String lerData(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String valor = scanner.nextLine().trim();
            try {
                return LocalDate.parse(valor, FORMATO_DATA).format(FORMATO_DATA);
            } catch (DateTimeParseException e) {
                System.out.println("Data inválida. Use o formato dd/MM/aaaa.");
            }
        }
    }

    private int lerInteiro(String mensagem, int minimo, int maximo) {
        while (true) {
            System.out.print(mensagem);
            if (scanner.hasNextInt()) {
                int valor = scanner.nextInt();
                scanner.nextLine();
                if (valor >= minimo && valor <= maximo) return valor;
            } else {
                scanner.nextLine();
            }
            System.out.printf("Digite um número inteiro entre %d e %d.%n", minimo, maximo);
        }
    }

    private double lerDecimal(String mensagem, double minimo, double maximo) {
        while (true) {
            System.out.print(mensagem);
            if (scanner.hasNextDouble()) {
                double valor = scanner.nextDouble();
                scanner.nextLine();
                if (Double.isFinite(valor) && valor >= minimo && valor <= maximo) return valor;
            } else {
                scanner.nextLine();
            }
            System.out.printf(Locale.US, "Digite um valor entre %.2f e %.2f.%n", minimo, maximo);
        }
    }

    private double somar(double[] valores) {
        double soma = 0.0;
        for (double valor : valores) soma += valor;
        return soma;
    }

    private String formatarNumero(double valor) {
        return String.format(Locale.US, "%.2f", valor);
    }

    private static final class Semana {
        private final String data;
        private final double media;
        private final double total;
        private Semana(String data, double media, double total) {
            this.data = data;
            this.media = media;
            this.total = total;
        }
    }
}
