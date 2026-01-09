# 🚚 로딩킹(Loading King) 개발 작업 지시서

## 1. 프로젝트 컨텍스트 (Project Context)
- **Tech Stack:** Spring Boot 3.2, Java 17, JPA, MySQL
- **Goal:** 배송 기사가 물품을 스캔하면, 주소를 분석해 **기사가 설정한 섹터(Sector)**로 자동 분류하고 적재 순서를 알려주는 시스템.
- **Key Logic:**
    - **공간 연산:** `SectorService.findContainingSector(point)`를 통해 주소가 어느 섹터에 포함되는지 확인.
    - **배송 업무(DeliveryJob):** 기사의 하루 배송 업무를 의미하며, 여러 개의 섹터(`List<Long> sectorId`)를 포함함.
    - **미배정 처리:** 섹터를 찾지 못할 경우 '미배정' 상태로 두고 기사가 수동 선택하게 유도해야 함 (추후 구현).

---

## 2. [Step 1] Item 도메인 정의 (기초 공사)
**목표:** 물품(Item) 정보를 담을 엔티티 구현

- **작업 파일:** `src/main/java/.../core/logistics/domain/Item.java`
- **요구 사항:**
    1. `@Entity`, `@Table(name="items")` 적용.
    2. **필수 필드:**
        - `Long id` (PK)
        - `String barcode` (바코드 번호, Unique)
        - `String address` (배송지 주소)
        - `String recipientName` (수령인)
        - `Point location` (좌표 - `GeometryUtils` 활용 예정)
        - `Long sectorId` (배정된 섹터 ID, Nullable - 미배정일 수 있음)
        - `Long deliveryJobId` (소속된 업무 ID)
    3. **생성자:** 정적 팩토리 메서드 `create(...)` 구현 권장.

---

## 3. [Step 2] LogisticsService 구현 (핵심 로직)
**목표:** 스캔 요청을 받아 섹터를 판별하고 데이터를 저장하는 로직

- **작업 파일:** `src/main/java/.../core/logistics/application/LogisticsService.java`
- **참고 파일:** `DeliveryJob.java`, `SectorService.java`, `GeometryUtils.java`
- **요구 사항:**
    1. **`registerItem(String barcode, String address, User user)` 메서드 구현:**
        - 1) 주소 -> 좌표 변환 (임시로 `GeometryUtils`에 더미 메서드 사용하거나, 카카오 API 연동 가정).
        - 2) **섹터 판별:** `sectorService.findContainingSector(user.getSectors(), location)` 호출.
        - 3) **DeliveryJob 조회/생성:**
            - 오늘 날짜(`LocalDate.now()`)의 `Active` 상태인 Job이 있으면 가져오고, 없으면 새로 생성 (`save`).
        - 4) **Item 저장:** 판별된 `sectorId`와 `jobId`를 넣어 `Item` 엔티티 저장.
        - 5) **결과 반환:** `ScanResponseDto` (성공 여부, 섹터 이름, 미배정 여부).
    2. **예외 처리:**
        - 이미 등록된 바코드면 예외 발생.
        - 섹터를 못 찾으면 `sectorId = null`로 저장하고 응답에 "미배정" 플래그 설정.

---

## 4. [Step 3] ScanController 구현 (API 연결)
**목표:** 앱과 통신할 엔드포인트 생성

- **작업 파일:** `src/main/java/.../core/logistics/api/ScanController.java`
- **요구 사항:**
    1. `POST /api/logistics/scan`
    2. **Request:** `ScanRequest` (barcode, address)
    3. **Response:** `ScanResponseDto`
        - 필드: `String barcode`, `String sectorName`, `boolean isAssigned`
    4. **Security:** `@AuthenticationPrincipal CustomUserDetail userDetail`을 받아 서비스에 User 정보 전달.