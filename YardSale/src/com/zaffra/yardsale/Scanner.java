/**
 * This class is a simple Activity that initiates a scan on a QR Code and passes its results
 * to the ItemDetails Activity. It is passed via an extra in an Intent as "uri" because it's 
 * expected that it'll be a URI that can be accessed to fetch details about an item to which
 * the QR Code is attached in a yard sale setting.
 * 
 * While the full jar file containing the ZXing scanner could could be trivially bundled with
 * this project, we've instead opted to selectively demonstrate the use of the IntentIntegrator 
 * class to showcase Android's Intents and to keep the size of the app intentionally light.
 */

package com.zaffra.yardsale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class Scanner extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scanner);
	}
	
	public void doScan(View v) {
		IntentIntegrator.initiateScan(this);
	}
	
	public void onActivityResult(int request, int result, Intent i) {
		IntentResult scan=IntentIntegrator.parseActivityResult(request, result, i);
		
		if (scan.getContents() !=null) {
			Intent intent = new Intent(getApplicationContext(), ItemDetails.class);
			intent.putExtra("uri", scan.getContents());
			startActivityForResult(intent, 0);
		}
	}
}
