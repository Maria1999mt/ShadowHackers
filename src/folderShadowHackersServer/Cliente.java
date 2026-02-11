/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java 
 */
package folderShadowHackersServer;

import java.io.BufferedReader;         
import java.io.IOException;            
import java.io.InputStreamReader;      
import java.io.OutputStreamWriter;      
import java.io.PrintWriter;            
import java.net.Socket;                 
import java.util.Scanner;               

/**
 Cliente:
 Esta clase representa el programa cliente del juego.
 Se conecta al servidor y permite enviar comandos desde teclado.
 */
public class Cliente {

    // Dirección IP del servidor
    public static final String HOST = "127.0.0.1";

    // Puerto donde está escuchando el servidor
    public static final int PUERTO = 5000;

    public static void main(String[] args) {
        System.out.println("Cliente arrancando...");

        try (
            // Se crea la conexión con el servidor
            Socket socket = new Socket(HOST, PUERTO);

            // Flujo de entrada: para leer mensajes del servidor
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), "UTF-8")
            );

            // Flujo de salida: para enviar comandos al servidor
            PrintWriter out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true
            );

           
            Scanner sc = new Scanner(System.in)
        ) {

            // Lee el mensaje de bienvenida del servidor
            leerHastaEND(in);

            // Bucle principal del cliente
            while (true) {

                // Pide al usuario que escriba un comando
                System.out.print("Escribe un comando: ");
                String cmd = sc.nextLine();

                // Envía el comando al servidor
                out.println(cmd);

                // Lee la respuesta del servidor hasta que encuentre "END"
                leerHastaEND(in);

                // Si el usuario escribe EXIT, termina el programa
                if (cmd.trim().equalsIgnoreCase("EXIT")) break;
            }

        } catch (IOException e) {
            // Si no se puede conectar o hay error de red
            System.out.println("Error en cliente: " + e.getMessage());
        }
    }

    /**
     Método auxiliar:
     Lee líneas del servidor hasta encontrar "END".
     Esto se usa para saber cuándo termina un mensaje completo.
     */
    private static void leerHastaEND(BufferedReader in) throws IOException {
        String linea;

        // Lee línea por línea
        while ((linea = in.readLine()) != null) {

            // Cuando llega "END", termina de leer
            if (linea.equals("END")) return;

            // Muestra el mensaje en pantalla
            System.out.println(linea);
        }
    }
}
