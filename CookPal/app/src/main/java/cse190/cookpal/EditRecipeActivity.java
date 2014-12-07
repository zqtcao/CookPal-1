package cse190.cookpal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;


public class EditRecipeActivity extends BaseDrawerActivity {
    final Context thisContext = this;
    Recipe currentRecipe;
    HttpUtil httpUtil;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        httpUtil = new HttpUtil();
        setContentView(R.layout.activity_edit_recipe);

        Intent intent = getIntent();
        currentRecipe = (Recipe)intent.getSerializableExtra("recipe");

        ((EditText)findViewById(R.id.editRecipeNameInput)).setText(currentRecipe.getRecipeName());
        ((EditText)findViewById(R.id.editRecipeImageUrlInput)).setText(currentRecipe.getImgUrl());

        ArrayList<Step> stepList = currentRecipe.getStepList();
        Log.d("s", "steplistsize: " + stepList.size());
        ArrayAdapter<Step> stepListAdapter = new StepListAdapter(stepList);
        ListView instructionsListView = (ListView) findViewById(R.id.instructionsEditListView);
        instructionsListView.setAdapter(stepListAdapter);

        ArrayList<Ingredients> ingredientList = currentRecipe.getIngredientList();
        Log.d("s", "ingrlistsize: " + ingredientList.size());

        ArrayAdapter<Ingredients> ingredientsListAdapter = new IngredientsListAdapter(ingredientList);
        ListView ingredientsListView = (ListView) findViewById(R.id.ingredientsEditListView);
        ingredientsListView.setAdapter(ingredientsListAdapter);

        Button updateRecipeButton = (Button) findViewById(R.id.updateRecipeButton);
        updateRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //delete
                HashMap<String,String> deleteRecipeParams = new HashMap<String,String>();
                deleteRecipeParams.put("r_name", currentRecipe.getRecipeName());
                deleteRecipeParams.put("fb_id", AccountActivity.getFbId());
                deleteRecipeParams.put("filter", "delete_recipe");
                httpUtil.makeHttpPost(deleteRecipeParams);
           /*     try {
                    Thread.sleep(20000);                 //1000 milliseconds is one second.
                } catch(InterruptedException ex) {
                    Log.d("thread sleep fail", "threadsleepfail");
                    Thread.currentThread().interrupt();
                }*/

                String newRecipeName = ((EditText) findViewById(R.id.editRecipeNameInput)).getText().toString();
                String newImageUrl = ((EditText) findViewById(R.id.editRecipeImageUrlInput)).getText().toString();

                //insert recipe
                HashMap<String,String> insertRecipeParams = new HashMap<String,String>();
                insertRecipeParams.put("r_name", newRecipeName);
                insertRecipeParams.put("fb_id", AccountActivity.getFbId());
                insertRecipeParams.put("filter", "insert_recipe");
                insertRecipeParams.put("cookbook_type", "private");
                insertRecipeParams.put("image_url", newImageUrl);
                httpUtil.makeHttpPost(insertRecipeParams);

                ListView instructionsEditListView = (ListView) findViewById(R.id.instructionsEditListView);
                Log.d("instrcount","instructions count: " + instructionsEditListView.getCount());


                //***************BELOW CODE CURRENTLY DOES NOTHING BECAUSE HTTP MAKE POST COMMENTED OUT*******
                //*****************************************************************************
                //loop through and insert instructions
                for(int i = 0;  i<instructionsEditListView.getCount(); i++ ) {
                    //instructionLayout has many horizontal linearlayout as children, who each have children containing EditText
                    View horizontalView = instructionsEditListView.getChildAt(i);

                    //loop throuhg view's children to find EditTexts

                    ViewGroup horizontalViewGroup = (ViewGroup)horizontalView;
                    TextView instructionNumView = (TextView)horizontalViewGroup.findViewById(R.id.stepNoTextView);
                    EditText instructionTitleEditText = (EditText) horizontalViewGroup.findViewById(R.id.instructionTitleEditText);
                    EditText instructionEditText = (EditText) horizontalViewGroup.findViewById(R.id.instructionEditText);
                    EditText instructionHoursEditText = (EditText) horizontalViewGroup.findViewById(R.id.hoursEditText);
                    EditText instructionMinsEditText = (EditText) horizontalViewGroup.findViewById(R.id.minutesEditText);
                    Log.d("AddRecipeActivity", "instr: " + instructionEditText.getText().toString() + " hrs: " + instructionHoursEditText.getText().toString() + " mins: " + instructionMinsEditText.getText().toString());
                    //TODO: INSERT INSTRUCTION

                    HashMap<String,String> insertRecipeInstructionParams = new HashMap<String,String>();
                    insertRecipeInstructionParams.put("name", newRecipeName);
                    insertRecipeInstructionParams.put("fb_id", AccountActivity.getFbId());
                    insertRecipeInstructionParams.put("instruction", instructionTitleEditText.getText().toString());
                    insertRecipeInstructionParams.put("description", instructionEditText.getText().toString());
                    insertRecipeInstructionParams.put("hrs", instructionHoursEditText.getText().toString());
                    insertRecipeInstructionParams.put("mins", instructionMinsEditText.getText().toString());
                    insertRecipeInstructionParams.put("step_no", instructionNumView.getText().toString().substring(0, instructionNumView.getText().toString().length() -1));
                    insertRecipeInstructionParams.put("filter", "insert_instruction");

                    httpUtil.makeHttpPost(insertRecipeInstructionParams);


                } //end for

