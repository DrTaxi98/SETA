package taxi.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class TaxiGrpcServer extends Thread {

    private final Server server;

    public TaxiGrpcServer(int portNumber) {
        server = ServerBuilder.forPort(portNumber)
                //.addService(new GreetingServiceImpl())
                .build();
    }

    @Override
    public void run() {
        try {
            System.out.println("[gRPC Server] Launching taxi services on port: " + server.getPort());
            server.start();
            System.out.println("[gRPC Server] Taxi gRPC server started.");
            server.awaitTermination();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            shutdown();
        }
    }

    public void shutdown() {
        if (!server.isShutdown())
            server.shutdown();
    }
}
