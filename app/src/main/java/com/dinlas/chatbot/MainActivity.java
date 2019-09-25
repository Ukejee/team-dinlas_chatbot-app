package com.dinlas.chatbot;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse;
import com.google.cloud.dialogflow.v2beta1.QueryInput;
import com.google.cloud.dialogflow.v2beta1.SessionName;
import com.google.cloud.dialogflow.v2beta1.SessionsClient;
import com.google.cloud.dialogflow.v2beta1.SessionsSettings;
import com.google.cloud.dialogflow.v2beta1.TextInput;

import java.io.InputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
	private static final int USER = 10001;
	private static final int BOT = 10002;
	
	private String uuid = UUID.randomUUID().toString();
	private LinearLayout chatLayout;
	private EditText queryEditText;
	
	// Java V2
	private SessionsClient sessionsClient;
	private SessionName session;
	
	private LinearLayout inputLayout;
	private ImageView sendBtn;
	
	// To make the bot speak first
	private boolean firstMessage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		inputLayout = findViewById(R.id.inputLayout);
		
		final ScrollView scrollview = findViewById(R.id.chatScrollView);
		scrollview.post(() -> scrollview.fullScroll(ScrollView.FOCUS_DOWN));
		
		chatLayout = findViewById(R.id.chatLayout);
		
		sendBtn = findViewById(R.id.sendBtn);
		sendBtn.setOnClickListener(this::sendMessage);
		
		queryEditText = findViewById(R.id.queryEditText);
		queryEditText.setOnKeyListener((view, keyCode, event) -> {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						sendMessage(sendBtn);
						return true;
					default:
						break;
				}
			}
			return false;
		});
		
		initV2Chatbot();
		toggleInputLayoutFocus();
		
		// First Message to initialize the bot
		firstMessage = false;
		sendMessage(null);
	}
	
	private void initV2Chatbot() {
		try {
			InputStream stream = getResources().openRawResource(R.raw.agent);
			GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
			String projectId = ((ServiceAccountCredentials) credentials).getProjectId();
			
			SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
			SessionsSettings sessionsSettings = settingsBuilder
					.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();
			sessionsClient = SessionsClient.create(sessionsSettings);
			session = SessionName.of(projectId, uuid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendMessage(View view) {
		if (!firstMessage) { // First message initialization
			QueryInput queryInput = QueryInput.newBuilder()
					.setText(TextInput.newBuilder().setText("Hello").setLanguageCode("en")).build();
			new RequestJavaV2Task(MainActivity.this, session, sessionsClient, queryInput).execute();
			firstMessage = true;
		} else { // To get the first text you'll start here
			String msg = queryEditText.getText().toString();
			if (msg.trim().isEmpty()) {
				Toast.makeText(MainActivity.this, "Please enter your query!", Toast.LENGTH_LONG).show();
			} else {
				showTextView(msg, USER);
				queryEditText.setText("");
				QueryInput queryInput = QueryInput.newBuilder()
						.setText(TextInput.newBuilder().setText(msg).setLanguageCode("en")).build();
				new RequestJavaV2Task(MainActivity.this, session, sessionsClient, queryInput).execute();
			}
		}
	}
	
	public void callbackV2(DetectIntentResponse response) {
		if (response != null) {
			// process aiResponse here
			int messageCount = response.getQueryResult().getFulfillmentMessagesCount();
			StringBuilder botReply = new StringBuilder();
			for (int i = 0; i < messageCount; i++)
				if (messageCount - i != 1)
					botReply.append(Helper.format(response.getQueryResult().getFulfillmentMessages(i).toString())).append("\n");
				else
					botReply.append(Helper.format(response.getQueryResult().getFulfillmentMessages(i).toString()));
			
			showTextView(botReply.toString(), BOT);
		} else {
			showTextView("There was some communication issue. Please exit the app and try again!", BOT);
		}
	}
	
	private void showTextView(String message, int type) {
		FrameLayout layout = new FrameLayout(this);
		switch (type) {
			case USER:
				layout = getUserLayout();
				break;
			case BOT:
				layout = getBotLayout();
				break;
		}
		layout.setFocusableInTouchMode(true);
		chatLayout.addView(layout); // move focus to text view to automatically make it scroll up if softfocus
		TextView tv = layout.findViewById(R.id.chatMsg);
		tv.setText(message);
		layout.requestFocus();
		queryEditText.requestFocus(); // change focus back to edit text to continue typing
	}
	
	FrameLayout getUserLayout() {
		LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
		return (FrameLayout) inflater.inflate(R.layout.user_msg_layout, null);
	}
	
	FrameLayout getBotLayout() {
		LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
		return (FrameLayout) inflater.inflate(R.layout.bot_msg_layout, null);
	}
	
	//method changes the input box color attributes on and off focus
	private void toggleInputLayoutFocus() {
		inputLayout.setFocusableInTouchMode(true);
		inputLayout.setOnFocusChangeListener((view, b) -> {
			if (b) {
			
			} else {
				inputLayout.setBackground(getDrawable(R.drawable.chat_message_background));
				sendBtn.setImageResource(R.drawable.on_focus_send_icon);
			}
		});
	}
}
