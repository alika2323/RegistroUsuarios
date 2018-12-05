package com.example.nallely.registrousuarios;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class FormularioUsuario extends AppCompatActivity {
    String tipo;
    TextView tipoForm;
    Spinner condicion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_usuario);


        tipoForm = (TextView) findViewById(R.id.tipoForm);

        /* Recibiendo datos de activity anterior*/
        Bundle miBundle = this.getIntent().getExtras();
        if (miBundle != null) {
            tipo = miBundle.getString("tipo");
            tipoForm.setText(tipo);
            Toast.makeText(this, "tipo form", Toast.LENGTH_SHORT).show();
        }


        /* Spinner de condicion telefono*/
        String[] opciones={"SI","NO"};
        ArrayAdapter adapter= new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,opciones);

        condicion=(Spinner)findViewById(R.id.condicionEntrega);
        condicion.setAdapter(adapter);

    }
}
