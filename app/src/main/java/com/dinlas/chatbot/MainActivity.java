package com.dinlas.chatbot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import ai.api.AIServiceContext;
import ai.api.android.AIDataService;
import ai.api.model.AIRequest;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
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

	private String invalidNameFormatMessage = "Please enter name in this format: My name is *username*";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		inputLayout = findViewById(R.id.inputLayout);
		
		final ScrollView scrollview = findViewById(R.id.chatScrollView);
		scrollview.post(() -> scrollview.fullScroll(ScrollView.FOCUS_DOWN));
		
		chatLayout = findViewById(R.id.chatLayout);
		
		sendBtn = findViewById(R.id.sendBtn);
		
		queryEditText = findViewById(R.id.queryEditText);

		ImageView backBtn = findViewById(R.id.toolbar_back_btn);

		backBtn.setClickable(true);
		backBtn.setFocusable(true);
		chatLayout.setOnClickListener(this);

		backBtn.setOnClickListener(view -> {
           onBackPressed();
        });

		initV2Chatbot();
		toggleInputLayoutFocus();
		initChatResponse();

	}

	//starts sending the communication to the DialogFlow bot
	private void setUpSendBtn(){

        sendBtn.setOnClickListener(this::sendMessage);

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
    }

    //Loads the initial bot responses before it is taken over by DialogFlow bot
	private void initChatResponse(){

        new Handler().postDelayed(() -> showTextView("Hi I'm Amaka, Dinlas Official Chat Bot and I'm here to help you", BOT), 500);

        new Handler().postDelayed(() -> showTextView("What shall I call you ?", BOT), 1000);

        new Handler().postDelayed(() -> showTextView(invalidNameFormatMessage, BOT), 1500);

        sendBtn.setOnClickListener(view -> {
            showTextView(queryEditText.getText().toString(), USER);
            checkUsername(queryEditText.getText().toString());
            queryEditText.setText("");
        });
        queryEditText.setOnKeyListener((view, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        showTextView(queryEditText.getText().toString(), USER);
                        checkUsername(queryEditText.getText().toString());
                        queryEditText.setText("");
                        return true;
                    default:
                        break;
                }
            }
            return false;
        });
    }

    //checks the username validity
    private void checkUsername(String username){

	    String[] arrOfString = username.split(" ");

	    if(username.contains("My name is ")){

            if(arrOfString.length >= 4){
                User.setUserName(arrOfString[3]);
                showTextView("Nice to meet you " + User.getUserName(), BOT);
                showTextView("How can I help you with HNG Internship", BOT);
                setUpSendBtn();
            }
            else{
                showTextViewV2(invalidNameFormatMessage, BOT);
            }

        }else{
            showTextViewV2(invalidNameFormatMessage, BOT);
        }

    }
	
	private void initV2Chatbot() {
		try {
			InputStream stream = getResources().openRawResource(R.raw.agent);
			GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
			String projectId = ((ServiceAccountCredentials) credentials).getProjectId();
			
			SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
			SessionsSettings sessionsSettings = settingsBuilder.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();
			sessionsClient = SessionsClient.create(sessionsSettings);
			session = SessionName.of(projectId, uuid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendMessage(View view) {
		String msg = queryEditText.getText().toString();
		if (msg.trim().isEmpty()) {
			Toast.makeText(MainActivity.this, "Please enter your query!", Toast.LENGTH_LONG).show();
		} else {
			showTextView(msg, USER);
			queryEditText.setText("");
			QueryInput queryInput = QueryInput.newBuilder().setText(TextInput.newBuilder().setText(msg).setLanguageCode("en")).build();
			new RequestJavaV2Task(MainActivity.this, session, sessionsClient, queryInput).execute();
		}
	}
	
	public void callbackV2(DetectIntentResponse response) {
		if (response != null) {
			// process aiResponse here
			String botReply = response.getQueryResult().getFulfillmentText();
			showTextViewV2(botReply, BOT);
		} else {
			showTextViewV2("There was some communication issue. Please go back one step and try again", BOT);
		}
	}
	
	private void showTextView(String message, int type) {
		FrameLayout layout;
		switch (type) {
			case USER:
				layout = getUserLayout();
				break;
			case BOT:
				layout = getBotLayout();
				break;
			default:
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


	//Appends username to every bot reply
    private void showTextViewV2(String message, int type) {
        FrameLayout layout;
        switch (type) {
            case USER:
                layout = getUserLayout();
                TextView tv2 = layout.findViewById(R.id.chatMsg);
                tv2.setText(message);
                break;
            case BOT:
                layout = getBotLayout();
                TextView tv = layout.findViewById(R.id.chatMsg);
                tv.setText(User.getUserName() +" " + message);
                break;
            default:
                layout = getBotLayout();
                TextView tv3 = layout.findViewById(R.id.chatMsg);
                tv3.setText(User.getUserName() + " " + message);
                break;
        }
        layout.setFocusableInTouchMode(true);
        chatLayout.addView(layout); // move focus to text view to automatically make it scroll up if softfocus
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
	private void toggleInputLayoutFocus(){

		inputLayout.setFocusableInTouchMode(true);
		inputLayout.setOnFocusChangeListener((view, b) -> {
			if(b){

			}
			else{

				inputLayout.setBackground(getDrawable(R.drawable.chat_message_background));
				sendBtn.setImageResource(R.drawable.on_focus_send_icon);
			}
		});
	}
	//method to close keyboard when the chat layout is pressed
	public void closeKeyboard() {
		View view = this.getCurrentFocus();
		if (view != null) {
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}
	public void onClick(View view) {
		int i = view.getId();
		if (i == R.id.chatLayout) {
			closeKeyboard();
		}
	}
}