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
	private final Player player1;         // Player 1 정보
	private final Player player2;         // Player 2 정보

	private boolean isMyTurn = false; // 현재 턴이 내 차례인지 여부

	private JLabel player1Label; // 상대방 이름 라벨
	private JLabel player2Label; // 내 이름 라벨

	private JPanel rightPanel; // 오른쪽 UI를 위한 패널

	private boolean isCardSubmitted = false;
	private int selectedCardIndex = -1; // 선택된 카드의 원래 인덱스
	private Card previousCenterCard = null; // 중앙 카드 이전 상태

	/**
	 * GamePanel 생성자: UI 초기화 및 게임 상태 관리 객체 생성
	 * @param player1 첫 번째 플레이어 객체
	 * @param player2 두 번째 플레이어 객체
	 * @param dos 서버와 통신하기 위한 DataOutputStream
	 */
	public GamePanel(Player player1, Player player2, DataOutputStream dos) {
		this.game = new BlackAndWhiteGame(player1, player2);
		this.dos = dos;
		cardLayout = new CardLayout();
		this.player1 = player1;
		this.player2 = player2;
		setLayout(cardLayout);

		// 시작 화면 설정
		startScreen = new JPanel(new BorderLayout());
		waitingLabel = new JLabel("서버 메시지 대기중...", SwingConstants.CENTER);
		waitingLabel.setFont(new Font("굴림", Font.BOLD, 20));
		startScreen.add(waitingLabel, BorderLayout.CENTER);
		add(startScreen, "START_SCREEN");

		// 게임 화면 설정
		gameScreen = new JPanel(new BorderLayout());
		setupGameScreen();
		add(gameScreen, "GAME_SCREEN");

		// `submitButton`은 `setupGameScreen`에서 초기화되므로 이후 호출
		if (submitButton != null) {
			submitButton.setEnabled(false);
		} else {
			System.err.println("[ERROR] submitButton이 초기화되지 않았습니다.");
		}

		setupReadyButton();
	}

	/**
	 * 서버 메시지 처리: 리더 여부 및 게임 시작 메시지 처리
	 */
	public void handleServerMessage(String message) {
		SwingUtilities.invokeLater(() -> {
			try {
	            System.out.println("[DEBUG] 서버로부터 메시지 수신: " + message);

				if (message.startsWith("OPPONENT_NAME:")) {
					// 상대방 이름 업데이트
					String opponentName = message.split(":")[1];
					setOpponentName(opponentName);
				} else if (message.startsWith("PLAYER_NAME:")) {
					// 본인 이름 업데이트
					String playerName = message.split(":")[1];
					setPlayerName(playerName);
				} else if (message.startsWith("PLAYER_NUMBER:")) {
					isLeader = Integer.parseInt(message.split(":")[1]) == 1;
					setLeader(isLeader);
				} else if (message.startsWith("PLAYER_COUNT:")) {
					waitingLabel.setText("게임 시작 대기중...");
				} else if (message.equals("GAME_START")) {
	                System.out.println("[DEBUG] GAME_START 메시지 처리 시작");
					startGame();
				} else if (message.startsWith("PLAYER_READY:")) {
					String playerName = message.split(":")[1];
					waitingLabel.setText(playerName + " 준비 완료");					
				} else if (message.startsWith("CARD_SELECTED:")) {
					// 상대방 카드 선택 처리
					int opponentCardNumber = Integer.parseInt(message.split(":")[1]);

					// 상대방 카드 배열에서 카드 제거
					Card opponentCard = game.getPlayer1().getCards().stream()
									.filter(card -> card.getNumber() == opponentCardNumber)
									.findFirst()
									.orElse(null);

					if (opponentCard != null) {
						game.getPlayer1().getCards().remove(opponentCard);
						renderOpponentCards(); // 상대방 카드 UI 갱신

						// 상대방 중앙 카드 업데이트 (뒷면 표시)
						showOpponentCard(opponentCard.isBlack() ? "BLACK" : "WHITE");
					}

				} else if (message.startsWith("SHOW_OPPONENT_CARD:")) {
					String cardColor = message.split(":")[1];
					showOpponentCard(cardColor); // 상대방 카드의 뒷면 표시

				} else if (message.startsWith("TURN_START:")) {
					// 턴 시작 처리
					String turnPlayerName = message.split(":")[1];
					boolean isMyTurn = turnPlayerName.equals(player2.getName()); // 내 이름과 비교
					setTurn(isMyTurn);
					
					if (isMyTurn) {
	                    System.out.println("[DEBUG] 내 턴이 시작되었습니다.");
	                    warningLabel.setText("당신의 턴입니다!");
	                } else {
	                    System.out.println("[DEBUG] 상대방의 턴입니다.");
	                    warningLabel.setText("상대방의 턴을 기다리세요...");
	                }

				} else if (message.equals("TURN_END")) {
					// 상대방이 카드를 제출한 후, 서버에서 메시지 처리
	                System.out.println("[DEBUG] 상대방이 턴을 종료했습니다.");
	                // 상대방의 턴이 종료되었으므로, 내 턴을 활성화
	                setTurn(true);

				} else if (message.startsWith("ROUND_RESULT:")) {
					// 라운드 결과 처리
					String[] parts = message.split(":");
					int opponentCardNumber = Integer.parseInt(parts[1]);
					String winner = parts[2];
					int player1Score = Integer.parseInt(parts[3]);
					int player2Score = Integer.parseInt(parts[4]);

					// 상대방 카드 표시 (앞면 표시)
					Card opponentCard = new Card(opponentCardNumber);
					player1CenterCard.setIcon(opponentCard.getCardImage(true));

					// 점수 업데이트
					game.getPlayer1().setPoints(player1Score);
					game.getPlayer2().setPoints(player2Score);
					updateScores();

					// 승리 메시지 표시
					String resultMessage;
					if (winner.equals(player1.getName())) {
						resultMessage = "Player1 승리!";
					} else if (winner.equals(player2.getName())) {
						resultMessage = "Player2 승리!";
					} else {
						resultMessage = "무승부!";
					}

					// 라운드 결과를 화면에 표시
					showRoundResult(resultMessage);

					// 다음 라운드 시작
					startRound();

				} else if (message.equals("GAME_OVER")) {
					JOptionPane.showMessageDialog(this, "게임 종료!", "결과", JOptionPane.INFORMATION_MESSAGE);
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
			// 리더의 경우에도 본인의 화면을 강제로 게임 화면으로 전환
			SwingUtilities.invokeLater(this::startGame);
		} catch (IOException e) {
			System.err.println("[ERROR] GAME_START 메시지 전송 실패: " + e.getMessage());
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

				 // 강제로 UI 갱신
	            gameScreen.revalidate();
	            gameScreen.repaint();
	            
	            // 추가 디버깅: 현재 표시 중인 화면 출력
	            if (gameScreen.isShowing()) {
	                System.out.println("[DEBUG] 게임 화면이 표시되고 있습니다.");
	            } else {
	                System.err.println("[ERROR] 게임 화면 표시 실패. CardLayout 확인 필요.");
	            }
	            
				// 랜덤으로 선플레이어 결정
				boolean isFirstTurn = new Random().nextBoolean();
				isMyTurn = isFirstTurn; // 현재 플레이어가 선플레이어인지 설정

				if (isMyTurn) {
					setTurn(true); // 내 턴 설정 및 타이머 시작
					warningLabel.setText("당신의 턴입니다!");
				} else {
					setTurn(false); // 상대방 턴으로 설정
					warningLabel.setText("상대방의 턴을 기다리세요...");
				}

				// 서버로 선플레이어 정보를 전송
				dos.writeUTF("TURN_START:" + (isMyTurn ? "SELF" : "OPPONENT"));
				dos.flush();

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
		JPanel mainPanel = new JPanel(new BorderLayout());

		JPanel leftPanel = new JPanel(new GridLayout(3, 1, 0, 10));
		player1ScoreLabel = new JLabel("0", SwingConstants.CENTER);
		player1ScoreLabel.setFont(new Font("굴림", Font.BOLD, 20));

		roundLabel = new JLabel("1R", SwingConstants.CENTER);
		roundLabel.setFont(new Font("굴림", Font.BOLD, 24));

		player2ScoreLabel = new JLabel("0", SwingConstants.CENTER);
		player2ScoreLabel.setFont(new Font("굴림", Font.BOLD, 20));

		leftPanel.add(player1ScoreLabel);
		leftPanel.add(roundLabel);
		leftPanel.add(player2ScoreLabel);

		JPanel topPanel = new JPanel(new BorderLayout());
		JPanel player1Wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 10));
		player1Label = new JLabel("상대방", SwingConstants.LEFT);
		player1Label.setFont(new Font("굴림", Font.BOLD, 16));
		player1Wrapper.add(player1Label);

		JPanel player1CardRow = new JPanel(new GridLayout(1, 9, 10, 0));
		for (Card card : game.getPlayer1().getCards()) {
			JLabel cardBack = new JLabel(card.getCardImage(false));
			cardBack.setPreferredSize(new Dimension(70, 98));
			player1CardRow.add(cardBack);
		}
		player1Wrapper.add(player1CardRow);
		topPanel.add(player1Wrapper, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new BorderLayout());
		JPanel player2Wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 10));
		player2Label = new JLabel("나", SwingConstants.LEFT);
		player2Label.setFont(new Font("굴림", Font.BOLD, 16));
		player2Wrapper.add(player2Label);

		player2CardsPanel = new JPanel(new GridLayout(1, 9, 10, 0));
		renderPlayerCards(); // 카드 렌더링 호출
		player2Wrapper.add(player2CardsPanel);

		bottomPanel.add(player2Wrapper, BorderLayout.CENTER);

		// Initialize and add warningLabel to the bottom panel
		warningLabel = new JLabel("", SwingConstants.CENTER);
		warningLabel.setFont(new Font("굴림", Font.BOLD, 16));
		warningLabel.setForeground(Color.RED); // Set text color to red for warnings
		bottomPanel.add(warningLabel, BorderLayout.NORTH);

		JPanel centerPanel = new JPanel(new GridLayout(1, 2));
		player1CenterCard = new JLabel();
		player1CenterCard.setHorizontalAlignment(SwingConstants.CENTER);
		player2CenterCard = new JLabel();
		player2CenterCard.setHorizontalAlignment(SwingConstants.CENTER);
		centerPanel.add(player1CenterCard);
		centerPanel.add(player2CenterCard);

		rightPanel = new JPanel(new GridBagLayout());
		setTurn(isMyTurn);

		mainPanel.add(leftPanel, BorderLayout.WEST);
		mainPanel.add(topPanel, BorderLayout.NORTH);
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);
		mainPanel.add(centerPanel, BorderLayout.CENTER);
		mainPanel.add(rightPanel, BorderLayout.EAST);

		gameScreen.add(mainPanel, BorderLayout.CENTER);
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
		if (isCardSubmitted) return; // 중복 호출 방지
		isCardSubmitted = true;

		if (selectedCard == null) {
			Random random = new Random();
			selectedCard = game.getPlayer2().getCards().get(random.nextInt(game.getPlayer2().getCards().size()));
		}

		submitCard();
		SwingUtilities.invokeLater(() -> player2CenterCard.setIcon(selectedCard.getCardImage(true)));

		try {
			dos.writeUTF("CARD_SELECTED:" + selectedCard.getNumber());
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		JOptionPane.showMessageDialog(this, "시간 초과! 랜덤 카드가 제출됩니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
		startRound();
	}
	
	/**
	 * 카드 제출 시 동작
	 */
	private void submitCard() {
		if (!isMyTurn) {
			warningLabel.setText("당신의 턴이 아닙니다!");
			return;
		}

		if (selectedCard == null) {
			warningLabel.setText("카드를 선택해주세요!");
			return;
		}

		warningLabel.setText(""); // 경고 메시지 초기화

		try {
			// 상대방에게 흑/백 카드 정보 전송
			String cardColor = selectedCard.isBlack() ? "BLACK" : "WHITE";
			dos.writeUTF("SHOW_OPPONENT_CARD:" + cardColor);
			dos.flush();

			// 중앙 카드 업데이트 (본인은 숫자 카드가 보임)
			player2CenterCard.setIcon(selectedCard.getCardImage(true));

			// 선택된 카드를 카드 배열에서 제거
			game.getPlayer2().getCards().remove(selectedCard);

			// 선택된 상태 초기화
			selectedCard = null;
			selectedCardIndex = -1;
			previousCenterCard = null;

			// 카드 UI 갱신
			renderPlayerCards();

			// 턴 종료 메시지 전송
			dos.writeUTF("TURN_END");
			dos.flush();

			// 내 턴을 종료하고, 상대방의 턴을 기다림
			setTurn(false);

			// 상대방이 카드를 제출했는지 확인
			Card opponentCard = game.getPlayer1().getCards().isEmpty() ? null : game.getPlayer1().getCards().get(0);

			// 양 플레이어가 모두 카드를 제출했을 때 라운드 처리
			if (opponentCard != null && selectedCard != null) {
				processRound(opponentCard); // 라운드 처리 호출
			}

		} catch (IOException e) {
			e.printStackTrace();
			warningLabel.setText("카드 제출 중 오류가 발생했습니다!");
		}
	}

	private void renderPlayerCards() {
		player2CardsPanel.removeAll(); // 기존 카드 UI 제거

		// 현재 카드 배열을 기반으로 버튼 생성
		for (int i = 0; i < game.getPlayer2().getCards().size(); i++) {
			int cardIndex = i; // 현재 카드의 인덱스 저장
			Card card = game.getPlayer2().getCards().get(cardIndex);

			JButton cardButton = new JButton(card.getCardImage(true));
			cardButton.setPreferredSize(new Dimension(70, 98));

			// 자신의 턴일 때만 버튼 활성화
			cardButton.setEnabled(isMyTurn);

			if (isMyTurn) {
				cardButton.addActionListener(e -> {
					// 선택된 카드 상태를 관리
					if (selectedCard != null) {
						// 이전 선택된 카드 복원
						if (previousCenterCard != null && selectedCardIndex != -1) {
							game.getPlayer2().getCards().add(selectedCardIndex, previousCenterCard);
						}
					}

					// 새로운 카드 선택
					selectedCard = card;
					selectedCardIndex = cardIndex;

					// 선택된 카드를 중앙 카드 영역으로 이동
					previousCenterCard = selectedCard;
					game.getPlayer2().getCards().remove(cardIndex);

					// 중앙 카드 이미지 갱신
					player2CenterCard.setIcon(card.getCardImage(true));

					// 카드 UI 갱신
					renderPlayerCards();
				});
			}

			player2CardsPanel.add(cardButton); // 패널에 카드 버튼 추가
		}

		// 카드 배열 상태가 변경되었을 경우 중앙에 있는 카드를 복원
		if (previousCenterCard != null && selectedCardIndex != -1) {
			game.getPlayer2().getCards().add(selectedCardIndex, previousCenterCard);
			previousCenterCard = null; // 복원 후 초기화
			selectedCardIndex = -1;
		}

		// UI 갱신
		player2CardsPanel.revalidate();
		player2CardsPanel.repaint();
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

			// 라운드 결과 메시지
			String winnerMessage;
			if (game.getLastRoundWinner().equals(game.getPlayer1().getName())) {
				winnerMessage = "Player1 승리!";
			} else if (game.getLastRoundWinner().equals(game.getPlayer2().getName())) {
				winnerMessage = "Player2 승리!";
			} else {
				winnerMessage = "무승부!";
			}

		// 중앙에 결과 표시
		showRoundResult(winnerMessage);


		// 다음 라운드 시작 준비
	    startRound();
	}

	// 자신의 카드 앞면을 보여줌
	public void showMyCard(int cardNumber) {
		Card myCard = new Card(cardNumber);
		player2CenterCard.setIcon(myCard.getCardImage(true)); // 자신의 카드 앞면
		revalidate();
		repaint();
	}

	// 상대방의 흑백 이미지를 보여줌
	public void showOpponentCard(String cardColor) {
		String imageName = cardColor.equals("BLACK") ? "Card-Black.png" : "Card-White.png";
		java.net.URL imageUrl = getClass().getResource("/cards/" + imageName);
		if (imageUrl != null) {
			ImageIcon opponentCardImage = new ImageIcon(imageUrl);
			Image resizedImage = opponentCardImage.getImage().getScaledInstance(80, 120, Image.SCALE_SMOOTH);
			player1CenterCard.setIcon(new ImageIcon(resizedImage));
		} else {
			System.err.println("이미지를 불러올 수 없습니다: " + imageName);
		}
		revalidate();
		repaint();
	}

	public void setTurn(boolean isMyTurn) {
		this.isMyTurn = isMyTurn;

		SwingUtilities.invokeLater(() -> {
			if (rightPanel == null) {
				System.err.println("[ERROR] rightPanel이 초기화되지 않았습니다.");
				return;
			}

			rightPanel.removeAll(); // 기존 컴포넌트 제거

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.insets = new Insets(10, 0, 10, 0);

			if (isMyTurn) {
				// 타이머 라벨 추가
				if (timerLabel == null) {
					timerLabel = new JLabel("남은 시간: 30초", SwingConstants.CENTER);
					timerLabel.setFont(new Font("굴림", Font.BOLD, 20));
					timerLabel.setForeground(Color.RED);
				}
				timerLabel.setText("남은 시간: " + timeLeft + "초");
				rightPanel.add(timerLabel, gbc);

				// 카드 제출 버튼 추가
				if (submitButton == null) {
					submitButton = new JButton("카드 제출");
					submitButton.setFont(new Font("굴림", Font.BOLD, 16));
					submitButton.setPreferredSize(new Dimension(150, 40));
					submitButton.addActionListener(e -> submitCard());
				}
				submitButton.setEnabled(true);
				gbc.gridy = 1;
				rightPanel.add(submitButton, gbc);

				startTurnTimer(); // 타이머 시작
			} else {
				// 상대방의 턴일 때 -> "턴 기다리는 중" 라벨 표시
				JLabel waitingLabel = new JLabel("턴 기다리는 중", SwingConstants.CENTER);
				waitingLabel.setFont(new Font("굴림", Font.BOLD, 20));
				waitingLabel.setForeground(Color.GRAY);
				rightPanel.add(waitingLabel, gbc);

				stopTurnTimer(); // 타이머 정지

				if (submitButton != null) {
					submitButton.setEnabled(false);
				}
			}

			// 플레이어 카드 UI 업데이트
			renderPlayerCards();

			// UI 갱신
			rightPanel.revalidate();
			rightPanel.repaint();
		});
	}

	private Timer turnTimer; // 턴 타이머
	private void startTurnTimer() {
		timeLeft = 30; // 30초로 초기화
		updateTimerLabel(); // 초기 타이머 UI 표시

		if (turnTimer != null) {
			turnTimer.stop(); // 기존 타이머 정지
		}

		turnTimer = new Timer(1000, e -> {
			if (timeLeft > 0) {
				timeLeft--; // 남은 시간 감소
				updateTimerLabel();
			} else {
				turnTimer.stop(); // 시간이 끝나면 타이머 정지
				autoSubmitCard(); // 시간 초과 시 자동으로 카드 제출
			}
		});
		turnTimer.start();
	}

	private void stopTurnTimer() {
		if (turnTimer != null) {
			turnTimer.stop();
		}
	}

	public void setOpponentName(String opponentName) {
		SwingUtilities.invokeLater(() -> {
			player1Label.setText(opponentName); // 상대방 이름 업데이트
		});
	}

	public void setPlayerName(String playerName) {
		SwingUtilities.invokeLater(() -> {
			player2Label.setText(playerName); // 내 이름 업데이트
		});
	}

	public boolean isLeader() {
		return isLeader;
	}

	private void renderOpponentCards() {
		player1CardsPanel.removeAll();

		game.getPlayer1().getCards().forEach(card -> {
			JLabel cardBack = new JLabel(card.getCardImage(false));
			cardBack.setPreferredSize(new Dimension(70, 98));
			player1CardsPanel.add(cardBack);
		});

		player1CardsPanel.revalidate();
		player1CardsPanel.repaint();
	}

	public String getPlayerName() {
		return player2.getName(); // 현재 플레이어의 이름 반환
	}

	private void showRoundResult(String winnerMessage) {
		JLabel resultLabel = new JLabel(winnerMessage, SwingConstants.CENTER);
		resultLabel.setFont(new Font("굴림", Font.BOLD, 28));
		resultLabel.setForeground(Color.BLACK);

		// 중앙 영역에 라운드 결과 표시
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(resultLabel, BorderLayout.CENTER);

		gameScreen.add(centerPanel, BorderLayout.CENTER);

		// 2초 후 결과 제거
		Timer resultTimer = new Timer(2000, e -> {
			gameScreen.remove(centerPanel);
			gameScreen.revalidate();
			gameScreen.repaint();
		});
		resultTimer.setRepeats(false);
		resultTimer.start();
	}

}
