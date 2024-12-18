package client;

import ui.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * ChatClientView 클래스: 채팅 메시지를 송수신하고 게임 상태를 제어합니다.
 */
public class ChatClientView extends JPanel {
    private final JTextArea chatArea; // 채팅창
    private final JTextField inputField; // 입력 필드
    private final JButton sendButton; // 전송 버튼

    private DataInputStream dis; // 서버 입력 스트림
    private DataOutputStream dos; // 서버 출력 스트림
    private GamePanel gamePanel; // 게임 패널 참조

    public ChatClientView(String username, String ipAddress, String port) {
        setLayout(new BorderLayout());

        // 채팅창 생성
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // 입력 필드 및 버튼
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("Send");
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        try {
            // 서버와 연결
            Socket socket = new Socket(ipAddress, Integer.parseInt(port));
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF(username); // 사용자 이름 서버로 전송

            // 서버 메시지 수신 스레드
            new Thread(() -> {
                try {
                    while (true) {
                        String message = dis.readUTF();
                        chatArea.append(message + "\n");
                        onMessageReceived(message); // 메시지 처리
                    }
                } catch (IOException e) {
                    chatArea.append("Disconnected from server.\n");
                }
            }).start();

            // 메시지 전송 이벤트
            sendButton.addActionListener(e -> sendMessage(inputField.getText().trim()));
            inputField.addActionListener(e -> sendMessage(inputField.getText().trim()));

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Unable to connect to server", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void sendMessage(String message) {
        try {
            if (!message.isEmpty()) {
                dos.writeUTF(message);
                inputField.setText("");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setGamePanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    /**
     * DataOutputStream 반환 메서드 (GameUI에서 사용)
     */
    public DataOutputStream getOutputStream() {
        return dos;
    }

    private void onMessageReceived(String message) {
        System.out.println("[DEBUG] Received: " + message); // 메시지 디버깅 출력
        if (message.equals("GAME_START")) {
            System.out.println("[DEBUG] GAME_START received");
            if (gamePanel != null) {
                gamePanel.handleServerMessage("GAME_START");
            }
        }
    }
}
