package interapp.erapps.com.connect4x;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


/**Created and Modified by Enrique Ramirez*/
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btn_inicio, btn_salir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_inicio = findViewById(R.id.btn_inicio);
        btn_salir = findViewById(R.id.btn_Salir);
        btn_inicio.setOnClickListener(this);
        btn_salir.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_inicio: goActivity(ActivityGame.class); break;
            case R.id.btn_Salir: System.exit(1); break;
        }
    }
    private void goActivity(Class<? extends AppCompatActivity> c4){
        startActivity(new Intent(MainActivity.this, c4));
    }
}
