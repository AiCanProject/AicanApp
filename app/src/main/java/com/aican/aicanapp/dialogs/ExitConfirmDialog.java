package com.aican.aicanapp.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.aican.aicanapp.R;

import org.jetbrains.annotations.NotNull;

public class ExitConfirmDialog extends DialogFragment {

    DialogCallbacks callbacks;
    TextView tvYes, tvNo;

    public ExitConfirmDialog(DialogCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_exit_confirm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvYes = view.findViewById(R.id.tvYes);
        tvNo = view.findViewById(R.id.tvNo);

        tvYes.setOnClickListener(v -> {
            callbacks.onYesClicked(getDialog());
        });

        tvNo.setOnClickListener(v -> {
            callbacks.onNoClicked(getDialog());
        });
    }

    public interface DialogCallbacks {
        void onYesClicked(Dialog dialog);

        void onNoClicked(Dialog dialog);
    }

}
