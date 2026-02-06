package com.loadingking.loading_king.core.logistics.dto;

public class ScanItemViewDto {
    private final Long itemId;
    private final String barcode;
    private final Double lat;
    private final Double lng;
    private final Long sectorId;

    public ScanItemViewDto(Long itemId, String barcode, Double lat, Double lng, Long sectorId) {
        this.itemId = itemId;
        this.barcode = barcode;
        this.lat = lat;
        this.lng = lng;
        this.sectorId = sectorId;
    }

    public Long getItemId() {
        return itemId;
    }

    public String getBarcode() {
        return barcode;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public Long getSectorId() {
        return sectorId;
    }
}
