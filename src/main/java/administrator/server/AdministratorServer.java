package administrator.server;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.util.Scanner;

public class AdministratorServer {

    private static final String HOST = "localhost";
    private static final int PORT = 1337;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServerFactory.create("http://" + HOST + ":" + PORT + "/");
        server.start();

        System.out.println("Server started on: http://" + HOST + ":" + PORT);
        System.out.println("Server running!");

        Scanner scanner = new Scanner(System.in);
        System.out.println("Hit return to stop...");
        scanner.nextLine();
        System.out.println("Stopping server...");
        server.stop(0);
        System.out.println("Server stopped.");
    }
}
