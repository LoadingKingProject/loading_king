package com.loadingking.loading_king.global.init;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeoJsonLoader implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. 이미 데이터가 있는지 확인 (있으면 실행 안 함)
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM village", Integer.class);
        if (count != null && count > 0) {
            log.info("✅ 행정동 데이터가 이미 존재합니다. (초기화 생략)");
            return;
        }

        log.info("🚀 행정동 GeoJSON 데이터 적재를 시작합니다...");

        // 2. 파일 읽기 (src/main/resources 폴더 안의 파일)
        ClassPathResource resource = new ClassPathResource("HangJeongDong_ver20250401.geojson");
        if (!resource.exists()) {
            log.error("❌ 파일을 찾을 수 없습니다! 위치를 확인하세요: src/main/resources/HangJeongDong_ver20250401.geojson");
            return;
        }

        try (InputStream inputStream = resource.getInputStream()) {
            JsonNode root = objectMapper.readTree(inputStream);
            JsonNode features = root.path("features");

            Set<String> processedCities = new HashSet<>();
            Set<String> processedDistricts = new HashSet<>();
            int total = 0;

            for (JsonNode feature : features) {
                JsonNode props = feature.path("properties");
                JsonNode geometry = feature.path("geometry");

                // 데이터 추출
                String sidoName = props.path("sidonm").asText(); // 서울특별시
                String sidoCode = props.path("sido").asText();   // 11
                String sggName = props.path("sggnm").asText();   // 종로구
                String sggCode = props.path("sgg").asText();     // 11110
                String admNameFull = props.path("adm_nm").asText(); // 전체 주소
                String admCode = props.path("adm_cd").asText();     // 행정동코드

                // "동" 이름만 깔끔하게 추출 (예: "사직동")
                String villageName = admNameFull.contains(" ")
                        ? admNameFull.substring(admNameFull.lastIndexOf(" ") + 1)
                        : admNameFull;

                // A. 시/도 저장
                if (!processedCities.contains(sidoCode)) {
                    jdbcTemplate.update("INSERT IGNORE INTO city (name, code) VALUES (?, ?)", sidoName, sidoCode);
                    processedCities.add(sidoCode);
                }

                // B. 시/군/구 저장
                if (!processedDistricts.contains(sggCode)) {
                    // city_id를 서브쿼리로 찾아서 넣기
                    String sql = "INSERT IGNORE INTO district (name, code, city_id) " +
                            "SELECT ?, ?, id FROM city WHERE code = ?";
                    jdbcTemplate.update(sql, sggName, sggCode, sidoCode);
                    processedDistricts.add(sggCode);
                }

                // C. 읍/면/동 저장 (좌표 변환 포함)
                // Java에서 WKT 문자열을 만들어서 MySQL 함수(ST_GeomFromText)로 넣습니다.
                String wkt = toWKT(geometry);

                String sql = "INSERT INTO village (name, code, district_id, field) " +
                        "SELECT ?, ?, id, ST_GeomFromText(?, 4326) FROM district WHERE code = ?";

                jdbcTemplate.update(sql, villageName, admCode, wkt, sggCode);

                total++;
                if (total % 500 == 0) log.info("... {}개 처리 중", total);
            }
            log.info("🎉 데이터 적재 완료! 총 {}개의 행정동이 저장되었습니다.", total);
        }
    }

    // [Helper] JSON 좌표 -> WKT(String) 변환기
    private String toWKT(JsonNode geometry) {
        String type = geometry.path("type").asText();
        StringBuilder sb = new StringBuilder(type.toUpperCase()).append("(");
        JsonNode coordinates = geometry.path("coordinates");

        if ("MultiPolygon".equalsIgnoreCase(type)) {
            for (int i = 0; i < coordinates.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append("(");
                processPolygon(coordinates.get(i), sb);
                sb.append(")");
            }
        } else if ("Polygon".equalsIgnoreCase(type)) {
            sb.append("(");
            processPolygon(coordinates, sb);
            sb.append(")");
        }
        sb.append(")");
        return sb.toString();
    }

    private void processPolygon(JsonNode poly, StringBuilder sb) {
        for (int i = 0; i < poly.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("(");
            JsonNode ring = poly.get(i);
            for (int j = 0; j < ring.size(); j++) {
                if (j > 0) sb.append(", ");
                JsonNode point = ring.get(j);

                // 👇 [수정] MySQL 4326 호환을 위해 순서를 (위도 Lat, 경도 Lng)로 변경
                double lng = point.get(0).asDouble();
                double lat = point.get(1).asDouble();

                // GeoJSON은 [Lng, Lat]이지만, MySQL WKT(4326)는 "Lat Lng" 순서를 원함
                sb.append(lat).append(" ").append(lng);
            }
            sb.append(")");
        }
    }
}