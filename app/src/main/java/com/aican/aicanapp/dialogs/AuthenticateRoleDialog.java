package com.aican.aicanapp.dialogs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aican.aicanapp.R;
import com.aican.aicanapp.fragments.ph.PhCalibFragment;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;

public class AuthenticateRoleDialog extends DialogFragment {

    private static final String FILE_NAME = "user_info.txt";

    EditText userId, passCode;
    Button generate;
    String validPasscode, validUserID;
    String[] lines;
    static AtomicReference<Boolean> success;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_authenticate_role_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userId = view.findViewById(R.id.userId);
        passCode = view.findViewById(R.id.userPwd);
        generate = view.findViewById(R.id.authenticateRole);
<<<<<<< HEAD
=======
        boolean success;
        generate.setOnClickListener(v -> {

            Intent intent = new Intent(requireContext(), PhCalibFragment.class);
            intent.putExtra(userId.getText().toString(), passCode.getText().toString());
            //intent.putExtra("success", success);
            startActivity(intent);
>>>>>>> 998ffc26d23ff0c10cc5846523b07fdea8d17917

     /*   FileInputStream fis = null;
        try {
            fis = getActivity().openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
            }
            lines = sb.toString().split("\\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        success = new AtomicReference<>(false);
        generate.setOnClickListener(v -> {

<<<<<<< HEAD
            Log.d("46513", "gooo");
            //if (isEmailValid() && isPassCodeValid()) {
            validPasscode = passCode.getText().toString();
            validUserID = userId.getText().toString();
                Log.d("46513", validPasscode);
                Log.d("46513", lines[2]);
                Log.d("46513", validUserID);
                Log.d("46513", lines[3]);

                if(validPasscode.equals(lines[2]) && validUserID.equals(lines[3])){
                    Toast.makeText(requireContext(), "GRANTEDDDDD", Toast.LENGTH_SHORT).show();

                   *//* Bundle bundle = new Bundle();
                    bundle.putString("edttext", "From Activity");
                    Fragmentclass fragobj = new Fragmentclass();
                    fragobj.setArguments(bundle);*//*

                    success.set(true);
                    Intent intent = new Intent(requireContext(), PhCalibFragment.class);
                    intent.putExtra(userId.getText().toString(), passCode.getText().toString());
                    intent.putExtra("success", success);
                    //startActivity(intent);
                    dismiss();
                }else{
                    Toast.makeText(requireContext(), "Access Not Granted", Toast.LENGTH_SHORT).show();
                }
           // }
        });*/
    }

    public static AtomicReference<Boolean> getStatus(){
        return success;
=======
                //success = true;
                dismiss();
            }
        });
>>>>>>> 998ffc26d23ff0c10cc5846523b07fdea8d17917
    }

  /*  private boolean isPassCodeValid() {

        if (validName.isEmpty()) {
            Toast.makeText(requireContext(), "Enter Passcode!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isEmailValid() {

        if (!validName.isEmpty()) {

        } else {
            Toast.makeText(requireContext(), "Enter Email Address!", Toast.LENGTH_SHORT).show();
        }
        return true;
    }*/

}