package com.oasis.sdk.activity;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.oasis.sdk.base.entity.PayConfigInfo;
import com.oasis.sdk.base.entity.PayInfoDetail;
import com.oasis.sdk.base.utils.BaseUtils;
import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalItem;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalPaymentDetails;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

/**
 * Basic sample using the SDK to make a payment or consent to future payments.
 * 
 * For sample mobile backend interactions, see
 * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
 */
public class OasisSdkPayPaypalActivity extends OasisSdkBaseActivity {
    private final String TAG = "Paypal payment";
    /**
     * - Set to PayPalConfiguration.ENVIRONMENT_PRODUCTION to move real money.
     * 
     * - Set to PayPalConfiguration.ENVIRONMENT_SANDBOX to use your test credentials
     * from https://developer.paypal.com
     * 
     * - Set to PayPalConfiguration.ENVIRONMENT_NO_NETWORK to kick the tires
     * without communicating to PayPal's servers.
     */
    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_PRODUCTION;

    // note that these credentials will differ between live & sandbox environments.
//    private static final String CONFIG_CLIENT_ID = "credential from developer.paypal.com";
//    private static final String CONFIG_CLIENT_ID = "Afsno2RzotiQoYlV5QCAnTER40KJhFnAN_sFYjFIbX0YjtrzytomZR1ik8RiRb1r-NpuM1e7R4jZI8Y-";// sandbox
//    private static final String CONFIG_CLIENT_ID = "AROOs4AGUva375OMCV8ljb3oVFrK_0aL5iEK4vFrI0IjSV-H4lnRX8E8Bja3qbOdIMKVJXlTmCupJoT_";// live

    private static final int REQUEST_CODE_PAYMENT = 1;
//    private final int REQUEST_CODE_FUTURE_PAYMENT = 2;
//    private final int REQUEST_CODE_PROFILE_SHARING = 3;

    private PayPalConfiguration config;
    PayInfoDetail payInfo;
	PayConfigInfo payConfig;
	
	MyHandler handler ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_pay_paypal"));

        setWaitScreen(true);
        Bundle b = getIntent().getExtras();
        if(b != null){
	    	payInfo = (PayInfoDetail) b.get("payInfo");
			payConfig = (PayConfigInfo) b.get("payConfig");
        }

        handler = new MyHandler(this);
        
        if(payInfo == null || TextUtils.isEmpty(payInfo.orderId)
        		|| payConfig == null
        		|| (payConfig!=null && TextUtils.isEmpty(payConfig.project_key))
        		){
        	
        	handler.sendEmptyMessageDelayed(HANDLER_FINISH, 2000);
        	return;
        }
        
        config = new PayPalConfiguration()
        .environment(CONFIG_ENVIRONMENT)
        .clientId(payConfig.project_key)//CONFIG_CLIENT_ID
        // The following are only used in PayPalFuturePaymentActivity.
//        .merchantName("Example Merchant")
//        .merchantPrivacyPolicyUri(Uri.parse("https://www.baidu.com"))
//        .merchantUserAgreementUri(Uri.parse("https://www.baidu.com/s?ie=utf-8&f=3&rsv_bp=1&rsv_idx=1&tn=baidu&wd=android%20webview%20%E6%89%A7%E8%A1%8Cjs%E4%BB%A3%E7%A0%81&oq=android%20routines%3ASSL2%26lt%3B_G%26gt%3BT_S%26gt%3BRV%26gt%3BR_H%26gt%3BLLO%3Asslv%26lt%3B%20alert%20handshake%20failure&rsv_pq=d951fcbf00037787&rsv_t=e19cIFbq%2FEhGCg5iatTuRrLQpu%2Bv4Nl%2B2kRCbRUyIiMY1PWN%2BkWXVeJdXFw&rsv_enter=1&inputT=17728&rsv_sug3=25&rsv_sug1=4&rsv_sug2=0&prefixsug=android%20WebView%20&rsp=1&rsv_sug7=100&rsv_sug4=18601"))
        .acceptCreditCards(false);
        
        Intent intent = new Intent(this.getApplicationContext(), PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);
        
