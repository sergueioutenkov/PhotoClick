package com.serguei.mobile.photoclick;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //Vistas
    Button takePictureButton;
    ImageView pictureImageView;

    //Este entero sirve para identificar la request de sacar una foto,
    // si tuviesemos mas de 1 "startActivityforResult()" para distintas cosas,
    // de esta manera podemos diferenciarlas
    private static final int PHOTO_REQUEST = 1;

    //Variable donde se guardara la foto
    private Bitmap photoBitmap;

    //Direccion URI donde se guardara la foto
    private Uri photoUri;

    //Identificador del URI de la foto en el bundle
    private static final String PHOTO_URI = "PHOTO_URI";

    //Identificador de la request de permisos.
    private static final int PERMISSION_PHOTO_STORAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Inicializamos Vistas
        takePictureButton = (Button) findViewById(R.id.takePictureButton);
        pictureImageView = (ImageView) findViewById(R.id.pictureImageView);

        //Chequeamos los permisos de sacar foto
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //No estan dados los permisos, deshabilitamos el boton
            takePictureButton.setEnabled(false);

            //Iniciamos un pedido de permisos para usar la camara y para usar la memoria externa
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_PHOTO_STORAGE);
        }

        //Logica cuando se hace click en el boton
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creamos el intent implicito diciendo q queremos sacar una foto
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Nos Aseguramos q haya una activity que pueda sacar fotos
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Creamos el archivo donde se guardara la foto
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        Log.e("Error", ex.getMessage());
                    }
                    // Continuar solamente si la creacion del archivo fue exitosa
                    if (photoFile != null) {
                        //Obtener el URI del archivo donde se guardara la foto
                        photoUri = FileProvider.getUriForFile(MainActivity.this, "com.serguei.mobile.photoclick.fileprovider", photoFile);

                        //Guardar en el intent el URI, identificado por la constante MediaStore.EXTRA_OUTPUT
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

                        //Lanzamos la activity, y esperamos el resultado
                        startActivityForResult(takePictureIntent, PHOTO_REQUEST);
                    }
                } else {
                    //No hay activities q saquen fotos en el dispositivo
                    Toast.makeText(MainActivity.this, "No hay app de foto", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    //Debemos sobreescribir éste método para capturar la imagen que devuelve la aplicación de fotos
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //Preguntamos si el codigo con el que mandamos la request es el mismo con el cual iniciamos
        //"startActivityForResult()", como dije mas arriba, ésto es para diferenciar por ej de otra
        // request que tengamos para mandar un email
        // El RESULT_OK es simplemente para chequear que no hubo error, se usa ésta constante que pertenece a la clase "Activity"
        //(lo pueden chequear haciendo "ctrl + click" en RESULT_OK)
        if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK) {

            try {
                //Guardamos la foto obtenida del URI en una variable.
                photoBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);

                //Seteamos la foto al ImageView
                pictureImageView.setImageBitmap(photoBitmap);

                //Mostramos un mensaje con el URI de la foto
                Toast.makeText(MainActivity.this, "URI de la imagen es: " + photoUri.toString(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Log.e("Error", e.getMessage());
            }

        }
    }

    //Metodo de ayuda que crea un archivo vacío
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefijo */
                ".jpg",         /* suffijo */
                storageDir      /* directorio */
        );

        return image;
    }

    /**
     * Callback que se ejecuta cuando el usuario acepta o no los permisos q se le piden
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_PHOTO_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                //Se dieron los permisos, habilitamos el boton
                takePictureButton.setEnabled(true);
            }
        }
    }

    /**
     * Debemos guardar el URI de la foto, ya que se pierde en le ciclo de vida de la activity
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(PHOTO_URI, photoUri);
    }

    /**
     * Recuperamos el URI
     *
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        photoUri = savedInstanceState.getParcelable(PHOTO_URI);

    }
}
