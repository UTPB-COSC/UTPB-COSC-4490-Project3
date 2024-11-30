import java.net.*;

public class GameClient {
    private static final int SERVER_PORT = 9876;
    private static final String SERVER_ADDRESS = "localhost";
    private static final int BUFFER_SIZE = 1024;

    private DatagramSocket socket;
    private InetAddress serverAddress;

    public GameClient() throws Exception {
        socket = new DatagramSocket();
        serverAddress = InetAddress.getByName(SERVER_ADDRESS);
    }

    public void send(String message) throws Exception {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, SERVER_PORT);
        socket.send(packet);
    }

    public String receive() throws Exception {
        byte[] buffer = new byte[BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return new String(packet.getData(), 0, packet.getLength());
    }

    public static void main(String[] args) throws Exception {
        GameClient client = new GameClient();

        // Example usage
        client.send("Player moved to (100, 200)");
        System.out.println("Server response: " + client.receive());
    }
}
