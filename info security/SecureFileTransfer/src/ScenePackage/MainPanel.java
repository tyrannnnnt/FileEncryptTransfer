package ScenePackage;

import FileTransfer.FileTransferClient;
import FileTransfer.FileTransferServer;

import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class MainPanel extends JPanel {

    private static MainPanel instance = new MainPanel();
    // server start button
    public JButton btnStartServer;
    // client start button
    public JButton btnStartClient;
    // input file path field
    public JLabel selectFile;
    public JTextField filePathField;
    //input IP address
    public JLabel IPAddressLabel;
    public JTextField IPAddressField;
    //info console of GUI
    public JTextArea textAreaClient;
    public JTextArea textAreaServer;

    /**
     * Create the panel.
     */
    private MainPanel() {
        setLayout(null);

        //server label
        JLabel lblNewLabel = new JLabel("Server");
        lblNewLabel.setVerticalAlignment(SwingConstants.TOP);
        lblNewLabel.setBounds(187, 13, 102, 38);
        lblNewLabel.setBackground(SystemColor.activeCaption);
        add(lblNewLabel);

        //client label
        JLabel lblNewLabel_1 = new JLabel("Client");
        lblNewLabel_1.setBounds(703, 6, 83, 46);
        add(lblNewLabel_1);

        //separator
        JSeparator separator = new JSeparator();
        separator.setOrientation(SwingConstants.VERTICAL);
        separator.setBounds(490, 0, 1, 700);
        add(separator);

        //start server button
        btnStartServer = new JButton("Start Server");
        btnStartServer.setForeground(SystemColor.textHighlight);
        btnStartServer.setBackground(SystemColor.activeCaption);
        btnStartServer.setBounds(14, 65, 113, 27);
        add(btnStartServer);

        //start client button
        btnStartClient = new JButton("Start Client");
        btnStartClient.setForeground(SystemColor.textHighlight);
        btnStartClient.setBackground(SystemColor.activeCaption);
        btnStartClient.setBounds(514, 65, 113, 27);
        add(btnStartClient);

        // input IP address label
        IPAddressLabel = new JLabel("Input IP address: ");
        IPAddressLabel.setForeground(SystemColor.textHighlight);
        IPAddressLabel.setBackground(SystemColor.activeCaption);
        IPAddressLabel.setBounds(514, 100, 113, 27);
        add(IPAddressLabel);

        //input IP address field
        IPAddressField = new JTextField();
        IPAddressField.setForeground(SystemColor.textHighlight);
        IPAddressField.setBackground(SystemColor.activeCaption);
        IPAddressField.setBounds(610, 100, 150, 27);
        add(IPAddressField);

        // file path label
        selectFile = new JLabel("Input File path: ");
        selectFile.setForeground(SystemColor.textHighlight);
        selectFile.setBackground(SystemColor.activeCaption);
        selectFile.setBounds(514, 140, 113, 27);
        add(selectFile);

        //input file path field
        filePathField = new JTextField();
        filePathField.setForeground(SystemColor.textHighlight);
        filePathField.setBackground(SystemColor.activeCaption);
        filePathField.setBounds(610, 140, 150, 27);
        add(filePathField);

        //client area console
        textAreaClient = new JTextArea();
        textAreaClient.setBounds(600, 180, 300, 400);
        textAreaClient.setEditable(false);
        textAreaClient.setLineWrap(true);
        add(textAreaClient);

        //server area console
        textAreaServer = new JTextArea();
        textAreaServer.setBounds(50, 180, 300, 400);
        textAreaServer.setEditable(false);
        textAreaServer.setLineWrap(true);
        add(textAreaServer);

        serverStartListener();
        clientStartListener();
    }

    /**
     * server start button action listener
     */
    public void serverStartListener() {
        btnStartServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    textAreaServer.paintImmediately(textAreaServer.getBounds());
                    System.out.println(textAreaServer.getText());
                    //start server
                    FileTransferServer server = new FileTransferServer();
                    System.out.println("Server launch");
                    new Thread(() ->{
                        try{
                            server.load();
                        }catch (Exception exception){
                            System.out.println(exception);
                        }
                    }).start();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
        });
    }

    /**
     * Client start button action listener
     */
    public void clientStartListener() {
        btnStartClient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    //start client
                    FileTransferClient.SERVER_IP = IPAddressField.getText();
                    FileTransferClient client = new FileTransferClient();
                    System.out.println("Client launch");
                    new Thread(() ->{
                        try{
                            client.start(filePathField.getText(), IPAddressField.getText());
                        }catch (Exception exception){
                            System.out.println(exception);
                        }
                    }).start();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
        });
    }

    /**
     * get instance of the MainPanel
     * @return MainPanel instance
     */
    public static MainPanel getInstance(){
        return instance;
    }

    /**
     * add text into server text area
     * @param newStr
     */
    public void addTextServer(String newStr){
        this.textAreaServer.append("\n"+newStr);
    }

    /**
     * add text into client text area
     * @param newStr
     */
    public void addTextClient(String newStr){
        this.textAreaClient.append("\n"+newStr);
    }
}