package com.example.nallely.registrousuarios;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.media.audiofx.AutomaticGainControl;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FormularioUsuario extends AppCompatActivity implements View.OnClickListener {

    Button btnRegistar;
    CharSequence imei_dato, numTelefonico_dato;
    FloatingActionButton btnFoto;
    ImageView imagen;
    Spinner condicion;
    String tipoForm, path, nombreImagen = "", resultado, idusuario;
    TextView txt_imei, txt_numTelefonico, tipo;
    TelephonyManager imei, numTelefonico;
    private final String RUTA_IMAGEN = "Proyecto/users";
    final int COD_FOTO = 20;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_usuario);


        /* Renderizando opciones de Spinner */
        String[] opciones = {"SI", "NO"};
        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, opciones);
        condicion = (Spinner) findViewById(R.id.condicionEntrega);
        condicion.setAdapter(adapter);


        /* AÑADIENDO DATOS AUTOMATIZADOS */
        obtener_imei();
        obtener_numTelefonico();

        Bundle miBundle = this.getIntent().getExtras();
        if (miBundle != null) {
            tipoForm = miBundle.getString("tipo");
            idusuario = miBundle.getString("idusuario");
        }


        /*  Toma de Fotografia */
        btnFoto = (FloatingActionButton) findViewById(R.id.btn_fotoUser);
        btnFoto.setOnClickListener(this);


        /* Traer datos */
        if (tipoForm.equals("actualizar")) {
            traeDatosUser(idusuario);
        }


        /* Agregando evento a btnRegistrar*/
        btnRegistar = (Button) findViewById(R.id.btnRegistrar);
        btnRegistar.setOnClickListener(this);

    }

    private void traeDatosUser(String idusuario) {
        ArrayList values=new ArrayList();
        final JSONArray data;
        values.add("");
        values.add("");
        values.add(idusuario);
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
                                cargarDatosFormulario(datos);

                            }else{
                                Toast.makeText(FormularioUsuario.this, "no se pudo taer datos", Toast.LENGTH_SHORT).show();
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


    /* Funcion: Mostrar datos usuario */
    public void cargarDatosFormulario(String Datos) throws JSONException {
        JSONObject datos = new JSONObject(Datos);


        String amaterno=datos.getString("APELLIDOMATERNO");
        if (amaterno.equals("null")){
            amaterno="x";
        }

        String curp=datos.getString("CURP");
        if (curp.equals("null")){
            curp="x";
        }

        String telPersonal=datos.getString("TELEFONO_C");
        if (telPersonal.equals("null")){
            telPersonal="";
        }

        String telTrabajo=datos.getString("TELEFONO_T");
        if (telTrabajo.equals("null")){
            telTrabajo="";
        }

        ((EditText)findViewById(R.id.nombre)).setText(datos.getString("NOMBRE"));
        ((EditText) findViewById(R.id.apaterno)).setText(datos.getString("APELLIDOPATERNO"));
        ((EditText) findViewById(R.id.amaterno)).setText(amaterno);
        ((EditText) findViewById(R.id.curp)).setText(curp);
        ((EditText) findViewById(R.id.tel_contacto)).setText(telPersonal);
        ((EditText) findViewById(R.id.tel_trabajo)).setText(telTrabajo);


        Toast.makeText(this, "aqui se cambiaran los datos", Toast.LENGTH_SHORT).show();

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
    ///////////////////////
    private void obtener_imei() { /* Funcion: obtener imei */
        txt_imei = (TextView) findViewById(R.id.txt_imei);
        imei = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "SIN PERMISOS", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 225);
        }
        imei_dato = imei.getDeviceId();

        final StringBuilder builder = new StringBuilder();
        builder.append("IMEI:  ").append(imei_dato).append("\n");
        txt_imei.setText(builder.toString());
    }


    private void obtener_numTelefonico() {  /* Funcion: obtener numero telefonico */
        txt_numTelefonico = (TextView) findViewById(R.id.txt_numTelefonico);
        numTelefonico = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 225);
        }

        numTelefonico_dato = numTelefonico.getLine1Number();
        final StringBuilder builder = new StringBuilder();
        builder.append("telefono:  ").append(numTelefonico_dato).append("\n");
        txt_numTelefonico.setText(builder.toString());
    }


    private void tomarFotografia() {
        File fileImagen = new File(Environment.getExternalStorageDirectory(), RUTA_IMAGEN);
        Boolean iscreada = fileImagen.exists();

        if (iscreada == false) {
            iscreada = fileImagen.mkdirs();
        }

        if (iscreada == true) {
            nombreImagen = (System.currentTimeMillis() / 1000) + ".jpg";

        }

        path = Environment.getExternalStorageDirectory() + File.separator + RUTA_IMAGEN + File.separator + nombreImagen;
        File imagen = new File(path);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imagen));
        startActivityForResult(intent, COD_FOTO);
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            MediaScannerConnection.scanFile(this, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String path, Uri uri) {
                    Log.i("RUTA DE ALMACENAMIENTO", "PATH:" + path);
                }
            });

            imagen = (ImageView) findViewById(R.id.fotoUser);
            Bitmap bitmap = BitmapFactory.decodeFile(path);

            imagen.setImageBitmap(bitmap);

        }
    }

    private void registrarActualizarDatos(String tipoForm) {
        String nombre = ((EditText) findViewById(R.id.nombre)).getText().toString();
        if (!TextUtils.isEmpty(nombre)) {
            ArrayList values = new ArrayList();
            final JSONArray data;

            values.add(tipoForm);
            values.add(nombre);
            values.add(((EditText) findViewById(R.id.apaterno)).getText());

            data = new JSONArray(values);

            Thread tr = new Thread() {
                @Override
                public void run() {
                    //super.run();
                    System.out.println("el path es:" + path);
                    POST(path, "dataResponse", "userRegister", data.toString());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("correcto");
                        }
                    });
                }
            };
            tr.start();
        } else {
            Toast.makeText(FormularioUsuario.this, "NO HAS INGRESADO LOS DATOS", Toast.LENGTH_LONG).show();
        }

    }


    /*  C-ENVIAR DATOS  */
    public void POST(final String filename, final String opcion, final String action, final String values) {
        final String boundary = "***";
        parameters parameters = new parameters();
        final String url = parameters.getUrlPOST();

        try {

            HttpClient send = new DefaultHttpClient();
            HttpPost post = new HttpPost(url);
            final String uploadId = UUID.randomUUID().toString();

            //Creating a multi part request
            MultipartUploadRequest request = new MultipartUploadRequest(getApplicationContext(), uploadId, url);

            if (filename != null){
                request.addHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
                request.addFileToUpload(filename, "imagen");
            }else{
                request.addFileToUpload("", "imagen");
            }


            request.addParameter("csrf_token", "token");
            request.addParameter("opcion", opcion);
            request.addParameter("action", action);
            request.addParameter("values", values);

            request.setNotificationConfig(new UploadNotificationConfig());
            request.setMaxRetries(2);
            request.setDelegate(new UploadStatusDelegate() {


                @Override
                public void onProgress(UploadInfo uploadInfo) {
                    System.out.println("AVANCE-> " + uploadInfo.getProgressPercent());
                }

                @Override
                public void onError(UploadInfo uploadInfo, Exception exception) {
                    Log.d("On error", String.valueOf(exception));
                    Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCompleted(UploadInfo uploadInfo, ServerResponse serverResponse) {

                    resultado = serverResponse.getBodyAsString().toString();
                    System.out.println("resultado completo" + resultado);

                    try {
                        JSONObject res = new JSONObject(resultado);
                        resultado = res.getString("DATOS");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    if (resultado.equals("true")) {
                        Toast.makeText(FormularioUsuario.this, "SE REGISTRO CORRECTAMENTE", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(FormularioUsuario.this, "ERROR AL REGISTRAR", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onCancelled(UploadInfo uploadInfo) {

                }
            });
            request.startUpload(); //Starting the upload

        } catch (Exception exc) {
            System.out.println(exc.getMessage());
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    /* Controlador de evento onClick*/
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_fotoUser:
                tomarFotografia();
                break;
            case R.id.btnRegistrar:

                registrarActualizarDatos(tipoForm);
                break;

        }
    }


}
