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
  private JLabel centerCardLabel;       // 중앙 카드 영역 라벨

  private Timer timer;          // 타이머 객체
  private int timeLeft;         // 라운드 남은 시간
  private Card selectedCard;    // 선택된 카드
  private int roundNumber = 1;  // 현재 라운드 번호

  private final JButton startButton;    // 게임 시작 버튼
  private final JLabel waitingLabel;    // 대기 메시지 라벨
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
    startButton = new JButton("게임 시작");
    startButton.setFont(new Font("굴림", Font.BOLD, 20));
    startButton.setBackground(Color.WHITE);
    startButton.setPreferredSize(new Dimension(200, 50));
    startButton.addActionListener(e -> startGameRequest());

    waitingLabel = new JLabel("게임 시작 대기중...", SwingConstants.CENTER);
    waitingLabel.setFont(new Font("굴림", Font.BOLD, 20));

    startScreen.add(startButton, BorderLayout.CENTER);
    startScreen.add(waitingLabel, BorderLayout.SOUTH);
    add(startScreen, "START_SCREEN");

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
      System.out.println("[DEBUG] Received message: " + message);

      if (message.startsWith("PLAYER_NUMBER:")) {
        isLeader = Integer.parseInt(message.split(":")[1]) == 1;
        setLeader(isLeader);
      }

      if (message.startsWith("PLAYER_COUNT:")) {
        playerCount = Integer.parseInt(message.split(":")[1]);
        waitingLabel.setText("게임 시작 대기중... (" + playerCount + "명 접속)");
      }

      if (message.equals("GAME_START")) {
        startGame();
      }
    });
  }

  /**
   * 리더 여부에 따라 시작 버튼과 대기 메시지 표시 설정
   */
  public void setLeader(boolean leader) {
    this.isLeader = leader;
    startButton.setVisible(isLeader);
    waitingLabel.setVisible(!isLeader);
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
    cardLayout.show(this, "GAME_SCREEN");
    renderCards();
    startRound();
  }

  /**
   * 게임 화면 UI 초기화
   */
  private void setupGameScreen() {
    // 상태 표시 패널
    JPanel leftPanel = new JPanel(new GridLayout(3, 1));
    player1ScoreLabel = new JLabel("Player1: 0", SwingConstants.CENTER);
    roundLabel = new JLabel("Round: 1", SwingConstants.CENTER);
    player2ScoreLabel = new JLabel("Player2: 0", SwingConstants.CENTER);
    leftPanel.add(player1ScoreLabel);
    leftPanel.add(roundLabel);
    leftPanel.add(player2ScoreLabel);
    gameScreen.add(leftPanel, BorderLayout.WEST);

    // 중앙 카드 영역
    centerCardLabel = new JLabel("중앙 카드 영역", SwingConstants.CENTER);
    centerCardLabel.setFont(new Font("굴림", Font.BOLD, 20));
    centerCardLabel.setOpaque(true);
    centerCardLabel.setBackground(Color.WHITE);
    gameScreen.add(centerCardLabel, BorderLayout.CENTER);

    // 타이머와 버튼
    JPanel rightPanel = new JPanel(new BorderLayout());
    timerLabel = new JLabel("남은 시간: 30초", SwingConstants.CENTER);
    submitButton = new JButton("카드 제출");
    submitButton.addActionListener(e -> submitCard());
    warningLabel = new JLabel("", SwingConstants.CENTER);
    warningLabel.setForeground(Color.RED);
    rightPanel.add(timerLabel, BorderLayout.NORTH);
    rightPanel.add(submitButton, BorderLayout.CENTER);
    rightPanel.add(warningLabel, BorderLayout.SOUTH);
    gameScreen.add(rightPanel, BorderLayout.EAST);

    // 카드 패널
    JPanel cardsPanel = new JPanel(new GridLayout(2, 1));
    player1CardsPanel = new JPanel(new FlowLayout());
    player2CardsPanel = new JPanel(new FlowLayout());
    cardsPanel.add(player1CardsPanel);
    cardsPanel.add(player2CardsPanel);
    gameScreen.add(cardsPanel, BorderLayout.SOUTH);
  }

  private void renderCards() {
    player1CardsPanel.removeAll();
    player2CardsPanel.removeAll();

    game.getPlayer1().getCards().forEach(card -> player1CardsPanel.add(new JButton(card.getCardImage(false))));
    game.getPlayer2().getCards().forEach(card -> {
      JButton cardButton = new JButton(card.getCardImage(true));
      cardButton.addActionListener(e -> selectedCard = card);
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

  private void submitCard() {
    if (selectedCard == null) {
      warningLabel.setText("카드를 선택해주세요!");
    } else {
      processRound();
    }
  }

  private void autoSubmitCard() {
    Random random = new Random();
    selectedCard = game.getPlayer2().getCards().get(random.nextInt(game.getPlayer2().getCards().size()));
    processRound();
  }

  private void processRound() {
    centerCardLabel.setText("라운드 결과 처리중...");
    timer.stop();
  }
}
