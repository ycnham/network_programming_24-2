package network_programming;

import javax.swing.*;
import java.awt.*;

public class ChatClientView extends JPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ChatClientView(String username, String serverIP, String serverPort) {
        setLayout(new BorderLayout());

        // 채팅 제목 표시
        JLabel titleLabel = new JLabel("Chat - " + username);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // 메시지 표시 영역
        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false); // 채팅 영역은 수정 불가
        JScrollPane chatScroll = new JScrollPane(chatArea);
        add(chatScroll, BorderLayout.CENTER);

        // 메시지 입력 및 전송
        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField messageField = new JTextField();
        JButton sendButton = new JButton("Send");

        sendButton.addActionListener(e -> {
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
                chatArea.append(username + ": " + message + "\n");
                messageField.setText("");
            }
        });

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);
    }
}
