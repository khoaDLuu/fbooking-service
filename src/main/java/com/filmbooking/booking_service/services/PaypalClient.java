package com.filmbooking.booking_service.services;

import java.util.concurrent.TimeUnit;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class PaypalClient {

    /**
     * Set up the PayPal Java SDK environment with PayPal access credentials.
     * This sample uses SandboxEnvironment. In production, use LiveEnvironment.
     */
    private String clientId;
    private String secretId;

    /**
     * Method to get client object
     *
     * @return PayPalHttpClient client
     */
    public PayPalHttpClient client() {

        PayPalEnvironment environment = new PayPalEnvironment.Sandbox(clientId, secretId);

        /**
         * PayPal HTTP client instance with environment that has access
         * credentials context. Use to invoke PayPal APIs.
         */
        PayPalHttpClient client = new PayPalHttpClient(environment);

        client.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(900));

        return client;
    }

	@Value("${paypal.client.id}")
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Value("${paypal.client.secret}")
	public void setSecretId(String secretId) {
		this.secretId = secretId;
	}
}
