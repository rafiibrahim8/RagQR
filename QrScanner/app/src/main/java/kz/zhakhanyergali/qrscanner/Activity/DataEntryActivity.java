package kz.zhakhanyergali.qrscanner.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.regex.Pattern;

import kz.zhakhanyergali.qrscanner.R;
import kz.zhakhanyergali.qrscanner.SQLite.ORM.AppItemORM;

public class DataEntryActivity extends AppCompatActivity {

    TextView sellType;
    TextView qrid;
    EditText name;
    EditText mobile;
    EditText amount;
    Button submit;
    Button cancel;

    Pattern pattern;
    AppItemORM appItemORM;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_entry);

        appItemORM = new AppItemORM();
        pattern = Pattern.compile("(?:\\+88|88)?(?:01[3-9]\\d{8})");

        sellType = findViewById(R.id.de_sell_type);
        qrid = findViewById(R.id.de_qrid);
        name = findViewById(R.id.de_name);
        mobile = findViewById(R.id.de_mobile);
        amount = findViewById(R.id.de_amount);
        submit = findViewById(R.id.de_submit);
        cancel = findViewById(R.id.de_cancel);

        boolean is_editing = getIntent().getBooleanExtra("customer_is_edit", false);
        if(is_editing){
            sellType.setText("Edit Sell");
            sellType.setBackgroundColor(getResources().getColor(R.color.sell_update_bg));
            sellType.setTextColor(getResources().getColor(R.color.sell_update_font));
        }
        else {
            sellType.setText("New Sell");
            sellType.setBackgroundColor(getResources().getColor(R.color.sell_new_bg));
            sellType.setTextColor(getResources().getColor(R.color.sell_new_font));
        }

        qrid.setText(getIntent().getStringExtra("qrid"));
        name.setText(getIntent().getStringExtra("customer_name"));
        amount.setText(getIntent().getStringExtra("customer_amount"));
        mobile.setText(getIntent().getStringExtra("customer_phone"));

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(! pattern.matcher(mobile.getText().toString()).matches()){
                    Toast.makeText(DataEntryActivity.this, "Invalid Mobile Number.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(name.getText().toString().replace(" ","").length() < 1){
                    Toast.makeText(DataEntryActivity.this, "Name can not be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                try{
                    if(Integer.parseInt(amount.getText().toString().replace(" ",""))<1){
                        int y = 10/0;
                    }
                } catch (Exception ex){
                    Toast.makeText(DataEntryActivity.this, "Invalid Amount.", Toast.LENGTH_SHORT).show();
                    return;
                }

                send_req();
            }
        });
    }

    private void send_req(){

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("customer_name", name.getText().toString());
        params.put("customer_phone", mobile.getText().toString());
        params.put("customer_amount", amount.getText().toString());
        params.put("qr_text", getIntent().getStringExtra("qr_text"));
        params.put("seller_token", appItemORM.get(this, "seller_token"));

        progressDialog = ProgressDialog.show(this, "Submitting Sell...", "Please wait...");
        JsonObjectRequest request_json = new JsonObjectRequest("https://gpsd0.ibrahimrafi.me/make_sell", new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialog.dismiss();
                        Toast.makeText(DataEntryActivity.this, "Sell made successfully.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressDialog.dismiss();
                if(volleyError.networkResponse == null){
                    Toast.makeText(DataEntryActivity.this, "Network connection error.", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(DataEntryActivity.this, "An unexpected error occurred. Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Volley.newRequestQueue(this).add(request_json);
    }
}