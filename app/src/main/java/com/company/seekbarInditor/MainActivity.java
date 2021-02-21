package com.company.seekbarInditor;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.company.myseekbar.AppSeekBar;

public class MainActivity extends AppCompatActivity {

    AppSeekBar appSeekBar;
    ProgressBar progress_bar;
    TextView tvContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        init();

    }

    public void init(){
        appSeekBar = (AppSeekBar) this.findViewById(R.id.app_seekBar);
        progress_bar = (ProgressBar)this.findViewById(R.id.progress_bar);
        tvContinue = (TextView)this.findViewById(R.id.tv_continue);
        tvContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAG","continue");
            }
        });
        appSeekBar.setOnProgressChangedListener(new AppSeekBar.OnProgressChangedListener(){
            @Override
            public void onProgressChanged(AppSeekBar AppSeekBar, int  progress , float pr, boolean fromUser) {
                float realProgress = (progress - 18)/30.0f * 100;
                progress_bar.setProgress((int)realProgress);
            }

            @Override
            public void getProgressOnActionUp(AppSeekBar AppSeekBar, int progress,float pr) {

            }

            @Override
            public void getProgressOnFinally(AppSeekBar AppSeekBar, int progress,float pr, boolean fromUser) {

            }
        });


    }
}