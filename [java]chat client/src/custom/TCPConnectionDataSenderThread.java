package custom;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class TCPConnectionDataSenderThread extends Thread {

    public String connectionType;
    public String ipAddress;
    public int portNumber;
    private PrintWriter printWriter;

    @Override
    public void run() {
        try {
            this.setName("TCPConnectionDataSenderThread");
            while (true) {
                MainController.appLogContent.add("TCPConnectionDataSenderThread bol spustený ");
                while (MainController.getSocketProvider().getSocket() == null) {
                    /*čakanie na to kým socket objekt bude existovať*/
                    Thread.sleep(500);
                }
                while (!MainController.getSocketProvider().getSocket().isConnected()) {
                    Thread.sleep(500);
                }
                if (MainController.getSocketProvider().getSocket() != null) {
                    printWriter = new PrintWriter(MainController.getSocketProvider().getSocket().getOutputStream(), true);
                }
                while (MainController.getSocketProvider().getSocket() != null) {
                    if (MainController.getSocketProvider().getSocket().isConnected()) {
                        if (MainController.sendMessage) {
                            MainController.appLogContent.add("TCPConnectionDataSenderThread - správa sa odosiela");
                            MainController.chatLogContent.add(new ChatEntry(MainController.getSocketProvider().getSocket().getLocalAddress() + ":" + MainController.getSocketProvider().getSocket().getLocalPort(), MainController.messageContent, "outcoming"));
                            if (MainController.useMessageEncryption) {
                                byte[] encryptedMessageContent = AdvancedEncryptionStandard.getInstance().encrypt(MainController.messageContent.getBytes(StandardCharsets.UTF_8));
                                JSONObject jsonObject = new JSONObject();
                                for (int x = 0; x < encryptedMessageContent.length; x++) {
                                    jsonObject.put(Integer.toString(x), encryptedMessageContent[x]);
                                }
                                printWriter.println(jsonObject.toString());
                            } else {
                                printWriter.println(MainController.messageContent);
                            }
                            MainController.messageContent = "";
                            MainController.sendMessage = false;
                            MainController.appLogContent.add("správa bola odoslaná");
                        }
                        if (MainController.sentCloseMessageState.equals("waiting")) {
                            printWriter.println("0FS0AFJ01F098U09210FOASIJFOSAIJFOIAJSFOIJASOFIJASOIFJOIASFOIJASF");
                            MainController.appLogContent.add(MainController.getSocketProvider().getSocket().getInetAddress() + ":" + MainController.getSocketProvider().getSocket().getPort() + " bola odoslaná informácia o ukončení pripojenia");
                            MainController.sentCloseMessageState = "done";
                        }
                        Thread.sleep(100);
                    }
                }
                MainController.appLogContent.add("TCPConnectionDataSenderThread zistil že socket neexistuje");
            }
        } catch (InterruptedException | IOException e) {
            MainController.appLogContent.add(e.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(TCPConnectionDataSenderThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}