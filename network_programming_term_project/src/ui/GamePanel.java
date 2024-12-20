package ui;

import models.BlackAndWhiteGame;
import models.Card;
import models.Player;

import javax.swing.*;
import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * GamePanel 클래스: 카드 게임 UI를 관리하고 게임 상태를 제어합니다.
 * 리더는 시작 버튼을 눌러 게임을 시작하고, 비리더는 대기 메시지를 확인합니다.
 */
public class GamePanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final BlackAndWhiteGame game; // 게임 로직 관리 객체
    private JLabel player1ScoreLabel;     // Player1 점수 라벨
	private JLabel player2ScoreLabel;     // Player2 점수 라벨
	private JLabel roundLabel;            // 라운드 번호 표시 라벨
	private JLabel timerLabel;            // 타이머 표시 라벨
	private JLabel warningLabel;          // 경고 메시지 라벨
	private JButton submitButton;         // 카드 제출 버튼
	private JPanel player1CardsPanel;     // Player1 카드 영역
	private JPanel player2CardsPanel;     // Player2 카드 영역
	
	// 중앙 카드 영역 추가
	private JLabel player1CenterCard;     // 상대방 중앙 카드
	private JLabel player2CenterCard;     // 나의 중앙 카드
	
	private Timer timer;          // 타이머 객체
	private int timeLeft;         // 라운드 남은 시간
	private Card selectedCard;    // 선택된 카드
	private int roundNumber = 1;  // 현재 라운드 번호
	
	private JButton startButton;    // 게임 시작 버튼
	private JLabel waitingLabel;    // 대기 메시지 라벨
	private final JPanel startScreen;     // 시작 화면 패널
	private final JPanel gameScreen;      // 게임 화면 패널
	private final CardLayout cardLayout;  // 화면 전환을 위한 CardLayout
	
	private boolean isLeader = false;     // 리더 여부
