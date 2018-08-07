package com.dgistalk.dgtalk.Fragment;

import android.app.Fragment;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dgistalk.dgtalk.R;
import com.dgistalk.dgtalk.model.ChatModel;
import com.dgistalk.dgtalk.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ChatFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        RecyclerView recyclerView = v.findViewById(R.id.chatFragment_recyclerview);
        recyclerView.setAdapter(new ChatRecyclerViewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));

        return v;
    }

    class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private List<ChatModel> chatModels = new ArrayList<>();
        private String uid;
        public ChatRecyclerViewAdapter() { // 채팅방 목록을 가져온다. 회원의 uid와 chatModel을 담는 List가 필요.
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    chatModels.clear();
                    for(DataSnapshot item : dataSnapshot.getChildren()){
                        chatModels.add(item.getValue(ChatModel.class));
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });


        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final CustomViewHolder customViewHolder =(CustomViewHolder)holder;
            String destinationUid = null;
            for(String user: chatModels.get(position).users.keySet()){ // 포지션 별로 모든 채팅방에서 keySet(유저 id:key, True: Value)을 받아와서 keySet의 값을 user에 넣음. 즉 해당 채팅방에 참가한 두 user id들이 담김
                if(!user.equals(uid)){
                    destinationUid = user;
                }
            }
            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() { //title은 한 번만 읽어들이면 되므로 singleValueEvent
                // 해당 채팅방 목록에서, 해당 destinationUid를 가진 database에 변동이 생긴 경우
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //////////////////
                    UserModel userModel = dataSnapshot.getValue(UserModel.class);
                    customViewHolder.textview_title.setText(userModel.username);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            //해당 리사이클러 뷰에 할당된 채팅방의 가장 마지막 대화를 string에 저장해 setText로 view에 뿌려줌
            Map<String, ChatModel.Comment> commentMap = new TreeMap<>(Collections.reverseOrder());
            commentMap.putAll(chatModels.get(position).comments);
            String lastMessageKey = (String) commentMap.keySet().toArray()[0];
            customViewHolder.textview_lastMessage.setText(chatModels.get(position).comments.get(lastMessageKey).message);
        }

        @Override
        public int getItemCount() {
            return chatModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textview_title;
            public TextView textview_lastMessage;

            public CustomViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.chatItem_imageview);
                textview_title=view.findViewById(R.id.chatitem_textview_title);
                textview_lastMessage=view.findViewById(R.id.chatitem_textview_lastMessage);
            }
        }
    }

}
