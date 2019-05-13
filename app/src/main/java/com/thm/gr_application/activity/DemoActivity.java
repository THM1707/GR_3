package com.thm.gr_application.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.iid.FirebaseInstanceId;
import com.thm.gr_application.R;

public class DemoActivity extends AppCompatActivity {

    private static final String TAG = DemoActivity.class.getName();
    private TextView mTokenText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        mTokenText = findViewById(R.id.tv_token);
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.d(TAG, "onComplete: ", task.getException());
            }
            if (task.getResult() != null) {
                String token = task.getResult().getToken();
                Log.d(TAG, token);
                mTokenText.setText(token);
            }
        });
    }
}
