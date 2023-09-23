package ScenePackage;

import javax.swing.JFrame;

public class MainFrame extends JFrame{
    public static MainFrame instance = new MainFrame();

    private MainFrame(){
        this.setSize(1000, 710);
        this.setContentPane(MainPanel.getInstance());
        this.setLocationRelativeTo(null);   //set in the middle of the window
        this.setTitle("Secure Transport");
        //cannot change size
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        instance.setVisible(true);
    }
}