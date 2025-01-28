package lk.ijse.chatapplicationexample;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.Socket;

public class Client1Controller {

    @FXML
    private TextArea client1TextArea;

    @FXML
    private TextArea client1FileTextArea;

    @FXML
    private ImageView client1ImageView;

    @FXML
    private TextField client1TextFeild;

    @FXML
    private TextField client1UserName;

    Socket socket;
    DataOutputStream dataOutputStream;
    DataInputStream dataInputStream;

    public void initialize() {
        try{
            socket = new Socket("localhost",3001);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        new Thread(() -> {
            try {
                while (true) {
                    if (dataInputStream != null) {
                        String dataType = dataInputStream.readUTF();

                        if (dataType.equals("text")){
                            String outPutMessage = dataInputStream.readUTF();
                            client1TextArea.appendText("\n" + outPutMessage);
                        } else if (dataType.equals("image")){
                            int imageSize = dataInputStream.readInt();

                            byte[] imageBytes = new byte[imageSize];
                            dataInputStream.readFully(imageBytes);

                            String filePath = "src/main/resources/lk/ijse/chatapplicationexample/store/received_image.jpg";
                            FileOutputStream fos = new FileOutputStream(filePath);
                            fos.write(imageBytes);
                            fos.close();

                            File imageFile = new File(filePath);
                            if (imageFile.exists()) {
                                Image image = new Image(imageFile.toURI().toString()); // Convert file path to URI

                                // Update the ImageView on the JavaFX Application Thread
                                Platform.runLater(() -> {
                                    client1ImageView.setImage(image);
                                    System.out.println("Image displayed in ImageView.");
                                });
                            } else {
                                System.out.println("Image file not found at: " + filePath);
                            }
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
                                    showFileDialog();
                                    fos.close();
                                }

                                System.out.println("File successfully saved at: " + filePath);

                                if (totalBytesRead < fileSize) {
                                    System.out.println("Warning: File transfer incomplete. Expected: " + fileSize + ", Received: " + totalBytesRead);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                client1FileTextArea.setText("Failed to save file: " + e.getMessage());
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

    private void showFileDialog(){
        String filePath = "src/main/resources/lk/ijse/chatapplicationexample/store/received_file.txt";
        File file = new File(filePath);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            client1FileTextArea.clear();
            client1FileTextArea.setText(content.toString());
            System.out.println("File content displayed in TextArea.");
        } catch (IOException e) {
            e.printStackTrace();
            client1FileTextArea.setText("Failed to load file: " + e.getMessage());
        }
    }

    public boolean checkUserNameField() {
        if (!client1UserName.getText().equals("") && !client1UserName.getText().isEmpty()){
            return true;
        }
        return false;
    }

    @FXML
    void client1SendBtnOnAction(ActionEvent event) {
        if (checkUserNameField()) {
            try {
                String message = client1TextFeild.getText();
                client1TextArea.appendText("\n" +client1UserName.getText() + " : " + message);

                dataOutputStream.writeUTF("text");
                dataOutputStream.writeUTF(client1UserName.getText() + " : " + message);
                dataOutputStream.flush();

                client1TextFeild.clear();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else {
            new Alert(Alert.AlertType.WARNING,"Please Set User Name !!").show();
        }
    }

    @FXML
    void client1SendFileOnAction(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select a File");

            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt")
            );

            File selectedFile = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());

            if (selectedFile != null) {
                System.out.println("Selected file: " + selectedFile.getAbsolutePath());

                FileInputStream fileInputStream = new FileInputStream(selectedFile.getAbsolutePath());

                byte[] buffer = new byte[4096];
                int bytesRead;

                dataOutputStream.writeUTF("file");

                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    dataOutputStream.write(buffer, 0, bytesRead);
                }
                dataOutputStream.flush();

                client1TextArea.appendText("\nFile sent.");

            } else {
                System.out.println("No file selected.");
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    void client1SendImageBtnOnAction(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select an Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );

            File selectedFile = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());

            if (selectedFile != null) {
                String imagePath = selectedFile.getAbsolutePath();
                System.out.println("Selected image path: " + imagePath);

                FileInputStream fis = new FileInputStream(imagePath);
                byte[] imageBytes = fis.readAllBytes();

                dataOutputStream.writeUTF("image");
                dataOutputStream.writeInt(imageBytes.length);
                dataOutputStream.flush();

                dataOutputStream.write(imageBytes); // Send image data
                dataOutputStream.flush();

                fis.close();
                client1TextArea.appendText("\nImage sent.");
            } else {
                System.out.println("No file was selected.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            client1TextArea.appendText("\nError sending image: " + e.getMessage());
        }
    }

    @FXML
    void client1DisconnectFileOnAction(ActionEvent event) {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
