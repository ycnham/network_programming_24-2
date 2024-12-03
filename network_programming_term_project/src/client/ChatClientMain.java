// ConnectionPanel을 호출하고 창 전환 작업만 처리하도록 단순화
package client;

import java.awt.EventQueue;
import javax.swing.JFrame;
import ui.ConnectionPanel;

public class ChatClientMain extends JFrame {

    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                ChatClientMain frame = new ChatClientMain();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public ChatClientMain() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 254, 321);

        // UI 연결 패널 생성 및 추가
        ConnectionPanel connectionPanel = new ConnectionPanel(this);
        setContentPane(connectionPanel);
    }
}
