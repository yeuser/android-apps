package dev.quizlearn.data;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Vector;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;

public class XMLQuizSets {
	private static void _runXMLPullParserOver(MyXMLHandler myxh, final XmlPullParser xpp) throws XmlPullParserException, SAXException,
			IOException {
		while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
			if (xpp.getEventType() == XmlPullParser.START_TAG) {
				myxh.startElement(xpp.getName(), new XMLAttributeWorker() {

					@Override
					public String getAttribute(String attribute) {
						for (int i = 0; i < xpp.getAttributeCount(); i++) {
							if (xpp.getAttributeName(i).equalsIgnoreCase(attribute)) {
								return xpp.getAttributeValue(i);
							}
						}
						return null;
					}
				});
			}
			if (xpp.getEventType() == XmlPullParser.END_TAG) {
				myxh.endElement(xpp.getName());
			}
			if (xpp.getEventType() == XmlPullParser.TEXT) {
				myxh.characters(xpp.getText());
			}
			xpp.next();
		}
	}

	public static QuizSets loadDataSets(XmlPullParser quizdatares, XmlPullParser answerdatares, String saveFile, Activity activity) {
		MyXMLHandler myxh = new MyXMLHandler();
		try {
			_runXMLPullParserOver(myxh, quizdatares);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			_runXMLPullParserOver(myxh, answerdatares);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new DataSets(myxh.sheets, myxh.answerdata, saveFile, activity);
	}

	public static QuizSets loadDataSets(InputStream quizdatafile, InputStream answerdatafile, String saveFile, Activity activity) {
		MyHandler myh = new MyHandler();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(true);
		try {
			factory.newSAXParser().parse(quizdatafile, myh);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			factory.newSAXParser().parse(answerdatafile, myh);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new DataSets(myh.handler.sheets, myh.handler.answerdata, saveFile, activity);
	}

	public static QuizSets loadDataSets(XmlPullParser quizdatares, InputStream answerdatafile, String saveFile, Activity activity) {
		MyXMLHandler myxh = new MyXMLHandler();
		try {
			_runXMLPullParserOver(myxh, quizdatares);
		} catch (Exception e) {
			e.printStackTrace();
		}
		MyHandler myh = new MyHandler();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(true);
		try {
			factory.newSAXParser().parse(answerdatafile, myh);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new DataSets(myxh.sheets, myh.handler.answerdata, saveFile, activity);
	}

	public static void saveDataSets(QuizSets quizSets, OutputStream answerdatafile) throws IOException {
		Vector<SheetAnswer> answerData = quizSets.getAnswerRecordData();
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(answerdatafile, Charset.forName("UTF-8")));
		out.write("<testanswers>");
		out.newLine();
		for (SheetAnswer sheetAnswer : answerData) {
			out.write("    <testanswer>");
			out.newLine();
			out.write("        <question>" + sheetAnswer.question + "</question>");
			out.newLine();
			out.write("        <answer>" + sheetAnswer.answer + "</answer>");
			out.newLine();
			out.write("        <answertime>" + sheetAnswer.answertime + "</answertime>");
			out.newLine();
			out.write("    </testanswer>");
			out.newLine();
		}
		out.write("</testanswers>");
		out.newLine();
		out.flush();
		out.close();
	}

	private static class MyHandler extends DefaultHandler {
		MyXMLHandler handler = new MyXMLHandler();

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			final Attributes _attributes = attributes;
			handler.startElement(qName, new XMLAttributeWorker() {

				@Override
				public String getAttribute(String attribute) {
					return _attributes.getValue(attribute);
				}
			});
		}

		public void endElement(String uri, String localName, String qName) throws SAXException {
			handler.endElement(qName);
		}

		public void characters(char ch[], int start, int length) throws SAXException {
			handler.characters(new String(ch, start, length));
		}
	}

	private static interface XMLAttributeWorker {
		String getAttribute(String attribute);
	}

	private static class MyXMLHandler extends SimpleXMLHandler {
		Vector<SheetAnswer> answerdata = new Vector<SheetAnswer>();
		Vector<QuizSheet> sheets = new Vector<QuizSheet>();
		Vector<String> answers = new Vector<String>();
		int correct = -1;

		public void characters(String text) {
			if (elements.size() > 2 && elements.elementAt(elements.size() - 2).equalsIgnoreCase("sheet")) {
				if (elements.lastElement().equalsIgnoreCase("question")) {
					sheets.lastElement().question = text;
				}
				if (elements.lastElement().equalsIgnoreCase("answer")) {
					answers.add(text);
				}
				if (elements.lastElement().equalsIgnoreCase("level")) {
					sheets.lastElement().level = Float.parseFloat(text);
				}
			}
			if (elements.size() > 2 && elements.elementAt(elements.size() - 2).equalsIgnoreCase("testanswer")) {
				if (elements.lastElement().equalsIgnoreCase("question")) {
					answerdata.lastElement().question = text;
				}
				if (elements.lastElement().equalsIgnoreCase("answer")) {
					answerdata.lastElement().answer = text;
				}
				if (elements.lastElement().equalsIgnoreCase("answertime")) {
					answerdata.lastElement().answertime = Long.parseLong(text);
				}
			}
		}

		@Override
		protected void onStartElement(String name, XMLAttributeWorker attributes) {
			if (name.equalsIgnoreCase("sheet")) {
				correct = -1;
				sheets.add(new QuizSheet());
				answers.clear();
			}
			if (name.equalsIgnoreCase("answer")) {
				if (attributes.getAttribute("correct") != null) {
					correct = answers.size();
				}
			}
			if (name.equalsIgnoreCase("testanswer")) {
				answerdata.add(new SheetAnswer(null));
			}
		}

		@Override
		protected void onEndElement(String name) {
			if (name.equalsIgnoreCase("sheet")) {
				sheets.lastElement().answer = answers.toArray(new String[] {});
				sheets.lastElement().correctAnswer = (byte) correct;
			}
		}
	}

	static abstract class SimpleXMLHandler {
		Vector<String> elements = new Vector<String>();

		public final void startElement(String name, XMLAttributeWorker attributes) {
			onStartElement(name, attributes);
			elements.add(name);
		}

		public final void endElement(String name) {
			elements.remove(elements.size() - 1);
			onEndElement(name);
		}

		protected abstract void onStartElement(String name, XMLAttributeWorker attributes);

		protected abstract void onEndElement(String name);

		public abstract void characters(String text);
	}
}