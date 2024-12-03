// 클라이언트 측 채팅 UI와 서버를 연결
package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClientView extends JPanel {
    private final JTextArea chatArea; // 채팅 내용을 표시하는 텍스트 영역
    private final JTextField inputField; // 메시지를 입력하는 텍스트 필드
    private final JButton sendButton;

    private DataInputStream dis;
    private DataOutputStream dos;
    private Socket socket;

    public ChatClientView(String username, String ipAddress, String port) {
        setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false); // 채팅 내용 수정 불가하도록 처리
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        add(chatScrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField(); // 메시지를 입력받는 텍스트 필드
        sendButton = new JButton("Send");

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        try {
            // 서버와 소켓 연결
            socket = new Socket(ipAddress, Integer.parseInt(port));
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            // 서버로 사용자 이름 전송
            dos.writeUTF(username);

            // 서버로부터 메시지를 수신하는 스레드
            new Thread(() -> {
                try {
                    while (true) {
                        String message = dis.readUTF(); // 서버로부터 메시지 읽기
                        chatArea.append(message + "\n"); // 채팅 영역에 메시지 표시
                        chatArea.setCaretPosition(chatArea.getDocument().getLength()); // 스크롤 위치를 마지막으로 이동
                    }
                } catch (IOException e) {
                    chatArea.append("Disconnected from server.\n"); // 연결 종료 메시지 표시
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // 메시지 전송 이벤트 처리
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
    }

    // 메시지를 서버로 전송
    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            try {
                dos.writeUTF(message); // 서버로 메시지 전송
                inputField.setText(""); // 입력 필드 초기화
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
