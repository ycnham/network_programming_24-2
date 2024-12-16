package models;

import java.util.Collections;

public class BlackAndWhiteGame {
  private final Player player1;
  private final Player player2;
  private Player currentPlayer; // 현재 선플레이어
  private String lastRoundWinner; // 마지막 라운드 승자

  public BlackAndWhiteGame(Player player1, Player player2) {
    this.player1 = player1;
    this.player2 = player2;
    shuffleCards();
    determineFirstPlayer(); // 초기 선플레이어 결정
  }

  // 카드 섞기
  private void shuffleCards() {
    Collections.shuffle(player1.getCards());
    Collections.shuffle(player2.getCards());
  }

  // 초기 선플레이어를 랜덤으로 설정
  private void determineFirstPlayer() {
    currentPlayer = Math.random() < 0.5 ? player1 : player2;
    System.out.println("선플레이어: " + currentPlayer.getName());
  }

  // 현재 선플레이어 반환
  public Player getCurrentPlayer() {
    return currentPlayer;
  }

  // 플레이어 반환
  public Player getPlayer1() {
    return player1;
  }

  public Player getPlayer2() {
    return player2;
  }

  // 라운드 실행
  public void playRound(Card card1, Card card2) {
    System.out.println("Player1 카드: " + card1.getNumber() + ", Player2 카드: " + card2.getNumber());

    if (card1.getNumber() > card2.getNumber()) {
      player1.addPoint();
      lastRoundWinner = player1.getName();
      currentPlayer = player1; // 승자가 다음 선플레이어
    } else if (card2.getNumber() > card1.getNumber()) {
      player2.addPoint();
      lastRoundWinner = player2.getName();
      currentPlayer = player2; // 승자가 다음 선플레이어
    } else {
      lastRoundWinner = "무승부";
    }
  }

  // 마지막 라운드 승자 반환
  public String getLastRoundWinner() {
    return lastRoundWinner;
  }

  // 게임 시작 조건 확인 (임시)
  public boolean isReadyToStart() {
    return true; // 조건 없이 바로 시작할 수 있도록 설정
  }
}
