package com.dinlas.chatbot;

// Helper class to format the given input
class Helper {
	static String format(String fulfillmentText) {
		String[] list = fulfillmentText.split("\"");
		char[] listChars = list[1].toCharArray();
		StringBuilder builder = new StringBuilder();
		for (char member : listChars) {
			if (member != '\\') builder.append(member);
		}
		
		return builder.toString();
	}
}