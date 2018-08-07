package com.dgistalk.dgtalk.chat;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dgistalk.dgtalk.R;
import com.dgistalk.dgtalk.model.ChatModel;
import com.dgistalk.dgtalk.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    private String destinationUid;
    private Button button;
    private EditText editText;

    private String uid;
    private String chatRoomUid;

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 채팅을 요구하는 아이디 = 단말기에 로그인된 UID
        destinationUid= getIntent().getStringExtra("destinationUid"); //destinationUid라는 이름으로 intent에 extra put된 값을 받아오는 코드. 채팅을 당하는 아이디
        button = findViewById(R.id.messageActivity_button);
        editText = findViewById(R.id.messageActivity_edittext);
        recyclerView = findViewById(R.id.messageActivity_recyclerview);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 버튼을 누르면 대화방이 만들어지거나(새로 대화할 경우) 대화가 추가되는 기능을 만든다
                ChatModel chatModel = new ChatModel();
                //대화방 데이터에 대화참여자들의 정보를 기재
                chatModel.users.put(uid,true);
                chatModel.users.put(destinationUid,true);
                //데이터베이스에 삽입


                if (chatRoomUid == null){ //
                    button.setEnabled(false); // chatroomUid가 null인지 확인하는 사이엔 버튼을 불활성화해놓는다. 버그를 막기 위해
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            checkChatRoom(); // 채팅방의 중복여부를 조사
                        }
                    });
                } else {
                    ChatModel.Comment comment = new ChatModel.Comment();
                    comment.uid = uid;
                    comment.message = editText.getText().toString();
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {  // push()는 데이터베이스에 데이터가 '쌓이도록' 한다
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            editText.setText(""); // 입력한 코멘트를 데이터베이스에 push하는데에 성공하면 call-back 함수로 editText(입력창)를 비워줌

                        }
                    });
                }
            }
        });
        checkChatRoom();
    }

    void checkChatRoom(){
        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() { // 만약 채팅방 중에 내가(currentuser.uid) 존재하는 채팅방이 있다면(true)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    ChatModel chatModel = item.getValue(ChatModel.class);
                        if(chatModel.users.containsKey(destinationUid)){ //그리고 이 채팅창에 destinationUid를 uid로 갖는 user가 존재한다면
                            chatRoomUid = item.getKey(); // 그 방의 키 값을 chatRoomUid로서 담아서 전달
                            button.setEnabled(true);
                            // 해당 채팅방의 comment들을 recycler view를 통해 화면에 출력
                            recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
                            recyclerView.setAdapter(new RecyclerViewAdapter());
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }); // chat rooms 테이블을 chatrooms table 안의 users 변수 안의 uid 값에 따라 정렬.
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<ChatModel.Comment> comments;
        UserModel userModel;
        public RecyclerViewAdapter() {
            comments = new ArrayList<>();
            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userModel = dataSnapshot.getValue(UserModel.class); // userModeal에 상대방 유저를 저장
                    getMessageList();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        void getMessageList(){
            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    comments.clear(); // 항상 list는 선언한 뒤에 본격적으로 쓰기 전에 clear.
                    for(DataSnapshot item : dataSnapshot.getChildren()){
                        comments.add(item.getValue(ChatModel.Comment.class));
                    }
                    notifyDataSetChanged(); // 데이터를 다 읽어 불러들인 뒤에는 항상 갱신.
                    recyclerView.scrollToPosition(comments.size()-1); // comments.size() - 1 은 맨 마지막 comment의 position을 의미. 즉, 코멘트 데이터가 갱신될 때마다 화면을 최하단으로 scroll.
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            MessageViewHolder messageViewHolder = ((MessageViewHolder) holder);
            //내가 보낸 메시지
            if (comments.get(position).uid.equals(uid)) {
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.rightbubble);
                messageViewHolder.linearLayout_destination.setVisibility(View.INVISIBLE); // 내 comment일 경우에는 프로필 정보란을 생략한다.
                messageViewHolder.textView_message.setTextSize(15);
                messageViewHolder.linearLayout_main.setGravity(Gravity.RIGHT); // 내가 보낸 메시지일 땐 message_item의 전체 layout인 linearLayout_main을 오른쪽으로 정렬
            }
            //상대방이 보낸 메시지
            else {
                messageViewHolder.textView_name.setText(userModel.username);
                messageViewHolder.linearLayout_destination.setVisibility(View.VISIBLE); // 상대방 comment면 상대방 프로필 정보 칸을 띄운다
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.leftbubble);
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setTextSize(15);
                messageViewHolder.linearLayout_main.setGravity(Gravity.LEFT); // 상대방이 보낸 메시지일 땐 message_item의 전체 layout인 linearLayout_main을 왼쪽으로 정렬
            }
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class MessageViewHolder extends RecyclerView.ViewHolder {
            public TextView textView_message;
            public TextView textView_name;
            public ImageView imageView_profile; // 채팅창의 상대방 프로필 이미지를 뷰홀더에서 선언
            public LinearLayout linearLayout_destination;
            public LinearLayout linearLayout_main;

            public MessageViewHolder(View view) {
                super(view);
                textView_message = view.findViewById(R.id.messageItem_textview_message);
                textView_name = view.findViewById(R.id.messageItem_textview_name);
                imageView_profile = view.findViewById(R.id.messageItem_imageview_profile);
                linearLayout_destination = view.findViewById(R.id.messageItem_linearlayout_destination);
                linearLayout_main = view.findViewById(R.id.messageItem_linearlayout_main);

            }
        }
    }
}
