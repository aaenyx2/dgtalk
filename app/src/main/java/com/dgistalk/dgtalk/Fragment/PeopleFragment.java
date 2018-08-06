package com.dgistalk.dgtalk.Fragment;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.ListActivity;
import android.content.Intent;
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
import com.dgistalk.dgtalk.chat.MessageActivity;
import com.dgistalk.dgtalk.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PeopleFragment extends Fragment { // V4.fragment 말고 app.fragment

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //People Fragment와 fragment_people.xml을 연결시켜주는 코드
        View view = inflater.inflate(R.layout.fragment_people, container, false);
        //프래그먼트 내부에 recycler view를 통해 친구 목록을 띄워준다
        RecyclerView recyclerView = view.findViewById(R.id.peoplefragment_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new PeopleFragmentRecyclerViewAdapter()); // setAdpater를 위해  RecyclerView.Adpater를 상속받는 MyAdapter(여기서는 PeopleFragmentRecyclerViewAdapter)를 선언해주자.

        return view;
    }

    class PeopleFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        public List<UserModel> userModels; // 유저 목록으로 쓰일 list 선언
        public PeopleFragmentRecyclerViewAdapter() { // 생성자 안에서 친구 생성에 따라 유저 목록을 업데이트하는 기능
            userModels = new ArrayList<>(); // 유저 목록으로 쓰일 list 선언
            final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() { // users라는 이름의 table이 추가되는지 판단하는 listener
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) { // 이벤트 발생 시
                    userModels.clear(); // 유저 목록을 일단 초기화한 후
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){ // FirebaseDatabase 안에서 반복문을 돌려가며 유저 목록에다가 UserModel 객체들을 집어넣는다
                        UserModel userModel = snapshot.getValue(UserModel.class);
                        if(userModel.uid.equals(myUid)){
                            continue; // 만약 userModel의 uid가 현재 접속 중인 유저의 uid와 같을 경우에는 채팅방의 친구목록에 이 사람 정보를(= 즉 나를) 출력하지 않음
                        }
                        userModels.add(userModel);
                    }
                    notifyDataSetChanged(); //새로고침
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // RecyclerView Adapter를 상속받으면 자동으로 추가되는 뷰홀더 함수.
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
            return new CutomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) { // 몇번째 view holdder에 몇번째(position) 데이터베이스의 자료를 출력해줄지 설정
            // usermodel 안에 image 파일을 저장한다면, glide를 통해 읽어올 수도 있다.
            ((CutomViewHolder)holder).textView.setText(userModels.get(position).username);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), MessageActivity.class);
                    intent.putExtra("destinationUid", userModels.get(position).uid); // 해당 position의 userModel의 uid를 받아 intent를 이용해 Message Activity로 넘겨준다.
                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(v.getContext(), R.anim.fromright, R.anim.toleft); // 애니메이션 적용. 들어오는 뷰는 from right anim, 나가는 뷰는 to left anim.
                    startActivity(intent, activityOptions.toBundle()); //intent와 activity options 객체를 함께 인자로
                }
            });
        }

        @Override
        public int getItemCount() { // recycler view의 횟수를 return
            return userModels.size();
        }

        private class CutomViewHolder extends RecyclerView.ViewHolder { // 이렇게 ViewHolder를 Fragment 파일 안에 인라인으로 만들어서 써버릴 수도 있다
            public ImageView imageView;
            public TextView textView;
            public CutomViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.frienditem_imageview);
                textView = view.findViewById(R.id.frienditem_textview);

            }
        }
    }
}
