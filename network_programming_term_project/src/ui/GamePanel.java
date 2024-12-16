package ui;

import models.BlackAndWhiteGame;
import models.Card;
import models.Player;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class GamePanel extends JPanel {
  private final BlackAndWhiteGame game; // 게임 로직
  private final JLabel player1ScoreLabel; // Player 1 점수 표시
  private final JLabel player2ScoreLabel; // Player 2 점수 표시
  private final JLabel roundLabel; // 라운드 표시
  private final JLabel timerLabel; // 타이머 표시
  private final JButton submitButton; // 카드 제출 버튼
  private final JLabel warningLabel; // 경고 메시지 표시
  private final JPanel player1CardsPanel; // 상대방 카드 패널
  private final JPanel player2CardsPanel; // 내 카드 패널
  private final JLabel centerCardLabel; // 중앙 카드 제출 영역
  private Timer timer; // 타이머
  private int timeLeft; // 남은 시간
  private Card selectedCard; // 플레이어가 선택한 카드
  private int roundNumber = 1; // 라운드 번호

  // 초기 화면 컴포넌트
  private final JButton startButton;
  private final JPanel gamePanel;

  public GamePanel(Player player1, Player player2) {
    this.game = new BlackAndWhiteGame(player1, player2);
    setLayout(new BorderLayout());
    setBackground(Color.GREEN);

    // 초기 화면: 시작하기 버튼
    startButton = new JButton("시작하기");
    startButton.setFont(new Font("굴림", Font.BOLD, 30));
    startButton.setBackground(Color.WHITE);
    startButton.addActionListener(e -> startGame());
    add(startButton, BorderLayout.CENTER);

    // 게임 화면 준비
    gamePanel = new JPanel(new BorderLayout());
    gamePanel.setBackground(Color.GREEN);

    // 왼쪽 점수 및 라운드 패널
    JPanel leftPanel = new JPanel(new GridLayout(3, 1));
    leftPanel.setBackground(Color.GREEN);
    player1ScoreLabel = new JLabel("Player1: 0", SwingConstants.CENTER);
    roundLabel = new JLabel("Round: 1", SwingConstants.CENTER);
    player2ScoreLabel = new JLabel("Player2: 0", SwingConstants.CENTER);
    leftPanel.add(player1ScoreLabel);
    leftPanel.add(roundLabel);
    leftPanel.add(player2ScoreLabel);
    gamePanel.add(leftPanel, BorderLayout.WEST);

    // 중앙 카드 패널
    centerCardLabel = new JLabel("", SwingConstants.CENTER);
    centerCardLabel.setFont(new Font("굴림", Font.BOLD, 20));
    centerCardLabel.setBackground(Color.WHITE);
    centerCardLabel.setOpaque(true);
    gamePanel.add(centerCardLabel, BorderLayout.CENTER);

    // 오른쪽 타이머와 제출 버튼 패널
    JPanel rightPanel = new JPanel(new BorderLayout());
    rightPanel.setBackground(Color.GREEN);
    timerLabel = new JLabel("남은 시간: 30초", SwingConstants.CENTER);
    submitButton = new JButton("카드 제출");
    submitButton.addActionListener(e -> submitCard());
    warningLabel = new JLabel("", SwingConstants.CENTER);
    warningLabel.setForeground(Color.RED);
    rightPanel.add(timerLabel, BorderLayout.NORTH);
    rightPanel.add(submitButton, BorderLayout.CENTER);
    rightPanel.add(warningLabel, BorderLayout.SOUTH);
    gamePanel.add(rightPanel, BorderLayout.EAST);

    // 카드 패널
    JPanel cardsPanel = new JPanel(new GridLayout(2, 1));
    player1CardsPanel = new JPanel(new FlowLayout()); // 상대방 카드
    player2CardsPanel = new JPanel(new FlowLayout()); // 내 카드
    cardsPanel.add(player1CardsPanel);
    cardsPanel.add(player2CardsPanel);
    gamePanel.add(cardsPanel, BorderLayout.SOUTH);

    // 게임 화면 준비 (초기에는 보이지 않음)
    gamePanel.setVisible(false);
    add(gamePanel, BorderLayout.CENTER);
  }

  // 게임 시작
  private void startGame() {
    startButton.setVisible(false);
    gamePanel.setVisible(true); // 게임 화면 보이기
    renderCards();
    startRound();
  }

  // 카드 렌더링
  private void renderCards() {
    player1CardsPanel.removeAll();
    player2CardsPanel.removeAll();

    // 상대방 카드 (뒷면만 보임)
    game.getPlayer1().getCards().forEach(card -> {
      JButton cardButton = new JButton(card.getCardImage(false));
      cardButton.setEnabled(false);
      player1CardsPanel.add(cardButton);
    });

    // 내 카드 (숫자 보임)
    game.getPlayer2().getCards().forEach(card -> {
      JButton cardButton = new JButton(card.getCardImage(true));
      cardButton.addActionListener(e -> selectedCard = card); // 카드 선택
      player2CardsPanel.add(cardButton);
    });

    revalidate();
    repaint();
  }

  // 라운드 시작
  private void startRound() {
    selectedCard = null;
    timeLeft = 30;
    timerLabel.setText("남은 시간: " + timeLeft + "초");
    startTimer();
  }

  // 타이머 시작
  private void startTimer() {
    if (timer != null) {
      timer.stop();
    }
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

  // 카드 제출
  private void submitCard() {
    if (selectedCard == null) {
      warningLabel.setText("카드를 선택해주세요!");
    } else {
      warningLabel.setText("");
      timer.stop();
      processRound();
    }
  }

  // 자동 카드 제출
  private void autoSubmitCard() {
    Random random = new Random();
    int randomIndex = random.nextInt(game.getPlayer2().getCards().size());
    selectedCard = game.getPlayer2().getCards().get(randomIndex);
    processRound();
  }

  // 라운드 진행 및 결과 처리
  private void processRound() {
    Card opponentCard = game.getPlayer1().getCards().get(new Random().nextInt(game.getPlayer1().getCards().size()));
    centerCardLabel.setText("내 카드: " + selectedCard.getNumber() + " | 상대 카드: " + opponentCard.getNumber());
    game.playRound(selectedCard, opponentCard);

    // 점수 및 라운드 업데이트
    player1ScoreLabel.setText("Player1: " + game.getPlayer1().getPoints());
    player2ScoreLabel.setText("Player2: " + game.getPlayer2().getPoints());
    roundLabel.setText("Round: " + (++roundNumber));

    if (roundNumber > 9) {
      endGame();
    } else {
      renderCards();
      startRound();
    }
  }

  // 게임 종료
  private void endGame() {
    String winner = game.getPlayer1().getPoints() > game.getPlayer2().getPoints()
            ? game.getPlayer1().getName() : game.getPlayer2().getName();
    JOptionPane.showMessageDialog(this, "게임 종료! 최종 승자: " + winner, "게임 종료", JOptionPane.INFORMATION_MESSAGE);

    // 게임 상태 초기화
    startButton.setVisible(true);
    gamePanel.setVisible(false);
    roundNumber = 1;
    game.getPlayer1().reset();
    game.getPlayer2().reset();

    // UI 초기화
    roundLabel.setText("Round: 1");
    player1ScoreLabel.setText("Player1: 0");
    player2ScoreLabel.setText("Player2: 0");
    centerCardLabel.setText("");
    player1CardsPanel.removeAll();
    player2CardsPanel.removeAll();
    revalidate();
    repaint();
  }

  // 서버로부터 메시지 처리 (예: 플레이어 수)
  public void handleServerMessage(String message) {
    if (message.startsWith("PLAYER_COUNT:")) {
      int playerCount = Integer.parseInt(message.split(":")[1]);
      if (playerCount == 2) {
        startButton.setVisible(true);
      }
    }
  }
}
