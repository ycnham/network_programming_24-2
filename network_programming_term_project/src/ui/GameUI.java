/* 
게임 화면 구성
ConnectionPanel에서 GameUI을 호출해 게임 화면을 띄움
*/
package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import client.ChatClientMain;
import client.ChatClientView;

public class GameUI {

    private JFrame gameFrame;

    public GameUI(String username, String ipAddress, String port, ChatClientMain parentFrame) {
        gameFrame = new JFrame("게임 메인화면 - " + username);
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
            parentFrame.setVisible(true); // 이전 ChatClientMain 창 표시
        });
        exitPanel.add(exitButton);
        gamePanel.add(exitPanel, BorderLayout.SOUTH);

        gameFrame.add(gamePanel);
        gameFrame.setVisible(true);
    }
}
