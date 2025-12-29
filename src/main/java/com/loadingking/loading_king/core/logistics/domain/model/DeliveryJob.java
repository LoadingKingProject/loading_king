package com.loadingking.loading_king.core.logistics.domain.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name="delivery_jobs")
public class DeliveryJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="job_id")
    private Long id;

    /*
     * TODO: 해당 부분 추가 확인 필요
     * UC-02의 3번 - 시스템은 변환된 좌표가 기사의 설정된 섹터 중 어디에 포함되는지 공간 연산으로 확인
     * 실패시 "구역 외 지역(미배정)" 경고를 띄워 기사가 별도로 분류하게 한다
     * 부분에서 미배정된 부분을 기사가 선택하게 하려면 기사 기반 미배정된 섹터를 조회하게 해야 함
     */
    @Column(name="driver_id", nullable = false)
    private Long driverId;

    @ElementCollection
    @CollectionTable(name = "job_sectors", joinColumns = @JoinColumn(name = "job_id"))
    private List<Long> sectorId;

    @Column(name="work_date", nullable = false)
    private LocalDate workDate;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    protected DeliveryJob() {}

    public static DeliveryJob create(Long driverId) {
        DeliveryJob job = new DeliveryJob();
        job.driverId = driverId;
        job.workDate = LocalDate.now();
        job.status = JobStatus.PENDING;
        return job;
    }

    public Long getId() {
        return id;
    }

    public Long getDriverId() {
        return driverId;
    }

    public List<Long> getSectorId() {
        return sectorId;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public JobStatus getStatus() {
        return status;
    }


}
