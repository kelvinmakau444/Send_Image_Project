package kev.com.send_image.api;


import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @Multipart
    @POST("insert_mult_image.php")
    Call<ResponseBody> uploadImage(
            @Part("posted_by") RequestBody posted_by,
            @Part List<MultipartBody.Part> files);

}