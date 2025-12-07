## 디렉토리 구조/설명

```
AndroidProject/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   ├── com.example.myfirstkotlinapp/
│   │   │   │   │   ├── SplashActivity.kt # 앱 실행시 초기화면
│   │   │   │   │   ├── LoginActivity.kt # 로그인 화면
│   │   │   │   │   ├── SignupActivity.kt # 회원가입 화면
│   │   │   │   │   ├── SignupScreen.kt # SignupActivity(회원가입 화면)에서 실행할 composable 함수
│   │   │   │   │   ├── GoalSelectionActivity.kt # 회원가입 이후 운동 목표 선택 화면
│   │   │   │   │   ├── SurveyActivity.kt # 사용자 신체 정보 입력 화면
│   │   │   │   │   ├── GoalSettingActivity.kt # 목표 신체 정보 입력 화면
│   │   │   │   │   ├── WelcomeActivity.kt # 로그인 이후 초기 화면
│   │   │   │   │   ├── MainActivity.kt # 초기 화면 이후 메인 composable 함수(workoutscreen) 실행
│   │   │   │   │   ├── ui/
│   │   │   │   │   │   ├── screen/
│   │   │   │   │   │   │   ├── WelcomeScreen.kt # WelcomeActivity에서 실행할 composable 함수
│   │   │   │   │   │   │   ├── WorkoutScreen.kt # flag 조건에 따라 하위 composable을 실행
│   │   │   │   │   │   │   ├── HomeScreen.kt # 홈 화면. 운동 플랜을 생성하거나 확인, 운동 시작
│   │   │   │   │   │   │   ├── CreateRoutineActivity.kt # 운동 플랜 생성 화면. 일일 목표 입력
│   │   │   │   │   │   │   ├── MyPageActivity.kt # 마이페이지 화면
│   │   │   │   │   │   │   ├── PoseCameraScreen.kt # 운동 화면. 카메라 실행, poselandmaker로 신체 좌표 추출
│   │   │   │   │   │   │   ├── imageProxyToBitmap.kt # imageproxy를 bitmap으로 변환하는 함수
│   │   │   │   │   │   │   ├── IntermediateScreen.kt # 휴식 화면. 남은 시간과 다음 운동 표시
│   │   │   │   │   │   │   └── WorkoutCompleteScreen.kt # 운동 종료. 총 운동/휴식 시간 표시
│   │   │   │   │   │   ├── mypage/
│   │   │   │   │   │   │   └── BodyCompositionChartView.kt # 마이페이지에서 차트를 그리기 위한 클래스
│   │   │   │   │   │   ├── model/
│   │   │   │   │   │   │   ├── UserDto.kt # 사용자 정보 데이터 객체
│   │   │   │   │   │   │   ├── SignupRequestDto.kt # 회원가입 정보 데이터 객체
│   │   │   │   │   │   │   ├── ExerciseRecordDto.kt # DB의 운동 목록 레코드를 위한 데이터 객체
│   │   │   │   │   │   │   ├── ExercisePlan.kt # 운동 목록에서 플랜으로 변경하기 위한 데이터 객체
│   │   │   │   │   │   │   ├── ExerciseRecordUpdateDto.kt # 운동 레코드의 시간, 완료 여부 패치를 위한 데이터 객체
│   │   │   │   │   │   │   └── BodyCompositionPointDto.kt # 마이페이지 차트를 위한 체성분 데이터 객체
│   │   │   │   │   ├── pose/
│   │   │   │   │   │   └── PoseLandmarkerHelper.kt # poselandmaker 사용을 위한 헬퍼
│   │   │   │   │   ├── session/
│   │   │   │   │   │   └── WorkoutSessionManager.kt # 운동 플랜을 세션 매니저로 관리, 운동/휴식 시간 계산
│   │   │   │   │   ├── network/
│   │   │   │   │   │   ├── RetrofitClient.kt # 백엔드 rest api와 통신을 위한 retrofit 인스턴스 생성
│   │   │   │   │   │   ├── AuthInterceptor.kt # 요청에 토큰을 포함시키기 위한 헤더 추가용 클래스
│   │   │   │   │   │   ├── AuthApi.kt # 로그인, 회원가입 요청을 담당하는 retrofit api 인터페이스
│   │   │   │   │   │   └── ExerciseApi.kt # 범용 요청을 담당하는 retrofit api 인터페이스
│   │   │   │   │   ├── exercise/
│   │   │   │   │   │   └── ExerciseLogic.kt # 운동별 카운팅 로직
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   └── *.xml # 액티비티/화면 구성을 위한 레이아웃 xml
│   │   │   │   ├── drawable/
│   │   │   │   │   └── *.xml # 아이콘, 버튼 배경 등 그래픽 리소스 정의
│   │   │   ├── assets/
│   │   │   │   └── pose_landmaker_lite.task # poselandmaker 모델(lite버전)
```
