package com.pharmacy.sales_service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class CatalogClient {

    private final RestTemplate rest = new RestTemplate();

    @Value("${catalog.base-url}")
    private String baseUrl;

    public MedicineDto getMedicineById(String bearerToken, Long id) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(bearerToken);

        ResponseEntity<MedicineDto> resp = rest.exchange(
                baseUrl + "/catalog/medicines/" + id,
                HttpMethod.GET,
                new HttpEntity<>(h),
                MedicineDto.class
        );

        return resp.getBody();
    }

    // chỉ cần đúng field id + salePrice để tính tiền
    public record MedicineDto(Long id, BigDecimal salePrice, String name, String code) {}
}
