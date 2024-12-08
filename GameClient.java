import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class GameClient {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;

    // Constructor to initialize the UDP client
    public GameClient(String serverHost, int serverPort) throws Exception {
        this.serverAddress = InetAddress.getByName(serverHost);
        this.serverPort = serverPort;
        this.socket = new DatagramSocket();
    }

    // Method to send a message to the server
    public void send(String message) throws Exception {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, serverPort);
        socket.send(packet);
    }

    // Method to receive a message from the server
    public String receive() throws Exception {
        byte[] buffer = new byte[1024]; // Buffer to store received data
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet); // Receive the packet
        return new String(packet.getData(), 0, packet.getLength()); // Return the received message
    }

    // Method to close the socket when done
    public void close() {
        socket.close();
    }
}
