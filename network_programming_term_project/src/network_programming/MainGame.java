package network_programming;

import javax.swing.*;
import java.awt.*;

public class MainGame extends JFrame {
    private static final long serialVersionUID = 1L;
    private String username;
    private String ipAddress;
    private String port;
    private AccessGame accessGame; // 이전 창 참조

    public MainGame(String username, String ipAddress, String port, AccessGame accessGame) {
        this.username = username;
        this.ipAddress = ipAddress;
        this.port = port;
        this.accessGame = accessGame;

        setTitle("게임 메인화면 - " + username);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720); // 창 크기 설정 (1280x720 해상도)        
        
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
        exitButton.addActionListener(e -> {
            this.dispose(); // 현재 창 닫기
            accessGame.setVisible(true); // 이전 AccessGame 창 표시
        });
        exitPanel.add(exitButton);
        gamePanel.add(exitPanel, BorderLayout.SOUTH);

        add(gamePanel);
    }
}
