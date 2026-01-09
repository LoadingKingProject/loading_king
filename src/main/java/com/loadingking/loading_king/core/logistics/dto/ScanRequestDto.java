package com.loadingking.loading_king.core.logistics.dto;



public class ScanRequestDto {
    private String barcode;
    private String address;

    public ScanRequestDto(String barcode, String address) {
        this.barcode = barcode;
        this.address = address;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getAddress() {
        return address;
    }
}