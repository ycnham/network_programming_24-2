package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class ChatClientMain extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField txtUserName;
    private JTextField txtIpAddress;
    private JTextField txtPortNumber;

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
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel lblNewLabel = new JLabel("User Name");
        lblNewLabel.setBounds(12, 39, 82, 33);
        contentPane.add(lblNewLabel);

        txtUserName = new JTextField();
        txtUserName.setHorizontalAlignment(SwingConstants.CENTER);
        txtUserName.setBounds(101, 39, 116, 33);
        contentPane.add(txtUserName);
        txtUserName.setColumns(10);

        JLabel lblIpAddress = new JLabel("IP Address");
        lblIpAddress.setBounds(12, 100, 82, 33);
        contentPane.add(lblIpAddress);

        txtIpAddress = new JTextField();
        txtIpAddress.setHorizontalAlignment(SwingConstants.CENTER);
        txtIpAddress.setText("127.0.0.1");
        txtIpAddress.setColumns(10);
        txtIpAddress.setBounds(101, 100, 116, 33);
        contentPane.add(txtIpAddress);

        JLabel lblPortNumber = new JLabel("Port Number");
        lblPortNumber.setBounds(12, 163, 82, 33);
        contentPane.add(lblPortNumber);

        txtPortNumber = new JTextField();
        txtPortNumber.setText("30000");
        txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
        txtPortNumber.setColumns(10);
        txtPortNumber.setBounds(101, 163, 116, 33);
        contentPane.add(txtPortNumber);

        JButton btnConnect = new JButton("Connect");
        btnConnect.setBounds(12, 223, 205, 38);
        contentPane.add(btnConnect);

        MyAction action = new MyAction();
        btnConnect.addActionListener(action);
        txtUserName.addActionListener(action);
        txtIpAddress.addActionListener(action);
        txtPortNumber.addActionListener(action);
    }

    class MyAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String username = txtUserName.getText().trim();
            String ipAddress = txtIpAddress.getText().trim();
            String port = txtPortNumber.getText().trim();

            // 기존 창 숨기기
            setVisible(false);

            // 게임 화면과 채팅 UI를 통합
            JFrame gameFrame = new JFrame("게임 메인화면 - " + username);
            gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            gameFrame.setSize(1280, 720); // 창 크기 설정 (1280x720 해상도)

            // 메인 패널 생성
            JPanel gamePanel = new JPanel(new BorderLayout());

            // 채팅 UI 생성 및 추가
            ChatClientView chatView = new ChatClientView(username, ipAddress, port);
            chatView.setPreferredSize(new Dimension(200, 600));
            gamePanel.add(chatView, BorderLayout.EAST);

            // 카드 게임 패널 생성 및 추가
            JPanel cardGamePanel = new JPanel();
            cardGamePanel.setBackground(Color.GREEN);
            gamePanel.add(cardGamePanel, BorderLayout.CENTER);

            // 나가기 버튼 패널 생성 및 추가
            JPanel exitPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JButton exitButton = new JButton("나가기");
            exitButton.addActionListener(event -> {
                gameFrame.dispose(); // 현재 창 닫기
                ChatClientMain.this.setVisible(true); // 이전 ChatClientMain 창 표시
            });
            exitPanel.add(exitButton);
            gamePanel.add(exitPanel, BorderLayout.SOUTH);

            gameFrame.add(gamePanel);
            gameFrame.setVisible(true);
        }
    }
}
