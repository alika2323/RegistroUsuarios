package com.example.nallely.registrousuarios;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class VerificacionProyecto extends AppCompatActivity implements View.OnClickListener {
    EditText codigoVerificar;
    Button btnVerificar, btnContinuar;
    String codigo;
    private AlertDialog alertDialog;
    private String resultado;
    TextView txtpro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verificacion_proyecto);


        /* Verificar codigo projecto */
        btnVerificar=(Button)findViewById(R.id.btn_verificar);
        btnContinuar=(Button)findViewById(R.id.btnContinuar);
        btnVerificar.setOnClickListener(this);
    }




    ///////////////////////
     /* Funcion: Verificar codigo */
    public void verificar() {
        Thread tr = new Thread(){
            @Override
            public void run() {
                codigoVerificar = (EditText)findViewById(R.id.codigo_verifica);
                codigo = codigoVerificar.getText().toString();
                final String resultado= POST("dataResponse","validateProject", codigo );
                System.out.println("RespuestaVAlidacion"+resultado);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            JSONObject res=new JSONObject(resultado);
                            Boolean valor= Boolean.valueOf(res.getString("CODIGO"));

                            String datos=res.getString("DATOS");
                            if (valor){
                                mostrarDatos(datos);
                            }else{
                                Toast.makeText(VerificacionProyecto.this, datos, Toast.LENGTH_SHORT).show();
                                btnContinuar.setVisibility(View.INVISIBLE);
                             }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        };

        tr.start();


    }



    /* Funcion:  Enviar y validar clave proyecto a WS*/
    public String POST(String opcion, String action, String claveproyecto){
        parameters parameters=new parameters();
        String resultPOST="";
        try{
            HttpClient send=new DefaultHttpClient();
            HttpPost post=new HttpPost(parameters.getUrlPOST());
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("csrf_token","token_proyecto"));
            params.add(new BasicNameValuePair("opcion",opcion));
            params.add(new BasicNameValuePair("action",action));
            params.add(new BasicNameValuePair("values",claveproyecto));
            post.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse resp= send.execute(post);
            HttpEntity ent=resp.getEntity();
            resultPOST= EntityUtils.toString(ent);
        }catch (Exception e){}

        return resultPOST;
    }



    /* Funcion: Mostrar datos proyectos */
    public void mostrarDatos(String Datos) throws JSONException {
        JSONObject datos = new JSONObject(Datos);
        String idproyecto=datos.getString("IDPROGRAMA");
        String nomProyecto=datos.getString("NOMBREPROGRAMA");

        txtpro=(TextView)findViewById(R.id.cambiarid);
        txtpro.setText("idproyecto: " + idproyecto +
                        "nombre: " + nomProyecto
        );

        btnContinuar.setVisibility(View.VISIBLE);
        btnContinuar.setOnClickListener(this);
    }

    /* Funcion: Muestra la siguiente aqctivity*/
    public void mostrarSiguiente(){
        Intent intent = new Intent(this,VerificacionUsuario.class);
        Bundle miBundle=new Bundle();
        miBundle.putString("idProyecto",codigo);
        intent.putExtras(miBundle);
        startActivity(intent);
    }

    /* Controlador de evento onClick*/
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_verificar:
                verificar();
                break;
            case R.id.btnContinuar:
                mostrarSiguiente();
                break;
        }
    }




}
