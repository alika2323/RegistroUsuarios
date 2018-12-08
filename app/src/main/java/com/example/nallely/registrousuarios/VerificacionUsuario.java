package com.example.nallely.registrousuarios;

import android.content.Intent;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class VerificacionUsuario extends AppCompatActivity implements View.OnClickListener {
  Button btnVerificar, btnRegistrar, btnActualizar;
  EditText edt_claveUsuario, edt_curp;
  TextView informacion;
  String claveUsuario, curp, idusuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verificacion_usuario);

        btnVerificar = (Button)findViewById(R.id.btnVerificar);
        btnRegistrar = (Button)findViewById(R.id.btnRegistrar);
        btnActualizar = (Button)findViewById(R.id.btnActualizar);


        btnVerificar.setOnClickListener(this);
        btnRegistrar.setOnClickListener(this);


    }

    ///////////////////////
    /* Funcion: Verificar codigo */
    public void verificarUsuario() {
        edt_claveUsuario=(EditText)findViewById(R.id.claveUsuario);
        edt_curp=(EditText)findViewById(R.id.curp);
        claveUsuario=edt_claveUsuario.getText().toString();
        curp=edt_curp.getText().toString();


        ArrayList values=new ArrayList();
        final JSONArray data;
        values.add(claveUsuario);
        values.add(curp);
        values.add("");
        data=new JSONArray( values );


        Thread tr=new Thread(){
            @Override
            public void run() {
                final String resultado= POST("dataResponse","validateUser", data.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            JSONObject res=new JSONObject(resultado);
                            Boolean valor= Boolean.valueOf(res.getString("CODIGO"));
                            System.out.println("respuestita->"+res);
                            if (valor){
                                String datos=res.getString("DATOS");
                                mostrarDatos(datos);

                            }else{
                                Toast.makeText(VerificacionUsuario.this, "no verificado", Toast.LENGTH_SHORT).show();
                                btnActualizar.setVisibility(View.INVISIBLE);
                                /*
                                String valor_error= res.getString("DATOS");
                                Toasty.error(TipoUsuario.this,valor_error, Toast.LENGTH_SHORT,true).show();
                                */
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
    public String POST(String opcion, String action, final String values){
        parameters parameters=new parameters();
        System.out.println("opcion->"+opcion);
        System.out.println("action->"+action);
        System.out.println("valores->"+values);
        String resultPOST="";
        try{
            HttpClient send=new DefaultHttpClient();
            HttpPost post=new HttpPost(parameters.getUrlPOST());
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("csrf_token","token_usuario"));
            params.add(new BasicNameValuePair("opcion",opcion));
            params.add(new BasicNameValuePair("action",action));
            params.add(new BasicNameValuePair("values",values));
            post.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse resp= send.execute(post);
            HttpEntity ent=resp.getEntity();
            resultPOST= EntityUtils.toString(ent);
        }catch (Exception e){}

        return resultPOST;
    }



    /* Funcion: Mostrar datos usuario */
    public void mostrarDatos(String Datos) throws JSONException {
        JSONObject datos = new JSONObject(Datos);
        String nombreUsuario=datos.getString("NOMBRE");
        String aPaterno=datos.getString("APELLIDOPATERNO");
         idusuario=datos.getString("IDUSUARIO");

        informacion=(TextView)findViewById(R.id.informacion);
        informacion.setText("nombre: " + nombreUsuario +
                "apellido: " + aPaterno +
                "idusuario: " + idusuario
        );
        btnActualizar.setVisibility(View.VISIBLE);
        btnActualizar.setOnClickListener(this);
    }


    /* Funcion: Muestra la siguiente activity*/
    public void mostrarSiguiente(final String tipo, final String idusuario ){
        Toast.makeText(this, tipo, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this,FormularioUsuario.class);
        Bundle miBundle=new Bundle();
        miBundle.putString("tipo",tipo);
        miBundle.putString("idusuario",idusuario);
        intent.putExtras(miBundle);
        startActivity(intent);
    }


    /* Controlador de evento onClick*/
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnVerificar:
                verificarUsuario();
                break;
            case R.id.btnRegistrar:
                mostrarSiguiente("registrar","0");
                break;
            case R.id.btnActualizar:
                mostrarSiguiente("actualizar",idusuario);
                break;

        }
    }





}
