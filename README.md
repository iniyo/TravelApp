
# Travel App
이 어플리케이션은 여행 관광지 및 숙박 업소 조사와 플래너를 위한 어플리케이션입니다.

현재 개발 내용 및 load는 다음과 같습니다.
1. 숙박업소 및 검색기반 관련성이 높은 순 대로 관광지 정보 제공 - 진행 중
2. google 지도 제공으로 위치 기반 숙소 정보 제공
3. 기타 회원가입 및 로그인
4. ChatGpt Api를 통해 실시간으로 gpt와 대화하며 실시간 Planner 작성 및 여행 경비 계산

확장 예정 기능 - 미정
1.


UI Design은 UiLover Android YouTube 채널 Travel app을 Base로
SkyScanner, Trip.com 어플리케이션 UI를 참고하여 만들어 졌습니다.

## Guide


## Design

### Required
- IDE : Android Studio 최신 버전 ([Download](https://developer.android.com/studio))
- JDK : Java 17을 실행할 수 있는 JDK

- Kotlin Language : 2.0.0


### Language
- Kotlin

### Libraries
- AndroidX
  - Activity
  - AppCompat
  - Core
  - Lifecycle
  - Navigation
  - ViewPager2
  - ConstraintLayout

- Kotlin Libraries
- Compose
  - Material3
  - Navigation

- Dagger & Hilt
- Square (Retrofit)

### Layer

#### UI Layer

### Module


**Module Graph 생성 방법**

```
1. 그래프를 시각화하는 오픈소스 설치
- brew install graphviz (예시 Homebrew)

2. 그래프 생성 Gradle Task 실행
./gradlew projectDependencyGraph
```


참고 사이트 정리 :

> DroidKnights
- Application & YouTube ([Application-Github](https://github.com/droidknights/DroidKnightsApp), [YouTube](https://www.youtube.com/@DroidKnights))
> UI/UX Design
- UiLover Android(YouTube): ([UiLover Android(YouTube)](https://www.youtube.com/watch?v=KPIGmyp8Bt0))
- 