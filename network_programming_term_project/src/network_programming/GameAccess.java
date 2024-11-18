package network_programming;

import javax.swing.*;
import java.awt.*;

public class GameAccess extends JFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtUserName;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                GameAccess frame = new GameAccess();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public GameAccess() {
        setTitle("Game Access");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);

        JPanel contentPane = new JPanel(new BorderLayout());
        setContentPane(contentPane);

        JLabel lblUserName = new JLabel("Enter Your Username:");
        txtUserName = new JTextField();
        JButton btnJoin = new JButton("Join Game");

        contentPane.add(lblUserName, BorderLayout.NORTH);
        contentPane.add(txtUserName, BorderLayout.CENTER);
        contentPane.add(btnJoin, BorderLayout.SOUTH);

        btnJoin.addActionListener(e -> connectToGame());
    }

    private void connectToGame() {
        String username = txtUserName.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a username.");
            return;
        }

        JFrame gameFrame = new JFrame("Card Game with Chat");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setSize(800, 600);

        JPanel gamePanel = new JPanel(new BorderLayout());

        ChatClientView chatView = new ChatClientView(username, "127.0.0.1", "30000");
        chatView.setPreferredSize(new Dimension(200, 600));
        gamePanel.add(chatView, BorderLayout.EAST);

        JPanel cardGamePanel = new JPanel();
        cardGamePanel.setBackground(Color.GREEN);
        gamePanel.add(cardGamePanel, BorderLayout.CENTER);

        gameFrame.add(gamePanel);
        gameFrame.setVisible(true);

        this.setVisible(false);
    }
}
