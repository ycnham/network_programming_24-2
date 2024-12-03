// ChatClientMain에서 전달받은 parentFrame을 사용해 창을 전환함
package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import client.ChatClientMain;

public class ConnectionPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTextField txtUserName;
    private JTextField txtIpAddress;
    private JTextField txtPortNumber;
    private ChatClientMain parentFrame;

    public ConnectionPanel(ChatClientMain parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(null);

        JLabel lblUserName = new JLabel("User Name");
        lblUserName.setBounds(12, 39, 82, 33);
        add(lblUserName);

        txtUserName = new JTextField();
        txtUserName.setHorizontalAlignment(SwingConstants.CENTER);
        txtUserName.setBounds(101, 39, 116, 33);
        add(txtUserName);
        txtUserName.setColumns(10);

        JLabel lblIpAddress = new JLabel("IP Address");
        lblIpAddress.setBounds(12, 100, 82, 33);
        add(lblIpAddress);

        txtIpAddress = new JTextField();
        txtIpAddress.setHorizontalAlignment(SwingConstants.CENTER);
        txtIpAddress.setText("127.0.0.1");
        txtIpAddress.setColumns(10);
        txtIpAddress.setBounds(101, 100, 116, 33);
        add(txtIpAddress);

        JLabel lblPortNumber = new JLabel("Port Number");
        lblPortNumber.setBounds(12, 163, 82, 33);
        add(lblPortNumber);

        txtPortNumber = new JTextField();
        txtPortNumber.setText("30000");
        txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
        txtPortNumber.setColumns(10);
        txtPortNumber.setBounds(101, 163, 116, 33);
        add(txtPortNumber);

        JButton btnConnect = new JButton("Connect");
        btnConnect.setBounds(12, 223, 205, 38);
        add(btnConnect);

        txtPortNumber = new JTextField();
        txtPortNumber.setText("30000");
        txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
        txtPortNumber.setColumns(10);
        txtPortNumber.setBounds(101, 163, 116, 33);
        add(txtPortNumber);
        
        btnConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connect();
            }
        });
    }

    private void connect() {
        String username = txtUserName.getText().trim();
        String ipAddress = txtIpAddress.getText().trim();
        String port = txtPortNumber.getText().trim();

        // 모든 필드 입력 체크
        if (username.isEmpty() || ipAddress.isEmpty() || port.isEmpty()) {
            return; // 유효성 체크 후 처리
        }
        
        // 기존 창 숨기기
        parentFrame.setVisible(false);

        // 게임 화면과 채팅 UI를 통합
        new GameUI(username, ipAddress, port, parentFrame);
    }
}
