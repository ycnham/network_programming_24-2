package client;

import ui.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    private final Queue<String> messageQueue = new ConcurrentLinkedQueue<>(); // 미처리 메시지 큐

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
            dos.flush();

            // 서버 메시지 수신 스레드 시작
            listenToServer();

            // 메시지 전송 이벤트
            sendButton.addActionListener(e -> sendMessage(inputField.getText().trim()));
            inputField.addActionListener(e -> sendMessage(inputField.getText().trim()));

            System.out.println("[DEBUG] 서버에 연결되었습니다: " + ipAddress + ":" + port);
        } catch (IOException e) {
            System.err.println("[ERROR] 서버에 연결할 수 없습니다.");
            JOptionPane.showMessageDialog(this, "Unable to connect to server", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void listenToServer() {
        new Thread(() -> {
            try {
                String message;
                while ((message = dis.readUTF()) != null) {
                    System.out.println("[DEBUG] 서버로부터 수신된 메시지: " + message);

                    if (gamePanel == null) {
                        // gamePanel이 설정되지 않은 경우 큐에 메시지 저장
                        messageQueue.add(message);
                        System.err.println("[INFO] gamePanel이 설정되지 않아 메시지를 큐에 저장했습니다: " + message);
                    } else {
                        // gamePanel이 설정된 경우 메시지 처리
                        processMessage(message);
                    }
                }
            } catch (IOException e) {
                System.out.println("[DEBUG] 서버 연결이 종료되었습니다.");
                chatArea.append("서버 연결이 종료되었습니다.\n");
            }
        }).start();
    }

    public void sendMessage(String message) {
        if (message.trim().isEmpty()) return; // 빈 메시지는 무시

        try {
            dos.writeUTF(message);
            dos.flush();
            inputField.setText(""); // 메시지 전송 후 입력 필드 초기화
        } catch (IOException e) {
            e.printStackTrace();
            chatArea.append("메시지를 전송할 수 없습니다.\n");
        }
    }

    public void setGamePanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        System.out.println("[DEBUG] GamePanel이 설정되었습니다.");

        // 큐에 저장된 메시지 처리
        while (!messageQueue.isEmpty()) {
            String message = messageQueue.poll();
            processMessage(message); // 큐에서 꺼낸 메시지 처리
        }
    }

    public DataInputStream getInputStream() {
        return dis;
    }
    
    public DataOutputStream getOutputStream() {
        return dos;
    }

    public void startListening() {
        // 서버에서 데이터를 수신하기 시작
        listenToServer();
    }

    private void processMessage(String message) {
        if (message.startsWith("TURN:PLAYER")) {
            if (gamePanel != null) {
                int playerIndex = Integer.parseInt(message.split(":")[1].replace("PLAYER", "")) - 1;
                boolean isMyTurn = (playerIndex == (gamePanel.isLeader() ? 0 : 1)); // 선플레이어 여부 판단
                gamePanel.setTurn(isMyTurn); // GamePanel에 턴 정보 전달
            } else {
                messageQueue.add(message); // gamePanel이 null이면 메시지를 큐에 저장
            }
        } else if (message.startsWith("OPPONENT_NAME:")) {
            String opponentName = message.split(":")[1];
            if (gamePanel != null) {
                gamePanel.setOpponentName(opponentName); // 상대방 이름 설정
            } else {
                messageQueue.add(message); // gamePanel이 null이면 메시지를 큐에 저장
            }
        } else if (message.startsWith("PLAYER_NUMBER:") || message.startsWith("PLAYER_COUNT:") ||
                message.startsWith("GAME_START") || message.startsWith("ROUND_RESULT:")) {
            if (gamePanel != null) {
                gamePanel.handleServerMessage(message);
            } else {
                messageQueue.add(message); // gamePanel이 null이면 메시지를 큐에 저장
            }
        } else if (message.startsWith("CARD_SELECTED:")) {
            int cardNumber = Integer.parseInt(message.split(":")[1]);
            if (gamePanel != null) {
                gamePanel.showOpponentCard(cardNumber); // 상대방 중앙에 카드 표시
            } else {
                messageQueue.add(message); // gamePanel이 null이면 메시지를 큐에 저장
            }
        } else if (message.startsWith("PLAYER1_CARD_VIEW:") || message.startsWith("PLAYER2_CARD_VIEW:")) {
            if (gamePanel != null) {
                String cardColor = message.split(":")[1];
                gamePanel.showOpponentCard(cardColor); // 상대방 카드 흑백 이미지 표시
            } else {
                messageQueue.add(message); // gamePanel이 null이면 메시지를 큐에 저장
            }
        } else {
            chatArea.append(message + "\n");
        }
    }
}
