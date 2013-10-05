package dev.quizlearn.data;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

public class DataSets implements QuizSets {
	/* Saved Datas */
	private Vector<QuizSheet> sheets = new Vector<QuizSheet>();
	private HashMap<String, QuizSheet> sheetMap = new HashMap<String, QuizSheet>();
	private HashMap<String, Vector<SheetAnswer>> answerMap = new HashMap<String, Vector<SheetAnswer>>();
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
			this.sheets.add(quizSheet);
			if (this.sheetMap.get(quizSheet.question) == null) {
				this.sheetMap.put(quizSheet.question, quizSheet);
			} else {
				System.err.println("Duplicate question found! " + quizSheet.question + " " + quizSheet.answer[quizSheet.correctAnswer]);
				System.err.println("Duplicate of : "
						+ this.sheetMap.get(quizSheet.question).answer[this.sheetMap.get(quizSheet.question).correctAnswer]);
			}
		}
		for (SheetAnswer anssht : answerdata) {
			if (answerMap.get(anssht.question) == null) {
				answerMap.put(anssht.question, new Vector<SheetAnswer>());
			}
			answerMap.get(anssht.question).add(anssht);
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
		return sheets.elementAt(index);
	}

	@Override
	public QuizSheet getSheet(String question) {
		return sheetMap.get(question);
	}

	@Override
	public QuizSheet getCurrent() {
		if (numAtList < 0)
			return null;
		return getSheet(answerList.elementAt(numAtList).question);
	}

	@Override
	public int getCurrentIndex() {
		return numAtList;
	}

	@Override
	public boolean moveForward() {
		if (++numAtList >= answerList.size()) {
			if (answerList.size() < this.getSheetCount()) {
				SheetAnswer newSheet = getNewSheet();
				if (newSheet == null) {
					numAtList--;
					return false;
				}
				answerList.add(newSheet);
				return true;
			}
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

	public String getUserAnswer() {
		return answerList.elementAt(numAtList).answer;
	}

	@Override
	public void setUserAnswer(String text) {
		answerList.elementAt(numAtList).setAnswer(text);
		this.calculateScore(this.getCurrent());
		this.calculateWeight(this.getCurrent());
	}

	public QuizSheet getRandomSheet() {
		int r = (int) (System.currentTimeMillis() % this.getSheetCount());
		return this.getSheetAt(r);
	}

	public SheetAnswer getNewSheet() {
		if (answerList.size() == this.getSheetCount())
			return null;
		QuizSheet qs = this.getScoredRandomSheet();
		while (answerListMap.get(qs.question) != null) {
			qs = this.getScoredRandomSheet();
		}
		SheetAnswer sheetAnswer = new SheetAnswer(qs.question);
		answerListMap.put(qs.question, sheetAnswer);
		return sheetAnswer;
	}

	private double calculateScore(QuizSheet sh) {
		if (answerMap.get(sh.question) == null) {
			return 1000;
		}
		long currtime = System.currentTimeMillis();
		double score = 0;
		Vector<SheetAnswer> answerList = answerMap.get(sh.question);
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
		String[] answerMapKeys = answerMap.keySet().toArray(new String[] {});
		String[] answerListMapKeys = answerListMap.keySet().toArray(new String[] {});
		// if (sheetMap.size() >= answerMapKeys.length) {
		// return getRandomSheet();
		// }
		if (answerMapKeys.length < 5) {
			return getRandomSheet();
		}
		double tscore = 0, tw = 0;
		for (String key : answerMapKeys) {
			tscore += this.getSheet(key).score;
		}
		if (tscore / answerMapKeys.length > constAttr2) {
			return getRandomSheet();
		} else {
			for (String key : answerMapKeys) {
				// if (sheetMap.get(getSheet(key).question) == null) {
				tw += this.getSheet(key).weight;
				// }
			}
			double d = random.nextDouble() * tw;
			for (String key : answerMapKeys) {
				// if (sheetMap.get(getSheet(key).question) == null) {
				d -= this.getSheet(key).weight;
				if (d <= 0) {
					return this.getSheet(key);
				}
				// }
			}
			return this.getSheet(answerMapKeys[answerMapKeys.length - 1]);
		}
	}

	@Override
	public Vector<SheetAnswer> getAnswerRecordData() {
		Vector<SheetAnswer> answerData = new Vector<SheetAnswer>();
		Set<String> keys = answerMap.keySet();
		for (String key : keys) {
			answerData.addAll(answerMap.get(key));
		}
		return answerData;
	}

	private Vector<SheetAnswer> reorderAnswerList(Vector<SheetAnswer> answers) {
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
			if (answerMap.get(answer.question) == null) {
				answerMap.put(answer.question, new Vector<SheetAnswer>());
			}
			answerMap.get(answer.question).add(answer);
		}
		Set<String> keys = answerMap.keySet();
		for (String key : keys) {
			answerMap.put(key, reorderAnswerList(answerMap.get(key)));
		}
	}

	private void clearSessionAnswers() {
		answerList.clear();
		numAtList = -1;
	}

	private void trimAnswerData() {
		Set<String> keys = answerMap.keySet();
		for (String key : keys) {
			Vector<SheetAnswer> ansvec = answerMap.get(key);
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
	private HashMap<String, SheetAnswer> answerListMap = new HashMap<String, SheetAnswer>();
	private Vector<SheetAnswer> answerList = new Vector<SheetAnswer>();
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
			answerList.add(sheetAnswer);
		}
	}

	@Override
	public void SaveState(Bundle outState) {
		outState.putInt("length", answerList.size());
		outState.putInt("numAtList", numAtList);
		for (int i = 0; i < answerList.size(); i++) {
			outState.putString("question[" + i + "]", answerList.elementAt(i).question);
			outState.putString("answer[" + i + "]", answerList.elementAt(i).answer);
			outState.putLong("answertime[" + i + "]", answerList.elementAt(i).answertime);
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
		Set<String> keySet = answerMap.keySet();
		for (String key : keySet) {
			QuizSheet sh = sheetMap.get(key);
			ret += "QuizSheet{" + sh.question + "," + sh.getCorrectAnswer() + "}\r\n";
			if (answerMap.get(sh.question) == null) {
				ret += "answerMap.get(sh.question) == null >> 1000\r\n";
			}
			long currtime = System.currentTimeMillis();
			double score = 0;
			Vector<SheetAnswer> answerList = answerMap.get(sh.question);
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
