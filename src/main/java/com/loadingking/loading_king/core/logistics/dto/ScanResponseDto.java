package com.loadingking.loading_king.core.logistics.dto;

public class ScanResponseDto {
    private final String barcode;
    private final String sectorName;
    private final boolean isAssigned;

    public ScanResponseDto(String barcode, String sectorName, boolean isAssigned) {
        this.barcode = barcode;
        this.sectorName = sectorName;
        this.isAssigned = isAssigned;
    }



    public String getBarcode() {
        return barcode;
    }

    public String getSectorName() {
        return sectorName;
    }

    public boolean isAssigned() {
        return isAssigned;
    }
}
