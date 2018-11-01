package com.originalstocksllc.himanshuraj.thenewschannel.Fragments;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.originalstocksllc.himanshuraj.thenewschannel.ConnectionDetector;
import com.originalstocksllc.himanshuraj.thenewschannel.Function;
import com.originalstocksllc.himanshuraj.thenewschannel.R;
import com.originalstocksllc.himanshuraj.thenewschannel.VerticalRecyclerAdapter;
import com.yarolegovich.discretescrollview.DSVOrientation;
import com.yarolegovich.discretescrollview.DiscreteScrollView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class MediaFragment extends Fragment {

    public static final String API_KEY = "44a3763104b94c76944260fe614a201a";
    static final int REQUEST_LOCATION = 1;
    static final String KEY_TITLE = "title";
    static final String KEY_DESCRIPTION = "description";
    static final String KEY_URL = "url";
    static final String KEY_URLTOIMAGE = "urlToImage";
    static final String KEY_PUBLISHEDAT = "publishedAt";
    private DiscreteScrollView recyclerView;
    private ProgressBar progressBar;
    private ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
    private String category = "entertainment";
    private View view;
    private ConnectionDetector connectionDetector;
    private VerticalRecyclerAdapter verticalRecyclerAdapter;

    public MediaFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Paper.init(context);
        connectionDetector = new ConnectionDetector(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_media, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.loader);

        ArrayList<HashMap<String, String>> arrayList = Paper.book().read("dataListM", null);
        VerticalRecyclerAdapter recyclerAdapter = new VerticalRecyclerAdapter(getActivity(), arrayList);
        if (!connectionDetector.isConnected()){
            progressBar.setVisibility(View.INVISIBLE);
            recyclerView.setAdapter(recyclerAdapter);
        }else if (Function.isNetworkAvailable(getContext())){
            DownloadMediaNews downloadMediaNews = new DownloadMediaNews();
            downloadMediaNews.execute();
        }
        // Inflate the layout for this fragment
        return view;
    }

    public class DownloadMediaNews extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String xml = "";

            String urlParameters = "";
            xml = Function.excuteGet("https://newsapi.org/v2/top-headlines?country=in&category=" + category + "&apiKey=" + API_KEY, urlParameters);
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {
            if (xml.length() > 10) { // checking if not empty

                progressBar.setVisibility(View.VISIBLE);
                try {
                    JSONObject jsonResponse = new JSONObject(xml);
                    JSONArray jsonArrays = jsonResponse.optJSONArray("articles");

                    for (int i = 0; i < jsonArrays.length(); i++) {
                        JSONObject jsonObject = jsonArrays.getJSONObject(i);
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(KEY_TITLE, jsonObject.optString(KEY_TITLE).toString());
                        map.put(KEY_DESCRIPTION, jsonObject.optString(KEY_DESCRIPTION).toString());
                        map.put(KEY_URL, jsonObject.optString(KEY_URL).toString());
                        map.put(KEY_URLTOIMAGE, jsonObject.optString(KEY_URLTOIMAGE).toString());
                        map.put(KEY_PUBLISHEDAT, jsonObject.optString(KEY_PUBLISHEDAT).toString());
                        dataList.add(map);

                        if (connectionDetector.isConnected()){
                            Paper.book().write("dataListM", dataList);
                        }

                    }
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), "Unexpected error", Toast.LENGTH_SHORT).show();
                }

                progressBar.setVisibility(View.INVISIBLE);
                verticalRecyclerAdapter = new VerticalRecyclerAdapter(getActivity(), dataList);
                //recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                recyclerView.setHasFixedSize(true);
                recyclerView.setOrientation(DSVOrientation.VERTICAL);
                recyclerView.setSlideOnFling(true);
                //recyclerView.setPaddingRelative(0,0,0,0);
               /* recyclerView.setItemTransformer(new ScaleTransformer.Builder()
                        .setMaxScale(1.05f)
                        .setMinScale(0.8f)
                        .setPivotX(Pivot.X.CENTER) // CENTER is a default one
                        .setPivotY(Pivot.Y.BOTTOM) // CENTER is a default one
                        .build());*/
                recyclerView.setOverScrollEnabled(true);
                recyclerView.setItemTransitionTimeMillis(200);
                if (connectionDetector.isConnected()) {
                    recyclerView.setAdapter(verticalRecyclerAdapter);
                }
            } else {
                Toast.makeText(getActivity(), "No news found", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
