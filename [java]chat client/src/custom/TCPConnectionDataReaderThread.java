package custom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
//import jdk.nashorn.internal.parser.JSONParser;
import org.json.JSONObject;

public class TCPConnectionDataReaderThread extends Thread {

    public String connectionType = "";
    public String ipAddress = "";
    public int portNumber = 0;

    private boolean logTheMessage;
    private InputStreamReader inputStreamReader;
    private BufferedReader bufferedReader;

    @Override
    public void run() {
        try {
            this.setName("TCPConnectionDataReaderThread");
            while (true) {
                MainController.appLogContent.add("TCPConnectionDataReaderThread - proces vo vlákne bol spustený od znova");
                while (!MainController.listenForConnection) {
                    Thread.sleep(3000);
                }
                if (MainController.connectionType == "clientListener" && MainController.connectionState.equals("waitingForConnection")) {
                    ipAddress = MainController.remoteHost;
                    portNumber = Integer.parseInt(MainController.remotePort);
                    MainController.getSocketProvider().setSocket(ipAddress, portNumber);
                }
                if (MainController.connectionType == "serverListener" && MainController.connectionState.equals("waitingForConnection")) {
                    MainController.appLogContent.add("Listening as server");
                    portNumber = Integer.parseInt(MainController.myPort);
                    MainController.getSocketProvider().setServerSocket(portNumber);
                }
                while (MainController.getSocketProvider().getSocket() == null && MainController.connectionState.equals("connected")) {
                    MainController.appLogContent.add("MainController.getSocketProvider().getSocket() == null");
                    Thread.sleep(500);
                }
                if (MainController.getSocketProvider().getSocket() != null && MainController.listenForConnection) {
                    if (MainController.getSocketProvider().getSocket().isConnected()) {
                        inputStreamReader = new InputStreamReader(MainController.getSocketProvider().getSocket().getInputStream());
                        bufferedReader = new BufferedReader(inputStreamReader);
                        while (MainController.getSocketProvider().getSocket() != null && MainController.connectionState.equals("connected")) {
                            try {
                                if (MainController.getSocketProvider().getSocket() != null) {
                                    if (MainController.getSocketProvider().getSocket().isConnected()) {
                                        String inputMessage = "";
                                        inputMessage = bufferedReader.readLine();
                                        if (inputMessage.length() > 0) {
                                            logTheMessage = false;
                                            if (inputMessage.equals("0FS0AFJ01F098U09210FOASIJFOSAIJFOIAJSFOIJASOFIJASOIFJOIASFOIJASF")) {
                                                MainController.socketProvider.closeConnection();
                                                MainController.listenForConnection = false;
                                                MainController.connectionType = "";
                                                MainController.appLogContent.add("spojenie bolo ukončené");
                                            } else {
                                                if (MainController.useMessageEncryption) {
                                                    JSONObject jsonObject = new JSONObject(inputMessage);
                                                    byte[] encryptedMessage = new byte[jsonObject.length()];
                                                    for (int x = 0; x < jsonObject.length(); x++) {
                                                        encryptedMessage[x] = (byte) jsonObject.getInt(Integer.toString(x));
                                                    }
                                                    try {
                                                        inputMessage = new String(AdvancedEncryptionStandard.getInstance().decrypt(encryptedMessage));
                                                        logTheMessage = true;
                                                    } catch (BadPaddingException e) {
                                                        MainController.appLogContent.add("Pokus o odšifrovanie správy zlyhal");
                                                        MainController.appLogContent.add("Zašifrovaný obsah prijatej správy:" + new String(encryptedMessage));
                                                        logTheMessage = false;
                                                    }
                                                } else {
                                                    logTheMessage = true;
                                                }
                                                if (logTheMessage) {
                                                    MainController.chatLogContent.add(new ChatEntry(MainController.getSocketProvider().getSocket().getInetAddress() + ":" + MainController.getSocketProvider().getSocket().getPort(), inputMessage, "incoming"));
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (NullPointerException e) {
                                MainController.appLogContent.add("TCPConnectionDataReaderThread - nullpointer exception");
                                ////vypisuje to null pointer exception, keď server zruší svoje pripojenie. Kontrola či existuje socket objekt alebo či je socket pripojený sa ukázala zbytočnou.
                                //spojenie teda zruším
                            } catch (Exception ex) {
                                Logger.getLogger(TCPConnectionDataReaderThread.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}