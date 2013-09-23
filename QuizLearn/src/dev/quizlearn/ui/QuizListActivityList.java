package dev.quizlearn.ui;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.ads.AdRequest.ErrorCode;

import dev.quizlearn.data.QuizSets;
import dev.quizlearn.data.XMLQuizSets;

public class QuizListActivityList extends Activity {
	private static HashMap<String, QuizSets> quizsets = new HashMap<String, QuizSets>();
	private TextView infoLabel;

	private AdView mAdView;
	// Ad network-specific mechanism to enable test mode.
	private static final String TEST_DEVICE_ID = "...";
	private static final String TAG = "debug_test";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		infoLabel = (TextView) findViewById(R.id.info);

		mAdView = (AdView) findViewById(R.id.ad);
		mAdView.setAdListener(new MyAdListener());

		AdRequest adRequest = new AdRequest();
		adRequest.addKeyword("learning languages");
		adRequest.addKeyword("japanese");
		adRequest.addKeyword("german");
		adRequest.setTesting(true);

		// if(getApplication().debugEnabled(this)) //debug flag from somewhere that you set
		// {
		//
		String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
		String deviceId = QuizListSpinner.md5(android_id).toUpperCase(Locale.US);
		adRequest.addTestDevice(deviceId);
		boolean isTestDevice = adRequest.isTestDevice(this);

		infoLabel.setText(deviceId);
		Log.v(TAG, "is Admob Test Device ? " + deviceId + " " + isTestDevice); // to confirm it worked
		// }

		// Ad network-specific mechanism to enable test mode. Be sure to disable before
		// publishing your application.
		adRequest.addTestDevice(TEST_DEVICE_ID);
		mAdView.loadAd(adRequest);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	public void goKanji(View view) {
		Intent intent = new Intent(this, QuizListSpinner.class);
		intent.putExtra("type", R.xml.kanji);
		intent.putExtra("answerfile", "kanji");
		startActivity(intent);
	}

	public void goArtikel(View view) {
		Intent intent = new Intent(this, QuizListSpinner.class);
		intent.putExtra("type", R.xml.artikel);
		intent.putExtra("answerfile", "artikel");
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public static QuizSets getQuizSet(XmlResourceParser xml_rid, String name, Activity activity) {
		if (quizsets.get(name) == null) {
			XmlPullParser p = null;
			try {
				FileInputStream fileInputStream = activity.openFileInput("answerdata_" + name + ".xml");
				p = XmlPullParserFactory.newInstance().newPullParser();
				p.setInput(fileInputStream, "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
			QuizSets quizes = XMLQuizSets.loadDataSets(xml_rid, p, "answerdata_" + name + ".xml", activity);
			quizes.moveForward();
			quizsets.put(name, quizes);
		}
		return quizsets.get(name);
	}

	private class MyAdListener implements AdListener {
		@Override
		public void onFailedToReceiveAd(Ad ad, ErrorCode errorCode) {
			// mAdStatus.setText(R.string.error_receive_ad);
			// TODO Auto-generated method stub
		}

		@Override
		public void onReceiveAd(Ad ad) {
			// mAdStatus.setText("" + ad.getClass());
			// TODO Auto-generated method stub
		}

		@Override
		public void onDismissScreen(Ad arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onLeaveApplication(Ad arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onPresentScreen(Ad arg0) {
			// TODO Auto-generated method stub
		}
	}
}