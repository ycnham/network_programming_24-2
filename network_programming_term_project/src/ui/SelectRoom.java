// 제일 나중에 이 페이지 추가할지 상황 보는 거로,,, 

package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SelectRoom extends JFrame {
	
    private static final long serialVersionUID = 1L; // 직렬화 버전 ID 추가

    public SelectRoom(String username) {
    	setTitle("게임룸 입장"); // 창 제목 설정
        setSize(1280, 720); // 창 크기 설정 (1280x720 해상도)        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 창 닫기 시 프로그램 종료
        setLocationRelativeTo(null); // 창을 화면 중앙에 배치
        getContentPane().setBackground(new Color(255, 228, 181)); // 전체 화면 배경 색상 설정
        getContentPane().setLayout(null); // 절대 위치 사용을 위해 Layout을 null로 설정

        // 사용자 이름 표시 (화면 왼쪽 상단)
        JLabel usernameLabel = new JLabel("Username: " + username);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 24)); // 폰트 설정
        usernameLabel.setForeground(Color.BLUE); // 글자색 설정
        usernameLabel.setBounds(100, 50, 300, 30); // 위치와 크기 설정
        getContentPane().add(usernameLabel); // 창에 추가
    
        // 하나의 방 생성
        JPanel roomPanel = new JPanel();
        roomPanel.setBounds(277, 159, 300, 200);
        getContentPane().add(roomPanel);
        roomPanel.setLayout(new BoxLayout(roomPanel, BoxLayout.Y_AXIS)); // BoxLayout으로 세로 정렬
        roomPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1)); // 테두리 설정
        roomPanel.setBackground(Color.WHITE); // 방 패널 배경색을 흰색으로 설정
        roomPanel.setPreferredSize(new Dimension(160, 140));
        
        // 방 번호 표시
        JLabel roomLabel = new JLabel("방 1");
        roomLabel.setFont(new Font("굴림", Font.BOLD, 14)); // 폰트 설정
        roomLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // 가운데 정렬
        roomPanel.add(roomLabel); // 방 번호 추가
        
        // 입장 버튼 생성
        JButton enterButton = new JButton("입장");
        roomPanel.add(enterButton);
        enterButton.setBackground(Color.GREEN); // 버튼 배경색 설정
        enterButton.setForeground(Color.BLACK); // 버튼 글자색 설정
        enterButton.setPreferredSize(new Dimension(80, 30)); // 버튼 크기 설정
        enterButton.setAlignmentX(Component.CENTER_ALIGNMENT); // 가운데 정렬
        
        // 버튼 배경색 완전히 보이도록 설정
        enterButton.setOpaque(true); // 배경색을 채우기 위해 투명도를 설정
        enterButton.setBorderPainted(false); // 버튼의 테두리 없애기
        
        enterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "입장 버튼 클릭됨.");
                // 여기에서 선택한 방에 입장하는 코드를 추가할 수 있음
            }
        });

        // 인원수 표시
        JLabel playerCountLabel = new JLabel("인원 수 0 / 2");
        playerCountLabel.setFont(new Font("굴림", Font.PLAIN, 12)); // 폰트 설정
        // playerCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // 가운데 정렬 삭제

        // roomPanel의 레이아웃을 BoxLayout으로 설정 (수직 정렬)
        roomPanel.setLayout(new BoxLayout(roomPanel, BoxLayout.Y_AXIS));

        // 새로운 JPanel을 생성하여 인원수 부분을 왼쪽 정렬로 설정
        JPanel playerCountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        playerCountPanel.setPreferredSize(new Dimension(50, 100));
        playerCountPanel.setBackground(new Color(255, 255, 255));
        
        // 인원수 패널의 가로 크기 조정
        playerCountLabel.setPreferredSize(new Dimension(100, 30)); // 가로 크기를 100으로 설정
        playerCountPanel.add(playerCountLabel);
        roomPanel.add(playerCountPanel);
        
        setVisible(true); // 창을 표시
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SelectRoom("player1")); // 프로그램 실행
    }
}
