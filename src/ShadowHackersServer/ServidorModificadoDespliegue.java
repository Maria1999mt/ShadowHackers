/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package ShadowHackersServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Usuario
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java 
 */

/**
 * Servidor:
 * - Abre el puerto
 * - Acepta clientes
 * - Crea un hilo (HiloCliente) por conexión
 * - Mantiene estado global (nodos y jugadores)
 */
public class ServidorModificadoDespliegue {

    // Puerto local por defecto
    public static final int PUERTO = 5000;

    // Mapa global de nodos (1..10): 0 libre, 1..N jugador
    public static final Map<Integer, Integer> nodos = new HashMap<>();

    // Contador de jugadores (seguro para varios hilos)
    public static final AtomicInteger contadorJugadores = new AtomicInteger(0);

    static {
        for (int i = 1; i <= 10; i++) {
            nodos.put(i, 0);
        }
    }

    public static int nuevoJugador() {
        return contadorJugadores.incrementAndGet();
    }

    public static void main(String[] args) {
        System.out.println("Servidor arrancando...");

        // ✅ Puerto dinámico para hosting (Railway): usa PORT si existe
        int puerto = PUERTO;
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isBlank()) {
            try {
                puerto = Integer.parseInt(envPort);
            } catch (NumberFormatException ignored) {
                // si PORT está mal, usamos 5000
            }
        }

        try (ServerSocket server = new ServerSocket(puerto)) {
            System.out.println("Servidor escuchando en puerto " + puerto);

            while (true) {
                Socket socketCliente = server.accept();
                System.out.println("✅ Cliente conectado: " + socketCliente.getRemoteSocketAddress());

                new Thread(new HiloCliente(socketCliente)).start();
            }

        } catch (IOException e) {
            System.out.println("Error en servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
