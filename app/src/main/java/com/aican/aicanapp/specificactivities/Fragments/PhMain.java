//package com.aican.aicanapp.specificactivities.Fragments;
//
//import android.os.Bundle;
//
//import androidx.fragment.app.Fragment;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.aican.aicanapp.R;
//import com.uzairiqbal.circulartimerview.CircularTimerListener;
//import com.uzairiqbal.circulartimerview.CircularTimerView;
//import com.uzairiqbal.circulartimerview.TimeFormatEnum;
//
//public class PhMain extends Fragment {
//
//    CircularTimerView progressBar;
//    Button nextBtn;
//    TextView currPh;
//    Button prevBtn;
//    TextView coefficient;
//    ImageView phScale;
//    TextView phHead;
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//
//        View view = inflater.inflate(R.layout.fragment_ph_main, container, false);
//        progressBar = view.findViewById(R.id.calibrateTimer);
//        nextBtn = view.findViewById(R.id.next_btn);
//        currPh = view.findViewById(R.id.currPh);
//        prevBtn = view.findViewById(R.id.prev_btn);
//        phScale = view.findViewById(R.id.ph_scale);
//        phHead = view.findViewById(R.id.phTag);
//        coefficient = view.findViewById(R.id.coefficient);
//        switch(getArguments().getInt("ph_val")){
//            case 2:phScale.setImageResource(R.drawable.ph2);
//            currPh.setText(""+2);
//            prevBtn.setVisibility(View.GONE);
//            phHead.setVisibility(View.VISIBLE);break;
//            case 4:phScale.setImageResource(R.drawable.ph4);
//                currPh.setText(""+4);break;
//            case 7:phScale.setImageResource(R.drawable.ph7);
//                currPh.setText(""+7);break;
//            case 9:phScale.setImageResource(R.drawable.ph9);
//                currPh.setText(""+9);break;
//            case 11:phScale.setImageResource(R.drawable.ph11);
//                currPh.setText(""+11);break;
//            default:phScale.setImageResource(R.drawable.ph2);
//        }
//        progressBar.setProgress(0);
//
//        coefficient.setText(""+(9*(int)Math.random()+1));
//
//
//
//
//        nextBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new PhFourTutorial()).commit();
//            }
//        });
//
//        progressBar.setCircularTimerListener(new CircularTimerListener() {
//            @Override
//            public String updateDataOnTick(long remainingTimeInMs) {
//                return String.valueOf((int)Math.ceil((remainingTimeInMs / 1000.f)));
//            }
//
//            @Override
//            public void onTimerFinished() {
//                Toast.makeText(getContext(), "FINISHED", Toast.LENGTH_SHORT).show();
//                progressBar.setPrefix("");
//                progressBar.setSuffix("");
//                progressBar.setText("Calibrate");
//            }
//        }, 120, TimeFormatEnum.SECONDS, 10);
//
//
//        progressBar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                progressBar.startTimer();
//            }
//        });
//
//
//
//// To start timer
//
//
//
//        return view;
//    }
//}