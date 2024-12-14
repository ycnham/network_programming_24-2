# network_programming_24-2
24-2 네트워크 프로그래밍 텀프로젝트

주요 수정 사항

**2024.12.04**

1. AccessGame.java -> StartingUI.java로 수정함, UI는 변함 없음 (게임 초기 접속 화면)

2. client/ChatClientMain.java에 채팅 ui 관련 코드까지 모두 포함되어 있어서 실행 시 게임 로직뿐만 아니라 게임 화면 출력까지 한꺼번에 동작하던 부분 -> 채팅 ui 관련 코드는 ui/GameUI.java에 분리함 ui/ConnectionPanel.java에서 GameUI.java를 호출해서 게임 화면을 띄우는 방식으로 로직 변경함

참고 사항

- 새로 수정한 "ConnectionPanel.java", "GameUI.java", "StartingUI.java" : 코드 맨 윗줄에 코드 기능 및 동작 원리 작성해놓았으니 참고 바랍니다.
- 프로그램 실행 순서:
	1. ChatServer.java 실행
	2. StartingUI.java 2번 실행 (클라이언트 수 = 2)
	3. 각각 클라이언트에서 username 입력 후 "입장하기" 클릭

위 순서로 실행 시 게임 메인 화면에 접속하여 카드 게임 및 채팅을 이용할 수 있음

-----------------------------------------------------------------------------------------

**2024.12.14**

"x"버튼과 "나가기" 버튼 누르는 경우
-> 게임 종료 확인 팝업에서 "예", "아니오"에 따라 게임화면 유지 혹은 종료가 제대로 동작하도록 수정

-----------------------------------------------------------------------------------------
