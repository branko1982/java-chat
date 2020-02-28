package custom;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.regex.PatternSyntaxException;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;

public class MainController implements Initializable {

    //nájsť týpka z číny, otestovať s ním tento program
    @FXML
    public GridPane gridPane1;
    @FXML
    public TextField remoteHostAddress;
    @FXML
    public TextField remotePortNumber;
    @FXML
    public Button connectToServerButton;
    @FXML
    public Button sendMessageButton;
    @FXML
    public CheckBox allowIncomingConnectionsCheckbox;
    @FXML
    public TextField localPortNumber;
    @FXML
    public TextField messageToSend;
    @FXML
    public Label myAddressValue;
    @FXML
    public TextField shittyURL;
    @FXML
    public TextArea chatLog;
    @FXML
    public TextArea applicationLog;
    @FXML
    public Label LANaddressLabel;
    @FXML
    public Label LANAddressValue;
    @FXML
    public Label connectionStateValue;
    @FXML
    public TextField messageEncryptionPassphraseValue;
    @FXML
    public CheckBox useEncryptionPassphraseCheckbox;

    public static String remoteHost = "";
    public static String remotePort = "";

    public static String myPort;
    public static String myAddress;

    public String clientPort;
    public String clientAddress;

    public PrintWriter printWriter;
    public BufferedReader bufferedReader;
    public InputStreamReader inputStreamReader;

    public static String connectionType = "";
    /* TCPConnectionDataReader kontroluje hodnotu  listenForConnection, a podľa nej zisťuje či má TCPconenctionDataReaderThread spustit pripojenie pre klient/server,alebo len čakať kým ho spustí.
        Nastavuje sa v MainControlleri, s hľadiska dizajnu je to najlepšie riešenie ktoré ma napadlo.
     */
    public static boolean listenForConnection = false;
    public static ArrayList<ChatEntry> chatLogContent = new ArrayList<ChatEntry>();
    public static ArrayList<String> appLogContent = new ArrayList<String>();
    public static SocketProvider socketProvider;
    public static boolean sendMessage = false;
    public static String connectionState = "notConnected";  // používa sa ako kontrola v MainController triede ,pri odoslaní správy, správa sa odošle len keď je connectionACtive true. Tak isto server sa vypne len keď je connectionActive tru
    public static String messageContent = "";
    public static String sentCloseMessageState = "false";
    private String messageEncryptionPassphraseString = "";
    public static boolean useMessageEncryption = false;

