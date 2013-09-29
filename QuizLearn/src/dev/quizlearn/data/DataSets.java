package dev.quizlearn.data;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

public class DataSets implements QuizSets {
	/* Saved Datas */
	private OrderedKeyList<String, QuizSheet> sheets = new OrderedKeyList<String, QuizSheet>();
	private HashMap<String, Vector<SheetAnswer>> answerDataMap = new HashMap<String, Vector<SheetAnswer>>();
	/* Semi-AI parameters */
	private final double constAttr1 = 10; // multiplier for scoring each sheet
	private final double constAttr2 = 0.8; // percent of being OK to go to next level
	private final int constAttr3 = 10; // number of accepted answer record for each word
	private static final double constAttr4 = 5 * 24 * 60 * 60 * 1000; // 5 Day period

	private String saveFile;
	private Activity activity;

	public DataSets(Vector<QuizSheet> sheets, Vector<SheetAnswer> answerdata, String saveFile, Activity activity) {
		this.saveFile = saveFile;
		this.activity = activity;
		for (QuizSheet quizSheet : sheets) {
			// this.sheets.add(quizSheet);
			// if (this.sheetMap.get(quizSheet.question) == null) {
			// this.sheetMap.put(quizSheet.question, quizSheet);
			// } else {
			if (!this.sheets.put(quizSheet.question, quizSheet)) {
				System.err.println("Duplicate question found! " + quizSheet.question + " " + quizSheet.answer[quizSheet.correctAnswer]);
				System.err.println("Duplicate of : "
						+ this.sheets.get(quizSheet.question).answer[this.sheets.get(quizSheet.question).correctAnswer]);
			}
		}
		for (SheetAnswer anssht : answerdata) {
			if (answerDataMap.get(anssht.question) == null) {
				answerDataMap.put(anssht.question, new Vector<SheetAnswer>());
			}
			answerDataMap.get(anssht.question).add(anssht);
		}
		for (QuizSheet quizSheet : sheets) {
			this.calculateScore(quizSheet);
			this.calculateWeight(quizSheet);
		}
		this.closeTestSession();
	}

	@Override
	public int getSheetCount() {
		return sheets.size();
	}

	public QuizSheet getSheetAt(int index) {
		return sheets.get(index);
	}

	@Override
	public QuizSheet getSheet(String question) {
		return sheets.get(question);
	}

	@Override
	public QuizSheet getCurrent() {
		if (numAtList < 0)
			return null;
		return getSheet(answerList.get(numAtList).question);
	}

	@Override
	public int getCurrentIndex() {
		return numAtList;
	}

	@Override
	public boolean moveForward() {
		numAtList++;
		if (numAtList < answerList.size()) {
			return true;
		}
		if (answerList.size() < this.getSheetCount()) {
			QuizSheet qs = this.getScoredRandomSheet();
			answerList.put(qs.question, new SheetAnswer(qs.question));
			return true;
		}
		numAtList--;
		return false;
	}

	@Override
	public boolean moveBackward() {
		if (numAtList <= 0)
			return false;
		--numAtList;
		return true;
	}

	@Override
	public String getUserAnswer() {
		return answerList.get(numAtList).answer;
	}

	@Override
	public void setUserAnswer(String text) {
		answerList.get(numAtList).setAnswer(text);
		this.calculateScore(this.getCurrent());
		this.calculateWeight(this.getCurrent());
	}

	private double calculateScore(QuizSheet sh) {
		if (answerDataMap.get(sh.question) == null) {
			return 0;
		}
		long currtime = System.currentTimeMillis();
		double score = 0;
		Vector<SheetAnswer> answerList = answerDataMap.get(sh.question);
		for (SheetAnswer shans : answerList) {
			if (sh.checkAnswer(shans.answer)) {
				score += Math.exp((shans.answertime - currtime) / constAttr4);
			} else {
				score -= Math.exp((shans.answertime - currtime) / constAttr4);
			}
		}
		return sh.score = score;
	}

	private double calculateWeight(QuizSheet sh) {
		return sh.weight = 1 / (1 + Math.exp(constAttr1 * sh.score));
	}

	private Random random = new Random(System.currentTimeMillis());

