/**
 * This class is where most of the action happens - The Scanner activity has scanned a QR Code with a URI target
 * and passes in that result as a "uri" extra that is accessed to retrieve details about that item for display.
 * A PayPal checkout button is also displayed using Android's MPL and a simple checkout flow is demonstrated since
 * a simple checkout probably makes the most sense for a yard sale setting.
 */

package com.zaffra.yardsale;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;
import com.paypal.android.MEP.CheckoutButton;
import com.paypal.android.MEP.PayPal;
import com.paypal.android.MEP.PayPalActivity;
import com.paypal.android.MEP.PayPalInvoiceData;
import com.paypal.android.MEP.PayPalInvoiceItem;
import com.paypal.android.MEP.PayPalPayment;
import com.zaffra.yardsale.Item;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View.OnClickListener;

public class ItemDetails extends Activity implements OnClickListener{
    /** Called when the activity is first created. */

	// The PayPal server to be used - can also be ENV_SANDBOX and ENV_LIVE
	// ENV_NONE runs in demo mode. Go to http://developer.paypal.com to get
	// api credentials for a Sandbox account. Get real credentials at http://paypal.com
	private static final int server = PayPal.ENV_NONE;
	
	// The ID of your application that you received from PayPal. The Sandbox appID is below
	private static final String appID = "APP-80W284485P519543T";
	
	private static final int INITIALIZE_SUCCESS = 0;
	private static final int INITIALIZE_FAILURE = 1;
	
	// This is passed in for the startActivityForResult() android function. 
	// The value used is up to you and is referenced later to verify the request result's authenticity
    private static final int request = 1234;

    // Labels for our UI display
    private TextView titleLabel;
    private TextView priceLabel;
    private TextView descriptionLabel;
    
    // Our PayPal checkout button
    private CheckoutButton checkoutButton;
    
    // This yard sale item is populated by Gson with results from the server
    private static Item item;
    
