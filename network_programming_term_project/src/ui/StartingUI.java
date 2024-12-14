// 게임 초기 접속 화면
package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import client.ChatClientMain;

public class StartingUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField usernameField;
    private JTextField ipAddressField;
    private JTextField portField;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
            	StartingUI frame = new StartingUI();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public StartingUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1280, 720);
        
        contentPane = new JPanel();
        contentPane.setBackground(new Color(255, 204, 0)); // 배경색 설정
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(null);
        setContentPane(contentPane);

        // 제목 레이블
        JLabel titleLabel = new JLabel("흑과 백");
        titleLabel.setFont(new Font("굴림", Font.BOLD, 32));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBounds(440, 50, 400, 60); // 화면 중앙 상단에 위치
        contentPane.add(titleLabel);

        // username 입력 필드
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("굴림", Font.PLAIN, 18));
        usernameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        usernameLabel.setBounds(430, 200, 120, 30); // 중앙 정렬된 레이블 위치
        contentPane.add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setFont(new Font("굴림", Font.PLAIN, 16));
        usernameField.setBounds(560, 200, 300, 30); // 필드 위치
        contentPane.add(usernameField);

        // IP Address 입력 필드
        JLabel ipAddressLabel = new JLabel("IP Address:");
        ipAddressLabel.setFont(new Font("굴림", Font.PLAIN, 18));
        ipAddressLabel.setHorizontalAlignment(SwingConstants.CENTER);
        ipAddressLabel.setBounds(430, 260, 120, 30); // 중앙 정렬된 레이블 위치
        contentPane.add(ipAddressLabel);

        ipAddressField = new JTextField("127.0.0.1");
        ipAddressField.setFont(new Font("굴림", Font.PLAIN, 16));
        ipAddressField.setBounds(560, 260, 300, 30); // 필드 위치
        contentPane.add(ipAddressField);

        // Port Number 입력 필드
        JLabel portLabel = new JLabel("Port Number:");
        portLabel.setHorizontalAlignment(SwingConstants.CENTER);
        portLabel.setFont(new Font("굴림", Font.PLAIN, 18));
        portLabel.setBounds(430, 320, 120, 30); // 중앙 정렬된 레이블 위치
        contentPane.add(portLabel);

        portField = new JTextField("30000");
        portField.setFont(new Font("굴림", Font.PLAIN, 16));
        portField.setBounds(560, 320, 300, 30); // 필드 위치
        contentPane.add(portField);

        // 입장하기 버튼
        JButton enterButton = new JButton("입장하기");
        enterButton.setFont(new Font("굴림", Font.BOLD, 18));
        enterButton.setBackground(new Color(102, 255, 204));
        enterButton.setBounds(590, 400, 150, 40); // 버튼을 중앙에 배치
        enterButton.addActionListener(e -> enterGame());
        contentPane.add(enterButton);
    }

    // username, ipAddress, port를 받는 생성자 추가
    public StartingUI(String username, String ipAddress, String port) {
        this(); // 기본 생성자 호출
        // 전달받은 값들을 필드에 세팅
        usernameField.setText(username);
//        ipAddressField.setText(ipAddress);
//        portField.setText(port);
    }
    
    // 입장하기 버튼 클릭 시 동작할 메서드
    private void enterGame() {
        String username = usernameField.getText();
        String ipAddress = ipAddressField.getText();
        String port = portField.getText();

        if (username.isEmpty() || ipAddress.isEmpty() || port.isEmpty()) {
            JOptionPane.showMessageDialog(this, "모든 필드를 입력해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            int portNumber = Integer.parseInt(port); // 포트 번호가 유효한지 확인
            if (portNumber < 1 || portNumber > 65535) {
                throw new NumberFormatException();
            }

            // StartingUI 화면을 종료하고 GameUI 화면을 시작
            this.dispose(); // 현재 StartingUI 창을 닫음
            ChatClientMain parentFrame = new ChatClientMain(); // ChatClientMain 객체 생성
            new GameUI(username, ipAddress, port, parentFrame); // GameUI 객체 생성 (변수는 필요하지 않음)
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "유효한 포트 번호를 입력해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}