    @FXML
    public void connectToServerButton_onClick() {
        if (connectionState.equals("notConnected")) {
            remoteHost = remoteHostAddress.getText();
            remotePort = remotePortNumber.getText();
            if (!remoteHost.isEmpty() && !remotePort.isEmpty()) {
                if (remotePort.matches("[0-9]+")) {
                    if (Integer.parseInt(remotePort) > 0 && Integer.parseInt(remotePort) < 65536) {
                        try {
                            String[] splitArray = remoteHost.split("\\.");
                            if (splitArray.length == 4) {
                                boolean ipAddressValidated = true;
                                for (int x = 0; x < splitArray.length; x++) {

                                    if (splitArray[x].matches("[0-9]+")) {
                                        if ((Integer.parseInt(splitArray[x]) < 0) || (Integer.parseInt(splitArray[x]) > 255)) {
                                            ipAddressValidated = false;
                                        }
                                    } else {
                                        ipAddressValidated = false;
                                        appLogContent.add(x + ".té číslo Ip adresy: " + splitArray[x]);
                                    }
                                }
                                if (ipAddressValidated) {
                                    appLogContent.add("IP adresa a port vvyzerájú byť správne. Nastavuje sa clientListener na : clientListener a listenForConnection : true");
                                    connectionState = "waitingForConnection";
                                    connectionType = "clientListener";
                                    connectionStateValue.setText("connecting");
                                    listenForConnection = true;
                                    //čas do kedy sa Socket musí napojit...nedalo sa to urobit cez thread.
                                    int x = 0;
                                    while (!connectionState.equals("connected") && x < 20) {
                                        try {
                                            x++;
                                            appLogContent.add("čaka sa na vytvorenie spojenia ... " + x);
                                            Thread.sleep(500);

                                        } catch (InterruptedException ex) {
                                            System.out.println(ex.getMessage());
                                        }
                                    }

                                } else {
                                    appLogContent.add("\"ip adresa má nesprávny formát.");

                                }
                            } else {
                                appLogContent.add("\"ip adresa má nesprávny formát. \" + splitArray.length");

                            }
                        } catch (PatternSyntaxException ex) {
                            appLogContent.add("Bola vyhodená exception: " + ex.getMessage());
                        }
                    } else {
                        appLogContent.add("vzdialený port je v nesprávnom formáte, port nemôže mať zadanú hodnotu: " + remotePort + " . ");
                    }
                } else {
                    appLogContent.add("vzdialený port je v nesprávnom formáte, musí obsahovať len čísla");
                }
            } else {
                appLogContent.add("ip adresa alebo port su prázdne");
            }
        } else if (connectionState.equals("waitingForConnection")) {
            appLogContent.add("ruší sa pokus o pripojenie sa na server");
            connectionState = "notConnected";
            connectionType = "";
            listenForConnection = false;
            socketProvider.closeConnection();
        } else if (connectionState.equals("connected")) {
            appLogContent.add("ruší sa pokus o pripojenie sa na server");
            sentCloseMessageState = "waiting";
            while (sentCloseMessageState.equals("waiting")) {
                System.out.println("waiting");
            }
            if (sentCloseMessageState.equals("done")) {
                connectionState = "notConnected";
                connectionType = "";
                listenForConnection = false;
                socketProvider.closeConnection();
                localPortNumber.setText("");
            }
        }
    }

    @FXML
    public void useEncryptionPassphraseCheckbox_onAction(ActionEvent event) {
        if (messageEncryptionPassphraseString.length() == 0) {
            ((CheckBox) event.getSource()).setSelected(false);
        }
        if (((CheckBox) event.getSource()).isSelected()) {
            useMessageEncryption = true;
            int stringEnd = (messageEncryptionPassphraseString.length() > 16 ? 15 : messageEncryptionPassphraseString.length());
            appLogContent.add("šifrovanie je povolené, string s ktorým sa šifrujú I/O správy je " + new String(messageEncryptionPassphraseString).substring(0, stringEnd));
        } else {
            messageEncryptionPassphraseString = "";
            useMessageEncryption = false;
            messageEncryptionPassphraseValue.setText("");
        }
    }

    @FXML
    public void sendMessageButton_onClick() {
        sendMessage();
    }

    public void sendMessage() {
        appLogContent.add("Pokus o odoslanie správy");
        if (connectionState.equals("connected")) {
            if (messageToSend.getText().length() > 0) {
                sendMessage = true;
                messageContent = messageToSend.getText();
                messageToSend.setText("");
                appLogContent.add("správa by sa mala odoslať");
            } else {
                appLogContent.add("správa nemôže byť prázdna");
            }
        } else {
            appLogContent.add("správa sa nemôže odoslať pretože nieje vytvorené spojenie");
        }
    }

