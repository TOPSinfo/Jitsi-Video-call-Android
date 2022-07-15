package com.app.demo.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;


import com.app.demo.R;

import java.util.Objects;

public class CustomProgressDialog extends Dialog {


    public CustomProgressDialog(@NonNull Context context) {
        super(context, R.style.Theme_Dialog);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_progress);
        try {
            Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
