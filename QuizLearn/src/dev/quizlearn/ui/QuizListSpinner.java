package dev.quizlearn.ui;

import java.util.Vector;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdView;

import android.app.Activity;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import dev.quizlearn.data.QuizSets;
import dev.quizlearn.data.QuizSheet;

public class QuizListSpinner extends Activity {
	private QuizSets quizes = null;
	private TextView questionLabel;
	private EditText answerField;
	private TextView answerLabel;
	private Vector<RadioButton> radioButtons = new Vector<RadioButton>();
	private RadioGroup options;
	// private TextView mAdStatus;
	private AdView mAdView;

	// Ad network-specific mechanism to enable test mode.
	private static final String TEST_DEVICE_ID = "...";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_spinner);
		initializeAll();
		if (savedInstanceState == null) {
			// go2Next();
		} else {
			quizes.LoadState(savedInstanceState);
		}
		changeUI();
		// mAdStatus = (TextView) findViewById(R.id.status);
		mAdView = (AdView) findViewById(R.id.ad);
		mAdView.setAdListener(new MyAdListener());

		AdRequest adRequest = new AdRequest();
		adRequest.addKeyword("sporting goods");
		// adRequest.addKeyword("ad keywords");

		// Ad network-specific mechanism to enable test mode. Be sure to disable before
		// publishing your application.
		adRequest.addTestDevice(TEST_DEVICE_ID);
		mAdView.loadAd(adRequest);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		quizes.SaveState(outState);
		super.onSaveInstanceState(outState);
	}

	private void initializeAll() {
		NotificationCompatibility.def = this;
		answerField = (EditText) findViewById(R.id.answerField);
		questionLabel = (TextView) findViewById(R.id.questionLabel);
		answerLabel = (TextView) findViewById(R.id.answerLabel);
		options = (RadioGroup) findViewById(R.id.radioGroup);
		radioButtons.add((RadioButton) findViewById(R.id.radioButton1));
		radioButtons.add((RadioButton) findViewById(R.id.radioButton2));
		radioButtons.add((RadioButton) findViewById(R.id.radioButton3));
		radioButtons.add((RadioButton) findViewById(R.id.radioButton4));
		radioButtons.add((RadioButton) findViewById(R.id.radioButton5));
		radioButtons.add((RadioButton) findViewById(R.id.radioButton6));
		radioButtons.add((RadioButton) findViewById(R.id.radioButton7));
		radioButtons.add((RadioButton) findViewById(R.id.radioButton8));
		try {
			XmlResourceParser xrp = getResources().getXml(getIntent().getIntExtra("type", R.xml.kanji));
			String name = getIntent().getStringExtra("answerfile");
			quizes = QuizListActivityList.getQuizSet(xrp, name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onResetSession(View view) {
		quizes.closeTestSession();
		go2Next();
	}

	public void onBackward(View view) {
		goPrevious();
	}

	public void onForward(View view) {
		saveGoNext();
	}

	public void onSave(View view) {
		saveAnswer();
		changeUI();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_spinner, menu);
		return true;
	}

	private void saveAnswer() {
		if (quizes.getCurrentIndex() < 0)
			return;
		QuizSheet val = quizes.getCurrent();
		if (val.answer.length > 1) {
			for (int i = 0; i < val.answer.length; i++) {
				if (radioButtons.elementAt(i).isChecked()) {
					quizes.setUserAnswer("" + i);
					break;
				}
			}
		} else {
			quizes.setUserAnswer(answerField.getText().toString());
		}
	}

	private void changeUI() {
		QuizSheet val = quizes.getCurrent();
		String qtext = ((quizes.getCurrentIndex() + 1) + "." + val.question).replaceAll("\\s+", " ");
		questionLabel.setText(qtext);
		if (val.answer.length > 1) {
			if (quizes.getUserAnswer() == null) {
				options.clearCheck();
				for (int i = 0; i < val.answer.length; i++) {
					RadioButton rb = radioButtons.elementAt(i);
					rb.setText(val.answer[i]);
					// rb.setChecked(false);
					rb.setVisibility(View.VISIBLE);
					rb.setBackgroundColor(Color.TRANSPARENT);
					rb.setEnabled(true);
				}
			} else {
				options.clearCheck();
				options.setEnabled(false);
				for (int i = 0; i < val.answer.length; i++) {
					RadioButton rb = radioButtons.elementAt(i);
					boolean checked = quizes.getUserAnswer().equals("" + i);
					if (i == val.correctAnswer) {
						rb.setBackgroundColor(Color.GREEN);
					} else if (checked) {
						rb.setBackgroundColor(Color.RED);
					} else {
						rb.setBackgroundColor(Color.TRANSPARENT);
					}
					rb.setText(val.answer[i]);
					rb.setChecked(checked);
					rb.setVisibility(View.VISIBLE);
					rb.setEnabled(false);
				}
			}
			options.setVisibility(View.VISIBLE);
			answerField.setVisibility(View.GONE);
		} else {
			if (quizes.getUserAnswer() == null) {
				answerField.setEnabled(true);
				answerField.setText("");
				answerLabel.setVisibility(View.GONE);
			} else {
				answerField.setEnabled(false);
				answerField.setText(quizes.getUserAnswer());
				answerLabel.setVisibility(View.VISIBLE);
				answerLabel.setText(quizes.getCurrent().getCorrectAnswer());
			}
			answerField.setVisibility(View.VISIBLE);
			for (int i = 0; i < radioButtons.size(); i++) {
				radioButtons.elementAt(i).setVisibility(View.GONE);
			}
		}
	}

	protected void go2Next() {
		quizes.moveForward();
		changeUI();
	}

	protected void saveGoNext() {
		saveAnswer();
		go2Next();
	}

	protected void goPrevious() {
		saveAnswer();
		if (quizes.moveBackward()) {
			questionLabel.setText(quizes.getCurrentIndex() + ". " + quizes.getCurrent().question);
		}
		changeUI();
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