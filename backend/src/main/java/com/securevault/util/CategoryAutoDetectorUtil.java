package com.securevault.util;

import com.securevault.entity.enums.CredentialCategory;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class CategoryAutoDetectorUtil {

    public CredentialCategory detectCategory(String url) {
        if (url == null || url.trim().isEmpty()) {
            return CredentialCategory.OTHER;
        }

        String lowerUrl = url.toLowerCase(Locale.ROOT);

        // Social Media
        if (lowerUrl.contains("instagram") || lowerUrl.contains("facebook") ||
            lowerUrl.contains("telegram") || lowerUrl.contains("snapchat") ||
            lowerUrl.contains("linkedin") || lowerUrl.contains("twitter") ||
            lowerUrl.contains("x.com") || lowerUrl.contains("tiktok") ||
            lowerUrl.contains("reddit") || lowerUrl.contains("whatsapp") ||
            lowerUrl.contains("pinterest") || lowerUrl.contains("discord")) {
            return CredentialCategory.SOCIAL_MEDIA;
        }

        // Banking
        if (lowerUrl.contains("bank") || lowerUrl.contains("chase") ||
            lowerUrl.contains("paypal") || lowerUrl.contains("wellsfargo") ||
            lowerUrl.contains("barclays") || lowerUrl.contains("stripe") ||
            lowerUrl.contains("hDFC") || lowerUrl.contains("icici") ||
            lowerUrl.contains("sbi") || lowerUrl.contains("revolut") ||
            lowerUrl.contains("venmo") || lowerUrl.contains("capitalone") ||
            lowerUrl.contains("paytm") || lowerUrl.contains("fidelity")) {
            return CredentialCategory.BANKING;
        }

        // Email
        if (lowerUrl.contains("gmail") || lowerUrl.contains("mail.google") ||
            lowerUrl.contains("outlook") || lowerUrl.contains("yahoo") ||
            lowerUrl.contains("proton") || lowerUrl.contains("zoho") ||
            lowerUrl.contains("icloud") || lowerUrl.contains("fastmail")) {
            return CredentialCategory.EMAIL;
        }

        // Shopping
        if (lowerUrl.contains("amazon") || lowerUrl.contains("flipkart") ||
            lowerUrl.contains("ebay") || lowerUrl.contains("walmart") ||
            lowerUrl.contains("ali") || lowerUrl.contains("target") ||
            lowerUrl.contains("bestbuy") || lowerUrl.contains("shopify") ||
            lowerUrl.contains("etsy") || lowerUrl.contains("myntra")) {
            return CredentialCategory.SHOPPING;
        }

        // Developer
        if (lowerUrl.contains("github") || lowerUrl.contains("gitlab") ||
            lowerUrl.contains("docker") || lowerUrl.contains("aws") ||
            lowerUrl.contains("cloud.google") || lowerUrl.contains("azure") ||
            lowerUrl.contains("vercel") || lowerUrl.contains("netlify") ||
            lowerUrl.contains("bitbucket") || lowerUrl.contains("stackoverflow") ||
            lowerUrl.contains("postman") || lowerUrl.contains("npm")) {
            return CredentialCategory.DEVELOPER;
        }

        return CredentialCategory.OTHER;
    }
}
