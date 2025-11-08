package com.example.ensa_meal;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    AdapterMeals adapterMeals;
    RecyclerView recyclerView;
    ArrayList<Plat>     arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView=findViewById(R.id.recyclerView);
        arrayList=new ArrayList<Plat>();
        adapterMeals=new AdapterMeals(arrayList,this);
        recyclerView.setAdapter(adapterMeals);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        RequestQueue queue=Volley.newRequestQueue(this);
        String url="https://www.themealdb.com/api/json/v1/1/categories.php";
        JsonObjectRequest request=new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray array= response.getJSONArray("categories");
                    for(int i=0;i<array.length();i++)
                    {
                        JSONObject elt=array.getJSONObject(i);
                        Plat p=new Plat(elt.getString("idCategory"),elt.getString("strCategory"),elt.getString("strCategoryThumb"),elt.getString("strCategoryDescription"));
                        arrayList.add(p);

                    }

                    adapterMeals.notifyDataSetChanged();

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();

            }
        });

        queue.add(request);




    }
}