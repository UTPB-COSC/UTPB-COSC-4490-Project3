import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class GameServer {
    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(9876)) {
            System.out.println("Server is running on port 9876");

            while (true) {
                // Receive packet
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                System.out.println("Waiting for a packet...");
                socket.receive(packet); // Receive the packet

                // Process the received message
                String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received: " + receivedMessage);

                // Send an acknowledgment back to the client
                String response = "ACK: " + receivedMessage;
                DatagramPacket responsePacket = new DatagramPacket(
                        response.getBytes(),
                        response.length(),
                        packet.getAddress(),
                        packet.getPort()
                );
                socket.send(responsePacket); // Send the response back to the client
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
