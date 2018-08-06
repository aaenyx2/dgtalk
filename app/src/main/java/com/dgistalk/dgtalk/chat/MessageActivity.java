package com.dgistalk.dgtalk.chat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.dgistalk.dgtalk.R;
import com.dgistalk.dgtalk.model.ChatModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MessageActivity extends AppCompatActivity {

    private String destinationUid;
    private Button button;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        destinationUid= getIntent().getStringExtra("destinationUid"); //destinationUid라는 이름으로 intent에 extra put된 값을 받아오는 코드
        button = findViewById(R.id.messageActivity_button);
        editText = findViewById(R.id.messageActivity_edittext);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 버튼을 누르면 대화방이 만들어지거나(새로 대화할 경우) 대화가 추가되는 기능을 만든다
                ChatModel chatModel = new ChatModel();
                chatModel.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                chatModel.destinationUid = destinationUid;
                //데이터베이스에 삽입
                FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel);
            }
        });
    }
}
