
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
<<<<<<< .merge_file_8un9qT
- Splash Screen(Velog): ([pachuho.log](https://velog.io/@pachuho/Android-%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-12-Splash-Screen-%EC%A0%81%EC%9A%A9%ED%95%98%EA%B8%B0))
- Rolling Textview(Github): ([YvesCheung](https://github.com/YvesCheung/RollingText))
- social login button design: ([stead](https://butsteadily.tistory.com/16))

- material design 모음: ([](https://medium.com/@mmbialas/30-new-android-libraries-and-projects-released-in-summer-2017-which-should-catch-your-attention-d3702bd9bdc6))

> Manifest 설정 관련
- Android Development[permission 종류](https://developer.android.com/reference/android/Manifest.permission)
- // 권한 종류 중 Protection level: dangerous 가 되어있는 경우 사용자에게 권한 허용을 받아야 함. 


> 각종 오류 및 이슈 해결
- search fragment로 전환 시 duration이 적용되지 않는 문제 ([StackOverFlow](https://stackoverflow.com/questions/69396539/using-jetpack-navigation-component-transition-animation-is-not-working)(issue해결)[https://github.com/iniyo/TravelApp/issues/3])
- 


=======
- Splash Screen: ([pachuho.log](https://velog.io/@pachuho/Android-%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-12-Splash-Screen-%EC%A0%81%EC%9A%A9%ED%95%98%EA%B8%B0))

> 각종 이슈 대응:
- navControll 사용 시 action(애니메이션) 적용 안됨: ([stackoverflow](https://stackoverflow.com/questions/69396539/using-jetpack-navigation-component-transition-animation-is-not-working))
>>>>>>> .merge_file_PW3w05
