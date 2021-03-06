package cse190.cookpal;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

public class AssistantRecipeListActivity extends BaseDrawerActivity {

    ArrayList<String> recipeList;
    ArrayList<String> recipeImageList;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    protected void onRestart() {
        Log.d("recipelist", "recipelist onrestart");
        recipeList = new ArrayList<String>();
        recipeImageList = new ArrayList<String>();
        new LongOperation().execute(RecipeList.SERVER_RECIPE_LIST_REQUEST_URL);
        populateListView();
        super.onRestart();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant_recipe_list);

        //initialize image lazy loader
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
        new LongOperation().execute(RecipeList.SERVER_RECIPE_LIST_REQUEST_URL);
    }

    // Note: c-p'ed from Kevin's RecipeList.java
    private class LongOperation  extends AsyncTask<String, Void, Void> {

        // Required initialization

        private final HttpClient Client = new DefaultHttpClient();
        private String jsonReturnString;
        private String jsonRetrievalErrorString = null;
        private ProgressDialog Dialog = new ProgressDialog(AssistantRecipeListActivity.this);
        String data = "";

        protected void onPreExecute() {
            Log.d("recipelist", "onPreExecute.....");
            //Start Progress Dialog (Message)
            recipeList = new ArrayList<String>();
            recipeImageList = new ArrayList<String>();
            Dialog.setMessage("Please wait..");
            Dialog.show();
        }

        // Call after onPreExecute method
        protected Void doInBackground(String... urls) {
            Log.d("recipelist", "do in background.....");
            /************ Make Post Call To Web Server ***********/
            BufferedReader reader = null;

            // Send data
            try {

                // Defined URL  where to send data
                URL url = new URL(urls[0]);

                // Send POST data request

                URLConnection conn = url.openConnection();

                // Get the server response

                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line + "");
                }

                // Append Server Response To jsonReturnString String
                jsonReturnString = sb.toString();
            } catch (Exception ex) {
                jsonRetrievalErrorString = ex.getMessage();
            } finally {
                try {

                    reader.close();
                } catch (Exception ex) {
                }
            }

            /*****************************************************/
            return null;
        }

        protected void onPostExecute(Void unused) {
            Log.d("recipelist", "onpostexecute.....");
            // Close progress dialog
            Dialog.dismiss();

            //error
            if (jsonRetrievalErrorString != null) {
                Log.d("recipelist", "error.....: " + jsonRetrievalErrorString);
                Context context = getApplicationContext();
                CharSequence text = "Error retrieving recipes";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            } else {
                /****************** Start Parse Response JSON Data *************/
                Log.d("recipeList activity", "json return string: " + jsonReturnString);
                JSONObject jsonResponse;
                try {

                    /****** Creates a new JSONObject with name/value mappings from the JSON string. ********/
                    jsonResponse = new JSONObject(jsonReturnString);

                    Iterator jsonKeysIterator = jsonResponse.keys();

                    while(jsonKeysIterator.hasNext()) {
                        String key = jsonKeysIterator.next().toString();
                        JSONArray recipeArray = (JSONArray) jsonResponse.get(key);
                        for (int i = 0; i < recipeArray.length(); i++) {
                            String recipeName = ((JSONObject)recipeArray.get(i)).get("recipe name").toString();
                            String recipeImageURL = ((JSONObject)recipeArray.get(i)).get("image").toString();
                            Log.d("recipeList activity", "recipeName from db: " + recipeName);
                            recipeList.add(recipeName);
                            recipeImageList.add(recipeImageURL);
                        }
                    }
                    populateListView();
                    /****************** End Parse Response JSON Data *************/

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void populateListView() {
        //Create list of items

        ArrayAdapter<String> recipeListAdapter = new RecipeListAdapter(recipeList, recipeImageList);
        ListView list = (ListView) findViewById(R.id.assistantRecipeListView);
        list.setAdapter(recipeListAdapter);

    }

    private class RecipeListAdapter extends ArrayAdapter<String> {
        ImageLoader imageLoader;
        ArrayList<String> urls;

        public RecipeListAdapter() {
            super(AssistantRecipeListActivity.this, R.layout.assistant_recipe_listview_entry, recipeList);
            imageLoader = imageLoader.getInstance();
        }

        public RecipeListAdapter(ArrayList<String> recipeLists, ArrayList<String> recipeImageURLs) {
            super(AssistantRecipeListActivity.this, R.layout.assistant_recipe_listview_entry, recipeLists);
            imageLoader = imageLoader.getInstance();
            urls = recipeImageURLs;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.assistant_recipe_listview_entry, parent, false);
            }
            final String recipeName = recipeList.get(position);

            TextView currTextView = (TextView) convertView.findViewById(R.id.assistantRecipeTitle);
            currTextView.setText(recipeName);
            currTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new PopulateRecipeOperation().execute(AccountActivity.getFbId(), recipeName, "RECIPEACTIVITY");
                }
            });
            ImageView currImageView = (ImageView) convertView.findViewById(R.id.assistantRecipeEntryImageView);
            //if the entry does not have an url, we use local resources to avoid slowdowns
            if (urls.get(position).length() == 0) {
                int id = getResources().getIdentifier("cse190.cookpal:drawable/placeholder", null, null);
                currImageView.setImageResource(id);
            }
            //else we grab according to the specified url
            else {
                imageLoader.displayImage(urls.get(position), currImageView);
            }

            return convertView;
        }
    }

    /////////////////////////////////////////////Start JSON Retrieval code///////////////
    //TODO: refactor into util class
    private class PopulateRecipeOperation  extends AsyncTask<String, Void, Void> {

        // Required initialization

        private final HttpClient Client = new DefaultHttpClient();
        private String imgReturnString;
        private String instructionsReturnString;
        private String ingredientsReturnString;
        private String instructionsRetrievalErrorString = null;
        private String ingredientsRetrievalErrorString = null;
        private String imgRetrievalErrorString = null;
        private String recipeName;
        private String imgUrl;
        private ProgressDialog Dialog = new ProgressDialog(AssistantRecipeListActivity.this);
        BufferedReader reader = null;
        String data = "";

        protected void onPreExecute() {
            Log.d("recipelist", "onPreExecute.....");
            //Start Progress Dialog (Message)
            recipeList = new ArrayList<String>();
            Dialog.setMessage("Please wait..");
            Dialog.show();
        }

        // Call after onPreExecute method
        //fb_id , recipename, nextactivity
        protected Void doInBackground(String... params) {
            Log.d("recipelist", "do in background.....");
            /************ Make Post Call To Web Server ***********/

            //retrieve img url
            try {
                // Defined URL  where to send data
                String serverUrlString = "http://ec2-54-69-39-93.us-west-2.compute.amazonaws.com:8080/request_handler.jsp?";
                String selectInstructionsUrlString = serverUrlString + "fb_id=" + params[0] + "&r_name=" + URLEncoder.encode(params[1],"UTF-8") + "&filter=" + "select_recipes";
                URL selectInstructionsUrl = new URL(selectInstructionsUrlString);

                // Send POST data request

                URLConnection conn = selectInstructionsUrl.openConnection();

                // Get the server response

                BufferedReader reader;
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line + "");
                }

                imgReturnString = sb.toString();
            } catch (Exception ex) {
                imgRetrievalErrorString = ex.getMessage();
            } finally {
                try {
                    reader.close();
                } catch (Exception ex) {
                }
            }

            // retrieve instruction
            try {
                recipeName = params[1];
                // Defined URL  where to send data
                String serverUrlString = "http://ec2-54-69-39-93.us-west-2.compute.amazonaws.com:8080/request_handler.jsp?";
                String selectInstructionsUrlString = serverUrlString + "fb_id=" + params[0] + "&r_name=" + URLEncoder.encode(params[1],"UTF-8") + "&filter=" + "select_instruction";
                URL selectInstructionsUrl = new URL(selectInstructionsUrlString);

                // Send POST data request

                URLConnection conn = selectInstructionsUrl.openConnection();

                // Get the server response

                BufferedReader reader;
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line + "");
                }

                instructionsReturnString = sb.toString();
            } catch (Exception ex) {
                instructionsRetrievalErrorString = ex.getMessage();
            } finally {
                try {

                    reader.close();
                } catch (Exception ex) {
                }
            }
            // retrieve ingredients
            try {

                // Defined URL  where to send data
                String serverUrlString = "http://ec2-54-69-39-93.us-west-2.compute.amazonaws.com:8080/request_handler.jsp?";
                String selectIngredientsUrlString = serverUrlString + "fb_id=" + params[0] + "&r_name=" + URLEncoder.encode(params[1], "UTF-8") + "&filter=" + "select_ingredient";
                URL selectIngredientsUrl = new URL(selectIngredientsUrlString);
                // Send POST data request

                URLConnection conn = selectIngredientsUrl.openConnection();

                // Get the server response

                BufferedReader reader;
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line + "");
                }

                ingredientsReturnString = sb.toString();
            } catch (Exception ex) {
                ingredientsRetrievalErrorString = ex.getMessage();
            } finally {
                try {

                    reader.close();
                } catch (Exception ex) {
                }
            }
            /*****************************************************/
            return null;
        }

        protected void onPostExecute(Void unused) {
            Log.d("recipelist", "onpostexecute.....");
            Log.d("recipeList activity", "instr json return string: " + instructionsReturnString);
            Log.d("recipeList activity", "ingr json return string: " + ingredientsReturnString);
            // Close progress dialog
            Dialog.dismiss();

            ArrayList<Step> stepList = new ArrayList<Step>();
            ArrayList<Ingredients> ingredientsList = new ArrayList<Ingredients>();
            Recipe clickedRecipe;

            if (imgRetrievalErrorString != null) {

            } else {
                /****************** Start Parse Response JSON Data *************/
                Log.d("recipeList activity", "json return string: " + imgReturnString);
                JSONObject jsonResponse;
                try {
                    JSONObject imgJsonResponse = new JSONObject(imgReturnString);
                    Iterator jsonKeysIterator = imgJsonResponse.keys();

                    while(jsonKeysIterator.hasNext()) {
                        String key = jsonKeysIterator.next().toString();
                        JSONArray recipeArray = (JSONArray) imgJsonResponse.get(key);
                        for (int i = 0; i < recipeArray.length(); i++) {
                            String recipeImageURL = ((JSONObject)recipeArray.get(i)).get("image").toString();
                            imgUrl = recipeImageURL;
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            //error
            if (instructionsRetrievalErrorString != null) {

            } else {

                try {
                    JSONObject jsonResponse = new JSONObject(instructionsReturnString);
                    Iterator jsonKeysIterator = jsonResponse.keys();

                    while (jsonKeysIterator.hasNext()) {
                        String key = jsonKeysIterator.next().toString();
                        JSONArray instructionsArray = (JSONArray) jsonResponse.get(key);
                        // public Step(String title, String desc, int hours, int minutes, int stepNum)
                        for (int i = 0; i < instructionsArray.length(); i++) {
                            int stepNumber = Integer.parseInt(((JSONObject) instructionsArray.get(i)).get("step number").toString());
                            String title = ((JSONObject) instructionsArray.get(i)).get("instruction").toString();
                            String description = ((JSONObject) instructionsArray.get(i)).get("description").toString();
                            int hours = Integer.valueOf( ((JSONObject) instructionsArray.get(i)).get("hours").toString() );
                            int minutes = Integer.valueOf(((JSONObject) instructionsArray.get(i)).get("minutes").toString());

                            //create new Step
                            Step step = new Step(title, description, hours, minutes, stepNumber);
                            stepList.add(step);
                            Log.d("recipeList activity", "step params: " + step.toStringDescription());
                        }
                    }
                }
                catch(JSONException e) {

                }
                /****************** Start Parse Response JSON Data *************/

                JSONObject jsonResponse;

            }

            //error
            if (ingredientsRetrievalErrorString != null) {

            } else {
                /****************** Start Parse Response JSON Data *************/
                Log.d("recipeList activity", "json return string: " + ingredientsReturnString);
                JSONObject jsonResponse;
                try {
                    JSONObject ingredientsJsonResponse = new JSONObject(ingredientsReturnString);
                    Iterator jsonKeysIterator = ingredientsJsonResponse.keys();

                    while (jsonKeysIterator.hasNext()) {
                        String key = jsonKeysIterator.next().toString();
                        JSONArray instructionsArray = (JSONArray) ingredientsJsonResponse.get(key);
                        // public Step(String title, String desc, int hours, int minutes, int stepNum)
                        for (int i = 0; i < instructionsArray.length(); i++) {
                            String quantity =  ((JSONObject) instructionsArray.get(i)).get("quantity").toString();
                            String ingr = ((JSONObject) instructionsArray.get(i)).get("ingredient").toString();
                            Ingredients ingredient = new Ingredients(ingr,quantity);


                            ingredientsList.add(ingredient);
                            Log.d("recipeList activity", "ingredient params: " + ingredient.toString());
                        }
                    }
                }
                catch(JSONException e) {

                }

            }

            Recipe recipe = new Recipe(recipeName,imgUrl,stepList,ingredientsList);
            Log.d("","steplistsize direct : " + stepList.size());
            Log.d("","ingrlistsize direct : " + ingredientsList.size());
            Log.d("","steplistsize frm recipelist b4 if: " + recipe.getStepList().size());
            Log.d("","ingrlistsize frm recipelist b4 if: " + recipe.getIngredientList().size());

            Intent intent= new Intent(AssistantRecipeListActivity.this, AssistantActivity.class);
            Log.d("","steplistsize frm recipelist: " + recipe.getStepList().size());
            Log.d("","ingrlistsize frm recipelist: " + recipe.getIngredientList().size());
            intent.putExtra("recipe", (Serializable) recipe);
            startActivity(intent);
        }//end onPostExecute
    }
}
