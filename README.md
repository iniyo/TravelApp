
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
- JDK : Java 18을 실행할 수 있는 JDK

- Kotlin Language : 1.9.0 


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
- Square (Retrofit, OkHTTP)

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

- Splash Screen(Velog): ([pachuho.log](https://velog.io/@pachuho/Android-%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-12-Splash-Screen-%EC%A0%81%EC%9A%A9%ED%95%98%EA%B8%B0))
- Rolling Textview(Github): ([YvesCheung](https://github.com/YvesCheung/RollingText))
- social login button design: ([stead](https://butsteadily.tistory.com/16))
- viewpager between view ([king-jungin.log](https://velog.io/@king-jungin/Android-%EC%96%91-%EC%98%86%EC%9D%B4-%EB%AF%B8%EB%A6%AC%EB%B3%B4%EC%9D%B4%EB%8A%94-ViewPager2-%EB%A7%8C%EB%93%A4%EA%B8%B0))
- material design 모음: ([for 2017](https://medium.com/@mmbialas/30-new-android-libraries-and-projects-released-in-summer-2017-which-should-catch-your-attention-d3702bd9bdc6))
- sliding layout(Github) ([AndroidSlidingUpPanel](https://github.com/hannesa2/AndroidSlidingUpPanel))

> network
- coroutine 통신(t-story): ([seokzoo](https://seokzoo.tistory.com/4))

```
구글 api 사용시 
특정 장소 선택 [google documents](https://developers.google.com/maps/documentation/places/android-sdk/supported_types?hl=ko&_gl=1*1co9b5h*_up*MQ..*_ga*MTMwODQ5OTYzNi4xNzIwNjc1ODU2*_ga_NRWSTWS78N*MTcyMDY3NTg1NS4xLjAuMTcyMDY3NTg1NS4wLjAuMA..)

```







> Manifest 설정 관련
- Android Development[permission 종류](https://developer.android.com/reference/android/Manifest.permission)
- // 권한 종류 중 Protection level: dangerous 가 되어있는 경우 사용자에게 권한 허용을 받아야 함.

> 각종 오류 및 이슈 해결
- search fragment로 전환 시 duration이 적용되지 않는 문제 ([StackOverFlow](https://stackoverflow.com/questions/69396539/using-jetpack-navigation-component-transition-animation-is-not-working)(issue해결)[https://github.com/iniyo/TravelApp/issues/3])
- ※ coroutine 에러 처리하기(t-story) ([카미유 테크](https://june0122.tistory.com/20))

> gradle 이슈
- hilt 실행시 빌드중 The following options were not recognized by any processor 관련 ([StackOverFlow](https://stackoverflow.com/questions/70550883/warning-the-following-options-were-not-recognized-by-any-processor-dagger-f))
- Kapt currently doesn't support language version 2.0+. Falling back to 1.9. : kapt가 kotlin 1.9.0 버전 이상을 지원하지 않아서 1.9.0 버전으로 낮춰서 사용
- Deprecated Gradle features were used in this build, making it incompatible with Gradle 9.0. ([기계공학하던 안드로이드 개발자](https://android-developer.tistory.com/entry/%ED%95%B4%EA%B2%B0-Deprecated-Gradle-features-were-used-in-this-build-making-it-incompatible-with-Gradle-80-%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EC%8A%A4%ED%8A%9C%EB%94%94%EC%98%A4))


=======
- Splash Screen: ([pachuho.log](https://velog.io/@pachuho/Android-%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-12-Splash-Screen-%EC%A0%81%EC%9A%A9%ED%95%98%EA%B8%B0))

> 각종 이슈 대응:
- navControll 사용 시 action(애니메이션) 적용 안됨: ([stackoverflow](https://stackoverflow.com/questions/69396539/using-jetpack-navigation-component-transition-animation-is-not-working))
- sandwich library 사용 시 jvm target 에러 -> wrapper 업그레이드 시도(안됨)

