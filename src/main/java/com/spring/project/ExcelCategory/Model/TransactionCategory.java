package com.spring.project.ExcelCategory.Model;

import java.util.Arrays;

public enum TransactionCategory {
    STATIONERY("mokasa"),
    INSURANCE("insu", "insurance"),
    LOAN_PAYMENT("barb0dbvasc"),
    GOING_OUT_1("sweet", "dabba", "wine", "hotel", "bakery", "restaurant", "temptation", "cityburger", "bar", "rest", "dominos", "pizzahut", "subway", "vazbakers", "grab", "wowmo", "cafe", "cake", "merchant", "zomato", "cremeux", "Goa Woodlands", "Ashoka Classic", "seafood", "Kanekar Ventures"),
    RETAIL_SHOPPING_6("vyapar", "lochan lau parab", "bharatpe"),
    ENTERTAINMENT("tatapay", "netflix", "amazonprime", "bigtree", "tataplay", "tatasky"),
    LEISURE("playstore"),
    FEES_1("mab", "fsv", "charge", "instaalert", "chrgs", "fee"),
    TRANSPORTATION("railway"),
    TRANSPORT("transport", "logistics"),
    SCHOOL_FEES("college", "institute", "school"),
    INVESTMENTS("zerodha", "5paisa", "groww"),
    SHOPPING("zudio", "mall", "Vishal Mega Mart", "mydiy", "ghar soaps"),
    GAS("bp", "royal", "boutique", "karma", "gas", "petroleum", "fuel", "omshr"),
    GROCERIES_1("goabagayatdar", "fish", "general store", "milk", "groceries", "store"),
    UTILITY_BILLS("mobility", "recharge", "euronet", "euro", "vi-", "vodaphone", "gpayrecharge", "lienlift", "bill", "railtel", "airtel", "utility", "cred"),
    TRANSFERS_1("gpay"),
    CASH("nwd", "cwdr", "cash"),
    EMI("shriramcity", "tvscreditservices"),
    SALARY("salary"),
    BIKE("goarajeevascospar"),
    ONLINE_SHOPPING("flipkart", "smartexpr", "amazon", "xpressbees", "armor", "razorpay", "bluedart"),
    HEALTHCARE("devas", "nation", "medical", "pill", "chemist", "health"),
    RETAIL_SHOPPING_4("ekart", "waykart", "store"),
    TRANSFERS_3; // Default case

    private final String[] keywords;

    TransactionCategory(String... keywords) {
        this.keywords = keywords;
    }

    public static String categorize(String narration) {
        String lowerCaseNarration = narration.toLowerCase();
        for (TransactionCategory category : TransactionCategory.values()) {
            if (category.keywords != null && Arrays.stream(category.keywords)
                    .anyMatch(keyword -> lowerCaseNarration.contains(keyword.toLowerCase()))) {
                return category.name();
            }
        }
        return TRANSFERS_3.name();
    }
}
