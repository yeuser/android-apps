package dev.quizlearn.data;

public class SheetScore {
	QuizSheet sheet;
	float score;
	byte[] testscores = new byte[10];
	long[] testtimes = new long[10];
	byte testcnt = 0;
	float weight;
}
