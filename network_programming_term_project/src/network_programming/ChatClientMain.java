package network_programming;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class ChatClientMain extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtUserName;
	private JTextField txtIpAddress;
	private JTextField txtPortNumber;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ChatClientMain frame = new ChatClientMain();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public ChatClientMain() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 254, 321);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("User Name");
		lblNewLabel.setBounds(12, 39, 82, 33);
		contentPane.add(lblNewLabel);
		
		txtUserName = new JTextField();
		txtUserName.setHorizontalAlignment(SwingConstants.CENTER);
		txtUserName.setBounds(101, 39, 116, 33);
		contentPane.add(txtUserName);
		txtUserName.setColumns(10);
		
		JLabel lblIpAddress = new JLabel("IP Address");
		lblIpAddress.setBounds(12, 100, 82, 33);
		contentPane.add(lblIpAddress);
		
		txtIpAddress = new JTextField();
		txtIpAddress.setHorizontalAlignment(SwingConstants.CENTER);
		txtIpAddress.setText("127.0.0.1");
		txtIpAddress.setColumns(10);
		txtIpAddress.setBounds(101, 100, 116, 33);
		contentPane.add(txtIpAddress);
		
		JLabel lblPortNumber = new JLabel("Port Number");
		lblPortNumber.setBounds(12, 163, 82, 33);
		contentPane.add(lblPortNumber);
		
		txtPortNumber = new JTextField();
		txtPortNumber.setText("30000");
		txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
		txtPortNumber.setColumns(10);
		txtPortNumber.setBounds(101, 163, 116, 33);
		contentPane.add(txtPortNumber);
		
		JButton btnConnect = new JButton("Connect");
		btnConnect.setBounds(12, 223, 205, 38);
		contentPane.add(btnConnect);
		Myaction action = new Myaction();
		btnConnect.addActionListener(action);
		txtUserName.addActionListener(action);
		txtIpAddress.addActionListener(action);
		txtPortNumber.addActionListener(action);
	}
	class Myaction implements ActionListener // 내부클래스로 액션 이벤트 처리 클래스
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			String username = txtUserName.getText().trim();
			String ip_addr = txtIpAddress.getText().trim();
			String port_no = txtPortNumber.getText().trim();
			@SuppressWarnings("unused")
			ChatClientView view = new ChatClientView(username, ip_addr, port_no);
			
			// 기존 창 숨기기
			setVisible(false);
			
			// 게임화면 내에 채팅 UI 구현하는 코드 추가함
			// 
			// 게임 화면과 채팅 UI를 통합
	        JPanel gamePanel = new JPanel();
	        gamePanel.setLayout(new BorderLayout());

	        // 채팅 UI 생성
	        ChatClientView chatView = new ChatClientView(username, ip_addr, port_no);

	        // 게임 화면 추가 (샘플로 채팅 UI 오른쪽에 배치)
	        JPanel cardGamePanel = new JPanel(); // 실제 카드 게임 구현은 별도로 작성 필요
	        cardGamePanel.setBackground(java.awt.Color.GREEN); // 카드 게임 패널 배경색 설정
	        gamePanel.add(cardGamePanel, BorderLayout.CENTER);
	        // gamePanel.add(chatView.getContentPane(), BorderLayout.EAST); // ChatClientView의 컨텐츠 패널 추가
	        gamePanel.add(chatView, BorderLayout.EAST); // 바로 추가
	        
	        // 메인 프레임 생성
	        JFrame mainFrame = new JFrame("Card Game with Chat");
	        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        mainFrame.setSize(800, 600);
	        mainFrame.add(gamePanel);
	        mainFrame.setVisible(true);
	    }
	}
}