                //insert ingredients
                ListView ingredientsEditListView = (ListView) findViewById(R.id.ingredientsEditListView);
                Log.d("instrcount","instructions count: " + ingredientsEditListView.getCount());

                //loop through and insert ingredients
                for(int i = 0; i < ingredientsEditListView.getCount(); i++) {
                    //ingredientsLayout has many horizontal linearlayout as children, who each have children containing EditText
                    View horizontalView = ingredientsEditListView.getChildAt(i);

                    //loop throuhg view's children to find EditTexts
                    ViewGroup horizontalViewGroup = (ViewGroup)horizontalView;
                    EditText ingredientEditText = (EditText) horizontalViewGroup.findViewById(R.id.ingredientEditText);
                    EditText ingredientQuantityEditText = (EditText) horizontalViewGroup.findViewById(R.id.quantityEditText);
                    Log.d("AddRecipeActivity", "ingred: " + ingredientEditText.getText().toString() + " quantity: " + ingredientQuantityEditText.getText().toString());
                    //TODO: INSERT INGREDIENT
                    HashMap<String,String> insertIngredientParams = new HashMap<String,String>();
                    insertIngredientParams.put("name", newRecipeName);
                    insertIngredientParams.put("fb_id", AccountActivity.getFbId());
                    insertIngredientParams.put("ingr_name", ingredientEditText.getText().toString());
                    insertIngredientParams.put("quantity",ingredientQuantityEditText.getText().toString());
                    insertIngredientParams.put("filter", "insert_ingredient");
                    httpUtil.makeHttpPost(insertIngredientParams);

                } //end for

                Intent i = new Intent(EditRecipeActivity.this, RecipeList.class);
                startActivity(i);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_recipe, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class StepListAdapter extends ArrayAdapter<Step> {


        public StepListAdapter(ArrayList<Step> stepList) {
            super(EditRecipeActivity.this, R.layout.editrecipeinstruction_listview_entry, stepList);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ArrayList<Step> stepList = currentRecipe.getStepList();


            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.editrecipeinstruction_listview_entry, parent, false);
            }
            final View thisConvertView = convertView;
            final String stepDescriptionTitle = stepList.get(position).getTitle();
            final String stepDescription = stepList.get(position).getDescription();
            EditText stepDescriptionTitleTextView = (EditText) convertView.findViewById(R.id.instructionTitleEditText);
            EditText stepDescriptionTextView = (EditText) convertView.findViewById(R.id.instructionEditText);
            //Log.d("editrecipeeacivity", "stepdesc editext: " + stepDescriptionTextView.toString());
            //Log.d("editrecipeeacivity", "stepdescription: " + stepDescription);


            TextView stepNoTextView = (TextView) convertView.findViewById(R.id.stepNoTextView);
            stepNoTextView.setText(stepList.get(position).getStepNumber() + ".");
            stepDescriptionTitleTextView.setText(stepDescriptionTitle);
            stepDescriptionTextView.setText(stepDescription);

            EditText hoursEditText = (EditText) (convertView.findViewById(R.id.hoursEditText));

            Log.d("hoursEditText", hoursEditText.toString());
            hoursEditText.setText(String.valueOf(stepList.get(position).getHours()));
            EditText minutesEditText = (EditText) convertView.findViewById(R.id.minutesEditText);
            minutesEditText.setText(String.valueOf(stepList.get(position).getMinutes()));

            return convertView;
        }
    }
    private class IngredientsListAdapter extends ArrayAdapter<Ingredients> {
        public IngredientsListAdapter(ArrayList<Ingredients> ingredientsList) {
            super(EditRecipeActivity.this, R.layout.editrecipeingredient_listview_entry, ingredientsList);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ArrayList<Ingredients> ingredientsList = currentRecipe.getIngredientList();


            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.editrecipeingredient_listview_entry, parent, false);
            }
            final View thisConvertView = convertView;
            final String name = ingredientsList.get(position).getIngredientName();
            final String quantity = ingredientsList.get(position).getQuantity();
            EditText nameEditText = (EditText) convertView.findViewById(R.id.ingredientEditText);
            EditText quantityEditText = (EditText) convertView.findViewById(R.id.quantityEditText);

            nameEditText.setText(name);
            quantityEditText.setText(quantity);

            return convertView;
        }
    }
    //sort steps

    // 2 1 3
    //1 2 3
    //1 2 3
   /* public ArrayList<Step> sortSteps (ArrayList<Step> stepList) {
        for (int i = 0; i < stepList.size(); i++) {
            for (int j = i; j < stepList.size(); j++) {
                if(stepList.get(j).getStepNumber() == i+1) {
                    //swap j with i
                    Step temp = stepList.get(j); //TODO: IMPLEMENT CLONE
                    stepList.set(j, stepList.get(i));
                    stepList.set(i,temp );
                    break; //break out inner
                }
            }
        }
    }*/
}
