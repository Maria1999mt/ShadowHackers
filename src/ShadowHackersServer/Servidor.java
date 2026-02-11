/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java 
 */
package ShadowHackersServer;

import java.io.IOException;                   
import java.net.ServerSocket;            
import java.net.Socket;      
import java.util.HashMap;    
import java.util.Map;              
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Servidor:
 * Esta clase es el servidor principal del juego.
 * Se encarga de:
 * - Abrir el puerto
 * - Aceptar clientes
 * - Crear un hilo (HiloCliente) para cada conexión
 * - Mantener el estado global del juego (nodos y jugadores)
 */
public class Servidor {

    // Puerto donde el servidor escuchará conexiones de clientes
    public static final int PUERTO = 5000;

    /**
     * Mapa de nodos (1..10):
     * valor 0  => nodo libre
     * valor 1..N => nodo capturado por Jugador X
     *
     * Es "static" porque es global y compartido por todos los hilos (todos los clientes).
     */
    public static final Map<Integer, Integer> nodos = new HashMap<>();

    /**
     * Contador de jugadores:
     * Se usa AtomicInteger para que sea seguro con varios hilos.
     * Cada vez que se conecta un cliente se incrementa:
     * Jugador 1, Jugador 2, Jugador 3...
     */
    public static final AtomicInteger contadorJugadores = new AtomicInteger(0);

    /**
     * Bloque static:
     * Se ejecuta una sola vez al cargar la clase.
     * Inicializa los 10 nodos como LIBRES (0).
     */
    static {
        for (int i = 1; i <= 10; i++) {
            nodos.put(i, 0);
        }
    }

    /**
     * Devuelve el número del nuevo jugador conectando.
     * incrementAndGet() incrementa y devuelve el nuevo valor.
     */
    public static int nuevoJugador() {
        return contadorJugadores.incrementAndGet();
    }

    /**
     * main:
     * Punto de entrada del servidor.
     * - Arranca el ServerSocket en el puerto 5000
     * - Se queda en bucle infinito esperando conexiones
     * - Cada vez que entra un cliente crea un hilo con HiloCliente
     */
    public static void main(String[] args) {

        System.out.println("Servidor arrancando...");

        // Asegura que el ServerSocket se cierra correctamente al salir
        try (ServerSocket server = new ServerSocket(PUERTO)) {

            System.out.println("Servidor escuchando en puerto " + PUERTO);

            // El servidor siempre está esperando clientes
            while (true) {

                // Bloquea hasta que un cliente se conecte
                Socket socketCliente = server.accept();

                // Se puede atender varios clientes a la vez
                new Thread(new HiloCliente(socketCliente)).start();
            }

        } catch (IOException e) {

            // Si hay error al abrir el puerto o aceptar conexiones, se muestra aquí
            System.out.println("Error en servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}