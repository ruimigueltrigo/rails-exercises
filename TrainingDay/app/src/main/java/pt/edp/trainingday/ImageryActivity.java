package pt.edp.trainingday;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import pt.edp.trainingday.Dialogs.GeoTagDisDialog;

public class ImageryActivity extends AppCompatActivity {

    private ImageButton ib_foto1, ib_foto2;
    private TextView tv_lat_foto1, tv_lng_foto1;
    private final static int REQUEST_CODE = 200;
    private boolean foto_1;
    private double latitude, longitude;
    private Button bu_enviar_fotos;
    private Bitmap foto1, foto2;
    private String nome, data_tirada, f1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagery);

        //tem câmara?
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            ib_foto1.setEnabled(false);
            ib_foto2.setEnabled(false);
            Toast.makeText(this, "O dispositivo não possui câmara", Toast.LENGTH_LONG).show();
        }

        tv_lat_foto1 = (TextView) findViewById(R.id.textView_latitude_foto1);
        tv_lng_foto1 = (TextView) findViewById(R.id.textView_longitude_foto1);

        ib_foto1 = (ImageButton) findViewById(R.id.imageButton_foto1);
        ib_foto1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                foto_1 = true;
                startActivityForResult(intent, REQUEST_CODE);

            }
        });

        ib_foto2 = (ImageButton) findViewById(R.id.imageButton_foto2);
        ib_foto2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                foto_1 = false;
                startActivityForResult(intent, REQUEST_CODE);

            }
        });

        bu_enviar_fotos = (Button) findViewById(R.id.button_enviar_fotos);
        bu_enviar_fotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (foto1 != null) {
                    String imjson = JSONBuilder(foto1);
                    Intent intent = new Intent(ImageryActivity.this, WhichJSONToPOSTActivity.class);
                    intent.putExtra("imjson", imjson);
                    intent.putExtra("base64", f1);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                Bundle extras = data.getExtras();
                Bitmap bmp = (Bitmap) extras.get("data");

                if (foto_1) {
                    ib_foto1.setImageBitmap(bmp);
                    foto1 = bmp;
                } else {
                    ib_foto2.setImageBitmap(bmp);
                }
                GetGeoTag();

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancelada", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void GetGeoTag() {
        String[] columns = {MediaStore.Images.ImageColumns.LATITUDE,
                MediaStore.Images.ImageColumns.LONGITUDE,
                MediaStore.Images.ImageColumns.TITLE,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.DATE_TAKEN
        };

        final String orderBy = MediaStore.Images.ImageColumns.DATE_MODIFIED + " DESC";

        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);

        cursor.moveToPosition(0);
        //nome = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
        data_tirada = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN));
        latitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Images.ImageColumns.LATITUDE));
        longitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Images.ImageColumns.LONGITUDE));

        if (latitude != 0.0 && longitude != 0.0) {
            tv_lat_foto1.setText(String.valueOf(latitude));
            tv_lng_foto1.setText(String.valueOf(longitude));
        } else {
            GeoTagDisDialog gtdd = new GeoTagDisDialog();
            gtdd.show(getSupportFragmentManager(), "gtdd");
        }
    }

    private String JSONBuilder(Bitmap foto1) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        foto1.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        f1 = encoded;

        String imjson = "{\n" +
                        "\t\"foto\": {\n" +
                        "\t\t\"nome\": \"" + nome +"\",\n" +
                        "\t\t\"lat\": " + latitude +",\n" +
                        "\t\t\"lng\": "+ longitude +",\n" +
                        "\t\t\"data_tirada\": "+ data_tirada +",\n" +
                        "\t\t\"imagem\": \"" + f1 + "\",\n" +
                        "\t}\n" +
                        "}";

        return imjson;
    }
}