    @FXML
    public void allowIncomingConnectionsCheckbox_onClick() {
        if (allowIncomingConnectionsCheckbox.isSelected()) {
            appLogContent.add("Pokus o zapnutie chat servera");
            // zistiť kde zlyhýva opatované zapnutie servera
            myPort = localPortNumber.getText();
            if (!myPort.isEmpty()) {
                if (myPort.matches("[0-9]+")) {
                    if (Integer.parseInt(myPort) > 0 && Integer.parseInt(myPort) < 65536) {
                        listenForConnection = true;
                        connectionType = "serverListener";
                        appLogContent.add("Server by mal bežať");
                        connectionState = "waitingForConnection";
                    } else {
                        appLogContent.add("Port nieje v správnom formáte");
                    }
                } else {
                    appLogContent.add("Port nieje v správnom formáte");

                }
            } else {
                appLogContent.add("Nebol definovaný port ktorý server musí používať");
            }
        } else {
            appLogContent.add("chat server sa vypína");
            //pripojenie ale môžem zrušiť až keď DataSenderThread odošle informáciu o ukončení spojenia klientovy
            if (connectionState.equals("connected")) {
                sentCloseMessageState = "waiting";
                while (sentCloseMessageState.equals("waiting")) {
                    System.out.println("waiting");
                }
                if (sentCloseMessageState.equals("done")) {
                    listenForConnection = false;
                    socketProvider.closeConnection();
                }
            }
            connectionType = "";
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        socketProvider = new SocketProvider();
        TCPConnectionDataReaderThread tcpConnectionDataReaderThread = new TCPConnectionDataReaderThread();
        tcpConnectionDataReaderThread.start();
        TCPConnectionDataSenderThread tcpConnectionDataSenderThread = new TCPConnectionDataSenderThread();
        tcpConnectionDataSenderThread.start();
        new Thread() {
            String newChatLogContent = "";
            int chatLogContentCount = 0;

            /*Použil som anonymné vlákna pretože som nevidel lepšie riešenie,. Bolo potrebné meniť hodnotu @FXML elementov alebo aj node-ov , ako ich javaFX nazýva.
            Bez threadu to nebolo možné uskutočniť, a ak by bol thread v inej triede, napríklad vymedzenej len pre Thread, nevedel by som k @FXML objektom nijak pristúpit. Statické byť nemôžu.*/
            public void run() {
                try {
                    appLogContent.add("RefreshGUIThread beží");
                    while (true) {
                        newChatLogContent = "";
                        if (chatLogContentCount != chatLogContent.size()) {

                            chatLogContentCount = chatLogContent.size();
                            for (ChatEntry i : chatLogContent) {
                                newChatLogContent += i.getMessageSource() + "    " + i.getMessageContent() + "\n";
                            }
                            chatLog.setText(newChatLogContent);
                        }
                        Thread.sleep(100);

                    }
                } catch (Exception e) {
                    appLogContent.add(e.getMessage());
                }
            }
        }.start();

        new Thread() {
            String newAppLogContent = "";
            int appLogContentCount = 0;

            public void run() {
                try {
                    this.setName("Refresh Log Thread");
                    appLogContent.add("Refresh Log Thread sputený");
                    while (true) {
                        newAppLogContent = "";
                        if (appLogContentCount != appLogContent.size()) {
                            appLogContentCount = appLogContent.size();
                            for (int x = 0; x < MainController.appLogContent.size(); x++) {
                                newAppLogContent += appLogContent.get(x) + "\n";
                            }
                            applicationLog.setText(newAppLogContent);
                        }
                        Thread.sleep(100);
                    }
                } catch (Exception e) {
                    appLogContent.add(e.getMessage());
                }
            }
        }.start();
        //získanie LAN adresy
        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements()) {
                    InetAddress i = (InetAddress) ee.nextElement();
                    if (i.getHostAddress().substring(0, 7).equals("192.168")) {
                        LANAddressValue.setText(i.getHostAddress());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        new Thread() {
            private String updatedFor = "";

            @Override
            public void run() {
                appLogContent.add("další thread na aktualitovanie GUI - spustený");
                while (true) {
                    try {
                        if (MainController.connectionState.equals("connected") && !updatedFor.equals("connected")) {
                            appLogContent.add("GUI aktualizovaná");
                            remoteHostAddress.setText(socketProvider.getSocket().getInetAddress().toString());
                            remotePortNumber.setText(Integer.toString(socketProvider.getSocket().getPort()));
                            localPortNumber.setText(Integer.toString(socketProvider.getSocket().getLocalPort()));
                            remoteHostAddress.setEditable(false);
                            remotePortNumber.setEditable(false);
                            localPortNumber.setEditable(false);
                            remoteHostAddress.setStyle("-fx-background-color: #000000");
                            remotePortNumber.setStyle("-fx-background-color: #000000");
                            localPortNumber.setStyle("-fx-background-color: #000000");
                            updatedFor = "connected";
                        } else if (MainController.connectionState.equals("notConnected") && !updatedFor.equals("notConnected")) {
                            remoteHostAddress.setText("");
                            remotePortNumber.setText("");
                            localPortNumber.setText("");
                            remoteHostAddress.setStyle("-fx-background-color: #414141");
                            remotePortNumber.setStyle("-fx-background-color: #414141");
                            localPortNumber.setStyle("-fx-background-color: #414141");
                            remoteHostAddress.setEditable(true);
                            remotePortNumber.setEditable(true);
                            localPortNumber.setEditable(true);
                            updatedFor = "notConnected";
                        }
                        Thread.sleep(500);
                    } catch (InterruptedException | IllegalStateException e) {
                        appLogContent.add(e.getMessage());
                    }
                }
            }
        }.start();

    /*   new Thread() {

            private String updatedFor = "";
           @Override
            public void run() {
            while(true) {
                try
                {
                    if(!updatedFor.equals("notConnected") && MainController.connectionState.equals("notConnected")) {
                        connectToServerButton.setText("connect");
                        connectToServerButton.setStyle("-fx-background-color: #0020A3");
                        updatedFor = "notConnected";
                    }
                    else  if(!updatedFor.equals("waitingForConnection") && MainController.connectionState.equals("waitingForConnection")) {
                        connectToServerButton.setText("waiting for connection");
                        connectToServerButton.setStyle("-fx-background-color: #0020A3");
                        updatedFor = "waitingForConnection";
                    }
                    else  if(!updatedFor.equals("connected") && MainController.connectionState.equals("connected")) {
                        connectToServerButton.setText("disconnect");
                        connectToServerButton.setStyle("-fx-background-color: rgb(161, 31, 31);");
                        updatedFor = "connected";
                    }
                    Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        appLogContent.add(e.getMessage());
                    }
            }
            }
        }.start(); */
        messageEncryptionPassphraseValue.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (messageEncryptionPassphraseValue.getText().length() == 0) {
                useMessageEncryption = false;
                useEncryptionPassphraseCheckbox.setSelected(false);
            }
            messageEncryptionPassphraseString = messageEncryptionPassphraseValue.getText();
            byte[] keyBytes = messageEncryptionPassphraseString.getBytes(StandardCharsets.UTF_8);
            int arrayLength = (messageEncryptionPassphraseString.getBytes(StandardCharsets.UTF_8).length > 16 ? 16 : messageEncryptionPassphraseString.getBytes(StandardCharsets.UTF_8).length);

            byte[] key = new byte[16];
            for (int x = 0; x < 16; x++) {
                key[x] = 0;
            }
            for (int x = 0; x < arrayLength; x++) {
                key[x] = keyBytes[x];
            }
            AdvancedEncryptionStandard.getInstance().setKey(key);
        });
    }

    public static SocketProvider getSocketProvider() {
        return socketProvider;
    }

    @FXML
    public void button2_onClick() {
        try {
            appLogContent.add("získava sa IP adresa z :" + shittyURL.getText() + "");
            URL url = new URL(shittyURL.getText());
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.setDoOutput(true);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
            if (httpConnection.getResponseCode() == 200) {
                myAddress = bufferedReader.readLine();
                myAddressValue.setText(myAddress);
                appLogContent.add("host navrátil IP adresu ktorá je : " + myAddress);
            } else {
                appLogContent.add("získavanie IP adresu od zadaného hosta zlyhalo. http kód: " + httpConnection.getResponseCode());
            }

        } catch (Exception e) {
            e.getMessage();
        }
    }

    public void messageToSend_onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            sendMessage();
        }
    }
}