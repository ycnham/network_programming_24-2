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
  private int playerCount = 0;          // 현재 접속한 플레이어 수

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
  }

  /**
   * 서버 메시지 처리: 리더 여부 및 게임 시작 메시지 처리
   */
  public void handleServerMessage(String message) {
    SwingUtilities.invokeLater(() -> {
      try {
        if (message.startsWith("PLAYER_NUMBER:")) {
          isLeader = Integer.parseInt(message.split(":")[1]) == 1;
          System.out.println("[DEBUG] PLAYER_NUMBER 처리: isLeader = " + isLeader); // 리더 여부 로그
          setLeader(isLeader);
        } else if (message.startsWith("PLAYER_COUNT:")) {
          playerCount = Integer.parseInt(message.split(":")[1]);
          System.out.println("[DEBUG] PLAYER_COUNT 처리: playerCount = " + playerCount); // 플레이어 수 로그
          waitingLabel.setText("게임 시작 대기중...");
        } else if (message.equals("GAME_START")) {
          System.out.println("[DEBUG] GAME_START 처리");
          startGame();
        } else if (message.startsWith("ROUND_RESULT")) {
          String[] parts = message.split(":");
          String result = parts[1];
          int player1Score = Integer.parseInt(parts[2]);
          int player2Score = Integer.parseInt(parts[3]);

          game.getPlayer1().setPoints(player1Score);
          game.getPlayer2().setPoints(player2Score);
          updateScores();
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

    System.out.println("[DEBUG] setLeader 호출됨. 리더 여부: " + (isLeader ? "리더" : "비리더"));

    SwingUtilities.invokeLater(() -> {
      try {
        System.out.println("[DEBUG] UI 갱신 시작");

        startScreen.removeAll();
        startScreen.setLayout(new BorderLayout());

        if (isLeader) {
          startButton = new JButton("게임 시작");
          startButton.setFont(new Font("굴림", Font.BOLD, 20));
          startButton.addActionListener(e -> startGameRequest());
          startScreen.add(startButton, BorderLayout.CENTER);
          System.out.println("[DEBUG] 게임 시작 버튼 추가됨");
        } else {
          waitingLabel.setText("게임 시작 대기중...");
          startScreen.add(waitingLabel, BorderLayout.CENTER);
          System.out.println("[DEBUG] 대기 메시지 추가됨");
        }

        startScreen.revalidate();
        startScreen.repaint();
        System.out.println("[DEBUG] UI 갱신 완료");
      } catch (Exception e) {
        e.printStackTrace();
        System.err.println("[ERROR] setLeader 중 예외 발생: " + e.getMessage());
      }
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

  /**
   * 게임 시작: 게임 화면으로 전환
   */
  private void startGame() {
    System.out.println("[DEBUG] startGame 호출됨. 게임 화면으로 전환 시도.");
    cardLayout.show(this, "GAME_SCREEN"); // 게임 화면으로 전환
    revalidate(); // UI 강제 갱신
    repaint();
    System.out.println("[DEBUG] 게임 화면으로 전환 완료.");
    renderCards(); // 카드 렌더링
    startRound(); // 라운드 시작
  }

  /**
   * 게임 화면 UI 초기화
   */
  private void setupGameScreen() {
    // 좌측 라운드 및 점수 표시
    JPanel scorePanel = new JPanel(new GridLayout(4, 1)); // 4개의 항목을 표시하기 위해 GridLayout 크기 변경
    roundLabel = new JLabel("Round: 1", SwingConstants.CENTER);
    player1ScoreLabel = new JLabel("Player1: 0", SwingConstants.CENTER);
    player2ScoreLabel = new JLabel("Player2: 0", SwingConstants.CENTER);
    timerLabel = new JLabel("남은 시간: 30초", SwingConstants.CENTER); // timerLabel 초기화

    scorePanel.add(roundLabel);
    scorePanel.add(player1ScoreLabel);
    scorePanel.add(player2ScoreLabel);
    scorePanel.add(timerLabel); // timerLabel 추가

    gameScreen.add(scorePanel, BorderLayout.WEST); // scorePanel 추가

    // 중앙 카드 배치 패널
    JPanel centerPanel = new JPanel(new GridLayout(1, 2));
    player1CenterCard = new JLabel("", SwingConstants.CENTER);
    player2CenterCard = new JLabel("", SwingConstants.CENTER);

    player1CenterCard.setOpaque(true);
    player1CenterCard.setBackground(Color.LIGHT_GRAY);

    player2CenterCard.setOpaque(true);
    player2CenterCard.setBackground(Color.LIGHT_GRAY);

    centerPanel.add(player1CenterCard);
    centerPanel.add(player2CenterCard);
    gameScreen.add(centerPanel, BorderLayout.CENTER); // centerPanel 추가

    // 상대방 카드 패널 (상단)
    player1CardsPanel = new JPanel(new FlowLayout());
    for (int i = 0; i < game.getPlayer1().getCards().size(); i++) {
      JLabel cardBack = new JLabel(new ImageIcon("Card-Back.png"));
      player1CardsPanel.add(cardBack);
    }
    gameScreen.add(player1CardsPanel, BorderLayout.NORTH); // player1CardsPanel 추가

    // 내 카드 패널 (하단)
    player2CardsPanel = new JPanel(new FlowLayout());
    for (Card card : game.getPlayer2().getCards()) {
      JButton cardButton = new JButton(card.getCardImage(true));
      cardButton.addActionListener(e -> {
        selectedCard = card;
        player2CenterCard.setIcon(card.getCardImage(true));
      });
      player2CardsPanel.add(cardButton);
    }
    gameScreen.add(player2CardsPanel, BorderLayout.SOUTH); // player2CardsPanel 추가
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

  private void startRound() {
    timeLeft = 30;
    timerLabel.setText("남은 시간: " + timeLeft + "초");
    timer = new Timer(1000, e -> {
      timeLeft--;
      timerLabel.setText("남은 시간: " + timeLeft + "초");
      if (timeLeft <= 0) {
        timer.stop();
        autoSubmitCard();
      }
    });
    timer.start();
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
    Random random = new Random();
    Card opponentCard = game.getPlayer1().getCards().get(random.nextInt(game.getPlayer1().getCards().size()));

    // 중앙 카드 영역에 카드 표시
    player1CenterCard.setIcon(opponentCard.getCardImage(true));
    player2CenterCard.setIcon(selectedCard.getCardImage(true));

    // 라운드 처리 호출
    processRound(opponentCard);

    // 게임 로직 처리
    game.playRound(opponentCard, selectedCard);
    updateScores();
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
   * 자동 제출: 타이머 종료 시 무작위 카드 제출
   */
  private void autoSubmitCard() {
    if (selectedCard == null) {
      Random random = new Random();
      selectedCard = game.getPlayer2().getCards().get(random.nextInt(game.getPlayer2().getCards().size()));
      player2CenterCard.setIcon(selectedCard.getCardImage(true));
    }

    // 상대방 카드 랜덤 선택
    Card opponentCard = game.getPlayer1().getCards().get(new Random().nextInt(game.getPlayer1().getCards().size()));

    // 라운드 처리 호출
    processRound(opponentCard);
  }


  /**
   * 라운드 결과 처리: 점수 갱신, 라운드 번호 업데이트, 다음 라운드 준비
   */
  private void processRound(Card opponentCard) {
    // 상대방 카드와 선택된 카드가 없으면 실행하지 않음
    if (opponentCard == null || selectedCard == null) {
      return;
    }

    // 타이머 중지
    timer.stop();

    // 중앙 카드 영역에 상대방 카드와 내 카드 표시
    player1CenterCard.setIcon(opponentCard.getCardImage(true));
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
