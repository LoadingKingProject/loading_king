package com.loadingking.loading_king.core.logistics.dto;

public class ScanResponseDto {
    private final String barcode;
    private final Long sectorId;
    private final String sectorName;
    private final boolean isAssigned;
    private final Double lat;
    private final Double lng;

    public ScanResponseDto(String barcode, Long sectorId, String sectorName, boolean isAssigned, Double lat, Double lng) {
        this.barcode = barcode;
        this.sectorId = sectorId;
        this.sectorName = sectorName;
        this.isAssigned = isAssigned;
        this.lat = lat;
        this.lng = lng;
    }



    public String getBarcode() {
        return barcode;
    }

    public Long getSectorId() {
        return sectorId;
    }

    public String getSectorName() {
        return sectorName;
    }

    public boolean isAssigned() {
        return isAssigned;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }
}
