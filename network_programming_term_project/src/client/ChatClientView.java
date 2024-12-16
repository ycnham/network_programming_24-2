package client;

import ui.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClientView extends JPanel {
    // 채팅 UI 구성 요소
    private final JTextArea chatArea;      // 채팅 내용 표시
    private final JTextField inputField;   // 입력 필드
    private final JButton sendButton;      // 전송 버튼

    // 서버 통신 스트림
    private DataInputStream dis;           // 서버로부터 데이터 수신
    private DataOutputStream dos;          // 서버로 데이터 전송
    private GamePanel gamePanel;           // GamePanel 참조 (게임 상태 제어)

    /**
     * ChatClientView 생성자: 서버 연결 및 채팅 UI 초기화
     *
     * @param username  사용자 이름
     * @param ipAddress 서버 IP 주소
     * @param port      서버 포트
     */
    public ChatClientView(String username, String ipAddress, String port) {
        setLayout(new BorderLayout());

        // 채팅창 영역 생성
        chatArea = new JTextArea();
        chatArea.setEditable(false); // 사용자 입력 방지 (읽기 전용)
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        // 입력창 영역 생성
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("Send");
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        // 서버 연결 설정
        try {
            Socket socket = new Socket(ipAddress, Integer.parseInt(port)); // 서버에 소켓 연결
            dis = new DataInputStream(socket.getInputStream());           // 서버 입력 스트림
            dos = new DataOutputStream(socket.getOutputStream());         // 서버 출력 스트림

            dos.writeUTF(username); // 서버에 사용자 이름 전송

            // 서버 메시지 수신 스레드 시작
            new Thread(() -> {
                try {
                    while (true) {
                        String message = dis.readUTF();  // 서버로부터 메시지 수신
                        chatArea.append(message + "\n"); // 채팅창에 표시
                        chatArea.setCaretPosition(chatArea.getDocument().getLength()); // 스크롤을 가장 아래로 이동
                        onMessageReceived(message); // 게임 상태 메시지 처리
                    }
                } catch (IOException e) {
                    chatArea.append("Disconnected from server.\n");
                }
            }).start();

            // 메시지 전송 이벤트 처리
            sendButton.addActionListener(e -> sendMessage(inputField.getText().trim()));
            inputField.addActionListener(e -> sendMessage(inputField.getText().trim()));

        } catch (IOException e) {
            // 서버 연결 실패 시 에러 표시
            JOptionPane.showMessageDialog(this, "Error: Unable to connect to server", "Connection Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * 사용자 입력 메시지를 서버로 전송
     */
    public void sendMessage(String message) {
        try {
            if (!message.isEmpty()) {
                dos.writeUTF(message); // 서버로 메시지 전송
                inputField.setText("");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 파라미터 없는 sendMessage 메서드 (오버로딩)
     */
    public void sendMessage() {
        sendMessage(""); // 기본 메시지 처리 (빈 문자열 전송)
    }

    /**
     * GamePanel 참조를 설정 (게임 상태 제어를 위해)
     *
     * @param gamePanel 연결된 GamePanel 객체
     */
    public void setGamePanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    /**
     * 서버로부터 수신된 메시지를 처리
     *
     * @param message 서버로부터 수신된 메시지
     */
    private void onMessageReceived(String message) {
        if (message.startsWith("PLAYER_COUNT:")) {
            // 플레이어 수 정보를 GamePanel에 전달
            if (gamePanel != null) {
                gamePanel.handleServerMessage(message);
            }
        }
    }
}
