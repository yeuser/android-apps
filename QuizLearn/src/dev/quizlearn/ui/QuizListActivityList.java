package dev.quizlearn.ui;

import java.io.FileInputStream;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import dev.quizlearn.data.QuizSets;
import dev.quizlearn.data.XMLQuizSets;

public class QuizListActivityList extends Activity {
	private static HashMap<String, QuizSets> quizsets = new HashMap<String, QuizSets>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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
}