/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
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
public class HiloClienteModificadoDespliegue implements Runnable {

    // Socket del cliente
    private final Socket socket;

    // Número del jugador (1,2,3...) asignado por el servidor
    private int jugadorNum;

    // Genera aleatorios para elegir nodos al azar
    private final Random random = new Random();

    // Constructor: recibe el socket del cliente cuando se conecta
    public HiloClienteModificadoDespliegue(Socket socket) {
        this.socket = socket;
    }

    // Devuelve el nombre con el formato: "Jugador X"
    private String nombreJugador() {
        return "Jugador " + jugadorNum;
    }

    /**
     * Calcula los puntos del jugador.
     * Un punto = un nodo que esté capturado por este jugador.
     */
    private int puntosActuales() {
        int p = 0;

        synchronized (ServidorModificadoDespliegue.nodos) {
            for (int i = 1; i <= 10; i++) {
                if (ServidorModificadoDespliegue.nodos.get(i) == jugadorNum) {
                    p++;
                }
            }
        }
        return p;
    }

    /**
     * Envía al cliente el estado de todos los nodos (1..10)
     */
    private void enviarStatus(PrintWriter out) {
        out.println(" ------ ESTADO DE NODOS ------");

        synchronized (ServidorModificadoDespliegue.nodos) {
            for (int i = 1; i <= 10; i++) {
                int owner = ServidorModificadoDespliegue.nodos.get(i);

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
        out.println("END");
    }

    // Devuelve un nodo aleatorio entre 1 y 10.
    private int nodoAleatorioDel1al10() {
        return 1 + random.nextInt(10);
    }

    // Busca todos los nodos libres y elige uno aleatorio.
    private int nodoLibreAleatorio() {
        List<Integer> libres = new ArrayList<>();

        synchronized (ServidorModificadoDespliegue.nodos) {
            for (int i = 1; i <= 10; i++) {
                if (ServidorModificadoDespliegue.nodos.get(i) == 0) {
                    libres.add(i);
                }
            }
        }

        if (libres.isEmpty()) return -1;

        return libres.get(random.nextInt(libres.size()));
    }

    @Override
    public void run() {
        try (
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true
                );

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), "UTF-8")
                )
        ) {
            // ✅ Opción B: el jugador lo asigna ServidorModificadoDespliegue
            jugadorNum = ServidorModificadoDespliegue.nuevoJugador();

            out.println("Bienvenido " + nombreJugador() + " --> "
                    + "Los comandos a utilizar durante el juego son: STATUS | HACK 3 | EXIT");
            out.println("END");

            String linea;

            while ((linea = in.readLine()) != null) {
                linea = linea.trim();

                if (linea.isEmpty()) {
                    out.println("Comando vacio. Usa: STATUS | HACK 3 | EXIT");
                    out.println("END");
                    continue;
                }

                if (linea.equalsIgnoreCase("EXIT")) {
                    out.println("Adios, " + nombreJugador() + "!");
                    out.println("END");
                    break;
                }

                if (linea.equalsIgnoreCase("STATUS")) {
                    enviarStatus(out);
                    continue;
                }

                if (linea.toUpperCase().startsWith("HACK")) {
                    String[] partes = linea.split("\\s+");

                    if (partes.length < 2) {
                        out.println("Formato incorrecto. Usa: HACK 3");
                        out.println("END");
                        continue;
                    }

                    try {
                        Integer.parseInt(partes[1]); // validamos que sea número
                    } catch (NumberFormatException e) {
                        out.println("Formato incorrecto. Usa: HACK 3");
                        out.println("END");
                        continue;
                    }

                    int objetivo = nodoAleatorioDel1al10();

                    synchronized (ServidorModificadoDespliegue.nodos) {
                        int owner = ServidorModificadoDespliegue.nodos.get(objetivo);

                        if (owner == 0) {
                            ServidorModificadoDespliegue.nodos.put(objetivo, jugadorNum);
                            out.println("Nodo " + objetivo + " (LIBRE)");
                            out.println("Capturas el Nodo " + objetivo + " | Puntos: " + puntosActuales());
                            out.println("END");
                            continue;
                        }

                        out.println("El nodo " + objetivo + " esta ocupado por Jugador " + owner);

                        int asignado = nodoLibreAleatorio();

                        if (asignado == -1) {
                            out.println("No hay nodos libres disponibles.");
                        } else {
                            ServidorModificadoDespliegue.nodos.put(asignado, jugadorNum);
                            out.println("Te asigno otro nodo: Nodo " + asignado + " | Puntos: " + puntosActuales());
                        }

                        out.println("END");
                    }
                    continue;
                }

                out.println("Comando no reconocido. Usa: STATUS | HACK 3 | EXIT");
                out.println("END");
            }

        } catch (IOException e) {
            System.out.println("Error en HiloCliente: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
