package custom;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketProvider {

    private ServerSocket serverSocket;
    private Socket socket;

    public void setSocket(String ipAddress, int port) {
        try {
            socket = new Socket(ipAddress, port);
            MainController.appLogContent.add("Vytvorila sa inštancia Triedy Socket, IP adresa: " + ipAddress + " port: " + port);
            MainController.connectionState = "connected";

        } catch (IOException e) {
            MainController.appLogContent.add(e.getMessage());
        }
    }

    public void setServerSocket(int port) {
        try {
            serverSocket = new ServerSocket(port);
            MainController.appLogContent.add("Vytvorila sa inštancia Triedy ServerSocket  port: " + port);
            /*zastaví sa proces v threade, dokiaĺ nieje ... akceptnuté pripojenie, znamená*/
            socket = serverSocket.accept();
            MainController.connectionState = "connected";
            MainController.appLogContent.add("ServerSocket  : Bolo vytvorené spojenie s :" + socket.getInetAddress() + ":" + socket.getPort());
        } catch (IOException e) {
            MainController.appLogContent.add(e.getMessage());
        }
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public Socket getSocket() {
        return socket;
    }

    public void closeConnection() {
        try {
            MainController.connectionState = "notConnected";
            if (socket != null) {
                socket.close();
                socket = null;
                MainController.appLogContent.add("SocketProvider: socket.close() ");
            } else {
                MainController.appLogContent.add("Socket objekt už je null");
            }
            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
                MainController.appLogContent.add("ServerSocket: socket.close() ");
            } else {

                MainController.appLogContent.add("ServerSocket objekt už je null");
            }
        } catch (IOException e) {
            MainController.appLogContent.add(e.getMessage());
        }
    }
}