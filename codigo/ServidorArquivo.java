package codigo;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ServidorArquivo {

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Uso: java ServidorArquivo <modo: 1|2|3> <caminho_arquivo> <porta> [N_threads]");
            System.out.println("Modo 1: Apenas 1 cliente por vez");
            System.out.println("Modo 2: Todos os clientes de uma vez (Thread por cliente)");
            System.out.println("Modo 3: Máximo de N clientes por vez (Thread Pool)");
            return;
        }

        int modo = Integer.parseInt(args[0]);
        File arquivo = new File(args[1]);
        int porta = Integer.parseInt(args[2]);

        if (!arquivo.exists()) {
            System.out.println("Erro: Arquivo não encontrado.");
            return;
        }

        ServerSocket serverSocket = new ServerSocket(porta);
        System.out.println("Servidor iniciado na porta " + porta + " | Modo: " + modo + " | Arquivo: " + arquivo.getName());

        ExecutorService pool = null;
        if (modo == 3) {
            int nThreads = args.length >= 4 ? Integer.parseInt(args[3]) : 5;
            pool = Executors.newFixedThreadPool(nThreads);
            System.out.println("Tamanho do Thread Pool: " + nThreads);
        }

        while (true) {
            Socket cliente = serverSocket.accept();
            System.out.println("Cliente conectado: " + cliente.getInetAddress());

            Runnable tarefa = () -> enviarArquivo(cliente, arquivo);

            if (modo == 1) {
                tarefa.run(); // Executa na mesma thread (Bloqueia até terminar)
            } else if (modo == 2) {
                new Thread(tarefa).start(); // Cria uma nova thread livre para cada cliente
            } else if (modo == 3) {
                pool.execute(tarefa); // Entra na fila do Pool de N threads
            }
        }
    }

    private static void enviarArquivo(Socket cliente, File arquivo) {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(arquivo));
             OutputStream os = cliente.getOutputStream()) {

            byte[] buffer = new byte[8192];
            int lidos;
            while ((lidos = bis.read(buffer)) != -1) {
                os.write(buffer, 0, lidos);
            }
            os.flush();
            System.out.println("Transferência concluída para: " + cliente.getInetAddress());

        } catch (IOException e) {
            System.err.println("Erro na transferência: " + e.getMessage());
        } finally {
            try { cliente.close(); } catch (IOException e) {}
        }
    }
}