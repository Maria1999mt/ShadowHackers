/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt 
 */
package ShadowHackersServer;

import java.io.BufferedReader;         
import java.io.IOException;           
import java.io.InputStreamReader;       
import java.io.OutputStreamWriter;      
import java.io.PrintWriter;            
import java.net.Socket;                 
import java.util.ArrayList;             
import java.util.List;                  
import java.util.Random;                

/**
 * HiloCliente:
 * Esta clase representa a UN cliente conectado al servidor.
 * Cada cliente corre en su propio hilo para poder jugar varios a la vez.
 */

public class HiloCliente implements Runnable {

    // Socket del cliente 
    private final Socket socket;

    // Número del jugador (1,2,3...) asignado por el servidor
    private int jugadorNum;

    // Genenera aleatorios para elegir nodos al azar
    private final Random random = new Random();

    // Constructor: recibe el socket del cliente cuando se conecta
    public HiloCliente(Socket socket) {
        this.socket = socket;
    }

    
    // Devuelve el nombre con el formato: "Jugador X"
   
    private String nombreJugador() {
        return "Jugador " + jugadorNum;
    }

    /**
     * Calcula los puntos del jugador.
     * Un punto = un nodo que esté “capturado” por este jugador.
     *
     * Se usa synchronized(Servidor.nodos) porque varios hilos
     * (varios clientes) pueden acceder/modificar el mapa de nodos a la vez.
     */
    private int puntosActuales() {
        int p = 0;

        // Bloque sincronizado: asegura que nadie cambie los nodos mientras contamos
        synchronized (Servidor.nodos) {
            for (int i = 1; i <= 10; i++) {
                // Si el nodo i lo tiene este jugador, suma 1 punto
                if (Servidor.nodos.get(i) == jugadorNum) {
                    p++;
                }
            }
        }
        return p;
    }

    /**
     * Envía al cliente el estado de todos los nodos (1..10)
     * diciendo si están libres o pertenecen a un jugador.
     * Al final envía los puntos del jugador y un "END" como marca de fin.
     */
    private void enviarStatus(PrintWriter out) {
        out.println(" ------ ESTADO DE NODOS ------");

        // Sincronizamos para leer el mapa de nodos de forma segura
        synchronized (Servidor.nodos) {
            for (int i = 1; i <= 10; i++) {
                int owner = Servidor.nodos.get(i);

                if (owner == 0) {
                    out.println("Nodo " + i + ": LIBRE");
                } else {
                    out.println("Nodo " + i + ": Jugador " + owner);
                }
            }
        }

        out.println("...............................");
        out.println(nombreJugador());
        out.println("Tus puntos son : " + puntosActuales());
        out.println("...............................");

        // "END" se usa para que el cliente sepa que el mensaje terminó
        out.println("END");
    }

    /**
     * Devuelve un nodo aleatorio entre 1 y 10.
     */
    private int nodoAleatorioDel1al10() {
        return 1 + random.nextInt(10); 
    }

    /**
     * Busca todos los nodos libres (owner == 0) y elige uno aleatorio.
     * Si no hay libres, devuelve -1.
     */
    private int nodoLibreAleatorio() {
        List<Integer> libres = new ArrayList<>();

        // Recorremos nodos 1..10 y guardamos los libres
        for (int i = 1; i <= 10; i++) {
            if (Servidor.nodos.get(i) == 0) {
                libres.add(i);
            }
        }

        // Si no hay libres, devolvemos -1 para indicar "no disponible"
        if (libres.isEmpty()) {
            return -1;
        }

        // Elegimos uno libre al azar de la lista
        return libres.get(random.nextInt(libres.size()));
    }

    /**
     * run():
     * Es el método que se ejecuta cuando se lanza el hilo del cliente.
     * Aquí está el bucle principal que lee comandos y responde.
     */
    @Override
    public void run() {
        try (
                // out: para enviar texto al cliente 
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true
                );

                // in: para leer lo que escribe el cliente
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), "UTF-8")
                )
        ) {
            // El servidor asigna un número único al jugador que se conecta
            jugadorNum = Servidor.nuevoJugador();

            // Mensaje de bienvenida + comandos disponibles
            out.println("Bienvenido " + nombreJugador() + " --> "
                    + "Los comandos a utilizar durante el juego son los siguientes: STATUS | HACK 3 | EXIT");
            out.println("END");

            String linea;

            // Bucle principal: leer comandos hasta que el cliente se desconecte o ponga EXIT
            while ((linea = in.readLine()) != null) {
                linea = linea.trim(); // quitamos espacios al inicio y final

                // Si el comando viene vacío, avisamos
                if (linea.isEmpty()) {
                    out.println("Comando vacio. Usa: STATUS | HACK 3 | EXIT");
                    out.println("END");
                    continue;
                }

                // Si el cliente quiere salir
                if (linea.equalsIgnoreCase("EXIT")) {
                    out.println("Adios, " + nombreJugador() + "!");
                    out.println("END");
                    break; // salimos del bucle
                }

                // Mostrar el estado actual de nodos
                if (linea.equalsIgnoreCase("STATUS")) {
                    enviarStatus(out);
                    continue;
                }

                /**
                 * Comando HACK 3
                 *   El servidor elige el objetivo aleatoriamente cada vez.
                 */
                if (linea.toUpperCase().startsWith("HACK")) {
                    String[] partes = linea.split("\\s+"); // separa por espacios

                    // Debe tener al menos 2 partes: ["HACK", "3"]
                    if (partes.length < 2) {
                        out.println("Formato incorrecto. Usa: HACK 3");
                        out.println("END");
                        continue;
                    }

                    // Intentamos leer el número 3
                    int x;
                    try {
                        x = Integer.parseInt(partes[1]);
                    } catch (NumberFormatException e) {
                        out.println("Formato incorrecto. Usa: HACK 3");
                        out.println("END");
                        continue;
                    }

                    // En este juego, el objetivo real se elige aleatorio (1..10)
                    int objetivo = nodoAleatorioDel1al10();

                    // Sincronizamos porque vamos a leer/modificar el mapa de nodos
                    synchronized (Servidor.nodos) {
                        int owner = Servidor.nodos.get(objetivo);

                        // Si el nodo está libre, lo capturas
                        if (owner == 0) {
                            Servidor.nodos.put(objetivo, jugadorNum);
                            out.println("Nodo " + objetivo + " (LIBRE)");
                            out.println("Capturas el Nodo " + objetivo + " | Puntos: " + puntosActuales());
                            out.println("END");
                            continue;
                        }

                        // Si el nodo está ocupado, buscamos otro nodo libre al azar
                        out.println("EL nodo " + objetivo + " esta ocupado por Jugador " + owner);

                        int asignado = nodoLibreAleatorio();

                        // Si no hay nodos libres, se informa
                        if (asignado == -1) {
                            out.println("No hay nodos libres disponibles.");
                        } else {
                            // Si hay un libre, se le asigna al jugador
                            Servidor.nodos.put(asignado, jugadorNum);
                            out.println("Te asigno otro nodo: Nodo " + asignado + " | Puntos: " + puntosActuales());
                        }

                        out.println("END");
                    }
                    continue;
                }

                // Si no coincide con ningún comando válido
                out.println("Comando no reconocido. Usa: STATUS | HACK 3 | EXIT");
                out.println("END");
            }

        } catch (IOException e) {
            // Normalmente aquí entra si el cliente se desconecta de golpe o hay error de red
        } finally {
            // Cerramos el socket siempre (liberar recursos)
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
