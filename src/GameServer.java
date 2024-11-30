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
                socket.receive(packet);
                String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received: " + receivedMessage);

                // Echo back or send a response
                String response = "ACK: " + receivedMessage;
                DatagramPacket responsePacket = new DatagramPacket(
                        response.getBytes(),
                        response.getBytes().length,
                        packet.getAddress(),
                        packet.getPort()
                );
                socket.send(responsePacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