	private QuizSheet getScoredRandomSheet() {
		String[] allAnswerMapQuestions = answerDataMap.keySet().toArray(new String[] {});
		if (allAnswerMapQuestions.length < 10) {
			return getNewRandomSheet();
		}
		ArrayList<String> unusedAnswerMapQuestions = new ArrayList<String>();
		for (String question : allAnswerMapQuestions) {
			if (answerList.get(question) == null)
				unusedAnswerMapQuestions.add(question);
		}
		if (unusedAnswerMapQuestions.size() < 3) {
			return getNewRandomSheet();
		}
		double tscore = 0, tw = 0;
		for (String question : unusedAnswerMapQuestions) {
			tscore += this.getSheet(question).score;
		}
		if (tscore / unusedAnswerMapQuestions.size() > constAttr2) {
			return getNewRandomSheet();
		}
		for (String question : unusedAnswerMapQuestions) {
			tw += this.getSheet(question).weight;
		}
		double d = random.nextDouble() * tw;
		for (String question : unusedAnswerMapQuestions) {
			d -= this.getSheet(question).weight;
			if (d <= 0) {
				return this.getSheet(question);
			}
		}
		return this.getSheet(unusedAnswerMapQuestions.get(unusedAnswerMapQuestions.size() - 1));
	}

	private QuizSheet getNewRandomSheet() {
		QuizSheet sheet = this.getSheetAt(random.nextInt(getSheetCount()));
		while (answerList.get(sheet.question) != null)
			sheet = this.getSheetAt(random.nextInt(getSheetCount()));
		return sheet;
	}

	@Override
	public Vector<SheetAnswer> getAnswerRecordData() {
		Vector<SheetAnswer> answerData = new Vector<SheetAnswer>();
		Set<String> keys = answerDataMap.keySet();
		for (String key : keys) {
			answerData.addAll(answerDataMap.get(key));
		}
		return answerData;
	}

	private Vector<SheetAnswer> reOrderAnswerList(Vector<SheetAnswer> answers) {
		Vector<SheetAnswer> ret = new Vector<SheetAnswer>();
		for (SheetAnswer answer : answers) {
			int j = 0;
			for (; j < ret.size() && ret.elementAt(j).answertime < answer.answertime; j++) {
			}
			ret.add(j, answer);
		}
		return ret;
	}

	private void saveSessionAnswers() {
		for (SheetAnswer answer : answerList) {
			if (answerDataMap.get(answer.question) == null) {
				answerDataMap.put(answer.question, new Vector<SheetAnswer>());
			}
			answerDataMap.get(answer.question).add(answer);
		}
		Set<String> keys = answerDataMap.keySet();
		for (String key : keys) {
			answerDataMap.put(key, reOrderAnswerList(answerDataMap.get(key)));
		}
	}

	private void clearSessionAnswers() {
		answerList.clear();
		numAtList = -1;
	}

	private void trimAnswerData() {
		Set<String> keys = answerDataMap.keySet();
		for (String key : keys) {
			Vector<SheetAnswer> ansvec = answerDataMap.get(key);
			while (ansvec.size() > constAttr3) {
				ansvec.remove(0);
			}
		}
	}