//	private int playerCount = 0;          // 현재 접속한 플레이어 수
	
	private final DataOutputStream dos;   // 서버와의 통신을 위한 출력 스트림
	
	/**
	 * GamePanel 생성자: UI 초기화 및 게임 상태 관리 객체 생성
	 * @param player1 첫 번째 플레이어 객체
	 * @param player2 두 번째 플레이어 객체
	 * @param dos 서버와 통신하기 위한 DataOutputStream
	 */
	public GamePanel(Player player1, Player player2, DataOutputStream dos) {
		this.game = new BlackAndWhiteGame(player1, player2); // 게임 로직 객체 초기화
	    this.dos = dos;
	    cardLayout = new CardLayout();
	    setLayout(cardLayout);
	
	    // 시작 화면 설정
	    startScreen = new JPanel(new BorderLayout());
	    waitingLabel = new JLabel("서버 메시지 대기중...", SwingConstants.CENTER);
	    waitingLabel.setFont(new Font("굴림", Font.BOLD, 20));
	    startScreen.add(waitingLabel, BorderLayout.CENTER); // 기본 메시지 추가
	    add(startScreen, "START_SCREEN");  // 초기에는 이 화면이 보임
	
	    // 게임 화면 설정
	    gameScreen = new JPanel(new BorderLayout());
	    setupGameScreen();
	    add(gameScreen, "GAME_SCREEN");
	
	    setupReadyButton();
    }
	
	/**
	 * 서버 메시지 처리: 리더 여부 및 게임 시작 메시지 처리
	 */
	public void handleServerMessage(String message) {
		SwingUtilities.invokeLater(() -> {
			System.out.println("[DEBUG] 서버로부터 메시지 수신: " + message);
			try {
				if (message.startsWith("PLAYER_NUMBER:")) {
				isLeader = Integer.parseInt(message.split(":")[1]) == 1;
				setLeader(isLeader);
				} else if (message.startsWith("PLAYER_COUNT:")) {
//					playerCount = Integer.parseInt(message.split(":")[1]);
//					// 플레이어 수를 waitingLabel에 표시
//	                waitingLabel.setText("현재 접속한 플레이어 수: " + playerCount);
					waitingLabel.setText("게임 시작 대기중...");
				} else if (message.equals("GAME_START")) {
					startGame();
				} else if (message.startsWith("PLAYER_READY:")) {
					String playerName = message.split(":")[1];
					waitingLabel.setText(playerName + " 준비 완료");
				} else if (message.startsWith("ROUND_RESULT")) {
					String[] parts = message.split(":");
					int opponentCardNumber = Integer.parseInt(parts[1]);
					String winner = parts[2];
					int player1Score = Integer.parseInt(parts[3]);
					int player2Score = Integer.parseInt(parts[4]);
	
					// 상대방 카드 표시
					Card opponentCard = new Card(opponentCardNumber); // 카드 객체 생성
					player1CenterCard.setIcon(opponentCard.getCardImage(true));
	
					// 점수 업데이트
					game.getPlayer1().setPoints(player1Score);
					game.getPlayer2().setPoints(player2Score);
					updateScores();
	
					// 승리 메시지
					if ("Player1".equals(winner)) {
						JOptionPane.showMessageDialog(this, "Player 1 승리!", "결과", JOptionPane.INFORMATION_MESSAGE);
					} else if ("Player2".equals(winner)) {
						JOptionPane.showMessageDialog(this, "Player 2 승리!", "결과", JOptionPane.INFORMATION_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(this, "무승부!", "결과", JOptionPane.INFORMATION_MESSAGE);
					}
					startRound(); // 다음 라운드 준비
				}
			} catch (Exception e) {
				e.printStackTrace();
	        }
	    });
	}

	/**
	 * 리더 여부에 따라 시작 버튼과 대기 메시지 표시 설정
   	 */
	public void setLeader(boolean leader) {
		this.isLeader = leader;

	    SwingUtilities.invokeLater(() -> {
	    	startScreen.removeAll();
	    	startScreen.setLayout(new BorderLayout());
	
	    	if (isLeader) {
	    		startButton = new JButton("게임 시작");
	    		startButton.setFont(new Font("굴림", Font.BOLD, 20));
	    		startButton.addActionListener(e -> startGameRequest());
	    		startScreen.add(startButton, BorderLayout.CENTER);
	    	} else {
	    		waitingLabel.setText("게임 시작 대기중...");
	    		startScreen.add(waitingLabel, BorderLayout.CENTER);
	    		
	    		// 	"준비" 버튼 추가
	    		readyButton = new JButton("준비");
	    		readyButton.setFont(new Font("굴림", Font.BOLD, 20));
	    		readyButton.addActionListener(e -> {
	    			System.out.println("[DEBUG] 준비 버튼 클릭됨");
	    			sendReadySignal();
				});
	    		startScreen.add(readyButton, BorderLayout.SOUTH); // 버튼 추가
	    	}    
	
	    	startScreen.revalidate();
	    	startScreen.repaint();
		});
	}
	/**
	 * 리더가 게임 시작 버튼을 누르면 서버로 게임 시작 요청 전송
	 */
	private void startGameRequest() {
		try {
	      dos.writeUTF("GAME_START");
	      dos.flush();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	}

	private void sendReadySignal() {
		try {
			dos.writeUTF("READY");
			dos.flush();
			System.out.println("[DEBUG] READY 신호 전송 완료");
	    } catch (IOException e) {
	    	System.err.println("[ERROR] READY 신호 전송 실패: " + e.getMessage());
	    	e.printStackTrace();
	    }
	}

	private JButton readyButton;

	private void setupReadyButton() {
		readyButton = new JButton("준비");
		readyButton.setFont(new Font("굴림", Font.BOLD, 20));
		readyButton.addActionListener(e -> {
			System.out.println("[DEBUG] 준비 버튼 클릭됨");
			sendReadySignal();
		});
		startScreen.add(readyButton, BorderLayout.SOUTH);
	}

	/**
	 * 게임 시작: 게임 화면으로 전환
	 */
	private void startGame() {
		SwingUtilities.invokeLater(() -> {
			try {
				System.out.println("[DEBUG] 게임 시작 화면으로 전환");
				cardLayout.show(this, "GAME_SCREEN");
			  
				// 카드 렌더링 및 라운드 시작
				renderCards();
				startRound();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * 게임 화면 UI 초기화
	 */
	private void setupGameScreen() {
	    // 화면을 세로로 나눔
	    JPanel mainPanel = new JPanel(new GridLayout(2, 1));

	    // 상단 영역 (Player1 영역)
	    JPanel topPanel = new JPanel(new BorderLayout());
	    JLabel player1Label = new JLabel("Player 1", SwingConstants.LEFT); // Player1 구분
	    player1Label.setFont(new Font("굴림", Font.BOLD, 16));
	    player1Label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//	    JLabel player1SelectedCardLabel = new JLabel("선택된 카드: 없음", SwingConstants.CENTER);
//	    player1SelectedCardLabel.setFont(new Font("굴림", Font.BOLD, 16));

	    // Player1 선택된 카드 표시
	    JLabel player1SelectedCard = new JLabel("", SwingConstants.CENTER);
	    player1SelectedCard.setOpaque(true);
	    player1SelectedCard.setPreferredSize(new Dimension(95, 133)); // 63:88 비율
	    player1SelectedCard.setHorizontalAlignment(SwingConstants.CENTER);
	    player1SelectedCard.setVerticalAlignment(SwingConstants.CENTER);
	    
	    // Player1 카드 리스트 영역
	    JPanel player1CardRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
	    for (int i = 0; i < game.getPlayer1().getCards().size(); i++) {
	        JLabel cardBack = new JLabel(game.getPlayer1().getCards().get(i).getCardImage(false)); // 뒷면
	        cardBack.setPreferredSize(new Dimension(95, 133)); // 63:88 비율
	        player1CardRow.add(cardBack);
	    }
	    topPanel.add(player1Label, BorderLayout.WEST); // Player1 레이블 추가
	    topPanel.add(player1CardRow, BorderLayout.NORTH);
	    topPanel.add(player1SelectedCard, BorderLayout.CENTER); // 선택한 카드 표시

	    // 하단 영역 (Player2 영역)
	    JPanel bottomPanel = new JPanel(new BorderLayout());
	    JLabel player2Label = new JLabel("Player 2", SwingConstants.LEFT);
	    player2Label.setFont(new Font("굴림", Font.BOLD, 16));
	    player2Label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//	    JLabel player2SelectedCardLabel = new JLabel("선택된 카드: 없음", SwingConstants.CENTER);
//	    player2SelectedCardLabel.setFont(new Font("굴림", Font.BOLD, 16));

	    // Player2 카드 전체 패널
	    JPanel player2CardPanel = new JPanel();
	    player2CardPanel.setLayout(new BoxLayout(player2CardPanel, BoxLayout.Y_AXIS)); // "선택된 카드" 섹션과 카드 리스트를 수직으로 배치
	    
	    // Player2 선택된 카드 표시
	    JPanel player2SelectedCardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
	    JLabel player2SelectedCard = new JLabel("선택된 카드 없음", SwingConstants.CENTER);
	    player2SelectedCard.setOpaque(true);
	    player2SelectedCard.setPreferredSize(new Dimension(95, 133)); // 63:88 비율
	    player2SelectedCard.setHorizontalAlignment(SwingConstants.CENTER);
	    player2SelectedCard.setVerticalAlignment(SwingConstants.CENTER);
	    player2SelectedCardPanel.add(player2SelectedCard); // 패널에 추가
	    
	    // Player2 카드 리스트 영역
	    JPanel player2CardRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
	    for (Card card : game.getPlayer2().getCards()) {
	        JButton cardButton = new JButton(card.getCardImage(true)); // 앞면
	        cardButton.setPreferredSize(new Dimension(95, 133)); // 63:88 비율
	        cardButton.addActionListener(e -> {
	            selectedCard = card;
	            
	            player2SelectedCard.setIcon(card.getCardImage(true)); // 본인 선택된 카드 앞면 표시
	            player2SelectedCardPanel.revalidate(); // UI 갱신
	            player2SelectedCardPanel.repaint();
	            
	            // 지금은 채팅창에 표시되긴 하는데, 채팅창에 나오면 안되므로 나중에 지워야 함 (try catch 문)
	            try {
	                // 상대방에게는 뒷면으로 보이도록 전송
	                dos.writeUTF("SELECTED_CARD:" + card.getNumber());
	                dos.flush();
	            } catch (IOException ex) {
	                ex.printStackTrace();
	            }
	        });
	        player2CardRow.add(cardButton);
	    }
	    
	    // Player2 전체 패널에 선택된 카드 섹션과 카드 리스트 섹션 추가
	    player2CardPanel.add(player2SelectedCardPanel); // 선택된 카드 섹션 추가
	    player2CardPanel.add(player2CardRow);          // 카드 리스트 섹션 추가
	    
	    bottomPanel.add(player2Label, BorderLayout.WEST); // Player2 레이블 추가
	    bottomPanel.add(player2CardPanel, BorderLayout.CENTER); // Player2 전체 패널 추가
	    bottomPanel.add(player2SelectedCard, BorderLayout.CENTER); // 선택한 카드 표시
	    bottomPanel.add(player2CardRow, BorderLayout.SOUTH); // 카드 버튼 추가

//	    // 중앙에 타이머 추가
//	    timerLabel = new JLabel("남은 시간: 30초", SwingConstants.CENTER);
//	    timerLabel.setFont(new Font("굴림", Font.BOLD, 20));
//	    timerLabel.setForeground(Color.RED);
//	    topPanel.add(timerLabel, BorderLayout.SOUTH);

	    // 메인 패널을 게임 화면에 추가
	    gameScreen.add(mainPanel, BorderLayout.CENTER);

	    // 카드 제출 버튼 (화면 오른쪽 세로 정중앙)
	    JPanel rightPanel = new JPanel(new BorderLayout());
	    submitButton = new JButton("카드 제출");
	    submitButton.setFont(new Font("굴림", Font.BOLD, 16));
	    submitButton.addActionListener(e -> submitCard()); // 카드 제출 동작
//	    rightPanel.add(submitButton, BorderLayout.CENTER);
//	    gameScreen.add(rightPanel, BorderLayout.EAST);

	    // 타이머 라벨
	    timerLabel = new JLabel("남은 시간: 30초", SwingConstants.CENTER);
	    timerLabel.setFont(new Font("굴림", Font.BOLD, 20));
	    timerLabel.setForeground(Color.RED);

	    // 카드 제출 버튼의 왼쪽에 타이머 배치
	    rightPanel.add(timerLabel, BorderLayout.WEST);
	    rightPanel.add(submitButton, BorderLayout.CENTER);
	    
	    // 상단과 하단 패널을 메인 패널에 추가
	    mainPanel.add(topPanel);
	    mainPanel.add(bottomPanel);
	    
	    // 게임 화면에 추가
	    gameScreen.add(mainPanel, BorderLayout.CENTER);
	    gameScreen.add(rightPanel, BorderLayout.EAST); // 오른쪽 영역 추가
	    
	    // UI 갱신
	    gameScreen.revalidate();
	    gameScreen.repaint();
	}

	
	/**
	 * 카드 렌더링
	 */
	private void renderCards() {
		player1CardsPanel.removeAll();
		player2CardsPanel.removeAll();

		// 상대방 카드: 뒷면
		game.getPlayer1().getCards().forEach(card ->
            player1CardsPanel.add(new JLabel(card.getCardImage(false)))
        );

		// 내 카드: 선택 가능
		game.getPlayer2().getCards().forEach(card -> {
			JButton cardButton = new JButton(card.getCardImage(true));
			cardButton.addActionListener(e -> {
				selectedCard = card;
				player2CenterCard.setIcon(card.getCardImage(true));
			});
			player2CardsPanel.add(cardButton);
		});
	
		revalidate();
		repaint();
	}

	/**
	 * 라운드 시작
	 */
	private void startRound() {
		if (timer != null) {
	        timer.stop(); // 기존 타이머 정지
	    }

	    timeLeft = 30; // 30초로 초기화
	    updateTimerLabel(); // 초기 타이머 UI 표시

	    // 새로운 타이머 생성
	    timer = new Timer(1000, e -> {
	        if (timeLeft > 0) {
	            timeLeft--; // 남은 시간을 줄임
	            updateTimerLabel(); // UI 업데이트
	        } else {
	            timer.stop(); // 시간이 끝나면 타이머 정지
	            autoSubmitCard(); // 시간 초과 시 자동 카드 제출
	        }
	    });

	    timer.start(); // 타이머 시작
	}

	/**
	 * 타이머 라벨 업데이트
	 */
	private void updateTimerLabel() {
	    SwingUtilities.invokeLater(() -> {
	        if (timerLabel != null) {
	            timerLabel.setText("남은 시간: " + timeLeft + "초");
	        }
	    });
	}

	/**
	 * 자동 제출: 타이머 종료 시 무작위 카드 제출
	 */
	private void autoSubmitCard() {
	    if (selectedCard == null) {
	        Random random = new Random();
	        selectedCard = game.getPlayer2().getCards().get(random.nextInt(game.getPlayer2().getCards().size()));
	    }

	    // 선택된 카드 UI에 표시
	    SwingUtilities.invokeLater(() -> player2CenterCard.setIcon(selectedCard.getCardImage(true)));

	    // 서버에 카드 제출 신호 전송
	    try {
	        dos.writeUTF("CARD_SELECTED:" + selectedCard.getNumber());
	        dos.flush();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    // 다음 라운드 시작
	    JOptionPane.showMessageDialog(this, "시간 초과! 랜덤 카드가 제출됩니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
	    processRound(null);
	}
	
	/**
	 * 카드 제출 시 동작
	 */
	private void submitCard() {
		if (selectedCard == null) {
			warningLabel.setText("카드를 선택해주세요!");
			return;
		}
		warningLabel.setText("");

		try {
			// 선택된 카드 정보를 서버로 전송
			dos.writeUTF("CARD_SELECTED:" + selectedCard.getNumber());
			dos.flush();
	
			// 선택된 카드 UI에 표시
			player2CenterCard.setIcon(selectedCard.getCardImage(true));
		} catch (IOException e) {
			e.printStackTrace();
	    }
	}

	/**
	 * 점수 및 라운드 업데이트
	 */
	private void updateScores() {
		player1ScoreLabel.setText("Player1: " + game.getPlayer1().getPoints());
		player2ScoreLabel.setText("Player2: " + game.getPlayer2().getPoints());
		roundLabel.setText("Round: " + roundNumber++);
	}

	/**
	 * 라운드 처리: 점수 갱신, 라운드 번호 업데이트, 다음 라운드 준비
	 */
	private void processRound(Card opponentCard) {
		// 상대방 카드와 선택된 카드가 없으면 실행하지 않음
		if (opponentCard == null || selectedCard == null) {
			return;
		}

		// 타이머 중지
		timer.stop();

		// 상대방 카드 (뒷면) 표시
	    player1CenterCard.setIcon(opponentCard.getCardImage(false)); // 상대방 카드는 뒷면 이미지

	    // 내 카드 (앞면) 표시
	    player2CenterCard.setIcon(selectedCard.getCardImage(true));

	    // 게임 로직에 따라 점수 계산 및 승리 판정
	    game.playRound(opponentCard, selectedCard);

	    // 점수 업데이트
	    updateScores();

	    // 라운드 번호 갱신
	    roundLabel.setText("Round: " + roundNumber++);

	    // 선택된 카드 초기화
	    selectedCard = null;

	    // 경고 메시지 초기화
	    warningLabel.setText("");

	    // 라운드 종료 메시지
	    JOptionPane.showMessageDialog(this, "라운드가 종료되었습니다!", "결과", JOptionPane.INFORMATION_MESSAGE);

	    // 다음 라운드 시작 준비
	    startRound();
	}
}

