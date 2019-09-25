package com.dinlas.chatbot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        //initialize the button view
        Button myButton = (Button)findViewById(R.id.chatbot_button);

        //set an onclick listener to "Start using the chat bot button"
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //method or intent  that takes one to chat screen after clicking the button

                Intent startChatScreen = new Intent(WelcomeActivity.this , MainActivity.class);
                startActivity(startChatScreen);
            }
        });

    }
}
