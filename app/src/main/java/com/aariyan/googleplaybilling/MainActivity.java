package com.aariyan.googleplaybilling;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //Member variable of Billing client:
    BillingClient billingClient;

    private TextView itemName;
    private Button priceBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //instantiate UI:
        initUI();

        //instantiate Billing Client:
        billingClient = BillingClient.newBuilder(this)
                .setListener(new PurchasesUpdatedListener() {
                    @Override
                    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
                        // To be implemented in a later section.
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                            for (Purchase purchase : list) {
                                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged()) {
                                    //Basically server URL handling
                                    handlePurchase(purchase);
                                }
                            }
                        }
                    }
                })
                .enablePendingPurchases()
                .build();

        //calling the google app billiing:
        connectToGooglePlay();
    }

    private void initUI() {
        itemName = findViewById(R.id.itemNameText);
        priceBtn = findViewById(R.id.priceBtn);
    }

    public void connectToGooglePlay() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    getProductDetails();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                connectToGooglePlay();
            }
        });
    }

    public void getProductDetails() {
        List<String> skuList = new ArrayList<>();
        skuList.add("premium_upgrade");
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                // Process the result.
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                    SkuDetails itemInfo = skuDetailsList.get(0);
                    itemName.setText(itemInfo.getTitle());
                    priceBtn.setText(itemInfo.getPrice());
                    priceBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            billingClient.launchBillingFlow(MainActivity.this,
                                    BillingFlowParams.newBuilder().build());
                        }
                    });
                }
            }
        });
    }

    //handling the purchase:
    private void handlePurchase(Purchase purchase) {
        //Handling the server URL:
        //There should have following parameter:
        //purchaseToken,purchaseTime,orderId:
    }

}