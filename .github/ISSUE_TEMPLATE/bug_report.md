---
name: 🐛 Bug Report (버그 제보)
about: 버그를 발견하셨나요? 문제를 해결하기 위해 도와주세요!
title: 'fix: '
labels: 'bug'
assignees: ''
---

## 🔍 Bug Description
<!-- 어떤 버그인지 간략하게 설명해주세요. -->


## 🔄 Steps to Reproduce
<!-- 버그를 재현하기 위한 순서를 적어주세요. -->
1. 
2. 
3. 


## 💻 Environment
<!-- 어떤 환경에서 발생했나요? (OS, Browser, App Version 등) -->
- OS: 
- Browser/Version: 


## 📸 Screenshots
<!-- 버그 발생 화면이나 로그 스크린샷이 있다면 첨부해주세요. -->


## 🩹 Expected Behavior
<!-- 원래는 어떻게 동작해야 했나요? -->

---


# 예시
아래는 bug_report.md 템플릿을 사용하여 작성한 실제 버그 리포트 예시입니다.

제목: fix: app crash when scanning QR code continuously

## 🔍 Bug Description
<!-- 어떤 버그인지 간략하게 설명해주세요. -->
기사님이 '상차 모드'에서 송장을 10개 이상 연속으로 빠르게 스캔할 경우, 
앱 화면이 멈추다가 강제 종료(Crash)되는 현상이 발생합니다.


## 🔄 Steps to Reproduce
<!-- 버그를 재현하기 위한 순서를 적어주세요. -->
1. '적재왕' 앱 실행 후 로그인
2. [배송 시작] > [상차 하기] 메뉴 진입
3. 카메라 화면이 켜진 상태에서 송장 바코드를 1초 간격으로 10회 이상 연속 스캔
4. 약 11~12번째 스캔 시도 시 화면이 프리징되고, 3초 뒤 앱이 꺼짐


## 💻 Environment
<!-- 어떤 환경에서 발생했나요? (OS, Browser, App Version 등) -->
- OS: Android 13 (Galaxy S23)
- App Version: v1.2.0 (Develop Build)
- Network: 5G 환경


## 📸 Screenshots
<!-- 버그 발생 화면이나 로그 스크린샷이 있다면 첨부해주세요. -->
![Crash_Log_Screenshot](https://user-images.githubusercontent.com/...)
(로그캣 에러 메시지: `java.lang.OutOfMemoryError: ...`)


## 🩹 Expected Behavior
<!-- 원래는 어떻게 동작해야 했나요? -->
연속으로 스캔하더라도 메모리 누수 없이 정상적으로 물품이 리스트에 추가되어야 하며, 앱이 종료되지 않아야 합니다.
