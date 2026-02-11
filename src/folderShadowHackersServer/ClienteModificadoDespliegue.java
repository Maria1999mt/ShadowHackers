/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
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
 *
 * @author Usuario
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java 
 */


/**
 * Cliente:
 * Se conecta al servidor y permite enviar comandos desde teclado.
 */
public class ClienteModificadoDespliegue {

    // Valores por defecto (LOCAL)
    public static final String HOST_DEF = "127.0.0.1";
    public static final int PUERTO_DEF = 5000;

    public static void main(String[] args) {
        System.out.println("Cliente arrancando...");

        // ✅ Permite: java Cliente host puerto
        String host = HOST_DEF;
        int puerto = PUERTO_DEF;

        if (args.length >= 2) {
            host = args[0];
            try {
                puerto = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("Puerto inválido. Usando 5000.");
                puerto = PUERTO_DEF;
            }
        }

        System.out.println("Conectando a " + host + ":" + puerto + "...");

        try (
                Socket socket = new Socket(host, puerto);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), "UTF-8")
                );

                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true
                );

                Scanner sc = new Scanner(System.in)
        ) {

            // Lee bienvenida
            leerHastaEND(in);

            // Bucle de comandos
            while (true) {
                System.out.print("Escribe un comando: ");
                String cmd = sc.nextLine();

                out.println(cmd);

                leerHastaEND(in);

                if (cmd.trim().equalsIgnoreCase("EXIT")) break;
            }

        } catch (IOException e) {
            System.out.println("Error en cliente: " + e.getMessage());
        }
    }

    private static void leerHastaEND(BufferedReader in) throws IOException {
        String linea;
        while ((linea = in.readLine()) != null) {
            if (linea.equals("END")) return;
            System.out.println(linea);
        }
        System.out.println("⚠ Conexión cerrada por el servidor.");
    }
}
