import java.util.Locale;
import java.util.Scanner;

/**
 * Sistema Integrado do Agronegócio (AgroJava).
 *
 * Projeto educacional sobre arrays unidimensionais, matrizes bidimensionais,
 * busca, acumulação e interação via terminal.
 */
public class AgroJava {
    private static final int DIAS_DA_SEMANA = 7;
    private static final int LINHAS_CAMPO = 4;
    private static final int COLUNAS_CAMPO = 4;
    private static final double LIMITE_IRRIGACAO = 30.0;

    private static final String[] DIAS = {
        "Domingo", "Segunda-feira", "Terça-feira", "Quarta-feira",
        "Quinta-feira", "Sexta-feira", "Sábado"
    };

    private final Scanner scanner;
    private final double[] chuvas = new double[DIAS_DA_SEMANA];
    private final double[][] umidades = new double[LINHAS_CAMPO][COLUNAS_CAMPO];
    private boolean dadosCadastrados;

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
            opcao = lerInteiro("Escolha uma opção: ", 1, 4);

            switch (opcao) {
                case 1 -> cadastrarDados();
                case 2 -> exibirMapaDoCampo();
                case 3 -> exibirRelatorioDeAlertas();
                case 4 -> System.out.println("\nAgroJava encerrado. Até mais!");
                default -> throw new IllegalStateException("Opção inválida");
            }
        } while (opcao != 4);

        scanner.close();
    }

    private void exibirMenu() {
        System.out.println("\n========================================");
        System.out.println("       SISTEMA AGROJAVA");
        System.out.println("========================================");
        System.out.println("1) Cadastrar Dados");
        System.out.println("2) Exibir Mapa do Campo");
        System.out.println("3) Relatório de Alertas de Irrigação");
        System.out.println("4) Sair");
        System.out.println("========================================");
    }

    private void cadastrarDados() {
        System.out.println("\n--- REGISTRO DE CHUVAS (mm) ---");
        for (int i = 0; i < chuvas.length; i++) {
            chuvas[i] = lerDecimal(DIAS[i] + ": ", 0.0, Double.MAX_VALUE);
        }

        System.out.println("\n--- MAPEAMENTO DE UMIDADE DO SOLO (%) ---");
        for (int linha = 0; linha < umidades.length; linha++) {
            for (int coluna = 0; coluna < umidades[linha].length; coluna++) {
                String identificacao = "Talhão [" + (linha + 1) + "][" + (coluna + 1) + "]";
                umidades[linha][coluna] = lerDecimal(identificacao + ": ", 0.0, 100.0);
            }
        }

        dadosCadastrados = true;
        exibirResumoDeChuvas();
        System.out.println("\nDados cadastrados com sucesso!");
    }

    private void exibirResumoDeChuvas() {
        double total = 0.0;
        int indiceMaiorChuva = 0;

        for (int i = 0; i < chuvas.length; i++) {
            total += chuvas[i];
            if (chuvas[i] > chuvas[indiceMaiorChuva]) {
                indiceMaiorChuva = i;
            }
        }

        double media = total / chuvas.length;
        System.out.printf(Locale.US, "Média semanal de chuva: %.2f mm%n", media);
        System.out.printf(Locale.US, "Maior índice: %s (%.2f mm)%n",
                DIAS[indiceMaiorChuva], chuvas[indiceMaiorChuva]);
    }

    private void exibirMapaDoCampo() {
        if (!verificarDadosCadastrados()) {
            return;
        }

        System.out.println("\n--- MAPA DE UMIDADE DO CAMPO (%) ---");
        System.out.print("       ");
        for (int coluna = 0; coluna < COLUNAS_CAMPO; coluna++) {
            System.out.printf("T%-7d", coluna + 1);
        }
        System.out.println();

        for (int linha = 0; linha < LINHAS_CAMPO; linha++) {
            System.out.printf("L%-5d", linha + 1);
            for (int coluna = 0; coluna < COLUNAS_CAMPO; coluna++) {
                System.out.printf(Locale.US, "%7.2f%%", umidades[linha][coluna]);
            }
            System.out.println();
        }
    }

    private void exibirRelatorioDeAlertas() {
        if (!verificarDadosCadastrados()) {
            return;
        }

        System.out.println("\n--- RELATÓRIO DE ALERTAS DE IRRIGAÇÃO ---");
        boolean existeAlerta = false;
        for (int linha = 0; linha < umidades.length; linha++) {
            for (int coluna = 0; coluna < umidades[linha].length; coluna++) {
                if (umidades[linha][coluna] < LIMITE_IRRIGACAO) {
                    existeAlerta = true;
                    System.out.printf(Locale.US,
                            "ALERTA: Talhão [%d][%d] com %.2f%% - necessita irrigação.%n",
                            linha + 1, coluna + 1, umidades[linha][coluna]);
                }
            }
        }

        if (!existeAlerta) {
            System.out.println("Nenhum talhão abaixo de 30%. Não há necessidade de irrigação.");
        }
    }

    private boolean verificarDadosCadastrados() {
        if (!dadosCadastrados) {
            System.out.println("\nCadastre os dados primeiro pela opção 1.");
            return false;
        }
        return true;
    }

    private int lerInteiro(String mensagem, int minimo, int maximo) {
        while (true) {
            System.out.print(mensagem);
            if (scanner.hasNextInt()) {
                int valor = scanner.nextInt();
                scanner.nextLine();
                if (valor >= minimo && valor <= maximo) {
                    return valor;
                }
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
                if (valor >= minimo && valor <= maximo) {
                    return valor;
                }
            } else {
                scanner.nextLine();
            }
            System.out.printf(Locale.US, "Digite um valor entre %.2f e %.2f.%n", minimo, maximo);
        }
    }
}
