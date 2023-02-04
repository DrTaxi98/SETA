package taxi.grpc.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import taxi.grpc.presentation.PresentationServiceImpl;
import taxi.grpc.recharge.RechargeServiceImpl;
import taxi.grpc.ride.RideServiceImpl;
import taxi.model.Taxi;

import java.io.IOException;

public class TaxiGrpcServer extends Thread {

    private final Server server;

    public TaxiGrpcServer(Taxi taxi) {
        server = ServerBuilder.forPort(taxi.getPortNumber())
                .addService(new PresentationServiceImpl(taxi))
                .addService(new RideServiceImpl(taxi))
                .addService(new RechargeServiceImpl(taxi))
                .build();
    }

    public int startServer() throws IOException {
        System.out.println("[gRPC Server] Launching taxi services.");
        server.start();
        System.out.println("[gRPC Server] Taxi gRPC server started on port: " + server.getPort());

        start();

        return server.getPort();
    }

    @Override
    public void run() {
        try {
            server.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
            shutdown();
        }
    }

    public void shutdown() {
        if (!server.isShutdown()) {
            System.out.println("[gRPC Server] Shutting down...");
            server.shutdown();
            System.out.println("[gRPC Server] Shut down.");
        }
    }
}