	@Override
	public void closeTestSession() {
		saveSessionAnswers();
		clearSessionAnswers();
		trimAnswerData();
		try {
			FileOutputStream fos = activity.openFileOutput(saveFile, Context.MODE_PRIVATE);
			XMLQuizSets.saveDataSets(this, fos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* Test Session Data */
	// private HashMap<String, SheetAnswer> answerListMap = new HashMap<String, SheetAnswer>();
	// private Vector<SheetAnswer> answerList = new Vector<SheetAnswer>();
	private OrderedKeyList<String, SheetAnswer> answerList = new OrderedKeyList<String, SheetAnswer>();
	private int numAtList = -1;

	@Override
	public void LoadState(Bundle savedState) {
		int len = savedState.getInt("length");
		numAtList = savedState.getInt("numAtList");
		for (int i = 0; i < len; i++) {
			String question = savedState.getString("question[" + i + "]");
			SheetAnswer sheetAnswer = new SheetAnswer(question);
			String answer = savedState.getString("answer[" + i + "]");
			sheetAnswer.answer = answer;
			long answertime = savedState.getLong("answertime[" + i + "]");
			sheetAnswer.answertime = answertime;
			answerList.put(sheetAnswer.question, sheetAnswer);
		}
	}

	@Override
	public void SaveState(Bundle outState) {
		outState.putInt("length", answerList.size());
		outState.putInt("numAtList", numAtList);
		for (int i = 0; i < answerList.size(); i++) {
			outState.putString("question[" + i + "]", answerList.get(i).question);
			outState.putString("answer[" + i + "]", answerList.get(i).answer);
			outState.putLong("answertime[" + i + "]", answerList.get(i).answertime);
		}
	}

	// @Override
	// public void LoadState(String savedStateString) {
	// DataSets ds = new Gson().fromJson(savedStateString, DataSets.class);
	// this.quizList = ds.quizList;
	// this.quizMap = ds.quizMap;
	// this.answerList = ds.answerList;
	// this.numAtList = ds.numAtList;
	// // int quizListLength = savedState.getInt("quizListLength");
	// // Log.i("load_app", "quizListLength=" + quizListLength);
	// // String notf = "quizListLength=" + quizListLength;
	// // for (int i = 0; i < quizListLength; i++) {
	// // Log.i("load_app", "Starting load for quiz sheet " + i);
	// // Bundle b = savedState.getBundle("quiz_sheet_" + i);
	// // notf += ",quiz_sheet_" + i;
	// // QuizSheet q = new QuizSheet();
	// // q.LoadState(b);
	// // quizList.add(q);
	// // quizMap.put(q.question, q);
	// // Log.i("load_app", "Starting load for sheet answer " + i);
	// // b = savedState.getBundle("quiz_sheet_answer_" + i);
	// // notf += ",quiz_sheet_answer_" + i;
	// // SheetAnswer sa = new SheetAnswer(q.question);
	// // sa.LoadState(b);
	// // answerList.add(sa);
	// // }
	// // numAtList = savedState.getInt("numAtList");
	// // Log.i("load_app", "numAtList=" + numAtList);
	// // notf += ",numAtList=" + numAtList;
	// // MainActivity.makeNotification(notf);
	// }
	//
	// @Override
	// public String SaveState() {
	// return new Gson().toJson(this);
	// // Log.i("save_app", "quizListLength=" + quizList.size());
	// // outState.putInt("quizListLength", quizList.size());
	// // String notf = "quizListLength=" + "quizListLength=" + quizList.size();
	// // int i = 0;
	// // for (QuizSheet quizSheet : quizList) {
	// // Log.i("save_app", "Starting save for quiz sheet " + i);
	// // Bundle b = new Bundle();
	// // quizSheet.SaveState(b);
	// // notf += ",quiz_sheet_" + i;
	// // outState.putBundle("quiz_sheet_" + i++, b);
	// // }
	// // i = 0;
	// // for (SheetAnswer sheetAnswer : answerList) {
	// // Log.i("save_app", "Starting save for sheet answer " + i);
	// // Bundle b = new Bundle();
	// // sheetAnswer.SaveState(b);
	// // notf += ",quiz_sheet_answer_" + i;
	// // outState.putBundle("quiz_sheet_answer_" + i++, b);
	// // }
	// // Log.i("save_app", "numAtList=" + numAtList);
	// // outState.putInt("numAtList", numAtList);
	// // notf += ",numAtList=" + numAtList;
	// // MainActivity.makeNotification(notf);
	// }
	@Override
	public String toString() {
		String ret = "";
		Set<String> keySet = answerDataMap.keySet();
		for (String key : keySet) {
			QuizSheet sh = sheets.get(key);
			ret += "QuizSheet{" + sh.question + "," + sh.getCorrectAnswer() + "}\r\n";
			if (answerDataMap.get(sh.question) == null) {
				ret += "answerMap.get(sh.question) == null >> 1000\r\n";
			}
			long currtime = System.currentTimeMillis();
			double score = 0;
			Vector<SheetAnswer> answerList = answerDataMap.get(sh.question);
			if (answerList != null)
				for (SheetAnswer shans : answerList) {
					if (sh.checkAnswer(shans.answer)) {
						score += Math.exp((shans.answertime - currtime) / 3.6E8);
					} else {
						score -= Math.exp((shans.answertime - currtime) / 3.6E8);
					}
				}
			ret += "score >> " + score + "\r\n";
		}
		return ret;
	}
}

class OrderedKeyList<K, V> implements Iterable<V> {
	private ArrayList<V> objs = new ArrayList<V>();
	private HashMap<K, V> objsMap = new HashMap<K, V>();

	public V get(K key) {
		return objsMap.get(key);
	}

	public V get(int index) {
		return objs.get(index);
	}

	public Set<K> keySet() {
		return objsMap.keySet();
	}

	public Set<V> valueSet() {
		Set<V> ret = new HashSet<V>();
		for (V obj : objs) {
			ret.add(obj);
		}
		return ret;
	}

	public boolean put(K key, V value) {
		if (objsMap.get(key) != null)
			return false;
		objsMap.put(key, value);
		objs.add(value);
		return true;
	}

	public boolean put(int index, K key, V value) {
		if (objsMap.get(key) != null)
			return false;
		objsMap.put(key, value);
		objs.add(index, value);
		return true;
	}

	public int size() {
		return objs.size();
	}

	public void clear() {
		objs.clear();
		objsMap.clear();
	}

	@Override
	public Iterator<V> iterator() {
		return objs.iterator();
	}
}