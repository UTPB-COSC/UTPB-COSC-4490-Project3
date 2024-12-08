import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class GameServer {
    public static void main(String[] args) {
        try (DatagramSocket serverSocket = new DatagramSocket(9876)) { // Bind to port 9876
            byte[] receiveBuffer = new byte[1024];

            System.out.println("Server is listening on port 9876...");

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                serverSocket.receive(receivePacket);

                String receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("Received Player Position: " + receivedData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