    /* This method is the entry point into the activity. It accesses the Intent
     * that switched to this Activity and uses its "uri" extra to perform
     * server I/O that populates the display. The PayPal MPL initialization also 
     * occurs in this method.
     */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.itemdetails);
        
        String itemUri = getIntent().getStringExtra("uri");
    	
        try {
    		
    		// Fetch an item from the server
	        item = getYardSaleItem(itemUri);
	        Log.i("YARD_SALE_ITEM", item.toString());
	        
	        // Populate the current view
	        titleLabel = (TextView)findViewById(R.id.title);
	        titleLabel.setText(item.getName());
	        
	        priceLabel = (TextView) findViewById(R.id.price);
	        priceLabel.setText("Price: $" + String.valueOf(item.getPrice()));
	        
	        descriptionLabel = (TextView) findViewById(R.id.description);
	        descriptionLabel.setText(item.getDescription());
	        
	        // Initialize the PayPal library in a separate thread because it requires communication with the server
			// which may take some time depending on the connection strength/speed.
			Thread libraryInitializationThread = new Thread() {
				public void run() {
								
					initLibrary();
					
					// The library is initialized so now create a CheckoutButton and update the UI.
					if (PayPal.getInstance().isLibraryInitialized()) {
						hRefresh.sendEmptyMessage(INITIALIZE_SUCCESS);
					}
					else {
						hRefresh.sendEmptyMessage(INITIALIZE_FAILURE);
					}
				}
			};
			libraryInitializationThread.start();	        
	        
        } catch(Exception ex) {
        	
        	// View log messages with logcat by invoking "adb logcat" in a terminal
        	Log.e("STACK_TRACE", ex.getMessage());
        }
    }
    
    /* This handler update the UI with a UI thread, which Android requires as a security mechanism */
	Handler hRefresh = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
		    	case INITIALIZE_SUCCESS:
		    		setupPaymentButton();
		            break;
		    	case INITIALIZE_FAILURE:
		    		Log.e("ERROR", "Failure initializing library");
		    		break;
			}
		}
	};
	
	/* Called from the MPL initialization thread. */
    private void initLibrary() {
    	PayPal pp = PayPal.getInstance();
		if (pp == null) {
			
			// This is the main initialization call that takes in your Context, the Application ID, 
			// and the server you would like to connect to.
			pp = PayPal.initWithAppID(this, appID, server);
   			
			// Required settings.
        	pp.setLanguage("en_US"); // Sets the language for the library.
        	
        	// Sets the fees for the payer. If there are fees for the transaction, this person will pay for them. 
        	// Possible values are FEEPAYER_SENDER, FEEPAYER_PRIMARYRECEIVER, FEEPAYER_EACHRECEIVER, and 
        	// FEEPAYER_SECONDARYONLY.
        	pp.setFeesPayer(PayPal.FEEPAYER_EACHRECEIVER); 
		}
    }
    
	/* Creates the CheckoutButton and updates the UI. Called when the hRefresh Handler is sent an
	 * INITIALIZE_SUCCESS message. */
	private void setupPaymentButton() {
		
		PayPal pp = PayPal.getInstance();
		
		// Get the CheckoutButton. There are five different sizes. The text on the button can either be of type TEXT_PAY or TEXT_DONATE.
		checkoutButton = pp.getCheckoutButton(this, PayPal.BUTTON_194x37, CheckoutButton.TEXT_PAY);
		
		// Setup a click handler
		checkoutButton.setOnClickListener(this);

		LinearLayout mainLayout = (LinearLayout)findViewById(R.id.details);
		mainLayout.addView(checkoutButton);
	}
	
	/* Helper for fetching yard sale items from the server */
    private Item getYardSaleItem(String uri){
        DefaultHttpClient httpClient = new DefaultHttpClient();
        InputStream data = null;
        try {
            HttpGet method = new HttpGet(new URI(uri));
            HttpResponse response = httpClient.execute(method);
            data = response.getEntity().getContent();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Gson gson = new Gson();
        return gson.fromJson(new InputStreamReader(data), Item.class);
    }
    
    /* The click handler for the checkout button */
	public void onClick(View v) {
		
        // Use our helper function to create the simple payment.
        PayPalPayment payment = yardSaleSimplePayment(item); 
        
        // Use checkout to create our Intent.
        Intent checkoutIntent = PayPal.getInstance().checkout(payment, this);
        
        // Use the android's startActivityForResult() and pass in our Intent. This will start the library.
        startActivityForResult(checkoutIntent, request);	
	}
    
	/* Where all of the action happens for setting up the payment and invoice details */
    private PayPalPayment yardSaleSimplePayment(Item ysi) {
        // Create a basic PayPalPayment.
        PayPalPayment payment = new PayPalPayment();
        
        // Sets the currency type for this payment.
        payment.setCurrencyType("USD");
        
        // Sets the recipient for the payment (email or phone number)
        payment.setRecipient(ysi.getSeller());
        
        // This can be PAYMENT_TYPE_GOODS, PAYMENT_TYPE_SERVICE, PAYMENT_TYPE_PERSONAL, or PAYMENT_TYPE_NONE.
        payment.setPaymentType(PayPal.PAYMENT_TYPE_GOODS);

        // Sets the amount of the payment, not including tax and shipping amounts.
        payment.setSubtotal(new BigDecimal(String.valueOf(ysi.getPrice())));
        
        // PayPalInvoiceData can contain tax and shipping amounts. It also contains an ArrayList of PayPalInvoiceItem which can
        // be filled out. These are not required for any transaction.
        PayPalInvoiceData invoice = new PayPalInvoiceData();

        // PayPalInvoiceItem has several parameters available to it. None of these parameters is required.
        PayPalInvoiceItem item1 = new PayPalInvoiceItem();
        
        // Sets the name of the item.
        item1.setName(ysi.getName());
        
        // Sets the ID. This is any ID that you would like to have associated with the item.
        item1.setID(ysi.getId());
        
        // Sets the unit price.
        item1.setUnitPrice(new BigDecimal(String.valueOf(ysi.getPrice())));

        // Sets the quantity.
        item1.setQuantity(1);
        
        // No taxes or shipping for yard sales.
        invoice.setTax(new BigDecimal("0.00")); 
        invoice.setShipping(new BigDecimal("0.00"));
        
        // Sets the total price which should be (quantity * unit price). The total prices of all PayPalInvoiceItem should add up
        // to less than or equal the subtotal of the payment.
        item1.setTotalPrice(new BigDecimal(String.valueOf(ysi.getPrice())));
        
        // Add the PayPalInvoiceItem to the PayPalInvoiceData. Alternatively, you can create an ArrayList<PayPalInvoiceItem>
        // and pass it to the PayPalInvoiceData function setInvoiceItems().
        invoice.getInvoiceItems().add(item1);

        // Sets the PayPalPayment invoice data.
        payment.setInvoiceData(invoice);
        
        // Sets the merchant name. This is the name of your Application or Company.
        payment.setMerchantName("Ye Old Neighborhood Yard Sale");
        
        // Sets the description of the payment.
        payment.setDescription("Quite a payment for a yard sale purchase.");
        
        // Sets the Custom ID. This is any ID that you would like to have associated with the payment.
        payment.setCustomID(ysi.getId());
        
        // Sets the Instant Payment Notification url. This url will be hit by the PayPal server upon completion of the payment.
        // See https://www.paypal.com/ipn for more details about the wonders of IPNs.
        payment.setIpnUrl("http://www.example.com/ipn");
        
        // Sets the memo. This memo will be part of the notification sent by PayPal to the necessary parties.
        payment.setMemo("Thanks for purchasing.");

        return payment;
    }
    
    /**
     * Transaction results.
     * The resultCode will tell you how the transaction ended and other information can be pulled
     * from the Intent using getStringExtra.
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode != request)
            return;
        
        // Display a message to the user.
        switch(resultCode) {
	        case PayPalActivity.RESULT_OK:
	            descriptionLabel.setText("You have successfully purchased this item.");
	            break;
	        case PayPalActivity.RESULT_CANCELED:
	            descriptionLabel.setText("The transaction has been cancelled.");
	            break;
	        case PayPalActivity.RESULT_FAILURE:
	            descriptionLabel.setText(data.getStringExtra(PayPalActivity.EXTRA_ERROR_MESSAGE));
        }

        checkoutButton.updateButton();
 
        // You could do various other things here like update the display with a button to resume
        // scanning, etc.
    }

	
}