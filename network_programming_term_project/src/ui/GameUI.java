package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import client.ChatClientMain;
import client.ChatClientView;
import models.Player;

/**
 * GameUI 클래스: 메인 게임 창을 구성하고 사용자 인터페이스를 관리.
 */
public class GameUI {

    private JFrame gameFrame; // 메인 게임 프레임

    /**
     * GameUI 생성자: 메인 게임 화면을 초기화.
     *
     * @param username    사용자 이름
     * @param ipAddress   서버 IP 주소
     * @param port        서버 포트 번호
     * @param parentFrame 부모 프레임 (ChatClientMain)
     */
    public GameUI(String username, String ipAddress, String port, ChatClientMain parentFrame) {
        gameFrame = new JFrame("게임 메인화면 - " + username);
        gameFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        gameFrame.setSize(1280, 720);

        JPanel gamePanel = new JPanel(new BorderLayout());

        ChatClientView chatView = new ChatClientView(username, ipAddress, port);
        chatView.setPreferredSize(new Dimension(200, 600));

        GamePanel cardGamePanel = new GamePanel(new Player("Player1"), new Player(username), chatView.getOutputStream());
        cardGamePanel.setPreferredSize(new Dimension(1080, 600));

        // GamePanel을 먼저 설정한 후 메시지 수신 시작
        chatView.setGamePanel(cardGamePanel);
        chatView.startListening();

        gamePanel.add(cardGamePanel, BorderLayout.CENTER);
        gamePanel.add(chatView, BorderLayout.EAST);

        JPanel exitPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton exitButton = new JButton("나가기");
        exitButton.addActionListener(event -> handleExit(parentFrame, chatView));
        exitPanel.add(exitButton);
        gamePanel.add(exitPanel, BorderLayout.SOUTH);

        gameFrame.add(gamePanel);
        gameFrame.setVisible(true);

        gameFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                handleExit(parentFrame, chatView);
            }
        });
    }

    /**
     * 게임을 종료하고 StartingUI로 돌아가는 메서드.
     *
     * @param parentFrame 부모 프레임 (ChatClientMain)
     * @param chatView    ChatClientView 인스턴스 (서버와 통신)
     */
    private void handleExit(ChatClientMain parentFrame, ChatClientView chatView) {
        int confirm = JOptionPane.showConfirmDialog(
                gameFrame,
                "게임을 종료하고 시작 화면으로 돌아가시겠습니까?",
                "게임 종료",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            // "예"를 클릭했을 경우
            try {
                chatView.sendMessage("Disconnected from server."); // 서버에 연결 종료 메시지 전송
            } catch (Exception e) {
                e.printStackTrace();
            }
            gameFrame.dispose(); // GameUI 창 닫기
            parentFrame.setVisible(false); // ChatClientMain 창 숨기기

            // StartingUI 창 표시
            StartingUI startingUI = new StartingUI(); // StartingUI 객체 생성
            startingUI.setVisible(true); // StartingUI 창 표시
        }
    }
}
