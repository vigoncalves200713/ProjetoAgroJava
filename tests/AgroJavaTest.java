import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/** Teste de integração do AgroJava, escrito em Java puro (Java 11+). */
public final class AgroJavaTest {
    private static final String ENTRADAS = String.join("\n", Arrays.asList(
            "1", "10/11/2026", "0", "10.5", "5", "0", "20", "7.5", "2.5",
            "18", "22", "38", "45", "25", "30", "55", "60",
            "14", "85", "40", "35", "55", "60", "75", "90",
            "2", "3", "5", "6", "7", "8", "4", ""));

    private AgroJavaTest() { }

    public static void main(String[] args) throws Exception {
        Path raiz = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        Path temporario = Files.createTempDirectory("agrojava-test-");
        try {
            compilarAplicacao(raiz, temporario);
            String saida = executar(temporario, ENTRADAS);
            exigir(saida, "Média semanal de chuva: 6.50 mm");
            exigir(saida, "Maior índice: Sexta-feira (20.00 mm)");
            exigir(saida, "A1 - Crítico");
            exigir(saida, "Total de alertas: 5");
            exigir(saida, "Semana mais chuvosa: 10/11/2026");
            exigir(saida, "Volume acumulado: 45.50 mm");
            exigir(saida, "AgroJava encerrado");
            exigirArquivo(temporario.resolve("historico_chuvas.csv"),
                    "data,seg,ter,qua,qui,sex,sab,dom,media,total");
            exigirArquivo(temporario.resolve("historico_chuvas.csv"),
                    "10/11/2026,0.00,10.50,5.00,0.00,20.00,7.50,2.50,6.50,45.50");
            exigirLinhas(temporario.resolve("mapa_umidade.csv"), 17);
            exigirLinhas(temporario.resolve("talhoes_classificados.csv"), 17);
            exigirArquivo(temporario.resolve("talhoes_classificados.csv"), "A1,18.00,Crítico");
            exigirArquivo(temporario.resolve("relatorio_fazenda.txt"),
                    "Talhão mais seco: C1 - 14.00%");
            String aposReiniciar = executar(temporario, "5\n4\n");
            exigir(aposReiniciar, "Semana: 10/11/2026 | Média: 6.50 mm | Total: 45.50 mm");
            System.out.println("Testes do AgroJava (RF01–RF12) executados com sucesso.");
        } finally {
            apagarRecursivamente(temporario);
        }
    }

    private static void compilarAplicacao(Path raiz, Path saida) {
        JavaCompiler compilador = ToolProvider.getSystemJavaCompiler();
        if (compilador == null) throw new IllegalStateException("É necessário um JDK para executar os testes.");
        int resultado = compilador.run(null, null, null, "-Xlint:all", "-d", saida.toString(),
                raiz.resolve("src/AgroJava.java").toString());
        if (resultado != 0) throw new AssertionError("Falha ao compilar a aplicação.");
    }

    private static String executar(Path diretorio, String entradas) throws Exception {
        String executavel = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
        Process processo = new ProcessBuilder(executavel, "-cp", diretorio.toString(), "AgroJava")
                .directory(diretorio.toFile()).redirectErrorStream(true).start();
        processo.getOutputStream().write(entradas.getBytes(StandardCharsets.UTF_8));
        processo.getOutputStream().close();
        byte[] bytes = processo.getInputStream().readAllBytes();
        int codigo = processo.waitFor();
        String saida = new String(bytes, StandardCharsets.UTF_8);
        if (codigo != 0) throw new AssertionError("AgroJava terminou com código " + codigo + ":\n" + saida);
        return saida;
    }

    private static void exigir(String texto, String esperado) {
        if (!texto.contains(esperado)) throw new AssertionError("Saída não contém: " + esperado);
    }

    private static void exigirArquivo(Path arquivo, String esperado) throws IOException {
        if (!Files.readString(arquivo, StandardCharsets.UTF_8).contains(esperado)) {
            throw new AssertionError("Arquivo " + arquivo.getFileName() + " não contém: " + esperado);
        }
    }

    private static void exigirLinhas(Path arquivo, long quantidade) throws IOException {
        long linhas;
        try (java.util.stream.Stream<String> conteudo = Files.lines(arquivo, StandardCharsets.UTF_8)) {
            linhas = conteudo.count();
        }
        if (linhas != quantidade) throw new AssertionError(arquivo.getFileName()
                + " deveria ter " + quantidade + " linhas, mas tem " + linhas);
    }

    private static void apagarRecursivamente(Path caminho) throws IOException {
        if (!Files.exists(caminho)) return;
        try (java.util.stream.Stream<Path> caminhos = Files.walk(caminho)) {
            caminhos.sorted(java.util.Comparator.reverseOrder()).forEach(item -> {
                try { Files.deleteIfExists(item); }
                catch (IOException e) { throw new java.io.UncheckedIOException(e); }
            });
        } catch (java.io.UncheckedIOException e) {
            throw e.getCause();
        }
    }
}
