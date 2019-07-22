/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mazitekgh;

import com.fazecast.jSerialComm.SerialPort;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

/**
 *
 * @author Zakaria
 */
public class FXMLDocumentController implements Initializable {

    SerialPort spConnected;

    @FXML
    private Label disp;

    @FXML
    private ComboBox<String> portCombo;

    @FXML
    private Label notifyLabel;

    @FXML
    private Button connectButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        connectButton.setDisable(true);

        //set combobox click handler
        portCombo.setOnMouseClicked((eh) -> {
            portComboAction();
        });
        connectButton.setOnAction((event) -> {
            handleConnectButtonAction(event);
        });
    }

    /**
     * displays available COM port in ComboBox
     */
    private void portComboAction() {
        portCombo.getItems().clear();
        SerialPort[] cmPorts = SerialPort.getCommPorts();

        if (cmPorts.length < 1) {
            connectButton.setDisable(true);
            return;
        } else {
            connectButton.setDisable(false);
        }

        Platform.runLater(() -> {

            for (SerialPort comPort : cmPorts) {
                portCombo.getItems().add(comPort.getDescriptivePortName());
            }
        });

    }

    private void handleConnectButtonAction(ActionEvent event) {

        //Disconnect the port if its connected
        if (spConnected != null && spConnected.isOpen()) {
            spConnected.closePort();
            spConnected = null;
            notifyLabel.setText("Disconnected");
            connectButton.setText("Connect");
            return;
        }

        SerialPort sp = SerialPort.getCommPorts()[portCombo.getItems().indexOf(portCombo.getValue())];
        if (sp == null) {
            notifyLabel.setText("Please select a Port");
            return;
        }

        if (connect(sp)) {

            spConnected = sp;
            spConnected.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
            new Thread(() -> {

                while (true) {
                    try {
                        InputStream portInputStream = sp.getInputStream();
                        if (portInputStream == null) {
                            //   isRun = false;
                            break;
                        }
                        BufferedReader bufferedReader
                                = new BufferedReader(new InputStreamReader(
                                                portInputStream));

                        String data = bufferedReader.readLine();
                        //debug
                        System.out.println(".serialEvent() " + data);

                        // we are in a different thread so update the label on ui thread
                        Platform.runLater(() -> {
                            disp.setText(data.trim() + " cm");
                        });

                        Thread.sleep(500);
                    } catch (IOException ex) {
                        
                        System.out.println(".serialEvent() time out no data read");
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }).start();

            notifyLabel.setText("Connected");
            connectButton.setText("Disconnect");
        } else {
            //show error
            notifyLabel.setText("Cant Connect");
            spConnected = null;
        }

    }

    /**
     * use to send info to the connect device
     *
     * @param data String the data to send
     * @return boolean true if successful
     */
    boolean sendData(String data) {
        if (spConnected == null || !spConnected.isOpen()) {
            return false;
        }

        OutputStream os = spConnected.getOutputStream();

        try {

            os.write(data.getBytes());

        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    /**
     * Connect to the specified serial port
     *
     * @param sp the SerialPort to connect to
     * @return boolean - true if connection is successful
     */
    private boolean connect(SerialPort sp) {

        if (sp.isOpen()) {
            //close before connection
            sp.closePort();

        }
        return sp.openPort();

    }

    /**
     * Called when the app is about to close
     */
    public void shutdown() {
        if (spConnected != null) {
            spConnected.closePort();
            spConnected = null;

        }
        Platform.exit();
    }

}
