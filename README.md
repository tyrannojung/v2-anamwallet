# ANAM Wallet V2 - Modular Wallet Platform

> 🎯 **차세대 모듈러 블록체인 지갑 플랫폼**  
> 다양한 블록체인과 서비스를 하나의 지갑에서 통합 관리할 수 있는 Android 기반 모듈러 지갑입니다.

## 🌟 주요 특징

### 모듈러 아키텍처

- **플러그인 방식의 블록체인 지원**: 새로운 블록체인을 독립적인 모듈로 추가
- **웹앱 통합**: 정부24, 금융 서비스 등 다양한 웹 서비스를 미니앱으로 통합
- **멀티 프로세스 설계**: 총 5개 프로세스(Main, WebApp, Blockchain, WebView Renderer x2)로 격리된 실행 환경

### 사용자 경험

- **통합 인터페이스**: 모든 블록체인과 서비스를 하나의 일관된 UI로 관리
- **원클릭 전환**: 활성 블록체인을 즉시 전환하여 다양한 네트워크 지원
- **네이티브 성능**: Jetpack Compose 기반의 현대적이고 빠른 UI

## 📱 스크린샷

<table>
  <tr>
    <td><img src="docs/screenshots/main.png" width="200" alt="메인 화면"/></td>
    <td><img src="docs/screenshots/blockchain.png" width="200" alt="블록체인 앱"/></td>
    <td><img src="docs/screenshots/webapp.png" width="200" alt="웹앱"/></td>
    <td><img src="docs/screenshots/settings.png" width="200" alt="설정"/></td>
  </tr>
  <tr>
    <td align="center">메인 대시보드</td>
    <td align="center">블록체인 관리</td>
    <td align="center">통합 웹앱</td>
    <td align="center">설정</td>
  </tr>
</table>

## 🏗️ 아키텍처

### Clean Architecture + MVI

```
┌─────────────────┐
│   Presentation  │ ← Jetpack Compose + MVI Pattern
├─────────────────┤
│     Domain      │ ← Business Logic (Use Cases)
├─────────────────┤
│      Data       │ ← Repository Implementation
└─────────────────┘
```

### Multi-Process Architecture

```
┌─────────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────────┐
│ WebView Renderer│    │   WebApp    │    │    Main     │    │   Blockchain    │
│    Process      │◀──▶│   Process   │◀──▶│   Process   │◀──▶│    Process      │
│  (JavaScript)   │ JS │    (:app)   │AIDL│   (:main)   │AIDL│  (:blockchain)  │
└─────────────────┘    └─────────────┘    └─────────────┘    └─────────────────┘
                                                                        │ JS
                                                                        ▼
                                                              ┌─────────────────┐
                                                              │ WebView Renderer│
                                                              │    Process      │
                                                              │ (Blockchain JS) │
                                                              └─────────────────┘

프로세스 간 통신 흐름:

1. 웹앱 결제 요청 시 (예: 정부24):
   - WebView Renderer (JavaScript) → WebApp Process (JavaScript Bridge)
   - WebApp Process → Main Process (WebAppService via AIDL)
   - Main Process → Blockchain Process (BlockchainService via AIDL)
   - Blockchain Process → WebView Renderer (블록체인 JavaScript)

2. 각 프로세스의 역할:
   - **WebView Renderer**: JavaScript 실행 환경 (샌드박스)
   - **WebApp Process (:app)**: 웹앱 UI 및 JavaScript Bridge 관리
   - **Main Process**: 서비스 중계 및 앱 전체 상태 관리
   - **Blockchain Process (:blockchain)**: 블록체인별 독립 실행 환경
```

### 모듈 구조

- **app**: 메인 애플리케이션 진입점
- **core**: 공통 기능 및 리소스
  - common: 도메인 모델, 유틸리티
  - ui: 공통 UI 컴포넌트 및 테마
  - data: 데이터 저장소
- **feature**: 각 기능별 모듈
  - main: 대시보드
  - miniapp: 미니앱 관리 (webapp/blockchain)
  - settings: 설정
  - hub/browser/identity: 추가 기능

## 🚀 시작하기

### 요구사항

- Android Studio Ladybug 이상
- JDK 17
- Android SDK 35
- Kotlin 2.0+

### 빌드 및 실행

```bash
# 프로젝트 클론
git clone https://github.com/anam145/v2-anamwallet.git

# Android Studio에서 열기
# File > Open > v2-anamwallet 선택

# 빌드 및 실행
./gradlew assembleDebug
```

## 🔧 기술 스택

### 핵심 기술

- **Kotlin**: 100% Kotlin 기반
- **Jetpack Compose**: 선언형 UI
- **Coroutines & Flow**: 비동기 프로그래밍
- **Hilt**: 의존성 주입

### 아키텍처 컴포넌트

- **Navigation Compose**: 타입 안전 네비게이션
- **ViewModel + MVI**: 상태 관리
- **DataStore**: 데이터 영속성
- **AIDL**: 프로세스 간 통신

### UI/UX

- **Material Design 3**: 최신 디자인 시스템
- **Dark Mode**: 다크 모드 완벽 지원
- **다국어**: 한국어/영어 실시간 전환

## 📦 지원 기능

### 현재 지원

- ✅ 이더리움 블록체인
- ✅ 메타마스크 호환
- ✅ 정부24 통합
- ✅ 다크 모드
- ✅ 다국어 (한국어/영어)

### 개발 중

- 🚧 비트코인 지원
- 🚧 솔라나 지원
- 🚧 DID (분산 신원)

## 🤝 기여하기

기여를 환영합니다! 다음 가이드라인을 따라주세요:

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

자세한 내용은 [CODE_CONVENTIONS.md](CODE_CONVENTIONS.md)를 참고하세요.

## 📄 라이선스

TBD - 라이선스 정보는 추후 결정됩니다.

## 👥 팀

- **Project Lead**: TBD
- **Android Developer**: TBD
- **Blockchain Engineer**: TBD
- **UI/UX Designer**: TBD

## 📞 문의

- **Email**: contact@anam145.com
- **Issue Tracker**: [GitHub Issues](https://github.com/anam145/v2-anamwallet/issues)

---

<p align="center">
Made with ❤️ by ANAM Team
</p>
