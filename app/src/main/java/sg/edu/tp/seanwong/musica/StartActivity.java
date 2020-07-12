 package sg.edu.tp.seanwong.musica;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

 public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }
    public void handleTap(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
