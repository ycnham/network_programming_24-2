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
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import client.ChatClientMain;
import client.ChatClientView;

public class GameUI {

    private JFrame gameFrame;

    public GameUI(String username, String ipAddress, String port, ChatClientMain parentFrame) {
        gameFrame = new JFrame("게임 메인화면 - " + username);
        gameFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // 창 닫을 때 종료되지 않도록 설정
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
        	handleExit(parentFrame, chatView); // exitGame 메서드 호출
        });
        	
        exitPanel.add(exitButton);
        gamePanel.add(exitPanel, BorderLayout.SOUTH);

        gameFrame.add(gamePanel);
        gameFrame.setVisible(true);
        
        // "x" 버튼 클릭 시 동작 설정
        gameFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                handleExit(parentFrame, chatView); // 창 닫을 때 handleExit 호출
            }
        });
    }
    
    // 게임을 종료하고 StartingUI로 돌아가는 메서드
    private void handleExit(ChatClientMain parentFrame, ChatClientView chatView) {
        int confirm = JOptionPane.showConfirmDialog(gameFrame, "게임을 종료하고 시작 화면으로 돌아가시겠습니까?", 
                                                    "게임 종료", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            // "예"를 클릭했을 경우
        	try {
                chatView.sendMessage("Disconnected from server."); // 서버로 메시지 전송
            } catch (Exception e) {
                e.printStackTrace();
            }
            gameFrame.dispose(); // GameUI 창 닫기
            parentFrame.setVisible(false); // ChatClientMain 창 숨기기
            
            // StartingUI 창 표시
            StartingUI startingUI = new StartingUI(); // StartingUI 객체 생성
            startingUI.setVisible(true); // StartingUI 창 표시
        } else if (confirm == JOptionPane.NO_OPTION) {
            // "아니오"를 클릭했을 경우
            return; // 현재 GameUI 창을 유지, 아무 동작도 하지 않음
        }
    }
}