        pay();
    }


    @Override
    public void onDestroy() {
        // Stop service when done
        stopService(new Intent(this.getApplicationContext(), PayPalService.class));
        super.onDestroy();
    }
    
    private void pay(){
		// --- include an item list, payment amount details
		PayPalItem[] items = {
			// new PayPalItem("sample item #1", 2, new BigDecimal("87.50"), "USD",
			// "sku-12345678"),
			// new PayPalItem("free sample item #2", 1, new BigDecimal("0.00"),
			// "USD", "sku-zero-price"),
			new PayPalItem("sample item #3 with a longer name", 1, new BigDecimal(
					payInfo.amount), payInfo.currency, payInfo.orderId) 
			};
		
		BigDecimal subtotal = PayPalItem.getItemTotal(items);
		BigDecimal shipping = new BigDecimal("0");// 运费 默认为0
		BigDecimal tax = new BigDecimal("0"); // 税费 默认为0
		BigDecimal amount = subtotal.add(shipping).add(tax);

		PayPalPaymentDetails paymentDetails = new PayPalPaymentDetails(
				shipping, subtotal, tax);
		
		PayPalPayment payment = new PayPalPayment(amount, 
				payInfo.currency,
				payInfo.game_coins_show/** Sample item.不能为空，暂用游戏币代替 **/, 
				PayPalPayment.PAYMENT_INTENT_SALE);
		payment.items(items).paymentDetails(paymentDetails);

		Intent intent = new Intent(this.getApplicationContext(), PaymentActivity.class);
		intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
		startActivityForResult(intent, REQUEST_CODE_PAYMENT);
    }
    public static class MyHandler extends Handler {

		// WeakReference to the outer class's instance.
		private WeakReference<OasisSdkPayPaypalActivity> mOuter;

		public MyHandler(OasisSdkPayPaypalActivity activity) {
			mOuter = new WeakReference<OasisSdkPayPaypalActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {

			OasisSdkPayPaypalActivity outer = mOuter.get();
			if (outer != null) {
				switch (msg.what) {
				case HANDLER_FINISH:
					outer.setWaitScreen(false);
					outer.finish();
					break;
				default:
					
					break;
				}
			}
		}
	}
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
            	handler.sendEmptyMessageDelayed(HANDLER_FINISH, 500);
            	// 支付完成，等待服务端发钻
                Toast.makeText(
                        getApplicationContext(),
                        getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_error_success2")), Toast.LENGTH_LONG)
                        .show();
                PaymentConfirmation confirm =
                        data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        Log.i(TAG, confirm.toJSONObject().toString(4));
                        Log.i(TAG, confirm.getPayment().toJSONObject().toString(4));
                        /**
                         *  TODO: send 'confirm' (and possibly confirm.getPayment() to your server for verification
                         * or consent completion.
                         * See https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
                         * for more details.
                         *
                         * For sample mobile backend interactions, see
                         * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
                         */
                        

                    } catch (JSONException e) {
                        Log.e(TAG, "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
            	handler.sendEmptyMessage(HANDLER_FINISH);
                Log.i(TAG, "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            	handler.sendEmptyMessageDelayed(HANDLER_FINISH, 500);
                Log.i(
                        TAG,
                        "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        } /*else if (requestCode == REQUEST_CODE_FUTURE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PayPalAuthorization auth =
                        data.getParcelableExtra(PayPalFuturePaymentActivity.EXTRA_RESULT_AUTHORIZATION);
                if (auth != null) {
                    try {
                        Log.i("FuturePaymentExample", auth.toJSONObject().toString(4));

                        String authorization_code = auth.getAuthorizationCode();
                        Log.i("FuturePaymentExample", authorization_code);

                        sendAuthorizationToServer(auth);
                        Toast.makeText(
                                getApplicationContext(),
                                "Future Payment code received from PayPal", Toast.LENGTH_LONG)
                                .show();

                    } catch (JSONException e) {
                        Log.e("FuturePaymentExample", "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("FuturePaymentExample", "The user canceled.");
            } else if (resultCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(
                        "FuturePaymentExample",
                        "Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.");
            } 
        } else if (requestCode == REQUEST_CODE_PROFILE_SHARING) {
            if (resultCode == Activity.RESULT_OK) {
                PayPalAuthorization auth =
                        data.getParcelableExtra(PayPalProfileSharingActivity.EXTRA_RESULT_AUTHORIZATION);
                if (auth != null) {
                    try {
                        Log.i("ProfileSharingExample", auth.toJSONObject().toString(4));

                        String authorization_code = auth.getAuthorizationCode();
                        Log.i("ProfileSharingExample", authorization_code);

                        sendAuthorizationToServer(auth);
                        Toast.makeText(
                                getApplicationContext(),
                                "Profile Sharing code received from PayPal", Toast.LENGTH_LONG)
                                .show();

                    } catch (JSONException e) {
                        Log.e("ProfileSharingExample", "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("ProfileSharingExample", "The user canceled.");
            } else if (resultCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(
                        "ProfileSharingExample",
                        "Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.");
            }
        }*/
    }
    
    
    
    
    
    
    
    
    
    
    //---------------------------以下是实例代码--------------------------

    public void onBuyPressed(View pressed) {
        /* 
         * PAYMENT_INTENT_SALE will cause the payment to complete immediately.
         * Change PAYMENT_INTENT_SALE to 
         *   - PAYMENT_INTENT_AUTHORIZE to only authorize payment and capture funds later.
         *   - PAYMENT_INTENT_ORDER to create a payment for authorization and capture
         *     later via calls from your server.
         * 
         * Also, to include additional payment details and an item list, see getStuffToBuy() below.
         */
        PayPalPayment thingToBuy = getThingToBuy(PayPalPayment.PAYMENT_INTENT_SALE);

        /*
         * See getStuffToBuy(..) for examples of some available payment options.
         */

        Intent intent = new Intent(OasisSdkPayPaypalActivity.this, PaymentActivity.class);

        // send the same configuration for restart resiliency
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);

        startActivityForResult(intent, REQUEST_CODE_PAYMENT);
    }
    
    private PayPalPayment getThingToBuy(String paymentIntent) {
//        return new PayPalPayment(new BigDecimal("1.75"), "USD", "sample item",
//                paymentIntent);
        return new PayPalPayment(new BigDecimal("1.55"), "EUR", "Item Name", "456789321");
    }
    
//    /* 
//     * This method shows use of optional payment details and item list.
//     */
//    private PayPalPayment getStuffToBuy(String paymentIntent) {
//        //--- include an item list, payment amount details
//        PayPalItem[] items =
//            {
//                    new PayPalItem("sample item #1", 2, new BigDecimal("87.50"), "USD",
//                            "sku-12345678"),
//                    new PayPalItem("free sample item #2", 1, new BigDecimal("0.00"),
//                            "USD", "sku-zero-price"),
//                    new PayPalItem("sample item #3 with a longer name", 6, new BigDecimal("37.99"),
//                            "USD", "sku-33333") 
//            };
//        BigDecimal subtotal = PayPalItem.getItemTotal(items);
//        BigDecimal shipping = new BigDecimal("7.21");
//        BigDecimal tax = new BigDecimal("4.67");
//        PayPalPaymentDetails paymentDetails = new PayPalPaymentDetails(shipping, subtotal, tax);
//        BigDecimal amount = subtotal.add(shipping).add(tax);
//        PayPalPayment payment = new PayPalPayment(amount, "USD", "sample item", paymentIntent);
//        payment.items(items).paymentDetails(paymentDetails);
//
//        //--- set other optional fields like invoice_number, custom field, and soft_descriptor
//        payment.custom("This is text that will be associated with the payment that the app can use.");
//
//        return payment;
//    }
//    
//    /*
//     * Add app-provided shipping address to payment
//     */
//    private void addAppProvidedShippingAddress(PayPalPayment paypalPayment) {
//        ShippingAddress shippingAddress =
//                new ShippingAddress().recipientName("Mom Parker").line1("52 North Main St.")
//                        .city("Austin").state("TX").postalCode("78729").countryCode("US");
//        paypalPayment.providedShippingAddress(shippingAddress);
//    }
//    
//    /*
//     * Enable retrieval of shipping addresses from buyer's PayPal account
//     */
//    private void enableShippingAddressRetrieval(PayPalPayment paypalPayment, boolean enable) {
//        paypalPayment.enablePayPalShippingAddressesRetrieval(enable);
//    }
//
//    public void onFuturePaymentPressed(View pressed) {
//        Intent intent = new Intent(OasisSdkPayPaypalActivity.this, PayPalFuturePaymentActivity.class);
//
//        // send the same configuration for restart resiliency
//        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
//
//        startActivityForResult(intent, REQUEST_CODE_FUTURE_PAYMENT);
//    }
//
//    public void onProfileSharingPressed(View pressed) {
//        Intent intent = new Intent(OasisSdkPayPaypalActivity.this, PayPalProfileSharingActivity.class);
//
//        // send the same configuration for restart resiliency
//        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
//
//        intent.putExtra(PayPalProfileSharingActivity.EXTRA_REQUESTED_SCOPES, getOauthScopes());
//
//        startActivityForResult(intent, REQUEST_CODE_PROFILE_SHARING);
//    }
//
//    private PayPalOAuthScopes getOauthScopes() {
//        /* create the set of required scopes
//         * Note: see https://developer.paypal.com/docs/integration/direct/identity/attributes/ for mapping between the
//         * attributes you select for this app in the PayPal developer portal and the scopes required here.
//         */
//        Set<String> scopes = new HashSet<String>(
//                Arrays.asList(PayPalOAuthScopes.PAYPAL_SCOPE_EMAIL, PayPalOAuthScopes.PAYPAL_SCOPE_ADDRESS) );
//        return new PayPalOAuthScopes(scopes);
//    }

    private void sendAuthorizationToServer(PayPalAuthorization authorization) {

        /**
         * TODO: Send the authorization response to your server, where it can
         * exchange the authorization code for OAuth access and refresh tokens.
         * 
         * Your server must then store these tokens, so that your server code
         * can execute payments for this user in the future.
         * 
         * A more complete example that includes the required app-server to
         * PayPal-server integration is available from
         * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
         */

    }

    public void onFuturePaymentPurchasePressed(View pressed) {
        // Get the Client Metadata ID from the SDK
//        String metadataId = PayPalConfiguration.getClientMetadataId(this);
//
//        Log.i("FuturePaymentExample", "Client Metadata ID: " + metadataId);

        // TODO: Send metadataId and transaction details to your server for processing with
        // PayPal...
        Toast.makeText(getApplicationContext(), "Client Metadata Id received from SDK", Toast.LENGTH_LONG).show();
    }
}

