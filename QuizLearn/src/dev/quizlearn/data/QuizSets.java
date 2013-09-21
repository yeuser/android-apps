package dev.quizlearn.data;

import java.util.Vector;

public interface QuizSets extends StateSavable {
	public int getSheetCount();

	public boolean moveForward();

	public boolean moveBackward();

	public int getCurrentIndex();

	public QuizSheet getCurrent();

	public String getUserAnswer();

	public void setUserAnswer(String text);

	public Vector<SheetAnswer> getAnswerRecordData();

	public void closeTestSession();

	public QuizSheet getSheet(String question);

}
