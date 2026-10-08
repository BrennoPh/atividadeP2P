package codigo;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClienteTeste {

    public static void main(String[] args) throws InterruptedException {
        if (args.length < 3) {
            System.out.println("Uso: java ClienteTeste <ip_servidor> <porta> <numero_de_clientes>");
            return;
        }

        String ip = args[0];
        int porta = Integer.parseInt(args[1]);
        int numClientes = Integer.parseInt(args[2]);

        System.out.println("Iniciando bateria de " + numClientes + " clientes simulados...");

        List<Thread> threads = new ArrayList<>();
        List<Long> tempos = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < numClientes; i++) {
            Thread t = new Thread(() -> {
                long inicio = System.currentTimeMillis();
                try (Socket socket = new Socket(ip, porta);
                     InputStream is = socket.getInputStream()) {

                    byte[] buffer = new byte[8192];
                    long totalBytes = 0;
                    int lidos;
                    
                    
                    while ((lidos = is.read(buffer)) != -1) {
                        totalBytes += lidos;
                    }

                    long fim = System.currentTimeMillis();
                    tempos.add(fim - inicio);
                    

                } catch (IOException e) {
                    System.err.println("Falha no cliente: " + e.getMessage());
                }
            });
            threads.add(t);
        }

       
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        calcularRelatorio(tempos, numClientes);
    }

    private static void calcularRelatorio(List<Long> tempos, int numClientes) {
        if (tempos.isEmpty()) return;

        long min = Collections.min(tempos);
        long max = Collections.max(tempos);
        double media = tempos.stream().mapToLong(val -> val).average().orElse(0.0);

        System.out.println("\n=== RESULTADOS (" + numClientes + " Clientes) ===");
        System.out.println("Tempo Mínimo: " + min + " ms");
        System.out.println("Tempo Médio:  " + String.format("%.2f", media) + " ms");
        System.out.println("Tempo Máximo: " + max + " ms");
        System.out.println("===============================");
    }
}