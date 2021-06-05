package com.filmbooking.booking_service.services;

import com.filmbooking.booking_service.errors_handling.QrSendingFailure;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

public class QrSender {

    public String sendCodeForQR(String code, String auth, String emailTo) throws QrSendingFailure {
        WebClient apiClient = WebClient.create(
            System.getenv("QR_MAIL_URL_BASE")
        );
        String bodyContent = String.format(
            "{\"mailFrom\":\"%s\"," +
            "\"mailTo\":\"%s\"," +
            "\"embeddedlink\":\"%s?code=%s\"}",
            System.getenv("EMAIL_DEFAULT"), emailTo,
            System.getenv("MANAGEMENT_SITE_URL"), code
        );

        // ############## DEBUG ############## //
        System.out.println("Body content: " + bodyContent);
        // ############## DEBUG ############## //

        String res = apiClient.post()
            .uri(System.getenv("QR_MAIL_URL_PATH"))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", auth)
            .body(BodyInserters.fromValue(bodyContent))
            .retrieve()
            .bodyToMono(String.class)
            .doOnError(throwable ->
                System.out.println("Failed for some reason: " + throwable))
            .onErrorReturn(new String(""))
            .block();

        // ############## DEBUG ############## //
        System.out.println("QR sending result: " + res);
        // ############## DEBUG ############## //

        if (res.equals("")) {
            throw new QrSendingFailure(
                "Failed to send QR code to mailing service"
            );
        }
        else {
            return res;
        }
    }
}
