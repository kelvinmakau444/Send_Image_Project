package kev.com.send_image.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import kev.com.send_image.R;
import kev.com.send_image.api.ApiService;
import kev.com.send_image.constants.Configs;
import kev.com.send_image.utils.FileUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {
ImageView imageView;
    Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      //  super.onActivityResult(requestCode, resultCode, data);

        imageUri  = data.getData();

        Glide.with(MainActivity.this).load(imageUri).into(imageView);

    }

    public void fetchImage(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,101);

    }

    public void onButtonClick(View view) {
        switch (view.getId()){
            case R.id.base:

                break;
            case R.id.multipart:

                if(imageUri!=null)
                sendImage(imageUri);
                break;
        }
    }

    //send using multipart
    private void sendImage(Uri imageUri) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Uploading..");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Configs.POST_IMAGE_AS_MULTIPART)
                .build();

        // create list of file parts (photo, video, ...)
        List<MultipartBody.Part> parts = new ArrayList<>();
        // create upload service client
        ApiService service = retrofit.create(ApiService.class);

        parts.add(prepareFilePart("image", imageUri));

        RequestBody user = RequestBody.create(MediaType.parse("text/plain"),"user_one");

        //  RequestBody size = createPartFromString(""+parts.size());

        // finally, execute the request
        Call<ResponseBody> call = service.uploadImage(user,parts);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                //  hideProgress();
                progressDialog.dismiss();
                if(response.isSuccessful()) {

                    try {
                        JSONObject jsonObject= new JSONObject(String.valueOf(response.body()));
                        String message = jsonObject.getString("message");
                        String status = jsonObject.getString("status");
                        Log.d("MMMMM: ","status: "+status+" message: "+message);
                        Toast.makeText(MainActivity.this, "status: "+status+"\n"+"message: "+message, Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(MainActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                //  hideProgress();
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    //converting file to parts
    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        // use the FileUtils to get the actual file by uri
        File file = FileUtils.getFile(MainActivity.this, fileUri);

        // create RequestBody instance from file
       RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(MainActivity.this.getContentResolver().getType(fileUri)),
                        file
                );
        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }
}
