package com.pharmacy.sales_service.client;

import com.pharmacy.sales_service.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class InventoryClient {

    private final RestTemplate rest = new RestTemplate();

    @Value("${inventory.base-url}")
    private String baseUrl;

    public InventoryReserveResponse reserve(String bearerToken, InventoryReserveRequest req){
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(bearerToken);
        h.setContentType(MediaType.APPLICATION_JSON);
        return rest.exchange(baseUrl + "/inventory/reserve",
                HttpMethod.POST,
                new HttpEntity<>(req, h),
                InventoryReserveResponse.class
        ).getBody();
    }

    public void commit(String bearerToken, String refType, String refId){
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(bearerToken);
        h.setContentType(MediaType.APPLICATION_JSON);
        rest.exchange(baseUrl + "/inventory/commit",
                HttpMethod.POST,
                new HttpEntity<>(new RefRequest(refType, refId), h),
                Void.class);
    }

    public void release(String bearerToken, String refType, String refId){
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(bearerToken);
        h.setContentType(MediaType.APPLICATION_JSON);
        rest.exchange(baseUrl + "/inventory/release",
                HttpMethod.POST,
                new HttpEntity<>(new RefRequest(refType, refId), h),
                Void.class);
    }
}
