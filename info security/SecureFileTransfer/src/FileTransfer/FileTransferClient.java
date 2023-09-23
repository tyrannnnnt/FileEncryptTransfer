package FileTransfer;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;

import ScenePackage.MainPanel;
import Security.DHCoder_EKE;
import Security.MD5DS;
import org.apache.commons.codec.binary.Base64;

/**
 * File transfer client
 */
public class FileTransferClient extends Socket {

    public static String SERVER_IP = ""; // server IP
    private static final int SERVER_PORT = 8899; // port number

    private BigInteger a;   //generated bigInteger a from client
    private BigInteger p;   //generated bigInteger p from client
    private BigInteger g;   //generated bigInteger g from client
    private BigInteger A;   //calculated bigInteger A (A=g^a(modp))
    private BigInteger B;   //received B from server
    private String CB;      //received challenge number from server
    private BigInteger CA;  //generated random challenge number

    //identifier numbers of messages
    final int fileIdentifier = 2222;
    final int DSIdentifier = 3333;
    final int pIdentifier = 4444;
    final int gIdentifier = 5555;
    final int AIdentifier = 6666;
    final int BIdentifier = 7777;
    final int CBIdentifier = 8888;
    final int CA_CBIdentifier = 9999;
    final int CAIdentifier = 2555;

    private Socket client;          //client socket
    private DataOutputStream dos;   //client data output stream
    private DataInputStream dis;    //client data input stream
    private String sigA;            //generated digital signature from client

    private Key secretKeyA;     //generated secret key of client

    /**
     * generate big integer a, p, g, calculate A
     * @throws NoSuchAlgorithmException
     * @throws InvalidParameterSpecException
     */
    private void generateAPG() throws NoSuchAlgorithmException, InvalidParameterSpecException {
        a = DHCoder_EKE.getA();
        BigInteger[] arr = DHCoder_EKE.getP_G();
        p = arr[0];
        g = arr[1];
        A = g.modPow(a, p); // calculated public server key (A=g^a(modp))
    }

