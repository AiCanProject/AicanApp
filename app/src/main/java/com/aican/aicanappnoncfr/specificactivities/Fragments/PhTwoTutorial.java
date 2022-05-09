//package com.aican.aicanapp.specificactivities.Fragments;
//
//import android.content.Intent;
//import android.os.Bundle;
//
//import androidx.fragment.app.Fragment;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.TextView;
//
//import com.aican.aicanapp.R;
//
//public class PhTwoTutorial extends Fragment {
//
//    TextView textView;
//    Button nextBtn;
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        View view = inflater.inflate(R.layout.fragment_ph_two_tutorial, container, false);
//        textView = view.findViewById(R.id.textView);
//        textView.setText("Tutorial");
//        nextBtn = view.findViewById(R.id.next_btn);
//        nextBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                PhMain ph = new PhMain();
//                Bundle arguments = new Bundle();
//                arguments.putInt("ph_val",2);
//                ph.setArguments(arguments);
//                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,ph).commit();
//            }
//        });
//        return view;
//    }
//}