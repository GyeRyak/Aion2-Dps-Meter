# Aion2-Dps-Meter Project Context

## 1. 프로젝트 개요
이 프로젝트는 **아이온2 (Aion 2)** 게임을 위한 실시간 DPS(초당 데미지) 측정기입니다. 네트워크 패킷을 캡처하여 전투 데이터를 분석하고, 투명 오버레이 웹 UI를 통해 사용자에게 통계를 제공합니다.

## 2. 아키텍처 및 데이터 흐름
데이터는 **[캡처] -> [가공] -> [계산] -> [시각화]**의 흐름을 따릅니다.

1.  **캡처 (Capture)**: `PcapCapturer`가 `pcap4j`를 이용해 TCP 패킷(포트 13328, NCSOFT 서버)을 수집합니다.
2.  **가공 (Processing)**:
    *   `StreamAssembler`: 파편화된 TCP 스트림을 재조립하고 패킷 경계(Magic Sequence)를 식별합니다.
    *   `StreamProcessor`: 프로토콜을 해독하여 데미지, 유저 정보, 소환수 관계 등을 추출합니다.
3.  **저장 및 계산 (Storage & Calc)**:
    *   `DataStorage`: 분석된 데이터를 메모리에 스레드 안전하게 저장합니다.
    *   `DpsCalculator`: 저장된 데이터를 기반으로 DPS, 기여도, 직업 추론 등을 수행합니다.
4.  **시각화 (Display)**:
    *   `BrowserApp`: JavaFX WebView를 통해 `index.html`을 렌더링합니다.
    *   `JSBridge`: Kotlin 백엔드와 JS 프론트엔드 간의 데이터 통신을 담당합니다.

## 3. 핵심 기술 스택
*   **Language**: Kotlin (Coroutines 활용)
*   **Network**: `pcap4j` (Packet Capture)
*   **UI**: Compose for Desktop + JavaFX WebView
*   **Serialization**: `kotlinx-serialization`

## 4. 주요 컴포넌트
*   `Main.kt`: 앱 진입점 및 컴포넌트 연결.
*   `PcapCapturer.kt`: 네트워크 패킷 캡처 및 필터링.
*   `DpsCalculator.kt`: 전투 통계 계산 및 직업 추론 로직.
*   `DataStorage.kt`: 데이터 중앙 저장소.
*   `BrowserApp.kt`: UI 윈도우 및 웹뷰 관리.
*   `src/main/resources/`: 웹 프론트엔드 리소스 (HTML/CSS/JS).

---

## 5. 작업 규칙 (Rules)

### 언어 (Language)
*   **모든 답변은 반드시 '한국어(Korean)'로 작성해야 합니다.**

### 버전 관리 (Version Control)
*   **기능 개발이 완료될 때마다 논리적인 단위로 Git 커밋을 수행해야 합니다.**
*   커밋 메시지는 변경 사항을 명확히 알 수 있도록 작성합니다.