    /**
     * Send p, g, A to server
     */
    private void sendAPG(){
        try {
            dos = new DataOutputStream(client.getOutputStream());
            sendStr(p.toString(), pIdentifier);
            sendStr(g.toString(), gIdentifier);
            sendStr(A.toString(), AIdentifier);
            MainPanel.getInstance().addTextClient("A g p has been send to server.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * connect to server
     * @throws Exception
     */
    public FileTransferClient() throws Exception {
        super(SERVER_IP, SERVER_PORT);
        this.client = this;
        MainPanel.getInstance().addTextClient("Client[port:" + client.getLocalPort() + "] connect to server successful!");
        System.out.println("Client[port:" + client.getLocalPort() + "] connect to server successful!");
    }

    /**
     * receive and handle message received from server
     * @param client    socket of the client
     * @param filePath  input file path
     */
    private void receiveMsg(Socket client, String filePath){
        try{
            //initial data input stream of client
            dis = new DataInputStream(client.getInputStream());
            while (true){
                //read msg length
                int len = dis.readInt();
                //read msg identifier
                int specifier = dis.readInt();

                switch (specifier){
                    //receive B from server
                    case BIdentifier:
                        B = new BigInteger(dis.readUTF());
                        BigInteger calculatedB_a = B.modPow(a, p);
                        //generate secret key in client
                        secretKeyA = DHCoder_EKE.generateKey(calculatedB_a.toByteArray());
                        //print just for test man, for security element should be deleted.
                        System.out.println("secretKeyA: " + Base64.encodeBase64String(secretKeyA.getEncoded()));
                        //print in GUI
                        MainPanel.getInstance().addTextClient("A secret key has been generated.");
                        break;

                    //receive challenge number B from the server
                    case CBIdentifier:
                        CB = new String(DHCoder_EKE.decrypt(Base64.decodeBase64(dis.readUTF()),secretKeyA));
                        CA = DHCoder_EKE.getA();
                        //print just for test man, for security element should be deleted.
                        System.out.println("Client received CB: " + CB);
                        System.out.println("CA: " + CA);
                        //send encrypted (CA||CB) to server
                        sendStr(Base64.encodeBase64String(DHCoder_EKE.encrypt((CA + " " + CB).getBytes(), secretKeyA)), CA_CBIdentifier);
                        //print in GUI
                        MainPanel.getInstance().addTextClient("Encrypted CB+CA has been sent.");
                        break;

                    //receive challenge number A form the server
                    case CAIdentifier:
                        //decrypt received challenge number A
                        String receivedCA = new String(DHCoder_EKE.decrypt(Base64.decodeBase64(dis.readUTF()),secretKeyA));
                        //print just for test man, for security element should be deleted.
                        System.out.println("received CA: " + receivedCA);
                        //check received CA legal or not
                        if(!receivedCA.equals(CA.toString())){
                            MainPanel.getInstance().addTextClient("Check CA failed!");
                            client.close();
                            break;
                        }
                        MainPanel.getInstance().addTextClient("Authentication check pass.");
                        //send file
                        sendFile(filePath);
                        //generate digital signature
                        generateSigA(filePath);
                        //send digital signature to server
                        sendDS();
                        break;
                    default:
                        System.out.println("Unknown identifier!");
                        MainPanel.getInstance().addTextClient("Received unknown message.");
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * send string with identifier to server
     * @param str   the message need to be sent
     * @param identifier    the message identifier
     */
    private void sendStr(String str, int identifier){
        try {
            dos.writeInt(str.length());
            dos.writeInt(identifier);
            dos.writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * send digital signature to server
     */
    private void sendDS(){
        try {
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            out.writeInt(sigA.length());
            out.writeInt(DSIdentifier);
            out.writeUTF(sigA);
            MainPanel.getInstance().addTextClient("Digital signature has been sent!");
        }catch (Exception e){
            System.out.println(e);
        }
    }

    /**
     * send file to server
     */
    private void sendFile(String filePath){
        try {
            String fileContent = FileManage.getInstance().readFile(filePath);
            //encrypt file content
            String encryptedContent = Base64.encodeBase64String(DHCoder_EKE.encrypt(fileContent.getBytes(), secretKeyA));
            dos = new DataOutputStream(client.getOutputStream());
            dos.writeInt(encryptedContent.length());
            dos.flush();
            dos.writeInt(fileIdentifier);
            dos.flush();
            dos.writeUTF(FileManage.getInstance().getFileName(filePath));
            dos.flush();
            byte[][] split;
            // split file to 256 bytes max
            if((split = chunkArray(encryptedContent.getBytes(), 256)) != null) {
                for(int i = 0; i < split.length; i++) {
                    // send split packet
                    dos.writeUTF(new String(split[i]));
                }
            }
            dos.writeUTF("");
            dos.flush();
            MainPanel.getInstance().addTextClient("Encrypted file has been sent!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[][] chunkArray(byte[] array, int chunkSize) {
        int numOfChunks = (int)Math.ceil((double)array.length / chunkSize);
        byte[][] output = new byte[numOfChunks][];

        for(int i = 0; i < numOfChunks; ++i) {
            int start = i * chunkSize;
            int length = Math.min(array.length - start, chunkSize);

            byte[] temp = new byte[length];

            System.arraycopy(array, start, temp, 0, length);

            output[i] = temp;
        }
        return output;
    }

    /**
     * generate file digital signature
     * @param filePath  file path
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    private void generateSigA(String filePath) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        sigA = MD5DS.getDigest(new FileInputStream(filePath), md, 2048);
        MainPanel.getInstance().addTextClient("Digital signature");
    }

    /**
     * initial transfer client
     * @param filePath sent file path
     */
    public void start(String filePath, String IP){
        File file = new File(filePath);
        if (file.exists()) {
            System.out.println("A: transfer: " + file.getName() + "\tfile path: " + file.getAbsolutePath() + "\tfile size: "
                    + file.length() + "byte");
        } else {
            System.out.println("The file do not exist!");
        }
        try{
            SERVER_IP = IP;
            generateAPG();
            sendAPG();
            receiveMsg(client, filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
