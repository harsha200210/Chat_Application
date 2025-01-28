package lk.ijse.chatapplicationexample;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerController {

    @FXML
    private TextArea serverTextArea;

    ServerSocket serverSocket;
    Socket socket;
    DataOutputStream dataOutputStream;
    DataInputStream dataInputStream;

    ServerSocket serverSocket1;
    Socket socket1;
    DataOutputStream dataOutputStream1;
    DataInputStream dataInputStream1;

    public void initialize() {
        try {
            serverSocket = new ServerSocket(3001);
            socket = serverSocket.accept();
            serverTextArea.appendText("\nclient Accept");

            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());

            serverSocket1 = new ServerSocket(3002);
            socket1 = serverSocket1.accept();
            serverTextArea.appendText("\nclient1 Accept");

            dataOutputStream1 = new DataOutputStream(socket1.getOutputStream());
            dataInputStream1 = new DataInputStream(socket1.getInputStream());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        new Thread(() -> {
            try {
                while (true) {
                    if (dataInputStream != null) {
                        String dataType = dataInputStream.readUTF();

                        if (dataType.equals("text")){
                            String outPutMessage = dataInputStream.readUTF();
                            send(outPutMessage,dataOutputStream1);
                        } else if (dataType.equals("image")){
                            int imageSize = dataInputStream.readInt();

                            byte[] imageBytes = new byte[imageSize];
                            dataInputStream.readFully(imageBytes);

                            String filePath = "src/main/resources/lk/ijse/chatapplicationexample/store/received_image.jpg";
                            FileOutputStream fos = new FileOutputStream(filePath);
                            fos.write(imageBytes);
                            fos.close();
                            serverTextArea.appendText("\nsave image");
                            sendImage(dataOutputStream1);
                        } else if (dataType.equals("file")) {
                            int fileSize = dataInputStream.readInt();

                            String filePath = "src/main/resources/lk/ijse/chatapplicationexample/store/received_file.txt";

                            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                int totalBytesRead = 0;

                                if (totalBytesRead < fileSize) {
                                    bytesRead = dataInputStream.read(buffer);

                                    if (bytesRead == -1) {
                                        System.out.println("Unexpected end of stream. Total bytes read: " + totalBytesRead);
                                        break;
                                    }

                                    fos.write(buffer, 0, bytesRead);
                                    totalBytesRead += bytesRead;
                                    System.out.println("Bytes read: " + bytesRead + ", Total bytes read: " + totalBytesRead);
                                    fos.close();
                                    serverTextArea.appendText("\nsave file");
                                }

                                if (totalBytesRead < fileSize) {
                                    System.out.println("Warning: File transfer incomplete. Expected: " + fileSize + ", Received: " + totalBytesRead);
                                }
                                sendFile(dataOutputStream1);
                            } catch (IOException e) {
                                e.printStackTrace();
                                return;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error receiving or displaying image: " + e.getMessage());
            }
        }).start();

        new Thread(() -> {
            try {
                while (true) {
                    if (dataInputStream1 != null) {
                        String dataType = dataInputStream1.readUTF();

                        if (dataType.equals("text")){
                            String outPutMessage = dataInputStream1.readUTF();
                            send(outPutMessage,dataOutputStream);
                        } else if (dataType.equals("image")){
                            int imageSize = dataInputStream1.readInt();

                            byte[] imageBytes = new byte[imageSize];
                            dataInputStream1.readFully(imageBytes);

                            String filePath = "src/main/resources/lk/ijse/chatapplicationexample/store/received_image.jpg";
                            FileOutputStream fos = new FileOutputStream(filePath);
                            fos.write(imageBytes);
                            fos.close();
                            serverTextArea.appendText("\nsave image");
                            sendImage(dataOutputStream);
                        } else if (dataType.equals("file")) {
                            int fileSize = dataInputStream1.readInt();

                            String filePath = "src/main/resources/lk/ijse/chatapplicationexample/store/received_file.txt";

                            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                int totalBytesRead = 0;

                                if (totalBytesRead < fileSize) {
                                    bytesRead = dataInputStream1.read(buffer);

                                    if (bytesRead == -1) {
                                        System.out.println("Unexpected end of stream. Total bytes read: " + totalBytesRead);
                                        break;
                                    }

                                    fos.write(buffer, 0, bytesRead);
                                    totalBytesRead += bytesRead;
                                    System.out.println("Bytes read: " + bytesRead + ", Total bytes read: " + totalBytesRead);
                                    fos.close();
                                    serverTextArea.appendText("\nsave file");
                                }

                                System.out.println("File successfully saved at: " + filePath);

                                if (totalBytesRead < fileSize) {
                                    System.out.println("Warning: File transfer incomplete. Expected: " + fileSize + ", Received: " + totalBytesRead);
                                }
                                sendFile(dataOutputStream);
                            } catch (IOException e) {
                                e.printStackTrace();
                                return;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error receiving or displaying image: " + e.getMessage());
            }
        }).start();
    }

    public void send(String message, DataOutputStream dataOutputStream) {
        try {
            dataOutputStream.writeUTF("text");
            dataOutputStream.writeUTF(message);
            dataOutputStream.flush();
            serverTextArea.appendText("\nsend message");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void sendFile(DataOutputStream dataOutputStream) {
        try {
            FileInputStream fileInputStream = new FileInputStream("src/main/resources/lk/ijse/chatapplicationexample/store/received_file.txt");

            byte[] buffer = new byte[4096];
            int bytesRead;

            dataOutputStream.writeUTF("file");

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytesRead);
            }
            dataOutputStream.flush();

            serverTextArea.appendText("\nsend file");

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendImage(DataOutputStream dataOutputStream) {
        try {
            FileInputStream fis = new FileInputStream("src/main/resources/lk/ijse/chatapplicationexample/store/received_image.jpg");
            byte[] imageBytes = fis.readAllBytes();

            dataOutputStream.writeUTF("image");
            dataOutputStream.writeInt(imageBytes.length);
            dataOutputStream.flush();

            dataOutputStream.write(imageBytes); // Send image data
            dataOutputStream.flush();

            fis.close();
            serverTextArea.appendText("\nImage sent to client.");
        } catch (IOException e) {
            e.printStackTrace();
            serverTextArea.appendText("\nError sending image: " + e.getMessage());
        }
    }

}
